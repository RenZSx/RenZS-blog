export type LetterStatus = 'idle' | 'loading' | 'ready' | 'empty' | 'error'

export interface LetterPayload {
  letterTitle?: string
  letterContent?: string
}

export interface LetterContent {
  letterTitle: string
  letterContent: string
}

export interface LetterPageState {
  status: LetterStatus
  letter: LetterContent | null
  errorMessage: string
}

export type LetterPageEvent =
  | { type: 'load' }
  | { type: 'resolve'; payload?: LetterPayload | null }
  | { type: 'reject'; message?: string }

const DEFAULT_ERROR_MESSAGE = '信件暂时无法送达，请稍后再试'

export function createInitialLetterState(): LetterPageState {
  return {
    status: 'idle',
    letter: null,
    errorMessage: ''
  }
}

function normalizeLetterPayload(payload?: LetterPayload | null): LetterContent {
  return {
    letterTitle: payload?.letterTitle?.trim() || '',
    letterContent: payload?.letterContent?.trim() || ''
  }
}

function hasReadableLetter(letter: LetterContent): boolean {
  return Boolean(letter.letterTitle || letter.letterContent)
}

export function reduceLetterState(
  state: LetterPageState,
  event: LetterPageEvent
): LetterPageState {
  switch (event.type) {
    case 'load':
      return {
        status: 'loading',
        // 重新加载时保留旧内容，避免路由重复进入或刷新请求造成页面闪烁。
        letter: state.letter,
        errorMessage: ''
      }
    case 'resolve': {
      const letter = normalizeLetterPayload(event.payload)
      return {
        status: hasReadableLetter(letter) ? 'ready' : 'empty',
        letter,
        errorMessage: ''
      }
    }
    case 'reject':
      return {
        status: 'error',
        letter: state.letter,
        errorMessage: event.message?.trim() || DEFAULT_ERROR_MESSAGE
      }
    default:
      return state
  }
}
