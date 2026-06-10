package com.chen.blog.module.user.service;

import com.chen.blog.module.user.dto.UserOnlineDTO;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.module.user.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.module.user.vo.EmailVO;
import com.chen.blog.module.user.vo.UserDisableVO;
import com.chen.blog.module.user.vo.UserInfoVO;
import com.chen.blog.module.user.vo.UserRoleVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户信息服务
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 修改用户资料
     *
     * @param userInfoVO 用户资料
     */
    void updateUserInfo(UserInfoVO userInfoVO);

    /**
     * 修改用户头像
     *
     * @param file 头像图片
     * @return 头像地址
     */
    String updateUserAvatar(MultipartFile file);

    /**
     * 使用链接修改用户头像
     *
     * @param avatar 头像链接
     * @return 头像地址
     */
    String updateUserAvatar(String avatar);

    /**
     * 绑定用户邮箱
     *
     * @param emailVO 邮箱
     */
    void saveUserEmail(EmailVO emailVO);

    /**
     * 更新用户角色
     *
     * @param userRoleVO 更新用户角色
     */
    void updateUserRole(UserRoleVO userRoleVO);

    /**
     * 修改用户禁用状态
     *
     * @param userDisableVO 用户禁用信息
     */
    void updateUserDisable(UserDisableVO userDisableVO);

    /**
     * 查看在线用户列表
     *
     * @param conditionVO 条件
     * @return 在线用户列表
     */
    PageResult<UserOnlineDTO> listOnlineUsers(ConditionVO conditionVO);

    /**
     * 下线用户
     *
     * @param userInfoId 用户信息id
     */
    void removeOnlineUser(Integer userInfoId);

}

