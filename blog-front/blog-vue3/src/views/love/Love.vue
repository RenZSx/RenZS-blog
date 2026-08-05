<template>
  <div class="love-page" :class="{ 'love-page--dark': isDark }">
    <section class="love-hero" :style="heroStyle">
      <div
        v-for="petal in petals"
        :key="petal"
        class="love-petal"
        :class="`love-petal--${petal}`"
      />

      <div class="love-couple-card">
        <div class="love-person">
          <div class="love-avatar">
            <img :src="defaultLovers[0].avatar" :alt="defaultLovers[0].name" />
          </div>
          <div class="love-name">{{ defaultLovers[0].name }}</div>
        </div>

        <div class="love-heart">
          <span>♥</span>
        </div>

        <div class="love-person">
          <div class="love-avatar">
            <img :src="defaultLovers[1].avatar" :alt="defaultLovers[1].name" />
          </div>
          <div class="love-name">{{ defaultLovers[1].name }}</div>
        </div>
      </div>

      <div class="love-wave" aria-hidden="true">
        <!-- 两层波浪错速移动，模拟背景图与内容区之间的流动分界。 -->
        <svg
          class="love-wave__layer love-wave__layer--back"
          viewBox="0 0 2880 120"
          preserveAspectRatio="none"
        >
          <path
            d="M0,54 C180,92 300,20 480,56 C660,92 780,28 960,58 C1140,90 1260,28 1440,54 C1620,92 1740,20 1920,56 C2100,92 2220,28 2400,58 C2580,90 2700,28 2880,54 L2880,120 L0,120 Z"
          />
        </svg>
        <svg
          class="love-wave__layer love-wave__layer--front"
          viewBox="0 0 2880 120"
          preserveAspectRatio="none"
        >
          <path
            d="M0,64 C180,112 300,20 480,62 C660,104 780,18 960,56 C1140,94 1260,24 1440,64 C1620,112 1740,20 1920,62 C2100,104 2220,18 2400,56 C2580,94 2700,24 2880,64 L2880,120 L0,120 Z"
          />
        </svg>
      </div>
    </section>

    <section class="love-content">
      <div class="love-lines" aria-hidden="true" />

      <div class="love-time">
        <h2>{{ loveConfig.title || '这是我们一起走过的' }}</h2>
        <div class="love-duration">
          <span class="love-prefix">第</span>
          <strong>{{ elapsedParts.years }}</strong>
          <span>年</span>
          <strong>{{ elapsedParts.months }}</strong>
          <span>月</span>
          <strong>{{ elapsedParts.days }}</strong>
          <span>日</span>
          <strong>{{ elapsedParts.hours }}</strong>
          <span>时</span>
          <strong>{{ elapsedParts.minutes }}</strong>
          <span>分</span>
          <strong>{{ elapsedParts.seconds }}</strong>
          <span>秒</span>
        </div>
        <p class="love-countdown">
          {{ loveConfig.anniversaryTitle || '纪念日倒计时' }}：{{ countdownText }}
        </p>
      </div>

      <FlyingLetterCard
        class="love-letter-card"
        :label="loveConfig.subtitle || '飞车传信'"
        :departing="letterDeparting"
        @activate="openLetter"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useTheme } from 'vuetify'
import { useRouter } from 'vue-router'
import { getLoveConfig } from '@/api/love'
import FlyingLetterCard from './FlyingLetterCard.vue'

interface LoveConfig {
  title: string
  subtitle: string
  background: string
  startTime: string
  anniversaryTime: string
  anniversaryTitle: string
  isEnabled: number
}


const theme = useTheme()
const router = useRouter()
const isDark = computed(() => theme.global.current.value.dark)
const petals = [1, 2, 3, 4, 5, 6, 7, 8]
const defaultLovers = [
  {
    name: 'RenZS',
    avatar:
      'https://static.renzs.top/love/cat.jpg'
  },
  {
    name: 'Love',
    avatar:
      'https://static.renzs.top/love/mm.jpg'
  }
]

const loveConfig = ref<LoveConfig>({
  title: '',
  subtitle: '',
  background: '',
  startTime: '',
  anniversaryTime: '',
  anniversaryTitle: '',
  isEnabled: 1
})

const elapsedParts = ref({
  years: 0,
  months: 0,
  days: 0,
  hours: 0,
  minutes: 0,
  seconds: 0
})
const countdownText = ref('0天0时0分0秒')
const timer = ref<ReturnType<typeof setInterval> | null>(null)
const navigationTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const letterDeparting = ref(false)

