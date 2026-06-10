<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <!-- 头像区 -->
    <view class="avatar-section">
      <view class="avatar-wrap" @click="onChooseAvatar">
        <image
          class="avatar"
          :src="userStore.userInfo?.avatar || defaultAvatar"
          mode="aspectFill"
        />
        <view class="avatar-mask">
          <bx-icon name="camera" :size="40" color="#ffffff" />
        </view>
      </view>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <!-- 表单 -->
    <view class="form-card">
      <view class="form-item">
        <text class="form-label">昵称</text>
        <input
          class="form-input"
          v-model="form.nickname"
          placeholder="请输入昵称"
          placeholder-class="ph"
          maxlength="20"
        />
      </view>
      <view class="divider" />
      <view class="form-item">
        <text class="form-label">简介</text>
        <input
          class="form-input"
          v-model="form.intro"
          placeholder="一句话介绍自己"
          placeholder-class="ph"
          maxlength="50"
        />
      </view>
      <view class="divider" />
      <view class="form-item">
        <text class="form-label">个人网站</text>
        <input
          class="form-input"
          v-model="form.webSite"
          placeholder="https://"
          placeholder-class="ph"
          maxlength="100"
        />
      </view>
    </view>

    <view class="action-area">
      <button class="save-btn" :loading="saving" :disabled="saving" @click="handleSave">
        保 存
      </button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { useThemeClass } from '@/composables/useThemeClass'
import { uploadAvatar } from '@/api/user'

const userStore = useUserStore()
const { isDark } = useThemeClass()

const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp'
const saving = ref(false)
const form = reactive({
  nickname: '',
  intro: '',
  webSite: ''
})

function onChooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const filePath = res.tempFilePaths?.[0]
      if (!filePath) return
      uni.showLoading({ title: '上传中...', mask: true })
      try {
        const result = await uploadAvatar(filePath)
        if (result.flag && result.data) {
          userStore.setAvatar(result.data)
          uni.showToast({ title: '头像更新成功', icon: 'success' })
        } else {
          uni.showToast({ title: result.message || '上传失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: e.message || '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

async function handleSave() {
  // 简单校验
  if (!form.nickname.trim()) {
    return uni.showToast({ title: '昵称不能为空', icon: 'none' })
  }
  saving.value = true
  try {
    await userStore.updateUserInfo({
      nickname: form.nickname.trim(),
      intro: form.intro.trim(),
      webSite: form.webSite.trim()
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack({ delta: 1 }), 600)
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onLoad(() => {
  // 初始化表单 = store 当前用户信息
  const u = userStore.userInfo
  if (u) {
    form.nickname = u.nickname || ''
    form.intro = u.intro || ''
    form.webSite = u.webSite || ''
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 32rpx 24rpx 48rpx;
}

/* ========= 头像区 ========= */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 0 48rpx;
}

.avatar-wrap {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.avatar {
  width: 100%;
  height: 100%;
  background: var(--bg-soft);
}

.avatar-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 220ms ease;
}

.avatar-wrap:active .avatar-mask {
  opacity: 1;
}

.avatar-tip {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

/* ========= 表单 ========= */
.form-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
}

.form-label {
  width: 160rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  height: 64rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  text-align: right;
}

.ph {
  color: var(--text-placeholder);
  font-size: 28rpx;
}

.divider {
  height: 2rpx;
  background: var(--border-color-light);
  margin-left: 28rpx;
}

/* ========= 保存按钮 ========= */
.action-area {
  margin-top: 64rpx;
  padding: 0 24rpx;
}

.save-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #42b983 0%, #2d8362 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 24rpx;
  letter-spacing: 8rpx;
  box-shadow: 0 8rpx 20rpx rgba(66, 185, 131, 0.32);
}

.save-btn:active { transform: translateY(2rpx); }
</style>
