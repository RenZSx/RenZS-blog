<template>
  <div class="comment-input-wrapper">
    <div class="comment-editor-row">
      <v-avatar size="40">
        <v-img v-if="avatar" :src="avatar" />
        <v-img v-else :src="touristAvatar" />
      </v-avatar>
      <div class="comment-editor-body">
        <div class="comment-input">
          <textarea
            class="comment-textarea"
            :value="modelValue"
            placeholder="留下点什么吧..."
            @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>
        <div class="emoji-container">
          <span
            :class="chooseEmoji ? 'emoji-btn-active' : 'emoji-btn'"
            @click="$emit('toggle-emoji')"
          >
            <v-icon>mdi-emoticon-outline</v-icon>
          </span>
          <button
            @click="$emit('submit')"
            class="upload-btn v-comment-btn"
            style="margin-left: auto"
          >
            提交
          </button>
        </div>
        <Emoji
          @add-emoji="$emit('add-emoji', $event)"
          :chooseEmoji="chooseEmoji"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Emoji from '@/components/Emoji.vue'

interface Props {
  modelValue: string
  chooseEmoji: boolean
  avatar?: string | null
  touristAvatar?: string
}

defineProps<Props>()

defineEmits<{
  'update:modelValue': [value: string]
  'toggle-emoji': []
  'submit': []
  'add-emoji': [key: string]
}>()
</script>

<style scoped>
.comment-input-wrapper {
  box-sizing: border-box;
  width: 100%;
  border: 1px solid var(--glass-border);
  border-radius: var(--card-radius-md);
  padding: 14px;
  margin: 0 0 14px;
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.comment-editor-row {
  display: flex;
  width: 100%;
  min-width: 0;
}

.comment-editor-body {
  flex: 1 1 auto;
  min-width: 0;
  margin-left: 0.75rem;
}

.comment-textarea {
  box-sizing: border-box;
  width: 100%;
  min-height: 80px;
  padding: 12px 14px;
  border: 1px solid var(--card-border-soft);
  border-radius: 16px;
  outline: none;
  resize: vertical;
  font-size: 14px;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.5);
  color: var(--text-primary);
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal),
    background var(--transition-normal);
}

.comment-textarea:focus {
  border-color: var(--card-border-accent-hover);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 0 0 3px rgba(73, 177, 245, 0.12);
}

@media (max-width: 759px) {
  .comment-editor-body {
    margin-left: 0.625rem;
  }
}

.emoji-container {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.emoji-btn,
.emoji-btn-active {
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 999px;
  transition: all 0.3s;
}

.emoji-btn:hover,
.emoji-btn-active {
  background: rgba(73, 177, 245, 0.12);
  color: #2a93d5;
}

.v-comment-btn {
  padding: 7px 18px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.3s;
}

.upload-btn {
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  color: #fff;
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.22);
}

.upload-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(73, 177, 245, 0.28);
}
</style>
