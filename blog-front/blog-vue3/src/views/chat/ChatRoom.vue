<template>
  <div v-if="shouldRenderChatPage" class="chat-page">
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">聊天室</h1>
    </div>

    <section class="chat-shell blog-container">
      <main class="chat-layout">
        <aside class="chat-sidebar">
          <section class="sidebar-card sidebar-card-hero">
            <div class="chat-room-kicker">Workbench</div>
            <h2 class="chat-room-title">公共会话</h2>
            <p class="chat-room-desc">{{ workbenchState.statusText }}</p>
            <div class="sidebar-status-row">
              <div class="chat-status-pill" :class="`chat-status-pill-${workbenchState.statusClass}`">
                {{ workbenchState.statusBadge }}
              </div>
              <div class="chat-online-card">
                <span class="chat-online-label">在线</span>
                <strong class="chat-online-value">{{ count }}</strong>
              </div>
            </div>
          </section>

          <section class="sidebar-card">
            <div class="sidebar-card-label">当前身份</div>
            <div class="chat-identity-main">
              <v-avatar size="42">
                <v-img :src="userStore.avatar || defaultAvatar" />
              </v-avatar>
              <div>
                <div class="chat-identity-name">{{ currentNickname }}</div>
                <div class="chat-identity-meta">{{ currentLocation }}</div>
              </div>
            </div>
            <div class="identity-lockup" :class="{ 'identity-lockup-muted': !isLoggedIn }">
              <span>{{ workbenchState.identityModeLabel }}</span>
              <span>{{ workbenchState.identityHint }}</span>
            </div>
          </section>

          <section class="sidebar-card">
            <div class="sidebar-card-label">使用提示</div>
            <div class="chat-identity-notes">
              <span class="chat-note-pill">Enter 发送</span>
              <span class="chat-note-pill">Shift + Enter 换行</span>
              <span class="chat-note-pill">支持表情消息</span>
              <span class="chat-note-pill">语音消息仅保留占位展示</span>
            </div>
          </section>

          <section class="sidebar-card sidebar-card-warning">
            <div class="sidebar-card-label">发言权限</div>
            <p class="sidebar-warning-text">
              {{ workbenchState.sendGateDescription }}
            </p>
          </section>
        </aside>

        <section class="chat-conversation-panel">
          <header class="conversation-header">
            <div>
              <div class="conversation-title">实时会话</div>
              <p class="conversation-subtitle">{{ workbenchState.conversationSubtitle }}</p>
            </div>
            <div class="conversation-header-side">
              <div v-if="workbenchState.showRetryAction" class="conversation-mini-card conversation-mini-card-action" @click="retryConnection">
                <span>状态</span>
                <strong>{{ workbenchState.retryActionLabel }}</strong>
              </div>
              <div class="conversation-mini-card">
                <span>模式</span>
                <strong>{{ workbenchState.identityModeLabel }}</strong>
              </div>
            </div>
          </header>

          <div
            class="chat-stream"
            ref="messagesRef"
            @click="closeTransientUi"
            @contextmenu.prevent="closeTransientUi"
          >
            <div v-if="chatRecordList.length === 0" class="empty-chat">
              <div class="empty-chat-icon">
                <v-icon color="#4f8cff" size="36">mdi-message-text-outline</v-icon>
              </div>
              <div class="empty-chat-title">聊天室刚刚开启</div>
              <div class="empty-chat-text">发出第一条消息，开启今天的交流。</div>
            </div>

            <div
              v-for="(item, index) in displayChatMessages"
              :key="item.id || index"
              class="message-row"
              :class="{
                'message-row-self': isSelf(item),
                'message-row-grouped': isGroupedWithPrevious(index)
              }"
            >
              <v-avatar
                v-if="!isGroupedWithPrevious(index)"
                size="40"
                class="message-avatar"
              >
                <v-img :src="item.avatar" />
              </v-avatar>
              <div v-else class="message-avatar-spacer" />

              <div class="message-bubble-wrap">
                <div
                  v-if="shouldShowMeta(index) || shouldShowTime(index)"
                  class="message-meta"
                  :class="{ 'message-meta-self': isSelf(item) }"
                >
                  <template v-if="shouldShowMeta(index)">
                    <span class="message-nickname">{{ item.nickname || '匿名用户' }}</span>
                    <span v-if="item.provinceLabel" class="message-region">{{ item.provinceLabel }}</span>
                  </template>
                  <span v-if="shouldShowTime(index)" class="message-time">{{ formatTime(item.createTime) }}</span>
                </div>

                <div
                  class="message-bubble"
                  :class="{ 'message-bubble-self': isSelf(item) }"
                  @contextmenu.prevent.stop="openContextMenu(item, $event)"
                >
                  <div v-if="item.type === 5" class="voice-placeholder">
                    <v-icon size="18">mdi-microphone</v-icon>
                    <span>语音消息暂未迁回</span>
                  </div>
                  <div v-else v-html="item.content" />
                </div>
              </div>
            </div>

            <div
              v-if="contextMenu.visible"
              class="chat-context-menu"
              :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
              @mouseleave="closeContextMenu"
            >
              <button type="button" class="chat-context-menu-item" @click="handleRetractAction">
                撤回消息
              </button>
            </div>
          </div>

          <div class="chat-composer">
            <transition name="emoji-panel">
              <div v-if="showEmojiPanel" class="chat-emoji-panel">
                <Emoji :chooseEmoji="true" @add-emoji="addEmoji" />
              </div>
            </transition>

            <div class="composer-login-banner" :class="{ 'composer-login-banner-muted': isLoggedIn }">
              {{ workbenchState.composerBannerText }}
            </div>

            <div class="chat-composer-row">
              <button
                type="button"
                class="composer-tool-btn"
                :class="{ 'composer-tool-btn-active': showEmojiPanel }"
                :disabled="!workbenchState.isComposerEnabled"
                @click="toggleEmojiPanel"
              >
                <v-icon size="22">mdi-emoticon-happy-outline</v-icon>
              </button>

              <v-textarea
                v-model="content"
                class="chat-input"
                :disabled="!workbenchState.isComposerEnabled"
                :placeholder="workbenchState.composerPlaceholder"
                rows="1"
                auto-grow
                max-rows="4"
                variant="outlined"
                hide-details
                @keydown.enter="handleKeydown"
              />

              <v-btn
                class="chat-send-btn"
                color="primary"
                :disabled="!workbenchState.canSend"
                @click="sendMessage"
              >
                发送
              </v-btn>
            </div>
          </div>
        </section>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import EmojiList from '@/assets/js/emoji'
