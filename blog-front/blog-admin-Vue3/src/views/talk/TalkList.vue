<template>
  <div class="talk-list-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>说说列表</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增说说
          </el-button>
        </div>
      </template>

      <!-- 状态筛选 -->
      <div class="status-menu">
        <span>状态</span>
        <span
          @click="changeStatus(null)"
          :class="queryParams.status === null ? 'active-status' : 'status'"
        >
          全部
        </span>
        <span
          @click="changeStatus(1)"
          :class="queryParams.status === 1 ? 'active-status' : 'status'"
        >
          公开
        </span>
        <span
          @click="changeStatus(2)"
          :class="queryParams.status === 2 ? 'active-status' : 'status'"
        >
          私密
        </span>
      </div>

      <!-- 说说列表 -->
      <el-empty v-if="!talkList || talkList.length === 0" description="暂无说说" />

      <div v-else class="talk-list">
        <div class="talk-item" v-for="item in talkList" :key="item.id">
          <div class="user-info-wrapper">
            <el-avatar :src="item.avatar" :size="40" class="user-avatar" />
            <div class="user-detail-wrapper">
              <div class="user-nickname">
                <div>{{ item.nickname }}</div>
                <!-- 操作菜单 -->
                <el-dropdown trigger="click" @command="handleCommand">
                  <el-icon style="cursor: pointer; font-size: 18px"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="'edit,' + item.id">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item :command="'delete,' + item.id">
                        <el-icon><Delete /></el-icon> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <!-- 发表时间 -->
              <div class="time">
                {{ formatDate(item.createTime, 'YYYY-MM-DD HH:mm:ss') }}
                <span class="top" v-if="item.isTop === 1">
                  <el-icon><Top /></el-icon> 置顶
                </span>
                <span class="secret" v-if="item.status === 2">
                  <el-icon><Lock /></el-icon> 私密
                </span>
              </div>

              <!-- 说说内容 -->
              <div class="talk-content" v-html="sanitizeHtml(item.content)"></div>

              <!-- 图片列表 -->
              <el-row v-if="item.imgList && item.imgList.length > 0" :gutter="8" class="talk-images">
                <el-col
                  v-for="(img, index) in item.imgList"
                  :key="index"
                  :xs="12"
                  :sm="8"
                  :md="8"
                >
                  <el-image
                    class="images-items"
                    :src="img"
                    :preview-src-list="item.imgList"
                    fit="cover"
                  />
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        :current-page="queryParams.current"
        :page-size="queryParams.size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, MoreFilled, Top, Lock } from '@element-plus/icons-vue'
import { listTalks, deleteTalks } from '@/api/blog/talk'
import { formatDate } from '@/utils/blog'
import { sanitizeHtml } from '@/utils/sanitize'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const talkList = ref([])

const queryParams = reactive({
  current: 1,
  size: 5,
  status: null
})

// 查询说说列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listTalks(queryParams)
    if (res.flag) {
      talkList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取说说列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换状态
const changeStatus = (status) => {
  queryParams.status = status
  queryParams.current = 1
  getList()
}

// 新增说说
const handleAdd = () => {
  router.push('/talks')
}

// 操作命令
const handleCommand = (command) => {
  const [action, id] = command.split(',')
  if (action === 'edit') {
    router.push(`/talks/${id}`)
  } else if (action === 'delete') {
    handleDelete(id)
  }
}

// 删除说说
const handleDelete = (id) => {
  ElMessageBox.confirm('是否确认删除该说说?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteTalks([id])
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除说说失败:', error)
    }
  }).catch(() => {})
}

// 分页
const handleCurrentChange = (val) => {
  queryParams.current = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.talk-list-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-menu {
  font-size: 14px;
  margin-top: 20px;
  color: var(--el-text-color-secondary);

  span {
    margin-right: 24px;
  }

  .status {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      color: var(--el-text-color-primary);
    }
  }

  .active-status {
    cursor: pointer;
    color: var(--el-text-color-primary);
    font-weight: bold;
  }
}

.talk-list {
  margin-top: 20px;
}

.talk-item {
  padding: 16px 20px;
  margin-bottom: 20px;
  border-radius: 10px;
  background: var(--el-bg-color-overlay);
  box-shadow: 0 3px 8px 6px rgba(7, 17, 27, 0.06);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 5px 10px 8px rgba(7, 17, 27, 0.16);
    transform: translateY(-3px);
  }
}

.user-info-wrapper {
  width: 100%;
  display: flex;
}

.user-avatar {
  border-radius: 50%;
  transition: all 0.5s;

  &:hover {
    transform: rotate(360deg);
  }
}

.user-detail-wrapper {
  margin-left: 12px;
  width: 100%;
}

.user-nickname {
  font-size: 15px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.top {
  color: #ff7242;
  margin-left: 10px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.secret {
  color: var(--el-text-color-secondary);
  margin-left: 10px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.talk-content {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-line;
  word-wrap: break-word;
  word-break: break-all;
}

.talk-images {
  margin-top: 12px;
}

.images-items {
  cursor: pointer;
  width: 100%;
  height: 200px;
  border-radius: 8px;
  margin-bottom: 8px;
}
</style>
