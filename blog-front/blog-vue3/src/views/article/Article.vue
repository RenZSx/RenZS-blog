<template>
  <div v-if="loading" class="article-loading">
    <v-progress-circular indeterminate color="primary" size="64" />
  </div>
  <div v-else>
    <ArticleHeader
      :article="article"
      :wordNum="wordNum"
      :readTime="readTime"
      :commentCount="commentCount"
    />
    <!-- 内容 -->
    <v-row class="article-container">
      <v-col cols="12" md="8" lg="8" class="article-main-column">
        <v-card class="article-wrapper article-shell">
          <section v-if="shouldShowAiSummary" class="article-ai-summary">
            <div class="article-ai-summary__avatar">
              <v-icon size="24">mdi-robot-excited-outline</v-icon>
            </div>
            <div class="article-ai-summary__body">
              <strong>小双</strong>
              <p class="article-ai-summary__text" :aria-label="article.aiSummary">
                <span>{{ typedAiSummary }}</span>
                <span
                  v-if="aiSummaryTyping"
                  class="article-ai-summary__cursor"
                  aria-hidden="true"
                />
              </p>
            </div>
          </section>
          <ArticleContent
            :content="article.articleContent"
            ref="articleContentRef"
          />
          <ArticleActions
            :article="article"
            :websiteConfig="websiteConfig"
            :articleHref="articleHref"
            :isLike="isLiked"
            :isCollected="isCollected"
            @like="handleLike"
            @collect="handleCollect"
          />
          <ArticleRecommend :article="article" />
          <!-- 分割线 -->
          <hr />
          <!-- 评论 -->
          <Comment :type="commentType" @get-comment-count="getCommentCount" />
        </v-card>
      </v-col>
      <!-- 侧边功能 -->
      <v-col cols="12" md="4" lg="3" class="article-side-column d-md-block d-none">
        <ArticleSidebar :newestArticleList="article.newestArticleList" />
      </v-col>
    </v-row>

    <transition name="lightbox-fade">
      <div
        v-if="previewVisible"
        class="article-lightbox"
        @click.self="closePreview"
      >
        <button
          type="button"
          class="lightbox-close"
          aria-label="关闭图片预览"
          @click="closePreview"
        >
          <v-icon size="20">mdi-close</v-icon>
        </button>

        <button
          v-if="imgList.length > 1"
          type="button"
          class="lightbox-nav lightbox-nav-prev"
          aria-label="上一张"
          @click.stop="showPrevImage"
        >
          <v-icon size="24">mdi-chevron-left</v-icon>
        </button>

        <div class="lightbox-body">
          <img
            v-if="currentPreviewImage"
            :src="currentPreviewImage"
            alt="文章图片预览"
            class="lightbox-image"
          />
          <div v-if="imgList.length > 1" class="lightbox-counter">
            {{ previewIndex + 1 }} / {{ imgList.length }}
          </div>
        </div>

        <button
          v-if="imgList.length > 1"
          type="button"
          class="lightbox-nav lightbox-nav-next"
          aria-label="下一张"
          @click.stop="showNextImage"
        >
          <v-icon size="24">mdi-chevron-right</v-icon>
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import Clipboard from 'clipboard'
import tocbot from 'tocbot'
import ArticleHeader from './components/ArticleHeader.vue'
import ArticleContent from './components/ArticleContent.vue'
import ArticleActions from './components/ArticleActions.vue'
import ArticleRecommend from './components/ArticleRecommend.vue'
import ArticleSidebar from './components/ArticleSidebar.vue'
import Comment from '@/components/Comment.vue'
import { fetchArticle, sendArticleLike } from './services/articleService'
import markdownToHtml from '@/utils/markdown'
import { useCollectStore } from '@/stores/collect'
import { useHistoryStore } from '@/stores/history'
import { useUserStore } from '@/stores/user'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useToast } from '@/composables/useToast'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'
import {
  computeHistoryProgress,
  resolveHistoryProgressReportAction
} from './utils/historyProgress'

