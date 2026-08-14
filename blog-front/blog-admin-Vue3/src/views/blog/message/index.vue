<template>
  <div class="message-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>留言管理</span>
        </div>
      </template>

      <!-- 审核状态筛选 -->
      <div class="review-menu">
        <span>状态</span>
        <span
          @click="changeReview(null)"
          :class="queryParams.isReview === null ? 'active-review' : 'review'"
        >
          全部
        </span>
        <span
          @click="changeReview(1)"
          :class="queryParams.isReview === 1 ? 'active-review' : 'review'"
        >
          正常
        </span>
        <span
          @click="changeReview(0)"
          :class="queryParams.isReview === 0 ? 'active-review' : 'review'"
        >
          审核中
        </span>
      </div>

      <!-- 操作栏 -->
      <div class="operation-container">
        <el-button
          type="danger"
          size="small"
          :disabled="multiple"
          @click="handleDelete()"
        >
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button
          type="success"
          size="small"
          :disabled="multiple"
          @click="handleReview()"
        >
          <el-icon><Select /></el-icon> 批量通过
        </el-button>

        <!-- 搜索栏 -->
        <div style="margin-left: auto; display: flex; align-items: center">
          <el-input
            v-model="queryParams.keywords"
            placeholder="请输入用户昵称"
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
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="messageList"
        @selection-change="handleSelectionChange"
        style="margin-top: 20px"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="头像" align="center" width="80">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" :size="40" />
          </template>
        </el-table-column>
        <el-table-column label="留言人" align="center" prop="nickname" width="150" />
        <el-table-column label="留言内容" align="center" prop="messageContent" />
        <el-table-column label="IP地址" align="center" prop="ipAddress" width="150" />
        <el-table-column label="IP来源" align="center" prop="ipSource" width="170" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isReview === 0" type="warning">审核中</el-tag>
            <el-tag v-else-if="scope.row.isReview === 1" type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="留言时间" align="center" width="180">
          <template #default="scope">
            <span>
              <el-icon><Clock /></el-icon>
              {{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template #default="scope">
            <el-button
              v-if="scope.row.isReview === 0"
              type="success"
              size="small"
              @click="handleReview(scope.row)"
            >
              <el-icon><Select /></el-icon> 通过
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Search, Select, Clock } from '@element-plus/icons-vue'
import { listMessages, deleteMessages, updateMessagesReview } from '@/api/blog/message'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const total = ref(0)
const messageList = ref([])
const multiple = ref(true)
const ids = ref([])

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: '',
  isReview: null
})

// 查询留言列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listMessages(queryParams)
    if (res.flag) {
      messageList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取留言列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 切换审核状态
const changeReview = (review) => {
  queryParams.isReview = review
  queryParams.current = 1
}

// 审核留言
const handleReview = (row) => {
  const messageIds = row?.id ? [row.id] : ids.value
  const message = row?.id ? '是否确认通过该留言?' : '是否确认通过选中的留言?'

  ElMessageBox.confirm(message, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateMessagesReview({
        idList: messageIds,
        isReview: 1
      })
      if (res.flag) {
        ElMessage.success('审核通过')
        getList()
      }
    } catch (error) {
      console.error('审核留言失败:', error)
    }
  }).catch(() => {})
}

// 删除留言
const handleDelete = (row) => {
  const messageIds = row?.id ? [row.id] : ids.value
  const message = row?.id ? '是否确认删除该留言?' : '是否确认删除选中的留言?'

  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMessages(messageIds)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除留言失败:', error)
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

// 监听审核状态变化
watch(() => queryParams.isReview, () => {
  getList()
})

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.message-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-menu {
  font-size: 14px;
  margin-top: 20px;
  color: #999;

  span {
    margin-right: 24px;
  }

  .review {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      color: #333;
    }
  }

  .active-review {
    cursor: pointer;
    color: #333;
    font-weight: bold;
  }
}

.operation-container {
  display: flex;
  align-items: center;
  margin-top: 20px;
}
</style>
