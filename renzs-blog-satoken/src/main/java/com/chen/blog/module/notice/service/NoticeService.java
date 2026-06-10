package com.chen.blog.module.notice.service;

import com.chen.blog.module.comment.entity.Comment;
import com.chen.blog.module.notice.entity.UserNotice;
import com.chen.blog.module.notice.dto.NoticeDTO;
import com.chen.blog.module.notice.vo.NoticeQueryVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.module.notice.vo.SystemNoticeVO;

/**
 * 通知服务
 *
 * @author chen
 * @date 2026/05/09
 */
public interface NoticeService {

    /**
     * 查询通知列表
     *
     * @param noticeQueryVO 通知查询条件
     * @return 通知列表
     */
    PageResult<NoticeDTO> listNotices(NoticeQueryVO noticeQueryVO);

    /**
     * 获取未读通知数量
     *
     * @return 未读通知数量
     */
    Integer getUnreadCount();

    /**
     * 获取指定用户未读通知数量
     *
     * @param userId 用户id
     * @return 未读通知数量
     */
    Integer getUnreadCountByUserId(Integer userId);

    /**
     * 已读单条通知
     *
     * @param noticeId   通知id
     * @param noticeType 通知类型，系统通知传 system
     */
    void readNotice(Integer noticeId, String noticeType);

    /**
     * 全部标记已读
     */
    void readAllNotices();

    /**
     * 发布系统通知
     *
     * @param systemNoticeVO 系统通知
     * @return 新增系统通知数量
     */
    Integer publishSystemNotice(SystemNoticeVO systemNoticeVO);

    /**
     * 保存通知
     *
     * @param userNotice 用户通知
     */
    void saveNotice(UserNotice userNotice);

    /**
     * 保存点赞通知
     *
     * @param actorUserId   点赞用户id
     * @param targetId      目标id
     * @param targetType    目标类型
     * @param receiveUserId 接收通知的用户id
     */
    void saveLikeNotice(Integer actorUserId, Integer targetId, String targetType, Integer receiveUserId);

    /**
     * 保存回复通知
     *
     * @param comment 评论信息
     */
    void saveReplyNotice(Comment comment);

}

