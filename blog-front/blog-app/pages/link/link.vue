<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="page-header">
      <text class="header-title">友情链接</text>
      <text class="header-sub">{{ links.length }} 个朋友</text>
    </view>

    <view v-if="loading" class="loading-tip">加载中...</view>

    <view v-else-if="links.length === 0" class="empty-state">
      <bx-icon name="link" :size="120" color="#c0c4cc" />
      <text class="empty-tip">还没有友链</text>
    </view>

    <view v-else class="link-list">
      <view
        v-for="(link, idx) in links"
        :key="link.id"
        class="link-card fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 60, 300)}ms` }"
        @click="openLink(link)"
      >
        <image class="link-avatar" :src="link.linkAvatar" mode="aspectFill" />
        <view class="link-info">
          <text class="link-name">{{ link.linkName }}</text>
          <text class="link-intro text-ellipsis-2">{{ link.linkIntro || '' }}</text>
          <view class="link-meta">
            <bx-icon name="externalLink" :size="22" color="#909399" />
            <text class="link-url text-ellipsis">{{ link.linkAddress }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 申请友链入口 -->
    <view class="apply-card">
      <text class="apply-title">想成为友链?</text>
      <text class="apply-tip">前往留言板说明你的网站信息</text>
      <view class="apply-btn" @click="goMessage">
        <text>去留言</text>
        <bx-icon name="arrowRight" :size="24" color="#ffffff" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getLinks } from '@/api/link'
import { useThemeClass } from '@/composables/useThemeClass'

const { isDark } = useThemeClass()

const links = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getLinks()
    if (res.flag) {
      links.value = Array.isArray(res.data) ? res.data : []
    }
  } finally {
    loading.value = false
  }
}

function openLink(link) {
  if (!link.linkAddress) return
  // App 端用 webview 内打开 / 系统浏览器打开
  uni.setClipboardData({
    data: link.linkAddress,
    success: () => {
      uni.showModal({
        title: link.linkName,
        content: `链接已复制\n${link.linkAddress}`,
        confirmText: '我知道了',
        showCancel: false
      })
    }
  })
}

function goMessage() {
  uni.navigateTo({ url: '/pages/message/message' })
}

onShow(() => {
  if (links.value.length === 0) load()
})
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
}

.empty-tip { margin-top: 24rpx; }

.link-list {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.link-card {
  display: flex;
  align-items: flex-start;
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-md);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.link-card:active {
  transform: scale(0.98);
  background: var(--bg-soft);
}

.link-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  background: var(--bg-soft);
  flex-shrink: 0;
}

.link-info {
  flex: 1;
  margin-left: 24rpx;
  min-width: 0;
}

.link-name {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8rpx;
}

.link-intro {
  display: block;
  font-size: 24rpx;
  color: var(--text-regular);
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.link-meta {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  max-width: 100%;
}

.link-url {
  font-size: 22rpx;
  color: var(--text-secondary);
  flex: 1;
  min-width: 0;
}

.apply-card {
  margin: 48rpx 24rpx 0;
  background: linear-gradient(135deg, rgba(71, 85, 105, 0.12) 0%, rgba(71, 85, 105, 0.04) 100%);
  border-radius: 20rpx;
  padding: 36rpx 28rpx;
  text-align: center;
}

.apply-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8rpx;
}

.apply-tip {
  display: block;
  font-size: 24rpx;
  color: var(--text-regular);
  margin-bottom: 20rpx;
}

.apply-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #3a3a3a 0%, #252525 100%);
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 36rpx;
  box-shadow: 0 8rpx 20rpx rgba(71, 85, 105, 0.3);
}
</style>
