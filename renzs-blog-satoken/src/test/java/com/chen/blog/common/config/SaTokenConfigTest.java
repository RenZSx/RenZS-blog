package com.chen.blog.common.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证 sa-token 关键配置存在且正确
 */
class SaTokenConfigTest {

    @Test
    void devProfileEnablesSaTokenCookieMode() {
        assertSaTokenConfig("application-dev.yml");
    }

    @Test
    void proProfileEnablesSaTokenCookieMode() {
        assertSaTokenConfig("application-pro.yml");
    }

    @SuppressWarnings("unchecked")
    private void assertSaTokenConfig(String resourceName) {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(inputStream, "配置文件应存在: " + resourceName);
        Map<String, Object> root = yaml.load(inputStream);
        Map<String, Object> saToken = (Map<String, Object>) root.get("sa-token");
        assertNotNull(saToken, "应包含 sa-token 配置块");
        assertEquals("JSESSIONID", saToken.get("token-name"), "Cookie 名应沿用 JSESSIONID");
        assertEquals(2592000, saToken.get("timeout"), "超时应为 30 天");
        assertEquals(true, saToken.get("is-read-cookie"), "应启用 Cookie 模式");
        assertEquals(20, saToken.get("max-login-count"), "最大登录数应为 20");
    }
}
