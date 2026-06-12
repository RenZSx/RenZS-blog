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
            <text class="c-name">{{ comment.nickname || '匿名' }}</text>
            <text class="c-time">{{ formatTime(comment.createTime) }}</text>
          </view>
          <view class="c-text">
            <mp-html :content="comment.commentContent || ''" :tag-style="commentTagStyle" />
          </view>
          <view class="c-actions">
            <text class="reply-btn" @click="enterReplyMode(comment)">回复</text>
          </view>

          <!-- 二级回复区 -->
          <view
            v-if="(comment.replyDTOList && comment.replyDTOList.length > 0) || comment.replyCount > 0"
            class="reply-block"
          >
            <view
              v-for="reply in comment.replyDTOList || []"
              :key="reply.id"
              class="reply-item"
            >
              <image class="r-avatar" :src="reply.avatar || defaultAvatar" mode="aspectFill" />
              <view class="r-content">
                <view class="r-line">
                  <text class="r-name">{{ reply.nickname || '匿名' }}</text>
                  <text v-if="reply.replyNickname" class="r-to">
                    <text class="r-arrow"> @ </text>
                    <text class="r-target">{{ reply.replyNickname }}</text>
                  </text>
                </view>
                <view class="r-text">
                  <mp-html :content="reply.commentContent || ''" :tag-style="replyTagStyle" />
                </view>
                <view class="r-meta">
                  <text class="r-time">{{ formatTime(reply.createTime) }}</text>
                  <text class="r-action" @click.stop="enterReplyMode(comment, reply)">回复</text>
                </view>
              </view>
            </view>

            <view
              v-if="(comment.replyCount || 0) > (comment.replyDTOList?.length || 0) && !comment._allRepliesLoaded"
              class="more-replies"
              @click="loadMoreReplies(comment)"
            >
              <text v-if="comment._loadingReplies">加载中...</text>
              <text v-else>共 {{ comment.replyCount }} 条回复,展开全部 ›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部输入栏 -->
    <view class="input-area">
      <view v-if="replyTarget.parentId" class="reply-hint">
        <text class="hint-text">
          回复 <text class="hint-name">{{ replyTarget.replyNickname || replyTarget.parentNickname }}</text>
        </text>
        <view class="hint-cancel" @click="cancelReplyMode">
          <bx-icon name="close" :size="22" color="#909399" />
        </view>
      </view>
      <view class="input-bar">
        <view class="input-wrap">
          <input
            class="input"
            v-model="newComment"
            :placeholder="replyTarget.parentId ? `回复 ${replyTarget.replyNickname || replyTarget.parentNickname}...` : '说点什么...'"
            placeholder-class="ph"
            confirm-type="send"
            :focus="inputFocus"
            @confirm="handleSend"
            @blur="inputFocus = false"
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
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTalkById, likeTalk } from '@/api/talk'
import { getComments, postComment, getCommentReplies } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const talkId = ref(null)
const talk = ref(null)
const comments = ref([])
const loading = ref(true)
const newComment = ref('')
const inputFocus = ref(false)
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

const COMMENT_TYPE_TALK = 3

// 回复目标(同 article/comments.vue 的设计)
const replyTarget = reactive({
  parentId: null,
  replyUserId: null,
  replyNickname: '',
  parentNickname: ''
})

const contentTagStyle = {
  p: 'margin:12rpx 0;font-size:30rpx;line-height:1.75;color:var(--text-primary);',
  a: 'color:#3a3a3a;',
  img: 'max-width:100%;border-radius:12rpx;display:block;margin:12rpx 0;'
}

// 评论 mp-html 配置:emoji img 走 inline 行内表情样式
const commentTagStyle = {
  p: 'margin:0;font-size:26rpx;line-height:1.6;color:var(--text-regular);',
  img: 'display:inline-block;height:48rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#3a3a3a;'
}

