<template>
  <div class="resource-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>资源管理</span>
          <div>
            <el-button type="success" size="small" @click="handleImportSwagger">
              <el-icon><Upload /></el-icon> 导入Swagger
            </el-button>
            <el-button type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增模块
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="keywords"
          placeholder="请输入资源名"
          clearable
          style="width: 200px"
          @keyup.enter="getList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="getList" style="margin-left: 10px">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="resourceList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="margin-top: 20px"
      >
        <el-table-column label="资源名" prop="resourceName" width="220" />
        <el-table-column label="资源路径" prop="url" width="300" />
        <el-table-column label="请求方式" align="center" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.requestMethod" :type="getMethodType(scope.row.requestMethod)">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="匿名访问" align="center" width="100">
          <template #default="scope">
            <el-switch
              v-if="scope.row.url"
              v-model="scope.row.isAnonymous"
              :active-value="1"
              :inactive-value="0"
              @change="handleAnonymousChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="240" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.children"
              type="success"
              size="small"
              @click="handleAddResource(scope.row)"
            >
              <el-icon><Plus /></el-icon> 新增
            </el-button>
            <el-button type="primary" size="small" @click="handleUpdate(scope.row)">
              <el-icon><Edit /></el-icon> 修改
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑模块对话框 -->
    <el-dialog
      v-model="moduleDialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleClose"
    >
      <el-form ref="moduleFormRef" :model="form" :rules="moduleRules" label-width="80px">
        <el-form-item label="模块名" prop="resourceName">
          <el-input v-model="form.resourceName" placeholder="请输入模块名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moduleDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleModuleSubmit" :loading="submitLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑资源对话框 -->
    <el-dialog
      v-model="resourceDialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleClose"
    >
      <el-form ref="resourceFormRef" :model="form" :rules="resourceRules" label-width="80px">
        <el-form-item label="资源名" prop="resourceName">
          <el-input v-model="form.resourceName" placeholder="请输入资源名" />
        </el-form-item>
        <el-form-item label="资源路径" prop="url">
          <el-input v-model="form.url" placeholder="请输入资源路径" />
        </el-form-item>
        <el-form-item label="请求方式" prop="requestMethod">
          <el-radio-group v-model="form.requestMethod">
            <el-radio label="GET">GET</el-radio>
            <el-radio label="POST">POST</el-radio>
            <el-radio label="PUT">PUT</el-radio>
            <el-radio label="DELETE">DELETE</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleResourceSubmit" :loading="submitLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Upload } from '@element-plus/icons-vue'
import { listResources, saveOrUpdateResource, deleteResource, importSwagger } from '@/api/blog/resource'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const submitLoading = ref(false)
const resourceList = ref([])
const moduleDialogVisible = ref(false)
const resourceDialogVisible = ref(false)
const dialogTitle = ref('')
const keywords = ref('')
const moduleFormRef = ref(null)
const resourceFormRef = ref(null)

const form = reactive({
  id: null,
  resourceName: '',
  url: '',
  requestMethod: 'GET',
  parentId: null
})

const moduleRules = {
  resourceName: [
    { required: true, message: '模块名不能为空', trigger: 'blur' }
  ]
}

const resourceRules = {
  resourceName: [
    { required: true, message: '资源名不能为空', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '资源路径不能为空', trigger: 'blur' }
  ],
  requestMethod: [
    { required: true, message: '请求方式不能为空', trigger: 'change' }
  ]
}

// 查询资源列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listResources({ keywords: keywords.value })
    if (res.flag) {
      resourceList.value = res.data
    }
  } catch (error) {
    console.error('获取资源列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 新增模块
const handleAdd = () => {
  dialogTitle.value = '新增模块'
  resetForm()
  moduleDialogVisible.value = true
}

// 新增资源
const handleAddResource = (row) => {
  dialogTitle.value = '新增资源'
  resetForm()
  form.parentId = row.id
  form.requestMethod = 'GET'
  resourceDialogVisible.value = true
}

// 编辑
const handleUpdate = (row) => {
  if (row.url) {
    dialogTitle.value = '修改资源'
    Object.assign(form, row)
    resourceDialogVisible.value = true
  } else {
    dialogTitle.value = '修改模块'
    Object.assign(form, row)
    moduleDialogVisible.value = true
  }
}

// 匿名访问状态改变
const handleAnonymousChange = async (row) => {
  try {
    const res = await saveOrUpdateResource(row)
    if (res.flag) {
      ElMessage.success('修改成功')
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    row.isAnonymous = row.isAnonymous === 1 ? 0 : 1
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`是否确认删除"${row.resourceName}"?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteResource(row.id)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除资源失败:', error)
    }
  }).catch(() => {})
}

// 导入Swagger
const handleImportSwagger = () => {
  ElMessageBox.confirm('是否导入Swagger资源?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    loading.value = true
    try {
      const res = await importSwagger()
      if (res.flag) {
        ElMessage.success('导入成功')
        getList()
      }
    } catch (error) {
      console.error('导入Swagger失败:', error)
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}

// 提交模块表单
const handleModuleSubmit = async () => {
  if (!moduleFormRef.value) return
  await moduleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await saveOrUpdateResource(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          moduleDialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存模块失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 提交资源表单
const handleResourceSubmit = async () => {
  if (!resourceFormRef.value) return
  await resourceFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await saveOrUpdateResource(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          resourceDialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存资源失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 获取请求方式标签类型
const getMethodType = (method) => {
  const typeMap = {
    'GET': '',
    'POST': 'success',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[method] || ''
}

// 关闭对话框
const handleClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.resourceName = ''
  form.url = ''
  form.requestMethod = 'GET'
  form.parentId = null
  moduleFormRef.value?.resetFields()
  resourceFormRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.resource-management {
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
