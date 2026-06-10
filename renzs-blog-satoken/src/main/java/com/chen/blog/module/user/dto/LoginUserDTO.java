package com.chen.blog.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * <p>
 * 用于账密登录与第三方 OAuth 登录的统一响应结构,
 * 同时下发用户信息 + Sa-Token 令牌信息,以支持:
 * <ul>
 *     <li>Web 端: 依赖 Cookie 鉴权,前端可忽略 token 字段</li>
 *     <li>App 端: 持久化 tokenValue 后,后续请求通过 Authorization Header 鉴权</li>
 * </ul>
 *
 * @author chen
 * @date 2026/06/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {

    /**
     * 用户信息(沿用既有 UserInfoDTO 结构)
     */
    private UserInfoDTO userInfo;

    /**
     * Token 在 Header / Cookie 中的字段名(对齐 sa-token 配置 token-name,默认 "Authorization")
     */
    private String tokenName;

    /**
     * 令牌值(UUID 字符串,前端需作为 Bearer 后缀挂在 Authorization Header,或存入 localStorage 备用)
     */
    private String tokenValue;

    /**
     * 令牌剩余有效期(秒),用于前端做提前续签或超时提醒
     */
    private Long tokenTimeout;
}
