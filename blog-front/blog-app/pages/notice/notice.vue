<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <!-- 顶部 Hero + 过滤 Tab -->
    <view class="hero">
      <view class="hero-row">
        <view class="hero-title-area">
          <text class="hero-title">通知</text>
          <text class="hero-sub" v-if="noticeStore.unreadCount > 0">
            {{ noticeStore.unreadCount }} 条未读
          </text>
          <text class="hero-sub" v-else>暂无未读</text>
        </view>
        <view v-if="noticeStore.unreadCount > 0" class="all-read-btn" @click="onMarkAll">
          <bx-icon name="check" :size="24" color="#42b983" />
          <text class="all-read-text">全部已读</text>
        </view>
      </view>

      <!-- 实时状态指示 -->
      <view class="status-line">
        <view class="status-dot" :class="{ online: noticeStore.realtimeReady }" />
        <text class="status-text">
          {{ noticeStore.realtimeReady ? '已连接' : '未连接' }}
        </text>
      </view>

      <!-- 过滤 Tab -->
      <view class="filter-tabs">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: noticeStore.activeFilter === tab.value }"
          @click="onChangeTab(tab.value)"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 列表 -->
    <scroll-view scroll-y class="list" @scrolltolower="onReachBottom">
      <view v-if="noticeStore.loading && noticeStore.noticeList.length === 0" class="loading-tip">
        加载中...
      </view>

      <view v-else-if="noticeStore.filteredNoticeList.length === 0" class="empty-state">
        <bx-icon name="bellRing" :size="120" color="#c0c4cc" />
        <text class="empty-title">暂无通知</text>
        <text class="empty-sub">新消息会实时推送到这里</text>
      </view>

      <view
        v-for="(notice, idx) in noticeStore.filteredNoticeList"
        :key="`${notice.noticeType}-${notice.id}`"
        class="notice-item fade-in-up"
        :class="{ unread: notice.isRead !== 1 }"
        :style="{ animationDelay: `${Math.min(idx * 40, 200)}ms` }"
        @click="onClickNotice(notice)"
      >
        <view class="icon-wrap" :class="`type-${notice.noticeType}`">
          <bx-icon :name="iconOfType(notice.noticeType)" :size="32" :color="colorOfType(notice.noticeType)" />
        </view>
        <view class="notice-content">
          <view class="notice-header">
            <text class="notice-type-label">{{ typeLabel(notice.noticeType) }}</text>
            <text class="notice-time">{{ formatNoticeTime(notice.createTime) }}</text>
          </view>
          <view class="notice-text">
            <mp-html :content="notice.content || '收到一条新通知'" :tag-style="noticeTagStyle" />
          </view>
          <view v-if="notice.replyContent" class="notice-reply">
            <mp-html :content="notice.replyContent" :tag-style="noticeReplyTagStyle" />
          </view>
        </view>
        <view v-if="notice.isRead !== 1" class="unread-dot" />
      </view>

      <view v-if="noticeStore.loading && noticeStore.noticeList.length > 0" class="loading-tip">
        加载中...
      </view>
      <view
        v-else-if="!noticeStore.pagination.hasMore && noticeStore.noticeList.length > 0"
        class="loading-tip end-tip"
      >
        — 已经到底了 —
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onShow, onHide } from '@dcloudio/uni-app'
import { useNoticeStore } from '@/store/notice'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'
import { formatNoticeTime } from '@/utils/notice-time'

const noticeStore = useNoticeStore()
const userStore = useUserStore()
const { isDark } = useThemeClass()

// 通知内容里可能含 emoji <img>,inline 渲染配置
const noticeTagStyle = {
  p: 'margin:0;font-size:26rpx;line-height:1.5;color:var(--text-regular);',
  img: 'display:inline-block;height:36rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#42b983;'
}

// 回复内容引用块的样式(颜色比正文淡)
const noticeReplyTagStyle = {
  p: 'margin:0;font-size:24rpx;line-height:1.5;color:var(--text-secondary);',
  img: 'display:inline-block;height:32rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#42b983;'
}

const tabs = [
  { value: 'all', label: '全部' },
  { value: 'reply', label: '回复' },
  { value: 'like', label: '点赞' },
  { value: 'system', label: '系统' }
]

// 类型到图标 / 颜色 / 文案的映射
function iconOfType(t) {
  if (t === 'like') return 'heart'
  if (t === 'system') return 'bell'
  return 'comment'  // reply / 其他
}

function colorOfType(t) {
  if (t === 'like') return '#f56c6c'
  if (t === 'system') return '#8b5cf6'
  return '#409eff'
}

function typeLabel(t) {
  if (t === 'like') return '点赞'
  if (t === 'system') return '系统通知'
  if (t === 'reply') return '回复'
  return '通知'
}

async function onChangeTab(value) {
  if (noticeStore.activeFilter === value) return
  await noticeStore.changeFilter(value)
}

function onReachBottom() {
  noticeStore.fetchNotices({ reset: false })
}

