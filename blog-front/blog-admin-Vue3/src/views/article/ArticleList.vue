<template>
  <div class="article-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文章管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增文章
          </el-button>
        </div>
      </template>

      <!-- 文章状态菜单 -->
      <div class="article-status-menu">
        <span class="status-label">状态</span>
        <span
          :class="['status-item', { 'active': activeStatus === 'all' }]"
          @click="changeStatus('all')"
        >
          全部
        </span>
        <span
          :class="['status-item', { 'active': activeStatus === 'public' }]"
          @click="changeStatus('public')"
        >
          公开
        </span>
        <span
          :class="['status-item', { 'active': activeStatus === 'secret' }]"
          @click="changeStatus('secret')"
        >
          私密
        </span>
        <span
          :class="['status-item', { 'active': activeStatus === 'draft' }]"
          @click="changeStatus('draft')"
        >
          草稿箱
        </span>
        <span
          :class="['status-item', { 'active': activeStatus === 'delete' }]"
          @click="changeStatus('delete')"
        >
          回收站
        </span>
      </div>

      <!-- 操作栏 -->
      <div class="operation-bar">
        <div class="operation-buttons">
          <el-button
            v-if="queryParams.isDelete === 0"
            type="danger"
            size="small"
            :disabled="multiple"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
          <el-button
            v-else
            type="danger"
            size="small"
            :disabled="multiple"
            @click="handleBatchDeletePermanently"
          >
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
          <el-button
            type="success"
            size="small"
            :disabled="multiple"
            @click="handleBatchExport"
          >
            <el-icon><Download /></el-icon> 批量导出
          </el-button>
          <el-dropdown style="margin-left: 10px">
            <el-button type="primary" size="small">
              <el-icon><Upload /></el-icon> 批量导入
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-upload
                    action="/api/admin/articles/import"
                    :headers="uploadHeaders"
                    multiple
                    :limit="9"
                    :show-file-list="false"
                    :on-success="handleImportSuccess"
                  >
                    普通文章
                  </el-upload>
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-upload
                    action="/api/admin/articles/import?type=hexo"
                    :headers="uploadHeaders"
                    multiple
                    :limit="9"
                    :show-file-list="false"
                    :on-success="handleImportSuccess"
                  >
                    Hexo文章
                  </el-upload>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 筛选条件 -->
        <div class="filter-group">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择文章类型"
            clearable
            size="small"
            style="width: 150px"
          >
            <el-option
              v-for="item in typeList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-model="queryParams.categoryId"
            placeholder="请选择分类"
            clearable
            filterable
            size="small"
            style="width: 150px; margin-left: 10px"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="queryParams.tagId"
            placeholder="请选择标签"
            clearable
            filterable
            size="small"
            style="width: 150px; margin-left: 10px"
          >
            <el-option
              v-for="item in tagList"
              :key="item.id"
              :label="item.tagName"
              :value="item.id"
            />
          </el-select>
          <el-input
            v-model="queryParams.keywords"
            placeholder="请输入文章标题"
            clearable
            size="small"
            style="width: 200px; margin-left: 10px"
            @keyup.enter="handleQuery"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="small" @click="handleQuery" style="margin-left: 10px">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="articleList"
        @selection-change="handleSelectionChange"
        style="margin-top: 20px"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="文章封面" width="180" align="center">
          <template #default="scope">
            <div class="article-cover-wrapper">
              <el-image
                class="article-cover"
                :src="scope.row.articleCover || 'https://static.talkxj.com/articles/c5cc2b2561bd0e3060a500198a4ad37d.png'"
                fit="cover"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="status-icon">
                <el-icon v-if="scope.row.status === 1" class="icon-public" :size="20">
                  <View />
                </el-icon>
                <el-icon v-if="scope.row.status === 2" class="icon-secret" :size="20">
                  <Lock />
                </el-icon>
                <el-icon v-if="scope.row.status === 3" class="icon-draft" :size="20">
                  <Document />
                </el-icon>
                <el-icon v-if="scope.row.status === 4" class="icon-recommend" :size="20">
                  <Star />
                </el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标题" align="center" prop="articleTitle" show-overflow-tooltip />
        <el-table-column label="分类" align="center" prop="categoryName" width="110" />
        <el-table-column label="标签" align="center" width="170">
          <template #default="scope">
            <el-tag
              v-for="item in scope.row.tagDTOList"
              :key="item.tagId"
              size="small"
              style="margin-right: 5px; margin-top: 2px"
            >
              {{ item.tagName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览量" align="center" prop="viewsCount" width="80">
          <template #default="scope">
            {{ scope.row.viewsCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="点赞量" align="center" prop="likeCount" width="80">
          <template #default="scope">
            {{ scope.row.likeCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="类型" align="center" width="80">
          <template #default="scope">
            <el-tag :type="getArticleTypeTag(scope.row.type).type">
              {{ getArticleTypeTag(scope.row.type).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发表时间" align="center" width="160">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="置顶" align="center" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isTop"
              :active-value="1"
              :inactive-value="0"
              :disabled="scope.row.isDelete === 1"
              @change="handleTopChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="110">
          <template #default="scope">
            <el-select
              v-model="scope.row.status"
              size="small"
              :disabled="scope.row.isDelete === 1"
              @change="handleStatusChange(scope.row)"
            >
              <el-option label="公开" :value="1">
                <el-icon><View /></el-icon> 公开
              </el-option>
              <el-option label="私密" :value="2">
                <el-icon><Lock /></el-icon> 私密
              </el-option>
              <el-option label="草稿" :value="3">
                <el-icon><Document /></el-icon> 草稿
              </el-option>
              <el-option label="推荐" :value="4">
                <el-icon><Star /></el-icon> 推荐
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.isDelete === 0"
              type="primary"
              size="small"
              @click="handleEdit(scope.row.id)"
            >
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-popconfirm
              v-if="scope.row.isDelete === 0"
              title="确定删除吗？"
              @confirm="handleDeleteArticle(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" style="margin-left: 5px">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="scope.row.isDelete === 1"
              title="确定恢复吗？"
              @confirm="handleDeleteArticle(scope.row.id)"
            >
              <template #reference>
                <el-button type="success" size="small">
                  <el-icon><RefreshRight /></el-icon> 恢复
                </el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="scope.row.isDelete === 1"
              title="确定彻底删除吗？"
              @confirm="handleDeletePermanently(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" style="margin-left: 5px">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        :current-page="queryParams.current"
        :page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 批量删除对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="提示" width="30%">
      <div style="font-size: 14px">
        <el-icon color="#ff9900" :size="20"><Warning /></el-icon>
        是否删除选中项？
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmBatchDelete">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 批量彻底删除对话框 -->
    <el-dialog v-model="deletePermanentlyDialogVisible" title="提示" width="30%">
      <div style="font-size: 14px">
        <el-icon color="#ff9900" :size="20"><Warning /></el-icon>
        是否彻底删除选中项？
      </div>
      <template #footer>
        <el-button @click="deletePermanentlyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmBatchDeletePermanently">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="提示" width="30%">
      <div style="font-size: 14px">
        <el-icon color="#ff9900" :size="20"><Warning /></el-icon>
        是否导出选中文章？
      </div>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmBatchExport">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Delete, Edit, Download, Upload, View, Lock, Document, Star,
  Picture, Warning, RefreshRight
} from '@element-plus/icons-vue'
import {
  listArticles, getArticle, updateArticleDelete, deleteArticle,
  updateArticleTop, exportArticles
} from '@/api/blog/article'
import { searchCategories } from '@/api/blog/category'
import { searchTags } from '@/api/blog/tag'
import { formatDate } from '@/utils/blog'
import { getToken } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const articleList = ref([])
const categoryList = ref([])
const tagList = ref([])
const multiple = ref(true)
const ids = ref([])
const activeStatus = ref('all')
const deleteDialogVisible = ref(false)
const deletePermanentlyDialogVisible = ref(false)
const exportDialogVisible = ref(false)

const typeList = [
  { value: 1, label: '原创' },
  { value: 2, label: '转载' },
  { value: 3, label: '翻译' }
]

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: '',
  type: null,
  categoryId: null,
  tagId: null,
  status: null,
  isDelete: 0
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return token ? { Authorization: 'Bearer ' + token } : {}
})

// 获取文章类型标签
const getArticleTypeTag = (type) => {
  const typeMap = {
    1: { type: 'danger', label: '原创' },
    2: { type: 'success', label: '转载' },
    3: { type: 'primary', label: '翻译' }
  }
  return typeMap[type] || { type: 'info', label: '未知' }
}

// 查询文章列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listArticles(queryParams)
    if (res.flag) {
      articleList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询分类列表
const getCategoryList = async () => {
  try {
    const res = await searchCategories()
    if (res.flag) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 查询标签列表
const getTagList = async () => {
  try {
    const res = await searchTags()
    if (res.flag) {
      tagList.value = res.data
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 切换状态
const changeStatus = (status) => {
  activeStatus.value = status
  switch (status) {
    case 'all':
      queryParams.isDelete = 0
      queryParams.status = null
      break
    case 'public':
      queryParams.isDelete = 0
      queryParams.status = 1
      break
    case 'secret':
      queryParams.isDelete = 0
      queryParams.status = 2
      break
    case 'draft':
      queryParams.isDelete = 0
      queryParams.status = 3
      break
    case 'delete':
      queryParams.isDelete = 1
      queryParams.status = null
      break
  }
  queryParams.current = 1
  getList()
}

// 新增文章
const handleAdd = () => {
  router.push('/articles')
}

// 编辑文章
const handleEdit = (id) => {
  router.push(`/articles/${id}`)
}

// 置顶切换
const handleTopChange = async (row) => {
  try {
    const res = await updateArticleTop({
      id: row.id,
      isTop: row.isTop
    })
    if (res.flag) {
      ElMessage.success('置顶设置成功')
    } else {
      ElMessage.error(res.message || '置顶设置失败')
      row.isTop = row.isTop === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('置顶设置失败:', error)
    row.isTop = row.isTop === 1 ? 0 : 1
  }
}

// 状态切换
const handleStatusChange = async (row) => {
  const statusMap = {
    1: '公开',
    2: '私密',
    3: '草稿',
    4: '推荐'
  }

  // 如果要设置为推荐，先检查是否已有推荐文章
  if (row.status === 4) {
    try {
      const res = await listArticles({
        current: 1,
        size: 100,
        status: 4,
        isDelete: 0
      })
      if (res.flag) {
        const existingRecommendList = res.data.recordList || []
        const otherRecommendList = existingRecommendList.filter(item => item.id !== row.id)
        if (otherRecommendList.length > 0) {
          const articleTitles = otherRecommendList.map(item => `《${item.articleTitle}》`).join('、')
          await ElMessageBox.confirm(
            `已存在推荐文章：${articleTitles}，是否将原来的推荐文章改为公开，并将当前文章《${row.articleTitle}》设为推荐？`,
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          await updateArticleStatusAPI(row, statusMap, true)
          return
        }
      }
    } catch (error) {
      if (error === 'cancel') {
        getList()
        return
      }
      console.error('检查推荐文章失败:', error)
      getList()
      return
    }
  }

  await updateArticleStatusAPI(row, statusMap, false)
}

// 更新文章状态API
const updateArticleStatusAPI = async (row, statusMap, replacedRecommend = false) => {
  try {
    const res = await getArticle(row.id)
    if (res.flag) {
      const articleVO = res.data
      articleVO.status = row.status
      const saveRes = await updateArticleDelete(articleVO)
      if (saveRes.flag) {
        ElMessage.success(
          replacedRecommend
            ? '已将原推荐文章改为公开，并将当前文章设置为推荐'
            : `文章已设置为${statusMap[row.status]}`
        )
        getList()
      } else {
        ElMessage.error(saveRes.message || '状态修改失败')
        getList()
      }
    }
  } catch (error) {
    console.error('状态修改失败:', error)
    getList()
  }
}

// 删除/恢复文章
const handleDeleteArticle = async (id) => {
  try {
    const res = await updateArticleDelete({
      idList: [id],
      isDelete: queryParams.isDelete === 0 ? 1 : 0
    })
    if (res.flag) {
      ElMessage.success(res.message || '操作成功')
      getList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 彻底删除文章
const handleDeletePermanently = async (id) => {
  try {
    const res = await deleteArticle([id])
    if (res.flag) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 批量删除
const handleBatchDelete = () => {
  deleteDialogVisible.value = true
}

const confirmBatchDelete = async () => {
  try {
    const res = await updateArticleDelete({
      idList: ids.value,
      isDelete: 1
    })
    if (res.flag) {
      ElMessage.success('删除成功')
      deleteDialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 批量彻底删除
const handleBatchDeletePermanently = () => {
  deletePermanentlyDialogVisible.value = true
}

const confirmBatchDeletePermanently = async () => {
  try {
    const res = await deleteArticle(ids.value)
    if (res.flag) {
      ElMessage.success('删除成功')
      deletePermanentlyDialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 批量导出
const handleBatchExport = () => {
  exportDialogVisible.value = true
}

const confirmBatchExport = async () => {
  try {
    const res = await exportArticles(ids.value)
    if (res.flag) {
      ElMessage.success('导出成功')
      res.data.forEach(url => {
        downloadFile(url)
      })
      exportDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
  }
}

// 下载文件
const downloadFile = (url) => {
  const iframe = document.createElement('iframe')
  iframe.style.display = 'none'
  iframe.style.height = '0'
  iframe.src = url
  document.body.appendChild(iframe)
  setTimeout(() => {
    iframe.remove()
  }, 5 * 60 * 1000)
}

// 导入成功回调
const handleImportSuccess = (response) => {
  if (response.flag) {
    ElMessage.success('导入成功')
    getList()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
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

// 监听查询参数变化
watch(() => queryParams.type, () => {
  queryParams.current = 1
  getList()
})

watch(() => queryParams.categoryId, () => {
  queryParams.current = 1
  getList()
})

watch(() => queryParams.tagId, () => {
  queryParams.current = 1
  getList()
})

onMounted(() => {
  getList()
  getCategoryList()
  getTagList()
})
</script>

<style scoped lang="scss">
.article-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-status-menu {
  font-size: 14px;
  margin-top: 20px;
  color: #999;

  .status-label {
    margin-right: 24px;
  }

  .status-item {
    margin-right: 24px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      color: #333;
    }

    &.active {
      color: #333;
      font-weight: bold;
    }
  }
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;

  .operation-buttons {
    display: flex;
    align-items: center;
  }

  .filter-group {
    display: flex;
    align-items: center;
  }
}

.article-cover-wrapper {
  position: relative;
  width: 100%;
  height: 90px;

  .article-cover {
    width: 100%;
    height: 100%;
    border-radius: 4px;
  }

  .status-icon {
    position: absolute;
    right: 8px;
    bottom: 8px;

    .icon-public {
      color: #67c23a;
    }

    .icon-secret {
      color: #e6a23c;
    }

    .icon-draft {
      color: #909399;
    }

    .icon-recommend {
      color: #ff9900;
    }
  }
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}
</style>
