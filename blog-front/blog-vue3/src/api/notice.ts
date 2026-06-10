import request from './request'
import type { NoticeListQuery, NoticeType } from '@/types/notice'

export function getNotices(params: NoticeListQuery = {}) {
  return request.get('/api/notices', { params })
}

export function getNoticeUnreadCount() {
  return request.get('/api/notices/unread-count')
}

export function readNotice(id: number, noticeType?: NoticeType) {
  return request.put(`/api/notices/${id}/read`, null, {
    params: noticeType ? { noticeType } : undefined
  })
}

export function readAllNotices() {
  return request.put('/api/notices/read-all')
}
