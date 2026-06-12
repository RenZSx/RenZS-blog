<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <!-- 顶部品牌区 -->
    <view class="hero" :style="{ paddingTop: `${statusBarHeight + 24}px` }">
      <view class="hero-blob blob-1" />
      <view class="hero-blob blob-2" />

      <view class="hero-content">
        <!-- 已登录 -->
        <view v-if="userStore.isLoggedIn" class="user-card">
          <image
            class="avatar"
            :src="userStore.userInfo.avatar || defaultAvatar"
            mode="aspectFill"
          />
          <view class="user-meta">
            <text class="nickname">{{ userStore.userInfo.nickname || '匿名用户' }}</text>
            <text class="intro" v-if="userStore.userInfo.intro">{{ userStore.userInfo.intro }}</text>
            <text class="intro" v-else>这个人很懒,什么都没留下</text>
          </view>
          <view class="edit-btn" @click="goEdit">
            <bx-icon name="edit" :size="28" color="#ffffff" />
          </view>
        </view>

        <!-- 未登录 -->
        <view v-else class="user-card not-logged">
          <view class="avatar avatar-placeholder">
            <bx-icon name="user" :size="64" color="#475569" />
          </view>
          <view class="user-meta">
            <text class="nickname">未登录</text>
            <button class="login-btn" @click="goLogin">立即登录</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 数据统计 -->
    <view class="stats-row" v-if="userStore.isLoggedIn">
      <view class="stat-card">
        <text class="stat-num">{{ likeCount }}</text>
        <text class="stat-label">点赞</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-card">
        <text class="stat-num">{{ commentLikeCount }}</text>
        <text class="stat-label">评论</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-card">
        <text class="stat-num">{{ talkLikeCount }}</text>
        <text class="stat-label">说说</text>
      </view>
    </view>

    <!-- 菜单组 -->
    <view class="menu-section">
      <view class="menu-card">
        <view class="menu-item" @click="goCollects">
          <view class="menu-left">
            <view class="menu-icon icon-bg-yellow">
              <bx-icon name="bookmark" :size="32" color="#e6a23c" />
            </view>
            <text class="menu-text">我的收藏</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
        <view class="menu-divider" />
        <view class="menu-item" @click="goLikes">
          <view class="menu-left">
            <view class="menu-icon icon-bg-pink">
              <bx-icon name="heart" :size="32" color="#f56c6c" />
            </view>
            <text class="menu-text">我的点赞</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
        <view class="menu-divider" />
        <view class="menu-item" @click="goMyComments">
          <view class="menu-left">
            <view class="menu-icon icon-bg-blue">
              <bx-icon name="comment" :size="32" color="#409eff" />
            </view>
            <text class="menu-text">我的评论</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @click="goTalk">
          <view class="menu-left">
            <view class="menu-icon icon-bg-green">
              <bx-icon name="smile" :size="32" color="#475569" />
            </view>
            <text class="menu-text">说说</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
        <view class="menu-divider" />
        <view class="menu-item" @click="goMessage">
          <view class="menu-left">
            <view class="menu-icon icon-bg-blue">
              <bx-icon name="messageSquare" :size="32" color="#409eff" />
            </view>
            <text class="menu-text">留言板</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
        <view class="menu-divider" />
        <view class="menu-item" @click="goLink">
          <view class="menu-left">
            <view class="menu-icon icon-bg-orange">
              <bx-icon name="link" :size="32" color="#e6a23c" />
            </view>
            <text class="menu-text">友情链接</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @click="goSettings">
          <view class="menu-left">
            <view class="menu-icon icon-bg-gray">
              <bx-icon name="settings" :size="32" color="#606266" />
            </view>
            <text class="menu-text">设置</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
        <view class="menu-divider" />
        <view class="menu-item" @click="goAbout">
          <view class="menu-left">
            <view class="menu-icon icon-bg-purple">
              <bx-icon name="info" :size="32" color="#8b5cf6" />
            </view>
            <text class="menu-text">关于</text>
          </view>
          <bx-icon name="chevronRight" :size="36" color="#c0c4cc" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

