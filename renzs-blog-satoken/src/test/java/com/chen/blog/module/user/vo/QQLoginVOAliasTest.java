package com.chen.blog.module.user.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QQ登录请求参数字段别名测试。
 */
class QQLoginVOAliasTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 兼容QQ回调和旧前端可能使用的下划线字段名。
     */
    @Test
    void should_read_snake_case_aliases() throws Exception {
        QQLoginVO qqLoginVO = objectMapper.readValue(
                "{\"openid\":\"qq-open-id\",\"access_token\":\"qq-access-token\"}",
                QQLoginVO.class);

        assertEquals("qq-open-id", qqLoginVO.getOpenId());
        assertEquals("qq-access-token", qqLoginVO.getAccessToken());
    }
}
