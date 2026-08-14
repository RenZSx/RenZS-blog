import request from '@/utils/request'

// 查询用户列表
export function listUsers(query) {
  return request({
    url: '/admin/users',
    method: 'get',
    params: query
  })
}

// 查询在线用户列表
export function listOnlineUsers(query) {
  return request({
    url: '/admin/users/online',
    method: 'get',
    params: query
  })
}

// 修改用户状态
export function updateUserStatus(data) {
  return request({
    url: '/admin/users/status',
    method: 'put',
    data: data
  })
}

// 修改用户角色
export function updateUserRole(data) {
  return request({
    url: '/admin/users/role',
    method: 'put',
    data: data
  })
}

// 下线用户
export function removeOnlineUser(userId) {
  return request({
    url: '/admin/users/online/' + userId,
    method: 'delete'
  })
}
