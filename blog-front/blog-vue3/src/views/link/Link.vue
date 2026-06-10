<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">友情链接</h1>
    </div>
    <!-- 链接列表 -->
    <v-card class="blog-container link-container">
      <div class="link-title mb-4">
        <v-icon color="blue">mdi-link-variant</v-icon> 大佬链接
      </div>
      <v-row class="link-grid">
        <v-col
          class="link-wrapper"
          md="4"
          sm="6"
          cols="12"
          v-for="item of friendLinkList"
          :key="item.id"
        >
          <div class="link-card">
            <a
              :href="item.linkAddress"
              target="_blank"
              rel="noopener"
              class="link-card-main"
            >
              <div class="link-card-head">
                <v-avatar size="72" class="link-avatar">
                  <v-img :src="item.linkAvatar" />
                </v-avatar>
                <span class="link-open-badge">
                  <v-icon size="16">mdi-arrow-top-right</v-icon>
                </span>
              </div>
              <div class="link-name">{{ item.linkName }}</div>
              <div class="link-intro">{{ item.linkIntro }}</div>
            </a>
            <div class="link-card-footer">
              <div class="link-meta">
                <v-icon size="12">mdi-clock-outline</v-icon>
                {{ item.createTime }}
              </div>
              <a
                :href="item.linkAddress"
                target="_blank"
                rel="noopener"
                class="link-card-cta"
              >
                访问站点
                <v-icon size="15">mdi-chevron-right</v-icon>
              </a>
            </div>
          </div>
        </v-col>
      </v-row>
      <div v-if="friendLinkList.length === 0" class="empty-tip">
        暂无友链
      </div>
      <!-- 说明 -->
      <div class="link-title mt-6 mb-4">
        <v-icon color="blue">mdi-dots-horizontal-circle</v-icon> 添加友链
      </div>
      <blockquote>
        <div>名称：{{ websiteConfig?.websiteName }}</div>
        <div>简介：{{ websiteConfig?.websiteIntro }}</div>
        <div>头像：{{ websiteConfig?.websiteAvatar }}</div>
      </blockquote>
      <div class="mt-5 mb-5">
        需要交换友链的可在下方留言💖
      </div>
      <blockquote class="mb-10">
        友链信息展示需要，你的信息格式要包含：名称、介绍、链接、头像
      </blockquote>
      <!-- 评论 -->
      <Comment :type="commentType" />
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getLinks } from '@/api/misc'
import Comment from '@/components/Comment.vue'

interface FriendLink {
  id: number
  linkName: string
  linkIntro: string
  linkAvatar: string
  linkAddress: string
  createTime: string
}

const blogInfoStore = useBlogInfoStore()

const friendLinkList = ref<FriendLink[]>([])
const commentType = 2

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const linkPage = pageList.find(item => item.pageLabel === 'link')
  const coverUrl = linkPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

const websiteConfig = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig
})

async function listFriendLinks() {
  try {
    const { data } = await getLinks()
    friendLinkList.value = data.data || []
  } catch (error) {
    console.error('获取友链列表失败:', error)
  }
}

onMounted(() => {
  listFriendLinks()
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

.link-container {
  max-width: 1200px;
  padding: 30px;
  margin: 20px auto 40px !important;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.link-title {
  color: #344c67;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  line-height: 2;
}

.link-grid {
  margin-top: 6px;
}

.link-wrapper {
  padding: 12px !important;
}

.link-card {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 280px;
  padding: 22px 22px 18px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg);
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
  transition: transform 0.35s ease, box-shadow 0.35s ease, border-color 0.35s ease;
  overflow: hidden;
}

.link-card-main {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  text-align: center;
  text-decoration: none;
  color: inherit;
}

.link-card-head {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 18px;
}

.link-avatar {
  border: 3px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.14);
  transition: transform 0.5s ease;
}

.link-open-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  color: #4c7ce6;
  box-shadow: 0 10px 20px rgba(94, 166, 229, 0.16);
}

.link-name {
  width: 100%;
  overflow: hidden;
  text-align: center;
  text-overflow: ellipsis;
  font-size: 1.65rem;
  font-weight: 700;
  line-height: 1.22;
  color: #2d3448;
}

.link-intro {
  text-align: center;
  width: 100%;
  margin-top: 14px;
  color: #5f6472;
  font-size: 0.92rem;
  line-height: 1.8;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 3.6em;
}

.link-card:hover {
  border-color: var(--card-border-accent-hover);
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-6px);
}

.link-card:hover .link-avatar {
  transform: translateY(-2px) scale(1.04);
}

.link-card-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px solid rgba(15, 23, 42, 0.06);
}

.link-meta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  font-size: 0.76rem;
  font-weight: 600;
  color: #7a8698;
}

.link-card-cta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #344c67 !important;
  font-size: 0.82rem;
  font-weight: 700;
  text-decoration: none;
  transition: transform 0.3s ease, color 0.3s ease;
  white-space: nowrap;
}

.link-card-cta:hover {
  color: #2ba1d1 !important;
  transform: translateX(2px);
}

blockquote {
  line-height: 2;
  margin: 0;
  font-size: 15px;
  border-left: 0.2rem solid #49b1f5;
  padding: 10px 1rem !important;
  background:
    linear-gradient(90deg, rgba(73, 177, 245, 0.12), rgba(73, 177, 245, 0.04)),
    rgba(255, 255, 255, 0.7);
  border-radius: 14px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
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

  .link-container {
    margin: 20px 10px 20px;
    padding: 15px;
  }

  .link-wrapper {
    padding: 8px 0 !important;
  }

  .link-card {
    min-height: auto;
    padding: 20px 18px 16px;
  }

  .link-name {
    font-size: 1.4rem;
  }

  .link-card-footer {
    flex-wrap: wrap;
  }

  .link-card-cta {
    margin-left: auto;
  }
}
</style>
