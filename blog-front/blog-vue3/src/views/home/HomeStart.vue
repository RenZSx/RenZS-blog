<template>
  <main class="start-page" :style="pageStyle">
    <div class="start-page__shade" aria-hidden="true"></div>
    <div class="start-page__glow" aria-hidden="true"></div>

    <section class="start-shell" aria-labelledby="start-title">
      <!-- 时钟与问候 -->
      <header class="start-hero">
        <p class="hero-greeting">{{ greeting }},{{ authorName }}</p>
        <h1 id="start-title" class="hero-clock">{{ clock.time }}</h1>
        <p class="hero-date">{{ clock.date }} · {{ clock.weekday }}</p>
      </header>

      <!-- 全站搜索 -->
      <form class="search-shell" @submit.prevent="openSearch">
        <v-icon size="21" class="search-icon">mdi-magnify</v-icon>
        <div class="engine-toggle" role="tablist" aria-label="搜索引擎">
          <button
            type="button"
            class="engine-toggle__item"
            :class="{ 'engine-toggle__item--on': engine === 'site' }"
            @click="engine = 'site'"
          >
            站内
          </button>
          <button
            type="button"
            class="engine-toggle__item"
            :class="{ 'engine-toggle__item--on': engine === 'bing' }"
            @click="engine = 'bing'"
          >
            Bing
          </button>
        </div>
        <input
          v-model="searchInput"
          class="search-input"
          type="text"
          :placeholder="engine === 'bing' ? '用 Bing 搜索,回车直达…' : '搜索全站文章,回车直达…'"
          aria-label="搜索"
          @keydown.enter.prevent="openSearch"
        />
        <button v-if="searchInput" class="search-clear" type="button" aria-label="清空" @click="searchInput = ''">
          <v-icon size="17">mdi-close</v-icon>
        </button>
        <button class="search-go" type="submit">搜索</button>
      </form>

      <!-- 快捷入口 -->
      <nav class="quick-grid" aria-label="快捷入口">
        <RouterLink
          v-for="entry in quickEntries"
          :key="entry.label"
          class="quick-entry"
          :to="entry.to"
        >
          <span class="quick-entry__icon"><v-icon size="22">{{ entry.icon }}</v-icon></span>
          <span class="quick-entry__name">{{ entry.label }}</span>
          <span class="quick-entry__desc">{{ entry.desc }}</span>
        </RouterLink>
      </nav>

      <!-- 底部信息条:公告 + 统计 + 社交 -->
      <footer class="start-foot">
        <div class="foot-notice" :title="noticeText">
          <v-icon size="16" class="foot-notice__icon">mdi-bullhorn-outline</v-icon>
          <span>{{ noticeText }}</span>
        </div>
        <div class="foot-stats">
          <span v-for="stat in stats" :key="stat.label" class="foot-stat">
            <b>{{ stat.value }}</b>{{ stat.label }}
          </span>
        </div>
        <nav v-if="socialLinks.length" class="foot-social" aria-label="社交链接">
          <a
            v-for="link in socialLinks"
            :key="link.type"
            :href="link.href"
            target="_blank"
            rel="noopener noreferrer"
            :aria-label="link.label"
          >
            <span :class="['iconfont', link.icon]"></span>
          </a>
        </nav>
      </footer>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useBlogInfoStore, type BlogInfo, type WebsiteConfig } from '@/stores/blogInfo'
import { useUIStore } from '@/stores/ui'

const blogInfoStore = useBlogInfoStore()
const uiStore = useUIStore()
const now = ref(new Date())
const searchInput = ref('')
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

const authorName = computed(() => websiteConfig.value.websiteAuthor || '朋友')
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

// 时钟与时段问候
const clock = computed(() => ({
  time: new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(now.value),
  date: new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric'
  }).format(now.value),
  weekday: new Intl.DateTimeFormat('zh-CN', { weekday: 'long' }).format(now.value)
}))

const greeting = computed(() => {
  const hour = now.value.getHours()
  if (hour < 5) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 23) return '晚上好'
  return '夜深了'
})

// 搜索引擎:站内走搜索弹窗,Bing 新标签页直达
const engine = ref<'site' | 'bing'>('site')

function openSearch() {
  const keyword = searchInput.value.trim()
  if (!keyword) return
  if (engine.value === 'bing') {
    window.open('https://www.bing.com/search?q=' + encodeURIComponent(keyword) + '&ensearch=0', '_blank', 'noopener')
    return
  }
  uiStore.setSearchKeyword(keyword)
  uiStore.setSearchFlag(true)
}

const quickEntries = [
  { label: '博客', desc: '最新文章', icon: 'mdi-rss', to: '/' },
  { label: '归档', desc: '全部文章', icon: 'mdi-text-box-multiple-outline', to: '/archives' },
  { label: '相册', desc: '光影记录', icon: 'mdi-image-multiple-outline', to: '/albums' },
  { label: '网址集', desc: '收藏夹', icon: 'mdi-book-open-page-variant', to: '/home/content' },
  { label: '今日热榜', desc: '热点速览', icon: 'mdi-fire', to: '/home/columns' },
  { label: '留言板', desc: '说点什么', icon: 'mdi-message-text-outline', to: '/message' }
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
  }, 10000)
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
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #fff;
  background:
    linear-gradient(160deg, #14213d 0%, #1f3a5f 48%, #10203a 100%);
  background-position: center;
  background-size: cover;
}

