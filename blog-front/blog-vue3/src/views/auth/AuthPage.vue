<template>
  <div class="auth-screen">
    <div class="auth-background">
      <div class="auth-orb auth-orb-one" />
      <div class="auth-orb auth-orb-two" />
      <div class="auth-orb auth-orb-three" />
    </div>

    <section class="auth-stage">
      <div class="auth-hero" :style="heroStyle">
        <div class="auth-hero-overlay" />
        <div class="auth-hero-content">
          <div>
            <div class="auth-kicker">{{ modeCopy.kicker }}</div>
            <h1 class="auth-title">{{ modeCopy.headline }}</h1>
            <p class="auth-description">{{ modeCopy.description }}</p>
          </div>

          <div class="auth-quote">
            <span class="auth-quote-line" />
            <p>{{ modeCopy.quote }}</p>
          </div>

          <div>
            <div class="auth-badges">
              <span v-for="item in modeCopy.badges" :key="item" class="auth-badge">{{ item }}</span>
            </div>

            <div class="auth-brand">
              <v-avatar size="52" class="auth-brand-avatar">
                <v-img :src="websiteConfig.websiteAvatar || fallbackAvatar" />
              </v-avatar>
              <div>
                <div class="auth-brand-name">
                  {{ websiteConfig.websiteAuthor || websiteConfig.websiteName }}
                </div>
                <div class="auth-brand-meta">
                  {{ websiteConfig.websiteIntro || websiteConfig.websiteNotice }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-panel">
        <div
          class="auth-flip-card"
          :class="{
            'is-register-mode': currentMode === 'register',
            'is-forgot-mode': currentMode === 'forgot-password'
          }"
          :style="authCardStyle"
        >
          <div class="auth-flip-inner">
            <div class="auth-card auth-card-front">
              <div class="auth-tabs">
                <button
                  v-for="item in navItems"
                  :key="item.key"
                  type="button"
                  class="auth-tab"
                  :class="{ 'auth-tab-active': currentMode === item.key }"
                  @click="setCurrentMode(item.key)"
                >
                  {{ item.label }}
                </button>
              </div>

              <div class="auth-login-content">
                  <div class="auth-card-head">
                    <div class="auth-card-kicker">Welcome Back</div>
                    <h2 class="auth-card-title">登录你的博客身份</h2>
                    <p class="auth-card-subtitle">
                      输入账号继续阅读、评论、点赞，或者使用你已绑定的社交账号快速进入。
                    </p>
                  </div>

                  <form class="auth-form" @submit.prevent="handleLogin">
                    <v-text-field
                      v-model="loginUsername"
                      label="邮箱号"
                      placeholder="name@example.com"
                      clearable
                      variant="outlined"
                      autocomplete="email"
                    />

                    <v-text-field
                      v-model="loginPassword"
                      class="mt-4"
                      label="密码"
                      placeholder="请输入您的密码"
                      variant="outlined"
                      autocomplete="current-password"
                      :append-icon="showLoginPassword ? 'mdi-eye' : 'mdi-eye-off'"
                      :type="showLoginPassword ? 'text' : 'password'"
                      @click:append="showLoginPassword = !showLoginPassword"
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
                      type="submit"
                    >
                      登录
                    </v-btn>
                  </form>

                  <div v-if="socialLoginList.length > 0" class="auth-social">
                    <div class="auth-social-title">或者用社交账号继续</div>
                    <div class="auth-social-list">
                      <button
                        v-if="showLogin('qq')"
                        type="button"
                        class="auth-social-btn"
                        @click="qqLogin"
                      >
                        <span class="iconfont iconqq auth-social-icon" style="color:#00AAEE" />
                        <span>QQ</span>
                      </button>
                      <button
                        v-if="showLogin('gitee')"
                        type="button"
                        class="auth-social-btn"
                        @click="giteeLogin"
                      >
                        <span class="iconfont icongitee-fill-round auth-social-icon" style="color:#d90909" />
                        <span>Gitee</span>
                      </button>
                      <button
                        v-if="showLogin('weibo')"
                        type="button"
                        class="auth-social-btn"
                        @click="weiboLogin"
                      >
                        <span class="iconfont iconweibo auth-social-icon" style="color:#e05244" />
                        <span>微博</span>
                      </button>
                    </div>
                  </div>

                  <div class="auth-footer-note">
                    还没有账号？
                    <button type="button" class="auth-inline-link" @click="setCurrentMode('register')">
                      创建一个新的身份
                    </button>
                  </div>
              </div>
            </div>

            <div class="auth-card auth-card-register">
              <div class="auth-tabs">
                <button
                  v-for="item in navItems"
                  :key="item.key"
                  type="button"
                  class="auth-tab"
                  :class="{ 'auth-tab-active': currentMode === item.key }"
                  @click="setCurrentMode(item.key)"
                >
                  {{ item.label }}
                </button>
              </div>

              <div class="auth-card-head">
                <div class="auth-card-kicker">New Account</div>
                <h2 class="auth-card-title">注册一个新的博客身份</h2>
                <p class="auth-card-subtitle">
                  完成邮箱验证后就能拥有自己的互动身份，也能直接回到刚才浏览的位置。
                </p>
              </div>

              <form class="auth-form" @submit.prevent="handleRegister">
                <v-alert
                  v-if="!emailRegisterEnabled"
                  class="mb-4"
                  type="warning"
                  variant="tonal"
                  density="comfortable"
                >
                  邮箱注册已关闭
                </v-alert>

                <v-text-field
                  v-model="registerUsername"
                  label="邮箱号"
                  placeholder="name@example.com"
                  clearable
                  variant="outlined"
                  autocomplete="email"
                />

                <v-text-field
                  v-model="registerNickname"
                  class="mt-4"
                  label="昵称"
                  placeholder="给自己起一个名字"
                  variant="outlined"
                  autocomplete="nickname"
                />

                <v-text-field
                  v-model="registerPassword"
                  class="mt-4"
                  label="密码"
                  placeholder="至少 6 位"
                  variant="outlined"
                  autocomplete="new-password"
                  :append-icon="showRegisterPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showRegisterPassword ? 'text' : 'password'"
                  @click:append="showRegisterPassword = !showRegisterPassword"
                />

                <div class="code-wrapper mt-4">
                  <v-text-field
                    v-model="registerCode"
                    label="验证码"
                    placeholder="请输入 6 位验证码"
                    variant="outlined"
                    maxlength="6"
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
                  type="submit"
                >
                  创建账号
                </v-btn>
              </form>

              <div class="auth-footer-note">
                已经有账号？
                <button type="button" class="auth-inline-link" @click="setCurrentMode('login')">
                  返回登录
                </button>
              </div>
            </div>

            <div class="auth-card auth-card-forgot">
              <div class="auth-tabs">
                <button
                  v-for="item in navItems"
                  :key="item.key"
                  type="button"
                  class="auth-tab"
                  :class="{ 'auth-tab-active': currentMode === item.key }"
                  @click="setCurrentMode(item.key)"
                >
                  {{ item.label }}
                </button>
              </div>

              <div class="auth-card-head">
                <div class="auth-card-kicker">Recovery</div>
                <h2 class="auth-card-title">重设你的登录密码</h2>
                <p class="auth-card-subtitle">
                  输入邮箱、验证码和新密码，我们会帮你把账户入口重新打开。
                </p>
              </div>

              <form class="auth-form" @submit.prevent="handleReset">
                <v-text-field
                  v-model="forgotUsername"
                  label="邮箱号"
                  placeholder="name@example.com"
                  variant="outlined"
                  autocomplete="email"
                />

                <div class="code-wrapper mt-4">
                  <v-text-field
                    v-model="forgotCode"
                    label="验证码"
                    placeholder="请输入 6 位验证码"
                    variant="outlined"
                    maxlength="6"
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
                  autocomplete="new-password"
                  :append-icon="showForgotPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showForgotPassword ? 'text' : 'password'"
                  @click:append="showForgotPassword = !showForgotPassword"
                />

                <v-btn
                  class="auth-submit"
                  block
                  color="primary"
                  size="large"
                  :loading="forgotSubmitting"
                  type="submit"
                >
                  重置密码
                </v-btn>
              </form>

              <div class="auth-footer-note">
                想起密码了？
                <button type="button" class="auth-inline-link" @click="setCurrentMode('login')">
                  返回登录
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
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
import config from '@/assets/js/config'

