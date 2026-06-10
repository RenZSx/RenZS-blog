package com.chen.blog.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gitee用户信息
 *
 * @author chen
 * @date 2023/12/10
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GiteeUserInfoDTO {

    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 昵称
     */
    private String name;

    /**
     * 头像
     */
    private String avatar_url;

}