/**
 * 功能说明：集中管理 Auth 页面模式、导航和展示文案。
 * 作者：ChenFY
 * 创建时间：2026-06-17
 * 用途概述：为登录、注册、找回密码共用卡片提供稳定的模式定义和内容查询。
 */
export type AuthMode = 'login' | 'register' | 'forgot-password'

/**
 * AuthModeContent 描述单个认证模式需要渲染的标题、说明和跳转文案。
 */
export type AuthModeContent = {
  kicker: string
  title: string
  subtitle: string
  actionLabel: string
  footerLead: string
  footerAction: string
  footerTarget: AuthMode
}

// 允许出现在路由 query 和页面状态中的认证模式集合。
const authModes = new Set<AuthMode>(['login', 'register', 'forgot-password'])

// 顶部三段切换的稳定导航项，顺序即页面展示顺序。
export const authNavItems: ReadonlyArray<{ key: AuthMode; label: string }> = [
  { key: 'login', label: '登录' },
  { key: 'register', label: '注册' },
  { key: 'forgot-password', label: '找回密码' }
] as const

// 每个认证模式对应的卡片文案，避免模板里堆叠条件表达式。
const authModeContent: Record<AuthMode, AuthModeContent> = {
  login: {
    kicker: 'Renzs Blog',
    title: '登录你的博客身份',
    subtitle: '继续阅读、评论、收藏，并同步你的互动状态。',
    actionLabel: '登录',
    footerLead: '还没有账号？',
    footerAction: '创建账号',
    footerTarget: 'register'
  },
  register: {
    kicker: 'New Account',
    title: '创建新的博客身份',
    subtitle: '完成邮箱验证后，就能拥有自己的互动身份。',
    actionLabel: '创建账号',
    footerLead: '已经有账号？',
    footerAction: '返回登录',
    footerTarget: 'login'
  },
  'forgot-password': {
    kicker: 'Recovery',
    title: '重设登录密码',
    subtitle: '通过邮箱验证码确认身份，然后设置一个新密码。',
    actionLabel: '重置密码',
    footerLead: '想起密码了？',
    footerAction: '返回登录',
    footerTarget: 'login'
  }
}

/**
 * 将路由 query 中的 mode 规范化为页面支持的认证模式。
 *
 * @param mode 路由传入的未知 mode 值。
 * @returns 合法认证模式；非法值统一降级为登录模式。
 */
export function normalizeAuthMode(mode: unknown): AuthMode {
  return typeof mode === 'string' && authModes.has(mode as AuthMode) ? (mode as AuthMode) : 'login'
}

/**
 * 获取指定认证模式的展示文案。
 *
 * @param mode 当前认证模式。
 * @returns 当前模式对应的卡片标题、说明、按钮和底部跳转文案。
 */
export function getAuthModeContent(mode: AuthMode): AuthModeContent {
  return authModeContent[mode]
}
