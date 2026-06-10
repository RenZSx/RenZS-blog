# HomeColumns 页面重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将专栏页面从扁平文章列表重构为 Hero + 分组卡片布局，接入专栏分组 API

**Architecture:** 使用 `getHomeArticleSections()` 获取分组数据，首页展示 Hero 精选文章 + 分组卡片网格 + 侧边栏导航。新建 `SectionCard.vue` 子组件渲染分组卡片，`homeColumnsContent.ts` 新增分组数据处理函数。

**Tech Stack:** Vue 3 Composition API (`<script setup lang="ts">`), TypeScript, Vuetify 3, unplugin-auto-import (无需显式 import Vue/VueRouter API)

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `src/views/home/homeColumnsContent.ts` | 修改 | 新增 ArticleSection 类型、normalizeSections、getSectionHotArticles、getSectionLink |
| `src/views/home/homeColumnsContent.spec.ts` | 修改 | 新增分组相关函数的单元测试 |
| `src/views/home/components/SectionCard.vue` | 新建 | 分组卡片子组件 |
| `src/views/home/HomeColumns.vue` | 重写 | 主页面：Hero + 分组网格 + 侧边栏 |

---

### Task 1: 扩展 homeColumnsContent.ts 数据层

**Files:**
- Modify: `src/views/home/homeColumnsContent.ts`
- Modify: `src/views/home/homeColumnsContent.spec.ts`

- [ ] **Step 1: 编写 normalizeSections 测试**

在 `homeColumnsContent.spec.ts` 末尾的 `})` 前添加新测试用例：

```ts
it('normalizes section data and filters empty sections', () => {
  const items = [
    { id: 1, sectionName: '技术', articleList: [{ id: 10, articleTitle: '文章A' }] },
    { id: 2, name: '读书', articleList: [{ id: 20, articleTitle: '文章B' }] },
    { id: 3, sectionName: '空分组', articleList: [] },
    { id: 4, sectionName: '无列表' },
    { id: 'skip', sectionName: '无ID', articleList: [{ id: 30 }] }
  ]

  expect(normalizeSections(items)).toEqual([
    { id: 1, sectionKey: undefined, sectionName: '技术', name: undefined, articleList: [{ id: 10, articleTitle: '文章A' }] },
    { id: 2, sectionKey: undefined, sectionName: '读书', name: '读书', articleList: [{ id: 20, articleTitle: '文章B' }] }
  ])
})
```

同时在文件顶部的 import 中添加 `normalizeSections`。

- [ ] **Step 2: 运行测试验证失败**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vitest run src/views/home/homeColumnsContent.spec.ts`
Expected: FAIL — `normalizeSections` is not exported

- [ ] **Step 3: 实现 normalizeSections**

在 `homeColumnsContent.ts` 中 `normalizeCategoryList` 函数之后添加：

```ts
export interface ArticleSection {
  id?: number
  sectionKey?: string
  sectionName?: string
  name?: string
  articleList: ArticleItem[]
}

