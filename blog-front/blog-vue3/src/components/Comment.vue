<template>
  <div class="comment-section">
    <div class="comment-title">
      <v-icon size="20">mdi-comment-multiple</v-icon>评论
    </div>
    <CommentEditor
      v-model="commentContent"
      :choose-emoji="chooseEmoji"
      :avatar="userStore.avatar || undefined"
      :tourist-avatar="touristAvatar"
      @toggle-emoji="chooseEmoji = !chooseEmoji"
      @submit="insertComment"
      @add-emoji="addEmoji"
    />
    <CommentList
      v-if="count > 0 && reFresh"
      ref="commentListRef"
      :comments="commentList"
      :count="count"
      :type="type"
      :is-like="isLike"
      @like="like"
      @reply="replyComment"
      @check-replies="checkReplies"
      @change-reply-current="changeReplyCurrent"
      @reload-reply="reloadReply"
    />
    <div v-else style="padding: 1.25rem; text-align: center">
      来发评论吧~
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import CommentEditor from './comment/CommentEditor.vue'
import CommentList from './comment/CommentList.vue'
import {
  buildCommentPayload,
  buildCommentQuery,
  fetchComments,
  fetchReplies,
  parseEmoji,
  sendCommentLike,
  submitComment
} from './comment/commentService'
import { useUserStore } from '@/stores/user'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useToast } from '@/composables/useToast'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

interface Props {
  type: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'get-comment-count': [count: number]
}>()

const route = useRoute()
const userStore = useUserStore()
const blogInfoStore = useBlogInfoStore()

const commentListRef = ref<InstanceType<typeof CommentList> | null>(null)
const reFresh = ref(true)
const commentContent = ref('')
const chooseEmoji = ref(false)
const current = ref(1)
const commentList = ref<any[]>([])
const count = ref(0)
const loading = ref(false)
const hasMore = ref(true)
const lastScrolledHash = ref('')

const touristAvatar = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig?.touristAvatar || ''
})

function isLike(commentId: number) {
  const commentLikeSet = userStore.commentLikeSet
  return commentLikeSet.includes(commentId) ? 'like-active' : 'like'
}

function replyComment(index: number, item: any) {
  if (!commentListRef.value) return
  commentListRef.value.hideAllReplyBoxes()
  commentListRef.value.showReplyBox(index, item)
}

function addEmoji(key: string) {
  commentContent.value += key
}

async function checkReplies(index: number, item: any) {
  const { data } = await fetchReplies(item.id, { current: 1, size: 5 })
  commentListRef.value?.hideCheck(index)
  item.replyDTOList = data.data
  if (Math.ceil(item.replyCount / 5) > 1) {
    commentListRef.value?.showPaging(index)
  }
}

async function changeReplyCurrent(currentVal: number, index: number, commentId: number) {
  const { data } = await fetchReplies(commentId, { current: currentVal, size: 5 })
  commentList.value[index].replyDTOList = data.data
}

async function listComments() {
  if (loading.value || !hasMore.value) return

  loading.value = true
  const params = buildCommentQuery({
    current: current.value,
    type: props.type,
    routePath: route.path
  })

  try {
    const { data } = await fetchComments(params)
    const records = data.data?.recordList || []
    if (current.value === 1) {
      commentList.value = records
    } else {
      commentList.value.push(...records)
    }
    current.value++
    count.value = data.data?.count ?? 0
    hasMore.value = commentList.value.length < count.value && records.length > 0
    emit('get-comment-count', count.value)
  } finally {
    loading.value = false
  }
}

async function insertComment() {
  if (!userStore.userId) {
    openLoginRequiredPrompt({ redirect: route.fullPath })
    return
  }

  if (commentContent.value.trim() === '') {
    useToast({ type: 'error', message: '评论不能为空' })
    return
  }

  const parsedContent = parseEmoji(commentContent.value)
  const comment = buildCommentPayload({
    content: parsedContent,
    type: props.type,
    routePath: route.path
  })

  commentContent.value = ''

  const { data } = await submitComment(comment)
  if (data.flag) {
    current.value = 1
    await listComments()
    const isReview = blogInfoStore.blogInfo?.websiteConfig?.isCommentReview
    if (isReview) {
      useToast({ type: 'warning', message: '评论成功，正在审核中' })
    } else {
      useToast({ type: 'success', message: '评论成功' })
    }
  } else {
    useToast({ type: 'error', message: data.message })
  }
}

async function like(comment: any) {
  if (!userStore.userId) {
    openLoginRequiredPrompt({ redirect: route.fullPath })
    return
  }

  const { data } = await sendCommentLike(comment.id)
  if (data.flag) {
    const commentLikeSet = userStore.commentLikeSet
    if (commentLikeSet.includes(comment.id)) {
      comment.likeCount -= 1
    } else {
      comment.likeCount += 1
    }
    userStore.toggleCommentLike(comment.id)
  }
}

async function reloadReply(index: number) {
  const replyCurrent = commentListRef.value?.getReplyCurrent(index) || 1
  const { data } = await fetchReplies(commentList.value[index].id, {
    current: replyCurrent,
    size: 5
  })
  commentList.value[index].replyCount++
  if (commentList.value[index].replyCount > 5) {
    commentListRef.value?.showPaging(index)
  }
  commentListRef.value?.hideCheck(index)
  commentListRef.value?.hideReplyBox(index)
  commentList.value[index].replyDTOList = data.data
}

async function scrollToHashTarget(hash: string) {
  if (!hash) {
    lastScrolledHash.value = ''
    return
  }

  await nextTick()
  const target = document.querySelector<HTMLElement>(hash)
  if (!target) {
    return
  }

  const top = target.getBoundingClientRect().top + window.scrollY - 88
  window.scrollTo({
    top: Math.max(top, 0),
    behavior: 'smooth'
  })
  lastScrolledHash.value = hash
}

watch(commentList, () => {
  reFresh.value = false
  nextTick(() => {
    reFresh.value = true
  })
}, { deep: true })

watch(
  [() => route.hash, reFresh],
  async ([hash, ready]) => {
    if (!ready || !hash || hash === lastScrolledHash.value) {
      return
    }

    await scrollToHashTarget(hash)
  },
  { flush: 'post' }
)

// 初始化加载评论
listComments()

useInfiniteScroll({
  loading,
  hasMore,
  onLoadMore: listComments
})
</script>

<style scoped>
.comment-section {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  padding: 0 42px 38px;
  overflow: hidden;
}

.comment-title {
  display: flex;
  align-items: center;
  font-size: 1.25rem;
  font-weight: bold;
  line-height: 40px;
  margin-bottom: 10px;
}
.comment-title i {
  margin-right: 5px;
}

@media (max-width: 759px) {
  .comment-section {
    padding: 0 18px 28px;
  }
}
</style>
