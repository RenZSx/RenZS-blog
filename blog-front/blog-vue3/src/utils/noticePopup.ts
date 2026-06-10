import type { RouteLocationRaw } from 'vue-router'
import { formatReplyContentPreview, type NoticeItem, type NoticeType } from '@/types/notice'

const NOTICE_POPUP_FALLBACK_ROUTE = {
  path: '/user',
  query: {
    tab: 'notices'
  }
} satisfies RouteLocationRaw

const NOTICE_POPUP_FALLBACK_SUMMARY = '点击查看通知详情'

const NOTICE_POPUP_TITLE_MAP: Record<NoticeType, string> = {
  comment_reply: '收到文章回复',
  talk_reply: '收到说说回复',
  article_like: '文章收到点赞',
  talk_like: '说说收到点赞',
  system: '系统通知'
}

const NOTICE_POPUP_ICON_MAP: Record<NoticeType, string> = {
  comment_reply: 'mdi-reply-text-outline',
  talk_reply: 'mdi-message-reply-text-outline',
  article_like: 'mdi-thumb-up-outline',
  talk_like: 'mdi-heart-outline',
  system: 'mdi-bell-outline'
}

export interface NoticePopupPayload {
  id: number | string
  noticeType: NoticeType | 'unread_summary'
  title: string
  summary: string
  timeText: string
  icon: string
  route: RouteLocationRaw
}

const NOTICE_SUMMARY_ROUTE = {
  path: '/user',
  query: {
    tab: 'notices'
  }
} satisfies RouteLocationRaw

function normalizeText(value: unknown): string {
  return typeof value === 'string' ? value : ''
}

function normalizePath(path: string | null): string | null {
  const normalized = path?.trim() ?? ''
  if (!normalized.startsWith('/') || normalized.startsWith('//')) {
    return null
  }

  return normalized
}

function buildRouteFromPath(item: NoticeItem): RouteLocationRaw {
  const normalizedPath = normalizePath(item.jumpPath)
  if (!normalizedPath) {
    return NOTICE_POPUP_FALLBACK_ROUTE
  }

  const [pathWithQuery, hashPart] = normalizedPath.split('#', 2)
  const [path, queryString] = pathWithQuery.split('?', 2)
  const query = queryString ? Object.fromEntries(new URLSearchParams(queryString).entries()) : undefined
  // 优先使用通知自身的锚点字段，避免后端未拼接 hash 时丢失定位信息。
  const hash = item.anchorKey ? `#${item.anchorKey}` : hashPart ? `#${hashPart}` : undefined

  return {
    path,
    query,
    hash
  }
}

export function getNoticePopupTitle(item: NoticeItem): string {
  return NOTICE_POPUP_TITLE_MAP[item.noticeType] ?? '通知提醒'
}

export function getNoticePopupSummary(item: NoticeItem): string {
  const replyPreview = formatReplyContentPreview(item.replyContent)
  if (replyPreview) {
    return replyPreview
  }

  // 运行时消息可能存在脏字段，这里统一兜底成空串后再做摘要裁剪。
  const normalizedContent = normalizeText(item.content).trim()
  // 摘要为空时统一给出通用文案，避免实时卡片出现空白正文。
  return normalizedContent || NOTICE_POPUP_FALLBACK_SUMMARY
}

export function getNoticePopupRoute(item: NoticeItem): RouteLocationRaw {
  return buildRouteFromPath(item)
}

export function getNoticePopupIcon(item: NoticeItem): string {
  return NOTICE_POPUP_ICON_MAP[item.noticeType] ?? 'mdi-bell-outline'
}

export function buildNoticePopupPayload(item: NoticeItem): NoticePopupPayload {
  return {
    id: item.id,
    noticeType: item.noticeType,
    title: getNoticePopupTitle(item),
    summary: getNoticePopupSummary(item),
    timeText: normalizeText(item.createTime) || '刚刚',
    icon: getNoticePopupIcon(item),
    route: getNoticePopupRoute(item)
  }
}

export function buildUnreadSummaryPopupPayload(unreadCount: number): NoticePopupPayload {
  const normalizedCount = Math.max(0, Math.trunc(unreadCount))
  const summaryText =
    normalizedCount > 99 ? '你有 99+ 条未读通知，点击立即查看' : `你有 ${normalizedCount} 条未读通知，点击立即查看`

  return {
    id: `unread-summary-${normalizedCount}`,
    noticeType: 'unread_summary',
    title: '未读通知提醒',
    summary: summaryText,
    timeText: '刚刚',
    icon: 'mdi-bell-badge-outline',
    route: NOTICE_SUMMARY_ROUTE
  }
}
