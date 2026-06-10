<template>
  <view class="page">
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
      <text class="emoji">📚</text>
      <text>暂无分类</text>
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
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCategories } from '@/api/article'

const categories = ref([])
const loading = ref(false)

const emojis = ['💻', '🎨', '📚', '🌍', '🚀', '🎮', '☕', '🏔️']

function getCatEmoji(idx) {
  return emojis[idx % emojis.length]
}

async function load() {
  loading.value = true
  try {
    const res = await getCategories()
    if (res.flag && res.data) {
      categories.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function goCategoryArticles(cat) {
  uni.showToast({ title: `${cat.categoryName}(待实现)`, icon: 'none' })
}

onShow(() => {
  if (categories.value.length === 0) load()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fa;
  padding: 24rpx 24rpx 48rpx;
}

.page-header {
  padding: 16rpx 16rpx 32rpx;
}

.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #1d1f23;
  margin-bottom: 8rpx;
  letter-spacing: -1rpx;
}

.header-sub {
  font-size: 24rpx;
  color: #909399;
}

/* ========= 骨架 ========= */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.skel-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx 24rpx;
  height: 200rpx;
}

.skel-thumb {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 24rpx;
  border-radius: 16rpx;
}

.skel-text {
  height: 28rpx;
  width: 70%;
}

/* ========= 分类网格 ========= */
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.cat-card {
  display: flex;
  flex-direction: column;
  padding: 32rpx 28rpx;
  border-radius: 24rpx;
  min-height: 200rpx;
  transition: all 220ms ease;
  position: relative;
  overflow: hidden;
}

.cat-card:active {
  transform: translateY(2rpx);
}

.bg-0 { background: linear-gradient(135deg, #42b983 0%, #2d8362 100%); color: #fff; }
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 48rpx;
  color: #909399;
  font-size: 26rpx;
}

.empty-state .emoji {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.6;
}
</style>
