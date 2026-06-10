package com.chen.blog.module.notice.websocket;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.chen.blog.module.notice.dto.NoticeDTO;
import com.chen.blog.module.notice.dto.NoticeSocketMessageDTO;
import com.chen.blog.module.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 通知 websocket 服务
 *
 * @author chen
 * @date 2026/05/10
 */
@Slf4j
@Service
@ServerEndpoint(value = "/notice-websocket/{userId}", configurator = NoticeWebSocketServiceImpl.NoticeConfigurator.class)
public class NoticeWebSocketServiceImpl {

    /**
     * 握手阶段写入会话属性的 key，后续 open 阶段直接读取，避免依赖线程态的安全上下文。
     */
    private static final String AUTH_USER_ID_KEY = "notice-auth-user-id";
    /**
     * 握手阶段同步记录 http session id，便于定位 websocket 是否拿到了正确的登录会话。
     */
    private static final String AUTH_HTTP_SESSION_ID_KEY = "notice-auth-http-session-id";

    /**
     * 维护用户到会话集合的映射，保证同一用户多个标签页都能收到推送。
     */
    private static final ConcurrentHashMap<Integer, CopyOnWriteArraySet<Session>> USER_SESSION_MAP =
            new ConcurrentHashMap<>();

    private static NoticeService noticeService;

