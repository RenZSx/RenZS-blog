package com.chen.blog.module.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页文章分组
 *
 * @author chenfuyun
 * @date 2026/05/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleHomeSectionDTO {

    /**
     * 分组标题，例如“最新”或分类名称
     */
    private String title;

    /**
     * 分类id，最新文章分组为空
     */
    private Integer categoryId;

    /**
     * 更多文章跳转路径
     */
    private String morePath;

    /**
     * 当前分组展示的文章列表
     */
    private List<ArticleHomeDTO> articleList;
}
