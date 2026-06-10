package com.chen.blog.module.friendLink.service;

import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.module.friendLink.dto.FriendLinkBackDTO;
import com.chen.blog.module.friendLink.dto.FriendLinkDTO;
import com.chen.blog.module.friendLink.entity.FriendLink;
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

}

