<template>
  <main class="start-page" :style="pageStyle">
    <div class="start-page__shade"></div>
    <section class="start-shell" aria-labelledby="start-title">
      <div class="profile-panel">
        <div class="profile-head">
          <v-avatar size="96" class="profile-avatar">
            <v-img :src="avatarUrl" />
          </v-avatar>
          <div class="profile-copy">
            <p class="profile-kicker">Personal Start</p>
            <h1 id="start-title" class="profile-name">{{ siteName }}</h1>
          </div>
        </div>

        <div class="heatmap" aria-label="站点活动">
          <span
            v-for="(cell, index) in heatCells"
            :key="index"
            class="heatmap-cell"
            :class="`heatmap-cell--${cell}`"
          ></span>
        </div>

        <div class="profile-line"></div>

        <div class="quote-card glass-card">
          <v-icon size="26" class="quote-mark">mdi-format-quote-open</v-icon>
          <div>
            <strong>{{ introTitle }}</strong>
            <p>{{ introText }}</p>
          </div>
          <v-icon size="24" class="quote-mark quote-mark--end">mdi-format-quote-close</v-icon>
        </div>

        <nav v-if="socialLinks.length" class="social-links" aria-label="社交链接">
          <a
            v-for="link in socialLinks"
            :key="link.type"
            :href="link.href"
            class="social-link"
            target="_blank"
            rel="noopener noreferrer"
            :aria-label="link.label"
          >
            <span :class="['iconfont', link.icon]"></span>
          </a>
        </nav>
      </div>

      <div class="dashboard-panel">
        <div class="dashboard-top">
          <article class="poem-card glass-card">
            <p>{{ noticeText }}</p>
            <strong>- 「说苑」</strong>
          </article>

          <article class="clock-card glass-card">
            <p>{{ currentDateText }}</p>
          <strong>{{ currentTimeText }}</strong>
            <div class="music-line">
              <span>Dancing Line - Local</span>
              <small>00:00 / 01:24</small>
            </div>
          </article>
        </div>

        <nav class="quick-grid" aria-label="快捷入口">
          <RouterLink
            v-for="entry in quickEntries"
            :key="entry.label"
            class="quick-entry glass-card"
            :to="entry.to"
          >
            <v-icon size="22">{{ entry.icon }}</v-icon>
            <span>{{ entry.label }}</span>
          </RouterLink>
        </nav>

        <div class="stat-strip">
          <div v-for="stat in stats" :key="stat.label" class="stat-item">
            <span>{{ stat.label }}</span>
            <strong>{{ stat.value }}</strong>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useBlogInfoStore, type BlogInfo, type WebsiteConfig } from '@/stores/blogInfo'

const blogInfoStore = useBlogInfoStore()
const now = ref(new Date())
let clockTimer: ReturnType<typeof setInterval> | null = null

const fallbackWebsiteConfig: Partial<WebsiteConfig> = {
  websiteName: 'Renzs Blog',
  websiteAuthor: 'Renzs',
  websiteAvatar: '',
  websiteIntro: '',
  websiteNotice: ''
}

const blogInfo = computed<Partial<BlogInfo>>(() => {
  const value = blogInfoStore.blogInfo
  return value && typeof value === 'object' ? value : {}
})

const websiteConfig = computed<Partial<WebsiteConfig>>(() => {
  const config = blogInfo.value.websiteConfig
  return config && typeof config === 'object'
    ? { ...fallbackWebsiteConfig, ...config }
    : fallbackWebsiteConfig
})

const siteName = computed(() => {
  return websiteConfig.value.websiteName || websiteConfig.value.websiteAuthor || 'Renzs Blog'
})

const avatarUrl = computed(() => {
  return websiteConfig.value.websiteAvatar || websiteConfig.value.touristAvatar || ''
})

