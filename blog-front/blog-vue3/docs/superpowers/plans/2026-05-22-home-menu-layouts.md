# Home Menu Layouts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Home dropdown menu that routes to four homepage variants: Blog, Start, Content, and Columns.

**Architecture:** Keep the existing `/` homepage unchanged and add three independent Vue Router pages under `/home/start`, `/home/content`, and `/home/columns`. Update desktop and mobile navigation to expose the same four homepage entries. The new pages read `useBlogInfoStore()` and reuse existing API wrappers for categories, tags, and home article sections.

**Tech Stack:** Vue 3 Composition API, Vue Router 4, Pinia, Vuetify, TypeScript, Vite.

---

## File Structure

- Modify `src/router/index.ts`: add route records for the three new homepage variants.
- Modify `src/components/layout/TopNavBar.vue`: replace the single Home link with a reference-style hover dropdown.
- Modify `src/components/layout/SideNavBar.vue`: add the same four homepage links for mobile drawer navigation.
- Create `src/views/home/HomeStart.vue`: lightweight start homepage.
- Create `src/views/home/HomeContent.vue`: content aggregation homepage.
- Create `src/views/home/HomeColumns.vue`: columns/topics homepage.
- Verify with `npm run build`.

### Task 1: Routes

**Files:**
- Modify: `src/router/index.ts`

- [ ] **Step 1: Add route records after the existing `/` Home route**

In `src/router/index.ts`, update the `routes` array so the first route block is followed by these three records:

```ts
  {
    path: '/home/start',
    name: 'HomeStart',
    component: () => import('@/views/home/HomeStart.vue'),
    meta: { title: '起始页' }
  },
  {
    path: '/home/content',
    name: 'HomeContent',
    component: () => import('@/views/home/HomeContent.vue'),
    meta: { title: '内容' }
  },
  {
    path: '/home/columns',
    name: 'HomeColumns',
    component: () => import('@/views/home/HomeColumns.vue'),
    meta: { title: '专栏' }
  },
```

- [ ] **Step 2: Run type check/build to verify missing components fail**

Run: `npm run build`

Expected: FAIL because `HomeStart.vue`, `HomeContent.vue`, and `HomeColumns.vue` do not exist yet.

### Task 2: Start Homepage

**Files:**
- Create: `src/views/home/HomeStart.vue`

- [ ] **Step 1: Create the start homepage component**

Create `src/views/home/HomeStart.vue`:

