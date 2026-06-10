<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view v-if="loading" class="loading-state">
      <view class="skeleton skel-cover" />
      <view class="skel-padding">
        <view class="skeleton skel-title" />
        <view class="skeleton skel-meta" />
        <view class="skeleton skel-line" />
        <view class="skeleton skel-line short" />
      </view>
    </view>

    <view v-else-if="article" class="article">
      <view v-if="article.articleCover" class="cover-area">
        <image :src="article.articleCover" class="cover" mode="aspectFill" />
        <view class="cover-mask" />
      </view>

      <view class="header" :class="{ 'no-cover': !article.articleCover }">
        <view class="cat-tags" v-if="article.categoryName">
          <text class="cat">{{ article.categoryName }}</text>
        </view>
        <text class="title">{{ article.articleTitle }}</text>
        <view class="meta-row">
          <view class="author">
            <image class="avatar" :src="article.avatar || defaultAvatar" mode="aspectFill" />
            <view class="author-info">
              <text class="author-name">{{ article.nickname || '匿名' }}</text>
              <text class="author-date">{{ formatTime(article.createTime) }}</text>
            </view>
          </view>
          <view class="stats">
            <view class="stat-item">
              <bx-icon name="eye" :size="22" color="#909399" />
              <text class="stat-num">{{ article.viewsCount || article.viewCount || 0 }}</text>
            </view>
            <view class="stat-item">
              <bx-icon name="comment" :size="22" color="#909399" />
              <text class="stat-num">{{ article.commentCount || article.commentCounts || 0 }}</text>
            </view>
          </view>
        </view>

        <view class="tag-row" v-if="article.tagDTOList?.length">
          <text v-for="tag in article.tagDTOList" :key="tag.id" class="tag">
            #{{ tag.tagName }}
          </text>
        </view>
      </view>

      <view class="divider" />

      <view class="body">
        <rich-text :nodes="renderedHtml" />
      </view>

      <view class="footer-tip">- The End -</view>
      <view class="bottom-padding" />
    </view>

    <!-- 底部操作栏 -->
    <view v-if="article" class="action-bar">
      <view class="action-btn" :class="{ active: isLiked, popping: liking }" @click="handleLike">
        <bx-icon
          :name="isLiked ? 'heartFilled' : 'heart'"
          :size="44"
          :color="isLiked ? '#f56c6c' : '#909399'"
        />
        <text class="action-label" :class="{ active: isLiked }">{{ article.likeCount || 0 }}</text>
      </view>
      <view class="action-btn" @click="goComments">
        <bx-icon name="comment" :size="44" color="#909399" />
        <text class="action-label">{{ article.commentCount || article.commentCounts || 0 }}</text>
      </view>
      <view class="comment-cta" @click="goComments">
        <text>发表评论...</text>
        <bx-icon name="edit" :size="28" color="#909399" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getArticle, likeArticle } from '@/api/article'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'
import { markdownToHtml } from '@/utils/markdown'

const userStore = useUserStore()
const { isDark } = useThemeClass()
const article = ref(null)
const loading = ref(true)
const articleId = ref(null)
const liking = ref(false)
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

const renderedHtml = computed(() => {
  if (!article.value) return ''
  return markdownToHtml(article.value.articleContent || '')
})

const isLiked = computed(() => {
  if (!articleId.value) return false
  return userStore.isArticleLiked(articleId.value)
})

async function loadArticle() {
  loading.value = true
  try {
    const res = await getArticle(articleId.value)
    if (res.flag && res.data) {
      article.value = res.data
    } else {
      uni.showToast({ title: res.message || '文章不存在', icon: 'none' })
    }
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 800)
    return
  }
  try {
    liking.value = true
    setTimeout(() => { liking.value = false }, 360)
    const res = await likeArticle(articleId.value)
    if (res.flag) {
      userStore.toggleArticleLike(articleId.value)
      if (userStore.isArticleLiked(articleId.value)) {
        article.value.likeCount = (article.value.likeCount || 0) + 1
      } else {
        article.value.likeCount = Math.max(0, (article.value.likeCount || 0) - 1)
      }
    }
  } catch (e) {
    // 已在拦截器提示
  }
}

function goComments() {
  uni.navigateTo({ url: `/pages/article/comments?id=${articleId.value}` })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onLoad((query) => {
  articleId.value = Number(query.id)
  if (!articleId.value) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    return
  }
  loadArticle()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-card);
  padding-bottom: 160rpx;
}

.loading-state { background: var(--bg-card); }

.skel-cover { width: 100%; height: 400rpx; border-radius: 0; }
.skel-padding { padding: 32rpx 32rpx 0; }
.skel-title { height: 48rpx; width: 90%; margin-bottom: 24rpx; }
.skel-meta { height: 28rpx; width: 60%; margin-bottom: 32rpx; }
.skel-line { height: 28rpx; margin-bottom: 20rpx; }
.skel-line.short { width: 70%; }

.cover-area {
  position: relative;
  width: 100%;
  height: 400rpx;
  background: #ebeef5;
}

.cover { width: 100%; height: 100%; }

.cover-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(255, 255, 255, 0.9) 100%);
}

