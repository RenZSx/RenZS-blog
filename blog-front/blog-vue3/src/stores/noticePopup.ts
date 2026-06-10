import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { NoticePopupPayload } from '@/utils/noticePopup'

export const NOTICE_POPUP_DEFAULT_DURATION = 60000

function isSamePayload(a: NoticePopupPayload, b: NoticePopupPayload) {
  return a.id === b.id && a.noticeType === b.noticeType
}

function normalizeRemaining(value?: number) {
  if (!Number.isFinite(value)) {
    return NOTICE_POPUP_DEFAULT_DURATION
  }

  return Math.max(0, Math.trunc(value as number))
}

export const useNoticePopupStore = defineStore('noticePopup', () => {
  const queue = ref<NoticePopupPayload[]>([])
  const current = ref<NoticePopupPayload | null>(null)
  const visible = ref(false)
  const hovered = ref(false)
  const remaining = ref(NOTICE_POPUP_DEFAULT_DURATION)

  function enqueue(payload: NoticePopupPayload) {
    if (current.value && isSamePayload(current.value, payload)) {
      return false
    }

    if (queue.value.some((item) => isSamePayload(item, payload))) {
      return false
    }

    queue.value.push(payload)
    return true
  }

  function shiftNext() {
    const nextItem = queue.value.shift() ?? null
    current.value = nextItem
    visible.value = nextItem !== null
    hovered.value = false
    remaining.value = NOTICE_POPUP_DEFAULT_DURATION
    return nextItem
  }

  // 计时器由后续组件层接管，这里只维护暂停态和剩余时长。
  function pause(nextRemaining?: number) {
    hovered.value = true
    if (nextRemaining !== undefined) {
      remaining.value = normalizeRemaining(nextRemaining)
    }
  }

  function resume(nextRemaining?: number) {
    hovered.value = false
    if (nextRemaining !== undefined) {
      remaining.value = normalizeRemaining(nextRemaining)
    }
  }

  function closeCurrent() {
    visible.value = false
  }

  function finalizeCurrent() {
    current.value = null
    visible.value = false
    hovered.value = false
    remaining.value = NOTICE_POPUP_DEFAULT_DURATION
  }

  function clear() {
    queue.value = []
    current.value = null
    visible.value = false
    hovered.value = false
    remaining.value = NOTICE_POPUP_DEFAULT_DURATION
  }

  return {
    queue,
    current,
    visible,
    hovered,
    remaining,
    enqueue,
    shiftNext,
    pause,
    resume,
    closeCurrent,
    finalizeCurrent,
    clear
  }
})
