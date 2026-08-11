<template>
  <div class="about-page">
    <section class="about-hero" :style="cover">
      <div class="about-hero__scrim" />
      <div class="about-hero__inner">
        <div class="terminal-path">
          <span class="terminal-path__dot" aria-hidden="true" />
          <span>~/about</span>
        </div>
        <h1><span class="hero-prompt">$</span> whoami</h1>
        <p class="about-hero__name">{{ author }}</p>
        <p class="about-hero__tagline">building quietly, shipping deliberately.</p>
      </div>
      <span class="about-hero__cursor" aria-hidden="true">_</span>
    </section>

    <main class="about-main">
      <section id="profile" class="terminal-window">
        <header class="terminal-window__bar">
          <div class="terminal-controls" aria-hidden="true">
            <span class="terminal-controls__dot terminal-controls__dot--red" />
            <span class="terminal-controls__dot terminal-controls__dot--amber" />
            <span class="terminal-controls__dot terminal-controls__dot--green" />
          </div>
          <span class="terminal-window__title">profile.md — zsh</span>
          <span class="terminal-window__path">UTF-8</span>
        </header>

        <div class="terminal-window__body">
          <div class="terminal-command">
            <span class="terminal-prompt">{{ prompt }}</span>
            <span>cat profile.md</span>
          </div>

          <div class="profile-layout">
            <aside class="profile-sidebar">
              <div class="avatar-frame">
                <v-avatar size="112">
                  <v-img class="author-avatar" :src="avatar" alt="博主头像" />
                </v-avatar>
              </div>
              <div class="profile-id">
                <span class="profile-label">user</span>
                <strong>{{ author }}</strong>
              </div>
              <div class="profile-status">
                <span class="profile-status__dot" aria-hidden="true" />
                <span>available for ideas</span>
              </div>
              <nav class="profile-nav" aria-label="关于页面导航">
                <a href="#profile"><span>01</span> ./profile</a>
                <a href="#toolchain"><span>02</span> ./toolchain</a>
                <a href="#session"><span>03</span> ./session</a>
              </nav>
            </aside>

            <article
              ref="aboutRef"
              class="about-content markdown-body"
              v-html="aboutContent"
            />
          </div>
        </div>
      </section>

      <section id="toolchain" class="toolchain-section">
        <div class="section-heading">
          <span class="section-heading__index">02</span>
          <div>
            <p class="section-heading__eyebrow">toolchain.json</p>
            <h2>习惯用的工具</h2>
          </div>
        </div>
        <div class="toolchain-list">
          <div v-for="item in toolchain" :key="item.name" class="toolchain-item">
            <span class="toolchain-item__icon" aria-hidden="true">
              <i :class="item.icon" />
            </span>
            <span class="toolchain-item__name">{{ item.name }}</span>
            <span class="toolchain-item__type">{{ item.type }}</span>
          </div>
        </div>
      </section>

      <section id="session" class="session-line" aria-label="终端状态">
        <span class="terminal-prompt">{{ prompt }}</span>
        <span>echo "thanks for stopping by"</span>
        <span class="session-line__result">&gt; 0</span>
        <span class="session-line__cursor" aria-hidden="true" />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getAbout } from '@/api/site'
import markdownToHtml from '@/utils/markdown'

const blogInfoStore = useBlogInfoStore()

const aboutContent = ref('')
const aboutRef = ref<HTMLElement | null>(null)
const imgList = ref<string[]>([])
const imageHandlers = new Map<HTMLImageElement, EventListener>()

const author = computed(() => {
  return blogInfoStore.blogInfo.websiteConfig.websiteAuthor || 'renzs'
})

const prompt = computed(() => `${author.value.toLowerCase().replace(/\s+/g, '-') || 'renzs'}@blog:~$`)

const avatar = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig?.websiteAvatar || ''
})

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const aboutPage = pageList.find(item => item.pageLabel === 'about')
  const coverUrl = aboutPage?.pageCover || ''
  if (coverUrl) {
    return `background-image: url(${coverUrl});`
  }
  return 'background-color: #15191d;'
})

