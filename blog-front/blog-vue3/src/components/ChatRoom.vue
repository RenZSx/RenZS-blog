<template>
  <transition name="room-float">
    <button
      v-if="enabled"
      type="button"
      class="floating-room"
      @click="goToChatPage"
    >
      <span class="floating-room-dot" :class="{ 'floating-room-dot-live': enabled }" />
      <span class="floating-room-copy">
        <span class="floating-room-title">聊天室</span>
        <span class="floating-room-desc">
          {{ websocketUrl ? '实时交流已开启' : '进入聊天页参与讨论' }}
        </span>
      </span>
      <v-icon size="18">mdi-arrow-right</v-icon>
    </button>
  </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'

const router = useRouter()
const blogInfoStore = useBlogInfoStore()

const enabled = computed(() => Number(blogInfoStore.blogInfo.websiteConfig.isChatRoom) === 1)
const websocketUrl = computed(() => blogInfoStore.blogInfo.websiteConfig.websocketUrl)

function goToChatPage() {
  router.push('/chat')
}
</script>

<style scoped>
.room-float-enter-active,
.room-float-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.room-float-enter-from,
.room-float-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.floating-room {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 1100;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 220px;
  padding: 14px 16px;
  border: 1px solid rgba(73, 177, 245, 0.18);
  border-radius: 18px;
  background:
    radial-gradient(circle at left top, rgba(73, 177, 245, 0.16), transparent 34%),
    rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.14);
  color: #142133;
  cursor: pointer;
  text-align: left;
  backdrop-filter: blur(10px);
}

.floating-room-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #9aa8b6;
  flex-shrink: 0;
}

.floating-room-dot-live {
  background: #2acb8d;
  box-shadow: 0 0 0 6px rgba(42, 203, 141, 0.12);
}

.floating-room-copy {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.floating-room-title {
  font-size: 15px;
  font-weight: 700;
}

.floating-room-desc {
  color: #627388;
  font-size: 12px;
  line-height: 1.4;
}

@media (max-width: 759px) {
  .floating-room {
    right: 12px;
    bottom: 16px;
    min-width: 0;
    width: calc(100vw - 24px);
  }
}
</style>
