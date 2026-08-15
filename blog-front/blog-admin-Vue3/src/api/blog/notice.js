import request from '@/utils/request'

// 发布系统通知
export function publishSystemNotice(data) {
  return request({
    url: '/admin/notices/system',
    method: 'post',
    data: data
  })
}

// 查询当前用户的通知列表（兼容顶部通知组件使用的数据结构）
export async function listNoticeTop() {
  const res = await request({
    url: '/notices',
    method: 'get',
    params: { current: 1, size: 10 }
  })
  const records = res.data?.recordList || []
  const data = records.map(item => ({
    ...item,
    noticeId: item.id,
    noticeTitle: item.content || item.replyContent || '系统通知',
    noticeContent: item.content || item.replyContent || '',
    isRead: item.isRead === 1 || item.isRead === true,
    status: 0
  }))
  return {
    ...res,
    data,
    unreadCount: data.filter(item => !item.isRead).length
  }
}

// 查询单条通知。后端没有详情接口，复用分页列表并在前端筛选。
export async function getNotice(noticeId) {
  const res = await request({
    url: '/notices',
    method: 'get',
    params: { current: 1, size: 100 }
  })
  const item = (res.data?.recordList || []).find(notice => String(notice.id) === String(noticeId))
  if (!item) {
    return { ...res, data: null }
  }
  return {
    ...res,
    data: {
      ...item,
      noticeId: item.id,
      noticeTitle: item.content || item.replyContent || '系统通知',
      noticeContent: item.content || item.replyContent || '',
      isRead: item.isRead === 1 || item.isRead === true,
      status: 0
    }
  }
}

// 标记通知已读。系统通知类型为 system，其他通知由后端按类型判断。
export function markNoticeRead(noticeId, noticeType = 'system') {
  return request({
    url: `/notices/${noticeId}/read`,
    method: 'put',
    params: { noticeType }
  })
}

export function markNoticeReadAll() {
  return request({
    url: '/notices/read-all',
    method: 'put'
  })
}
