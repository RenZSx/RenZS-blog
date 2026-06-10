<template>
  <main class="magazine-home">
    <div class="magazine-home__shell">
      <section class="magazine-home__main">
        <!-- Hero 精选文章 -->
        <RouterLink
          v-if="featuredArticle"
          :to="articlePath(featuredArticle)"
          class="featured-post"
        >
          <div class="featured-post__media">
            <img :src="articleCover(featuredArticle)" :alt="articleTitle(featuredArticle)" />
          </div>
          <div class="featured-post__body">
            <span class="post-chip">{{ articleCategory(featuredArticle) }}</span>
            <h1>{{ articleTitle(featuredArticle) }}</h1>
            <p>{{ articleSummary(featuredArticle) }}</p>
            <div class="post-meta">
              <span>{{ articleDate(featuredArticle) }}</span>
              <span aria-hidden="true">·</span>
              <span>{{ articleReadTime(featuredArticle) }}</span>
              <span aria-hidden="true">·</span>
              <span>{{ articleViews(featuredArticle) }} 阅读</span>
            </div>
          </div>
        </RouterLink>

        <!-- 分组标题 -->
        <div class="magazine-section-title">
          <span>专栏分组</span>
        </div>

        <!-- 分组卡片网格 -->
        <div v-if="sections.length" class="section-grid">
          <SectionCard
            v-for="(section, index) in sections"
            :key="section.id || section.sectionKey || index"
            :section="section"
            :color-index="index"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="magazine-empty">暂无专栏内容</div>
      </section>

      <!-- 侧边栏 -->
      <aside class="magazine-sidebar" aria-label="专栏侧边栏">
        <!-- 作者信息 -->
        <section class="side-panel author-panel">
          <img class="author-panel__avatar" :src="authorAvatar" :alt="authorName" />
          <h2>{{ authorName }}</h2>
          <p>{{ authorIntro }}</p>
          <div class="author-panel__stats">
            <div>
              <strong>{{ statValue(blogInfo.articleCount) }}</strong>
              <span>文章</span>
            </div>
            <div>
              <strong>{{ shortNum(blogInfo.viewsCount) }}</strong>
              <span>阅读</span>
            </div>
            <div>
              <strong>{{ statValue(blogInfo.tagCount) }}</strong>
              <span>关注</span>
            </div>
          </div>
        </section>

        <!-- 专栏导航 -->
        <section v-if="sections.length" class="side-panel">
          <h3>专栏导航</h3>
          <nav class="section-nav">
            <a
              v-for="(section, index) in sections"
              :key="section.id || index"
              role="link"
              tabindex="0"
              class="section-nav__item"
              @click="scrollToSection(index)"
              @keydown.enter="scrollToSection(index)"
            >
              <span class="section-nav__name">{{ section.sectionName }}</span>
              <span class="section-nav__badge">{{ section.articleList.length }}</span>
            </a>
          </nav>
        </section>

        <!-- 热门文章 -->
        <section v-if="hotArticles.length" class="side-panel">
          <h3>热门文章</h3>
          <ol class="hot-list">
            <li v-for="(article, index) in hotArticles" :key="article.id || index">
              <RouterLink :to="articlePath(article)">
                <span>{{ index + 1 }}</span>
                <strong>{{ articleTitle(article) }}</strong>
              </RouterLink>
            </li>
          </ol>
        </section>
      </aside>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchHomeArticleSections } from './services/homeService'
import { useBlogInfoStore, type BlogInfo, type WebsiteConfig } from '@/stores/blogInfo'
import { formatDate } from '@/utils/filters'
import {
  getArticleCategory,
  getArticlePath,
  getArticleReadTime,
  getArticleSummary,
  getArticleTitle,
  getArticleViews,
  getSectionHotArticles,
  normalizeSections,
  readApiList,
  shortNumber,
  type ArticleItem,
  type ArticleSection,
} from './homeColumnsContent'
import SectionCard from './components/SectionCard.vue'

