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
        <mp-html
          :content="renderedHtml"
          :tag-style="mdTagStyle"
          selectable
        />
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
      <view class="action-btn" :class="{ active: isCollected, popping: collecting }" @click="handleCollect">
        <bx-icon
          :name="isCollected ? 'starFilled' : 'star'"
          :size="44"
          :color="isCollected ? '#e6a23c' : '#909399'"
        />
        <text class="action-label" :class="{ collected: isCollected }">{{ isCollected ? '已收藏' : '收藏' }}</text>
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
import { getArticle, likeArticle, collectArticle, uncollectArticle } from '@/api/article'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'
import { markdownToHtml, sanitizeRichHtml } from '@/utils/markdown'

const userStore = useUserStore()
const { isDark } = useThemeClass()
const article = ref(null)
const loading = ref(true)
const articleId = ref(null)
const liking = ref(false)
const collecting = ref(false)
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'

const renderedHtml = computed(() => {
  if (!article.value) return ''
  // 1. md → html(markdown 文章)
  // 2. 清洗内联 font-size / font-family / 旧式 <font> 标签,避免覆盖 tagStyle 字号
  //    (后端富文本编辑器写入的文章常含 <p style="font-size:24px">)
  return compactArticleHtml(sanitizeRichHtml(markdownToHtml(article.value.articleContent || '')))
})

/**
 * 给文章详情页的 mp-html 内容写入内联压缩字号。
 *
 * App 端 mp-html 对 scoped / :deep / rpx 的覆盖不稳定,标题和表格容易回退到
 * h1/table 默认字号,所以这里在最终 HTML 节点上写 px,只作用于详情页正文。
 */
function compactArticleHtml(html) {
  if (!html || typeof html !== 'string') return ''

  const compactRules = [
    ['md-h1', 'font-size:19.5px;line-height:1.35;margin:14px 0 9px;font-weight:700;'],
    ['md-h2', 'font-size:18px;line-height:1.35;margin:12px 0 7px;font-weight:700;'],
    ['md-h3', 'font-size:16.5px;line-height:1.35;margin:10px 0 6px;font-weight:700;'],
    ['md-p', 'font-size:13.5px;line-height:1.65;margin:6px 0;'],
    ['md-li', 'font-size:13.5px;line-height:1.65;margin:4px 0;'],
    ['md-ul', 'font-size:13.5px;line-height:1.65;margin:6px 0;padding-left:16px;'],
    ['md-ol', 'font-size:13.5px;line-height:1.65;margin:6px 0;padding-left:16px;'],
    ['md-th', 'font-size:13.5px;line-height:1.5;padding:5px 6px;'],
    ['md-td', 'font-size:13.5px;line-height:1.5;padding:5px 6px;'],
    ['md-table', 'font-size:13.5px;line-height:1.5;margin:10px 0;'],
    ['md-inline-code', 'font-size:13.5px;line-height:1.4;'],
    ['md-pre', 'font-size:13.5px;line-height:1.5;margin:10px 0;padding:9px;']
  ]

  return compactRules.reduce((nextHtml, [className, style]) => {
    return addStyleForClass(nextHtml, className, style)
  }, html)
}

function addStyleForClass(html, className, style) {
  const reg = new RegExp(`<([a-z0-9-]+)\\b([^>]*\\bclass=["'][^"']*\\b${className}\\b[^"']*["'][^>]*)>`, 'gi')

  return html.replace(reg, (match, tagName, attrs) => {
    if (/style\s*=\s*"/i.test(attrs)) {
      return `<${tagName}${attrs.replace(/style\s*=\s*"([^"]*)"/i, `style="${style}$1"`)}>`
    }
    if (/style\s*=\s*'/i.test(attrs)) {
      return `<${tagName}${attrs.replace(/style\s*=\s*'([^']*)'/i, `style='${style}$1`)}>`
    }
    return `<${tagName}${attrs} style="${style}">`
  })
}

