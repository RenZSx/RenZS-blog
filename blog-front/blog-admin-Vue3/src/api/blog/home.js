import request from '@/utils/request'

// 查询后台首页数据
export function getHomeData() {
  return request({
    url: '/admin',
    method: 'get'
  })
}

// 查询用户地区分布
export function getUserArea(type) {
  return request({
    url: '/admin/users/area',
    method: 'get',
    params: { type }
  })
}

// 上报访客信息
export function reportVisitor() {
  return request({
    url: '/report',
    method: 'post'
  })
}