async function onClickNotice(notice) {
  // 先本地置已读 + 调接口
  if (notice.isRead !== 1) {
    noticeStore.markRead(notice)
  }
  // 有跳转路径 → 跳转
  if (notice.jumpPath) {
    // jumpPath 形如 /articles/123,转成 App 路由 /pages/article/article?id=123
    const match = /^\/articles\/(\d+)/.exec(notice.jumpPath)
    if (match) {
      uni.navigateTo({ url: `/pages/article/article?id=${match[1]}` })
    } else {
      uni.showToast({ title: '该通知暂无关联页面', icon: 'none' })
    }
  }
}

function onMarkAll() {
  uni.showModal({
    title: '全部标记为已读',
    content: `将 ${noticeStore.unreadCount} 条未读通知标记为已读?`,
    success: (res) => {
      if (res.confirm) noticeStore.markAllAsRead()
    }
  })
}

// 生命周期:进入通知页 → panelActive=true + 拉列表
onShow(() => {
  noticeStore.setPanelActive(true)
  if (userStore.isLoggedIn) {
    noticeStore.fetchUnreadCount()
    noticeStore.fetchNotices({ reset: true })
  }
})

onHide(() => {
  noticeStore.setPanelActive(false)
})
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-page);
}

/* ========= Hero ========= */
.hero {
  background: var(--bg-card);
  padding: 24rpx 32rpx 16rpx;
  box-shadow: var(--shadow-sm);
}

.hero-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.hero-title-area {
  display: flex;
  flex-direction: column;
}

.hero-title {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -1rpx;
}

.hero-sub {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.all-read-btn {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 12rpx 20rpx;
  background: rgba(66, 185, 131, 0.1);
  border-radius: 24rpx;
}

.all-read-btn:active {
  background: rgba(66, 185, 131, 0.2);
}

.all-read-text {
  font-size: 24rpx;
  color: #42b983;
  font-weight: 500;
}

/* ========= 状态线 ========= */
.status-line {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #c0c4cc;
  margin-right: 8rpx;
  transition: background 220ms ease;
}

.status-dot.online {
  background: #42b983;
  box-shadow: 0 0 8rpx rgba(66, 185, 131, 0.6);
}

.status-text {
  font-size: 20rpx;
  color: var(--text-secondary);
}

/* ========= 过滤 Tab ========= */
.filter-tabs {
  display: flex;
  gap: 8rpx;
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  color: var(--text-regular);
  border-radius: 16rpx;
  transition: all 220ms ease;
}

.filter-tab.active {
  background: rgba(66, 185, 131, 0.1);
  color: #42b983;
  font-weight: 600;
}

/* ========= 列表 ========= */
.list {
  flex: 1;
  overflow-y: auto;
  padding: 16rpx 24rpx 32rpx;
}

.loading-tip {
  text-align: center;
  color: var(--text-secondary);
  padding: 32rpx 0;
  font-size: 22rpx;
}

.end-tip { letter-spacing: 4rpx; opacity: 0.6; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
  color: var(--text-secondary);
}

.empty-title {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: var(--text-regular);
}

.empty-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.7;
}

/* ========= 通知项 ========= */
.notice-item {
  display: flex;
  background: var(--bg-card);
  border-radius: 20rpx;
  // 右侧加大留白:32rpx,给未读小红点留位 + 内容呼吸感
  padding: 24rpx 32rpx 24rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow-sm);
  position: relative;
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.notice-item.unread::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 6rpx;
  background: #42b983;
  border-radius: 4rpx 0 0 4rpx;
}

.notice-item:active {
  transform: scale(0.98);
}

.icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.type-reply { background: rgba(64, 158, 255, 0.12); }
.type-like { background: rgba(245, 108, 108, 0.12); }
.type-system { background: rgba(139, 92, 246, 0.12); }

.notice-content {
  flex: 1;
  margin-left: 20rpx;
  // 右侧给 unread-dot 留出 28rpx 安全区,长文本不会顶进红点
  padding-right: 28rpx;
  min-width: 0;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.notice-type-label {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.notice-time {
  font-size: 22rpx;
  color: var(--text-placeholder);
}

.notice-text {
  display: block;
  font-size: 26rpx;
  color: var(--text-regular);
  line-height: 1.5;
  // 容器层级 2 行限高:emoji 比文字略大,行高 1.5 * 26rpx * 2 ≈ 78rpx + 余量
  max-height: 90rpx;
  overflow: hidden;
  // 长英文/URL 强制换行,避免横向溢出
  word-break: break-all;
  overflow-wrap: anywhere;
}

.notice-reply {
  display: block;
  margin-top: 8rpx;
  padding: 8rpx 12rpx 8rpx 16rpx;
  background: var(--bg-soft);
  border-left: 6rpx solid #42b983;
  border-radius: 0 8rpx 8rpx 0;
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.5;
  // 引用块 2 行限高
  max-height: 80rpx;
  overflow: hidden;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.reply-quote {
  font-size: 24rpx;
  color: var(--text-secondary);
  opacity: 0.6;
}

.unread-dot {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  width: 16rpx;
  height: 16rpx;
  background: #f56c6c;
  border-radius: 50%;
  box-shadow: 0 0 6rpx rgba(245, 108, 108, 0.5);
}
</style>
