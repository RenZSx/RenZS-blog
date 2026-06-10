package com.chen.blog.module.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知 websocket 消息
 *
 * @author chen
 * @date 2026/05/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeSocketMessageDTO {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 当前用户未读数量
     */
    private Integer unreadCount;

    /**
     * 具体通知内容
     */
    private NoticeDTO notice;

}
