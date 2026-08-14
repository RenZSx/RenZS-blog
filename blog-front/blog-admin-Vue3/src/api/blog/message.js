import request from '@/utils/request'

// 查询留言列表
export function listMessages(query) {
  return request({
    url: '/admin/messages',
    method: 'get',
    params: query
  })
}

// 删除留言
export function deleteMessages(messageIds) {
  return request({
    url: '/admin/messages',
    method: 'delete',
    data: messageIds
  })
}

// 审核留言
export function updateMessagesReview(data) {
  return request({
    url: '/admin/messages/review',
    method: 'put',
    data: data
  })
}