const route = useRoute()
const collectStore = useCollectStore()
const historyStore = useHistoryStore()
const userStore = useUserStore()
const blogInfoStore = useBlogInfoStore()
const HISTORY_PROGRESS_THRESHOLD_PERCENT = 10
const HISTORY_REPORT_INTERVAL_MS = 8000

// Refs
const articleContentRef = ref<InstanceType<typeof ArticleContent> | null>(null)
const clipboard = ref<Clipboard | null>(null)
const imgList = ref<string[]>([])
const previewVisible = ref(false)
const previewIndex = ref(0)
const highestHistoryProgress = ref(0)
const lastReportedHistoryProgress = ref(0)
const lastReportedHistoryAt = ref(0)
const lastReservedHistoryProgress = ref(0)
const lastReservedHistoryAt = ref(0)
const historyReportInFlight = ref(false)
const queuedForcedHistoryProgress = ref(0)
const typedAiSummary = ref('')
const aiSummaryTyping = ref(false)
const aiSummaryTypingTimer = ref<number | null>(null)

// State
const loading = ref(true)
const article = ref<any>({
  articleCover: '',
  articleTitle: '',
  nextArticle: { id: 0, articleCover: '' },
  lastArticle: { id: 0, articleCover: '' },
  recommendArticleList: [],
  newestArticleList: []
})
const wordNum = ref(0)
const readTime = ref('')
const commentType = 1
const articleHref = window.location.href
const commentCount = ref(0)

// Computed
const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig || {})
const isLiked = computed(() => {
  const articleLikeSet = userStore.articleLikeSet
  return articleLikeSet.includes(article.value.id) ? 'like-btn-active' : 'like-btn'
})
const isCollected = computed(() => collectStore.isCollected(article.value.id))
const currentPreviewImage = computed(() => imgList.value[previewIndex.value] || '')
const shouldShowAiSummary = computed(() => {
  return Boolean(article.value.aiSummary && article.value.aiSummaryStatus === 2)
})

// Methods
async function getArticle() {
  loading.value = true
  imgList.value = []
  clearAiSummaryTypingTimer()
  typedAiSummary.value = ''
  aiSummaryTyping.value = false
  try {
    const { data } = await fetchArticle(route.path)
    document.title = data.data.articleTitle
    article.value = data.data
    article.value.articleContent = markdownToHtml(article.value.articleContent)
    startAiSummaryTyping()
    loading.value = false

    await nextTick()

    // 统计文章字数
    wordNum.value = deleteHTMLTag(article.value.articleContent).length
    // 计算阅读时间
    readTime.value = Math.round(wordNum.value / 400) + '分钟'

    // 添加代码复制功能
    clipboard.value = new Clipboard('.copy-btn')
    clipboard.value.on('success', () => {
      useToast({ type: 'success', message: '复制成功' })
    })

    // 添加文章生成目录功能
    const articleElement = articleContentRef.value?.getArticleElement()
    if (articleElement) {
      const nodes = articleElement.children
      for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i] as HTMLElement
        const reg = /^H[1-4]{1}$/
        if (reg.exec(node.tagName)) {
          node.id = String(i)
        }
      }
    }

    tocbot.destroy()
    tocbot.init({
      tocSelector: '#toc',
      contentSelector: '.article-content',
      headingSelector: 'h1, h2, h3, h4',
      hasInnerContainers: true,
      onClick: (e: Event) => {
        e.preventDefault()
      }
    })

    // 添加图片预览功能
    if (articleElement) {
      const imgElements = articleElement.getElementsByTagName('img')
      for (let i = 0; i < imgElements.length; i++) {
        imgList.value.push(imgElements[i].src)
        imgElements[i].addEventListener('click', (e: Event) => {
          const target = e.target as HTMLImageElement
          previewImg(target.currentSrc)
        })
      }
    }

  } catch (error) {
    console.error('获取文章失败:', error)
    loading.value = false
  }
}

