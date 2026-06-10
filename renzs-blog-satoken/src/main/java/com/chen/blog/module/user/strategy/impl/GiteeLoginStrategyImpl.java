package com.chen.blog.module.user.strategy.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chen.blog.common.config.GiteeConfigProperties;
import com.chen.blog.common.constant.SocialLoginConst;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.enums.StatusCodeEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.HttpUtils;
import com.chen.blog.module.user.dto.GiteeTokenDTO;
import com.chen.blog.module.user.dto.SocialTokenDTO;
import com.chen.blog.module.user.dto.SocialUserInfoDTO;
import com.chen.blog.module.user.dto.WeiboTokenDTO;
import com.chen.blog.module.user.vo.GiteeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ChenFY
 * @date 2025/11/14 11:29
 */
@Service("giteeLoginStrategyImpl")
@Slf4j
public class GiteeLoginStrategyImpl extends AbstractSocialLoginStrategyImpl{

    @Autowired
    private GiteeConfigProperties giteeConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取第三方登录信息
     *
     * @param data 数据
     * @return {@link SocialTokenDTO}
     */
    @Override
    public SocialTokenDTO getSocialToken(String data) {
        // 解析json
        GiteeLoginVO giteeLoginVO = JSON.parseObject(data, GiteeLoginVO.class);
        // 获取Gitee token
        GiteeTokenDTO giteeToken = getGiteeToken(giteeLoginVO);

        return SocialTokenDTO.builder()
                .openId(giteeConfigProperties.getAppId())
                .accessToken(giteeToken.getAccess_token())
                .loginType(LoginTypeEnum.WEIBO.getType())
                .build();
    }

    /**
     * 获取微博token信息
     *
     * @param giteeLoginVO gitee登录信息
     * @return {@link WeiboTokenDTO} 微博token
     */
    private GiteeTokenDTO getGiteeToken(GiteeLoginVO giteeLoginVO) {
        Map<String, String> map = new HashMap();
        map.put(SocialLoginConst.CLIENT_ID,giteeConfigProperties.getAppId());
        map.put(SocialLoginConst.REDIRECT_URI,giteeConfigProperties.getRedirectUrl());
        map.put(SocialLoginConst.CLIENT_SECRET, giteeConfigProperties.getAppSecret());
        map.put(SocialLoginConst.CODE,giteeLoginVO.getCode());
        map.put(SocialLoginConst.GRANT_TYPE, giteeConfigProperties.getGrantType());
        try {
            HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post",  new HashMap<>(), map, new HashMap<>());
            String string = EntityUtils.toString(response.getEntity());
            GiteeTokenDTO giteeTokenDTO = JSON.parseObject(string, GiteeTokenDTO.class);
            return giteeTokenDTO;
        } catch (Exception ex) {
            throw new BizException(StatusCodeEnum.GITEE_TOKEN_ERROR);
        }

    }
    /**
     * 获取第三方用户信息
     *
     * @param socialTokenDTO 第三方token信息
     * @return {@link SocialUserInfoDTO}
     */
    @Override
    public SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO) {
        Map<String, String> query = new HashMap<>();
        query.put("access_token",socialTokenDTO.getAccessToken());
        try {
            HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), query);
            String json = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(json);
            String name = jsonObject.get("name").toString();
            String avatarUrl = jsonObject.get("avatar_url").toString();
            return SocialUserInfoDTO.builder()
                    .avatar(avatarUrl)
                    .nickname(name)
                    .build();
        } catch (Exception e) {
            log.error("获取Gitee用户信息失败: {}", e.getMessage());
            throw new BizException(StatusCodeEnum.GITEE_LOGIN_ERROR);
        }
    }
}
