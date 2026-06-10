<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="comment-header" v-if="comments.length > 0">
      <text class="header-title">全部评论</text>
      <text class="header-count">{{ comments.length }}</text>
    </view>

    <scroll-view scroll-y class="list" @scrolltolower="loadMore">
      <view v-if="comments.length === 0 && !loading" class="empty-state">
        <bx-icon name="comment" :size="120" color="#c0c4cc" />
        <text class="empty-title">还没有评论</text>
        <text class="empty-sub">来发表第一条吧~</text>
      </view>

      <view
        v-for="(comment, idx) in comments"
        :key="comment.id"
        class="comment-item fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 40, 200)}ms` }"
      >
        <image class="avatar" :src="comment.avatar || defaultAvatar" mode="aspectFill" />
        <view class="comment-content">
          <view class="comment-header-row">
            <view class="user-info">
              <text class="nickname">{{ comment.nickname || comment.username || '匿名' }}</text>
              <text class="role-badge" v-if="comment.isAuthor === 1">作者</text>
            </view>
            <text class="time">{{ formatTime(comment.createTime) }}</text>
          </view>
          <text class="text">{{ comment.commentContent }}</text>
          <view class="comment-actions">
            <view
              class="like-btn"
              :class="{ liked: userStore.isCommentLiked(comment.id) }"
              @click="handleLike(comment)"
            >
              <bx-icon
                :name="userStore.isCommentLiked(comment.id) ? 'heartFilled' : 'heart'"
                :size="26"
                :color="userStore.isCommentLiked(comment.id) ? '#f56c6c' : '#909399'"
              />
              <text class="like-count">{{ comment.likeCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading && comments.length > 0" class="loading-tip">加载中...</view>
      <view v-else-if="!hasMore && comments.length > 0" class="loading-tip end-tip">— 已经到底了 —</view>
    </scroll-view>

    <!-- 底部输入栏 -->
    <view class="input-bar">
      <view class="input-wrap">
        <input
          class="input"
          v-model="newComment"
          placeholder="说点什么..."
          placeholder-class="ph"
          confirm-type="send"
          @confirm="handleSend"
        />
      </view>
      <view
        class="send-btn"
        :class="{ active: newComment.trim() }"
        @click="handleSend"
      >
        <bx-icon name="send" :size="32" :color="newComment.trim() ? '#ffffff' : '#c0c4cc'" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getComments, postComment, likeComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'
import { COMMENT_TYPE_ARTICLE } from '@/utils/config'

const userStore = useUserStore()
const { isDark } = useThemeClass()
const comments = ref([])
const current = ref(1)
const loading = ref(false)
const hasMore = ref(true)
const sending = ref(false)
const newComment = ref('')
const articleId = ref(null)
const routePath = ref('')
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

async function loadComments(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) {
      current.value = 1
      hasMore.value = true
    }
    const res = await getComments({
      current: current.value,
      type: COMMENT_TYPE_ARTICLE,
      routePath: routePath.value
    })
    if (res.flag && res.data) {
      // 后端 /comments 返回 PageResult { recordList, count }
      const list = res.data.recordList || res.data.records || res.data.commentDTOList || []
      if (reset) {
        comments.value = list
      } else {
        comments.value = comments.value.concat(list)
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

function loadMore() { loadComments(false) }

async function handleSend() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 800)
    return
  }
  if (!newComment.value.trim()) return
  sending.value = true
  try {
    const res = await postComment({
      content: newComment.value.trim(),
      type: COMMENT_TYPE_ARTICLE,
      routePath: routePath.value
    })
    if (res.flag) {
      uni.showToast({ title: '评论成功', icon: 'success' })
      newComment.value = ''
      loadComments(true)
    } else {
      uni.showToast({ title: res.message || '发表失败', icon: 'none' })
    }
  } catch (e) {
    // 已在拦截器处理
  } finally {
    sending.value = false
  }
}

async function handleLike(comment) {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const res = await likeComment(comment.id)
    if (res.flag) {
      userStore.toggleCommentLike(comment.id)
      if (userStore.isCommentLiked(comment.id)) {
        comment.likeCount = (comment.likeCount || 0) + 1
      } else {
        comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
      }
    }
  } catch (e) {
    // 已在拦截器处理
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getMonth() + 1}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onLoad((query) => {
  articleId.value = Number(query.id)
  routePath.value = `/articles/${query.id}`
  loadComments(true)
})
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-page);
}

.comment-header {
  display: flex;
  align-items: baseline;
  padding: 24rpx 32rpx 12rpx;
  background: var(--bg-page);
}

.header-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.header-count {
  margin-left: 12rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.list { flex: 1; overflow-y: auto; padding: 8rpx 24rpx; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0 80rpx;
  color: var(--text-secondary);
}

.empty-title { margin-top: 24rpx; font-size: 28rpx; }
.empty-sub { margin-top: 8rpx; font-size: 22rpx; opacity: 0.7; }

.loading-tip {
  text-align: center;
  color: var(--text-secondary);
  padding: 32rpx 0;
  font-size: 22rpx;
}

.end-tip { letter-spacing: 4rpx; opacity: 0.6; }

.comment-item {
  display: flex;
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow-sm);
  opacity: 0;
  animation-fill-mode: both;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #ebeef5;
  flex-shrink: 0;
}

.comment-content { flex: 1; margin-left: 20rpx; min-width: 0; }

.comment-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.user-info { display: flex; align-items: center; }

.nickname {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.role-badge {
  margin-left: 12rpx;
  padding: 2rpx 12rpx;
  background: rgba(66, 185, 131, 0.1);
  color: #339268;
  font-size: 18rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.time { font-size: 22rpx; color: var(--text-placeholder); }

.text {
  display: block;
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.7;
  margin-bottom: 12rpx;
  word-break: break-all;
}

.comment-actions { display: flex; }

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  transition: all 220ms ease;
}

.like-btn.liked { background: rgba(245, 108, 108, 0.08); }

.like-count {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.like-btn.liked .like-count { color: #f56c6c; font-weight: 600; }

/* ========= 输入栏 ========= */
.input-bar {
  display: flex;
  align-items: center;
  background: var(--bg-card);
  padding: 16rpx 24rpx;
  border-top: 2rpx solid var(--border-color);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  gap: 16rpx;
}

.input-wrap {
  flex: 1;
  background: var(--bg-soft);
  border-radius: 36rpx;
  padding: 0 24rpx;
}

.input {
  height: 72rpx;
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
  background: linear-gradient(135deg, #42b983 0%, #2d8362 100%);
  box-shadow: 0 6rpx 16rpx rgba(66, 185, 131, 0.4);
}

.send-btn.active:active {
  transform: scale(0.94);
}
</style>
