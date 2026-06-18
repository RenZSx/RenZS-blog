<!--
  功能说明：全站樱花飘落特效与开关。
  作者：OpenAI Codex
  创建时间：2026-06-18
  用途概述：在不阻塞页面点击的全屏 canvas 上绘制轻量樱花动画，并提供本地持久化开关。
-->
<template>
  <Teleport to="body">
    <canvas
      v-show="enabled"
      id="canvas_sakura"
      ref="canvasRef"
      class="sakura-canvas"
      aria-hidden="true"
    />
    <div class="sakura-tools" :class="{ 'sakura-tools--expanded': toolsExpanded }">
      <div v-show="toolsExpanded" class="sakura-tools__panel" aria-label="特效工具">
        <button
          type="button"
          class="sakura-tools__item"
          :title="themeButtonTitle"
          @click="handleThemeToggle"
        >
          <v-icon size="20">{{ themeIcon }}</v-icon>
        </button>
        <button
          type="button"
          class="sakura-tools__item"
          :class="{ 'sakura-tools__item--active': enabled }"
          :aria-pressed="enabled"
          :title="enabled ? '关闭樱花' : '开启樱花'"
          @click="toggleSakura"
        >
          <v-icon size="20">mdi-snowflake</v-icon>
        </button>
      </div>
      <button
        type="button"
        class="sakura-tools__gear-button"
        :aria-expanded="toolsExpanded"
        title="展开特效工具"
        @click="toggleTools"
      >
        <v-icon size="27" class="sakura-tools__gear">mdi-cog</v-icon>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useTheme } from 'vuetify'
import { SAKURA_IMAGE_SRC } from '@/assets/js/sakuraImage'
import { toggleTheme } from '@/utils/theme/toggleTheme'
import {
  SAKURA_STORAGE_KEY,
  createSakuraPetals,
  resolveInitialSakuraEnabled,
  serializeSakuraPreference,
  updateSakuraPetal,
  type SakuraPetal
} from '@/utils/sakuraEffect'

const theme = useTheme()
const canvasRef = ref<HTMLCanvasElement | null>(null)
const enabled = ref(true)
const toolsExpanded = ref(false)
const petals = ref<SakuraPetal[]>([])
const sakuraImage = new Image()
let animationFrameId: number | null = null
let canvasContext: CanvasRenderingContext2D | null = null
let imageLoaded = false

sakuraImage.src = SAKURA_IMAGE_SRC
const isDark = computed(() => theme.global.current.value.dark)
const themeIcon = computed(() => (isDark.value ? 'mdi-weather-night' : 'mdi-white-balance-sunny'))
const themeButtonTitle = computed(() => (isDark.value ? '切换白天模式' : '切换黑夜模式'))

/**
 * 获取设备像素比，并限制上限避免高分屏下 canvas 绘制过重。
 *
 * @returns 用于 canvas 缩放的像素比。
 */
function getPixelRatio() {
  return Math.min(window.devicePixelRatio || 1, 1.5)
}

/**
 * 调整 canvas 尺寸并同步重建樱花数据。
 */
