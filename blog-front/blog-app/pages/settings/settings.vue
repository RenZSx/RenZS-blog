<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="section-title">外观</view>
    <view class="card">
      <view class="theme-row">
        <view
          v-for="opt in themeOptions"
          :key="opt.value"
          class="theme-option"
          :class="{ active: themeStore.mode === opt.value }"
          @click="setTheme(opt.value)"
        >
          <view class="theme-icon-wrap">
            <bx-icon :name="opt.icon" :size="48" :color="themeStore.mode === opt.value ? '#42b983' : '#909399'" />
          </view>
          <text class="theme-label">{{ opt.label }}</text>
          <view v-if="themeStore.mode === opt.value" class="check-mark">
            <bx-icon name="check" :size="24" color="#ffffff" />
          </view>
        </view>
      </view>
    </view>

    <view class="section-title">应用信息</view>
    <view class="card">
      <view class="info-item">
        <text class="label">版本</text>
        <text class="value">1.0.0</text>
      </view>
      <view class="divider" />
      <view class="info-item">
        <text class="label">服务器</text>
        <text class="value">{{ baseUrl }}</text>
      </view>
    </view>

    <view class="section-title">账号</view>
    <view class="card">
      <view v-if="userStore.isLoggedIn" class="info-item">
        <text class="label">当前用户</text>
        <text class="value">{{ userStore.userInfo?.nickname || '匿名' }}</text>
      </view>
      <view v-else class="info-item">
        <text class="label">登录状态</text>
        <text class="value">未登录</text>
      </view>
    </view>

    <view v-if="userStore.isLoggedIn" class="logout-area">
      <view class="logout-btn" @click="handleLogout">
        <bx-icon name="logout" :size="32" color="#f56c6c" />
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'
import { useThemeClass } from '@/composables/useThemeClass'
import { BASE_URL } from '@/utils/config'

const userStore = useUserStore()
const themeStore = useThemeStore()
const { isDark } = useThemeClass()
const loading = ref(false)
const baseUrl = BASE_URL

const themeOptions = [
  { value: 'light', label: '浅色', icon: 'sun' },
  { value: 'dark', label: '深色', icon: 'moon' },
  { value: 'auto', label: '跟随系统', icon: 'settings' }
]

function setTheme(mode) {
  themeStore.setMode(mode)
  uni.showToast({
    title: `已切换为${themeOptions.find(o => o.value === mode).label}`,
    icon: 'none',
    duration: 1200
  })
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '退出后需要重新登录',
    confirmColor: '#f56c6c',
    success: async (res) => {
      if (!res.confirm) return
      loading.value = true
      try {
        await userStore.logout()
        uni.showToast({ title: '已退出', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/login' })
        }, 600)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 24rpx;
}

.section-title {
  padding: 16rpx 16rpx 12rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.card {
  background: var(--bg-card);
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* ========= 主题切换 ========= */
.theme-row {
  display: flex;
  padding: 24rpx;
  gap: 16rpx;
}

.theme-option {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 16rpx;
  background: var(--bg-soft);
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 220ms ease;
}

.theme-option.active {
  background: rgba(66, 185, 131, 0.08);
  border-color: #42b983;
}

.theme-option:active { transform: scale(0.97); }

.theme-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  background: var(--bg-card);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.theme-option.active .theme-icon-wrap {
  background: rgba(66, 185, 131, 0.15);
}

.theme-label {
  font-size: 24rpx;
  color: var(--text-regular);
}

.theme-option.active .theme-label {
  color: #42b983;
  font-weight: 600;
}

.check-mark {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 32rpx;
  height: 32rpx;
  background: #42b983;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========= 信息项 ========= */
.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
}

.label {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
}

.value {
  font-size: 24rpx;
  color: var(--text-secondary);
  max-width: 60%;
  text-align: right;
  word-break: break-all;
}

.divider {
  height: 2rpx;
  background: var(--border-color-light);
  margin-left: 28rpx;
}

/* ========= 退出 ========= */
.logout-area {
  margin-top: 64rpx;
  padding: 0 24rpx;
}

.logout-btn {
  width: 100%;
  height: 96rpx;
  background: var(--bg-card);
  color: #f56c6c;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 24rpx;
  box-shadow: var(--shadow-sm);
  transition: all 220ms ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.logout-btn:active {
  background: rgba(245, 108, 108, 0.08);
  transform: translateY(2rpx);
}

.logout-text { font-size: 30rpx; color: #f56c6c; font-weight: 600; }
</style>
