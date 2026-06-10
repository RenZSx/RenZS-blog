package com.chen.blog.common.strategy.email.context;

import com.chen.blog.common.strategy.email.EmailStrategy;
import com.chen.blog.module.user.dto.EmailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.chen.blog.common.enums.EmailModeEnum.getStrategy;

/**
 * 邮件发送策略上下文
 *
 * @author chen
 * @date 2026/05/20
 */
@Service
public class EmailStrategyContext {

    /**
     * 邮件发送模式
     */
    @Value("${email.mode}")
    private String emailMode;

    @Autowired
    private Map<String, EmailStrategy> emailStrategyMap;

    /**
     * 执行邮件发送策略
     *
     * @param emailDTO 邮件信息
     */
    public void executeEmailStrategy(EmailDTO emailDTO) {
        emailStrategyMap.get(getStrategy(emailMode)).sendEmail(emailDTO);
    }

}
