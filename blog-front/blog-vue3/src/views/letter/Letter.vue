<template>
  <div class="letter-page" :class="{ 'letter-page--dark': isDark }">
    <div class="letter-sky-lines" aria-hidden="true" />
    <div class="letter-stars" aria-hidden="true">
      <i v-for="star in 9" :key="star" :class="`letter-star letter-star--${star}`" />
    </div>

    <Transition name="letter-delivery">
      <section
        v-if="presentationPhase !== 'reading'"
        class="letter-delivery"
        :class="{
          'letter-delivery--opening': presentationPhase === 'opening',
          'letter-delivery--direct': !arrivedFromLove
        }"
        aria-label="信件正在送达"
      >
        <div class="letter-flight-path" aria-hidden="true" />
        <div class="letter-envelope" aria-hidden="true">
          <div class="letter-envelope__back" />
          <div class="letter-envelope__paper-preview">
            <span />
            <span />
            <span />
          </div>
          <div class="letter-envelope__pocket" />
          <div class="letter-envelope__flap" />
          <div class="letter-envelope__seal">♥</div>
        </div>
      </section>
    </Transition>

    <Transition name="letter-paper">
      <main v-if="presentationPhase === 'reading'" class="letter-shell">
        <button class="letter-back" type="button" title="返回纪念页" aria-label="返回纪念页" @click="router.push('/love')">
          <v-icon size="22">mdi-arrow-left</v-icon>
        </button>

        <article class="letter-paper">
          <div class="letter-paper__edge" aria-hidden="true" />

          <div v-if="state.status === 'loading' || state.status === 'idle'" class="letter-state">
            <div class="letter-ink-loading" aria-hidden="true">
              <span />
              <span />
              <span />
            </div>
            <p>信件正在显现...</p>
          </div>

          <div v-else-if="state.status === 'error'" class="letter-state letter-state--error">
            <v-icon size="36">mdi-email-alert-outline</v-icon>
            <p>{{ state.errorMessage }}</p>
            <button class="letter-action" type="button" @click="loadLetter">
              <v-icon size="18">mdi-refresh</v-icon>
              <span>重新获取</span>
            </button>
          </div>

          <div v-else-if="state.status === 'empty'" class="letter-state">
            <v-icon size="38">mdi-email-open-outline</v-icon>
            <p>这封信还没有内容。</p>
          </div>

          <div v-else class="letter-content">
            <header class="letter-content__header">
              <div class="letter-stamp" aria-hidden="true">FOR YOU</div>
              <div>
                <div class="letter-kicker">Love Letter</div>
                <h1>{{ state.letter?.letterTitle || '未命名的信' }}</h1>
              </div>
            </header>
            <div class="letter-rule" aria-hidden="true">
              <span>♥</span>
            </div>
            <div class="letter-body markdown-body" v-html="letterContentHtml" />
          </div>
        </article>
      </main>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from 'vuetify'
import { getLoveLetter } from '@/api/love'
import markdownToHtml from '@/utils/markdown'
import {
  createInitialLetterState,
  reduceLetterState,
  type LetterPayload
} from './letterState'
import { resolveLetterPresentationTiming } from './letterMotion'

type PresentationPhase = 'arriving' | 'opening' | 'reading'

const router = useRouter()
const theme = useTheme()
const isDark = computed(() => theme.global.current.value.dark)
const state = ref(createInitialLetterState())
const presentationPhase = ref<PresentationPhase>('arriving')
const arrivedFromLove = ref(false)
const presentationTimers: Array<ReturnType<typeof setTimeout>> = []

const letterContentHtml = computed(() => {
  const content = state.value.letter?.letterContent || ''
  return markdownToHtml(content)
})

async function loadLetter() {
  state.value = reduceLetterState(state.value, { type: 'load' })

  try {
    const { data } = await getLoveLetter()
    const payload = (data?.data || data || null) as LetterPayload | null
    state.value = reduceLetterState(state.value, {
      type: 'resolve',
      payload
    })
  } catch (error) {
    console.error('获取信件失败:', error)
    state.value = reduceLetterState(state.value, { type: 'reject' })
  }
}

