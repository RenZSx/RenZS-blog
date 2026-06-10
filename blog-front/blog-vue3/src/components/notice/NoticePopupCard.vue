<template>
  <article
    class="notice-popup-card"
    tabindex="0"
    role="button"
    @click="emit('open')"
    @mouseenter="emit('pause')"
    @mouseleave="emit('resume')"
    @focusin="emit('pause')"
    @focusout="handleFocusOut"
    @keydown.enter.prevent="emit('open')"
    @keydown.space.prevent="emit('open')"
  >
    <div class="notice-popup-card__status">
      <span class="notice-popup-card__accent" aria-hidden="true" />
      <div class="notice-popup-card__icon" aria-hidden="true">
        <v-icon :icon="payload.icon" size="20" />
      </div>
    </div>

    <div class="notice-popup-card__content">
      <div class="notice-popup-card__head">
        <div class="notice-popup-card__meta">
          <h3 class="notice-popup-card__title">{{ payload.title }}</h3>
        </div>
        <button
          type="button"
          class="notice-popup-card__close"
          aria-label="关闭通知弹窗"
          @click.stop="emit('close')"
        >
          <v-icon icon="mdi-close" size="18" />
        </button>
      </div>

      <time class="notice-popup-card__time">{{ payload.timeText }}</time>
      <div class="notice-popup-card__summary" v-html="payload.summary" />
    </div>

    <div class="notice-popup-card__progress" aria-hidden="true">
      <span
        class="notice-popup-card__progress-bar"
        :style="{
          width: progressWidth,
          transitionDuration: `${progressDuration}ms`
        }"
      />
    </div>
  </article>
</template>

<script setup lang="ts">
import type { NoticePopupPayload } from '@/utils/noticePopup'

defineProps<{
  payload: NoticePopupPayload
  progressWidth: string
  progressDuration: number
}>()

const emit = defineEmits<{
  pause: []
  resume: []
  open: []
  close: []
}>()

function handleFocusOut(event: FocusEvent) {
  const nextTarget = event.relatedTarget
  if (nextTarget instanceof Node && event.currentTarget instanceof Node) {
    if (event.currentTarget.contains(nextTarget)) {
      return
    }
  }

  emit('resume')
}
</script>

<style scoped>
.notice-popup-card {
  position: relative;
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  gap: 10px;
  width: min(280px, calc(100vw - 28px));
  padding: 13px 14px;
  border: 1px solid rgba(229, 231, 235, 0.92);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow:
    0 10px 28px rgba(15, 23, 42, 0.14),
    0 2px 8px rgba(15, 23, 42, 0.06);
  color: var(--text-primary);
  cursor: pointer;
  pointer-events: auto;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.notice-popup-card:hover,
.notice-popup-card:focus-visible {
  border-color: rgba(191, 196, 204, 0.96);
  box-shadow:
    0 14px 34px rgba(15, 23, 42, 0.18),
    0 4px 12px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
  outline: none;
}

.notice-popup-card__status {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 6px;
  padding-top: 1px;
}

.notice-popup-card__accent {
  width: 4px;
  min-width: 4px;
  height: 30px;
  border-radius: 999px;
  background: #409eff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.08);
}

.notice-popup-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.notice-popup-card__content {
  min-width: 0;
}

.notice-popup-card__head {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.notice-popup-card__meta {
  flex: 1;
  min-width: 0;
}

.notice-popup-card__title {
  margin: 0;
  color: #303133;
  font-size: 0.92rem;
  font-weight: 600;
  line-height: 1.35;
}

.notice-popup-card__time {
  display: inline-block;
  margin-top: 2px;
  color: #909399;
  font-size: 0.72rem;
  line-height: 1.3;
}

.notice-popup-card__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #c0c4cc;
  cursor: pointer;
  transition:
    background-color 0.18s ease,
    color 0.18s ease;
}

.notice-popup-card__close:hover,
.notice-popup-card__close:focus-visible {
  background: rgba(243, 244, 246, 0.92);
  color: #606266;
  outline: none;
}

.notice-popup-card__summary {
  margin-top: 8px;
  color: #606266;
  font-size: 0.8rem;
  line-height: 1.5;
  word-break: break-word;
}

.notice-popup-card__summary :deep(img) {
  display: inline-block;
  max-width: 100%;
  max-height: 1.45em;
  margin: 0 2px;
  vertical-align: text-bottom;
  border-radius: 4px;
}

.notice-popup-card__progress {
  grid-column: 1 / -1;
  height: 2px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(64, 158, 255, 0.16);
}

.notice-popup-card__progress-bar {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #409eff;
  transition-property: width;
  transition-timing-function: linear;
}

:global(.dark) .notice-popup-card {
  border-color: rgba(70, 76, 86, 0.94);
  background: rgba(28, 31, 36, 0.98);
  box-shadow:
    0 16px 34px rgba(0, 0, 0, 0.36),
    0 4px 12px rgba(0, 0, 0, 0.24);
}

:global(.dark) .notice-popup-card:hover,
:global(.dark) .notice-popup-card:focus-visible {
  border-color: rgba(94, 101, 113, 0.96);
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.4),
    0 6px 16px rgba(0, 0, 0, 0.28);
}

:global(.dark) .notice-popup-card__accent {
  background: #79bbff;
  box-shadow: 0 0 0 1px rgba(121, 187, 255, 0.1);
}

:global(.dark) .notice-popup-card__icon {
  background: rgba(121, 187, 255, 0.16);
  color: #a9d3ff;
}

:global(.dark) .notice-popup-card__title {
  color: rgba(245, 247, 250, 0.96);
}

:global(.dark) .notice-popup-card__time {
  color: rgba(166, 173, 186, 0.84);
}

:global(.dark) .notice-popup-card__summary {
  color: rgba(214, 220, 235, 0.84);
}

:global(.dark) .notice-popup-card__progress {
  background: rgba(121, 187, 255, 0.18);
}

:global(.dark) .notice-popup-card__progress-bar {
  background: #79bbff;
}

:global(.dark) .notice-popup-card__close {
  color: rgba(140, 147, 157, 0.82);
}

:global(.dark) .notice-popup-card__close:hover,
:global(.dark) .notice-popup-card__close:focus-visible {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(245, 247, 250, 0.92);
}

@media (max-width: 759px) {
  .notice-popup-card {
    width: min(280px, calc(100vw - 20px));
    grid-template-columns: 34px minmax(0, 1fr);
    gap: 8px;
    padding: 12px 13px;
    border-radius: 12px;
  }

  .notice-popup-card__accent {
    height: 28px;
  }

  .notice-popup-card__icon {
    width: 22px;
    height: 22px;
  }
}
</style>
