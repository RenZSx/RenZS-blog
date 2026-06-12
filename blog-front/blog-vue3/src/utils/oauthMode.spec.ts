import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearOauthMode, getOauthMode, saveOauthMode } from './oauthMode'

describe('oauthMode', () => {
  beforeEach(() => {
    const store = new Map<string, string>()
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => store.get(key) ?? null,
      setItem: (key: string, value: string) => store.set(key, value),
      removeItem: (key: string) => store.delete(key),
      clear: () => store.clear()
    })
  })

  afterEach(() => {
    localStorage.clear()
    vi.unstubAllGlobals()
  })

  it('persists bind mode for qq', () => {
    saveOauthMode({ provider: 'qq', mode: 'bind' })

    expect(getOauthMode()).toEqual({ provider: 'qq', mode: 'bind' })
  })

  it('clears mode after callback', () => {
    saveOauthMode({ provider: 'qq', mode: 'bind' })

    clearOauthMode()

    expect(getOauthMode()).toBeNull()
  })
})
