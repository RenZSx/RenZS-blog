<template>
  <section ref="collectPanelRef" class="collect-panel">
    <div class="collect-panel__header">
      <div>
        <h2 class="collect-panel__title">我的收藏</h2>
        <p class="collect-panel__meta">已收藏 {{ collectStore.total }} 篇文章</p>
      </div>
    </div>

    <div v-if="loginRequired" class="collect-panel__state">
      请先登录后查看收藏
      <v-btn
        class="collect-panel__login-btn"
        color="primary"
        variant="text"
        size="small"
        @click="openLogin"
      >
        立即登录
      </v-btn>
    </div>

    <div v-else-if="collectStore.loading" class="collect-panel__state">
      正在加载收藏...
    </div>

    <div v-else-if="collectStore.collectList.length === 0" class="collect-panel__state">
      暂无收藏文章
    </div>

    <div v-else class="collect-list">
      <article v-for="item in collectStore.collectList" :key="item.id" class="collect-item">
        <router-link :to="`/articles/${item.articleId}`" class="collect-item__cover">
          <img :src="item.articleCover" :alt="item.articleTitle" />
        </router-link>
        <div class="collect-item__body">
          <router-link :to="`/articles/${item.articleId}`" class="collect-item__title">
            {{ item.articleTitle }}
          </router-link>
          <p class="collect-item__time">收藏于 {{ formatTime(item.createTime) }}</p>
          <div class="collect-item__actions">
            <v-btn :to="`/articles/${item.articleId}`" color="primary" variant="text" size="small">
              继续阅读
            </v-btn>
            <v-btn color="error" variant="text" size="small" @click="handleCancel(item.articleId)">
              取消收藏
            </v-btn>
          </div>
        </div>
      </article>
    </div>

    <div v-if="collectStore.total > collectStore.size" class="collect-panel__pagination">
      <v-pagination
        :model-value="collectStore.current"
        :length="Math.ceil(collectStore.total / collectStore.size)"
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
import { useCollectStore } from '@/stores/collect'
import { useToast } from '@/composables/useToast'
import { formatTime } from '@/utils/filters'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

const route = useRoute()
const collectStore = useCollectStore()
const loginRequired = ref(false)
const collectPanelRef = ref<HTMLElement | null>(null)

function openLogin() {
  openLoginRequiredPrompt({
    message: '当前页面需要登录后才能访问',
    redirect: route.fullPath
  })
}

async function handleCancel(articleId: number) {
  try {
    const result = await collectStore.cancelCollectArticle(articleId)
    if (result === 'success') {
      useToast({ type: 'success', message: '已取消收藏' })
      return
    }

    if (result === 'login_required') {
      openLogin()
      return
    }

    useToast({ type: 'error', message: '取消收藏失败，请稍后重试' })
  } catch {
    useToast({ type: 'error', message: '取消收藏失败，请稍后重试' })
  }
}

async function handlePageChange(page: number) {
  try {
    await collectStore.changePage(page)
    await nextTick()
    collectPanelRef.value?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    })
  } catch {
    useToast({ type: 'error', message: '加载收藏列表失败' })
  }
}

onMounted(async () => {
  try {
    const result = await collectStore.initialize()
    loginRequired.value = result === 'login_required'
    if (result === 'login_required') {
      openLogin()
    }
  } catch {
    useToast({ type: 'error', message: '加载收藏列表失败' })
  }
})
</script>

<style scoped>
.collect-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.collect-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.collect-panel__title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.collect-panel__meta {
  margin: 6px 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.collect-panel__state {
  padding: 36px 16px;
  border: 1px dashed var(--card-border-accent);
  border-radius: var(--card-radius-md);
  text-align: center;
  color: var(--text-secondary);
  background: var(--surface-raised);
}

.collect-panel__login-btn {
  margin-left: 8px;
}

.collect-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.collect-panel__pagination {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.collect-panel__pagination :deep(.v-pagination__item),
.collect-panel__pagination :deep(.v-pagination__prev),
.collect-panel__pagination :deep(.v-pagination__next) {
  color: var(--text-primary);
}

.collect-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-md);
  background: var(--card-surface);
}

.collect-item__cover {
  display: block;
  flex-shrink: 0;
  width: 120px;
  height: 78px;
  overflow: hidden;
  border-radius: 14px;
}

.collect-item__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.collect-item__body {
  flex: 1;
  min-width: 0;
}

.collect-item__title {
  color: var(--text-primary);
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
}

.collect-item__time {
  margin: 8px 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.collect-item__actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

:global(.dark) .collect-panel__state {
  background: var(--surface-raised);
  border-color: var(--surface-border-strong);
  color: rgba(214, 220, 235, 0.78);
}

@media (max-width: 759px) {
  .collect-item {
    flex-direction: column;
  }

  .collect-item__cover {
    width: 100%;
    height: 170px;
  }

  .collect-item__actions {
    flex-wrap: wrap;
  }
}
</style>
