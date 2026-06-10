import request from './request'
import type { GetCollectArticlesParams, GetCollectArticlesResponse } from '@/types/collect'

// 文章列表
export function getArticles(params: { current: number; size?: number }) {
  return request.get('/api/articles', { params })
}

// 文章详情
export function getArticle(id: number) {
  return request.get(`/api/articles/${id}`)
}

// 文章点赞
export function likeArticle(id: number) {
  return request.post(`/api/articles/${id}/like`)
}

// 搜索文章
export function searchArticles(params: { keywords: string; current: number }) {
  return request.get('/api/articles/search', { params })
}

// 文章归档
export function getArchives() {
  return request.get('/api/articles/archives')
}

// 条件查询文章
export function getArticlesByCondition(params: { current: number; categoryId?: string; tagId?: string }) {
  return request.get('/api/articles/condition', { params })
}

// 高级搜索文章
export function advancedSearchArticles(params: {
  current: number
  keywords?: string
  searchType?: string
  categoryId?: number
  tagId?: number
  startTime?: string
  endTime?: string
}) {
  return request.get('/api/search/articles', { params })
}

// 初始推荐文章
export function getInitialArticle() {
  return request.get('/api/InitialArticle')
}

// 文章收藏
export function collectArticle(id: number) {
  return request.post(`/api/articles/${id}/collect`)
}

// 取消文章收藏
export function cancelCollectArticle(id: number) {
  return request.delete(`/api/articles/${id}/collect`)
}

// 收藏文章列表
export function getCollectArticles(params: GetCollectArticlesParams = { current: 1 }) {
  return request.get<GetCollectArticlesResponse>('/api/user/collects', {
    params
  })
}
