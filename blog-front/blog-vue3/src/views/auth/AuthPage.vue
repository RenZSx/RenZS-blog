<!--
  功能说明：Auth 页面统一承载登录、注册和找回密码表单。
  作者：ChenFY
  创建时间：2026-06-17
  用途概述：全屏沉浸式认证页——站点封面铺底，表单以玻璃质感直接悬浮于背景之上。
-->
<template>
  <main class="auth-screen" @animationstart="handleAnimationStart">
    <!-- 背景：站点封面图打底 + 深色遮罩 + 光斑 -->
    <div class="auth-bg" aria-hidden="true">
      <div class="auth-bg-cover" :style="coverStyle" />
      <div class="auth-bg-veil" />
      <div class="auth-orb auth-orb-one" />
      <div class="auth-orb auth-orb-two" />
      <div class="auth-orb auth-orb-three" />
    </div>

    <!-- 页面顶部：品牌 + 返回首页 -->
    <header class="auth-top">
      <router-link to="/" class="auth-brand">
        <v-avatar size="40" class="auth-brand-avatar">
          <v-img :src="websiteConfig.websiteAvatar || fallbackAvatar" />
        </v-avatar>
        <span class="auth-brand-name">{{ brandName }}</span>
      </router-link>

      <v-btn class="auth-home-btn" variant="text" size="small" prepend-icon="mdi-arrow-left" @click="goHome">
        返回首页
      </v-btn>
    </header>

    <!-- 中央表单区 -->
    <div class="auth-body">
      <nav class="auth-tabs" aria-label="认证方式">
        <button
          v-for="item in navItems"
          :key="item.key"
          type="button"
          class="auth-tab"
          :class="{ 'auth-tab-active': currentMode === item.key }"
          :aria-pressed="currentMode === item.key"
          @click="setCurrentMode(item.key)"
        >
          {{ item.label }}
        </button>
      </nav>

      <Transition name="auth-form-fade" mode="out-in">
        <div v-if="currentMode === 'login'" key="login" class="auth-pane">
          <h1 class="auth-title">{{ currentContent.title }}</h1>
          <p class="auth-subtitle">{{ currentContent.subtitle }}</p>

          <form class="auth-form" @submit.prevent="handleLogin">
            <v-text-field
              v-model="loginUsername"
              label="邮箱号"
              placeholder="name@example.com"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              inputmode="email"
              prepend-inner-icon="mdi-email-outline"
              spellcheck="false"
              autocomplete="email"
            />

            <v-text-field
              v-model="loginPassword"
              label="密码"
              placeholder="请输入您的密码"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              prepend-inner-icon="mdi-lock-outline"
              autocomplete="current-password"
              :append-inner-icon="showLoginPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showLoginPassword ? 'text' : 'password'"
              @click:append-inner="showLoginPassword = !showLoginPassword"
            />

            <div class="auth-form-row">
              <button type="button" class="auth-inline-link" @click="setCurrentMode('forgot-password')">
                忘记密码？
              </button>
            </div>

            <v-btn class="auth-submit" block size="large" :loading="loginSubmitting" type="submit">
              {{ currentContent.actionLabel }}
            </v-btn>
          </form>

          <!-- 社交登录只属于登录模式，避免注册和找回密码流程被额外入口打断。 -->
          <div v-if="socialLoginList.length > 0" class="auth-social">
            <div class="auth-social-title">或者用社交账号继续</div>
            <div class="auth-social-list">
              <button v-if="showLogin('qq')" type="button" class="auth-social-btn" aria-label="使用 QQ 登录" @click="qqLogin">
                <span class="iconfont iconqq auth-social-icon" style="color:#4aa8ff" aria-hidden="true" />
                <span>QQ</span>
              </button>
              <button
                v-if="showLogin('gitee')"
                type="button"
                class="auth-social-btn"
                aria-label="使用 Gitee 登录"
                @click="giteeLogin"
              >
                <span class="iconfont icongitee-fill-round auth-social-icon" style="color:#ff6b5e" aria-hidden="true" />
                <span>Gitee</span>
              </button>
              <button
                v-if="showLogin('weibo')"
                type="button"
                class="auth-social-btn"
                aria-label="使用微博登录"
                @click="weiboLogin"
              >
                <span class="iconfont iconweibo auth-social-icon" style="color:#ff8073" aria-hidden="true" />
                <span>微博</span>
              </button>
            </div>
          </div>
        </div>

        <div v-else-if="currentMode === 'register'" key="register" class="auth-pane">
          <h1 class="auth-title">{{ currentContent.title }}</h1>
          <p class="auth-subtitle">{{ currentContent.subtitle }}</p>

          <form class="auth-form" @submit.prevent="handleRegister">
            <v-alert v-if="!emailRegisterEnabled" type="warning" variant="tonal" density="comfortable">
              邮箱注册已关闭
            </v-alert>

            <v-text-field
              v-model="registerUsername"
              label="邮箱号"
              placeholder="name@example.com"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              inputmode="email"
              prepend-inner-icon="mdi-email-outline"
              spellcheck="false"
              autocomplete="email"
            />

            <v-text-field
              v-model="registerNickname"
              label="昵称"
              placeholder="给自己起一个名字"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              prepend-inner-icon="mdi-account-outline"
              autocomplete="nickname"
            />

            <v-text-field
              v-model="registerPassword"
              label="密码"
              placeholder="至少 6 位"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              prepend-inner-icon="mdi-lock-outline"
              autocomplete="new-password"
              :append-inner-icon="showRegisterPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showRegisterPassword ? 'text' : 'password'"
              @click:append-inner="showRegisterPassword = !showRegisterPassword"
            />

            <div class="code-wrapper">
              <v-text-field
                v-model="registerCode"
                label="验证码"
                placeholder="请输入 6 位验证码"
                variant="solo"
                elevation="0"
                density="comfortable"
                hide-details="auto"
                inputmode="numeric"
                maxlength="6"
                prepend-inner-icon="mdi-shield-key-outline"
                spellcheck="false"
                autocomplete="one-time-code"
              />
              <v-btn
                class="code-btn"
                variant="tonal"
                :disabled="!emailRegisterEnabled || registerCountdown > 0 || registerSendingCode"
                :loading="registerSendingCode"
                @click="sendRegisterCode"
              >
                {{ registerCountdown > 0 ? `${registerCountdown}s` : '发送验证码' }}
              </v-btn>
            </div>

            <v-btn
              class="auth-submit"
              block
              size="large"
              :disabled="!emailRegisterEnabled"
              :loading="registerSubmitting"
              type="submit"
            >
              {{ currentContent.actionLabel }}
            </v-btn>
          </form>
        </div>

        <div v-else key="forgot-password" class="auth-pane">
          <h1 class="auth-title">{{ currentContent.title }}</h1>
          <p class="auth-subtitle">{{ currentContent.subtitle }}</p>

          <form class="auth-form" @submit.prevent="handleReset">
            <v-text-field
              v-model="forgotUsername"
              label="邮箱号"
              placeholder="name@example.com"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              inputmode="email"
              prepend-inner-icon="mdi-email-outline"
              spellcheck="false"
              autocomplete="email"
            />

            <div class="code-wrapper">
              <v-text-field
                v-model="forgotCode"
                label="验证码"
                placeholder="请输入 6 位验证码"
                variant="solo"
                elevation="0"
                density="comfortable"
                hide-details="auto"
                inputmode="numeric"
                maxlength="6"
                prepend-inner-icon="mdi-shield-key-outline"
                spellcheck="false"
                autocomplete="one-time-code"
              />
              <v-btn
                class="code-btn"
                variant="tonal"
                :disabled="forgotCountdown > 0 || forgotSendingCode"
                :loading="forgotSendingCode"
                @click="sendForgotCode"
              >
                {{ forgotCountdown > 0 ? `${forgotCountdown}s` : '发送验证码' }}
              </v-btn>
            </div>

            <v-text-field
              v-model="forgotPassword"
              label="新密码"
              placeholder="请输入新密码"
              variant="solo"
              elevation="0"
              density="comfortable"
              hide-details="auto"
              prepend-inner-icon="mdi-lock-reset"
              autocomplete="new-password"
              :append-inner-icon="showForgotPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showForgotPassword ? 'text' : 'password'"
              @click:append-inner="showForgotPassword = !showForgotPassword"
            />

            <v-btn class="auth-submit" block size="large" :loading="forgotSubmitting" type="submit">
              {{ currentContent.actionLabel }}
            </v-btn>
          </form>
        </div>
      </Transition>

      <p class="auth-footer-note">
        {{ currentContent.footerLead }}
        <button type="button" class="auth-inline-link" @click="setCurrentMode(currentContent.footerTarget)">
          {{ currentContent.footerAction }}
        </button>
      </p>
    </div>

    <!-- 底部：站点数据与备案 -->
    <footer class="auth-bottom">
      <ul class="auth-stats">
        <li v-for="stat in siteStats" :key="stat.key" class="auth-stat">
          <span class="auth-stat-value">{{ stat.value }}</span>
          <span class="auth-stat-label">{{ stat.label }}</span>
        </li>
      </ul>
      <p v-if="websiteConfig.websiteRecordNo" class="auth-beian">
        {{ websiteConfig.websiteRecordNo }}
      </p>
    </footer>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useUIStore } from '@/stores/ui'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { login, register, sendCode as sendCodeApi, updatePassword } from '@/api/user'
