<template>
  <v-app-bar
    :class="[
      navClass,
      { 'nav-albums-overlay': isAlbumsPage, 'nav-love-overlay': isLovePage, 'nav-letter-overlay': isLetterPage }
    ]"
    flat
    height="60"
  >
    <!-- 手机端导航栏 -->
    <div class="d-md-none nav-mobile-container">
      <div style="font-size: 18px; font-weight: bold">
        <router-link to="/">
          {{ websiteConfig.websiteAuthor }}
        </router-link>
      </div>
      <div style="margin-left: auto">
        <a @click="openSearch"><v-icon>mdi-magnify</v-icon></a>
        <a @click="openDrawer" style="margin-left: 10px; font-size: 20px">
          <v-icon>mdi-menu</v-icon>
        </a>
      </div>
    </div>
    <!-- 电脑导航栏 -->
    <div class="d-md-block d-none nav-container">
      <div class="float-left blog-title">
        <router-link to="/">
          {{ websiteConfig.websiteAuthor }}
        </router-link>
      </div>
      <div class="float-right nav-title">
        <div class="menus-item">
          <a class="menu-btn" @click="openSearch">
            <v-icon size="18">mdi-magnify</v-icon> 搜索
          </a>
        </div>
        <div class="menus-item">
          <a class="menu-btn">
            <v-icon size="18">mdi-home</v-icon> 首页
            <v-icon size="14">mdi-chevron-down</v-icon>
          </a>
          <ul class="menus-submenu">
            <li>
              <router-link to="/">
                <v-icon size="16">mdi-home-city</v-icon> 博客
              </router-link>
            </li>
            <li>
              <router-link to="/home/start">
                <v-icon size="16">mdi-magnify</v-icon> 起始页
              </router-link>
            </li>
            <li>
              <router-link to="/home/content">
                <v-icon size="16">mdi-folder-open</v-icon> 内容
              </router-link>
            </li>
            <li>
              <router-link to="/home/columns">
                <v-icon size="16">mdi-tag-multiple</v-icon> 专栏
              </router-link>
            </li>
          </ul>
        </div>
        <div class="menus-item">
          <a class="menu-btn">
            <v-icon size="18">mdi-compass</v-icon> 发现
            <v-icon size="14">mdi-chevron-down</v-icon>
          </a>
          <ul class="menus-submenu">
            <li>
              <router-link to="/archives">
                <v-icon size="16">mdi-archive</v-icon> 归档
              </router-link>
            </li>
            <li>
              <router-link to="/categories">
                <v-icon size="16">mdi-folder</v-icon> 分类
              </router-link>
            </li>
            <li>
              <router-link to="/tags">
                <v-icon size="16">mdi-tag</v-icon> 标签
              </router-link>
            </li>
          </ul>
        </div>
        <div class="menus-item">
          <a class="menu-btn">
            <v-icon size="18">mdi-gamepad-variant</v-icon> 娱乐
            <v-icon size="14">mdi-chevron-down</v-icon>
          </a>
          <ul class="menus-submenu">
            <li>
              <router-link to="/albums">
                <v-icon size="16">mdi-image-multiple</v-icon> 相册
              </router-link>
            </li>
            <li>
              <router-link to="/talks">
                <v-icon size="16">mdi-comment-text</v-icon> 说说
              </router-link>
            </li>
            <li v-if="isChatRoomEnabled">
              <router-link to="/chat">
                <v-icon size="16">mdi-chat</v-icon> 聊天室
              </router-link>
            </li>
          </ul>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/love">
            <v-icon size="18">mdi-heart-pulse</v-icon> Love
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/links">
            <v-icon size="18">mdi-link</v-icon> 友链
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/about">
            <v-icon size="18">mdi-airplane</v-icon> 关于
          </router-link>
        </div>
        <div class="menus-item">
          <router-link class="menu-btn" to="/message">
            <v-icon size="18">mdi-comment-multiple</v-icon> 留言
          </router-link>
        </div>
        <div class="menus-item">
          <a
            :href="websiteConfig.websiteBgAddress"
            class="menu-btn"
            target="_blank"
          >
            <v-icon size="18">mdi-view-dashboard</v-icon> 后台
          </a>
        </div>
        <div class="menus-item">
          <a class="menu-btn" v-if="!userStore.avatar" @click="openLogin">
            <v-icon size="18">mdi-login</v-icon> 登录
          </a>
          <template v-else>
            <div class="user-avatar-wrapper">
              <img
                class="user-avatar"
                :src="userStore.avatar"
                height="30"
                width="30"
              />
              <span v-if="hasNoticeBadge" class="user-avatar-badge">
                {{ noticeBadgeText }}
              </span>
            </div>
            <ul class="menus-submenu">
              <li>
                <router-link class="menu-link-with-badge" to="/user">
                  <span class="menu-link-with-badge__label">
                    <v-icon size="16">mdi-account</v-icon> 个人中心
                  </span>
                  <span v-if="hasNoticeBadge" class="menu-link-with-badge__dot" />
                </router-link>
              </li>
              <li>
                <a @click="handleLogout">
                  <v-icon size="16">mdi-logout</v-icon> 退出
                </a>
              </li>
            </ul>
          </template>
        </div>
      </div>
    </div>
  </v-app-bar>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from 'vuetify'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'
