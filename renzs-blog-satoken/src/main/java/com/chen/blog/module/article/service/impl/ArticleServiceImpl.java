package com.chen.blog.module.article.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.article.dto.InitialArticleDto;
import com.chen.blog.module.article.dao.ArticleDao;
import com.chen.blog.module.article.dao.ArticleTagDao;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.module.article.entity.ArticleTag;
import com.chen.blog.module.article.dto.*;
import com.chen.blog.module.article.service.ArticleService;
import com.chen.blog.module.article.service.ArticleTagService;
import com.chen.blog.module.article.vo.ArticleTopVO;
import com.chen.blog.module.article.vo.ArticleVO;
import com.chen.blog.module.category.dao.CategoryDao;
import com.chen.blog.module.comment.dao.CommentDao;
import com.chen.blog.module.comment.dto.CommentCountDTO;
import com.chen.blog.module.tag.dao.TagDao;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.category.entity.Category;
import com.chen.blog.module.tag.entity.Tag;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.common.enums.FileExtEnum;
import com.chen.blog.common.enums.FilePathEnum;
import com.chen.blog.module.blogInfo.service.BlogInfoService;
import com.chen.blog.module.blogInfo.vo.WebsiteConfigVO;
import com.chen.blog.module.notice.service.NoticeService;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.module.tag.service.TagService;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.article.strategy.context.SearchStrategyContext;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.CommonUtils;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.chen.blog.common.constant.CommonConst.ARTICLE_SET;
import static com.chen.blog.common.constant.CommonConst.FALSE;
import static com.chen.blog.common.constant.CommonConst.TRUE;
import static com.chen.blog.common.constant.RedisPrefixConst.*;
import static com.chen.blog.common.enums.CommentTypeEnum.ARTICLE;
import static com.chen.blog.common.enums.ArticleStatusEnum.DRAFT;
import static com.chen.blog.common.enums.ArticleStatusEnum.PUBLIC;
import static com.chen.blog.common.enums.ArticleStatusEnum.RECOMMEND;


