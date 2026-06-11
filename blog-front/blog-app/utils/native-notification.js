/**
 * App 端本地系统通知工具(Level 1 推送方案)
 *
 * 在 WebSocket 收到新通知 / 系统消息时,调本工具向系统通知栏推送一条本地通知。
 *
 * 平台兼容:
 *   - App (uniapp Android/iOS): 走 plus.push.createMessage,会落到系统通知栏 + 锁屏
 *   - H5 / 微信小程序 / 其他: plus 不存在,本工具静默 no-op,业务调用方无需判平台
 *
 * 局限:
 *   - 仅当 App 在前台/后台且未被系统杀掉时有效
 *   - App 被完全杀掉后 WS 断开,本工具同样不会触发 (这是 Level 1 的天然限制)
 *   - 完全杀掉后仍想接收推送 → 升级 Level 2 uni-push 2.0 (后端 + App 改造)
 */

/**
 * 创建一条系统本地通知
 *
 * @param {Object} options
 * @param {string} options.title     标题(显示在通知栏顶部)
 * @param {string} options.content   正文(显示在标题下方,过长会折叠)
 * @param {Object} [options.payload] 点击通知时回传的自定义数据(必须可 JSON 序列化)
 *                                   常用:{ type: 'notice', noticeId, jumpPath }
 * @param {boolean} [options.cover=false] 是否覆盖之前的同 title 通知
 */
export function createLocalNotification({ title, content, payload, cover = false }) {
  // H5 / 小程序等没 plus 对象,直接返回
  if (typeof plus === 'undefined' || !plus.push) {
    return
  }
  if (!title || !content) return

  try {
    // payload 必须是字符串(plus.push 限制)
    const payloadStr = typeof payload === 'string'
      ? payload
      : JSON.stringify(payload || {})

    plus.push.createMessage(
      content,
      payloadStr,
      {
        title,
        cover,
        // 点击通知后会唤起 App,我们用 plus.push.addEventListener('click') 监听
        // 也可以设 sound 'system' / vibrate true,这里走系统默认
      }
    )
  } catch (e) {
    console.warn('[native-notification] createMessage failed:', e)
  }
}

/**
 * 注册"点击通知"全局监听
 * 在 App.vue onLaunch 调用一次即可
 *
 * @param {Function} handler 接收 payload 对象,业务自己路由跳转
 */
export function onNotificationClick(handler) {
  if (typeof plus === 'undefined' || !plus.push) {
    return
  }
  try {
    plus.push.addEventListener('click', (msg) => {
      let payload = {}
      try {
        payload = typeof msg.payload === 'string'
          ? JSON.parse(msg.payload)
          : (msg.payload || {})
      } catch (e) {
        payload = { raw: msg.payload }
      }
      handler && handler(payload)
    }, false)
  } catch (e) {
    console.warn('[native-notification] addEventListener click failed:', e)
  }
}

/**
 * 清空本 App 在通知栏的所有通知(可选)
 * 适用于用户进通知页时,把堆积的系统通知清空
 */
export function clearAllNotifications() {
  if (typeof plus === 'undefined' || !plus.push) return
  try {
    plus.push.clear()
  } catch (e) {}
}
