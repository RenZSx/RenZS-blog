import request from './request'
import type { GetHistoryListParams, GetHistoryListResponse } from '@/types/history'

export function saveArticleHistory(articleId: number, progressPercent: number) {
  return request.post(`/api/articles/${articleId}/history`, null, {
    params: { progressPercent }
  })
}

export function getArticleHistoryList(params: GetHistoryListParams = { current: 1 }) {
  return request.get<GetHistoryListResponse>('/api/user/history', {
    params
  })
}

export function deleteArticleHistory(historyId: number) {
  return request.delete(`/api/user/history/${historyId}`)
}
