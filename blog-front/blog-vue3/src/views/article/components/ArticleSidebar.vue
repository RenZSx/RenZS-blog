<template>
  <div class="article-side-sticky">
    <v-card class="right-container article-side-card">
      <div class="right-title article-side-title">
        <v-icon size="16">mdi-format-list-bulleted</v-icon>
        <span style="margin-left: 10px">目录</span>
      </div>
      <div id="toc" class="article-toc" />
    </v-card>
    <v-card class="right-container article-side-card article-side-card-spaced">
      <div class="right-title article-side-title">
        <v-icon size="16">mdi-file-document-outline</v-icon>
        <span style="margin-left: 10px">最新文章</span>
      </div>
      <div class="article-list">
        <div
          class="article-item"
          v-for="item of newestArticleList"
          :key="item.id"
        >
          <router-link :to="'/articles/' + item.id" class="content-cover">
            <v-img :src="item.articleCover" width="62" height="62" cover />
          </router-link>
          <div class="content">
            <div class="content-title">
              <router-link :to="'/articles/' + item.id">
                {{ item.articleTitle }}
              </router-link>
            </div>
            <div class="content-time">{{ formatDate(item.createTime) }}</div>
          </div>
        </div>
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/filters'

interface Article {
  id: number
  articleTitle: string
  articleCover: string
  createTime: string
}

interface Props {
  newestArticleList: Article[]
}

defineProps<Props>()
</script>

<style scoped>
.article-side-sticky {
  position: sticky;
  top: 20px;
  margin-top: -325px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 2px;
  scrollbar-width: thin;
  scrollbar-color: rgba(73, 177, 245, 0.32) transparent;
}

.article-side-sticky::-webkit-scrollbar {
  width: 6px;
}

.article-side-sticky::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.3);
}

.article-side-card {
  border: 1px solid var(--glass-border);
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: border-color var(--transition-normal),
    box-shadow var(--transition-normal), transform var(--transition-normal);
}

.article-side-card:hover {
  border-color: var(--glass-border-hover);
  box-shadow: var(--glass-shadow-hover);
  transform: translateY(-2px);
}

.right-container {
  padding: 0;
  overflow: hidden;
  border-radius: var(--card-radius-md) !important;
  font-size: 14px;
}

.article-side-card-spaced {
  margin-top: 20px;
}

.right-title {
  display: flex;
  align-items: center;
  line-height: 2;
  font-size: 16.8px;
}

.article-side-title {
  padding: 16px 20px 12px;
  margin-bottom: 0;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.right-title i {
  font-weight: bold;
}

.article-list {
  padding: 8px 20px 18px;
}

.article-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.article-item:first-child {
  padding-top: 0;
}

.article-item:last-child {
  padding-bottom: 0;
}

.article-item:not(:last-child) {
  border-bottom: 1px dashed rgba(115, 132, 155, 0.18);
}

.content-cover {
  overflow: hidden;
  width: 62px;
  height: 62px;
  border-radius: 14px;
}

.content {
  display: -webkit-box;
  flex: 1;
  overflow: hidden;
  padding-left: 12px;
  word-break: break-all;
  -webkit-box-orient: vertical;
}

.content-title a {
  font-size: 95%;
  transition: all 0.2s;
  color: #2d3448;
  text-decoration: none;
}

.content-title a:hover {
  color: #2ba1d1;
}

.content-time {
  color: #7d8796;
  font-size: 85%;
  line-height: 2;
}

@media (max-width: 759px) {
  .article-side-sticky {
    margin-top: -96px;
  }
}
</style>
