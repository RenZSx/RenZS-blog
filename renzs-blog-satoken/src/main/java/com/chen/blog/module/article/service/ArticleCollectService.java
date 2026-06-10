package com.chen.blog.module.article.service;

import com.chen.blog.module.article.dto.ArticleCollectDTO;
import com.chen.blog.common.domain.vo.PageResult;

/**
 * 文章收藏服务
 *
 * @author chen
 * @date 2026/05/09
 */
public interface ArticleCollectService {

    /**
     * 收藏文章
     *
     * @param articleId 文章id
     */
    void collectArticle(Integer articleId);

    /**
     * 取消收藏文章
     *
     * @param articleId 文章id
     */
    void cancelCollectArticle(Integer articleId);

    /**
     * 查看收藏文章列表
     *
     * @return 收藏文章列表
     */
    PageResult<ArticleCollectDTO> listCollects();

}

