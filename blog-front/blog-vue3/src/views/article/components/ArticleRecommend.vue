<template>
  <div>
    <div class="pagination-post">
      <div
        :class="isFull(article.lastArticle.id)"
        v-if="article.lastArticle.id"
      >
        <router-link :to="'/articles/' + article.lastArticle.id">
          <img class="post-cover" :src="article.lastArticle.articleCover" />
          <div class="post-info">
            <div class="label">上一篇</div>
            <div class="post-title">
              {{ article.lastArticle.articleTitle }}
            </div>
          </div>
        </router-link>
      </div>
      <div
        :class="isFull(article.nextArticle.id)"
        v-if="article.nextArticle.id"
      >
        <router-link :to="'/articles/' + article.nextArticle.id">
          <img class="post-cover" :src="article.nextArticle.articleCover" />
          <div class="post-info post-info-next">
            <div class="label">下一篇</div>
            <div class="post-title">
              {{ article.nextArticle.articleTitle }}
            </div>
          </div>
        </router-link>
      </div>
    </div>
    <div class="recommend-container" v-if="article.recommendArticleList?.length">
      <div class="recommend-title">
        <v-icon size="20" color="#4c4948">mdi-thumb-up</v-icon> 相关推荐
      </div>
      <div class="recommend-list">
        <div
          class="recommend-item"
          v-for="item of article.recommendArticleList"
          :key="item.id"
        >
          <router-link :to="'/articles/' + item.id">
            <img class="recommend-cover" :src="item.articleCover" />
            <div class="recommend-info">
              <div class="recommend-date">
                <v-icon size="14">mdi-calendar</v-icon>
                {{ formatDate(item.createTime) }}
              </div>
              <div>{{ item.articleTitle }}</div>
            </div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/filters'

interface ArticleItem {
  id: number
  articleCover: string
  articleTitle: string
  createTime: string
}

interface Article {
  lastArticle: ArticleItem
  nextArticle: ArticleItem
  recommendArticleList?: ArticleItem[]
}

interface Props {
  article: Article
}

defineProps<Props>()

function isFull(id: number) {
  return id ? 'post full' : 'post'
}
</script>

<style scoped>
.pagination-post {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 40px;
  padding: 0 42px;
  background: transparent;
  box-sizing: border-box;
}

.post {
  position: relative;
  min-width: 0;
  height: 160px;
  overflow: hidden;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-md);
  background: #0d1525;
  box-shadow: var(--card-shadow-soft);
  transition: transform 0.35s ease, border-color 0.35s ease,
    box-shadow 0.35s ease;
}

.post:before,
.recommend-item:before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 0;
  background: linear-gradient(
    180deg,
    rgba(12, 18, 30, 0.1),
    rgba(12, 18, 30, 0.6)
  );
}

.post-info {
  position: absolute;
  top: 50%;
  z-index: 1;
  box-sizing: border-box;
  width: 100%;
  padding: 20px 40px;
  transform: translateY(-50%);
  line-height: 2;
  font-size: 14px;
}

.post-info-next {
  text-align: right;
}

.post-cover {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0.52;
  transition: all 0.6s;
  object-fit: cover;
}

.post a {
  position: relative;
  display: block;
  overflow: hidden;
  height: 100%;
}

.post:hover,
.recommend-item:hover {
  transform: translateY(-4px);
  border-color: var(--card-border-accent-hover);
  box-shadow: var(--card-shadow-hover);
}

.post:hover .post-cover,
.recommend-item:hover .recommend-cover {
  opacity: 0.85;
  transform: scale(1.08);
}

.label {
  font-size: 90%;
  color: rgba(255, 255, 255, 0.78);
}

.post-title {
  overflow: hidden;
  font-weight: 600;
  color: #fff;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.full {
  width: 100% !important;
}

.recommend-container {
  margin-top: 40px;
}

.recommend-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 42px;
  margin-bottom: 12px;
  font-size: 20px;
  line-height: 2;
  font-weight: bold;
}

.recommend-list {
  padding: 0 39px 0;
}

.recommend-item {
  position: relative;
  display: inline-block;
  overflow: hidden;
  margin: 3px;
  border: 1px solid var(--card-border-accent);
  background: #0d1525;
  vertical-align: bottom;
  border-radius: var(--card-radius-md);
  box-shadow: var(--card-shadow-soft);
  transition: transform 0.35s ease, border-color 0.35s ease,
    box-shadow 0.35s ease;
}

.recommend-cover {
  width: 100%;
  height: 100%;
  opacity: 0.5;
  transition: all 0.6s;
  object-fit: cover;
}

.recommend-info {
  position: absolute;
  top: 50%;
  z-index: 1;
  width: 100%;
  padding: 0 20px;
  transform: translateY(-50%);
  line-height: 2;
  color: #fff;
  text-align: center;
  font-size: 14px;
}

.recommend-date {
  font-size: 90%;
}

@media (min-width: 760px) {
  .post {
    width: calc(50% - 7px);
  }

  .recommend-item {
    width: calc(33.333% - 6px);
    height: 200px;
  }
}

@media (max-width: 759px) {
  .post {
    width: 100%;
  }

  .pagination-post {
    display: flex;
    padding: 0 18px;
    margin-top: 32px;
  }

  .pagination-post .post + .post {
    margin-top: 0;
  }

  .post-info {
    padding: 18px 22px;
  }

  .recommend-title {
    padding: 0 18px;
  }

  .recommend-list {
    padding: 0 16px;
  }

  .recommend-item {
    width: calc(100% - 4px);
    height: 150px;
    margin: 2px;
  }
}
</style>