type AuthMode = 'login' | 'register' | 'forgot-password'

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

const authModes = new Set<AuthMode>(['login', 'register', 'forgot-password'])

function normalizeAuthMode(mode: unknown): AuthMode {
  return typeof mode === 'string' && authModes.has(mode as AuthMode) ? (mode as AuthMode) : 'login'
}

const currentMode = ref<AuthMode>(normalizeAuthMode(route.query.mode))

const websiteConfig = computed(() => blogInfoStore.blogInfo.websiteConfig)
const fallbackAvatar = computed(() => websiteConfig.value.touristAvatar || '')
const socialLoginList = computed(() => websiteConfig.value.socialLoginList || [])
const emailRegisterEnabled = computed(() => {
  return Number(websiteConfig.value.isEmailRegister ?? 1) === 1
})

const modeCopy = computed(() => {
  if (currentMode.value === 'login') {
    return {
      kicker: 'Sign In',
      headline: '把夜色、灵感和未写完的故事，重新接回你的账户。',
      // description: '新的登录页不再是一个临时弹窗，而是博客身份系统真正的入口。',
      quote: '“每一次登录，都是重新进入自己的精神现场。”',
      badges: ['继续阅读', '保存互动状态', '同步点赞与评论']
    }
  }
  if (currentMode.value === 'register') {
    return {
      kicker: 'Create Identity',
      headline: '给你的阅读、评论与灵感，准备一个真正属于自己的名字。',
      // description: '注册页会保留情绪感和品牌感，让进入这个博客的第一步也值得记住。',
      quote: '“身份不是表单里填出的结果，而是你愿意留下来的证据。”',
      badges: ['创建账号', '保存互动记录', '同步收藏与偏好']
    }
  }
  if (currentMode.value === 'forgot-password') {
    return {
      kicker: 'Reset Access',
      headline: '有些入口会暂时忘记，但你仍然可以温柔地找回它。',
      // description: '找回密码页保留统一的情绪表达，但表单流程更专注、更冷静。',
      quote: '“真正的重置，不是回到起点，而是重新获得继续前进的能力。”',
      badges: ['重置密码', '邮箱验证', '快速回到登录']
    }
  }
  return {
    kicker: 'Sign In',
    headline: '把夜色、灵感和未写完的故事，重新接回你的账户。',
    description: '新的登录页不再是一个临时弹窗，而是博客身份系统真正的入口。',
    quote: '“每一次登录，都是重新进入自己的精神现场。”',
    badges: ['继续阅读', '保存互动状态', '同步点赞与评论']
  }
})

