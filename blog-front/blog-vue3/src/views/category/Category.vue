<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">分类</h1>
    </div>
    <!-- 分类列表 -->
    <v-card class="blog-container category-container">
      <div class="category-title">分类 - {{ count }}</div>
      <ul class="category-list">
        <li
          class="category-list-item"
          v-for="item of categoryList"
          :key="item.id"
        >
          <router-link :to="'/categories/' + item.id">
            {{ item.categoryName }}
            <span class="category-count">({{ item.articleCount }})</span>
          </router-link>
        </li>
      </ul>
      <div v-if="categoryList.length === 0" class="empty-tip">
        暂无分类
      </div>
    </v-card>
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

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const categoryPage = pageList.find(item => item.pageLabel === 'category')
  const coverUrl = categoryPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

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

.category-container {
  padding: 30px 40px;
  margin: 20px auto 40px !important;
  max-width: 860px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.category-title {
  text-align: center;
  font-size: 36px;
  line-height: 2;
}

.category-list {
  margin: 0 1.8rem;
  list-style: none;
  padding: 0;
}

.category-list-item {
  display: flex;
  align-items: center;
  padding: 8px 1.8rem 8px 0;
}

.category-list-item:before {
  display: inline-block;
  position: relative;
  left: -0.75rem;
  width: 12px;
  height: 12px;
  border: 0.2rem solid #49b1f5;
  border-radius: 50%;
  background: #fff;
  content: "";
  transition-duration: 0.3s;
}

.category-list-item:hover:before {
  border: 0.2rem solid #ff7242;
}

.category-list-item a {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: 999px;
  text-decoration: none;
  color: inherit;
  transition: all var(--transition-normal);
}

.category-list-item a:hover {
  border-color: var(--card-border-accent);
  background: rgba(73, 177, 245, 0.08);
  color: #49b1f5;
  transform: translateX(4px);
}

.category-count {
  margin-left: 0.5rem;
  font-size: 0.75rem;
  color: #858585;
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

  .category-container {
    margin: 20px 10px 20px;
    padding: 20px;
  }

  .category-title {
    font-size: 28px;
  }
}
</style>
