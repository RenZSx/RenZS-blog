<template>
  <div class="photo-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>照片管理</span>
          <el-button type="primary" size="small" @click="handleBack">
            <el-icon><Back /></el-icon> 返回
          </el-button>
        </div>
      </template>

      <!-- 相册信息 -->
      <div class="album-info">
        <el-image fit="cover" class="album-cover" :src="albumInfo.albumCover" />
        <div class="album-detail">
          <div style="margin-bottom: 0.6rem">
            <span class="album-name">{{ albumInfo.albumName }}</span>
            <span class="photo-count">{{ albumInfo.photoCount }}张</span>
          </div>
          <div>
            <span v-if="albumInfo.albumDesc" class="album-desc">
              {{ albumInfo.albumDesc }}
            </span>
            <el-button type="primary" size="small" @click="uploadDialogVisible = true">
              <el-icon><Picture /></el-icon> 上传照片
            </el-button>
          </div>
        </div>
        <!-- 相册操作 -->
        <div class="operation">
          <div class="all-check">
            <el-checkbox
              :indeterminate="isIndeterminate"
              v-model="checkAll"
              @change="handleCheckAllChange"
            >
              全选
            </el-checkbox>
            <div class="check-count">已选择{{ selectPhotoIds.length }}张</div>
          </div>
          <el-button
            type="success"
            @click="moveDialogVisible = true"
            :disabled="selectPhotoIds.length === 0"
            size="small"
          >
            <el-icon><Folder /></el-icon> 移动到
          </el-button>
          <el-button
            type="danger"
            @click="handleBatchDelete"
            :disabled="selectPhotoIds.length === 0"
            size="small"
          >
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
        </div>
      </div>

      <!-- 照片列表 -->
      <el-row class="photo-container" :gutter="16" v-loading="loading">
        <el-empty v-if="photoList.length === 0" description="暂无照片" />
        <el-col
          v-for="item in photoList"
          :key="item.id"
          :md="4"
          :sm="6"
          :xs="12"
        >
          <el-checkbox
            :value="item.id"
            :model-value="selectPhotoIds.includes(item.id)"
            class="photo-checkbox"
            @change="(checked) => handlePhotoCheck(item.id, checked)"
          >
            <div class="photo-item">
              <div class="photo-operation">
                <el-dropdown @command="handleCommand">
                  <el-icon style="color: #fff; font-size: 20px"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ type: 'edit', data: item }">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <el-image
                fit="cover"
                class="photo-img"
                :src="item.photoSrc"
                :preview-src-list="photoList.map(p => p.photoSrc)"
                :preview-teleported="true"
              />
              <div class="photo-name">{{ item.photoDesc || '未命名' }}</div>
            </div>
          </el-checkbox>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-show="total > 0"
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 上传照片对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传照片"
      width="70%"
      @close="handleUploadClose"
    >
      <div class="upload-container">
        <ImageUpload
          v-model="uploadPhotos"
          :limit="20"
          :file-size="5"
          action="/admin/photos/albums/cover"
        />
      </div>
      <template #footer>
        <div class="upload-footer">
          <div class="upload-count">共上传{{ uploadPhotos ? uploadPhotos.split(',').filter(p => p).length : 0 }}张照片</div>
          <div style="margin-left: auto">
            <el-button @click="uploadDialogVisible = false">取 消</el-button>
            <el-button
              type="primary"
              @click="handleSavePhotos"
              :loading="uploadLoading"
              :disabled="!uploadPhotos"
            >
              开始上传
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑照片对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改信息"
      width="500px"
      @close="handleEditClose"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="照片名称" prop="photoName">
          <el-input v-model="editForm.photoName" placeholder="请输入照片名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="照片描述" prop="photoDesc">
          <el-input v-model="editForm.photoDesc" placeholder="请输入照片描述" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUpdatePhoto" :loading="editLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>

    <!-- 移动照片对话框 -->
    <el-dialog
      v-model="moveDialogVisible"
      title="移动照片"
      width="500px"
    >
      <el-empty v-if="albumList.length < 2" description="暂无其他相册" />
      <el-form v-else label-width="90px" :model="moveForm">
        <el-form-item label="目标相册">
          <el-select v-model="moveForm.albumId" placeholder="请选择相册" style="width: 100%">
            <el-option
              v-for="album in albumList.filter(a => a.id !== albumInfo.id)"
              :key="album.id"
              :label="album.albumName"
              :value="album.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click="handleMovePhotos"
          :loading="moveLoading"
          :disabled="!moveForm.albumId"
        >
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { Back, Picture, Delete, Edit, MoreFilled, Folder } from '@element-plus/icons-vue'
import { listPhotos, savePhotos, updatePhoto, deletePhotos, movePhotos, listAlbums, getAlbum } from '@/api/blog/album'
import ImageUpload from '@/components/ImageUpload/index.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const uploadLoading = ref(false)
const editLoading = ref(false)
const moveLoading = ref(false)
const total = ref(0)
const photoList = ref([])
const albumInfo = ref({})
const albumList = ref([])
const uploadDialogVisible = ref(false)
const editDialogVisible = ref(false)
const moveDialogVisible = ref(false)
const selectPhotoIds = ref([])
const checkAll = ref(false)
const uploadPhotos = ref('')
const editFormRef = ref(null)

