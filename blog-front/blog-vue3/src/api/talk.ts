import request from '@/api/request'

// 获取说说列表
export function getTalks(params: { current: number }) {
  return request.get('/api/talks', { params })
}

// 获取说说详情
export function getTalkById(id: number) {
  return request.get(`/api/talks/${id}`)
}

// 说说点赞
export function likeTalk(id: number) {
  return request.post(`/api/talks/${id}/like`)
}