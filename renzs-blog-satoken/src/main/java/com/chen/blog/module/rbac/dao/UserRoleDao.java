package com.chen.blog.module.rbac.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.rbac.entity.UserRole;
import org.springframework.stereotype.Repository;


/**
 * 用户角色
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Repository
public interface UserRoleDao extends BaseMapper<UserRole> {

}
