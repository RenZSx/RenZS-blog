<template>
  <div>
    <div :id="`comment-${reply.id}`" class="reply-item" v-for="reply of replies" :key="reply.id">
      <v-avatar size="36" class="comment-avatar">
        <v-img :src="reply.avatar" />
      </v-avatar>
      <div class="reply-meta">
        <div class="comment-user">
          <span v-if="!reply.webSite">{{ reply.nickname }}</span>
          <a v-else :href="reply.webSite" target="_blank">
            {{ reply.nickname }}
          </a>
          <span class="blogger-tag" v-if="reply.userId == 1">博主</span>
        </div>
        <div class="comment-info">
          <span style="margin-right: 10px">
            {{ formatDate(reply.createTime) }}
          </span>
          <span
            :class="isLike(reply.id) + ' like-icon'"
            @click="$emit('like', reply)"
          >
            <v-icon size="14">mdi-thumb-up-outline</v-icon>
          </span>
          <span v-show="reply.likeCount > 0"> {{ reply.likeCount }}</span>
          <span class="reply-btn" @click="$emit('reply', reply)">
            回复
          </span>
        </div>
        <p class="comment-content">
          <template v-if="reply.replyUserId != parentUserId">
            <span v-if="!reply.replyWebSite" class="ml-1">
              @{{ reply.replyNickname }}
            </span>
            <a
              v-else
              :href="reply.replyWebSite"
              target="_blank"
              class="comment-nickname ml-1"
            >
              @{{ reply.replyNickname }}
            </a>
            ，
          </template>
          <span v-html="reply.commentContent" />
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
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

interface Props {
  replies?: ReplyItem[]
  parentUserId: number
  isLike: (id: number) => string
}

withDefaults(defineProps<Props>(), {
  replies: () => []
})

defineEmits<{
  like: [reply: ReplyItem]
  reply: [reply: ReplyItem]
}>()
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
.reply-item {
  display: flex;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.reply-meta {
  flex: 1 1 auto;
  min-width: 0;
  margin-left: 0.8rem;
}
.comment-user {
  font-size: 14px;
  line-height: 1.75;
}
.comment-user a,
.comment-nickname {
  text-decoration: none;
  color: #1abc9c !important;
  font-weight: 500;
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
  .reply-meta {
    margin-left: 0.625rem;
  }

  .reply-btn {
    float: none;
    margin-left: 10px;
  }
}
</style>
