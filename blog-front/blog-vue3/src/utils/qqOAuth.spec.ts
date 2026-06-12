import { describe, expect, it } from 'vitest'
import { extractQqOAuthPayload } from './qqOAuth'

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
})
