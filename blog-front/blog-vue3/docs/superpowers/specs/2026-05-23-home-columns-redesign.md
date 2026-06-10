# HomeColumns 页面重构设计

**日期**: 2026-05-23
**范围**: `src/views/home/HomeColumns.vue` + `src/views/home/homeColumnsContent.ts`
**目标**: 将专栏页面从"扁平文章列表"重构为"Hero + 分组卡片"布局，接入专栏分组 API

---

## 问题分析

当前 HomeColumns.vue 存在以下问题：

1. **数据源错误** — 调用 `getArticles({ current: 1, size: 8 })` 获取扁平文章列表，而非 `getHomeArticleSections()` 获取分组数据
2. **文章数量过少** — 仅展示 1 篇精选 + 3 篇次要文章
3. **占位文章** — 不足 3 篇时填充硬编码假文章（不可点击）
4. **侧边栏无专栏特色** — 作者信息、分类、热门文章与其他页面重复
5. **无分组概念** — "专栏"页面不展示专栏分组

---

## 页面结构

```
┌─────────────────────────────────────────────────────┐
│  Hero 精选文章区                                      │
│  ┌──────────────┬──────────────────────────────────┐ │
│  │   封面图      │  分类标签                         │ │
│  │              │  文章标题 (h1)                    │ │
│  │              │  文章摘要                         │ │
│  │              │  日期 · 阅读时间 · 阅读量           │ │
│  └──────────────┴──────────────────────────────────┘ │
├───────────────────────────────┬─────────────────────┤
│  分组卡片网格                  │  侧边栏              │
│  ┌─────────┐ ┌─────────┐     │  ┌───────────────┐  │
│  │ 分组名   │ │ 分组名   │     │  │  作者信息面板   │  │
│  │ 5篇文章  │ │ 3篇文章  │     │  │  (保留现有)    │  │
│  │ ·文章1   │ │ ·文章1   │     │  └───────────────┘  │
│  │ ·文章2   │ │ ·文章2   │     │  ┌───────────────┐  │
│  │ ·文章3   │ │ ·文章3   │     │  │  专栏导航      │  │
│  │ 查看全部→ │ │ 查看全部→ │     │  │  · 技术 (5)   │  │
│  └─────────┘ └─────────┘     │  │  · 读书 (3)   │  │
│  ┌─────────┐ ┌─────────┐     │  │  · 旅行 (2)   │  │
│  │  ...     │ │  ...     │     │  └───────────────┘  │
│  └─────────┘ └─────────┘     │  ┌───────────────┐  │
│                               │  │  热门文章      │  │
│                               │  │  (保留现有)    │  │
│                               │  └───────────────┘  │
└───────────────────────────────┴─────────────────────┘
```

---

## 组件拆分

### HomeColumns.vue (主页面)

职责：页面容器，数据获取，状态管理。

数据获取：
```ts
const sections = ref<ArticleSection[]>([])
const featuredArticle = computed(() => {
  const firstSection = sections.value[0]
  return firstSection?.articleList?.[0]
})
const sectionCards = computed(() => sections.value)

onMounted(async () => {
  const [sectionRes, categoryRes] = await Promise.allSettled([
    fetchHomeArticleSections(),
    getCategories()
  ])
  // 处理分组数据
  sections.value = sectionRes.status === 'fulfilled'
    ? normalizeSections(readApiList(sectionRes.value))
    : []
  // 处理分类数据（侧边栏用）
  categories.value = categoryRes.status === 'fulfilled'
    ? normalizeCategoryList(readApiList(categoryRes.value))
    : []
})
```

### SectionCard 子组件

新建 `src/views/home/components/SectionCard.vue`

Props：
- `section: ArticleSection` — 分组数据
- `colorIndex: number` — 配色索引（用于左侧装饰条）

模板结构：
```html
<article class="section-card">
  <div class="section-card__accent" :class="accentClass"></div>
  <div class="section-card__body">
    <div class="section-card__header">
      <h3>{{ section.sectionName }}</h3>
      <span class="section-card__count">{{ section.articleList.length }} 篇</span>
    </div>
    <ul class="section-card__articles">
      <li v-for="article in section.articleList.slice(0, 3)" :key="article.id">
        <RouterLink :to="getArticlePath(article)">
          {{ getArticleTitle(article) }}
        </RouterLink>
      </li>
    </ul>
    <RouterLink
      v-if="getSectionLink(section)"
      :to="getSectionLink(section)"
      class="section-card__more"
    >
      查看全部 →
    </RouterLink>
  </div>
</article>
```

