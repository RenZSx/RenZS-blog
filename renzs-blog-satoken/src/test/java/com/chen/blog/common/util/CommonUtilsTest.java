package com.chen.blog.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 公共工具方法测试。
 */
class CommonUtilsTest {

    /**
     * 邮箱校验遇到空值时应返回false，而不是抛出系统异常。
     */
    @Test
    void checkEmail_should_return_false_for_null() {
        assertFalse(CommonUtils.checkEmail(null));
    }
}
