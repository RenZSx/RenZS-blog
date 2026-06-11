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
              <text class="nickname">{{ comment.nickname || '匿名' }}</text>
            </view>
            <text class="time">{{ formatTime(comment.createTime) }}</text>
          </view>

          <view class="text">
            <mp-html :content="comment.commentContent || ''" :tag-style="commentTagStyle" />
          </view>

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
            <text class="reply-btn" @click="enterReplyMode(comment)">回复</text>
          </view>

          <!-- 二级回复区(预加载的前几条 + 折叠展开) -->
          <view
            v-if="(comment.replyDTOList && comment.replyDTOList.length > 0) || comment.replyCount > 0"
            class="reply-block"
          >
            <view
              v-for="reply in comment.replyDTOList || []"
              :key="reply.id"
              class="reply-item"
            >
              <image class="reply-avatar" :src="reply.avatar || defaultAvatar" mode="aspectFill" />
              <view class="reply-content">
                <view class="reply-line">
                  <text class="reply-nickname">{{ reply.nickname || '匿名' }}</text>
                  <text v-if="reply.replyNickname" class="reply-to">
                    <text class="arrow"> @ </text>
                    <text class="target-name">{{ reply.replyNickname }}</text>
                  </text>
                </view>
                <view class="reply-text">
                  <mp-html :content="reply.commentContent || ''" :tag-style="replyTagStyle" />
                </view>
                <view class="reply-meta">
                  <text class="reply-time">{{ formatTime(reply.createTime) }}</text>
                  <text class="reply-action" @click.stop="enterReplyMode(comment, reply)">回复</text>
                </view>
              </view>
            </view>

            <!-- 查看更多 -->
            <view
              v-if="(comment.replyCount || 0) > (comment.replyDTOList?.length || 0) && !comment._allRepliesLoaded"
              class="more-replies"
              @click="loadMoreReplies(comment)"
            >
              <text v-if="comment._loadingReplies">加载中...</text>
              <text v-else>
                共 {{ comment.replyCount }} 条回复,展开全部 ›
              </text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading && comments.length > 0" class="loading-tip">加载中...</view>
      <view v-else-if="!hasMore && comments.length > 0" class="loading-tip end-tip">— 已经到底了 —</view>
    </scroll-view>

    <!-- 底部输入栏 -->
    <view class="input-area">
      <!-- 回复模式提示 -->
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
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getComments, postComment, likeComment, getCommentReplies } from '@/api/comment'
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
const inputFocus = ref(false)
const articleId = ref(null)
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

/**
 * 回复目标 state
 * parentId 为 null 时是一级评论;有值时是回复某条评论或回复别人的回复
 *   parentId       — 顶级评论 ID(后端 CommentVO.parentId)
 *   replyUserId    — 被回复者 userId(后端 CommentVO.replyUserId)
 *   replyNickname  — 被回复者昵称(仅 UI 展示)
 *   parentNickname — 顶级评论作者昵称(回复顶级评论时占位用)
 */
const replyTarget = reactive({
  parentId: null,
  replyUserId: null,
  replyNickname: '',
  parentNickname: ''
})

// 评论内容 mp-html 样式:支持 emoji <img> inline 渲染
const commentTagStyle = {
  p: 'margin:0;font-size:28rpx;line-height:1.7;color:var(--text-primary);',
  img: 'display:inline-block;height:48rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#42b983;'
}

// 二级回复字号比一级小一档
const replyTagStyle = {
  p: 'margin:0;font-size:26rpx;line-height:1.6;color:var(--text-regular);',
  img: 'display:inline-block;height:40rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#42b983;'
}

async function loadComments(reset = false) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) { current.value = 1; hasMore.value = true }
    const res = await getComments({
      current: current.value,
      type: COMMENT_TYPE_ARTICLE,
      topicId: articleId.value
    })
    if (res.flag && res.data) {
      const list = res.data.recordList || res.data.records || res.data.commentDTOList || []
      if (reset) comments.value = list
      else comments.value = comments.value.concat(list)
      if (list.length === 0) hasMore.value = false
      else current.value += 1
    }
  } finally {
    loading.value = false
  }
}

function loadMore() { loadComments(false) }

/**
 * 加载某条评论的全部回复
 * 后端 /comments/{cid}/replies 一次性返回所有回复(没有分页元数据,size 设大一点)
 */
