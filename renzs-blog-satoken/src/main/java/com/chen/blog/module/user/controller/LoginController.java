package com.chen.blog.module.user.controller;

import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.user.dto.LoginUserDTO;
import com.chen.blog.module.user.service.LoginService;
import com.chen.blog.module.user.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录登出接口
 * <p>
 * 自实现,替代原 Spring Security 的 formLogin 过滤器。
 * URL 与原 Spring Security 配置(loginProcessingUrl/logoutUrl)保持一致,前端零改动。
 *
 * @author chen
 */
@Api(tags = "登录登出")
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 账号密码登录
     *
     * @param loginVO 登录请求(username + password)
     * @return 用户信息 + sa-token 令牌信息(供 App 端 Header 鉴权使用,Web 端可忽略 token 字段)
     */
    @ApiOperation("账号密码登录")
    @PostMapping("/login")
    public Result<LoginUserDTO> login(@Validated LoginVO loginVO) {
        return Result.ok(loginService.login(loginVO));
    }

    /**
     * 注销当前登录
     */
    @ApiOperation("注销")
    @PostMapping("/logout")
    public Result<?> logout() {
        loginService.logout();
        return Result.ok();
    }
}
