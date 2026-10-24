<template>
  <div class="tag-cloud">
    <span
      v-for="(tag, index) in tagList"
      :key="tag.id"
      :style="getTagStyle(index)"
      class="tag-item"
    >
      {{ tag.name }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const tagList = computed(() => props.data)

// 后台没有 /tags/:id 路由,标签仅作展示;
// 颜色按位置固定分配,避免每次重渲染随机跳变
const palette = ['#4f7cff', '#22b07d', '#f59e0b', '#8250df', '#ef6c6c', '#38bdf8']

const getTagStyle = (index) => {
  const minFontSize = 13
  const maxFontSize = 22
  const total = tagList.value.length
  const ratio = total > 1 ? index / (total - 1) : 0

  return {
    fontSize: (minFontSize + (maxFontSize - minFontSize) * ratio).toFixed(1) + 'px',
    color: palette[index % palette.length]
  }
}
</script>

<style scoped>
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  padding: 20px;
  min-height: 200px;
}

.tag-item {
  margin: 10px;
  padding: 5px 10px;
  font-weight: 600;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: scale(1.15);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.15);
}
</style>
