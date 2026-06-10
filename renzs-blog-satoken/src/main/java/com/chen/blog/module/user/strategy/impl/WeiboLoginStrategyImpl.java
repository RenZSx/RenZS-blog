package com.chen.blog.module.user.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.chen.blog.common.constant.SocialLoginConst;
import com.chen.blog.module.user.dto.SocialTokenDTO;
import com.chen.blog.module.user.dto.SocialUserInfoDTO;
import com.chen.blog.module.user.dto.WeiboTokenDTO;
import com.chen.blog.module.user.dto.WeiboUserInfoDTO;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.enums.StatusCodeEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.config.WeiboConfigProperties;
import com.chen.blog.module.user.vo.WeiboLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 微博登录策略实现
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Service("weiboLoginStrategyImpl")
@Slf4j
public class WeiboLoginStrategyImpl extends AbstractSocialLoginStrategyImpl {
    @Autowired
    private WeiboConfigProperties weiboConfigProperties;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public SocialTokenDTO getSocialToken(String data) {
        WeiboLoginVO weiBoLoginVO = JSON.parseObject(data, WeiboLoginVO.class);
        // 获取微博token信息
        WeiboTokenDTO weiboToken = getWeiboToken(weiBoLoginVO);
        // 返回token信息
        return SocialTokenDTO.builder()
                .openId(weiboToken.getUid())
                .accessToken(weiboToken.getAccess_token())
                .loginType(LoginTypeEnum.WEIBO.getType())
                .build();
    }

    @Override
    public SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO) {
        // 定义请求参数
        Map<String, String> data = new HashMap<>(2);
        data.put(SocialLoginConst.UID, socialTokenDTO.getOpenId());
        data.put(SocialLoginConst.ACCESS_TOKEN, socialTokenDTO.getAccessToken());
        // 获取微博用户信息
        WeiboUserInfoDTO weiboUserInfoDTO = restTemplate.getForObject(weiboConfigProperties.getUserInfoUrl(), WeiboUserInfoDTO.class, data);
        // 返回用户信息
        return SocialUserInfoDTO.builder()
                .nickname(Objects.requireNonNull(weiboUserInfoDTO).getScreen_name())
                .avatar(weiboUserInfoDTO.getAvatar_hd())
                .build();
    }

    /**
     * 获取微博token信息
     *
     * @param weiBoLoginVO 微博登录信息
     * @return {@link WeiboTokenDTO} 微博token
     */
    private WeiboTokenDTO getWeiboToken(WeiboLoginVO weiBoLoginVO) {
        // 构建包含查询参数的URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(weiboConfigProperties.getAccessTokenUrl())
                .queryParam(SocialLoginConst.CLIENT_ID, weiboConfigProperties.getAppId())
                .queryParam(SocialLoginConst.CLIENT_SECRET, weiboConfigProperties.getAppSecret())
                .queryParam(SocialLoginConst.GRANT_TYPE, weiboConfigProperties.getGrantType())
                .queryParam(SocialLoginConst.REDIRECT_URI, weiboConfigProperties.getRedirectUrl())
                .queryParam(SocialLoginConst.CODE, weiBoLoginVO.getCode());
        // 创建空的请求体
        HttpEntity<Void> requestEntity = new HttpEntity<>(null);
        try {
            ResponseEntity<WeiboTokenDTO> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.POST,
                    requestEntity,
                    WeiboTokenDTO.class
            );
            return response.getBody();
        }catch (Exception e) {
            throw new BizException(StatusCodeEnum.WEIBO_LOGIN_ERROR);
        }
    }
//    private WeiboTokenDTO getWeiboToken(WeiboLoginVO weiBoLoginVO) {
//        // 根据code换取微博uid和accessToken
//        MultiValueMap<String, String> weiboData = new LinkedMultiValueMap<>();
//        // 定义微博token请求参数
//        weiboData.add(SocialLoginConst.CLIENT_ID, weiboConfigProperties.getAppId());
//        weiboData.add(SocialLoginConst.CLIENT_SECRET, weiboConfigProperties.getAppSecret());
//        weiboData.add(SocialLoginConst.GRANT_TYPE, weiboConfigProperties.getGrantType());
//        weiboData.add(SocialLoginConst.REDIRECT_URI, weiboConfigProperties.getRedirectUrl());
//        weiboData.add(SocialLoginConst.CODE, weiBoLoginVO.getCode());
//        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(weiboData, null);
//        try {
//            return restTemplate.exchange(weiboConfigProperties.getAccessTokenUrl(), HttpMethod.POST, requestEntity, WeiboTokenDTO.class).getBody();
//        } catch (Exception e) {
//            throw new BizException(StatusCodeEnum.WEIBO_LOGIN_ERROR);
//        }
//    }
}
