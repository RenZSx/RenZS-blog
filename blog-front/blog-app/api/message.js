import request from './request'

/**
 * 留言列表(后端一次返回全部弹幕)
 */
export function getMessages() {
  return request({
    url: '/messages',
    method: 'GET'
  })
}

/**
 * 发表留言
 * 后端 MessageVO 要求 4 字段全部非空:
 *   - nickname:留言人昵称
 *   - avatar:头像 URL
 *   - messageContent:内容
 *   - time:弹幕滚动速度(秒),0-60 整数,前端默认 15
 *
 * @param {{nickname,avatar,messageContent,time}} data
 */
export function sendMessage(data) {
  return request({
    url: '/messages',
    method: 'POST',
    data
  })
}
