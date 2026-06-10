package com.chen.blog.module.notice.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.notice.entity.SystemNoticeRead;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 系统通知已读记录
 *
 * @author chen
 * @date 2026/05/10
 */
@Repository
public interface SystemNoticeReadDao extends BaseMapper<SystemNoticeRead> {

    /**
     * 标记系统通知已读
     *
     * @param noticeId 通知id
     * @param userId   用户id
     */
    void readSystemNotice(@Param("noticeId") Integer noticeId, @Param("userId") Integer userId);

    /**
     * 标记所有系统通知已读
     *
     * @param userId 用户id
     */
    void readAllSystemNotices(@Param("userId") Integer userId);

}
