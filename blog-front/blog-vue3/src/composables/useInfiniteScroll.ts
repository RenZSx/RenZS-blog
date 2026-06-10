import {
  computed,
  getCurrentInstance,
  onMounted,
  onUnmounted,
  ref,
  type Ref,
  unref
} from 'vue'

interface UseInfiniteScrollOptions {
  loading: Ref<boolean>
  hasMore: Ref<boolean>
  onLoadMore: () => Promise<void> | void
  threshold?: number
}

export function useInfiniteScroll(options: UseInfiniteScrollOptions) {
  const threshold = options.threshold ?? 120
  const pending = ref(false)

  const isLocked = computed(() => {
    return pending.value || unref(options.loading) || !unref(options.hasMore)
  })

  async function triggerLoad() {
    if (isLocked.value) return

    pending.value = true
    try {
      await options.onLoadMore()
    } finally {
      pending.value = false
    }
  }

  function handleScroll() {
    if (isLocked.value) return

    const scrollTop =
      document.documentElement.scrollTop || document.body.scrollTop || 0
    const clientHeight =
      document.documentElement.clientHeight || window.innerHeight || 0
    const scrollHeight = Math.max(
      document.documentElement.scrollHeight,
      document.body.scrollHeight
    )

    if (scrollTop + clientHeight >= scrollHeight - threshold) {
      void triggerLoad()
    }
  }

  if (getCurrentInstance()) {
    onMounted(() => {
      window.addEventListener('scroll', handleScroll, { passive: true })
      handleScroll()
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
    })
  }

  return {
    handleScroll,
    cleanup() {
      window.removeEventListener('scroll', handleScroll)
    }
  }
}