const statusBarHeight = ref(0)
uni.getSystemInfo({
  success(info) { statusBarHeight.value = info.statusBarHeight || 20 }
})

const likeCount = computed(() => userStore.userInfo?.articleLikeSet?.length || 0)
const commentLikeCount = computed(() => userStore.userInfo?.commentLikeSet?.length || 0)
const talkLikeCount = computed(() => userStore.userInfo?.talkLikeSet?.length || 0)

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

function goEdit() {
  uni.navigateTo({ url: '/pages/profile-edit/profile-edit' })
}

function goSettings() {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

function goCollects() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/collects/collects' })
}

function goLikes() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/likes/likes' })
}

function goMyComments() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/my-comments/my-comments' })
}

function goTalk() {
  uni.navigateTo({ url: '/pages/talk/talk' })
}

function goMessage() {
  uni.navigateTo({ url: '/pages/message/message' })
}

function goLink() {
  uni.navigateTo({ url: '/pages/link/link' })
}

function goAbout() {
  uni.navigateTo({ url: '/pages/about/about' })
}

function onTodo() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 48rpx;
}

.hero {
  position: relative;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-muted) 100%);
  padding-bottom: 80rpx;
  overflow: hidden;
}

.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(40rpx);
  opacity: 0.4;
}

.blob-1 {
  top: -100rpx;
  right: -120rpx;
  width: 360rpx;
  height: 360rpx;
  background: rgba(255, 255, 255, 0.4);
}

.blob-2 {
  bottom: -80rpx;
  left: -80rpx;
  width: 280rpx;
  height: 280rpx;
  background: rgba(255, 255, 255, 0.25);
}

.hero-content {
  position: relative;
  z-index: 2;
  padding: 24rpx 32rpx 32rpx;
}

.user-card {
  display: flex;
  align-items: center;
}

.avatar {
  width: 132rpx;
  height: 132rpx;
  border-radius: 50%;
  background: #ffffff;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-meta {
  flex: 1;
  margin-left: 28rpx;
  color: #ffffff;
  min-width: 0;
}

.nickname {
  display: block;
  font-size: 38rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
  letter-spacing: -0.5rpx;
}

.intro {
  display: block;
  font-size: 24rpx;
  opacity: 0.9;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.not-logged .login-btn {
  display: inline-block;
  margin-top: 16rpx;
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 32rpx;
  background: #ffffff;
  color: var(--color-primary);
  font-size: 24rpx;
  font-weight: 600;
  border-radius: 28rpx;
  border: none;
}

.edit-btn {
  width: 64rpx;
  height: 64rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  transition: all 220ms ease;
}

.edit-btn:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.94);
}

.stats-row {
  margin: -56rpx 32rpx 24rpx;
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx 0;
  box-shadow: var(--shadow-lg);
}

.stat-card { flex: 1; text-align: center; }

.stat-num {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4rpx;
  letter-spacing: -1rpx;
}

.stat-label { font-size: 22rpx; color: var(--text-secondary); }

.stat-divider {
  width: 2rpx;
  height: 56rpx;
  background: var(--border-color);
}

.menu-section { padding: 0 24rpx; }

.menu-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-md);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
  transition: background 220ms ease;
}

.menu-item:active { background: var(--bg-soft); }

.menu-left { display: flex; align-items: center; }

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.icon-bg-pink { background: rgba(245, 108, 108, 0.12); }
.icon-bg-blue { background: rgba(64, 158, 255, 0.12); }
.icon-bg-yellow { background: rgba(230, 162, 60, 0.12); }
.icon-bg-gray { background: rgba(144, 147, 153, 0.12); }
.icon-bg-purple { background: rgba(139, 92, 246, 0.12); }
.icon-bg-green { background: rgba(71, 85, 105, 0.12); }
.icon-bg-orange { background: rgba(255, 154, 60, 0.12); }

.menu-text {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
}

.menu-divider {
  height: 2rpx;
  background: var(--border-color-light);
  margin-left: 112rpx;
}
</style>
