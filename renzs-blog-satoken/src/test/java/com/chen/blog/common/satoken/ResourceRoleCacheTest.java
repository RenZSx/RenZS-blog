package com.chen.blog.common.satoken;

import com.chen.blog.module.rbac.dao.RoleDao;
import com.chen.blog.module.user.dto.ResourceRoleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceRoleCacheTest {

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private ResourceRoleCache cache;

    private ResourceRoleDTO adminResource;
    private ResourceRoleDTO userResource;

    @BeforeEach
    void setUp() {
        adminResource = new ResourceRoleDTO();
        adminResource.setUrl("/admin/**");
        adminResource.setRequestMethod("GET");
        adminResource.setRoleList(Collections.singletonList("admin"));

        userResource = new ResourceRoleDTO();
        userResource.setUrl("/users/current");
        userResource.setRequestMethod("GET");
        userResource.setRoleList(Arrays.asList("admin", "user"));

        when(roleDao.listResourceRoles()).thenReturn(Arrays.asList(adminResource, userResource));
        cache.init();
    }

    @Test
    void matchRequiredRoles_should_return_roles_when_url_matches() {
        List<String> roles = cache.matchRequiredRoles("/admin/users", "GET");
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("admin", roles.get(0));
    }

    @Test
    void matchRequiredRoles_should_return_null_when_url_not_match() {
        List<String> roles = cache.matchRequiredRoles("/unknown/path", "GET");
        assertNull(roles);
    }

    @Test
    void matchRequiredRoles_should_distinguish_method() {
        List<String> roles = cache.matchRequiredRoles("/users/current", "POST");
        assertNull(roles);
    }

    @Test
    void refresh_should_reload_from_dao() {
        ResourceRoleDTO newRes = new ResourceRoleDTO();
        newRes.setUrl("/new/path");
        newRes.setRequestMethod("GET");
        newRes.setRoleList(Collections.singletonList("admin"));
        when(roleDao.listResourceRoles()).thenReturn(Collections.singletonList(newRes));

        cache.refresh();

        assertNull(cache.matchRequiredRoles("/admin/users", "GET"));
        assertNotNull(cache.matchRequiredRoles("/new/path", "GET"));
    }
}
