import request from './request'

// 纪念页前台配置接口
export function getLoveConfig() {
  return request.get('/api/love/config')
}

// 飞书传信前台接口
export function getLoveLetter() {
  return request.get('/api/love/letter')
}
