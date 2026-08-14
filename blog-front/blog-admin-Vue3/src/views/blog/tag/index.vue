<template>
  <div class="tag-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>标签管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入标签名"
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
        :data="tagList"
        @selection-change="handleSelectionChange"
        style="margin-top: 20px"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="标签ID" align="center" prop="id" width="80" />
        <el-table-column label="标签名" align="center" prop="tagName" />
        <el-table-column label="文章数量" align="center" prop="articleCount" width="100" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleUpdate(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
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
      width="500px"
      @close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名" prop="tagName">
          <el-input v-model="form.tagName" placeholder="请输入标签名" maxlength="20" />
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
import { Plus, Search, Refresh, Delete, Edit } from '@element-plus/icons-vue'
import { listTags, getTag, saveOrUpdateTag, deleteTags } from '@/api/blog/tag'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const tagList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const multiple = ref(true)
const ids = ref([])
const formRef = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: ''
})

const form = reactive({
  id: null,
  tagName: ''
})

const rules = {
  tagName: [
    { required: true, message: '标签名不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '标签名长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

// 查询标签列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listTags(queryParams)
    if (res.flag) {
      tagList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 重置
const handleReset = () => {
  queryParams.current = 1
  queryParams.keywords = ''
  getList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增标签'
  dialogVisible.value = true
  resetForm()
}

// 编辑
const handleUpdate = async (row) => {
  dialogTitle.value = '编辑标签'
  try {
    const res = await getTag(row.id)
    if (res.flag) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取标签详情失败:', error)
  }
}

// 删除
const handleDelete = (row) => {
  const tagIds = row?.id ? [row.id] : ids.value
  const message = row?.id ? `是否确认删除标签"${row.tagName}"?` : '是否确认删除选中的标签?'

  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteTags(tagIds)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除标签失败:', error)
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await saveOrUpdateTag(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存标签失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

// 分页
const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  getList()
}

// 关闭对话框
const handleClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.tagName = ''
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.tag-management {
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
