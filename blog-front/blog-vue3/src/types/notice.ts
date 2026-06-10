export type NoticeType =
  | 'comment_reply'
  | 'talk_reply'
  | 'article_like'
  | 'talk_like'
  | 'system'

export type NoticeFilter = 'all' | 'reply' | 'like' | 'system'

export interface NoticeItem {
  id: number
  userId: number
  noticeType: NoticeType
  sourceId: number | null
  sourceType: string | null
  targetId: number | null
  targetType: string | null
  jumpPath: string | null
  anchorKey: string | null
  content: string
  replyContent: string | null
  isRead: number
  createTime: string
}

export interface NoticeListQuery {
  current?: number
  size?: number
  noticeType?: NoticeType | 'all'
}

const REPLY_PREVIEW_TEXT_LIMIT = 10
const REPLY_PREVIEW_IMAGE_ATTRIBUTES = ['src', 'alt', 'width', 'height'] as const
const REPLY_PREVIEW_HIDDEN_TAGS = new Set(['script', 'style', 'noscript'])

export function formatReplyContentPreview(content: string | null): string | null {
  if (!content || content.trim().length === 0) {
    return null
  }

  const parser = new DOMParser()
  const document = parser.parseFromString(content, 'text/html')
  let visibleTextCount = 0
  let preview = ''

  const appendNode = (node: Node) => {
    if (visibleTextCount >= REPLY_PREVIEW_TEXT_LIMIT && node.nodeType !== Node.ELEMENT_NODE) {
      return
    }

    if (node.nodeType === Node.TEXT_NODE) {
      const textContent = node.textContent ?? ''
      if (textContent.length === 0 || visibleTextCount >= REPLY_PREVIEW_TEXT_LIMIT) {
        return
      }

      const characters = Array.from(textContent)
      const remaining = REPLY_PREVIEW_TEXT_LIMIT - visibleTextCount
      const nextText = characters.slice(0, remaining).join('')
      preview += nextText
      visibleTextCount += Array.from(nextText).length
      return
    }

    if (node.nodeType !== Node.ELEMENT_NODE) {
      return
    }

    const element = node as HTMLElement
    const tagName = element.tagName.toLowerCase()

    if (REPLY_PREVIEW_HIDDEN_TAGS.has(tagName)) {
      return
    }

    if (tagName === 'img') {
      if (visibleTextCount > REPLY_PREVIEW_TEXT_LIMIT - 1) {
        return
      }

      const safeAttributes = REPLY_PREVIEW_IMAGE_ATTRIBUTES.map((attribute) => {
        const value = element.getAttribute(attribute)
        return value === null ? null : `${attribute}="${value.replace(/"/g, '&quot;')}"`
      }).filter((attribute): attribute is string => attribute !== null)

      preview += `<img${safeAttributes.length > 0 ? ` ${safeAttributes.join(' ')}` : ''}>`
      return
    }

    Array.from(element.childNodes).forEach(appendNode)
  }

  Array.from(document.body.childNodes).forEach(appendNode)

  return preview.trim().length > 0 ? preview : null
}

export function getReplyPreview(item: NoticeItem): string | null {
  if (item.noticeType !== 'comment_reply' && item.noticeType !== 'talk_reply') {
    return null
  }

  return formatReplyContentPreview(item.replyContent)
}
