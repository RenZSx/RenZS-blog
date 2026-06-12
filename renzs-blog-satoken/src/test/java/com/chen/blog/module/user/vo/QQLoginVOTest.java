package com.chen.blog.module.user.vo;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QQ登录请求参数校验测试。
 */
class QQLoginVOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * QQ回调允许缺少openId，因为后端可以根据accessToken查询得到。
     */
    @Test
    void validate_should_allow_missing_open_id() {
        QQLoginVO qqLoginVO = QQLoginVO.builder()
                .accessToken("qq-access-token")
                .build();

        Set<ConstraintViolation<QQLoginVO>> violations = validator.validate(qqLoginVO);

        assertEquals(0, violations.size());
    }

    /**
     * accessToken仍必须存在，因为QQ token校验依赖该字段。
     */
    @Test
    void validate_should_reject_missing_access_token() {
        QQLoginVO qqLoginVO = QQLoginVO.builder()
                .openId("qq-open-id")
                .build();

        Set<ConstraintViolation<QQLoginVO>> violations = validator.validate(qqLoginVO);

        assertEquals(1, violations.size());
        assertEquals("accessToken", violations.iterator().next().getPropertyPath().toString());
    }
}
