<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="page-header">
      <text class="header-title">说说</text>
      <text class="header-sub">记录日常,分享生活</text>
    </view>

    <view v-if="loading && talks.length === 0" class="loading-tip">加载中...</view>

    <view v-else-if="talks.length === 0" class="empty-state">
      <bx-icon name="smile" :size="120" color="#c0c4cc" />
      <text class="empty-tip">暂无说说</text>
    </view>

    <view v-else class="talk-list">
      <view
        v-for="(talk, idx) in talks"
        :key="talk.id"
        class="talk-card fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 60, 300)}ms` }"
        @click="goDetail(talk.id)"
      >
        <view class="card-header">
          <image class="avatar" :src="talk.avatar || defaultAvatar" mode="aspectFill" />
          <view class="user-meta">
            <view class="user-row">
              <text class="nickname">{{ talk.nickname || '匿名' }}</text>
              <view v-if="talk.isTop === 1" class="top-badge">置顶</view>
            </view>
            <text class="time">{{ formatTime(talk.createTime) }}</text>
          </view>
        </view>

        <mp-html
          class="content"
          :content="talk.content || ''"
          :tag-style="contentTagStyle"
        />

        <!-- 图片九宫格 -->
        <view v-if="talk.imgList && talk.imgList.length" class="image-grid" :class="`grid-${gridType(talk.imgList.length)}`">
          <image
            v-for="(img, i) in talk.imgList"
            :key="i"
            class="grid-img"
            :src="img"
            mode="aspectFill"
            @click.stop="previewImg(talk.imgList, i)"
          />
        </view>

        <view class="card-footer">
          <view
            class="action-btn"
            :class="{ active: userStore.isArticleLiked(talkLikeKey(talk.id)) }"
            @click.stop="handleLike(talk)"
          >
            <bx-icon
              :name="userStore.isArticleLiked(talkLikeKey(talk.id)) ? 'heartFilled' : 'heart'"
              :size="28"
              :color="userStore.isArticleLiked(talkLikeKey(talk.id)) ? '#f56c6c' : '#909399'"
            />
            <text class="action-text">{{ talk.likeCount || 0 }}</text>
          </view>
          <view class="action-btn">
            <bx-icon name="comment" :size="28" color="#909399" />
            <text class="action-text">{{ talk.commentCount || 0 }}</text>
          </view>
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
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { getTalks, likeTalk } from '@/api/talk'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const talks = ref([])
const current = ref(1)
const loading = ref(false)
const hasMore = ref(true)
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

// 说说点赞复用 articleLikeSet,加前缀避免与文章 id 冲突
function talkLikeKey(id) {
  return `talk-${id}`
}

// rich-text/mp-html 内嵌段落样式覆盖
const contentTagStyle = {
  p: 'margin:8rpx 0;font-size:30rpx;line-height:1.7;color:var(--text-primary);',
  a: 'color:#42b983;',
  img: 'max-width:100%;border-radius:12rpx;display:block;margin:12rpx 0;'
}

function gridType(n) {
  if (n === 1) return '1'
  if (n === 4) return '2'
  if (n <= 3) return '3'
  return '3'  // 5+ 按 3 列排
}

async function load(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) { current.value = 1; hasMore.value = true }
    const res = await getTalks(current.value)
    if (res.flag && res.data) {
      const list = res.data.recordList || res.data.records || []
      // 后端 images 字段可能是逗号分隔字符串,如已有 imgList 则用,否则手动拆
      list.forEach((t) => {
        if (!t.imgList && t.images) {
          try {
            const arr = JSON.parse(t.images)
            t.imgList = Array.isArray(arr) ? arr : String(t.images).split(',').filter(Boolean)
          } catch (e) {
            t.imgList = String(t.images).split(',').filter(Boolean)
          }
        }
      })
      talks.value = reset ? list : talks.value.concat(list)
      if (list.length === 0) hasMore.value = false
      else current.value += 1
    }
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/talk/talk-detail?id=${id}` })
}

async function handleLike(talk) {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const res = await likeTalk(talk.id)
    if (res.flag) {
      userStore.toggleArticleLike(talkLikeKey(talk.id))
      if (userStore.isArticleLiked(talkLikeKey(talk.id))) {
        talk.likeCount = (talk.likeCount || 0) + 1
      } else {
        talk.likeCount = Math.max(0, (talk.likeCount || 0) - 1)
      }
    }
  } catch (e) { /* 拦截器已处理 */ }
}

function previewImg(list, current) {
  uni.previewImage({ urls: list, current: list[current] })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onShow(() => {
  if (talks.value.length === 0) load(true)
})

onReachBottom(() => load(false))
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 32rpx;
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

.header-sub {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.loading-tip,
.empty-state {
  text-align: center;
  padding: 80rpx 0;
  color: var(--text-secondary);
  font-size: 26rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-tip { margin-top: 24rpx; }

.talk-list { padding: 0 24rpx; }

.talk-card {
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 24rpx;
  box-shadow: var(--shadow-md);
  opacity: 0;
  animation-fill-mode: both;
  transition: all 220ms ease;
}

.talk-card:active {
  transform: scale(0.99);
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: var(--bg-soft);
  flex-shrink: 0;
}

.user-meta {
  margin-left: 20rpx;
  flex: 1;
  min-width: 0;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.nickname {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.top-badge {
  padding: 2rpx 12rpx;
  background: linear-gradient(135deg, #f56c6c 0%, #c0392b 100%);
  color: #ffffff;
  font-size: 18rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.time {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.content {
  font-size: 30rpx;
  line-height: 1.7;
  color: var(--text-primary);
  margin-bottom: 16rpx;
  word-break: break-all;
}

.image-grid {
  display: grid;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.image-grid.grid-1 {
  grid-template-columns: 1fr;
}

.image-grid.grid-1 .grid-img {
  height: 400rpx;
}

.image-grid.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.image-grid.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.grid-img {
  width: 100%;
  height: 200rpx;
  border-radius: 12rpx;
  background: var(--bg-soft);
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid var(--border-color-light);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.action-btn.active .action-text { color: #f56c6c; font-weight: 600; }

.load-more {
  text-align: center;
  padding: 24rpx 0;
  color: var(--text-secondary);
  font-size: 22rpx;
}

.end-tip { letter-spacing: 4rpx; opacity: 0.6; }
</style>
