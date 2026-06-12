package com.chen.blog.module.user.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.blogInfo.service.BlogInfoService;
import com.chen.blog.module.blogInfo.vo.WebsiteConfigVO;
import com.chen.blog.module.user.dto.UserAreaDTO;
import com.chen.blog.module.user.dto.UserBackDTO;
import com.chen.blog.module.user.dto.LoginUserDTO;
import com.chen.blog.module.user.strategy.context.SocialLoginStrategyContext;
import com.chen.blog.module.user.strategy.impl.QQLoginStrategyImpl;
import com.chen.blog.module.user.vo.*;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.module.user.service.UserAuthService;
import com.chen.blog.common.constant.CommonConst;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.module.user.entity.UserInfo;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.rbac.entity.UserRole;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.enums.RoleEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.rbac.dao.UserRoleDao;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.chen.blog.common.constant.CommonConst.*;
import static com.chen.blog.common.constant.RedisPrefixConst.*;
import static com.chen.blog.common.enums.UserAreaTypeEnum.getUserAreaType;

/**
 * 用户账号服务
 *
 * @author chenfuyun
 * @date 2021/08/10
 */

@Service
public class UserAuthServiceImpl extends ServiceImpl<UserAuthDao, UserAuth> implements UserAuthService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserAuthDao userAuthDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private BlogInfoService blogInfoService;
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SocialLoginStrategyContext socialLoginStrategyContext;
    @Autowired
    private QQLoginStrategyImpl qqLoginStrategy;

    @Override
    public List<UserAreaDTO> listUserAreas(ConditionVO conditionVO) {
        List<UserAreaDTO> userAreaDTOList = new ArrayList<>();
        switch (Objects.requireNonNull(getUserAreaType(conditionVO.getType()))) {
            case USER:
                // 查询注册用户区域分布
                Object userArea = redisService.get(USER_AREA);
                if (Objects.nonNull(userArea)) {
                    userAreaDTOList = JSON.parseObject(userArea.toString(), List.class);
                }
                return userAreaDTOList;
            case VISITOR:
                // 查询游客区域分布
                Map<String, Object> visitorArea = redisService.hGetAll(VISITOR_AREA);
                if (Objects.nonNull(visitorArea)) {
                    userAreaDTOList = visitorArea.entrySet().stream()
                            .map(item -> UserAreaDTO.builder()
                                    .name(item.getKey())
                                    .value(Long.valueOf(item.getValue().toString()))
                                    .build())
                            .collect(Collectors.toList());
                }
                return userAreaDTOList;
            default:
                break;
        }
        return userAreaDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(UserVO user) {
        WebsiteConfigVO websiteConfig = blogInfoService.getWebsiteConfig();
        if (Objects.nonNull(websiteConfig.getIsEmailRegister()) && websiteConfig.getIsEmailRegister().equals(FALSE)) {
            throw new BizException("邮箱注册已关闭");
        }
        //校验账号是否合法
        //检查验证码是否正确、用户名是否被注册
        //用户名不可重复（昵称不重复）
        if (checkUser(user)) {
            throw new BizException("邮箱已被注册！");
        }
        // 新增用户信息（邮箱、昵称、头像（默认头像））
        UserInfo userInfo = UserInfo.builder()
                .email(user.getUsername())
                .nickname(CommonConst.DEFAULT_NICKNAME + IdWorker.getId())
                .avatar(blogInfoService.getWebsiteConfig().getUserAvatar())
                .build();
        userInfoDao.insert(userInfo);
        // 绑定用户角色
        UserRole userRole = UserRole.builder()
                .userId(userInfo.getId())
                //注册为用户
                .roleId(RoleEnum.USER.getRoleId())
                .build();
        userRoleDao.insert(userRole);
        // 新增用户账号
        UserAuth userAuth = UserAuth.builder()
                .userInfoId(userInfo.getId())
                .username(user.getUsername())
                .password(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()))
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();
        userAuthDao.insert(userAuth);
    }

    /**
     * 修改密码进行邮箱验证
     * @param user 用户对象
     */
    @Override
    public void updatePassword(UserVO user) {
        // 校验账号是否合法
        if (!checkUser(user)) {
            throw new BizException("邮箱尚未注册！");
        }
        // 根据用户名修改密码
        userAuthDao.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getPassword, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()))
                .eq(UserAuth::getUsername, user.getUsername()));
    }

    /**
     * 更新密码
     * @param passwordVO 密码对象
     */
    @Override
    public void updateAdminPassword(PasswordVO passwordVO) {
        // 查询旧密码是否正确
        UserAuth user = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getId, UserUtils.getLoginUser().getId()));
        // 正确则修改密码，错误则提示不正确
        if (Objects.nonNull(user) && BCrypt.checkpw(passwordVO.getOldPassword(), user.getPassword())) {
            UserAuth userAuth = UserAuth.builder()
                    .id(UserUtils.getLoginUser().getId())
                    .password(BCrypt.hashpw(passwordVO.getNewPassword(), BCrypt.gensalt()))
                    .build();
            userAuthDao.updateById(userAuth);
        } else {
            throw new BizException("旧密码不正确");
        }
    }

    @Override
    public PageResult<UserBackDTO> listUserBackDTO(ConditionVO condition) {
        // 获取后台用户数量
        Integer count = userAuthDao.countUser(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 获取后台用户列表
        List<UserBackDTO> userBackDTOList = userAuthDao.listUsers(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(userBackDTOList, count);
    }

    /**
     * qq登录
     * @param qqLoginVO qq登录信息
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginUserDTO qqLogin(QQLoginVO qqLoginVO) {
        return socialLoginStrategyContext.executeLoginStrategy(JSON.toJSONString(qqLoginVO), LoginTypeEnum.QQ);
    }

    /**
     * 绑定QQ登录凭证到当前登录用户。
     *
     * @param qqLoginVO QQ授权信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void bindQq(QQLoginVO qqLoginVO) {
        qqLoginStrategy.validateToken(qqLoginVO);
        Integer currentUserInfoId = UserUtils.getLoginUser().getUserInfoId();
        UserAuth qqAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getUsername, qqLoginVO.getOpenId())
                .eq(UserAuth::getLoginType, LoginTypeEnum.QQ.getType()));
        if (Objects.nonNull(qqAuth) && !Objects.equals(qqAuth.getUserInfoId(), currentUserInfoId)) {
            throw new BizException("该QQ已绑定其他账号");
        }
        if (Objects.isNull(qqAuth)) {
            // 绑定只新增凭证行，资料、角色等主体数据继续归属当前 userInfoId。
            userAuthDao.insert(UserAuth.builder()
                    .userInfoId(currentUserInfoId)
                    .username(qqLoginVO.getOpenId())
                    .password(qqLoginVO.getAccessToken())
                    .loginType(LoginTypeEnum.QQ.getType())
                    .build());
        }
    }

    /**
     * 微博登录
     * @param weiboLoginVO 微博登录信息
     * @return
     */
    @Transactional(rollbackFor = BizException.class)
    @Override
    public LoginUserDTO weiboLogin(WeiboLoginVO weiboLoginVO) {
        return socialLoginStrategyContext.executeLoginStrategy(JSON.toJSONString(weiboLoginVO), LoginTypeEnum.WEIBO);
    }

    @Transactional(rollbackFor = BizException.class)
    @Override
    public LoginUserDTO giteeLogin(GiteeLoginVO giteeLoginVO) {
        return socialLoginStrategyContext.executeLoginStrategy(JSON.toJSONString(giteeLoginVO), LoginTypeEnum.GITEE);
    }

    /**
     * 校验用户数据是否合法
     *
     * @param user 用户数据
     * @return 结果
     */
    private Boolean checkUser(UserVO user) {
        if (!user.getCode().equals(redisService.get(USER_CODE_KEY + user.getUsername()))) {
            throw new BizException("验证码错误！");
        }
        //查询用户名是否存在
        UserAuth userAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .select(UserAuth::getUsername)
                .eq(UserAuth::getUsername, user.getUsername()));
        return Objects.nonNull(userAuth);
    }

    /**
     * 统计用户地区
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void statisticalUserArea() {
        // 统计用户地域分布
        Map<String, Long> userAreaMap = userAuthDao.selectList(new LambdaQueryWrapper<UserAuth>().select(UserAuth::getIpSource))
                .stream()
                .map(item -> {
                    if (StringUtils.isNotBlank(item.getIpSource())) {
                        return item.getIpSource().substring(0, 2)
                                .replaceAll(PROVINCE, "")
                                .replaceAll(CITY, "");
                    }
                    return UNKNOWN;
                })
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()));
        // 转换格式
        List<UserAreaDTO> userAreaList = userAreaMap.entrySet().stream()
                .map(item -> UserAreaDTO.builder()
                        .name(item.getKey())
                        .value(item.getValue())
                        .build())
                .collect(Collectors.toList());
        redisService.set(USER_AREA, JSON.toJSONString(userAreaList));
    }

}
