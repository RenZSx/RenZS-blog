<template>
  <main class="home-content">
    <section class="home-content__shell">
      <header class="home-content__header">
        <p class="home-content__eyebrow">Content Homepage</p>
        <h1>内容</h1>
        <span>从文章、分类和标签快速进入你想看的内容。</span>
      </header>

      <section class="home-content__entries" aria-label="内容入口">
        <RouterLink class="home-content__entry" to="/archives">
          <strong>{{ statValue(blogInfo.articleCount) }}</strong>
          <span>文章归档</span>
        </RouterLink>
        <RouterLink class="home-content__entry" to="/categories">
          <strong>{{ statValue(blogInfo.categoryCount) }}</strong>
          <span>分类目录</span>
        </RouterLink>
        <RouterLink class="home-content__entry" to="/tags">
          <strong>{{ statValue(blogInfo.tagCount) }}</strong>
          <span>标签索引</span>
        </RouterLink>
      </section>

      <section class="home-content__section" aria-labelledby="home-content-sections-title">
        <div class="home-content__section-title">
          <h2 id="home-content-sections-title">文章分组</h2>
          <RouterLink to="/">查看博客首页</RouterLink>
        </div>

        <div v-if="loading" class="home-content__empty">正在加载内容...</div>
        <div v-else-if="articleSections.length === 0" class="home-content__empty">
          暂无内容
        </div>
        <div v-else class="home-content__list">
          <article
            v-for="(section, index) in articleSections"
            :key="sectionKey(section, index)"
            class="home-content__card"
          >
            <div class="home-content__card-head">
              <h3>{{ sectionTitle(section) }}</h3>
              <span>{{ section.articleList?.length || 0 }} 篇</span>
            </div>
            <RouterLink
              v-for="article in section.articleList?.slice(0, 3) || []"
              :key="article.id || article.articleId"
              :to="'/articles/' + (article.id || article.articleId)"
              class="home-content__article"
            >
              {{ article.articleTitle || article.title || '未命名文章' }}
            </RouterLink>
          </article>
        </div>
      </section>
    </section>
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
.home-content {
  min-height: 100vh;
  padding: 104px 20px 48px;
  background: #f6f8fb;
}

.home-content__shell {
  width: min(1120px, 100%);
  margin: 0 auto;
}

.home-content__header {
  margin-bottom: 22px;
}

.home-content__eyebrow {
  margin: 0 0 8px;
  color: #49b1f5;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.home-content__header h1 {
  margin: 0;
  color: #223044;
  font-size: 38px;
  overflow-wrap: anywhere;
}

.home-content__header span {
  display: block;
  margin-top: 8px;
  color: #687385;
  overflow-wrap: anywhere;
}

.home-content__entries {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.home-content__entry,
.home-content__card {
  border: 1px solid rgba(210, 224, 238, 0.82);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 16px 34px rgba(33, 68, 98, 0.08);
}

.home-content__entry {
  display: flex;
  flex-direction: column;
  min-height: 128px;
  padding: 22px;
  color: inherit;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.home-content__entry:hover,
.home-content__entry:focus-visible {
  box-shadow: 0 20px 40px rgba(33, 68, 98, 0.12);
  transform: translateY(-2px);
}

.home-content__entry strong {
  color: #26384d;
  font-size: 34px;
  overflow-wrap: anywhere;
}

.home-content__entry span {
  margin-top: 6px;
  color: #657286;
}

.home-content__section {
  margin-top: 26px;
}

.home-content__section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.home-content__section-title h2 {
  margin: 0;
  color: #26384d;
  font-size: 24px;
}

.home-content__section-title a {
  color: #2d8bd4;
  font-weight: 700;
  text-decoration: none;
}

.home-content__list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.home-content__card {
  min-width: 0;
  padding: 18px;
}

.home-content__card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.home-content__card-head h3 {
  min-width: 0;
  margin: 0;
  color: #26384d;
  font-size: 18px;
  overflow-wrap: anywhere;
}

.home-content__card-head span {
  flex-shrink: 0;
  color: #778397;
  font-size: 13px;
}

.home-content__article {
  display: block;
  padding: 10px 0;
  border-top: 1px solid rgba(216, 226, 238, 0.72);
  color: #3d4a5d;
  text-decoration: none;
  overflow-wrap: anywhere;
}

.home-content__article:hover,
.home-content__article:focus-visible {
  color: #2d8bd4;
}

.home-content__empty {
  padding: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  color: #718096;
  text-align: center;
}

:global(.dark) .home-content {
  background: #111821;
}

:global(.dark) .home-content__header h1,
:global(.dark) .home-content__entry strong,
:global(.dark) .home-content__section-title h2,
:global(.dark) .home-content__card-head h3 {
  color: #edf4ff;
}

:global(.dark) .home-content__header span,
:global(.dark) .home-content__entry span,
:global(.dark) .home-content__card-head span,
:global(.dark) .home-content__article,
:global(.dark) .home-content__empty {
  color: rgba(226, 235, 248, 0.74);
}

:global(.dark) .home-content__entry,
:global(.dark) .home-content__card,
:global(.dark) .home-content__empty {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.82);
}

:global(.dark) .home-content__article {
  border-top-color: rgba(186, 200, 224, 0.14);
}

@media (max-width: 760px) {
  .home-content {
    padding: 88px 14px 32px;
  }

  .home-content__entries,
  .home-content__list {
    grid-template-columns: 1fr;
  }

  .home-content__section-title {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
