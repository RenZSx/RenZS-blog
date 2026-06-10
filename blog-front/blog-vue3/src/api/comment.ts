import request from './request'

// 评论列表
export function getComments(params: {
  current: number
  type: number
  routePath?: string
}) {
  return request.get('/api/comments', { params })
}

// 发表评论
export function submitComment(data: {
  content: string
  type: number
  routePath?: string
  parentId?: number
  replyId?: number
}) {
  return request.post('/api/comments', data)
}

// 评论点赞
export function likeComment(id: number) {
  return request.post(`/api/comments/${id}/like`)
}

// 回复列表
export function getReplies(commentId: number, params: { current: number; size: number }) {
  return request.get(`/api/comments/${commentId}/replies`, { params })
}

// 最新评论
export function getNewestComments() {
  return request.get('/api/newComments')
}
