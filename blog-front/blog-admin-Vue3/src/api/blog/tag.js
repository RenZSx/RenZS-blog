import request from '@/utils/request'

// 查询标签列表
export function listTags(query) {
  return request({
    url: '/admin/tags',
    method: 'get',
    params: query
  })
}

// 查询标签搜索列表
export function searchTags() {
  return request({
    url: '/admin/tags/search',
    method: 'get'
  })
}

// 新增或修改标签
export function saveOrUpdateTag(data) {
  return request({
    url: '/admin/tags',
    method: 'post',
    data: data
  })
}

// 删除标签
export function deleteTags(tagIds) {
  return request({
    url: '/admin/tags',
    method: 'delete',
    data: tagIds
  })
}