function schedulePresentation() {
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  arrivedFromLove.value = sessionStorage.getItem('letter-arrival-source') === 'love'
  sessionStorage.removeItem('letter-arrival-source')
  const timing = resolveLetterPresentationTiming(reducedMotion, arrivedFromLove.value)

  if (timing.readingDelay === 0) {
    presentationPhase.value = 'reading'
    return
  }

  presentationTimers.push(setTimeout(() => {
    presentationPhase.value = 'opening'
  }, timing.openingDelay))
  presentationTimers.push(setTimeout(() => {
    presentationPhase.value = 'reading'
  }, timing.readingDelay))
}

onMounted(() => {
  loadLetter()
  schedulePresentation()
})

onUnmounted(() => {
  presentationTimers.forEach(timer => clearTimeout(timer))
})
</script>

<style scoped>
.letter-page {
  --letter-sky: #dceef2;
  --letter-sky-line: rgba(42, 113, 136, 0.16);
  --letter-paper: #fffaf0;
  --letter-paper-line: rgba(153, 105, 65, 0.1);
  --letter-ink: #3f2c24;
  --letter-muted: #775f52;
  --letter-accent: #b94a4a;
  position: relative;
  min-height: 100vh;
  margin-top: -60px;
  overflow: hidden;
  padding-top: 60px;
  background: var(--letter-sky);
  color: var(--letter-ink);
}

.letter-page--dark {
  --letter-sky: #101b25;
  --letter-sky-line: rgba(131, 190, 207, 0.12);
  --letter-paper: #f4ead8;
  --letter-paper-line: rgba(117, 76, 47, 0.12);
  --letter-ink: #39271f;
  --letter-muted: #6f594d;
}

.letter-sky-lines {
  position: fixed;
  inset: 0;
  background-image: repeating-linear-gradient(
    125deg,
    transparent 0,
    transparent 76px,
    var(--letter-sky-line) 77px,
    transparent 78px
  );
  pointer-events: none;
}

.letter-stars {
  position: fixed;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.letter-star {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.12);
  animation: letter-star-twinkle 3.8s ease-in-out infinite;
}

.letter-star--1 { top: 18%; left: 10%; animation-delay: -1s; }
.letter-star--2 { top: 28%; left: 28%; animation-delay: -2.3s; }
.letter-star--3 { top: 14%; right: 20%; animation-delay: -0.5s; }
.letter-star--4 { top: 44%; right: 8%; animation-delay: -3s; }
.letter-star--5 { top: 70%; left: 12%; animation-delay: -1.8s; }
.letter-star--6 { top: 82%; right: 22%; animation-delay: -2.7s; }
.letter-star--7 { top: 59%; left: 33%; animation-delay: -0.2s; }
.letter-star--8 { top: 76%; right: 43%; animation-delay: -3.4s; }
.letter-star--9 { top: 35%; right: 36%; animation-delay: -1.3s; }

.letter-delivery {
  position: relative;
  z-index: 2;
  min-height: calc(100vh - 60px);
  display: grid;
  place-items: center;
  padding: 70px 20px 30px;
  perspective: 1100px;
}

.letter-flight-path {
  position: absolute;
  top: 50%;
  left: 7%;
  width: 48%;
  border-top: 3px dashed rgba(255, 255, 255, 0.55);
  transform: rotate(-8deg);
  transform-origin: right center;
  animation: letter-flight-path 1s linear infinite;
}

.letter-envelope {
  position: relative;
  width: min(390px, 82vw);
  aspect-ratio: 1.58;
  filter: drop-shadow(0 26px 25px rgba(34, 69, 77, 0.22));
  transform-style: preserve-3d;
  animation: letter-envelope-arrive 680ms cubic-bezier(0.18, 0.82, 0.35, 1) both;
}

.letter-delivery--direct .letter-envelope {
  animation-duration: 360ms;
}

.letter-envelope__back,
.letter-envelope__pocket,
.letter-envelope__flap {
  position: absolute;
  inset: 0;
}

.letter-envelope__back {
  border: 1px solid rgba(133, 87, 45, 0.28);
  border-radius: 4px;
  background: #eed7a7;
}

