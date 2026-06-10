package com.chen.blog.module.rbac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.rbac.dao.UserRoleDao;
import com.chen.blog.module.rbac.service.UserRoleService;
import com.chen.blog.module.rbac.entity.UserRole;
import org.springframework.stereotype.Service;


/**
 * 用户角色服务
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleDao, UserRole> implements UserRoleService {
}
