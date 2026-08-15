<template>
  <el-card class="main-card love-card">
    <div class="title">纪念页管理</div>
    <div class="love-tip">这里统一维护纪念页基础配置和飞书传信正文。</div>

    <el-tabs v-model="activeName">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="config">
        <el-form
          ref="loveConfigFormRef"
          :model="loveConfigForm"
          :rules="rules"
          label-width="110px"
          label-position="left"
          class="love-form"
        >
          <el-form-item label="页面标题" prop="title">
            <el-input
              v-model="loveConfigForm.title"
              style="width: 420px"
              maxlength="100"
              show-word-limit
              placeholder="请输入纪念页标题"
            />
          </el-form-item>
          <el-form-item label="页面副标题" prop="subtitle">
            <el-input
              v-model="loveConfigForm.subtitle"
              style="width: 420px"
              maxlength="255"
              show-word-limit
              placeholder="请输入纪念页副标题"
            />
          </el-form-item>
          <el-form-item label="背景图片" prop="background">
            <el-upload
              class="love-background-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleBackgroundSuccess"
            >
              <img
                v-if="loveConfigForm.background"
                :src="loveConfigForm.background"
                class="love-background-preview"
              />
              <div v-else class="love-background-icon">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="loveConfigForm.startTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="datetime"
              placeholder="请选择开始时间"
              style="width: 420px"
            />
          </el-form-item>
          <el-form-item label="纪念日时间" prop="anniversaryTime">
            <el-date-picker
              v-model="loveConfigForm.anniversaryTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="datetime"
              placeholder="请选择纪念日时间"
              style="width: 420px"
            />
          </el-form-item>
          <el-form-item label="纪念日标题" prop="anniversaryTitle">
            <el-input
              v-model="loveConfigForm.anniversaryTitle"
              style="width: 420px"
              maxlength="100"
              show-word-limit
              placeholder="请输入纪念日标题"
            />
          </el-form-item>
          <el-form-item label="是否启用" prop="isEnabled">
            <el-radio-group v-model="loveConfigForm.isEnabled">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveLoveConfig">
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 飞书传信 -->
      <el-tab-pane label="飞书传信" name="letter">
        <el-form
          :model="letterForm"
          label-width="110px"
          label-position="left"
          class="love-form"
        >
          <el-form-item label="信件标题">
            <el-input
              v-model="letterForm.letterTitle"
              style="width: 420px"
              maxlength="100"
              show-word-limit
              placeholder="请输入信件标题"
            />
          </el-form-item>
          <el-form-item label="信件正文">
            <el-input
              v-model="letterForm.letterContent"
              type="textarea"
              :rows="12"
              style="width: 520px"
              maxlength="5000"
              show-word-limit
              placeholder="请输入信件正文，支持 HTML 内容"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="letterSaving" @click="saveLoveLetter">
              保存信件
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup name="BlogLove">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getLoveConfig,
  updateLoveConfig,
  getLoveLetter,
  updateLoveLetter
} from '@/api/blog/love'
import { getUploadHeaders } from '@/utils/blog'
import { createDefaultLetterForm, normalizeLetterForm } from './letterEditor'

const activeName = ref('config')
const saving = ref(false)
const letterSaving = ref(false)
const loveConfigFormRef = ref(null)

// 背景图上传地址与请求头
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/admin/config/images'
const uploadHeaders = getUploadHeaders()

const loveConfigForm = reactive({
  title: '',
  subtitle: '',
  background: '',
  startTime: '',
  anniversaryTime: '',
  anniversaryTitle: '',
  isEnabled: 1
})

const letterForm = reactive(createDefaultLetterForm())

const rules = {
  title: [{ required: true, message: '请输入页面标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

// 拉取纪念页基础配置
const fetchLoveConfig = async () => {
  try {
    const res = await getLoveConfig()
    if (res.flag && res.data) {
      Object.assign(loveConfigForm, res.data)
    }
  } catch (error) {
    console.error('加载纪念页配置失败:', error)
  }
}

// 拉取飞书传信正文
const fetchLoveLetter = async () => {
  try {
    const res = await getLoveLetter()
    Object.assign(letterForm, normalizeLetterForm(res.flag ? res.data : null))
  } catch (error) {
    console.error('加载飞书传信失败:', error)
  }
}

// 背景图上传成功后回填地址
const handleBackgroundSuccess = (response) => {
  if (response.flag) {
    loveConfigForm.background = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 保存纪念页基础配置
const saveLoveConfig = () => {
  loveConfigFormRef.value?.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      await updateLoveConfig(loveConfigForm)
      ElNotification.success({ title: '成功', message: '保存成功' })
    } catch (error) {
      console.error('保存纪念页配置失败:', error)
    } finally {
      saving.value = false
    }
  })
}

// 保存飞书传信正文
const saveLoveLetter = async () => {
  letterSaving.value = true
  try {
    await updateLoveLetter(letterForm)
    ElNotification.success({ title: '成功', message: '保存成功' })
  } catch (error) {
    console.error('保存飞书传信失败:', error)
  } finally {
    letterSaving.value = false
  }
}

onMounted(() => {
  fetchLoveConfig()
  fetchLoveLetter()
})
</script>

<style scoped>
.title {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.love-card {
  min-height: calc(100vh - 120px);
}

.love-tip {
  margin: 20px 0 2px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.love-form {
  max-width: 780px;
}

.love-background-uploader :deep(.el-upload) {
  width: 320px;
  height: 180px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background: var(--el-fill-color-blank);
}

.love-background-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.love-background-icon {
  width: 320px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: var(--el-text-color-placeholder);
}

.love-background-preview {
  width: 320px;
  height: 180px;
  display: block;
  object-fit: cover;
}
</style>
