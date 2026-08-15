<template>
  <div class="album-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>相册管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入相册名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleQuery" style="margin-left: 10px">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
      </div>

      <!-- 相册列表 -->
      <el-row class="album-container" :gutter="12" v-loading="loading">
        <el-empty v-if="albumList.length === 0" description="暂无相册" />
        <el-col v-for="item in albumList" :key="item.id" :md="6">
          <div class="album-item" @click="handleViewPhotos(item)">
            <div class="album-operation">
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
            <div class="album-photo-count">
              <div>{{ item.photoCount }}</div>
              <el-icon v-if="item.status === 2" style="font-size: 18px"><Lock /></el-icon>
            </div>
            <el-image fit="cover" class="album-cover" :src="item.albumCover" />
            <div class="album-name">{{ item.albumName }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        :current-page="queryParams.current"
        :page-size="queryParams.size"
        :total="total"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="相册名称" prop="albumName">
          <el-input v-model="form.albumName" placeholder="请输入相册名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="相册描述" prop="albumDesc">
          <el-input v-model="form.albumDesc" placeholder="请输入相册描述" maxlength="50" />
        </el-form-item>
        <el-form-item label="相册封面" prop="albumCover">
          <el-upload
            class="upload-cover"
            drag
            :show-file-list="false"
            :action="uploadUrl"
            :headers="headers"
            :on-success="handleUploadSuccess"
            :before-upload="handleBeforeUpload"
          >
            <el-icon v-if="!form.albumCover" class="el-icon--upload"><UploadFilled /></el-icon>
            <div v-if="!form.albumCover" class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <img v-else :src="form.albumCover" style="width: 100%; height: 180px; object-fit: cover" />
          </el-upload>
        </el-form-item>
        <el-form-item label="发布形式" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">公开</el-radio>
            <el-radio :label="2">私密</el-radio>
          </el-radio-group>
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
import { useRouter } from 'vue-router'
import { Plus, Search, Refresh, Delete, Edit, MoreFilled, Lock, UploadFilled } from '@element-plus/icons-vue'
import { listAlbums, getAlbum, saveOrUpdateAlbum, deleteAlbum } from '@/api/blog/album'
import { getToken } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const albumList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/admin/photos/albums/cover')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

const queryParams = reactive({
  current: 1,
  size: 8,
  keywords: ''
})

const form = reactive({
  id: null,
  albumName: '',
  albumDesc: '',
  albumCover: '',
  status: 1
})

const rules = {
  albumName: [
    { required: true, message: '相册名称不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '相册名称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  albumDesc: [
    { required: true, message: '相册描述不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '相册描述长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  albumCover: [
    { required: true, message: '相册封面不能为空', trigger: 'change' }
  ]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listAlbums(queryParams)
    if (res.flag) {
      albumList.value = res.data.recordList || []
      total.value = res.data.count || 0
    }
  } catch (error) {
    console.error('获取相册列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.current = 1
  getList()
}

const handleReset = () => {
  queryParams.current = 1
  queryParams.keywords = ''
  getList()
}

const handleAdd = () => {
  dialogTitle.value = '新增相册'
  dialogVisible.value = true
  resetForm()
}

const handleUpdate = async (row) => {
  dialogTitle.value = '编辑相册'
  try {
    const res = await getAlbum(row.id)
    if (res.flag) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取相册详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`是否确认删除相册"${row.albumName}"?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteAlbum(row.id)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除相册失败:', error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await saveOrUpdateAlbum(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存相册失败:', error)
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

const handleViewPhotos = (album) => {
  router.push({ path: `/albums/${album.id}` })
}

const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  getList()
}

const handleClose = () => {
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.albumName = ''
  form.albumDesc = ''
  form.albumCover = ''
  form.status = 1
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
    form.albumCover = response.data
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
.album-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  align-items: center;
}

.album-container {
  margin-top: 20px;
}

.album-item {
  position: relative;
  cursor: pointer;
  margin-bottom: 1rem;
}

.album-cover {
  position: relative;
  border-radius: 4px;
  width: 100%;
  height: 170px;
}

.album-cover::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.album-photo-count {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 1.5rem;
  z-index: 100;
  position: absolute;
  left: 0;
  right: 0;
  padding: 0 0.5rem;
  bottom: 2.6rem;
  color: #fff;
}

.album-name {
  text-align: center;
  margin-top: 0.5rem;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.album-operation {
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
