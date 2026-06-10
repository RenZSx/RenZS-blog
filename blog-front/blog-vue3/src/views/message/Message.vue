<template>
  <div class="message-page">
    <div class="message-banner" :style="cover">
      <div class="message-overlay" />

      <div class="message-hero">
        <div class="message-kicker">Message Flow</div>
        <h1 class="message-title animated fadeInDown">留言板</h1>
        <p class="message-subtitle animated fadeInUp">
          让整页空间都成为流动的留言现场，而不是静止的评论列表。
        </p>

        <div class="message-input-shell animated fadeInUp">
          <input
            v-model="messageContent"
            class="message-input"
            @focus="showInput = true"
            @keyup.enter="addMessage"
            placeholder="留下点什么吧！"
          />
          <button
            class="send-btn"
            :class="{ 'send-btn-active': showInput || messageContent.trim() }"
            @click="addMessage"
          >
            发射
          </button>
        </div>
      </div>

      <div class="barrage-stage">
        <div
          v-for="item in activeBarrageItems"
          :key="item.key"
          class="barrage-item"
          :style="getBarrageStyle(item)"
          @animationend="removeBarrageItem(item.key)"
        >
          <div class="barrage-pill">
            <v-avatar size="34" class="barrage-avatar">
              <v-img :src="item.avatar" />
            </v-avatar>
            <div class="barrage-copy">
              <div class="barrage-meta">
                <span class="barrage-nickname">{{ item.nickname }}</span>
                <span class="barrage-dot" />
                <span class="barrage-time">{{ item.timeLabel }}</span>
              </div>
              <div class="barrage-text">{{ item.messageContent }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && sourceMessages.length === 0" class="message-empty">
        还没有留言，发出第一条吧。
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useDisplay } from 'vuetify'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useUserStore } from '@/stores/user'
import { getMessages, sendMessage } from '@/api/misc'
import { useToast } from '@/composables/useToast'
import { formatHour } from '@/utils/filters'

interface MessageItem {
  avatar: string
  nickname: string
  messageContent: string
  createTime?: string
  time?: number
}

interface ActiveBarrageItem extends MessageItem {
  key: string
  track: number
  duration: number
  width: number
  top: number
  timeLabel: string
}

const { mobile } = useDisplay()
const blogInfoStore = useBlogInfoStore()
const userStore = useUserStore()

const showInput = ref(false)
const messageContent = ref('')
const loading = ref(false)
const sourceMessages = ref<MessageItem[]>([])
const activeBarrageItems = ref<ActiveBarrageItem[]>([])
const barrageSeed = ref(0)

const previousBodyOverflow = ref('')
const previousHtmlOverflow = ref('')
const trackTimers = new Map<number, ReturnType<typeof setTimeout>>()
const trackCursor = new Map<number, number>()
const isPageVisible = ref(true)

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const messagePage = pageList.find(item => item.pageLabel === 'message')
  const coverUrl = messagePage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

const viewportWidth = computed(() => window.innerWidth || 1440)
const viewportHeight = computed(() => window.innerHeight || 900)
const topTrackGap = computed(() => (mobile.value ? 86 : 92))
const navSafeArea = computed(() => (mobile.value ? 78 : 88) + topTrackGap.value)
const bottomTrackGap = computed(() => (mobile.value ? 86 : 92))
const bottomSafeArea = computed(() => (mobile.value ? 190 : 230) + bottomTrackGap.value)
const centerAvoidTop = computed(() => (mobile.value ? 292 : 300))
const centerAvoidBottom = computed(() => (mobile.value ? 452 : 444))
const trackCount = computed(() => (mobile.value ? 6 : 9))
const trackGap = computed(() => (mobile.value ? 86 : 82))
const spawnGap = computed(() => (mobile.value ? 54 : 72))

function clampTime(value?: number) {
  if (!value || Number.isNaN(Number(value))) return 10
  return Math.max(8, Math.min(Number(value), 22))
}

function estimateWidth(item: MessageItem) {
  const nicknameWidth = item.nickname.length * 13
  const contentWidth = Math.min(item.messageContent.length * 14, mobile.value ? 200 : 300)
  return Math.max(220, Math.min(nicknameWidth + contentWidth + 126, mobile.value ? 300 : 410))
}

