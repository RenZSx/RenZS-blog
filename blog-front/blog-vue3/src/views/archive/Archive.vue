<template>
  <div>
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">归档</h1>
    </div>
    <div class="archive-page">
      <div class="petal petal-one"></div>
      <div class="petal petal-two"></div>
      <div class="petal petal-three"></div>
    <section class="timeline-stage">
      <div v-if="archiveList.length" class="timeline-board">
        <div class="timeline-title">
          <span class="title-line"></span>
          <div>
            <h1>时间线</h1>
            <p>The soul is walking</p>
          </div>
        </div>

        <router-link
          v-for="(item, index) of archiveList"
          :key="item.id"
          :to="'/articles/' + item.id"
          class="timeline-card"
          :style="cardGridStyle(index)"
        >
          <span
            v-if="hasNextInRow(index)"
            :class="['connector', isReverseRow(index) ? 'to-left' : 'to-right']"
          ></span>
          <span
            v-if="hasTurnConnector(index)"
            :class="['turn-connector', isReverseRow(index) ? 'turn-left' : 'turn-right']"
          ></span>
          <div class="card-cover" :style="coverFallbackStyle(index)">
            <img
              v-if="item.articleCover"
              :src="item.articleCover"
              :alt="item.articleTitle"
            />
            <v-icon v-else size="38" color="rgba(255,255,255,0.9)">mdi-image-outline</v-icon>
          </div>
          <h2>{{ item.articleTitle }}</h2>
          <div class="card-meta">
            <span>{{ formatDate(item.createTime) }}</span>
            <strong>{{ getTimelineNumber(index) }}</strong>
          </div>
        </router-link>
      </div>

      <div v-else class="archive-empty">
        <v-icon size="42" color="#8ca0b5">mdi-calendar-search</v-icon>
        <span>暂无归档文章</span>
      </div>

      <div class="pagination-wrapper">
        <v-pagination
          color="#90a4c2"
          v-model="current"
          :length="totalPages"
          total-visible="7"
        />
      </div>
    </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, type CSSProperties } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getArchives } from '@/api/article'
import { formatDate } from '@/utils/filters'

interface ArchiveItem {
  id: number
  articleTitle: string
  articleCover?: string
  createTime: string
}

const blogInfoStore = useBlogInfoStore()
const current = ref(1)
const pageSize = 15
const count = ref(0)
const archiveList = ref<ArchiveItem[]>([])
const timelineColumns = 5
const coverPalettes = [
  ['#85d8ff', '#5b8def'],
  ['#88f0d0', '#39a6d8'],
  ['#a8b8ff', '#7d72f0'],
  ['#ffd28a', '#ff9e7a'],
  ['#c6e6ff', '#8aa7ff']
]

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const archivePage = pageList.find(item => item.pageLabel === 'archive')
  const coverUrl = archivePage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

const totalPages = computed(() => Math.max(1, Math.ceil(count.value / pageSize)))

function getGridPoint(index: number) {
  const slot = index + 1
  const rowIndex = Math.floor(slot / timelineColumns)
  const slotInRow = slot % timelineColumns
  const reverse = rowIndex % 2 === 1
  const column = reverse ? timelineColumns - slotInRow : slotInRow + 1
  return {
    row: rowIndex + 1,
    column,
    reverse
  }
}

function cardGridStyle(index: number): CSSProperties {
  const point = getGridPoint(index)
  return {
    gridColumn: String(point.column),
    gridRow: String(point.row)
  }
}

function isReverseRow(index: number) {
  return getGridPoint(index).reverse
}

function hasNextInRow(index: number) {
  const nextIndex = index + 1
  if (nextIndex >= archiveList.value.length) {
    return false
  }
  return getGridPoint(index).row === getGridPoint(nextIndex).row
}

function hasTurnConnector(index: number) {
  const nextIndex = index + 1
  if (nextIndex >= archiveList.value.length) {
    return false
  }
  return getGridPoint(index).row !== getGridPoint(nextIndex).row
}

function getTimelineNumber(index: number) {
  const number = count.value - ((current.value - 1) * pageSize + index)
  return number > 0 ? number : index + 1
}

function coverFallbackStyle(index: number): CSSProperties {
  const palette = coverPalettes[index % coverPalettes.length]
  return {
    background: `linear-gradient(135deg, ${palette[0]}, ${palette[1]})`
  }
}

async function listArchives() {
  try {
    const { data } = await getArchives({ current: current.value, size: pageSize })
    archiveList.value = data.data?.recordList || []
    count.value = data.data?.count || archiveList.value.length
  } catch (error) {
    console.error('获取归档失败:', error)
  }
}

watch(current, () => {
  if (current.value > totalPages.value) {
    current.value = totalPages.value
    return
  }
  listArchives()
})