const defaultCover = 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80'

const blogInfoStore = useBlogInfoStore()
const sections = ref<ArticleSection[]>([])

const blogInfo = computed<Partial<BlogInfo>>(() => {
  const value = blogInfoStore.blogInfo
  return value && typeof value === 'object' ? value : {}
})

const websiteConfig = computed<Partial<WebsiteConfig>>(() => {
  const config = blogInfo.value.websiteConfig
  return config && typeof config === 'object' ? config : {}
})

const authorName = computed(() => websiteConfig.value.websiteAuthor || '林晓')
const authorIntro = computed(
  () => websiteConfig.value.websiteIntro || '写字、读书、爬山。记录日常里那些细小的美好。'
)
const authorAvatar = computed(() => {
  return (
    websiteConfig.value.websiteAvatar ||
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80'
  )
})

const featuredArticle = computed(() => sections.value[0]?.articleList?.[0])
const hotArticles = computed(() => getSectionHotArticles(sections.value))

function articlePath(article: ArticleItem) {
  return getArticlePath(article)
}

function articleCover(article: ArticleItem) {
  return article.articleCover || defaultCover
}

function articleTitle(article: ArticleItem) {
  return getArticleTitle(article)
}

function articleCategory(article: ArticleItem) {
  return getArticleCategory(article)
}

function articleSummary(article: ArticleItem) {
  return getArticleSummary(article)
}

function articleDate(article: ArticleItem) {
  return article.createTime ? formatDate(article.createTime) : ''
}

function articleViews(article: ArticleItem) {
  return getArticleViews(article)
}

function articleReadTime(article: ArticleItem) {
  return getArticleReadTime(article)
}

function statValue(value: unknown) {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0
}

function shortNum(value: unknown) {
  return shortNumber(value)
}

