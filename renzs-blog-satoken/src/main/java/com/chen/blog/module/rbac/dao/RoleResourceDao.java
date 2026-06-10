package com.chen.blog.module.rbac.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.rbac.entity.RoleResource;
import org.springframework.stereotype.Repository;


/**
 * 角色资源
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Repository
public interface RoleResourceDao extends BaseMapper<RoleResource> {
}
