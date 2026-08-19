import { getToken } from '@/utils/auth'

// 日期格式化
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

// 获取上传文件的请求头
export function getUploadHeaders() {
  // 登录态统一存储在 Admin-Token Cookie；保留旧 localStorage 作为兼容兜底。
  const token = getToken() || localStorage.getItem('token')
  return token ? { Authorization: 'Bearer ' + token } : {}
}

// 压缩图片
export function compressImage(file, quality = 0.8) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        canvas.width = img.width
        canvas.height = img.height
        ctx.drawImage(img, 0, 0, img.width, img.height)
        canvas.toBlob(
          (blob) => {
            resolve(new File([blob], file.name, { type: file.type }))
          },
          file.type,
          quality
        )
      }
      img.onerror = reject
    }
    reader.onerror = reject
  })
}

// 检查文件大小
export function checkFileSize(file, maxSize) {
  const fileSize = file.size / 1024 // KB
  return fileSize <= maxSize
}

// 生成随机ID
export function generateId() {
  return Math.random().toString(36).substr(2, 9)
}
