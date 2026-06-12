<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="page-header">
      <text class="header-title">浏览分类</text>
      <text class="header-sub">{{ categories.length }} 个主题</text>
    </view>

    <view v-if="loading && categories.length === 0" class="skeleton-grid">
      <view v-for="i in 6" :key="i" class="skel-card">
        <view class="skeleton skel-thumb" />
        <view class="skeleton skel-text" />
      </view>
    </view>

    <view v-else-if="categories.length === 0" class="empty-state">
      <bx-icon name="bookOpen" :size="120" color="#c0c4cc" />
      <text class="empty-tip">暂无分类</text>
    </view>

    <view v-else class="grid">
      <view
        v-for="(cat, idx) in categories"
        :key="cat.id"
        class="cat-card"
        :class="`bg-${idx % 5}`"
        @click="goCategoryArticles(cat)"
      >
        <text class="cat-emoji">{{ getCatEmoji(idx) }}</text>
        <text class="cat-name">{{ cat.categoryName }}</text>
        <text class="cat-count">{{ cat.articleCount || 0 }} 篇文章</text>
      </view>
    </view>

    <!-- 热门标签 -->
    <view v-if="tags.length > 0" class="tag-section">
      <view class="section-header">
        <text class="section-title">热门标签</text>
      </view>
      <view class="tag-cloud">
        <text
          v-for="(t, idx) in tags"
          :key="t.id"
          class="tag-chip"
          :class="`tag-color-${idx % 4}`"
          @click="goTagArticles(t)"
        >
          #{{ t.tagName }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCategories, getTags } from '@/api/article'
import { useThemeClass } from '@/composables/useThemeClass'

const { isDark } = useThemeClass()

const categories = ref([])
const tags = ref([])
const loading = ref(false)

const emojis = ['💻', '🎨', '📚', '🌍', '🚀', '🎮', '☕', '🏔️']

function getCatEmoji(idx) {
  return emojis[idx % emojis.length]
}

async function load() {
  loading.value = true
  try {
    const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
    // 后端 /categories 和 /tags 返回 PageResult { recordList, count },不是直接数组
    if (catRes.flag && catRes.data) {
      categories.value = Array.isArray(catRes.data)
        ? catRes.data
        : (catRes.data.recordList || catRes.data.records || [])
    }
    if (tagRes.flag && tagRes.data) {
      tags.value = Array.isArray(tagRes.data)
        ? tagRes.data
        : (tagRes.data.recordList || tagRes.data.records || [])
    }
  } finally {
    loading.value = false
  }
}

function goCategoryArticles(cat) {
  uni.navigateTo({
    url: `/pages/article-list/article-list?categoryId=${cat.id}&categoryName=${encodeURIComponent(cat.categoryName)}`
  })
}

function goTagArticles(tag) {
  uni.navigateTo({
    url: `/pages/article-list/article-list?tagId=${tag.id}&tagName=${encodeURIComponent(tag.tagName)}`
  })
}

onShow(() => {
  if (categories.value.length === 0) load()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 24rpx 24rpx 48rpx;
}

.page-header {
  padding: 16rpx 16rpx 32rpx;
}

.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8rpx;
  letter-spacing: -1rpx;
}

.header-sub {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.skel-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  height: 200rpx;
}

.skel-thumb { width: 80rpx; height: 80rpx; margin-bottom: 24rpx; border-radius: 16rpx; }
.skel-text { height: 28rpx; width: 70%; }

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.cat-card {
  display: flex;
  flex-direction: column;
  padding: 32rpx 28rpx;
  border-radius: 20rpx;
  min-height: 200rpx;
  transition: all 220ms ease;
  position: relative;
  overflow: hidden;
}

.cat-card:active { transform: translateY(2rpx); }

.bg-0 { background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-muted) 100%); color: #fff; }
.bg-1 { background: linear-gradient(135deg, #5e72e4 0%, #3a4cb1 100%); color: #fff; }
.bg-2 { background: linear-gradient(135deg, #f56c6c 0%, #c0392b 100%); color: #fff; }
.bg-3 { background: linear-gradient(135deg, #e6a23c 0%, #b87800 100%); color: #fff; }
.bg-4 { background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%); color: #fff; }

.cat-emoji {
  font-size: 56rpx;
  margin-bottom: 16rpx;
  line-height: 1;
}

.cat-name {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 6rpx;
  letter-spacing: -0.5rpx;
}

.cat-count {
  font-size: 22rpx;
  opacity: 0.85;
}

/* ========= 标签云 ========= */
.tag-section {
  margin-top: 48rpx;
  padding: 0 16rpx;
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: var(--bg-card);
  font-size: 26rpx;
  border-radius: 32rpx;
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-md);
  font-weight: 500;
  transition: all 220ms ease;
}

.tag-chip:active { transform: scale(0.94); }

.tag-color-0 { color: var(--color-primary); }
.tag-color-1 { color: #5e72e4; }
.tag-color-2 { color: #f56c6c; }
.tag-color-3 { color: #e6a23c; }

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

