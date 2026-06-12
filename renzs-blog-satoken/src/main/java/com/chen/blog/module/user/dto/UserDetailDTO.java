package com.chen.blog.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户认证详情 DTO
 * (sa-token 迁移后不再实现 UserDetails,改为普通 POJO)
 * <p>
 * 同时声明 {@link NoArgsConstructor} 与 {@link AllArgsConstructor}:
 * <ul>
 *   <li>{@code @Builder} 默认只生成全参构造器,Jackson 反序列化时找不到无参构造器会失败</li>
 *   <li>本 DTO 会被存入 sa-token Redis Session,登录后从 Redis 取回时需要 Jackson 反序列化</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer userInfoId;
    private String email;
    private Integer loginType;
    private String username;
    private String password;
    private List<String> roleList;
    private String nickname;
    private String avatar;
    private String intro;
    private String webSite;
    private Set<Object> articleLikeSet;
    private Set<Object> commentLikeSet;
    private Set<Object> talkLikeSet;
    private String ipAddress;
    private String ipSource;
    private Integer isDisable;
    private String browser;
    private String os;
    private LocalDateTime lastLoginTime;
    /**
     * 当前资料账号是否已绑定邮箱登录凭证。
     */
    private Boolean emailBound;
    /**
     * 当前资料账号是否已绑定QQ登录凭证。
     */
    private Boolean qqBound;
}
