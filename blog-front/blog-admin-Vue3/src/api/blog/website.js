import request from '@/utils/request'

// 查询网站配置
export function getWebsiteConfig() {
  return request({
    url: '/admin/website/config',
    method: 'get'
  })
}

// 更新网站配置
export function updateWebsiteConfig(data) {
  return request({
    url: '/admin/website/config',
    method: 'put',
    data: data
  })
}

// 查询关于我信息(公开接口, 无需 /admin 前缀)
export function getAbout() {
  return request({
    url: '/about',
    method: 'get'
  })
}

// 更新关于我信息
export function updateAbout(data) {
  return request({
    url: '/admin/about',
    method: 'put',
    data: data
  })
}
