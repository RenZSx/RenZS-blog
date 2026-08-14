import request from '@/utils/request'

// 查询友链列表
export function listFriendLinks(query) {
  return request({
    url: '/admin/links',
    method: 'get',
    params: query
  })
}

// 查询友链详情
export function getFriendLink(linkId) {
  return request({
    url: '/admin/links/' + linkId,
    method: 'get'
  })
}

// 新增或修改友链
export function saveOrUpdateFriendLink(data) {
  return request({
    url: '/admin/links',
    method: 'post',
    data: data
  })
}

// 删除友链
export function deleteFriendLinks(linkIds) {
  return request({
    url: '/admin/links',
    method: 'delete',
    data: linkIds
  })
}
