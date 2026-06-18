/**
 * 功能说明: 樱花特效偏好和动画数据工具。
 * 作者: ChenFY
 * 创建时间: 2026-06-18
 * 用途概述: 为全局樱花组件提供本地开关持久化、减少动态效果兜底和花瓣数据生成。
 */

export const SAKURA_STORAGE_KEY = 'sakura-effect-enabled'

export type SakuraPreference = boolean | null

export interface SakuraPetal {
  x: number
  y: number
  size: number
  rotate: number
  speedX: number
  speedY: number
  rotateSpeed: number
  remainingResets: number
}

export interface SakuraBounds {
  width: number
  height: number
}

export interface SakuraOptions {
  count?: number
  limitTimes?: number
}

const DEFAULT_SAKURA_COUNT = 28
const DEFAULT_LIMIT_TIMES = -1
const UNIFORM_SPEED_X = -1.45
const UNIFORM_SPEED_Y = 1.65
const UNIFORM_ROTATE_SPEED = 0.014

/**
 * 解析本地存储中的樱花开关值。
 *
 * @param value localStorage 中读取到的原始字符串。
 * @returns 布尔偏好；非法值返回 null。
 */
export function parseSakuraPreference(value: string | null): SakuraPreference {
  if (value === 'true') return true
  if (value === 'false') return false
  return null
}

/**
 * 将樱花开关值序列化为 localStorage 字符串。
 *
 * @param enabled 是否开启樱花。
 * @returns 可持久化的字符串。
 */
export function serializeSakuraPreference(enabled: boolean) {
  return enabled ? 'true' : 'false'
}

/**
 * 计算樱花特效的初始开启状态。
 *
 * @param storedValue 本地存储中的偏好值。
 * @param prefersReducedMotion 系统是否偏好减少动态效果。
 * @returns 初始是否启用。
 */
export function resolveInitialSakuraEnabled(storedValue: string | null, prefersReducedMotion: boolean) {
  const parsedPreference = parseSakuraPreference(storedValue)
  if (parsedPreference !== null) {
    return parsedPreference
  }
  return !prefersReducedMotion
}

/**
 * 生成一片樱花的运动参数。
 *
 * @param bounds 当前画布尺寸。
 * @param limitTimes 越界后允许重置的次数，-1 表示无限循环。
 * @returns 樱花花瓣数据。
 */
export function createSakuraPetal(bounds: SakuraBounds, limitTimes = DEFAULT_LIMIT_TIMES): SakuraPetal {
  return {
    x: Math.random() * bounds.width,
    y: Math.random() * bounds.height,
    size: Math.random(),
    rotate: Math.random() * 6,
    speedX: UNIFORM_SPEED_X,
    speedY: UNIFORM_SPEED_Y,
    rotateSpeed: UNIFORM_ROTATE_SPEED,
    remainingResets: limitTimes
  }
}

/**
 * 生成指定数量的樱花花瓣。
 *
 * @param bounds 当前画布尺寸。
 * @param options 数量和越界重置配置。
 * @returns 樱花花瓣列表。
 */
export function createSakuraPetals(bounds: SakuraBounds, options: SakuraOptions = {}) {
  const count = Math.max(0, Math.floor(options.count ?? DEFAULT_SAKURA_COUNT))
  const limitTimes = options.limitTimes ?? DEFAULT_LIMIT_TIMES
  return Array.from({ length: count }, () => createSakuraPetal(bounds, limitTimes))
}

/**
 * 判断樱花是否已经离开可视区域。
 *
 * @param petal 樱花花瓣。
 * @param bounds 当前画布尺寸。
 * @returns 是否越界。
 */
export function isSakuraOutOfBounds(petal: SakuraPetal, bounds: SakuraBounds) {
  const padding = 48 * petal.size
  return (
    petal.x > bounds.width + padding ||
    petal.x < -padding ||
    petal.y > bounds.height + padding ||
    petal.y < -padding
  )
}

/**
 * 推进一片樱花的位置。
 *
 * @param petal 樱花花瓣。
 * @param bounds 当前画布尺寸。
 * @returns 更新后的花瓣数据。
 */
export function updateSakuraPetal(petal: SakuraPetal, bounds: SakuraBounds) {
  petal.x += petal.speedX
  petal.y += petal.speedY
  petal.rotate += petal.rotateSpeed

  if (!isSakuraOutOfBounds(petal, bounds)) {
    return petal
  }

  if (petal.remainingResets === 0) {
    return petal
  }

  if (petal.remainingResets > 0) {
    petal.remainingResets -= 1
  }

  if (Math.random() > 0.4) {
    petal.x = Math.random() * bounds.width
    petal.y = 0
  } else {
    petal.x = bounds.width
    petal.y = Math.random() * bounds.height
  }
  petal.size = Math.random()
  petal.rotate = Math.random() * 6
  petal.speedX = UNIFORM_SPEED_X
  petal.speedY = UNIFORM_SPEED_Y
  petal.rotateSpeed = UNIFORM_ROTATE_SPEED
  return petal
}