/**
 * 文章服务
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, Article> implements ArticleService {
    /**
     * 首页每个文章分组展示数量
     */
    private static final int HOME_SECTION_ARTICLE_SIZE = 6;
    /**
     * AI总结默认系统提示词
     */
    private static final String DEFAULT_AI_SUMMARY_PROMPT = "你是博客文章总结助手。请用中文总结文章，控制在80到160字，语言自然，不要编造文章中没有的内容，适合展示在文章详情页顶部。";
    /**
     * 发送给AI模型的正文最大长度，避免长文超过模型上下文。
     */
    private static final int AI_SUMMARY_CONTENT_LIMIT = 8000;
    /**
     * AI总结状态：已生成，等待后台审核。
     */
    private static final int AI_SUMMARY_GENERATED = 1;
    /**
     * AI接口类型：Responses API。
     */
    private static final String AI_API_TYPE_RESPONSES = "responses";
    /**
     * AI接口类型：Chat Completions API。
     */
    private static final String AI_API_TYPE_CHAT_COMPLETIONS = "chat_completions";

    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleTagDao articleTagDao;
    @Autowired
    private SearchStrategyContext searchStrategyContext;
    @Autowired
    private HttpSession session;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private BlogInfoService blogInfoService;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public PageResult<ArchiveDTO> listArchives() {
        Page<Article> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        // 获取分页数据
        Page<Article> articlePage = articleDao.selectPage(page, new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getArticleTitle, Article::getArticleCover, Article::getCreateTime).orderByDesc(Article::getCreateTime)
                .eq(Article::getIsDelete, FALSE)
                .in(Article::getStatus, PUBLIC.getStatus(), RECOMMEND.getStatus()));
        List<ArchiveDTO> archiveDTOList = BeanCopyUtils.copyList(articlePage.getRecords(), ArchiveDTO.class);
        return new PageResult<>(archiveDTOList, (int) articlePage.getTotal());
    }

    @Override
    public PageResult<ArticleBackDTO> listArticleBacks(ConditionVO condition) {
        // 查询文章总量
        Integer count = articleDao.countArticleBacks(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 查询后台文章
        List<ArticleBackDTO> articleBackDTOList = articleDao.listArticleBacks(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        // 查询文章点赞量和浏览量
        Map<Object, Double> viewsCountMap = redisService.zAllScore(ARTICLE_VIEWS_COUNT);
        Map<String, Object> likeCountMap = redisService.hGetAll(ARTICLE_LIKE_COUNT);
        // 封装点赞量和浏览量
        articleBackDTOList.forEach(item -> {
            Double viewsCount = viewsCountMap.get(item.getId());
            if (Objects.nonNull(viewsCount)) {
                item.setViewsCount(viewsCount.intValue());
            }
            item.setLikeCount((Integer) likeCountMap.get(item.getId().toString()));
        });
        return new PageResult<>(articleBackDTOList, count);
    }

    @Override
    public List<ArticleHomeDTO> listArticles() {
        List<ArticleHomeDTO> articleHomeDTOList = articleDao.listArticles(PageUtils.getLimitCurrent(), PageUtils.getSize());
        fillHomeArticleStatistics(articleHomeDTOList);
        return articleHomeDTOList;
    }

    @Override
    public List<ArticleHomeSectionDTO> listHomeArticleSections() {
        List<ArticleHomeSectionDTO> sectionList = new ArrayList<>();
        List<ArticleHomeDTO> newestArticleList = articleDao.listNewestHomeArticles(HOME_SECTION_ARTICLE_SIZE);
        fillHomeArticleStatistics(newestArticleList);
        if (CollectionUtils.isNotEmpty(newestArticleList)) {
            sectionList.add(ArticleHomeSectionDTO.builder()
                    .title("最新")
                    .morePath("/archives")
                    .articleList(newestArticleList)
                    .build());
        }
        // 分类分组只展示有文章的分类，避免首页出现空卡片区域。
        categoryDao.listCategoryDTO().stream()
                .filter(item -> Objects.nonNull(item.getArticleCount()) && item.getArticleCount() > 0)
                .forEach(category -> {
                    List<ArticleHomeDTO> categoryArticleList = articleDao.listHomeArticlesByCategory(category.getId(), HOME_SECTION_ARTICLE_SIZE);
                    fillHomeArticleStatistics(categoryArticleList);
                    if (CollectionUtils.isNotEmpty(categoryArticleList)) {
                        sectionList.add(ArticleHomeSectionDTO.builder()
                                .title(category.getCategoryName())
                                .categoryId(category.getId())
                                .morePath("/categories/" + category.getId())
                                .articleList(categoryArticleList)
                                .build());
                    }
                });
        return sectionList;
    }

    /**
     * 填充首页文章统计数据
     *
     * @param articleHomeDTOList 首页文章列表
     */
    private void fillHomeArticleStatistics(List<ArticleHomeDTO> articleHomeDTOList) {
        if (CollectionUtils.isEmpty(articleHomeDTOList)) {
            return;
        }
        // 首页文章列表需要展示热度和评论数，基础 SQL 查询完成后统一补充统计数据。
        List<Integer> articleIdList = articleHomeDTOList.stream()
                .map(ArticleHomeDTO::getId)
                .collect(Collectors.toList());
        Map<Object, Double> viewsCountMap = redisService.zAllScore(ARTICLE_VIEWS_COUNT);
        Map<String, Object> likeCountMap = redisService.hGetAll(ARTICLE_LIKE_COUNT);
        Map<Integer, Integer> commentCountMap = commentDao.listCommentCountByTopicIds(articleIdList, ARTICLE.getType())
                .stream()
                .collect(Collectors.toMap(CommentCountDTO::getId, CommentCountDTO::getCommentCount));
        articleHomeDTOList.forEach(item -> {
            Double viewsCount = viewsCountMap.get(item.getId());
            item.setViewsCount(Objects.nonNull(viewsCount) ? viewsCount.intValue() : 0);
            item.setLikeCount((Integer) likeCountMap.getOrDefault(item.getId().toString(), 0));
            item.setCommentCount(commentCountMap.getOrDefault(item.getId(), 0));
        });
    }

    /**
     * 填充文章预览列表的统计数据
     *
     * @param articlePreviewDTOList 文章预览列表
     */
    private void fillArticlePreviewStatistics(List<ArticlePreviewDTO> articlePreviewDTOList) {
        if (CollectionUtils.isEmpty(articlePreviewDTOList)) {
            return;
        }
        // 分类和标签文章列表需要展示热度与评论数，统一从 Redis 和评论表补齐统计字段。
        List<Integer> articleIdList = articlePreviewDTOList.stream()
                .map(ArticlePreviewDTO::getId)
                .collect(Collectors.toList());
        Map<Object, Double> viewsCountMap = redisService.zAllScore(ARTICLE_VIEWS_COUNT);
        Map<String, Object> likeCountMap = redisService.hGetAll(ARTICLE_LIKE_COUNT);
        Map<Integer, Integer> commentCountMap = commentDao.listCommentCountByTopicIds(articleIdList, ARTICLE.getType())
                .stream()
                .collect(Collectors.toMap(CommentCountDTO::getId, CommentCountDTO::getCommentCount));
        articlePreviewDTOList.forEach(item -> {
            Double viewsCount = viewsCountMap.get(item.getId());
            item.setViewsCount(Objects.nonNull(viewsCount) ? viewsCount.intValue() : 0);
            item.setLikeCount((Integer) likeCountMap.getOrDefault(item.getId().toString(), 0));
            item.setCommentCount(commentCountMap.getOrDefault(item.getId(), 0));
        });
    }
    
    @Override
    public ArticlePreviewListDTO listArticlesByCondition(ConditionVO condition) {
        // 查询文章
        List<ArticlePreviewDTO> articlePreviewDTOList = articleDao.listArticlesByCondition(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        fillArticlePreviewStatistics(articlePreviewDTOList);
        // 搜索条件对应名(标签或分类名)
        String name;
        if (Objects.nonNull(condition.getCategoryId())) {
            name = categoryDao.selectOne(new LambdaQueryWrapper<Category>().select(Category::getCategoryName)
                    .eq(Category::getId, condition.getCategoryId())).getCategoryName();
        } else {
            name = tagService.getOne(new LambdaQueryWrapper<Tag>()
                    .select(Tag::getTagName).eq(Tag::getId, condition.getTagId())).getTagName();
        }
        return ArticlePreviewListDTO.builder().articlePreviewDTOList(articlePreviewDTOList).name(name).build();
    }

    @Override
    public ArticleDTO getArticleById(Integer articleId) {
        // 查询推荐文章
        CompletableFuture<List<ArticleRecommendDTO>> recommendArticleList = CompletableFuture.supplyAsync(() -> articleDao.listRecommendArticles(articleId));
        // 查询最新文章
        CompletableFuture<List<ArticleRecommendDTO>> newestArticleList = CompletableFuture.supplyAsync(() -> {
            List<Article> articleList = articleDao.selectList(new LambdaQueryWrapper<Article>()
                    .select(Article::getId, Article::getArticleTitle, Article::getArticleCover, Article::getCreateTime).eq(Article::getIsDelete, FALSE)
                    .in(Article::getStatus, PUBLIC.getStatus(), RECOMMEND.getStatus()).orderByDesc(Article::getId).last("limit 5"));
            return BeanCopyUtils.copyList(articleList, ArticleRecommendDTO.class);
        });
        // 查询id对应文章
        ArticleDTO article = articleDao.getArticleById(articleId);
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        // 更新文章浏览量
        updateArticleViewsCount(articleId);
        // 查询上一篇下一篇文章
        Article lastArticle = articleDao.selectOne(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getArticleTitle, Article::getArticleCover).eq(Article::getIsDelete, FALSE)
                .in(Article::getStatus, PUBLIC.getStatus(), RECOMMEND.getStatus())
                .lt(Article::getId, articleId)
                .orderByDesc(Article::getId).last("limit 1"));
        Article nextArticle = articleDao.selectOne(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getArticleTitle, Article::getArticleCover).eq(Article::getIsDelete, FALSE)
                .in(Article::getStatus, PUBLIC.getStatus(), RECOMMEND.getStatus())
                .gt(Article::getId, articleId).orderByAsc(Article::getId)
                .last("limit 1"));
        article.setLastArticle(BeanCopyUtils.copyObject(lastArticle, ArticlePaginationDTO.class));
        article.setNextArticle(BeanCopyUtils.copyObject(nextArticle, ArticlePaginationDTO.class));
        // 封装点赞量和浏览量
        Double score = redisService.zScore(ARTICLE_VIEWS_COUNT, articleId);
        if (Objects.nonNull(score)) {
            article.setViewsCount(score.intValue());
        }
        article.setLikeCount((Integer) redisService.hGet(ARTICLE_LIKE_COUNT, articleId.toString()));
        // 封装文章信息
        try {
            article.setRecommendArticleList(recommendArticleList.get());
            article.setNewestArticleList(newestArticleList.get());
        } catch (Exception e) {
            log.error(StrUtil.format("堆栈信息:{}", ExceptionUtil.stacktraceToString(e)));
        }
        return article;
    }


    @Override
    public void saveArticleLike(Integer articleId) {
        Article article = articleDao.selectById(articleId);
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        // 判断是否点赞
        String articleLikeKey = ARTICLE_USER_LIKE + UserUtils.getLoginUser().getUserInfoId();
        if (redisService.sIsMember(articleLikeKey, articleId)) {
            // 点过赞则删除文章id
            redisService.sRemove(articleLikeKey, articleId);
            // 文章点赞量-1
            redisService.hDecr(ARTICLE_LIKE_COUNT, articleId.toString(), 1L);
        } else {
            // 未点赞则增加文章id
            redisService.sAdd(articleLikeKey, articleId);
            // 文章点赞量+1
            redisService.hIncr(ARTICLE_LIKE_COUNT, articleId.toString(), 1L);
            Integer receiveUserId = article.getUserId();
            noticeService.saveLikeNotice(UserUtils.getLoginUser().getUserInfoId(), articleId, "article", receiveUserId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateArticle(ArticleVO articleVO) {
        // 查询博客配置信息
        CompletableFuture<WebsiteConfigVO> webConfig = CompletableFuture.supplyAsync(() -> blogInfoService.getWebsiteConfig());

        // 保存文章分类
        Category category = saveArticleCategory(articleVO);
        // 保存或修改文章
        Article article = BeanCopyUtils.copyObject(articleVO, Article.class);
        if (Objects.nonNull(category)) {
            article.setCategoryId(category.getId());
        }
        // 设定默认文章封面
        if (StrUtil.isBlank(article.getArticleCover())){
            try {
                article.setArticleCover(webConfig.get().getArticleCover());
            } catch (Exception e) {
                throw new BizException("设定默认文章封面失败");
            }
        }
        // 推荐文章只保留一篇，设置当前文章为推荐时将其它推荐文章恢复为公开
        if (RECOMMEND.getStatus().equals(article.getStatus())) {
            Article updateArticle = Article.builder().status(PUBLIC.getStatus()).build();
            LambdaQueryWrapper<Article> updateWrapper = new LambdaQueryWrapper<Article>()
                    .eq(Article::getIsDelete, FALSE)
                    .eq(Article::getStatus, RECOMMEND.getStatus());
            if (Objects.nonNull(article.getId())) {
                updateWrapper.ne(Article::getId, article.getId());
            }
            articleDao.update(updateArticle, updateWrapper);
        }
        article.setUserId(UserUtils.getLoginUser().getUserInfoId());
        this.saveOrUpdate(article);
        // 保存文章标签
        saveArticleTag(articleVO, article.getId());
    }

    /**
     * 保存文章分类
     *
     * @param articleVO 文章信息
     * @return {@link Category} 文章分类
     */
    private Category saveArticleCategory(ArticleVO articleVO) {
        // 判断分类是否存在
        Category category = categoryDao.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getCategoryName, articleVO.getCategoryName()));
        if (Objects.isNull(category) && !articleVO.getStatus().equals(DRAFT.getStatus())) {
            category = Category.builder().categoryName(articleVO.getCategoryName()).build();
            categoryDao.insert(category);
        }
        return category;
    }

    @Override
    public void updateArticleTop(ArticleTopVO articleTopVO) {
        // 修改文章置顶状态
        Article article = Article.builder().id(articleTopVO.getId()).isTop(articleTopVO.getIsTop()).build();
        articleDao.updateById(article);
    }

    @Override
    public void updateArticleDelete(DeleteVO deleteVO) {
        // 修改文章逻辑删除状态
        List<Article> articleList = deleteVO.getIdList().stream().map(id -> Article.builder()
                        .id(id)
                        .isTop(FALSE)
                        .isDelete(deleteVO.getIsDelete())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(articleList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteArticles(List<Integer> articleIdList) {
        // 删除文章标签关联
        articleTagDao.delete(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIdList));
        // 删除文章
        articleDao.deleteBatchIds(articleIdList);
    }

    @Override
    public List<String> exportArticles(List<Integer> articleIdList) {
        // 查询文章信息
        List<Article> articleList = articleDao.selectList(new LambdaQueryWrapper<Article>()
                .select(Article::getArticleTitle, Article::getArticleContent)
                .in(Article::getId, articleIdList));
        // 写入文件并上传
        List<String> urlList = new ArrayList<>();
        for (Article article : articleList) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(article.getArticleContent().getBytes())) {
                String url = uploadStrategyContext.executeUploadStrategy(article.getArticleTitle() + FileExtEnum.MD.getExtName(), inputStream, FilePathEnum.MD.getPath());
                urlList.add(url);
            } catch (Exception e) {
                log.error(StrUtil.format("导入文章失败,堆栈:{}", ExceptionUtil.stacktraceToString(e)));
                throw new BizException("导出文章失败");
            }
        }
        return urlList;
    }

    /**
     * 页面初始化文章
     * @return ArticleSearchDTO
     */
    @Override
    public InitialArticleDto InitialArticle() {
        Article article = articleDao.getArticleByStatus();
        return BeanCopyUtils.copyObject(article, InitialArticleDto.class);
    }

    @Override
    public List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition) {
        return searchStrategyContext.executeSearchStrategy(condition.getKeywords());
    }

    @Override
    public PageResult<ArticlePreviewDTO> searchArticles(ConditionVO condition) {
        Integer count = articleDao.countSearchArticles(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        List<ArticlePreviewDTO> articleList = articleDao.searchArticles(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        fillArticlePreviewStatistics(articleList);
        return new PageResult<>(articleList, count);
    }

    @Override
    public String generateArticleSummary(Integer articleId) {
        Article article = articleDao.selectById(articleId);
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        WebsiteConfigVO websiteConfig = blogInfoService.getWebsiteConfig();
        validateAiSummaryConfig(websiteConfig);

        String summary = requestAiSummary(article, websiteConfig);
        if (StrUtil.isBlank(summary)) {
            throw new BizException("AI未返回有效总结");
        }
        Article updateArticle = Article.builder()
                .id(articleId)
                .aiSummary(summary)
                .aiSummaryStatus(AI_SUMMARY_GENERATED)
                .aiSummaryTime(LocalDateTime.now())
                .build();
        articleDao.updateById(updateArticle);
        return summary;
    }

    @Override
    public ArticleVO getArticleBackById(Integer articleId) {
        // 查询文章信息
        Article article = articleDao.selectById(articleId);
        // 查询文章分类
        Category category = categoryDao.selectById(article.getCategoryId());
        String categoryName = null;
        if (Objects.nonNull(category)) {
            categoryName = category.getCategoryName();
        }
        // 查询文章标签
        List<String> tagNameList = tagDao.listTagNameByArticleId(articleId);
        // 封装数据
        ArticleVO articleVO = BeanCopyUtils.copyObject(article, ArticleVO.class);
        articleVO.setCategoryName(categoryName);
        articleVO.setTagNameList(tagNameList);
        return articleVO;
    }


    /**
     * 更新文章浏览量
     *
     * @param articleId 文章id
     */
    public void updateArticleViewsCount(Integer articleId) {
        // 判断是否第一次访问，增加浏览量
        Set<Integer> articleSet = CommonUtils.castSet(Optional.ofNullable(session.getAttribute(ARTICLE_SET)).orElseGet(HashSet::new), Integer.class);
        if (!articleSet.contains(articleId)) {
            articleSet.add(articleId);
            session.setAttribute(ARTICLE_SET, articleSet);
            // 浏览量+1
            redisService.zIncr(ARTICLE_VIEWS_COUNT, articleId, 1D);
        }
    }

    /**
     * 保存文章标签
     *
     * @param articleVO 文章信息
     */
    private void saveArticleTag(ArticleVO articleVO, Integer articleId) {
        // 编辑文章则删除文章所有标签
        if (Objects.nonNull(articleVO.getId())) {
            articleTagDao.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleVO.getId()));
        }
        // 添加文章标签
        List<String> tagNameList = articleVO.getTagNameList();
        if (CollectionUtils.isNotEmpty(tagNameList)) {
            // 查询已存在的标签
            List<Tag> existTagList = tagService.list(new LambdaQueryWrapper<Tag>().in(Tag::getTagName, tagNameList));
            List<String> existTagNameList = existTagList.stream().map(Tag::getTagName).collect(Collectors.toList());
            List<Integer> existTagIdList = existTagList.stream().map(Tag::getId).collect(Collectors.toList());
            // 对比新增不存在的标签
            tagNameList.removeAll(existTagNameList);
            if (CollectionUtils.isNotEmpty(tagNameList)) {
                List<Tag> tagList = tagNameList.stream().map(item -> Tag.builder().tagName(item).build()).collect(Collectors.toList());
                tagService.saveBatch(tagList);
                List<Integer> tagIdList = tagList.stream().map(Tag::getId).collect(Collectors.toList());
                existTagIdList.addAll(tagIdList);
            }
            // 提取标签id绑定文章
            List<ArticleTag> articleTagList = existTagIdList.stream().map(item -> ArticleTag.builder()
                            .articleId(articleId)
                            .tagId(item)
                            .build())
                    .collect(Collectors.toList());
            articleTagService.saveBatch(articleTagList);
        }
    }

    /**
     * 校验AI总结配置，配置来自tb_website_config.config。
     *
     * @param websiteConfig 网站配置
     */
    private void validateAiSummaryConfig(WebsiteConfigVO websiteConfig) {
        if (!Integer.valueOf(TRUE).equals(websiteConfig.getIsAiSummary())) {
            throw new BizException("AI文章总结未开启");
        }
        if (StrUtil.isBlank(websiteConfig.getAiApiUrl())) {
            throw new BizException("请先配置AI接口地址");
        }
        if (StrUtil.isBlank(websiteConfig.getAiApiKey())) {
            throw new BizException("请先配置AI接口密钥");
        }
        if (StrUtil.isBlank(websiteConfig.getAiModel())) {
            throw new BizException("请先配置AI模型名称");
        }
    }

    /**
     * 调用OpenAI兼容接口生成文章总结。
     *
     * @param article       文章信息
     * @param websiteConfig 网站AI配置
     * @return AI总结
     */
    private String requestAiSummary(Article article, WebsiteConfigVO websiteConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(websiteConfig.getAiApiKey());

        Map<String, Object> requestBody = isResponsesApi(websiteConfig)
                ? buildResponsesSummaryRequest(article, websiteConfig)
                : buildChatSummaryRequest(article, websiteConfig);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    websiteConfig.getAiApiUrl(),
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            return parseAiSummaryResponse(response.getBody(), websiteConfig);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error(StrUtil.format("AI文章总结生成失败,文章id:{},堆栈:{}", article.getId(), ExceptionUtil.stacktraceToString(e)));
            throw new BizException("AI文章总结生成失败");
        }
    }

    /**
     * 构造Chat Completions格式的总结请求体。
     *
     * @param article       文章信息
     * @param websiteConfig 网站AI配置
     * @return Chat Completions请求体
     */
    private Map<String, Object> buildChatSummaryRequest(Article article, WebsiteConfigVO websiteConfig) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", websiteConfig.getAiModel());
        requestBody.put("temperature", 0.3);
        requestBody.put("messages", buildAiSummaryMessages(article, websiteConfig));
        return requestBody;
    }

    /**
     * 构造Responses格式的总结请求体。
     *
     * @param article       文章信息
     * @param websiteConfig 网站AI配置
     * @return Responses请求体
     */
    private Map<String, Object> buildResponsesSummaryRequest(Article article, WebsiteConfigVO websiteConfig) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", websiteConfig.getAiModel());
        requestBody.put("instructions", StrUtil.blankToDefault(websiteConfig.getAiSummaryPrompt(), DEFAULT_AI_SUMMARY_PROMPT));
        requestBody.put("input", buildArticleSummaryUserPrompt(article));

        if (StrUtil.isNotBlank(websiteConfig.getAiReasoningEffort())) {
            Map<String, Object> reasoning = new LinkedHashMap<>();
            reasoning.put("effort", websiteConfig.getAiReasoningEffort());
            requestBody.put("reasoning", reasoning);
        }
        if (Integer.valueOf(TRUE).equals(websiteConfig.getAiDisableResponseStorage())) {
            requestBody.put("store", false);
        }
        return requestBody;
    }

    /**
     * 构造AI总结消息列表。
     *
     * @param article       文章信息
     * @param websiteConfig 网站AI配置
     * @return OpenAI兼容messages
     */
    private List<Map<String, String>> buildAiSummaryMessages(Article article, WebsiteConfigVO websiteConfig) {
        String systemPrompt = StrUtil.blankToDefault(websiteConfig.getAiSummaryPrompt(), DEFAULT_AI_SUMMARY_PROMPT);
        String userPrompt = buildArticleSummaryUserPrompt(article);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildAiMessage("system", systemPrompt));
        messages.add(buildAiMessage("user", userPrompt));
        return messages;
    }

    /**
     * 构造文章总结用户提示词。
     *
     * @param article 文章信息
     * @return 用户提示词
     */
    private String buildArticleSummaryUserPrompt(Article article) {
        String articleContent = normalizeArticleContent(article.getArticleContent());
        if (articleContent.length() > AI_SUMMARY_CONTENT_LIMIT) {
            articleContent = articleContent.substring(0, AI_SUMMARY_CONTENT_LIMIT);
        }
        return "文章标题：" + article.getArticleTitle() + "\n\n文章内容：\n" + articleContent;
    }

    /**
     * 构造AI消息对象。
     *
     * @param role    角色
     * @param content 内容
     * @return 消息对象
     */
    private Map<String, String> buildAiMessage(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 解析OpenAI兼容响应体。
     *
     * @param responseBody 响应JSON
     * @return AI总结文本
     */
    private String parseAiSummaryResponse(String responseBody, WebsiteConfigVO websiteConfig) {
        if (StrUtil.isBlank(responseBody)) {
            throw new BizException("AI接口返回为空");
        }
        String trimmedBody = responseBody.trim();
        if (!trimmedBody.startsWith("{")) {
            log.warn("AI接口返回非JSON内容:{}", abbreviateAiResponse(trimmedBody));
            throw new BizException("AI接口返回的不是JSON，请检查AI接口地址是否为" + getExpectedAiEndpointName(websiteConfig));
        }
        JSONObject responseJson;
        try {
            responseJson = JSON.parseObject(trimmedBody);
        } catch (JSONException e) {
            log.warn("AI接口JSON解析失败,响应片段:{}", abbreviateAiResponse(trimmedBody));
            throw new BizException("AI接口返回JSON格式不正确");
        }
        JSONObject error = responseJson.getJSONObject("error");
        if (error != null) {
            throw new BizException("AI接口返回错误：" + error.getString("message"));
        }
        if (isResponsesApi(websiteConfig)) {
            return parseResponsesSummaryContent(responseJson);
        }
        return parseChatSummaryContent(responseJson);
    }

    /**
     * 解析Chat Completions响应内容。
     *
     * @param responseJson 响应JSON
     * @return AI总结文本
     */
    private String parseChatSummaryContent(JSONObject responseJson) {
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BizException("AI接口未返回choices");
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            throw new BizException("AI接口返回格式不正确");
        }
        return StrUtil.trim(message.getString("content"));
    }

    /**
     * 解析Responses响应内容。
     *
     * @param responseJson 响应JSON
     * @return AI总结文本
     */
    private String parseResponsesSummaryContent(JSONObject responseJson) {
        String outputText = responseJson.getString("output_text");
        if (StrUtil.isNotBlank(outputText)) {
            return StrUtil.trim(outputText);
        }

        JSONArray output = responseJson.getJSONArray("output");
        if (output == null || output.isEmpty()) {
            throw new BizException("AI接口未返回output");
        }
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < output.size(); i++) {
            JSONObject outputItem = output.getJSONObject(i);
            JSONArray content = outputItem.getJSONArray("content");
            if (content == null || content.isEmpty()) {
                continue;
            }
            for (int j = 0; j < content.size(); j++) {
                JSONObject contentItem = content.getJSONObject(j);
                String text = contentItem.getString("text");
                if (StrUtil.isNotBlank(text)) {
                    contentBuilder.append(text);
                }
            }
        }
        String summary = StrUtil.trim(contentBuilder.toString());
        if (StrUtil.isBlank(summary)) {
            throw new BizException("AI接口未返回可用文本");
        }
        return summary;
    }

    /**
     * 判断当前配置是否使用Responses API。
     *
     * @param websiteConfig 网站AI配置
     * @return 是否为Responses API
     */
    private boolean isResponsesApi(WebsiteConfigVO websiteConfig) {
        String apiType = StrUtil.blankToDefault(websiteConfig.getAiApiType(), AI_API_TYPE_CHAT_COMPLETIONS);
        return AI_API_TYPE_RESPONSES.equalsIgnoreCase(apiType)
                || (StrUtil.isBlank(websiteConfig.getAiApiType()) && websiteConfig.getAiApiUrl().contains("/responses"));
    }

    /**
     * 获取当前配置期望的AI接口名称。
     *
     * @param websiteConfig 网站AI配置
     * @return 接口名称
     */
    private String getExpectedAiEndpointName(WebsiteConfigVO websiteConfig) {
        return isResponsesApi(websiteConfig) ? "responses地址" : "chat/completions地址";
    }

    /**
     * 截断AI响应体，避免日志中写入过长HTML或敏感内容。
     *
     * @param responseBody AI接口响应体
     * @return 截断后的响应片段
     */
    private String abbreviateAiResponse(String responseBody) {
        if (StrUtil.isBlank(responseBody)) {
            return "";
        }
        String compactBody = responseBody.replaceAll("\\s+", " ").trim();
        return compactBody.length() > 240 ? compactBody.substring(0, 240) + "..." : compactBody;
    }

    /**
     * 清理Markdown和HTML中影响总结的噪音。
     *
     * @param content 文章正文
     * @return 适合提交给AI模型的正文
     */
    private String normalizeArticleContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return content
                .replaceAll("(?s)```.*?```", " ")
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[#>*_`~\\-]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

}
