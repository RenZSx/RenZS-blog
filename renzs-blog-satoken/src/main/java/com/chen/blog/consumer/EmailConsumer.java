
package com.chen.blog.consumer;
import com.chen.blog.common.constant.MQPrefixConst;
import com.chen.blog.module.user.dto.EmailDTO;
import com.chen.blog.module.user.service.EmailSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 通知邮箱
 *
 * @author chenfuyun
 * @date 2021/06/13
 * @since 1.0.0
 **/

@Component
@RocketMQMessageListener(topic = MQPrefixConst.EMAIL_EXCHANGE,consumerGroup = "email-group")
@Slf4j
public class EmailConsumer implements RocketMQListener<EmailDTO> {

    @Resource
    private EmailSendService emailSendService;

    @Override
    public void onMessage(EmailDTO emailDTO) {
        log.info("mq消费信息");
        log.info("emailDTO：{}",emailDTO);
        emailSendService.sendEmails(emailDTO);
    }
}

