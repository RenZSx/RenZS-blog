<template>
  <section class="notice-panel">
    <div class="notice-panel__header">
      <div>
        <h2 class="notice-panel__title">
          <span>我的通知</span>
          <span v-if="noticeStore.unreadCount > 0" class="notice-panel__badge">
            {{ noticeBadgeText }}
          </span>
        </h2>
        <p class="notice-panel__meta">未读 {{ noticeStore.unreadCount }} 条</p>
      </div>
      <v-btn
        color="primary"
        variant="text"
        size="small"
        :disabled="noticeStore.noticeList.length === 0 || noticeStore.unreadCount === 0"
        @click="handleMarkAllRead"
      >
        全部已读
      </v-btn>
    </div>

    <div class="notice-panel__filters">
      <button
        v-for="filter in filterTabs"
        :key="filter.value"
        type="button"
        class="notice-filter"
        :class="{ 'notice-filter--active': noticeStore.activeFilter === filter.value }"
        @click="handleFilterChange(filter.value)"
      >
        {{ filter.label }}
      </button>
    </div>

    <div v-if="loginRequired" class="notice-panel__state">
      请先登录后查看通知
      <v-btn
        class="notice-panel__login-btn"
        color="primary"
        variant="text"
        size="small"
        @click="openLogin"
      >
        立即登录
      </v-btn>
    </div>

    <div v-else-if="noticeStore.loading" class="notice-panel__state">
      正在加载通知...
    </div>

    <div v-else-if="noticeListWithPreview.length === 0" class="notice-panel__state">
      {{ emptyStateText }}
    </div>

    <div v-else class="notice-list">
      <article
        v-for="item in noticeListWithPreview"
        :key="`${item.noticeType}-${item.id}`"
        class="notice-item"
        :class="{ 'notice-item--read': item.isRead === 1 }"
      >
        <button
          type="button"
          class="notice-item__mark"
          :aria-label="`标记通知为已读：${item.content}`"
          @click="handleNoticeClick(item)"
        />
        <span v-if="item.isRead === 0" class="notice-item__dot" aria-hidden="true" />
        <div class="notice-item__body">
          <div class="notice-item__head">
            <span class="notice-item__type">{{ getNoticeTypeLabel(item.noticeType) }}</span>
            <time class="notice-item__time" :datetime="item.createTime">
              {{ formatTime(item.createTime) }}
            </time>
          </div>
          <p class="notice-item__content">{{ item.content }}</p>
          <div v-if="item.replyPreviewHtml" class="notice-item__reply-bubble">
            <p class="notice-item__reply-preview">
              <span class="notice-item__reply-label">回复摘要：</span>
              <span class="notice-item__reply-html" v-html="item.replyPreviewHtml" />
            </p>
          </div>
          <div class="notice-item__actions">
            <span class="notice-item__status">{{ item.isRead === 1 ? '已读' : '未读' }}</span>
            <v-btn
              v-if="noticeStore.hasJumpTarget(item)"
              size="small"
              variant="text"
              color="primary"
              @click.stop="handleJump(item)"
            >
              前往查看
            </v-btn>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNoticeStore } from '@/stores/notice'
import { useToast } from '@/composables/useToast'
import { formatTime } from '@/utils/filters'
import {
  getReplyPreview,
  type NoticeFilter,
  type NoticeItem,
  type NoticeType
} from '@/types/notice'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

const route = useRoute()
const noticeStore = useNoticeStore()
const router = useRouter()
const loginRequired = ref(false)

const filterTabs: Array<{ label: string; value: NoticeFilter }> = [
  { label: '全部', value: 'all' },
  { label: '回复', value: 'reply' },
  { label: '点赞', value: 'like' },
  { label: '系统', value: 'system' }
]

const noticeTypeLabelMap: Record<NoticeType, string> = {
  comment_reply: '文章回复',
  talk_reply: '说说回复',
  article_like: '文章点赞',
  talk_like: '说说点赞',
  system: '系统通知'
}

const emptyStateText = computed(() => {
  if (noticeStore.activeFilter === 'reply') {
    return '暂无回复通知'
  }

  if (noticeStore.activeFilter === 'like') {
    return '暂无点赞通知'
  }

  if (noticeStore.activeFilter === 'system') {
    return '暂无系统通知'
  }

  return '暂无通知'
})

const noticeListWithPreview = computed(() =>
  noticeStore.filteredNoticeList.map((item) => ({
    ...item,
    replyPreviewHtml: getReplyPreview(item)
  }))
)
// 面板标题上的角标只负责提示当前未读数量，不参与列表筛选逻辑。
const noticeBadgeText = computed(() => {
  const unreadCount = noticeStore.unreadCount
  return unreadCount > 99 ? '99+' : String(unreadCount)
})

function handleFilterChange(filter: NoticeFilter) {
  noticeStore.setActiveFilter(filter)
}

function getNoticeTypeLabel(type: NoticeType) {
  return noticeTypeLabelMap[type] ?? '通知'
}

async function handleNoticeClick(item: NoticeItem) {
  try {
    await noticeStore.markNoticeRead(item.id, item.noticeType)
  } catch {
    useToast({ type: 'error', message: '标记通知已读失败' })
  }
}

