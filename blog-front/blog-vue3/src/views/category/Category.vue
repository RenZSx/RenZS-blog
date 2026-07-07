<template>
  <div>
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">分类</h1>
    </div>
    <section class="category-page">
      <div class="category-panel">
        <div class="category-header">
          <span>分类</span>
          <strong>{{ count }}</strong>
        </div>

        <div v-if="categoryList.length" class="category-content">
          <div class="pie-card">
            <div class="pie-chart">
              <svg viewBox="0 0 200 200" role="img" aria-label="分类文章占比饼图">
                <circle class="pie-track" cx="100" cy="100" r="78" />
                <circle
                  v-for="segment of chartSegments"
                  :key="segment.id"
                  class="pie-segment"
                  cx="100"
                  cy="100"
                  r="78"
                  :stroke="segment.color"
                  :stroke-dasharray="`${segment.length} ${circumference - segment.length}`"
                  :stroke-dashoffset="segment.offset"
                />
              </svg>
              <div class="pie-center">
                <strong>{{ totalArticles }}</strong>
                <span>篇文章</span>
              </div>
            </div>
            <p>按分类统计文章分布</p>
          </div>

          <div class="category-rank">
            <router-link
              v-for="(item, index) of categoryList"
              :key="item.id"
              :to="'/categories/' + item.id"
              class="category-row"
            >
              <span class="category-dot" :style="{ background: getCategoryColor(index) }"></span>
              <span class="category-name">{{ item.categoryName }}</span>
              <span class="category-percent">{{ getPercent(item.articleCount) }}%</span>
              <strong>{{ item.articleCount }}</strong>
            </router-link>
          </div>
        </div>

        <div v-else class="empty-tip">
          暂无分类
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getCategories } from '@/api/misc'

interface Category {
  id: number
  categoryName: string
  articleCount: number
}

const blogInfoStore = useBlogInfoStore()
const categoryList = ref<Category[]>([])
const count = ref(0)
const circumference = 2 * Math.PI * 78
const chartColors = ['#36c6d3', '#6dd7a8', '#7aa7ff', '#b18cff', '#ffb86c', '#f27f8f', '#7bd4d0', '#91c86b']

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const categoryPage = pageList.find(item => item.pageLabel === 'category')
  const coverUrl = categoryPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

const totalArticles = computed(() => {
  return categoryList.value.reduce((sum, item) => sum + (item.articleCount || 0), 0)
})

const chartSegments = computed(() => {
  let offset = 0
  return categoryList.value.map((item, index) => {
    const length = totalArticles.value ? (item.articleCount / totalArticles.value) * circumference : 0
    const segment = {
      id: item.id,
      color: getCategoryColor(index),
      length,
      offset: -offset
    }
    offset += length
    return segment
  })
})

function getCategoryColor(index: number) {
  return chartColors[index % chartColors.length]
}

function getPercent(countValue: number) {
  if (!totalArticles.value) {
    return 0
  }
  return Math.round((countValue / totalArticles.value) * 100)
}

async function listCategories() {
  try {
    const { data } = await getCategories()
    categoryList.value = data.data?.recordList || []
    count.value = data.data?.count || 0
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

onMounted(() => {
  listCategories()
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

.category-page {
  min-height: 520px;
  padding: 34px 18px 46px;
  background: #fff;
}

.category-panel {
  width: min(980px, 100%);
  margin: 0 auto;
  padding: 30px 36px 36px;
  border: 1px solid rgba(42, 183, 190, 0.3);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 18px 42px rgba(33, 74, 92, 0.08);
}

.category-header {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10px;
  margin-bottom: 28px;
  color: #1f2d3d;
}

.category-header span {
  font-size: 30px;
  font-weight: 800;
}

.category-header strong {
  color: #36a9b7;
  font-size: 18px;
}

.category-content {
  display: grid;
  grid-template-columns: minmax(260px, 360px) 1fr;
  gap: 34px;
  align-items: center;
}

.pie-card {
  display: grid;
  justify-items: center;
  gap: 14px;
}

.pie-card p {
  margin: 0;
  color: #6b7785;
  font-size: 14px;
}

.pie-chart {
  position: relative;
  width: min(320px, 100%);
  aspect-ratio: 1;
}

.pie-chart svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.pie-track,
.pie-segment {
  fill: none;
  stroke-width: 28;
}

.pie-track {
  stroke: #eef3f6;
}

.pie-segment {
  stroke-linecap: butt;
  transition: stroke-dasharray 0.3s ease;
}

.pie-center {
  position: absolute;
  inset: 50%;
  display: grid;
  place-items: center;
  width: 118px;
  height: 118px;
  border-radius: 50%;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(42, 183, 190, 0.12);
  transform: translate(-50%, -50%);
}

.pie-center strong,
.pie-center span {
  display: block;
}

.pie-center strong {
  align-self: end;
  color: #1f2d3d;
  font-size: 32px;
  line-height: 1;
}

.pie-center span {
  align-self: start;
  margin-top: 6px;
  color: #7b8794;
  font-size: 13px;
}

.category-rank {
  display: grid;
  gap: 12px;
}

.category-row {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #eef3f6;
  border-radius: 8px;
  color: #2d3b48;
  text-decoration: none;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.category-row:hover {
  border-color: rgba(42, 183, 190, 0.38);
  box-shadow: 0 10px 22px rgba(33, 74, 92, 0.08);
  transform: translateY(-2px);
}

.category-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.category-name {
  overflow: hidden;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-percent {
  color: #7b8794;
  font-size: 13px;
}

.category-row strong {
  color: #1f2d3d;
  font-size: 16px;
}

.empty-tip {
  padding: 48px 0;
  color: #8b96a8;
  text-align: center;
}

.dark .category-page {
  background: #151b23;
}

.dark .category-panel,
.dark .pie-center {
  background: #1d2630;
  box-shadow: none;
}

.dark .category-header,
.dark .pie-center strong,
.dark .category-row,
.dark .category-row strong {
  color: #edf5f7;
}

.dark .pie-track {
  stroke: #2a3541;
}

.dark .category-row {
  border-color: #2d3945;
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    bottom: 50px;
    font-size: 1.5rem;
  }

  .category-page {
    padding: 18px 10px 32px;
  }

  .category-panel {
    padding: 24px 16px 28px;
    border-radius: 12px;
  }

  .category-content {
    grid-template-columns: 1fr;
  }

  .pie-chart {
    width: min(260px, 100%);
  }
}
</style>
