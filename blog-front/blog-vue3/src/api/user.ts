import request from './request'
import type { UserInfo } from '@/stores/user'

// 登录
export function login(data: { username: string; password: string }) {
  const params = new URLSearchParams()
  params.append('username', data.username)
  params.append('password', data.password)
  return request.post('/api/login', params)
}

// 退出登录
export function logout() {
  return request.post('/api/logout')
}

// 获取当前服务端会话中的登录用户；启动校验使用静默头，避免本地旧状态被清理时弹出过期提示。
export function getCurrentUser() {
  return request.get('/api/users/current', {
    headers: {
      'X-Silent-Session-Check': '1'
    }
  })
}

// 注册
export function register(data: { username: string; password: string; code: string; nickname?: string }) {
  return request.post('/api/register', data)
}

// 发送验证码
export function sendCode(email: string) {
  return request.get('/api/users/code', { params: { email } })
}

// 修改密码
export function updatePassword(data: { username: string; password: string; code: string }) {
  return request.put('/api/users/password', data)
}

// 绑定邮箱
export function bindEmail(data: { email: string; code: string }) {
  return request.post('/api/users/email', data)
}

// QQ 登录
export function qqLogin(data: { code: string; state: string }) {
  return request.post('/api/users/oauth/qq', data)
}

// 微博登录
export function weiboLogin(data: { code: string }) {
  return request.post('/api/users/oauth/weibo', data)
}

// Gitee 登录
export function giteeLogin(data: { code: string }) {
  return request.post('/api/users/oauth/gitee', data)
}

// 更新用户信息
export function updateUserInfo(data: Partial<UserInfo>) {
  return request.put('/api/users/info', data)
}

// 上传头像文件
export function updateAvatarFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/users/avatar', formData)
}

// 使用链接更新头像
export function updateAvatarLink(avatar: string) {
  return request.put('/api/users/avatar/link', { avatar })
}