.start-page__shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(8, 14, 28, 0.55), rgba(8, 14, 28, 0.25) 45%, rgba(8, 14, 28, 0.66));
}

/* 顶部一角的环境光 */
.start-page__glow {
  position: absolute;
  top: -180px;
  right: -120px;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(96, 148, 255, 0.28), transparent 68%);
  pointer-events: none;
}

.start-shell {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  gap: 34px;
  width: min(880px, calc(100% - 48px));
  margin: 0 auto;
  padding: 84px 0 44px;
}

/* ========== 时钟 ========== */
.start-hero {
  text-align: center;
}

.hero-greeting {
  margin: 0 0 6px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 16px;
  letter-spacing: 0.14em;
}

.hero-clock {
  margin: 0;
  font-family: 'SFMono-Regular', 'Cascadia Code', Consolas, monospace;
  font-size: clamp(64px, 13vw, 108px);
  font-weight: 700;
  line-height: 1.05;
  letter-spacing: 0.04em;
  text-shadow: 0 10px 40px rgba(0, 0, 0, 0.35);
  font-variant-numeric: tabular-nums;
}

.hero-date {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.62);
  font-size: 14px;
  letter-spacing: 0.2em;
}

/* ========== 搜索 ========== */
.search-shell {
  display: flex;
  align-items: center;
  gap: 10px;
  width: min(620px, 100%);
  margin: 0 auto;
  padding: 6px 8px 6px 18px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(14px);
  transition: border-color 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
}

.search-shell:focus-within {
  border-color: rgba(140, 190, 255, 0.75);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 12px 40px rgba(20, 40, 90, 0.35);
}

.search-icon {
  color: rgba(255, 255, 255, 0.66);
}

/* 引擎切换 */
.engine-toggle {
  display: inline-flex;
  flex: 0 0 auto;
  gap: 2px;
  padding: 3px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
}

.engine-toggle__item {
  padding: 4px 11px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.engine-toggle__item--on {
  background: rgba(140, 190, 255, 0.28);
  color: #fff;
}

.engine-toggle__item:hover {
  color: #fff;
}

.search-input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  color: #fff;
  font-size: 15px;
  line-height: 40px;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-clear {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
}

.search-go {
  flex: 0 0 auto;
  padding: 9px 22px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(120deg, #3f7bff, #6a5cff);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.08em;
  cursor: pointer;
  transition: filter 0.2s ease, transform 0.2s ease;
}

.search-go:hover {
  filter: brightness(1.12);
  transform: translateY(-1px);
}

/* ========== 快捷入口 ========== */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.quick-entry {
  display: grid;
  justify-items: center;
  gap: 3px;
  padding: 16px 8px 13px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  color: #fff;
  text-decoration: none;
  text-align: center;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.quick-entry:hover {
  border-color: rgba(140, 190, 255, 0.55);
  background: rgba(255, 255, 255, 0.14);
  transform: translateY(-3px);
}

.quick-entry__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  margin-bottom: 3px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
}

.quick-entry__name {
  font-size: 13.5px;
  font-weight: 600;
}

.quick-entry__desc {
  color: rgba(255, 255, 255, 0.52);
  font-size: 11px;
}

/* ========== 底部信息条 ========== */
.start-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 22px;
  padding: 13px 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  background: rgba(10, 18, 38, 0.42);
  backdrop-filter: blur(10px);
  font-size: 13px;
}

.foot-notice {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1 1 260px;
  color: rgba(255, 255, 255, 0.8);
}

.foot-notice__icon {
  flex: 0 0 auto;
  color: #8bc5ff;
}

.foot-notice span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.foot-stats {
  display: inline-flex;
  gap: 16px;
  margin-left: auto;
}

.foot-stat {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.foot-stat b {
  color: #fff;
  font-size: 15px;
}

.foot-social {
  display: inline-flex;
  gap: 14px;
}

.foot-social a {
  color: rgba(255, 255, 255, 0.78);
  font-size: 18px;
  text-decoration: none;
  transition: color 0.2s ease, transform 0.2s ease;
}

.foot-social a:hover {
  color: #8bc5ff;
  transform: translateY(-2px);
}

/* ========== 响应式 ========== */
@media (max-width: 960px) {
  .start-shell {
    gap: 26px;
    width: calc(100% - 32px);
    padding: 96px 0 34px;
  }

  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .foot-stats {
    margin-left: 0;
  }
}

@media (max-width: 560px) {
  .hero-clock {
    font-size: 58px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .start-foot {
    flex-direction: column;
    align-items: flex-start;
  }

  .foot-social {
    margin-left: auto;
  }
}
</style>