const toolchain = [
  { name: 'Vue 3', type: 'frontend', icon: 'mdi mdi-vuejs' },
  { name: 'Spring Boot', type: 'backend', icon: 'mdi mdi-leaf' },
  { name: 'MySQL', type: 'data', icon: 'mdi mdi-database-outline' },
  { name: 'Docker', type: 'ship', icon: 'mdi mdi-docker' }
]

async function getAboutContent() {
  try {
    const { data } = await getAbout()
    aboutContent.value = markdownToHtml(data.data || '')

    await nextTick()

    if (aboutRef.value) {
      const imgs = aboutRef.value.getElementsByTagName('img')
      for (let i = 0; i < imgs.length; i++) {
        const image = imgs[i]
        imgList.value.push(image.src)
        image.style.cursor = 'pointer'
        const handler: EventListener = () => previewImg(image.src)
        imageHandlers.set(image, handler)
        image.addEventListener('click', handler)
      }
    }
  } catch (error) {
    console.error('获取关于内容失败:', error)
  }
}

function previewImg(src: string) {
  if (imgList.value.includes(src)) {
    window.open(src, '_blank')
  }
}

onMounted(() => {
  getAboutContent()
})

onUnmounted(() => {
  imageHandlers.forEach((handler, image) => image.removeEventListener('click', handler))
  imageHandlers.clear()
})
</script>

<style scoped>
.about-page {
  --terminal-ink: #111416;
  --terminal-panel: #191e21;
  --terminal-panel-soft: #20272a;
  --terminal-line: rgba(196, 214, 207, 0.16);
  --terminal-text: #d8e3de;
  --terminal-muted: #8a9a94;
  --terminal-green: #9de28f;
  --terminal-cyan: #79d5d1;
  --terminal-amber: #efbf76;
  background: #101416;
  color: var(--terminal-text);
}

:global(.about-overlay-main) {
  padding-top: 0 !important;
}

.about-hero {
  position: relative;
  min-height: 430px;
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  isolation: isolate;
}

.about-hero__scrim {
  position: absolute;
  inset: 0;
  z-index: -1;
  background: rgba(7, 12, 14, 0.72);
}

.about-hero__inner {
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
  padding: 150px 0 70px;
}

.terminal-path,
.terminal-command,
.session-line,
.section-heading__eyebrow,
.profile-label,
.profile-nav,
.toolchain-item__type,
.terminal-window__title,
.terminal-window__path {
  font-family: 'SFMono-Regular', 'Cascadia Code', 'Roboto Mono', Consolas, monospace;
}

.terminal-path {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--terminal-green);
  font-size: 0.85rem;
  letter-spacing: 0.08em;
}

.terminal-path__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--terminal-green);
  box-shadow: 0 0 16px rgba(157, 226, 143, 0.9);
}

.about-hero h1 {
  margin: 18px 0 8px;
  color: #f1f6f2;
  font-family: 'SFMono-Regular', 'Cascadia Code', Consolas, monospace;
  font-size: clamp(2.4rem, 6vw, 5rem);
  font-weight: 700;
  letter-spacing: 0;
}

.hero-prompt {
  color: var(--terminal-green);
}

.about-hero__name {
  margin: 0;
  color: var(--terminal-cyan);
  font-family: 'SFMono-Regular', 'Cascadia Code', Consolas, monospace;
  font-size: clamp(1.25rem, 2.8vw, 2rem);
}

.about-hero__tagline {
  margin: 18px 0 0;
  color: rgba(241, 246, 242, 0.72);
  font-size: 0.95rem;
  letter-spacing: 0.06em;
}

.about-hero__cursor {
  position: absolute;
  right: max(24px, calc((100% - 1120px) / 2));
  bottom: 38px;
  color: var(--terminal-green);
  font-family: Consolas, monospace;
  font-size: 1.5rem;
  animation: cursor-blink 1s steps(2, start) infinite;
}

