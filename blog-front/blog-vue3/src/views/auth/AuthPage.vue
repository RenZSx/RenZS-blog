<!--
  功能说明：Auth 页面统一承载登录、注册和找回密码表单。
  作者：ChenFY
  创建时间：2026-06-17
  用途概述：提供居中的主题化账号入口，并复用现有认证业务逻辑。
-->
<template>
  <main class="auth-screen" :class="`auth-mode-${currentMode}`">
    <!-- 单卡片壳承载三种认证模式，避免旧版翻转卡片造成布局抖动。 -->
    <section class="auth-card" aria-labelledby="auth-title">
      <header class="auth-card-head">
        <div class="auth-brand-row">
          <v-avatar size="46" class="auth-brand-avatar">
            <v-img :src="websiteConfig.websiteAvatar || fallbackAvatar" />
          </v-avatar>
          <div class="auth-brand-copy">
            <div class="auth-card-kicker">{{ currentContent.kicker }}</div>
            <div class="auth-site-name">{{ siteName }}</div>
          </div>
          <div class="auth-mode-symbol" aria-hidden="true">
            <v-icon :icon="modeIcon" size="20" />
          </div>
        </div>

        <h1 id="auth-title" class="auth-card-title">{{ currentContent.title }}</h1>
        <p class="auth-card-subtitle">{{ currentContent.subtitle }}</p>
        <p v-if="siteIntro" class="auth-site-intro">{{ siteIntro }}</p>
      </header>

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
        <form v-if="currentMode === 'login'" key="login" class="auth-form" @submit.prevent="handleLogin">
          <v-text-field
            v-model="loginUsername"
            label="邮箱号"
            placeholder="name@example.com"
            clearable
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            inputmode="email"
            prepend-inner-icon="mdi-email-outline"
            spellcheck="false"
            autocomplete="email"
          />

          <v-text-field
            v-model="loginPassword"
            class="mt-4"
            label="密码"
            placeholder="请输入您的密码"
            variant="outlined"
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

          <v-btn
            class="auth-submit"
            block
            color="primary"
            size="large"
            :loading="loginSubmitting"
            :prepend-icon="modeIcon"
            type="submit"
          >
            {{ currentContent.actionLabel }}
          </v-btn>
        </form>

        <form v-else-if="currentMode === 'register'" key="register" class="auth-form" @submit.prevent="handleRegister">
          <v-alert v-if="!emailRegisterEnabled" class="mb-4" type="warning" variant="tonal" density="comfortable">
            邮箱注册已关闭
          </v-alert>

          <v-text-field
            v-model="registerUsername"
            label="邮箱号"
            placeholder="name@example.com"
            clearable
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            inputmode="email"
            prepend-inner-icon="mdi-email-outline"
            spellcheck="false"
            autocomplete="email"
          />

          <v-text-field
            v-model="registerNickname"
            class="mt-4"
            label="昵称"
            placeholder="给自己起一个名字"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            prepend-inner-icon="mdi-account-outline"
            autocomplete="nickname"
          />

          <v-text-field
            v-model="registerPassword"
            class="mt-4"
            label="密码"
            placeholder="至少 6 位"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            prepend-inner-icon="mdi-lock-outline"
            autocomplete="new-password"
            :append-inner-icon="showRegisterPassword ? 'mdi-eye' : 'mdi-eye-off'"
            :type="showRegisterPassword ? 'text' : 'password'"
            @click:append-inner="showRegisterPassword = !showRegisterPassword"
          />

          <div class="code-wrapper mt-4">
            <v-text-field
              v-model="registerCode"
              label="验证码"
              placeholder="请输入 6 位验证码"
              variant="outlined"
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
              color="primary"
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
            color="primary"
            size="large"
            :disabled="!emailRegisterEnabled"
            :loading="registerSubmitting"
            :prepend-icon="modeIcon"
            type="submit"
          >
            {{ currentContent.actionLabel }}
          </v-btn>
        </form>

        <form v-else key="forgot-password" class="auth-form" @submit.prevent="handleReset">
          <v-text-field
            v-model="forgotUsername"
            label="邮箱号"
            placeholder="name@example.com"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            inputmode="email"
            prepend-inner-icon="mdi-email-outline"
            spellcheck="false"
            autocomplete="email"
          />

          <div class="code-wrapper mt-4">
            <v-text-field
              v-model="forgotCode"
              label="验证码"
              placeholder="请输入 6 位验证码"
              variant="outlined"
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
              color="primary"
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
            class="mt-4"
            label="新密码"
            placeholder="请输入新密码"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            prepend-inner-icon="mdi-lock-reset"
            autocomplete="new-password"
            :append-inner-icon="showForgotPassword ? 'mdi-eye' : 'mdi-eye-off'"
            :type="showForgotPassword ? 'text' : 'password'"
            @click:append-inner="showForgotPassword = !showForgotPassword"
          />

          <v-btn
            class="auth-submit"
            block
            color="primary"
            size="large"
            :loading="forgotSubmitting"
            :prepend-icon="modeIcon"
            type="submit"
          >
            {{ currentContent.actionLabel }}
          </v-btn>
        </form>
      </Transition>

      <!-- 社交登录只属于登录模式，避免注册和找回密码流程被额外入口打断。 -->
      <div v-if="currentMode === 'login' && socialLoginList.length > 0" class="auth-social">
        <div class="auth-social-title">或者用社交账号继续</div>
        <div class="auth-social-list">
          <button v-if="showLogin('qq')" type="button" class="auth-social-btn" aria-label="使用 QQ 登录" @click="qqLogin">
            <span class="iconfont iconqq auth-social-icon" style="color:#00AAEE" aria-hidden="true" />
            <span>QQ</span>
          </button>
          <button
            v-if="showLogin('gitee')"
            type="button"
            class="auth-social-btn"
            aria-label="使用 Gitee 登录"
            @click="giteeLogin"
          >
            <span class="iconfont icongitee-fill-round auth-social-icon" style="color:#d90909" aria-hidden="true" />
            <span>Gitee</span>
          </button>
          <button
            v-if="showLogin('weibo')"
            type="button"
            class="auth-social-btn"
            aria-label="使用微博登录"
            @click="weiboLogin"
          >
            <span class="iconfont iconweibo auth-social-icon" style="color:#e05244" aria-hidden="true" />
            <span>微博</span>
          </button>
        </div>
      </div>

      <div class="auth-footer-note">
        {{ currentContent.footerLead }}
        <button type="button" class="auth-inline-link" @click="setCurrentMode(currentContent.footerTarget)">
          {{ currentContent.footerAction }}
        </button>
      </div>
    </section>
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
// 模式图标只服务于视觉识别，实际业务分支仍由 currentMode 控制。
const modeIcon = computed(() => {
  const icons: Record<AuthMode, string> = {
    login: 'mdi-login-variant',
    register: 'mdi-account-plus-outline',
    'forgot-password': 'mdi-lock-reset'
  }
  return icons[currentMode.value]
})
// 站点名与简介沿用博客配置，并提供兜底文案保证首屏稳定。
const siteName = computed(() => {
  return websiteConfig.value.websiteName || websiteConfig.value.websiteAuthor || 'Renzs Blog'
})
const siteIntro = computed(() => {
  return websiteConfig.value.websiteIntro || websiteConfig.value.websiteNotice || '欢迎回来'
})