async function handleLike() {
  // 判断登录
  if (!userStore.userId) {
    openLoginRequiredPrompt({ redirect: route.fullPath })
    return
  }

  try {
    const { data } = await sendArticleLike(article.value.id)
    if (data.flag) {
      const articleLikeSet = userStore.articleLikeSet
      if (articleLikeSet.includes(article.value.id)) {
        article.value.likeCount -= 1
      } else {
        article.value.likeCount += 1
      }
      userStore.toggleArticleLike(article.value.id)
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

async function handleCollect() {
  if (!userStore.userId) {
    openLoginRequiredPrompt({ redirect: route.fullPath })
    return
  }

  try {
    if (collectStore.isCollected(article.value.id)) {
      const result = await collectStore.cancelCollectArticle(article.value.id)
      if (result === 'success') {
        useToast({ type: 'success', message: '已取消收藏' })
      }
      return
    }

    const result = await collectStore.collectArticle(article.value.id)
    if (result === 'success') {
      useToast({ type: 'success', message: '收藏成功' })
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

function previewImg(img: string) {
  const index = imgList.value.indexOf(img)
  previewIndex.value = index >= 0 ? index : 0
  previewVisible.value = true
  document.body.style.overflow = 'hidden'
}

function closePreview() {
  previewVisible.value = false
  document.body.style.overflow = ''
}

function showPrevImage() {
  if (!imgList.value.length) return
  previewIndex.value =
    (previewIndex.value - 1 + imgList.value.length) % imgList.value.length
}

function showNextImage() {
  if (!imgList.value.length) return
  previewIndex.value = (previewIndex.value + 1) % imgList.value.length
}

function clearAiSummaryTypingTimer() {
  if (aiSummaryTypingTimer.value !== null) {
    window.clearInterval(aiSummaryTypingTimer.value)
    aiSummaryTypingTimer.value = null
  }
}

function startAiSummaryTyping() {
  clearAiSummaryTypingTimer()
  typedAiSummary.value = ''
  aiSummaryTyping.value = false

  if (!shouldShowAiSummary.value) {
    return
  }

  // 使用 Array.from 逐字符拆分，避免中文和部分特殊字符被截断。
  const summaryChars = Array.from(article.value.aiSummary)
  let currentIndex = 0
  aiSummaryTyping.value = true
  aiSummaryTypingTimer.value = window.setInterval(() => {
    typedAiSummary.value += summaryChars[currentIndex]
    currentIndex += 1

    if (currentIndex >= summaryChars.length) {
      clearAiSummaryTypingTimer()
      aiSummaryTyping.value = false
    }
  }, 45)
}

function handlePreviewKeydown(event: KeyboardEvent) {
  if (!previewVisible.value) return

  if (event.key === 'Escape') {
    closePreview()
  } else if (event.key === 'ArrowLeft') {
    showPrevImage()
  } else if (event.key === 'ArrowRight') {
    showNextImage()
  }
}

function deleteHTMLTag(content: string) {
  return content
    .replace(/<\/?[^>]*>/g, '')
    .replace(/[|]*\n/, '')
    .replace(/&npsp;/gi, '')
}

function getCommentCount(count: number) {
  commentCount.value = count
}

function getCurrentHistoryProgress() {
  const articleElement = articleContentRef.value?.getArticleElement()
  if (!articleElement) {
    return 0
  }

  const rect = articleElement.getBoundingClientRect()
  return computeHistoryProgress({
    articleTop: rect.top + window.scrollY,
    articleHeight: Math.max(articleElement.scrollHeight, articleElement.clientHeight),
    scrollY: window.scrollY,
    viewportHeight: window.innerHeight
  })
}

async function reportArticleHistoryProgress(force = false) {
  if (!userStore.isLoggedIn) {
    return
  }

  const articleId = Number(article.value.id)
  if (!Number.isFinite(articleId) || articleId <= 0) {
    return
  }

  const progressPercent = Math.max(
    highestHistoryProgress.value,
    getCurrentHistoryProgress()
  )
  highestHistoryProgress.value = progressPercent

  const now = Date.now()
  const action = resolveHistoryProgressReportAction({
    progressPercent,
    lastReportedProgressPercent: lastReportedHistoryProgress.value,
    now,
    lastReportedAt: lastReportedHistoryAt.value,
    lastReservedProgressPercent: lastReservedHistoryProgress.value,
    lastReservedAt: lastReservedHistoryAt.value,
    progressThresholdPercent: HISTORY_PROGRESS_THRESHOLD_PERCENT,
    reportIntervalMs: HISTORY_REPORT_INTERVAL_MS,
    isReporting: historyReportInFlight.value,
    force
  })

  if (action === 'skip') {
    return
  }

  if (action === 'queue') {
    queuedForcedHistoryProgress.value = Math.max(
      queuedForcedHistoryProgress.value,
      progressPercent
    )
    return
  }

  lastReservedHistoryProgress.value = progressPercent
  lastReservedHistoryAt.value = now
  historyReportInFlight.value = true

  try {
    const result = await historyStore.reportHistoryProgress(articleId, progressPercent)
    if (result === 'success' || result === 'noop') {
      lastReportedHistoryProgress.value = progressPercent
      lastReportedHistoryAt.value = now
    } else {
      lastReservedHistoryProgress.value = lastReportedHistoryProgress.value
      lastReservedHistoryAt.value = lastReportedHistoryAt.value
    }
  } catch (error) {
    lastReservedHistoryProgress.value = lastReportedHistoryProgress.value
    lastReservedHistoryAt.value = lastReportedHistoryAt.value
    console.error('上报阅读进度失败:', error)
  } finally {
    historyReportInFlight.value = false

    if (queuedForcedHistoryProgress.value > 0) {
      const queuedProgress = queuedForcedHistoryProgress.value
      queuedForcedHistoryProgress.value = 0
      highestHistoryProgress.value = Math.max(
        highestHistoryProgress.value,
        queuedProgress
      )
      await reportArticleHistoryProgress(true)
    }
  }
}

function handleArticleScroll() {
  void reportArticleHistoryProgress()
}

// Lifecycle
onMounted(() => {
  getArticle()
  if (userStore.isLoggedIn) {
    collectStore.initialize().catch(() => {})
  }
  window.addEventListener('keydown', handlePreviewKeydown)
  window.addEventListener('scroll', handleArticleScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handlePreviewKeydown)
  window.removeEventListener('scroll', handleArticleScroll)
  void reportArticleHistoryProgress(true)
  if (clipboard.value) {
    clipboard.value.destroy()
  }
  clearAiSummaryTypingTimer()
  document.body.style.overflow = ''
  tocbot.destroy()
})
</script>

<style scoped>
.article-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.lightbox-fade-enter-active,
.lightbox-fade-leave-active {
  transition: opacity 0.22s ease;
}

.lightbox-fade-enter-from,
.lightbox-fade-leave-to {
  opacity: 0;
}

.article-lightbox {
  position: fixed;
  inset: 0;
  z-index: 1300;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(8, 15, 29, 0.88);
  backdrop-filter: blur(6px);
}

.lightbox-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  max-width: min(92vw, 1200px);
}

.lightbox-image {
  display: block;
  max-width: 100%;
  max-height: calc(100vh - 120px);
  border-radius: 18px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
}

.lightbox-counter {
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.lightbox-close,
.lightbox-nav {
  position: absolute;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
}

.lightbox-close {
  top: 20px;
  right: 20px;
}

.lightbox-nav {
  top: 50%;
  transform: translateY(-50%);
}

.lightbox-nav-prev {
  left: 24px;
}

.lightbox-nav-next {
  right: 24px;
}

.article-container {
  position: relative;
  z-index: 2;
  display: grid !important;
  grid-template-columns: minmax(220px, 1fr) minmax(0, 1000px) minmax(300px, 1fr);
  column-gap: 36px;
  max-width: 1720px;
  margin: 326px auto 56px !important;
  padding: 0 28px;
  align-items: flex-start;
  justify-content: center;
}

.article-main-column {
  grid-column: 2;
  width: 100%;
  max-width: 1000px;
  padding-right: 0;
  padding-left: 0;
}

.article-side-column {
  grid-column: 3;
  width: min(100%, 340px);
  max-width: 340px;
  justify-self: end;
  padding-left: 0;
  align-self: stretch;
}

.article-shell {
  overflow: hidden;
  margin-top: -325px;
  border: 0;
  border-radius: var(--card-radius-lg) !important;
  background: var(--bg-primary);
  box-shadow: none;
}

.article-shell:hover {
  box-shadow: none;
}

.article-shell:before {
  display: none;
}

.article-ai-summary {
  display: flex;
  gap: 14px;
  box-sizing: border-box;
  width: min(calc(100% - 108px), 860px);
  margin: 24px auto 14px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.05);
}

.article-ai-summary__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #2563eb;
  background: #ffffff;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.12);
}

.article-ai-summary__body {
  min-width: 0;
}

.article-ai-summary__body strong {
  display: block;
  margin-bottom: 8px;
  color: #1f2d3d;
  font-size: 15px;
  font-weight: 800;
}

.article-ai-summary__body p {
  margin: 0;
  color: #34445a;
  font-size: 15px;
  line-height: 1.8;
}

.article-ai-summary__text {
  min-height: 1.8em;
}

.article-ai-summary__cursor {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  margin-left: 3px;
  vertical-align: -0.16em;
  border-radius: 999px;
  background: #2563eb;
  animation: ai-summary-cursor 0.86s steps(2, start) infinite;
}

@keyframes ai-summary-cursor {
  0%,
  45% {
    opacity: 1;
  }

  46%,
  100% {
    opacity: 0;
  }
}

.article-wrapper :deep(.v-image__image),
.article-wrapper :deep(.v-image__placeholder) {
  border-radius: inherit;
}

hr {
  position: relative;
  margin: 46px auto 8px;
  border: none;
  width: calc(100% - 84px);
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(73, 177, 245, 0),
    rgba(73, 177, 245, 0.5),
    rgba(73, 177, 245, 0)
  );
}

@media (max-width: 759px) {
  .article-container {
    display: block !important;
    margin: 130px auto 20px !important;
    padding: 0 12px;
  }

  .article-main-column {
    max-width: none;
    padding-right: 12px;
    padding-left: 12px;
  }

  .article-lightbox {
    padding: 14px;
  }

  .lightbox-nav {
    width: 40px;
    height: 40px;
  }

  .lightbox-nav-prev {
    left: 10px;
  }

  .lightbox-nav-next {
    right: 10px;
  }

  .article-shell {
    margin-top: -125px;
    border-radius: 18px !important;
  }

  .article-ai-summary {
    width: calc(100% - 36px);
    margin: 16px auto 8px;
    padding: 15px 16px;
  }

  hr {
    width: calc(100% - 36px);
  }
}
</style>

<style>
.article-shell .article-markdown {
  color: #243042;
  font-size: 17px;
  line-height: 1.95;
}

.article-shell .article-markdown > :first-child {
  margin-top: 0 !important;
}

.article-shell .article-markdown > :last-child {
  margin-bottom: 0 !important;
}

.article-shell .article-markdown h1,
.article-shell .article-markdown h2,
.article-shell .article-markdown h3,
.article-shell .article-markdown h4,
.article-shell .article-markdown h5,
.article-shell .article-markdown h6 {
  color: #152033;
  font-weight: 700;
  letter-spacing: 0.01em;
  line-height: 1.45;
}

.article-shell .article-markdown h1 {
  font-size: 2.05em;
}

.article-shell .article-markdown h2 {
  margin-top: 2.4em;
  padding-bottom: 0.7rem;
  font-size: 1.65em;
  border-bottom: 1px solid rgba(73, 177, 245, 0.18);
}

.article-shell .article-markdown h3 {
  margin-top: 2em;
  font-size: 1.35em;
}

.article-shell .article-markdown p,
.article-shell .article-markdown ul,
.article-shell .article-markdown ol,
.article-shell .article-markdown blockquote,
.article-shell .article-markdown pre,
.article-shell .article-markdown table {
  margin-top: 0;
  margin-bottom: 0.5rem;
}

.article-shell .article-markdown ul,
.article-shell .article-markdown ol {
  padding-left: 2.5rem !important;
}

.article-shell .article-markdown > p:first-of-type {
  margin-bottom: 1.6rem;
  color: #314156;
  font-size: 1.06em;
}

.article-shell .article-markdown li + li {
  margin-top: 0.4rem;
}

.article-shell .article-markdown li::marker {
  color: #49b1f5;
}

.article-shell .article-markdown blockquote {
  position: relative;
  padding: 1rem 1.2rem;
  border-left: 4px solid #49b1f5;
  border-radius: 0 16px 16px 0;
  background:
    linear-gradient(90deg, rgba(73, 177, 245, 0.14), rgba(73, 177, 245, 0.04)),
    rgba(255, 255, 255, 0.52);
  color: #415168;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.58);
  overflow: hidden;
}

.article-shell .article-markdown blockquote p:last-child {
  margin-bottom: 0;
}

.article-shell .article-markdown a {
  color: #2b8bd1;
  text-decoration-thickness: 1px;
  text-underline-offset: 3px;
  transition: color 0.25s ease, opacity 0.25s ease;
}

.article-shell .article-markdown a:hover {
  color: #1e6ea7;
}

.article-shell .article-markdown img {
  display: block;
  max-width: 100%;
  margin: 1.75rem auto;
  border-radius: 18px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.14);
  cursor: zoom-in;
}

.article-shell .article-markdown :not(pre) > code {
  padding: 0.16rem 0.46rem;
  border: 1px solid rgba(73, 177, 245, 0.2);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(245, 251, 255, 0.92), rgba(230, 244, 255, 0.76));
  color: #176fa9;
  font-family: "JetBrains Mono", "Fira Code", Consolas, "Liberation Mono", monospace;
  font-size: 0.92em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.76);
}

