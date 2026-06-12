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
  const hashStartIndex = normalizedHash.indexOf('#')
  const hashContent = hashStartIndex >= 0 ? normalizedHash.slice(hashStartIndex + 1) : normalizedHash
  const queryStartIndex = hashContent.indexOf('?')
  const hashQuery = queryStartIndex >= 0 ? hashContent.slice(queryStartIndex + 1) : hashContent
  return new URLSearchParams(hashQuery).get(key) || ''
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

export function hasQqAccessToken(payload: QqOAuthPayload) {
  return payload.accessToken.trim().length > 0
}
