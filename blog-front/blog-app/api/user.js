import request from './request'
import { UPLOAD_AVATAR_URL } from '@/utils/config'
import { getToken } from '@/utils/auth'

/**
 * 账密登录
 * 与后端 LoginVO 对接,Content-Type: form-urlencoded
 * 响应 data 结构:LoginUserDTO { userInfo, tokenName, tokenValue, tokenTimeout }
 */
export function login({ username, password }) {
  return request({
    url: '/login',
    method: 'POST',
    data: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
    header: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

/**
 * 退出登录
 * 后端会清掉 Redis 中该 token,后续请求 401
 */
export function logout() {
  return request({ url: '/logout', method: 'POST' })
}

/**
 * 获取当前登录用户
 * silentSessionCheck=true:启动校验场景,40001 不弹提示不跳转
 */
export function getCurrentUser() {
  return request({
    url: '/users/current',
    method: 'GET',
    silentSessionCheck: true
  })
}

/**
 * QQ 登录
 * 后端要求 { openId, accessToken },uni.login provider=qq 拿到后转发即可
 * 响应 data 同账密登录:LoginUserDTO { userInfo, tokenName, tokenValue, tokenTimeout }
 */
export function qqLogin({ openId, accessToken }) {
  return request({
    url: '/users/oauth/qq',
    method: 'POST',
    data: { openId, accessToken }
  })
}

/**
 * 注册
 * @param {Object} data { username, password, code, nickname? }
 */
export function register(data) {
  return request({
    url: '/register',
    method: 'POST',
    data
  })
}

/**
 * 发送邮箱验证码
 * @param {string} username 邮箱地址(后端用 username 接收)
 */
export function sendVerificationCode(username) {
  return request({
    url: '/users/code',
    method: 'GET',
    data: { username }
  })
}

/**
 * 修改密码
 */
export function updatePassword(data) {
  return request({
    url: '/users/password',
    method: 'PUT',
    data
  })
}

/**
 * 更新个人资料
 */
export function updateUserInfo(data) {
  return request({
    url: '/users/info',
    method: 'PUT',
    data
  })
}

/**
 * 上传用户头像
 *
 * 注意:uni.uploadFile 不经过 api/request.js 的拦截器,
 * 必须在这里手动注入 Authorization Header,与全局约定保持一致。
 *
 * @param {string} filePath uni.chooseImage 返回的临时本地路径
 * @returns {Promise<{code, flag, message, data: 头像URL}>}
 */
export function uploadAvatar(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: UPLOAD_AVATAR_URL,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(res) {
        try {
          // uni.uploadFile 返回的 res.data 是字符串,需自己 JSON.parse
          const body = JSON.parse(res.data)
          if (res.statusCode < 200 || res.statusCode >= 300) {
            reject(new Error(body.message || `HTTP ${res.statusCode}`))
            return
          }
          resolve(body)
        } catch (e) {
          reject(new Error('响应格式异常'))
        }
      },
      fail(err) {
        reject(err)
      }
    })
  })
}