export function normalizeSections(items: unknown[]): ArticleSection[] {
  return items
    .filter((item: any) => Array.isArray(item?.articleList) && item.articleList.length > 0)
    .map((item: any) => ({
      id: item.id,
      sectionKey: item.sectionKey,
      sectionName: item.sectionName || item.name || '未命名专栏',
      name: item.name,
      articleList: item.articleList
    }))
}
```

- [ ] **Step 4: 编写 getSectionHotArticles 测试**

在 spec 文件中添加：

```ts
it('extracts hot articles across all sections', () => {
  const sections = [
    { id: 1, sectionName: 'A', articleList: [{ id: 1, viewCount: 5 }] },
    { id: 2, sectionName: 'B', articleList: [{ id: 2, viewsCount: 100 }, { id: 3, likeCount: 50 }] }
  ]

  expect(getSectionHotArticles(sections).map(a => a.id)).toEqual([2, 3, 1])
})
```

同时在 import 中添加 `getSectionHotArticles`。

- [ ] **Step 5: 实现 getSectionHotArticles**

在 `normalizeSections` 函数之后添加：

```ts
export function getSectionHotArticles(sections: ArticleSection[]): ArticleItem[] {
  const all = sections.flatMap(s => s.articleList)
  return getHotArticles(all)
}
```

- [ ] **Step 6: 编写 getSectionLink 测试**

在 spec 文件中添加：

```ts
it('builds section link with fallback logic', () => {
  expect(getSectionLink({ id: 5, sectionName: '技术', articleList: [] })).toBe('/categories/5')
  expect(getSectionLink({ sectionKey: 'tech', sectionName: '技术', articleList: [] })).toBe('/categories')
  expect(getSectionLink({ sectionName: '未知', articleList: [] })).toBeNull()
})
```

同时在 import 中添加 `getSectionLink`。

- [ ] **Step 7: 实现 getSectionLink**

在 `getSectionHotArticles` 函数之后添加：

```ts
export function getSectionLink(section: ArticleSection): string | null {
  if (section.id) return `/categories/${section.id}`
  if (section.sectionKey) return '/categories'
  return null
}
```

- [ ] **Step 8: 运行全部测试验证通过**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vitest run src/views/home/homeColumnsContent.spec.ts`
Expected: PASS — 所有测试通过

- [ ] **Step 9: 提交**

```bash
git add src/views/home/homeColumnsContent.ts src/views/home/homeColumnsContent.spec.ts
git commit -m "feat(home): add section data types and utility functions for columns page"
```

---

### Task 2: 创建 SectionCard 子组件

**Files:**
- Create: `src/views/home/components/SectionCard.vue`

- [ ] **Step 1: 创建 SectionCard.vue**

创建文件 `src/views/home/components/SectionCard.vue`：

```vue
<template>
  <article class="section-card" :data-section-id="section.id">
    <div class="section-card__accent" :class="accentClass"></div>
    <div class="section-card__body">
      <div class="section-card__header">
        <h3>{{ section.sectionName }}</h3>
        <span class="section-card__count">{{ section.articleList.length }} 篇</span>
      </div>
      <ul class="section-card__articles">
        <li
          v-for="article in section.articleList.slice(0, 3)"
          :key="article.id || article.articleId"
        >
          <RouterLink :to="getArticlePath(article)">
            {{ getArticleTitle(article) }}
          </RouterLink>
        </li>
      </ul>
      <RouterLink
        v-if="sectionLink"
        :to="sectionLink"
        class="section-card__more"
      >
        查看全部 →
      </RouterLink>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import {
  getArticlePath,
  getArticleTitle,
  getSectionLink,
  type ArticleSection
} from '../homeColumnsContent'

const props = defineProps<{
  section: ArticleSection
  colorIndex: number
}>()

const accentColors = [
  'section-card__accent--teal',
  'section-card__accent--warm',
  'section-card__accent--green',
  'section-card__accent--purple',
  'section-card__accent--yellow',
  'section-card__accent--cyan'
]

const accentClass = computed(() => accentColors[props.colorIndex % accentColors.length])
const sectionLink = computed(() => getSectionLink(props.section))
</script>

<style scoped>
.section-card {
  display: flex;
  overflow: hidden;
  border: 1px solid #e6e1dc;
  border-radius: 10px;
  background: #fff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.section-card:hover {
  border-color: #d8d0c8;
  box-shadow: 0 14px 34px rgba(31, 41, 51, 0.08);
  transform: translateY(-2px);
}

.section-card__accent {
  flex-shrink: 0;
  width: 4px;
}

.section-card__accent--teal { background: #0b6f76; }
.section-card__accent--warm { background: #c95d3d; }
.section-card__accent--green { background: #178060; }
.section-card__accent--purple { background: #6b55c7; }
.section-card__accent--yellow { background: #9d6a00; }
.section-card__accent--cyan { background: #0b7682; }

.section-card__body {
  flex: 1;
  min-width: 0;
  padding: 20px;
}

.section-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-card__header h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  overflow-wrap: anywhere;
}

.section-card__count {
  flex-shrink: 0;
  padding: 3px 8px;
  border-radius: 6px;
  color: #0b6f76;
  font-size: 12px;
  font-weight: 700;
  background: #dff4f3;
}

.section-card__articles {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.section-card__articles li + li {
  border-top: 1px solid #ece7e2;
}

.section-card__articles a {
  display: block;
  padding: 10px 0;
  color: #3d4a5d;
  font-size: 14px;
  text-decoration: none;
  overflow-wrap: anywhere;
}

.section-card__articles a:hover {
  color: #0b6f76;
}

.section-card__more {
  display: inline-block;
  margin-top: 12px;
  color: #0b6f76;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
}

.section-card__more:hover {
  text-decoration: underline;
}

:global(.dark) .section-card {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.92);
}

:global(.dark) .section-card__header h3 {
  color: #edf3ff;
}

:global(.dark) .section-card__articles a {
  color: rgba(226, 235, 248, 0.72);
}

:global(.dark) .section-card__articles a:hover {
  color: #93c5fd;
}

:global(.dark) .section-card__articles li + li {
  border-top-color: rgba(186, 200, 224, 0.14);
}

:global(.dark) .section-card__count {
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.16);
}

:global(.dark) .section-card__more {
  color: #93c5fd;
}
</style>
```

