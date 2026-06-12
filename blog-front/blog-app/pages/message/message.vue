<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="page-header">
      <text class="header-title">留言板</text>
      <text class="header-sub">{{ messages.length }} 条留言</text>
    </view>

    <!-- 留言列表 -->
    <view v-if="loading && messages.length === 0" class="loading-tip">加载中...</view>

    <view v-else-if="messages.length === 0" class="empty-state">
      <bx-icon name="messageSquare" :size="120" color="#c0c4cc" />
      <text class="empty-title">还没有留言</text>
      <text class="empty-sub">来留下第一条吧</text>
    </view>

    <view v-else class="msg-list">
      <view
        v-for="(msg, idx) in messages"
        :key="msg.id"
        class="msg-item fade-in-up"
        :class="`accent-${idx % 5}`"
        :style="{ animationDelay: `${Math.min(idx * 40, 240)}ms` }"
      >
        <image class="avatar" :src="msg.avatar || defaultAvatar" mode="aspectFill" />
        <view class="msg-body">
          <text class="msg-nickname">{{ msg.nickname || '匿名' }}</text>
          <text class="msg-content">{{ msg.messageContent }}</text>
        </view>
      </view>
    </view>

    <!-- 留言输入 -->
    <view class="input-area">
      <view class="input-bar">
        <input
          class="input"
          v-model="content"
          placeholder="写下你的留言..."
          placeholder-class="ph"
          maxlength="100"
          confirm-type="send"
          @confirm="handleSend"
        />
        <view
          class="send-btn"
          :class="{ active: content.trim() && !sending }"
          @click="handleSend"
        >
          <bx-icon name="send" :size="32" :color="content.trim() && !sending ? '#ffffff' : '#c0c4cc'" />
        </view>
      </view>
      <text v-if="!userStore.isLoggedIn" class="tip">登录后留言会带上你的头像和昵称</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMessages, sendMessage } from '@/api/message'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const content = ref('')
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

async function load() {
  loading.value = true
  try {
    const res = await getMessages()
    if (res.flag) {
      messages.value = Array.isArray(res.data) ? res.data : []
    }
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (sending.value) return
  if (!content.value.trim()) return

  sending.value = true
  try {
    const u = userStore.userInfo
    const res = await sendMessage({
      nickname: u?.nickname || '游客',
      avatar: u?.avatar || defaultAvatar,
      messageContent: content.value.trim(),
      time: 15  // 弹幕滚动速度,后端要求 0-60 整数
    })
    if (res.flag) {
      uni.showToast({ title: '留言成功', icon: 'success' })
      content.value = ''
      load()
    } else {
      uni.showToast({ title: res.message || '发送失败', icon: 'none' })
    }
  } catch (e) {
    // 拦截器已处理
  } finally {
    sending.value = false
  }
}

onShow(() => {
  if (messages.value.length === 0) load()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 240rpx;
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

.loading-tip {
  text-align: center;
  padding: 80rpx 0;
  color: var(--text-secondary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
  color: var(--text-secondary);
}

.empty-title { margin-top: 24rpx; font-size: 28rpx; color: var(--text-regular); }
.empty-sub { margin-top: 8rpx; font-size: 22rpx; opacity: 0.7; }

.msg-list {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 24rpx;
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-md);
  opacity: 0;
  animation-fill-mode: both;
  position: relative;
  overflow: hidden;
}

.msg-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6rpx;
}

.accent-0::before { background: #3a3a3a; }
.accent-1::before { background: #5e72e4; }
.accent-2::before { background: #f56c6c; }
.accent-3::before { background: #e6a23c; }
.accent-4::before { background: #8b5cf6; }

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: var(--bg-soft);
  flex-shrink: 0;
  margin-left: 8rpx;
}

.msg-body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.msg-nickname {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8rpx;
}

.msg-content {
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.7;
  word-break: break-all;
}

/* ========= 输入区 ========= */
.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-card);
  padding: 16rpx 24rpx;
  border-top: 2rpx solid var(--border-color);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.04);
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.input {
  flex: 1;
  height: 80rpx;
  padding: 0 24rpx;
  background: var(--bg-soft);
  border-radius: 40rpx;
  font-size: 28rpx;
  color: var(--text-primary);
}

.ph { color: var(--text-placeholder); }

.send-btn {
  width: 80rpx;
  height: 80rpx;
  background: var(--bg-soft);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 220ms ease;
}

.send-btn.active {
  background: linear-gradient(135deg, #3a3a3a 0%, #252525 100%);
  box-shadow: 0 6rpx 16rpx rgba(71, 85, 105,0.4);
}

.tip {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
  text-align: center;
}
</style>
