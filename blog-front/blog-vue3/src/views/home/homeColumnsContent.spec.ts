import { describe, expect, it } from 'vitest'
import {
  getArticleCategory,
  getArticleLikes,
  getArticleReadTime,
  getArticleSummary,
  getArticleTitle,
  getArticleViews,
  getHotArticles,
  getSectionHotArticles,
  getSectionLink,
  normalizeCategoryList,
  normalizeSections,
  readApiList,
  shortNumber
} from './homeColumnsContent'

describe('homeColumnsContent', () => {
  it('reads list data from common API response shapes', () => {
    const plainList = [{ id: 1 }]
    const recordList = [{ id: 2 }]
    const records = [{ id: 3 }]

    expect(readApiList({ data: { data: plainList } })).toEqual(plainList)
    expect(readApiList({ data: { data: { recordList } } })).toEqual(recordList)
    expect(readApiList({ data: { data: { records } } })).toEqual(records)
    expect(readApiList({ data: { data: null } })).toEqual([])
  })

  it('normalizes article display fallbacks', () => {
    const article = {
      articleContent: '<p>  山间清晨&nbsp; 有雾和风  </p>',
      viewCount: 1200
    }

    expect(getArticleTitle(article)).toBe('未命名文章')
    expect(getArticleCategory(article)).toBe('随笔')
    expect(getArticleSummary(article)).toBe('山间清晨 有雾和风')
    expect(getArticleViews(article)).toBe(1200)
    expect(getArticleReadTime(article)).toBe('5 分钟')
  })

  it('sorts hot articles by readable engagement count', () => {
    const articles = [
      { id: 1, articleTitle: 'low', viewCount: 2 },
      { id: 2, articleTitle: 'high', viewsCount: 90 },
      { id: 3, articleTitle: 'middle', likeCount: 20 },
      { id: 4, articleTitle: 'overflow', viewCount: 10 }
    ]

    expect(getHotArticles(articles).map((item) => item.id)).toEqual([2, 3, 4])
  })

  it('cleans article summaries and reads like counts for feed cards', () => {
    const article = {
      articleContent: '<p>First&nbsp;line</p><p>Second <strong>line</strong></p>',
      likeCount: 7
    }

    expect(getArticleSummary(article)).toBe('First line Second line')
    expect(getArticleLikes(article)).toBe(7)
    expect(getArticleLikes({})).toBe(0)
  })

  it('normalizes categories and short numbers', () => {
    expect(
      normalizeCategoryList([
        { id: 1, categoryName: '技术', articleCount: 12 },
        { id: 'skip', categoryName: 'invalid', articleCount: 99 },
        { id: 2, name: '阅读', count: 9 }
      ])
    ).toEqual([
      { id: 1, name: '技术', count: 12 },
      { id: 2, name: '阅读', count: 9 }
    ])

    expect(shortNumber(999)).toBe('999')
    expect(shortNumber(1200)).toBe('1.2k')
    expect(shortNumber(32000)).toBe('32k')
  })

  it('normalizes section data and filters empty sections', () => {
    const items = [
      { id: 1, sectionName: '技术', articleList: [{ id: 10, articleTitle: '文章A' }] },
      { id: 2, name: '读书', articleList: [{ id: 20, articleTitle: '文章B' }] },
      { id: 3, sectionName: '空分组', articleList: [] },
      { id: 4, sectionName: '无列表' }
    ]

    const result = normalizeSections(items)
    expect(result).toHaveLength(2)
    expect(result[0].sectionName).toBe('技术')
    expect(result[1].sectionName).toBe('读书')
  })

  it('normalizes sections from API response format with title and categoryId', () => {
    const apiItems = [
      { title: '最新', categoryId: null, morePath: '/archives', articleList: [{ id: 1 }] },
      { title: '学习人生', categoryId: 192, morePath: '/categories/192', articleList: [{ id: 2 }] },
      { title: '网站', categoryId: 194, morePath: '/categories/194', articleList: [{ id: 3 }] }
    ]

    const result = normalizeSections(apiItems)
    expect(result).toHaveLength(3)
    expect(result[0]).toEqual(expect.objectContaining({
      id: undefined,
      categoryId: null,
      sectionName: '最新',
      morePath: '/archives'
    }))
    expect(result[1]).toEqual(expect.objectContaining({
      id: 192,
      categoryId: 192,
      sectionName: '学习人生',
      morePath: '/categories/192'
    }))
  })

  it('extracts hot articles across all sections', () => {
    const sections = [
      { id: 1, sectionName: 'A', articleList: [{ id: 1, viewCount: 5 }] },
      { id: 2, sectionName: 'B', articleList: [{ id: 2, viewsCount: 100 }, { id: 3, likeCount: 50 }] }
    ]

    expect(getSectionHotArticles(sections).map(a => a.id)).toEqual([2, 3, 1])
  })

  it('builds section link with fallback logic', () => {
    expect(getSectionLink({ morePath: '/archives', sectionName: '最新', articleList: [] })).toBe('/archives')
    expect(getSectionLink({ id: 5, sectionName: '技术', articleList: [] })).toBe('/categories/5')
    expect(getSectionLink({ sectionName: '未知', articleList: [] })).toBeNull()
  })

  it('handles empty sections array for hot articles', () => {
    expect(getSectionHotArticles([])).toEqual([])
  })
})
