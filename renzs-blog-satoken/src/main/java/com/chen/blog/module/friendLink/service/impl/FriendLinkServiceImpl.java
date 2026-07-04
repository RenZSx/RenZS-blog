package com.chen.blog.module.friendLink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.module.friendLink.dao.FriendLinkDao;
import com.chen.blog.module.friendLink.dto.FriendLinkBackDTO;
import com.chen.blog.module.friendLink.dto.FriendLinkDTO;
import com.chen.blog.module.friendLink.entity.FriendLink;
import com.chen.blog.module.friendLink.service.FriendLinkService;
import com.chen.blog.module.friendLink.vo.FriendLinkReviewVO;
import com.chen.blog.module.friendLink.vo.FriendLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 友情链接服务
 *
 * @author xiaojie
 * @date 2021/08/10
 */
@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkDao, FriendLink> implements FriendLinkService {
    /**
     * 友链审核状态: 待审核。
     */
    private static final int LINK_STATUS_PENDING = 0;

    /**
     * 友链审核状态: 已通过。
     */
    private static final int LINK_STATUS_APPROVED = 1;

    @Autowired
    private FriendLinkDao friendLinkDao;

    @Override
    public List<FriendLinkDTO> listFriendLinks() {
        // 前台只展示审核通过的友链，避免待审核申请被公开。
        List<FriendLink> friendLinkList = friendLinkDao.selectList(new QueryWrapper<FriendLink>()
                .eq("link_status", LINK_STATUS_APPROVED)
                .orderByDesc("create_time"));
        return BeanCopyUtils.copyList(friendLinkList, FriendLinkDTO.class);
    }

    @Override
    public PageResult<FriendLinkBackDTO> listFriendLinkDTO(ConditionVO condition) {
        Page<FriendLink> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        Page<FriendLink> friendLinkPage = friendLinkDao.selectPage(page, new QueryWrapper<FriendLink>()
                .like(StringUtils.isNotBlank(condition.getKeywords()), "link_name", condition.getKeywords())
                .eq(condition.getStatus() != null, "link_status", condition.getStatus())
                .orderByAsc("link_status")
                .orderByDesc("create_time"));
        List<FriendLinkBackDTO> friendLinkBackDTOList = BeanCopyUtils.copyList(friendLinkPage.getRecords(), FriendLinkBackDTO.class);
        return new PageResult<>(friendLinkBackDTOList, (int) friendLinkPage.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateFriendLink(FriendLinkVO friendLinkVO) {
        FriendLink friendLink = BeanCopyUtils.copyObject(friendLinkVO, FriendLink.class);
        // 后台手动新增的友链默认通过审核，避免管理员新增后还要二次审核。
        if (friendLink.getId() == null && friendLink.getLinkStatus() == null) {
            friendLink.setLinkStatus(LINK_STATUS_APPROVED);
        }
        this.saveOrUpdate(friendLink);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyFriendLink(FriendLinkVO friendLinkVO) {
        FriendLink friendLink = BeanCopyUtils.copyObject(friendLinkVO, FriendLink.class);
        // 公开申请不能指定 ID 和状态，统一进入待审核队列。
        friendLink.setId(null);
        friendLink.setLinkStatus(LINK_STATUS_PENDING);
        friendLinkDao.insert(friendLink);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reviewFriendLink(FriendLinkReviewVO friendLinkReviewVO) {
        FriendLink friendLink = FriendLink.builder()
                .id(friendLinkReviewVO.getId())
                .linkStatus(friendLinkReviewVO.getLinkStatus())
                .build();
        friendLinkDao.updateById(friendLink);
    }

}
