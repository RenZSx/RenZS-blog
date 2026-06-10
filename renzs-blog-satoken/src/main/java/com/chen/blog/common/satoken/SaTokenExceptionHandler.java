package com.chen.blog.common.satoken;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.common.enums.StatusCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * sa-token 鉴权异常 → 统一 Result 映射
 * <p>
 * 替代原 Spring Security 的:
 * <ul>
 *   <li>AuthenticationEntryPointImpl(未登录 → 40001)</li>
 *   <li>AccessDeniedHandlerImpl(权限不足 → 40300)</li>
 * </ul>
 * <p>
 * 使用 {@code @Order(0)} 保证比项目原有 ControllerAdviceHandler
 * (默认 {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE})优先级高,
 * 使 sa-token 异常先被本类处理。
 *
 * @author chen
 */
@Slf4j
@Order(0)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 未登录异常(无 token / token 无效 / token 过期 / 被踢下线 等)
     *
     * @param e sa-token 抛出的未登录异常
     * @return 统一 Result,业务码 NO_LOGIN(40001)
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        log.debug("未登录: {}", e.getMessage());
        return Result.fail(StatusCodeEnum.NO_LOGIN);
    }

    /**
     * 权限不足异常(@SaCheckPermission 校验失败)
     *
     * @param e sa-token 抛出的权限不足异常
     * @return 统一 Result,业务码 AUTHORIZED(40300)
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermission(NotPermissionException e) {
        log.debug("权限不足: {}", e.getMessage());
        return Result.fail(StatusCodeEnum.AUTHORIZED);
    }

    /**
     * 角色不足异常(@SaCheckRole 校验失败)
     *
     * @param e sa-token 抛出的角色不足异常
     * @return 统一 Result,业务码 AUTHORIZED(40300)
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRole(NotRoleException e) {
        log.debug("角色不足: {}", e.getMessage());
        return Result.fail(StatusCodeEnum.AUTHORIZED);
    }
}
