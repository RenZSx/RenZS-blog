package com.chen.blog.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.blogInfo.service.BlogInfoService;
import com.chen.blog.module.rbac.dao.UserRoleDao;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.user.service.AccountMergeService;
import com.chen.blog.module.user.strategy.context.SocialLoginStrategyContext;
import com.chen.blog.module.user.strategy.impl.QQLoginStrategyImpl;
import com.chen.blog.module.user.vo.QQLoginVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户账号服务的 QQ 绑定行为测试。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthServiceImplTest {

    @Mock
    private RedisService redisService;

    @Mock
    private UserAuthDao userAuthDao;

    @Mock
    private UserRoleDao userRoleDao;

    @Mock
    private UserInfoDao userInfoDao;

    @Mock
    private BlogInfoService blogInfoService;

    @Mock
    private SocialLoginStrategyContext socialLoginStrategyContext;

    @Mock
    private QQLoginStrategyImpl qqLoginStrategy;

    @Mock
    private AccountMergeService accountMergeService;

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    /**
     * 已属于 QQ 独立账号的 QQ 绑定到邮箱账号时，应把 QQ 账号合并到当前邮箱账号。
     */
    @Test
    void bindQq_should_merge_existing_qq_account_to_current_email_user() {
        QQLoginVO qqLoginVO = QQLoginVO.builder()
                .openId("qq-open-id")
                .accessToken("token")
                .build();
        UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();
        UserAuth existingAuth = UserAuth.builder()
                .id(10)
                .userInfoId(2)
                .username("qq-open-id")
                .loginType(LoginTypeEnum.QQ.getType())
                .build();

        doNothing().when(qqLoginStrategy).validateToken(qqLoginVO);
        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingAuth);

        try (MockedStatic<UserUtils> userUtils = mockStatic(UserUtils.class)) {
            userUtils.when(UserUtils::getLoginUser).thenReturn(loginUser);

            userAuthService.bindQq(qqLoginVO);
        }

        verify(accountMergeService).mergeQqAccountToEmailAccount(1, 2);
        verify(userAuthDao, never()).insert(any(UserAuth.class));
    }

    /**
     * 未绑定过的 QQ 会创建当前资料账号的 QQ 登录凭证。
     */
    @Test
    void bindQq_should_insert_qq_auth_for_current_user() {
        QQLoginVO qqLoginVO = QQLoginVO.builder()
                .openId("qq-open-id")
                .accessToken("token")
                .build();
        UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();

        doNothing().when(qqLoginStrategy).validateToken(qqLoginVO);
        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        try (MockedStatic<UserUtils> userUtils = mockStatic(UserUtils.class)) {
            userUtils.when(UserUtils::getLoginUser).thenReturn(loginUser);

            userAuthService.bindQq(qqLoginVO);
        }

        ArgumentCaptor<UserAuth> userAuthCaptor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthDao).insert(userAuthCaptor.capture());
        UserAuth savedAuth = userAuthCaptor.getValue();
        assertEquals(1, savedAuth.getUserInfoId());
        assertEquals("qq-open-id", savedAuth.getUsername());
        assertEquals("token", savedAuth.getPassword());
        assertEquals(LoginTypeEnum.QQ.getType(), savedAuth.getLoginType());
        verify(qqLoginStrategy).validateToken(qqLoginVO);
    }

    /**
     * 已绑定当前用户时保持幂等成功，不重复插入凭证。
     */
    @Test
    void bindQq_should_be_idempotent_when_open_id_already_bound_to_current_user() {
        QQLoginVO qqLoginVO = QQLoginVO.builder()
                .openId("qq-open-id")
                .accessToken("token")
                .build();
        UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();
        UserAuth existingAuth = UserAuth.builder()
                .id(10)
                .userInfoId(1)
                .username("qq-open-id")
                .loginType(LoginTypeEnum.QQ.getType())
                .build();

        doNothing().when(qqLoginStrategy).validateToken(qqLoginVO);
        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingAuth);

        try (MockedStatic<UserUtils> userUtils = mockStatic(UserUtils.class)) {
            userUtils.when(UserUtils::getLoginUser).thenReturn(loginUser);

            userAuthService.bindQq(qqLoginVO);
        }

        verify(qqLoginStrategy).validateToken(qqLoginVO);
        verify(userAuthDao, never()).insert(any(UserAuth.class));
        assertEquals(1, existingAuth.getUserInfoId());
        assertEquals(LoginTypeEnum.QQ.getType(), existingAuth.getLoginType());
        assertEquals("qq-open-id", existingAuth.getUsername());
    }
}