.letter-envelope__paper-preview {
  position: absolute;
  top: 12%;
  right: 8%;
  left: 8%;
  z-index: 2;
  height: 76%;
  display: grid;
  align-content: start;
  gap: 13px;
  padding: 30px 28px;
  border: 1px solid rgba(120, 77, 44, 0.16);
  border-radius: 3px;
  background: #fffaf0;
  transition: transform 720ms cubic-bezier(0.22, 0.72, 0.24, 1), opacity 420ms ease;
}

.letter-envelope__paper-preview span {
  height: 2px;
  display: block;
  background: rgba(119, 86, 61, 0.2);
}

.letter-envelope__paper-preview span:nth-child(2) { width: 78%; }
.letter-envelope__paper-preview span:nth-child(3) { width: 54%; }

.letter-envelope__pocket {
  z-index: 3;
  border-radius: 0 0 4px 4px;
  background: #dfbd79;
  clip-path: polygon(0 18%, 50% 68%, 100% 18%, 100% 100%, 0 100%);
}

.letter-envelope__flap {
  z-index: 4;
  background: #f2d79f;
  clip-path: polygon(0 0, 100% 0, 50% 63%);
  transform-origin: 50% 0;
  transition: transform 680ms cubic-bezier(0.4, 0, 0.2, 1), z-index 0ms 320ms;
}

.letter-envelope__seal {
  position: absolute;
  top: 49%;
  left: 50%;
  z-index: 5;
  width: 48px;
  height: 48px;
  display: grid;
  border: 2px solid rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  place-items: center;
  background: #b94a4a;
  box-shadow: 0 7px 12px rgba(88, 29, 25, 0.22);
  color: #f8dfc1;
  font-size: 19px;
  transform: translate(-50%, -50%);
  transition: opacity 180ms ease, transform 220ms ease;
}

.letter-delivery--opening .letter-envelope__flap {
  z-index: 1;
  transform: rotateX(178deg);
}

.letter-delivery--opening .letter-envelope__seal {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.72) rotate(-12deg);
}

.letter-delivery--opening .letter-envelope__paper-preview {
  opacity: 0.98;
  transform: translateY(-48%);
}

.letter-shell {
  position: relative;
  z-index: 2;
  width: min(900px, calc(100% - 28px));
  margin: 0 auto;
  padding: 108px 0 72px;
}

.letter-back {
  width: 42px;
  height: 42px;
  display: grid;
  margin: 0 0 14px;
  border: 1px solid rgba(42, 95, 111, 0.22);
  border-radius: 50%;
  place-items: center;
  background: rgba(255, 255, 255, 0.62);
  color: #285d6e;
  cursor: pointer;
  transition: background 180ms ease, transform 180ms ease;
}

.letter-back:hover {
  background: #fff;
  transform: translateX(-3px);
}

.letter-back:focus-visible,
.letter-action:focus-visible {
  outline: 3px solid #df9a42;
  outline-offset: 3px;
}

.letter-paper {
  position: relative;
  min-height: 560px;
  padding: clamp(38px, 7vw, 78px);
  border: 1px solid rgba(125, 84, 48, 0.18);
  border-radius: 4px;
  background-color: var(--letter-paper);
  background-image: repeating-linear-gradient(
    0deg,
    transparent 0,
    transparent 33px,
    var(--letter-paper-line) 34px
  );
  box-shadow: 0 30px 70px rgba(32, 68, 78, 0.2);
}

.letter-paper__edge {
  position: absolute;
  inset: 12px;
  border: 1px solid rgba(133, 88, 51, 0.14);
  pointer-events: none;
}

.letter-state {
  position: relative;
  z-index: 1;
  min-height: 390px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 18px;
  color: var(--letter-muted);
  text-align: center;
}

.letter-state--error {
  color: #8c3c34;
}

.letter-ink-loading {
  width: min(280px, 70vw);
  display: grid;
  gap: 12px;
}

.letter-ink-loading span {
  height: 3px;
  display: block;
  background: rgba(116, 82, 58, 0.17);
  transform-origin: left;
  animation: letter-ink-write 1.6s ease-in-out infinite;
}

.letter-ink-loading span:nth-child(2) { width: 82%; animation-delay: 180ms; }
.letter-ink-loading span:nth-child(3) { width: 58%; animation-delay: 360ms; }

