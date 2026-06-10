package com.chen.blog.module.user.service.impl;

import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.entity.UserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录信息异步更新助手
 * <p>
 * 独立 Bean,避免 Spring AOP 的"同类自调用陷阱"导致 {@link Async} 失效。
 * 由 {@link LoginServiceImpl#login} 显式注入并跨 Bean 调用,代理才会正确生效。
 *
 * @author chen
 */
@Component
public class LoginAsyncHelper {

    @Autowired
    private UserAuthDao userAuthDao;

    /**
     * 异步更新登录信息(IP / IP 来源 / 最后登录时间)
     *
     * @param userDetail 当前登录的用户详情
     */
    @Async
    public void updateLoginInfo(UserDetailDTO userDetail) {
        UserAuth userAuth = UserAuth.builder()
                .id(userDetail.getId())
                .ipAddress(userDetail.getIpAddress())
                .ipSource(userDetail.getIpSource())
                .lastLoginTime(userDetail.getLastLoginTime())
                .build();
        userAuthDao.updateById(userAuth);
    }
}