.about-main {
  width: min(1120px, calc(100% - 48px));
  margin: -56px auto 0;
  padding-bottom: 72px;
  position: relative;
  z-index: 1;
}

.terminal-window {
  overflow: hidden;
  border: 1px solid rgba(157, 226, 143, 0.24);
  border-radius: 8px;
  background: var(--terminal-panel);
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.3);
}

.terminal-window__bar {
  display: flex;
  align-items: center;
  min-height: 44px;
  padding: 0 18px;
  border-bottom: 1px solid var(--terminal-line);
  background: var(--terminal-panel-soft);
}

.terminal-controls {
  display: flex;
  gap: 7px;
  width: 104px;
}

.terminal-controls__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.terminal-controls__dot--red { background: #e86e62; }
.terminal-controls__dot--amber { background: #e6b86f; }
.terminal-controls__dot--green { background: #79c98b; }

.terminal-window__title {
  flex: 1;
  color: var(--terminal-text);
  font-size: 0.78rem;
  text-align: center;
}

.terminal-window__path {
  width: 104px;
  color: var(--terminal-muted);
  font-size: 0.7rem;
  text-align: right;
}

.terminal-window__body {
  padding: 26px clamp(18px, 4vw, 42px) 40px;
}

.terminal-command {
  display: flex;
  gap: 10px;
  margin-bottom: 32px;
  color: var(--terminal-text);
  font-size: 0.86rem;
}

.terminal-prompt {
  color: var(--terminal-green);
}

.profile-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: clamp(28px, 5vw, 66px);
}

.profile-sidebar {
  padding-right: 26px;
  border-right: 1px solid var(--terminal-line);
}

.avatar-frame {
  display: inline-flex;
  padding: 5px;
  border: 1px solid rgba(121, 213, 209, 0.5);
  border-radius: 8px;
  background: #101416;
}

.avatar-frame :deep(.v-avatar) {
  border-radius: 4px;
}

.author-avatar {
  filter: saturate(0.8) contrast(1.05);
  transition: filter 180ms ease, transform 180ms ease;
}

.author-avatar:hover {
  filter: saturate(1.1) contrast(1.08);
  transform: scale(1.04);
}

.profile-id {
  display: grid;
  gap: 5px;
  margin-top: 18px;
}

.profile-label {
  color: var(--terminal-muted);
  font-size: 0.68rem;
  text-transform: uppercase;
}

.profile-id strong {
  color: #f1f6f2;
  font-size: 1.1rem;
  font-weight: 600;
}

.profile-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 15px;
  color: var(--terminal-muted);
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 0.7rem;
}

.profile-status__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--terminal-green);
  box-shadow: 0 0 10px rgba(157, 226, 143, 0.78);
}

.profile-nav {
  display: grid;
  gap: 10px;
  margin-top: 36px;
}

.profile-nav a {
  display: flex;
  gap: 12px;
  color: var(--terminal-muted);
  font-size: 0.78rem;
  text-decoration: none;
  transition: color 160ms ease, transform 160ms ease;
}

.profile-nav a span {
  color: var(--terminal-amber);
}

.profile-nav a:hover {
  color: var(--terminal-cyan);
  transform: translateX(4px);
}

.about-content {
  min-width: 0;
  color: var(--terminal-text);
  font-size: 1rem;
  line-height: 1.9;
}

.about-content :deep(*) {
  max-width: 100%;
}

.about-content :deep(p),
.about-content :deep(ul),
.about-content :deep(ol),
.about-content :deep(blockquote) {
  position: relative;
  margin: 0 0 16px;
  padding-left: 38px;
}

.about-content :deep(p::before),
.about-content :deep(ul::before),
.about-content :deep(ol::before),
.about-content :deep(blockquote::before) {
  position: absolute;
  left: 0;
  color: rgba(121, 213, 209, 0.36);
  content: '//';
  font-family: Consolas, monospace;
}

.about-content :deep(h1),
.about-content :deep(h2),
.about-content :deep(h3) {
  margin: 0 0 14px;
  color: #f1f6f2;
  font-family: 'SFMono-Regular', 'Cascadia Code', Consolas, monospace;
  font-weight: 600;
  letter-spacing: 0;
}