function getTrackTops() {
  const tops: number[] = []
  const maxTop = viewportHeight.value - bottomSafeArea.value
  let currentTop = navSafeArea.value
  let trackIndex = 0

  while (currentTop <= maxTop && tops.length < trackCount.value) {
    const inAvoidBand = currentTop >= centerAvoidTop.value && currentTop <= centerAvoidBottom.value
    if (!inAvoidBand || trackIndex % 2 === 0) {
      tops.push(currentTop)
    }
    currentTop += trackGap.value
    trackIndex++
  }

  return tops
}

function getDuration(item: MessageItem) {
  return clampTime(item.time)
}

function getTrackDurationFactor(track: number) {
  const totalTracks = Math.max(trackCount.value - 1, 1)
  const normalized = track / totalTracks
  return 0.86 + normalized * 0.34
}

function getMessageForTrack(track: number) {
  if (!sourceMessages.value.length) return null

  const cursor = trackCursor.get(track) ?? track % sourceMessages.value.length
  const message = sourceMessages.value[cursor]
  trackCursor.set(track, (cursor + 1) % sourceMessages.value.length)
  return message
}

function createActiveBarrageItem(item: MessageItem, track: number, top: number): ActiveBarrageItem {
  const duration = getDuration(item) * getTrackDurationFactor(track)
  const width = estimateWidth(item)
  const seed = barrageSeed.value++

  return {
    ...item,
    key: `${item.nickname}-${seed}-${Date.now()}`,
    track,
    duration,
    width,
    top: top + (seed % 2) * 3,
    timeLabel: item.createTime ? formatHour(item.createTime) : '刚刚'
  }
}

function getNextDelay(item: ActiveBarrageItem) {
  const totalDistance = viewportWidth.value + item.width + 140
  const speed = totalDistance / item.duration
  const gapDistance = item.width + spawnGap.value
  return Math.max((gapDistance / speed) * 1000, mobile.value ? 2600 : 3000)
}

function scheduleTrack(track: number, top: number, initialDelay = 0) {
  if (!sourceMessages.value.length || !isPageVisible.value) return

  if (trackTimers.has(track)) {
    clearTimeout(trackTimers.get(track)!)
    trackTimers.delete(track)
  }

  const timer = setTimeout(() => {
    const message = getMessageForTrack(track)
    if (!message) return

    const barrageItem = createActiveBarrageItem(message, track, top)
    activeBarrageItems.value.push(barrageItem)

    scheduleTrack(track, top, getNextDelay(barrageItem))
  }, initialDelay)

  trackTimers.set(track, timer)
}

function startBarrage() {
  stopBarrage()
  activeBarrageItems.value = []
  barrageSeed.value = 0

  const tops = getTrackTops()
  tops.forEach((top, track) => {
    trackCursor.set(track, track % Math.max(sourceMessages.value.length, 1))
    scheduleTrack(track, top, track * (mobile.value ? 420 : 480))
  })
}

function stopBarrage() {
  trackTimers.forEach((timer) => clearTimeout(timer))
  trackTimers.clear()
}

function handleVisibilityChange() {
  isPageVisible.value = !document.hidden

  if (isPageVisible.value) {
    startBarrage()
  } else {
    stopBarrage()
    activeBarrageItems.value = []
  }
}

function removeBarrageItem(key: string) {
  activeBarrageItems.value = activeBarrageItems.value.filter(item => item.key !== key)
}

function getBarrageStyle(item: ActiveBarrageItem) {
  return {
    '--barrage-top': `${item.top}px`,
    '--barrage-width': `${item.width}px`,
    '--barrage-duration': `${item.duration}s`
  }
}

