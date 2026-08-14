import request from '@/utils/request'

// 发布系统通知
export function publishSystemNotice(data) {
  return request({
    url: '/admin/notices/system',
    method: 'post',
    data: data
  })
}
