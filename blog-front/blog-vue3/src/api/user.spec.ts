import { describe, expect, it, vi } from 'vitest'

vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn()
}))

describe('user api', () => {
  it('sends QQ login payload with backend field names', async () => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      },
      configurable: true
    })

    const { qqLogin } = await import('./user')
    const { default: request } = await import('./request')
    const adapter = vi.fn((config) => Promise.resolve({
      data: { flag: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config
    }))
    request.defaults.adapter = adapter

    await qqLogin({ openId: 'qq-open-id', accessToken: 'qq-access-token' })

    const config = adapter.mock.calls[0][0]
    expect(config.url).toBe('/api/users/oauth/qq')
    expect(config.method).toBe('post')
    expect(JSON.parse(config.data)).toEqual({
      openId: 'qq-open-id',
      accessToken: 'qq-access-token'
    })
  })

  it('sends QQ bind payload with backend field names', async () => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      },
      configurable: true
    })

    const { bindQq } = await import('./user')
    const { default: request } = await import('./request')
    const adapter = vi.fn((config) => Promise.resolve({
      data: { flag: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config
    }))
    request.defaults.adapter = adapter

    await bindQq({ openId: 'qq-open-id', accessToken: 'qq-access-token' })

    const config = adapter.mock.calls[0][0]
    expect(config.url).toBe('/api/users/oauth/qq/bind')
    expect(config.method).toBe('post')
    expect(JSON.parse(config.data)).toEqual({
      openId: 'qq-open-id',
      accessToken: 'qq-access-token'
    })
  })

  it('rejects QQ login without access token before sending request', async () => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      },
      configurable: true
    })

    const { qqLogin } = await import('./user')
    const { default: request } = await import('./request')
    const adapter = vi.fn()
    request.defaults.adapter = adapter

    await expect(qqLogin({ openId: '', accessToken: '' })).rejects.toThrow('QQ授权信息缺失，请重新授权')
    expect(adapter).not.toHaveBeenCalled()
  })

  it('sends email code request with backend username parameter', async () => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      },
      configurable: true
    })

    const { sendCode } = await import('./user')
    const { default: request } = await import('./request')
    const adapter = vi.fn((config) => Promise.resolve({
      data: { flag: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config
    }))
    request.defaults.adapter = adapter

    await sendCode('bind@example.com')

    const config = adapter.mock.calls[0][0]
    expect(config.url).toBe('/api/users/code')
    expect(config.method).toBe('get')
    expect(config.params).toEqual({ username: 'bind@example.com' })
  })
})
