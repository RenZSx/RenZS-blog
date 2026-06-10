package com.chen.blog.module.article.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.article.entity.ArticleCollect;
import com.chen.blog.module.article.dto.ArticleCollectDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文章收藏
 *
 * @author chen
 * @date 2026/05/09
 */
@Repository
public interface ArticleCollectDao extends BaseMapper<ArticleCollect> {

    /**
     * 查询用户收藏数量
     *
     * @param userId 用户id
     * @return 收藏数量
     */
    Integer countCollects(@Param("userId") Integer userId);

    /**
     * 查询用户收藏列表
     *
     * @param limitCurrent 分页偏移量
     * @param size         分页大小
     * @param userId       用户id
     * @return 收藏列表
     */
    List<ArticleCollectDTO> listCollects(@Param("limitCurrent") Long limitCurrent, @Param("size") Long size,
                                         @Param("userId") Integer userId);

}
