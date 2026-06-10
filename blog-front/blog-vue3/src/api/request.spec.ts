/**
 * 功能说明: 验证请求实例的会话 cookie 配置。
 * 作者: OpenAI Codex
 * 创建时间: 2026-06-08
 * 用途概述: 防止跨域或直连后端时丢失 Spring Session 的 JSESSIONID。
 */

import { describe, expect, it, vi } from 'vitest'

vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn()
}))

describe('request', () => {
  it('sends browser credentials with API requests', async () => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      },
      configurable: true
    })

    const { default: request } = await import('./request')

    // Spring Security 当前使用服务端 Session，所有接口请求都必须允许浏览器带上 JSESSIONID。
    expect(request.defaults.withCredentials).toBe(true)
  })

  it('marks current-user checks as silent session validation requests', async () => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      },
      configurable: true
    })

    const { getCurrentUser } = await import('./user')
    const { default: request } = await import('./request')
    const adapter = vi.fn((config) => Promise.resolve({
      data: { flag: false, code: 40001 },
      status: 200,
      statusText: 'OK',
      headers: {},
      config
    }))
    request.defaults.adapter = adapter

    await getCurrentUser()

    expect(adapter).toHaveBeenCalledWith(
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Silent-Session-Check': '1'
        })
      })
    )
  })
})
