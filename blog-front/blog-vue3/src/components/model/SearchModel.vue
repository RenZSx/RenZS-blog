<template>
  <v-dialog :model-value="uiStore.searchFlag" @update:model-value="uiStore.setSearchFlag($event)" :fullscreen="isMobile" max-width="680">
    <v-card class="search-card">
      <button class="close-btn" type="button" aria-label="关闭搜索" @click="uiStore.setSearchFlag(false)">
        mdi-close
      </button>

      <div class="search-wrapper">
        <div class="search-head">
          <div class="search-kicker">Article Search</div>
          <h2 class="search-title">文章搜索</h2>
          <p class="search-subtitle">输入关键词搜索，或展开高级筛选按分类、标签、时间组合检索。</p>
        </div>

        <div class="search-input-shell">
          <v-icon class="search-input-icon" size="22">mdi-magnify</v-icon>
          <input
            v-model="keywords"
            class="search-input"
            autocomplete="off"
            autocapitalize="off"
            autocorrect="off"
            name="blog-article-search-keyword"
            placeholder="搜索文章标题或内容..."
            spellcheck="false"
            type="search"
            @keyup.enter="handleSearch"
          />
          <button
            v-if="keywords"
            class="search-clear-btn"
            type="button"
            aria-label="清空搜索"
            @click="keywords = ''"
          >
            <v-icon size="18">mdi-close</v-icon>
          </button>
        </div>

        <!-- 高级筛选切换 -->
        <button class="filter-toggle" type="button" @click="showFilters = !showFilters">
          <v-icon size="16">{{ showFilters ? 'mdi-chevron-up' : 'mdi-tune-variant' }}</v-icon>
          {{ showFilters ? '收起筛选' : '高级筛选' }}
          <span v-if="activeFilterCount > 0" class="filter-badge">{{ activeFilterCount }}</span>
        </button>

        <!-- 高级筛选面板 -->
        <div v-show="showFilters" class="filter-panel">
          <div class="filter-row">
            <div class="filter-field">
              <label class="filter-label">搜索范围</label>
              <select v-model="searchType" class="filter-select">
                <option value="all">标题+正文</option>
                <option value="title">仅标题</option>
                <option value="content">仅正文</option>
              </select>
            </div>
            <div class="filter-field">
              <label class="filter-label">分类</label>
              <select v-model="categoryId" class="filter-select">
                <option :value="null">全部分类</option>
                <option v-for="c in categoryOptions" :key="c.id" :value="c.id">{{ c.categoryName }}</option>
              </select>
            </div>
          </div>
          <div class="filter-row">
            <div class="filter-field">
              <label class="filter-label">标签</label>
              <select v-model="tagId" class="filter-select">
                <option :value="null">全部标签</option>
                <option v-for="t in tagOptions" :key="t.id" :value="t.id">{{ t.tagName }}</option>
              </select>
            </div>
            <div class="filter-field filter-field-date">
              <label class="filter-label">时间范围</label>
              <div class="date-range">
                <input v-model="startTime" type="date" class="filter-date-input" />
                <span class="date-sep">~</span>
                <input v-model="endTime" type="date" class="filter-date-input" />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <button class="filter-search-btn" type="button" @click="handleSearch">
              <v-icon size="16">mdi-magnify</v-icon> 搜索
            </button>
            <button v-if="activeFilterCount > 0" class="filter-reset-btn" type="button" @click="resetFilters">
              重置筛选
            </button>
          </div>
        </div>

        <div class="search-meta">
          <span v-if="keywords.trim() && !searched">正在输入关键词</span>
          <span v-else-if="searched && totalCount > 0">找到 {{ totalCount }} 篇相关文章</span>
          <span v-else-if="!searched">支持标题和正文内容搜索</span>
        </div>

        <div v-if="searchResults.length > 0" class="search-results">
          <div
            v-for="article in searchResults"
            :key="article.id"
            class="search-item"
            @click="goToArticle(article.id)"
          >
            <div class="article-title">{{ article.articleTitle }}</div>
            <div
              v-if="article.articleContent"
              class="article-content"
              v-html="article.articleContent"
            />
            <div class="article-info" v-if="article.categoryName || (article.tagDTOList && article.tagDTOList.length)">
              <span v-if="article.categoryName" class="article-category">
                <v-icon size="13">mdi-folder-outline</v-icon> {{ article.categoryName }}
              </span>
              <span v-for="tag in (article.tagDTOList || []).slice(0, 2)" :key="tag.id" class="article-tag">
                <v-icon size="13">mdi-tag-outline</v-icon> {{ tag.tagName }}
              </span>
            </div>
            <div class="article-footer">
              <span class="article-time">{{ formatDate(article.createTime) }}</span>
              <v-icon size="16">mdi-arrow-right</v-icon>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="totalPages > 1" class="search-pagination">
            <button
              class="page-btn"
              :disabled="currentPage <= 1"
              @click="goPage(currentPage - 1)"
            >
              <v-icon size="18">mdi-chevron-left</v-icon>
            </button>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
            <button
              class="page-btn"
              :disabled="currentPage >= totalPages"
              @click="goPage(currentPage + 1)"
            >
              <v-icon size="18">mdi-chevron-right</v-icon>
            </button>
          </div>
        </div>

        <div v-else-if="searched && (keywords.trim() || hasActiveFilters)" class="search-empty">
          <div class="search-empty-icon">
            <v-icon size="28">mdi-file-search-outline</v-icon>
          </div>
          <strong>没有找到相关文章</strong>
          <span>换个关键词或调整筛选条件试试</span>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useDisplay } from 'vuetify'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { searchArticles, advancedSearchArticles } from '@/api/article'
