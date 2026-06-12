package com.chen.blog.module.user.controller;


import com.chen.blog.common.annotation.AccessLimit;
import com.chen.blog.module.user.dto.LoginUserDTO;
import com.chen.blog.module.user.dto.UserAreaDTO;
import com.chen.blog.module.user.dto.UserInfoDTO;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.user.dto.UserBackDTO;
import com.chen.blog.module.user.vo.*;
import com.chen.blog.module.user.service.EmailSendService;
import com.chen.blog.module.user.service.UserAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户账号控制器
 *
 */
@Api(tags = "用户账号模块")
@RestController
public class UserAuthController {
    @Autowired
    private UserAuthService userAuthService;
    @Autowired
    private EmailSendService emailSendService;

    /**
     * 获取当前服务端会话中的登录用户。
     * <p>
     * 该接口不创建新登录态,仅读取 {@code Authorization: Bearer xxx} Header 中的 sa-token,
     * 通过 sa-token 反查 Redis 中的登录态。前端启动时调用它,
     * 用于确认本地持久化用户信息是否仍然匹配服务端真实会话。
     * </p>
     *
     * @return {@link Result<UserInfoDTO>} 当前登录用户信息，未登录时由 UserUtils 抛出 NO_LOGIN 业务异常
     */
    @ApiOperation(value = "获取当前登录用户")
    @GetMapping("/users/current")
    public Result<UserInfoDTO> getCurrentUser() {
        return Result.ok(BeanCopyUtils.copyObject(UserUtils.getLoginUser(), UserInfoDTO.class));
    }

    /**
     * 发送邮箱验证码
     *
     * @param username 用户名
     * @return {@link Result<>}
     */
    @AccessLimit(seconds = 60, maxCount = 1)
    @ApiOperation(value = "发送邮箱验证码")
    @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String")
    @GetMapping("/users/code")
    public Result<?> sendCode(String username, String email) {
        emailSendService.sendCode(StringUtils.hasText(username) ? username : email);
        return Result.ok();
    }

    /**
     * 获取用户区域分布
     *
     * @param conditionVO 条件
     * @return {@link Result<UserAreaDTO>} 用户区域分布
     */
    @ApiOperation(value = "获取用户区域分布")
    @GetMapping("/admin/users/area")
    public Result<List<UserAreaDTO>> listUserAreas(ConditionVO conditionVO) {
        return Result.ok(userAuthService.listUserAreas(conditionVO));
    }

    /**
     * 查询后台用户列表
     *
     * @param condition 条件
     * @return {@link Result<UserBackDTO>} 用户列表
     */
    @ApiOperation(value = "查询后台用户列表")
    @GetMapping("/admin/users")
    public Result<PageResult<UserBackDTO>> listUsers(ConditionVO condition) {
        return Result.ok(userAuthService.listUserBackDTO(condition));
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserVO user) {
        userAuthService.register(user);
        return Result.ok();
    }

    /**
     * 修改密码
     *
     * @param user 用户信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "修改密码")
    @PutMapping("/users/password")
    public Result<?> updatePassword(@Valid @RequestBody UserVO user) {
        userAuthService.updatePassword(user);
        return Result.ok();
    }

    /**
     * 修改管理员密码
     *
     * @param passwordVO 密码信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "修改管理员密码")
    @PutMapping("/admin/users/password")
    public Result<?> updateAdminPassword(@Valid @RequestBody PasswordVO passwordVO) {
        userAuthService.updateAdminPassword(passwordVO);
        return Result.ok();
    }

    /**
     * 微博登录
     *
     * @param weiBoLoginVO 微博登录信息
     * @return {@link Result<LoginUserDTO>} 用户信息 + sa-token 令牌信息
     */
    @ApiOperation(value = "微博登录")
    @PostMapping("/users/oauth/weibo")
    public Result<LoginUserDTO> weiboLogin(@Valid @RequestBody WeiboLoginVO weiBoLoginVO) {
        return Result.ok(userAuthService.weiboLogin(weiBoLoginVO));
    }

    /**
     * qq登录
     *
     * @param qqLoginVO qq登录信息
     * @return {@link Result<LoginUserDTO>} 用户信息 + sa-token 令牌信息
     */
    @ApiOperation(value = "qq登录")
    @PostMapping("/users/oauth/qq")
    public Result<LoginUserDTO> qqLogin(@Valid @RequestBody QQLoginVO qqLoginVO) {
        return Result.ok(userAuthService.qqLogin(qqLoginVO));
    }

    /**
     * 缁戝畾qq
     *
     * @param qqLoginVO qq授权信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "缁戝畾qq")
    @PostMapping("/users/oauth/qq/bind")
    public Result<?> bindQq(@Valid @RequestBody QQLoginVO qqLoginVO) {
        userAuthService.bindQq(qqLoginVO);
        return Result.ok();
    }

    /**
     * Gitee登录
     *
     * @param giteeLoginVO Gitee登录信息
     * @return {@link Result<LoginUserDTO>} 用户信息 + sa-token 令牌信息
     */
    @ApiOperation(value = "Gitee登录")
    @PostMapping("/users/oauth/gitee")
    public Result<LoginUserDTO> giteeLogin(@Valid @RequestBody GiteeLoginVO giteeLoginVO) {
        return Result.ok(userAuthService.giteeLogin(giteeLoginVO));
    }

}
