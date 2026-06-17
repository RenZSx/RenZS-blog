package com.chen.blog.module.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.dto.UserOnlineDTO;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.user.vo.EmailVO;
import com.chen.blog.module.user.vo.UserDisableVO;
import com.chen.blog.module.user.vo.UserInfoVO;
import com.chen.blog.module.user.vo.UserRoleVO;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.module.user.service.UserInfoService;
import com.chen.blog.module.rbac.service.UserRoleService;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.user.entity.UserInfo;
import com.chen.blog.module.rbac.entity.UserRole;
import com.chen.blog.common.enums.FilePathEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.chen.blog.common.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.chen.blog.common.constant.RedisPrefixConst.USER_CODE_KEY;
import static com.chen.blog.common.util.PageUtils.*;
import static com.chen.blog.common.util.PageUtils.getLimitCurrent;


/**
 * 用户信息服务
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoDao, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    @Autowired
    private UserAuthDao userAuthDao;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserInfo(UserInfoVO userInfoVO) {
        // 封装用户信息
        UserInfo userInfo = UserInfo.builder()
                .id(UserUtils.getLoginUser().getUserInfoId())
                .nickname(userInfoVO.getNickname())
                .intro(userInfoVO.getIntro())
                .webSite(userInfoVO.getWebSite())
                .build();
        userInfoDao.updateById(userInfo);
    }

    /**
     * 判断是否允许修正历史邮箱凭证归属。
     *
     * @param currentUserInfoId 当前资料账号ID
     * @param email             绑定邮箱
     * @return true 表示当前 QQ 资料账号已拥有该邮箱，可以迁移邮箱登录凭证
     */
    private boolean canRepairHistoricalEmailOwner(Integer currentUserInfoId, String email) {
        UserInfo currentUserInfo = userInfoDao.selectById(currentUserInfoId);
        if (Objects.isNull(currentUserInfo) || !Objects.equals(currentUserInfo.getEmail(), email)) {
            return false;
        }
        UserAuth qqAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .select(UserAuth::getId)
                .eq(UserAuth::getUserInfoId, currentUserInfoId)
                .eq(UserAuth::getLoginType, LoginTypeEnum.QQ.getType()));
        return Objects.nonNull(qqAuth);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String updateUserAvatar(MultipartFile file) {
        // 头像上传
        String avatar = uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.AVATAR.getPath());
        // 更新用户信息
        UserInfo userInfo = UserInfo.builder()
                .id(UserUtils.getLoginUser().getUserInfoId())
                .avatar(avatar)
                .build();
        userInfoDao.updateById(userInfo);
        return avatar;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String updateUserAvatar(String avatar) {
        UserInfo userInfo = UserInfo.builder()
                .id(UserUtils.getLoginUser().getUserInfoId())
                .avatar(avatar.trim())
                .build();
        userInfoDao.updateById(userInfo);
        return avatar.trim();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUserEmail(EmailVO emailVO) {
        Object cachedCode = redisService.get(USER_CODE_KEY + emailVO.getEmail());
        if (Objects.isNull(cachedCode) || !emailVO.getCode().equals(cachedCode.toString())) {
            throw new BizException("验证码错误！");
        }
        UserDetailDTO loginUser = UserUtils.getLoginUser();
        Integer currentUserInfoId = loginUser.getUserInfoId();
        UserAuth emailAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getUsername, emailVO.getEmail())
                .eq(UserAuth::getLoginType, LoginTypeEnum.EMAIL.getType()));
        if (Objects.nonNull(emailAuth) && !Objects.equals(emailAuth.getUserInfoId(), currentUserInfoId)) {
            if (!canRepairHistoricalEmailOwner(currentUserInfoId, emailVO.getEmail())) {
                throw new BizException("该邮箱已绑定其他账号");
            }
            UserAuth updateAuth = UserAuth.builder()
                    .id(emailAuth.getId())
                    .userInfoId(currentUserInfoId)
                    .build();
            userAuthDao.updateById(updateAuth);
            emailAuth.setUserInfoId(currentUserInfoId);
        }
        if (Objects.isNull(emailAuth)) {
            if (StringUtils.isBlank(emailVO.getPassword())) {
                throw new BizException("请设置邮箱登录密码");
            }
            // 新邮箱绑定必须同步创建登录凭证，否则后续无法用邮箱登录同一个资料账号。
            userAuthDao.insert(UserAuth.builder()
                    .userInfoId(currentUserInfoId)
                    .username(emailVO.getEmail())
                    .password(BCrypt.hashpw(emailVO.getPassword(), BCrypt.gensalt()))
                    .loginType(LoginTypeEnum.EMAIL.getType())
                    .build());
        }
        UserInfo userInfo = UserInfo.builder()
                .id(currentUserInfoId)
                .email(emailVO.getEmail())
                .build();
        userInfoDao.updateById(userInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserRole(UserRoleVO userRoleVO) {
        // 更新用户角色和昵称
        UserInfo userInfo = UserInfo.builder()
                .id(userRoleVO.getUserInfoId())
                .nickname(userRoleVO.getNickname())
                .build();
        userInfoDao.updateById(userInfo);
        // 删除用户角色重新添加
        userRoleService.remove(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userRoleVO.getUserInfoId()));
        List<UserRole> userRoleList = userRoleVO.getRoleIdList().stream()
                .map(roleId -> UserRole.builder()
                        .roleId(roleId)
                        .userId(userRoleVO.getUserInfoId())
                        .build())
                .collect(Collectors.toList());
        userRoleService.saveBatch(userRoleList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserDisable(UserDisableVO userDisableVO) {
        // 更新用户禁用状态
        UserInfo userInfo = UserInfo.builder()
                .id(userDisableVO.getId())
                .isDisable(userDisableVO.getIsDisable())
                .build();
        userInfoDao.updateById(userInfo);
    }

    @Override
    public PageResult<UserOnlineDTO> listOnlineUsers(ConditionVO conditionVO) {
        // sa-token 版本"在线用户列表"实现:
        //  - 原 Spring Security 通过 SessionRegistry.getAllPrincipals 拿到所有登录态的 principal (UserDetailDTO)
        //  - sa-token 等价路径:遍历所有有效 token → TokenSession 中取我们登录时塞入的 UserDetailDTO
        //  - 由于允许并发登录 (is-concurrent: true),同一用户可能有多个 token,需要按 userInfoId 去重
        //  - searchTokenValue(keyword, start, size, sortType) 第二参数 -1 表示不分页全量取
        List<String> tokenList = StpUtil.searchTokenValue("", 0, -1, false);
        Map<Integer, UserOnlineDTO> onlineMap = new HashMap<>();
        for (String token : tokenList) {
            // searchTokenValue 拿回来的是带前缀的完整 Redis key,需剥掉前缀拿到 raw token 才能查 TokenSession
            String rawToken = stripTokenPrefix(token);
            // token 已过期或 loginId 已不在 → 跳过
            Object loginId = StpUtil.getLoginIdByToken(rawToken);
            if (Objects.isNull(loginId)) {
                continue;
            }
            SaSession tokenSession = StpUtil.getTokenSessionByToken(rawToken);
            if (Objects.isNull(tokenSession)) {
                continue;
            }
            UserDetailDTO detail = convertLoginUser(tokenSession.get(UserUtils.LOGIN_USER_KEY));
            if (Objects.isNull(detail) || Objects.isNull(detail.getUserInfoId())) {
                continue;
            }
            // 多 token 同账号去重:按 userInfoId 保留首个(行为与原 SessionRegistry.getAllPrincipals 去重一致)
            onlineMap.putIfAbsent(detail.getUserInfoId(),
                    JSON.parseObject(JSON.toJSONString(detail), UserOnlineDTO.class));
        }

        // 关键字过滤 + 按最近登录时间倒序
        List<UserOnlineDTO> userOnlineDTOList = onlineMap.values().stream()
                .filter(item -> StringUtils.isBlank(conditionVO.getKeywords())
                        || (Objects.nonNull(item.getNickname()) && item.getNickname().contains(conditionVO.getKeywords())))
                .sorted(Comparator.comparing(UserOnlineDTO::getLastLoginTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // 手动分页 (沿用原实现)
        int fromIndex = getLimitCurrent().intValue();
        int size = getSize().intValue();
        int toIndex = userOnlineDTOList.size() - fromIndex > size ? fromIndex + size : userOnlineDTOList.size();
        if (fromIndex > userOnlineDTOList.size()) {
            return new PageResult<>(java.util.Collections.emptyList(), userOnlineDTOList.size());
        }
        List<UserOnlineDTO> userOnlineList = userOnlineDTOList.subList(fromIndex, toIndex);
        return new PageResult<>(userOnlineList, userOnlineDTOList.size());
    }

    @Override
    public void removeOnlineUser(Integer userInfoId) {
        // sa-token 版本"踢人下线"实现:
        //  - LoginServiceImpl / AbstractSocialLoginStrategyImpl 已统一以 userInfoId 为 loginId
        //  - 故踢人直接 StpUtil.kickout(loginId) 即可,允许并发登录场景下会一并下线所有 token
        if (Objects.isNull(userInfoId)) {
            return;
        }
        StpUtil.kickout(userInfoId);
    }

    /**
     * 剥掉 searchTokenValue 返回值的 Redis key 前缀,得到 raw token
     *
     * <p>sa-token 1.39.0 的 searchTokenValue 返回形如 "satoken:login:token:xxxxxx" 的完整 Redis key,
     * 但 getLoginIdByToken / getTokenSessionByToken 需要 raw token (即最后一段)。
     *
     * @param fullKey searchTokenValue 返回的完整 key
     * @return raw token,无前缀时原样返回
     */
    private String stripTokenPrefix(String fullKey) {
        if (Objects.isNull(fullKey)) {
            return null;
        }
        int idx = fullKey.lastIndexOf(':');
        return idx >= 0 ? fullKey.substring(idx + 1) : fullKey;
    }

    /**
     * 兼容 sa-token Redis Jackson 反序列化后的登录用户对象形态。
     *
     * @param loginUserObj TokenSession 中存放的 loginUser
     * @return 用户详情 DTO,无法解析时返回 null
     */
    private UserDetailDTO convertLoginUser(Object loginUserObj) {
        if (Objects.isNull(loginUserObj)) {
            return null;
        }
        if (loginUserObj instanceof UserDetailDTO) {
            return (UserDetailDTO) loginUserObj;
        }
        return JSON.parseObject(JSON.toJSONString(loginUserObj), UserDetailDTO.class);
    }

}
