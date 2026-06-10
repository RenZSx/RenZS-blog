import { watch, type WatchStopHandle } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useNoticePopupStore } from '@/stores/noticePopup'
import { useNoticeStore } from '@/stores/notice'
import { useUserStore } from '@/stores/user'
import type { NoticeItem } from '@/types/notice'
import { buildNoticePopupPayload } from '@/utils/noticePopup'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'
import { resolveNoticeSocketUrl } from '@/utils/websocket'

type NoticeSocketEventType = 'notice_init' | 'notice_created' | 'auth_failed'

interface NoticeSocketEnvelope {
  type?: unknown
  event?: unknown
  data?: unknown
  unreadCount?: unknown
  nextUnreadCount?: unknown
  notice?: unknown
}

const RECONNECT_DELAYS = [1000, 3000, 5000] as const

let noticeSocket: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let reconnectAttempt = 0
let activeSocketUrl = ''
let stopConnectionWatch: WatchStopHandle | null = null
const intentionallyClosedSockets = new WeakSet<WebSocket>()

function getEventType(payload: NoticeSocketEnvelope): NoticeSocketEventType | null {
  const eventType = typeof payload.type === 'string' ? payload.type : payload.event
  return eventType === 'notice_init' || eventType === 'notice_created' || eventType === 'auth_failed'
    ? eventType
    : null
}

function toRecord(value: unknown): Record<string, unknown> {
  return typeof value === 'object' && value !== null ? (value as Record<string, unknown>) : {}
}

function getPayloadRecord(payload: NoticeSocketEnvelope) {
  return toRecord(payload.data)
}

function readUnreadCount(...sources: Array<Record<string, unknown>>) {
  for (const source of sources) {
    const candidate =
      source.nextUnreadCount ?? source.unreadCount ?? source.unread_count ?? source.count
    if (typeof candidate === 'number' && Number.isFinite(candidate)) {
      return Math.max(0, Math.trunc(candidate))
    }
  }

  return null
}

function readInteger(value: unknown) {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return Math.trunc(value)
  }

  if (typeof value === 'string' && value.trim() !== '') {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return Math.trunc(parsed)
    }
  }

  return null
}

function readString(value: unknown) {
  return typeof value === 'string' ? value : null
}

function readNullableString(value: unknown) {
  return value === undefined || value === null || typeof value === 'string' ? value ?? null : null
}

function padTimeSegment(value: number) {
  return String(value).padStart(2, '0')
}

function normalizeCreateTime(value: unknown) {
  if (typeof value === 'string' && value.trim() !== '') {
    return value
  }

  if (typeof value === 'number' && Number.isFinite(value)) {
    return new Date(value).toISOString()
  }

  const record = toRecord(value)
  const year = readInteger(record.year)
  const month =
    readInteger(record.monthValue) ??
    readInteger(record.month) ??
    readInteger(record.monthNumber)
  const day = readInteger(record.dayOfMonth) ?? readInteger(record.day)
  const hour = readInteger(record.hour) ?? 0
  const minute = readInteger(record.minute) ?? 0
  const second = readInteger(record.second) ?? 0

  // 兼容后端 websocket 里 LocalDateTime 被序列化成对象的情况，避免实时通知被前端严格校验拦掉。
  if (year !== null && month !== null && day !== null) {
    return `${year}-${padTimeSegment(month)}-${padTimeSegment(day)} ${padTimeSegment(hour)}:${padTimeSegment(minute)}:${padTimeSegment(second)}`
  }

  return '刚刚'
}

function normalizeNoticeItem(value: unknown): NoticeItem | null {
  const candidate = toRecord(value)
  const id = readInteger(candidate.id)
  const userId = readInteger(candidate.userId)
  const noticeType = readString(candidate.noticeType)
  const content = readString(candidate.content)

  if (id === null || userId === null || !noticeType || !content) {
    return null
  }

  return {
    id,
    userId,
    noticeType: noticeType as NoticeItem['noticeType'],
    sourceId: readInteger(candidate.sourceId),
    sourceType: readNullableString(candidate.sourceType),
    targetId: readInteger(candidate.targetId),
    targetType: readNullableString(candidate.targetType),
    jumpPath: readNullableString(candidate.jumpPath),
    anchorKey: readNullableString(candidate.anchorKey),
    content,
    replyContent: readNullableString(candidate.replyContent),
    isRead: readInteger(candidate.isRead) ?? 0,
    createTime: normalizeCreateTime(candidate.createTime)
  }
}

function readNotice(...sources: Array<Record<string, unknown>>) {
  for (const source of sources) {
    const nestedNotice = source.notice ?? source.item
    const normalizedNestedNotice = normalizeNoticeItem(nestedNotice)
    if (normalizedNestedNotice) {
      return normalizedNestedNotice
    }

    const normalizedNotice = normalizeNoticeItem(source)
    if (normalizedNotice) {
      return normalizedNotice
    }
  }

  return null
}

function stopReconnect() {
  if (!reconnectTimer) {
    return
  }

  clearTimeout(reconnectTimer)
  reconnectTimer = null
}

function disconnect() {
  const noticeStore = useNoticeStore()
  const noticePopupStore = useNoticePopupStore()

  stopReconnect()
  reconnectAttempt = 0
  activeSocketUrl = ''
  noticeStore.setRealtimeReady(false)
  noticePopupStore.clear()

  const currentSocket = noticeSocket
  noticeSocket = null
  if (currentSocket) {
    intentionallyClosedSockets.add(currentSocket)
    currentSocket.close()
  }
}