const heroStyle = computed(() => {
  const background = loveConfig.value.background
  return background
    ? { backgroundImage: `linear-gradient(rgba(0, 71, 84, 0.2), rgba(0, 71, 84, 0.16)), url(${background})` }
    : {}
})

function formatDurationText(ms: number) {
  const safeMs = Math.max(ms, 0)
  const totalSeconds = Math.floor(safeMs / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  return `${days}天${hours}时${minutes}分${seconds}秒`
}

function getElapsedParts(startTime: dayjs.Dayjs) {
  const now = dayjs()
  const safeStart = startTime.isAfter(now) ? now : startTime
  const years = now.diff(safeStart, 'year')
  const afterYears = safeStart.add(years, 'year')
  const months = now.diff(afterYears, 'month')
  const afterMonths = afterYears.add(months, 'month')
  const days = now.diff(afterMonths, 'day')
  const afterDays = afterMonths.add(days, 'day')
  const hours = now.diff(afterDays, 'hour')
  const afterHours = afterDays.add(hours, 'hour')
  const minutes = now.diff(afterHours, 'minute')
  const afterMinutes = afterHours.add(minutes, 'minute')
  const seconds = now.diff(afterMinutes, 'second')

  return { years, months, days, hours, minutes, seconds }
}

function updateTimers() {
  const startTime = loveConfig.value.startTime ? dayjs(loveConfig.value.startTime) : null
  const anniversaryTime = loveConfig.value.anniversaryTime ? dayjs(loveConfig.value.anniversaryTime) : null

  if (startTime) {
    elapsedParts.value = getElapsedParts(startTime)
  }

  if (anniversaryTime) {
    countdownText.value = formatDurationText(anniversaryTime.valueOf() - Date.now())
  }
}

async function loadLoveConfig() {
  const { data } = await getLoveConfig()
  loveConfig.value = data?.data || loveConfig.value
  // 配置加载后立刻刷新计时，避免首屏停留在默认值。
  updateTimers()
}

function openLetter() {
  if (letterDeparting.value) {
    return
  }
  sessionStorage.setItem('letter-arrival-source', 'love')
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    router.push('/letter')
    return
  }
  letterDeparting.value = true
  navigationTimer.value = setTimeout(() => {
    router.push('/letter')
  }, 560)
}


onMounted(async () => {
  await loadLoveConfig()
  timer.value = setInterval(updateTimers, 1000)
})

onUnmounted(() => {
  if (timer.value) {
    // 离开页面时清理定时器，防止路由切换后继续计算。
    clearInterval(timer.value)
    timer.value = null
  }
  if (navigationTimer.value) {
    clearTimeout(navigationTimer.value)
    navigationTimer.value = null
  }
})
</script>

<style scoped>
.love-page {
  position: relative;
  min-height: 100vh;
  background: #fff;
}

.love-page--dark {
  background: #121820;
}

.love-hero {
  position: relative;
  min-height: 620px;
  margin-top: -60px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 170px 20px 120px;
  background:
    linear-gradient(135deg, #037990 0%, #0b8aa1 48%, #08748a 100%);
  background-position: center;
  background-size: cover;
}

.love-hero::before {
  position: absolute;
  inset: 0;
  background: rgba(0, 83, 100, 0.16);
  content: '';
}

.love-couple-card {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 1fr 150px 1fr;
  align-items: center;
  width: min(950px, 100%);
  min-height: 310px;
  padding: 48px 86px 34px;
  border-radius: 48px;
  background: rgba(255, 255, 255, 0.15);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(1px);
}

.love-person {
  display: grid;
  justify-items: center;
  gap: 16px;
}

.love-avatar {
  width: 178px;
  height: 178px;
  padding: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 42px rgba(0, 48, 60, 0.2);
}

.love-avatar img {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: inherit;
  object-fit: cover;
}

.love-name {
  color: #fff;
  font-size: 28px;
  font-weight: 800;
  text-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
}

.love-heart {
  display: grid;
  place-items: center;
  color: #ff3b66;
  font-size: 92px;
  line-height: 1;
  text-shadow: 0 18px 30px rgba(106, 0, 34, 0.18);
  animation: heartBeat 1.8s ease-in-out infinite;
}

.love-heart span {
  display: block;
}

.love-wave {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  z-index: 3;
  height: 112px;
  color: #fff;
  overflow: hidden;
  pointer-events: none;
}

.love-wave__layer {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 200%;
  height: 100%;
  display: block;
  animation: loveWaveMove 16s linear infinite;
}

.love-wave__layer path {
  fill: currentColor;
}

.love-wave__layer--back {
  bottom: 12px;
  height: 86px;
  opacity: 0.48;
  animation-duration: 24s;
  animation-direction: reverse;
}

.love-wave__layer--front {
  opacity: 0.96;
}

.love-content {
  position: relative;
  min-height: 420px;
  margin-top: -1px;
  padding: 34px 20px 72px;
  background: #fff;
  text-align: center;
}

.love-lines {
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    90deg,
    rgba(32, 105, 139, 0.12) 0,
    rgba(32, 105, 139, 0.12) 1px,
    transparent 1px,
    transparent 48px
  );
  pointer-events: none;
}