.article-shell .article-markdown pre code {
  padding: 0;
  background: transparent;
  color: inherit;
}

.article-shell .article-markdown pre {
  margin: 0;
  padding: 0;
  background: transparent;
}

.article-shell .article-markdown table {
  display: table;
  width: 100%;
  min-width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 0;
  border-collapse: collapse;
  border-spacing: 0;
  background: transparent;
  box-shadow: none;
}

.article-shell .article-markdown table thead th {
  background: transparent;
  color: inherit;
  font-weight: 700;
}

.article-shell .article-markdown table tbody tr:nth-child(even) {
  background: transparent;
}

.article-shell .article-markdown table tbody tr {
  background: transparent;
}

.article-shell .article-markdown table tbody tr:hover {
  background: transparent;
}

.article-shell .article-markdown table th,
.article-shell .article-markdown table td {
  padding: 0.9rem 1rem;
  border: 1px solid var(--border-color);
  white-space: nowrap;
}

.article-shell .article-markdown table tr:last-child td {
  border-bottom: 1px solid var(--border-color);
}

.article-shell .article-markdown hr {
  margin: 2rem 0;
  border: none;
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(73, 177, 245, 0),
    rgba(73, 177, 245, 0.45),
    rgba(73, 177, 245, 0)
  );
}

