package com.chen.blog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件发送模式枚举
 *
 * @author chen
 * @date 2026/05/20
 */
@Getter
@AllArgsConstructor
public enum EmailModeEnum {
    /**
     * RocketMQ异步发送
     */
    MQ("mq", "mqEmailStrategyImpl"),
    /**
     * 直接调用发送
     */
    DIRECT("direct", "directEmailStrategyImpl");

    /**
     * 模式
     */
    private final String mode;

    /**
     * 策略
     */
    private final String strategy;

    /**
     * 获取策略
     *
     * @param mode 模式
     * @return {@link String} 邮件发送策略
     */
    public static String getStrategy(String mode) {
        for (EmailModeEnum value : EmailModeEnum.values()) {
            if (value.getMode().equals(mode)) {
                return value.getStrategy();
            }
        }
        return null;
    }

}
