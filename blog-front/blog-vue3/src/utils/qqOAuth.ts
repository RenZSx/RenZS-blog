import type { LocationQuery } from 'vue-router'

export interface QqOAuthPayload {
  openId: string
  accessToken: string
}

function firstQueryValue(value: LocationQuery[string]) {
  return Array.isArray(value) ? value[0] : value
}

function readHashParam(hash: string, key: string) {
  const normalizedHash = hash.startsWith('#') ? hash.slice(1) : hash
  return new URLSearchParams(normalizedHash).get(key) || ''
}

export function extractQqOAuthPayload(query: LocationQuery, hash = ''): QqOAuthPayload {
  const queryCode = firstQueryValue(query.code)
  const queryState = firstQueryValue(query.state)
  const queryOpenId = firstQueryValue(query.openId) || firstQueryValue(query.openid)
  const queryAccessToken = firstQueryValue(query.accessToken) || firstQueryValue(query.access_token)

  return {
    openId: String(queryOpenId || queryCode || readHashParam(hash, 'openid') || ''),
    accessToken: String(queryAccessToken || queryState || readHashParam(hash, 'access_token') || '')
  }
}
