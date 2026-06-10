<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">标签</h1>
    </div>
    <!-- 标签列表 -->
    <v-card class="blog-container tag-container">
      <div class="tag-cloud-title">标签 - {{ count }}</div>
      <div class="tag-cloud">
        <router-link
          v-for="item of tagList"
          :key="item.id"
          :to="'/tags/' + item.id"
          class="tag-item"
          :style="{ fontSize: getRandomFontSize() }"
        >
          {{ item.tagName }}
        </router-link>
      </div>
      <div v-if="tagList.length === 0" class="empty-tip">
        暂无标签
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getTags } from '@/api/misc'

interface Tag {
  id: number
  tagName: string
}

const blogInfoStore = useBlogInfoStore()

const tagList = ref<Tag[]>([])
const count = ref(0)

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const tagPage = pageList.find(item => item.pageLabel === 'tag')
  const coverUrl = tagPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

function getRandomFontSize() {
  return Math.floor(Math.random() * 10) + 18 + 'px'
}

async function listTags() {
  try {
    const { data } = await getTags()
    tagList.value = data.data?.recordList || []
    count.value = data.data?.count || 0
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

.tag-container {
  padding: 30px 40px;
  margin: 20px auto 40px !important;
  max-width: 860px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.tag-cloud-title {
  line-height: 2;
  font-size: 36px;
  text-align: center;
  color: #344c67;
}

.tag-cloud {
  text-align: center;
  padding: 20px 0;
}

.tag-item {
  display: inline-block;
  margin: 5px;
  padding: 2px 14px;
  border: 1px solid var(--card-border-accent);
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.08);
  color: #2a93d5;
  text-decoration: none;
  line-height: 2;
  transition: all var(--transition-normal);
}

.tag-item:hover {
  border-color: var(--card-border-accent-hover);
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  color: #fff !important;
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.18);
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .tag-container {
    margin: 20px 10px 20px;
    padding: 20px;
  }

  .tag-cloud-title {
    font-size: 25px;
  }
}
</style>