.love-time {
  position: relative;
  z-index: 1;
  margin: 0 auto;
}

.love-time h2 {
  margin: 0 0 14px;
  color: #ffa000;
  font-size: clamp(24px, 3.2vw, 34px);
  font-weight: 900;
  letter-spacing: 0.08em;
}

.love-duration {
  display: flex;
  align-items: baseline;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #050505;
  font-family: var(--font-family);
  font-size: clamp(18px, 2.6vw, 28px);
  font-weight: 800;
}

.love-duration strong {
  color: #000;
  font-family: inherit;
  font-size: clamp(48px, 6vw, 70px);
  font-weight: 800;
  line-height: 1.1;
}

.love-prefix {
  margin-right: -2px;
}

.love-countdown {
  margin: 20px 0 0;
  color: #050505;
  font-size: clamp(18px, 2vw, 26px);
  font-weight: 900;
}

.love-letter-card {
  position: relative;
  z-index: 1;
  margin: 48px auto 0;
}

.love-petal {
  position: absolute;
  z-index: 4;
  width: 14px;
  height: 8px;
  border-radius: 70% 30% 70% 30%;
  background: #ffb3cb;
  opacity: 0.86;
  animation: petalFloat 9s linear infinite;
}

.love-petal--1 {
  top: 9%;
  left: 16%;
  animation-delay: -1s;
}

.love-petal--2 {
  top: 22%;
  left: 44%;
  animation-delay: -3s;
}

.love-petal--3 {
  top: 38%;
  left: 7%;
  animation-delay: -5s;
}

.love-petal--4 {
  top: 55%;
  left: 61%;
  animation-delay: -2s;
}

.love-petal--5 {
  top: 28%;
  right: 10%;
  animation-delay: -6s;
}

.love-petal--6 {
  top: 78%;
  left: 22%;
  animation-delay: -4s;
}

.love-petal--7 {
  top: 70%;
  right: 18%;
  animation-delay: -7s;
}

.love-petal--8 {
  top: 48%;
  right: 4%;
  animation-delay: -8s;
}

.love-page--dark .love-wave {
  color: #121820;
}

.love-page--dark .love-content {
  background: #121820;
}

.love-page--dark .love-lines {
  background-image: repeating-linear-gradient(
    90deg,
    rgba(156, 205, 229, 0.12) 0,
    rgba(156, 205, 229, 0.12) 1px,
    transparent 1px,
    transparent 48px
  );
}

.love-page--dark .love-duration,
.love-page--dark .love-duration strong,
.love-page--dark .love-countdown {
  color: rgba(245, 248, 255, 0.94);
}

@keyframes heartBeat {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes petalFloat {
  0% {
    transform: translate3d(0, -12px, 0) rotate(0deg);
  }
  50% {
    transform: translate3d(22px, 34px, 0) rotate(120deg);
  }
  100% {
    transform: translate3d(-16px, 82px, 0) rotate(260deg);
  }
}

@keyframes loveWaveMove {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-50%, 0, 0);
  }
}

@media (max-width: 768px) {
  .love-hero {
    min-height: 520px;
    padding: 152px 14px 100px;
  }

  .love-content {
    padding-top: 28px;
  }

  .love-couple-card {
    grid-template-columns: 1fr 74px 1fr;
    min-height: 220px;
    padding: 28px 18px 24px;
    border-radius: 32px;
  }

  .love-avatar {
    width: 104px;
    height: 104px;
  }

  .love-name {
    font-size: 20px;
  }

  .love-heart {
    font-size: 54px;
  }

  .love-duration {
    gap: 5px;
  }

  .love-duration strong {
    font-size: clamp(38px, 12vw, 52px);
  }

  .love-letter-card {
    margin-top: 36px;
  }
}
</style>
