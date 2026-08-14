import request from '@/utils/request'

// 查询评论列表
export function listComments(query) {
  return request({
    url: '/admin/comments',
    method: 'get',
    params: query
  })
}

// 删除评论
export function deleteComments(commentIds) {
  return request({
    url: '/admin/comments',
    method: 'delete',
    data: commentIds
  })
}

// 审核评论
export function updateCommentsReview(data) {
  return request({
    url: '/admin/comments/review',
    method: 'put',
    data: data
  })
}
