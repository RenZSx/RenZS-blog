import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'app-theme'  // 'light' | 'dark' | 'auto'

/**
 * 主题 store
 *
 * 设计:
 *   - 持久化用户偏好到 uni.storage('app-theme')
 *   - 'auto' 模式下跟随系统(uniapp 提供 uni.getSystemInfoSync().theme)
 *   - 暴露 isDark 计算属性,组件通过 :class="{ dark: isDark }" 切换
 *
 * 使用:
 *   <view :class="{ dark: themeStore.isDark }">
 *   或在 App.vue 顶层加全局 class
 */
export const useThemeStore = defineStore('theme', () => {
  // 'light' | 'dark' | 'auto'
  const mode = ref(uni.getStorageSync(STORAGE_KEY) || 'light')
  const systemIsDark = ref(false)

  // 探测系统主题(只在 App 平台有意义)
  try {
    const info = uni.getSystemInfoSync()
    systemIsDark.value = info.theme === 'dark'
    // 监听系统主题变化
    if (uni.onThemeChange) {
      uni.onThemeChange(({ theme }) => {
        systemIsDark.value = theme === 'dark'
      })
    }
  } catch (e) {
    systemIsDark.value = false
  }

  const isDark = computed(() => {
    if (mode.value === 'auto') return systemIsDark.value
    return mode.value === 'dark'
  })

  function setMode(next) {
    if (!['light', 'dark', 'auto'].includes(next)) return
    mode.value = next
    uni.setStorageSync(STORAGE_KEY, next)
  }

  function toggle() {
    setMode(isDark.value ? 'light' : 'dark')
  }

  return { mode, systemIsDark, isDark, setMode, toggle }
})
