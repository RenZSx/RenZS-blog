<template>
  <div>
    <v-card class="home-card home-toolbar-card">
      <div class="home-toolbar-shell">
        <div class="home-toolbar">
          <div class="home-toolbar-center">
            <div class="home-toolbar-message">
              <v-icon size="20" color="#ff6b3d">mdi-volume-high</v-icon>
              <strong class="home-toolbar-notice">
                {{ systemNotice || '暂无系统通知' }}
              </strong>
            </div>
          </div>
          <div class="home-toolbar-actions">
            <!-- 使用小型胶囊开关承载布局切换，视觉更接近工具按钮而不是表单控件。 -->
            <button
              type="button"
              class="layout-toggle hidden-on-mobile"
              :class="{ 'is-list': !isCardLayout }"
              :aria-label="isCardLayout ? '切换为列表布局' : '切换为卡片布局'"
              @click="emit('update:isCardLayout', !isCardLayout)"
            >
              <span class="layout-toggle-dot"></span>
              <span class="layout-toggle-thumb">
                <v-icon size="15">
                  {{ isCardLayout ? 'mdi-view-grid-outline' : 'mdi-format-list-bulleted' }}
                </v-icon>
              </span>
            </button>
          </div>
        </div>
      </div>
    </v-card>

    <!-- 卡片布局 -->
    <div v-if="isCardLayout" class="article-section-list">
      <div
        v-for="section of articleSections"
        :key="section.title + '-' + (section.categoryId ?? 'newest')"
        class="article-section"
      >
        <div class="article-section-header">
          <div class="article-section-title">
            <v-icon size="18" color="#ff6b3d">mdi-view-grid</v-icon>
            <span>{{ section.title }}</span>
          </div>
          <button class="article-section-more" type="button" @click="showSectionList(section)">
            <v-icon size="20">mdi-chevron-double-right</v-icon>
            MORE
          </button>
        </div>
        <div class="article-card-grid">
          <div
            v-for="item of section.articleList"
            :key="item.id"
            class="article-card-item animated zoomIn"
          >
            <v-card class="home-card home-feed-card">
              <router-link :to="'/articles/' + item.id" class="card-image-container">
                <v-img class="card-image" :src="item.articleCover" aspect-ratio="16/9" cover />
              </router-link>
              <div class="card-content">
                <div class="card-time">
                  <v-icon size="14" color="#7c8797">mdi-calendar-month-outline</v-icon>
                  发布于 {{ formatTime(item.createTime) }}
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
                  <span class="card-meta-like">
                    <v-icon size="14" color="#eb5055">mdi-thumb-up-outline</v-icon>
                    {{ getArticleLikes(item) }} 点赞
                  </span>
                </div>
                <div class="card-summary">
                  {{ getArticleSummary(item) }}
                </div>
                <div class="card-footer">
                  <router-link :to="'/categories/' + item.categoryId" class="card-category">
                    <v-icon size="14" color="#f0b84d">mdi-folder-outline</v-icon>
                    {{ item.categoryName }}
                  </router-link>
                  <div class="card-tags">
                    <router-link
                      v-for="tag of item.tagDTOList?.slice(0, 1)"
                      :key="tag.id"
                      :to="'/tags/' + tag.id"
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
      </div>
    </div>

    <!-- 列表布局 -->
    <v-card
      v-else
      v-for="(item, index) of flatArticleList"
      :key="item.id"
      :ref="index === 0 ? setFirstArticleCardRef : undefined"
      class="animated zoomIn article-card"
      tabindex="-1"
    >
      <div :class="isRight(index)">
        <router-link :to="'/articles/' + item.id" class="article-cover-link">
          <v-img
            class="on-hover article-cover-image"
            width="100%"
            height="100%"
            :src="item.articleCover"
            cover
          />
        </router-link>
      </div>
      <div class="article-wrapper">
        <div class="article-meta-row">
          <div class="article-info">
            <span v-if="item.isTop == 1" class="article-top-badge">
              <span class="article-top-badge-text">
                <v-icon size="14">mdi-pin</v-icon> 置顶
              </span>
            </span>
            <router-link :to="'/categories/' + item.categoryId" class="article-meta-category">
              <v-icon size="15" color="#4b86f6">mdi-inbox-full</v-icon>
              {{ item.categoryName }}
            </router-link>
            <div class="article-time article-time-minimal">
              <v-icon size="16" color="#1EC6C6">mdi-calendar-month-outline</v-icon>
              {{ formatTime(item.createTime) }}
            </div>
          </div>
        </div>
        <div class="article-title-block">
          <router-link :to="'/articles/' + item.id" class="article-title-link">
            <span class="article-title-text">{{ item.articleTitle }}</span>
          </router-link>
        </div>
        <div class="article-content">
          {{ item.articleContent }}
        </div>
        <div class="article-tag">
          <router-link
            v-for="tag of item.tagDTOList?.slice(0, 2)"
            :key="tag.id"
            :to="'/tags/' + tag.id"
            class="article-tag-chip"
          >
            <v-icon size="15" color="#6583EE">mdi-tag-multiple</v-icon>
            {{ tag.tagName }}
          </router-link>
          <router-link :to="'/articles/' + item.id" class="article-readmore">
            继续阅读
            <v-icon size="16">mdi-arrow-right</v-icon>
          </router-link>
        </div>
      </div>
    </v-card>

    <div v-if="flatArticleList.length > 0" class="no-more">已经到底啦~</div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, type ComponentPublicInstance } from 'vue'
