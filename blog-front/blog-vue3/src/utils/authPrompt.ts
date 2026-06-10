import {
  AUTH_PROMPT_DEFAULTS,
  normalizeAuthRedirect,
  useUIStore
} from '@/stores/ui'

export { normalizeAuthRedirect } from '@/stores/ui'

type AuthPromptOptions = {
  title?: string
  message?: string
  redirect?: string
  confirmText?: string
}

export function buildLoginLocation(redirect?: string) {
  return {
    path: '/auth',
    query: {
      redirect: normalizeAuthRedirect(redirect)
    }
  }
}

export function openLoginRequiredPrompt(options?: AuthPromptOptions) {
  const uiStore = useUIStore()

  uiStore.openAuthPrompt({
    title: options?.title ?? AUTH_PROMPT_DEFAULTS.title,
    message: options?.message ?? AUTH_PROMPT_DEFAULTS.message,
    redirect: normalizeAuthRedirect(options?.redirect),
    confirmText: options?.confirmText ?? AUTH_PROMPT_DEFAULTS.confirmText
  })
}
