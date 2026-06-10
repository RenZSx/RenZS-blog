import request from './request'

/**
 * 关于页 Markdown 文本
 */
export function getAbout() {
  return request({
    url: '/about',
    method: 'GET'
  })
}

/**
 * 网站首页基础信息(博主资料、公告等)
 */
export function getBlogInfo() {
  return request({
    url: '/',
    method: 'GET'
  })
}
