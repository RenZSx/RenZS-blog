import request from '@/utils/request'

// 登录方法 - 博客后端不需要验证码
export function login(username, password) {
  // 使用 URLSearchParams 发送表单格式数据
  const data = new URLSearchParams()
  data.append('username', username)
  data.append('password', password)

  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    method: 'post',
    data: data
  })
}

// 注册方法
export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取当前登录用户信息
// 博客后端接口: GET /users/current
// 该接口不创建新登录态,仅凭 Authorization Header 中的 sa-token 反查 Redis,
// 专供前端刷新页面后重建用户状态使用,返回结构与登录接口的 userInfo 一致。
export function getInfo() {
  return request({
    url: '/users/current',
    method: 'get'
  })
}

// 解锁屏幕
export function unlockScreen(password) {
  return request({
    url: '/unlockscreen',
    method: 'post',
    data: { password }
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码 - 博客后端不需要,保留接口定义以兼容
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}