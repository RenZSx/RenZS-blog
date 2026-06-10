import request from './request'

// 获取博客信息
export function getBlogInfo() {
  return request.get('/api/')
}

// 上报访客信息
export function reportVisitor() {
  return request.post('/api/report')
}

// 获取关于页面
export function getAbout() {
  return request.get('/api/about')
}
