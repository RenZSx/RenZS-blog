import type {
  ChatMessage,
  ChatStatusLabelInput,
  ChatWorkbenchState,
  ChatWorkbenchStateInput,
  RetractOwnershipContext
} from './chatRoom.types'

const GROUP_WINDOW_MS = 5 * 60 * 1000
const TIME_BREAK_MS = 2 * 60 * 1000

function getMessageTimestamp(message?: ChatMessage): number | null {
  if (!message?.createTime) {
    return null
  }

  const timestamp = new Date(message.createTime).getTime()
  return Number.isNaN(timestamp) ? null : timestamp
}

export function getMessageIdentity(message: ChatMessage): string {
  if (message.userId) {
    return `user:${message.userId}`
  }

  if (message.ipAddress) {
    return `ip:${message.ipAddress}`
  }

  return `guest:${message.nickname}|${message.avatar}`
}

export function isMessageGroupedWithPrevious(messages: ChatMessage[], index: number): boolean {
  if (index <= 0) {
    return false
  }

  const current = messages[index]
  const previous = messages[index - 1]
  if (!current || !previous) {
    return false
  }

  if (getMessageIdentity(current) !== getMessageIdentity(previous)) {
    return false
  }

  const currentTime = getMessageTimestamp(current)
  const previousTime = getMessageTimestamp(previous)
  if (currentTime === null || previousTime === null) {
    return false
  }

  return Math.abs(currentTime - previousTime) < GROUP_WINDOW_MS
}

export function shouldShowMessageTime(messages: ChatMessage[], index: number): boolean {
  if (index <= 0) {
    return true
  }

  const current = messages[index]
  const previous = messages[index - 1]
  if (!current || !previous) {
    return true
  }

  const currentTime = getMessageTimestamp(current)
  const previousTime = getMessageTimestamp(previous)
  if (currentTime === null || previousTime === null) {
    return true
  }

  return Math.abs(currentTime - previousTime) >= TIME_BREAK_MS
}

export function canRetractMessage(
  message: ChatMessage,
  context: RetractOwnershipContext
): boolean {
  if (context.currentUserId && message.userId) {
    return String(context.currentUserId) === String(message.userId)
  }

  return !message.userId && !!context.currentIpAddress && context.currentIpAddress === message.ipAddress
}

export function formatChatStatusLabel({ isEnabled, status }: ChatStatusLabelInput): string {
  if (!isEnabled) {
    return '已关闭'
  }

  switch (status) {
    case 'connected':
      return '已连接'
    case 'connecting':
      return '连接中'
    case 'error':
      return '连接异常'
    case 'closed':
    default:
      return '已断开'
  }
}

export function formatChatProvince(ipSource?: string): string {
  if (!ipSource) {
    return ''
  }

  const source = String(ipSource).trim()
  if (!source || source === '未知IP' || source === '未知ip') {
    return ''
  }

  const directCityList = [
    '北京市',
    '上海市',
    '天津市',
    '重庆市',
    '香港特别行政区',
    '澳门特别行政区'
  ]
  const directCity = directCityList.find(name => source.indexOf(name) === 0)
  if (directCity) {
    return directCity.replace('特别行政区', '').replace('市', '')
  }

  const provinceMatch = source.match(/^(.+?省)/)
  if (provinceMatch) {
    return provinceMatch[1].replace('省', '')
  }

  const autonomousMatch = source.match(/^(.+?自治区)/)
  if (autonomousMatch) {
    return autonomousMatch[1]
      .replace('壮族自治区', '')
      .replace('回族自治区', '')
      .replace('维吾尔自治区', '')
      .replace('自治区', '')
  }

  const cityMatch = source.match(/^(.+?市)/)
  if (cityMatch) {
    return cityMatch[1].replace('市', '')
  }

  return ''
}

