import request from '@/utils/request'

// 修改个人信息
export function updateUserInfo(data) {
  return request({
    url: '/users/info',
    method: 'put',
    data: data
  })
}

// 修改密码
export function updateUserPassword(data) {
  return request({
    url: '/admin/users/password',
    method: 'put',
    data: data
  })
}
