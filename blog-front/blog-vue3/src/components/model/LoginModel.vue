<template>
  <v-dialog :model-value="uiStore.loginFlag" @update:model-value="uiStore.setLoginFlag($event)" :fullscreen="isMobile" max-width="460">
    <v-card class="login-card">
      <v-icon class="close-btn" @click="uiStore.setLoginFlag(false)">
        mdi-close
      </v-icon>

      <div class="login-wrapper">
        <!-- 用户名 -->
        <v-text-field
          v-model="username"
          label="邮箱号"
          placeholder="请输入您的邮箱号"
          clearable
          variant="outlined"
          @keyup.enter="handleLogin"
        />

        <!-- 密码 -->
        <v-text-field
          v-model="password"
          class="mt-4"
          label="密码"
          placeholder="请输入您的密码"
          variant="outlined"
          :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showPassword ? 'text' : 'password'"
          @click:append="showPassword = !showPassword"
          @keyup.enter="handleLogin"
        />

        <!-- 登录按钮 -->
        <v-btn
          class="mt-6"
          block
          color="primary"
          size="large"
          @click="handleLogin"
        >
          登录
        </v-btn>

        <!-- 注册和找回密码 -->
        <div class="login-tip mt-6">
          <span class="tip-link" @click="openRegister">立即注册</span>
          <span class="tip-link float-right" @click="openForget">忘记密码?</span>
        </div>

        <!-- 社交登录 -->
        <div v-if="socialLoginList.length > 0" class="social-login">
          <div class="social-login-title">社交账号登录</div>
          <div class="social-login-wrapper">
            <a
              v-if="showLogin('qq')"
              class="social-icon iconfont iconqq"
              style="color: #00AAEE"
              @click="qqLogin"
            />
            <a
              v-if="showLogin('gitee')"
              class="social-icon iconfont icongitee-fill-round"
              style="color: #d90909"
              @click="giteeLogin"
            />
            <a
              v-if="showLogin('weibo')"
              class="social-icon iconfont iconweibo"
              style="color: #e05244"
              @click="weiboLogin"
            />
          </div>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDisplay } from 'vuetify'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { login } from '@/api/user'
import { useToast } from '@/composables/useToast'
import config from '@/assets/js/config'
import { saveOauthMode } from '@/utils/oauthMode'

const { mobile } = useDisplay()
const router = useRouter()
const uiStore = useUIStore()
const userStore = useUserStore()
const blogInfoStore = useBlogInfoStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)

const isMobile = computed(() => mobile.value)
const socialLoginList = computed(() => {
  return blogInfoStore.blogInfo.websiteConfig.socialLoginList || []
})

function showLogin(type: string) {
  return socialLoginList.value.includes(type)
}

function openRegister() {
  uiStore.setLoginFlag(false)
  uiStore.setRegisterFlag(true)
}

function openForget() {
  uiStore.setLoginFlag(false)
  uiStore.setForgetFlag(true)
}

async function handleLogin() {
  // 验证邮箱格式
  const emailReg = /^[A-Za-z0-9一-龥]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailReg.test(username.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }

  // 验证密码
  if (!password.value.trim()) {
    useToast({ type: 'error', message: '密码不能为空' })
    return
  }

  try {
    const { data } = await login({ username: username.value, password: password.value })
    if (data.flag) {
      username.value = ''
      password.value = ''
      userStore.login(data.data)
      uiStore.closeAllModals()
      useToast({ type: 'success', message: '登录成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '登录失败' })
  }
}

function qqLogin() {
  saveOauthMode({ provider: 'qq', mode: 'login' })
  uiStore.saveLoginUrl(router.currentRoute.value.path)
  // QQ 登录逻辑
  if (mobile.value) {
    // @ts-ignore
    QC.Login.showPopup({
      appId: config.QQ_APP_ID,
      redirectURI: config.QQ_REDIRECT_URI
    })
  } else {
    window.open(
      `https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=${config.QQ_APP_ID}&response_type=token&scope=all&redirect_uri=${config.QQ_REDIRECT_URI}`,
      '_self'
    )
  }
}

function weiboLogin() {
  uiStore.saveLoginUrl(router.currentRoute.value.path)
  window.open(
    `https://api.weibo.com/oauth2/authorize?client_id=${config.WEIBO_APP_ID}&response_type=code&redirect_uri=${config.WEIBO_REDIRECT_URI}`,
    '_self'
  )
}

function giteeLogin() {
  uiStore.saveLoginUrl(router.currentRoute.value.path)
  window.open(
    `https://gitee.com/oauth/authorize?client_id=${config.GITEE_CLIENT_ID}&response_type=code&redirect_uri=${config.GITEE_REDIRECT_URI}`,
    '_self'
  )
}
</script>

<style scoped>
.login-card {
  padding: 24px;
  border-radius: 8px;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  cursor: pointer;
}

.login-wrapper {
  padding: 20px 0;
}

.login-tip {
  display: flex;
  justify-content: space-between;
}

.tip-link {
  color: #1976D2;
  cursor: pointer;
  font-size: 0.9rem;
}

.tip-link:hover {
  text-decoration: underline;
}

.social-login {
  margin-top: 24px;
}

.social-login-title {
  color: #b5b5b5;
  font-size: 0.75rem;
  text-align: center;
  margin-bottom: 16px;
}

.social-login-title::before,
.social-login-title::after {
  content: '';
  display: inline-block;
  background-color: #d8d8d8;
  width: 60px;
  height: 1px;
  margin: 0 12px;
  vertical-align: middle;
}

.social-login-wrapper {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-icon {
  font-size: 30px;
  cursor: pointer;
  text-decoration: none;
}
</style>
