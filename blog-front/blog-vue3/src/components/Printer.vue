<template>
  <span>{{ content }}<span class="typed-cursor">|</span></span>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'

interface Props {
  printerInfo: string
  duration?: number
  delay?: number
  working?: boolean
  once?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  printerInfo: '',
  duration: 100,
  delay: 3000,
  working: true,
  once: false
})

const content = ref('')
const cursor = ref(0)
const print = ref(true)
let timer: ReturnType<typeof setInterval> | null = null
let timeout: ReturnType<typeof setTimeout> | null = null

function start(work: () => void) {
  timeout = setTimeout(() => {
    timer = setInterval(() => {
      work()
      if (
        cursor.value === 0 ||
        (cursor.value === props.printerInfo.length && !props.once)
      ) {
        clearInterval(timer!)
        start(work)
      } else if (cursor.value === props.printerInfo.length && props.once) {
        clearInterval(timer!)
      }
    }, props.duration)
  }, props.delay)
}

function work() {
  let cur = cursor.value
  cur += print.value ? 1 : -1
  if (print.value) {
    if (cur === props.printerInfo.length + 1) {
      cur -= 2
      print.value = !print.value
    }
  } else {
    if (cur === -1) {
      cur += 2
      print.value = !print.value
    }
  }
  cursor.value = cur
}

function toBegin() {
  cursor.value = 0
  if (timeout !== null) {
    clearTimeout(timeout)
    if (timer !== null) {
      clearInterval(timer)
    }
  }
  if (props.working) {
    start(work)
  } else {
    content.value = props.printerInfo
  }
}

watch(cursor, (cur) => {
  content.value = props.printerInfo.slice(0, cur)
})

onMounted(() => {
  if (props.working) {
    start(work)
  } else {
    content.value = props.printerInfo
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (timeout) clearTimeout(timeout)
})

defineExpose({ toBegin })
</script>

<style scoped>
.typed-cursor {
  opacity: 1;
  animation: blink 0.7s infinite;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0; }
  100% { opacity: 1; }
}
</style>