```vue
<template>
  <main class="home-variant-page home-start-page">
    <section class="home-start-hero">
      <div class="home-start-hero__content">
        <p class="home-start-kicker">START</p>
        <h1>{{ websiteConfig.websiteName || 'Renzs Blog' }}</h1>
        <p class="home-start-intro">
          {{ websiteConfig.websiteIntro || websiteConfig.websiteNotice || '欢迎来到我的博客。' }}
        </p>
        <div class="home-start-actions">
          <router-link class="home-start-button home-start-button-primary" to="/">
            进入博客
          </router-link>
          <router-link class="home-start-button" to="/home/content">
            浏览内容
          </router-link>
        </div>
      </div>
      <div class="home-start-panel">
        <div class="home-start-stat">
          <span>{{ blogInfo.articleCount || 0 }}</span>
          <label>文章</label>
        </div>
        <div class="home-start-stat">
          <span>{{ blogInfo.categoryCount || 0 }}</span>
          <label>分类</label>
        </div>
        <div class="home-start-stat">
          <span>{{ blogInfo.tagCount || 0 }}</span>
          <label>标签</label>
        </div>
        <div class="home-start-stat">
          <span>{{ blogInfo.viewsCount || 0 }}</span>
          <label>访问</label>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'

const blogInfoStore = useBlogInfoStore()
const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig || {})
</script>

<style scoped>
.home-variant-page {
  min-height: 100vh;
  padding: 112px 24px 48px;
  background:
    linear-gradient(135deg, rgba(73, 177, 245, 0.12), rgba(255, 255, 255, 0.72)),
    #f7fbff;
}

.home-start-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.6fr);
  gap: 28px;
  width: min(1120px, 100%);
  margin: 0 auto;
  align-items: stretch;
}

.home-start-hero__content,
.home-start-panel {
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 24px 54px rgba(26, 84, 130, 0.12);
  backdrop-filter: blur(14px);
}

.home-start-hero__content {
  padding: clamp(28px, 5vw, 58px);
}

.home-start-kicker {
  margin: 0 0 12px;
  color: #2b8fd8;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.home-start-hero h1 {
  margin: 0;
  color: #1f2d3d;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.08;
}

.home-start-intro {
  max-width: 640px;
  margin: 18px 0 0;
  color: #5b6675;
  font-size: 17px;
  line-height: 1.8;
}

.home-start-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.home-start-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 18px;
  border: 1px solid rgba(73, 177, 245, 0.28);
  border-radius: 10px;
  color: #2b6f9f;
  font-weight: 700;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.72);
}

.home-start-button-primary {
  color: #fff;
  background: linear-gradient(135deg, #49b1f5, #2f7df4);
}

.home-start-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 22px;
}

.home-start-stat {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 112px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.58);
  text-align: center;
}

.home-start-stat span {
  color: #26384d;
  font-size: 28px;
  font-weight: 800;
}

.home-start-stat label {
  margin-top: 6px;
  color: #6c7785;
  font-size: 13px;
}

:global(.dark) .home-variant-page {
  background:
    linear-gradient(135deg, rgba(73, 177, 245, 0.12), rgba(15, 20, 28, 0.92)),
    #111821;
}

:global(.dark) .home-start-hero__content,
:global(.dark) .home-start-panel {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.82);
}

:global(.dark) .home-start-hero h1,
:global(.dark) .home-start-stat span {
  color: #edf4ff;
}

:global(.dark) .home-start-intro,
:global(.dark) .home-start-stat label {
  color: rgba(226, 235, 248, 0.74);
}

:global(.dark) .home-start-stat {
  background: rgba(255, 255, 255, 0.06);
}

@media (max-width: 900px) {
  .home-start-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .home-variant-page {
    padding: 88px 14px 32px;
  }

  .home-start-panel {
    grid-template-columns: 1fr;
  }
}
</style>
```

- [ ] **Step 2: Run build and confirm remaining missing components**

Run: `npm run build`

Expected: FAIL only for missing `HomeContent.vue` and `HomeColumns.vue`.

### Task 3: Content Homepage

**Files:**
- Create: `src/views/home/HomeContent.vue`

- [ ] **Step 1: Create the content homepage component**

Create `src/views/home/HomeContent.vue`:

```vue
<template>
  <main class="home-variant-page home-content-page">
    <section class="home-content-shell">
      <header class="home-content-header">
        <p>CONTENT</p>
        <h1>内容</h1>
        <span>从文章、分类和标签快速进入你想看的内容。</span>
      </header>

      <section class="home-content-grid">
        <router-link class="home-content-entry" to="/archives">
          <strong>{{ blogInfo.articleCount || 0 }}</strong>
          <span>文章归档</span>
        </router-link>
        <router-link class="home-content-entry" to="/categories">
          <strong>{{ blogInfo.categoryCount || 0 }}</strong>
          <span>分类目录</span>
        </router-link>
        <router-link class="home-content-entry" to="/tags">
          <strong>{{ blogInfo.tagCount || 0 }}</strong>
          <span>标签索引</span>
        </router-link>
      </section>

      <section class="home-content-section">
        <div class="home-content-section__title">
          <h2>文章分组</h2>
          <router-link to="/">查看博客首页</router-link>
        </div>
        <div v-if="loading" class="home-content-empty">正在加载内容...</div>
        <div v-else-if="articleSections.length === 0" class="home-content-empty">暂无内容</div>
        <div v-else class="home-content-list">
          <article
            v-for="section in articleSections"
            :key="section.id || section.sectionKey || section.sectionName"
            class="home-content-card"
          >
            <div class="home-content-card__head">
              <h3>{{ section.sectionName || section.name || '文章' }}</h3>
              <span>{{ section.articleList?.length || 0 }} 篇</span>
            </div>
            <router-link
              v-for="article in section.articleList?.slice(0, 3) || []"
              :key="article.id || article.articleId"
              :to="'/articles/' + (article.id || article.articleId)"
              class="home-content-article"
            >
              {{ article.articleTitle || article.title || '未命名文章' }}
            </router-link>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { fetchHomeArticleSections } from './services/homeService'

const blogInfoStore = useBlogInfoStore()
const blogInfo = computed(() => blogInfoStore.blogInfo)
const articleSections = ref<any[]>([])
const loading = ref(false)

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
.home-variant-page {
  min-height: 100vh;
  padding: 104px 20px 48px;
  background: #f6f8fb;
}

.home-content-shell {
  width: min(1120px, 100%);
  margin: 0 auto;
}

.home-content-header {
  margin-bottom: 22px;
}

.home-content-header p {
  margin: 0 0 8px;
  color: #49b1f5;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.home-content-header h1 {
  margin: 0;
  color: #223044;
  font-size: 38px;
}

.home-content-header span {
  display: block;
  margin-top: 8px;
  color: #687385;
}

.home-content-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.home-content-entry,
.home-content-card {
  border: 1px solid rgba(210, 224, 238, 0.82);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 16px 34px rgba(33, 68, 98, 0.08);
}

.home-content-entry {
  display: flex;
  flex-direction: column;
  min-height: 128px;
  padding: 22px;
  color: inherit;
  text-decoration: none;
}

.home-content-entry strong {
  color: #26384d;
  font-size: 34px;
}

.home-content-entry span {
  margin-top: 6px;
  color: #657286;
}

.home-content-section {
  margin-top: 26px;
}

.home-content-section__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.home-content-section__title h2 {
  margin: 0;
  color: #26384d;
  font-size: 24px;
}

.home-content-section__title a {
  color: #2d8bd4;
  font-weight: 700;
  text-decoration: none;
}

.home-content-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.home-content-card {
  padding: 18px;
}

.home-content-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.home-content-card__head h3 {
  margin: 0;
  color: #26384d;
  font-size: 18px;
}

.home-content-card__head span {
  color: #778397;
  font-size: 13px;
}

.home-content-article {
  display: block;
  padding: 10px 0;
  border-top: 1px solid rgba(216, 226, 238, 0.72);
  color: #3d4a5d;
  text-decoration: none;
}

.home-content-empty {
  padding: 32px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: #718096;
  text-align: center;
}

:global(.dark) .home-variant-page {
  background: #111821;
}

:global(.dark) .home-content-header h1,
:global(.dark) .home-content-entry strong,
:global(.dark) .home-content-section__title h2,
:global(.dark) .home-content-card__head h3 {
  color: #edf4ff;
}

:global(.dark) .home-content-header span,
:global(.dark) .home-content-entry span,
:global(.dark) .home-content-card__head span,
:global(.dark) .home-content-article,
:global(.dark) .home-content-empty {
  color: rgba(226, 235, 248, 0.74);
}

:global(.dark) .home-content-entry,
:global(.dark) .home-content-card,
:global(.dark) .home-content-empty {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.82);
}

:global(.dark) .home-content-article {
  border-top-color: rgba(186, 200, 224, 0.14);
}

@media (max-width: 760px) {
  .home-content-grid,
  .home-content-list {
    grid-template-columns: 1fr;
  }
}
</style>
```

- [ ] **Step 2: Run build and confirm remaining missing component**

Run: `npm run build`

Expected: FAIL only for missing `HomeColumns.vue`.

### Task 4: Columns Homepage

**Files:**
- Create: `src/views/home/HomeColumns.vue`

- [ ] **Step 1: Create the columns homepage component**

Create `src/views/home/HomeColumns.vue`:

```vue
<template>
  <main class="home-variant-page home-columns-page">
    <section class="home-columns-shell">
      <header class="home-columns-header">
        <p>COLUMNS</p>
        <h1>专栏</h1>
        <span>按分类和标签进入长期主题。</span>
      </header>

      <div v-if="loading" class="home-columns-empty">正在加载专栏...</div>
      <div v-else-if="columns.length === 0" class="home-columns-empty">暂无专栏</div>
      <section v-else class="home-columns-grid">
        <router-link
          v-for="column in columns"
          :key="column.key"
          :to="column.to"
          class="home-column-card"
        >
          <span class="home-column-card__type">{{ column.type }}</span>
          <h2>{{ column.name }}</h2>
          <p>{{ column.description }}</p>
        </router-link>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getCategories, getTags } from '@/api/misc'

interface ColumnEntry {
  key: string
  type: string
  name: string
  description: string
  to: string
}

const columns = ref<ColumnEntry[]>([])
const loading = ref(false)

function readList(payload: any) {
  const data = payload?.data?.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.recordList)) return data.recordList
  return []
}

onMounted(async () => {
  loading.value = true
  try {
    const [categoryRes, tagRes] = await Promise.allSettled([getCategories(), getTags()])
    const categories =
      categoryRes.status === 'fulfilled' ? readList(categoryRes.value).slice(0, 6) : []
    const tags =
      tagRes.status === 'fulfilled' ? readList(tagRes.value).slice(0, 6) : []

    columns.value = [
      ...categories.map((category: any) => ({
        key: `category-${category.id}`,
        type: '分类',
        name: category.categoryName || '未命名分类',
        description: '查看这个分类下的文章。',
        to: `/categories/${category.id}`
      })),
      ...tags.map((tag: any) => ({
        key: `tag-${tag.id}`,
        type: '标签',
        name: tag.tagName || '未命名标签',
        description: '查看这个标签关联的文章。',
        to: `/tags/${tag.id}`
      }))
    ]
  } catch (error) {
    console.error('加载专栏首页失败:', error)
    columns.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-variant-page {
  min-height: 100vh;
  padding: 104px 20px 48px;
  background:
    linear-gradient(180deg, rgba(73, 177, 245, 0.12), transparent 280px),
    #f7f9fc;
}

.home-columns-shell {
  width: min(1120px, 100%);
  margin: 0 auto;
}

.home-columns-header {
  margin-bottom: 22px;
}

.home-columns-header p {
  margin: 0 0 8px;
  color: #49b1f5;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.home-columns-header h1 {
  margin: 0;
  color: #223044;
  font-size: 38px;
}

.home-columns-header span {
  display: block;
  margin-top: 8px;
  color: #687385;
}

.home-columns-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.home-column-card {
  display: flex;
  flex-direction: column;
  min-height: 176px;
  padding: 20px;
  border: 1px solid rgba(210, 224, 238, 0.82);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 16px 34px rgba(33, 68, 98, 0.08);
  color: inherit;
  text-decoration: none;
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.home-column-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 22px 44px rgba(33, 68, 98, 0.12);
}

.home-column-card__type {
  width: max-content;
  padding: 4px 9px;
  border-radius: 999px;
  color: #237dc0;
  font-size: 12px;
  font-weight: 800;
  background: rgba(73, 177, 245, 0.14);
}

.home-column-card h2 {
  margin: 18px 0 8px;
  color: #26384d;
  font-size: 22px;
}

.home-column-card p {
  margin: 0;
  color: #657286;
  line-height: 1.7;
}

.home-columns-empty {
  padding: 36px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: #718096;
  text-align: center;
}

:global(.dark) .home-variant-page {
  background:
    linear-gradient(180deg, rgba(73, 177, 245, 0.1), transparent 280px),
    #111821;
}

:global(.dark) .home-columns-header h1,
:global(.dark) .home-column-card h2 {
  color: #edf4ff;
}

:global(.dark) .home-columns-header span,
:global(.dark) .home-column-card p,
:global(.dark) .home-columns-empty {
  color: rgba(226, 235, 248, 0.74);
}

:global(.dark) .home-column-card,
:global(.dark) .home-columns-empty {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.82);
}

@media (max-width: 960px) {
  .home-columns-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .home-columns-grid {
    grid-template-columns: 1fr;
  }
}
</style>
```

- [ ] **Step 2: Run build and confirm routes compile**

Run: `npm run build`

Expected: PASS route/component resolution. Other unrelated warnings may remain.

### Task 5: Desktop Home Dropdown

**Files:**
- Modify: `src/components/layout/TopNavBar.vue`

- [ ] **Step 1: Replace the current desktop Home link block**

Find this block:

```vue
        <div class="menus-item">
          <router-link class="menu-btn" to="/">
            <v-icon size="18">mdi-home</v-icon> 棣栭〉
          </router-link>
        </div>
```

Replace it with:

```vue
        <div class="menus-item home-menu-item">
          <a class="menu-btn">
            <v-icon size="18">mdi-home</v-icon> 首页
            <v-icon size="14">mdi-chevron-down</v-icon>
          </a>
          <ul class="menus-submenu home-submenu">
            <li>
              <router-link to="/">
                <v-icon size="16">mdi-home-city</v-icon> 博客
              </router-link>
            </li>
            <li>
              <router-link to="/home/start">
                <v-icon size="16">mdi-magnify</v-icon> 起始页
              </router-link>
            </li>
            <li>
              <router-link to="/home/content">
                <v-icon size="16">mdi-folder-open</v-icon> 内容
              </router-link>
            </li>
            <li>
              <router-link to="/home/columns">
                <v-icon size="16">mdi-tag-multiple</v-icon> 专栏
              </router-link>
            </li>
          </ul>
        </div>
```

- [ ] **Step 2: Add reference-style dropdown CSS**

In the same file, add this CSS after the existing `.menus-submenu a:hover` rules:

```css
.home-submenu {
  right: auto;
  left: 50%;
  width: 220px;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  background: rgba(54, 91, 104, 0.78);
  box-shadow: 0 18px 34px rgba(0, 0, 0, 0.24);
  transform: translateX(-50%);
  backdrop-filter: blur(10px) saturate(120%);
}

.home-submenu li + li {
  margin-top: 7px;
}

.home-submenu a,
.nav .home-submenu a,
.nav-fixed-light .home-submenu a,
.nav-fixed-dark .home-submenu a,
.nav-love-overlay .home-submenu a,
.nav-albums-overlay .home-submenu a,
.nav-letter-overlay .home-submenu a {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 14px;
  border-radius: 7px;
  color: rgba(58, 70, 74, 0.92) !important;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.2;
  text-shadow: none;
  background: rgba(205, 234, 241, 0.8);
}

.home-submenu a:hover,
:global(.dark) .home-submenu a:hover {
  color: #2b4e5a !important;
  background: rgba(222, 245, 250, 0.94);
}
```

- [ ] **Step 3: Run build**

Run: `npm run build`

Expected: PASS.

### Task 6: Mobile Drawer Links

**Files:**
- Modify: `src/components/layout/SideNavBar.vue`

- [ ] **Step 1: Replace the current drawer Home link**

Find this block:

```vue
      <div class="menus-item">
        <router-link to="/">
          <v-icon size="18">mdi-home</v-icon> 棣栭〉
        </router-link>
      </div>
```

Replace it with:

```vue
      <div class="menu-group-title">首页</div>
      <div class="menus-item">
        <router-link to="/">
          <v-icon size="18">mdi-home-city</v-icon> 博客
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/home/start">
          <v-icon size="18">mdi-magnify</v-icon> 起始页
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/home/content">
          <v-icon size="18">mdi-folder-open</v-icon> 内容
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/home/columns">
          <v-icon size="18">mdi-tag-multiple</v-icon> 专栏
        </router-link>
      </div>
```

- [ ] **Step 2: Add drawer group title CSS**

In the same file, add this CSS before `.menus-item a`:

```css
.menu-group-title {
  padding: 4px 30px 2px;
  color: #7b8794;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}
```

- [ ] **Step 3: Run build**

Run: `npm run build`

Expected: PASS.

### Task 7: Final Verification

**Files:**
- Verify only

- [ ] **Step 1: Run production build**

Run: `npm run build`

Expected: PASS. Existing Vite dynamic import warnings are acceptable if no new errors appear.

- [ ] **Step 2: Start dev server**

Run: `npm run dev -- --host 127.0.0.1`

Expected: Vite prints a local URL, usually `http://127.0.0.1:3000/`.

- [ ] **Step 3: Browser smoke test**

Open the local URL and verify:

- Hovering desktop "首页" opens the dropdown.
- Dropdown contains 博客, 起始页, 内容, 专栏.
- 博客 routes to `/`.
- 起始页 routes to `/home/start`.
- 内容 routes to `/home/content`.
- 专栏 routes to `/home/columns`.
- On a mobile viewport, the side drawer contains the same four links.
- The existing `/` homepage still renders as before.

- [ ] **Step 4: Commit implementation**

Run:

```bash
git add src/router/index.ts src/components/layout/TopNavBar.vue src/components/layout/SideNavBar.vue src/views/home/HomeStart.vue src/views/home/HomeContent.vue src/views/home/HomeColumns.vue
git commit -m "feat: add home layout menu"
```

Expected: Commit succeeds. Do not include unrelated changes such as `public/images/love-letter-bg.svg`.