function getRedirectTarget() {
  return normalizeAuthRedirect(
    typeof route.query.redirect === 'string' ? route.query.redirect : uiStore.loginUrl
  )
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
  --auth-accent: #1f76d2;
  --auth-accent-strong: #1558a6;
  --auth-accent-soft: rgba(31, 118, 210, 0.12);
  --auth-teal-soft: rgba(20, 184, 166, 0.12);
  --auth-focus-ring: rgba(31, 118, 210, 0.24);
  --auth-card-border: rgba(105, 132, 166, 0.24);
  --auth-card-bg:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 251, 255, 0.98));
  --auth-screen-bg:
    linear-gradient(135deg, rgba(31, 118, 210, 0.08) 0%, rgba(255, 255, 255, 0) 32%),
    linear-gradient(225deg, rgba(20, 184, 166, 0.07) 0%, rgba(255, 255, 255, 0) 34%),
    linear-gradient(180deg, #f5f8fb 0%, #edf3f8 100%);
  position: relative;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  padding: 32px 16px;
  overflow: hidden;
  background: var(--auth-screen-bg);
  color: var(--text-primary);
}

.auth-screen::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(83, 110, 145, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(83, 110, 145, 0.08) 1px, transparent 1px);
  background-position: center;
  background-size: 36px 36px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.86), rgba(0, 0, 0, 0.18));
}

.auth-screen,
.auth-screen *,
.auth-screen *::before,
.auth-screen *::after {
  box-sizing: border-box;
}

.auth-mode-register {
  --auth-accent: #157b72;
  --auth-accent-strong: #0f625b;
  --auth-accent-soft: rgba(21, 123, 114, 0.13);
  --auth-teal-soft: rgba(31, 118, 210, 0.08);
  --auth-focus-ring: rgba(21, 123, 114, 0.24);
}

.auth-mode-forgot-password {
  --auth-accent: #6b5bd2;
  --auth-accent-strong: #4d3fa8;
  --auth-accent-soft: rgba(107, 91, 210, 0.13);
  --auth-teal-soft: rgba(31, 118, 210, 0.08);
  --auth-focus-ring: rgba(107, 91, 210, 0.24);
}

