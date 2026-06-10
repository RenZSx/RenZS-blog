<template>
  <div class="letter-page">
    <section class="letter-hero">
      <div class="letter-hero__glow letter-hero__glow--left" />
      <div class="letter-hero__glow letter-hero__glow--right" />

      <div class="letter-shell">
        <div class="letter-kicker">Love Letter</div>
        <h1 class="letter-heading">一封给你的信</h1>
        <p class="letter-intro">
<!--          把页面留给一段安静的话，也把读取、空内容和失败都清楚地呈现出来。-->
        </p>

        <v-card class="letter-card" elevation="0">
          <div v-if="state.status === 'loading' || state.status === 'idle'" class="letter-state">
            <v-progress-circular indeterminate color="#b87945" size="34" />
            <p>信件正在送达...</p>
          </div>

          <div v-else-if="state.status === 'error'" class="letter-state letter-state--error">
            <v-icon size="36">mdi-email-alert-outline</v-icon>
            <p>{{ state.errorMessage }}</p>
            <button class="letter-action" type="button" @click="loadLetter">重新获取</button>
          </div>

          <div v-else-if="state.status === 'empty'" class="letter-state">
            <v-icon size="36">mdi-email-open-outline</v-icon>
            <p>这封信还没有内容。</p>
          </div>

          <article v-else class="letter-content">
            <div class="letter-stamp">For You</div>
            <h2>{{ state.letter?.letterTitle || '未命名的信' }}</h2>
            <div class="letter-body markdown-body" v-html="letterContentHtml" />
          </article>
        </v-card>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getLoveLetter } from '@/api/love'
import markdownToHtml from '@/utils/markdown'
import {
  createInitialLetterState,
  reduceLetterState,
  type LetterPayload
} from './letterState'

const state = ref(createInitialLetterState())

const letterContentHtml = computed(() => {
  const content = state.value.letter?.letterContent || ''
  return markdownToHtml(content)
})

async function loadLetter() {
  state.value = reduceLetterState(state.value, { type: 'load' })

  try {
    const { data } = await getLoveLetter()
    const payload = (data?.data || data || null) as LetterPayload | null
    // 后端约定只需要 letterTitle 和 letterContent，状态机负责清洗和分流。
    state.value = reduceLetterState(state.value, {
      type: 'resolve',
      payload
    })
  } catch (error) {
    console.error('获取信件失败:', error)
    state.value = reduceLetterState(state.value, { type: 'reject' })
  }
}

onMounted(() => {
  loadLetter()
})
</script>

<style scoped>
.letter-page {
  min-height: 100vh;
  margin-top: -60px;
  padding-top: 60px;
  background:
    radial-gradient(circle at 16% 18%, rgba(255, 214, 168, 0.42), transparent 30%),
    linear-gradient(135deg, #fff5e8 0%, #f8ddc1 44%, #c58a61 100%);
}

.letter-hero {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 130px 20px 64px;
}

.letter-hero__glow {
  position: absolute;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  filter: blur(12px);
  opacity: 0.5;
}

.letter-hero__glow--left {
  left: -120px;
  top: 120px;
  background: #fff1cf;
}

.letter-hero__glow--right {
  right: -90px;
  bottom: 80px;
  background: #9f5838;
}

.letter-shell {
  position: relative;
  z-index: 1;
  width: min(880px, 100%);
  margin: 0 auto;
  text-align: center;
}

.letter-kicker {
  color: rgba(91, 50, 28, 0.7);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.letter-heading {
  margin-top: 14px;
  color: #4c2c1c;
  font-family: Georgia, 'Noto Serif SC', serif;
  font-size: clamp(38px, 7vw, 74px);
  line-height: 1.05;
}

.letter-intro {
  width: min(560px, 100%);
  margin: 18px auto 34px;
  color: rgba(76, 44, 28, 0.74);
  font-size: 15px;
  line-height: 1.9;
}

.letter-card {
  position: relative;
  min-height: 380px;
  padding: clamp(28px, 5vw, 56px);
  border: 1px solid rgba(120, 68, 37, 0.18);
  border-radius: 28px !important;
  background:
    linear-gradient(145deg, rgba(255, 252, 245, 0.94), rgba(255, 244, 229, 0.88)),
    repeating-linear-gradient(0deg, transparent 0, transparent 31px, rgba(170, 109, 66, 0.08) 32px);
  box-shadow: 0 28px 80px rgba(92, 50, 24, 0.24);
  text-align: left;
}

.letter-card::before {
  position: absolute;
  inset: 18px;
  border: 1px dashed rgba(130, 75, 42, 0.2);
  border-radius: 20px;
  content: '';
  pointer-events: none;
}

.letter-state {
  position: relative;
  z-index: 1;
  display: grid;
  min-height: 270px;
  place-items: center;
  align-content: center;
  gap: 16px;
  color: rgba(76, 44, 28, 0.7);
  text-align: center;
}

.letter-state--error {
  color: #8f3f2b;
}

.letter-action {
  border: 0;
  border-radius: 999px;
  padding: 10px 20px;
  background: #7b4a2b;
  color: #fff9f0;
  cursor: pointer;
  font-weight: 700;
}

.letter-content {
  position: relative;
  z-index: 1;
}

.letter-stamp {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 86px;
  height: 86px;
  border: 2px solid rgba(137, 76, 42, 0.34);
  border-radius: 50%;
  color: rgba(121, 68, 38, 0.68);
  font-family: Georgia, serif;
  font-style: italic;
  transform: rotate(-10deg);
}

.letter-content h2 {
  margin: 24px 0 22px;
  color: #3f2417;
  font-family: Georgia, 'Noto Serif SC', serif;
  font-size: clamp(28px, 4vw, 44px);
}

.letter-body {
  color: rgba(63, 36, 23, 0.86);
  font-size: 16px;
  line-height: 2.05;
  word-break: break-word;
}

@media (max-width: 759px) {
  .letter-hero {
    padding: 104px 14px 38px;
  }

  .letter-card {
    border-radius: 22px !important;
  }
}
</style>
