package com.chen.blog.module.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章阅读历史dto
 *
 * @author chen
 * @date 2026/05/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleHistoryDTO {

    /**
     * 阅读历史id
     */
    private Integer id;

    /**
     * 文章id
     */
    private Integer articleId;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章封面
     */
    private String articleCover;

    /**
     * 阅读进度百分比
     */
    private Integer progressPercent;

    /**
     * 最后阅读时间
     */
    private LocalDateTime lastReadTime;

}
