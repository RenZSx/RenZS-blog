<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view v-if="loading" class="loading-tip">加载中...</view>

    <view v-else-if="talk" class="talk">
      <view class="card-header">
        <image class="avatar" :src="talk.avatar || defaultAvatar" mode="aspectFill" />
        <view class="user-meta">
          <text class="nickname">{{ talk.nickname || '匿名' }}</text>
          <text class="time">{{ formatTime(talk.createTime) }}</text>
        </view>
      </view>

      <mp-html
        class="content"
        :content="talk.content || ''"
        :tag-style="contentTagStyle"
      />

      <view v-if="talk.imgList && talk.imgList.length" class="image-list">
        <image
          v-for="(img, i) in talk.imgList"
          :key="i"
          class="single-img"
          :src="img"
          mode="widthFix"
          @click="previewImg(talk.imgList, i)"
        />
      </view>

      <view class="stats">
        <view
          class="stat-item"
          :class="{ liked: isLiked }"
          @click="handleLike"
        >
          <bx-icon :name="isLiked ? 'heartFilled' : 'heart'" :size="32" :color="isLiked ? '#f56c6c' : '#909399'" />
          <text class="stat-num">{{ talk.likeCount || 0 }}</text>
        </view>
        <view class="stat-item">
          <bx-icon name="comment" :size="32" color="#909399" />
          <text class="stat-num">{{ talk.commentCount || 0 }}</text>
        </view>
      </view>
    </view>

    <view class="divider" />

    <!-- 评论区(type=3) -->
    <view class="comment-section">
      <view class="comment-title">
        <text>评论 {{ comments.length }}</text>
      </view>

      <view v-if="comments.length === 0" class="empty-comments">
        <text>暂无评论,快来抢沙发~</text>
      </view>

      <view
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <image class="c-avatar" :src="comment.avatar || defaultAvatar" mode="aspectFill" />
        <view class="c-content">
          <view class="c-header">
            <text class="c-name">{{ comment.nickname || comment.username || '匿名' }}</text>
            <text class="c-time">{{ formatTime(comment.createTime) }}</text>
          </view>
          <text class="c-text">{{ comment.commentContent }}</text>
        </view>
      </view>
    </view>

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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTalkById, likeTalk } from '@/api/talk'
import { getComments, postComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const talkId = ref(null)
const talk = ref(null)
const comments = ref([])
const loading = ref(true)
const newComment = ref('')
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

const COMMENT_TYPE_TALK = 3

const contentTagStyle = {
  p: 'margin:12rpx 0;font-size:30rpx;line-height:1.75;color:var(--text-primary);',
  a: 'color:#42b983;',
  img: 'max-width:100%;border-radius:12rpx;display:block;margin:12rpx 0;'
}

function talkLikeKey(id) { return `talk-${id}` }

const isLiked = computed(() => userStore.isArticleLiked(talkLikeKey(talkId.value)))

async function load() {
  loading.value = true
  try {
    const res = await getTalkById(talkId.value)
    if (res.flag && res.data) {
      talk.value = res.data
      if (!talk.value.imgList && talk.value.images) {
        try {
          const arr = JSON.parse(talk.value.images)
          talk.value.imgList = Array.isArray(arr) ? arr : String(talk.value.images).split(',').filter(Boolean)
        } catch (e) {
          talk.value.imgList = String(talk.value.images).split(',').filter(Boolean)
        }
      }
    }
    // 评论
    const cres = await getComments({ topicId: talkId.value, type: COMMENT_TYPE_TALK, current: 1, size: 20 })
    if (cres.flag && cres.data) {
      comments.value = cres.data.recordList || cres.data.records || cres.data.commentDTOList || []
    }
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!userStore.isLoggedIn) return uni.showToast({ title: '请先登录', icon: 'none' })
  try {
    const res = await likeTalk(talkId.value)
    if (res.flag) {
      userStore.toggleArticleLike(talkLikeKey(talkId.value))
      if (userStore.isArticleLiked(talkLikeKey(talkId.value))) {
        talk.value.likeCount = (talk.value.likeCount || 0) + 1
      } else {
        talk.value.likeCount = Math.max(0, (talk.value.likeCount || 0) - 1)
      }
    }
  } catch (e) {}
}

async function handleSend() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!newComment.value.trim()) return
  try {
    const res = await postComment({
      topicId: talkId.value,
      content: newComment.value.trim(),
      type: COMMENT_TYPE_TALK
    })
    if (res.flag) {
      uni.showToast({ title: '评论成功', icon: 'success' })
      newComment.value = ''
      load()  // 简单刷新
    }
  } catch (e) {}
}

function previewImg(list, current) {
  uni.previewImage({ urls: list, current: list[current] })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onLoad((query) => {
  talkId.value = Number(query.id)
  if (!talkId.value) return uni.showToast({ title: '参数错误', icon: 'none' })
  load()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 160rpx;
}

.loading-tip { text-align: center; padding: 80rpx 0; color: var(--text-secondary); }

.talk {
  background: var(--bg-card);
  padding: 32rpx 28rpx 24rpx;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: var(--bg-soft);
}

.user-meta { margin-left: 20rpx; }
.nickname { display: block; font-size: 30rpx; font-weight: 600; color: var(--text-primary); margin-bottom: 4rpx; }
.time { font-size: 22rpx; color: var(--text-secondary); }

.content { font-size: 30rpx; line-height: 1.75; color: var(--text-primary); }

.image-list { margin-top: 16rpx; }

.single-img {
  width: 100%;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  background: var(--bg-soft);
}

.stats {
  display: flex;
  gap: 32rpx;
  margin-top: 16rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid var(--border-color-light);
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}

.stat-num { font-size: 24rpx; color: var(--text-secondary); }
.stat-item.liked .stat-num { color: #f56c6c; font-weight: 600; }

.divider { height: 16rpx; background: var(--bg-page); }

.comment-section {
  background: var(--bg-card);
  padding: 24rpx 28rpx 80rpx;
}

.comment-title {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

.empty-comments {
  text-align: center;
  padding: 60rpx 0;
  color: var(--text-secondary);
  font-size: 24rpx;
}

.comment-item {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 2rpx solid var(--border-color-light);
}

.c-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: var(--bg-soft);
}

.c-content { flex: 1; margin-left: 16rpx; min-width: 0; }

.c-header { display: flex; justify-content: space-between; margin-bottom: 4rpx; }
.c-name { font-size: 24rpx; font-weight: 600; color: var(--text-primary); }
.c-time { font-size: 20rpx; color: var(--text-placeholder); }
.c-text { display: block; font-size: 26rpx; color: var(--text-regular); line-height: 1.6; word-break: break-all; }

.input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
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

.input { height: 72rpx; font-size: 28rpx; color: var(--text-primary); }
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
</style>
