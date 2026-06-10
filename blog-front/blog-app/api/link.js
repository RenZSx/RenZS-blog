import request from './request'

/**
 * 友链列表
 */
export function getLinks() {
  return request({
    url: '/links',
    method: 'GET'
  })
}