async function loadMoreReplies(comment) {
  if (comment._loadingReplies) return
  comment._loadingReplies = true
  try {
    const res = await getCommentReplies(comment.id, 1, 100)
    if (res.flag && res.data) {
      // 后端返回 List<ReplyDTO>(直接数组)
      const all = Array.isArray(res.data) ? res.data : (res.data.recordList || [])
      comment.replyDTOList = all
      comment._allRepliesLoaded = true
    }
  } finally {
    comment._loadingReplies = false
  }
}

/**
 * 进入回复模式
 * @param {Object} parent - 顶级评论
 * @param {Object} [reply] - 二级回复(回复别人的回复时传)
 */
function enterReplyMode(parent, reply) {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  replyTarget.parentId = parent.id
  replyTarget.parentNickname = parent.nickname || '匿名'
  if (reply) {
    // 回复别人的回复:replyUserId = 被回复者 ID
    replyTarget.replyUserId = reply.userId
    replyTarget.replyNickname = reply.nickname || '匿名'
  } else {
    // 直接回复一级评论:replyUserId = 一级评论作者 ID
    replyTarget.replyUserId = parent.userId
    replyTarget.replyNickname = parent.nickname || '匿名'
  }
  // 唤起键盘
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

async function handleSend() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 800)
    return
  }
  if (!newComment.value.trim()) return
  if (sending.value) return
  sending.value = true
  try {
    // 与后端 CommentVO 对齐字段:commentContent / topicId / type / parentId / replyUserId
    const payload = {
      commentContent: newComment.value.trim(),
      topicId: articleId.value,
      type: COMMENT_TYPE_ARTICLE
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
      loadComments(true)
    } else {
      uni.showToast({ title: res.message || '发表失败', icon: 'none' })
    }
  } catch (e) {
    // 拦截器已处理
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
  } catch (e) {}
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

.header-title { font-size: 30rpx; font-weight: 700; color: var(--text-primary); }
.header-count { margin-left: 12rpx; font-size: 24rpx; color: var(--text-secondary); }

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

.nickname { font-size: 26rpx; font-weight: 600; color: var(--text-primary); }
.time { font-size: 22rpx; color: var(--text-placeholder); }

.text {
  display: block;
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.7;
  margin-bottom: 12rpx;
  word-break: break-all;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  transition: all 220ms ease;
}

.like-btn.liked { background: rgba(245, 108, 108, 0.08); }
.like-count { font-size: 22rpx; color: var(--text-secondary); }
.like-btn.liked .like-count { color: #f56c6c; font-weight: 600; }

.reply-btn {
  font-size: 22rpx;
  color: var(--text-secondary);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  transition: all 220ms ease;
}

.reply-btn:active {
  background: rgba(66, 185, 131, 0.08);
  color: #42b983;
}

/* ========= 二级回复区 ========= */
.reply-block {
  margin-top: 16rpx;
  padding: 16rpx 20rpx;
  background: var(--bg-soft);
  border-radius: 12rpx;
}

.reply-item {
  display: flex;
  padding: 12rpx 0;
}

.reply-item + .reply-item {
  border-top: 2rpx dashed var(--border-color);
}

.reply-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #ebeef5;
  flex-shrink: 0;
}

.reply-content { flex: 1; margin-left: 16rpx; min-width: 0; }

.reply-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 6rpx;
}

.reply-nickname {
  font-size: 24rpx;
  font-weight: 600;
  color: #339268;
}

.reply-to { display: inline-flex; align-items: center; }
.arrow { font-size: 22rpx; color: var(--text-placeholder); }
.target-name { font-size: 24rpx; color: #339268; }

.reply-text {
  font-size: 26rpx;
  color: var(--text-regular);
  line-height: 1.6;
  margin-bottom: 6rpx;
  word-break: break-all;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.reply-time {
  font-size: 20rpx;
  color: var(--text-placeholder);
}

.reply-action {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.more-replies {
  margin-top: 12rpx;
  padding: 8rpx 0;
  text-align: center;
  font-size: 22rpx;
  color: #42b983;
  font-weight: 500;
}

/* ========= 输入栏 ========= */
.input-area {
  background: var(--bg-card);
  border-top: 2rpx solid var(--border-color);
  padding-bottom: env(safe-area-inset-bottom);
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 32rpx;
  background: rgba(66, 185, 131, 0.06);
  border-bottom: 2rpx solid var(--border-color-light);
}

.hint-text {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.hint-name {
  color: #42b983;
  font-weight: 600;
}

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

.send-btn.active:active { transform: scale(0.94); }
</style>
