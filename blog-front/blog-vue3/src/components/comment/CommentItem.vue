<template>
  <div :id="`comment-${comment.id}`" class="comment-item pt-5">
    <v-avatar size="40" class="comment-avatar">
      <v-img :src="comment.avatar" />
    </v-avatar>
    <div class="comment-meta">
      <div class="comment-user">
        <span v-if="!comment.webSite">{{ comment.nickname }}</span>
        <a v-else :href="comment.webSite" target="_blank">
          {{ comment.nickname }}
        </a>
        <span class="blogger-tag" v-if="comment.userId == 1">博主</span>
      </div>
      <div class="comment-info">
        <span style="margin-right: 10px">{{ floor }}楼</span>
        <span style="margin-right: 10px">{{ formatDate(comment.createTime) }}</span>
        <span
          :class="isLike(comment.id) + ' like-icon'"
          @click="$emit('like', comment)"
        >
          <v-icon size="14">mdi-thumb-up-outline</v-icon>
        </span>
        <span v-show="comment.likeCount > 0"> {{ comment.likeCount }}</span>
        <span class="reply-btn" @click="$emit('reply', comment)">
          回复
        </span>
      </div>
      <p v-html="comment.commentContent" class="comment-content"></p>
      <ReplyList
        :replies="comment.replyDTOList"
        :parentUserId="comment.userId"
        :isLike="isLike"
        @like="$emit('like', $event)"
        @reply="$emit('reply', $event)"
      />
      <div
        class="mb-3"
        style="font-size: 0.75rem; color: #6d757a"
        v-show="comment.replyCount > 3"
        ref="checkRef"
      >
        共
        <b>{{ comment.replyCount }}</b>
        条回复，
        <span
          style="color: #00a1d6; cursor: pointer"
          @click="$emit('check-replies')"
        >
          点击查看
        </span>
      </div>
      <div
        class="mb-3"
        style="font-size: 0.75rem; color: #222; display: none"
        ref="pagingRef"
      >
        <span style="padding-right: 10px">
          共{{ Math.ceil(comment.replyCount / 5) }}页
        </span>
        <Paging
          ref="pageRef"
          :totalPage="Math.ceil(comment.replyCount / 5)"
          :index="index"
          :commentId="comment.id"
          @change-reply-current="handleChangeReplyCurrent"
        />
      </div>
      <Reply
        :type="type"
        ref="replyRef"
        @reload-reply="$emit('reload-reply', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ReplyList from './ReplyList.vue'
import Reply from './Reply.vue'
import Paging from '@/components/Paging.vue'
import { formatDate } from '@/utils/filters'

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
  replyDTOList?: ReplyItem[]
}

interface Props {
  comment: Comment
  index: number
  floor: number
  type: number
  isLike: (id: number) => string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  like: [comment: Comment | ReplyItem]
  reply: [comment: Comment | ReplyItem]
  'check-replies': []
  'change-reply-current': [current: number, index: number, commentId: number]
  'reload-reply': [index: number]
}>()

const checkRef = ref<HTMLElement | null>(null)
const pagingRef = ref<HTMLElement | null>(null)
const pageRef = ref<InstanceType<typeof Paging> | null>(null)
const replyRef = ref<InstanceType<typeof Reply> | null>(null)

function showReplyBox(target: Comment | ReplyItem) {
  if (replyRef.value) {
    replyRef.value.showReply(target.nickname, target.userId, props.comment.id, props.index)
  }
}

function hideReplyBox() {
  if (replyRef.value) {
    replyRef.value.hide()
  }
}

function hideCheck() {
  if (checkRef.value) {
    checkRef.value.style.display = 'none'
  }
}

function showPaging() {
  if (pagingRef.value) {
    pagingRef.value.style.display = 'flex'
  }
}

function getReplyCurrent() {
  return pageRef.value?.getCurrent() || 1
}

function handleChangeReplyCurrent(current: number, index: number, commentId: number) {
  emit('change-reply-current', current, index, commentId)
}

defineExpose({
  showReplyBox,
  hideReplyBox,
  hideCheck,
  showPaging,
  getReplyCurrent
})
</script>

<style scoped>
.blogger-tag {
  background: #ffa51e;
  font-size: 12px;
  display: inline-block;
  border-radius: 2px;
  color: #fff;
  padding: 0 5px;
  margin-left: 6px;
}
.comment-item {
  display: flex;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.comment-meta {
  flex: 1 1 auto;
  min-width: 0;
  margin-left: 0.8rem;
  border-bottom: 1px dashed #f5f5f5;
}
.comment-user {
  font-size: 14px;
  line-height: 1.75;
}
.comment-user a {
  color: #1abc9c !important;
  font-weight: 500;
  transition: 0.3s all;
  text-decoration: none;
}
.comment-info {
  line-height: 1.75;
  font-size: 0.75rem;
  color: #b3b3b3;
}
.reply-btn {
  cursor: pointer;
  float: right;
  color: #ef2f11;
}
.comment-content {
  max-width: 100%;
  font-size: 0.875rem;
  line-height: 1.75;
  padding-top: 0.625rem;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
  overflow-wrap: anywhere;
}
.comment-avatar {
  flex: 0 0 auto;
  transition: all 0.5s;
}
.comment-avatar:hover {
  transform: rotate(360deg);
}
.like-icon {
  cursor: pointer;
  font-size: 0.875rem;
}
.like:hover,
.like-active {
  color: #eb5055;
}

@media (max-width: 759px) {
  .comment-meta {
    margin-left: 0.625rem;
  }

  .reply-btn {
    float: none;
    margin-left: 10px;
  }
}
</style>
