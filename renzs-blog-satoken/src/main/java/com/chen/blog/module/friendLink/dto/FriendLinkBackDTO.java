package com.chen.blog.module.friendLink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 后台友情链接
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendLinkBackDTO {
    /**
     * id
     */
    private Integer id;

    /**
     * 链接名
     */
    private String linkName;

    /**
     * 链接封面
     */
    private String linkCover;

    /**
     * 链接地址
     */
    private String linkAddress;

    /**
     * 链接介绍
     */
    private String linkIntro;

    /**
     * 审核状态 0=待审核 1=已通过 2=已拒绝
     */
    private Integer linkStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