.article-side-card .article-toc {
  max-height: 420px;
  overflow: auto;
  padding: 12px 16px 18px;
  scroll-behavior: smooth;
}

.article-side-card .article-toc::-webkit-scrollbar {
  width: 6px;
}

.article-side-card .article-toc::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.28);
}

.article-side-card .toc-list-item {
  margin: 0;
  line-height: 1.9;
}

.article-side-card .toc-list {
  overflow: hidden;
  transform-origin: top;
  will-change: max-height, opacity, transform;
  transition: max-height 0.46s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.34s ease, transform 0.42s cubic-bezier(0.22, 1, 0.36, 1);
}

.article-side-card .is-collapsible {
  max-height: 960px;
  opacity: 1;
  transform: translateY(0) scaleY(1);
}

.article-side-card .is-collapsed {
  max-height: 0;
  opacity: 0;
  transform: translateY(-8px) scaleY(0.98);
  pointer-events: none;
}

.article-side-card .toc-list .toc-list {
  padding-top: 2px;
  padding-bottom: 4px;
}

.article-side-card .toc-list .toc-list .toc-link {
  opacity: 0.92;
  transform: translateX(0);
  transition: background 0.25s ease, color 0.25s ease, opacity 0.32s ease,
    transform 0.32s cubic-bezier(0.22, 1, 0.36, 1);
}

