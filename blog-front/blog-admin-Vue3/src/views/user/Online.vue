<template>
  <div class="online-user-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>在线用户</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
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

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="margin-top: 20px"
        border
      >
        <el-table-column label="头像" align="center" width="80">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" :size="40" />
          </template>
        </el-table-column>
        <el-table-column label="昵称" align="center" prop="nickname" />
        <el-table-column label="IP地址" align="center" prop="ipAddress" width="140" />
        <el-table-column label="登录地址" align="center" prop="ipSource" width="200" />
        <el-table-column label="浏览器" align="center" prop="browser" width="160" />
        <el-table-column label="操作系统" align="center" prop="os" />
        <el-table-column label="登录时间" align="center" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.lastLoginTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="handleOffline(scope.row)"
            >
              <el-icon><SwitchButton /></el-icon> 下线
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, SwitchButton } from '@element-plus/icons-vue'
import { listOnlineUsers, removeOnlineUser } from '@/api/blog/user'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const total = ref(0)
const userList = ref([])

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: ''
})

// 查询在线用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listOnlineUsers(queryParams)
    if (res.flag) {
      userList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取在线用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 下线用户
const handleOffline = (row) => {
  ElMessageBox.confirm(`确定下线用户"${row.nickname}"吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await removeOnlineUser(row.userInfoId)
      if (res.flag) {
        ElMessage.success('下线成功')
        getList()
      }
    } catch (error) {
      console.error('下线用户失败:', error)
    }
  }).catch(() => {})
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

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.online-user-management {
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