import { getCategories, getTags } from '@/api/misc'
import { formatDate } from '@/utils/filters'
import { useToast } from '@/composables/useToast'

interface Tag {
  id: number
  tagName: string
}

interface Article {
  id: number
  articleTitle: string
  articleContent?: string
  articleCover?: string
  createTime: string
  categoryId?: number
  categoryName?: string
  tagDTOList?: Tag[]
}

const PAGE_SIZE = 10

const { mobile } = useDisplay()
const router = useRouter()
const uiStore = useUIStore()

const keywords = ref('')
const searchResults = ref<Article[]>([])
const searched = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)

const showFilters = ref(false)
const searchType = ref('all')
const categoryId = ref<number | null>(null)
const tagId = ref<number | null>(null)
const startTime = ref('')
const endTime = ref('')

const categoryOptions = ref<any[]>([])
const tagOptions = ref<any[]>([])
let filterOptionsLoaded = false

const isMobile = computed(() => mobile.value)
const totalPages = computed(() => Math.ceil(totalCount.value / PAGE_SIZE))

const hasActiveFilters = computed(() =>
  searchType.value !== 'all' || categoryId.value !== null || tagId.value !== null || startTime.value !== '' || endTime.value !== ''
)

const activeFilterCount = computed(() => {
  let count = 0
  if (searchType.value !== 'all') count++
  if (categoryId.value !== null) count++
  if (tagId.value !== null) count++
  if (startTime.value) count++
  if (endTime.value) count++
  return count
})

let searchTimer: ReturnType<typeof setTimeout> | null = null

async function loadFilterOptions() {
  if (filterOptionsLoaded) return
  try {
    const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
    const cats = catRes.data.data?.recordList || catRes.data.data || []
    categoryOptions.value = Array.isArray(cats) ? cats : []
    const tags = tagRes.data.data?.recordList || tagRes.data.data || []
    tagOptions.value = Array.isArray(tags) ? tags : []
    filterOptionsLoaded = true
  } catch (e) {
    console.error('加载筛选选项失败:', e)
  }
}

async function handleSearch() {
  const hasKeywords = keywords.value.trim()
  if (!hasKeywords && !hasActiveFilters.value) {
    searchResults.value = []
    searched.value = false
    totalCount.value = 0
    return
  }

  try {
    if (hasActiveFilters.value) {
      const params: any = { current: currentPage.value }
      if (hasKeywords) params.keywords = keywords.value
      if (searchType.value !== 'all') params.searchType = searchType.value
      if (categoryId.value) params.categoryId = categoryId.value
      if (tagId.value) params.tagId = tagId.value
      if (startTime.value) params.startTime = startTime.value + ' 00:00:00'
      if (endTime.value) params.endTime = endTime.value + ' 23:59:59'

      const { data } = await advancedSearchArticles(params)
      searchResults.value = data.data?.recordList || []
      totalCount.value = data.data?.count || 0
    } else {
      const { data } = await searchArticles({ keywords: keywords.value, current: 1 })
      const results = Array.isArray(data.data) ? data.data : data.data?.recordList || []
      searchResults.value = results
      totalCount.value = results.length
    }
    searched.value = true
  } catch (error) {
    useToast({ type: 'error', message: '搜索失败' })
  }
}

function goPage(page: number) {
  currentPage.value = page
  handleSearch()
}

function goToArticle(id: number) {
  uiStore.setSearchFlag(false)
  router.push(`/articles/${id}`)
}

