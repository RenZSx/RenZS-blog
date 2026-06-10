package com.chen.blog.common.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.chen.blog.module.rbac.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * sa-token 权限/角色查询实现
 * 角色直接复用 RoleDao.listRolesByUserInfoId(),零转换成本
 *
 * @author chen
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 角色 DAO,提供基于 userInfoId 的角色标签查询
     */
    @Autowired
    private RoleDao roleDao;

    /**
     * 返回登录账号的权限列表
     * 本项目按角色鉴权,不使用细粒度权限,因此返回空列表
     *
     * @param loginId   登录账号 id(此处为 userInfoId)
     * @param loginType 账号体系类型
     * @return 空权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目按角色鉴权,不使用细粒度权限
        return Collections.emptyList();
    }

    /**
     * 返回登录账号的角色列表
     * 直接复用现有 RoleDao 查询,返回类型即为 List<String>(role_label)
     *
     * @param loginId   登录账号 id(此处为 userInfoId)
     * @param loginType 账号体系类型
     * @return 角色标签列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Integer userInfoId = Integer.parseInt(loginId.toString());
        return roleDao.listRolesByUserInfoId(userInfoId);
    }
}