import { useNoticeStore } from '@/stores/notice'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { logout } from '@/api/user'
import { useToast } from '@/composables/useToast'
import { createScrollFrameScheduler } from '@/utils/scrollFrame'
import { getTopNavBaseClass } from './topNavBehavior'

const router = useRouter()
const route = useRoute()
const theme = useTheme()
const uiStore = useUIStore()
const userStore = useUserStore()
const noticeStore = useNoticeStore()
const blogInfoStore = useBlogInfoStore()

const navClass = ref('nav')
const lastScrollTop = ref(0)
const isNavHidden = ref(false)
const NAV_HIDE_START = 120
const NAV_SCROLL_DELTA = 8
const navScrollScheduler = createScrollFrameScheduler(updateNavOnScroll)

const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig || {})
const isDark = computed(() => theme.global.current.value.dark)
const isChatRoomEnabled = computed(() => Number(websiteConfig.value.isChatRoom) === 1)
// 头像角标直接复用通知仓库的未读数，避免顶部导航和通知页出现两套不同步状态。
const hasNoticeBadge = computed(() => userStore.isLoggedIn && noticeStore.unreadCount > 0)
// 未读数过大时统一折叠为 99+，防止角标宽度把头像布局撑开。
const noticeBadgeText = computed(() => {
  const unreadCount = noticeStore.unreadCount
  return unreadCount > 99 ? '99+' : String(unreadCount)
})

/**
 * 是否为相册相关页面（/albums、/albums/:id）
 *
 * 用于让导航栏在相册页采用「半透明黑」专用样式，
 * 与其他页面的默认透明 / 滚动后白底/暗底样式区分开。
 * 用 startsWith 是为了同时覆盖相册主页和单相册详情页两种场景。
 */
const isAlbumsPage = computed(() => route.path.startsWith('/albums'))
const isLovePage = computed(() => route.path.startsWith('/love'))
const isLetterPage = computed(() => route.path.startsWith('/letter'))
const isOverlayRoute = computed(() => {
  return (
    route.path === '/' ||
    route.path === '/home/start' ||
    route.path.startsWith('/albums') ||
    route.path.startsWith('/love') ||
    route.path.startsWith('/letter')
  )
})

function getNavBaseClass(scrollTop: number) {
  return getTopNavBaseClass({
    isDark: isDark.value,
    isOverlayRoute: isOverlayRoute.value,
    scrollTop
  })
}

function updateNavOnScroll() {
  const scrollTop =
    window.pageYOffset ||
    document.documentElement.scrollTop ||
    document.body.scrollTop
  const normalizedScrollTop = Math.max(scrollTop, 0)
  const scrollDelta = normalizedScrollTop - lastScrollTop.value

  // 顶部区域始终展示导航栏，避免首屏向上滚动时出现空白状态。
  if (normalizedScrollTop <= 60) {
    isNavHidden.value = false
  } else if (Math.abs(scrollDelta) > NAV_SCROLL_DELTA) {
    // 向下滚动隐藏导航栏，向上滚动恢复导航栏。
    isNavHidden.value = scrollDelta > 0 && normalizedScrollTop > NAV_HIDE_START
  }

  lastScrollTop.value = normalizedScrollTop
  const baseClass = getNavBaseClass(normalizedScrollTop)
  const nextNavClass = isNavHidden.value ? `${baseClass} nav-hidden` : baseClass

  if (nextNavClass !== navClass.value) {
    navClass.value = nextNavClass
  }
}

function openSearch() {
  uiStore.setSearchFlag(true)
}

function openDrawer() {
  uiStore.setDrawer(true)
}

function openLogin() {
  uiStore.saveLoginUrl(router.currentRoute.value.fullPath)
  router.push({
    path: '/login',
    query: { redirect: router.currentRoute.value.fullPath }
  })
}

