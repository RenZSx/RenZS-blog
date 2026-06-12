<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="page-header">
      <text class="header-title">我的收藏</text>
      <text class="header-sub">{{ collects.length }} 篇</text>
    </view>

    <view v-if="loading && collects.length === 0" class="loading-tip">加载中...</view>

    <view v-else-if="collects.length === 0" class="empty-state">
      <bx-icon name="bookmark" :size="120" color="#c0c4cc" />
      <text class="empty-title">还没有收藏</text>
      <text class="empty-sub">看到喜欢的文章随手收藏吧</text>
    </view>

    <view v-else class="article-list">
      <view
        v-for="(article, idx) in collects"
        :key="article.id"
        class="article-card fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 60, 300)}ms` }"
        @click="goDetail(article.articleId)"
      >
        <view class="card-body">
          <view class="card-text">
            <text class="card-title text-ellipsis-2">{{ article.articleTitle }}</text>
            <view class="card-meta">
              <view class="meta-item">
                <bx-icon name="clock" :size="22" color="#909399" />
                <text class="meta-text">{{ formatTime(article.createTime) }}</text>
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
          <view class="cancel-btn" @click.stop="onCancel(article)">
            <bx-icon name="close" :size="24" color="#909399" />
          </view>
        </view>
      </view>

      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="!hasMore" class="end-tip">— 没有更多了 —</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { getMyCollects, uncollectArticle } from '@/api/article'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const collects = ref([])
const current = ref(1)
const loading = ref(false)
const hasMore = ref(true)

async function load(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) { current.value = 1; hasMore.value = true }
    const res = await getMyCollects(current.value)
    if (res.flag && res.data) {
      const list = res.data.recordList || res.data.records || []
      collects.value = reset ? list : collects.value.concat(list)
      if (list.length === 0) hasMore.value = false
      else current.value += 1
    }
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/article/article?id=${id}` })
}

function onCancel(article) {
  uni.showModal({
    title: '取消收藏',
    content: `不再收藏《${article.articleTitle}》?`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        // 后端 ArticleCollectDTO: id=收藏记录主键, articleId=真正的文章 ID
        // 收藏/取消/详情都用 articleId,id 仅用作列表 :key
        const r = await uncollectArticle(article.articleId)
        if (r.flag) {
          collects.value = collects.value.filter((a) => a.id !== article.id)
          // 同步 store,文章详情页"收藏"按钮状态也跟着变
          if (userStore.isArticleCollected(article.articleId)) {
            userStore.toggleArticleCollect(article.articleId)
          }
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        }
      } catch (e) {}
    }
  })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onShow(() => {
  load(true)
})

onReachBottom(() => load(false))
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 48rpx;
}

.page-header {
  padding: 32rpx 32rpx 24rpx;
}

.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -1rpx;
  margin-bottom: 8rpx;
}

.header-sub { font-size: 24rpx; color: var(--text-secondary); }

.loading-tip,
.empty-state {
  text-align: center;
  padding: 80rpx 0;
  color: var(--text-secondary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
}

.empty-title { margin-top: 24rpx; font-size: 28rpx; color: var(--text-regular); }
.empty-sub { margin-top: 8rpx; font-size: 22rpx; opacity: 0.7; }

.article-list { padding: 8rpx 24rpx; }

.article-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-md);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.article-card:active { transform: scale(0.98); }

.card-body {
  display: flex;
  padding: 20rpx;
  gap: 20rpx;
  position: relative;
}

.card-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-right: 48rpx;  // 给取消按钮留位
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.45;
  margin-bottom: 16rpx;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
}

.meta-text {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.card-cover {
  width: 160rpx;
  height: 130rpx;
  border-radius: 12rpx;
  background-color: #ebeef5;
  flex-shrink: 0;
}

.cancel-btn {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 48rpx;
  height: 48rpx;
  background: var(--bg-soft);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn:active { background: var(--border-color); }

.load-more {
  text-align: center;
  padding: 24rpx 0;
  color: var(--text-secondary);
  font-size: 22rpx;
}

.end-tip { letter-spacing: 4rpx; opacity: 0.6; }
</style>
