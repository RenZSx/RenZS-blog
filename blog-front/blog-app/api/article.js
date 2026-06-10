import request from './request'

/**
 * 文章列表(分页)
 */
export function getArticles(current = 1, size = 10) {
  return request({
    url: '/articles',
    method: 'GET',
    data: { current, size }
  })
}

/**
 * 文章详情
 */
export function getArticle(id) {
  return request({
    url: `/articles/${id}`,
    method: 'GET'
  })
}

/**
 * 文章点赞(切换)
 */
export function likeArticle(id) {
  return request({
    url: `/articles/${id}/like`,
    method: 'POST'
  })
}

/**
 * 按分类/标签查询文章
 */
export function getArticlesByCondition({ current = 1, categoryId, tagId }) {
  return request({
    url: '/articles/condition',
    method: 'GET',
    data: { current, categoryId, tagId }
  })
}

/**
 * 搜索文章
 */
export function searchArticles(keywords, current = 1) {
  return request({
    url: '/articles/search',
    method: 'GET',
    data: { keywords, current }
  })
}

/**
 * 分类列表
 */
export function getCategories() {
  return request({ url: '/categories', method: 'GET' })
}

/**
 * 标签列表
 */
export function getTags() {
  return request({ url: '/tags', method: 'GET' })
}

/**
 * 收藏文章(切换:已收藏则取消)
 */
export function collectArticle(id) {
  return request({ url: `/articles/${id}/collect`, method: 'POST' })
}

/**
 * 取消收藏
 */
export function uncollectArticle(id) {
  return request({ url: `/articles/${id}/collect`, method: 'DELETE' })
}

/**
 * 我的收藏列表
 */
export function getMyCollects(current = 1, size = 10) {
  return request({
    url: '/user/collects',
    method: 'GET',
    data: { current, size }
  })
}
