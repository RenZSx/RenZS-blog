package com.chen.blog.common.satoken;

import com.chen.blog.module.rbac.dao.RoleDao;
import com.chen.blog.module.user.dto.ResourceRoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 资源-角色映射缓存(替代 FilterInvocationSecurityMetadataSourceImpl)
 * 启动时从 tb_resource 表加载,权限变更后调用 refresh() 刷新
 */
@Component
public class ResourceRoleCache {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Autowired
    private RoleDao roleDao;

    private volatile List<ResourceRoleDTO> resourceRoleList;

    @PostConstruct
    public void init() {
        refresh();
    }

    /** 权限变更后由角色/资源管理 Controller 调用,等价于原 clearDataSource() */
    public synchronized void refresh() {
        this.resourceRoleList = roleDao.listResourceRoles();
    }

    /**
     * 匹配请求,返回需要的角色列表
     * @return null = 未配置该 URL(默认放行,与原行为一致);empty list = 任意已登录用户可访问;非空 list = 需要其中任一角色
     */
    public List<String> matchRequiredRoles(String url, String method) {
        if (CollectionUtils.isEmpty(resourceRoleList)) {
            return null;
        }
        for (ResourceRoleDTO r : resourceRoleList) {
            if (r.getRequestMethod() != null
                    && r.getRequestMethod().equalsIgnoreCase(method)
                    && MATCHER.match(r.getUrl(), url)) {
                return r.getRoleList();
            }
        }
        return null;
    }
}
