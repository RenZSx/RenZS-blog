import { ref, h, createApp } from 'vue'
import ToastComponent from '@/components/toast/Toast.vue'
import vuetify from '@/plugins/vuetify'

export interface ToastOptions {
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  duration?: number
}

// Toast 队列管理
let toastContainer: HTMLDivElement | null = null
let currentApp: ReturnType<typeof createApp> | null = null
let timer: ReturnType<typeof setTimeout> | null = null

/**
 * 显示 Toast 提示
 */
export function useToast(options: ToastOptions) {
  const { type, message, duration = 2000 } = options

  // 创建容器
  if (!toastContainer) {
    toastContainer = document.createElement('div')
    toastContainer.className = 'toast-wrapper'
    document.body.appendChild(toastContainer)
  }

  // 清除之前的 Toast
  if (timer) {
    clearTimeout(timer)
  }
  if (currentApp) {
    currentApp.unmount()
  }

  // 创建新的 Toast
  const show = ref(true)
  currentApp = createApp({
    render() {
      return h(ToastComponent, {
        type,
        message,
        show: show.value
      })
    }
  })

  // 注册 Vuetify 插件
  currentApp.use(vuetify)

  currentApp.mount(toastContainer)

  // 自动关闭
  timer = setTimeout(() => {
    show.value = false
    setTimeout(() => {
      if (currentApp) {
        currentApp.unmount()
        currentApp = null
      }
    }, 300) // 等待动画完成
  }, duration)
}

// 快捷方法
export const toast = {
  success: (message: string, duration?: number) => useToast({ type: 'success', message, duration }),
  error: (message: string, duration?: number) => useToast({ type: 'error', message, duration }),
  warning: (message: string, duration?: number) => useToast({ type: 'warning', message, duration }),
  info: (message: string, duration?: number) => useToast({ type: 'info', message, duration })
}
