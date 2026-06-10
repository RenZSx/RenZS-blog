package com.chen.blog.module.article.dao;

import com.chen.blog.module.article.dto.*;
import com.chen.blog.module.article.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.common.domain.vo.ConditionVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 文章
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Repository
public interface ArticleDao extends BaseMapper<Article> {

    /**
     * 查询首页文章
     *
     * @param current 页码
     * @param size    大小
     * @return 文章列表
     */
    List<ArticleHomeDTO> listArticles(@Param("current") Long current, @Param("size") Long size);

    /**
     * 查询首页最新文章
     *
     * @param size 展示数量
     * @return 首页最新文章列表
     */
    List<ArticleHomeDTO> listNewestHomeArticles(@Param("size") Integer size);

    /**
     * 根据分类查询首页文章
     *
     * @param categoryId 分类id
     * @param size       展示数量
     * @return 分类文章列表
     */
    List<ArticleHomeDTO> listHomeArticlesByCategory(@Param("categoryId") Integer categoryId, @Param("size") Integer size);

    /**
     * 根据id查询文章
     *
     * @param articleId 文章id
     * @return 文章信息
     */
    ArticleDTO getArticleById(@Param("articleId") Integer articleId);

    /**
     * 根据条件查询文章
     *
     * @param current   页码
     * @param size      大小
     * @param condition 条件
     * @return 文章列表
     */
    List<ArticlePreviewDTO> listArticlesByCondition(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionVO condition);

    /**
     * 查询后台文章
     *
     * @param current   页码
     * @param size      大小
     * @param condition 条件
     * @return 文章列表
     */
    List<ArticleBackDTO> listArticleBacks(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionVO condition);

    /**
     * 查询后台文章总量
     *
     * @param condition 条件
     * @return 文章总量
     */
    Integer countArticleBacks(@Param("condition") ConditionVO condition);

    /**
     * 查看文章的推荐文章
     *
     * @param articleId 文章id
     * @return 文章列表
     */
    List<ArticleRecommendDTO> listRecommendArticles(@Param("articleId") Integer articleId);

    /**
     * 文章统计
     *
     * @return {@link List<ArticleStatisticsDTO>} 文章统计结果
     */
    List<ArticleStatisticsDTO> listArticleStatistics();

    /**
     * 根据状态查询文章
     * @return Article
     */
    Article getArticleByStatus();

    /**
     * 高级搜索文章
     *
     * @param current   页码
     * @param size      大小
     * @param condition 条件
     * @return 文章预览列表
     */
    List<ArticlePreviewDTO> searchArticles(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionVO condition);

    /**
     * 高级搜索文章总数
     *
     * @param condition 条件
     * @return 文章总数
     */
    Integer countSearchArticles(@Param("condition") ConditionVO condition);
}

