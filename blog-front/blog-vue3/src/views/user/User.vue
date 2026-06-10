<template>
  <div>
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">个人中心</h1>
    </div>
    <v-card class="blog-container user-container">
      <div class="user-tabs">
        <button
          type="button"
          class="user-tab-btn"
          :class="{ 'user-tab-btn--active': activeTab === 'profile' }"
          @click="activeTab = 'profile'"
        >
          基本信息
        </button>
        <button
          type="button"
          class="user-tab-btn"
          :class="{ 'user-tab-btn--active': activeTab === 'notices' }"
          @click="activeTab = 'notices'"
        >
          <span class="user-tab-btn__label">我的通知</span>
          <span v-if="hasNoticeBadge" class="user-tab-btn__badge">
            {{ noticeBadgeText }}
          </span>
        </button>
        <button
          type="button"
          class="user-tab-btn"
          :class="{ 'user-tab-btn--active': activeTab === 'collects' }"
          @click="activeTab = 'collects'"
        >
          我的收藏
        </button>
        <button
          type="button"
          class="user-tab-btn"
          :class="{ 'user-tab-btn--active': activeTab === 'history' }"
          @click="activeTab = 'history'"
        >
          最近阅读
        </button>
      </div>

      <div v-if="activeTab === 'profile'">
        <div>
          <span class="info-title">基本信息</span>
        </div>
        <v-row class="info-wrapper">
          <v-col md="3" cols="12" class="avatar-col">
            <div class="avatar-wrapper">
              <v-avatar size="140">
                <v-img :src="userStore.avatar || defaultAvatar" />
              </v-avatar>
              <v-btn
                class="change-avatar-btn"
                size="small"
                color="primary"
                variant="outlined"
                @click="showAvatarDialog = true"
              >
                更换头像
              </v-btn>
            </div>
          </v-col>
          <v-col md="7" cols="12">
            <v-text-field
              v-model="userInfo.nickname"
              label="昵称"
              placeholder="请输入您的昵称"
              variant="outlined"
            />
            <v-text-field
              v-model="userInfo.webSite"
              class="mt-4"
              label="个人网站"
              placeholder="http://你的网址"
              variant="outlined"
            />
            <v-text-field
              v-model="userInfo.intro"
              class="mt-4"
              label="简介"
              placeholder="介绍下自己吧"
              variant="outlined"
            />
            <div v-if="loginType !== 0" class="mt-4 binding-wrapper">
              <v-text-field
                disabled
                :model-value="userStore.email || '未绑定'"
                label="邮箱号"
                placeholder="请绑定邮箱"
                variant="outlined"
              />
              <v-btn
                color="primary"
                variant="text"
                size="small"
                @click="openEmailDialog"
              >
                {{ userStore.email ? '修改绑定' : '绑定邮箱' }}
              </v-btn>
            </div>
            <v-btn
              @click="updateUserInfo"
              color="primary"
              variant="outlined"
              class="mt-5"
            >
              修改
            </v-btn>
          </v-col>
        </v-row>
      </div>

      <div v-else-if="activeTab === 'notices'">
        <UserNoticePanel />
      </div>

      <div v-else-if="activeTab === 'collects'">
        <UserCollectPanel />
      </div>

      <div v-else-if="activeTab === 'history'">
        <UserHistoryPanel />
      </div>
    </v-card>

    <!-- 头像上传对话框 -->
    <v-dialog v-model="showAvatarDialog" max-width="460">
      <v-card class="avatar-dialog-card">
        <v-card-title class="avatar-dialog-title">更换头像</v-card-title>
        <v-card-text>
          <div class="avatar-mode-tabs">
            <button
              type="button"
              class="avatar-mode-tab"
              :class="{ 'avatar-mode-tab--active': avatarMode === 'file' }"
              @click="avatarMode = 'file'"
            >
              本地选择
            </button>
            <button
              type="button"
              class="avatar-mode-tab"
              :class="{ 'avatar-mode-tab--active': avatarMode === 'link' }"
              @click="avatarMode = 'link'"
            >
              图片链接
            </button>
          </div>

          <div v-if="avatarMode === 'file'" class="avatar-upload-panel">
            <label class="avatar-file-picker">
              <input type="file" accept="image/*" @change="handleAvatarFileChange" />
              <span class="avatar-file-picker__title">选择一张头像图片</span>
              <span class="avatar-file-picker__desc">
                支持 jpg、png、webp 等常见图片格式
              </span>
            </label>
            <div v-if="selectedAvatarFile" class="avatar-file-name">
              已选择：{{ selectedAvatarFile.name }}
            </div>
          </div>

          <v-text-field
            v-else
            v-model="avatarUrl"
            label="头像链接"
            placeholder="请输入头像图片链接"
            variant="outlined"
          />

          <div class="avatar-preview">
            <v-avatar size="80">
              <v-img :src="avatarPreview" />
            </v-avatar>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey" variant="text" @click="showAvatarDialog = false">
            取消
          </v-btn>
          <v-btn
            color="primary"
            variant="text"
            :loading="avatarSubmitting"
            @click="avatarMode === 'file' ? submitAvatarFile() : submitAvatarLink()"
          >
            确定
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useNoticeStore } from '@/stores/notice'
import { useUserStore } from '@/stores/user'
import { useUIStore } from '@/stores/ui'
import {
  updateUserInfo as updateUserInfoRequest,
  updateAvatarFile,
  updateAvatarLink
} from '@/api/user'
import { useToast } from '@/composables/useToast'
import UserNoticePanel from './components/UserNoticePanel.vue'
import UserCollectPanel from './components/UserCollectPanel.vue'
import UserHistoryPanel from './components/UserHistoryPanel.vue'

