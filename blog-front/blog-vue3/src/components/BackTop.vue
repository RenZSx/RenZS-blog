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

const isShow = ref('')

function backTop() {
  window.scrollTo({
    behavior: 'smooth',
    top: 0
  })
}

function scrollToTop() {
  const scrollTop =
    window.pageYOffset ||
    document.documentElement.scrollTop ||
    document.body.scrollTop

  if (scrollTop > 20) {
    isShow.value = 'opacity: 1;transform: translateX(-38px);'
  } else {
    isShow.value = ''
  }
}

onMounted(() => {
  window.addEventListener('scroll', scrollToTop)
})

onUnmounted(() => {
  window.removeEventListener('scroll', scrollToTop)
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
