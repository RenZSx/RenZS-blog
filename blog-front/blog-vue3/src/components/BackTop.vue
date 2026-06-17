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
const visibleStyle = 'opacity: 1;transform: translateX(-38px);'
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
  z-index: 4;
  position: fixed;
  right: 12px;
  bottom: 85px;
  transition: all 0.5s;
}

.rightside-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background-color: #49b1f5;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  border-radius: 4px;
}

.rightside-icon:hover {
  background-color: #ff7242;
}
</style>