async function addMessage() {
  if (messageContent.value.trim() === '') {
    useToast({ type: 'error', message: '留言不能为空' })
    return
  }

  const userAvatar = userStore.avatar || blogInfoStore.blogInfo?.websiteConfig?.touristAvatar || ''
  const userNickname = userStore.nickname || '游客'
  const speed = Math.floor(Math.random() * 3) + 9

  const message: MessageItem = {
    avatar: userAvatar,
    nickname: userNickname,
    messageContent: messageContent.value.trim(),
    createTime: new Date().toISOString(),
    time: speed
  }

  try {
    const { data } = await sendMessage({ content: message.messageContent })
    if (data.flag) {
      sourceMessages.value.unshift(message)
      messageContent.value = ''
      useToast({ type: 'success', message: '留言成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    console.error('留言失败:', error)
  }
}

async function listMessages() {
  loading.value = true
  try {
    const { data } = await getMessages({ current: 1 })
    if (data.flag) {
      sourceMessages.value = (data.data?.recordList || data.data || []) as MessageItem[]
      startBarrage()
    }
  } catch (error) {
    console.error('获取留言列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  previousBodyOverflow.value = document.body.style.overflow
  previousHtmlOverflow.value = document.documentElement.style.overflow
  document.body.style.overflow = 'hidden'
  document.documentElement.style.overflow = 'hidden'
  document.addEventListener('visibilitychange', handleVisibilityChange)
  listMessages()
})

onBeforeUnmount(() => {
  stopBarrage()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.body.style.overflow = previousBodyOverflow.value
  document.documentElement.style.overflow = previousHtmlOverflow.value
})
</script>

<style scoped>
.message-page {
  position: fixed;
  inset: 0;
  overflow: hidden;
}

.message-banner {
  position: absolute;
  top: -60px;
  left: 0;
  right: 0;
  height: calc(100vh + 60px);
  overflow: hidden;
  background-color: #49b1f5;
}

.message-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at top, rgba(173, 215, 255, 0.24), transparent 28%),
    linear-gradient(180deg, rgba(5, 18, 34, 0.1), rgba(5, 18, 34, 0.24) 42%, rgba(5, 18, 34, 0.4) 100%);
}

.message-hero {
  position: relative;
  z-index: 4;
  width: min(560px, calc(100vw - 32px));
  margin: 0 auto;
  padding-top: min(30vh, 250px);
  text-align: center;
  color: #fff;
}

.message-kicker {
  color: rgba(233, 245, 255, 0.78);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.message-title {
  margin-top: 16px;
  color: #f8fbff;
  font-family: Georgia, 'Times New Roman', 'Noto Serif SC', serif;
  font-size: clamp(36px, 5vw, 56px);
  text-shadow: 0 12px 36px rgba(7, 16, 32, 0.28);
}

.message-subtitle {
  margin-top: 16px;
  color: rgba(238, 246, 255, 0.84);
  font-size: 15px;
  line-height: 1.9;
}

.message-input-shell {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 22px;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.26);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px);
  box-shadow: 0 22px 48px rgba(6, 18, 36, 0.2);
}

.message-input {
  flex: 1;
  min-width: 0;
  height: 52px;
  padding: 0 18px;
  border: none;
  background: transparent;
  color: #fff;
  font-size: 14px;
  outline: none;
}

.message-input::placeholder {
  color: rgba(255, 255, 255, 0.76);
}

.send-btn {
  height: 52px;
  padding: 0 22px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.88);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
}

.send-btn-active,
.send-btn:hover {
  background: linear-gradient(135deg, #fafdff, #d8ecff);
  color: #236ead;
  box-shadow: 0 10px 24px rgba(10, 30, 55, 0.18);
}

.barrage-stage {
  position: absolute;
  inset: 0;
  z-index: 2;
  overflow: hidden;
}

.barrage-item {
  position: absolute;
  top: var(--barrage-top);
  left: 100%;
  width: var(--barrage-width);
  animation: barrage-move linear var(--barrage-duration) forwards;
  will-change: transform;
}

.barrage-pill {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px 8px 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background:
    linear-gradient(180deg, rgba(12, 24, 44, 0.38), rgba(9, 20, 38, 0.28)),
    rgba(9, 20, 38, 0.24);
  color: #fff;
  box-shadow: 0 16px 34px rgba(5, 15, 31, 0.16);
  backdrop-filter: blur(12px) saturate(125%);
}

.barrage-avatar {
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.barrage-copy {
  min-width: 0;
}

.barrage-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.barrage-nickname {
  color: #f8fbff;
  font-size: 13px;
  font-weight: 700;
}

.barrage-dot {
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
}

.barrage-time {
  color: rgba(255, 255, 255, 0.66);
  font-size: 12px;
}

.barrage-text {
  overflow: hidden;
  color: rgba(248, 251, 255, 0.86);
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-empty {
  position: absolute;
  left: 50%;
  bottom: 72px;
  z-index: 4;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

@keyframes barrage-move {
  0% {
    transform: translate3d(0, 0, 0);
    opacity: 0;
  }

  8% {
    opacity: 1;
  }

  92% {
    opacity: 1;
  }

  100% {
    transform: translate3d(calc(-100vw - var(--barrage-width) - 140px), 0, 0);
    opacity: 0;
  }
}

@media (max-width: 759px) {
  .message-hero {
    width: min(92vw, 520px);
    padding-top: min(28vh, 210px);
  }

  .message-input-shell {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
    border-radius: 24px;
  }

  .message-input {
    width: 100%;
    padding: 0 14px;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
