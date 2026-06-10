import request from '@/api/request'
import EmojiList from '@/assets/js/emoji'

interface CommentParams {
  current: number
  type: number
  topicId?: number
}

interface CommentPayload {
  commentContent: string
  type: number
  topicId?: number
  replyUserId?: number
  parentId?: number
}

export function fetchComments(params: CommentParams) {
  return request.get('/api/comments', { params })
}

export function submitComment(comment: CommentPayload) {
  return request.post('/api/comments', comment)
}

export function fetchReplies(commentId: number, params: { current: number; size: number }) {
  return request.get(`/api/comments/${commentId}/replies`, { params })
}

export function sendCommentLike(commentId: number) {
  return request.post(`/api/comments/${commentId}/like`)
}

export function parseEmoji(
  content: string,
  {
    width = 24,
    height = width,
    style = 'margin: 0 1px;vertical-align: text-bottom'
  } = {}
): string {
  return content.replace(/\[.+?\]/g, function (str) {
    const emojiUrl = EmojiList[str as keyof typeof EmojiList]
    if (!emojiUrl) return str
    return `<img src='${emojiUrl}' width='${width}' height='${height}' style='${style}'/>`
  })
}

export function buildCommentPayload({
  content,
  type,
  routePath,
  extra = {}
}: {
  content: string
  type: number
  routePath: string
  extra?: Record<string, any>
}): CommentPayload {
  const arr = routePath.split('/')
  const comment: CommentPayload = {
    commentContent: content,
    type,
    ...extra
  }

  // 文章和说说评论需要绑定 URL 中的主题 ID，留言等全局评论不需要。
  switch (type) {
    case 1:
    case 3:
      comment.topicId = Number(arr[2])
      break
    default:
      break
  }

  return comment
}

export function buildCommentQuery({
  current,
  type,
  routePath
}: {
  current: number
  type: number
  routePath: string
}): CommentParams {
  const arr = routePath.split('/')
  const params: CommentParams = {
    current,
    type
  }

  switch (type) {
    case 1:
    case 3:
      params.topicId = Number(arr[2])
      break
    default:
      break
  }

  return params
}