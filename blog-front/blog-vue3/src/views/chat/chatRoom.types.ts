export interface ChatMessage {
  id?: number
  nickname: string
  avatar: string
  content: string
  createTime?: string
  userId?: number
  ipAddress?: string
  ipSource?: string
  type?: number
}

export type ChatRoomStatus = 'connecting' | 'connected' | 'error' | 'closed'

export interface RetractOwnershipContext {
  currentUserId?: number
  currentIpAddress?: string
}

export interface ChatStatusLabelInput {
  isEnabled: boolean
  status: ChatRoomStatus
}

export interface ChatWorkbenchStateInput {
  isEnabled: boolean
  isLoggedIn: boolean
  status: ChatRoomStatus
  isRetrying: boolean
  hasContent: boolean
}

export type ChatSendBlockedReason = 'disabled' | 'login_required' | 'connecting' | 'empty' | null

export interface ChatWorkbenchState {
  statusBadge: string
  statusClass: ChatRoomStatus | 'disabled'
  statusText: string
  conversationSubtitle: string
  composerPlaceholder: string
  composerBannerText: string
  sendGateDescription: string
  identityModeLabel: string
  identityHint: string
  isComposerEnabled: boolean
  canSend: boolean
  sendBlockedReason: ChatSendBlockedReason
  showRetryAction: boolean
  retryActionLabel: string
}
