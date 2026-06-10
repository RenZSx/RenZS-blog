package com.chen.blog.module.user.service;

import com.chen.blog.module.user.dto.LoginUserDTO;
import com.chen.blog.module.user.vo.LoginVO;

/**
 * 登录业务接口
 * sa-token 迁移后自实现登录逻辑(替代原 Spring Security 表单登录过滤器)
 *
 * @author chen
 */
public interface LoginService {

    /**
     * 账密登录
     *
     * @param loginVO 登录请求(用户名 + 密码)
     * @return 登录成功的用户信息 + sa-token 令牌信息(供 App 端 Header 鉴权使用)
     */
    LoginUserDTO login(LoginVO loginVO);

    /**
     * 注销当前登录
     */
    void logout();
}