const introTitle = computed(() => websiteConfig.value.websiteAuthor || siteName.value)
const introText = computed(() => {
  return websiteConfig.value.websiteIntro || '一个建立于互联网边缘的博客，记录技术、生活与灵感。'
})
const noticeText = computed(() => {
  return websiteConfig.value.websiteNotice || '乘风破浪，虎豹避之；飞鸟成列，鹰鹫不击。'
})

const startCover = computed(() => {
  const startPage = blogInfo.value.pageList?.find(item => item.pageLabel === 'start')
  const homePage = blogInfo.value.pageList?.find(item => item.pageLabel === 'home')
  return startPage?.pageCover || homePage?.pageCover || ''
})

const pageStyle = computed(() => {
  if (!startCover.value) return {}
  return {
    backgroundImage: `url(${startCover.value})`
  }
})

const currentDateText = computed(() => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'long'
  }).format(now.value)
})

const currentTimeText = computed(() => {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(now.value)
})

const heatCells = computed(() => {
  return Array.from({ length: 70 }, (_, index) => {
    const seed = (index * 17 + toStatValue(blogInfo.value.articleCount) + toStatValue(blogInfo.value.viewsCount)) % 11
    if (seed > 8) return 'hot'
    if (seed > 5) return 'warm'
    if (seed > 2) return 'cool'
    return 'empty'
  })
})

const quickEntries = [
  { label: '博客', icon: 'mdi-rss', to: '/' },
  { label: '网盘', icon: 'mdi-cloud', to: '/links' },
  { label: '起始页', icon: 'mdi-compass', to: '/home/start' },
  { label: '网址集', icon: 'mdi-book-open-page-variant', to: '/home/content' },
  { label: '今日热榜', icon: 'mdi-fire', to: '/home/columns' }
] as const

const socialLinks = computed(() => {
  const links = [
    { type: 'github', label: 'GitHub', icon: 'icongithub', href: websiteConfig.value.github },
    { type: 'gitee', label: 'Gitee', icon: 'icongitee-fill-round', href: websiteConfig.value.gitee },
    {
      type: 'qq',
      label: 'QQ',
      icon: 'iconqq',
      href: websiteConfig.value.qq
        ? `http://wpa.qq.com/msgrd?v=3&uin=${websiteConfig.value.qq}&site=qq&menu=yes`
        : ''
    }
  ]
  return links.filter(link => Boolean(link.href))
})

function toStatValue(value: unknown) {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0
}

const stats = computed(() => [
  { label: '文章', value: toStatValue(blogInfo.value.articleCount) },
  { label: '分类', value: toStatValue(blogInfo.value.categoryCount) },
  { label: '标签', value: toStatValue(blogInfo.value.tagCount) },
  { label: '访问', value: toStatValue(blogInfo.value.viewsCount) }
])

onMounted(() => {
  now.value = new Date()
  clockTimer = setInterval(() => {
    now.value = new Date()
  }, 60000)
})

onBeforeUnmount(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
    clockTimer = null
  }
})
</script>

<style scoped>
.start-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #fff;
  background:
    linear-gradient(135deg, #5d2f28 0%, #41647c 48%, #162436 100%);
  background-position: center;
  background-size: cover;
}

.start-page__shade {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 42% 58%, rgba(255, 234, 169, 0.28), transparent 28%),
    linear-gradient(90deg, rgba(30, 14, 14, 0.42), rgba(12, 24, 40, 0.18) 48%, rgba(9, 15, 26, 0.58));
}

.start-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(420px, 520px);
  align-items: center;
  gap: 74px;
  width: min(1060px, calc(100% - 56px));
  min-height: 100vh;
  margin: 0 auto;
  padding: 78px 0 54px;
}

.profile-panel,
.dashboard-panel {
  min-width: 0;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 18px;
}

.profile-avatar {
  flex: 0 0 auto;
  border: 2px solid rgba(255, 255, 255, 0.42);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.22);
}