onMounted(() => {
  listArchives()
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

.archive-page {
  position: relative;
  min-height: 100vh;
  padding: 38px 24px 52px;
  overflow: hidden;
  background: #f4f7fb;
}

.archive-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(ellipse at 50% 50%, rgba(245, 155, 185, 0.34) 0 28%, transparent 30%),
    radial-gradient(ellipse at 50% 50%, rgba(245, 155, 185, 0.28) 0 24%, transparent 26%);
  background-position: 8% 22%, 72% 68%;
  background-size: 26px 16px, 22px 14px;
  opacity: 0.72;
  pointer-events: none;
}

.petal {
  position: absolute;
  z-index: 0;
  width: 12px;
  height: 22px;
  border-radius: 70% 30% 70% 30%;
  background: rgba(248, 139, 177, 0.45);
  filter: blur(0.2px);
  transform: rotate(-26deg);
}

.petal-one {
  top: 20%;
  left: 24%;
}

.petal-two {
  top: 43%;
  right: 27%;
  transform: rotate(18deg);
}

.petal-three {
  right: 8%;
  bottom: 17%;
  transform: rotate(-12deg);
}

.timeline-stage {
  position: relative;
  z-index: 1;
  width: min(1080px, 100%);
  margin: 0 auto;
}

.timeline-board {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  grid-auto-rows: auto;
  column-gap: 50px;
  row-gap: 34px;
  align-items: start;
}

.timeline-title {
  display: flex;
  grid-column: 1;
  grid-row: 1;
  align-items: center;
  gap: 12px;
  min-height: 86px;
}

.title-line {
  width: 4px;
  height: 36px;
  background: #111827;
}

.timeline-title h1 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.1;
}

.timeline-title p {
  margin: 5px 0 0;
  color: #111827;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 11px;
}

.timeline-card {
  position: relative;
  display: block;
  min-width: 0;
  color: #111827;
  text-decoration: none;
  transition: transform 0.24s ease, filter 0.24s ease;
}

.timeline-card:hover {
  filter: drop-shadow(0 12px 16px rgba(70, 90, 110, 0.12));
  transform: translateY(-4px);
}

.card-cover {
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 8px;
  background: #d8e3ef;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.28s ease;
}

.timeline-card:hover .card-cover img {
  transform: scale(1.04);
}

.timeline-card h2 {
  min-width: 0;
  margin: 9px 0 4px;
  overflow: hidden;
  color: #111827;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #8b96a8;
  font-size: 12px;
  line-height: 1.2;
}

.card-meta strong {
  flex: 0 0 auto;
  color: #9ca3af;
  font-size: 12px;
  font-weight: 600;
}

.connector {
  position: absolute;
  top: 47px;
  z-index: 0;
  width: 50px;
  height: 1px;
  border-top: 1px dashed rgba(96, 108, 124, 0.62);
  pointer-events: none;
}

.connector::after {
  content: '';
  position: absolute;
  top: -4px;
  width: 7px;
  height: 7px;
  border-top: 1px solid rgba(96, 108, 124, 0.74);
  border-right: 1px solid rgba(96, 108, 124, 0.74);
}

.connector.to-right {
  left: 100%;
}

.connector.to-right::after {
  right: 0;
  transform: rotate(45deg);
}

.connector.to-left {
  right: 100%;
}

.connector.to-left::after {
  left: 0;
  transform: rotate(-135deg);
}

.turn-connector {
  position: absolute;
  top: 47px;
  z-index: 0;
  width: 26px;
  height: calc(100% + 34px);
  border-top: 1px dashed rgba(96, 108, 124, 0.62);
  border-bottom: 1px dashed rgba(96, 108, 124, 0.62);
  pointer-events: none;
}

.turn-connector::after {
  content: '';
  position: absolute;
  bottom: -4px;
  width: 7px;
  height: 7px;
  border-top: 1px solid rgba(96, 108, 124, 0.74);
  border-right: 1px solid rgba(96, 108, 124, 0.74);
}

.turn-right {
  left: 100%;
  border-right: 1px dashed rgba(96, 108, 124, 0.62);
  border-radius: 0 10px 10px 0;
}

.turn-right::after {
  right: 18px;
  transform: rotate(135deg);
}

.turn-left {
  right: 100%;
  border-left: 1px dashed rgba(96, 108, 124, 0.62);
  border-radius: 10px 0 0 10px;
}

.turn-left::after {
  left: 18px;
  transform: rotate(-45deg);
}

.archive-empty {
  display: grid;
  place-items: center;
  gap: 10px;
  min-height: 260px;
  color: #8ca0b5;
}

.pagination-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  padding-top: 42px;
}

:deep(.v-pagination__item--is-active .v-btn) {
  box-shadow: 0 8px 18px rgba(144, 164, 194, 0.24);
}

.dark .archive-page {
  background: #151b23;
}

.dark .timeline-title h1,
.dark .timeline-title p,
.dark .timeline-card,
.dark .timeline-card h2 {
  color: #f3f7fb;
}

.dark .title-line {
  background: #f3f7fb;
}

.dark .card-meta,
.dark .card-meta strong {
  color: #9aa8b8;
}

@media (max-width: 1180px) {
  .timeline-stage {
    width: min(920px, 100%);
  }

  .timeline-board {
    column-gap: 34px;
  }

  .connector {
    width: 34px;
  }
}

@media (max-width: 900px) {
  .timeline-board {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    column-gap: 28px;
  }

  .timeline-card {
    grid-column: auto !important;
    grid-row: auto !important;
  }

  .timeline-title {
    grid-column: 1 / -1;
    grid-row: auto;
  }

  .connector,
  .turn-connector {
    display: none;
  }
}

@media (max-width: 620px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    bottom: 50px;
    font-size: 1.5rem;
  }

  .archive-page {
    padding: 26px 14px 38px;
  }

  .timeline-board {
    grid-template-columns: 1fr;
    row-gap: 24px;
  }

  .timeline-card h2 {
    white-space: normal;
  }
}
</style>