import request from '@/api/request'
import Emoji from '@/components/Emoji.vue'
import { resolveChatSocketUrl } from '@/utils/websocket'
import type { ChatMessage, ChatRoomStatus } from './chatRoom.types'
import {
  canRetractMessage,
  formatChatProvince,
  getChatWorkbenchState,
  isMessageGroupedWithPrevious,
  shouldShowMessageTime
} from './chatRoomUtils'

const blogInfoStore = useBlogInfoStore()
const userStore = useUserStore()
const router = useRouter()

const content = ref('')
const count = ref(0)
const ipAddress = ref('')
const ipSource = ref('')
const chatRecordList = ref<ChatMessage[]>([])
const messagesRef = ref<HTMLElement | null>(null)
const websocket = ref<WebSocket | null>(null)
const websocketStatus = ref<ChatRoomStatus>('connecting')
const emojiMap: Record<string, string> = EmojiList
const showEmojiPanel = ref(false)
const contextMenu = ref<{
  visible: boolean
  x: number
  y: number
  message: ChatMessage | null
}>({
  visible: false,
  x: 0,
  y: 0,
  message: null
})
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let intentionallyClosed = false

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const chatPage = pageList.find(item => item.pageLabel === 'chat')
  const coverUrl = chatPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

const isChatRoomEnabled = computed(() => {
  return Number(blogInfoStore.blogInfo?.websiteConfig?.isChatRoom) === 1
})

const isBlogInfoLoaded = computed(() => blogInfoStore.loaded)

const shouldRenderChatPage = computed(() => isBlogInfoLoaded.value && isChatRoomEnabled.value)

const defaultAvatar = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig?.touristAvatar || ''
})

const currentNickname = computed(() => {
  return userStore.nickname || ipAddress.value || '匿名用户'
})

const currentLocation = computed(() => {
  return ipSource.value || ipAddress.value || '正在获取位置信息...'
})

const isLoggedIn = computed(() => {
  return !!userStore.userId
})

const isRetrying = ref(false)

const workbenchState = computed(() => {
  return getChatWorkbenchState({
    isEnabled: isChatRoomEnabled.value,
    isLoggedIn: isLoggedIn.value,
    status: websocketStatus.value,
    isRetrying: isRetrying.value,
    hasContent: content.value.trim().length > 0
  })
})

