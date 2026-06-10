package com.chen.blog.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gitee配置属性
 *
 * @author chen
 * @date 2023/12/10
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "gitee")
public class GiteeConfigProperties {

    /**
     * Gitee appId
     */
    private String appId;

    /**
     * Gitee appSecret
     */
    private String appSecret;

    /**
     * Gitee登录类型
     */
    private String grantType;

    /**
     * Gitee回调域名
     */
    private String redirectUrl;

    /**
     * Gitee访问令牌地址
     */
    private String accessTokenUrl;

    /**
     * Gitee用户信息地址
     */
    private String userInfoUrl;
}