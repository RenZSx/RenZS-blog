package com.chen.blog.module.user.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
     * 邮箱作为主账号时，邮箱登录不能把邮箱凭证反向迁到 QQ 资料账号。
     */
    @Test
    void loadUserByUsername_should_keep_email_user_as_primary_when_same_email_qq_profile_exists() {
        String email = "bind@example.com";
        UserAuth emailAuth = UserAuth.builder()
                .id(10)
                .userInfoId(1)
                .username(email)
                .password("encoded-password")
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();
        UserInfo emailUserInfo = UserInfo.builder()
                .id(1)
                .email(email)
                .nickname("email-user")
                .isDisable(0)
                .build();
        UserAuth qqAuth = UserAuth.builder()
                .id(20)
                .userInfoId(1)
                .username("qq-open-id")
                .loginType(LoginTypeEnum.QQ.getType())
                .build();

        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(emailAuth);
        when(userAuthDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(emailAuth, qqAuth));
        when(userInfoDao.selectById(1)).thenReturn(emailUserInfo);
        when(roleDao.listRolesByUserInfoId(1)).thenReturn(Collections.emptyList());
        when(redisService.sMembers(any())).thenReturn(Collections.emptySet());
        when(request.getHeader(any())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        UserDetailDTO userDetail = userDetailsService.loadUserByUsername(email);

        assertEquals(1, userDetail.getUserInfoId());
        assertEquals("email-user", userDetail.getNickname());
        assertEquals(email, userDetail.getEmail());
        assertTrue(userDetail.getEmailBound());
        assertTrue(userDetail.getQqBound());
        verify(userAuthDao, never()).updateById(any(UserAuth.class));
    }

    private void initTableInfo(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
    }
}