.about-content :deep(a) {
  color: var(--terminal-cyan);
}

.about-content :deep(code) {
  padding: 2px 5px;
  border: 1px solid var(--terminal-line);
  border-radius: 3px;
  color: var(--terminal-amber);
  background: rgba(239, 191, 118, 0.08);
  font-family: Consolas, monospace;
}

.about-content :deep(img) {
  max-width: 100%;
  border: 1px solid var(--terminal-line);
  border-radius: 4px;
}

.toolchain-section {
  padding: 58px 0 22px;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 24px;
}

.section-heading__index {
  padding-top: 3px;
  color: var(--terminal-amber);
  font-family: Consolas, monospace;
  font-size: 0.85rem;
}

.section-heading__eyebrow {
  margin: 0 0 6px;
  color: var(--terminal-green);
  font-size: 0.76rem;
}

.section-heading h2 {
  margin: 0;
  color: #f1f6f2;
  font-size: 1.65rem;
  font-weight: 600;
}

.toolchain-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border-top: 1px solid var(--terminal-line);
  border-bottom: 1px solid var(--terminal-line);
}

.toolchain-item {
  display: grid;
  grid-template-columns: 34px 1fr;
  grid-template-rows: auto auto;
  gap: 2px 12px;
  padding: 18px 16px;
  border-right: 1px solid var(--terminal-line);
}

.toolchain-item:last-child {
  border-right: 0;
}

.toolchain-item__icon {
  grid-row: 1 / 3;
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 1px solid rgba(121, 213, 209, 0.3);
  border-radius: 4px;
  color: var(--terminal-cyan);
}

.toolchain-item__name {
  color: var(--terminal-text);
  font-size: 0.92rem;
}

.toolchain-item__type {
  color: var(--terminal-muted);
  font-size: 0.66rem;
  text-transform: uppercase;
}

.session-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 28px 0 0;
  color: var(--terminal-muted);
  font-size: 0.8rem;
}

.session-line__result {
  color: var(--terminal-green);
}

.session-line__cursor {
  width: 8px;
  height: 17px;
  background: var(--terminal-green);
  animation: cursor-blink 1s steps(2, start) infinite;
}

@keyframes cursor-blink {
  50% { opacity: 0; }
}

@media (max-width: 759px) {
  .about-hero {
    min-height: 360px;
  }

  .about-hero__inner,
  .about-main {
    width: min(100% - 28px, 1120px);
  }

  .about-hero__inner {
    padding: 120px 0 58px;
  }

  .about-hero__tagline {
    max-width: 250px;
    line-height: 1.6;
  }

  .about-main {
    margin-top: -36px;
    padding-bottom: 44px;
  }

  .terminal-window__bar {
    padding: 0 12px;
  }

  .terminal-controls,
  .terminal-window__path {
    width: 72px;
  }

  .terminal-window__path {
    font-size: 0;
  }

  .terminal-window__path::after {
    color: var(--terminal-muted);
    content: '●';
    font-size: 0.65rem;
  }

  .profile-layout {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .profile-sidebar {
    display: grid;
    grid-template-columns: auto 1fr;
    column-gap: 16px;
    align-items: center;
    padding: 0 0 24px;
    border-right: 0;
    border-bottom: 1px solid var(--terminal-line);
  }

  .profile-id,
  .profile-status {
    margin-top: 0;
  }

  .profile-nav {
    grid-column: 1 / -1;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-top: 22px;
  }

  .profile-nav a {
    display: block;
    font-size: 0.7rem;
  }

  .profile-nav a span {
    display: block;
    margin-bottom: 4px;
  }

  .toolchain-section {
    padding-top: 42px;
  }

  .toolchain-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolchain-item:nth-child(2) {
    border-right: 0;
  }

  .toolchain-item:nth-child(-n + 2) {
    border-bottom: 1px solid var(--terminal-line);
  }
}
</style>
