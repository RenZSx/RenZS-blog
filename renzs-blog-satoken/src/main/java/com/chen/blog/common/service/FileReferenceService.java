package com.chen.blog.common.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.blog.module.article.dao.ArticleDao;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.module.blogInfo.dao.WebsiteConfigDao;
import com.chen.blog.module.blogInfo.entity.WebsiteConfig;
import com.chen.blog.module.chat.dao.ChatRecordDao;
import com.chen.blog.module.chat.entity.ChatRecord;
import com.chen.blog.module.friendLink.dao.FriendLinkDao;
import com.chen.blog.module.friendLink.entity.FriendLink;
import com.chen.blog.module.message.dao.MessageDao;
import com.chen.blog.module.message.entity.Message;
import com.chen.blog.module.page.dao.PageDao;
import com.chen.blog.module.page.entity.Page;
import com.chen.blog.module.photo.dao.PhotoAlbumDao;
import com.chen.blog.module.photo.dao.PhotoDao;
import com.chen.blog.module.photo.entity.Photo;
import com.chen.blog.module.photo.entity.PhotoAlbum;
import com.chen.blog.module.talk.dao.TalkDao;
import com.chen.blog.module.talk.entity.Talk;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 查询文件地址在业务数据中的引用关系。
 *
 * <p>上传文件按内容 MD5 命名，多个业务记录可能共享同一个物理文件，删除前必须检查所有已知引用。</p>
 */
