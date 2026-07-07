package com.chen.blog.module.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.module.article.dto.InitialArticleDto;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.module.article.dto.*;
import com.chen.blog.module.article.vo.ArticleTopVO;
import com.chen.blog.module.article.vo.ArticleVO;
import com.chen.blog.common.domain.vo.*;

import java.util.List;

/**
 * 文章服务
 *
 * @author chenfuyun
 * @date 2021/07/29
 */
public interface ArticleService extends IService<Article> {

    /**
     * 查询文章归档
     *
     * @return 文章归档
     */
    PageResult<ArchiveDTO> listArchives();

    /**
     * 查询后台文章
     *
     * @param condition 条件
     * @return 文章列表
     */
    PageResult<ArticleBackDTO> listArticleBacks(ConditionVO condition);

    /**
     * 查询首页文章
     *
     * @return 文章列表
     */
    List<ArticleHomeDTO> listArticles();

    /**
     * 查询首页文章分组
     *
     * @return 首页文章分组列表
     */
    List<ArticleHomeSectionDTO> listHomeArticleSections();
    
    /**
     * 根据条件查询文章列表
     *
     * @param condition 条件
     * @return 文章列表
     */
    ArticlePreviewListDTO listArticlesByCondition(ConditionVO condition);

    /**
     * 搜索文章
     *
     * @param condition 条件
     * @return 文章列表
     */
    List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition);

    /**
     * 根据id查看后台文章
     *
     * @param articleId 文章id
     * @return 文章列表
     */
    ArticleVO getArticleBackById(Integer articleId);

    /**
     * 根据id查看文章
     *
     * @param articleId 文章id
     * @return {@link ArticleDTO} 文章信息
     */
    ArticleDTO getArticleById(Integer articleId);

    /**
     * 点赞文章
     *
     * @param articleId 文章id
     */
    void saveArticleLike(Integer articleId);

    /**
     * 添加或修改文章
     *
     * @param articleVO 文章信息
     */
    void saveOrUpdateArticle(ArticleVO articleVO);

    /**
     * 修改文章置顶
     *
     * @param articleTopVO 文章置顶信息
     */
    void updateArticleTop(ArticleTopVO articleTopVO);

    /**
     * 删除或恢复文章
     *
     * @param deleteVO 逻辑删除对象
     */
    void updateArticleDelete(DeleteVO deleteVO);

    /**
     * 物理删除文章
     *
     * @param articleIdList 文章id集合
     */
    void deleteArticles(List<Integer> articleIdList);

    /**
     * 导出文章
     *
     * @param articleIdList 文章id列表
     * @return {@link List}<{@link String}> 文件地址
     */
    List<String> exportArticles(List<Integer> articleIdList);

    /**
     * 页面初始化文章
     * @return ArticleSearchDTO
     */
    InitialArticleDto InitialArticle();

    /**
     * 高级搜索文章
     *
     * @param condition 条件
     * @return 文章分页结果
     */
    PageResult<ArticlePreviewDTO> searchArticles(ConditionVO condition);

    /**
     * 生成文章AI总结
     *
     * @param articleId 文章id
     * @return AI总结内容
     */
    String generateArticleSummary(Integer articleId);
}
