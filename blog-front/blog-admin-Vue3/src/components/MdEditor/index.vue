<template>
  <div class="md-editor-container">
    <MdEditor
      :model-value="modelValue"
      :theme="settingsStore.isDark ? 'dark' : 'light'"
      :style="{ height: height + 'px' }"
      :placeholder="placeholder"
      :on-upload-img="handleUploadImg"
      @update:model-value="handleChange"
    />
  </div>
</template>

<script setup>
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getToken } from '@/utils/auth'
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()

defineProps({
  /* 编辑器的内容(Markdown) */
  modelValue: {
    type: String,
    default: ''
  },
  /* 编辑器高度(px) */
  height: {
    type: Number,
    default: 600
  },
  /* 占位提示 */
  placeholder: {
    type: String,
    default: '请输入内容'
  }
})

const emit = defineEmits(['update:modelValue'])

// 内容变化时同步 v-model
const handleChange = (value) => {
  emit('update:modelValue', value)
}

// 图片上传: 上传到后端文章图片接口, 成功后在编辑器中插入图片链接
const handleUploadImg = async (files, callback) => {
  const urls = []
  for (const file of files) {
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await axios.post(
        import.meta.env.VITE_APP_BASE_API + '/admin/articles/images',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: 'Bearer ' + getToken()
          }
        }
      )
      if (res.data.flag) {
        // 后端返回 { flag, data: url }
        urls.push(res.data.data)
      } else {
        ElMessage.error(res.data.message || '图片上传失败')
      }
    } catch (error) {
      ElMessage.error('图片上传失败')
      console.error('文章图片上传失败:', error)
    }
  }
  // 全部上传完成后一次性插入
  if (urls.length) {
    callback(urls)
  }
}
</script>

<style scoped>
.md-editor-container {
  width: 100%;
}
</style>
