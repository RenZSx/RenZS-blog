package com.chen.blog.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chen.blog.common.annotation.OptLog;
import com.chen.blog.common.util.IpUtils;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.log.dao.OperationLogDao;
import com.chen.blog.module.log.entity.OperationLog;
import com.chen.blog.module.user.dto.UserDetailDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面处理
 * <p>
 * 加固点:
 * <ul>
 *   <li>注解缺失或取值为空时降级处理,不再抛空指针</li>
 *   <li>未登录请求照常记录,用户信息留空而非抛 {@code NO_LOGIN}</li>
 *   <li>{@link MultipartFile} 参数只记录文件名/大小/类型,不序列化文件内容</li>
 *   <li>密码、令牌等敏感字段统一脱敏后再入库</li>
 *   <li>日志写入失败不影响业务返回</li>
 * </ul>
 *
 * @author chenfuyun
 * @date 2021/07/27
 */
@Slf4j
@Aspect
@Component
public class OptLogAspect {

    /**
     * 脱敏后的占位符
     */
    private static final String MASK = "******";

    /**
     * 需要脱敏的字段名(小写包含匹配)
     */
    private static final String[] SENSITIVE_KEYS = {"password", "token", "secret", "openid"};

    /**
     * 单条日志内容最大长度,超出截断,避免大报文撑爆日志表
     */
    private static final int MAX_CONTENT_LENGTH = 20000;

    /**
     * 匿名访问时的用户昵称
     */
    private static final String ANONYMOUS_NICKNAME = "匿名用户";

    /**
     * 匿名访问时的用户id占位值
     * <p>
     * {@code tb_operation_log.user_id} 为 NOT NULL,匿名请求(友链申请等)无法取到登录用户,
     * 用 0 作为哨兵值,避免插入失败导致日志丢失。
     */
    private static final Integer ANONYMOUS_USER_ID = 0;

    @Autowired
    private OperationLogDao operationLogDao;