import { formatTime } from '@/utils/filters'
import { getArticleLikes, getArticleSummary } from '../homeColumnsContent'

interface Tag {
  id: number
  tagName: string
}

interface Article {
  id: number
  articleTitle: string
  articleCover: string
  articleContent: string
  createTime: string
  categoryId: number
  categoryName: string
  isTop: number
  viewCount?: number
  viewsCount?: number
  likeCount?: number
  commentCount?: number
  commentCounts?: number
  tagDTOList?: Tag[]
}

interface ArticleSection {
  title: string
  categoryId?: number
  morePath: string
  articleList: Article[]
}

interface Props {
  articleSections: ArticleSection[]
  activeSectionKey?: string
  systemNotice?: string
  isCardLayout: boolean
}

const props = defineProps<Props>()

const flatArticleList = computed(() => {
  const activeSection = props.articleSections.find(section => getSectionKey(section) === props.activeSectionKey)
  return activeSection?.articleList || props.articleSections.flatMap(section => section.articleList || [])
})

const emit = defineEmits<{
  'update:isCardLayout': [value: boolean]
  'show-section-list': [sectionKey: string]
}>()

const firstArticleCardRef = ref<HTMLElement | null>(null)

function getSectionKey(section: ArticleSection) {
  return section.categoryId ? `category-${section.categoryId}` : 'newest'
}

function showSectionList(section: ArticleSection) {
  emit('show-section-list', getSectionKey(section))
}

function isRight(index: number) {
  return index % 2 === 0 ? 'article-cover right-radius' : 'article-cover left-radius'
}

function setFirstArticleCardRef(element: Element | ComponentPublicInstance | null) {
  if (!element) {
    firstArticleCardRef.value = null
    return
  }

  const rootElement = '$el' in element ? element.$el : element
  firstArticleCardRef.value = rootElement instanceof HTMLElement ? rootElement : null
}

/**
 * 将首页列表模式的第一篇文章滚动到视口焦点位置。
 */
function focusFirstArticle() {
  if (!firstArticleCardRef.value) return

  const firstArticleTop = firstArticleCardRef.value.getBoundingClientRect().top + window.scrollY
  window.scrollTo({
    behavior: 'smooth',
    top: Math.max(firstArticleTop - 180, 0)
  })
  firstArticleCardRef.value.focus({ preventScroll: true })
}

defineExpose({
  focusFirstArticle
})
</script>

<style scoped>
.home-card {
  border: 1px solid var(--card-border-soft);
  border-radius: var(--card-radius-md) !important;
  background: var(--card-surface-soft);
  box-shadow: var(--card-shadow-soft);
  transition: transform 0.32s ease, border-color 0.32s ease, box-shadow 0.32s ease;
}

.home-toolbar-card {
  margin-bottom: 32px;
  border: 0;
  background: transparent;
  box-shadow: none;
}

.home-toolbar-card:hover {
  transform: none;
  border-color: transparent;
  box-shadow: none;
}

.home-toolbar-shell {
  padding: 0;
}

.home-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  min-height: 76px;
  padding: 0 18px;
  border: 1px dashed rgba(178, 187, 202, 0.62);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.58);
}

.home-toolbar-center {
  grid-column: 1;
  min-width: 0;
}

.home-toolbar-actions {
  display: flex;
  grid-column: 2;
  justify-self: end;
  align-items: center;
}

.home-toolbar-message {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  margin: 0;
  width: 100%;
  max-width: 100%;
  text-align: left;
  color: #8a8f99;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  overflow: hidden;
}

