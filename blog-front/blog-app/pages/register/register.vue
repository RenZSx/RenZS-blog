<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="header">
      <text class="title">创建账号</text>
      <text class="subtitle">几步即可加入</text>
    </view>

    <view class="form-card">
      <view class="form-field" :class="{ active: focusField === 'username' }">
        <text class="field-icon">📧</text>
        <input
          class="field-input"
          v-model="form.username"
          placeholder="邮箱"
          placeholder-class="ph"
          @focus="focusField = 'username'"
          @blur="focusField = ''"
        />
      </view>

      <view class="form-field" :class="{ active: focusField === 'password' }">
        <text class="field-icon">🔒</text>
        <input
          class="field-input"
          v-model="form.password"
          password
          placeholder="密码"
          placeholder-class="ph"
          @focus="focusField = 'password'"
          @blur="focusField = ''"
        />
      </view>

      <view class="form-field code-field" :class="{ active: focusField === 'code' }">
        <text class="field-icon">🔑</text>
        <input
          class="field-input"
          v-model="form.code"
          placeholder="验证码"
          placeholder-class="ph"
          @focus="focusField = 'code'"
          @blur="focusField = ''"
        />
        <button
          class="code-btn"
          :disabled="codeDown > 0 || codeSending"
          @click.stop="sendCode"
        >
          {{ codeDown > 0 ? `${codeDown}s` : (codeSending ? '发送中' : '获取验证码') }}
        </button>
      </view>

      <button class="submit-btn" :loading="loading" :disabled="loading" @click="handleRegister">
        <text v-if="!loading">注册并登录</text>
      </button>

      <view class="form-footer">
        <text class="footer-link" @click="goLogin">已有账号 · 立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { register, sendVerificationCode } from '@/api/user'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'

const userStore = useUserStore()
const { isDark } = useThemeClass()
const form = reactive({ username: '', password: '', code: '' })
const loading = ref(false)
const codeSending = ref(false)
const codeDown = ref(0)
const focusField = ref('')
let codeTimer = null

function startCountdown() {
  codeDown.value = 60
  codeTimer = setInterval(() => {
    codeDown.value -= 1
    if (codeDown.value <= 0) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

async function sendCode() {
  if (!form.username.trim()) {
    return uni.showToast({ title: '请先输入邮箱', icon: 'none' })
  }
  codeSending.value = true
  try {
    const res = await sendVerificationCode(form.username.trim())
    if (res.flag) {
      uni.showToast({ title: '验证码已发送', icon: 'success' })
      startCountdown()
    } else {
      uni.showToast({ title: res.message || '发送失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  if (!form.username.trim() || !form.password.trim() || !form.code.trim()) {
    return uni.showToast({ title: '请填写完整信息', icon: 'none' })
  }
  loading.value = true
  try {
    const res = await register({
      username: form.username.trim(),
      password: form.password,
      code: form.code.trim()
    })
    if (!res.flag) {
      throw new Error(res.message || '注册失败')
    }
    await userStore.login({ username: form.username.trim(), password: form.password })
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 600)
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.redirectTo({ url: '/pages/login/login' })
}

onUnmounted(() => {
  if (codeTimer) clearInterval(codeTimer)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 48rpx 32rpx;
}

.header {
  padding: 32rpx 16rpx 48rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #1d1f23;
  margin-bottom: 12rpx;
  letter-spacing: -1rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #909399;
}

.form-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 6rpx 16rpx rgba(15, 23, 42, 0.06),
              0 2rpx 4rpx rgba(15, 23, 42, 0.04);
}

.form-field {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 24rpx;
  background: #f5f7fa;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid transparent;
  transition: all 220ms ease;
}

.form-field.active {
  background: #ffffff;
  border-color: #3a3a3a;
  box-shadow: 0 4rpx 12rpx rgba(71, 85, 105, 0.15);
}

.field-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
  opacity: 0.6;
}

.field-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1d1f23;
}

.ph {
  color: #c0c4cc;
  font-size: 28rpx;
}

.code-field {
  padding-right: 8rpx;
}

.code-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  background: rgba(71, 85, 105, 0.12);
  color: #3a3a3a;
  font-size: 24rpx;
  font-weight: 500;
  border-radius: 32rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.code-btn[disabled] {
  background: #f0f2f5;
  color: #c0c4cc;
}

.submit-btn {
  margin-top: 16rpx;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #3a3a3a 0%, #252525 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 20rpx;
  letter-spacing: 4rpx;
  box-shadow: 0 8rpx 20rpx rgba(71, 85, 105, 0.32);
  transition: all 220ms ease;
}

.submit-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(71, 85, 105, 0.32);
}

.form-footer {
  margin-top: 28rpx;
  text-align: center;
}

.footer-link {
  font-size: 26rpx;
  color: #3a3a3a;
  font-weight: 500;
}
</style>