const displayChatMessages = computed(() => {
  return chatRecordList.value.map((item) => ({
    ...item,
    provinceLabel: formatChatProvince(item.ipSource)
  }))
})

function formatTime(time?: string) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function isSelf(item: ChatMessage) {
  if (userStore.userId && item.userId) {
    return String(userStore.userId) === String(item.userId)
  }
  return !item.userId && !!ipAddress.value && ipAddress.value === item.ipAddress
}

function isGroupedWithPrevious(index: number) {
  return isMessageGroupedWithPrevious(chatRecordList.value, index)
}

function shouldShowMeta(index: number) {
  return !isGroupedWithPrevious(index)
}

function shouldShowTime(index: number) {
  return shouldShowMessageTime(chatRecordList.value, index)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function fetchClientIp() {
  try {
    const { data } = await request.get('/api/ip')
    if (data?.flag && data.data) {
      ipAddress.value = data.data
    }
  } catch (error) {
    console.error('获取客户端 IP 失败:', error)
  }
}

async function fetchClientLocation() {
  try {
    const { data } = await request.get('/api/location')
    if (data?.flag && data.data) {
      ipSource.value = data.data
    }
  } catch (error) {
    console.error('获取客户端地理位置失败:', error)
  }
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function stopReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  isRetrying.value = false
}

function closeWebSocket() {
  stopHeartbeat()
  stopReconnect()
  const socket = websocket.value
  websocket.value = null
  if (socket) {
    intentionallyClosed = true
    socket.close()
  }
  websocketStatus.value = 'closed'
}

function scheduleReconnect() {
  if (!isChatRoomEnabled.value || reconnectTimer || isRetrying.value) return
  isRetrying.value = true
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connect()
  }, 2500)
}

function retryConnection() {
  isRetrying.value = false
  stopReconnect()
  connect()
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (websocket.value?.readyState === WebSocket.OPEN) {
      websocket.value.send(JSON.stringify({ type: 6, data: 'ping' }))
    }
  }, 30 * 1000)
}

function connect() {
  if (!isChatRoomEnabled.value) {
    closeWebSocket()
    return
  }

  if (
    websocket.value &&
    (websocket.value.readyState === WebSocket.OPEN || websocket.value.readyState === WebSocket.CONNECTING)
  ) {
    return
  }

  // 聊天室和通知模块共用同一套地址解析规则，本地开发时走 Vite 的 ws 代理。
  const wsUrl = resolveChatSocketUrl(blogInfoStore.blogInfo?.websiteConfig?.websocketUrl)
  if (!wsUrl) {
    websocketStatus.value = 'error'
    return
  }

  websocketStatus.value = 'connecting'
  try {
    intentionallyClosed = false
    websocket.value = new WebSocket(wsUrl)

    websocket.value.onerror = () => {
      websocketStatus.value = 'error'
      scheduleReconnect()
    }

    websocket.value.onopen = () => {
      websocketStatus.value = 'connected'
      isRetrying.value = false
      startHeartbeat()
    }

    websocket.value.onmessage = (event) => {
      const data = JSON.parse(event.data)
      switch (data.type) {
        case 1:
          count.value = data.data || 0
          break
        case 2:
          chatRecordList.value = data.data?.chatRecordList || []
          ipAddress.value = ipAddress.value || data.data?.ipAddress || ''
          ipSource.value = ipSource.value || data.data?.ipSource || ''
          scrollToBottom()
          break
        case 3:
        case 5:
          chatRecordList.value.push(data.data)
          scrollToBottom()
          break
        case 4: {
          const idx = chatRecordList.value.findIndex(m => m.id === data.data?.id)
          if (idx !== -1) {
            chatRecordList.value.splice(idx, 1)
          }
          break
        }
      }
    }

    websocket.value.onclose = () => {
      stopHeartbeat()
      websocketStatus.value = 'closed'
      if (intentionallyClosed) {
        intentionallyClosed = false
        return
      }
      scheduleReconnect()
    }
  } catch (error) {
    console.error('WebSocket 连接失败:', error)
    websocketStatus.value = 'error'
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.shiftKey) return
  event.preventDefault()
  sendMessage()
}

