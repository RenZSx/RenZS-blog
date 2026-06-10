import request from './request'

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