// mp-html 标签样式覆盖(由于 mp-html 使用自身的 nodes,内联 var(--) 在部分平台会失效,
// 这里用具体颜色 + 注意暗色场景下文章详情整体仍是 var(--bg-card) 背景)
//
// 字号设计(手机 App 阅读流):
//   正文 13.5px → h3 16.5px → h2 18px → h1 19.5px,在上一版基础上放大 1.5 倍
//   line-height 1.4 防止移动端标题"上下吃距"
const mdTagStyle = {
  h1: 'font-size:19.5px;font-weight:700;line-height:1.35;margin:14px 0 9px;',
  h2: 'font-size:18px;font-weight:700;line-height:1.35;margin:12px 0 7px;padding-bottom:4px;border-bottom:1px solid #f0f2f5;',
  h3: 'font-size:16.5px;font-weight:700;line-height:1.35;margin:10px 0 6px;',
  h4: 'font-size:13.5px;font-weight:700;line-height:1.35;margin:7px 0 5px;',
  h5: 'font-size:13.5px;font-weight:700;line-height:1.35;margin:7px 0 5px;',
  h6: 'font-size:13.5px;font-weight:600;line-height:1.35;margin:7px 0 5px;',
  // 段落显式声明 13.5px,防富文本原文里 <p> 没 style 时浏览器走 16px 默认
  p: 'font-size:13.5px;margin:6px 0;line-height:1.65;',
  // 富文本常用容器,显式声明字号统一阅读流
  div: 'font-size:13.5px;line-height:1.65;',
  span: 'font-size:13.5px;',
  a: 'color:#42b983;',
  strong: 'font-weight:700;',
  em: 'font-style:italic;',
  // 行内代码和代码块略小于正文,避免等宽字体在移动端显得过大
  code: 'background:#000000;color:#e8e8e8;padding:1px 4px;border-radius:3px;font-family:Consolas,Menlo,monospace;font-size:13.5px;',
  pre: 'background:#000000;color:#e8e8e8;padding:9px;border-radius:6px;overflow-x:auto;font-family:Consolas,Menlo,monospace;font-size:13.5px;line-height:1.5;margin:10px 0;white-space:pre;box-shadow:0 2px 6px rgba(0,0,0,0.12);',
  blockquote: 'border-left:3px solid #42b983;padding:6px 8px;background:rgba(66,185,131,0.06);color:#606266;margin:8px 0;border-radius:0 6px 6px 0;',
  img: 'max-width:100%;display:block;margin:24rpx 0;border-radius:16rpx;',
  hr: 'border:none;height:1px;background:#f0f2f5;margin:14px 0;',
  ul: 'font-size:7px;line-height:1.6;margin:5px 0;padding-left:14px;',
  ol: 'font-size:7px;line-height:1.6;margin:5px 0;padding-left:14px;',
  li: 'font-size:7px;line-height:1.6;margin:3px 0;',
  // 表格(GFM):横向滚动 + 斑马纹
  table: 'border-collapse:collapse;width:100%;margin:10px 0;font-size:13.5px;line-height:1.5;display:table;',
  thead: 'background:#f5f7fa;',
  th: 'font-size:13.5px;line-height:1.5;padding:5px 6px;border:1px solid #ebeef5;font-weight:600;color:#303133;text-align:left;',
  td: 'font-size:13.5px;line-height:1.5;padding:5px 6px;border:1px solid #ebeef5;color:#606266;',
  tr: ''
}

const isLiked = computed(() => {
  if (!articleId.value) return false
  return userStore.isArticleLiked(articleId.value)
})

const isCollected = computed(() => {
  if (!articleId.value) return false
  return userStore.isArticleCollected(articleId.value)
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

/**
 * 收藏 / 取消收藏
 * 后端 POST 收藏、DELETE 取消,前端先决断当前状态,再调对应接口,最后乐观更新本地 collectSet
 */
async function handleCollect() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 800)
    return
  }
  if (collecting.value) return
  const wasCollected = isCollected.value
  collecting.value = true
  setTimeout(() => { collecting.value = false }, 360)
  try {
    const res = wasCollected
      ? await uncollectArticle(articleId.value)
      : await collectArticle(articleId.value)
    if (res.flag) {
      userStore.toggleArticleCollect(articleId.value)
      uni.showToast({
        title: wasCollected ? '已取消收藏' : '收藏成功',
        icon: 'none',
        duration: 1200
      })
    }
  } catch (e) {
    // 拦截器已处理
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
  font-size: 13.5px;
  line-height: 1.65;
  color: var(--text-primary);
  letter-spacing: 0;
}

/* ============================================================
 * mp-html 内部 class 字号覆盖
 * ============================================================
 * mp-html 把 <h1>~<h6> 等标签转成自定义 class (._h1 ._h2...),
 * 内置 font-size:2em/1.5em/1.17em 等相对值,在 750rpx 设计屏会被
 * 放大成 60-80px 级,移动端阅读体验极差。
 *
 * 用 :deep() 穿透 + 类选择器优先级,把这些写死的 em 值覆盖为
 * 与 .body 正文一致的 rpx 体系。!important 是为防 mp-html 后续
 * 版本升级把字号改为 inline style 覆盖类样式。
 */
.body :deep(._h1) {
  font-size: 19.5px !important;
  line-height: 1.35 !important;
  margin: 14px 0 9px !important;
}
.body :deep(._h2) {
  font-size: 18px !important;
  line-height: 1.35 !important;
  margin: 12px 0 7px !important;
}
.body :deep(._h3) {
  font-size: 16.5px !important;
  line-height: 1.35 !important;
  margin: 10px 0 6px !important;
}
.body :deep(._h4) {
  font-size: 13.5px !important;
  line-height: 1.35 !important;
  margin: 7px 0 5px !important;
}
.body :deep(._h5),
.body :deep(._h6) {
  font-size: 13.5px !important;
  line-height: 1.35 !important;
  margin: 7px 0 5px !important;
}
/* 富文本 ._big ._small 也压回正常 */
.body :deep(._big) {
  font-size: 16.5px !important;
}
.body :deep(._small) {
  font-size: 12px !important;
}

.body :deep(.md-h1) {
  font-size: 19.5px;
  font-weight: 700;
  line-height: 1.35;
  margin: 14px 0 9px;
  color: var(--text-primary);
}

.body :deep(.md-h2) {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  margin: 12px 0 7px;
  padding-bottom: 4px;
  border-bottom: 2rpx solid var(--border-color);
  color: var(--text-primary);
}

.body :deep(.md-h3) {
  font-size: 16.5px;
  font-weight: 700;
  line-height: 1.35;
  margin: 10px 0 6px;
  color: var(--text-primary);
}

.body :deep(.md-p) {
  margin: 6px 0;
  font-size: 13.5px;
  line-height: 1.65;
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
  padding: 1px 4px;
  border-radius: 3px;
  font-family: Consolas, Menlo, monospace;
  font-size: 13.5px;
}

.body :deep(.md-pre) {
  position: relative;
  background: #282c34;
  color: #abb2bf;
  padding: 9px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
  font-family: Consolas, Menlo, monospace;
  font-size: 13.5px;
  line-height: 1.5;
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

.action-label.collected {
  color: #e6a23c;
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
