<template>
  <div class="auth-screen">
    <div class="auth-background">
      <div class="auth-orb auth-orb-one" />
      <div class="auth-orb auth-orb-two" />
      <div class="auth-orb auth-orb-three" />
    </div>

    <section class="auth-stage">
      <div class="auth-hero" :style="heroStyle">
        <div class="auth-hero-overlay" />
        <div class="auth-hero-content">
          <div class="auth-kicker">{{ kicker }}</div>
          <h1 class="auth-title">{{ headline }}</h1>
          <p class="auth-description">{{ description }}</p>

          <div class="auth-quote">
            <span class="auth-quote-line" />
            <p>{{ quote }}</p>
          </div>

          <div class="auth-badges">
            <span v-for="item in badges" :key="item" class="auth-badge">{{ item }}</span>
          </div>

          <div class="auth-brand">
            <v-avatar size="52" class="auth-brand-avatar">
              <v-img :src="websiteConfig.websiteAvatar || fallbackAvatar" />
            </v-avatar>
            <div>
              <div class="auth-brand-name">{{ websiteConfig.websiteAuthor || websiteConfig.websiteName }}</div>
              <div class="auth-brand-meta">{{ websiteConfig.websiteIntro || websiteConfig.websiteNotice }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-panel">
        <div class="auth-card">
          <div class="auth-tabs">
            <router-link
              v-for="item in navItems"
              :key="item.to"
              :to="resolveRoute(item.to)"
              class="auth-tab"
              :class="{ 'auth-tab-active': current === item.key }"
            >
              {{ item.label }}
            </router-link>
          </div>

          <div class="auth-card-head">
            <div class="auth-card-kicker">{{ panelKicker }}</div>
            <h2 class="auth-card-title">{{ panelTitle }}</h2>
            <p class="auth-card-subtitle">{{ panelSubtitle }}</p>
          </div>

          <div class="auth-card-body">
            <slot />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'

interface Props {
  kicker: string
  headline: string
  description: string
  quote: string
  badges: string[]
  current: 'login' | 'register' | 'forgot-password'
  panelKicker: string
  panelTitle: string
  panelSubtitle: string
}

defineProps<Props>()

const route = useRoute()
const blogInfoStore = useBlogInfoStore()

const websiteConfig = computed(() => blogInfoStore.blogInfo.websiteConfig)
const fallbackAvatar = computed(() => websiteConfig.value.touristAvatar || '')

const heroStyle = computed(() => {
  const cover = blogInfoStore.getPageCover('home') || blogInfoStore.getPageCover('about')
  return cover
    ? {
        backgroundImage: `linear-gradient(180deg, rgba(7, 14, 28, 0.18), rgba(7, 14, 28, 0.74)), url(${cover})`
      }
    : undefined
})

const navItems = [
  { key: 'login', label: '登录', to: '/login' },
  { key: 'register', label: '注册', to: '/register' },
  { key: 'forgot-password', label: '找回密码', to: '/forgot-password' }
] as const

function resolveRoute(path: string) {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : undefined
  return redirect ? { path, query: { redirect } } : { path }
}
</script>

<style scoped>
.auth-screen {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(73, 177, 245, 0.18), transparent 28%),
    linear-gradient(160deg, #08111f 0%, #10243f 44%, #0a172b 100%);
}

.auth-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(8px);
  opacity: 0.72;
}

.auth-orb-one {
  top: 8%;
  left: -4%;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(67, 180, 255, 0.42), transparent 68%);
}

.auth-orb-two {
  right: 10%;
  top: 20%;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(141, 123, 255, 0.26), transparent 70%);
}

.auth-orb-three {
  right: -2%;
  bottom: 2%;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(82, 203, 196, 0.18), transparent 72%);
}

.auth-stage {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.18fr 0.82fr;
  align-items: stretch;
  min-height: 100vh;
  padding: 34px;
  gap: 24px;
}

.auth-hero,
.auth-card {
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 30px 80px rgba(3, 10, 22, 0.35);
}