const heroStyle = computed(() => {
  const cover = blogInfoStore.getPageCover('home') || blogInfoStore.getPageCover('about')
  return cover
    ? {
        backgroundImage: `linear-gradient(180deg, rgba(7, 14, 28, 0.18), rgba(7, 14, 28, 0.74)), url(${cover})`
      }
    : undefined
})

const authCardStyle = computed(() => {
  if (currentMode.value === 'register') {
    return { '--auth-card-height': '760px' }
  }
  if (currentMode.value === 'forgot-password') {
    return { '--auth-card-height': '680px' }
  }
  return { '--auth-card-height': '650px' }
})

const navItems: ReadonlyArray<{ key: AuthMode; label: string }> = [
  { key: 'login', label: '登录' },
  { key: 'register', label: '注册' },
  { key: 'forgot-password', label: '找回密码' }
] as const

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
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(73, 177, 245, 0.18), transparent 28%),
    linear-gradient(160deg, #08111f 0%, #10243f 44%, #0a172b 100%);
}

.auth-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(8px);
  opacity: 0.72;
}

.auth-orb-one {
  top: 8%;
  left: -4%;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(67, 180, 255, 0.42), transparent 68%);
}

.auth-orb-two {
  top: 20%;
  right: 10%;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(141, 123, 255, 0.26), transparent 70%);
}