import { useToast } from '@/composables/useToast'
import { normalizeAuthRedirect } from '@/utils/authPrompt'
import { saveOauthMode } from '@/utils/oauthMode'
import config from '@/assets/js/config'
import {
  authNavItems,
  getAuthModeContent,
  normalizeAuthMode,
  type AuthMode
} from './authPageContent'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const uiStore = useUIStore()
const blogInfoStore = useBlogInfoStore()

const loginUsername = ref('')
const loginPassword = ref('')
const showLoginPassword = ref(false)
const loginSubmitting = ref(false)

const registerUsername = ref('')
const registerNickname = ref('')
const registerPassword = ref('')
const registerCode = ref('')
const showRegisterPassword = ref(false)
const registerCountdown = ref(0)
const registerSendingCode = ref(false)
const registerSubmitting = ref(false)
let registerCountdownTimer: ReturnType<typeof setInterval> | null = null

const forgotUsername = ref('')
const forgotCode = ref('')
const forgotPassword = ref('')
const showForgotPassword = ref(false)
const forgotCountdown = ref(0)
const forgotSendingCode = ref(false)
const forgotSubmitting = ref(false)
let forgotCountdownTimer: ReturnType<typeof setInterval> | null = null

const currentMode = ref<AuthMode>(normalizeAuthMode(route.query.mode))

