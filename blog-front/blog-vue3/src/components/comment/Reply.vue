<template>
  <div class="reply-input-wrapper" ref="replyRef" style="display: none">
    <textarea
      class="comment-textarea"
      :placeholder="'回复 @' + nickname + '：'"
      v-model="commentContent"
    />
    <div class="emoji-container">
      <span
        :class="chooseEmoji ? 'emoji-btn-active' : 'emoji-btn'"
        @click="chooseEmoji = !chooseEmoji"
      >
        <v-icon>mdi-emoticon-outline</v-icon>
      </span>
      <div style="margin-left: auto">
        <button @click="cancelReply" class="cancel-btn v-comment-btn">
          取消
        </button>
        <button @click="insertReply" class="upload-btn v-comment-btn">
          提交
        </button>
      </div>
    </div>
    <!-- 表情框 -->
    <Emoji @add-emoji="addEmoji" :chooseEmoji="chooseEmoji" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import Emoji from '@/components/Emoji.vue'
import {
  buildCommentPayload,
  parseEmoji,
  submitComment
} from './commentService'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

interface Props {
  type: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'reload-reply': [index: number]
}>()

const route = useRoute()
const userStore = useUserStore()

const replyRef = ref<HTMLElement | null>(null)
const index = ref(0)
const chooseEmoji = ref(false)
const nickname = ref('')
const replyUserId = ref<number | null>(null)
const parentId = ref<number | null>(null)
const commentContent = ref('')

function showReply(targetNickname: string, targetUserId: number, commentParentId: number, commentIndex: number) {
  commentContent.value = ''
  nickname.value = targetNickname
  replyUserId.value = targetUserId
  parentId.value = commentParentId
  chooseEmoji.value = false
  index.value = commentIndex
  if (replyRef.value) {
    replyRef.value.style.display = 'block'
  }
}

function hide() {
  if (replyRef.value) {
    replyRef.value.style.display = 'none'
  }
}

function cancelReply() {
  hide()
}

async function insertReply() {
  if (!userStore.userId) {
    openLoginRequiredPrompt({ redirect: route.fullPath })
    return
  }

  if (commentContent.value.trim() === '') {
    useToast({ type: 'error', message: '回复不能为空' })
    return
  }

  const comment = buildCommentPayload({
    content: parseEmoji(commentContent.value),
    type: props.type,
    routePath: route.path,
    extra: {
      replyUserId: replyUserId.value,
      parentId: parentId.value
    }
  })

  commentContent.value = ''

  try {
    const { data } = await submitComment(comment)
    if (data.flag) {
      emit('reload-reply', index.value)
      useToast({ type: 'success', message: '回复成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    console.error('回复失败:', error)
  }
}

function addEmoji(text: string) {
  commentContent.value += text
}

defineExpose({
  showReply,
  hide
})
</script>

<style scoped>
.reply-input-wrapper {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  border: 1px solid var(--glass-border);
  border-radius: 18px;
  padding: 12px;
  margin: 0 0 12px;
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.comment-textarea {
  box-sizing: border-box;
  width: 100%;
  min-height: 60px;
  padding: 10px 12px;
  border: 1px solid var(--card-border-soft);
  border-radius: 14px;
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
  padding: 7px 16px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.3s;
}

.cancel-btn {
  background: rgba(148, 163, 184, 0.14);
  color: var(--text-secondary);
  margin-right: 10px;
}

.cancel-btn:hover {
  background: rgba(148, 163, 184, 0.22);
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
