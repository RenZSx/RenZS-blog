package com.chen.blog.module.friendLink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.module.friendLink.dto.FriendLinkBackDTO;
import com.chen.blog.module.friendLink.dto.FriendLinkDTO;
import com.chen.blog.module.friendLink.entity.FriendLink;
import com.chen.blog.module.friendLink.vo.FriendLinkReviewVO;
import com.chen.blog.module.friendLink.vo.FriendLinkVO;

import java.util.List;

/**
 * 友链服务
 *
 * @author chenfuyun
 * @date 2021/07/29
 */
public interface FriendLinkService extends IService<FriendLink> {

    /**
     * 查看友链列表
     *
     * @return 友链列表
     */
    List<FriendLinkDTO> listFriendLinks();

    /**
     * 查看后台友链列表
     *
     * @param condition 条件
     * @return 友链列表
     */
    PageResult<FriendLinkBackDTO> listFriendLinkDTO(ConditionVO condition);

    /**
     * 保存或更新友链
     *
     * @param friendLinkVO 友链
     */
    void saveOrUpdateFriendLink(FriendLinkVO friendLinkVO);

    /**
     * 提交友链申请。
     *
     * @param friendLinkVO 友链申请信息
     */
    void applyFriendLink(FriendLinkVO friendLinkVO);

    /**
     * 审核友链申请。
     *
     * @param friendLinkReviewVO 审核信息
     */
    void reviewFriendLink(FriendLinkReviewVO friendLinkReviewVO);

}
