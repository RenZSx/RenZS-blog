<template>
  <v-card class="home-card home-talk-card">
    <div class="home-talk-card-inner">
      <div class="home-talk-card-meta">
        <div class="home-talk-card-title">
          <div class="home-talk-card-icon">
            <v-icon color="#fff" size="20">mdi-comment-quote</v-icon>
          </div>
          <div>
            <div class="home-talk-card-heading">说说动态</div>
            <div class="home-talk-card-copy">记录生活点滴</div>
          </div>
        </div>
        <span class="home-talk-card-badge">{{ normalizedTalkList.length }} 条</span>
      </div>
      <div class="home-talk-carousel">
        <router-link to="/talks" class="talk-swiper">
          <div class="talk-window">
            <div
              class="talk-list"
              :class="{ 'talk-list-no-transition': talkIndex === 0 }"
              :style="{ transform: `translateY(-${talkIndex * talkItemHeight}px)` }"
            >
              <div
                v-for="talk in loopTalkList"
                :key="talk.loopKey"
                class="talk-item"
                :style="{ height: `${talkItemHeight}px` }"
                v-html="talk.content"
              />
            </div>
          </div>
        </router-link>
        <router-link
          v-if="normalizedTalkList.length > 0"
          :to="'/talks'"
          class="talk-detail-link"
          :aria-label="'查看说说：' + normalizedTalkList[activeTalkIndex].plainText"
        />
      </div>
    </div>
  </v-card>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

type Talk = string | {
  id?: number
  content?: string
  talkContent?: string
  createTime?: string
}

interface NormalizedTalk {
  id: number
  content: string
  plainText: string
}

interface Props {
  talkList: Talk[]
}

const props = defineProps<Props>()
const talkIndex = ref(0)
const talkItemHeight = 28
let talkTimer: ReturnType<typeof setInterval> | null = null

function stripHtml(value: string) {
  return value.replace(/<\/?[^>]*>/g, '').trim()
}

const normalizedTalkList = computed<NormalizedTalk[]>(() =>
  props.talkList
    .map((talk, index) => {
      const content = typeof talk === 'string'
        ? talk
        : talk.content || talk.talkContent || ''

      return {
        id: typeof talk === 'string' ? index : talk.id ?? index,
        content,
        plainText: stripHtml(content)
      }
    })
    .filter(talk => talk.plainText)
)

const loopTalkList = computed(() => [
  ...normalizedTalkList.value.map((talk, index) => ({
    ...talk,
    loopKey: `main-${talk.id}-${index}`
  })),
  ...normalizedTalkList.value.map((talk, index) => ({
    ...talk,
    loopKey: `copy-${talk.id}-${index}`
  }))
])

const activeTalkIndex = computed(() => {
  if (normalizedTalkList.value.length === 0) return 0
  return talkIndex.value % normalizedTalkList.value.length
})

function startTalkSwiper() {
  stopTalkSwiper()

  if (normalizedTalkList.value.length <= 1) {
    talkIndex.value = 0
    return
  }

  talkTimer = setInterval(() => {
    if (talkIndex.value >= normalizedTalkList.value.length) {
      talkIndex.value = 0
      return
    }

    talkIndex.value++
  }, 3000)
}

function stopTalkSwiper() {
  if (talkTimer) {
    clearInterval(talkTimer)
    talkTimer = null
  }
}

onMounted(() => {
  startTalkSwiper()
})

onUnmounted(() => {
  stopTalkSwiper()
})

watch(
  () => normalizedTalkList.value.length,
  () => {
    talkIndex.value = 0
    startTalkSwiper()
  }
)
</script>

<style scoped>
.home-talk-card {
  position: relative;
  padding: 0;
  overflow: hidden;
  border: 1px solid var(--card-border-soft);
  border-radius: var(--card-radius-md) !important;
  background: var(--card-surface-soft);
  box-shadow: var(--card-shadow-soft);
  transition: transform 0.32s ease, border-color 0.32s ease, box-shadow 0.32s ease;
}

.home-talk-card:hover {
  transform: translateY(-3px);
  border-color: var(--card-border-accent-hover);
  box-shadow: var(--card-shadow-hover);
}

.home-talk-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(
      circle at top right,
      rgba(94, 166, 229, 0.18),
      transparent 34%
    ),
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.98),
      rgba(245, 249, 255, 0.94)
    );
  pointer-events: none;
}

.home-talk-card-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0.9rem 1rem 1rem;
}

.home-talk-card-meta {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.home-talk-card-title {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.home-talk-card-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 12px;
  background: linear-gradient(135deg, #5ea6e5, #80d0ff);
  box-shadow: 0 12px 24px rgba(94, 166, 229, 0.28);
  flex-shrink: 0;
}

.home-talk-card-heading {
  color: #253046;
  font-size: 0.9rem;
  font-weight: 700;
  line-height: 1.35;
}

.home-talk-card-copy {
  margin-top: 2px;
  color: #6b7382;
  font-size: 0.72rem;
  line-height: 1.7;
}

.home-talk-card-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  padding: 0.25rem 0.55rem;
  border: 1px solid var(--card-border-accent);
  border-radius: 999px;
  background: rgba(94, 166, 229, 0.08);
  color: #5ea6e5;
  font-size: 0.66rem;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.home-talk-carousel {
  position: relative;
  border: 1px solid var(--card-border-soft);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
  padding: 0.65rem 0.8rem;
}

.talk-swiper {
  display: block;
  color: inherit;
  text-decoration: none;
}

.talk-window {
  height: 28px;
  overflow: hidden;
}

.talk-list {
  transition: transform 1s linear;
}

.talk-list-no-transition {
  transition: none;
}

.talk-item {
  display: block;
  color: #475163;
  font-size: 0.78rem;
  font-weight: 600;
  line-height: 28px;
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}

.talk-swiper:hover .talk-item {
  color: #2ba1d1;
}

.talk-detail-link {
  position: absolute;
  inset: 0;
  border-radius: 18px;
}
</style>
