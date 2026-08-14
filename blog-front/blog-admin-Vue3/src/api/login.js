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

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/getInfo',
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