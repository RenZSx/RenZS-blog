package com.chen.blog.module.user.strategy.impl;

import com.chen.blog.common.config.QQConfigProperties;
import com.chen.blog.module.user.vo.QQLoginVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * QQ登录策略的token校验测试。
 */
@ExtendWith(MockitoExtension.class)
class QQLoginStrategyImplTest {

    @Mock
    private QQConfigProperties qqConfigProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private QQLoginStrategyImpl qqLoginStrategy;

    /**
     * 当QQ回调只携带access_token时，后端应从QQ校验结果中回填openId。
     */
    @Test
    void validateToken_should_fill_open_id_when_missing() {
        QQLoginVO qqLoginVO = QQLoginVO.builder()
                .accessToken("qq-access-token")
                .build();

        when(qqConfigProperties.getCheckTokenUrl()).thenReturn("https://graph.qq.com/oauth2.0/me?access_token={access_token}");
        when(restTemplate.getForObject(eq("https://graph.qq.com/oauth2.0/me?access_token={access_token}"), eq(String.class), anyMap()))
                .thenReturn("callback( {\"client_id\":\"102120306\",\"openid\":\"qq-open-id\"} );");

        qqLoginStrategy.validateToken(qqLoginVO);

        assertEquals("qq-open-id", qqLoginVO.getOpenId());
    }
}
