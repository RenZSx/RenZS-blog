<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <!-- 顶部小 Hero -->
    <view class="hero-bar">
      <view class="hero-info">
        <text class="hero-label">{{ filterTypeLabel }}</text>
        <text class="hero-name">{{ filterName }}</text>
      </view>
      <view class="hero-stat" v-if="articles.length > 0">
        <text class="stat-num">{{ articles.length }}</text>
        <text class="stat-label">篇文章</text>
      </view>
    </view>

    <!-- 骨架 -->
    <view v-if="loading && articles.length === 0" class="skeleton-list">
      <view v-for="i in 3" :key="i" class="skeleton-card">
        <view class="skeleton skel-title" />
        <view class="skeleton skel-meta" />
      </view>
    </view>

    <!-- 空态 -->
    <view v-else-if="articles.length === 0" class="empty-state">
      <bx-icon name="feather" :size="120" color="#c0c4cc" />
      <text class="empty-tip">该{{ filterTypeLabel }}下还没有文章</text>
    </view>

    <!-- 文章列表 -->
    <view v-else class="article-list">
      <view
        v-for="(article, idx) in articles"
        :key="article.id"
        class="article-card fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 60, 300)}ms` }"
        @click="goDetail(article.id)"
      >
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
      </view>

      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="!hasMore" class="end-tip">— 没有更多了 —</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getArticlesByCondition } from '@/api/article'
import { useThemeClass } from '@/composables/useThemeClass'

const { isDark } = useThemeClass()

const articles = ref([])
const current = ref(1)
const loading = ref(false)
const hasMore = ref(true)

// query 参数
const categoryId = ref(null)
const categoryName = ref('')
const tagId = ref(null)
const tagName = ref('')

const filterTypeLabel = computed(() => (categoryId.value ? '分类' : '标签'))
const filterName = computed(() => categoryName.value || tagName.value || '全部')

async function loadArticles(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) {
      current.value = 1
      hasMore.value = true
    }
    const res = await getArticlesByCondition({
      current: current.value,
      categoryId: categoryId.value || undefined,
      tagId: tagId.value || undefined
    })
    if (res.flag && res.data) {
      // 后端返回 { name, articlePreviewDTOList }
      // 后端 /articles/condition 返回 ArticlePreviewListDTO { name, articlePreviewDTOList }
      const list = res.data.articlePreviewDTOList || res.data.recordList || res.data.records || []
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

onLoad((query) => {
  if (query.categoryId) {
    categoryId.value = Number(query.categoryId)
    categoryName.value = decodeURIComponent(query.categoryName || '')
    uni.setNavigationBarTitle({ title: categoryName.value || '分类文章' })
  } else if (query.tagId) {
    tagId.value = Number(query.tagId)
    tagName.value = decodeURIComponent(query.tagName || '')
    uni.setNavigationBarTitle({ title: tagName.value ? `#${tagName.value}` : '标签文章' })
  }
  loadArticles(true)
})

onReachBottom(() => loadArticles(false))
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
  padding: 32rpx;
  background: linear-gradient(180deg, rgba(66, 185, 131, 0.1) 0%, transparent 100%);
}

.hero-info {
  display: flex;
  flex-direction: column;
}

.hero-label {
  font-size: 22rpx;
  color: var(--text-secondary);
  margin-bottom: 6rpx;
  letter-spacing: 2rpx;
}

.hero-name {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -1rpx;
}

.hero-stat {
  text-align: right;
}

.stat-num {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #42b983;
}

.stat-label {
  font-size: 20rpx;
  color: var(--text-secondary);
}

/* ========= 骨架 ========= */
.skeleton-list { padding: 8rpx 24rpx; }

.skeleton-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.skel-title { height: 32rpx; margin-bottom: 16rpx; width: 70%; }
.skel-meta { height: 24rpx; width: 50%; }

/* ========= 列表 ========= */
.article-list { padding: 8rpx 24rpx; }

.article-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: var(--shadow-md);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.article-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.card-body {
  display: flex;
  padding: 24rpx;
  gap: 24rpx;
}

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

.empty-tip { margin-top: 24rpx; }
</style>
