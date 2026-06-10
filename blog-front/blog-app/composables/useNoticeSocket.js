/**
 * 通知 WebSocket 客户端 (uniapp SocketTask 版)
 *
 * 设计要点:
 *   - 模块单例:任意页面调 useNoticeSocket(),拿到同一个 task/状态
 *   - SocketTask 多实例方式:每次 connect 拿到独立 task,事件全用 task.onXxx,
 *     避免与全局 uni.onSocketXxx 串台
 *   - 状态机:'idle' → 'connecting' → 'open' → 'closing' → 'idle'
 *   - 重连:1s / 3s / 5s 三档退避,1008/主动关不重连
 *   - 协议:接收 'notice_init' / 'notice_created' / 'auth_failed'
 *
 * 用法:
 *   const { start, disconnect, status } = useNoticeSocket()
 *   start()        // 已登录前提
 *   disconnect()   // 登出前调用
 */

import { ref } from 'vue'
import { useUserStore } from '@/store/user'
import { useNoticeStore } from '@/store/notice'
import { buildNoticeSocketUrl } from '@/utils/notice-socket-url'

// ============ 模块单例状态 ============
const RECONNECT_DELAYS = [1000, 3000, 5000]

let task = null                            // 当前 SocketTask
let status = ref('idle')                   // 'idle' | 'connecting' | 'open' | 'closing'
let intentionallyClosed = false            // 主动关标志(避免触发重连)
let reconnectTimer = null
let reconnectAttempt = 0
let currentUrl = ''                        // 当前已连接的 URL,避免重复 connect

// ============ 工具函数 ============
function log(...args) {
  // 关闭日志改为 const DEBUG = false
  console.log('[NoticeSocket]', ...args)
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

function scheduleReconnect() {
  if (reconnectTimer) return
  if (reconnectAttempt >= RECONNECT_DELAYS.length) {
    log('重连次数已用尽,停止')
    return
  }
  const delay = RECONNECT_DELAYS[reconnectAttempt]
  reconnectAttempt += 1
  log(`将于 ${delay}ms 后第 ${reconnectAttempt} 次重连`)
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    start()
  }, delay)
}

// ============ 消息处理 ============

function handleMessage(rawData) {
  let envelope
  try {
    envelope = typeof rawData === 'string' ? JSON.parse(rawData) : rawData
  } catch (e) {
    log('消息解析失败:', e, rawData)
    return
  }

  const type = envelope.type
  const userStore = useUserStore()
  const noticeStore = useNoticeStore()

  if (type === 'auth_failed') {
    log('收到 auth_failed,清理登录态')
    handleAuthFailed()
    return
  }

  if (type === 'notice_init') {
    log('notice_init unreadCount=', envelope.unreadCount)
    noticeStore.setUnreadCount(envelope.unreadCount || 0)
    return
  }

  if (type === 'notice_created') {
    const notice = envelope.notice
    const nextUnread =
      typeof envelope.unreadCount === 'number'
        ? envelope.unreadCount
        : noticeStore.unreadCount + 1

    noticeStore.receiveRealtimeNotice(notice, nextUnread)

    // 通知页未聚焦 → toast 提示
    if (!noticeStore.panelActive && notice && notice.content) {
      uni.showToast({
        title: previewText(notice.content),
        icon: 'none',
        duration: 2000
      })
    }
    return
  }

  log('未知 type:', type, envelope)
}

function previewText(text) {
  if (!text) return '收到新通知'
  const clean = String(text).replace(/\s+/g, ' ').trim()
  return clean.length > 20 ? clean.slice(0, 20) + '...' : clean
}

function handleAuthFailed() {
  intentionallyClosed = true
  clearReconnectTimer()
  closeTask()

  const userStore = useUserStore()
  userStore.clearLocal()
  uni.showToast({ title: '登录已失效,请重新登录', icon: 'none', duration: 1500 })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/login/login' })
  }, 1500)
}

function closeTask() {
  if (!task) return
  try {
    task.close({ code: 1000, reason: 'client_close' })
  } catch (e) {
    /* noop */
  }
  task = null
}

// ============ 公开 API ============

/**
 * 启动连接(需已登录)
 * 已连接时:URL 一致直接 return,URL 不一致重连
 */
function start() {
  const userStore = useUserStore()
  const noticeStore = useNoticeStore()

  if (!userStore.isLoggedIn || !userStore.userId) {
    log('未登录,跳过连接')
    return
  }

  const url = buildNoticeSocketUrl(userStore.userId)
  if (!url) {
    log('URL 构造失败,token 或 userId 缺失')
    return
  }

  // 已经连接到同一 URL,无需重复
  if ((status.value === 'open' || status.value === 'connecting') && currentUrl === url) {
    return
  }

  // URL 不同或已断开,先清掉旧 task
  if (task) {
    intentionallyClosed = true
    closeTask()
  }

  intentionallyClosed = false
  status.value = 'connecting'
  currentUrl = url
  log('connect →', url)

  try {
    // uniapp 推荐方式:在 complete 拿 SocketTask
    let newTask = null

    newTask = uni.connectSocket({
      url,
      complete: () => {
        // 部分平台 connectSocket 同步返回 undefined,必须用 complete 兜底
      }
    })

    // 部分平台同步返回 task
    if (!newTask || typeof newTask.onOpen !== 'function') {
      log('uni.connectSocket 未返回有效 SocketTask,可能平台不支持')
      status.value = 'idle'
      return
    }

    task = newTask

    task.onOpen(() => {
      log('connected')
      status.value = 'open'
      reconnectAttempt = 0
      noticeStore.setRealtimeReady(true)
    })

    task.onMessage((event) => {
      handleMessage(event.data)
    })

    task.onError((err) => {
      log('error:', err && err.errMsg)
      noticeStore.setRealtimeReady(false)
      // onError 后通常会跟 onClose,这里不重连,交给 onClose 统一处理
    })

    task.onClose((event) => {
      const code = event && typeof event.code === 'number' ? event.code : 0
      log('closed code=', code, 'intentional=', intentionallyClosed)
      noticeStore.setRealtimeReady(false)
      status.value = 'idle'
      task = null
      currentUrl = ''

      // 1008 = 服务端鉴权失败,不重连(handleAuthFailed 可能已被 message 触发)
      if (code === 1008) {
        if (!intentionallyClosed) handleAuthFailed()
        return
      }

      // 主动关,不重连
      if (intentionallyClosed) {
        intentionallyClosed = false
        return
      }

      // 其余(网络异常 / code=0 / 1006)走重连
      scheduleReconnect()
    })
  } catch (e) {
    log('connect 抛错:', e)
    status.value = 'idle'
    task = null
  }
}

/**
 * 主动断开
 */
function disconnect() {
  log('disconnect')
  intentionallyClosed = true
  clearReconnectTimer()
  reconnectAttempt = 0
  if (task) {
    status.value = 'closing'
    closeTask()
    status.value = 'idle'
  }
  currentUrl = ''
  const noticeStore = useNoticeStore()
  noticeStore.setRealtimeReady(false)
}

/**
 * 已连接判断(供 onAppShow 检查)
 */
function isConnected() {
  return status.value === 'open'
}

export function useNoticeSocket() {
  return {
    status,
    start,
    disconnect,
    isConnected
  }
}