function scrollToSection(index: number) {
  const el = document.querySelector(`[data-section-index="${index}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

async function fetchData() {
  const [sectionRes] = await Promise.allSettled([
    fetchHomeArticleSections()
  ])

  sections.value =
    sectionRes.status === 'fulfilled'
      ? normalizeSections(readApiList(sectionRes.value))
      : []
}

onMounted(() => {
  fetchData().catch((error) => {
    console.error('加载专栏页面失败:', error)
  })
})
</script>

<style scoped>
.magazine-home {
  min-height: 100vh;
  padding: 96px clamp(28px, 5vw, 72px) 54px;
  color: #1f2933;
  background: #f7f7f5;
}

.magazine-home__shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: clamp(34px, 4vw, 56px);
  width: min(1320px, 100%);
  margin: 0 auto;
}

/* Section title */
.magazine-section-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 32px 0 22px;
}

.magazine-section-title::after {
  flex: 1;
  height: 1px;
  background: #e5e0dc;
  content: "";
}

.magazine-section-title span {
  color: #8d8a86;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.35em;
  text-transform: uppercase;
}

/* Featured post */
.featured-post {
  display: grid;
  grid-template-columns: minmax(360px, 0.82fr) minmax(0, 1.08fr);
  min-height: 300px;
  overflow: hidden;
  border: 1px solid #e6e1dc;
  border-radius: 10px;
  background: #fff;
  color: inherit;
  text-decoration: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.featured-post:hover {
  border-color: #d8d0c8;
  box-shadow: 0 14px 34px rgba(31, 41, 51, 0.08);
  transform: translateY(-2px);
}

.featured-post__media {
  min-height: 300px;
  background: #e9efee;
}

.featured-post__media img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.featured-post__body {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  padding: clamp(30px, 3vw, 42px);
}

.post-chip {
  width: max-content;
  max-width: 100%;
  padding: 5px 10px;
  border-radius: 6px;
  color: #0b6f76;
  font-size: 12px;
  font-weight: 700;
  background: #dff4f3;
}

.featured-post h1 {
  margin: 20px 0 12px;
  color: #111827;
  font-size: clamp(28px, 2.2vw, 34px);
  font-weight: 700;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.featured-post p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #6b625d;
  font-size: 14px;
  line-height: 1.8;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
  color: #918d89;
  font-size: 13px;
}

/* Section grid */
.section-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(20px, 2vw, 28px);
}

/* Sidebar */
.magazine-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 84px;
  align-self: start;
}

.side-panel {
  padding: 20px;
  border: 1px solid #e6e1dc;
  border-radius: 10px;
  background: #fff;
}

.author-panel {
  padding-top: 22px;
  text-align: center;
}

.author-panel__avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.author-panel h2 {
  margin: 14px 0 12px;
  color: #111827;
  font-size: 18px;
}

.author-panel p {
  margin: 0;
  color: #68645f;
  font-size: 13px;
  line-height: 1.8;
  text-align: left;
}

.author-panel__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 18px;
}

.author-panel__stats strong,
.author-panel__stats span {
  display: block;
}

.author-panel__stats strong {
  color: #111827;
  font-size: 20px;
}

.author-panel__stats span {
  margin-top: 4px;
  color: #9b9690;
  font-size: 11px;
}

.side-panel h3 {
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ece7e2;
  color: #111827;
  font-size: 16px;
}

/* Section nav */
.section-nav {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.section-nav__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #ece7e2;
  color: #3d4a5d;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

.section-nav__item:last-child {
  border-bottom: none;
}

.section-nav__item:hover {
  color: #0b6f76;
}

.section-nav__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-nav__badge {
  flex-shrink: 0;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 11px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 22px;
  text-align: center;
  background: #16858d;
}

/* Hot list */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.hot-list li + li {
  border-top: 1px solid #ece7e2;
}

.hot-list a {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  padding: 10px 0;
  color: #1f2933;
  text-decoration: none;
}

.hot-list span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  background: #16858d;
}

.hot-list li:nth-child(2) span {
  background: #e77852;
}

.hot-list li:nth-child(3) span {
  color: #666;
  background: #ddd;
}

.hot-list strong {
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Empty state */
.magazine-empty {
  padding: 28px;
  border: 1px solid #e6e1dc;
  border-radius: 10px;
  color: #918d89;
  background: #fff;
  text-align: center;
}

/* Dark mode */
:global(.dark) .magazine-home {
  color: #edf3ff;
  background: #101722;
}

:global(.dark) .magazine-section-title::after,
:global(.dark) .side-panel h3,
:global(.dark) .hot-list li + li,
:global(.dark) .section-nav__item {
  border-color: rgba(186, 200, 224, 0.16);
}

:global(.dark) .featured-post,
:global(.dark) .side-panel,
:global(.dark) .magazine-empty {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.92);
}

:global(.dark) .featured-post h1,
:global(.dark) .author-panel h2,
:global(.dark) .author-panel__stats strong,
:global(.dark) .side-panel h3,
:global(.dark) .hot-list a,
:global(.dark) .section-nav__item {
  color: #edf3ff;
}

:global(.dark) .featured-post p,
:global(.dark) .post-meta,
:global(.dark) .author-panel p,
:global(.dark) .author-panel__stats span,
:global(.dark) .magazine-empty {
  color: rgba(226, 235, 248, 0.72);
}

:global(.dark) .section-nav__item:hover {
  color: #93c5fd;
}

:global(.dark) .post-chip {
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.16);
}

:global(.dark) .section-nav__badge {
  background: #3b82f6;
}

/* Responsive */
@media (max-width: 980px) {
  .magazine-home__shell {
    grid-template-columns: 1fr;
  }

  .magazine-sidebar {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .author-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 760px) {
  .magazine-home {
    padding: 84px 14px 32px;
  }

  .featured-post,
  .section-grid,
  .magazine-sidebar {
    grid-template-columns: 1fr;
  }

  .featured-post__media {
    min-height: 220px;
  }

  .featured-post__body {
    padding: 22px;
  }
}
</style>