    /**
     * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
     */
    @Pointcut("@annotation(com.chen.blog.common.annotation.OptLog)")
    public void optLogPointCut() {}

    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     * @param keys      返回结果
     */
    @AfterReturning(value = "optLogPointCut()", returning = "keys")
    public void saveOptLog(JoinPoint joinPoint, Object keys) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return;
            }
            HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            if (request == null) {
                return;
            }
            OperationLog operationLog = buildOperationLog(joinPoint, keys, request);
            operationLogDao.insert(operationLog);
        } catch (Exception e) {
            // 日志记录属于旁路逻辑,任何异常都不应影响业务请求的正常返回
            log.warn("记录操作日志失败: {}", e.getMessage());
        }
    }

    /**
     * 组装操作日志
     *
     * @param joinPoint 切入点
     * @param keys      返回结果
     * @param request   当前请求
     * @return {@link OperationLog} 操作日志
     */
    private OperationLog buildOperationLog(JoinPoint joinPoint, Object keys, HttpServletRequest request) {
        OperationLog operationLog = new OperationLog();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> declaringType = signature.getDeclaringType();
        Api api = declaringType.getAnnotation(Api.class);
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        OptLog optLog = method.getAnnotation(OptLog.class);

        // 操作模块:优先取 @Api 的 tags,缺失时降级为类名
        operationLog.setOptModule(api != null && api.tags() != null && api.tags().length > 0
                ? api.tags()[0] : declaringType.getSimpleName());
        // 操作类型
        operationLog.setOptType(optLog == null ? "" : optLog.optType());
        // 操作描述:优先取 @ApiOperation 的 value,为空时降级为方法名
        String optDesc = apiOperation == null ? "" : apiOperation.value();
        operationLog.setOptDesc(isBlank(optDesc) ? method.getName() : optDesc);
        // 请求方法
        operationLog.setOptMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
        // 请求方式
        operationLog.setRequestMethod(request.getMethod());
        // 请求URL
        operationLog.setOptUrl(request.getRequestURI());
        // 请求参数(文件与敏感字段已处理)
        operationLog.setRequestParam(truncate(sanitize(JSON.toJSONString(buildSafeArgs(joinPoint.getArgs())))));
        // 返回结果(敏感字段已处理)
        operationLog.setResponseData(truncate(sanitize(JSON.toJSONString(keys))));

        // 请求用户:未登录(如留言、举报、友链申请等匿名接口)时不记录真实用户,而不是中断请求
        UserDetailDTO loginUser = UserUtils.getLoginUserQuietly();
        if (loginUser != null) {
            operationLog.setUserId(loginUser.getId());
            operationLog.setNickname(loginUser.getNickname());
        } else {
            operationLog.setUserId(ANONYMOUS_USER_ID);
            operationLog.setNickname(ANONYMOUS_NICKNAME);
        }

        // 请求IP
        String ipAddress = IpUtils.getIpAddress(request);
        operationLog.setIpAddress(ipAddress);
        operationLog.setIpSource(IpUtils.getIpSource(ipAddress));
        return operationLog;
    }

    /**
     * 构造可安全序列化的参数数组
     * <p>
     * 文件参数只保留元信息,请求/响应对象替换为类名,避免序列化出二进制内容或不可序列化的对象。
     *
     * @param args 原参数
     * @return {@link Object[]} 可安全序列化的参数
     */
    private Object[] buildSafeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }
        Object[] safeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof MultipartFile) {
                safeArgs[i] = describeFile((MultipartFile) arg);
            } else if (arg instanceof MultipartFile[]) {
                MultipartFile[] files = (MultipartFile[]) arg;
                Object[] descriptions = new Object[files.length];
                for (int j = 0; j < files.length; j++) {
                    descriptions[j] = describeFile(files[j]);
                }
                safeArgs[i] = descriptions;
            } else if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
                safeArgs[i] = arg.getClass().getSimpleName();
            } else {
                safeArgs[i] = arg;
            }
        }
        return safeArgs;
    }

    /**
     * 提取上传文件的元信息,不读取文件内容
     *
     * @param file 上传文件
     * @return {@link Map} 文件元信息
     */
    private Map<String, Object> describeFile(MultipartFile file) {
        Map<String, Object> description = new HashMap<>(4);
        if (file == null) {
            return description;
        }
        description.put("fileName", file.getOriginalFilename());
        description.put("size", file.getSize());
        description.put("contentType", file.getContentType());
        return description;
    }

    /**
     * 对 JSON 中的敏感字段做脱敏
     *
     * @param json 原始 JSON 字符串
     * @return {@link String} 脱敏后的 JSON 字符串
     */
    private String sanitize(String json) {
        if (isBlank(json)) {
            return json;
        }
        try {
            Object parsed = JSON.parse(json);
            redact(parsed);
            return JSON.toJSONString(parsed);
        } catch (Exception e) {
            // 解析失败时保留原内容,不影响日志落库
            return json;
        }
    }

    /**
     * 递归脱敏
     *
     * @param node JSON 节点
     */
    private void redact(Object node) {
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            for (String key : new ArrayList<>(object.keySet())) {
                if (isSensitive(key)) {
                    object.put(key, MASK);
                } else {
                    redact(object.get(key));
                }
            }
        } else if (node instanceof JSONArray) {
            for (Object item : (JSONArray) node) {
                redact(item);
            }
        }
    }

    /**
     * 判断字段名是否需要脱敏
     *
     * @param key 字段名
     * @return true 表示需要脱敏
     */
    private boolean isSensitive(String key) {
        if (key == null) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        for (String sensitiveKey : SENSITIVE_KEYS) {
            if (lowerKey.contains(sensitiveKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 超长内容截断
     *
     * @param content 原始内容
     * @return {@link String} 截断后的内容
     */
    private String truncate(String content) {
        if (content == null || content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_CONTENT_LENGTH) + "...(内容过长已截断)";
    }

    /**
     * 判断字符串是否为空
     *
     * @param value 字符串
     * @return true 表示为空
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