.auth-card {
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  width: 100%;
  max-width: 462px;
  padding: 30px;
  overflow: hidden;
  border: 1px solid var(--auth-card-border);
  border-radius: 8px;
  background: var(--auth-card-bg) !important;
  box-shadow:
    0 28px 70px rgba(55, 77, 103, 0.18),
    0 8px 18px rgba(55, 77, 103, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.86) !important;
}

.auth-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 4px;
  background: linear-gradient(90deg, var(--auth-accent), #35b7a5);
}

.auth-card::after {
  content: '';
  position: absolute;
  inset: 4px 0 auto;
  height: 96px;
  pointer-events: none;
  z-index: -1;
  background:
    linear-gradient(180deg, var(--auth-accent-soft), rgba(255, 255, 255, 0)),
    linear-gradient(90deg, rgba(255, 255, 255, 0), var(--auth-teal-soft));
}

.auth-card-head {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 8px;
}

.auth-brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 50px;
}

.auth-brand-avatar {
  flex: 0 0 auto;
  border: 1px solid rgba(255, 255, 255, 0.82);
  background: var(--surface-base);
  box-shadow:
    0 10px 22px rgba(31, 118, 210, 0.14),
    0 0 0 4px var(--auth-accent-soft);
}

.auth-brand-copy {
  min-width: 0;
  flex: 1;
}

.auth-card-kicker {
  color: var(--auth-accent);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth-site-name {
  overflow: hidden;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-mode-symbol {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  border: 1px solid var(--auth-card-border);
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(244, 249, 255, 0.82));
  color: var(--auth-accent);
  box-shadow: 0 8px 18px rgba(55, 77, 103, 0.08);
}

.auth-card-title {
  margin: 12px 0 0;
  color: var(--text-primary);
  font-size: 29px;
  font-weight: 800;
  line-height: 1.22;
}

.auth-card-subtitle,
.auth-site-intro {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.65;
}

.auth-site-intro {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.auth-tabs {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px;
  margin-top: 25px;
  padding: 5px;
  border: 1px solid var(--auth-card-border);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(237, 243, 249, 0.94), rgba(247, 250, 253, 0.88));
}

.auth-tab {
  min-width: 0;
  min-height: 38px;
  padding: 0 6px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.2;
  transition:
    background var(--transition-fast),
    color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
}

.auth-tab:hover,
.auth-tab-active {
  color: var(--auth-accent-strong);
}

.auth-tab-active {
  background: var(--surface-base);
  box-shadow:
    0 8px 18px rgba(55, 77, 103, 0.1),
    inset 0 0 0 1px rgba(255, 255, 255, 0.8);
}

.auth-tab:focus-visible,
.auth-inline-link:focus-visible,
.auth-social-btn:focus-visible {
  outline: 3px solid var(--auth-focus-ring);
  outline-offset: 3px;
}

.auth-form {
  position: relative;
  z-index: 1;
  margin-top: 25px;
}

.auth-form :deep(.v-input + .v-input) {
  margin-top: 16px;
}

.auth-form :deep(.v-input__details) {
  padding-top: 5px;
}

.auth-form :deep(.v-field) {
  min-height: 52px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    background var(--transition-fast);
}

.auth-form :deep(.v-field--focused) {
  background: rgba(255, 255, 255, 0.96);
  box-shadow:
    0 0 0 4px var(--auth-focus-ring),
    0 8px 20px rgba(55, 77, 103, 0.08);
}

.auth-form :deep(.v-field__prepend-inner) {
  color: var(--auth-accent);
  opacity: 0.84;
}

.auth-form :deep(.v-field__outline) {
  --v-field-border-opacity: 0.2;
}

.auth-form :deep(.v-label) {
  color: var(--text-secondary);
  opacity: 1;
}

.auth-form :deep(.v-field--focused .v-label) {
  color: var(--auth-accent);
}

.auth-form-row {
  display: flex;
  justify-content: flex-end;
  min-height: 28px;
  margin-top: 8px;
}

.code-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 122px;
  gap: 11px;
  align-items: start;
}

.code-btn {
  min-height: 52px;
  border-radius: 8px !important;
  font-weight: 800;
  box-shadow: none !important;
}

.auth-submit {
  margin-top: 19px;
  min-height: 52px;
  border-radius: 8px !important;
  font-weight: 800;
  letter-spacing: 0;
  background: linear-gradient(135deg, var(--auth-accent), var(--auth-accent-strong)) !important;
  color: #fff !important;
  box-shadow:
    0 13px 26px color-mix(in srgb, var(--auth-accent) 26%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.22) !important;
  transition:
    box-shadow var(--transition-fast),
    transform var(--transition-fast),
    filter var(--transition-fast);
}

.auth-submit:hover {
  transform: translateY(-1px);
  filter: saturate(1.06);
}

