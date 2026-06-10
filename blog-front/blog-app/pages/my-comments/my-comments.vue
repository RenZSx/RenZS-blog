<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="page-header">
      <text class="header-title">我的评论</text>
      <text class="header-sub">{{ totalCount }} 条评论</text>
    </view>

    <scroll-view scroll-y class="list" @scrolltolower="onReachBottom">
      <view v-if="loading && comments.length === 0" class="loading-tip">加载中...</view>

      <view v-else-if="comments.length === 0" class="empty-state">
        <bx-icon name="comment" :size="120" color="#c0c4cc" />
        <text class="empty-title">还没有评论过</text>
        <text class="empty-sub">读到精彩内容,留下你的想法吧</text>
      </view>

      <view
        v-for="(c, idx) in comments"
        :key="c.id"
        class="comment-card fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 40, 200)}ms` }"
        @click="goSource(c)"
      >
        <!-- 类型徽章 + 主题标题 -->
        <view class="topic-row">
          <text class="type-badge" :class="typeClass(c.type)">{{ typeLabel(c.type) }}</text>
          <text class="topic-title text-ellipsis">{{ c.topicTitle || '未知主题' }}</text>
          <bx-icon name="chevronRight" :size="28" color="#c0c4cc" />
        </view>

        <!-- 评论内容 -->
        <view class="content-wrap">
          <mp-html :content="c.commentContent || ''" :tag-style="commentContentStyle" />
        </view>

        <!-- 底部状态 + 时间 -->
        <view class="meta-row">
          <text v-if="c.isReview !== 1" class="review-badge">审核中</text>
          <text v-else-if="c.parentId" class="reply-badge">回复</text>
          <text class="time">{{ formatTime(c.createTime) }}</text>
        </view>
      </view>

      <view v-if="loading && comments.length > 0" class="loading-tip">加载中...</view>
      <view v-else-if="!hasMore && comments.length > 0" class="loading-tip end-tip">— 全部加载完毕 —</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyComments } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const comments = ref([])
const current = ref(1)
const loading = ref(false)
const hasMore = ref(true)
const totalCount = ref(0)

const commentContentStyle = {
  p: 'margin:4rpx 0;font-size:28rpx;line-height:1.7;color:var(--text-primary);',
  a: 'color:#42b983;',
  img: 'max-width:100%;border-radius:8rpx;'
}

function typeLabel(t) {
  if (t === 1) return '文章'
  if (t === 2) return '友链'
  if (t === 3) return '说说'
  if (t === 4) return '留言'
  return '其他'
}

function typeClass(t) {
  if (t === 1) return 'type-article'
  if (t === 2) return 'type-link'
  if (t === 3) return 'type-talk'
  if (t === 4) return 'type-message'
  return 'type-other'
}

async function load(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) { current.value = 1; hasMore.value = true }
    const res = await getMyComments(current.value, 10)
    if (res.flag && res.data) {
      const list = res.data.recordList || res.data.records || []
      totalCount.value = res.data.count || 0
      comments.value = reset ? list : comments.value.concat(list)
      if (list.length === 0) hasMore.value = false
      else current.value += 1
    }
  } finally {
    loading.value = false
  }
}

function onReachBottom() {
  load(false)
}

function goSource(c) {
  if (!c.type || !c.topicId) return
  if (c.type === 1) {
    uni.navigateTo({ url: `/pages/article/article?id=${c.topicId}` })
  } else if (c.type === 3) {
    uni.navigateTo({ url: `/pages/talk/talk-detail?id=${c.topicId}` })
  } else if (c.type === 2) {
    uni.navigateTo({ url: '/pages/link/link' })
  } else if (c.type === 4) {
    uni.navigateTo({ url: '/pages/message/message' })
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onShow(() => {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  load(true)
})
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-page);
}

.page-header {
  padding: 24rpx 32rpx 16rpx;
  background: var(--bg-card);
  border-bottom: 2rpx solid var(--border-color);
}

.header-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4rpx;
  letter-spacing: -0.5rpx;
}

.header-sub { font-size: 22rpx; color: var(--text-secondary); }

.list {
  flex: 1;
  overflow-y: auto;
  padding: 16rpx 24rpx 32rpx;
}

.loading-tip {
  text-align: center;
  color: var(--text-secondary);
  padding: 32rpx 0;
  font-size: 22rpx;
}

.end-tip { letter-spacing: 4rpx; opacity: 0.6; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
  color: var(--text-secondary);
}

.empty-title { margin-top: 24rpx; font-size: 28rpx; color: var(--text-regular); }
.empty-sub { margin-top: 8rpx; font-size: 22rpx; opacity: 0.7; }

.comment-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow-sm);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.comment-card:active { transform: scale(0.98); }

.topic-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding-bottom: 16rpx;
  margin-bottom: 16rpx;
  border-bottom: 2rpx solid var(--border-color-light);
}

.type-badge {
  padding: 4rpx 14rpx;
  font-size: 20rpx;
  border-radius: 12rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.type-article { background: rgba(66, 185, 131, 0.12); color: #339268; }
.type-link    { background: rgba(94, 114, 228, 0.12); color: #5e72e4; }
.type-talk    { background: rgba(230, 162, 60, 0.12); color: #e6a23c; }
.type-message { background: rgba(245, 108, 108, 0.12); color: #f56c6c; }
.type-other   { background: rgba(144, 147, 153, 0.12); color: #909399; }

.topic-title {
  flex: 1;
  font-size: 24rpx;
  color: var(--text-regular);
  font-weight: 500;
  min-width: 0;
}

.content-wrap {
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.7;
  margin-bottom: 12rpx;
  word-break: break-all;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.review-badge {
  padding: 2rpx 10rpx;
  background: rgba(230, 162, 60, 0.12);
  color: #e6a23c;
  font-size: 18rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.reply-badge {
  padding: 2rpx 10rpx;
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
  font-size: 18rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.time {
  font-size: 22rpx;
  color: var(--text-placeholder);
  margin-left: auto;
}
</style>
