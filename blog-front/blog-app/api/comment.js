import request from './request'

/**
 * 评论列表
 * @param {Object} params { current, type, routePath? }
 *   type: 1=文章, 2=友链, 3=说说, 4=留言, 5=关于
 *   routePath: 文章评论用 /articles/{id}
 */
export function getComments(params) {
  return request({
    url: '/comments',
    method: 'GET',
    data: params
  })
}

/**
 * 发表评论
 * @param {Object} data { content, type, routePath?, parentId?, replyId? }
 */
export function postComment(data) {
  return request({
    url: '/comments',
    method: 'POST',
    data
  })
}

/**
 * 评论点赞
 */
export function likeComment(id) {
  return request({
    url: `/comments/${id}/like`,
    method: 'POST'
  })
}

/**
 * 二级回复列表
 */
export function getCommentReplies(cid, current = 1, size = 5) {
  return request({
    url: `/comments/${cid}/replies`,
    method: 'GET',
    data: { current, size }
  })
}
