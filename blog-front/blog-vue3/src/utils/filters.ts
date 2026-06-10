import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置 dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 格式化日期 (YYYY-MM-DD)
 */
export function formatDate(value: string | Date | number): string {
  if (!value) return ''
  return dayjs(value).format('YYYY-MM-DD')
}

/**
 * 格式化年份 (YYYY)
 */
export function formatYear(value: string | Date | number): string {
  if (!value) return ''
  return dayjs(value).format('YYYY')
}

/**
 * 格式化时间 (HH:mm:ss)
 */
export function formatHour(value: string | Date | number): string {
  if (!value) return ''
  return dayjs(value).format('HH:mm:ss')
}

/**
 * 格式化完整时间 (YYYY-MM-DD HH:mm:ss)
 */
export function formatTime(value: string | Date | number): string {
  if (!value) return ''
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化相对时间 (几秒前、几分钟前等)
 */
export function formatRelativeTime(value: string | Date | number): string {
  if (!value) return ''
  return dayjs(value).fromNow()
}

/**
 * 格式化数字 (1000 -> 1k)
 */
export function formatNum(value: number): string {
  if (value === null || value === undefined) return '0'
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + 'w'
  }
  if (value >= 1000) {
    return (value / 1000).toFixed(1) + 'k'
  }
  return String(value)
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export { dayjs }
