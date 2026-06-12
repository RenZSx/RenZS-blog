/**
 * App 本地通知工具。
 *
 * 用于 App 正在运行时把实时消息同步到系统通知栏。未集成厂商 SDK 时,
 * 该能力不保证应用被系统杀死后的离线到达。
 */

/**
 * 创建一条系统本地通知。
 *
 * @param {Object} options 通知参数。
 * @param {string} options.title 通知标题。
 * @param {string} options.content 通知内容。
 * @param {Object|string} [options.payload] 点击通知后回传的数据。
 * @param {boolean} [options.cover=false] 是否覆盖同标题通知。
 */
export function createLocalNotification({ title, content, payload, cover = false }) {
  if (typeof plus === 'undefined' || !plus.push) return
  if (!title || !content) return

  try {
    const payloadStr = typeof payload === 'string'
      ? payload
      : JSON.stringify(payload || {})

    plus.push.createMessage(content, payloadStr, {
      title,
      cover
    })
  } catch (e) {
    console.warn('[native-notification] createMessage failed:', e)
  }
}

/**
 * 监听系统通知点击事件。
 *
 * @param {Function} handler 接收解析后的 payload。
 */
export function onNotificationClick(handler) {
  if (typeof plus === 'undefined' || !plus.push) return

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
