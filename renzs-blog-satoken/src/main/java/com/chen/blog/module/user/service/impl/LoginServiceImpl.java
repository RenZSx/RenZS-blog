package com.chen.blog.module.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.chen.blog.common.constant.CommonConst;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.user.dto.LoginUserDTO;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.dto.UserInfoDTO;
import com.chen.blog.module.user.service.LoginService;
import com.chen.blog.module.user.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.chen.blog.common.enums.StatusCodeEnum.USERNAME_NOT_EXIST;

/**
 * 登录业务实现
 * <p>
 * 复刻原 AuthenticationSuccessHandlerImpl 的"异步更新登录时间/IP"逻辑;
 * 同时填补原 Spring Security UserDetails.isAccountNonLocked 的"账号禁用"检查
 * (Task 2.1 中 UserDetailDTO 移除 implements UserDetails 后丢失,在此重新实现)。
 * <p>
 * 异步更新逻辑通过 {@link LoginAsyncHelper} 跨 Bean 调用,避免同类自调用让 @Async 失效。
 *
 * @author chen
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private LoginAsyncHelper loginAsyncHelper;

    @Override
    public LoginUserDTO login(LoginVO loginVO) {
        // 1. 查询用户 (沿用 UserDetailsServiceImpl.loadUserByUsername)
        UserDetailDTO userDetail;
        try {
            userDetail = userDetailsService.loadUserByUsername(loginVO.getUsername());
        } catch (BizException e) {
            // 用户名不存在 → 统一抛 USERNAME_NOT_EXIST (code=52002)
            throw new BizException(USERNAME_NOT_EXIST);
        }
        // 2. 校验密码 (Hutool BCrypt)
        if (!BCrypt.checkpw(loginVO.getPassword(), userDetail.getPassword())) {
            throw new BizException("密码错误");
        }
        // 3. 校验账号是否被禁用 (复刻原 UserDetailDTO.isAccountNonLocked 行为)
        //    与 AbstractSocialLoginStrategyImpl 中的禁用检查保持一致:用 CommonConst.TRUE 判断
        if (userDetail.getIsDisable() != null && userDetail.getIsDisable().equals(CommonConst.TRUE)) {
            throw new BizException("账号已被禁用");
        }
        // 4. sa-token 登录,以 UserInfo.id (userInfoId) 为账号标识,并把 UserDetailDTO 塞入 TokenSession
        //    选择 userInfoId 而非 UserAuth.id 的原因:
        //      - StpInterfaceImpl.getRoleList 直接以 loginId 当 userInfoId 查角色 (零转换)
        //      - WebSocket 握手原 Spring Security 流程拿的也是 userInfoId,保持行为一致
        //      - 业务侧均通过 UserUtils.getLoginUser() 取回完整 UserDetailDTO,不直接读 loginId,不受影响
        StpUtil.login(userDetail.getUserInfoId());
        StpUtil.getTokenSession().set(UserUtils.LOGIN_USER_KEY, userDetail);
        // 5. 跨 Bean 调用 LoginAsyncHelper 异步更新登录信息,避免同类自调用陷阱让 @Async 失效
        loginAsyncHelper.updateLoginInfo(userDetail);
        // 6. 组装登录响应:
        //    - userInfo: 复刻原 AuthenticationSuccessHandlerImpl 的 BeanCopyUtils 转换
        //    - tokenName/tokenValue/tokenTimeout: 从 sa-token 取出供 App 端 Header 鉴权使用,
        //      Web 端可忽略 (依旧通过 Cookie 自动鉴权)
        UserInfoDTO userInfo = BeanCopyUtils.copyObject(userDetail, UserInfoDTO.class);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return LoginUserDTO.builder()
                .userInfo(userInfo)
                .tokenName(tokenInfo.getTokenName())
                .tokenValue(tokenInfo.getTokenValue())
                .tokenTimeout(tokenInfo.getTokenTimeout())
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
