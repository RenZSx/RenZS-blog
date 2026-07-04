import request from '@/api/request'

// 获取首页文章分组
export function getHomeArticleSections() {
  return request.get('/api/articles/home/sections')
}

// 获取分类列表
export function getCategories() {
  return request.get('/api/categories')
}

// 获取最新评论
export function getNewComments() {
  return request.get('/api/newComments')
}

// 获取首页说说
export function getHomeTalks() {
  return request.get('/api/home/talks')
}

// 获取标签列表
export function getTags() {
  return request.get('/api/tags')
}

// 获取审核通过的友链列表
export function getLinks() {
  return request.get('/api/links')
}

interface FriendLinkApplyData {
  linkName: string
  linkIntro: string
  linkCover: string
  linkAddress: string
}

// 提交友链申请，后端会写入待审核状态。
export function sendFriendLinkApply(data: FriendLinkApplyData) {
  return request.post('/api/links/apply', data)
}

// 获取留言列表
export function getMessages(params: { current: number }) {
  return request.get('/api/messages', { params })
}

// 发送留言
export function sendMessage(data: { content: string }) {
  return request.post('/api/messages', data)
}
