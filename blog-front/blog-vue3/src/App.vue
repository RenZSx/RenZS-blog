<template>
  <v-app id="app">
    <!-- 顶部导航栏；独立认证页不挂载主站导航，保证登录入口是完整页面。 -->
    <TopNavBar/>

    <!-- 侧边导航栏；独立认证页隐藏移动端抽屉入口，避免挤占认证卡片宽度。 -->
    <SideNavBar/>

    <!-- 主内容区域 -->
    <v-main :class="{ 'auth-main': isStandaloneAuth, 'start-main': isStartPage }">
      <router-view v-slot="{ Component, route }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </v-main>

    <!-- 初始弹窗 -->
    <InitialPopup v-if="!isStandaloneAuth" />

    <!-- 页脚 -->
    <Footer v-if="!isStandaloneAuth && !isStartPage" />

    <!-- 返回顶部 -->
    <BackTop v-if="!isStandaloneAuth" />

    <!-- 模态框 -->
    <SearchModel />
    <EmailModel />
    <AuthPromptDialog />
    <NoticePopupContainer />

    <!-- 聊天室 -->
    <ChatRoom
      v-if="!isStandaloneAuth && !hideFloatingChatRoom && blogInfoStore.blogInfo.websiteConfig?.isChatRoom"
    />

    <!-- 全站樱花特效；用户可通过浮动开关随时开启或关闭。 -->
    <SakuraEffect />
  </v-app>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from 'vuetify'

// 布局组件
import TopNavBar from '@/components/layout/TopNavBar.vue'
import SideNavBar from '@/components/layout/SideNavBar.vue'
import Footer from '@/components/layout/Footer.vue'
import BackTop from '@/components/BackTop.vue'

// 模态框组件
import SearchModel from '@/components/model/SearchModel.vue'
import EmailModel from '@/components/model/EmailModel.vue'
import AuthPromptDialog from '@/components/model/AuthPromptDialog.vue'
import NoticePopupContainer from '@/components/notice/NoticePopupContainer.vue'

// 其他组件
import InitialPopup from '@/components/InitialPopup.vue'
import ChatRoom from '@/components/ChatRoom.vue'
import SakuraEffect from '@/components/SakuraEffect.vue'

// Stores
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useNoticeStore } from '@/stores/notice'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'
import { useNoticeSocket } from '@/composables/useNoticeSocket'
import { getCurrentUser } from '@/api/user'
import { syncAuthenticatedSession } from '@/utils/authSession'
import { syncPageTheme } from '@/utils/theme/syncPageTheme'

// API
import { getBlogInfo, reportVisitor } from '@/api/site'

const route = useRoute()
const router = useRouter()
const theme = useTheme()
const blogInfoStore = useBlogInfoStore()
const noticeStore = useNoticeStore()
const uiStore = useUIStore()
const userStore = useUserStore()
const { start: startNoticeSocket, disconnect: disconnectNoticeSocket } = useNoticeSocket()
const standaloneAuthRoutes = new Set(['/auth', '/login', '/register', '/forgot-password'])
const isStandaloneAuth = computed(() => route.meta.layout === 'auth')
const isStartPage = computed(() => route.path === '/home/start')
const hideFloatingChatRoom = computed(() => route.path === '/message' || route.path === '/chat')

function redirectToAuthPage(
  mode: 'login' | 'register' | 'forgot-password',
  resetFlag: (value: boolean) => void
) {
  const currentRedirect =
    typeof route.query.redirect === 'string'
      ? route.query.redirect
      : standaloneAuthRoutes.has(route.path)
        ? uiStore.loginUrl || '/'
        : route.fullPath

  if (!standaloneAuthRoutes.has(route.path)) {
    uiStore.saveLoginUrl(route.fullPath)
  }

  if (route.path === '/auth') {
    return
  }

  resetFlag(false)
  router.push({
    path: '/auth',
    query: currentRedirect ? { mode, redirect: currentRedirect } : { mode }
  })
}

// 初始化
onMounted(async () => {
  try {
    // 获取博客信息
    const { data } = await getBlogInfo()
    if (data.data) {
      blogInfoStore.setBlogInfo(data.data)
    }
    // 上报访客信息
    await reportVisitor()
  } catch (error) {
    console.error('初始化失败:', error)
  }

  // 站点配置到位后先校验后端 Session，再启动通知连接，避免旧本地用户态误触发过期提示。
  const hasValidSession = await syncAuthenticatedSession({
    isLoggedIn: userStore.isLoggedIn,
    login: userStore.login,
    logout: userStore.logout,
    getCurrentUser
  })
  startNoticeSocket()
  if (hasValidSession) {
    await noticeStore.fetchUnreadCount()
    noticeStore.enqueueUnreadSummaryIfNeeded()
  }

  // 同步主题背景
  syncPageTheme(theme.global.current.value.dark)
})

onUnmounted(() => {
  disconnectNoticeSocket()
})

// 监听主题变化
watch(
  () => theme.global.current.value.dark,
  (isDark) => {
    syncPageTheme(isDark)
  }
)

watch(
  () => uiStore.loginFlag,
  (value) => {
    if (value) {
      redirectToAuthPage('login', uiStore.setLoginFlag)
    }
  }
)

watch(
  () => uiStore.registerFlag,
  (value) => {
    if (value) {
      redirectToAuthPage('register', uiStore.setRegisterFlag)
    }
  }
)

watch(
  () => uiStore.forgetFlag,
  (value) => {
    if (value) {
      redirectToAuthPage('forgot-password', uiStore.setForgetFlag)
    }
  }
)

watch(
  () => userStore.userId,
  async (userId, previousUserId) => {
    if (userId) {
      startNoticeSocket()
      await noticeStore.fetchUnreadCount()
      noticeStore.enqueueUnreadSummaryIfNeeded()
      return
    }

    if (previousUserId !== null) {
      noticeStore.resetUnreadSummarySessionFlag(previousUserId)
      disconnectNoticeSocket()
      noticeStore.resetState()
    }
  }
)

</script>

<style>
/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.auth-main {
  padding-top: 0 !important;
  min-height: 100vh;
}

.auth-main .auth-screen {
  width: 100%;
  min-height: 100vh;
}

.start-main {
  padding-top: 0 !important;
  min-height: 100vh;
}

.start-main .start-page {
  min-height: 100vh;
}

::view-transition-old(root),
::view-transition-new(root) {
  animation: none;
  mix-blend-mode: normal;
}

::view-transition-old(root) {
  z-index: 1;
}

::view-transition-new(root) {
  z-index: 2;
}
</style>
