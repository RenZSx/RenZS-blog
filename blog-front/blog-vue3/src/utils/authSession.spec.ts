import { describe, expect, it, vi } from 'vitest'
import { syncAuthenticatedSession } from './authSession'

describe('authSession', () => {
  it('clears stale local login state when the server session is missing', async () => {
    const logout = vi.fn()
    const login = vi.fn()
    const getCurrentUser = vi.fn().mockResolvedValue({
      data: {
        flag: false,
        code: 40001,
        message: '用户未登录'
      }
    })

    const result = await syncAuthenticatedSession({
      isLoggedIn: true,
      login,
      logout,
      getCurrentUser
    })

    expect(result).toBe(false)
    expect(logout).toHaveBeenCalledTimes(1)
    expect(login).not.toHaveBeenCalled()
  })
})