.article-side-card .is-collapsed .toc-link {
  opacity: 0;
  transform: translateX(-6px);
}

.article-side-card .toc-link {
  border-left: 3px solid transparent;
  border-radius: 0 10px 10px 0;
  padding: 6px 10px;
  color: #516173 !important;
  transition: all 0.25s ease;
}

.article-side-card .toc-link:hover {
  background: rgba(73, 177, 245, 0.08);
  color: #1f76b1 !important;
}

.article-side-card .is-active-link {
  border-left-color: #49b1f5;
  background: rgba(73, 177, 245, 0.12);
  color: #1976b5 !important;
}

@media (max-width: 759px) {
  .article-shell .article-markdown {
    font-size: 15px;
    line-height: 1.85;
  }

  .article-shell .article-markdown h1 {
    font-size: 1.75em;
  }

  .article-shell .article-markdown h2 {
    font-size: 1.4em;
  }

  .article-shell .article-markdown h3 {
    font-size: 1.2em;
  }

  .article-shell .article-markdown table {
    border-radius: 12px;
  }
}
</style>

<style lang="scss">
// Code block wrapper — keeps header + pre visually connected
.article-shell .code-block {
  margin: 0 0 0.5rem 0;
  border: 1px solid rgba(154, 169, 196, 0.16);
  border-radius: 10px;
  background: #0f141c;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  color-scheme: dark;
}