// 二级回复字号小一档
const replyTagStyle = {
  p: 'margin:0;font-size:24rpx;line-height:1.55;color:var(--text-regular);',
  img: 'display:inline-block;height:40rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#3a3a3a;'
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
    // 与后端 CommentVO 对齐:commentContent / topicId / type / parentId / replyUserId
    const payload = {
      commentContent: newComment.value.trim(),
      topicId: talkId.value,
      type: COMMENT_TYPE_TALK
    }
    if (replyTarget.parentId) {
      payload.parentId = replyTarget.parentId
      payload.replyUserId = replyTarget.replyUserId
    }
    const res = await postComment(payload)
    if (res.flag) {
      uni.showToast({ title: '评论成功', icon: 'success' })
      newComment.value = ''
      cancelReplyMode()
      load()
    } else {
      uni.showToast({ title: res.message || '发表失败', icon: 'none' })
    }
  } catch (e) {}
}

/**
 * 进入回复模式
 * @param {Object} parent 顶级评论
 * @param {Object} [reply] 二级回复(回复别人的回复时传)
 */
function enterReplyMode(parent, reply) {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  replyTarget.parentId = parent.id
  replyTarget.parentNickname = parent.nickname || '匿名'
  if (reply) {
    replyTarget.replyUserId = reply.userId
    replyTarget.replyNickname = reply.nickname || '匿名'
  } else {
    replyTarget.replyUserId = parent.userId
    replyTarget.replyNickname = parent.nickname || '匿名'
  }
  inputFocus.value = true
  setTimeout(() => { inputFocus.value = true }, 50)
}

function cancelReplyMode() {
  replyTarget.parentId = null
  replyTarget.replyUserId = null
  replyTarget.replyNickname = ''
  replyTarget.parentNickname = ''
  inputFocus.value = false
}

/**
 * 展开某条评论的全部回复
 */
async function loadMoreReplies(comment) {
  if (comment._loadingReplies) return
  comment._loadingReplies = true
  try {
    const res = await getCommentReplies(comment.id, 1, 100)
    if (res.flag && res.data) {
      const all = Array.isArray(res.data) ? res.data : (res.data.recordList || [])
      comment.replyDTOList = all
      comment._allRepliesLoaded = true
    }
  } finally {
    comment._loadingReplies = false
  }
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

.c-actions { margin-top: 6rpx; }

.reply-btn {
  font-size: 20rpx;
  color: var(--text-secondary);
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.reply-btn:active {
  background: rgba(58, 58, 58, 0.08);
  color: #3a3a3a;
}

/* ========= 二级回复区 ========= */
.reply-block {
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  background: var(--bg-soft);
  border-radius: 10rpx;
}

.reply-item {
  display: flex;
  padding: 10rpx 0;
}

.reply-item + .reply-item {
  border-top: 2rpx dashed var(--border-color);
}

.r-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: var(--bg-card);
  flex-shrink: 0;
}

.r-content { flex: 1; margin-left: 12rpx; min-width: 0; }

.r-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 4rpx;
}

.r-name {
  font-size: 22rpx;
  font-weight: 600;
  color: #555552;
}

.r-to { display: inline-flex; align-items: center; }
.r-arrow { font-size: 20rpx; color: var(--text-placeholder); }
.r-target { font-size: 22rpx; color: #555552; }

.r-text {
  font-size: 24rpx;
  color: var(--text-regular);
  line-height: 1.55;
  word-break: break-all;
}

.r-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 4rpx;
}

.r-time { font-size: 18rpx; color: var(--text-placeholder); }
.r-action { font-size: 18rpx; color: var(--text-secondary); }

.more-replies {
  margin-top: 8rpx;
  padding: 6rpx 0;
  text-align: center;
  font-size: 20rpx;
  color: #3a3a3a;
  font-weight: 500;
}

/* ========= 输入栏 ========= */
.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-card);
  border-top: 2rpx solid var(--border-color);
  padding-bottom: env(safe-area-inset-bottom);
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 32rpx;
  background: rgba(58, 58, 58, 0.06);
  border-bottom: 2rpx solid var(--border-color-light);
}

.hint-text { font-size: 22rpx; color: var(--text-secondary); }
.hint-name { color: #3a3a3a; font-weight: 600; }

.hint-cancel {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-soft);
  border-radius: 50%;
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
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
  background: linear-gradient(135deg, #3a3a3a 0%, #252525 100%);
  box-shadow: 0 6rpx 16rpx rgba(58, 58, 58, 0.4);
}
</style>
