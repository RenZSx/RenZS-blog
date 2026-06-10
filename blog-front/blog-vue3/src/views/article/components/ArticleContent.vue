<template>
  <div class="article-main">
    <article
      id="write"
      class="article-content markdown-body article-markdown"
      v-html="content"
      ref="articleRef"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  content?: string
}

withDefaults(defineProps<Props>(), {
  content: ''
})

const articleRef = ref<HTMLElement | null>(null)

function getArticleElement() {
  return articleRef.value
}

defineExpose({
  getArticleElement
})
</script>

<style scoped>
.article-main {
  padding: 5px 54px 20px;
}

.article-content {
  max-width: 860px;
  margin: 0 auto;
  word-break: break-word;
}

@media (max-width: 759px) {
  .article-main {
    padding: 5px 18px 12px;
  }

  .article-content {
    max-width: none;
  }
}
</style>
