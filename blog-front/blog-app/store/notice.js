import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getNotices as apiGetNotices,
  getUnreadCount as apiGetUnreadCount,
  markNoticeRead as apiMarkRead,
  markAllRead as apiMarkAllRead
} from '@/api/notice'

// TabBar 第几项是通知(0:首页 1:分类 2:通知 3:我的)
const TABBAR_NOTICE_INDEX = 2

/**
 * 通知 store
 *
 * 设计要点:
 *   - `unreadCount` 是单一可信源,WS 推送 / HTTP 请求 / 标记已读都通过 setUnreadCount 进入
 *   - setUnreadCount 内部统一调 uni.setTabBarBadge / removeTabBarBadge,避免各页面散落同步逻辑
 *   - `panelActive` 由通知页 onShow/onHide 控制,决定 WS 新通知是否插入列表
 */
export const useNoticeStore = defineStore('notice', () => {
  // ============ State ============
  const unreadCount = ref(0)
  const noticeList = ref([])
  const loading = ref(false)
  const realtimeReady = ref(false)
  const panelActive = ref(false)
  const activeFilter = ref('all')  // 'all' | 'reply' | 'like' | 'system'
  const pagination = ref({ current: 1, hasMore: true })

  // ============ Getters ============
  const filteredNoticeList = computed(() => {
    if (activeFilter.value === 'all') return noticeList.value
    return noticeList.value.filter((n) => n.noticeType === activeFilter.value)
  })

  // ============ Internal:同步 TabBar 红点 ============
  function syncTabBarBadge(count) {
    try {
      if (count > 0) {
        uni.setTabBarBadge({
          index: TABBAR_NOTICE_INDEX,
          text: count > 99 ? '99+' : String(count)
        })
      } else {
        uni.removeTabBarBadge({ index: TABBAR_NOTICE_INDEX })
      }
    } catch (e) {
      // 某些场景(未启动 TabBar 时)会抛错,静默忽略
    }
  }

  // ============ Actions ============

  function setUnreadCount(n) {
    const next = Math.max(0, Number(n) || 0)
    unreadCount.value = next
    syncTabBarBadge(next)
  }

  function setRealtimeReady(b) {
    realtimeReady.value = !!b
  }

  function setPanelActive(b) {
    panelActive.value = !!b
  }

  function setActiveFilter(f) {
    if (['all', 'reply', 'like', 'system'].includes(f)) {
      activeFilter.value = f
    }
  }

  /**
   * 拉取未读数(启动时 / 后台返回时调用)
   */
  async function fetchUnreadCount() {
    try {
      const res = await apiGetUnreadCount()
      if (res.flag) {
        setUnreadCount(res.data || 0)
      }
    } catch (e) {
      // 拦截器已处理
    }
  }

  /**
   * 拉取通知列表
   * @param {Object} options { reset?: boolean }
   */
  async function fetchNotices({ reset = false } = {}) {
    if (loading.value) return
    if (!reset && !pagination.value.hasMore) return
    loading.value = true
    try {
      if (reset) {
        pagination.value.current = 1
        pagination.value.hasMore = true
      }
      const params = { current: pagination.value.current, size: 15 }
      // activeFilter !== 'all' 时附带 noticeType(若后端不支持,忽略即可不影响)
      if (activeFilter.value !== 'all') params.noticeType = activeFilter.value

      const res = await apiGetNotices(params)
      if (res.flag && res.data) {
        const list = res.data.records || res.data || []
        if (reset) {
          noticeList.value = list
        } else {
          noticeList.value = noticeList.value.concat(list)
        }
        if (list.length === 0) {
          pagination.value.hasMore = false
        } else {
          pagination.value.current += 1
        }
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * WS 新通知入口
   * - panelActive=true:插入列表顶部
   * - panelActive=false:仅更新红点 + toast
   * @param {Object} notice
   * @param {number} nextUnread
   */
  function receiveRealtimeNotice(notice, nextUnread) {
    setUnreadCount(nextUnread)
    if (panelActive.value && notice && notice.id) {
      // 避免重复插入
      if (!noticeList.value.find((n) => n.id === notice.id)) {
        noticeList.value.unshift(notice)
      }
    }
  }

  /**
   * 标记单条已读
   */
  async function markRead(notice) {
    if (!notice || notice.isRead === 1) return
    try {
      const res = await apiMarkRead(notice.id, notice.noticeType)
      if (res.flag) {
        notice.isRead = 1
        setUnreadCount(Math.max(0, unreadCount.value - 1))
      }
    } catch (e) {
      // 拦截器已处理
    }
  }

  /**
   * 全部已读
   */
  async function markAllAsRead() {
    try {
      const res = await apiMarkAllRead()
      if (res.flag) {
        noticeList.value.forEach((n) => { n.isRead = 1 })
        setUnreadCount(0)
        uni.showToast({ title: '已全部标记为已读', icon: 'success' })
      }
    } catch (e) {
      // 拦截器已处理
    }
  }

  /**
   * 切换过滤,重置列表
   */
  async function changeFilter(f) {
    setActiveFilter(f)
    pagination.value = { current: 1, hasMore: true }
    noticeList.value = []
    await fetchNotices({ reset: true })
  }

  /**
   * 登出/切账号时清理
   */
  function resetState() {
    unreadCount.value = 0
    noticeList.value = []
    realtimeReady.value = false
    panelActive.value = false
    activeFilter.value = 'all'
    pagination.value = { current: 1, hasMore: true }
    syncTabBarBadge(0)
  }

  return {
    // state
    unreadCount,
    noticeList,
    loading,
    realtimeReady,
    panelActive,
    activeFilter,
    pagination,
    // getters
    filteredNoticeList,
    // actions
    setUnreadCount,
    setRealtimeReady,
    setPanelActive,
    setActiveFilter,
    fetchUnreadCount,
    fetchNotices,
    receiveRealtimeNotice,
    markRead,
    markAllAsRead,
    changeFilter,
    resetState
  }
})
