import request from '@/utils/request'

// 查询说说列表
export function listTalks(query) {
  return request({
    url: '/admin/talks',
    method: 'get',
    params: query
  })
}

// 查询说说详情
export function getTalk(talkId) {
  return request({
    url: '/admin/talks/' + talkId,
    method: 'get'
  })
}

// 新增或修改说说
export function saveOrUpdateTalk(data) {
  return request({
    url: '/admin/talks',
    method: 'post',
    data: data
  })
}

// 删除说说
export function deleteTalks(talkIds) {
  return request({
    url: '/admin/talks',
    method: 'delete',
    data: talkIds
  })
}
