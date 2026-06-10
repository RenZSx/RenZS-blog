import request from './request'

/**
 * 通知列表(分页)
 * @param {Object} params { current, size?, noticeType? }
 *   noticeType:'reply' | 'like' | 'system' | undefined(全部)
 */
export function getNotices(params = {}) {
  return request({
    url: '/notices',
    method: 'GET',
    data: params
  })
}

/**
 * 未读数
 * @returns {Promise<{data: number}>}
 */
export function getUnreadCount() {
  return request({
    url: '/notices/unread-count',
    method: 'GET'
  })
}

/**
 * 标记单条通知为已读
 * @param {number} id 通知 ID
 * @param {string} [noticeType] 通知类型,系统通知需要传 'system'
 */
export function markNoticeRead(id, noticeType) {
  return request({
    url: `/notices/${id}/read`,
    method: 'PUT',
    data: noticeType ? { noticeType } : undefined
  })
}

/**
 * 全部标记已读
 */
export function markAllRead() {
  return request({
    url: '/notices/read-all',
    method: 'PUT'
  })
}
