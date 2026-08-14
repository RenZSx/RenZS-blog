<template>
  <div class="operation-log-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入模块名或描述"
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
        :data="logList"
        @selection-change="handleSelectionChange"
        style="margin-top: 20px"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="系统模块" align="center" prop="optModule" width="120" />
        <el-table-column label="操作类型" align="center" prop="optType" width="100" />
        <el-table-column label="操作描述" align="center" prop="optDesc" width="150" />
        <el-table-column label="请求方式" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.requestMethod" :type="getMethodType(scope.row.requestMethod)">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人员" align="center" prop="nickname" />
        <el-table-column label="登录IP" align="center" prop="ipAddress" width="130" />
        <el-table-column label="登录地址" align="center" prop="ipSource" width="150" />
        <el-table-column label="操作日期" align="center" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon> 查看
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

    <!-- 查看对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="详细信息"
      width="600px"
    >
      <el-form :model="currentLog" label-width="100px">
        <el-form-item label="操作模块">
          {{ currentLog.optModule }}
        </el-form-item>
        <el-form-item label="请求地址">
          {{ currentLog.optUrl }}
        </el-form-item>
        <el-form-item label="请求方式">
          <el-tag v-if="currentLog.requestMethod" :type="getMethodType(currentLog.requestMethod)">
            {{ currentLog.requestMethod }}
          </el-tag>
        </el-form-item>
        <el-form-item label="操作方法">
          {{ currentLog.optMethod }}
        </el-form-item>
        <el-form-item label="请求参数">
          <el-input
            v-model="currentLog.requestParam"
            type="textarea"
            :rows="4"
            readonly
          />
        </el-form-item>
        <el-form-item label="返回数据">
          <el-input
            v-model="currentLog.responseData"
            type="textarea"
            :rows="4"
            readonly
          />
        </el-form-item>
        <el-form-item label="操作人员">
          {{ currentLog.nickname }}
        </el-form-item>
        <el-form-item label="IP地址">
          {{ currentLog.ipAddress }}
        </el-form-item>
        <el-form-item label="IP来源">
          {{ currentLog.ipSource }}
        </el-form-item>
        <el-form-item label="操作时间">
          {{ formatDate(currentLog.createTime, 'YYYY-MM-DD HH:mm:ss') }}
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="dialogVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, View } from '@element-plus/icons-vue'
import { listOperationLogs, deleteOperationLogs } from '@/api/blog/operationLog'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const total = ref(0)
const logList = ref([])
const dialogVisible = ref(false)
const multiple = ref(true)
const ids = ref([])
const currentLog = ref({})

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: ''
})

// 查询日志列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listOperationLogs(queryParams)
    if (res.flag) {
      logList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取操作日志列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 查看
const handleView = (row) => {
  currentLog.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  const logIds = row?.id ? [row.id] : ids.value
  const message = row?.id ? '是否确认删除该操作日志?' : '是否确认删除选中的操作日志?'

  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteOperationLogs(logIds)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除操作日志失败:', error)
    }
  }).catch(() => {})
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

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.operation-log-management {
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