function toggleEmojiPanel() {
  if (!workbenchState.value.isComposerEnabled) return
  showEmojiPanel.value = !showEmojiPanel.value
  closeContextMenu()
}

function addEmoji(text: string) {
  content.value += text
}

function canRetract(item: ChatMessage) {
  return canRetractMessage(item, {
    currentUserId: userStore.userId ?? undefined,
    currentIpAddress: ipAddress.value
  })
}

function retractMessage(item: ChatMessage) {
  if (!websocket.value || websocket.value.readyState !== WebSocket.OPEN || !item.id) return

  websocket.value.send(
    JSON.stringify({
      type: 4,
      data: {
        id: item.id,
        isVoice: item.type === 5
      }
    })
  )
  closeContextMenu()
}

function closeContextMenu() {
  contextMenu.value.visible = false
  contextMenu.value.message = null
}

function openContextMenu(item: ChatMessage, event: MouseEvent) {
  if (!canRetract(item)) {
    closeContextMenu()
    return
  }

  const contentEl = event.currentTarget as HTMLElement | null
  if (!contentEl) return

  const rect = contentEl.getBoundingClientRect()
  const isMine = isSelf(item)
  const menuWidth = 116
  const menuHeight = 52
  let x = isMine ? rect.right - menuWidth : rect.left
  let y = rect.bottom + 10

  x = Math.max(8, Math.min(x, window.innerWidth - menuWidth - 8))
  y = Math.max(8, Math.min(y, window.innerHeight - menuHeight - 8))

  contextMenu.value = {
    visible: true,
    x,
    y,
    message: item
  }
}

function handleRetractAction() {
  if (contextMenu.value.message) {
    retractMessage(contextMenu.value.message)
  }
}

function closeTransientUi() {
  showEmojiPanel.value = false
  closeContextMenu()
}

function sendMessage() {
  if (!workbenchState.value.canSend) {
    const blockedMessageMap = {
      disabled: '聊天室已关闭',
      login_required: '请先登录再发送消息',
      connecting: '聊天室连接中，请稍后再试',
      empty: '内容不能为空'
    } as const
    const blockedReason = workbenchState.value.sendBlockedReason
    if (blockedReason) {
      useToast({ type: 'error', message: blockedMessageMap[blockedReason] })
    }
    return
  }
  if (!websocket.value || websocket.value.readyState !== WebSocket.OPEN) {
    useToast({ type: 'error', message: '聊天室连接中，请稍后再试' })
    return
  }

  const reg = /\[.+?\]/g
  const processedContent = content.value.replace(reg, (str) => {
    const emojiUrl = emojiMap[str]
    return emojiUrl
      ? `<img src='${emojiUrl}' width='24' height='24' style='margin: 0 1px;vertical-align: text-bottom'/>`
      : str
  })

  websocket.value.send(
    JSON.stringify({
      type: 3,
      data: {
        nickname: userStore.nickname || '匿名用户',
        avatar: userStore.avatar || defaultAvatar.value,
        content: processedContent,
        userId: userStore.userId,
        ipAddress: ipAddress.value,
        ipSource: ipSource.value,
        type: 3
      }
    })
  )

  content.value = ''
  showEmojiPanel.value = false
}

watch(
  [isBlogInfoLoaded, isChatRoomEnabled],
  ([loaded, enabled]) => {
    if (!loaded) {
      return
    }

    if (!enabled) {
      closeWebSocket()
      router.replace('/404')
      return
    }

    fetchClientIp()
    fetchClientLocation()
    connect()
  },
  { immediate: true }
)

