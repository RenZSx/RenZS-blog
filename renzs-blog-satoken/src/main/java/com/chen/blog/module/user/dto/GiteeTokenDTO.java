package com.chen.blog.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gitee token信息
 *
 * @author chen
 * @date 2023/12/10
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GiteeTokenDTO {
    /**
     * 微博uid
     */
    private String uid;
    /**
     * 访问令牌
     */
    private String access_token;

    /**
     * token类型
     */
    private String token_type;

    /**
     * 刷新令牌
     */
    private String refresh_token;

    /**
     * 过期时间
     */
    private Integer expires_in;

    /**
     * 作用域
     */
    private String scope;

}