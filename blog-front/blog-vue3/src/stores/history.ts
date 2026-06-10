import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  deleteArticleHistory as deleteArticleHistoryRequest,
  getArticleHistoryList,
  saveArticleHistory as saveArticleHistoryRequest
} from '@/api/history'
import { useUserStore } from '@/stores/user'
import type { HistoryItem } from '@/types/history'

const DEFAULT_PAGE_SIZE = 10

export const useHistoryStore = defineStore('history', () => {
  const userStore = useUserStore()
  const historyList = ref<HistoryItem[]>([])
  const current = ref(1)
  const size = ref(DEFAULT_PAGE_SIZE)
  const total = ref(0)
  const loading = ref(false)

  function reset() {
    historyList.value = []
    current.value = 1
    size.value = DEFAULT_PAGE_SIZE
    total.value = 0
    loading.value = false
  }

  async function fetchHistoryList(params: { current: number; size?: number } = { current: 1 }) {
    loading.value = true
    try {
      const { data } = await getArticleHistoryList(params)
      const recordList = data?.data?.recordList
      const count = data?.data?.count ?? 0
      current.value = params.current
      if (params.size !== undefined) {
        size.value = params.size
      }
      total.value = count
      historyList.value = Array.isArray(recordList) ? recordList : []
    } finally {
      loading.value = false
    }
  }

  async function initialize() {
    if (!userStore.isLoggedIn) {
      reset()
      return 'login_required' as const
    }

    await fetchHistoryList({ current: 1, size: DEFAULT_PAGE_SIZE })
    return 'ready' as const
  }

  async function changePage(page: number) {
    await fetchHistoryList({ current: page, size: size.value })
  }

  async function reportHistoryProgress(articleId: number, progressPercent: number) {
    if (!userStore.isLoggedIn) {
      return 'login_required' as const
    }

    const { data } = await saveArticleHistoryRequest(articleId, progressPercent)
    if (!data?.flag) {
      return 'noop' as const
    }
    return 'success' as const
  }

  async function deleteHistory(historyId: number) {
    if (!userStore.isLoggedIn) {
      return 'login_required' as const
    }

    const { data } = await deleteArticleHistoryRequest(historyId)
    if (!data?.flag) {
      return 'noop' as const
    }
    const isCurrentPageEmptied =
      historyList.value.length === 1 && historyList.value[0]?.id === historyId
    const targetPage = isCurrentPageEmptied && current.value > 1 ? current.value - 1 : current.value

    try {
      await fetchHistoryList({ current: targetPage, size: size.value })
      return 'success' as const
    } catch {
      return 'success_but_stale' as const
    }
  }

  return {
    historyList,
    current,
    size,
    total,
    loading,
    initialize,
    fetchHistoryList,
    changePage,
    reportHistoryProgress,
    deleteHistory,
    reset
  }
})
