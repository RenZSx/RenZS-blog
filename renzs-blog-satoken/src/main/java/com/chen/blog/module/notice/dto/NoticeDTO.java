package com.chen.blog.module.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知
 *
 * @author chen
 * @date 2026/05/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO {

    /**
     * 通知id
     */
    private Integer id;

    /**
     * 接收通知的用户id
     */
    private Integer userId;

    /**
     * 通知类型
     */
    private String noticeType;

    /**
     * 触发通知的来源id
     */
    private Integer sourceId;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 通知目标id
     */
    private Integer targetId;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 跳转路径
     */
    private String jumpPath;

    /**
     * 锚点标识
     */
    private String anchorKey;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