.profile-kicker {
  margin: 0 0 6px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
  font-weight: 700;
}

.profile-name {
  margin: 0;
  font-size: clamp(42px, 6vw, 58px);
  font-weight: 800;
  line-height: 1;
  text-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
  overflow-wrap: anywhere;
}

.heatmap {
  display: grid;
  grid-template-columns: repeat(14, 1fr);
  gap: 4px;
  width: min(382px, 100%);
  margin-top: 38px;
}

.heatmap-cell {
  aspect-ratio: 1;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.72);
}

.heatmap-cell--cool {
  background: rgba(111, 204, 135, 0.72);
}

.heatmap-cell--warm {
  background: rgba(45, 172, 79, 0.86);
}

.heatmap-cell--hot {
  background: rgba(166, 86, 213, 0.9);
}

.profile-line {
  width: 86px;
  height: 5px;
  margin: 16px 0 40px;
  border-radius: 999px;
  background: rgba(142, 244, 205, 0.9);
}

.glass-card {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 4px;
  background: rgba(42, 38, 34, 0.54);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.14);
}

.quote-card {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  width: min(420px, 100%);
  padding: 22px 24px;
}

.quote-card strong,
.quote-card p {
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.28);
}

.quote-card p {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
  line-height: 1.7;
}

.quote-mark {
  color: rgba(255, 255, 255, 0.72);
}

.quote-mark--end {
  align-self: end;
}

.social-links {
  display: flex;
  gap: 18px;
  margin-top: 18px;
}

.social-link {
  color: rgba(255, 255, 255, 0.86);
  font-size: 20px;
  text-decoration: none;
  transition: color 0.2s ease, transform 0.2s ease;
}

.social-link:hover {
  color: #fff;
  transform: translateY(-2px);
}

.dashboard-panel {
  display: grid;
  gap: 14px;
}

.dashboard-top {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.poem-card,
.clock-card {
  min-height: 168px;
  padding: 22px 24px;
}

.poem-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.poem-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 15px;
  font-weight: 700;
  line-height: 1.9;
}

.poem-card strong {
  align-self: flex-end;
  color: rgba(255, 255, 255, 0.88);
}

.clock-card p {
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  font-weight: 700;
}

.clock-card strong {
  display: block;
  font-family: Consolas, "Courier New", monospace;
  font-size: clamp(34px, 4vw, 43px);
  line-height: 1.05;
  text-shadow: 0 0 18px rgba(255, 255, 255, 0.2);
}

.music-line {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.18);
}

.music-line span,
.music-line small {
  display: block;
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
}

.music-line small {
  margin-top: 4px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.quick-entry {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  min-height: 74px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 15px;
  font-weight: 800;
  text-decoration: none;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.quick-entry:hover {
  border-color: rgba(255, 255, 255, 0.34);
  background: rgba(255, 255, 255, 0.16);
  transform: translateY(-2px);
}

.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-top: 4px;
}

.stat-item {
  text-align: center;
  color: rgba(255, 255, 255, 0.82);
}

.stat-item span,
.stat-item strong {
  display: block;
}

.stat-item span {
  font-size: 12px;
}

.stat-item strong {
  margin-top: 4px;
  font-size: 18px;
}

@media (max-width: 960px) {
  .start-shell {
    grid-template-columns: 1fr;
    gap: 34px;
    width: min(640px, calc(100% - 32px));
    padding: 86px 0 34px;
  }

  .dashboard-top,
  .quick-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 560px) {
  .profile-head {
    align-items: flex-start;
  }

  .profile-avatar {
    width: 72px !important;
    height: 72px !important;
  }

  .profile-name {
    font-size: 34px;
  }

  .dashboard-top,
  .quick-grid,
  .stat-strip {
    grid-template-columns: 1fr;
  }

  .quote-card {
    grid-template-columns: 1fr;
  }

  .quote-mark--end {
    display: none;
  }
}
</style>
