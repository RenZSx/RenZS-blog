import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  getNoticeUnreadCount,
  getNotices,
  readAllNotices,
  readNotice
} from '@/api/notice'
import { useNoticePopupStore } from '@/stores/noticePopup'
import { useUserStore } from '@/stores/user'
import type { NoticeFilter, NoticeItem, NoticeListQuery, NoticeType } from '@/types/notice'
import { buildUnreadSummaryPopupPayload } from '@/utils/noticePopup'

const replyNoticeTypes = new Set(['comment_reply', 'talk_reply'])
const likeNoticeTypes = new Set(['article_like', 'talk_like'])
const systemNoticeTypes = new Set(['system'])
const NOTICE_UNREAD_SUMMARY_SESSION_KEY = 'notice-unread-summary-shown'

function buildUnreadSummarySessionKey(userId: number) {
  return `${NOTICE_UNREAD_SUMMARY_SESSION_KEY}:${userId}`
}

export const useNoticeStore = defineStore('notice', () => {
  const userStore = useUserStore()
  const noticePopupStore = useNoticePopupStore()
  const unreadCount = ref(0)
  const noticeList = ref<NoticeItem[]>([])
  const loading = ref(false)
  const initialized = ref(false)
  const realtimeReady = ref(false)
  const isNoticePanelActive = ref(false)
  const activeFilter = ref<NoticeFilter>('all')

  const filteredNoticeList = computed(() => {
    if (activeFilter.value === 'all') {
      return noticeList.value
    }

    const filterSet =
      activeFilter.value === 'reply'
        ? replyNoticeTypes
        : activeFilter.value === 'like'
          ? likeNoticeTypes
          : systemNoticeTypes

    return noticeList.value.filter((item) => filterSet.has(item.noticeType))
  })

  function setActiveFilter(filter: NoticeFilter) {
    activeFilter.value = filter
  }

  function setUnreadCount(count: number) {
    unreadCount.value = Number.isFinite(count) ? Math.max(0, Math.trunc(count)) : 0
  }

  function setRealtimeReady(ready: boolean) {
    realtimeReady.value = ready
  }

  function setNoticePanelActive(active: boolean) {
    isNoticePanelActive.value = active
  }

  function hasJumpTarget(notice: NoticeItem) {
    return typeof notice.jumpPath === 'string' && notice.jumpPath.trim().length > 0
  }

  async function fetchUnreadCount() {
    const { data } = await getNoticeUnreadCount()
    setUnreadCount(data?.data ?? 0)
  }

  function enqueueUnreadSummaryIfNeeded() {
    if (!userStore.isLoggedIn || userStore.userId === null || unreadCount.value <= 0) {
      return false
    }

    const sessionKey = buildUnreadSummarySessionKey(userStore.userId)
    if (sessionStorage.getItem(sessionKey) === '1') {
      return false
    }

    sessionStorage.setItem(sessionKey, '1')
    return noticePopupStore.enqueue(buildUnreadSummaryPopupPayload(unreadCount.value))
  }

  function resetUnreadSummarySessionFlag(userId?: number | null) {
    if (userId === null || userId === undefined) {
      return
    }

    sessionStorage.removeItem(buildUnreadSummarySessionKey(userId))
  }

  async function fetchNotices(params: NoticeListQuery = {}) {
    loading.value = true
    try {
      const { data } = await getNotices(params)
      const recordList = data?.data?.recordList
      noticeList.value = Array.isArray(recordList) ? recordList : []
    } finally {
      loading.value = false
    }
  }

  async function markNoticeRead(id: number, noticeType?: NoticeType) {
    const target = noticeList.value.find((item) =>
      item.id === id && (noticeType ? item.noticeType === noticeType : true)
    )
    const targetType = noticeType ?? target?.noticeType
    const { data } = await readNotice(id, targetType)
    if (!data?.flag) {
      return
    }

    if (target && target.isRead === 0) {
      target.isRead = 1
      setUnreadCount(unreadCount.value - 1)
    }
  }

  async function markAllRead() {
    const { data } = await readAllNotices()
    if (!data?.flag) {
      return
    }

    noticeList.value = noticeList.value.map((item) => ({
      ...item,
      isRead: 1
    }))
    setUnreadCount(0)
  }

  function receiveRealtimeNotice(notice: NoticeItem, nextUnreadCount: number) {
    setUnreadCount(nextUnreadCount)

    // 只有通知页当前处于激活态时才直接改列表，避免后台实时流把未展示页的数据源静默打乱。
    if (!isNoticePanelActive.value) {
      return
    }

    const nextNoticeList = noticeList.value.filter((item) => item.id !== notice.id)
    noticeList.value = [notice, ...nextNoticeList]
  }

  function resetState() {
    unreadCount.value = 0
    noticeList.value = []
    loading.value = false
    initialized.value = false
    realtimeReady.value = false
    isNoticePanelActive.value = false
    activeFilter.value = 'all'
  }

  async function initialize() {
    if (!userStore.isLoggedIn) {
      resetState()
      return 'login_required' as const
    }

    initialized.value = false
    await Promise.all([fetchNotices(), fetchUnreadCount()])
    initialized.value = true
    return 'ready' as const
  }

  return {
    unreadCount,
    noticeList,
    loading,
    initialized,
    realtimeReady,
    isNoticePanelActive,
    activeFilter,
    filteredNoticeList,
    initialize,
    fetchUnreadCount,
    enqueueUnreadSummaryIfNeeded,
    fetchNotices,
    setActiveFilter,
    setUnreadCount,
    setRealtimeReady,
    setNoticePanelActive,
    receiveRealtimeNotice,
    resetUnreadSummarySessionFlag,
    resetState,
    hasJumpTarget,
    markNoticeRead,
    markAllRead
  }
})