onUnmounted(() => {
  closeWebSocket()
  closeContextMenu()
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 380px;
  color: #eee;
}

.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-title {
  position: absolute;
  bottom: 50px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.chat-shell {
  margin-top: 20px;
  margin-bottom: 48px;
  padding: 0 12px;
  position: relative;
  z-index: 1;
}

.chat-layout {
  display: grid;
  grid-template-columns: minmax(250px, 320px) minmax(0, 1fr);
  gap: 22px;
  align-items: stretch;
}

.chat-sidebar {
  display: grid;
  gap: 16px;
  align-content: start;
}

.sidebar-card {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(246, 249, 252, 0.92)),
    #fff;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
}

.sidebar-card-hero {
  background:
    radial-gradient(circle at top right, rgba(79, 140, 255, 0.18), transparent 42%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(243, 248, 255, 0.94));
}

.sidebar-card-warning {
  border-color: rgba(245, 158, 11, 0.24);
  background:
    linear-gradient(180deg, rgba(255, 251, 235, 0.96), rgba(255, 247, 237, 0.96)),
    #fff;
}

.sidebar-card-label {
  margin-bottom: 12px;
  color: #7b8795;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.chat-room-kicker {
  color: #6b8db8;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.chat-room-title {
  margin-top: 6px;
  color: #1a2635;
  font-size: 24px;
}

.chat-room-desc {
  margin-top: 8px;
  color: #738296;
  font-size: 14px;
}

.sidebar-status-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.chat-online-card {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 12px;
  background: rgba(79, 140, 255, 0.08);
  color: #3d6fa8;
}

.chat-online-label {
  font-size: 12px;
}

.chat-online-value {
  font-size: 18px;
}

.chat-conversation-panel {
  display: flex;
  flex-direction: column;
  min-height: 580px;
  padding: 22px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 248, 252, 0.94)),
    #fff;
  box-shadow: 0 24px 54px rgba(15, 23, 42, 0.1);
}

.conversation-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.conversation-title {
  color: #1a2635;
  font-size: 24px;
  font-weight: 700;
}

.conversation-subtitle {
  margin-top: 8px;
  color: #738296;
  font-size: 14px;
}

.conversation-header-side {
  display: flex;
  align-items: center;
  gap: 10px;
}

.conversation-mini-card {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  min-width: 96px;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(79, 140, 255, 0.08);
  color: #61748a;
  font-size: 12px;
}

.conversation-mini-card strong {
  color: #1d4ed8;
  font-size: 15px;
}

.conversation-mini-card-action {
  background: rgba(245, 158, 11, 0.12);
  color: #9a3412;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.2s ease;
}

.conversation-mini-card-action:hover {
  background: rgba(245, 158, 11, 0.2);
  transform: translateY(-1px);
}

.conversation-mini-card-action strong {
  color: #c2410c;
}

.chat-identity-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 15px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.92);
}

.chat-identity-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-identity-name {
  color: #1c2838;
  font-weight: 700;
}

.chat-identity-meta {
  color: #738296;
  font-size: 12px;
}

.identity-lockup {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  color: #536273;
  font-size: 13px;
}

.identity-lockup-muted {
  color: #b45309;
}

.chat-identity-notes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chat-note-pill {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(79, 140, 255, 0.08);
  color: #61748a;
  font-size: 12px;
}

.chat-emoji-panel {
  margin-bottom: 14px;
  padding: 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
}

.chat-status-pill {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.chat-status-pill-disabled {
  background: rgba(100, 116, 139, 0.14);
  color: #475569;
}

.chat-status-pill-connected {
  background: rgba(76, 175, 80, 0.14);
  color: #2e7d32;
}

.chat-status-pill-connecting {
  background: rgba(33, 150, 243, 0.14);
  color: #1565c0;
}

.chat-status-pill-error,
.chat-status-pill-closed {
  background: rgba(255, 82, 82, 0.14);
  color: #c62828;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 15px;
}

.empty-chat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: rgba(79, 140, 255, 0.1);
}

.empty-chat-title {
  color: #243548;
  font-weight: 700;
}

