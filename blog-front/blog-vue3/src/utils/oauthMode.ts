export type OauthProvider = 'qq' | 'weibo' | 'gitee'
export type OauthMode = 'login' | 'bind'

export interface SavedOauthMode {
  provider: OauthProvider
  mode: OauthMode
}

const OAUTH_MODE_KEY = 'oauth-mode'

export function saveOauthMode(mode: SavedOauthMode) {
  localStorage.setItem(OAUTH_MODE_KEY, JSON.stringify(mode))
}

export function getOauthMode(): SavedOauthMode | null {
  const raw = localStorage.getItem(OAUTH_MODE_KEY)
  if (!raw) return null

  try {
    const parsed = JSON.parse(raw) as SavedOauthMode
    if (parsed.provider && parsed.mode) return parsed
  } catch (error) {
    clearOauthMode()
  }

  return null
}

export function clearOauthMode() {
  localStorage.removeItem(OAUTH_MODE_KEY)
}
