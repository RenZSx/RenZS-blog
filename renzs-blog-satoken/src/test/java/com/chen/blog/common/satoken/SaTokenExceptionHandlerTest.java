package com.chen.blog.common.satoken;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.common.enums.StatusCodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * SaTokenExceptionHandler 单元测试
 * <p>
 * 验证 sa-token 三类鉴权异常被正确映射为统一 Result 业务码:
 * <ul>
 *   <li>NotLoginException  → NO_LOGIN(40001)</li>
 *   <li>NotPermissionException → AUTHORIZED(40300)</li>
 *   <li>NotRoleException   → AUTHORIZED(40300)</li>
 * </ul>
 *
 * @author chen
 */
class SaTokenExceptionHandlerTest {

    /**
     * 被测对象,无依赖直接 new
     */
    private final SaTokenExceptionHandler handler = new SaTokenExceptionHandler();

    /**
     * NotLoginException 映射为 NO_LOGIN(40001)
     * <p>
     * sa-token 1.39.0 中 NotLoginException 构造器签名为:
     * {@code NotLoginException(String type, String loginType, String token)}
     */
    @Test
    void notLoginException_should_return_NO_LOGIN() {
        NotLoginException ex = new NotLoginException(
                NotLoginException.INVALID_TOKEN, "login", "fake-token");
        Result<?> r = handler.handleNotLogin(ex);
        assertEquals(StatusCodeEnum.NO_LOGIN.getCode(), r.getCode());
        assertFalse(r.getFlag());
    }

    /**
     * NotPermissionException 映射为 AUTHORIZED(40300)
     * <p>
     * sa-token 1.39.0 中 NotPermissionException 双参构造器签名为:
     * {@code NotPermissionException(String permission, String loginType)}
     */
    @Test
    void notPermissionException_should_return_AUTHORIZED() {
        Result<?> r = handler.handleNotPermission(
                new NotPermissionException("user:add", "login"));
        assertEquals(StatusCodeEnum.AUTHORIZED.getCode(), r.getCode());
        assertFalse(r.getFlag());
    }

    /**
     * NotRoleException 映射为 AUTHORIZED(40300)
     * <p>
     * sa-token 1.39.0 中 NotRoleException 双参构造器签名为:
     * {@code NotRoleException(String role, String loginType)}
     */
    @Test
    void notRoleException_should_return_AUTHORIZED() {
        Result<?> r = handler.handleNotRole(
                new NotRoleException("admin", "login"));
        assertEquals(StatusCodeEnum.AUTHORIZED.getCode(), r.getCode());
        assertFalse(r.getFlag());
    }
}