.auth-hero {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  background:
    radial-gradient(circle at top left, rgba(109, 178, 255, 0.16), transparent 35%),
    linear-gradient(160deg, #0b1324 0%, #11284d 55%, #0b1830 100%);
  background-position: center;
  background-size: cover;
}

.auth-hero-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(5, 10, 18, 0.16), rgba(5, 10, 18, 0.72)),
    radial-gradient(circle at top left, rgba(125, 186, 255, 0.28), transparent 34%);
}

.auth-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  padding: 48px;
  color: #f7fbff;
}

.auth-kicker,
.auth-card-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.auth-kicker {
  color: #9fd0ff;
}

.auth-title {
  max-width: 560px;
  margin-top: 18px;
  font-family: Georgia, 'Times New Roman', 'Noto Serif SC', serif;
  font-size: clamp(34px, 4vw, 54px);
  line-height: 1.12;
  letter-spacing: 0.01em;
}

.auth-description {
  max-width: 500px;
  margin-top: 16px;
  color: rgba(244, 249, 255, 0.78);
  font-size: 16px;
  line-height: 1.85;
}

.auth-quote {
  max-width: 420px;
  margin-top: auto;
  color: rgba(244, 249, 255, 0.8);
  font-size: 15px;
  line-height: 1.8;
}

.auth-quote-line {
  display: block;
  width: 64px;
  height: 1px;
  margin-bottom: 18px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.75), transparent);
}

.auth-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 26px;
}

.auth-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(246, 250, 255, 0.9);
  font-size: 12px;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 28px;
}

.auth-brand-avatar {
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
}

.auth-brand-name {
  font-size: 16px;
  font-weight: 700;
}

.auth-brand-meta {
  max-width: 360px;
  color: rgba(246, 250, 255, 0.7);
  font-size: 13px;
}

.auth-panel {
  display: flex;
  align-items: center;
}

.auth-card {
  width: 100%;
  padding: 24px;
  border-radius: 30px;
  background:
    radial-gradient(circle at top right, rgba(73, 177, 245, 0.14), transparent 24%),
    rgba(248, 251, 255, 0.92);
  backdrop-filter: blur(18px) saturate(120%);
  box-shadow:
    var(--card-shadow-raised),
    inset 0 1px 0 rgba(255, 255, 255, 0.42);
}

.auth-tabs {
  display: inline-flex;
  gap: 8px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.auth-tab {
  padding: 10px 14px;
  border-radius: 999px;
  color: #607286;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.22s ease;
}

.auth-tab:hover {
  text-decoration: none;
  color: #1a2434;
}

.auth-tab-active {
  background: #fff;
  color: #172233;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.1);
}

.auth-card-head {
  margin-top: 28px;
}

.auth-card-kicker {
  color: #3b90d0;
}

.auth-card-title {
  margin-top: 10px;
  color: #132034;
  font-family: Georgia, 'Times New Roman', 'Noto Serif SC', serif;
  font-size: clamp(28px, 3vw, 38px);
  line-height: 1.18;
}

.auth-card-subtitle {
  margin-top: 12px;
  color: #5e6f84;
  font-size: 14px;
  line-height: 1.8;
}

.auth-card-body {
  margin-top: 28px;
}

@media (max-width: 1100px) {
  .auth-stage {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .auth-hero {
    min-height: 320px;
  }

  .auth-hero-content {
    padding: 30px 24px;
  }

  .auth-panel {
    align-items: stretch;
  }
}

@media (max-width: 759px) {
  .auth-screen {
    background:
      radial-gradient(circle at top, rgba(73, 177, 245, 0.2), transparent 32%),
      linear-gradient(180deg, #0a1324 0%, #0d1d36 100%);
  }

  .auth-stage {
    gap: 14px;
    padding: 10px;
  }

  .auth-hero {
    min-height: 250px;
    border-radius: 24px;
  }

  .auth-card {
    padding: 18px;
    border-radius: 24px;
  }

  .auth-title {
    font-size: 32px;
  }

  .auth-brand-meta {
    max-width: none;
  }
}
</style>
