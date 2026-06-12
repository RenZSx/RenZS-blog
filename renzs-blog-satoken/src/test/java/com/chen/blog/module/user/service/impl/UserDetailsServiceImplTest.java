package com.chen.blog.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.module.rbac.dao.RoleDao;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.user.entity.UserInfo;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * 用户详情服务登录解析测试。
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserAuthDao userAuthDao;

    @Mock
    private UserInfoDao userInfoDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private RedisService redisService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        initTableInfo(UserAuth.class);
        initTableInfo(UserInfo.class);
    }

    /**
     * 历史数据里邮箱资料在 QQ 账号上时，邮箱登录应修正邮箱凭证归属并进入同一个资料账号。
     */
    @Test
    void loadUserByUsername_should_repair_email_auth_owner_when_profile_email_belongs_to_qq_user() {
        String email = "bind@example.com";
        UserAuth emailAuth = UserAuth.builder()
                .id(10)
                .userInfoId(1)
                .username(email)
                .password("encoded-password")
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();
        UserInfo oldEmailUserInfo = UserInfo.builder()
                .id(1)
                .email(email)
                .nickname("old")
                .isDisable(0)
                .build();
        UserInfo qqUserInfo = UserInfo.builder()
                .id(2)
                .email(email)
                .nickname("qq")
                .isDisable(0)
                .build();
        UserAuth qqAuth = UserAuth.builder()
                .id(20)
                .userInfoId(2)
                .username("qq-open-id")
                .loginType(LoginTypeEnum.QQ.getType())
                .build();

        lenient().when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(emailAuth, qqAuth);
        lenient().when(userInfoDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(oldEmailUserInfo, qqUserInfo));
        when(userAuthDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(emailAuth, qqAuth));
        when(userInfoDao.selectById(any())).thenAnswer(invocation -> {
            Integer userInfoId = invocation.getArgument(0);
            return userInfoId.equals(2) ? qqUserInfo : oldEmailUserInfo;
        });
        when(roleDao.listRolesByUserInfoId(any())).thenReturn(Collections.emptyList());
        when(redisService.sMembers(any())).thenReturn(Collections.emptySet());
        when(request.getHeader(any())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserDetailDTO userDetail = userDetailsService.loadUserByUsername(email);

        assertEquals(2, userDetail.getUserInfoId());
        assertEquals("qq", userDetail.getNickname());
        assertEquals(email, userDetail.getEmail());
        assertTrue(userDetail.getEmailBound());
        assertTrue(userDetail.getQqBound());

        ArgumentCaptor<UserAuth> captor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthDao).updateById(captor.capture());
        assertEquals(10, captor.getValue().getId());
        assertEquals(2, captor.getValue().getUserInfoId());
    }

    private void initTableInfo(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
    }
}
