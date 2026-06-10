<template>
  <div>
    <div class="count">{{ count }} 评论</div>
    <CommentItem
      v-for="(item, index) of comments"
      :key="item.id"
      :ref="(el: any) => setItemRef(el, index)"
      :comment="item"
      :index="index"
      :floor="count - index"
      :type="type"
      :isLike="isLike"
      @like="$emit('like', $event)"
      @reply="$emit('reply', index, $event)"
      @check-replies="$emit('check-replies', index, item)"
      @change-reply-current="handleChangeReplyCurrent"
      @reload-reply="$emit('reload-reply', $event)"
    />
    <div class="load-wrapper">
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CommentItem from './CommentItem.vue'

interface Comment {
  id: number
  avatar: string
  nickname: string
  userId: number
  webSite?: string
  commentContent: string
  createTime: string
  likeCount: number
  replyCount: number
  replyDTOList?: any[]
}

interface ReplyItem {
  id: number
  avatar: string
  nickname: string
  userId: number
  webSite?: string
  replyUserId?: number
  replyNickname?: string
  replyWebSite?: string
  commentContent: string
  createTime: string
  likeCount: number
}

type ReplyTarget = Comment | ReplyItem

interface Props {
  comments: Comment[]
  count: number
  type: number
  isLike: (id: number) => string
}

defineProps<Props>()

const emit = defineEmits<{
  like: [comment: ReplyTarget]
  reply: [index: number, comment: ReplyTarget]
  'check-replies': [index: number, comment: Comment]
  'change-reply-current': [current: number, index: number, commentId: number]
  'reload-reply': [index: number]
}>()

const itemRefs = ref<Map<number, InstanceType<typeof CommentItem>>>(new Map())

function setItemRef(el: InstanceType<typeof CommentItem> | null, index: number) {
  if (el) {
    itemRefs.value.set(index, el)
  }
}

function getItem(index: number) {
  return itemRefs.value.get(index)
}

function hideAllReplyBoxes() {
  itemRefs.value.forEach((item) => {
    item.hideReplyBox()
  })
}

function showReplyBox(index: number, target: ReplyTarget) {
  const commentItem = getItem(index)
  if (commentItem) {
    commentItem.showReplyBox(target)
  }
}

function hideCheck(index: number) {
  const commentItem = getItem(index)
  if (commentItem) {
    commentItem.hideCheck()
  }
}

function showPaging(index: number) {
  const commentItem = getItem(index)
  if (commentItem) {
    commentItem.showPaging()
  }
}

function getReplyCurrent(index: number) {
  const commentItem = getItem(index)
  return commentItem ? commentItem.getReplyCurrent() : 1
}

function hideReplyBox(index: number) {
  const commentItem = getItem(index)
  if (commentItem) {
    commentItem.hideReplyBox()
  }
}

function handleChangeReplyCurrent(current: number, index: number, commentId: number) {
  emit('change-reply-current', current, index, commentId)
}

defineExpose({
  hideAllReplyBoxes,
  showReplyBox,
  hideCheck,
  showPaging,
  getReplyCurrent,
  hideReplyBox
})
</script>

<style scoped>
.count {
  padding: 5px;
  line-height: 1.75;
  font-size: 1.25rem;
  font-weight: bold;
}
.load-wrapper {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