const websiteConfig = computed(() => blogInfoStore.blogInfo.websiteConfig)
const fallbackAvatar = computed(() => websiteConfig.value.touristAvatar || '')
const socialLoginList = computed(() => websiteConfig.value.socialLoginList || [])
const emailRegisterEnabled = computed(() => {
  return Number(websiteConfig.value.isEmailRegister ?? 1) === 1
})
// 当前模式文案由独立 helper 提供，避免模板内堆叠模式判断。
const currentContent = computed(() => getAuthModeContent(currentMode.value))
const navItems = authNavItems
const brandName = computed(() => {
  return websiteConfig.value.websiteAuthor || websiteConfig.value.websiteName || 'Renzs Blog'
})
const siteStats = computed(() => [
  { key: 'articles', label: '文章', value: blogInfoStore.blogInfo.articleCount ?? 0 },
  { key: 'categories', label: '分类', value: blogInfoStore.blogInfo.categoryCount ?? 0 },
  { key: 'tags', label: '标签', value: blogInfoStore.blogInfo.tagCount ?? 0 }
])

// 背景优先使用站点配置的首页封面，保证认证页与主站视觉一致。
const coverStyle = computed(() => {
  const cover = blogInfoStore.getPageCover('home') || blogInfoStore.getPageCover('about')
  return cover ? { backgroundImage: `url(${cover})` } : undefined
})

function getRedirectTarget() {
  return normalizeAuthRedirect(
    typeof route.query.redirect === 'string' ? route.query.redirect : uiStore.loginUrl
  )
}

function goHome() {
  const redirect = getRedirectTarget()
  if (redirect && redirect !== '/') {
    router.replace(redirect)
    return
  }
  router.replace('/')
}

function setCurrentMode(mode: AuthMode) {
  currentMode.value = mode
  if (mode === 'login') {
    uiStore.setLoginFlag(false)
    return
  }
  if (mode === 'register') {
    uiStore.setRegisterFlag(false)
    return
  }
  uiStore.setForgetFlag(false)
}

function isEmail(value: string) {
  return /^[A-Za-z0-9一-龥]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(value)
}

/*
 * Chrome/Edge 自动填充时以动画事件（onautofillstart）而非 input 事件通知页面，
 * Vuetify 感知不到值变化，浮动标签停留在原位与填充文字重叠。
 * 通过在 autofill 专属动画上补发 input 事件，让 Vuetify 正常收起标签。
 */
