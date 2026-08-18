import request from './request'

export type MusicRecord = {
  id: number
  musicName: string
  musicUrl: string
  createTime?: string
}

export function getMusicList() {
  return request.get('/api/music')
}

export function uploadMusic(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/admin/music', formData)
}
