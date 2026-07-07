<template>
  <div>
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">标签</h1>
    </div>
    <div class="tag-page">
      <section class="tag-panel">
        <h1>标签</h1>

        <div v-if="tagList.length" class="tag-cloud">
          <router-link
            v-for="(item, index) of tagList"
            :key="item.id"
            :to="'/tags/' + item.id"
            class="tag-word"
            :style="tagStyle(item, index)"
          >
            {{ item.tagName }}<sup>{{ item.articleCount ?? 0 }}</sup>
          </router-link>
        </div>

        <div v-if="topTags.length" class="tag-chart">
          <h2>Top 10 标签统计图</h2>
          <div class="chart-wrap">
            <div class="chart-y-axis">
              <span>{{ maxArticleCount }}</span>
              <span>{{ Math.round(maxArticleCount * 0.75) }}</span>
              <span>{{ Math.round(maxArticleCount * 0.5) }}</span>
              <span>{{ Math.round(maxArticleCount * 0.25) }}</span>
              <span>0</span>
            </div>
            <div class="chart-main">
              <div class="chart-bars">
                <router-link
                  v-for="item of topTags"
                  :key="item.id"
                  :to="'/tags/' + item.id"
                  class="bar-item"
                  :title="`${item.tagName}: ${item.articleCount ?? 0}`"
                >
                  <span class="bar-stack">
                    <span class="bar-value">{{ item.articleCount ?? 0 }}</span>
                    <span class="bar" :style="{ height: getBarHeight(item) }"></span>
                  </span>
                  <span class="bar-label">{{ item.tagName }}</span>
                </router-link>
              </div>
            </div>
          </div>
        </div>

        <div v-if="tagList.length === 0" class="empty-tip">
          暂无标签
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, type CSSProperties } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getTags } from '@/api/misc'

interface Tag {
  id: number
  tagName: string
  articleCount?: number
}

const blogInfoStore = useBlogInfoStore()
const tagList = ref<Tag[]>([])
const accentColors = ['#2f7d73', '#3479a8', '#6c5d96', '#8c5d6f', '#5e7f3f', '#9a6a37']

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const tagPage = pageList.find(item => item.pageLabel === 'tag')
  const coverUrl = tagPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

const topTags = computed(() => {
  return [...tagList.value]
    .sort((a, b) => (b.articleCount ?? 0) - (a.articleCount ?? 0))
    .slice(0, 10)
})

const maxArticleCount = computed(() => {
  return Math.max(...topTags.value.map(item => item.articleCount ?? 0), 1)
})

function tagStyle(tag: Tag, index: number): CSSProperties {
  const count = tag.articleCount ?? 0
  const scale = count / maxArticleCount.value
  return {
    color: accentColors[index % accentColors.length],
    fontSize: `${18 + Math.round(scale * 16)}px`,
    fontWeight: String(600 + Math.round(scale * 200))
  }
}

function getBarHeight(tag: Tag) {
  const percent = ((tag.articleCount ?? 0) / maxArticleCount.value) * 100
  return `${Math.max(percent, 4)}%`
}

async function listTags() {
  try {
    const { data } = await getTags()
    tagList.value = data.data?.recordList || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

onMounted(() => {
  listTags()
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 380px;
  color: #eee;
}

.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-title {
  position: absolute;
  bottom: 50px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.tag-page {
  min-height: 100vh;
  padding: 34px 18px 46px;
  background: #fff;
}

.tag-panel {
  width: min(1180px, 100%);
  margin: 0 auto;
  padding: 32px 42px 40px;
  border: 1px solid rgba(42, 183, 190, 0.34);
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 252, 252, 0.98)),
    #fff;
  box-shadow: 0 18px 42px rgba(33, 74, 92, 0.08);
}

.tag-panel h1 {
  margin: 0 0 22px;
  color: #1f2d3d;
  font-size: 28px;
  font-weight: 800;
  text-align: center;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: center;
  gap: 16px 20px;
  min-height: 116px;
  padding: 4px 0 24px;
}

.tag-word {
  display: inline-flex;
  align-items: flex-start;
  color: #2f7d73;
  line-height: 1.05;
  text-decoration: none;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.tag-word:hover {
  opacity: 0.78;
  transform: translateY(-2px);
}

.tag-word sup {
  margin-left: 2px;
  color: currentColor;
  font-size: 0.62em;
  font-weight: 800;
  line-height: 1;
}

.tag-chart {
  margin-top: 4px;
}

.tag-chart h2 {
  margin: 0 0 22px;
  color: #394656;
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 0;
  text-align: center;
}

.chart-wrap {
  display: grid;
  grid-template-columns: 44px 1fr;
  width: min(920px, 100%);
  height: 230px;
  margin: 0 auto;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0 10px 26px 0;
  color: #536170;
  font-size: 12px;
  text-align: right;
}

.chart-main {
  position: relative;
  border-bottom: 1px solid #2f4050;
  border-left: 1px solid #2f4050;
}

.chart-main::before {
  content: '文章篇数';
  position: absolute;
  top: -23px;
  left: -28px;
  color: #536170;
  font-size: 12px;
}

.chart-bars {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  align-items: end;
  gap: 18px;
  height: 100%;
  padding: 0 10px;
}

.bar-item {
  position: relative;
  display: grid;
  grid-template-rows: 1fr 24px;
  height: 100%;
  min-width: 0;
  color: #4f5f6f;
  text-decoration: none;
}

.bar-stack {
  position: relative;
  display: flex;
  align-items: flex-end;
  height: 100%;
}

.bar {
  display: block;
  width: 100%;
  min-height: 8px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #89f0b8 0%, #25c6d9 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45);
  transition: filter 0.2s ease, transform 0.2s ease;
}

.bar-item:hover .bar {
  filter: saturate(1.08);
  transform: translateY(-2px);
}

.bar-value {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 6px);
  color: #3b4a5a;
  font-size: 12px;
  opacity: 0;
  transform: translateX(-50%);
  transition: opacity 0.2s ease;
}

.bar-item:hover .bar-value {
  opacity: 1;
}

.bar-label {
  display: block;
  overflow: hidden;
  padding-top: 8px;
  color: #4f5f6f;
  font-size: 12px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tip {
  padding: 48px 0;
  color: #8b96a8;
  text-align: center;
}

.dark .tag-page {
  background: #151b23;
}

.dark .tag-panel {
  border-color: rgba(79, 202, 211, 0.26);
  background: #1d2630;
  box-shadow: none;
}

.dark .tag-panel h1,
.dark .tag-chart h2 {
  color: #edf5f7;
}

.dark .chart-main {
  border-color: #8091a3;
}

.dark .chart-main::before,
.dark .chart-y-axis,
.dark .bar-label {
  color: #aab7c6;
}

@media (max-width: 760px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    bottom: 50px;
    font-size: 1.5rem;
  }

  .tag-page {
    padding: 18px 10px 32px;
  }

  .tag-panel {
    padding: 24px 16px 28px;
    border-radius: 12px;
  }

  .tag-cloud {
    justify-content: flex-start;
    gap: 12px 14px;
  }

  .chart-wrap {
    grid-template-columns: 34px 1fr;
    height: 220px;
    overflow-x: auto;
  }

  .chart-main {
    min-width: 620px;
  }
}
</style>
