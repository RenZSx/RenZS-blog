/**
 * 通知 createTime 字段兼容处理
 *
 * 后端 NoticeDTO.createTime 是 LocalDateTime,序列化可能出现 3 种格式:
 *   1. ISO 字符串 "2026-06-10T15:30:00"
 *   2. timestamp 数字
 *   3. 对象 { year, monthValue, dayOfMonth, hour, minute, second }
 *
 * 统一返回 "M-D HH:mm" 或 "YYYY-M-D" 友好格式
 */

function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

function readInt(v) {
  if (typeof v === 'number' && Number.isFinite(v)) return v
  if (typeof v === 'string' && /^\d+$/.test(v)) return parseInt(v, 10)
  return null
}

/**
 * 解析为 Date 对象(失败返回 null)
 */
function parseToDate(value) {
  if (!value) return null

  // 1. 已经是 Date
  if (value instanceof Date) return value

  // 2. timestamp 数字
  if (typeof value === 'number' && Number.isFinite(value)) {
    const d = new Date(value)
    return isNaN(d.getTime()) ? null : d
  }

  // 3. ISO 字符串
  if (typeof value === 'string') {
    const d = new Date(value)
    return isNaN(d.getTime()) ? null : d
  }

  // 4. LocalDateTime 对象
  if (typeof value === 'object') {
    const year = readInt(value.year)
    const month = readInt(value.monthValue) || readInt(value.month) || readInt(value.monthNumber)
    const day = readInt(value.dayOfMonth) || readInt(value.day)
    const hour = readInt(value.hour) || 0
    const minute = readInt(value.minute) || 0
    const second = readInt(value.second) || 0
    if (year !== null && month !== null && day !== null) {
      const d = new Date(year, month - 1, day, hour, minute, second)
      return isNaN(d.getTime()) ? null : d
    }
  }

  return null
}

/**
 * 格式化通知时间:
 *   - 1 分钟内:刚刚
 *   - 1 小时内:N 分钟前
 *   - 今天:HH:mm
 *   - 今年:M-D HH:mm
 *   - 跨年:YYYY-M-D
 */
export function formatNoticeTime(value) {
  const d = parseToDate(value)
  if (!d) return '刚刚'

  const now = Date.now()
  const diff = now - d.getTime()

  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)} 分钟前`

  const today = new Date()
  const isSameDay =
    d.getFullYear() === today.getFullYear() &&
    d.getMonth() === today.getMonth() &&
    d.getDate() === today.getDate()
  if (isSameDay) return `${pad(d.getHours())}:${pad(d.getMinutes())}`

  const isSameYear = d.getFullYear() === today.getFullYear()
  if (isSameYear) {
    return `${d.getMonth() + 1}-${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }

  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`
}