- [ ] **Step 2: 验证组件语法**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vue-tsc --noEmit --pretty 2>&1 | head -30`
Expected: 无 SectionCard 相关错误（可能有其他已有错误，忽略即可）

- [ ] **Step 3: 提交**

```bash
git add src/views/home/components/SectionCard.vue
git commit -m "feat(home): add SectionCard component for columns page"
```

---

### Task 3: 重写 HomeColumns.vue 主页面

**Files:**
- Rewrite: `src/views/home/HomeColumns.vue`

- [ ] **Step 1: 重写 HomeColumns.vue**

完全重写 `src/views/home/HomeColumns.vue`：

```vue
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
              href="javascript:void(0)"
              class="section-nav__item"
              @click="scrollToSection(index)"
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
import { getCategories } from '@/api/misc'
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
  normalizeCategoryList,
  normalizeSections,
  readApiList,
  shortNumber,
  type ArticleItem,
  type ArticleSection,
  type CategoryItem
} from './homeColumnsContent'
import SectionCard from './components/SectionCard.vue'

const defaultCover = 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80'

const blogInfoStore = useBlogInfoStore()
const sections = ref<ArticleSection[]>([])
const categories = ref<CategoryItem[]>([])
const sectionRefs = ref<HTMLElement[]>([])

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
  const el = document.querySelector(`[data-section-id="${sections.value[index]?.id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

async function fetchData() {
  const [sectionRes, categoryRes] = await Promise.allSettled([
    fetchHomeArticleSections(),
    getCategories()
  ])

  sections.value =
    sectionRes.status === 'fulfilled'
      ? normalizeSections(readApiList(sectionRes.value))
      : []

  categories.value =
    categoryRes.status === 'fulfilled'
      ? normalizeCategoryList(readApiList(categoryRes.value))
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
```

- [ ] **Step 2: 运行类型检查**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vue-tsc --noEmit --pretty 2>&1 | head -30`
Expected: 无 HomeColumns 相关错误

- [ ] **Step 3: 运行全部测试**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vitest run`
Expected: 全部测试通过

- [ ] **Step 4: 提交**

```bash
git add src/views/home/HomeColumns.vue
git commit -m "feat(home): rewrite columns page with hero + section card layout"
```

---

### Task 4: 最终验证

- [ ] **Step 1: 运行完整构建**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vue-tsc --noEmit && npx vite build 2>&1 | tail -20`
Expected: 构建成功，无错误

- [ ] **Step 2: 运行全部测试**

Run: `cd "D:/桌面/blog-master/blog-vue/blog-vue3" && npx vitest run`
Expected: 全部测试通过

- [ ] **Step 3: 最终提交（如有遗漏文件）**

```bash
git status
# 确认所有变更已提交
```
