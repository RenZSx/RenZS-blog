import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  cancelCollectArticle as cancelCollectArticleRequest,
  collectArticle as collectArticleRequest,
  getCollectArticles
} from '@/api/article'
import { useUserStore } from '@/stores/user'
import type { CollectArticleItem } from '@/types/collect'

const DEFAULT_PAGE_SIZE = 10

export const useCollectStore = defineStore('collect', () => {
  const userStore = useUserStore()
  const collectArticleIds = ref<number[]>([])
  const collectList = ref<CollectArticleItem[]>([])
  const current = ref(1)
  const size = ref(DEFAULT_PAGE_SIZE)
  const total = ref(0)
  const hasLoaded = ref(false)
  const loading = ref(false)

  function syncCollectIds(list: CollectArticleItem[]) {
    collectArticleIds.value = list.map((item) => item.articleId)
  }

  function isCollected(articleId: number) {
    return collectArticleIds.value.includes(articleId)
  }

  function reset() {
    collectArticleIds.value = []
    collectList.value = []
    current.value = 1
    size.value = DEFAULT_PAGE_SIZE
    total.value = 0
    hasLoaded.value = false
    loading.value = false
  }

  async function fetchCollectList(params: { current: number; size?: number } = { current: 1 }) {
    loading.value = true
    try {
      const { data } = await getCollectArticles(params)
      const recordList = data?.data?.recordList
      const count = data?.data?.count ?? 0
      current.value = params.current
      size.value = params.size ?? DEFAULT_PAGE_SIZE
      total.value = count
      collectList.value = Array.isArray(recordList) ? recordList : []
      syncCollectIds(collectList.value)
      hasLoaded.value = true
    } finally {
      loading.value = false
    }
  }

  async function initialize() {
    if (!userStore.isLoggedIn) {
      reset()
      return 'login_required' as const
    }

    await fetchCollectList({ current: 1, size: DEFAULT_PAGE_SIZE })
    return 'ready' as const
  }

  async function changePage(page: number) {
    await fetchCollectList({ current: page, size: size.value })
  }

  async function collectArticle(articleId: number) {
    if (!userStore.isLoggedIn) {
      return 'login_required' as const
    }

    const { data } = await collectArticleRequest(articleId)
    if (!data?.flag || collectArticleIds.value.includes(articleId)) {
      return 'noop' as const
    }
    collectArticleIds.value = [...collectArticleIds.value, articleId]
    return 'success' as const
  }

  async function cancelCollectArticle(articleId: number) {
    if (!userStore.isLoggedIn) {
      return 'login_required' as const
    }

    const { data } = await cancelCollectArticleRequest(articleId)
    if (!data?.flag) {
      return 'noop' as const
    }
    collectArticleIds.value = collectArticleIds.value.filter((id) => id !== articleId)
    const isCurrentPageEmptied =
      collectList.value.length === 1 && collectList.value[0]?.articleId === articleId
    const targetPage = isCurrentPageEmptied && current.value > 1 ? current.value - 1 : current.value

    await fetchCollectList({ current: targetPage, size: size.value })
    return 'success' as const
  }

  return {
    collectArticleIds,
    collectList,
    current,
    size,
    total,
    hasLoaded,
    loading,
    initialize,
    fetchCollectList,
    changePage,
    collectArticle,
    cancelCollectArticle,
    isCollected,
    reset
  }
})
