package com.chen.blog.common.strategy.email.impl;

import com.chen.blog.common.constant.MQPrefixConst;
import com.chen.blog.common.strategy.email.EmailStrategy;
import com.chen.blog.module.user.dto.EmailDTO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RocketMQ异步邮件发送策略
 *
 * @author chen
 * @date 2026/05/20
 */
@Service("mqEmailStrategyImpl")
public class MqEmailStrategyImpl implements EmailStrategy {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void sendEmail(EmailDTO emailDTO) {
        rocketMQTemplate.convertAndSend(MQPrefixConst.EMAIL_EXCHANGE, emailDTO);
    }

}
