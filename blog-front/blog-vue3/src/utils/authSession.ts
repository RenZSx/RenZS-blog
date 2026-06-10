/**
 * 功能说明: 同步前端持久化登录态与后端 sa-token 登录态。
 * 作者: OpenAI Codex
 * 创建时间: 2026-06-08
 * 用途概述: 应用启动时先确认 Redis 中的 sa-token 登录态是否仍有效,避免旧的 Pinia 持久化用户触发错误的过期弹窗。
 */

import type { LoginPayload, UserInfo } from '@/stores/user'

interface SessionResponse {
  data?: {
    flag?: boolean
    code?: number
    data?: UserInfo
  }
}

interface SyncAuthenticatedSessionOptions {
  isLoggedIn: boolean
  /** 与 useUserStore.login 签名保持一致,接收完整 LoginPayload */
  login: (payload: LoginPayload) => void
  logout: () => void
  getCurrentUser: () => Promise<SessionResponse>
}

const NO_LOGIN_CODE = 40001

/**
 * 校验当前浏览器携带的后端会话，并按服务端结果修正本地登录态。
 *
 * @param options 会话校验依赖，便于单元测试注入 store 和接口调用。
 * @returns 服务端会话是否有效。
 */
export async function syncAuthenticatedSession(options: SyncAuthenticatedSessionOptions) {
  // 本地没有登录态时不需要访问受保护接口，避免匿名用户产生多余请求。
  if (!options.isLoggedIn) {
    return false
  }

  try {
    const { data } = await options.getCurrentUser()
    // 服务端明确返回当前用户时，以服务端数据刷新本地用户缓存。
    // 此处仅刷新用户信息,不下发新 token(后端 getCurrentUser 不返回 token);
    // userStore.login 内部已对 tokenValue 为空时跳过 localStorage 写入,本地 token 保持不变。
    if (data?.flag && data.data) {
      options.login({
        userInfo: data.data,
        tokenName: '',
        tokenValue: '',
        tokenTimeout: 0
      })
      return true
    }

    // 业务未登录码说明会话已无效,前端必须清理本地用户缓存与 token。
    if (data?.code === NO_LOGIN_CODE || data?.flag === false) {
      options.logout()
      return false
    }
  } catch (error) {
    // 校验接口自身失败时保守清理旧登录态，避免启动后继续用陈旧用户身份连接通知通道。
    options.logout()
    return false
  }

  return false
}
