<template>
  <div>
    <!-- 最新评论卡片 -->
    <v-card class="animated zoomIn home-side-card home-side-card-comments">
      <div class="home-side-title">
        <span class="home-side-title-badge">
          <v-icon size="16">mdi-comment-outline</v-icon>
        </span>
        <span class="home-side-title-text">最新评论</span>
      </div>
      <div
        class="comment-scroll-container"
        v-if="newCommentsList.length"
        ref="scrollViewRef"
        @mouseenter="isAutoScrolling = false"
        @mouseleave="isAutoScrolling = true"
      >
        <div
          ref="commentListRef"
          class="comment-list"
          v-for="n in commentListCount"
          :key="n"
        >
          <div
            class="comment-item"
            v-for="(item, index) of newCommentsList"
            :key="item.id + '-' + index + '-' + n"
          >
            <v-avatar size="32">
              <v-img :src="item.avatar" />
            </v-avatar>
            <div class="comment-content">
              <div class="comment-nickname">
                {{ item.nickname }}
                <span class="comment-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="comment-text" v-html="item.commentContent"></div>
            </div>
          </div>
        </div>
      </div>
    </v-card>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { formatTime } from '@/utils/filters'

interface Comment {
  id: number
  nickname: string
  avatar: string
  commentContent: string
  createTime: string
}

interface BlogInfo {
  viewsCount: number
  [key: string]: any
}

interface Props {
  blogInfo: BlogInfo
  newCommentsList: Comment[]
}

const props = defineProps<Props>()

const scrollViewRef = ref<HTMLElement | null>(null)
const commentListRef = ref<HTMLElement[] | null>(null)
const commentListCount = ref(1)
const isAutoScrolling = ref(true)
let scrollTimer: ReturnType<typeof setInterval> | null = null

function hasScrollBar() {
  if (!scrollViewRef.value) return false
  return scrollViewRef.value.scrollHeight > scrollViewRef.value.clientHeight
}

function initCommentScroll() {
  if (scrollTimer) {
    clearInterval(scrollTimer)
  }

  if (!scrollViewRef.value) return

  commentListCount.value = hasScrollBar() ? 2 : 1

  if (commentListCount.value === 2) {
    nextTick(() => {
      scrollTimer = setInterval(() => {
        if (
          isAutoScrolling.value &&
          scrollViewRef.value &&
          commentListRef.value &&
          commentListRef.value.length > 0
        ) {
          const scrollView = scrollViewRef.value
          const firstList = commentListRef.value[0]
          if (scrollView.scrollTop >= firstList.clientHeight) {
            scrollView.scrollTop = 0
          } else {
            scrollView.scrollTop += 1
          }
        }
      }, 10)
    })
  }
}

onMounted(() => {
  initCommentScroll()
})

onUnmounted(() => {
  if (scrollTimer) {
    clearInterval(scrollTimer)
  }
})

watch(
  () => props.newCommentsList.length,
  () => {
    nextTick(() => {
      initCommentScroll()
    })
  }
)
</script>

<style scoped>
.home-side-card {
  position: relative;
  margin-top: 0 !important;
  border: 1px solid var(--glass-border);
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--card-radius-md) !important;
  overflow: hidden;
  transition: transform 0.32s ease, border-color 0.32s ease, box-shadow 0.32s ease;
}

.home-side-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background:
    radial-gradient(circle at 14% 0%, rgba(255, 255, 255, 0.74), transparent 34%),
    radial-gradient(circle at 90% 12%, rgba(73, 177, 245, 0.16), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.28), transparent 44%);
  pointer-events: none;
}

.home-side-card:hover {
  transform: translateY(-3px);
  border-color: var(--glass-border-hover);
  box-shadow: var(--glass-shadow-hover);
}

.home-side-title {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 13px 14px 10px;
  margin-bottom: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.38);
  font-size: 14px;
  font-weight: 700;
  line-height: 2;
  background:
    linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.28),
      rgba(255, 255, 255, 0.08)
    );
}

.home-side-title-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 10px;
  background:
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.78),
      rgba(244, 248, 255, 0.34)
    );
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88),
    0 10px 20px rgba(94, 166, 229, 0.12);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  flex-shrink: 0;
}

.home-side-title-badge-emoji {
  font-size: 1rem;
}

.home-side-title-text {
  margin-left: 0;
  letter-spacing: 0.01em;
}

.home-side-text {
  font-size: 0.78rem;
  font-weight: 600;
}

.home-side-body {
  position: relative;
  z-index: 1;
  padding: 12px 14px 14px;
}

.home-notice-body {
  line-height: 1.7;
  color: #5f6472;
}

.home-side-info {
  color: #5f6472;
}

.home-side-info-row {
  padding: 5px 0 0;
}

.article-list {
  position: relative;
  z-index: 1;
  padding: 6px 14px 12px;
}

.article-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.article-item:not(:last-child) {
  border-bottom: 1px dashed rgba(115, 132, 155, 0.18);
}

.content-cover {
  width: 52px;
  height: 52px;
  display: block;
  overflow: hidden;
  border-radius: 12px;
  flex-shrink: 0;
}

.content-newArticle {
  flex: 1;
  min-width: 0;
  padding-left: 10px;
  word-break: break-all;
  font-weight: 600;
}

.content-title a {
  transition: all 0.2s;
  font-size: 0.82rem;
  color: #2d3448;
  text-decoration: none;
}

.content-title a:hover {
  color: #2ba1d1;
}

.content-time {
  color: #858585;
  font-size: 0.72rem;
  line-height: 1.7;
}

.comment-scroll-container {
  z-index: 1;
  height: 180px;
  overflow-y: auto;
  position: relative;
  padding: 4px 0 10px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.comment-scroll-container::-webkit-scrollbar {
  display: none;
}

.comment-list {
  width: 100%;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  padding: 9px 14px;
  border-bottom: 1px dashed rgba(115, 132, 155, 0.18);
}

.comment-content {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
  overflow: hidden;
}

.comment-nickname {
  font-weight: bold;
  font-size: 0.8rem;
  margin-bottom: 3px;
}

.comment-time {
  font-size: 0.62rem;
  color: #999;
  margin-left: 8px;
  font-style: italic;
}

.comment-text {
  font-size: 0.72rem;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