.auth-orb-three {
  right: -2%;
  bottom: 2%;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(82, 203, 196, 0.18), transparent 72%);
}

.auth-stage {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.18fr 0.82fr;
  align-items: stretch;
  min-height: 100vh;
  padding: 34px;
  gap: 24px;
}

.auth-hero,
.auth-card {
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 30px 80px rgba(3, 10, 22, 0.35);
}

.auth-hero {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  background:
    radial-gradient(circle at top left, rgba(109, 178, 255, 0.16), transparent 35%),
    linear-gradient(160deg, #0b1324 0%, #11284d 55%, #0b1830 100%);
  background-position: center;
  background-size: cover;
}

.auth-hero-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(5, 10, 18, 0.16), rgba(5, 10, 18, 0.72)),
    radial-gradient(circle at top left, rgba(125, 186, 255, 0.28), transparent 34%);
}

.auth-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  padding: 48px;
  color: #f7fbff;
}

.auth-kicker,
.auth-card-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.auth-kicker {
  color: #9fd0ff;
}

.auth-title {
  max-width: 560px;
  margin-top: 18px;
  font-family: Georgia, 'Times New Roman', 'Noto Serif SC', serif;
  font-size: clamp(34px, 4vw, 54px);
  line-height: 1.12;
  letter-spacing: 0.01em;
}

.auth-description {
  max-width: 500px;
  margin-top: 16px;
  color: rgba(244, 249, 255, 0.78);
  font-size: 16px;
  line-height: 1.85;
}

.auth-quote {
  max-width: 420px;
  margin-top: auto;
  color: rgba(244, 249, 255, 0.8);
  font-size: 15px;
  line-height: 1.8;
}

.auth-quote-line {
  display: block;
  width: 64px;
  height: 1px;
  margin-bottom: 18px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.75), transparent);
}

.auth-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 26px;
}

.auth-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(246, 250, 255, 0.9);
  font-size: 12px;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 28px;
}

.auth-brand-avatar {
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
}

.auth-brand-name {
  font-size: 16px;
  font-weight: 700;
}

.auth-brand-meta {
  max-width: 360px;
  color: rgba(246, 250, 255, 0.7);
  font-size: 13px;
}

.auth-panel {
  display: flex;
  align-items: center;
  perspective: 1600px;
}

.auth-flip-card {
  width: 100%;
  min-height: var(--auth-card-height);
}

