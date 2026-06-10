<template>
  <v-dialog :model-value="uiStore.forgetFlag" @update:model-value="uiStore.setForgetFlag($event)" :fullscreen="isMobile" max-width="460">
    <v-card class="forget-card">
      <v-icon class="close-btn" @click="uiStore.setForgetFlag(false)">
        mdi-close
      </v-icon>

      <div class="forget-wrapper">
        <h3 class="title">找回密码</h3>

        <!-- 邮箱 -->
        <v-text-field
          v-model="username"
          label="邮箱号"
          placeholder="请输入您的邮箱号"
          variant="outlined"
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
            :disabled="countdown > 0"
            @click="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </v-btn>
        </div>

        <!-- 新密码 -->
        <v-text-field
          v-model="password"
          class="mt-4"
          label="新密码"
          placeholder="请输入新密码"
          variant="outlined"
          :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showPassword ? 'text' : 'password'"
          @click:append="showPassword = !showPassword"
        />

        <!-- 确认按钮 -->
        <v-btn
          class="mt-6"
          block
          color="primary"
          size="large"
          @click="handleReset"
        >
          重置密码
        </v-btn>

        <!-- 返回登录 -->
        <div class="forget-tip mt-6">
          <span class="tip-link" @click="openLogin">返回登录</span>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDisplay } from 'vuetify'
import { useUIStore } from '@/stores/ui'
import { sendCode as sendCodeApi, updatePassword } from '@/api/user'
import { useToast } from '@/composables/useToast'

const { mobile } = useDisplay()
const uiStore = useUIStore()

const username = ref('')
const code = ref('')
const password = ref('')
const showPassword = ref(false)
const countdown = ref(0)

const isMobile = computed(() => mobile.value)

function openLogin() {
  uiStore.setForgetFlag(false)
  uiStore.setLoginFlag(true)
}

async function sendCode() {
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

async function handleReset() {
  if (!username.value.trim()) {
    useToast({ type: 'error', message: '邮箱不能为空' })
    return
  }
  if (!code.value.trim()) {
    useToast({ type: 'error', message: '验证码不能为空' })
    return
  }
  if (!password.value.trim()) {
    useToast({ type: 'error', message: '密码不能为空' })
    return
  }

  try {
    const { data } = await updatePassword({
      username: username.value,
      password: password.value,
      code: code.value
    })
    if (data.flag) {
      useToast({ type: 'success', message: '密码重置成功' })
      openLogin()
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '重置密码失败' })
  }
}
</script>

<style scoped>
.forget-card {
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

.forget-wrapper {
  padding: 20px 0;
}

.title {
  text-align: center;
  margin-bottom: 24px;
}

.code-wrapper {
  display: flex;
  gap: 12px;
}

.code-wrapper .v-text-field {
  flex: 1;
}

.forget-tip {
  text-align: center;
}

.tip-link {
  color: #1976D2;
  cursor: pointer;
  font-size: 0.9rem;
}

.tip-link:hover {
  text-decoration: underline;
}
</style>