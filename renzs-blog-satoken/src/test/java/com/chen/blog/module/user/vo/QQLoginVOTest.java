package com.chen.blog.module.user.vo;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validation tests for QQ login payloads.
 */
class QQLoginVOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * QQ callbacks can omit openId because the backend can derive it from the access token.
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
     * The access token must still be present because QQ token validation depends on it.
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
