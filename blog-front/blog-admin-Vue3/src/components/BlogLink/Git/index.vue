<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('https://gitee.com/chen_fuyun/blog-satoken')

function goto() {
  window.open(url.value)
}
</script>
