<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">说说</h1>
    </div>
    <!-- 说说内容 -->
    <v-card class="blog-container talk-container">
      <div
        v-for="(item, index) of talkList"
        :key="item.id"
        :class="getTalkItemClass(index, talkList.length)"
      >
        <router-link :to="'/talks/' + item.id">
          <!-- 用户信息 -->
          <div class="user-info-wrapper">
            <v-avatar size="36" class="user-avatar">
              <v-img :src="item.avatar" />
            </v-avatar>
            <div class="user-detail-wrapper">
              <div class="user-nickname">
                {{ item.nickname }}
                <v-icon class="user-sign" size="20" color="#ffa51e">
                  mdi-check-decagram
                </v-icon>
              </div>
              <!-- 发表时间 -->
              <div class="time">
                {{ formatTime(item.createTime) }}
                <span class="top" v-if="item.isTop == 1">
                  <v-icon size="12" color="#ff7242">mdi-arrow-up-bold</v-icon> 置顶
                </span>
              </div>
              <!-- 说说信息 -->
              <div class="talk-content" v-html="item.content" />
              <!-- 图片列表 -->
              <v-row class="talk-images" v-if="item.imgList && item.imgList.length">
                <v-col
                  :md="4"
                  :cols="6"
                  v-for="(img, index) of item.imgList"
                  :key="index"
                >
                  <v-img
                    class="images-items"
                    :src="img"
                    aspect-ratio="1"
                    max-height="200"
                    cover
                    @click.prevent="previewImg(item.imgList, index)"
                  />
                </v-col>
              </v-row>
              <!-- 说说操作 -->
              <div class="talk-operation">
                <div class="talk-operation-item">
                  <v-icon
                    size="16"
                    :color="isLike(item.id)"
                    class="like-btn"
                    @click.prevent="like(item)"
                  >
                    mdi-thumb-up
                  </v-icon>
                  <div class="operation-count">
                    {{ item.likeCount ?? 0 }}
                  </div>
                </div>
                <div class="talk-operation-item">
                  <v-icon size="16" color="#999">mdi-chat</v-icon>
                  <div class="operation-count">
                    {{ item.commentCount ?? 0 }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </router-link>
      </div>
      <div v-if="!hasMore && talkList.length > 0" class="no-more">
<!--        已加载全部说说-->
      </div>
      <div v-else-if="!loading && talkList.length === 0" class="empty-tip">
        暂无说说
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useUserStore } from '@/stores/user'
import { getTalks, likeTalk } from '@/api/talk'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'
import { getTalkItemClass } from './talkListStyle'

interface TalkItem {
  id: number
  avatar: string
  nickname: string
  createTime: string
  content: string
  imgList: string[]
  likeCount: number
  commentCount: number
  isTop: number
}

const route = useRoute()
const blogInfoStore = useBlogInfoStore()
const userStore = useUserStore()

const current = ref(1)
const talkList = ref<TalkItem[]>([])
const loading = ref(false)
const hasMore = ref(true)

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const talkPage = pageList.find(item => item.pageLabel === 'talk')
  const coverUrl = talkPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function isLike(talkId: number) {
  const talkLikeSet = userStore.talkLikeSet
  return talkLikeSet.includes(talkId) ? '#eb5055' : '#999'
}

function previewImg(imgList: string[], index: number) {
  // 简单实现：在新窗口打开图片
  window.open(imgList[index], '_blank')
}

async function listTalks() {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const { data } = await getTalks({ current: current.value })
    const records = data.data?.recordList || []

    if (records.length) {
      current.value++
      talkList.value.push(...records)
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取说说列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function like(talk: TalkItem) {
  if (!userStore.userId) {
    openLoginRequiredPrompt({ redirect: route.fullPath })
    return
  }

  try {
    const { data } = await likeTalk(talk.id)
    if (data.flag) {
      const talkLikeSet = userStore.talkLikeSet
      if (talkLikeSet.includes(talk.id)) {
        talk.likeCount = (talk.likeCount ?? 1) - 1
      } else {
        talk.likeCount = (talk.likeCount ?? -1) + 1
      }
      userStore.toggleTalkLike(talk.id)
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

onMounted(() => {
  listTalks()
})

useInfiniteScroll({
  loading,
  hasMore,
  onLoadMore: listTalks
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

.talk-container {
  padding: 0 30px;
  margin: 20px auto 40px !important;
  max-width: 815px;
  border: 1px solid rgba(99, 139, 184, 0.16);
  border-radius: var(--card-radius-lg) !important;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(250, 253, 255, 0.97));
  box-shadow: none;
}

.talk-item {
  padding: 24px 0;
}

.talk-item-with-divider {
  border-bottom: 1px solid rgba(119, 142, 170, 0.26);
}

.talk-item a {
  text-decoration: none;
  color: inherit;
}

.user-info-wrapper {
  width: 100%;
  display: flex;
}

.user-avatar {
  border-radius: 50%;
  transition: all 0.5s;
}

.user-avatar:hover {
  transform: rotate(360deg);
}

.user-detail-wrapper {
  flex: 1;
  margin-left: 10px;
  min-width: 0;
}

.user-nickname {
  font-size: 15px;
  font-weight: bold;
  vertical-align: middle;
}

.user-sign {
  margin-left: 4px;
}

.time {
  color: var(--text-secondary);
  margin-top: 2px;
  font-size: 12px;
}

.top {
  color: #ff7242;
  margin-left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.talk-content {
  margin-top: 8px;
  font-size: 14px;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}

.talk-images {
  padding: 0 10px;
  margin-top: 8px;
}

.talk-images :deep(.v-col) {
  padding: 2px !important;
}

.images-items {
  cursor: pointer;
  border-radius: 14px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
}

.talk-operation {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.talk-operation-item {
  display: flex;
  align-items: center;
  margin-right: 40px;
  font-size: 12px;
}

.operation-count {
  margin-left: 4px;
}

.no-more,
.empty-tip {
  color: #999;
  font-size: 14px;
}

.like-btn {
  cursor: pointer;
}

.like-btn:hover {
  color: #eb5055 !important;
}

:global(.dark) .talk-container {
  border-color: rgba(186, 200, 224, 0.14);
  background:
    linear-gradient(180deg, rgba(35, 43, 58, 0.98), rgba(24, 30, 41, 0.96));
  box-shadow: none;
}

:global(.dark) .talk-item-with-divider {
  border-bottom-color: rgba(186, 200, 224, 0.16);
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .talk-container {
    margin: 20px 10px 20px;
    padding: 0 16px;
  }

  .talk-item {
    padding: 20px 0;
  }
}
</style>
