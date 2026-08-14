import request from '@/utils/request'

// 查询页面列表
export function listPages(query) {
  return request({
    url: '/admin/pages',
    method: 'get',
    params: query
  })
}

// 保存或更新页面
export function saveOrUpdatePage(data) {
  return request({
    url: '/admin/pages',
    method: 'post',
    data: data
  })
}

// 删除页面
export function deletePage(pageId) {
  return request({
    url: '/admin/pages/' + pageId,
    method: 'delete'
  })
}
