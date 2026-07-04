package com.chen.blog.module.friendLink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chen.blog.module.friendLink.dao.FriendLinkDao;
import com.chen.blog.module.friendLink.entity.FriendLink;
import com.chen.blog.module.friendLink.vo.FriendLinkReviewVO;
import com.chen.blog.module.friendLink.vo.FriendLinkVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 功能说明: 友链服务审核流程测试。
 * 作者: OpenAI Codex
 * 创建时间: 2026-07-05
 * 用途概述: 锁定前台申请默认待审核、公开列表只展示通过记录、后台审核可更新状态。
 */
class FriendLinkServiceImplTest {

    private FriendLinkDao friendLinkDao;
    private FriendLinkServiceImpl friendLinkService;

    @BeforeEach
    void setUp() {
        friendLinkDao = mock(FriendLinkDao.class);
        friendLinkService = new FriendLinkServiceImpl();
        ReflectionTestUtils.setField(friendLinkService, "friendLinkDao", friendLinkDao);
    }

    /**
     * 验证公开申请不会直接展示到前台，而是以待审核状态进入后台列表。
     */
    @Test
    void applyFriendLinkCreatesPendingRecord() {
        FriendLinkVO applyVO = FriendLinkVO.builder()
                .linkName("Example")
                .linkCover("https://example.com/cover.png")
                .linkAddress("https://example.com")
                .linkIntro("Example site")
                .build();
        ArgumentCaptor<FriendLink> captor = ArgumentCaptor.forClass(FriendLink.class);

        friendLinkService.applyFriendLink(applyVO);

        verify(friendLinkDao).insert(captor.capture());
        assertThat(captor.getValue().getLinkStatus()).isEqualTo(0);
        assertThat(captor.getValue().getLinkName()).isEqualTo("Example");
        assertThat(captor.getValue().getLinkCover()).isEqualTo("https://example.com/cover.png");
    }

    /**
     * 验证后台审核只更新指定记录的状态，避免误改申请内容。
     */
    @Test
    void reviewFriendLinkUpdatesOnlyStatus() {
        FriendLinkReviewVO reviewVO = FriendLinkReviewVO.builder()
                .id(12)
                .linkStatus(1)
                .build();
        ArgumentCaptor<FriendLink> captor = ArgumentCaptor.forClass(FriendLink.class);

        friendLinkService.reviewFriendLink(reviewVO);

        verify(friendLinkDao).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(12);
        assertThat(captor.getValue().getLinkStatus()).isEqualTo(1);
        assertThat(captor.getValue().getLinkName()).isNull();
    }

    /**
     * 验证公开友链列表通过查询条件过滤，只返回审核通过的数据。
     */
    @Test
    void listFriendLinksQueriesApprovedRecords() {
        when(friendLinkDao.selectList(any())).thenReturn(Collections.emptyList());
        ArgumentCaptor<QueryWrapper<FriendLink>> captor = ArgumentCaptor.forClass(QueryWrapper.class);

        friendLinkService.listFriendLinks();

        verify(friendLinkDao).selectList(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("link_status");
    }
}