function handleAnimationStart(event: AnimationEvent) {
  const target = event.target as HTMLInputElement | null
  if (
    !target ||
    target.tagName !== 'INPUT' ||
    event.animationName !== 'onautofillstart' ||
    target.dataset.autofillSynced === 'true'
  ) {
    return
  }
  target.dataset.autofillSynced = 'true'
  target.dispatchEvent(new Event('input', { bubbles: true }))
}

function startCountdown(target: typeof registerCountdown, timerSetter: (timer: ReturnType<typeof setInterval> | null) => void) {
  target.value = 60
  const timer = setInterval(() => {
    target.value -= 1
    if (target.value <= 0) {
      clearInterval(timer)
      timerSetter(null)
    }
  }, 1000)
  timerSetter(timer)
}

function clearRegisterTimer() {
  if (registerCountdownTimer) {
    clearInterval(registerCountdownTimer)
    registerCountdownTimer = null
  }
}

function clearForgotTimer() {
  if (forgotCountdownTimer) {
    clearInterval(forgotCountdownTimer)
    forgotCountdownTimer = null
  }
}

function showLogin(type: string) {
  return socialLoginList.value.includes(type)
}

function goAfterLogin() {
  const redirect = getRedirectTarget()
  uiStore.saveLoginUrl('')
  router.replace(redirect)
}

