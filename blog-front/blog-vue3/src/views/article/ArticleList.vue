<template>
  <div class="article-list-page">
    <!-- 标签或分类名 -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">{{ title }} - {{ name }}</h1>
    </div>
    <div class="article-list-wrapper">
      <!-- 分类和标签文章卡片复用主页卡片视觉，避免列表页与首页风格割裂。 -->
      <div class="article-card-grid">
        <div
          v-for="item of articleList"
          :key="item.id"
          class="article-card-item animated zoomIn"
        >
          <v-card class="home-feed-card">
            <router-link :to="'/articles/' + item.id" class="card-image-container">
              <v-img class="card-image" :src="item.articleCover" aspect-ratio="16/9" cover />
            </router-link>
            <div class="card-content">
              <div class="card-time">
                <v-icon size="14" color="#7c8797">mdi-calendar-month-outline</v-icon>
                发布于 {{ formatDate(item.createTime) }}
              </div>
              <router-link :to="'/articles/' + item.id" class="card-title">
                <h3>{{ item.articleTitle }}</h3>
              </router-link>
              <div class="card-meta-row">
                <span class="card-meta-hot">
                  <v-icon size="15" color="#ff7242">mdi-fire</v-icon>
                  {{ item.viewCount ?? item.viewsCount ?? item.likeCount ?? 0 }} 热度
                </span>
                <span class="card-meta-comment">
                  <v-icon size="14" color="#ff7242">mdi-comment-outline</v-icon>
                  {{ item.commentCount ?? item.commentCounts ?? 0 }} 评论
                </span>
              </div>
              <div class="card-footer">
                <router-link :to="'/categories/' + item.categoryId" class="card-category">
                  <v-icon size="14" color="#f0b84d">mdi-folder-outline</v-icon>
                  {{ item.categoryName }}
                </router-link>
                <div class="card-tags">
                  <router-link
                    :to="'/tags/' + tag.id"
                    v-for="tag of item.tagDTOList?.slice(0, 1)"
                    :key="tag.id"
                    class="card-tag"
                  >
                    <v-icon size="14" color="#6583EE">mdi-tag-multiple</v-icon>
                    {{ tag.tagName }}
                  </router-link>
                </div>
              </div>
            </div>
          </v-card>
        </div>
      </div>
      <div v-if="!hasMore && articleList.length > 0" class="no-more">
        已加载全部文章
      </div>
      <div v-else-if="!loading && articleList.length === 0" class="no-data">
        暂无文章
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { formatDate } from '@/utils/filters'
import { getArticlesByCondition } from '@/api/article'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'

interface Tag {
  id: number
  tagName: string
}

interface ArticleItem {
  id: number
  articleCover: string
  articleTitle: string
  createTime: string
  categoryId: number
  categoryName: string
  viewCount?: number
  viewsCount?: number
  likeCount?: number
  commentCount?: number
  commentCounts?: number
  tagDTOList: Tag[]
}

const route = useRoute()
const blogInfoStore = useBlogInfoStore()

// State
const current = ref(1)
const articleList = ref<ArticleItem[]>([])
const name = ref('')
const title = ref('')
const loading = ref(false)
const hasMore = ref(true)

// Computed
const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const articleListPage = pageList.find(item => item.pageLabel === 'articleList')
  const coverUrl = articleListPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

function getTitleFromPath() {
  const path = route.path
  if (path.indexOf('/categories') !== -1) {
    title.value = '分类'
  } else {
    title.value = '标签'
  }
}

async function fetchArticles() {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const { data } = await getArticlesByCondition({
      categoryId: route.params.categoryId as string,
      tagId: route.params.tagId as string,
      current: current.value
    })

    if (data.data?.name) {
      name.value = data.data.name
      document.title = title.value + ' - ' + name.value
    }

    const articles = data.data?.articlePreviewDTOList || []
    if (articles.length) {
      current.value++
      articleList.value.push(...articles)
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  getTitleFromPath()
  fetchArticles()
})

useInfiniteScroll({
  loading,
  hasMore,
  onLoadMore: fetchArticles
})

// Watch route changes
watch(() => route.params, () => {
  current.value = 1
  articleList.value = []
  hasMore.value = true
  name.value = ''
  getTitleFromPath()
  fetchArticles()
}, { immediate: false })
</script>

<style scoped>
.banner {
  position: relative;
  height: 420px;
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
  bottom: 60px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

@media (min-width: 760px) {
  .article-list-wrapper {
    max-width: 1106px;
    margin: 32px auto 1rem;
    padding: 0 4px;
  }
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .article-list-wrapper {
    margin-top: 20px;
    padding: 0 12px;
  }
}

.article-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.article-card-item {
  min-width: 0;
  border-radius: 8px;
}

.home-feed-card {
  position: relative;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  border: 1px solid var(--card-border-accent);
  border-radius: 8px !important;
  background: var(--card-surface-soft);
  box-shadow: var(--card-shadow-soft);
  display: flex;
  flex-direction: column;
  transition: border-color 0.35s ease, box-shadow 0.35s ease;
}

.article-card-item:hover .home-feed-card {
  border-color: var(--card-border-accent-hover);
  box-shadow:
    0 18px 34px rgba(61, 94, 132, 0.18),
    0 8px 18px rgba(73, 177, 245, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.82);
}

.card-image-container {
  overflow: hidden;
  display: block;
  height: 170px;
  background: #f3f7fc;
}

.card-image {
  width: 100%;
  height: 100%;
  transition: transform 0.6s ease, opacity 0.6s ease;
}

.card-image-container:hover .card-image {
  transform: scale(1.06);
  opacity: 0.96;
}

.card-content {
  position: relative;
  z-index: 2;
  padding: 10px 14px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-time {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 5px;
  margin-bottom: 8px;
  color: #7c8797;
  font-size: 0.76rem;
  font-weight: 600;
}

.card-title {
  margin-bottom: 4px;
  display: block;
  color: inherit !important;
  text-decoration: none;
}

.card-title h3 {
  margin: 0;
  color: #253046;
  font-size: 1.02rem;
  font-weight: 700;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.card-title:hover h3 {
  color: #2ba1d1;
}

.card-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  color: #7c8797;
  font-size: 0.76rem;
  font-weight: 600;
}

.card-meta-hot,
.card-meta-comment {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 10px;
  margin-top: auto;
}

.card-category,
.card-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0.22rem 0.55rem;
  border-radius: 4px;
  background: #f6f8fb;
  border: 1px solid rgba(229, 235, 244, 0.9);
  font-size: 0.72rem;
  text-decoration: none;
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease, color 0.3s ease;
}

.card-category {
  color: #8a8f99 !important;
}

.card-tag {
  color: #7a7fe8 !important;
}

.card-category:hover,
.card-tag:hover {
  transform: translateY(-1px);
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.2);
  color: #fff !important;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.no-more,
.no-data {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 14px;
}

@media (max-width: 759px) {
  .article-card-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .home-feed-card {
    border-radius: 18px !important;
    box-shadow: 0 12px 24px rgba(15, 23, 42, 0.1);
  }

  .card-content {
    padding: 16px 16px 18px;
  }
}

@media (min-width: 760px) and (max-width: 1199px) {
  .article-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1200px) {
  .article-card-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
