<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <bx-icon name="search" :size="32" color="#909399" />
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索文章..."
          placeholder-class="ph"
          confirm-type="search"
          :focus="autoFocus"
          @confirm="doSearch"
        />
        <view v-if="keyword" class="clear-btn" @click="clearInput">
          <bx-icon name="xCircle" :size="32" color="#c0c4cc" />
        </view>
      </view>
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <!-- 历史搜索 -->
    <view v-if="!hasSearched && history.length > 0" class="history-section">
      <view class="section-header">
        <text class="section-title">搜索历史</text>
        <view class="clear-history" @click="clearHistory">
          <bx-icon name="trash" :size="24" color="#909399" />
          <text class="clear-text">清空</text>
        </view>
      </view>
      <view class="chip-list">
        <text
          v-for="(item, idx) in history"
          :key="idx"
          class="chip"
          @click="searchFromHistory(item)"
        >{{ item }}</text>
      </view>
    </view>

    <!-- 搜索中 -->
    <view v-if="loading" class="loading-tip">
      <text>搜索中...</text>
    </view>

    <!-- 无结果 -->
    <view v-else-if="hasSearched && results.length === 0" class="empty-state">
      <bx-icon name="search" :size="120" color="#c0c4cc" />
      <text class="empty-title">没找到相关文章</text>
      <text class="empty-sub">换个关键词试试?</text>
    </view>

    <!-- 结果列表 -->
    <view v-else-if="results.length > 0" class="result-list">
      <view class="result-header">
        <text class="result-count">找到 {{ results.length }} 篇</text>
      </view>
      <view
        v-for="(article, idx) in results"
        :key="article.id"
        class="article-card fade-in-up"
        :style="{ animationDelay: `${Math.min(idx * 40, 200)}ms` }"
        @click="goDetail(article.id)"
      >
        <view class="card-body">
          <view class="card-text">
            <text class="card-title text-ellipsis-2" v-html="highlight(article.articleTitle)" />
            <text class="card-content text-ellipsis-2">{{ stripContent(article.articleContent) }}</text>
            <view class="card-meta">
              <bx-icon name="eye" :size="22" color="#909399" />
              <text class="meta-text">{{ article.viewsCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 初始空态 -->
    <view v-else-if="!hasSearched && history.length === 0" class="initial-tip">
      <bx-icon name="feather" :size="120" color="#c0c4cc" />
      <text class="initial-title">输入关键词开始搜索</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { searchArticles } from '@/api/article'
import { useThemeClass } from '@/composables/useThemeClass'
import { SEARCH_HISTORY_KEY, SEARCH_HISTORY_MAX } from '@/utils/config'

const { isDark } = useThemeClass()

const keyword = ref('')
const results = ref([])
const loading = ref(false)
const hasSearched = ref(false)
const autoFocus = ref(false)
const history = ref([])

function loadHistory() {
  const raw = uni.getStorageSync(SEARCH_HISTORY_KEY)
  history.value = Array.isArray(raw) ? raw : []
}

function saveHistory(word) {
  if (!word) return
  // 去重置顶
  const next = [word, ...history.value.filter((w) => w !== word)].slice(0, SEARCH_HISTORY_MAX)
  history.value = next
  uni.setStorageSync(SEARCH_HISTORY_KEY, next)
}

function clearHistory() {
  uni.showModal({
    title: '清空历史',
    content: '确定清空所有搜索历史?',
    success: (res) => {
      if (!res.confirm) return
      history.value = []
      uni.removeStorageSync(SEARCH_HISTORY_KEY)
    }
  })
}

async function doSearch() {
  const word = keyword.value.trim()
  if (!word) return
  loading.value = true
  hasSearched.value = true
  try {
    const res = await searchArticles(word)
    if (res.flag && res.data) {
      // 后端 PageResult 字段是 recordList(不是 records)
      results.value = Array.isArray(res.data)
        ? res.data
        : (res.data.recordList || res.data.records || [])
    } else {
      results.value = []
    }
    saveHistory(word)
  } finally {
    loading.value = false
  }
}

function searchFromHistory(word) {
  keyword.value = word
  doSearch()
}

function clearInput() {
  keyword.value = ''
  results.value = []
  hasSearched.value = false
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/article/article?id=${id}` })
}

function goBack() {
  uni.navigateBack({ delta: 1 })
}

// 高亮匹配关键词(标题用)
function highlight(text) {
  if (!text || !keyword.value) return text
  const reg = new RegExp(`(${escapeReg(keyword.value)})`, 'gi')
  return text.replace(reg, '<span style="color:#3a3a3a;font-weight:700;">$1</span>')
}

function escapeReg(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function stripContent(c) {
  if (!c) return ''
  // 去 markdown 符号,只取前 80 字预览
  return c.replace(/[#*`>\-!\[\]\(\)]/g, '').replace(/\s+/g, ' ').trim().slice(0, 80)
}

onLoad(() => {
  loadHistory()
  // 200ms 后自动 focus 输入框(避免直接 focus 导致键盘动画卡)
  setTimeout(() => { autoFocus.value = true }, 200)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
}

/* ========= 搜索栏 ========= */
.search-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: var(--bg-card);
  gap: 16rpx;
  border-bottom: 2rpx solid var(--border-color);
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  height: 72rpx;
  padding: 0 20rpx;
  background: var(--bg-soft);
  border-radius: 36rpx;
  gap: 12rpx;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: var(--text-primary);
}

.ph { color: var(--text-placeholder); }

.clear-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  font-size: 28rpx;
  color: var(--text-regular);
  padding: 0 8rpx;
}

/* ========= 历史 ========= */
.history-section {
  padding: 32rpx 32rpx 16rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.clear-history {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
}

.clear-text {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.chip {
  display: inline-block;
  padding: 12rpx 24rpx;
  background: var(--bg-card);
  color: var(--text-regular);
  font-size: 26rpx;
  border-radius: 32rpx;
  border: 2rpx solid var(--border-color);
}

.chip:active {
  background: rgba(71, 85, 105, 0.08);
  border-color: #3a3a3a;
  color: #3a3a3a;
}

/* ========= 结果 ========= */
.loading-tip {
  text-align: center;
  color: var(--text-secondary);
  padding: 80rpx 0;
  font-size: 26rpx;
}

.result-list {
  padding: 16rpx 24rpx;
}

.result-header {
  padding: 12rpx 16rpx 16rpx;
}

.result-count {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.article-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-md);
  opacity: 0;
  animation-fill-mode: both;
}

.article-card:active {
  transform: scale(0.98);
}

.card-body {
  display: flex;
  padding: 24rpx;
}

.card-text {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.45;
  margin-bottom: 12rpx;
  display: block;
}

.card-content {
  display: block;
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-text {
  font-size: 22rpx;
  color: var(--text-secondary);
}

/* ========= 空态 ========= */
.empty-state,
.initial-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 48rpx 80rpx;
  color: var(--text-secondary);
}

.empty-title,
.initial-title {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: var(--text-regular);
}

.empty-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.7;
}
</style>
