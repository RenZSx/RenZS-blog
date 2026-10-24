<template>
  <main class="hot-rank">
    <div class="hot-rank__glow" aria-hidden="true"></div>

    <div class="hot-rank__shell">
      <!-- 头部 -->
      <header class="rank-head">
        <p class="rank-head__eyebrow">TRENDING NOW</p>
        <h1 class="rank-head__title">今日热榜</h1>
        <p class="rank-head__sub">按阅读量排序,看看大家都在读什么。</p>
      </header>

      <!-- 分组筛选 -->
      <nav v-if="sections.length" class="rank-filter" aria-label="分组筛选">
        <button
          type="button"
          class="rank-filter__chip"
          :class="{ 'rank-filter__chip--on': activeSection === '' }"
          @click="activeSection = ''"
        >
          全部
        </button>
        <button
          v-for="section in sections"
          :key="section.id || section.sectionKey || section.sectionName"
          type="button"
          class="rank-filter__chip"
          :class="{ 'rank-filter__chip--on': activeSection === section.sectionName }"
          @click="activeSection = section.sectionName"
        >
          {{ section.sectionName }}
          <i>{{ section.articleList.length }}</i>
        </button>
      </nav>

      <!-- 榜首 -->
      <RouterLink v-if="featuredArticle" :to="articlePath(featuredArticle)" class="rank-champion">
        <div class="rank-champion__media">
          <img :src="articleCover(featuredArticle)" :alt="articleTitle(featuredArticle)" loading="lazy" />
        </div>
        <div class="rank-champion__body">
          <span class="rank-champion__crown">🏆 今日榜首</span>
          <h2 class="rank-champion__title">{{ articleTitle(featuredArticle) }}</h2>
          <p class="rank-champion__summary">{{ articleSummary(featuredArticle) }}</p>
          <div class="rank-champion__meta">
            <span class="rank-champion__chip">{{ articleCategory(featuredArticle) }}</span>
            <span>{{ articleDate(featuredArticle) }}</span>
            <span><v-icon size="13">mdi-eye-outline</v-icon> {{ shortNum(articleViews(featuredArticle)) }} 阅读</span>
          </div>
        </div>
      </RouterLink>

      <!-- 榜单 -->
      <section aria-label="热榜列表">
        <div v-if="loading" class="rank-skeletons">
          <div v-for="i in 6" :key="i" class="rank-skeleton"></div>
        </div>
        <div v-else-if="rankList.length === 0" class="rank-empty">
          <v-icon size="34">mdi-fire-off</v-icon>
          <p>这里暂时空空如也</p>
        </div>
        <ol v-else class="rank-list">
          <li v-for="(article, index) in restList" :key="article.id || index">
            <RouterLink :to="articlePath(article)" class="rank-row">
              <span class="rank-row__no" :class="{ 'rank-row__no--top': index < 2 }">{{ index + 2 }}</span>
              <div class="rank-row__main">
                <strong class="rank-row__title">{{ articleTitle(article) }}</strong>
                <span class="rank-row__meta">
                  <i class="rank-row__cat">{{ articleCategory(article) }}</i>
                  {{ articleDate(article) }}
                  <b><v-icon size="12">mdi-fire</v-icon>{{ shortNum(articleViews(article)) }}</b>
                </span>
              </div>
              <v-icon size="16" class="rank-row__arrow">mdi-chevron-right</v-icon>
            </RouterLink>
          </li>
        </ol>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchHomeArticleSections } from './services/homeService'
import { formatDate } from '@/utils/filters'
import {
  getArticleCategory,
  getArticlePath,
  getArticleSummary,
  getArticleTitle,
  getArticleViews,
  normalizeSections,
  readApiList,
  shortNumber,
  type ArticleItem,
  type ArticleSection,
} from './homeColumnsContent'

const sections = ref<ArticleSection[]>([])
const loading = ref(true)
const activeSection = ref('')

async function fetchData() {
  loading.value = true
  try {
    const [sectionRes] = await Promise.allSettled([fetchHomeArticleSections()])
    sections.value =
      sectionRes.status === 'fulfilled'
        ? normalizeSections(readApiList(sectionRes.value))
        : []
  } finally {
    loading.value = false
  }
}

// 榜单数据:按分组筛选后合并,按阅读量降序取前 20
const rankList = computed<ArticleItem[]>(() => {
  const pool = activeSection.value
    ? sections.value.filter(s => s.sectionName === activeSection.value)
    : sections.value
  return (pool as ArticleSection[])
    .flatMap(s => s.articleList || [])
    .sort((a, b) => getArticleViews(b) - getArticleViews(a))
    .slice(0, 20)
})

const featuredArticle = computed(() => rankList.value[0])
// 榜单从第 2 名开始展示,榜首单独展示
const restList = computed(() => rankList.value.slice(1))

function articlePath(article: ArticleItem) {
  return getArticlePath(article)
}

function articleCover(article: ArticleItem) {
  return article.articleCover || ''
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

function shortNum(value: unknown) {
  return shortNumber(value)
}

onMounted(() => {
  fetchData().catch((error) => {
    console.error('加载热榜失败:', error)
  })
})
</script>

<style scoped>
.hot-rank {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(165deg, #1a1430 0%, #201c42 50%, #131126 100%);
}

.hot-rank__glow {
  position: absolute;
  top: -170px;
  right: -110px;
  width: 480px;
  height: 480px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 122, 89, 0.2), transparent 68%);
  pointer-events: none;
}