function resetFilters() {
  searchType.value = 'all'
  categoryId.value = null
  tagId.value = null
  startTime.value = ''
  endTime.value = ''
  currentPage.value = 1
  if (keywords.value.trim()) {
    handleSearch()
  }
}

function resetSearchState() {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  keywords.value = ''
  searchResults.value = []
  searched.value = false
  totalCount.value = 0
  currentPage.value = 1
  showFilters.value = false
  searchType.value = 'all'
  categoryId.value = null
  tagId.value = null
  startTime.value = ''
  endTime.value = ''
}

watch(keywords, () => {
  if (searchTimer) clearTimeout(searchTimer)
  currentPage.value = 1
  searchTimer = setTimeout(() => handleSearch(), 250)
})

watch(showFilters, (val) => {
  if (val) loadFilterOptions()
})

watch(
  () => uiStore.searchFlag,
  (value) => {
    if (!value) resetSearchState()
  }
)
</script>

<style scoped>
.search-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(94, 166, 229, 0.18);
  border-radius: 24px !important;
  background:
    radial-gradient(circle at top left, rgba(94, 166, 229, 0.16), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 250, 255, 0.96));
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.22);
}

.close-btn {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.06);
  color: transparent;
  cursor: pointer;
  font-size: 0;
  transition: background 0.2s ease, transform 0.2s ease;
}

.close-btn::before {
  content: "X";
  color: #283044;
  font-size: 17px;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(73, 177, 245, 0.16);
  transform: translateY(-1px);
}

.search-wrapper {
  padding: 34px 34px 30px;
}

.search-head {
  margin-bottom: 20px;
  padding-right: 42px;
}

.search-kicker {
  color: #49b1f5;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.search-title {
  margin: 6px 0 4px;
  color: #253046;
  font-size: 1.6rem;
  line-height: 1.25;
}

.search-subtitle {
  margin: 0;
  color: #6c7384;
  font-size: 0.9rem;
}

.search-input-shell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 58px;
  padding: 0 16px;
  border: 1px solid rgba(73, 177, 245, 0.22);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 16px 32px rgba(73, 177, 245, 0.1);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.search-input-shell:focus-within {
  border-color: rgba(73, 177, 245, 0.64);
  box-shadow:
    0 0 0 4px rgba(73, 177, 245, 0.12),
    0 18px 36px rgba(73, 177, 245, 0.14);
  transform: translateY(-1px);
}

.search-input-icon {
  color: #49b1f5;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #253046;
  font-size: 1rem;
}

.search-input::placeholder {
  color: #a2aaba;
}

.search-clear-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.06);
  color: #6c7384;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s ease, color 0.2s ease;
}

.search-clear-btn:hover {
  background: rgba(73, 177, 245, 0.14);
  color: #2b8fd8;
}

/* 高级筛选 */
.filter-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 6px 14px;
  border: 1px solid rgba(73, 177, 245, 0.2);
  border-radius: 20px;
  background: rgba(73, 177, 245, 0.06);
  color: #49b1f5;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.filter-toggle:hover {
  background: rgba(73, 177, 245, 0.12);
  border-color: rgba(73, 177, 245, 0.36);
}

.filter-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 10px;
  background: #49b1f5;
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
}

.filter-panel {
  margin-top: 12px;
  padding: 16px;
  border: 1px solid rgba(73, 177, 245, 0.14);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
}

.filter-row {
  display: flex;
  gap: 12px;
}

.filter-row + .filter-row {
  margin-top: 10px;
}

.filter-field {
  flex: 1;
  min-width: 0;
}

.filter-field-date {
  flex: 1.4;
}

.filter-label {
  display: block;
  margin-bottom: 4px;
  color: #6c7384;
  font-size: 0.75rem;
  font-weight: 600;
}