export function getChatWorkbenchState({
  isEnabled,
  isLoggedIn,
  status,
  isRetrying,
  hasContent
}: ChatWorkbenchStateInput): ChatWorkbenchState {
  const isComposerEnabled = isEnabled && isLoggedIn
  const statusBadge =
    isEnabled && status === 'connecting' && isRetrying
      ? '重连中'
      : formatChatStatusLabel({ isEnabled, status })
  const statusClass = isEnabled ? status : 'disabled'
  const showRetryAction = isEnabled && status !== 'connected'
  const retryActionLabel = showRetryAction ? '立即重试' : ''
  const statusTextMap = {
    connecting: '聊天室连接中，请稍候',
    connected: '聊天室连接正常，可以开始实时交流',
    error: '聊天室连接异常，请刷新后重试',
    closed: '聊天室已断开连接'
  } as const
  const loggedOutConversationSubtitle =
    '当前为只读模式，登录后才能发言。'

  if (!isEnabled) {
    return {
      statusBadge,
      statusClass,
      statusText: '管理员已关闭聊天室功能',
      conversationSubtitle: '聊天室当前已关闭，仅展示历史记录。',
      composerPlaceholder: '聊天室已关闭',
      composerBannerText: '聊天室已关闭，当前不可发送消息。',
      sendGateDescription: '聊天室已被管理员关闭，当前仅保留页面展示。',
      identityModeLabel: isLoggedIn ? '已登录' : '只读',
      identityHint: isLoggedIn ? '聊天室已关闭' : '登录后才能发言',
      isComposerEnabled,
      canSend: false,
      sendBlockedReason: 'disabled',
      showRetryAction: false,
      retryActionLabel: ''
    }
  }

  if (!isLoggedIn) {
    return {
      statusBadge,
      statusClass,
      statusText: statusTextMap[status],
      conversationSubtitle: loggedOutConversationSubtitle,
      composerPlaceholder: '登录后才能发言',
      composerBannerText: '登录后才能发言，当前仅可浏览会话内容。',
      sendGateDescription: '未登录用户可以浏览全部消息，但发送入口会保持禁用。',
      identityModeLabel: '只读',
      identityHint: '登录后才能发言',
      isComposerEnabled,
      canSend: false,
      sendBlockedReason: 'login_required',
      showRetryAction,
      retryActionLabel
    }
  }

  if (status !== 'connected') {
    const conversationSubtitleMap = {
      connecting: isRetrying ? '正在重新接入实时通道，请稍候。' : '正在接入实时通道，请稍候。',
      error: '连接异常，系统会自动重试，你也可以手动重试。',
      closed: '连接已断开，系统会自动重试，你也可以手动重试。'
    } as const
    const statusText =
      status === 'connecting' && isRetrying
        ? '聊天室重连中，正在恢复实时通道'
        : statusTextMap[status]
    const composerBannerText = isRetrying
      ? '正在尝试恢复实时连接，请稍候再发送。'
      : '实时连接尚未就绪，请稍候再发送。'

    return {
      statusBadge,
      statusClass,
      statusText,
      conversationSubtitle: conversationSubtitleMap[status],
      composerPlaceholder: '连接建立后可发送消息',
      composerBannerText,
      sendGateDescription: '你已登录，但需要等待连接恢复后才能发送消息。',
      identityModeLabel: '已登录',
      identityHint: '右击自己的消息可撤回',
      isComposerEnabled,
      canSend: false,
      sendBlockedReason: 'connecting',
      showRetryAction,
      retryActionLabel
    }
  }

  return {
    statusBadge,
    statusClass,
    statusText: '聊天室连接正常，可以开始实时交流',
    conversationSubtitle: '连接稳定，可以直接参与实时交流。',
    composerPlaceholder: '发送一条消息...',
    composerBannerText: '已连接聊天室，右击自己的消息可撤回。',
    sendGateDescription: '你已具备发言权限，可以参与公共会话。',
    identityModeLabel: '已登录',
    identityHint: '右击自己的消息可撤回',
    isComposerEnabled,
    canSend: hasContent,
    sendBlockedReason: hasContent ? null : 'empty',
    showRetryAction: false,
    retryActionLabel: ''
  }
}
