/**
 * 通知 WebSocket URL 拼接
 *
 * 后端协议:wss?://host/notice-websocket/{userId}?token=xxx
 * - http → ws
 * - https → wss
 * - token 从 localStorage 取(已登录前提下)
 */

import { BASE_URL } from './config'
import { getToken } from './auth'

/**
 * 构造通知 WebSocket 连接 URL
 *
 * @param {number} userId 当前登录用户 ID
 * @returns {string|null} 完整 ws/wss URL,缺少 userId 或 token 返回 null
 */
export function buildNoticeSocketUrl(userId) {
  if (!userId || !Number.isFinite(Number(userId))) return null

  const token = getToken()
  if (!token) return null

  // BASE_URL 形如 http://localhost:8088
  let url = BASE_URL.replace(/^http(s?):\/\//i, 'ws$1://')
  // 兜底:用户配错了 BASE_URL
  if (!/^wss?:\/\//i.test(url)) {
    url = 'ws://' + url
  }

  return `${url}/notice-websocket/${userId}?token=${encodeURIComponent(token)}`
}