.filter-select,
.filter-date-input {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  border: 1px solid rgba(73, 177, 245, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: #253046;
  font-size: 0.85rem;
  outline: none;
  transition: border-color 0.2s ease;
}

.filter-select:focus,
.filter-date-input:focus {
  border-color: rgba(73, 177, 245, 0.5);
}

.date-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.date-range .filter-date-input {
  flex: 1;
  min-width: 0;
}

.date-sep {
  color: #a2aaba;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.filter-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.filter-search-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 18px;
  border: none;
  border-radius: 10px;
  background: #49b1f5;
  color: #fff;
  font-size: 0.84rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.filter-search-btn:hover {
  background: #3a9fe0;
}

.filter-reset-btn {
  padding: 7px 14px;
  border: 1px solid rgba(73, 177, 245, 0.22);
  border-radius: 10px;
  background: transparent;
  color: #6c7384;
  font-size: 0.84rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.filter-reset-btn:hover {
  background: rgba(73, 177, 245, 0.08);
}

.search-meta {
  margin: 14px 2px 10px;
  color: #7a8394;
  font-size: 0.78rem;
  font-weight: 600;
}

.search-results {
  max-height: 430px;
  overflow-y: auto;
  padding-right: 4px;
}

.search-results::-webkit-scrollbar {
  width: 6px;
}

.search-results::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.28);
}

.search-item {
  padding: 16px 18px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.search-item + .search-item {
  margin-top: 10px;
}

.search-item:hover {
  border-color: rgba(73, 177, 245, 0.24);
  background: rgba(239, 248, 255, 0.92);
  box-shadow: 0 18px 36px rgba(73, 177, 245, 0.12);
  transform: translateY(-2px);
}

.article-title {
  margin-bottom: 8px;
  color: #253046;
  font-weight: 800;
  line-height: 1.5;
}

.article-content {
  color: #667085;
  font-size: 0.86rem;
  line-height: 1.8;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-info {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.article-category,
.article-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 0.74rem;
  font-weight: 600;
}

.article-category {
  background: rgba(240, 184, 77, 0.12);
  color: #c49230;
}

.article-tag {
  background: rgba(101, 131, 238, 0.1);
  color: #6583ee;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  color: #6aaee8;
}

.article-time {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.1);
  font-size: 0.76rem;
  font-weight: 700;
}

.search-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 14px;
  padding: 8px 0;
}

.page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(73, 177, 245, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  color: #49b1f5;
  cursor: pointer;
  transition: background 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(73, 177, 245, 0.1);
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-info {
  color: #7a8394;
  font-size: 0.82rem;
  font-weight: 600;
}

.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  padding: 34px 20px;
  border: 1px dashed rgba(73, 177, 245, 0.24);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.56);
  color: #6c7384;
}

.search-empty strong {
  color: #253046;
  font-size: 1rem;
}

.search-empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(73, 177, 245, 0.16), rgba(128, 208, 255, 0.22));
  color: #49b1f5;
}

:global(.dark) .search-card {
  background:
    radial-gradient(circle at top left, rgba(92, 155, 230, 0.18), transparent 34%),
    linear-gradient(180deg, rgba(31, 38, 52, 0.98), rgba(19, 24, 34, 0.98));
  border-color: rgba(186, 200, 224, 0.16);
  box-shadow: 0 30px 84px rgba(0, 0, 0, 0.42);
}

:global(.dark) .close-btn,
:global(.dark) .search-clear-btn {
  background: rgba(255, 255, 255, 0.08);
}

:global(.dark) .close-btn::before,
:global(.dark) .search-title,
:global(.dark) .article-title,
:global(.dark) .search-empty strong {
  color: rgba(246, 249, 255, 0.94);
}

:global(.dark) .search-subtitle,
:global(.dark) .search-meta,
:global(.dark) .article-content,
:global(.dark) .search-empty {
  color: rgba(214, 220, 235, 0.72);
}

:global(.dark) .search-input-shell,
:global(.dark) .search-item,
:global(.dark) .search-empty {
  background: rgba(14, 19, 28, 0.46);
  border-color: rgba(186, 200, 224, 0.14);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

:global(.dark) .search-input {
  color: rgba(246, 249, 255, 0.94);
}

:global(.dark) .search-input::placeholder {
  color: rgba(214, 220, 235, 0.48);
}

:global(.dark) .search-item:hover {
  background: rgba(32, 43, 60, 0.84);
  border-color: rgba(92, 155, 230, 0.3);
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.26);
}

:global(.dark) .filter-panel {
  background: rgba(14, 19, 28, 0.5);
  border-color: rgba(186, 200, 224, 0.12);
}

:global(.dark) .filter-select,
:global(.dark) .filter-date-input {
  background: rgba(14, 19, 28, 0.6);
  border-color: rgba(186, 200, 224, 0.14);
  color: rgba(246, 249, 255, 0.9);
}

:global(.dark) .filter-label {
  color: rgba(214, 220, 235, 0.6);
}

@media (max-width: 600px) {
  .search-card {
    min-height: 100vh;
    border-radius: 0 !important;
  }

  .search-wrapper {
    padding: 30px 18px 22px;
  }

  .search-title {
    font-size: 1.35rem;
  }

  .search-results {
    max-height: calc(100vh - 280px);
  }

  .filter-row {
    flex-direction: column;
    gap: 10px;
  }

  .date-range {
    flex-wrap: nowrap;
  }
}
</style>
