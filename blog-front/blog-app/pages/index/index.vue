<template>
  <view class="page">
    <!-- 顶部欢迎条 -->
    <view class="hero-bar">
      <view class="greeting">
        <text class="greet-text">{{ greeting }}</text>
        <text class="user-name">{{ userStore.userInfo?.nickname || '游客' }}</text>
      </view>
      <view class="hero-stat">
        <text class="stat-num">{{ articles.length }}</text>
        <text class="stat-label">已加载</text>
      </view>
    </view>

    <!-- 骨架屏(初始加载) -->
    <view v-if="loading && articles.length === 0" class="skeleton-list">
      <view v-for="i in 3" :key="i" class="skeleton-card">
        <view class="skeleton skel-cover" />
        <view class="skeleton skel-title" />
        <view class="skeleton skel-meta" />
      </view>
    </view>

    <!-- 空态 -->
    <view v-else-if="articles.length === 0" class="empty-state">
      <bx-icon name="feather" :size="120" color="#c0c4cc" />
      <text class="empty-tip">还没有文章哦</text>
    </view>

    <!-- 文章列表 -->
    <view v-else class="article-list">
      <view
        v-for="(article, idx) in articles"
        :key="article.id"
        class="article-card fade-in-up"
        :class="{ 'card-hero': idx === 0 && article.articleCover }"
        :style="{ animationDelay: `${Math.min(idx * 60, 300)}ms` }"
        @click="goDetail(article.id)"
      >
        <!-- 头条 Hero 卡片 -->
        <template v-if="idx === 0 && article.articleCover">
          <image :src="article.articleCover" class="hero-cover" mode="aspectFill" />
          <view class="hero-mask" />
          <view class="hero-info">
            <view class="tag-row" v-if="article.tagDTOList?.length">
              <text class="tag-chip">{{ article.tagDTOList[0].tagName }}</text>
            </view>
            <text class="hero-title text-ellipsis-2">{{ article.articleTitle }}</text>
            <view class="hero-meta">
              <bx-icon name="clock" :size="22" color="rgba(255,255,255,0.85)" />
              <text style="margin-left: 8rpx">{{ formatTime(article.createTime) }}</text>
              <text class="dot">·</text>
              <bx-icon name="eye" :size="22" color="rgba(255,255,255,0.85)" />
              <text style="margin-left: 8rpx">{{ article.viewsCount || 0 }} 阅读</text>
            </view>
          </view>
        </template>

        <!-- 常规卡片(图文混排) -->
        <template v-else>
          <view class="card-body">
            <view class="card-text">
              <view class="card-tags" v-if="article.tagDTOList?.length">
                <text
                  v-for="tag in article.tagDTOList.slice(0, 2)"
                  :key="tag.id"
                  class="tag"
                >{{ tag.tagName }}</text>
              </view>
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
                <view class="meta-item">
                  <bx-icon name="comment" :size="22" color="#909399" />
                  <text class="meta-text">{{ article.commentCount || 0 }}</text>
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
        </template>
      </view>

      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="!hasMore" class="end-tip">— 已经到底了 —</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getArticles } from '@/api/article'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const articles = ref([])
const current = ref(1)
const loading = ref(false)
const hasMore = ref(true)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了 ·'
  if (h < 12) return '上午好 ·'
  if (h < 18) return '下午好 ·'
  return '晚上好 ·'
})

async function loadArticles(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) {
      current.value = 1
      hasMore.value = true
    }
    const res = await getArticles(current.value)
    if (res.flag && res.data) {
      const list = res.data.records || res.data.articlePreviewDTOList || res.data || []
      if (reset) {
        articles.value = list
      } else {
        articles.value = articles.value.concat(list)
      }
      if (list.length === 0) {
        hasMore.value = false
      } else {
        current.value += 1
      }
    }
  } catch (e) {
    // 已由拦截器提示
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/article/article?id=${id}` })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onShow(() => {
  if (articles.value.length === 0) loadArticles(true)
})

onPullDownRefresh(async () => {
  await loadArticles(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  loadArticles(false)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 32rpx;
}

.hero-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 24rpx 32rpx 24rpx;
  background: linear-gradient(180deg, rgba(66, 185, 131, 0.08) 0%, transparent 100%);
}

.greeting {
  display: flex;
  flex-direction: column;
}

.greet-text {
  font-size: 24rpx;
  color: var(--text-secondary);
  margin-bottom: 6rpx;
}

.user-name {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -1rpx;
}

.hero-stat {
  text-align: right;
}

.stat-num {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #42b983;
}

.stat-label {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.skeleton-list { padding: 8rpx 24rpx; }

.skeleton-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.skel-cover { height: 280rpx; margin-bottom: 24rpx; border-radius: 16rpx; }
.skel-title { height: 32rpx; margin-bottom: 16rpx; width: 80%; }
.skel-meta { height: 24rpx; width: 50%; }

.article-list { padding: 8rpx 24rpx; }

.article-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: var(--shadow-md);
  transition: all 220ms ease;
  opacity: 0;
  animation-fill-mode: both;
}

.article-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.card-hero { position: relative; height: 380rpx; }

.hero-cover { width: 100%; height: 100%; background-color: #ebeef5; }

.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 30%, rgba(0, 0, 0, 0.7) 100%);
}

.hero-info {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 32rpx 28rpx;
  color: #ffffff;
}

.tag-row { margin-bottom: 16rpx; }

.tag-chip {
  display: inline-block;
  padding: 6rpx 18rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #ffffff;
}

.hero-title {
  display: block;
  font-size: 38rpx;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 16rpx;
  letter-spacing: -0.5rpx;
}

.hero-meta {
  display: flex;
  align-items: center;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}

.hero-meta .dot { margin: 0 12rpx; opacity: 0.6; }

.card-body { display: flex; padding: 24rpx; gap: 24rpx; }

.card-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.tag {
  display: inline-block;
  padding: 4rpx 14rpx;
  background: rgba(66, 185, 131, 0.1);
  color: #339268;
  font-size: 20rpx;
  border-radius: 16rpx;
  font-weight: 500;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.45;
  margin-bottom: 16rpx;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
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
  width: 200rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background-color: #ebeef5;
  flex-shrink: 0;
}

.load-more {
  text-align: center;
  padding: 40rpx 0 24rpx;
  color: var(--text-secondary);
  font-size: 24rpx;
}

.end-tip { letter-spacing: 4rpx; opacity: 0.6; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 48rpx;
  color: var(--text-secondary);
  font-size: 26rpx;
}

.empty-tip {
  margin-top: 24rpx;
}
</style>
