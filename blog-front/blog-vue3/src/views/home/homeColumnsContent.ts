export interface ArticleItem {
  id?: number
  articleId?: number
  articleCover?: string
  articleTitle?: string
  title?: string
  articleContent?: string
  articleDesc?: string
  createTime?: string
  categoryName?: string
  viewCount?: number
  viewsCount?: number
  likeCount?: number
}

export interface CategoryItem {
  id: number
  name: string
  count: number
}

type ApiListPayload = {
  data?: {
    data?: unknown
  }
}

function readFiniteNumber(value: unknown) {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0
}

export function readApiList(payload: ApiListPayload) {
  const data = payload?.data?.data
  if (Array.isArray(data)) return data
  if (typeof data === 'object' && data !== null && Array.isArray((data as any).recordList)) {
    return (data as any).recordList
  }
  if (typeof data === 'object' && data !== null && Array.isArray((data as any).records)) {
    return (data as any).records
  }
  return []
}

export function getArticlePath(article: ArticleItem) {
  return `/articles/${article.id || article.articleId || ''}`
}

export function getArticleTitle(article: ArticleItem) {
  return article.articleTitle || article.title || '未命名文章'
}

export function getArticleCategory(article: ArticleItem) {
  return article.categoryName || '随笔'
}

export function getArticleSummary(article: ArticleItem) {
  const source =
    article.articleDesc ||
    article.articleContent ||
    '那些在日常里被记录下来的片段，最终都会成为抵达远方的路。'

  return source
    .replace(/<\/?[^>]*>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 76)
}

export function getArticleViews(article: ArticleItem) {
  return article.viewCount ?? article.viewsCount ?? article.likeCount ?? 0
}

export function getArticleReadTime(article: ArticleItem) {
  const length = (article.articleContent || article.articleDesc || getArticleTitle(article)).length
  return `${Math.max(5, Math.ceil(length / 420))} 分钟`
}

export function getHotArticles(articles: ArticleItem[]) {
  return [...articles]
    .sort((left, right) => getArticleViews(right) - getArticleViews(left))
    .slice(0, 3)
}

export function normalizeCategoryList(items: unknown[]): CategoryItem[] {
  return items
    .filter((item: any) => typeof item?.id === 'number')
    .map((item: any) => ({
      id: item.id,
      name: item.categoryName || item.name || '未命名',
      count: item.articleCount ?? item.count ?? item.articleNum ?? 0
    }))
}

export interface ArticleSection {
  id?: number
  categoryId?: number | null
  sectionKey?: string
  sectionName?: string
  name?: string
  morePath?: string
  articleList: ArticleItem[]
}

export function normalizeSections(items: unknown[]): ArticleSection[] {
  return items
    .filter((item: any) => Array.isArray(item?.articleList) && item.articleList.length > 0)
    .map((item: any) => ({
      id: item.id ?? item.categoryId ?? undefined,
      categoryId: item.categoryId,
      sectionKey: item.sectionKey,
      sectionName: item.sectionName || item.name || item.title || '未命名专栏',
      name: item.name,
      morePath: item.morePath,
      articleList: item.articleList
    }))
}

export function getSectionHotArticles(sections: ArticleSection[]): ArticleItem[] {
  const all = sections.flatMap(s => s.articleList)
  return getHotArticles(all)
}

export function getSectionLink(section: ArticleSection): string | null {
  if (section.morePath) return section.morePath
  if (section.id != null) return `/categories/${section.id}`
  return null
}

export function shortNumber(value: unknown) {
  const count = readFiniteNumber(value)
  if (count >= 1000) {
    return `${Number((count / 1000).toFixed(count >= 10000 ? 0 : 1))}k`
  }
  return String(count)
}
