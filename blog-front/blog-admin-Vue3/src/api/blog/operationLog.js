import request from '@/utils/request'

// 查询操作日志列表
export function listOperationLogs(query) {
  return request({
    url: '/admin/operation/logs',
    method: 'get',
    params: query
  })
}

// 删除操作日志
export function deleteOperationLogs(logIds) {
  return request({
    url: '/admin/operation/logs',
    method: 'delete',
    data: logIds
  })
}
