<template>
  <div v-show="chooseEmoji" class="emoji-wrapper">
    <span
      class="emoji-item"
      v-for="(value, key, index) of emojiList"
      :key="index"
      @click="addEmoji(key as string)"
    >
      <v-img
        :lazy-src="value"
        :src="value"
        :title="key as string"
        class="emoji"
        width="35"
        height="35"
      />
    </span>
  </div>
</template>

<script setup lang="ts">
import EmojiList from '@/assets/js/emoji'

interface Props {
  chooseEmoji: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  addEmoji: [key: string]
}>()

const emojiList = EmojiList

function addEmoji(key: string) {
  emit('addEmoji', key)
}
</script>

<style scoped>
.emoji {
  user-select: none;
  margin: 0.25rem;
  display: inline-block;
  vertical-align: middle;
}
.emoji-item {
  cursor: pointer;
  display: inline-block;
}
.emoji-item:hover {
  transition: all 0.2s;
  border-radius: 0.25rem;
  background: #dddddd;
}
.emoji-wrapper {
  max-height: 150px;
  overflow-y: auto;
}
</style>