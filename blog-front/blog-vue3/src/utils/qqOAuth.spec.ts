import { describe, expect, it } from 'vitest'
import { extractQqOAuthPayload, hasQqAccessToken } from './qqOAuth'

describe('qqOAuth', () => {
  it('extracts QQ payload from legacy query code and state', () => {
    expect(extractQqOAuthPayload({ code: 'qq-open-id', state: 'qq-access-token' })).toEqual({
      openId: 'qq-open-id',
      accessToken: 'qq-access-token'
    })
  })

  it('extracts QQ access token from hash callback', () => {
    expect(extractQqOAuthPayload({}, '#access_token=qq-access-token&expires_in=7776000')).toEqual({
      openId: '',
      accessToken: 'qq-access-token'
    })
  })

  it('extracts QQ access token from hash route callback', () => {
    expect(extractQqOAuthPayload({}, '#/oauth/login/qq?access_token=qq-access-token&expires_in=7776000')).toEqual({
      openId: '',
      accessToken: 'qq-access-token'
    })
  })

  it('extracts QQ access token from full callback url fallback', () => {
    expect(
      extractQqOAuthPayload({}, 'https://www.renzs.top/oauth/login/qq#/oauth/login/qq?access_token=qq-access-token')
    ).toEqual({
      openId: '',
      accessToken: 'qq-access-token'
    })
  })

  it('detects missing QQ access token', () => {
    expect(hasQqAccessToken({ openId: '', accessToken: '' })).toBe(false)
    expect(hasQqAccessToken({ openId: '', accessToken: ' qq-access-token ' })).toBe(true)
  })
})