.home-toolbar-label {
  color: #5ea6e5;
  flex-shrink: 0;
}

.home-toolbar-notice {
  color: #8a8f99;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-toggle {
  position: relative;
  display: block;
  width: 54px;
  height: 21px;
  padding: 0;
  border: 1px solid rgba(73, 177, 245, 0.32);
  border-radius: 999px;
  background: linear-gradient(145deg, #eef8ff, #d8ecfb);
  box-shadow:
    inset 0 1px 2px rgba(255, 255, 255, 0.9),
    inset 0 -4px 10px rgba(73, 177, 245, 0.12),
    0 6px 14px rgba(73, 177, 245, 0.12);
  cursor: pointer;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

.layout-toggle:hover {
  border-color: rgba(73, 177, 245, 0.58);
  box-shadow:
    inset 0 1px 2px rgba(255, 255, 255, 0.95),
    inset 0 -4px 10px rgba(73, 177, 245, 0.14),
    0 0 0 3px rgba(73, 177, 245, 0.11),
    0 8px 18px rgba(73, 177, 245, 0.16);
}

.layout-toggle-dot {
  position: absolute;
  top: 50%;
  left: 10px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #49b1f5;
  box-shadow: 0 0 10px rgba(73, 177, 245, 0.72);
  transform: translateY(-50%);
  transition: left 0.28s ease, background 0.28s ease, box-shadow 0.28s ease;
}

.layout-toggle-thumb {
  position: absolute;
  top: 2px;
  right: 2px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 17px;
  height: 17px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 32%, #ffffff 0 12%, transparent 14%),
    radial-gradient(circle at 68% 62%, rgba(73, 177, 245, 0.24) 0 18%, transparent 20%),
    linear-gradient(145deg, #ffffff, #e6f4ff);
  color: #3a89c9;
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.96),
    0 2px 6px rgba(73, 177, 245, 0.28);
  transition: right 0.28s ease, transform 0.28s ease;
}

.layout-toggle-thumb :deep(.v-icon) {
  opacity: 0.72;
}

.layout-toggle.is-list .layout-toggle-dot {
  left: 40px;
  background: #ff7242;
  box-shadow: 0 0 9px rgba(255, 114, 66, 0.62);
}

.layout-toggle.is-list .layout-toggle-thumb {
  right: 35px;
}

/* 卡片布局 */
.article-section-list {
  display: flex;
  flex-direction: column;
  gap: 26px;
}

.article-section {
  min-width: 0;
}

.article-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 0 0 14px;
  padding: 0 2px 10px;
  border-bottom: 1px dashed rgba(140, 151, 168, 0.28);
}

.article-section-title {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #5f6472;
  font-size: 0.9rem;
  font-weight: 700;
}

.article-section-more {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #777;
  cursor: pointer;
  font-size: 0.86rem;
  font-weight: 800;
  letter-spacing: 0.04em;
  font-family: inherit;
  text-decoration: none;
  transition: color 0.25s ease, transform 0.25s ease;
}

.article-section-more :deep(.v-icon) {
  color: #ff5f3d;
}

.article-section-more:hover {
  color: #ff5f3d;
  transform: translateX(2px);
}

.article-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.article-card-item {
  min-width: 0;
  border-radius: 8px;
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}

.home-feed-card {
  position: relative;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  border: 1px solid var(--card-border-accent);
  border-radius: 8px !important;
  display: flex;
  flex-direction: column;
  transition: transform 0.35s ease, border-color 0.35s ease, box-shadow 0.35s ease;
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
  transition: transform 0.6s ease, opacity 0.6s ease;
  width: 100%;
  height: 100%;
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
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
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
  font-size: 1.02rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.35;
  color: #253046;
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
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
  color: #7c8797;
  font-size: 0.76rem;
  font-weight: 600;
}

.card-meta-hot,
.card-meta-comment,
.card-meta-like {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-summary {
  margin-bottom: 12px;
  color: #111;
  font-size: 0.74rem;
  line-height: 1.55;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
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

/* 列表布局 */
.article-card {
  position: relative;
  overflow: hidden;
  width: min(100%, 780px);
  margin: 0 auto;
  border: 1px solid rgba(229, 235, 244, 0.72);
  border-radius: 8px !important;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.08);
  display: flex;
  align-items: stretch;
  min-height: 300px;
  margin-top: 38px;
  transition: border-color 0.35s ease, box-shadow 0.35s ease;
}

.article-card:hover {
  border-color: rgba(73, 177, 245, 0.62);
  box-shadow:
    0 18px 42px rgba(15, 23, 42, 0.15),
    0 0 0 3px rgba(73, 177, 245, 0.1);
}

.article-cover {
  display: flex;
  align-self: stretch;
  overflow: hidden;
  min-height: 100%;
  width: 50%;
  background: #f5f7fa;
}

.left-radius {
  border-radius: 8px 0 0 8px !important;
  order: 0;
}

.right-radius {
  border-radius: 0 8px 8px 0 !important;
  order: 1;
}

.article-cover-link {
  display: flex;
  width: 100%;
  height: 100%;
}

.article-cover-image {
  flex: 1;
  width: 100%;
  height: 100%;
}

.on-hover {
  transition: all 0.6s;
}

.article-cover-link:hover .on-hover {
  transform: scale(1.08);
}

.article-wrapper {
  position: relative;
  z-index: 2;
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 28px 36px;
}

.article-meta-row {
  margin-bottom: 4px;
}

.article-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.8;
}

.article-top-badge {
  display: inline-flex;
  align-items: center;
}

.article-top-badge-text {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 114, 66, 0.1);
  border: 1px solid rgba(255, 114, 66, 0.18);
  color: #ff7242;
  font-size: 0.8rem;
}

