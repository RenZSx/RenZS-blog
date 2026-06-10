<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">归档</h1>
    </div>
    <!-- 归档列表 -->
    <v-card class="blog-container archive-container">
      <div class="archive-header">
        <v-icon size="20" color="#00C4B6">mdi-file-document-multiple</v-icon>
        目前共计 {{ count }} 篇文章，继续加油
      </div>
      <div class="timeline">
        <div
          v-for="item of archiveList"
          :key="item.id"
          class="timeline-item"
        >
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <span class="time">{{ formatDate(item.createTime) }}</span>
            <router-link :to="'/articles/' + item.id" class="article-link">
              {{ item.articleTitle }}
            </router-link>
          </div>
        </div>
      </div>
      <!-- 分页按钮 -->
      <div class="pagination-wrapper">
        <v-pagination
          color="#00C4B6"
          v-model="current"
          :length="Math.ceil(count / 10)"
          total-visible="7"
        />
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getArchives } from '@/api/article'
import { formatDate } from '@/utils/filters'

interface ArchiveItem {
  id: number
  articleTitle: string
  createTime: string
}

const blogInfoStore = useBlogInfoStore()

const current = ref(1)
const count = ref(0)
const archiveList = ref<ArchiveItem[]>([])

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const archivePage = pageList.find(item => item.pageLabel === 'archive')
  const coverUrl = archivePage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

async function listArchives() {
  try {
    const { data } = await getArchives()
    // API 返回的是完整列表，需要分页处理
    const allItems = data.data?.recordList || []
    count.value = data.data?.count || allItems.length
    archiveList.value = allItems
  } catch (error) {
    console.error('获取归档失败:', error)
  }
}

// 监听分页变化
watch(current, async () => {
  try {
    const { data } = await getArchives()
    archiveList.value = data.data?.recordList || []
    count.value = data.data?.count || archiveList.value.length
  } catch (error) {
    console.error('获取归档失败:', error)
  }
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

.archive-container {
  padding: 30px 40px;
  margin: 20px auto 40px !important;
  max-width: 900px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.archive-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 20px;
  font-size: 1rem;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--card-border-soft);
}

.timeline {
  padding: 20px 0;
}

.timeline-item {
  position: relative;
  padding: 15px 0 15px 30px;
  border-left: 2px solid rgba(73, 177, 245, 0.26);
}

.timeline-dot {
  position: absolute;
  left: -6px;
  top: 20px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #49b1f5;
  box-shadow: 0 0 0 5px rgba(73, 177, 245, 0.14);
}

.timeline-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 14px;
  border: 1px solid transparent;
  border-radius: 16px;
  transition: all var(--transition-normal);
}

.timeline-content:hover {
  border-color: var(--card-border-accent);
  background: rgba(73, 177, 245, 0.08);
  transform: translateX(4px);
}

.time {
  font-size: 0.875rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.article-link {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.3s;
}

.article-link:hover {
  color: #49b1f5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .archive-container {
    margin: 20px 10px 20px;
    padding: 20px;
  }

  .timeline-item {
    padding-left: 20px;
  }

  .timeline-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>