    @Autowired
    public void setNoticeService(NoticeService noticeService) {
        NoticeWebSocketServiceImpl.noticeService = noticeService;
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param userId  用户id
     * @param session websocket会话
     * @throws IOException io异常
     */
    @OnOpen
    public void onOpen(@PathParam("userId") Integer userId, Session session, EndpointConfig endpointConfig) {
        if (Objects.isNull(userId) || Objects.isNull(session) || Objects.isNull(noticeService)) {
            closeQuietly(session);
            return;
        }
        // 握手阶段放入的是 endpoint 配置属性，这里直接从 EndpointConfig 读取，避免依赖 Session 运行态属性复制行为。
        Integer authUserId = getAuthUserId(endpointConfig);
        String httpSessionId = getHttpSessionId(endpointConfig);
        log.info("通知 websocket open，pathUserId={}, authUserId={}, httpSessionId={}, wsSessionId={}",
                userId, authUserId, httpSessionId, session.getId());
        if (!Objects.equals(authUserId, userId)) {
            log.warn("通知 websocket 鉴权失败，pathUserId={}, authUserId={}", userId, authUserId);
            try {
                sendMessage(session, NoticeSocketMessageDTO.builder()
                        .type("auth_failed")
                        .unreadCount(null)
                        .notice(null)
                        .build());
            } catch (IOException ignored) {
            }
            closeWithReason(session, CloseReason.CloseCodes.VIOLATED_POLICY, "auth_failed");
            return;
        }
        // 连接建立后立即同步一次服务端权威未读数，避免多标签页本地状态漂移。
        try {
            Integer unreadCount = noticeService.getUnreadCountByUserId(userId);
            sendMessage(session, NoticeSocketMessageDTO.builder()
                    .type("notice_init")
                    .unreadCount(unreadCount)
                    .notice(null)
                    .build());
            USER_SESSION_MAP.computeIfAbsent(userId, key -> new CopyOnWriteArraySet<>()).add(session);
        } catch (IOException exception) {
            closeQuietly(session);
        }
    }

    /**
     * 当前通知通道只做服务端主动推送，暂不接收客户端业务消息。
     */
    @OnMessage
    public void onMessage(String message, Session session) {
    }

    /**
     * 连接关闭调用的方法
     *
     * @param userId  用户id
     * @param session websocket会话
     */
    @OnClose
    public void onClose(@PathParam("userId") Integer userId, Session session) {
        removeSession(userId, session);
    }

    /**
     * 异常关闭时清理失效会话，避免在线用户集合和真实连接状态不一致。
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        closeQuietly(session);
        removeSession(session);
    }

    /**
     * 获取当前在线的用户id列表
     *
     * @return 在线用户id列表
     */
    public static List<Integer> listOnlineUserIds() {
        return new ArrayList<>(USER_SESSION_MAP.keySet());
    }

    /**
     * 推送新通知创建事件
     *
     * @param userId      用户id
     * @param unreadCount 未读数量
     * @param noticeDTO   通知内容
     */
    public static void sendNoticeCreated(Integer userId, Integer unreadCount, NoticeDTO noticeDTO) {
        if (Objects.isNull(userId) || Objects.isNull(unreadCount)) {
            log.warn("通知 websocket 推送跳过，userId={}, unreadCount={}, noticeId={}",
                    userId, unreadCount, Objects.nonNull(noticeDTO) ? noticeDTO.getId() : null);
            return;
        }
        log.info("通知 websocket 准备推送，userId={}, unreadCount={}, noticeId={}, noticeType={}",
                userId, unreadCount,
                Objects.nonNull(noticeDTO) ? noticeDTO.getId() : null,
                Objects.nonNull(noticeDTO) ? noticeDTO.getNoticeType() : null);
        sendToUser(userId, NoticeSocketMessageDTO.builder()
                .type("notice_created")
                .unreadCount(unreadCount)
                .notice(noticeDTO)
                .build());
    }

    private static void sendToUser(Integer userId, NoticeSocketMessageDTO messageDTO) {
        CopyOnWriteArraySet<Session> sessionSet = USER_SESSION_MAP.get(userId);
        if (Objects.isNull(sessionSet) || sessionSet.isEmpty()) {
            log.warn("通知 websocket 未找到在线会话，userId={}, messageType={}",
                    userId, Objects.nonNull(messageDTO) ? messageDTO.getType() : null);
            return;
        }
        log.info("通知 websocket 开始广播，userId={}, sessionCount={}, messageType={}",
                userId, sessionSet.size(),
                Objects.nonNull(messageDTO) ? messageDTO.getType() : null);
        for (Session session : sessionSet) {
            if (Objects.isNull(session) || !session.isOpen()) {
                log.warn("通知 websocket 会话不可用，userId={}, wsSessionId={}",
                        userId, Objects.nonNull(session) ? session.getId() : null);
                removeSession(userId, session);
                continue;
            }
            try {
                sendMessage(session, messageDTO);
                log.info("通知 websocket 推送成功，userId={}, wsSessionId={}, messageType={}",
                        userId, session.getId(),
                        Objects.nonNull(messageDTO) ? messageDTO.getType() : null);
            } catch (IOException e) {
                // 发送失败时清理失效会话，避免后续广播一直命中脏连接。
                log.error("通知 websocket 推送失败，userId={}, wsSessionId={}, messageType={}",
                        userId, session.getId(),
                        Objects.nonNull(messageDTO) ? messageDTO.getType() : null, e);
                removeSession(userId, session);
                closeQuietly(session);
            }
        }
    }

    private static void sendMessage(Session session, NoticeSocketMessageDTO messageDTO) throws IOException {
        synchronized (session) {
            session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
        }
    }

    private static void removeSession(Integer userId, Session session) {
        if (Objects.isNull(userId) || Objects.isNull(session)) {
            return;
        }
        CopyOnWriteArraySet<Session> sessionSet = USER_SESSION_MAP.get(userId);
        if (Objects.isNull(sessionSet)) {
            return;
        }
        sessionSet.remove(session);
        if (sessionSet.isEmpty()) {
            USER_SESSION_MAP.remove(userId, sessionSet);
        }
    }

    private static void closeQuietly(Session session) {
        if (Objects.isNull(session) || !session.isOpen()) {
            return;
        }
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeWithReason(Session session, CloseReason.CloseCode code, String reason) {
        if (Objects.isNull(session) || !session.isOpen()) {
            return;
        }
        try {
            session.close(new CloseReason(code, reason));
        } catch (IOException ignored) {
        }
    }

    private static void removeSession(Session session) {
        if (Objects.isNull(session)) {
            return;
        }
        USER_SESSION_MAP.forEach((userId, sessionSet) -> {
            if (sessionSet.remove(session) && sessionSet.isEmpty()) {
                USER_SESSION_MAP.remove(userId, sessionSet);
            }
        });
    }

    /**
     * 从 websocket 握手配置中读取登录用户 id，避免在 endpoint 线程里直接依赖 sa-token 上下文。
     *
     * @param endpointConfig websocket 端点配置
     * @return 当前登录用户 id，读取失败返回 null
     */
    private Integer getAuthUserId(EndpointConfig endpointConfig) {
        if (Objects.isNull(endpointConfig)) {
            return null;
        }
        Object authUserId = endpointConfig.getUserProperties().get(AUTH_USER_ID_KEY);
        return authUserId instanceof Integer ? (Integer) authUserId : null;
    }

    /**
     * 读取握手阶段记录的 http session id，便于串联登录态与 websocket 会话。
     *
     * @param endpointConfig websocket 端点配置
     * @return http session id，读取失败返回 null
     */
    private String getHttpSessionId(EndpointConfig endpointConfig) {
        if (Objects.isNull(endpointConfig)) {
            return null;
        }
        Object httpSessionId = endpointConfig.getUserProperties().get(AUTH_HTTP_SESSION_ID_KEY);
        return httpSessionId instanceof String ? (String) httpSessionId : null;
    }

    /**
     * websocket 握手配置器，负责把 HTTP 会话中的登录用户信息搬运到 websocket 会话属性里。
     */
    public static class NoticeConfigurator extends ServerEndpointConfig.Configurator {

        /**
         * 在握手阶段读取 sa-token 登录态,并把 loginId(此处即 userInfoId)放进 websocket 用户属性。
         *
         * <p>实现要点:
         * <ol>
         *   <li>WebSocket 端点线程脱离了 Servlet 请求上下文,无法直接 StpUtil.getLoginId()</li>
         *   <li>HandshakeRequest 可以拿到 URL Query 参数,固定从 {@code ?token=xxx} 取 sa-token token 值</li>
         *   <li>用 StpUtil.getLoginIdByToken(token) 通过 token 反查 loginId(在 Redis 中持久化)</li>
         *   <li>loginId 即 userInfoId(LoginServiceImpl/AbstractSocialLoginStrategyImpl 已统一)</li>
         * </ol>
         *
         * <p>纯 Header 鉴权后,WebSocket 不再回退 Cookie 解析(浏览器/原生 WebSocket API 无法自定义 Header,
         * Query 是事实标准)。前端 {@code utils/websocket.ts} 已统一拼接 token query。
         * 同时,HttpSession 仅作诊断信息:浏览器不再带 JSESSIONID Cookie 时容器可能不创建 HttpSession,
         * 但鉴权完全依赖 Query token,不再阻塞握手。
         *
         * @param sec      websocket 配置
         * @param request  握手请求
         * @param response 握手响应
         */
        @Override
        public void modifyHandshake(ServerEndpointConfig sec,
                                    javax.websocket.server.HandshakeRequest request,
                                    HandshakeResponse response) {
            // HttpSession 仅作诊断信息:纯 Header 模式下浏览器不再带 JSESSIONID Cookie,
            // Servlet 容器可能不创建 HttpSession,但这不影响鉴权(鉴权全靠下方 Query token)。
            HttpSession httpSession = (HttpSession) request.getHttpSession();
            String httpSessionId = Objects.nonNull(httpSession) ? httpSession.getId() : "none";
            if (Objects.nonNull(httpSession)) {
                sec.getUserProperties().put(AUTH_HTTP_SESSION_ID_KEY, httpSession.getId());
            }

            // 从 Query 参数 ?token=xxx 提取 sa-token token 值 (全端统一约定)
            String tokenValue = extractSaToken(request);
            if (Objects.isNull(tokenValue)) {
                log.warn("通知 websocket 握手未获取到 sa-token (Query 参数 token 缺失)，httpSessionId={}",
                        httpSessionId);
                return;
            }

            // 通过 token 反查 loginId(即 userInfoId)
            Object loginIdObj = StpUtil.getLoginIdByToken(tokenValue);
            if (Objects.isNull(loginIdObj)) {
                log.warn("通知 websocket 握手 sa-token 无效或已过期，httpSessionId={}", httpSessionId);
                return;
            }

            Integer authUserId;
            try {
                authUserId = Integer.parseInt(loginIdObj.toString());
            } catch (NumberFormatException e) {
                log.warn("通知 websocket 握手 loginId 解析失败，loginId={}, httpSessionId={}",
                        loginIdObj, httpSessionId);
                return;
            }

            // 只保留必要的用户主键,既方便鉴权,也避免把整个 principal 暴露到 websocket 会话属性
            sec.getUserProperties().put(AUTH_USER_ID_KEY, authUserId);
            log.info("通知 websocket 握手获取登录用户成功，httpSessionId={}, authUserId={}",
                    httpSessionId, authUserId);
        }

        /**
         * 从握手请求 Query 参数 {@code ?token=xxx} 中提取 sa-token 的 token 值。
         * <p>
         * 双通道兜底:JSR-356 的 {@code HandshakeRequest.getParameterMap()} 在 Tomcat/Undertow 部分版本下
         * 并不会自动解析 query string(规范未强制要求),因此优先从 {@code getRequestURI()} 手动解析,
         * 解析不到再回退到 {@code getParameterMap()},最大程度避免容器实现差异导致的鉴权失败。
         * <p>
         * 注意:sa-token 配置了 {@code token-prefix: Bearer} 之后,Header 取值会自动剥前缀,
         * 但 Query 不会。客户端传值时不应带 "Bearer " 前缀;
         * 若传了,本方法会做兼容剥离,避免低级 bug 影响鉴权。
         *
         * @param request 握手请求
         * @return token 值,提取不到返回 null
         */
        private String extractSaToken(javax.websocket.server.HandshakeRequest request) {
            // 1. 优先从 RequestURI 手动解析 query string (规范最稳)
            java.net.URI uri = request.getRequestURI();
            if (Objects.nonNull(uri) && Objects.nonNull(uri.getRawQuery())) {
                String token = parseTokenFromQuery(uri.getRawQuery());
                if (Objects.nonNull(token)) {
                    return stripTokenPrefix(token);
                }
            }
            // 2. 兜底:容器实现填充了 parameterMap 的情况
            Map<String, List<String>> params = request.getParameterMap();
            if (Objects.nonNull(params)) {
                List<String> tokens = params.get("token");
                if (Objects.nonNull(tokens) && !tokens.isEmpty()) {
                    return stripTokenPrefix(tokens.get(0));
                }
            }
            return null;
        }

        /**
         * 从 raw query string 中提取 {@code token} 字段。
         * 使用 URLDecoder 解码,与浏览器 {@code URLSearchParams} 行为一致。
         *
         * @param rawQuery query string,不含开头的 '?'
         * @return token 值,未找到返回 null
         */
        private String parseTokenFromQuery(String rawQuery) {
            for (String pair : rawQuery.split("&")) {
                int eq = pair.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = pair.substring(0, eq);
                if (!"token".equals(key)) {
                    continue;
                }
                String rawValue = pair.substring(eq + 1);
                try {
                    return java.net.URLDecoder.decode(rawValue, "UTF-8");
                } catch (java.io.UnsupportedEncodingException ignored) {
                    return rawValue;
                }
            }
            return null;
        }

        /**
         * 兼容客户端误带 "Bearer " 前缀的场景,剥离后返回纯 token
         */
        private String stripTokenPrefix(String raw) {
            if (Objects.isNull(raw)) {
                return null;
            }
            String tokenPrefix = SaManager.getConfig().getTokenPrefix();
            if (Objects.nonNull(tokenPrefix) && !tokenPrefix.isEmpty()) {
                String prefixWithSpace = tokenPrefix + " ";
                if (raw.startsWith(prefixWithSpace)) {
                    return raw.substring(prefixWithSpace.length());
                }
            }
            return raw;
        }
    }

}
