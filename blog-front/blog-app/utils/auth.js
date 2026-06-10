import { TOKEN_KEY } from './config'

/**
 * token 存取封装
 * uni.getStorageSync 在 key 不存在时返回 '',不会抛错;
 * 但为保持一致,统一返回 string(空字符串视为未登录)。
 */

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (!token) return
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}