.hot-rank__shell {
  position: relative;
  z-index: 1;
  width: min(860px, calc(100% - 40px));
  margin: 0 auto;
  padding: 56px 0 60px;
}

/* ========== 头部 ========== */
.rank-head {
  text-align: center;
  margin-bottom: 28px;
}

.rank-head__eyebrow {
  margin: 0 0 8px;
  color: rgba(255, 171, 145, 0.85);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.4em;
}

.rank-head__title {
  margin: 0;
  background: linear-gradient(120deg, #ffb199 10%, #ffd580 55%, #ff8fa4 90%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  font-size: clamp(30px, 5vw, 40px);
  font-weight: 800;
  letter-spacing: 0.06em;
}

.rank-head__sub {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

/* ========== 分组筛选 ========== */
.rank-filter {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 26px;
}

.rank-filter__chip {
  padding: 6px 14px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.07);
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.rank-filter__chip i {
  margin-left: 4px;
  font-style: normal;
  font-size: 11.5px;
  opacity: 0.65;
}

.rank-filter__chip:hover {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.34);
}

.rank-filter__chip--on {
  border-color: transparent;
  background: linear-gradient(120deg, #ff7a59, #ff9a6b);
  color: #fff;
}

/* ========== 榜首 ========== */
.rank-champion {
  display: grid;
  grid-template-columns: minmax(220px, 0.9fr) minmax(0, 1.1fr);
  overflow: hidden;
  margin-bottom: 22px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(10px);
  text-decoration: none;
  transition: transform 0.25s ease, border-color 0.25s ease;
}

.rank-champion:hover {
  border-color: rgba(255, 171, 145, 0.55);
  transform: translateY(-3px);
}

.rank-champion__media {
  position: relative;
  min-height: 218px;
  background: rgba(255, 255, 255, 0.06);
}

.rank-champion__media img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}

.rank-champion:hover .rank-champion__media img {
  transform: scale(1.05);
}

.rank-champion__body {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 24px 26px;
}

.rank-champion__crown {
  color: #ffd580;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.rank-champion__title {
  margin: 0;
  color: rgba(255, 255, 255, 0.96);
  font-size: 21px;
  font-weight: 800;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rank-champion__summary {
  margin: 0;
  color: rgba(255, 255, 255, 0.62);
  font-size: 13.5px;
  line-height: 1.75;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rank-champion__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.55);
  font-size: 12.5px;
}

.rank-champion__meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.rank-champion__chip {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 171, 145, 0.16);
  color: #ffab91;
  font-weight: 600;
}

/* ========== 榜单 ========== */
.rank-skeletons {
  display: grid;
  gap: 10px;
}

.rank-skeleton {
  height: 74px;
  border-radius: 14px;
  background: linear-gradient(100deg, rgba(255, 255, 255, 0.05) 40%, rgba(255, 255, 255, 0.1) 50%, rgba(255, 255, 255, 0.05) 60%);
  background-size: 200% 100%;
  animation: skeleton-shine 1.4s ease infinite;
}

@keyframes skeleton-shine {
  to {
    background-position: -200% 0;
  }
}

.rank-empty {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 60px 20px;
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.55);
}

.rank-empty p {
  margin: 0;
  font-size: 14px;
}

.rank-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 15px 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  color: inherit;
  text-decoration: none;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.rank-row:hover {
  border-color: rgba(255, 171, 145, 0.45);
  background: rgba(255, 255, 255, 0.09);
  transform: translateX(4px);
}

.rank-row__no {
  flex: 0 0 34px;
  color: rgba(255, 255, 255, 0.35);
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 20px;
  font-weight: 800;
  font-style: italic;
  text-align: center;
}

.rank-row__no--top {
  background: linear-gradient(160deg, #ffd580, #ff7a59);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.rank-row__main {
  flex: 1;
  min-width: 0;
}

.rank-row__title {
  display: block;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease;
}

.rank-row:hover .rank-row__title {
  color: #ffd580;
}

.rank-row__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
}

.rank-row__cat {
  flex-shrink: 0;
  padding: 2px 9px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.09);
  color: rgba(255, 255, 255, 0.68);
  font-style: normal;
  font-size: 11.5px;
}

.rank-row__meta b {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: rgba(255, 171, 145, 0.85);
  font-weight: 600;
}

.rank-row__arrow {
  flex: 0 0 auto;
  color: rgba(255, 255, 255, 0.3);
  transition: color 0.2s ease, transform 0.2s ease;
}

.rank-row:hover .rank-row__arrow {
  color: #ffd580;
  transform: translateX(3px);
}

/* ========== 响应式 ========== */
@media (max-width: 720px) {
  .hot-rank__shell {
    width: calc(100% - 28px);
    padding: 40px 0 44px;
  }

  .rank-champion {
    grid-template-columns: 1fr;
  }

  .rank-champion__media {
    min-height: 180px;
  }
}
</style>
