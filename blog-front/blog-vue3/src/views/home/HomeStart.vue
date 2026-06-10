<template>
  <main class="home-start">
    <section class="home-start__hero" aria-labelledby="home-start-title">
      <div class="home-start__content">
        <p class="home-start__eyebrow">Start Homepage</p>
        <h1 id="home-start-title" class="home-start__title">
          {{ websiteConfig.websiteName || 'Renzs Blog' }}
        </h1>
        <p class="home-start__intro">
          {{ introText }}
        </p>

        <nav class="home-start__actions" aria-label="首页入口">
          <RouterLink class="home-start__link home-start__link--primary" to="/">
            进入博客
          </RouterLink>
          <RouterLink class="home-start__link" to="/home/content">
            浏览内容
          </RouterLink>
        </nav>
      </div>

      <dl class="home-start__stats" aria-label="博客统计">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="home-start__stat"
        >
          <dt>{{ stat.label }}</dt>
          <dd>{{ stat.value }}</dd>
        </div>
      </dl>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useBlogInfoStore, type BlogInfo, type WebsiteConfig } from '@/stores/blogInfo'

const blogInfoStore = useBlogInfoStore()

const fallbackWebsiteConfig: Partial<WebsiteConfig> = {
  websiteName: 'Renzs Blog',
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

const introText = computed(() => {
  return websiteConfig.value.websiteIntro || websiteConfig.value.websiteNotice || '欢迎来到我的博客。'
})

function toStatValue(value: unknown) {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0
}

const stats = computed(() => [
  {
    label: '文章',
    value: toStatValue(blogInfo.value.articleCount)
  },
  {
    label: '分类',
    value: toStatValue(blogInfo.value.categoryCount)
  },
  {
    label: '标签',
    value: toStatValue(blogInfo.value.tagCount)
  },
  {
    label: '访问',
    value: toStatValue(blogInfo.value.viewsCount)
  }
])
</script>

<style scoped>
.home-start {
  min-height: 100vh;
  padding: 72px 20px 48px;
  color: #172033;
  background:
    radial-gradient(circle at 18% 18%, rgba(70, 126, 255, 0.14), transparent 32%),
    radial-gradient(circle at 82% 16%, rgba(34, 197, 94, 0.12), transparent 28%),
    linear-gradient(135deg, #f8fbff 0%, #eef4f8 54%, #f7f8fb 100%);
}

.home-start__hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 420px);
  align-items: center;
  gap: 56px;
  width: min(1120px, 100%);
  min-height: calc(100vh - 120px);
  margin: 0 auto;
}

.home-start__content {
  min-width: 0;
}

.home-start__eyebrow {
  margin: 0 0 18px;
  color: #3b82f6;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.home-start__title {
  max-width: 760px;
  margin: 0;
  font-size: clamp(40px, 7vw, 78px);
  font-weight: 800;
  line-height: 1.05;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.home-start__intro {
  max-width: 680px;
  margin: 24px 0 0;
  color: #536174;
  font-size: 20px;
  line-height: 1.8;
  overflow-wrap: anywhere;
}

.home-start__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 38px;
}

.home-start__link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 46px;
  padding: 0 22px;
  border: 1px solid rgba(59, 130, 246, 0.28);
  border-radius: 8px;
  color: #1d4ed8;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 12px 30px rgba(30, 64, 175, 0.08);
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.home-start__link:hover,
.home-start__link:focus-visible {
  border-color: rgba(59, 130, 246, 0.58);
  box-shadow: 0 16px 36px rgba(30, 64, 175, 0.14);
  transform: translateY(-2px);
}

.home-start__link--primary {
  color: #fff;
  border-color: #2563eb;
  background: #2563eb;
}

.home-start__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin: 0;
}

.home-start__stat {
  min-height: 144px;
  padding: 24px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 20px 52px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
}

.home-start__stat dt {
  color: #64748b;
  font-size: 14px;
  font-weight: 700;
}

.home-start__stat dd {
  margin: 14px 0 0;
  color: #172033;
  font-size: clamp(28px, 5vw, 38px);
  font-weight: 800;
  line-height: 1;
  overflow-wrap: anywhere;
  word-break: break-word;
}

:global(.dark) .home-start {
  color: #edf3ff;
  background:
    radial-gradient(circle at 18% 18%, rgba(59, 130, 246, 0.28), transparent 32%),
    radial-gradient(circle at 82% 16%, rgba(20, 184, 166, 0.18), transparent 28%),
    linear-gradient(135deg, #101725 0%, #151e2e 54%, #0d1320 100%);
}

:global(.dark) .home-start__eyebrow {
  color: #93c5fd;
}

:global(.dark) .home-start__intro {
  color: #c0ccdc;
}

:global(.dark) .home-start__link {
  color: #bfdbfe;
  border-color: rgba(147, 197, 253, 0.28);
  background: rgba(15, 23, 42, 0.72);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.18);
}

:global(.dark) .home-start__link--primary {
  color: #fff;
  border-color: #3b82f6;
  background: #3b82f6;
}

:global(.dark) .home-start__stat {
  border-color: rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.66);
  box-shadow: 0 20px 52px rgba(0, 0, 0, 0.22);
}

:global(.dark) .home-start__stat dt {
  color: #a8b6ca;
}

:global(.dark) .home-start__stat dd {
  color: #f8fbff;
}

@media (max-width: 860px) {
  .home-start {
    padding: 56px 16px 32px;
  }

  .home-start__hero {
    grid-template-columns: 1fr;
    gap: 36px;
    min-height: auto;
  }
}

@media (max-width: 520px) {
  .home-start__title {
    font-size: 38px;
  }

  .home-start__intro {
    font-size: 17px;
  }

  .home-start__actions,
  .home-start__link {
    width: 100%;
  }

  .home-start__stats {
    grid-template-columns: 1fr;
  }

  .home-start__stat {
    min-height: 112px;
    padding: 20px;
  }
}
</style>
