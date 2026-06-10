package com.chen.blog.module.chat.webSocket;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.blog.module.chat.dao.ChatRecordDao;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.chat.dto.WebsocketMessageDTO;
import com.chen.blog.module.chat.service.impl.ChatRecordGuard;
import com.chen.blog.module.chat.dto.ChatRecordDTO;
import com.chen.blog.module.chat.dto.RecallMessageDTO;
import com.chen.blog.module.chat.entity.ChatRecord;
import com.chen.blog.common.enums.FilePathEnum;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.HTMLUtils;
import com.chen.blog.common.util.IpUtils;
import com.chen.blog.module.blogInfo.vo.VoiceVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.chen.blog.common.enums.ChatTypeEnum.*;

/**
 * websocket服务
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Data
@Service
@ServerEndpoint(value = "/websocket", configurator = WebSocketServiceImpl.ChatConfigurator.class)
public class WebSocketServiceImpl {

    /**
     * 用户session
     */
    private Session session;

    /**
     * 用户session集合
     */
    private static CopyOnWriteArraySet<WebSocketServiceImpl> webSocketSet = new CopyOnWriteArraySet<>();

    @Autowired
    public void setChatRecordDao(ChatRecordDao chatRecordDao) {
        WebSocketServiceImpl.chatRecordDao = chatRecordDao;
    }

    @Autowired
    public void setUploadStrategyContext(UploadStrategyContext uploadStrategyContext) {
        WebSocketServiceImpl.uploadStrategyContext = uploadStrategyContext;
    }

    private static ChatRecordDao chatRecordDao;

    private static UploadStrategyContext uploadStrategyContext;

    /**
     * 获取客户端真实ip
     */
    public static class ChatConfigurator extends ServerEndpointConfig.Configurator {

        public static String HEADER_NAME = "X-Real-IP";
        public static String FORWARDED_HEADER_NAME = "x-forwarded-for";

        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            try {
                String ipAddress = getFirstHeader(request, HEADER_NAME.toLowerCase());
                if (isUnknownIp(ipAddress)) {
                    ipAddress = getFirstHeader(request, FORWARDED_HEADER_NAME);
                }
                if (isUnknownIp(ipAddress)) {
                    ipAddress = getFirstHeader(request, "Proxy-Client-IP");
                }
                if (isUnknownIp(ipAddress)) {
                    ipAddress = getFirstHeader(request, "WL-Proxy-Client-IP");
                }
                if (isUnknownIp(ipAddress)) {
                    ipAddress = "";
                }
                sec.getUserProperties().put(HEADER_NAME, ipAddress);
            } catch (Exception e) {
                sec.getUserProperties().put(HEADER_NAME, "");
            }
        }

        private String getFirstHeader(HandshakeRequest request, String headerName) {
            List<String> values = request.getHeaders().get(headerName);
            if (values == null || values.isEmpty()) {
                return "";
            }
            String value = values.get(0);
            if (value != null && value.contains(",")) {
                return value.substring(0, value.indexOf(",")).trim();
            }
            return value == null ? "" : value.trim();
        }

        private boolean isUnknownIp(String ipAddress) {
            return ipAddress == null
                    || ipAddress.length() == 0
                    || "unknown".equalsIgnoreCase(ipAddress)
                    || "未知ip".equalsIgnoreCase(ipAddress)
                    || "未知IP".equalsIgnoreCase(ipAddress);
        }
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) throws IOException {
        // 加入连接
        this.session = session;
        webSocketSet.add(this);
        // 更新在线人数
        updateOnlineCount();
        // 加载历史聊天记录
        ChatRecordDTO chatRecordDTO = listChartRecords(endpointConfig);
        // 发送消息
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(HISTORY_RECORD.getType())
                .data(chatRecordDTO)
                .build();
        synchronized (session) {
            session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        WebsocketMessageDTO messageDTO = JSON.parseObject(message, WebsocketMessageDTO.class);
        switch (Objects.requireNonNull(getChatType(messageDTO.getType()))) {
            case SEND_MESSAGE:
                // 发送消息
                ChatRecord chatRecord = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), ChatRecord.class);
                fillChatRecordIp(chatRecord);
                // 标准化消息字段
                ChatRecordGuard.normalizeOutgoingRecord(chatRecord, "");
                // 过滤html标签
                chatRecord.setContent(HTMLUtils.filter(chatRecord.getContent()));
                chatRecordDao.insert(chatRecord);
                messageDTO.setData(chatRecord);
                // 广播消息
                broadcastMessage(messageDTO);
                break;
            case RECALL_MESSAGE:
                // 撤回消息
                RecallMessageDTO recallMessage = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), RecallMessageDTO.class);
                // 查询原消息
                ChatRecord storedRecord = chatRecordDao.selectById(recallMessage.getId());
                // 获取当前用户信息
                String currentIp = getSessionIp(session);
                Integer currentUserId = extractUserIdFromRecallData(messageDTO);
                // 权限校验
                if (ChatRecordGuard.canRecall(storedRecord, currentUserId, currentIp)) {
                    chatRecordDao.deleteById(recallMessage.getId());
                    // 广播消息
                    broadcastMessage(messageDTO);
                }
                break;
            case HEART_BEAT:
                // 心跳消息
                messageDTO.setData("pong");
                session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
                break;
            default:
                // 未知类型安全忽略
                break;
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        // 更新在线人数
        webSocketSet.remove(this);
        updateOnlineCount();
    }

    /**
     * 加载历史聊天记录
     *
     * @param endpointConfig 配置
     * @return 加载历史聊天记录
     */
    private ChatRecordDTO listChartRecords(EndpointConfig endpointConfig) {
        // 获取聊天历史记录
        List<ChatRecord> chatRecordList = chatRecordDao.selectList(new LambdaQueryWrapper<ChatRecord>()
                .ge(ChatRecord::getCreateTime, DateUtil.offsetHour(new Date(), -12)));
        // 获取当前用户ip
        String ipAddress = endpointConfig.getUserProperties().get(ChatConfigurator.HEADER_NAME).toString();
        return ChatRecordDTO.builder()
                .chatRecordList(chatRecordList)
                .ipAddress(ipAddress)
                .ipSource(isUnknownIp(ipAddress) ? "" : IpUtils.getIpSource(ipAddress))
                .build();
    }

    /**
     * 更新在线人数
     * <p>
     * 注:此处不能加 {@code @Async}。原始代码虽然标注了 @Async,但项目此前未启用
     * {@code @EnableAsync},注解实际未生效(且同类自调用也会绕过代理)。在 sa-token
     * 迁移启用 {@code @EnableAsync} 后,该注解会让 Spring 为本类创建 CGLIB 代理,
     * 而 {@code @ServerEndpoint} 类不允许是代理对象,会导致启动失败:
     * <pre>
     * IllegalStateException: Failed to register @ServerEndpoint class:
     *   class WebSocketServiceImpl$$EnhancerBySpringCGLIB$$xxx
     * </pre>
     * 因此保留同步语义(原本也是同步执行),不再加 @Async。
     *
     * @throws IOException io异常
     */
    public void updateOnlineCount() throws IOException {
        // 获取当前在线人数
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(ONLINE_COUNT.getType())
                .data(webSocketSet.size())
                .build();
        // 广播消息
        broadcastMessage(messageDTO);
    }

    /**
     * 发送语音
     *
     * @param voiceVO 语音路径
     */
    public void sendVoice(VoiceVO voiceVO) {
        // 上传语音文件
        String content = uploadStrategyContext.executeUploadStrategy(voiceVO.getFile(), FilePathEnum.VOICE.getPath());
        voiceVO.setContent(content);
        // 保存记录
        ChatRecord chatRecord = BeanCopyUtils.copyObject(voiceVO, ChatRecord.class);
        if (isUnknownIp(chatRecord.getIpAddress())) {
            chatRecord.setIpAddress("");
        }
        chatRecordDao.insert(chatRecord);
        // 发送消息
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(VOICE_MESSAGE.getType())
                .data(chatRecord)
                .build();
        // 广播消息
        try {
            broadcastMessage(messageDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 广播消息
     *
     * @param messageDTO 消息dto
     * @throws IOException io异常
     */
    private void broadcastMessage(WebsocketMessageDTO messageDTO) throws IOException {
        for (WebSocketServiceImpl webSocketService : webSocketSet) {
            synchronized (webSocketService.session) {
                webSocketService.session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
            }
        }
    }

    private boolean isUnknownIp(String ipAddress) {
        return ipAddress == null
                || ipAddress.length() == 0
                || "unknown".equalsIgnoreCase(ipAddress)
                || "未知ip".equalsIgnoreCase(ipAddress)
                || "未知IP".equalsIgnoreCase(ipAddress);
    }

    private void fillChatRecordIp(ChatRecord chatRecord) {
        if (chatRecord == null) {
            return;
        }
        if (isUnknownIp(chatRecord.getIpAddress())) {
            chatRecord.setIpAddress("");
        }
        if (chatRecord.getIpSource() != null && chatRecord.getIpSource().trim().length() == 0) {
            chatRecord.setIpSource("");
        }
        if (!isUnknownIp(chatRecord.getIpAddress()) && isUnknownIp(chatRecord.getIpSource())) {
            chatRecord.setIpSource(IpUtils.getIpSource(chatRecord.getIpAddress()));
        }
    }

    /**
     * 获取会话对应的 IP 地址
     */
    private String getSessionIp(Session session) {
        if (session == null || session.getUserProperties() == null) {
            return "";
        }
        Object ip = session.getUserProperties().get(ChatConfigurator.HEADER_NAME);
        return ip != null ? ip.toString() : "";
    }

    /**
     * 从撤回消息数据中提取 userId
     */
    private Integer extractUserIdFromRecallData(WebsocketMessageDTO messageDTO) {
        if (messageDTO == null || messageDTO.getData() == null) {
            return null;
        }
        try {
            String json = JSON.toJSONString(messageDTO.getData());
            return JSON.parseObject(json).getInteger("userId");
        } catch (Exception e) {
            return null;
        }
    }

}