async function handleJump(item: NoticeItem) {
  try {
    await router.push({
      path: item.jumpPath ?? '',
      hash: item.anchorKey ? `#${item.anchorKey}` : ''
    })
  } catch {
    useToast({ type: 'error', message: '跳转失败，请稍后重试' })
  }
}

async function handleMarkAllRead() {
  try {
    await noticeStore.markAllRead()
  } catch {
    useToast({ type: 'error', message: '全部已读操作失败' })
  }
}

function openLogin() {
  openLoginRequiredPrompt({
    message: '当前页面需要登录后才能访问',
    redirect: route.fullPath
  })
}

onMounted(async () => {
  try {
    const result = await noticeStore.initialize()
    loginRequired.value = result === 'login_required'
    if (result === 'login_required') {
      openLogin()
    }
  } catch {
    useToast({ type: 'error', message: '加载通知失败，请稍后重试' })
  }
})
</script>

<style scoped>
.notice-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.notice-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.notice-panel__title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.notice-panel__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  box-shadow: 0 0 0 2px var(--surface-raised);
}

.notice-panel__meta {
  margin: 6px 0 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.notice-panel__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.notice-filter {
  padding: 8px 14px;
  border: 1px solid var(--card-border-accent);
  border-radius: 999px;
  background: var(--surface-raised);
  color: var(--text-secondary);
  font-size: 0.9rem;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    color 0.2s ease;
}

.notice-filter:hover,
.notice-filter--active {
  border-color: var(--primary-color);
  background: rgb(from var(--primary-color) r g b / 0.1);
  color: var(--primary-color);
}

.notice-panel__state {
  padding: 36px 16px;
  border: 1px dashed var(--card-border-accent);
  border-radius: var(--card-radius-md);
  text-align: center;
  color: var(--text-secondary);
  background: var(--surface-raised);
}

.notice-panel__login-btn {
  margin-left: 8px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 16px 18px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-md);
  background: var(--surface-raised);
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.notice-item:hover,
.notice-item:focus-within {
  border-color: var(--primary-color);
  box-shadow: var(--card-shadow-sm);
  transform: translateY(-1px);
}

.notice-item--read {
  opacity: 0.82;
}

.notice-item__mark {
  position: absolute;
  inset: 0;
  z-index: 1;
  border: 0;
  border-radius: inherit;
  background: transparent;
  cursor: pointer;
}

.notice-item__mark:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.notice-item__dot {
  position: relative;
  z-index: 2;
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--primary-color);
  box-shadow: 0 0 0 4px rgb(from var(--primary-color) r g b / 0.12);
}

.notice-item__body {
  position: relative;
  z-index: 2;
  flex: 1;
  min-width: 0;
}

.notice-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.notice-item__type {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--bg-hover);
  color: var(--text-secondary);
  font-size: 0.8125rem;
  font-weight: 600;
}

.notice-item__content {
  margin: 0;
  font-size: 0.98rem;
  line-height: 1.7;
  color: var(--text-primary);
  word-break: break-word;
}

.notice-item__time {
  font-size: 0.875rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.notice-item__reply-bubble {
  margin-top: 10px;
  padding: 10px 12px;
  border: 1px solid var(--card-border-accent);
  border-radius: 14px;
  background: rgb(from var(--primary-color) r g b / 0.05);
}

.notice-item__reply-preview {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--text-secondary);
  word-break: break-word;
}

.notice-item__reply-label {
  margin-right: 4px;
}

.notice-item__reply-html {
  display: inline;
}

.notice-item__reply-html :deep(img) {
  display: inline-block;
  vertical-align: text-bottom;
}

.notice-item__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  pointer-events: none;
}

.notice-item__status {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  pointer-events: auto;
}

:global(.dark) .notice-filter {
  background: var(--surface-sunken);
  color: var(--text-secondary);
}

:global(.dark) .notice-panel__state,
:global(.dark) .notice-item {
  background: var(--surface-raised);
  border-color: var(--surface-border-strong);
}

:global(.dark) .notice-item:hover,
:global(.dark) .notice-item:focus-within {
  border-color: rgba(116, 166, 230, 0.36);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 18px 38px rgba(0, 0, 0, 0.28);
}

:global(.dark) .notice-item__type {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(232, 238, 250, 0.82);
}

:global(.dark) .notice-item__content {
  color: rgba(244, 247, 255, 0.94);
}

:global(.dark) .notice-item__reply-preview {
  color: rgba(226, 232, 245, 0.82);
}

:global(.dark) .notice-item__reply-bubble {
  border-color: rgba(116, 166, 230, 0.28);
  background: rgba(116, 166, 230, 0.1);
}

:global(.dark) .notice-panel__badge {
  box-shadow: 0 0 0 2px var(--surface-raised);
}

:global(.dark) .notice-item__time,
:global(.dark) .notice-item__status,
:global(.dark) .notice-panel__state,
:global(.dark) .notice-panel__meta {
  color: rgba(214, 220, 235, 0.78);
}

.notice-item__actions :deep(.v-btn) {
  pointer-events: auto;
  position: relative;
  z-index: 3;
}

@media (max-width: 759px) {
  .notice-panel__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .notice-item {
    padding: 14px 16px;
  }

  .notice-item__head,
  .notice-item__actions {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
