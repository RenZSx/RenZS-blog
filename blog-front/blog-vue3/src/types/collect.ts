export interface CollectArticleItem {
  id: number
  articleId: number
  articleTitle: string
  articleCover: string
  createTime: string
}

export interface GetCollectArticlesParams {
  current: number
  size?: number
}

export interface GetCollectArticlesResponse {
  data: {
    recordList: CollectArticleItem[]
    count: number
  }
}
