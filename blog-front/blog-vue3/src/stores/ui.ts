import { defineStore } from 'pinia'
import { ref } from 'vue'

export const AUTH_PROMPT_DEFAULTS: {
  title: string
  message: string
  redirect: string
  confirmText: string
} = {
  title: '请先登录',
  message: '当前操作需要登录后才能继续',
  redirect: '/',
  confirmText: '前往登录'
}

const AUTH_ROUTE_PREFIXES = ['/auth', '/login', '/register', '/forgot-password'] as const

export function normalizeAuthRedirect(redirect?: string) {
  const normalized = redirect?.trim() || AUTH_PROMPT_DEFAULTS.redirect

  if (!normalized.startsWith('/')) {
    return AUTH_PROMPT_DEFAULTS.redirect
  }

  if (normalized.startsWith('//')) {
    return AUTH_PROMPT_DEFAULTS.redirect
  }

  const pathname = normalized.split(/[?#]/, 1)[0]

  if (
    AUTH_ROUTE_PREFIXES.some((route) => pathname === route)
  ) {
    return AUTH_PROMPT_DEFAULTS.redirect
  }

  return normalized
}

export const useUIStore = defineStore('ui', () => {
  // State - 模态框状态
  const searchFlag = ref(false)
  const loginFlag = ref(false)
  const registerFlag = ref(false)
  const forgetFlag = ref(false)
  const emailFlag = ref(false)
  const drawer = ref(false)
  const loginUrl = ref('')
  const authPromptVisible = ref(false)
  const authPromptTitle = ref(AUTH_PROMPT_DEFAULTS.title)
  const authPromptMessage = ref(AUTH_PROMPT_DEFAULTS.message)
  const authPromptRedirect = ref(AUTH_PROMPT_DEFAULTS.redirect)
  const authPromptConfirmText = ref(AUTH_PROMPT_DEFAULTS.confirmText)

  // Actions - 模态框控制
  function setSearchFlag(value: boolean) {
    searchFlag.value = value
  }

  function setLoginFlag(value: boolean) {
    loginFlag.value = value
  }

  function setRegisterFlag(value: boolean) {
    registerFlag.value = value
  }

  function setForgetFlag(value: boolean) {
    forgetFlag.value = value
  }

  function setEmailFlag(value: boolean) {
    emailFlag.value = value
  }

  function setDrawer(value: boolean) {
    drawer.value = value
  }

  function saveLoginUrl(url: string) {
    loginUrl.value = url
  }

  function openAuthPrompt(options?: {
    title?: string
    message?: string
    redirect?: string
    confirmText?: string
  }) {
    const normalizedRedirect = normalizeAuthRedirect(options?.redirect)

    authPromptVisible.value = true
    authPromptTitle.value = options?.title ?? AUTH_PROMPT_DEFAULTS.title
    authPromptMessage.value = options?.message ?? AUTH_PROMPT_DEFAULTS.message
    authPromptRedirect.value = normalizedRedirect
    authPromptConfirmText.value = options?.confirmText ?? AUTH_PROMPT_DEFAULTS.confirmText
  }

  function closeAuthPrompt() {
    authPromptVisible.value = false
    authPromptTitle.value = AUTH_PROMPT_DEFAULTS.title
    authPromptMessage.value = AUTH_PROMPT_DEFAULTS.message
    authPromptRedirect.value = AUTH_PROMPT_DEFAULTS.redirect
    authPromptConfirmText.value = AUTH_PROMPT_DEFAULTS.confirmText
  }

  // 关闭所有模态框
  function closeAllModals() {
    searchFlag.value = false
    loginFlag.value = false
    registerFlag.value = false
    forgetFlag.value = false
    emailFlag.value = false
  }

  // 打开登录框
  function openLogin() {
    closeAllModals()
    loginFlag.value = true
  }

  // 打开注册框
  function openRegister() {
    closeAllModals()
    registerFlag.value = true
  }

  // 打开搜索框
  function openSearch() {
    closeAllModals()
    searchFlag.value = true
  }

  return {
    // State
    searchFlag,
    loginFlag,
    registerFlag,
    forgetFlag,
    emailFlag,
    drawer,
    loginUrl,
    authPromptVisible,
    authPromptTitle,
    authPromptMessage,
    authPromptRedirect,
    authPromptConfirmText,
    // Actions
    setSearchFlag,
    setLoginFlag,
    setRegisterFlag,
    setForgetFlag,
    setEmailFlag,
    setDrawer,
    saveLoginUrl,
    openAuthPrompt,
    closeAuthPrompt,
    closeAllModals,
    openLogin,
    openRegister,
    openSearch
  }
})