function scheduleReconnect() {
  const userStore = useUserStore()
  const blogInfoStore = useBlogInfoStore()
  const noticeStore = useNoticeStore()

  if (reconnectTimer || reconnectAttempt >= RECONNECT_DELAYS.length) {
    return
  }

  const userId = userStore.userId
  if (!userId) {
    if (!userId) {
      noticeStore.resetState()
    }
    disconnect()
    return
  }

  const delay = RECONNECT_DELAYS[reconnectAttempt]
  reconnectAttempt += 1
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    const nextUserId = userStore.userId
    const nextWebsocketUrl = blogInfoStore.blogInfo.websiteConfig.websocketUrl
    if (!nextUserId) {
      if (!nextUserId) {
        noticeStore.resetState()
      }
      disconnect()
      return
    }

    connect(resolveNoticeSocketUrl(nextWebsocketUrl, nextUserId))
  }, delay)
}

function handleMessage(rawMessage: string) {
  const noticeStore = useNoticeStore()
  const noticePopupStore = useNoticePopupStore()
  const envelope = JSON.parse(rawMessage) as NoticeSocketEnvelope
  const eventType = getEventType(envelope)
  if (!eventType) {
    return
  }

  if (eventType === 'auth_failed') {
    const userStore = useUserStore()
    userStore.logout()
    disconnect()
    openLoginRequiredPrompt({
      title: '登录已过期',
      message: '您的登录状态已失效，请重新登录'
    })
    return
  }

  const envelopeRecord = toRecord(envelope)
  const payloadRecord = getPayloadRecord(envelope)

  if (eventType === 'notice_init') {
    const unreadCount = readUnreadCount(payloadRecord, envelopeRecord)
    if (unreadCount !== null) {
      noticeStore.setUnreadCount(unreadCount)
    }
    return
  }

  const nextUnreadCount =
    readUnreadCount(payloadRecord, envelopeRecord) ?? noticeStore.unreadCount + 1
  const notice = readNotice(payloadRecord, envelopeRecord)
  if (notice) {
    noticeStore.receiveRealtimeNotice(notice, nextUnreadCount)
    // 初始化事件只同步未读数，实时新增事件才进入弹窗队列。
    noticePopupStore.enqueue(buildNoticePopupPayload(notice))
  } else {
    noticeStore.setUnreadCount(nextUnreadCount)
  }
}

function connect(socketUrl: string) {
  const noticeStore = useNoticeStore()
  if (!socketUrl) {
    disconnect()
    return
  }

  if (
    noticeSocket &&
    activeSocketUrl === socketUrl &&
    (noticeSocket.readyState === WebSocket.OPEN || noticeSocket.readyState === WebSocket.CONNECTING)
  ) {
    return
  }

  const previousSocket = noticeSocket
  if (previousSocket) {
    intentionallyClosedSockets.add(previousSocket)
    previousSocket.close()
  }

  activeSocketUrl = socketUrl
  noticeStore.setRealtimeReady(false)

  let socket: WebSocket
  try {
    socket = new WebSocket(socketUrl)
  } catch (error) {
    console.error('创建通知 WebSocket 失败:', error)
    noticeStore.setRealtimeReady(false)
    return
  }

  noticeSocket = socket

  socket.onopen = () => {
    if (noticeSocket !== socket) {
      intentionallyClosedSockets.add(socket)
      socket.close()
      return
    }

    stopReconnect()
    reconnectAttempt = 0
    noticeStore.setRealtimeReady(true)
  }

  socket.onmessage = (event) => {
    try {
      handleMessage(event.data)
    } catch (error) {
      console.error('解析通知实时消息失败:', error)
    }
  }

  socket.onerror = () => {
    if (noticeSocket === socket) {
      noticeStore.setRealtimeReady(false)
    }
  }

  socket.onclose = (event) => {
    const wasIntentionalClose = intentionallyClosedSockets.has(socket)
    intentionallyClosedSockets.delete(socket)

    if (noticeSocket === socket) {
      noticeSocket = null
      noticeStore.setRealtimeReady(false)

      if (event.code === 1008) {
        const userStore = useUserStore()
        userStore.logout()
        disconnect()
        openLoginRequiredPrompt({
          title: '登录已过期',
          message: '您的登录状态已失效，请重新登录'
        })
        return
      }

      if (!wasIntentionalClose) {
        scheduleReconnect()
      }
    }
  }
}

function start() {
  if (stopConnectionWatch) {
    return
  }

  const userStore = useUserStore()
  const blogInfoStore = useBlogInfoStore()
  const noticeStore = useNoticeStore()

  stopConnectionWatch = watch(
    () =>
      [userStore.userId, userStore.authSessionVersion, blogInfoStore.blogInfo.websiteConfig.websocketUrl] as const,
    ([userId, _authSessionVersion, websocketUrl]) => {
      if (!userId) {
        // 未登录态不保留实时连接和通知缓存，避免切换账号后展示上一个用户的通知状态。
        disconnect()
        noticeStore.resetState()
        return
      }

      // 地址解析统一下沉到工具层，本地开发优先走同源 ws，避免后台仍返回线上 wss 导致连错环境。
      const socketUrl = resolveNoticeSocketUrl(websocketUrl, userId)
      if (!socketUrl) {
        disconnect()
        return
      }

      connect(socketUrl)
    },
    {
      immediate: true
    }
  )
}

export function useNoticeSocket() {
  return {
    start,
    disconnect
  }
}
