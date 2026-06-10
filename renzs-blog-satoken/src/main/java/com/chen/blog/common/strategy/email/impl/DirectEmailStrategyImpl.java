package com.chen.blog.common.strategy.email.impl;

import com.chen.blog.common.strategy.email.EmailStrategy;
import com.chen.blog.module.user.dto.EmailDTO;
import com.chen.blog.module.user.service.EmailSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 直接调用邮件发送策略
 *
 * @author chen
 * @date 2026/05/20
 */
@Service("directEmailStrategyImpl")
public class DirectEmailStrategyImpl implements EmailStrategy {

    @Autowired
    private EmailSendService emailSendService;

    @Override
    public void sendEmail(EmailDTO emailDTO) {
        emailSendService.sendEmails(emailDTO);
    }

}