// 后端菜单"照片管理"路径为 /albums/:albumId,参数名必须与之一致
const albumId = computed(() => route.params.albumId)

const queryParams = reactive({
  current: 1,
  size: 12,
  albumId: albumId.value
})

const editForm = reactive({
  id: null,
  photoName: '',
  photoDesc: ''
})

const moveForm = reactive({
  albumId: null
})

const editRules = {
  photoName: [
    { required: true, message: '照片名称不能为空', trigger: 'blur' },
    { min: 1, max: 30, message: '照片名称长度在 1 到 30 个字符', trigger: 'blur' }
  ]
}

const isIndeterminate = computed(() => {
  return selectPhotoIds.value.length > 0 && selectPhotoIds.value.length < photoList.value.length
})

const getAlbumInfo = async () => {
  try {
    const res = await getAlbum(albumId.value)
    if (res.flag) {
      albumInfo.value = res.data
    }
  } catch (error) {
    console.error('获取相册信息失败:', error)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listPhotos(queryParams)
    if (res.flag) {
      photoList.value = res.data.recordList || []
      total.value = res.data.count || 0
    }
  } catch (error) {
    console.error('获取照片列表失败:', error)
  } finally {
    loading.value = false
  }
}

const getAlbumList = async () => {
  try {
    const res = await listAlbums({ current: 1, size: 100 })
    if (res.flag) {
      albumList.value = res.data.recordList || []
    }
  } catch (error) {
    console.error('获取相册列表失败:', error)
  }
}

const handleBack = () => {
  router.push('/albums')
}

const handleCheckAllChange = (val) => {
  selectPhotoIds.value = val ? photoList.value.map(item => item.id) : []
}

// 单个照片勾选: 手动增删选中数组
const handlePhotoCheck = (id, checked) => {
  if (checked) {
    if (!selectPhotoIds.value.includes(id)) {
      selectPhotoIds.value.push(id)
    }
  } else {
    const idx = selectPhotoIds.value.indexOf(id)
    if (idx > -1) {
      selectPhotoIds.value.splice(idx, 1)
    }
  }
  checkAll.value = selectPhotoIds.value.length === photoList.value.length
}

const handleCheckedPhotoChange = (value) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === photoList.value.length
}

const handleCommand = (command) => {
  if (command.type === 'edit') {
    editForm.id = command.data.id
    editForm.photoName = command.data.photoName
    editForm.photoDesc = command.data.photoDesc || ''
    editDialogVisible.value = true
  }
}

const handleSavePhotos = async () => {
  if (!uploadPhotos.value) {
    ElMessage.warning('请先上传照片')
    return
  }

  uploadLoading.value = true
  try {
    const photoUrls = uploadPhotos.value.split(',').filter(p => p)
    const res = await savePhotos({
      albumId: albumId.value,
      photoUrlList: photoUrls
    })
    if (res.flag) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      uploadPhotos.value = ''
      getList()
      getAlbumInfo()
    }
  } catch (error) {
    console.error('保存照片失败:', error)
  } finally {
    uploadLoading.value = false
  }
}

