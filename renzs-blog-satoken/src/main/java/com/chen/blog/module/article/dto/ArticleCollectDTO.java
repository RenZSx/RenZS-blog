package com.chen.blog.module.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章收藏dto
 *
 * @author chen
 * @date 2026/05/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCollectDTO {

    /**
     * 收藏id
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
     * 收藏时间
     */
    private LocalDateTime createTime;

}
