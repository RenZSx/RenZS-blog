package com.chen.blog.module.user.strategy;

import com.chen.blog.module.user.dto.LoginUserDTO;

/**
 * 第三方登录策略
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
public interface SocialLoginStrategy {

    /**
     * 登录
     *
     * @param data 数据
     * @return {@link LoginUserDTO} 用户信息 + sa-token 令牌信息
     */
    LoginUserDTO login(String data);

}
