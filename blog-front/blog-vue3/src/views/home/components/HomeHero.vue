<template>
  <div class="home-banner home-hero" :style="cover">
    <div class="home-hero-overlay" />
    <div class="banner-container home-hero-content">
      <h1 class="home-hero-title animated zoomIn">
        {{ websiteConfig.websiteName }}
      </h1>
      <div class="home-hero-intro" @click="emit('refresh-poem')">
        <Printer :printerInfo="printerInfo">
          <template #default="{ content }">
            <h4>{{ content }}<span class="cursor">|</span></h4>
          </template>
        </Printer>
      </div>
      <div v-if="hasSocialLinks" class="home-hero-contact">
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
    </div>
    <div class="scroll-down home-scroll-down" @click="emit('scroll-down')">
      <v-icon color="#fff" class="scroll-down-effects">
        mdi-chevron-down
      </v-icon>
    </div>
    <div class="home-hero-wave" aria-hidden="true">
      <!-- 与 love 页面保持一致的双层流动波浪，用于统一顶部图和内容区的衔接。 -->
      <svg
        class="home-hero-wave__layer home-hero-wave__layer--back"
        viewBox="0 0 2880 120"
        preserveAspectRatio="none"
      >
        <path
          d="M0,54 C180,92 300,20 480,56 C660,92 780,28 960,58 C1140,90 1260,28 1440,54 C1620,92 1740,20 1920,56 C2100,92 2220,28 2400,58 C2580,90 2700,28 2880,54 L2880,120 L0,120 Z"
        />
      </svg>
      <svg
        class="home-hero-wave__layer home-hero-wave__layer--front"
        viewBox="0 0 2880 120"
        preserveAspectRatio="none"
      >
        <path
          d="M0,64 C180,112 300,20 480,62 C660,104 780,18 960,56 C1140,94 1260,24 1440,64 C1620,112 1740,20 1920,62 C2100,104 2220,18 2400,56 C2580,94 2700,24 2880,64 L2880,120 L0,120 Z"
        />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Printer from '@/components/Printer.vue'

interface WebsiteConfig {
  websiteName: string
  websiteAvatar: string
  websiteIntro: string
  qq?: string
  github?: string
  gitee?: string
  socialUrlList?: string[]
  [key: string]: any
}

interface Props {
  cover: string
  printerInfo: string
  websiteConfig: WebsiteConfig
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'refresh-poem': []
  'scroll-down': []
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
.home-banner {
  position: absolute;
  top: -60px;
  left: 0;
  right: 0;
  height: 100vh;
  text-align: center;
  color: #fff !important;
}

.home-hero {
  overflow: hidden;
  background-attachment: fixed;
  animation: header-effect 1s;
}

.home-hero-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      circle at top,
      rgba(92, 162, 255, 0.18),
      transparent 42%
    ),
    linear-gradient(
      180deg,
      rgba(6, 13, 28, 0.18) 0%,
      rgba(6, 13, 28, 0.42) 48%,
      rgba(6, 13, 28, 0.74) 100%
    );
}

.banner-container {
  margin-top: 35vh;
  line-height: 1.5;
  color: #eee;
}

.home-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22px;
}

.home-hero-title {
  max-width: 880px;
  margin-bottom: 0;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.28);
}

.home-hero-intro {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  max-width: min(720px, calc(100vw - 48px));
  margin-top: 0;
  padding: 14px 22px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
  box-shadow: 0 20px 45px rgba(5, 13, 30, 0.18);
  color: #fff !important;
  cursor: pointer;
}

.home-hero-intro h4 {
  margin: 0;
}

.cursor {
  margin-left: 5px;
  animation: blink 0.7s infinite;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0; }
  100% { opacity: 1; }
}

.home-hero-contact {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 10px 18px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
  box-shadow: 0 18px 40px rgba(5, 13, 30, 0.14);
}

.home-hero-contact a {
  color: #fff !important;
  font-size: 1.35rem;
  transition: transform 0.25s ease, opacity 0.25s ease, text-shadow 0.25s ease;
}

.home-hero-contact a:hover {
  transform: translateY(-2px);
  opacity: 0.92;
  text-shadow: 0 0 18px rgba(255, 255, 255, 0.35);
}

.scroll-down {
  cursor: pointer;
  position: absolute;
  bottom: 150px;
  width: 100%;
}

.home-scroll-down {
  z-index: 1;
}

.scroll-down i {
  font-size: 2rem;
}

.scroll-down-effects {
  color: #eee !important;
  text-align: center;
  text-shadow: 0.1rem 0.1rem 0.2rem rgba(0, 0, 0, 0.15);
  line-height: 1.5;
  display: inline-block;
  text-rendering: auto;
  -webkit-font-smoothing: antialiased;
  animation: scroll-down-effect 1.5s infinite;
}

@keyframes scroll-down-effect {
  0% {
    top: 0;
    opacity: 0.4;
  }
  50% {
    top: -16px;
    opacity: 1;
  }
  100% {
    top: 0;
    opacity: 0.4;
  }
}

.home-hero-wave {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  z-index: 1;
  height: 112px;
  color: #fff;
  overflow: hidden;
  pointer-events: none;
}

.home-hero-wave__layer {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 200%;
  height: 100%;
  display: block;
  animation: homeHeroWaveMove 16s linear infinite;
}

.home-hero-wave__layer path {
  fill: currentColor;
}

.home-hero-wave__layer--back {
  bottom: 12px;
  height: 86px;
  opacity: 0.48;
  animation-duration: 24s;
  animation-direction: reverse;
}

.home-hero-wave__layer--front {
  opacity: 0.96;
}

@keyframes homeHeroWaveMove {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-50%, 0, 0);
  }
}

@media (min-width: 760px) {
  .home-hero-title {
    font-size: 2.5rem;
  }
  .home-hero-intro {
    font-size: 1.5rem;
    padding: 16px 26px;
  }
}

@media (max-width: 759px) {
  .home-hero-title {
    font-size: 26px;
  }
  .home-hero-contact {
    font-size: 1.25rem;
    line-height: 2;
  }
  .banner-container {
    margin-top: 30vh;
    padding: 0 14px;
  }
  .home-hero-content {
    gap: 14px;
  }
  .home-hero-intro {
    max-width: calc(100vw - 28px);
    padding: 10px 14px;
    border-radius: 16px;
  }
  .home-hero-contact {
    gap: 12px;
    padding: 8px 14px;
  }
  .home-scroll-down {
    bottom: 110px;
  }
  .home-hero-wave {
    height: 78px;
  }
}
</style>