async function handleLogin() {
  if (!isEmail(loginUsername.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }

  if (!loginPassword.value.trim()) {
    useToast({ type: 'error', message: '密码不能为空' })
    return
  }

  loginSubmitting.value = true
  try {
    const { data } = await login({ username: loginUsername.value, password: loginPassword.value })
    if (data.flag) {
      userStore.login(data.data)
      loginUsername.value = ''
      loginPassword.value = ''
      useToast({ type: 'success', message: '登录成功' })
      goAfterLogin()
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch {
    useToast({ type: 'error', message: '登录失败' })
  } finally {
    loginSubmitting.value = false
  }
}

async function sendRegisterCode() {
  if (!emailRegisterEnabled.value) {
    useToast({ type: 'warning', message: '邮箱注册已关闭' })
    return
  }

  if (!isEmail(registerUsername.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }

  registerSendingCode.value = true
  try {
    const { data } = await sendCodeApi(registerUsername.value)
    if (data.flag) {
      useToast({ type: 'success', message: '验证码已发送' })
      clearRegisterTimer()
      startCountdown(registerCountdown, (timer) => {
        registerCountdownTimer = timer
      })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch {
    useToast({ type: 'error', message: '发送验证码失败' })
  } finally {
    registerSendingCode.value = false
  }
}

async function handleRegister() {
  if (!emailRegisterEnabled.value) {
    useToast({ type: 'warning', message: '邮箱注册已关闭' })
    return
  }

  if (!isEmail(registerUsername.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }
  if (!registerNickname.value.trim()) {
    useToast({ type: 'error', message: '昵称不能为空' })
    return
  }
  if (registerCode.value.trim().length !== 6) {
    useToast({ type: 'error', message: '请输入6位验证码' })
    return
  }
  if (registerPassword.value.trim().length < 6) {
    useToast({ type: 'error', message: '密码不能少于6位' })
    return
  }

  registerSubmitting.value = true
  try {
    const { data } = await register({
      username: registerUsername.value,
      password: registerPassword.value,
      code: registerCode.value,
      nickname: registerNickname.value
    })

    if (!data.flag) {
      useToast({ type: 'error', message: data.message })
      return
    }

    const loginResult = await login({
      username: registerUsername.value,
      password: registerPassword.value
    })

    if (loginResult.data.flag) {
      userStore.login(loginResult.data.data)
      uiStore.saveLoginUrl('')
      useToast({ type: 'success', message: '注册并登录成功' })
      router.replace(getRedirectTarget())
    } else {
      useToast({ type: 'success', message: '注册成功，请登录' })
      loginUsername.value = registerUsername.value
      setCurrentMode('login')
    }
  } catch {
    useToast({ type: 'error', message: '注册失败' })
  } finally {
    registerSubmitting.value = false
  }
}

async function sendForgotCode() {
  if (!isEmail(forgotUsername.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }

  forgotSendingCode.value = true
  try {
    const { data } = await sendCodeApi(forgotUsername.value)
    if (data.flag) {
      useToast({ type: 'success', message: '验证码已发送' })
      clearForgotTimer()
      startCountdown(forgotCountdown, (timer) => {
        forgotCountdownTimer = timer
      })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch {
    useToast({ type: 'error', message: '发送验证码失败' })
  } finally {
    forgotSendingCode.value = false
  }
}

async function handleReset() {
  if (!isEmail(forgotUsername.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }
  if (forgotCode.value.trim().length !== 6) {
    useToast({ type: 'error', message: '请输入6位验证码' })
    return
  }
  if (forgotPassword.value.trim().length < 6) {
    useToast({ type: 'error', message: '密码不能少于6位' })
    return
  }

  forgotSubmitting.value = true
  try {
    const { data } = await updatePassword({
      username: forgotUsername.value,
      password: forgotPassword.value,
      code: forgotCode.value
    })
    if (data.flag) {
      useToast({ type: 'success', message: '密码重置成功' })
      loginUsername.value = forgotUsername.value
      setCurrentMode('login')
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch {
    useToast({ type: 'error', message: '重置密码失败' })
  } finally {
    forgotSubmitting.value = false
  }
}

function qqLogin() {
  saveOauthMode({ provider: 'qq', mode: 'login' })
  uiStore.saveLoginUrl(getRedirectTarget())
  window.open(
    `https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=${config.QQ_APP_ID}&response_type=token&scope=all&redirect_uri=${config.QQ_REDIRECT_URI}`,
    '_self'
  )
}

function weiboLogin() {
  uiStore.saveLoginUrl(getRedirectTarget())
  window.open(
    `https://api.weibo.com/oauth2/authorize?client_id=${config.WEIBO_APP_ID}&response_type=code&redirect_uri=${config.WEIBO_REDIRECT_URI}`,
    '_self'
  )
}

function giteeLogin() {
  uiStore.saveLoginUrl(getRedirectTarget())
  window.open(
    `https://gitee.com/oauth/authorize?client_id=${config.GITEE_CLIENT_ID}&response_type=code&redirect_uri=${config.GITEE_REDIRECT_URI}`,
    '_self'
  )
}

watch(
  () => route.query.mode,
  (mode) => {
    currentMode.value = normalizeAuthMode(mode)
  },
  { immediate: true }
)

watch(
  () => route.query.username,
  (username) => {
    if (typeof username === 'string') {
      loginUsername.value = username
    }
  },
  { immediate: true }
)

watch(
  () => uiStore.loginFlag,
  (value) => {
    if (value) {
      setCurrentMode('login')
    }
  }
)

watch(
  () => uiStore.registerFlag,
  (value) => {
    if (value) {
      setCurrentMode('register')
    }
  }
)

watch(
  () => uiStore.forgetFlag,
  (value) => {
    if (value) {
      setCurrentMode('forgot-password')
    }
  }
)

onMounted(() => {
  uiStore.setLoginFlag(false)
  uiStore.setRegisterFlag(false)
  uiStore.setForgetFlag(false)
  if (userStore.isLoggedIn) {
    goAfterLogin()
  }
})

onBeforeUnmount(() => {
  clearRegisterTimer()
  clearForgotTimer()
})
</script>

<style scoped>
.auth-screen {
  position: relative;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  padding: 28px clamp(16px, 6vw, 64px) 24px;
  overflow: hidden;
  /* 深色打底兜底：封面图加载前也保证白色文字可读 */
  background: linear-gradient(180deg, #0d1a30 0%, #0a1425 100%);
  color: #f7fbff;
}

.auth-screen,
.auth-screen *,
.auth-screen *::before,
.auth-screen *::after {
  box-sizing: border-box;
}

/* ========== 背景层 ========== */
.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-bg-cover {
  position: absolute;
  inset: 0;
  background-position: center;
  background-size: cover;
}

.auth-bg-veil {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(1100px 560px at 16% 10%, rgba(47, 128, 237, 0.28), transparent 62%),
    radial-gradient(900px 600px at 86% 86%, rgba(22, 189, 178, 0.2), transparent 62%),
    linear-gradient(180deg, rgba(7, 14, 28, 0.42) 0%, rgba(6, 12, 25, 0.62) 55%, rgba(4, 10, 22, 0.78) 100%);
}

.auth-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(14px);
}

.auth-orb-one {
  top: 12%;
  left: 8%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(80, 158, 255, 0.3), transparent 68%);
  animation: auth-float 11s ease-in-out infinite;
}

.auth-orb-two {
  right: 10%;
  top: 20%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(150, 128, 255, 0.24), transparent 70%);
  animation: auth-float 13s ease-in-out 1.2s infinite reverse;
}

.auth-orb-three {
  right: -4%;
  bottom: -8%;
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, rgba(45, 208, 196, 0.22), transparent 72%);
  animation: auth-float 15s ease-in-out 0.6s infinite;
}

@keyframes auth-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }
  50% {
    transform: translate3d(0, -22px, 0);
  }
}

/* ========== 顶部品牌行 ========== */
.auth-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.auth-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.auth-brand-avatar {
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.auth-brand-name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}

.auth-home-btn {
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(12px);
  color: rgba(255, 255, 255, 0.92) !important;
  transition:
    background var(--transition-fast),
    border-color var(--transition-fast);
}

.auth-home-btn:hover {
  border-color: rgba(255, 255, 255, 0.42);
  background: rgba(255, 255, 255, 0.18) !important;
}

/* ========== 中央表单区 ========== */
.auth-body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
  padding: 32px 0;
  animation: auth-rise 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes auth-rise {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 模式切换：纯文字 + 渐变下划线 */
.auth-tabs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 34px;
}

.auth-tab {
  position: relative;
  padding: 6px 2px 10px;
  border: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.62);
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.04em;
  transition: color var(--transition-fast);
}

.auth-tab::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 0;
  height: 2px;
  border-radius: 2px;
  background: linear-gradient(90deg, #57a6ff, #3ad6c5);
  transform: translateX(-50%);
  transition: width 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-tab:hover {
  color: rgba(255, 255, 255, 0.88);
}

.auth-tab-active {
  color: #ffffff;
}

.auth-tab-active::after {
  width: 26px;
}

.auth-tab:focus-visible,
.auth-inline-link:focus-visible,
.auth-social-btn:focus-visible {
  outline: 3px solid rgba(125, 186, 255, 0.5);
  outline-offset: 3px;
  border-radius: 4px;
}

.auth-pane {
  width: 100%;
  margin-top: 26px;
}

.auth-title {
  margin: 0;
  font-size: clamp(28px, 3.2vw, 36px);
  font-weight: 800;
  line-height: 1.2;
  text-align: center;
  text-shadow: 0 2px 14px rgba(0, 0, 0, 0.35);
}

.auth-subtitle {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  line-height: 1.7;
  text-align: center;
}

/* ========== 表单 ========== */
.auth-form {
  margin-top: 26px;
}

.auth-form > * + * {
  margin-top: 14px;
}

/* 玻璃质感输入框：半透明 + 模糊，直接悬浮在封面图上 */
.auth-form :deep(.v-field) {
  min-height: 52px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px) saturate(140%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast),
    box-shadow var(--transition-fast);
}

.auth-form :deep(.v-field--focused) {
  border-color: rgba(140, 196, 255, 0.65);
  background: rgba(255, 255, 255, 0.16);
  box-shadow:
    0 0 0 4px rgba(87, 166, 255, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.16);
}

.auth-form :deep(.v-field__prepend-inner),
.auth-form :deep(.v-field__append-inner) {
  color: rgba(255, 255, 255, 0.72);
}

.auth-form :deep(.v-label) {
  color: rgba(255, 255, 255, 0.72);
  opacity: 1;
}

.auth-form :deep(.v-field--focused .v-label) {
  color: #ffffff;
}

.auth-form :deep(input) {
  color: #ffffff;
}

.auth-form :deep(input)::placeholder {
  color: rgba(255, 255, 255, 0.45);
  opacity: 1;
}

/*
 * 浏览器自动填充（Chrome/Safari/Edge）会给输入框强加浅色底和深色文字，
 * 破坏玻璃质感；用足够大的不透明内阴影盖掉默认底色，并保持白色文字。
 * 颜色取玻璃底色（白 10% 叠加在深色封面上）的近似不透明值。
 */
.auth-form :deep(input:-webkit-autofill),
.auth-form :deep(input:-webkit-autofill:hover),
.auth-form :deep(input:-webkit-autofill:focus),
.auth-form :deep(input:-webkit-autofill:active),
.auth-form :deep(input:autofill),
.auth-form :deep(input:autofill:hover),
.auth-form :deep(input:autofill:focus) {
  -webkit-text-fill-color: #ffffff;
  caret-color: #ffffff;
  -webkit-box-shadow: 0 0 0 1000px #202a44 inset;
  box-shadow: 0 0 0 1000px #202a44 inset;
  transition: background-color 9999s ease-out;
}

.auth-form :deep(.v-field--focused input:-webkit-autofill),
.auth-form :deep(.v-field--focused input:autofill) {
  -webkit-box-shadow: 0 0 0 1000px #273352 inset;
  box-shadow: 0 0 0 1000px #273352 inset;
}

.auth-form :deep(.v-alert) {
  backdrop-filter: blur(12px);
}

.auth-form-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  min-height: 24px;
  margin-top: 4px;
}

.auth-inline-link {
  border: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  padding: 0;
  font-size: 13.5px;
  font-weight: 600;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.auth-inline-link:hover {
  color: #ffffff;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.code-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 122px;
  gap: 12px;
  align-items: stretch;
  margin-top: 14px;
}

.code-btn {
  min-height: 52px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(18px);
  color: #ffffff !important;
  font-weight: 700;
  box-shadow: none !important;
}

.code-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.18) !important;
}

/* 主按钮：品牌蓝渐变 */
.auth-submit {
  margin-top: 18px;
  min-height: 52px;
  border-radius: 14px !important;
  background: linear-gradient(135deg, #3a9bff 0%, #1e6fd9 55%, #1558a6 100%) !important;
  color: #ffffff !important;
  font-weight: 800;
  letter-spacing: 0.06em;
  box-shadow:
    0 14px 30px rgba(21, 111, 217, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.25) !important;
  transition:
    box-shadow var(--transition-fast),
    transform var(--transition-fast),
    filter var(--transition-fast);
}

.auth-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.06);
}

.auth-submit:focus-visible {
  outline: 3px solid rgba(125, 186, 255, 0.5);
  outline-offset: 3px;
}

/* ========== 社交登录 ========== */
.auth-social {
  margin-top: 26px;
}

.auth-social-title {
  display: flex;
  align-items: center;
  gap: 14px;
  color: rgba(255, 255, 255, 0.55);
  font-size: 12.5px;
}

.auth-social-title::before,
.auth-social-title::after {
  content: '';
  height: 1px;
  flex: 1;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.22), transparent);
}

.auth-social-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.auth-social-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  gap: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(14px);
  color: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  transition:
    background var(--transition-fast),
    border-color var(--transition-fast),
    transform var(--transition-fast);
}

.auth-social-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.16);
  transform: translateY(-1px);
}

