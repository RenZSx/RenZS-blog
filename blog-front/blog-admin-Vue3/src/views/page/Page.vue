<template>
  <div class="page-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>页面管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <!-- 页面列表 -->
      <el-row class="page-container" :gutter="12" v-loading="loading">
        <el-empty v-if="pageList.length === 0" description="暂无页面" />
        <el-col v-for="item in pageList" :key="item.id" :md="6">
          <div class="page-item">
            <div class="page-operation">
              <el-dropdown @command="handleCommand">
                <el-icon style="color: #fff; font-size: 20px"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ type: 'update', data: item }">
                      <el-icon><Edit /></el-icon> 编辑
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ type: 'delete', data: item }">
                      <el-icon><Delete /></el-icon> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <el-image fit="cover" class="page-cover" :src="item.pageCover" />
            <div class="page-name">{{ item.pageName }}</div>
            <div class="page-label">{{ item.pageLabel }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="页面名称" prop="pageName">
          <el-input v-model="form.pageName" placeholder="请输入页面名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="页面标签" prop="pageLabel">
          <el-input v-model="form.pageLabel" placeholder="请输入页面标签" maxlength="20" />
        </el-form-item>
        <el-form-item label="页面封面" prop="pageCover">
          <el-upload
            class="upload-cover"
            drag
            :show-file-list="false"
            :action="uploadUrl"
            :headers="headers"
            :on-success="handleUploadSuccess"
            :before-upload="handleBeforeUpload"
          >
            <el-icon v-if="!form.pageCover" class="el-icon--upload"><UploadFilled /></el-icon>
            <div v-if="!form.pageCover" class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <img v-else :src="form.pageCover" style="width: 100%; height: 180px; object-fit: cover" />
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Edit, MoreFilled, UploadFilled } from '@element-plus/icons-vue'
import { listPages, saveOrUpdatePage, deletePage } from '@/api/blog/page'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const submitLoading = ref(false)
const pageList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/admin/config/images')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

const form = reactive({
  id: null,
  pageName: '',
  pageLabel: '',
  pageCover: ''
})

const rules = {
  pageName: [
    { required: true, message: '页面名称不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '页面名称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  pageLabel: [
    { required: true, message: '页面标签不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '页面标签长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  pageCover: [
    { required: true, message: '页面封面不能为空', trigger: 'change' }
  ]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listPages()
    if (res.flag) {
      pageList.value = res.data || []
    }
  } catch (error) {
    console.error('获取页面列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增页面'
  dialogVisible.value = true
  resetForm()
}

const handleUpdate = (row) => {
  dialogTitle.value = '编辑页面'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`是否确认删除页面"${row.pageName}"?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePage(row.id)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除页面失败:', error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await saveOrUpdatePage(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存页面失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleCommand = (command) => {
  if (command.type === 'update') {
    handleUpdate(command.data)
  } else if (command.type === 'delete') {
    handleDelete(command.data)
  }
}

const handleClose = () => {
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.pageName = ''
  form.pageLabel = ''
  form.pageCover = ''
  formRef.value?.resetFields()
}

const handleBeforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.flag) {
    form.pageCover = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.page-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-container {
  margin-top: 20px;
}

.page-item {
  position: relative;
  margin-bottom: 1rem;
}

.page-cover {
  position: relative;
  border-radius: 4px;
  width: 100%;
  height: 170px;
}

.page-name {
  text-align: center;
  margin-top: 0.5rem;
  font-size: 14px;
  font-weight: bold;
}

.page-label {
  text-align: center;
  margin-top: 0.3rem;
  font-size: 12px;
  color: #909399;
}

.page-operation {
  position: absolute;
  z-index: 100;
  top: 0.5rem;
  right: 0.8rem;
  cursor: pointer;
}

.upload-cover {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
  }
}
</style>