### 侧边栏

侧边栏结构保留在 HomeColumns.vue 内，分为三块：
1. **author-panel** — 保留现有（头像、名称、简介、统计）
2. **section-nav** — 新增，列出所有分组名称 + 文章数
3. **hot-list** — 保留现有热门文章

---

## "查看全部"链接逻辑

`getSectionLink(section)` 函数决定分组卡片的"查看全部"跳转目标：
- 如果分组有 `id` → `/categories/{id}`（假设分组 ID 与分类 ID 对应）
- 如果分组无 `id` 但有 `sectionKey` → `/categories`（降级到分类列表页）
- 如果都无 → 不显示"查看全部"链接

如果实际 API 返回的分组 ID 与分类 ID 不对应，需在实现时调整路由。

---

## 数据层变更

### homeColumnsContent.ts

新增类型和函数：

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

export function getSectionHotArticles(sections: ArticleSection[]): ArticleItem[] {
  const all = sections.flatMap(s => s.articleList)
  return getHotArticles(all)
}
```

移除：
- `placeholderArticles` 数组
- `displaySecondaryArticles` 计算属性

### 数据源变更

| 旧行为 | 新行为 |
|--------|--------|
| `getArticles({ current: 1, size: 8 })` | `fetchHomeArticleSections()` (调用 `getHomeArticleSections()`) |
| 本地排序取热门 | 从全部分组文章中提取热门 |
| 占位文章填充 | 无数据时显示空状态 |

---

## 视觉设计

### Hero 区
- 保持现有 `.featured-post` 样式：左右布局（图 + 文字）
- 无 Hero 文章时整个区域隐藏

### 分组卡片
- 两列网格：`grid-template-columns: repeat(2, minmax(0, 1fr))`
- 每张卡片：白色背景、1px 边框、10px 圆角
- 左侧 4px 彩色装饰条（6 种颜色轮换）
- 分组名称：18px 加粗
- 文章列表：简洁标题链接，hover 变色
- "查看全部 →"：底部链接，带箭头

### 侧边栏导航
- 分组列表：垂直排列，每项显示分组名 + 文章数徽章
- 点击分组名可滚动到对应分组卡片区域（使用 `scrollIntoView`）
- 当前可视分组高亮（可选，Intersection Observer）

### 响应式
- `> 980px`：主内容 + 300px 侧边栏
- `760px - 980px`：单列，侧边栏在下方
- `< 760px`：单列，侧边栏隐藏或折叠

### 暗黑模式
- 复用现有 `:global(.dark)` 模式
- 背景 `rgba(24, 31, 43, 0.92)`，边框 `rgba(186, 200, 224, 0.16)`
- 文字 `#edf3ff`，次要文字 `rgba(226, 235, 248, 0.72)`

---

## 文件变更清单

| 文件 | 操作 |
|------|------|
| `src/views/home/HomeColumns.vue` | 重写模板、脚本、样式 |
| `src/views/home/homeColumnsContent.ts` | 新增 ArticleSection 类型、normalizeSections、getSectionHotArticles |
| `src/views/home/homeColumnsContent.spec.ts` | 新增 normalizeSections、getSectionHotArticles 测试 |
| `src/views/home/components/SectionCard.vue` | 新建分组卡片组件 |

---

## 移除内容

- `placeholderArticles` 数组及所有占位逻辑
- `displaySecondaryArticles` 计算属性
- `fallbackCovers` 数组（Hero 仍需要，但简化为 1 个默认封面）
- `getArticles` API 调用

---

## 验收标准

1. 专栏页面展示 Hero 精选文章 + 分组卡片网格
2. 数据来自 `getHomeArticleSections()` API
3. 无数据时显示空状态，不显示占位文章
4. 分组卡片可点击文章标题跳转详情
5. "查看全部"链接跳转到分类页
6. 侧边栏展示专栏导航列表
7. 暗黑模式正常
8. 响应式布局正常（桌面/平板/手机）
9. 现有单元测试通过，新增测试覆盖新函数
