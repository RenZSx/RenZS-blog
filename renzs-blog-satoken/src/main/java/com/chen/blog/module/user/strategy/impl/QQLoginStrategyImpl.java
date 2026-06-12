package com.chen.blog.module.user.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.chen.blog.common.config.QQConfigProperties;
import com.chen.blog.common.constant.SocialLoginConst;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.enums.StatusCodeEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.CommonUtils;
import com.chen.blog.module.user.dto.QQTokenDTO;
import com.chen.blog.module.user.dto.QQUserInfoDTO;
import com.chen.blog.module.user.dto.SocialTokenDTO;
import com.chen.blog.module.user.dto.SocialUserInfoDTO;
import com.chen.blog.module.user.vo.QQLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * QQ login strategy implementation.
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Service("qqLoginStrategyImpl")
public class QQLoginStrategyImpl extends AbstractSocialLoginStrategyImpl {
    @Autowired
    private QQConfigProperties qqConfigProperties;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public SocialTokenDTO getSocialToken(String data) {
        QQLoginVO qqLoginVO = JSON.parseObject(data, QQLoginVO.class);
        // Validate the QQ access token before issuing the local social token.
        validateToken(qqLoginVO);
        return SocialTokenDTO.builder()
                .openId(qqLoginVO.getOpenId())
                .accessToken(qqLoginVO.getAccessToken())
                .loginType(LoginTypeEnum.QQ.getType())
                .build();
    }

    @Override
    public SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO) {
        // Build the QQ user info request parameters.
        Map<String, String> formData = new HashMap<>(3);
        formData.put(SocialLoginConst.QQ_OPEN_ID, socialTokenDTO.getOpenId());
        formData.put(SocialLoginConst.ACCESS_TOKEN, socialTokenDTO.getAccessToken());
        formData.put(SocialLoginConst.OAUTH_CONSUMER_KEY, qqConfigProperties.getAppId());
        QQUserInfoDTO qqUserInfoDTO = JSON.parseObject(restTemplate.getForObject(qqConfigProperties.getUserInfoUrl(), String.class, formData), QQUserInfoDTO.class);
        return SocialUserInfoDTO.builder()
                .nickname(Objects.requireNonNull(qqUserInfoDTO).getNickname())
                .avatar(qqUserInfoDTO.getFigureurl_qq_1())
                .build();
    }

    /**
     * Validate QQ token information and fill the openId when QQ only returns it from the check-token API.
     *
     * @param qqLoginVO QQ login payload
     */
    public void validateToken(QQLoginVO qqLoginVO) {
        Map<String, String> qqData = new HashMap<>(1);
        qqData.put(SocialLoginConst.ACCESS_TOKEN, qqLoginVO.getAccessToken());
        try {
            String result = restTemplate.getForObject(qqConfigProperties.getCheckTokenUrl(), String.class, qqData);
            QQTokenDTO qqTokenDTO = JSON.parseObject(CommonUtils.getBracketsContent(Objects.requireNonNull(result)), QQTokenDTO.class);
            if (!StringUtils.hasText(qqLoginVO.getOpenId())) {
                // QQ implicit flow may only return access_token to the callback; fill openId from QQ validation.
                qqLoginVO.setOpenId(qqTokenDTO.getOpenid());
            }
            if (!qqLoginVO.getOpenId().equals(qqTokenDTO.getOpenid())) {
                throw new BizException(StatusCodeEnum.QQ_LOGIN_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(StatusCodeEnum.QQ_LOGIN_ERROR);
        }
    }

}
