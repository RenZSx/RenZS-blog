import request from './request'

/**
 * 说说列表(后端内部分页,size 由后端决定)
 * @param {number} current 页码
 */
export function getTalks(current = 1) {
  return request({
    url: '/talks',
    method: 'GET',
    data: { current }
  })
}

/**
 * 说说详情
 */
export function getTalkById(id) {
  return request({
    url: `/talks/${id}`,
    method: 'GET'
  })
}

/**
 * 说说点赞(切换)
 */
export function likeTalk(id) {
  return request({
    url: `/talks/${id}/like`,
    method: 'POST'
  })
}

/**
 * 首页说说预览(返回 List<String> 或 List<TalkDTO>,按后端实现)
 */
export function getHomeTalks() {
  return request({
    url: '/home/talks',
    method: 'GET'
  })
}
