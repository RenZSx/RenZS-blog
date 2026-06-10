package com.chen.blog.module.article.service;

import com.chen.blog.module.article.dto.ArticleHistoryDTO;
import com.chen.blog.common.domain.vo.PageResult;

/**
 * 文章阅读历史服务
 *
 * @author chen
 * @date 2026/05/09
 */
public interface ArticleHistoryService {

    /**
     * 保存阅读历史
     *
     * @param articleId       文章id
     * @param progressPercent 阅读进度
     */
    void saveArticleHistory(Integer articleId, Integer progressPercent);

    /**
     * 查看阅读历史列表
     *
     * @return 阅读历史列表
     */
    PageResult<ArticleHistoryDTO> listArticleHistory();

    /**
     * 删除阅读历史
     *
     * @param historyId 阅读历史id
     */
    void deleteArticleHistory(Integer historyId);

}

