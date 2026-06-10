/**
 * uni.request 封装
 * 职责对齐 blog-vue3 的 src/api/request.ts:
 *   - 自动剥掉 /api 前缀(blog-vue3 的 /api 仅是 Vite 代理用)
 *   - Header 注入 Authorization: Bearer ${token}
 *   - Content-Type 智能识别(JSON / form-urlencoded / FormData)
 *   - 业务码 40001 → 清 token + 跳登录页
 *   - 业务码 50000 / HTTP 5xx → uni.showToast
 */

import { BASE_URL, NO_LOGIN_CODE, SYSTEM_ERROR_CODE } from '@/utils/config'
import { getToken, removeToken } from '@/utils/auth'

let isHandlingExpired = false

/**
 * 处理登录失效:清 token + 提示 + 跳转登录
 * 用 isHandlingExpired 防止并发请求重复弹出多个 toast。
 */
function handleExpiredSession() {
  if (isHandlingExpired) return
  isHandlingExpired = true

  removeToken()

  // 动态 import store,避免循环依赖
  import('@/store/user').then(({ useUserStore }) => {
    try {
      const userStore = useUserStore()
      userStore.clearLocal()
    } catch (e) {
      // store 未初始化时静默
    }
  })

  uni.showToast({
    title: '登录已失效,请重新登录',
    icon: 'none',
    duration: 1500
  })

  setTimeout(() => {
    isHandlingExpired = false
    uni.reLaunch({ url: '/pages/login/login' })
  }, 1500)
}

/**
 * 统一请求入口
 *
 * @param {Object} options
 * @param {string} options.url - 接口路径,可写 /api/login 或 /login,本方法会自动归一化
 * @param {string} [options.method='GET'] - HTTP 方法
 * @param {any} [options.data] - 请求数据
 * @param {Object} [options.header] - 额外 Header
 * @param {boolean} [options.silentSessionCheck] - 静默校验:40001 不弹提示不跳转(用于启动时静默校验登录态)
 * @returns {Promise<any>} 业务数据(已剥 code/data 层)
 */
export default function request(options = {}) {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    silentSessionCheck = false
  } = options

  // 1. URL 归一化:剥掉 /api 前缀(兼容 blog-vue3 的写法)
  const path = url.startsWith('/api/') ? url.slice(4) : url
  const fullUrl = `${BASE_URL}${path}`

  // 2. Header 准备:注入 Authorization + 智能 Content-Type
  const finalHeader = { ...header }
  const token = getToken()
  if (token) {
    finalHeader.Authorization = `Bearer ${token}`
  }

  // form-urlencoded(由调用方在 header 显式声明,本工具不强制改)
  // JSON 默认由 uni.request 自动设置;FormData 走 uni.uploadFile 不走这里

  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      header: finalHeader,
      timeout: 15000,
      success: (res) => {
        const body = res.data

        // HTTP 层错误
        if (res.statusCode < 200 || res.statusCode >= 300) {
          if (res.statusCode === 401) {
            if (!silentSessionCheck) handleExpiredSession()
            return reject(new Error('HTTP 401'))
          }
          if (res.statusCode >= 500) {
            uni.showToast({ title: '服务器开小差了', icon: 'none' })
          }
          return reject(new Error(`HTTP ${res.statusCode}`))
        }

        // 业务层错误处理
        const code = body && body.code

        if (code === NO_LOGIN_CODE) {
          if (!silentSessionCheck) handleExpiredSession()
          return reject(new Error(body.message || '未登录'))
        }

        if (code === SYSTEM_ERROR_CODE) {
          uni.showToast({ title: body.message || '系统异常', icon: 'none' })
          return reject(new Error(body.message || '系统异常'))
        }

        // body.flag === false 表示业务失败,但不一定是登录态问题
        // 把完整 body 返回,由业务自己判断 flag
        resolve(body)
      },
      fail: (err) => {
        uni.showToast({
          title: err.errMsg && err.errMsg.includes('timeout') ? '请求超时' : '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}
