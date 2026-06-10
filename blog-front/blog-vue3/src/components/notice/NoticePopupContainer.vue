<template>
  <Teleport to="body">
    <div class="notice-popup-container" aria-live="polite" aria-atomic="true">
      <Transition name="notice-popup" @after-leave="handleAfterLeave">
        <NoticePopupCard
          v-if="noticePopupStore.current && noticePopupStore.visible"
          :payload="noticePopupStore.current"
          :progress-width="progressWidth"
          :progress-duration="progressDuration"
          @pause="handlePause"
          @resume="handleResume"
          @open="handleOpen"
          @close="handleClose"
        />
      </Transition>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import NoticePopupCard from '@/components/notice/NoticePopupCard.vue'
import { NOTICE_POPUP_DEFAULT_DURATION, useNoticePopupStore } from '@/stores/noticePopup'

const router = useRouter()
const noticePopupStore = useNoticePopupStore()

let dismissTimer: number | null = null
let dismissTimerStartedAt = 0
const progressWidth = ref('100%')
const progressDuration = ref(0)

function getRemainingPercent(remaining: number) {
  const normalizedRemaining = Math.max(0, Math.min(NOTICE_POPUP_DEFAULT_DURATION, remaining))
  return `${(normalizedRemaining / NOTICE_POPUP_DEFAULT_DURATION) * 100}%`
}

async function startProgressAnimation(remaining: number) {
  progressDuration.value = 0
  progressWidth.value = getRemainingPercent(remaining)
  await nextTick()
  progressDuration.value = Math.max(0, remaining)
  progressWidth.value = '0%'
}

function clearDismissTimer() {
  if (dismissTimer !== null) {
    window.clearTimeout(dismissTimer)
    dismissTimer = null
  }
  dismissTimerStartedAt = 0
}

function startDismissTimer(duration = NOTICE_POPUP_DEFAULT_DURATION) {
  clearDismissTimer()
  if (!noticePopupStore.current || !noticePopupStore.visible) {
    return
  }

  const nextDuration = Math.max(0, duration)
  dismissTimerStartedAt = Date.now()
  dismissTimer = window.setTimeout(() => {
    dismissTimer = null
    dismissTimerStartedAt = 0
    noticePopupStore.pause(0)
    noticePopupStore.closeCurrent()
  }, nextDuration)
}

function handlePause() {
  const elapsed =
    dismissTimerStartedAt > 0 ? Math.max(0, Date.now() - dismissTimerStartedAt) : 0
  const nextRemaining = Math.max(0, noticePopupStore.remaining - elapsed)
  clearDismissTimer()
  noticePopupStore.pause(nextRemaining)
  progressDuration.value = 0
  progressWidth.value = getRemainingPercent(nextRemaining)
}

async function handleResume() {
  if (!noticePopupStore.current || !noticePopupStore.visible) {
    return
  }

  noticePopupStore.resume(noticePopupStore.remaining)
  await startProgressAnimation(noticePopupStore.remaining)
}

async function handleOpen() {
  const current = noticePopupStore.current
  if (!current) {
    return
  }

  clearDismissTimer()

  try {
    await router.push(current.route)
  } catch (error) {
    console.error('通知弹窗跳转失败:', error)
  } finally {
    noticePopupStore.closeCurrent()
  }
}

function handleClose() {
  clearDismissTimer()
  noticePopupStore.closeCurrent()
}

function handleAfterLeave() {
  clearDismissTimer()
  progressDuration.value = 0
  progressWidth.value = '100%'
  noticePopupStore.finalizeCurrent()
}

watch(
  () => [noticePopupStore.current, noticePopupStore.queue.length] as const,
  ([current, queueLength]) => {
    if (!current && queueLength > 0) {
      noticePopupStore.shiftNext()
    }
  },
  { immediate: true }
)

watch(
  () => [noticePopupStore.current, noticePopupStore.visible, noticePopupStore.hovered] as const,
  async ([current, visible, hovered]) => {
    if (!current || !visible || hovered) {
      clearDismissTimer()
      return
    }

    await startProgressAnimation(noticePopupStore.remaining)
    startDismissTimer(noticePopupStore.remaining)
  },
  { immediate: true }
)

onUnmounted(() => {
  clearDismissTimer()
})
</script>

<style scoped>
.notice-popup-container {
  position: fixed;
  top: 104px;
  right: 24px;
  z-index: 2600;
  pointer-events: none;
}

.notice-popup-enter-active,
.notice-popup-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.notice-popup-enter-from,
.notice-popup-leave-to {
  opacity: 0;
  transform: translate3d(18px, -12px, 0) scale(0.98);
}

@media (max-width: 1100px) {
  .notice-popup-container {
    top: 88px;
    right: 18px;
  }
}

@media (max-width: 759px) {
  .notice-popup-container {
    top: 82px;
    right: 12px;
    left: 12px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
