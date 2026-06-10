package com.chen.blog.common.satoken;

import com.chen.blog.module.rbac.dao.RoleDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link StpInterfaceImpl} 单元测试
 * 仅 mock RoleDao,验证 sa-token 扩展点契约
 *
 * @author chen
 */
@ExtendWith(MockitoExtension.class)
class StpInterfaceImplTest {

    /**
     * 被 mock 的角色 DAO
     */
    @Mock
    private RoleDao roleDao;

    /**
     * 被测对象,Mockito 会自动注入上面的 mock
     */
    @InjectMocks
    private StpInterfaceImpl stpInterface;

    /**
     * 角色列表应直接由 RoleDao 返回
     */
    @Test
    void getRoleList_should_return_roles_from_dao() {
        // 准备 mock 数据
        when(roleDao.listRolesByUserInfoId(1)).thenReturn(Arrays.asList("admin", "user"));

        // 执行
        List<String> roles = stpInterface.getRoleList(1, "login");

        // 断言
        assertEquals(2, roles.size());
        assertTrue(roles.contains("admin"));
    }

    /**
     * 本项目不使用细粒度权限,getPermissionList 始终返回空
     */
    @Test
    void getPermissionList_should_always_be_empty() {
        List<String> perms = stpInterface.getPermissionList(1, "login");
        assertTrue(perms.isEmpty());
    }
}
