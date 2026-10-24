<template>
  <main class="content-hub">
    <div class="content-hub__glow" aria-hidden="true"></div>

    <div class="content-hub__shell">
      <!-- 头部 -->
      <header class="hub-head">
        <p class="hub-head__eyebrow">CONTENT HUB</p>
        <h1 class="hub-head__title">内容导航</h1>
        <p class="hub-head__sub">从归档、分类和标签快速进入你想看的内容。</p>
      </header>

      <!-- 统计入口 -->
      <nav class="hub-entries" aria-label="内容入口">
        <RouterLink class="hub-entry" to="/archives">
          <v-icon size="22">mdi-text-box-multiple-outline</v-icon>
          <strong>{{ statValue(blogInfo.articleCount) }}</strong>
          <span>文章归档</span>
        </RouterLink>
        <RouterLink class="hub-entry" to="/categories">
          <v-icon size="22">mdi-shape-outline</v-icon>
          <strong>{{ statValue(blogInfo.categoryCount) }}</strong>
          <span>分类目录</span>
        </RouterLink>
        <RouterLink class="hub-entry" to="/tags">
          <v-icon size="22">mdi-tag-multiple-outline</v-icon>
          <strong>{{ statValue(blogInfo.tagCount) }}</strong>
          <span>标签索引</span>
        </RouterLink>
      </nav>

      <!-- 文章分组 -->
      <section aria-labelledby="hub-groups-title">
        <div class="hub-groups-head">
          <h2 id="hub-groups-title">文章分组</h2>
          <RouterLink class="hub-groups-head__link" to="/">
            去博客首页
            <v-icon size="15">mdi-arrow-right</v-icon>
          </RouterLink>
        </div>

        <!-- 加载骨架 -->
        <div v-if="loading" class="hub-grid">
          <div v-for="i in 4" :key="i" class="hub-card hub-card--skeleton"></div>
        </div>
        <div v-else-if="articleSections.length === 0" class="hub-empty">
          <v-icon size="34">mdi-inbox-outline</v-icon>
          <p>暂无内容,快去发布第一篇文章吧</p>
        </div>
        <div v-else class="hub-grid">
          <article
            v-for="(section, index) in articleSections"
            :key="sectionKey(section, index)"
            class="hub-card"
          >
            <header class="hub-card__head">
              <span class="hub-card__badge" :data-accent="index % 6">{{ sectionTitle(section).slice(0, 1) }}</span>
              <h3 class="hub-card__name">{{ sectionTitle(section) }}</h3>
              <span class="hub-card__count">{{ section.articleList?.length || 0 }} 篇</span>
            </header>
            <ul class="hub-card__list">
              <li v-for="article in section.articleList?.slice(0, 6) || []" :key="article.id || article.articleId">
                <RouterLink :to="'/articles/' + (article.id || article.articleId)">
                  <span class="hub-card__dot" aria-hidden="true"></span>
                  <span class="hub-card__article-title">{{ articleTitle(article) }}</span>
                </RouterLink>
              </li>
            </ul>
            <RouterLink
              v-if="(section.articleList?.length || 0) > 6"
              class="hub-card__more"
              to="/categories"
            >
              查看该分组全部文章
              <v-icon size="13">mdi-chevron-right</v-icon>
            </RouterLink>
          </article>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useBlogInfoStore, type BlogInfo } from '@/stores/blogInfo'
import { fetchHomeArticleSections } from './services/homeService'

interface ArticleSummary {
  id?: number
  articleId?: number
  articleTitle?: string
  title?: string
}

interface ArticleSection {
  id?: number
  sectionKey?: string
  sectionName?: string
  name?: string
  articleList?: ArticleSummary[]
}

const blogInfoStore = useBlogInfoStore()
const articleSections = ref<ArticleSection[]>([])
const loading = ref(false)

const blogInfo = computed<Partial<BlogInfo>>(() => {
  const value = blogInfoStore.blogInfo
  return value && typeof value === 'object' ? value : {}
})

function statValue(value: unknown) {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0
}

function articleTitle(article: ArticleSummary) {
  return article.articleTitle || article.title || '未命名文章'
}

function sectionKey(section: ArticleSection, index: number) {
  return section.id || section.sectionKey || section.sectionName || section.name || `section-${index}`
}

