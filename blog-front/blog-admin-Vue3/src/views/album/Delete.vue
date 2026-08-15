<template>
  <el-card class="main-card">
    <div class="title">{{ route.meta.title }}</div>
    <!-- 操作栏 -->
    <div class="operation">
      <div class="all-check">
        <el-checkbox
          v-model="checkAll"
          :indeterminate="isIndeterminate"
          @change="handleCheckAllChange"
        >
          全选
        </el-checkbox>
        <div class="check-count">已选择{{ selectPhotoIdList.length }}张</div>
      </div>
      <el-button
        type="success"
        size="small"
        :disabled="selectPhotoIdList.length === 0"
        @click="restorePhotos"
      >
        <el-icon><RefreshLeft /></el-icon> 批量恢复
      </el-button>
      <el-button
        type="danger"
        size="small"
        :disabled="selectPhotoIdList.length === 0"
        @click="batchDeleteVisible = true"
      >
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
    </div>

    <!-- 照片列表 -->
    <div v-loading="loading" class="photo-container">
      <el-empty v-if="photoList.length === 0" description="暂无照片" />
      <el-checkbox-group
        v-else
        v-model="selectPhotoIdList"
        @change="handleCheckedPhotoChange"
      >
        <el-row :gutter="10">
          <el-col v-for="item of photoList" :key="item.id" :md="4" :sm="8" :xs="12">
            <el-checkbox :value="item.id" :label="item.id">
              <div class="photo-item">
                <el-image
                  fit="cover"
                  class="photo-img"
                  :src="item.photoSrc"
                  :preview-src-list="previewSrcList"
                  :initial-index="photoList.indexOf(item)"
                />
                <div class="photo-name">{{ item.photoName }}</div>
              </div>
            </el-checkbox>
          </el-col>
        </el-row>
      </el-checkbox-group>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-show="count > 0"
      class="pagination-container"
      background
      :current-page="queryParams.current"
      :page-size="queryParams.size"
      :total="count"
      layout="prev, pager, next"
      @current-change="handleCurrentChange"
    />

    <!-- 批量删除确认 -->
    <el-dialog v-model="batchDeleteVisible" title="提示" width="30%">
      <div style="font-size: 1rem">是否彻底删除选中照片？</div>
      <template #footer>
        <el-button @click="batchDeleteVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleDeletePhotos">确 定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup name="PhotoDelete">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Delete, RefreshLeft } from '@element-plus/icons-vue'
import { listPhotos, deletePhotos, updatePhotoDelete } from '@/api/blog/album'

const route = useRoute()

const loading = ref(false)
const batchDeleteVisible = ref(false)
const isIndeterminate = ref(false)
const checkAll = ref(false)
const photoList = ref([])
const photoIdList = ref([])
const selectPhotoIdList = ref([])
const count = ref(0)

const queryParams = reactive({
  current: 1,
  size: 18,
  isDelete: 1
})

// 图片预览列表
const previewSrcList = computed(() => photoList.value.map(item => item.photoSrc))

// 查询回收站照片列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listPhotos(queryParams)
    photoList.value = res.recordList || []
    count.value = res.count || 0
  } catch (error) {
    console.error('获取回收站照片失败:', error)
  } finally {
    loading.value = false
  }
}

// 批量恢复照片
const restorePhotos = async () => {
  try {
    await updatePhotoDelete({
      idList: selectPhotoIdList.value,
      isDelete: 0
    })
    ElNotification.success({ title: '成功', message: '恢复成功' })
    resetSelection()
    getList()
  } catch (error) {
    console.error('恢复照片失败:', error)
  }
}

// 彻底删除照片
const handleDeletePhotos = async () => {
  try {
    await deletePhotos(selectPhotoIdList.value)
    ElNotification.success({ title: '成功', message: '删除成功' })
    resetSelection()
    getList()
  } catch (error) {
    console.error('删除照片失败:', error)
  } finally {
    batchDeleteVisible.value = false
  }
}

// 全选
const handleCheckAllChange = (val) => {
  selectPhotoIdList.value = val ? [...photoIdList.value] : []
  isIndeterminate.value = false
}

// 单选变化
const handleCheckedPhotoChange = (value) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === photoIdList.value.length && checkedCount > 0
  isIndeterminate.value = checkedCount > 0 && checkedCount < photoIdList.value.length
}

// 重置选中状态
const resetSelection = () => {
  selectPhotoIdList.value = []
  checkAll.value = false
  isIndeterminate.value = false
}

// 分页
const handleCurrentChange = (val) => {
  queryParams.current = val
  resetSelection()
  getList()
}

// 照片列表变化时同步 id 列表
watch(photoList, (list) => {
  photoIdList.value = list.map(item => item.id)
})

onMounted(() => {
  getList()
})
</script>

<style scoped>
.title {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.operation {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 2.25rem;
  margin-bottom: 2rem;
}

.all-check {
  display: inline-flex;
  align-items: center;
  margin-right: 1rem;
}

.check-count {
  margin-left: 1rem;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.photo-container {
  min-height: 200px;
}

.photo-item {
  position: relative;
  cursor: pointer;
  margin-bottom: 1rem;
}

.photo-img {
  width: 100%;
  height: 7rem;
  border-radius: 4px;
}

.photo-name {
  font-size: 14px;
  margin-top: 0.3rem;
  text-align: center;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  justify-content: flex-end;
}
</style>
