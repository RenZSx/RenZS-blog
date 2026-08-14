import request from '@/utils/request'

// 查询角色列表
export function listRoles(query) {
  return request({
    url: '/admin/roles',
    method: 'get',
    params: query
  })
}

// 查询角色详情
export function getRole(roleId) {
  return request({
    url: '/admin/roles/' + roleId,
    method: 'get'
  })
}

// 新增或修改角色
export function saveOrUpdateRole(data) {
  return request({
    url: '/admin/roles',
    method: 'post',
    data: data
  })
}

// 删除角色
export function deleteRoles(roleIds) {
  return request({
    url: '/admin/roles',
    method: 'delete',
    data: roleIds
  })
}
