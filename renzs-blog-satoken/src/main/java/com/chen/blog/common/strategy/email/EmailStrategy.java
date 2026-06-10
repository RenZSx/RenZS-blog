package com.chen.blog.common.strategy.email;

import com.chen.blog.module.user.dto.EmailDTO;

/**
 * 邮件发送策略
 *
 * @author chen
 * @date 2026/05/20
 */
public interface EmailStrategy {

    /**
     * 发送邮件
     *
     * @param emailDTO 邮件信息
     */
    void sendEmail(EmailDTO emailDTO);

}
