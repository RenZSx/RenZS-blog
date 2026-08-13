package com.chen.blog.common.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Loads RocketMQ only when asynchronous email delivery is selected.
 */
@Configuration
@ConditionalOnProperty(name = "email.mode", havingValue = "mq")
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