function sectionTitle(section: ArticleSection) {
  return section.sectionName || section.name || '文章'
}

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await fetchHomeArticleSections()
    articleSections.value = Array.isArray(data?.data) ? data.data : []
  } catch (error) {
    console.error('加载内容首页失败:', error)
    articleSections.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.content-hub {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(165deg, #101d33 0%, #16263f 52%, #0e1a2e 100%);
}

.content-hub__glow {
  position: absolute;
  top: -160px;
  left: -100px;
  width: 460px;
  height: 460px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(84, 134, 255, 0.24), transparent 68%);
  pointer-events: none;
}

.content-hub__shell {
  position: relative;
  z-index: 1;
  width: min(1080px, calc(100% - 40px));
  margin: 0 auto;
  padding: 56px 0 60px;
}

/* ========== 头部 ========== */
.hub-head {
  text-align: center;
  margin-bottom: 34px;
}

.hub-head__eyebrow {
  margin: 0 0 8px;
  color: rgba(140, 190, 255, 0.85);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.4em;
}

.hub-head__title {
  margin: 0;
  background: linear-gradient(120deg, #9cc3ff 10%, #c9adff 55%, #7de3f0 90%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  font-size: clamp(30px, 5vw, 40px);
  font-weight: 800;
  letter-spacing: 0.06em;
}

.hub-head__sub {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.62);
  font-size: 14px;
}

/* ========== 统计入口 ========== */
.hub-entries {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 40px;
}

.hub-entry {
  display: grid;
  justify-items: center;
  gap: 4px;
  padding: 20px 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(10px);
  color: #fff;
  text-decoration: none;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.hub-entry:hover {
  border-color: rgba(140, 190, 255, 0.5);
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-3px);
}

.hub-entry :deep(.v-icon) {
  color: #8bc5ff;
}

.hub-entry strong {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.2;
}

.hub-entry span {
  color: rgba(255, 255, 255, 0.58);
  font-size: 12.5px;
}

/* ========== 分组 ========== */
.hub-groups-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.hub-groups-head h2 {
  margin: 0;
  color: rgba(255, 255, 255, 0.94);
  font-size: 20px;
  font-weight: 700;
}

.hub-groups-head__link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: rgba(140, 190, 255, 0.9);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.hub-groups-head__link:hover {
  color: #fff;
}

.hub-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.hub-card {
  min-width: 0;
  padding: 20px 22px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(10px);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.hub-card:hover {
  border-color: rgba(140, 190, 255, 0.4);
  transform: translateY(-2px);
}

.hub-card--skeleton {
  height: 190px;
  background: linear-gradient(100deg, rgba(255, 255, 255, 0.05) 40%, rgba(255, 255, 255, 0.1) 50%, rgba(255, 255, 255, 0.05) 60%);
  background-size: 200% 100%;
  animation: skeleton-shine 1.4s ease infinite;
}

@keyframes skeleton-shine {
  to {
    background-position: -200% 0;
  }
}

.hub-card__head {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-bottom: 12px;
}

.hub-card__badge {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(140, 190, 255, 0.16);
  color: #8bc5ff;
  font-size: 15px;
  font-weight: 700;
}

.hub-card__badge[data-accent='1'] { background: rgba(125, 227, 240, 0.16); color: #7de3f0; }
.hub-card__badge[data-accent='2'] { background: rgba(201, 173, 255, 0.16); color: #c9adff; }
.hub-card__badge[data-accent='3'] { background: rgba(255, 213, 128, 0.16); color: #ffd580; }
.hub-card__badge[data-accent='4'] { background: rgba(125, 226, 162, 0.16); color: #7de2a2; }
.hub-card__badge[data-accent='5'] { background: rgba(255, 143, 164, 0.16); color: #ff8fa4; }

.hub-card__name {
  flex: 1;
  min-width: 0;
  margin: 0;
  color: rgba(255, 255, 255, 0.94);
  font-size: 17px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hub-card__count {
  flex-shrink: 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12.5px;
}

.hub-card__list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.hub-card__list li a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  color: rgba(255, 255, 255, 0.78);
  font-size: 13.5px;
  text-decoration: none;
  transition: color 0.2s ease;
}

.hub-card__list li a:hover {
  color: #8bc5ff;
}

.hub-card__dot {
  flex: 0 0 auto;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgba(140, 190, 255, 0.55);
}

.hub-card__article-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hub-card__more {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-top: 10px;
  color: rgba(140, 190, 255, 0.85);
  font-size: 12.5px;
  font-weight: 600;
  text-decoration: none;
}

.hub-card__more:hover {
  color: #fff;
}

.hub-empty {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 56px 20px;
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.55);
}

.hub-empty p {
  margin: 0;
  font-size: 14px;
}

/* ========== 响应式 ========== */
@media (max-width: 760px) {
  .content-hub__shell {
    width: calc(100% - 28px);
    padding: 40px 0 44px;
  }

  .hub-grid {
    grid-template-columns: 1fr;
  }
}
</style>
