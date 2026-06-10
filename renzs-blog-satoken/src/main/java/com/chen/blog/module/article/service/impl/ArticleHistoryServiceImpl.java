package com.chen.blog.module.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.article.dao.ArticleDao;
import com.chen.blog.module.article.dao.ArticleHistoryDao;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.module.article.entity.ArticleHistory;
import com.chen.blog.module.article.service.ArticleHistoryService;
import com.chen.blog.module.article.dto.ArticleHistoryDTO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 文章阅读历史服务实现
 *
 * @author chen
 * @date 2026/05/09
 */
@Service
public class ArticleHistoryServiceImpl extends ServiceImpl<ArticleHistoryDao, ArticleHistory> implements ArticleHistoryService {
    @Autowired
    private ArticleDao articleDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveArticleHistory(Integer articleId, Integer progressPercent) {
        Integer safeProgressPercent = normalizeProgressPercent(progressPercent);
        if (safeProgressPercent < 0 || safeProgressPercent > 100) {
            throw new BizException("阅读进度必须在0到100之间");
        }
        Article article = articleDao.selectById(articleId);
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        Integer userId = UserUtils.getLoginUser().getUserInfoId();
        LocalDateTime now = LocalDateTime.now();
        ArticleHistory articleHistory = this.getOne(new LambdaQueryWrapper<ArticleHistory>()
                .eq(ArticleHistory::getUserId, userId)
                .eq(ArticleHistory::getArticleId, articleId));
        if (Objects.nonNull(articleHistory)) {
            articleHistory.setProgressPercent(safeProgressPercent);
            articleHistory.setLastReadTime(now);
            this.updateById(articleHistory);
            return;
        }
        this.save(ArticleHistory.builder()
                .userId(userId)
                .articleId(articleId)
                .progressPercent(safeProgressPercent)
                .lastReadTime(now)
                .build());
    }

    @Override
    public PageResult<ArticleHistoryDTO> listArticleHistory() {
        Integer userId = UserUtils.getLoginUser().getUserInfoId();
        Integer count = baseMapper.countHistory(userId);
        if (count == 0) {
            return new PageResult<>();
        }
        List<ArticleHistoryDTO> articleHistoryDTOList = baseMapper.listHistory(PageUtils.getLimitCurrent(),
                PageUtils.getSize(), userId);
        return new PageResult<>(articleHistoryDTOList, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticleHistory(Integer historyId) {
        Integer userId = UserUtils.getLoginUser().getUserInfoId();
        this.remove(new LambdaQueryWrapper<ArticleHistory>()
                .eq(ArticleHistory::getId, historyId)
                .eq(ArticleHistory::getUserId, userId));
    }

    /**
     * 规范化阅读进度
     *
     * @param progressPercent 阅读进度
     * @return 阅读进度
     */
    private Integer normalizeProgressPercent(Integer progressPercent) {
        if (Objects.isNull(progressPercent)) {
            return 0;
        }
        return progressPercent;
    }

}

