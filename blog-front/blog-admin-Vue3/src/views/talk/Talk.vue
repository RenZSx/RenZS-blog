<template>
  <div class="talk-edit">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ talkId ? '编辑说说' : '发布说说' }}</span>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon> 返回
          </el-button>
        </div>
      </template>

      <div class="talk-container">
        <!-- 内容编辑器 -->
        <BlogEditor
          ref="editorRef"
          v-model="talkForm.content"
          placeholder="说点什么吧..."
          class="editor-wrapper"
        />

        <!-- 操作菜单 -->
        <div class="operation-wrapper">
          <div class="left-wrapper">
            <!-- 表情选择 -->
            <el-popover placement="bottom-start" width="460" trigger="click">
              <template #reference>
                <el-icon class="operation-btn"><ChatDotRound /></el-icon>
              </template>
              <div class="emoji-container">
                <span
                  v-for="(value, key) in emojiList"
                  :key="key"
                  class="emoji-item"
                  @click="addEmoji(key, value)"
                >
                  <img :src="value" :title="key" class="emoji" width="24" height="24" />
                </span>
              </div>
            </el-popover>

            <!-- 图片上传 -->
            <el-upload
              :action="baseApi + '/admin/talks/images'"
              :headers="uploadHeaders"
              multiple
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :show-file-list="false"
            >
              <el-icon class="operation-btn"><Picture /></el-icon>
            </el-upload>
          </div>

          <div class="right-wrapper">
            <!-- 是否置顶 -->
            <el-switch
              v-model="talkForm.isTop"
              inactive-text="置顶"
              :active-value="1"
              :inactive-value="0"
              style="margin-right: 16px"
            />

            <!-- 说说状态 -->
            <el-dropdown trigger="click" @command="handleStatusCommand" style="margin-right: 16px">
              <span class="talk-status">
                {{ statusText }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="1">公开</el-dropdown-item>
                  <el-dropdown-item command="2">私密</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 发布按钮 -->
            <el-button
              type="primary"
              size="small"
              @click="handleSubmit"
              :disabled="!talkForm.content.trim()"
              :loading="submitLoading"
            >
              发布
            </el-button>
          </div>
        </div>

        <!-- 图片列表 -->
        <div v-if="uploadList.length > 0" class="talk-image-upload">
          <el-upload
            :action="baseApi + '/admin/talks/images'"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="uploadList"
            multiple
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
          >
            <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Back,
  ChatDotRound,
  Picture,
  ArrowDown,
  Plus
} from '@element-plus/icons-vue'
import { getTalk, saveOrUpdateTalk } from '@/api/blog/talk'
import { getUploadHeaders, compressImage } from '@/utils/blog'
import BlogEditor from '@/components/BlogEditor/index.vue'
import EmojiList from '@/assets/js/emoji'

const router = useRouter()
const route = useRoute()
const baseApi = import.meta.env.VITE_APP_BASE_API
// 后端菜单"修改说说"路径为 /talks/:talkId,参数名必须与之一致
const talkId = ref(route.params.talkId || null)

const editorRef = ref(null)
const submitLoading = ref(false)
const uploadList = ref([])

const talkForm = reactive({
  id: null,
  content: '',
  isTop: 0,
  status: 1,
  images: null
})

const statusList = [
  { status: 1, desc: '公开' },
  { status: 2, desc: '私密' }
]

// 表情列表（从完整表情库配置文件导入）
const emojiList = EmojiList

// 上传请求头
const uploadHeaders = computed(() => getUploadHeaders())

// 状态文本
const statusText = computed(() => {
  const status = statusList.find(item => item.status === talkForm.status)
  return status ? status.desc : '公开'
})

// 获取说说详情
const getTalkDetail = async () => {
  if (!talkId.value) return

  try {
    const res = await getTalk(talkId.value)
    if (res.flag && res.data) {
      Object.assign(talkForm, {
        id: res.data.id,
        content: res.data.content,
        isTop: res.data.isTop,
        status: res.data.status
      })

      // 处理图片列表
      if (res.data.imgList && res.data.imgList.length > 0) {
        uploadList.value = res.data.imgList.map(url => ({ url }))
      }
    }
  } catch (error) {
    console.error('获取说说详情失败:', error)
  }
}

// 添加表情
const addEmoji = (key, value) => {
  if (editorRef.value) {
    const emojiHtml = `<img src="${value}" width="24" height="24" alt="${key}" style="margin: 0 1px; vertical-align: text-bottom;" />`
    editorRef.value.addText(emojiHtml)
  }
}

// 上传前处理
const beforeUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  const maxSize = 5 * 1024 // 5MB
  if (file.size / 1024 > maxSize) {
    try {
      return await compressImage(file, 0.8)
    } catch (error) {
      console.error('图片压缩失败:', error)
      return file
    }
  }

  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.flag && response.data) {
    uploadList.value.push({ url: response.data })
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 移除图片
const handleRemove = (file) => {
  const index = uploadList.value.findIndex(item => item.url === file.url)
  if (index > -1) {
    uploadList.value.splice(index, 1)
  }
}

// 状态命令
const handleStatusCommand = (command) => {
  talkForm.status = parseInt(command)
}

// 提交表单
const handleSubmit = async () => {
  if (!talkForm.content.trim()) {
    ElMessage.error('说说内容不能为空')
    return
  }

  submitLoading.value = true

  try {
    // 处理图片
    if (uploadList.value.length > 0) {
      const imgList = uploadList.value.map(item => item.url)
      talkForm.images = JSON.stringify(imgList)
    } else {
      talkForm.images = null
    }

    const res = await saveOrUpdateTalk(talkForm)
    if (res.flag) {
      ElMessage.success(talkId.value ? '修改成功' : '发布成功')
      // 清空表单
      if (!talkId.value && editorRef.value) {
        editorRef.value.clear()
        uploadList.value = []
        talkForm.content = ''
        talkForm.isTop = 0
        talkForm.status = 1
      } else {
        router.back()
      }
    }
  } catch (error) {
    console.error('保存说说失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

onMounted(() => {
  getTalkDetail()
})
</script>

<style scoped lang="scss">
.talk-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.talk-container {
  margin-top: 20px;
}

.editor-wrapper {
  min-height: 150px;
}

.operation-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.left-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.right-wrapper {
  display: flex;
  align-items: center;
}

.operation-btn {
  cursor: pointer;
  color: var(--el-text-color-secondary);
  font-size: 20px;
  transition: color 0.3s;

  &:hover {
    color: var(--el-color-primary);
  }
}

.talk-status {
  cursor: pointer;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover {
    color: var(--el-color-primary);
  }
}

.emoji-container {
  max-height: 300px;
  overflow-y: auto;
}

.emoji-item {
  cursor: pointer;
  display: inline-block;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.emoji {
  user-select: none;
  display: inline-block;
  vertical-align: middle;
}

.talk-image-upload {
  margin-top: 16px;
}
</style>
