package com.chen.blog.module.notice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户通知
 *
 * @author chen
 * @date 2026/05/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_user_notice")
public class UserNotice {

    /**
     * 通知id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 接收通知的用户id
     */
    private Integer userId;

    /**
     * 通知类型，可选值：comment_reply、talk_reply、article_like、talk_like
     */
    private String noticeType;

    /**
     * 触发通知的来源id
     */
    private Integer sourceId;

    /**
     * 来源类型，可选值：comment、article、talk
     */
    private String sourceType;

    /**
     * 通知目标id
     */
    private Integer targetId;

    /**
     * 目标类型，可选值：article、talk、comment
     */
    private String targetType;

    /**
     * 跳转路径，必须为非空字符串
     */
    private String jumpPath;

    /**
     * 锚点标识，comment_reply 场景必须提供
     */
    private String anchorKey;

    /**
     * 通知内容，必须为非空字符串
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
