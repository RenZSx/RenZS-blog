<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <!-- Tab 切换 -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <text class="tab-count">{{ countOf(tab.value) }}</text>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view scroll-y class="list" @scrolltolower="onReachBottom">
      <!-- 加载中 -->
      <view v-if="loading && items.length === 0" class="loading-tip">加载中...</view>

      <!-- 空态 -->
      <view v-else-if="items.length === 0" class="empty-state">
        <bx-icon name="heart" :size="120" color="#c0c4cc" />
        <text class="empty-title">还没有{{ activeLabel }}</text>
        <text class="empty-sub">看到喜欢的随手点个赞吧</text>
      </view>

      <!-- 文章 Tab -->
      <view v-else-if="activeTab === 'article'" class="article-list">
        <view
          v-for="(article, idx) in items"
          :key="article.id"
          class="article-card fade-in-up"
          :style="{ animationDelay: `${Math.min(idx * 60, 240)}ms` }"
          @click="goArticleDetail(article.id)"
        >
          <view class="card-body">
            <view class="card-text">
              <text class="card-title text-ellipsis-2">{{ article.articleTitle }}</text>
              <view class="card-meta">
                <view class="meta-item">
                  <bx-icon name="clock" :size="22" color="#909399" />
                  <text class="meta-text">{{ formatDate(article.createTime) }}</text>
                </view>
                <view class="meta-item">
                  <bx-icon name="eye" :size="22" color="#909399" />
                  <text class="meta-text">{{ article.viewsCount || 0 }}</text>
                </view>
              </view>
            </view>
            <image
              v-if="article.articleCover"
              :src="article.articleCover"
              class="card-cover"
              mode="aspectFill"
            />
          </view>
        </view>
      </view>

      <!-- 说说 Tab -->
      <view v-else-if="activeTab === 'talk'" class="talk-list">
        <view
          v-for="(talk, idx) in items"
          :key="talk.id"
          class="talk-card fade-in-up"
          :style="{ animationDelay: `${Math.min(idx * 60, 240)}ms` }"
          @click="goTalkDetail(talk.id)"
        >
          <view class="talk-header">
            <image class="avatar" :src="talk.avatar || defaultAvatar" mode="aspectFill" />
            <view class="user-meta">
              <text class="nickname">{{ talk.nickname || '匿名' }}</text>
              <text class="time">{{ formatDate(talk.createTime) }}</text>
            </view>
          </view>
          <view class="talk-content text-ellipsis-3">
            <mp-html :content="talk.content || ''" :tag-style="talkContentStyle" />
          </view>
        </view>
      </view>

      <!-- 评论 Tab(暂未开放) -->
      <view v-else-if="activeTab === 'comment'" class="comment-disabled">
        <bx-icon name="comment" :size="100" color="#c0c4cc" />
        <text class="disabled-title">即将上线</text>
        <text class="disabled-sub">评论点赞功能正在开发中</text>
      </view>

      <view v-if="loading && items.length > 0" class="loading-tip">加载中...</view>
      <view v-else-if="!hasMore && items.length > 0" class="loading-tip end-tip">— 全部加载完毕 —</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getArticle } from '@/api/article'
import { getTalkById } from '@/api/talk'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

const tabs = [
  { value: 'article', label: '文章' },
  { value: 'talk', label: '说说' },
  { value: 'comment', label: '评论' }
]

const activeTab = ref('article')
const items = ref([])
const loading = ref(false)
const hasMore = ref(true)
const PAGE_SIZE = 10

const talkContentStyle = {
  p: 'margin:4rpx 0;font-size:28rpx;line-height:1.6;color:var(--text-regular);',
  a: 'color:#42b983;',
  img: 'max-width:100%;border-radius:8rpx;display:block;margin:6rpx 0;'
}

const activeLabel = computed(() => tabs.find((t) => t.value === activeTab.value)?.label || '')

function countOf(type) {
  if (!userStore.userInfo) return 0
  if (type === 'article') return userStore.userInfo.articleLikeSet?.length || 0
  if (type === 'talk') return userStore.userInfo.talkLikeSet?.length || 0
  if (type === 'comment') return userStore.userInfo.commentLikeSet?.length || 0
  return 0
}

// 当前 tab 的 ID 数组
function idsOf(type) {
  if (!userStore.userInfo) return []
  if (type === 'article') return userStore.userInfo.articleLikeSet || []
  if (type === 'talk') return userStore.userInfo.talkLikeSet || []
  return []
}

let pageCursor = 0

