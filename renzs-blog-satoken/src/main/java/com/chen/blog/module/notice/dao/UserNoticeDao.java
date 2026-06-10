package com.chen.blog.module.notice.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.notice.entity.UserNotice;
import com.chen.blog.module.notice.dto.NoticeDTO;
import com.chen.blog.module.notice.vo.NoticeQueryVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户通知
 *
 * @author chen
 * @date 2026/05/09
 */
@Repository
public interface UserNoticeDao extends BaseMapper<UserNotice> {

    /**
     * 统计通知数量
     *
     * @param userId 用户id
     * @param noticeQueryVO 通知查询条件
     * @return 通知数量
     */
    Integer countNotices(@Param("userId") Integer userId, @Param("noticeQueryVO") NoticeQueryVO noticeQueryVO);

    /**
     * 查询通知列表
     *
     * @param limitCurrent 分页偏移量
     * @param size 条数
     * @param userId 用户id
     * @param noticeQueryVO 通知查询条件
     * @return 通知列表
     */
    List<NoticeDTO> listNotices(@Param("limitCurrent") Long limitCurrent, @Param("size") Long size,
                                @Param("userId") Integer userId, @Param("noticeQueryVO") NoticeQueryVO noticeQueryVO);

    /**
     * 根据用户id和通知id查询单条通知
     *
     * @param userId 用户id
     * @param noticeId 通知id
     * @return 通知详情
     */
    NoticeDTO getNoticeByUserIdAndNoticeId(@Param("userId") Integer userId, @Param("noticeId") Integer noticeId);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户id
     * @return 未读通知数量
     */
    Integer getUnreadCount(@Param("userId") Integer userId);

    /**
     * 获取通知处理锁
     *
     * @param lockName 锁名称
     * @param timeoutSeconds 超时时间（秒）
     * @return 1-成功 0-超时 null-异常
     */
    Integer acquireNoticeLock(@Param("lockName") String lockName, @Param("timeoutSeconds") Integer timeoutSeconds);

    /**
     * 释放通知处理锁
     *
     * @param lockName 锁名称
     * @return 1-成功 0-当前会话未持有 null-异常
     */
    Integer releaseNoticeLock(@Param("lockName") String lockName);

}
