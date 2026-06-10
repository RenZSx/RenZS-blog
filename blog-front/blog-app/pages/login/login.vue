<template>
  <view class="login-page">
    <!-- 顶部装饰渐变 + 模糊光斑 -->
    <view class="hero">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
      <view class="logo">
        <text class="logo-mark">博</text>
      </view>
      <text class="brand-name">博客</text>
      <text class="brand-slogan">分享技术 · 记录生活</text>
    </view>

    <!-- 表单卡片(覆盖上去的浮层) -->
    <view class="form-card">
      <text class="form-title">欢迎回来</text>
      <text class="form-subtitle">请使用您的账号登录</text>

      <view class="form-field" :class="{ active: focusField === 'username' }">
        <text class="field-icon">👤</text>
        <input
          class="field-input"
          v-model="form.username"
          placeholder="账号 / 邮箱"
          placeholder-class="ph"
          confirm-type="next"
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
          confirm-type="done"
          @focus="focusField = 'password'"
          @blur="focusField = ''"
          @confirm="handleLogin"
        />
      </view>

      <button class="submit-btn" :loading="loading" :disabled="loading" @click="handleLogin">
        <text v-if="!loading">登 录</text>
      </button>

      <view class="form-footer">
        <text class="footer-link" @click="goRegister">还没账号 · 立即注册</text>
      </view>
    </view>

    <view class="bottom-tip">
      <text>登录即代表你已阅读并同意</text>
      <text class="link">《用户协议》</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const focusField = ref('')

async function handleLogin() {
  if (!form.username.trim()) {
    return uni.showToast({ title: '请输入账号', icon: 'none' })
  }
  if (!form.password.trim()) {
    return uni.showToast({ title: '请输入密码', icon: 'none' })
  }

  loading.value = true
  try {
    await userStore.login({
      username: form.username.trim(),
      password: form.password
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 600)
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goRegister() {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #f7f8fa;
  position: relative;
  overflow: hidden;
}

/* ========= Hero 渐变背景 ========= */
.hero {
  position: relative;
  height: 520rpx;
  padding: 120rpx 48rpx 60rpx;
  background: linear-gradient(135deg, #42b983 0%, #2d8362 60%, #1f6d52 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(40rpx);
  opacity: 0.55;
}

.blob-1 {
  top: -100rpx;
  right: -80rpx;
  width: 360rpx;
  height: 360rpx;
  background: rgba(255, 255, 255, 0.35);
}

.blob-2 {
  bottom: 40rpx;
  left: -80rpx;
  width: 260rpx;
  height: 260rpx;
  background: rgba(255, 255, 255, 0.22);
}

.logo {
  width: 140rpx;
  height: 140rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.18),
              inset 0 -2rpx 4rpx rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 2;
}

.logo-mark {
  font-size: 64rpx;
  font-weight: 700;
  color: #339268;
  letter-spacing: -2rpx;
}

.brand-name {
  position: relative;
  z-index: 2;
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 10rpx;
  letter-spacing: 4rpx;
}

.brand-slogan {
  position: relative;
  z-index: 2;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 2rpx;
}

/* ========= 表单卡片 ========= */
.form-card {
  position: relative;
  margin: -80rpx 32rpx 0;
  background: #ffffff;
  border-radius: 32rpx;
  padding: 56rpx 40rpx 40rpx;
  box-shadow: 0 24rpx 48rpx rgba(15, 23, 42, 0.08),
              0 8rpx 16rpx rgba(15, 23, 42, 0.04);
  animation: fadeIn 360ms ease;
}

.form-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1d1f23;
  margin-bottom: 8rpx;
}

.form-subtitle {
  display: block;
  font-size: 24rpx;
  color: #909399;
  margin-bottom: 40rpx;
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
  border-color: #42b983;
  box-shadow: 0 4rpx 12rpx rgba(66, 185, 131, 0.15);
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

.submit-btn {
  margin-top: 16rpx;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #42b983 0%, #2d8362 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 24rpx;
  letter-spacing: 8rpx;
  box-shadow: 0 8rpx 20rpx rgba(66, 185, 131, 0.32);
  transition: all 220ms ease;
}

.submit-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(66, 185, 131, 0.32);
}

.submit-btn[disabled] {
  opacity: 0.7;
}

.form-footer {
  margin-top: 28rpx;
  text-align: center;
}

.footer-link {
  font-size: 26rpx;
  color: #42b983;
  font-weight: 500;
}

.bottom-tip {
  position: absolute;
  bottom: calc(40rpx + env(safe-area-inset-bottom));
  left: 0;
  right: 0;
  text-align: center;
  font-size: 22rpx;
  color: #909399;
}

.bottom-tip .link {
  color: #42b983;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
