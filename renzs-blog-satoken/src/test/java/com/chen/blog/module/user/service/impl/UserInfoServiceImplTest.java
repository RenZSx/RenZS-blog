package com.chen.blog.module.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.rbac.service.UserRoleService;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.dto.UserOnlineDTO;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.user.entity.UserInfo;
import com.chen.blog.module.user.vo.EmailVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.chen.blog.common.constant.RedisPrefixConst.USER_CODE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户资料服务的账号绑定行为测试。
 */
@ExtendWith(MockitoExtension.class)
class UserInfoServiceImplTest {

    @Mock
    private UserInfoDao userInfoDao;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private RedisService redisService;

    @Mock
    private UploadStrategyContext uploadStrategyContext;

    @Mock
    private UserAuthDao userAuthDao;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    @BeforeEach
    void setUp() {
        initTableInfo(UserAuth.class);
        initTableInfo(UserInfo.class);
    }

    /**
     * 已属于其他用户的邮箱不能被当前用户抢占绑定。
     */
    @Test
    void saveUserEmail_should_reject_email_bound_to_other_user() {
        EmailVO emailVO = EmailVO.builder()
                .email("bind@example.com")
                .code("123456")
                .password("abc123")
                .build();
        UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();
        UserAuth existingAuth = UserAuth.builder()
                .id(10)
                .userInfoId(2)
                .username("bind@example.com")
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();

        when(redisService.get(USER_CODE_KEY + "bind@example.com")).thenReturn("123456");
        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingAuth);

        try (MockedStatic<UserUtils> userUtils = mockStatic(UserUtils.class)) {
            userUtils.when(UserUtils::getLoginUser).thenReturn(loginUser);

            BizException exception = assertThrows(BizException.class, () -> userInfoService.saveUserEmail(emailVO));

            assertEquals("该邮箱已绑定其他账号", exception.getMessage());
        }
    }

    /**
     * 未绑定过的邮箱会创建邮箱登录凭证并写入当前资料账号。
     */
    @Test
    void saveUserEmail_should_insert_email_auth_for_current_user() {
        EmailVO emailVO = EmailVO.builder()
                .email("bind@example.com")
                .code("123456")
                .password("abc123")
                .build();
        UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();

        when(redisService.get(USER_CODE_KEY + "bind@example.com")).thenReturn("123456");
        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        try (MockedStatic<UserUtils> userUtils = mockStatic(UserUtils.class)) {
            userUtils.when(UserUtils::getLoginUser).thenReturn(loginUser);

            userInfoService.saveUserEmail(emailVO);
        }

        ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userInfoDao).updateById(userInfoCaptor.capture());
        assertEquals(1, userInfoCaptor.getValue().getId());
        assertEquals("bind@example.com", userInfoCaptor.getValue().getEmail());

        ArgumentCaptor<UserAuth> userAuthCaptor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthDao).insert(userAuthCaptor.capture());
        UserAuth savedAuth = userAuthCaptor.getValue();
        assertEquals(1, savedAuth.getUserInfoId());
        assertEquals("bind@example.com", savedAuth.getUsername());
        assertEquals(LoginTypeEnum.EMAIL.getType(), savedAuth.getLoginType());
        assertTrue(BCrypt.checkpw("abc123", savedAuth.getPassword()));
        assertTrue(Objects.nonNull(savedAuth.getPassword()));
    }

    /**
     * 历史数据里当前 QQ 资料账号已经写入邮箱时，绑定邮箱应把邮箱登录凭证迁回当前账号。
     */
    @Test
    void saveUserEmail_should_repair_history_email_auth_owner_when_current_profile_has_same_email() {
        EmailVO emailVO = EmailVO.builder()
                .email("bind@example.com")
                .code("123456")
                .build();
        UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(2).build();
        UserInfo currentUserInfo = UserInfo.builder()
                .id(2)
                .email("bind@example.com")
                .build();
        UserAuth existingEmailAuth = UserAuth.builder()
                .id(10)
                .userInfoId(1)
                .username("bind@example.com")
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();
        UserAuth qqAuth = UserAuth.builder()
                .id(20)
                .userInfoId(2)
                .username("qq-open-id")
                .loginType(LoginTypeEnum.QQ.getType())
                .build();

        when(redisService.get(USER_CODE_KEY + "bind@example.com")).thenReturn("123456");
        when(userAuthDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingEmailAuth, qqAuth);
        when(userInfoDao.selectById(2)).thenReturn(currentUserInfo);

        try (MockedStatic<UserUtils> userUtils = mockStatic(UserUtils.class)) {
            userUtils.when(UserUtils::getLoginUser).thenReturn(loginUser);

            userInfoService.saveUserEmail(emailVO);
        }

        ArgumentCaptor<UserAuth> userAuthCaptor = ArgumentCaptor.forClass(UserAuth.class);
        verify(userAuthDao).updateById(userAuthCaptor.capture());
        assertEquals(10, userAuthCaptor.getValue().getId());
        assertEquals(2, userAuthCaptor.getValue().getUserInfoId());
        verify(userAuthDao, never()).insert(any(UserAuth.class));

        ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userInfoDao).updateById(userInfoCaptor.capture());
        assertEquals(2, userInfoCaptor.getValue().getId());
        assertEquals("bind@example.com", userInfoCaptor.getValue().getEmail());
    }

    /**
     * sa-token Redis Jackson 反序列化为 Map 时,在线列表仍应还原登录用户资料。
     */
    @Test
    void listOnlineUsers_should_convert_map_login_user_from_token_session() {
        SaSession tokenSession = new SaSession("token-session:abc");
        Map<String, Object> loginUser = new HashMap<>();
        loginUser.put("userInfoId", 1);
        loginUser.put("nickname", "tester");
        loginUser.put("avatar", "avatar.png");
        loginUser.put("ipAddress", "127.0.0.1");
        tokenSession.set(UserUtils.LOGIN_USER_KEY, loginUser);

        PageUtils.setCurrentPage(new Page<>(1, 10));
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.searchTokenValue("", 0, -1, false))
                    .thenReturn(Arrays.asList("satoken:login:token:abc"));
            stpUtil.when(() -> StpUtil.getLoginIdByToken("abc")).thenReturn(1);
            stpUtil.when(() -> StpUtil.getTokenSessionByToken("abc")).thenReturn(tokenSession);

            PageResult<UserOnlineDTO> result = userInfoService.listOnlineUsers(new ConditionVO());

            assertEquals(1, result.getCount());
            assertEquals(1, result.getRecordList().size());
            assertEquals(1, result.getRecordList().get(0).getUserInfoId());
            assertEquals("tester", result.getRecordList().get(0).getNickname());
        } finally {
            PageUtils.remove();
        }
    }

    private void initTableInfo(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
    }
}