.empty-chat-text {
  color: #7a889a;
  font-size: 13px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.message-row-self {
  flex-direction: row-reverse;
}

.message-row-grouped {
  margin-top: -10px;
  margin-bottom: 10px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar-spacer {
  width: 40px;
  flex-shrink: 0;
}

.message-bubble-wrap {
  max-width: min(64%, 460px);
}

.message-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 4px;
  color: #78879a;
  font-size: 11px;
}

.message-meta-self {
  justify-content: flex-end;
}

.message-nickname {
  color: #243548;
  font-weight: 700;
}

.message-region {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(79, 140, 255, 0.12);
  color: #3d6fa8;
  font-size: 11px;
}

.message-time {
  color: #b8c2cf;
  font-size: 11px;
}

.message-bubble {
  display: inline-block;
  max-width: 100%;
  padding: 10px 14px;
  border-radius: 18px 18px 18px 8px;
  background: #fff;
  color: #243548;
  font-size: 14px;
  line-height: 1.65;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  word-break: break-word;
}

.message-bubble-self {
  border-radius: 18px 18px 8px 18px;
  background: linear-gradient(135deg, #4f8cff 0%, #2cb5ff 100%);
  color: #fff;
  box-shadow: 0 14px 30px rgba(44, 108, 214, 0.22);
}

.voice-placeholder {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.chat-context-menu {
  position: fixed;
  z-index: 20;
  min-width: 104px;
  padding: 6px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.18);
}

.chat-context-menu-item {
  width: 100%;
  padding: 9px 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #32445c;
  cursor: pointer;
  text-align: left;
}

.chat-context-menu-item:hover {
  background: rgba(79, 140, 255, 0.08);
}

.chat-stream {
  position: relative;
  flex: 1;
  min-height: 320px;
  overflow-y: auto;
  padding: 20px 20px 12px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(79, 140, 255, 0.08), transparent 32%),
    linear-gradient(180deg, #fbfdff, #f4f8fc);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.chat-composer {
  position: relative;
  margin-top: 16px;
  padding: 14px;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.95);
}

.composer-login-banner {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(245, 158, 11, 0.12);
  color: #9a3412;
  font-size: 13px;
}

.composer-login-banner-muted {
  background: rgba(79, 140, 255, 0.08);
  color: #3d6fa8;
}

.emoji-panel-enter-active,
.emoji-panel-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.emoji-panel-enter-from,
.emoji-panel-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.chat-composer-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-input {
  flex: 1;
}

.chat-composer :deep(.v-field) {
  border-radius: 16px !important;
  background: #fff;
}

.chat-composer :deep(.v-field__input) {
  min-height: 48px;
  padding-top: 12px;
  padding-bottom: 12px;
}

.composer-tool-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border: 1px solid rgba(79, 140, 255, 0.16);
  border-radius: 14px;
  background: rgba(79, 140, 255, 0.08);
  color: #3d6fa8;
  cursor: pointer;
}

.composer-tool-btn-active {
  background: rgba(79, 140, 255, 0.14);
}

.composer-tool-btn:disabled {
  cursor: not-allowed;
  opacity: 0.48;
}

.chat-send-btn {
  min-width: 96px;
  height: 48px !important;
  border-radius: 14px !important;
}

.sidebar-warning-text {
  color: #8a4b10;
  font-size: 13px;
  line-height: 1.7;
}

/* 黑夜模式适配 */
:global(.v-theme--dark) .sidebar-card {
  background: linear-gradient(180deg, rgba(30, 30, 30, 0.96), rgba(25, 25, 25, 0.92));
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.3);
}

:global(.v-theme--dark) .sidebar-card-hero {
  background: radial-gradient(circle at top right, rgba(79, 140, 255, 0.12), transparent 42%),
    linear-gradient(180deg, rgba(35, 35, 35, 0.98), rgba(28, 28, 28, 0.94));
}

:global(.v-theme--dark) .sidebar-card-warning {
  border-color: rgba(245, 158, 11, 0.2);
  background: linear-gradient(180deg, rgba(45, 35, 20, 0.96), rgba(40, 30, 15, 0.96));
}

:global(.v-theme--dark) .sidebar-card-label {
  color: #9ca3af;
}

:global(.v-theme--dark) .chat-room-kicker {
  color: #6b8db8;
}

:global(.v-theme--dark) .chat-room-title {
  color: #f3f4f6;
}

:global(.v-theme--dark) .chat-room-desc {
  color: #9ca3af;
}

:global(.v-theme--dark) .chat-online-card {
  background: rgba(79, 140, 255, 0.12);
  color: #7db0e8;
}

:global(.v-theme--dark) .chat-conversation-panel {
  background: linear-gradient(180deg, rgba(35, 35, 35, 0.98), rgba(28, 28, 28, 0.94));
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 24px 54px rgba(0, 0, 0, 0.3);
}

:global(.v-theme--dark) .conversation-title {
  color: #f3f4f6;
}

:global(.v-theme--dark) .conversation-subtitle {
  color: #9ca3af;
}

:global(.v-theme--dark) .conversation-mini-card {
  background: rgba(79, 140, 255, 0.12);
  color: #8da3b8;
}

:global(.v-theme--dark) .conversation-mini-card strong {
  color: #5c9be6;
}

:global(.v-theme--dark) .conversation-mini-card-action {
  background: rgba(245, 158, 11, 0.15);
  color: #e6a23c;
}

:global(.v-theme--dark) .conversation-mini-card-action:hover {
  background: rgba(245, 158, 11, 0.25);
}

:global(.v-theme--dark) .conversation-mini-card-action strong {
  color: #f5a623;
}

