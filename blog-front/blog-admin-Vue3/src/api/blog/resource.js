import request from '@/utils/request'

// 查询资源列表
export function listResources(query) {
  return request({
    url: '/admin/resources',
    method: 'get',
    params: query
  })
}

// 新增或修改资源
export function saveOrUpdateResource(data) {
  return request({
    url: '/admin/resources',
    method: 'post',
    data: data
  })
}

// 删除资源
export function deleteResource(resourceId) {
  return request({
    url: '/admin/resources/' + resourceId,
    method: 'delete'
  })
}
