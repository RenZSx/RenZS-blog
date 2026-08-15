import request from '@/utils/request'

// 查询分类列表
export function listCategories(query) {
  return request({
    url: '/admin/categories',
    method: 'get',
    params: query
  })
}

// 查询分类搜索列表
export function searchCategories() {
  return request({
    url: '/admin/categories/search',
    method: 'get'
  })
}

// 新增或修改分类
export function saveOrUpdateCategory(data) {
  return request({
    url: '/admin/categories',
    method: 'post',
    data: data
  })
}

// 删除分类
export function deleteCategories(categoryIds) {
  return request({
    url: '/admin/categories',
    method: 'delete',
    data: categoryIds
  })
}