.auth-social-icon {
  font-size: 22px;
}

/* 底部跳转 */
.auth-footer-note {
  margin-top: 22px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 14px;
  text-align: center;
}

/* ========== 底部数据与备案 ========== */
.auth-bottom {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.auth-stats {
  display: flex;
  align-items: center;
  gap: 40px;
  margin: 0;
  padding: 10px 28px;
  list-style: none;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(14px);
}

.auth-stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.auth-stat-value {
  font-size: 17px;
  font-weight: 800;
}

.auth-stat-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.auth-beian {
  margin: 0;
  color: rgba(255, 255, 255, 0.42);
  font-size: 12px;
  text-align: center;
}

/* ========== 表单切换动画 ========== */
.auth-form-fade-enter-active,
.auth-form-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.auth-form-fade-enter-from,
.auth-form-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ========== 动效偏好 ========== */
@media (prefers-reduced-motion: reduce) {
  .auth-orb,
  .auth-body {
    animation: none;
  }

  .auth-tab::after,
  .auth-submit,
  .auth-social-btn,
  .auth-form-fade-enter-active,
  .auth-form-fade-leave-active {
    transition: none;
  }

  .auth-submit:hover:not(:disabled),
  .auth-social-btn:hover {
    transform: none;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 640px) {
  .auth-screen {
    padding: 20px 16px 18px;
  }

  .auth-tabs {
    gap: 24px;
  }

  .auth-tab {
    font-size: 15px;
  }

  .auth-social-list,
  .code-wrapper {
    grid-template-columns: 1fr;
  }

  .code-btn {
    min-height: 46px;
  }

  .auth-stats {
    gap: 22px;
    padding: 8px 18px;
  }
}

@media (max-height: 720px) {
  .auth-bottom {
    display: none;
  }

  .auth-body {
    padding: 16px 0;
  }
}
</style>
