import request from '@/utils/request'

// 查询纪念页配置
export function getLoveConfig() {
  return request({
    url: '/admin/love/config',
    method: 'get'
  })
}

// 更新纪念页配置
export function updateLoveConfig(data) {
  return request({
    url: '/admin/love/config',
    method: 'put',
    data: data
  })
}

// 查询飞书传信
export function getLoveLetter() {
  return request({
    url: '/admin/love/letter',
    method: 'get'
  })
}

// 更新飞书传信
export function updateLoveLetter(data) {
  return request({
    url: '/admin/love/letter',
    method: 'put',
    data: data
  })
}