.article-meta-category {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.36rem 0.82rem;
  border: 1px solid rgba(73, 177, 245, 0.14);
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.06);
  color: #3a89c9 !important;
  font-weight: 700;
  font-size: 0.8rem;
  text-decoration: none;
}

.article-time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: 1px solid rgba(73, 177, 245, 0.16);
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.08);
  color: #3a89c9;
  font-weight: 700;
  font-size: 15px;
}

.article-time-minimal {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: #7c8a9d;
  font-size: 0.8rem;
  font-weight: 600;
}

.article-title-block {
  margin-top: 4px;
  line-height: 1.25;
}

.article-title-link {
  display: inline-flex;
  color: #253046 !important;
  text-decoration: none;
}

.article-title-text {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  transition: color 0.3s;
}

.article-title-link:hover .article-title-text {
  color: #2ba1d1;
}

.article-content {
  margin-top: 18px;
  line-height: 1.75;
  color: #5f6472;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  font-size: 15px;
}

.article-tag {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 22px;
  font-weight: 700;
}

.article-tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.35rem 0.9rem;
  border: 1px solid rgba(73, 177, 245, 0.18);
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.08);
  color: #2a93d5 !important;
  font-size: 13px;
  line-height: 1.5;
  text-decoration: none;
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease, color 0.3s ease;
}

.article-tag-chip:hover {
  transform: translateY(-1px);
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.2);
  color: #fff !important;
}

.article-readmore {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  color: #253046 !important;
  font-size: 0.88rem;
  font-weight: 700;
  letter-spacing: 0.01em;
  text-decoration: none;
  transition: transform 0.3s ease, color 0.3s ease;
}

.article-readmore:hover {
  transform: translateX(2px);
  color: #2ba1d1 !important;
}

.no-more {
  text-align: center;
  padding: 24px;
  color: rgba(0, 0, 0, 0.4);
  font-size: 0.9rem;
}

@media (max-width: 759px) {
  .article-card-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .hidden-on-mobile {
    display: none !important;
  }

  .home-toolbar {
    min-height: 62px;
    padding: 0 14px;
  }

  .home-toolbar-center {
    grid-column: 1 / -1;
    min-width: 0;
  }

  .home-toolbar-actions {
    grid-column: auto;
    justify-self: auto;
  }

  .home-toolbar-message {
    max-width: 100%;
    font-size: 14px;
  }

  .home-toolbar-card {
    margin-bottom: 22px;
  }

  .article-card,
  .home-feed-card {
    border-radius: 18px !important;
    box-shadow: 0 12px 24px rgba(15, 23, 42, 0.1);
  }

  .card-content {
    padding: 16px 16px 18px;
  }

  .left-radius,
  .right-radius {
    border-radius: 18px 18px 0 0 !important;
  }

  .article-card {
    flex-direction: column;
    margin-top: 1rem;
    width: 100%;
    min-height: 0;
  }

  .article-cover {
    height: 230px !important;
    width: 100%;
  }

  .article-wrapper {
    width: 100%;
    padding: 1.25rem 1.25rem 1.5rem;
  }

  .article-title-text {
    font-size: 1.25rem;
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
