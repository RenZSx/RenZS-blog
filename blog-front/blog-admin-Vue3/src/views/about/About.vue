<template>
  <div class="about-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>关于管理</span>
        </div>
      </template>

      <div class="editor-container">
        <MdEditor
          v-model="aboutContent"
          :height="500"
        />
      </div>

      <div class="button-container">
        <el-button type="primary" @click="handleUpdate" :loading="loading">
          <el-icon><Check /></el-icon> 保存
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getAbout, updateAbout } from '@/api/blog/website'
import MdEditor from '@/components/MdEditor/index.vue'

const loading = ref(false)
const aboutContent = ref('')

const getAboutInfo = async () => {
  try {
    const res = await getAbout()
    if (res.flag) {
      aboutContent.value = res.data || ''
    }
  } catch (error) {
    console.error('获取关于信息失败:', error)
  }
}

const handleUpdate = async () => {
  if (!aboutContent.value || aboutContent.value.trim() === '') {
    ElMessage.warning('内容不能为空')
    return
  }

  loading.value = true
  try {
    const res = await updateAbout({ aboutContent: aboutContent.value })
    if (res.flag) {
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error('保存关于信息失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getAboutInfo()
})
</script>

<style scoped lang="scss">
.about-management {
  padding: 20px;
}

.about-management :deep(.md-editor) {
  --md-color: var(--el-text-color-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-container {
  margin-top: 20px;
}

.button-container {
  margin-top: 20px;
  text-align: right;
}
</style>
