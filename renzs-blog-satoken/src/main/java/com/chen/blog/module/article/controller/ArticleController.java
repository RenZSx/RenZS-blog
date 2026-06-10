package com.chen.blog.module.article.controller;


import com.chen.blog.common.annotation.OptLog;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.article.dto.InitialArticleDto;
import com.chen.blog.module.article.dto.*;
import com.chen.blog.module.article.service.ArticleCollectService;
import com.chen.blog.module.article.service.ArticleHistoryService;
import com.chen.blog.module.article.service.ArticleService;
import com.chen.blog.module.article.strategy.context.ArticleImportStrategyContext;
import com.chen.blog.module.article.vo.ArticleTopVO;
import com.chen.blog.module.article.vo.ArticleVO;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.common.enums.FilePathEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.chen.blog.common.constant.OptTypeConst.*;

/**
 * 文章控制器
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Api(tags = "文章模块")
@RestController
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleCollectService articleCollectService;
    @Autowired
    private ArticleHistoryService articleHistoryService;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    @Autowired
    private ArticleImportStrategyContext articleImportStrategyContext;

    /**
     * 查看文章归档
     *
     * @return {@link Result < ArchiveDTO >} 文章归档列表
     */
    @ApiOperation(value = "查看文章归档")
    @GetMapping("/articles/archives")
    public Result<PageResult<ArchiveDTO>> listArchives() {
        return Result.ok(articleService.listArchives());
    }

    /**
     * 查看首页文章
     *
     * @return {@link Result<ArticleHomeDTO>} 首页文章列表
     */
    @ApiOperation(value = "查看首页文章")
    @GetMapping("/articles")
    public Result<List<ArticleHomeDTO>> listArticles() {
        return Result.ok(articleService.listArticles());
    }

    /**
     * 查看首页文章分组
     *
     * @return {@link Result<ArticleHomeSectionDTO>} 首页文章分组列表
     */
    @ApiOperation(value = "查看首页文章分组")
    @GetMapping("/articles/home/sections")
    public Result<List<ArticleHomeSectionDTO>> listHomeArticleSections() {
        return Result.ok(articleService.listHomeArticleSections());
    }
    
    /**
     * 页面初始化文章
     * @return InitialArticleDto
     */
    @GetMapping("/InitialArticle")
    public Result<InitialArticleDto> InitialArticle(){
        return Result.ok(articleService.InitialArticle());
    }
    /**
     * 查看后台文章
     *
     * @param conditionVO 条件
     * @return {@link Result<  ArticleBackDTO  >} 后台文章列表
     */
    @ApiOperation(value = "查看后台文章")
    @GetMapping("/admin/articles")
    public Result<PageResult<ArticleBackDTO>> listArticleBacks(ConditionVO conditionVO) {
        return Result.ok(articleService.listArticleBacks(conditionVO));
    }

    /**
     * 添加或修改文章
     *
     * @param articleVO 文章信息
     * @return {@link Result<>}
     */
    @OptLog(optType = SAVE_OR_UPDATE)
    @ApiOperation(value = "添加或修改文章")
    @PostMapping("/admin/articles")
    public Result<?> saveOrUpdateArticle(@Valid @RequestBody ArticleVO articleVO) {
        articleService.saveOrUpdateArticle(articleVO);
        return Result.ok();
    }

    /**
     * 修改文章置顶状态
     *
     * @param articleTopVO 文章置顶信息
     * @return {@link Result<>}
     */
    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改文章置顶")
    @PutMapping("/admin/articles/top")
    public Result<?> updateArticleTop(@Valid @RequestBody ArticleTopVO articleTopVO) {
        articleService.updateArticleTop(articleTopVO);
        return Result.ok();
    }

    /**
     * 恢复或删除文章
     *
     * @param deleteVO 逻辑删除信息
     * @return {@link Result<>}
     */
    @OptLog(optType = UPDATE)
    @ApiOperation(value = "恢复或删除文章")
    @PutMapping("/admin/articles")
    public Result<?> updateArticleDelete(@Valid @RequestBody DeleteVO deleteVO) {
        articleService.updateArticleDelete(deleteVO);
        return Result.ok();
    }

    /**
     * 上传文章图片
     *
     * @param file 文件
     * @return {@link Result<String>} 文章图片地址
     */
    @ApiOperation(value = "上传文章图片")
    @ApiImplicitParam(name = "file", value = "文章图片", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/articles/images")
    public Result<String> saveArticleImages(MultipartFile file) {
        String images = uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.ARTICLE.getPath());
        return Result.ok(images);
    }

    /**
     * 删除文章
     *
     * @param articleIdList 文章id列表
     * @return {@link Result<>}
     */
    @OptLog(optType = REMOVE)
    @ApiOperation(value = "物理删除文章")
    @DeleteMapping("/admin/articles")
    public Result<?> deleteArticles(@RequestBody List<Integer> articleIdList) {
        articleService.deleteArticles(articleIdList);
        return Result.ok();
    }

    /**
     * 根据id查看后台文章
     *
     * @param articleId 文章id
     * @return {@link Result<ArticleVO>} 后台文章
     */
    @ApiOperation(value = "根据id查看后台文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @GetMapping("/admin/articles/{articleId}")
    public Result<ArticleVO> getArticleBackById(@PathVariable("articleId") Integer articleId) {
        return Result.ok(articleService.getArticleBackById(articleId));
    }

    /**
     * 根据id查看文章
     *
     * @param articleId 文章id
     * @return {@link Result<  ArticleDTO  >} 文章信息
     */
    @ApiOperation(value = "根据id查看文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @GetMapping("/articles/{articleId}")
    public Result<ArticleDTO> getArticleById(@PathVariable("articleId") Integer articleId) {
        return Result.ok(articleService.getArticleById(articleId));
    }

    /**
     * 根据条件查询文章
     *
     * @param condition 条件
     * @return {@link Result< ArticlePreviewListDTO >} 文章列表
     */
    @ApiOperation(value = "根据条件查询文章")
    @GetMapping("/articles/condition")
    public Result<ArticlePreviewListDTO> listArticlesByCondition(ConditionVO condition) {
        return Result.ok(articleService.listArticlesByCondition(condition));
    }

    /**
     * 搜索文章
     *
     * @param condition 条件
     * @return {@link Result< ArticleSearchDTO >} 文章列表
     */
    @ApiOperation(value = "搜索文章")
    @GetMapping("/articles/search")
    public Result<List<ArticleSearchDTO>> listArticlesBySearch(ConditionVO condition) {
        return Result.ok(articleService.listArticlesBySearch(condition));
    }

    /**
     * 高级搜索文章
     *
     * @param condition 条件（keywords, searchType, categoryId, tagId, startTime, endTime）
     * @return 文章分页结果
     */
    @ApiOperation(value = "高级搜索文章")
    @GetMapping("/search/articles")
    public Result<PageResult<ArticlePreviewDTO>> searchArticles(ConditionVO condition) {
        return Result.ok(articleService.searchArticles(condition));
    }

    /**
     * 点赞文章
     *
     * @param articleId 文章id
     * @return {@link Result<>}
     */
    @ApiOperation(value = "点赞文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @PostMapping("/articles/{articleId}/like")
    public Result<?> saveArticleLike(@PathVariable("articleId") Integer articleId) {
        articleService.saveArticleLike(articleId);
        return Result.ok();
    }

    /**
     * 收藏文章
     *
     * @param articleId 文章id
     * @return {@link Result<>}
     */
    @ApiOperation(value = "收藏文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @PostMapping("/articles/{articleId}/collect")
    public Result<?> collectArticle(@PathVariable("articleId") Integer articleId) {
        articleCollectService.collectArticle(articleId);
        return Result.ok();
    }

    /**
     * 取消收藏文章
     *
     * @param articleId 文章id
     * @return {@link Result<>}
     */
    @ApiOperation(value = "取消收藏文章")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @DeleteMapping("/articles/{articleId}/collect")
    public Result<?> cancelCollectArticle(@PathVariable("articleId") Integer articleId) {
        articleCollectService.cancelCollectArticle(articleId);
        return Result.ok();
    }

    /**
     * 保存文章阅读历史
     *
     * @param articleId       文章id
     * @param progressPercent 阅读进度
     * @return {@link Result<>}
     */
    @ApiOperation(value = "保存文章阅读历史")
    @ApiImplicitParam(name = "articleId", value = "文章id", required = true, dataType = "Integer")
    @PostMapping("/articles/{articleId}/history")
    public Result<?> saveArticleHistory(@PathVariable("articleId") Integer articleId,
                                        @RequestParam(defaultValue = "0") Integer progressPercent) {
        articleHistoryService.saveArticleHistory(articleId, progressPercent);
        return Result.ok();
    }

    /**
     * 查看阅读历史列表
     *
     * @return {@link Result<PageResult< ArticleHistoryDTO >>}
     */
    @ApiOperation(value = "查看阅读历史列表")
    @GetMapping("/user/history")
    public Result<PageResult<ArticleHistoryDTO>> listHistorys() {
        return Result.ok(articleHistoryService.listArticleHistory());
    }

    /**
     * 删除阅读历史
     *
     * @param historyId 阅读历史id
     * @return {@link Result<>}
     */
    @ApiOperation(value = "删除阅读历史")
    @ApiImplicitParam(name = "historyId", value = "阅读历史id", required = true, dataType = "Integer")
    @DeleteMapping("/user/history/{historyId}")
    public Result<?> deleteArticleHistory(@PathVariable("historyId") Integer historyId) {
        articleHistoryService.deleteArticleHistory(historyId);
        return Result.ok();
    }

    /**
     * 查看收藏文章列表
     *
     * @return {@link Result<PageResult< ArticleCollectDTO >>}
     */
    @ApiOperation(value = "查看收藏文章列表")
    @GetMapping("/user/collects")
    public Result<PageResult<ArticleCollectDTO>> listCollects() {
        return Result.ok(articleCollectService.listCollects());
    }

    /**
     * 导出文章
     *
     * @param articleIdList 文章id列表
     * @return {@link List<String>} 文件url列表
     */
    @ApiOperation(value = "导出文章")
    @ApiImplicitParam(name = "articleIdList", value = "文章id", required = true, dataType = "List<Integer>")
    @PostMapping("/admin/articles/export")
    public Result<List<String>> exportArticles(@RequestBody List<Integer> articleIdList) {
        return Result.ok(articleService.exportArticles(articleIdList));
    }

    /**
     * 导入文章
     *
     * @param file 文件
     * @param type 文章类型
     * @return {@link Result<>}
     */
    @ApiOperation(value = "导入文章")
    @PostMapping("/admin/articles/import")
    public Result<?> importArticles(MultipartFile file, @RequestParam(required = false) String type) {
        articleImportStrategyContext.importArticles(file, type);
        return Result.ok();
    }
}

