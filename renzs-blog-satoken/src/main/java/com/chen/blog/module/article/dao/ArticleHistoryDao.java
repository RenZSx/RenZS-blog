package com.chen.blog.module.article.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.article.entity.ArticleHistory;
import com.chen.blog.module.article.dto.ArticleHistoryDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文章阅读历史
 *
 * @author chen
 * @date 2026/05/09
 */
@Repository
public interface ArticleHistoryDao extends BaseMapper<ArticleHistory> {

    /**
     * 查询用户阅读历史数量
     *
     * @param userId 用户id
     * @return 阅读历史数量
     */
    Integer countHistory(@Param("userId") Integer userId);

    /**
     * 查询用户阅读历史列表
     *
     * @param limitCurrent 分页偏移量
     * @param size         分页大小
     * @param userId       用户id
     * @return 阅读历史列表
     */
    List<ArticleHistoryDTO> listHistory(@Param("limitCurrent") Long limitCurrent, @Param("size") Long size,
                                        @Param("userId") Integer userId);

}
