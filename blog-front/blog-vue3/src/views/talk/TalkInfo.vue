<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">说说</h1>
    </div>
    <!-- 说说内容 -->
    <v-card class="blog-container talk-info-container">
      <div class="talk-wrapper">
        <!-- 用户信息 -->
        <div class="user-info-wrapper">
          <v-avatar size="36" class="user-avatar">
            <v-img :src="talkInfo.avatar" />
          </v-avatar>
          <div class="user-detail-wrapper">
            <div class="user-nickname">
              {{ talkInfo.nickname }}
              <v-icon class="user-sign" size="20" color="#ffa51e">
                mdi-check-decagram
              </v-icon>
            </div>
            <!-- 发表时间 -->
            <div class="time">{{ formatTime(talkInfo.createTime) }}</div>
            <!-- 说说信息 -->
            <div class="talk-content" v-html="talkInfo.content" />
            <!-- 图片列表 -->
            <v-row class="talk-images" v-if="talkInfo.imgList && talkInfo.imgList.length">
              <v-col
                :md="4"
                :cols="6"
                v-for="(img, index) of talkInfo.imgList"
                :key="index"
              >
                <v-img
                  class="images-items"
                  :src="img"
                  aspect-ratio="1"
                  max-height="200"
                  cover
                  @click="previewImg(index)"
                />
              </v-col>
            </v-row>
            <!-- 说说操作 -->
            <div class="talk-operation">
              <div class="talk-operation-item">
                <v-icon
                  size="16"
                  :color="isLike(talkInfo.id)"
                  class="like-btn"
                  @click.prevent="like(talkInfo)"
                >
                  mdi-thumb-up
                </v-icon>
                <div class="operation-count">
                  {{ talkInfo.likeCount ?? 0 }}
                </div>
              </div>
              <div class="talk-operation-item">
                <v-icon size="16" color="#999">mdi-chat</v-icon>
                <div class="operation-count">
                  {{ commentCount ?? 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 评论 -->
      <Comment :type="commentType" @get-comment-count="getCommentCount" />
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useUserStore } from '@/stores/user'
import { getTalkById, likeTalk } from '@/api/talk'
import Comment from '@/components/Comment.vue'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

interface TalkInfo {
  id: number
  avatar: string
  nickname: string
  createTime: string
  content: string
  imgList: string[]
  likeCount: number
}

const route = useRoute()
const blogInfoStore = useBlogInfoStore()
const userStore = useUserStore()

const commentType = 3
const commentCount = ref(0)
const talkInfo = ref<TalkInfo>({} as TalkInfo)

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
  if (!talkId) return '#999'
  const talkLikeSet = userStore.talkLikeSet
  return talkLikeSet.includes(talkId) ? '#eb5055' : '#999'
}

function previewImg(index: number) {
  if (talkInfo.value.imgList && talkInfo.value.imgList[index]) {
    window.open(talkInfo.value.imgList[index], '_blank')
  }
}

function getCommentCount(count: number) {
  commentCount.value = count
}

async function fetchTalkById() {
  try {
    const talkId = route.params.talkId as string
    const { data } = await getTalkById(Number(talkId))
    talkInfo.value = data.data || {}
  } catch (error) {
    console.error('获取说说详情失败:', error)
  }
}

async function like(talk: TalkInfo) {
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
  fetchTalkById()
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 280px;
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
  bottom: 40px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.talk-info-container {
  padding: 30px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.talk-wrapper {
  padding: 18px 22px;
  border: 1px solid var(--card-border-soft);
  border-radius: var(--card-radius-md);
  background: var(--card-surface-soft);
  box-shadow: var(--card-shadow-soft);
  transition: all 0.3s ease 0s;
}

.talk-wrapper:hover {
  border-color: var(--card-border-accent-hover);
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-3px);
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
  margin-left: 10px;
  flex: 1;
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
  display: flex;
  align-items: center;
  margin-top: 10px;
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

.like-btn {
  cursor: pointer;
}

.like-btn:hover {
  color: #eb5055 !important;
}

@media (max-width: 759px) {
  .banner {
    height: 220px;
  }

  .banner-title {
    font-size: 1.5rem;
  }

  .talk-info-container {
    margin: -40px 10px 20px;
    padding: 15px;
  }
}
</style>
