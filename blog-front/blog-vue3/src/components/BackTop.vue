<template>
  <div class="rightside" :style="isShow">
    <v-icon
      class="rightside-icon"
      @click="backTop"
    >
      mdi-chevron-up
    </v-icon>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { createScrollFrameScheduler } from '@/utils/scrollFrame'

const isShow = ref('')
const visibleStyle = 'opacity: 1;transform: translateY(0);pointer-events: auto;'
const backTopScheduler = createScrollFrameScheduler(updateBackTopVisibility)

function backTop() {
  window.scrollTo({
    behavior: 'smooth',
    top: 0
  })
}

function updateBackTopVisibility() {
  const scrollTop =
    window.pageYOffset ||
    document.documentElement.scrollTop ||
    document.body.scrollTop
  const nextStyle = scrollTop > 20 ? visibleStyle : ''

  if (nextStyle !== isShow.value) {
    isShow.value = nextStyle
  }
}

onMounted(() => {
  backTopScheduler.runNow()
  window.addEventListener('scroll', backTopScheduler.requestUpdate, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', backTopScheduler.requestUpdate)
  backTopScheduler.cancel()
})
</script>

<style scoped>
.rightside {
  position: fixed;
  right: 18px;
  bottom: 88px;
  z-index: 1110;
  opacity: 0;
  pointer-events: none;
  transform: translateY(8px);
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.rightside-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 5px;
  background: transparent;
  color: #050505;
  filter: drop-shadow(0 1px 1px rgba(15, 23, 42, 0.18));
  font-size: 27px;
  cursor: pointer;
  transition: transform 0.18s ease, color 0.18s ease;
}

.rightside-icon:hover {
  color: #111;
  transform: translateY(-1px);
}

:global(.dark) .rightside-icon {
  color: rgba(248, 250, 252, 0.96);
}

@media (max-width: 759px) {
  .rightside {
    right: 12px;
    bottom: 70px;
  }
}
</style>