const handleUpdatePhoto = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        const res = await updatePhoto(editForm)
        if (res.flag) {
          ElMessage.success('修改成功')
          editDialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('修改照片失败:', error)
      } finally {
        editLoading.value = false
      }
    }
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`是否确认删除选中的${selectPhotoIds.value.length}张照片?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePhotos(selectPhotoIds.value)
      if (res.flag) {
        ElMessage.success('删除成功')
        selectPhotoIds.value = []
        checkAll.value = false
        getList()
        getAlbumInfo()
      }
    } catch (error) {
      console.error('删除照片失败:', error)
    }
  }).catch(() => {})
}

const handleMovePhotos = async () => {
  if (!moveForm.albumId) {
    ElMessage.warning('请选择目标相册')
    return
  }

  moveLoading.value = true
  try {
    const res = await movePhotos({
      albumId: moveForm.albumId,
      photoIdList: selectPhotoIds.value
    })
    if (res.flag) {
      ElMessage.success('移动成功')
      moveDialogVisible.value = false
      moveForm.albumId = null
      selectPhotoIds.value = []
      checkAll.value = false
      getList()
      getAlbumInfo()
    }
  } catch (error) {
    console.error('移动照片失败:', error)
  } finally {
    moveLoading.value = false
  }
}

const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  getList()
}

const handleUploadClose = () => {
  uploadPhotos.value = ''
}

const handleEditClose = () => {
  editForm.id = null
  editForm.photoName = ''
  editForm.photoDesc = ''
  editFormRef.value?.resetFields()
}

onMounted(() => {
  getAlbumInfo()
  getList()
  getAlbumList()
})
</script>

<style scoped lang="scss">
.photo-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.album-info {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
}

.album-cover {
  width: 150px;
  height: 150px;
  border-radius: 4px;
}

.album-detail {
  flex: 1;
  margin-left: 20px;
}

.album-name {
  font-size: 20px;
  font-weight: bold;
  margin-right: 10px;
}

.photo-count {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.album-desc {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-right: 15px;
}

.operation {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.all-check {
  display: flex;
  align-items: center;
  gap: 10px;
}

.check-count {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.photo-container {
  margin-top: 20px;
}

/* 分页区域: 与照片网格隔离, 避免重叠 */
.pagination-wrapper {
  margin-top: 24px;
  text-align: right;
  clear: both;
  position: relative;
  z-index: 1;
}

/* 照片卡片: 等宽图片 + 圆角 + hover 提升 */
.photo-item {
  position: relative;
  width: 100%;
  margin-bottom: 16px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  background: #f5f7fa;
}

.photo-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.photo-img {
  display: block;
  width: 100%;
  height: 150px;
}

/* 照片名称: 图片下方正常流式展示 */
.photo-name {
  padding: 6px 10px;
  font-size: 13px;
  color: #606266;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

/* 更多操作按钮 */
.photo-operation {
  position: absolute;
  z-index: 10;
  top: 0.4rem;
  right: 0.4rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.photo-item:hover .photo-operation {
  opacity: 1;
}

.upload-container {
  min-height: 200px;
}

.upload-footer {
  display: flex;
  align-items: center;
}

.upload-count {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

/* checkbox 透明化: 勾选圈悬浮在图片左上角, 不显示文字标签 */
:deep(.el-checkbox) {
  position: relative;
  width: 100%;
  margin-right: 0;
  display: block;
  height: auto;
}

:deep(.el-checkbox__input) {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 20;
}

:deep(.el-checkbox__label) {
  padding-left: 0;
  display: block;
  width: 100%;
  height: auto;
  line-height: normal;
  white-space: normal;
  vertical-align: top;
  overflow: visible;
}

/* 只隐藏 checkbox 自带的文字标签节点, 不误伤图片/下拉等内部 span */
:deep(.el-checkbox__label > span) {
  display: none;
}
</style>