.auth-flip-inner {
  position: relative;
  width: 100%;
  min-height: var(--auth-card-height);
  transform-style: preserve-3d;
  transition: transform 0.68s cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-flip-card.is-register-mode .auth-flip-inner {
  transform: rotateY(180deg);
}

.auth-flip-card.is-forgot-mode .auth-flip-inner {
  transform: rotateY(-180deg);
}

.auth-card {
  position: absolute;
  inset: 0;
  width: 100%;
  padding: 28px;
  border-radius: 30px;
  background:
    radial-gradient(circle at top right, rgba(73, 177, 245, 0.14), transparent 24%),
    rgba(248, 251, 255, 0.92);
  backdrop-filter: blur(18px) saturate(120%);
  backface-visibility: hidden;
  box-shadow:
    var(--card-shadow-raised),
    inset 0 1px 0 rgba(255, 255, 255, 0.42);
}

.auth-card-register {
  transform: rotateY(180deg);
}

.auth-card-forgot {
  transform: rotateY(-180deg);
}

.auth-tabs {
  display: inline-flex;
  gap: 8px;
  padding: 6px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
}

.auth-tab {
  padding: 10px 14px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #607286;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.22s ease;
}

.auth-tab:hover {
  color: #1a2434;
  text-decoration: none;
}

.auth-tab-active {
  background: #fff;
  color: #172233;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.1);
}

.auth-card-head {
  margin-top: 24px;
}

.auth-card-kicker {
  color: #3b90d0;
}

.auth-card-title {
  margin-top: 8px;
  color: #132034;
  font-family: Georgia, 'Times New Roman', 'Noto Serif SC', serif;
  font-size: clamp(26px, 2.7vw, 36px);
  line-height: 1.18;
}

.auth-card-subtitle {
  margin-top: 10px;
  color: #5e6f84;
  font-size: 14px;
  line-height: 1.7;
}

.auth-form {
  margin-top: 24px;
}

.auth-form-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.code-wrapper {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.code-btn {
  min-width: 128px;
  height: 56px;
  border-radius: 16px !important;
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.14);
}

.auth-submit {
  margin-top: 18px;
  min-height: 52px;
  border-radius: 18px !important;
  background: linear-gradient(135deg, #2288d2, #6d87ff) !important;
  box-shadow: 0 18px 34px rgba(64, 123, 255, 0.24);
}

.auth-social {
  margin-top: 18px;
}

.auth-social-title {
  position: relative;
  color: #7c8ca0;
  font-size: 13px;
  text-align: center;
}

.auth-social-title::before,
.auth-social-title::after {
  content: '';
  position: absolute;
  top: 50%;
  width: calc(50% - 64px);
  height: 1px;
  background: rgba(15, 23, 42, 0.08);
}

.auth-social-title::before {
  left: 0;
}

.auth-social-title::after {
  right: 0;
}

.auth-social-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.auth-social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  border: 1px solid var(--card-border-soft);
  border-radius: 16px;
  gap: 8px;
  background: rgba(255, 255, 255, 0.78);
  color: #233247;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast),
    border-color var(--transition-fast);
}

.auth-social-btn:hover {
  border-color: var(--card-border-accent);
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.auth-social-icon {
  font-size: 22px;
}

.auth-footer-note {
  margin-top: 18px;
  color: #6a7a90;
  font-size: 14px;
  text-align: center;
}

.auth-login-content .auth-footer-note {
  margin-top: 10px;
}

.auth-inline-link {
  border: 0;
  background: transparent;
  color: #2f89cf;
  cursor: pointer;
  font-weight: 700;
  padding: 0;
  text-decoration: none;
}

.auth-form-slide-enter-active,
.auth-form-slide-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.auth-form-slide-enter-from {
  opacity: 0;
  transform: translateX(18px);
}

.auth-form-slide-leave-to {
  opacity: 0;
  transform: translateX(-18px);
}

@media (max-width: 1100px) {
  .auth-stage {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .auth-hero {
    min-height: 320px;
  }

  .auth-hero-content {
    padding: 30px 24px;
  }

  .auth-panel {
    align-items: stretch;
  }
}

@media (max-width: 759px) {
  .auth-screen {
    background:
      radial-gradient(circle at top, rgba(73, 177, 245, 0.2), transparent 32%),
      linear-gradient(180deg, #0a1324 0%, #0d1d36 100%);
  }

  .auth-stage {
    gap: 14px;
    padding: 10px;
  }

  .auth-hero {
    min-height: 250px;
    border-radius: 24px;
  }

  .auth-flip-card,
  .auth-flip-inner {
    min-height: calc(var(--auth-card-height) + 40px);
  }

  .auth-card {
    padding: 20px;
    border-radius: 24px;
  }

  .auth-title {
    font-size: 32px;
  }

  .auth-brand-meta {
    max-width: none;
  }

  .auth-tabs {
    width: 100%;
    justify-content: center;
  }

  .code-wrapper,
  .auth-social-list {
    grid-template-columns: 1fr;
  }

  .code-btn {
    width: 100%;
  }
}
</style>
