<template>
  <v-dialog :model-value="uiStore.registerFlag" @update:model-value="uiStore.setRegisterFlag($event)" :fullscreen="isMobile" max-width="460">
    <v-card class="register-card">
      <v-icon class="close-btn" @click="uiStore.setRegisterFlag(false)">
        mdi-close
      </v-icon>

      <div class="register-wrapper">
        <!-- 邮箱 -->
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
          v-model="username"
          label="邮箱号"
          placeholder="请输入您的邮箱号"
          clearable
          variant="outlined"
        />

        <!-- 昵称 -->
        <v-text-field
          v-model="nickname"
          class="mt-4"
          label="昵称"
          placeholder="请输入您的昵称"
          variant="outlined"
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
        />

        <!-- 验证码 -->
        <div class="code-wrapper mt-4">
          <v-text-field
            v-model="code"
            label="验证码"
            placeholder="请输入验证码"
            variant="outlined"
          />
          <v-btn
            color="primary"
            :disabled="!emailRegisterEnabled || countdown > 0"
            @click="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </v-btn>
        </div>

        <!-- 注册按钮 -->
        <v-btn
          class="mt-6"
          block
          color="primary"
          size="large"
          :disabled="!emailRegisterEnabled"
          @click="handleRegister"
        >
          注册
        </v-btn>

        <!-- 登录链接 -->
        <div class="register-tip mt-6">
          <span>已有账号？</span>
          <span class="tip-link" @click="openLogin">立即登录</span>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDisplay } from 'vuetify'
import { useUIStore } from '@/stores/ui'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { register, sendCode as sendCodeApi } from '@/api/user'
import { useToast } from '@/composables/useToast'

const { mobile } = useDisplay()
const uiStore = useUIStore()
const blogInfoStore = useBlogInfoStore()

const username = ref('')
const nickname = ref('')
const password = ref('')
const code = ref('')
const showPassword = ref(false)
const countdown = ref(0)

const isMobile = computed(() => mobile.value)
const emailRegisterEnabled = computed(() => {
  return Number(blogInfoStore.blogInfo.websiteConfig.isEmailRegister ?? 1) === 1
})

function openLogin() {
  uiStore.setRegisterFlag(false)
  uiStore.setLoginFlag(true)
}

async function sendCode() {
  if (!emailRegisterEnabled.value) {
    useToast({ type: 'warning', message: '邮箱注册已关闭' })
    return
  }

  const emailReg = /^[A-Za-z0-9一-龥]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailReg.test(username.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }

  try {
    const { data } = await sendCodeApi(username.value)
    if (data.flag) {
      useToast({ type: 'success', message: '验证码已发送' })
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '发送验证码失败' })
  }
}

async function handleRegister() {
  if (!emailRegisterEnabled.value) {
    useToast({ type: 'warning', message: '邮箱注册已关闭' })
    return
  }

  if (!username.value.trim()) {
    useToast({ type: 'error', message: '邮箱不能为空' })
    return
  }
  if (!nickname.value.trim()) {
    useToast({ type: 'error', message: '昵称不能为空' })
    return
  }
  if (!password.value.trim()) {
    useToast({ type: 'error', message: '密码不能为空' })
    return
  }
  if (!code.value.trim()) {
    useToast({ type: 'error', message: '验证码不能为空' })
    return
  }

  try {
    const { data } = await register({
      username: username.value,
      password: password.value,
      code: code.value,
      nickname: nickname.value
    })
    if (data.flag) {
      useToast({ type: 'success', message: '注册成功' })
      openLogin()
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '注册失败' })
  }
}
</script>

<style scoped>
.register-card {
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

.register-wrapper {
  padding: 20px 0;
}

.code-wrapper {
  display: flex;
  gap: 12px;
}

.code-wrapper .v-text-field {
  flex: 1;
}

.register-tip {
  text-align: center;
  font-size: 0.9rem;
}

.tip-link {
  color: #1976D2;
  cursor: pointer;
}

.tip-link:hover {
  text-decoration: underline;
}
</style>
