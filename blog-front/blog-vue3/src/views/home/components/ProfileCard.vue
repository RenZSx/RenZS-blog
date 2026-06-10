<template>
  <v-card class="content home-card home-profile-card">
    <div class="author-wrapper">
      <v-avatar size="64">
        <v-img class="author-avatar" :src="websiteConfig.websiteAvatar" alt="avatar" />
      </v-avatar>
      <div class="home-profile-name">
        {{ websiteConfig.websiteAuthor }}
      </div>
      <div class="home-profile-intro">
        {{ websiteConfig.websiteIntro }}
      </div>
    </div>
    <div class="blog-info-wrapper">
      <div class="blog-info-data">
        <router-link to="/archives">
          <div class="home-profile-label">💌文章</div>
          <div class="home-profile-value">
            {{ blogInfo.articleCount }}
          </div>
        </router-link>
      </div>
      <div class="blog-info-data">
        <router-link to="/categories">
          <div class="home-profile-label">⚡分类</div>
          <div class="home-profile-value">
            {{ blogInfo.categoryCount }}
          </div>
        </router-link>
      </div>
      <div class="blog-info-data">
        <router-link to="/tags">
          <div class="home-profile-label">📰标签</div>
          <div class="home-profile-value">{{ blogInfo.tagCount }}</div>
        </router-link>
      </div>
    </div>
    <a class="collection-btn" @click="emit('bookmark-tip')">
      <v-icon color="#40DEDE" size="18" class="mr-1">mdi-bookmark</v-icon>
      加入书签
    </a>
    <div v-if="hasSocialLinks" class="card-info-social">
      <a
        v-if="isShowSocial('qq')"
        class="mr-5 iconfont iconqq"
        target="_blank"
        :href="'http://wpa.qq.com/msgrd?v=3&uin=' + websiteConfig.qq + '&site=qq&menu=yes'"
      />
      <a
        v-if="isShowSocial('github')"
        target="_blank"
        :href="websiteConfig.github"
        class="mr-5 iconfont icongithub"
      />
      <a
        v-if="isShowSocial('gitee')"
        target="_blank"
        :href="websiteConfig.gitee"
        class="iconfont icongitee-fill-round"
      />
    </div>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface BlogInfo {
  articleCount: number
  categoryCount: number
  tagCount: number
  [key: string]: any
}

interface WebsiteConfig {
  websiteAuthor: string
  websiteAvatar: string
  websiteIntro: string
  qq?: string
  github?: string
  gitee?: string
  socialUrlList?: string[]
  [key: string]: any
}

interface Props {
  blogInfo: BlogInfo
  websiteConfig: WebsiteConfig
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'bookmark-tip': []
}>()

const socialTypes = ['qq', 'github', 'gitee'] as const

const hasSocialLinks = computed(() => socialTypes.some(social => isShowSocial(social)))

function isShowSocial(social: typeof socialTypes[number]) {
  const socialUrlList = props.websiteConfig.socialUrlList || []
  if (socialUrlList.includes(social)) {
    return true
  }
  return Boolean(props.websiteConfig[social])
}
</script>

<style scoped>
.home-profile-card {
  position: relative;
  min-height: 276px;
  border: 1px solid var(--glass-border);
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  padding: 0.95rem 1rem;
  border-radius: var(--card-radius-md) !important;
  overflow: hidden;
  transition: transform 0.32s ease, border-color 0.32s ease, box-shadow 0.32s ease;
}

.home-profile-card:hover {
  transform: translateY(-3px);
  border-color: var(--glass-border-hover);
  box-shadow: var(--glass-shadow-hover);
}

.home-profile-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(
      circle at 14% 12%,
      rgba(255, 255, 255, 0.5),
      transparent 28%
    ),
    radial-gradient(
      circle at 84% 14%,
      rgba(132, 190, 255, 0.34),
      transparent 32%
    ),
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.38) 0%,
      rgba(255, 255, 255, 0.04) 34%,
      rgba(255, 255, 255, 0.16) 100%
    );
  pointer-events: none;
}

.author-wrapper {
  margin: -0.2rem -0.15rem 0.7rem;
  padding: 0.9rem 0.8rem 0.85rem;
  border: 1px solid var(--glass-border);
  border-radius: 18px;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.42),
    rgba(255, 255, 255, 0.16)
  );
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5),
    0 14px 30px rgba(111, 149, 189, 0.12);
  backdrop-filter: blur(14px) saturate(130%);
  text-align: center;
  position: relative;
  z-index: 1;
}

.author-avatar {
  transition: all 0.5s;
}

.author-avatar:hover {
  transform: rotate(360deg);
}

.home-profile-name {
  margin-top: 0.5rem;
  font-size: 1.15rem;
  font-weight: 600;
}

.home-profile-intro {
  font-size: 0.78rem;
  color: rgba(0, 0, 0, 0.6);
  line-height: 1.6;
}

.blog-info-wrapper {
  margin: 0 0 0.75rem;
  padding: 0.68rem 0.2rem;
  border-radius: 15px;
  border: 1px solid var(--glass-border);
  background: rgba(255, 255, 255, 0.42);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72),
    0 10px 24px rgba(111, 149, 189, 0.08);
  backdrop-filter: blur(12px) saturate(130%);
  display: flex;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.blog-info-data {
  flex: 1;
  text-align: center;
}

.blog-info-data a {
  text-decoration: none;
  color: inherit;
}

.home-profile-label {
  font-size: 0.76rem;
  color: rgba(0, 0, 0, 0.6);
}

.home-profile-value {
  font-size: 1rem;
  font-weight: 600;
}

.collection-btn {
  display: block;
  text-align: center;
  padding: 6px 8px;
  border-radius: 8px;
  background-color: #49b1f5;
  color: #fff !important;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
  position: relative;
  z-index: 1;
  transition: all 0.3s;
}

.collection-btn:hover {
  background-color: #ff7242;
}

.card-info-social {
  line-height: 34px;
  text-align: center;
  margin: 8px 0 -4px;
  position: relative;
  z-index: 1;
}

.card-info-social a {
  font-size: 1.25rem;
  color: inherit;
  text-decoration: none;
}
</style>
