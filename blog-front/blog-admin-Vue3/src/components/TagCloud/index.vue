<template>
  <div class="tag-cloud">
    <router-link
      v-for="tag in tagList"
      :key="tag.id"
      :to="'/tags/' + tag.id"
      :style="getTagStyle(tag)"
      class="tag-item"
    >
      {{ tag.name }}
    </router-link>
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

const getTagStyle = (tag) => {
  const minFontSize = 12
  const maxFontSize = 30
  const index = tagList.value.indexOf(tag)
  const total = tagList.value.length

  // 根据标签在列表中的位置计算字体大小
  const fontSize = minFontSize + (maxFontSize - minFontSize) * (index / total)

  // 生成随机颜色
  const colors = [
    '#7EC0EE',
    '#FF9F7F',
    '#FFD700',
    '#C9C9C9',
    '#E066FF',
    '#C0FF3E',
    '#48D1CC',
    '#F08080'
  ]
  const color = colors[Math.floor(Math.random() * colors.length)]

  return {
    fontSize: fontSize + 'px',
    color: color
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
  text-decoration: none;
  transition: all 0.3s;
  cursor: pointer;
}

.tag-item:hover {
  transform: scale(1.2);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}
</style>
