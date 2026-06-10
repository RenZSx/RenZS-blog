package com.chen.blog.common.util;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.user.dto.UserDetailDTO;
import org.springframework.stereotype.Component;

import static com.chen.blog.common.enums.StatusCodeEnum.NO_LOGIN;

/**
 * 用户工具类(sa-token 版)
 * 对外签名保持不变,业务侧 30 处调用零改动
 */
@Component
public class UserUtils {

    /** SaSession 中存放 UserDetailDTO 的 key,登录时塞入,业务侧通过 getLoginUser() 取回 */
    public static final String LOGIN_USER_KEY = "loginUser";

    public static UserDetailDTO getLoginUser() {
        if (!StpUtil.isLogin()) {
            throw new BizException(NO_LOGIN);
        }
        SaSession session = StpUtil.getTokenSession();
        Object obj = session.get(LOGIN_USER_KEY);
        if (!(obj instanceof UserDetailDTO)) {
            throw new BizException(NO_LOGIN);
        }
        return (UserDetailDTO) obj;
    }
}
