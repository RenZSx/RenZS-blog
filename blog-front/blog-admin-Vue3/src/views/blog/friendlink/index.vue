<template>
  <div class="friendlink-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>友链管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select
          v-model="queryParams.status"
          clearable
          size="small"
          placeholder="审核状态"
          style="width: 120px; margin-right: 10px"
          @change="handleQuery"
        >
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入友链名"
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
        <el-button
          type="danger"
          :disabled="multiple"
          @click="handleDelete()"
          style="margin-left: 10px"
        >
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="friendLinkList"
        @selection-change="handleSelectionChange"
        style="margin-top: 20px"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="链接封面" align="center" width="150">
          <template #default="scope">
            <el-image
              :src="scope.row.linkCover"
              style="width: 120px; height: 60px; object-fit: cover"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column label="链接名" align="center" prop="linkName" />
        <el-table-column label="链接地址" align="center" prop="linkAddress" />
        <el-table-column label="链接介绍" align="center" prop="linkIntro" />
        <el-table-column label="审核状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.linkStatus)">
              {{ getStatusText(scope.row.linkStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="280">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleUpdate(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              v-if="scope.row.linkStatus !== 1"
              type="success"
              size="small"
              @click="handleReview(scope.row.id, 1)"
            >
              <el-icon><Check /></el-icon> 通过
            </el-button>
            <el-button
              v-if="scope.row.linkStatus !== 2"
              type="warning"
              size="small"
              @click="handleReview(scope.row.id, 2)"
            >
              <el-icon><Close /></el-icon> 拒绝
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        :current-page="queryParams.current"
        :page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
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
        <el-form-item label="链接名" prop="linkName">
          <el-input v-model="form.linkName" placeholder="请输入链接名" maxlength="20" />
        </el-form-item>
        <el-form-item label="链接封面" prop="linkCover">
          <el-input v-model="form.linkCover" placeholder="请输入链接封面地址" />
        </el-form-item>
        <el-form-item label="链接地址" prop="linkAddress">
          <el-input v-model="form.linkAddress" placeholder="请输入链接地址" />
        </el-form-item>
        <el-form-item label="链接介绍" prop="linkIntro">
          <el-input v-model="form.linkIntro" placeholder="请输入链接介绍" maxlength="50" />
        </el-form-item>
        <el-form-item label="审核状态" prop="linkStatus">
          <el-select v-model="form.linkStatus" placeholder="请选择审核状态" style="width: 100%">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
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
import { Plus, Search, Refresh, Delete, Edit, Check, Close } from '@element-plus/icons-vue'
import { listFriendLinks, getFriendLink, saveOrUpdateFriendLink, deleteFriendLinks } from '@/api/blog/friendLink'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const friendLinkList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const multiple = ref(true)
const ids = ref([])
const formRef = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: '',
  status: null
})

const form = reactive({
  id: null,
  linkName: '',
  linkCover: '',
  linkAddress: '',
  linkIntro: '',
  linkStatus: 1
})

const rules = {
  linkName: [
    { required: true, message: '链接名不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '链接名长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  linkCover: [
    { required: true, message: '链接封面不能为空', trigger: 'blur' }
  ],
  linkAddress: [
    { required: true, message: '链接地址不能为空', trigger: 'blur' }
  ],
  linkIntro: [
    { required: true, message: '链接介绍不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '链接介绍长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listFriendLinks(queryParams)
    if (res.flag) {
      friendLinkList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取友链列表失败:', error)
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
  queryParams.status = null
  getList()
}

const handleAdd = () => {
  dialogTitle.value = '新增友链'
  dialogVisible.value = true
  resetForm()
  form.linkStatus = 1
}

const handleUpdate = async (row) => {
  dialogTitle.value = '编辑友链'
  try {
    const res = await getFriendLink(row.id)
    if (res.flag) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取友链详情失败:', error)
  }
}

const handleReview = async (id, status) => {
  const statusText = status === 1 ? '通过' : '拒绝'
  ElMessageBox.confirm(`是否确认${statusText}该友链?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await saveOrUpdateFriendLink({ id, linkStatus: status })
      if (res.flag) {
        ElMessage.success('操作成功')
        getList()
      }
    } catch (error) {
      console.error('审核友链失败:', error)
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  const linkIds = row?.id ? [row.id] : ids.value
  const message = row?.id ? `是否确认删除友链"${row.linkName}"?` : '是否确认删除选中的友链?'

  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteFriendLinks(linkIds)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除友链失败:', error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await saveOrUpdateFriendLink(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存友链失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
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
  form.linkName = ''
  form.linkCover = ''
  form.linkAddress = ''
  form.linkIntro = ''
  form.linkStatus = 1
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.friendlink-management {
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
</style>
