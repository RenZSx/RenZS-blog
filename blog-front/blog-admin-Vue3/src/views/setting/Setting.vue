<template>
  <el-card class="main-card">
    <el-tabs v-model="activeName">
      <!-- 修改信息 -->
      <el-tab-pane label="修改信息" name="info">
        <div class="info-container">
          <el-upload
            class="avatar-uploader"
            :action="avatarUploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
          >
            <img v-if="avatar" :src="avatar" class="avatar" />
            <div v-else class="avatar-uploader-icon">
              <el-icon><Plus /></el-icon>
            </div>
          </el-upload>
          <el-form label-width="80px" :model="infoForm" class="info-form">
            <el-form-item label="昵称">
              <el-input v-model="infoForm.nickname" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="infoForm.intro" />
            </el-form-item>
            <el-form-item label="个人网站">
              <el-input v-model="infoForm.webSite" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="infoSaving" @click="handleUpdateInfo">
                修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <el-form label-width="80px" :model="passwordForm" class="password-form">
          <el-form-item label="旧密码">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
              @keyup.enter="handleUpdatePassword"
            />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              @keyup.enter="handleUpdatePassword"
            />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              @keyup.enter="handleUpdatePassword"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="handleUpdatePassword">
              修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup name="BlogSetting">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { updateUserInfo, updateUserPassword } from '@/api/blog/setting'
import { getUploadHeaders } from '@/utils/blog'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const activeName = ref('info')
const infoSaving = ref(false)
const pwdSaving = ref(false)

// 头像上传地址与请求头
const avatarUploadUrl = import.meta.env.VITE_APP_BASE_API + '/users/avatar'
const uploadHeaders = getUploadHeaders()

const avatar = computed(() => userStore.avatar)

const infoForm = reactive({
  nickname: userStore.nickName || '',
  intro: userStore.intro || '',
  webSite: userStore.webSite || ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.flag) {
    userStore.avatar = response.data
    ElMessage.success(response.message || '上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 修改个人信息
const handleUpdateInfo = async () => {
  if (!infoForm.nickname.trim()) {
    ElMessage.error('昵称不能为空')
    return
  }
  infoSaving.value = true
  try {
    await updateUserInfo(infoForm)
    // 同步更新 store 中的用户信息
    userStore.nickName = infoForm.nickname
    userStore.intro = infoForm.intro
    userStore.webSite = infoForm.webSite
    ElMessage.success('修改成功')
  } catch (error) {
    console.error('修改个人信息失败:', error)
  } finally {
    infoSaving.value = false
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordForm.oldPassword.trim()) {
    ElMessage.error('旧密码不能为空')
    return
  }
  if (!passwordForm.newPassword.trim()) {
    ElMessage.error('新密码不能为空')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('新密码不能少于6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  pwdSaving.value = true
  try {
    await updateUserPassword(passwordForm)
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('修改成功')
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    pwdSaving.value = false
  }
}
</script>

<style scoped>
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--el-text-color-placeholder);
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.info-container {
  display: flex;
  align-items: center;
  margin-left: 15%;
  margin-top: 4rem;
}

.info-form {
  width: 320px;
  margin-left: 3rem;
}

.password-form {
  width: 320px;
  margin-top: 2rem;
}
</style>
