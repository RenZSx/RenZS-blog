import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/store/theme'

/**
 * 主题 class composable
 *
 * 用法(每个页面 root view 上):
 *   <view class="page" :class="{ 'theme-dark': isDark }">
 *
 * 配合 App.vue 中浅/暗色 CSS 变量,实现 App 端(无 document.body)也能切换暗色。
 * H5 端 App.vue 已经通过 document.body.classList 切换,这里再加 class 也无副作用。
 */
export function useThemeClass() {
  const { isDark } = storeToRefs(useThemeStore())
  return { isDark }
}