.letter-action {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 0;
  border-radius: 4px;
  padding: 10px 16px;
  background: #8b4c3c;
  color: #fffaf0;
  cursor: pointer;
  font-weight: 700;
}

.letter-content {
  position: relative;
  z-index: 1;
}

.letter-content__header {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  align-items: center;
  gap: 24px;
}

.letter-stamp {
  width: 82px;
  height: 82px;
  display: grid;
  border: 2px solid rgba(169, 64, 61, 0.45);
  border-radius: 50%;
  place-items: center;
  color: rgba(169, 64, 61, 0.72);
  font-family: Georgia, serif;
  font-size: 13px;
  font-weight: 700;
  transform: rotate(-8deg);
}

.letter-kicker {
  margin-bottom: 7px;
  color: var(--letter-accent);
  font-family: Georgia, serif;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
}

.letter-content h1 {
  margin: 0;
  color: var(--letter-ink);
  font-family: Georgia, 'Noto Serif SC', serif;
  font-size: clamp(28px, 5vw, 44px);
  line-height: 1.2;
  overflow-wrap: anywhere;
}

.letter-rule {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 30px 0;
  color: var(--letter-accent);
  font-size: 15px;
}

.letter-rule::before,
.letter-rule::after {
  height: 1px;
  flex: 1;
  background: rgba(142, 85, 55, 0.22);
  content: '';
}

.letter-body {
  color: var(--letter-ink);
  font-size: 16px;
  line-height: 2.05;
  overflow-wrap: anywhere;
}

.letter-body :deep(> *) {
  animation: letter-paragraph-reveal 520ms ease both;
}

.letter-body :deep(> :nth-child(2)) { animation-delay: 90ms; }
.letter-body :deep(> :nth-child(3)) { animation-delay: 180ms; }
.letter-body :deep(> :nth-child(4)) { animation-delay: 270ms; }
.letter-body :deep(> :nth-child(5)) { animation-delay: 360ms; }
.letter-body :deep(> :nth-child(n + 6)) { animation-delay: 430ms; }

.letter-delivery-leave-active {
  transition: opacity 280ms ease, transform 360ms ease;
}

.letter-delivery-leave-to {
  opacity: 0;
  transform: translateY(-24px) scale(1.03);
}

.letter-paper-enter-active {
  transition: opacity 460ms ease, transform 620ms cubic-bezier(0.2, 0.72, 0.2, 1);
}

.letter-paper-enter-from {
  opacity: 0;
  transform: translateY(44px) scale(0.97);
}

@keyframes letter-envelope-arrive {
  from { opacity: 0; transform: translate3d(62vw, -18vh, 0) rotate(8deg) scale(0.72); }
  72% { opacity: 1; transform: translate3d(-8px, 4px, 0) rotate(-1.5deg) scale(1.015); }
  to { opacity: 1; transform: translate3d(0, 0, 0) rotate(0) scale(1); }
}

@keyframes letter-flight-path {
  to { border-color: rgba(255, 255, 255, 0.16); }
}

@keyframes letter-star-twinkle {
  0%, 100% { opacity: 0.28; transform: scale(0.75); }
  50% { opacity: 0.9; transform: scale(1.15); }
}

@keyframes letter-ink-write {
  0%, 100% { transform: scaleX(0.12); opacity: 0.35; }
  55% { transform: scaleX(1); opacity: 1; }
}

@keyframes letter-paragraph-reveal {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 640px) {
  .letter-shell {
    width: min(100% - 20px, 900px);
    padding: 94px 0 32px;
  }

  .letter-paper {
    min-height: 500px;
    padding: 34px 24px 46px;
  }

  .letter-paper__edge {
    inset: 8px;
  }

  .letter-content__header {
    grid-template-columns: 58px minmax(0, 1fr);
    gap: 15px;
  }

  .letter-stamp {
    width: 56px;
    height: 56px;
    font-size: 10px;
  }

  .letter-rule {
    margin: 24px 0;
  }

  .letter-body {
    font-size: 15px;
    line-height: 1.95;
  }
}

@media (prefers-reduced-motion: reduce) {
  .letter-page *,
  .letter-page *::before,
  .letter-page *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