function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  const pixelRatio = getPixelRatio()
  const width = window.innerWidth
  const height = window.innerHeight
  canvas.width = Math.floor(width * pixelRatio)
  canvas.height = Math.floor(height * pixelRatio)
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`
  canvasContext = canvas.getContext('2d')

  if (canvasContext) {
    canvasContext.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
  }

  petals.value = createSakuraPetals({ width, height })
}

/**
 * 使用图片素材绘制一朵樱花，保持 Quaint 版本 drawImage 的视觉方式。
 *
 * @param context canvas 2D 上下文。
 * @param petal 当前花瓣。
 */
function drawPetal(context: CanvasRenderingContext2D, petal: SakuraPetal) {
  const size = 40 * petal.size
  context.save()
  context.translate(petal.x, petal.y)
  context.rotate(petal.rotate)
  context.drawImage(sakuraImage, 0, 0, size, size)
  context.restore()
}

/**
 * 绘制当前帧并调度下一帧。
 */
function drawFrame() {
  const canvas = canvasRef.value
  const context = canvasContext
  if (!canvas || !context || !enabled.value || !imageLoaded) return

  const width = window.innerWidth
  const height = window.innerHeight
  context.clearRect(0, 0, width, height)
  petals.value.forEach((petal) => {
    updateSakuraPetal(petal, { width, height })
    drawPetal(context, petal)
  })
  animationFrameId = window.requestAnimationFrame(drawFrame)
}

/**
 * 启动樱花动画。
 */
async function startSakura() {
  await nextTick()
  if (!imageLoaded) return
  resizeCanvas()
  if (animationFrameId !== null) {
    window.cancelAnimationFrame(animationFrameId)
  }
  animationFrameId = window.requestAnimationFrame(drawFrame)
}

/**
 * 停止樱花动画并清空画布。
 */
function stopSakura() {
  if (animationFrameId !== null) {
    window.cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }

  const canvas = canvasRef.value
  if (canvas && canvasContext) {
    canvasContext.clearRect(0, 0, canvas.width, canvas.height)
  }
}

/**
 * 切换樱花开关并持久化用户偏好。
 */
function toggleSakura() {
  enabled.value = !enabled.value
  localStorage.setItem(SAKURA_STORAGE_KEY, serializeSakuraPreference(enabled.value))
}

/**
 * 展开或收起右下角特效工具面板。
 */
function toggleTools() {
  toolsExpanded.value = !toolsExpanded.value
}

/**
 * 切换全站白天 / 黑夜主题。
 *
 * @param event 鼠标点击事件，用于视图过渡扩散位置。
 */
function handleThemeToggle(event: MouseEvent) {
  const shouldResumeSakura = enabled.value
  if (shouldResumeSakura) {
    stopSakura()
  }

  toggleTheme(theme, {
    x: event.clientX,
    y: event.clientY,
    lightweight: true
  })

  if (shouldResumeSakura) {
    window.requestAnimationFrame(() => {
      startSakura()
    })
  }
}

watch(enabled, (value) => {
  if (value) {
    startSakura()
    return
  }
  stopSakura()
})

onMounted(() => {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  enabled.value = resolveInitialSakuraEnabled(
    localStorage.getItem(SAKURA_STORAGE_KEY),
    prefersReducedMotion
  )
  window.addEventListener('resize', resizeCanvas, { passive: true })

  sakuraImage.onload = () => {
    imageLoaded = true
    if (enabled.value) {
      startSakura()
    }
  }

  if (enabled.value && imageLoaded) {
    startSakura()
  }
})

onBeforeUnmount(() => {
  stopSakura()
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<style scoped>
.sakura-canvas {
  position: fixed;
  inset: 0;
  z-index: 1090;
  pointer-events: none;
}

.sakura-tools {
  position: fixed;
  right: 18px;
  bottom: 132px;
  z-index: 1110;
  display: inline-flex;
  align-items: center;
  gap: 13px;
  min-height: 40px;
}

.sakura-tools__gear-button,
.sakura-tools__item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.sakura-tools__gear-button {
  width: 34px;
  height: 34px;
  color: #050505;
  transition: transform 0.18s ease;
}

.sakura-tools__gear-button:hover {
  transform: translateY(-1px);
}

.sakura-tools__panel {
  display: inline-flex;
  align-items: center;
  justify-content: space-around;
  width: 148px;
  min-height: 54px;
  padding: 0 24px;
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.08);
  color: #5f6368;
  animation: sakura-panel-in 0.18s ease both;
}

.sakura-tools__item {
  width: 34px;
  height: 34px;
  border-radius: 5px;
  color: inherit;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.sakura-tools__item:hover,
.sakura-tools__item--active {
  background: rgba(236, 104, 140, 0.1);
  color: #b64668;
}

.sakura-tools__gear {
  filter: drop-shadow(0 1px 1px rgba(15, 23, 42, 0.18));
  animation: sakura-gear-spin 1.45s linear infinite;
}

:global(.dark) .sakura-tools__panel {
  border-color: rgba(71, 85, 105, 0.78);
  background: rgba(30, 41, 59, 0.96);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.34);
  color: rgba(226, 232, 240, 0.88);
}

:global(.dark) .sakura-tools__gear-button {
  color: rgba(248, 250, 252, 0.96);
}

:global(.dark) .sakura-tools__item:hover,
:global(.dark) .sakura-tools__item--active {
  background: rgba(255, 178, 202, 0.16);
  color: #ffd9e5;
}

@keyframes sakura-panel-in {
  from {
    opacity: 0;
    transform: translateX(12px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes sakura-gear-spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 759px) {
  .sakura-tools {
    right: 12px;
    bottom: 112px;
    gap: 10px;
  }

  .sakura-tools__panel {
    width: 124px;
    min-height: 48px;
    padding: 0 18px;
  }
}
</style>