const blogInfoStore = useBlogInfoStore()
const userStore = useUserStore()
const noticeStore = useNoticeStore()
const uiStore = useUIStore()
const route = useRoute()

const defaultAvatar = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig?.touristAvatar || ''
})

const loginType = computed(() => userStore.loginType || 0)
// “我的通知”页签的角标直接跟随通知总未读数，保证个人中心和顶部导航一致。
const hasNoticeBadge = computed(() => userStore.isLoggedIn && noticeStore.unreadCount > 0)
// 角标文案与顶部导航保持一致，超出两位数后统一折叠显示。
const noticeBadgeText = computed(() => {
  const unreadCount = noticeStore.unreadCount
  return unreadCount > 99 ? '99+' : String(unreadCount)
})

const userInfo = ref({
  nickname: userStore.nickname || '',
  intro: userStore.intro || '',
  webSite: userStore.webSite || ''
})

const showAvatarDialog = ref(false)
const avatarMode = ref<'file' | 'link'>('file')
const avatarUrl = ref('')
const selectedAvatarFile = ref<File | null>(null)
const selectedAvatarPreview = ref('')
const avatarSubmitting = ref(false)
const activeTab = ref<'profile' | 'notices' | 'collects' | 'history'>('profile')

const avatarPreview = computed(() => {
  if (avatarMode.value === 'file') {
    return selectedAvatarPreview.value || userStore.avatar || defaultAvatar.value
  }
  return avatarUrl.value.trim() || userStore.avatar || defaultAvatar.value
})

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const userPage = pageList.find(item => item.pageLabel === 'user')
  const coverUrl = userPage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

