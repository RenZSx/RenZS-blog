<template>
  <div class="banner" :style="articleCover">
    <div class="article-info-container">
      <div class="article-title">{{ article.articleTitle }}</div>
      <div class="article-info">
        <div class="article-meta article-meta-primary first-line">
          <span>
            <v-icon size="14">mdi-calendar</v-icon>
            发表于 {{ formatDate(article.createTime) }}
          </span>
          <span class="separator">|</span>
          <span>
            <v-icon size="14">mdi-update</v-icon>
            更新于
            <template v-if="article.updateTime">
              {{ formatDate(article.updateTime) }}
            </template>
            <template v-else>
              {{ formatDate(article.createTime) }}
            </template>
          </span>
          <span class="separator">|</span>
          <span class="article-category">
            <v-icon size="14">mdi-folder</v-icon>
            <router-link :to="'/categories/' + article.categoryId">
              {{ article.categoryName }}
            </router-link>
          </span>
        </div>
        <div class="article-meta article-meta-secondary second-line">
          <span>
            <v-icon size="14">mdi-text</v-icon>
            字数统计: {{ formatNum(wordNum) }}
          </span>
          <span class="separator">|</span>
          <span>
            <v-icon size="14">mdi-clock-outline</v-icon>
            阅读时长: {{ readTime }}
          </span>
        </div>
        <div class="article-meta article-meta-tertiary third-line">
          <span class="separator">|</span>
          <span>
            <v-icon size="14">mdi-eye</v-icon> 阅读量: {{ article.viewsCount }}
          </span>
          <span class="separator">|</span>
          <span>
            <v-icon size="14">mdi-comment</v-icon>评论数: {{ commentCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatDate, formatNum } from '@/utils/filters'

interface Article {
  articleTitle: string
  articleCover: string
  createTime: string
  updateTime?: string
  categoryId: number
  categoryName: string
  viewsCount: number
}

interface Props {
  article: Article
  wordNum: number
  readTime: string
  commentCount: number
}

const props = defineProps<Props>()

const articleCover = computed(() => {
  return `background: url(${props.article.articleCover}) center center / cover no-repeat`
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 500px;
  overflow: hidden;
  color: #eee !important;
}

.banner:before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(6, 13, 28, 0.2) 0%,
    rgba(6, 13, 28, 0.44) 48%,
    rgba(6, 13, 28, 0.72) 100%
  );
}

.article-info-container {
  position: absolute;
  z-index: 1;
  width: 100%;
}

.article-title {
  font-weight: 700;
  letter-spacing: 0.02em;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.28);
}

.article-info {
  display: inline-flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14px;
  line-height: 1.8;
}

.article-info i {
  font-size: 14px;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
}

.article-meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.22),
    0 10px 24px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(12px) saturate(145%);
  -webkit-backdrop-filter: blur(12px) saturate(145%);
}

.separator {
  display: none !important;
}

.article-category a {
  color: #fff !important;
}

@media (min-width: 760px) {
  .article-info-container {
    bottom: 6.25rem;
    padding: 0 8%;
    text-align: center;
  }

  .article-meta {
    justify-content: center;
  }

  .article-title {
    font-size: 40px;
    margin: 20px 0 14px;
  }
}

@media (max-width: 759px) {
  .banner {
    height: 400px;
  }

  .article-info-container {
    bottom: 1.5rem;
    padding: 0 5%;
    text-align: left;
  }

  .article-title {
    font-size: 1.8rem;
    margin-bottom: 0.65rem;
  }

  .article-info {
    display: flex;
  }

  .article-meta {
    justify-content: flex-start;
    gap: 8px 10px;
  }

  .article-meta span {
    padding: 6px 10px;
    font-size: 12px;
  }
}
</style>
