package com.chen.blog.common.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证 sa-token 关键配置存在且正确
 * <p>
 * 项目当前为纯 Header 鉴权(Authorization: Bearer xxx),不再使用 Cookie 通道。
 * 本测试守住关键配置,防止后续误改回 Cookie 模式或改错关键字段。
 */
class SaTokenConfigTest {

    @Test
    void devProfileEnablesSaTokenHeaderMode() {
        assertSaTokenConfig("application-dev.yml");
    }

    @Test
    void proProfileEnablesSaTokenHeaderMode() {
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
        assertEquals("Authorization", saToken.get("token-name"), "Header 字段名应为标准 Authorization");
        assertEquals("Bearer", saToken.get("token-prefix"), "Header 前缀应为 Bearer");
        assertEquals(2592000, saToken.get("timeout"), "超时应为 30 天");
        assertEquals(false, saToken.get("is-read-cookie"), "应关闭 Cookie 通道(纯 Header 鉴权)");
        assertEquals(true, saToken.get("is-read-header"), "应启用 Header 通道");
        assertEquals(20, saToken.get("max-login-count"), "最大登录数应为 20");
    }
}
