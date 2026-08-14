import request from '@/utils/request'

// 查询菜单列表
export function listMenus(query) {
  return request({
    url: '/admin/menus',
    method: 'get',
    params: query
  })
}

// 查询用户菜单
export function getUserMenus() {
  return request({
    url: '/admin/user/menus',
    method: 'get'
  })
}

// 新增或修改菜单
export function saveOrUpdateMenu(data) {
  return request({
    url: '/admin/menus',
    method: 'post',
    data: data
  })
}

// 删除菜单
export function deleteMenu(menuId) {
  return request({
    url: '/admin/menus/' + menuId,
    method: 'delete'
  })
}
