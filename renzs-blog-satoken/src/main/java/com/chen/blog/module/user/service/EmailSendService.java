package com.chen.blog.module.user.service;

import com.chen.blog.module.user.dto.EmailDTO;

public interface EmailSendService {

    /**
     * 发送邮箱验证码
     *
     * @param username 邮箱号
     */
    void sendCode(String username);

    /**
     * 发送通知邮件
     */
    void sendEmails(EmailDTO emailDTO);


}