.auth-submit:focus-visible {
  outline: 3px solid var(--auth-focus-ring);
  outline-offset: 3px;
}

.auth-social {
  position: relative;
  z-index: 1;
  margin-top: 23px;
}

.auth-social-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 13px;
  text-align: center;
}

.auth-social-title::before,
.auth-social-title::after {
  content: '';
  height: 1px;
  flex: 1;
  background: var(--surface-border);
}

.auth-social-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 11px;
  margin-top: 14px;
}

.auth-social-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  gap: 8px;
  border: 1px solid var(--auth-card-border);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  color: var(--text-primary);
  cursor: pointer;
  font-weight: 800;
  transition:
    background var(--transition-fast),
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
}

.auth-social-btn:hover {
  border-color: color-mix(in srgb, var(--auth-accent) 42%, var(--auth-card-border));
  background: var(--surface-base);
  box-shadow:
    0 10px 22px rgba(55, 77, 103, 0.11),
    0 0 0 4px var(--auth-accent-soft);
  transform: translateY(-1px);
}

.auth-social-icon {
  font-size: 22px;
}

.auth-footer-note {
  position: relative;
  z-index: 1;
  margin-top: 19px;
  color: var(--text-secondary);
  font-size: 14px;
  text-align: center;
}

.auth-inline-link {
  border: 0;
  background: transparent;
  color: var(--auth-accent);
  cursor: pointer;
  font-weight: 800;
  padding: 0;
  text-decoration: none;
}

.auth-inline-link:hover {
  color: var(--auth-accent-strong);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.auth-form-fade-enter-active,
.auth-form-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.auth-form-fade-enter-from,
.auth-form-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

:global(.dark) .auth-screen {
  --auth-card-border: rgba(186, 200, 224, 0.2);
  --auth-card-bg:
    linear-gradient(180deg, rgba(35, 43, 58, 0.98), rgba(20, 25, 35, 0.98));
  --auth-screen-bg:
    linear-gradient(135deg, rgba(31, 118, 210, 0.12) 0%, rgba(10, 15, 24, 0) 36%),
    linear-gradient(225deg, rgba(20, 184, 166, 0.09) 0%, rgba(10, 15, 24, 0) 34%),
    linear-gradient(180deg, #111720 0%, #0e141d 100%);
}

:global(.dark) .auth-screen::before {
  background-image:
    linear-gradient(rgba(186, 200, 224, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(186, 200, 224, 0.05) 1px, transparent 1px);
}

:global(.dark) .auth-card {
  background: var(--auth-card-bg) !important;
  box-shadow:
    0 30px 72px rgba(0, 0, 0, 0.42),
    inset 0 1px 0 rgba(255, 255, 255, 0.06) !important;
}

:global(.dark) .auth-card::after {
  background:
    linear-gradient(180deg, var(--auth-accent-soft), rgba(20, 25, 35, 0)),
    linear-gradient(90deg, rgba(20, 25, 35, 0), var(--auth-teal-soft));
}

:global(.dark) .auth-brand-avatar {
  border-color: rgba(255, 255, 255, 0.14);
  background: rgba(17, 22, 31, 0.88);
}

:global(.dark) .auth-mode-symbol,
:global(.dark) .auth-social-btn,
:global(.dark) .auth-form :deep(.v-field) {
  background: rgba(17, 22, 31, 0.58);
}

:global(.dark) .auth-tabs {
  background: rgba(12, 17, 25, 0.52);
}

:global(.dark) .auth-tab-active {
  background: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 8px 18px rgba(0, 0, 0, 0.18),
    inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

:global(.dark) .auth-form :deep(.v-field--focused) {
  background: rgba(22, 29, 41, 0.92);
}

@media (prefers-reduced-motion: reduce) {
  .auth-tab,
  .auth-submit,
  .auth-social-btn,
  .auth-form-fade-enter-active,
  .auth-form-fade-leave-active {
    transition: none;
  }

  .auth-submit:hover,
  .auth-social-btn:hover {
    transform: none;
  }
}

@media (max-width: 640px) {
  .auth-screen {
    align-items: flex-start;
    padding: 14px 12px;
    overflow-y: auto;
  }

  .auth-card {
    width: calc(100vw - 32px);
    max-width: calc(100vw - 32px);
    padding: 22px 18px;
  }

  .auth-card-title {
    font-size: 24px;
  }

  .auth-tabs {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 3px;
    padding: 4px;
  }

  .auth-social-list,
  .code-wrapper {
    grid-template-columns: 1fr;
  }

  .auth-tab {
    min-height: 34px;
    padding: 0 4px;
    overflow: hidden;
    font-size: 11px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .auth-mode-symbol {
    width: 34px;
    height: 34px;
  }

  .code-btn,
  .auth-submit {
    min-height: 50px;
  }
}
</style>
