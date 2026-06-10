<template>
  <section ref="historyPanelRef" class="history-panel">
    <div class="history-panel__header">
      <div>
        <h2 class="history-panel__title">最近阅读</h2>
        <p class="history-panel__meta">已记录 {{ historyStore.total }} 篇文章</p>
        <p class="history-panel__summary">回看阅读进度，快速续读最近浏览过的文章</p>
      </div>
    </div>

    <div v-if="loginRequired" class="history-panel__state">
      请先登录后查看最近阅读
      <v-btn
        class="history-panel__login-btn"
        color="primary"
        variant="text"
        size="small"
        @click="openLogin"
      >
        立即登录
      </v-btn>
    </div>

    <div v-else-if="historyStore.loading" class="history-panel__state">
      正在加载阅读记录...
    </div>

    <div v-else-if="historyStore.historyList.length === 0" class="history-panel__state">
      暂无最近阅读记录
    </div>

    <div v-else class="history-list">
      <article v-for="item in historyStore.historyList" :key="item.id" class="history-item">
        <router-link :to="`/articles/${item.articleId}`" class="history-item__cover">
          <img :src="item.articleCover" :alt="item.articleTitle" />
        </router-link>
        <div class="history-item__body">
          <router-link :to="`/articles/${item.articleId}`" class="history-item__title">
            {{ item.articleTitle }}
          </router-link>
          <p class="history-item__progress">阅读至 {{ item.progressPercent }}%</p>
          <p class="history-item__time">上次阅读于 {{ formatTime(item.lastReadTime) }}</p>
          <div class="history-item__actions">
            <v-btn :to="`/articles/${item.articleId}`" color="primary" variant="text" size="small">
              继续阅读
            </v-btn>
            <v-btn color="error" variant="text" size="small" @click="handleDelete(item.id)">
              删除记录
            </v-btn>
          </div>
        </div>
      </article>
    </div>

    <div v-if="historyStore.total > historyStore.size" class="history-panel__pagination">
      <v-pagination
        :model-value="historyStore.current"
        :length="Math.ceil(historyStore.total / historyStore.size)"
        :total-visible="6"
        rounded="circle"
        @update:model-value="handlePageChange"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useHistoryStore } from '@/stores/history'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { formatTime } from '@/utils/filters'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

const route = useRoute()
const historyStore = useHistoryStore()
const userStore = useUserStore()
const loginRequired = ref(false)
const historyPanelRef = ref<HTMLElement | null>(null)

function openLogin() {
  openLoginRequiredPrompt({
    message: '当前页面需要登录后才能访问',
    redirect: route.fullPath
  })
}

async function handleDelete(historyId: number) {
  try {
    const result = await historyStore.deleteHistory(historyId)
    if (result === 'success') {
      useToast({ type: 'success', message: '阅读记录已删除' })
      return
    }

    if (result === 'success_but_stale') {
      useToast({
        type: 'success',
        message: '删除已生效，列表刷新稍后再试'
      })
      return
    }

    if (result === 'login_required') {
      openLogin()
      return
    }

    useToast({ type: 'error', message: '删除阅读记录失败，请稍后重试' })
  } catch {
    useToast({ type: 'error', message: '删除阅读记录失败，请稍后重试' })
  }
}

async function handlePageChange(page: number) {
  try {
    loginRequired.value = false
    await historyStore.changePage(page)
    await nextTick()
    historyPanelRef.value?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    })
  } catch {
    if (!userStore.isLoggedIn) {
      loginRequired.value = true
      openLogin()
      return
    }

    useToast({ type: 'error', message: '加载阅读记录失败' })
  }
}

onMounted(async () => {
  try {
    const result = await historyStore.initialize()
    loginRequired.value = result === 'login_required'
    if (result === 'login_required') {
      openLogin()
    }
  } catch {
    useToast({ type: 'error', message: '加载阅读记录失败' })
  }
})
</script>

<style scoped>
.history-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.history-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.history-panel__title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.history-panel__meta {
  margin: 6px 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.history-panel__summary {
  margin: 6px 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.history-panel__state {
  padding: 36px 16px;
  border: 1px dashed var(--card-border-accent);
  border-radius: var(--card-radius-md);
  text-align: center;
  color: var(--text-secondary);
  background: var(--surface-raised);
}

.history-panel__login-btn {
  margin-left: 8px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.history-panel__pagination {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.history-panel__pagination :deep(.v-pagination__item),
.history-panel__pagination :deep(.v-pagination__prev),
.history-panel__pagination :deep(.v-pagination__next) {
  color: var(--text-primary);
}

.history-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-md);
  background: var(--card-surface);
}

.history-item__cover {
  display: block;
  flex-shrink: 0;
  width: 120px;
  height: 78px;
  overflow: hidden;
  border-radius: 14px;
}

.history-item__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.history-item__body {
  flex: 1;
  min-width: 0;
}

.history-item__title {
  color: var(--text-primary);
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
}

.history-item__progress {
  margin: 8px 0 0;
  font-size: 0.875rem;
  color: var(--primary-color);
}

.history-item__time {
  margin: 6px 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.history-item__actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

:global(.dark) .history-panel__state {
  background: var(--surface-raised);
  border-color: var(--surface-border-strong);
  color: rgba(214, 220, 235, 0.78);
}

@media (max-width: 759px) {
  .history-item {
    flex-direction: column;
  }

  .history-item__cover {
    width: 100%;
    height: 170px;
  }

  .history-item__actions {
    flex-wrap: wrap;
  }
}
</style>