async function load(reset = false) {
  if (activeTab.value === 'comment') {
    // 评论 Tab 占位,不发请求
    items.value = []
    hasMore.value = false
    return
  }

  if (loading.value) return

  const ids = idsOf(activeTab.value)
  if (ids.length === 0) {
    items.value = []
    hasMore.value = false
    return
  }

  if (reset) {
    pageCursor = 0
    items.value = []
    hasMore.value = true
  }

  if (!hasMore.value) return

  loading.value = true
  try {
    // 取本页要拉的 ID 切片
    const slice = ids.slice(pageCursor, pageCursor + PAGE_SIZE)
    if (slice.length === 0) {
      hasMore.value = false
      return
    }

    // 并发拉详情(允许部分失败)
    const fetcher = activeTab.value === 'article' ? getArticle : getTalkById
    const results = await Promise.allSettled(slice.map((id) => fetcher(id)))
    const valid = results
      .filter((r) => r.status === 'fulfilled' && r.value?.flag && r.value.data)
      .map((r) => r.value.data)

    items.value = items.value.concat(valid)
    pageCursor += slice.length

    if (pageCursor >= ids.length) hasMore.value = false
  } finally {
    loading.value = false
  }
}

function switchTab(value) {
  if (activeTab.value === value) return
  activeTab.value = value
  load(true)
}

function onReachBottom() {
  load(false)
}

function goArticleDetail(id) {
  uni.navigateTo({ url: `/pages/article/article?id=${id}` })
}

function goTalkDetail(id) {
  uni.navigateTo({ url: `/pages/talk/talk-detail?id=${id}` })
}

function formatDate(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onShow(() => {
  // 进入页面 先确保用户信息已同步,再加载首屏
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

/* ========= Tab ========= */
.tabs {
  display: flex;
  background: var(--bg-card);
  padding: 16rpx 24rpx;
  gap: 12rpx;
  border-bottom: 2rpx solid var(--border-color);
}

.tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0;
  border-radius: 16rpx;
  transition: all 220ms ease;
}

.tab.active {
  background: rgba(66, 185, 131, 0.1);
}

.tab-label {
  font-size: 26rpx;
  color: var(--text-regular);
  margin-bottom: 4rpx;
}

.tab.active .tab-label {
  color: #42b983;
  font-weight: 600;
}

.tab-count {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.tab.active .tab-count { color: #42b983; }

/* ========= 列表 ========= */
.list {
  flex: 1;
  overflow-y: auto;
  // 右侧加大 4rpx 边距,让卡片不顶满屏幕右边
  padding: 16rpx 28rpx 32rpx 24rpx;
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

/* ========= 文章卡 ========= */
.article-list { display: flex; flex-direction: column; gap: 16rpx; }

.article-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  box-shadow: var(--shadow-sm);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.article-card:active { transform: scale(0.98); }

.card-body {
  display: flex;
  // 卡片内部:右侧 padding 加大,长标题/数字不顶到右边
  padding: 20rpx 24rpx 20rpx 20rpx;
  gap: 20rpx;
}

.card-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  // 长 URL 强制换行
  word-break: break-all;
  overflow-wrap: anywhere;
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.45;
  margin-bottom: 16rpx;
}

.card-meta { display: flex; flex-wrap: wrap; gap: 16rpx; }
.meta-item { display: inline-flex; align-items: center; gap: 6rpx; }
.meta-text { font-size: 22rpx; color: var(--text-secondary); }

.card-cover {
  width: 160rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: var(--bg-soft);
  flex-shrink: 0;
}

/* ========= 说说卡 ========= */
.talk-list { display: flex; flex-direction: column; gap: 16rpx; }

.talk-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: var(--shadow-sm);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.talk-card:active { transform: scale(0.98); }

.talk-header { display: flex; align-items: center; margin-bottom: 16rpx; }

.avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: var(--bg-soft);
}

.user-meta { margin-left: 16rpx; }
.nickname { display: block; font-size: 26rpx; font-weight: 600; color: var(--text-primary); }
.time { font-size: 20rpx; color: var(--text-secondary); }

.talk-content {
  font-size: 28rpx;
  line-height: 1.6;
  color: var(--text-regular);
  max-height: 180rpx;
  overflow: hidden;
  // 长内容强制换行,不会撑爆容器
  word-break: break-all;
  overflow-wrap: anywhere;
}

/* ========= 评论占位 ========= */
.comment-disabled {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
  color: var(--text-secondary);
}

.disabled-title { margin-top: 24rpx; font-size: 28rpx; color: var(--text-regular); }
.disabled-sub { margin-top: 8rpx; font-size: 22rpx; opacity: 0.7; }
</style>