async function updateUserInfo() {
  try {
    const { data } = await updateUserInfoRequest(userInfo.value)
    if (data.flag) {
      userStore.updateUserInfo(userInfo.value)
      useToast({ type: 'success', message: '修改成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    console.error('修改用户信息失败:', error)
  }
}

function handleAvatarFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file) {
    selectedAvatarFile.value = null
    selectedAvatarPreview.value = ''
    return
  }

  if (!file.type.startsWith('image/')) {
    selectedAvatarFile.value = null
    selectedAvatarPreview.value = ''
    input.value = ''
    useToast({ type: 'error', message: '请选择图片文件' })
    return
  }

  selectedAvatarFile.value = file
  selectedAvatarPreview.value = URL.createObjectURL(file)
}

async function submitAvatarFile() {
  if (!selectedAvatarFile.value) {
    useToast({ type: 'error', message: '请选择头像图片' })
    return
  }

  avatarSubmitting.value = true
  try {
    const { data } = await updateAvatarFile(selectedAvatarFile.value)
    if (data.flag) {
      userStore.updateAvatar(data.data)
      resetAvatarDialog()
      useToast({ type: 'success', message: '头像更新成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    console.error('上传头像失败:', error)
  } finally {
    avatarSubmitting.value = false
  }
}

async function submitAvatarLink() {
  if (!avatarUrl.value.trim()) {
    useToast({ type: 'error', message: '请输入头像链接' })
    return
  }

  avatarSubmitting.value = true
  try {
    const { data } = await updateAvatarLink(avatarUrl.value.trim())
    if (data.flag) {
      userStore.updateAvatar(data.data || avatarUrl.value)
      resetAvatarDialog()
      useToast({ type: 'success', message: '头像更新成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    console.error('更新头像失败:', error)
  } finally {
    avatarSubmitting.value = false
  }
}

function resetAvatarDialog() {
  showAvatarDialog.value = false
  avatarMode.value = 'file'
  avatarUrl.value = ''
  selectedAvatarFile.value = null
  selectedAvatarPreview.value = ''
}

function openEmailDialog() {
  uiStore.setEmailFlag(true)
}

watch(
  () => route.query.tab,
  (tab) => {
    // 路由参数只作为外部跳转兜底入口使用，例如系统通知直接落到“我的通知”页签。
    if (tab === 'notices' || tab === 'collects' || tab === 'history') {
      activeTab.value = tab
      return
    }

    activeTab.value = 'profile'
  },
  { immediate: true }
)

onMounted(() => {
  userInfo.value = {
    nickname: userStore.nickname || '',
    intro: userStore.intro || '',
    webSite: userStore.webSite || ''
  }
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 380px;
  color: #eee;
}

.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-title {
  position: absolute;
  bottom: 50px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.user-container {
  padding: 30px 40px;
  margin: 20px auto 40px !important;
  max-width: 860px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.info-title {
  font-size: 1.25rem;
  font-weight: bold;
}

.user-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--card-border-accent);
}

.user-tab-btn {
  position: relative;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.user-tab-btn--active {
  border-color: var(--card-border-accent);
  background: var(--card-surface);
  color: var(--primary-color);
}

.user-tab-btn__label {
  display: inline-flex;
  align-items: center;
}

.user-tab-btn__badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  box-shadow: 0 0 0 2px var(--card-surface-elevated);
}

:global(.dark) .user-tab-btn__badge {
  box-shadow: 0 0 0 2px var(--surface-raised);
}

.info-wrapper {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-col {
  display: flex;
  justify-content: center;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 18px 20px;
  border: 1px solid var(--glass-border);
  border-radius: var(--card-radius-md);
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.change-avatar-btn {
  margin-top: 10px;
}

.binding-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-preview {
  display: flex;
  justify-content: center;
  margin-top: 15px;
}

.avatar-dialog-card {
  border-radius: var(--card-radius-lg) !important;
}

.avatar-dialog-title {
  font-weight: 700;
}

.avatar-mode-tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 18px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
}

.avatar-mode-tab {
  padding: 9px 12px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-weight: 700;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.avatar-mode-tab--active {
  background: var(--card-surface);
  color: var(--primary-color);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.1);
}

.avatar-upload-panel {
  display: grid;
  gap: 10px;
}

.avatar-file-picker {
  display: grid;
  gap: 6px;
  padding: 20px;
  border: 1px dashed var(--card-border-accent);
  border-radius: var(--card-radius-md);
  background: var(--card-surface);
  color: var(--text-secondary);
  cursor: pointer;
  text-align: center;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.avatar-file-picker:hover {
  border-color: var(--primary-color);
  box-shadow: 0 12px 28px rgba(73, 177, 245, 0.12);
}

.avatar-file-picker input {
  display: none;
}

.avatar-file-picker__title {
  color: var(--text-primary);
  font-weight: 700;
}

.avatar-file-picker__desc,
.avatar-file-name {
  font-size: 13px;
}

.avatar-file-name {
  color: var(--text-secondary);
  text-align: center;
}

.avatar-preview :deep(.v-avatar),
.avatar-wrapper :deep(.v-avatar) {
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.16);
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .user-container {
    margin: 20px 10px 20px;
    padding: 20px;
  }

  .user-tabs {
    gap: 10px;
    overflow-x: auto;
  }

  .info-wrapper {
    flex-direction: column;
  }
}
</style>
