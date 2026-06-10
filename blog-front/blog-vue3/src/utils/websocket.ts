/**
 * WebSocket 地址工具
 *
 * 作者: OpenAI Codex
 * 创建时间: 2026-05-10
 * 用途概述:
 * 1. 统一本地开发和线上环境的 websocket 地址解析。
 * 2. 本地开发优先走当前页面同源地址，配合 Vite ws 代理，避免后台配置仍指向线上域名时误连 wss。
 * 3. 通知 websocket 在 URL 上追加 ?token=xxx,与后端 NoticeWebSocketServiceImpl 的 Query 鉴权对齐,
 *    全端(Web / App)统一通过 Query 完成 WebSocket 握手鉴权。
 */

/** 与 stores/user.ts / api/request.ts 中 token 存取 key 保持一致 */
const TOKEN_STORAGE_KEY = 'token'

/**
 * 判断当前是否为本地开发访问域名。
 *
 * @returns 是否本地域名
 */
function isLocalDevHost() {
  const { hostname } = window.location
  return hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '::1'
}

/**
 * 统一把 http/https 转成 ws/wss。
 *
 * @param url 目标地址
 * @returns websocket 地址
 */
function normalizeToWebSocketProtocol(url: URL) {
  if (url.protocol === 'http:') {
    url.protocol = 'ws:'
  } else if (url.protocol === 'https:') {
    url.protocol = 'wss:'
  }
  return url
}

/**
 * 给 URL 追加 token 查询参数。仅在 localStorage 中存有 token 时追加,避免空值污染。
 *
 * @param url 待追加的 URL
 */
function appendTokenQuery(url: URL) {
  try {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (token) {
      url.searchParams.set('token', token)
    }
  } catch (error) {
    // localStorage 在隐私模式或 SSR 环境可能抛错,静默降级为不附带 token;
    // 此时后端会以未鉴权处理握手,WebSocket 将无法连接(纯 Header 模式无 Cookie 兜底)。
    console.warn('读取 token 注入 websocket URL 失败:', error)
  }
  return url
}

/**
 * 本地开发时直接拼接当前页面同源 websocket 路径，再交给 Vite 代理转发。
 *
 * @param pathname websocket 路径
 * @param withToken 是否附带登录 token (仅鉴权类 websocket 需要)
 * @returns 可直接连接的 websocket 地址
 */
function buildLocalSocketUrl(pathname: string, withToken = false) {
  const url = new URL(pathname, window.location.origin)
  normalizeToWebSocketProtocol(url)
  url.search = ''
  url.hash = ''
  if (withToken) {
    appendTokenQuery(url)
  }
  return url.toString()
}

/**
 * 解析聊天室 websocket 地址。
 *
 * @param configuredUrl 后台配置返回的 websocket 地址
 * @returns 可直接连接的 websocket 地址
 */
export function resolveChatSocketUrl(configuredUrl?: string | null) {
  if (isLocalDevHost()) {
    return buildLocalSocketUrl('/websocket')
  }

  const sourceUrl = configuredUrl?.trim()
  if (!sourceUrl) {
    return buildLocalSocketUrl('/websocket')
  }

  try {
    const url = new URL(sourceUrl, window.location.origin)
    normalizeToWebSocketProtocol(url)
    url.search = ''
    url.hash = ''
    return url.toString()
  } catch (error) {
    console.error('解析聊天室 WebSocket 地址失败:', error)
    return ''
  }
}

/**
 * 解析通知 websocket 地址。
 * 与后端 NoticeWebSocketServiceImpl.extractSaToken 对齐,追加 ?token=xxx 让全端(Web / App)统一鉴权;
 * 后端已关闭 Cookie 通道,Query 是 WebSocket 唯一鉴权来源。
 *
 * @param configuredUrl 后台配置返回的 websocket 地址
 * @param userId 当前用户 id
 * @returns 可直接连接的 websocket 地址
 */
export function resolveNoticeSocketUrl(configuredUrl: string | null | undefined, userId: number) {
  if (!Number.isFinite(userId)) {
    return ''
  }

  if (isLocalDevHost()) {
    return buildLocalSocketUrl(`/notice-websocket/${userId}`, true)
  }

  const sourceUrl = configuredUrl?.trim()
  if (!sourceUrl) {
    return buildLocalSocketUrl(`/notice-websocket/${userId}`, true)
  }

  try {
    const url = new URL(sourceUrl, window.location.origin)
    normalizeToWebSocketProtocol(url)
    // 通知连接独立于聊天室路径，避免两类实时流互相耦合或复用协议语义。
    url.pathname = `/notice-websocket/${userId}`
    url.search = ''
    url.hash = ''
    appendTokenQuery(url)
    return url.toString()
  } catch (error) {
    console.error('解析通知 WebSocket 地址失败:', error)
    return ''
  }
}
