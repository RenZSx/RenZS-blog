<template>
  <v-dialog :model-value="uiStore.emailFlag" @update:model-value="uiStore.setEmailFlag($event)" :fullscreen="isMobile" max-width="460">
    <v-card class="email-card">
      <v-icon class="close-btn" @click="uiStore.setEmailFlag(false)">
        mdi-close
      </v-icon>

      <div class="email-wrapper">
        <h3 class="title">绑定邮箱</h3>

        <!-- 邮箱 -->
        <v-text-field
          v-model="email"
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

        <!-- 确认按钮 -->
        <v-btn
          class="mt-6"
          block
          color="primary"
          size="large"
          @click="handleBind"
        >
          绑定邮箱
        </v-btn>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDisplay } from 'vuetify'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'
import { sendCode as sendCodeApi, bindEmail } from '@/api/user'
import { useToast } from '@/composables/useToast'

const { mobile } = useDisplay()
const uiStore = useUIStore()
const userStore = useUserStore()

const email = ref('')
const code = ref('')
const countdown = ref(0)

const isMobile = computed(() => mobile.value)

async function sendCode() {
  const emailReg = /^[A-Za-z0-9一-龥]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailReg.test(email.value)) {
    useToast({ type: 'error', message: '邮箱格式不正确' })
    return
  }

  try {
    const { data } = await sendCodeApi(email.value)
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

async function handleBind() {
  if (!email.value.trim()) {
    useToast({ type: 'error', message: '邮箱不能为空' })
    return
  }
  if (!code.value.trim()) {
    useToast({ type: 'error', message: '验证码不能为空' })
    return
  }

  try {
    const { data } = await bindEmail({
      email: email.value,
      code: code.value
    })
    if (data.flag) {
      userStore.email = email.value
      useToast({ type: 'success', message: '绑定成功' })
      uiStore.setEmailFlag(false)
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '绑定失败' })
  }
}
</script>

<style scoped>
.email-card {
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

.email-wrapper {
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
</style>