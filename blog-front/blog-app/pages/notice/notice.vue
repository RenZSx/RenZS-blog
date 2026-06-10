<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="empty-card">
      <view class="emoji-bg">
        <bx-icon name="bellRing" :size="80" color="#42b983" />
      </view>
      <text class="title">通知功能开发中</text>
      <text class="subtitle">未来将在此查看</text>
      <view class="feature-list">
        <view class="feature-item">
          <view class="dot" />
          <text>评论回复提醒</text>
        </view>
        <view class="feature-item">
          <view class="dot" />
          <text>点赞通知</text>
        </view>
        <view class="feature-item">
          <view class="dot" />
          <text>系统公告</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { useThemeClass } from '@/composables/useThemeClass'
const { isDark } = useThemeClass()
// 未来接入 WebSocket /notice-websocket/{userId}?token=xxx
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
}

.empty-card {
  width: 100%;
  background: var(--bg-card);
  border-radius: 28rpx;
  padding: 80rpx 48rpx;
  text-align: center;
  box-shadow: var(--shadow-md);
}

.emoji-bg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(66, 185, 131, 0.12) 0%, rgba(66, 185, 131, 0.04) 100%);
  margin-bottom: 32rpx;
}

.title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12rpx;
}

.subtitle {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary);
  margin-bottom: 36rpx;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  align-items: flex-start;
  padding: 32rpx;
  background: var(--bg-soft);
  border-radius: 20rpx;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: var(--text-regular);
}

.dot {
  width: 12rpx;
  height: 12rpx;
  background: #42b983;
  border-radius: 50%;
  margin-right: 16rpx;
}
</style>