async function handleLogout() {
  if (router.currentRoute.value.path === '/user') {
    router.go(-1)
  }

  try {
    const { data } = await logout()
    if (data.flag) {
      userStore.logout()
      useToast({ type: 'success', message: '注销成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '注销失败' })
  }
}

onMounted(() => {
  navScrollScheduler.runNow()
  window.addEventListener('scroll', navScrollScheduler.requestUpdate, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', navScrollScheduler.requestUpdate)
  navScrollScheduler.cancel()
})

watch(isDark, () => {
  navScrollScheduler.runNow()
})

watch(
  () => route.fullPath,
  () => {
    lastScrollTop.value = 0
    isNavHidden.value = false
    navScrollScheduler.runNow()
  }
)
</script>

<style scoped>
ul {
  list-style: none;
}

:deep(.v-app-bar__content) {
  overflow: visible !important;
}

:deep(.v-toolbar__content) {
  overflow: visible !important;
  padding-right: 34px !important;
  padding-left: 34px !important;
}

.v-app-bar {
  overflow: visible !important;
  transition: transform 0.28s ease, background 0.28s ease, box-shadow 0.28s ease;
  will-change: transform;
}

.nav {
  background: rgba(0, 0, 0, 0) !important;
  z-index: 999 !important;
  overflow: visible !important;
}

.nav a {
  color: #eee !important;
}

.nav .menu-btn {
  text-shadow: 0.05rem 0.05rem 0.1rem rgba(0, 0, 0, 0.3);
}

.nav .blog-title a {
  text-shadow: 0.1rem 0.1rem 0.2rem rgba(0, 0, 0, 0.15);
}

.nav-hidden {
  transform: translateY(-110%) !important;
}

.nav-fixed-light {
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 5px 6px -5px rgba(133, 133, 133, 0.6);
}

.nav-fixed-dark {
  background:
    linear-gradient(
      180deg,
      rgba(18, 23, 32, 0.92) 0%,
      rgba(15, 20, 28, 0.9) 100%
    ) !important;
  border-bottom: 1px solid rgba(186, 200, 224, 0.18);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    0 18px 42px rgba(0, 0, 0, 0.34);
  backdrop-filter: blur(18px) saturate(130%);
}

.nav-fixed-dark a {
  color: rgba(241, 246, 255, 0.9) !important;
}

.nav-fixed-light a {
  color: #4c4948 !important;
}

/*
 * 相册页（/albums、/albums/:id）专用导航栏样式
 *
 * 设计目标：相册页是图片为主的视觉场景，浅色或纯透明导航栏会和缤纷的封面冲突，
 *          统一改成「半透明黑」可以让任何配色的封面都有清晰的对比度，
 *          同时保留一定透明度让背景图能微微透出。
 *
 * 实现说明：
 *   - 同时列出 .nav / .nav-fixed-light / .nav-fixed-dark 三种状态，
 *     是为了在「未滚动 / 滚动后浅色 / 滚动后暗色」三种 JS 控制的状态下都覆盖背景。
 *   - 用 !important 是因为原有 .nav-fixed-light/dark 也用了 !important，
 *     不加优先级不够，背景会被原样式覆盖。
 *   - backdrop-filter 加一点模糊，避免半透明黑下面文字依然可见时显得突兀。
 */
.nav-albums-overlay,
.nav-albums-overlay.nav,
.nav-albums-overlay.nav-fixed-light,
.nav-albums-overlay.nav-fixed-dark,
.nav-letter-overlay,
.nav-letter-overlay.nav,
.nav-letter-overlay.nav-fixed-light,
.nav-letter-overlay.nav-fixed-dark {
  background: rgba(0, 0, 0, 0.45) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(8px) saturate(120%);
}

/* 半透明黑背景下，所有链接 / 按钮文字统一用近白色，保证对比度 */
.nav-albums-overlay a,
.nav-letter-overlay a {
  color: rgba(255, 255, 255, 0.92) !important;
}

/* hover 高亮色与原导航保持一致（蓝色），仅在相册页定向覆盖一次即可 */
.nav-albums-overlay .menus-item > .menu-btn:hover,
.nav-albums-overlay .menus-item:hover > .menu-btn,
.nav-albums-overlay .blog-title a:hover,
.nav-letter-overlay .menus-item > .menu-btn:hover,
.nav-letter-overlay .menus-item:hover > .menu-btn,
.nav-letter-overlay .blog-title a:hover {
  color: #8bc5ff !important;
}

/* 给文字加一点阴影，避免遇到极亮的封面时白字看不清 */
.nav-albums-overlay .menu-btn,
.nav-albums-overlay .blog-title a,
.nav-letter-overlay .menu-btn,
.nav-letter-overlay .blog-title a {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
}

/*
 * 纪念页专用导航栏样式
 *
 * 目的：让纪念页顶部导航和背景图融合成同一层视觉，而不是像普通白底页面那样分离。
 * 这里仅在 /love 页面覆盖导航栏的背景、边框和文字阴影，不影响其他页面。
 */
.nav-love-overlay,
.nav-love-overlay.nav,
.nav-love-overlay.nav-fixed-light,
.nav-love-overlay.nav-fixed-dark {
  background: linear-gradient(180deg, rgba(12, 18, 22, 0.18), rgba(12, 18, 22, 0.04)) !important;
  border-bottom: 0;
  box-shadow: none;
  backdrop-filter: blur(3px) saturate(108%);
}

.nav-love-overlay a {
  color: rgba(255, 248, 241, 0.95) !important;
}

.nav-love-overlay .menu-btn,
.nav-love-overlay .blog-title a {
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.28);
}

.nav-love-overlay .menus-submenu a {
  color: #4c4948 !important;
  text-shadow: none;
}

.nav-fixed .menus-item a,
.nav-fixed .blog-title a {
  text-shadow: none;
}

.nav-container {
  font-size: 14px;
  width: 100%;
  height: 100%;
  overflow: visible !important;
}

.nav-mobile-container {
  width: 100%;
  display: flex;
  align-items: center;
}

.blog-title,
.nav-title {
  display: flex;
  align-items: center;
  height: 100%;
  overflow: visible !important;
}

.blog-title a {
  font-size: 18px;
  font-weight: bold;
}

.menus-item {
  position: relative;
  display: inline-block;
  margin: 0 0 0 0.875rem;
  z-index: 100;
  overflow: visible !important;
}

.menus-item a {
  transition: all 0.2s;
  cursor: pointer;
  text-decoration: none;
}

.nav-fixed-dark .menus-item > .menu-btn:hover,
.nav-fixed-dark .menus-item:hover > .menu-btn,
.nav-fixed-dark .blog-title a:hover {
  color: #8bc5ff !important;
}

.nav-fixed .menu-btn:hover {
  color: #49b1f5 !important;
}

.menu-btn:hover:after {
  width: 100%;
}

.menus-item a:after {
  position: absolute;
  bottom: -5px;
  left: 0;
  z-index: -1;
  width: 0;
  height: 3px;
  background-color: #80c8f8;
  content: "";
  transition: all 0.3s ease-in-out;
}

.user-avatar {
  cursor: pointer;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.18);
}

.user-avatar-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.user-avatar-badge {
  position: absolute;
  top: -4px;
  right: -7px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  box-shadow: 0 0 0 2px rgba(18, 23, 32, 0.92);
}

.menus-item:hover .menus-submenu {
  display: block;
}

.menus-submenu {
  position: absolute;
  display: none;
  right: 0;
  top: 100%;
  width: max-content;
  margin-top: 8px;
  box-shadow: 0 5px 20px -4px rgba(0, 0, 0, 0.5);
  background-color: #fff;
  border-radius: 8px;
  animation: submenu 0.3s 0.1s ease both;
  padding: 8px 0;
  z-index: 1000;
}

:global(.dark) .menus-submenu {
  background:
    linear-gradient(
      180deg,
      rgba(35, 43, 58, 0.98) 0%,
      rgba(24, 29, 39, 0.98) 100%
    );
  border: 1px solid rgba(186, 200, 224, 0.18);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    0 24px 46px rgba(0, 0, 0, 0.38);
  backdrop-filter: blur(18px) saturate(125%);
}

.menus-submenu:before {
  position: absolute;
  top: -8px;
  left: 0;
  width: 100%;
  height: 20px;
  content: "";
}

.menus-submenu a {
  line-height: 2;
  color: #4c4948 !important;
  text-shadow: none;
  display: block;
  padding: 6px 14px;
}

.menu-link-with-badge {
  display: flex !important;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.menu-link-with-badge__label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.menu-link-with-badge__dot {
  flex-shrink: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff4d4f;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.16);
}

:global(.dark) .menus-submenu a {
  color: rgba(236, 242, 252, 0.92) !important;
}

:global(.dark) .menu-link-with-badge__dot {
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.22);
}

.menus-submenu a:hover {
  background: #4ab1f4;
  color: #fff !important;
}

:global(.dark) .menus-submenu a:hover {
  background: linear-gradient(135deg, rgba(87, 163, 232, 0.92), rgba(90, 125, 255, 0.92));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

:global(.dark) .user-avatar-badge {
  box-shadow: 0 0 0 2px rgba(24, 29, 39, 0.98);
}

@keyframes submenu {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (min-width: 1200px) {
  :deep(.v-toolbar__content) {
    padding-right: 48px !important;
    padding-left: 48px !important;
  }
}

@media (max-width: 759px) {
  :deep(.v-toolbar__content) {
    padding-right: 16px !important;
    padding-left: 16px !important;
  }
}
</style>