.article-shell .code-block pre {
  background: linear-gradient(180deg, #121720 0%, #0f141c 100%) !important;
  color: rgba(248, 250, 255, 0.96) !important;
}

.article-shell .code-block pre code {
  background: transparent !important;
  color: inherit !important;
}

// Step 1: Code block container - macOS dark style
.article-shell pre.hljs {
  position: relative;
  margin: 0 !important;
  padding: 0 !important;
  border: none;
  border-radius: 0 !important;
  background: linear-gradient(180deg, #121720 0%, #0f141c 100%) !important;
  color: rgba(248, 250, 255, 0.96) !important;
  font-size: 14px !important;
  line-height: 1.6 !important;
  overflow: hidden !important;
}

// Step 3: macOS title bar
.article-shell .code-header {
  padding: 12px 16px;
  background: linear-gradient(180deg, #121720 0%, #0f141c 100%);
  border-radius: 0;
  border-bottom: 1px solid rgba(154, 169, 196, 0.16);
  display: flex;
  align-items: center;
  gap: 8px;
}

.article-shell .code-header .window-buttons {
  display: flex;
  gap: 8px;
}

.article-shell .code-header .window-button {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.article-shell .code-header .window-button.red {
  background: #ff5f57;
}

.article-shell .code-header .window-button.yellow {
  background: #febc2e;
}

.article-shell .code-header .window-button.green {
  background: #28c840;
}

.article-shell .code-header .filename {
  flex: 1;
  text-align: center;
  color: #999;
  font-size: 13px;
}

// Step 4: Code content area
.article-shell pre.hljs code {
  display: block !important;
  margin: 0 !important;
  padding: 20px 20px 12px 56px !important;
  color: inherit !important;
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, 'Liberation Mono', monospace !important;
  font-size: 14px !important;
  line-height: 1.6 !important;
  overflow-x: auto !important;
  &::-webkit-scrollbar {
    z-index: 11;
    width: 6px;
  }
  &::-webkit-scrollbar:horizontal {
    height: 6px;
  }
  &::-webkit-scrollbar-thumb {
    border-radius: 5px;
    width: 6px;
    background: rgba(118, 151, 196, 0.72);
  }
  &::-webkit-scrollbar-corner,
  &::-webkit-scrollbar-track {
    background: #1a1a1a;
  }
  &::-webkit-scrollbar-track-piece {
    background: #1a1a1a;
    width: 6px;
  }
}

// Step 5: Line numbers
.article-shell pre.hljs .line-numbers-rows {
  position: absolute;
  pointer-events: none;
  top: 20px;
  bottom: 12px;
  left: 0;
  font-size: 100%;
  width: 44px;
  text-align: center;
  letter-spacing: -1px;
  border-right: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(8, 13, 23, 0.68);
  user-select: none;
  counter-reset: linenumber;
}

.article-shell pre.hljs .line-numbers-rows span {
  pointer-events: none;
  display: block;
  counter-increment: linenumber;
}

.article-shell pre.hljs .line-numbers-rows span::before {
  content: counter(linenumber);
  color: rgba(203, 213, 225, 0.58);
  display: block;
  text-align: center;
}

// Step 6: Syntax highlighting colors
.article-shell pre.hljs .hljs-keyword,
.article-shell pre.hljs .hljs-selector-tag,
.article-shell pre.hljs .hljs-built_in,
.article-shell pre.hljs .hljs-name,
.article-shell pre.hljs .hljs-tag {
  color: #c084fc !important;
}

.article-shell pre.hljs .hljs-string,
.article-shell pre.hljs .hljs-title,
.article-shell pre.hljs .hljs-section,
.article-shell pre.hljs .hljs-attribute,
.article-shell pre.hljs .hljs-literal,
.article-shell pre.hljs .hljs-template-tag,
.article-shell pre.hljs .hljs-template-variable,
.article-shell pre.hljs .hljs-type {
  color: #86efac !important;
}

.article-shell pre.hljs .hljs-symbol,
.article-shell pre.hljs .hljs-bullet,
.article-shell pre.hljs .hljs-addition,
.article-shell pre.hljs .hljs-variable,
.article-shell pre.hljs .hljs-link {
  color: #67e8f9 !important;
}

.article-shell pre.hljs .hljs-comment,
.article-shell pre.hljs .hljs-quote,
.article-shell pre.hljs .hljs-deletion,
.article-shell pre.hljs .hljs-meta {
  color: #94a3b8 !important;
  font-style: italic;
}

.article-shell pre.hljs .hljs-number,
.article-shell pre.hljs .hljs-regexp {
  color: #fbbf24 !important;
}

.article-shell pre.hljs .hljs-class .hljs-title,
.article-shell pre.hljs .hljs-function .hljs-title {
  color: #60a5fa !important;
}

// Step 7: Copy button
.article-shell .code-header .copy-btn {
  border: 1px solid rgba(148, 163, 184, 0.24);
  color: #dbeafe;
  background: rgba(30, 41, 59, 0.92);
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  width: 32px;
  height: 24px;
  outline: none;
  cursor: pointer;
}
</style>