:global(.v-theme--dark) .chat-identity-strip {
  background: rgba(40, 40, 40, 0.92);
}

:global(.v-theme--dark) .chat-identity-name {
  color: #f3f4f6;
}

:global(.v-theme--dark) .chat-identity-meta {
  color: #9ca3af;
}

:global(.v-theme--dark) .identity-lockup {
  color: #b0b8c4;
}

:global(.v-theme--dark) .identity-lockup-muted {
  color: #e6a23c;
}

:global(.v-theme--dark) .chat-note-pill {
  background: rgba(79, 140, 255, 0.12);
  color: #8da3b8;
}

:global(.v-theme--dark) .chat-emoji-panel {
  background: rgba(45, 45, 45, 0.92);
}

:global(.v-theme--dark) .chat-status-pill-disabled {
  background: rgba(100, 116, 139, 0.2);
  color: #94a3b8;
}

:global(.v-theme--dark) .chat-status-pill-connected {
  background: rgba(76, 175, 80, 0.2);
  color: #6bcf6b;
}

:global(.v-theme--dark) .chat-status-pill-connecting {
  background: rgba(33, 150, 243, 0.2);
  color: #5cb8ff;
}

:global(.v-theme--dark) .chat-status-pill-error,
:global(.v-theme--dark) .chat-status-pill-closed {
  background: rgba(255, 82, 82, 0.2);
  color: #ff7a7a;
}

:global(.v-theme--dark) .empty-chat-icon {
  background: rgba(79, 140, 255, 0.15);
}

:global(.v-theme--dark) .empty-chat-title {
  color: #f3f4f6;
}

:global(.v-theme--dark) .empty-chat-text {
  color: #9ca3af;
}

:global(.v-theme--dark) .message-meta {
  color: #9ca3af;
}

:global(.v-theme--dark) .message-nickname {
  color: #d1d5db;
}

:global(.v-theme--dark) .message-region {
  background: rgba(79, 140, 255, 0.15);
  color: #7db0e8;
}

:global(.v-theme--dark) .message-time {
  color: #6b7280;
}

:global(.v-theme--dark) .message-bubble {
  background: #2d2d2d;
  color: #e5e7eb;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
}

:global(.v-theme--dark) .chat-context-menu {
  background: rgba(40, 40, 40, 0.96);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.35);
}

:global(.v-theme--dark) .chat-context-menu-item {
  color: #d1d5db;
}

:global(.v-theme--dark) .chat-context-menu-item:hover {
  background: rgba(79, 140, 255, 0.15);
}

:global(.v-theme--dark) .chat-stream {
  background: radial-gradient(circle at top left, rgba(79, 140, 255, 0.06), transparent 32%),
    linear-gradient(180deg, #252525, #1f1f1f);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

:global(.v-theme--dark) .chat-composer {
  background: rgba(35, 35, 35, 0.95);
}

:global(.v-theme--dark) .composer-login-banner {
  background: rgba(245, 158, 11, 0.15);
  color: #e6a23c;
}

:global(.v-theme--dark) .composer-login-banner-muted {
  background: rgba(79, 140, 255, 0.12);
  color: #7db0e8;
}

:global(.v-theme--dark) .chat-composer :deep(.v-field) {
  background: #2d2d2d;
}

:global(.v-theme--dark) .composer-tool-btn {
  background: rgba(79, 140, 255, 0.12);
  border-color: rgba(79, 140, 255, 0.2);
  color: #7db0e8;
}

:global(.v-theme--dark) .composer-tool-btn-active {
  background: rgba(79, 140, 255, 0.2);
}

:global(.v-theme--dark) .sidebar-warning-text {
  color: #e6a23c;
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .chat-shell {
    margin-top: 20px;
    margin-bottom: 28px;
    padding: 0 10px;
  }

  .chat-layout {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .chat-conversation-panel {
    min-height: auto;
    padding: 16px;
    border-radius: 22px;
  }

  .conversation-header {
    flex-direction: column;
  }

  .chat-stream {
    min-height: 360px;
    padding: 16px 14px 10px;
  }

  .message-bubble-wrap {
    max-width: 86%;
  }

  .message-avatar-spacer {
    width: 36px;
  }

  .chat-composer-row {
    align-items: stretch;
    flex-direction: column;
  }

  .composer-tool-btn,
  .chat-send-btn {
    width: 100%;
  }
}
</style>
