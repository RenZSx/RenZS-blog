export interface HistoryItem {
  id: number
  articleId: number
  articleTitle: string
  articleCover: string
  progressPercent: number
  lastReadTime: string
}

export interface GetHistoryListParams {
  current: number
  size?: number
}

export interface GetHistoryListResponse {
  data: {
    recordList: HistoryItem[]
    count: number
  }
}
