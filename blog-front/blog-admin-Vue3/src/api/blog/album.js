import request from '@/utils/request'

// 查询相册列表
export function listAlbums(query) {
  return request({
    url: '/admin/photos/albums',
    method: 'get',
    params: query
  })
}

// 查询相册详情
export function getAlbum(albumId) {
  return request({
    url: '/admin/photos/albums/' + albumId + '/info',
    method: 'get'
  })
}

// 新增或修改相册
export function saveOrUpdateAlbum(data) {
  return request({
    url: '/admin/photos/albums',
    method: 'post',
    data: data
  })
}

// 删除相册
export function deleteAlbum(albumId) {
  return request({
    url: '/admin/photos/albums/' + albumId,
    method: 'delete'
  })
}

// 查询照片列表
export function listPhotos(query) {
  return request({
    url: '/admin/photos',
    method: 'get',
    params: query
  })
}

// 保存照片
export function savePhotos(data) {
  return request({
    url: '/admin/photos',
    method: 'post',
    data: data
  })
}

// 更新照片信息
export function updatePhoto(data) {
  return request({
    url: '/admin/photos',
    method: 'put',
    data: data
  })
}

// 删除照片
export function deletePhotos(photoIds) {
  return request({
    url: '/admin/photos',
    method: 'delete',
    data: photoIds
  })
}

// 移动照片
export function movePhotos(data) {
  return request({
    url: '/admin/photos/album',
    method: 'put',
    data: data
  })
}

// 更新照片逻辑删除状态(回收站恢复/移入回收站)
export function updatePhotoDelete(data) {
  return request({
    url: '/admin/photos/delete',
    method: 'put',
    data: data
  })
}
