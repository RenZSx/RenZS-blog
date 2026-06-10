package com.chen.blog.module.user.strategy.context;

import com.chen.blog.module.user.dto.LoginUserDTO;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.module.user.strategy.SocialLoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * 第三方登录策略上下文
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Service
public class SocialLoginStrategyContext {

    @Autowired
    private Map<String, SocialLoginStrategy> socialLoginStrategyMap;

    /**
     * 执行第三方登录策略
     *
     * @param data          数据
     * @param loginTypeEnum 登录枚举类型
     * @return {@link LoginUserDTO} 用户信息 + sa-token 令牌信息
     */
    public LoginUserDTO executeLoginStrategy(String data, LoginTypeEnum loginTypeEnum) {
        return socialLoginStrategyMap.get(loginTypeEnum.getStrategy()).login(data);
    }

}