.theme-dark .cover-mask {
  background: linear-gradient(180deg, transparent 60%, rgba(29, 31, 35, 0.9) 100%);
}

.header { padding: 32rpx 32rpx 24rpx; }

.header.no-cover { padding-top: 48rpx; }

.cat-tags { margin-bottom: 20rpx; }

.cat {
  display: inline-block;
  padding: 6rpx 20rpx;
  background: rgba(66, 185, 131, 0.1);
  color: #339268;
  font-size: 22rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.35;
  letter-spacing: -1rpx;
  margin-bottom: 28rpx;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.author { display: flex; align-items: center; }

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #ebeef5;
  margin-right: 16rpx;
}

.author-info { display: flex; flex-direction: column; }

.author-name {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4rpx;
}

.author-date { font-size: 22rpx; color: var(--text-secondary); }

.stats { display: flex; gap: 20rpx; }

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
}

.stat-num { font-size: 22rpx; color: var(--text-secondary); }

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.tag {
  font-size: 22rpx;
  color: #339268;
  background: rgba(66, 185, 131, 0.08);
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
}

.divider {
  height: 2rpx;
  background: var(--border-color);
  margin: 0 32rpx;
}

/* ========= 正文(markdown 渲染) ========= */
.body {
  padding: 32rpx;
  font-size: 30rpx;
  line-height: 1.85;
  color: var(--text-primary);
  letter-spacing: 0.5rpx;
}

.body :deep(.md-h1) {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 1.4;
  margin: 48rpx 0 24rpx;
  letter-spacing: -1rpx;
  color: var(--text-primary);
}

.body :deep(.md-h2) {
  font-size: 34rpx;
  font-weight: 700;
  line-height: 1.4;
  margin: 40rpx 0 20rpx;
  padding-bottom: 12rpx;
  border-bottom: 2rpx solid var(--border-color);
  color: var(--text-primary);
}

.body :deep(.md-h3) {
  font-size: 30rpx;
  font-weight: 700;
  margin: 32rpx 0 16rpx;
  color: var(--text-primary);
}

.body :deep(.md-p) {
  margin: 16rpx 0;
  color: var(--text-regular);
}

.body :deep(.md-bold) {
  font-weight: 700;
  color: var(--text-primary);
}

.body :deep(.md-em) { font-style: italic; }
.body :deep(.md-del) { color: var(--text-secondary); text-decoration: line-through; }

.body :deep(.md-link) {
  color: #42b983;
  text-decoration: underline;
}

.body :deep(.md-inline-code) {
  background: var(--bg-soft);
  color: #d35400;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
  font-family: Consolas, Menlo, monospace;
  font-size: 26rpx;
}

.body :deep(.md-pre) {
  position: relative;
  background: #282c34;
  color: #abb2bf;
  padding: 32rpx 24rpx 24rpx;
  border-radius: 16rpx;
  overflow-x: auto;
  margin: 24rpx 0;
  font-family: Consolas, Menlo, monospace;
  font-size: 26rpx;
  line-height: 1.6;
}

.body :deep(.md-code) { color: #abb2bf; }

.body :deep(.md-code-lang) {
  position: absolute;
  top: 8rpx;
  right: 16rpx;
  font-size: 20rpx;
  color: #5c6370;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.body :deep(.md-quote) {
  border-left: 6rpx solid #42b983;
  padding: 16rpx 20rpx;
  background: rgba(66, 185, 131, 0.06);
  color: var(--text-regular);
  margin: 24rpx 0;
  border-radius: 0 12rpx 12rpx 0;
}

.body :deep(.md-img) {
  max-width: 100%;
  display: block;
  margin: 24rpx 0;
  border-radius: 16rpx;
}

.body :deep(.md-hr) {
  border: none;
  height: 2rpx;
  background: var(--border-color);
  margin: 48rpx 0;
}

.body :deep(.md-ul),
.body :deep(.md-ol) {
  margin: 16rpx 0;
  padding-left: 40rpx;
}

.body :deep(.md-li) {
  margin: 8rpx 0;
  color: var(--text-regular);
}

.footer-tip {
  text-align: center;
  color: var(--text-placeholder);
  font-size: 24rpx;
  letter-spacing: 8rpx;
  padding: 48rpx 0 24rpx;
}

.bottom-padding { height: 96rpx; }

/* ========= 底部操作栏 ========= */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  border-top: 2rpx solid var(--border-color);
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.04);
  gap: 16rpx;
}

.theme-dark .action-bar {
  background: rgba(29, 31, 35, 0.92);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 96rpx;
  height: 88rpx;
  border-radius: 20rpx;
  transition: all 220ms ease;
}

.action-btn:active { background: var(--bg-soft); }

.action-btn.popping {
  animation: pop 320ms cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-label {
  font-size: 20rpx;
  color: var(--text-secondary);
  margin-top: 2rpx;
}

.action-label.active {
  color: #f56c6c;
  font-weight: 600;
}

.comment-cta {
  flex: 1;
  height: 88rpx;
  background: var(--bg-soft);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
  font-size: 26rpx;
  color: var(--text-secondary);
  transition: all 220ms ease;
}

.comment-cta:active { background: var(--border-color); }
</style>