@Service
public class FileReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileReferenceService.class);
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\(\\s*<?([^\\s)>]+)>?[^)]*\\)");
    private static final Pattern HTML_DOUBLE_QUOTE_IMAGE_PATTERN = Pattern.compile("(?i)<img\\b[^>]*\\bsrc\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern HTML_SINGLE_QUOTE_IMAGE_PATTERN = Pattern.compile("(?i)<img\\b[^>]*\\bsrc\\s*=\\s*'([^']+)'");

    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private ChatRecordDao chatRecordDao;
    @Autowired
    private FriendLinkDao friendLinkDao;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private PageDao pageDao;
    @Autowired
    private PhotoDao photoDao;
    @Autowired
    private PhotoAlbumDao photoAlbumDao;
    @Autowired
    private TalkDao talkDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private WebsiteConfigDao websiteConfigDao;

    /**
     * 查询文件地址在其他业务记录中的引用。
     *
     * @param fileUrls          待检查的文件地址
     * @param excludedPhotoIds 本次正在删除的照片 ID
     * @param excludedTalkIds  本次正在删除的说说 ID
     * @param excludedArticleIds 本次正在删除的文章 ID
     * @return 仍被其他记录使用的文件地址
     */
    public Set<String> findReferencedUrls(Collection<String> fileUrls,
                                          Collection<Integer> excludedPhotoIds,
                                          Collection<Integer> excludedTalkIds,
                                          Collection<Integer> excludedArticleIds) {
        Set<String> candidates = normalize(fileUrls);
        if (candidates.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> referencedUrls = new HashSet<>();
        addMatches(referencedUrls, candidates, photoDao.selectList(photoQuery(candidates, excludedPhotoIds))
                .stream().map(Photo::getPhotoSrc).collect(Collectors.toList()), "photo");
        addMatches(referencedUrls, candidates, photoAlbumDao.selectList(new LambdaQueryWrapper<PhotoAlbum>()
                .select(PhotoAlbum::getAlbumCover)
                .in(PhotoAlbum::getAlbumCover, candidates))
                .stream().map(PhotoAlbum::getAlbumCover).collect(Collectors.toList()), "photo_album.album_cover");
        addMatches(referencedUrls, candidates, talkDao.selectList(talkQuery(candidates, excludedTalkIds))
                .stream().flatMap(talk -> parseJsonUrls(talk.getImages()).stream()).collect(Collectors.toList()), "talk.images");
        LambdaQueryWrapper<Article> articleCoverQuery = new LambdaQueryWrapper<Article>()
                .select(Article::getArticleCover)
                .in(Article::getArticleCover, candidates);
        if (excludedArticleIds != null && !excludedArticleIds.isEmpty()) {
            articleCoverQuery.notIn(Article::getId, excludedArticleIds);
        }
        addMatches(referencedUrls, candidates, articleDao.selectList(articleCoverQuery)
                .stream().map(Article::getArticleCover).collect(Collectors.toList()), "article.article_cover");
        addMatches(referencedUrls, candidates, articleDao.selectList(articleContentQuery(candidates, excludedArticleIds))
                .stream().map(Article::getArticleContent).collect(Collectors.toList()), "article.article_content");
        addMatches(referencedUrls, candidates, pageDao.selectList(new LambdaQueryWrapper<Page>()
                .select(Page::getPageCover)
                .in(Page::getPageCover, candidates))
                .stream().map(Page::getPageCover).collect(Collectors.toList()), "page.page_cover");
        addMatches(referencedUrls, candidates, friendLinkDao.selectList(new LambdaQueryWrapper<FriendLink>()
                .select(FriendLink::getLinkCover)
                .in(FriendLink::getLinkCover, candidates))
                .stream().map(FriendLink::getLinkCover).collect(Collectors.toList()), "friend_link.link_cover");
        addMatches(referencedUrls, candidates, userInfoDao.selectList(new LambdaQueryWrapper<UserInfo>()
                .select(UserInfo::getAvatar)
                .in(UserInfo::getAvatar, candidates))
                .stream().map(UserInfo::getAvatar).collect(Collectors.toList()), "user_info.avatar");
        addMatches(referencedUrls, candidates, messageDao.selectList(new LambdaQueryWrapper<Message>()
                .select(Message::getAvatar)
                .in(Message::getAvatar, candidates))
                .stream().map(Message::getAvatar).collect(Collectors.toList()), "message.avatar");
        addMatches(referencedUrls, candidates, chatRecordDao.selectList(new LambdaQueryWrapper<ChatRecord>()
                .select(ChatRecord::getAvatar)
                .in(ChatRecord::getAvatar, candidates))
                .stream().map(ChatRecord::getAvatar).collect(Collectors.toList()), "chat_record.avatar");
        addMatches(referencedUrls, candidates, websiteConfigDao.selectList(websiteConfigQuery(candidates))
                .stream().map(WebsiteConfig::getConfig).collect(Collectors.toList()), "website_config.config");
        return referencedUrls;
    }

    private LambdaQueryWrapper<Photo> photoQuery(Set<String> candidates, Collection<Integer> excludedPhotoIds) {
        LambdaQueryWrapper<Photo> query = new LambdaQueryWrapper<Photo>()
                .select(Photo::getPhotoSrc)
                .in(Photo::getPhotoSrc, candidates);
        if (excludedPhotoIds != null && !excludedPhotoIds.isEmpty()) {
            query.notIn(Photo::getId, excludedPhotoIds);
        }
        return query;
    }

    private LambdaQueryWrapper<Talk> talkQuery(Set<String> candidates, Collection<Integer> excludedTalkIds) {
        LambdaQueryWrapper<Talk> query = new LambdaQueryWrapper<Talk>()
                .select(Talk::getId, Talk::getImages);
        if (excludedTalkIds != null && !excludedTalkIds.isEmpty()) {
            query.notIn(Talk::getId, excludedTalkIds);
        }
        query.and(group -> {
            boolean first = true;
            for (String candidate : candidates) {
                if (first) {
                    group.like(Talk::getImages, candidate);
                    first = false;
                } else {
                    group.or().like(Talk::getImages, candidate);
                }
            }
        });
        return query;
    }

    private LambdaQueryWrapper<Article> articleContentQuery(Set<String> candidates,
                                                            Collection<Integer> excludedArticleIds) {
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .select(Article::getArticleContent);
        if (excludedArticleIds != null && !excludedArticleIds.isEmpty()) {
            query.notIn(Article::getId, excludedArticleIds);
        }
        query.and(group -> {
            boolean first = true;
            for (String candidate : candidates) {
                if (first) {
                    group.like(Article::getArticleContent, candidate);
                    first = false;
                } else {
                    group.or().like(Article::getArticleContent, candidate);
                }
            }
        });
        return query;
    }

    private LambdaQueryWrapper<WebsiteConfig> websiteConfigQuery(Set<String> candidates) {
        LambdaQueryWrapper<WebsiteConfig> query = new LambdaQueryWrapper<WebsiteConfig>()
                .select(WebsiteConfig::getConfig);
        boolean first = true;
        for (String candidate : candidates) {
            if (first) {
                query.like(WebsiteConfig::getConfig, candidate);
                first = false;
            } else {
                query.or().like(WebsiteConfig::getConfig, candidate);
            }
        }
        return query;
    }

    private Set<String> normalize(Collection<String> urls) {
        if (urls == null) {
            return Collections.emptySet();
        }
        return urls.stream()
                .filter(url -> url != null && !url.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * 提取 Markdown 图片和 HTML img 标签中的地址，普通文本链接不参与物理文件删除。
     */
    public Set<String> extractImageUrls(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> urls = new HashSet<>();
        collectPatternMatches(urls, content, MARKDOWN_IMAGE_PATTERN);
        collectPatternMatches(urls, content, HTML_DOUBLE_QUOTE_IMAGE_PATTERN);
        collectPatternMatches(urls, content, HTML_SINGLE_QUOTE_IMAGE_PATTERN);
        return urls;
    }

    private void collectPatternMatches(Set<String> urls, String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            for (int group = 1; group <= matcher.groupCount(); group++) {
                if (matcher.group(group) != null && !matcher.group(group).trim().isEmpty()) {
                    urls.add(matcher.group(group).trim());
                    break;
                }
            }
        }
    }

    private void addMatches(Set<String> referencedUrls, Set<String> candidates,
                            Collection<String> values, String source) {
        Set<String> matches = values.stream()
                .filter(value -> value != null && candidates.stream().anyMatch(value::contains))
                .flatMap(value -> candidates.stream().filter(value::contains))
                .collect(Collectors.toSet());
        if (!matches.isEmpty()) {
            referencedUrls.addAll(matches);
            LOGGER.info("发现文件引用，source={}, count={}, urls={}", source, matches.size(), matches);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseJsonUrls(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<?> parsedUrls = JSON.parseObject(json, List.class);
            return parsedUrls.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            LOGGER.warn("解析图片引用 JSON 失败，json={}", json, exception);
            return Collections.emptyList();
        }
    }
}
