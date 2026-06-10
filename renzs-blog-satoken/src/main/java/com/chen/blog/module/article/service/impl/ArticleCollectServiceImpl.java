package com.chen.blog.module.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.article.dao.ArticleCollectDao;
import com.chen.blog.module.article.dao.ArticleDao;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.module.article.entity.ArticleCollect;
import com.chen.blog.module.article.service.ArticleCollectService;
import com.chen.blog.module.article.dto.ArticleCollectDTO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 文章收藏服务实现
 *
 * @author chen
 * @date 2026/05/09
 */
@Service
public class ArticleCollectServiceImpl extends ServiceImpl<ArticleCollectDao, ArticleCollect> implements ArticleCollectService {
    @Autowired
    private ArticleDao articleDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Integer articleId) {
        Article article = articleDao.selectById(articleId);
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        Integer userId = UserUtils.getLoginUser().getUserInfoId();
        ArticleCollect articleCollect = this.getOne(new LambdaQueryWrapper<ArticleCollect>()
                .eq(ArticleCollect::getUserId, userId)
                .eq(ArticleCollect::getArticleId, articleId));
        if (Objects.nonNull(articleCollect)) {
            return;
        }
        this.save(ArticleCollect.builder()
                .userId(userId)
                .articleId(articleId)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCollectArticle(Integer articleId) {
        Integer userId = UserUtils.getLoginUser().getUserInfoId();
        this.remove(new LambdaQueryWrapper<ArticleCollect>()
                .eq(ArticleCollect::getUserId, userId)
                .eq(ArticleCollect::getArticleId, articleId));
    }

    @Override
    public PageResult<ArticleCollectDTO> listCollects() {
        Integer userId = UserUtils.getLoginUser().getUserInfoId();
        Integer count = baseMapper.countCollects(userId);
        if (count == 0) {
            return new PageResult<>();
        }
        List<ArticleCollectDTO> articleCollectDTOList = baseMapper.listCollects(PageUtils.getLimitCurrent(),
                PageUtils.getSize(), userId);
        return new PageResult<>(articleCollectDTOList, count);
    }

}

