package com.chen.blog.module.user.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.chen.blog.common.config.QQConfigProperties;
import com.chen.blog.common.constant.SocialLoginConst;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.CommonUtils;
import com.chen.blog.module.user.dto.QQTokenDTO;
import com.chen.blog.module.user.dto.QQUserInfoDTO;
import com.chen.blog.module.user.dto.SocialTokenDTO;
import com.chen.blog.module.user.dto.SocialUserInfoDTO;
import com.chen.blog.module.user.vo.QQLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * QQ登录策略实现。
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Slf4j
@Service("qqLoginStrategyImpl")
public class QQLoginStrategyImpl extends AbstractSocialLoginStrategyImpl {
    @Autowired
    private QQConfigProperties qqConfigProperties;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public SocialTokenDTO getSocialToken(String data) {
        QQLoginVO qqLoginVO = JSON.parseObject(data, QQLoginVO.class);
        // 校验QQ accessToken，校验通过后再生成本地社交登录token。
        validateToken(qqLoginVO);
        return SocialTokenDTO.builder()
                .openId(qqLoginVO.getOpenId())
                .accessToken(qqLoginVO.getAccessToken())
                .loginType(LoginTypeEnum.QQ.getType())
                .build();
    }

    @Override
    public SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO) {
        // 构建QQ用户信息请求参数。
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
     * 校验QQ token信息；当QQ回调只返回access_token时，从校验接口回填openId。
     *
     * @param qqLoginVO QQ登录请求参数
     */
    public void validateToken(QQLoginVO qqLoginVO) {
        if (!StringUtils.hasText(qqLoginVO.getAccessToken())) {
            throw new BizException("QQ授权信息缺失，请重新授权");
        }
        Map<String, String> qqData = new HashMap<>(1);
        qqData.put(SocialLoginConst.ACCESS_TOKEN, qqLoginVO.getAccessToken());
        try {
            String result = restTemplate.getForObject(qqConfigProperties.getCheckTokenUrl(), String.class, qqData);
            QQTokenDTO qqTokenDTO = JSON.parseObject(CommonUtils.getBracketsContent(Objects.requireNonNull(result)), QQTokenDTO.class);
            if (Objects.isNull(qqTokenDTO) || !StringUtils.hasText(qqTokenDTO.getOpenid())) {
                log.warn("QQ token校验未返回openid，腾讯响应：{}", result);
                throw new BizException("QQ授权校验失败，请重新授权");
            }
            if (!StringUtils.hasText(qqLoginVO.getOpenId())) {
                // QQ implicit授权回调可能只返回access_token，需要从QQ校验结果中回填openId。
                qqLoginVO.setOpenId(qqTokenDTO.getOpenid());
            }
            if (!qqLoginVO.getOpenId().equals(qqTokenDTO.getOpenid())) {
                log.warn("QQ openId不一致，请求openId={}，腾讯openid={}", qqLoginVO.getOpenId(), qqTokenDTO.getOpenid());
                throw new BizException("QQ授权信息不一致，请重新授权");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("QQ token校验异常", e);
            throw new BizException("QQ授权校验失败，请重新授权");
        }
    }

}
