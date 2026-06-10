/**
 * 功能说明: Axios 请求实例与统一错误处理。
 * 作者: OpenAI Codex
 * 创建时间: 2026-06-08
 * 用途概述: 统一接口基础配置、请求头、sa-token Header 鉴权(Authorization: Bearer xxx)、登录失效处理。
 */

import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { useToast } from '@/composables/useToast'

const NO_LOGIN_CODE = 40001
const SILENT_SESSION_CHECK_HEADER = 'X-Silent-Session-Check'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  // 全端纯 Header 鉴权,不再依赖浏览器 Cookie;后端 CORS 已关闭 allowCredentials,
  // 显式 withCredentials: false 避免某些代理在 OPTIONS 预检中带 Cookie 触发 CORS 拒绝。
  withCredentials: false
})

/**
 * 统一处理服务端会话失效，避免 HTTP 401 和业务码 40001 两套路径表现不一致。
 */
function handleExpiredSession() {
  import('@/stores/user').then(({ useUserStore }) => {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) return

    userStore.logout()
    import('@/utils/authPrompt').then(({ openLoginRequiredPrompt }) => {
      openLoginRequiredPrompt({
        title: '登录已过期',
        message: '您的登录状态已失效，请重新登录'
      })
    })
  })
}

/**
 * 根据请求体类型补齐 Content-Type，并注入 sa-token 鉴权 Header。
 *
 * <p>注入约定:从 localStorage('token') 取 token,加 "Bearer " 前缀挂到 Authorization。
 * 后端 sa-token 配置了 {@code token-prefix: Bearer},会自动剥前缀解析,前后端约定一致。
 * 全端(Web / App)统一走 Header,不再依赖任何浏览器 Cookie。
 *
 * @param config Axios 请求配置。
 * @returns 补齐后的请求配置。
 */
function normalizeRequestConfig(config: InternalAxiosRequestConfig) {
  if (config.headers) {
    if (config.data instanceof URLSearchParams) {
      config.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8'
    } else if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
      delete config.headers['Content-Type']
    } else if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
  }

  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
}

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => normalizeRequestConfig(config),
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    const isSilentSessionCheck = response.config.headers?.[SILENT_SESSION_CHECK_HEADER] === '1'

    if (data?.code === NO_LOGIN_CODE && !isSilentSessionCheck) {
      handleExpiredSession()
    }

    if (data?.code === 50000) {
      useToast({ type: 'error', message: '系统异常' })
    }

    return response
  },
  (error) => {
    let message = '网络错误，请稍后重试'

    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          handleExpiredSession()
          return Promise.reject(error)
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || message
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    }

    useToast({ type: 'error', message })
    return Promise.reject(error)
  }
)

export default request
