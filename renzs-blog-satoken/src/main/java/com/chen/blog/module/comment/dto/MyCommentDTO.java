package com.chen.blog.module.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * "我的评论" 列表项 DTO
 * <p>
 * 用于用户中心 - "我的评论" 页:
 *   - 列出当前登录用户发的所有评论(含审核中)
 *   - 携带 topicTitle 让前端能展示"评论在《XXX》下"
 *   - 携带 type/topicId 让前端可跳回文章/说说/友链详情
 *
 * @author chen
 * @date 2026/06/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyCommentDTO {

    /**
     * 评论 ID
     */
    private Integer id;

    /**
     * 评论主题 ID (文章/说说/友链 ID,按 type 区分)
     */
    private Integer topicId;

    /**
     * 评论类型 1=文章 2=友链 3=说说 4=留言 5=关于
     */
    private Integer type;

    /**
     * 主题标题(文章标题/说说前 20 字/留言/友链链接名)
     */
    private String topicTitle;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 父评论 ID(顶级评论为 null)
     */
    private Integer parentId;

    /**
     * 被回复用户 ID(顶级评论为 null)
     */
    private Integer replyUserId;

    /**
     * 是否审核 0=待审 1=已审
     */
    private Integer isReview;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;
}
