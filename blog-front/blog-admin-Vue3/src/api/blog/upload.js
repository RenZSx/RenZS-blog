import request from '@/utils/request'

// 上传文件
export function uploadFile(data) {
  return request({
    url: '/admin/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传语音
export function uploadVoice(data) {
  return request({
    url: '/admin/voice',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
