<template>
  <div class="menu-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增菜单
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="keywords"
          placeholder="请输入菜单名"
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
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="margin-top: 20px"
      >
        <el-table-column label="菜单名称" prop="name" width="160" />
        <el-table-column label="图标" align="center" width="80">
          <template #default="scope">
            <i :class="'iconfont ' + scope.row.icon" v-if="scope.row.icon" />
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" prop="orderNum" width="80" />
        <el-table-column label="访问路径" prop="path" />
        <el-table-column label="组件路径" prop="component" />
        <el-table-column label="隐藏" align="center" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isHidden"
              :active-value="1"
              :inactive-value="0"
              @change="handleHiddenChange(scope.row)"
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
              @click="handleAddChild(scope.row)"
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单类型" v-if="showType">
          <el-radio-group v-model="isCatalog">
            <el-radio :label="true">目录</el-radio>
            <el-radio :label="false">菜单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单图标" prop="icon">
          <el-popover placement="bottom-start" width="400" trigger="click">
            <template #reference>
              <el-input v-model="form.icon" placeholder="请选择图标">
                <template #prefix>
                  <i :class="'iconfont ' + form.icon" v-if="form.icon" />
                </template>
              </el-input>
            </template>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;">
              <div
                v-for="(item, index) in iconList"
                :key="index"
                class="icon-item"
                @click="selectIcon(item)"
              >
                <i :class="'iconfont ' + item" /> {{ item }}
              </div>
            </div>
          </el-popover>
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="!isCatalog">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="访问路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入访问路径" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number
            v-model="form.orderNum"
            :min="1"
            :max="999"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="显示状态">
          <el-radio-group v-model="form.isHidden">
            <el-radio :label="0">显示</el-radio>
            <el-radio :label="1">隐藏</el-radio>
          </el-radio-group>
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
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { listMenus, saveOrUpdateMenu, deleteMenu } from '@/api/blog/menu'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const submitLoading = ref(false)
const menuList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const showType = ref(true)
const isCatalog = ref(true)
const keywords = ref('')
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  icon: '',
  component: 'Layout',
  path: '',
  orderNum: 1,
  parentId: null,
  isHidden: 0
})

const rules = {
  name: [
    { required: true, message: '菜单名称不能为空', trigger: 'blur' }
  ],
  icon: [
    { required: true, message: '菜单图标不能为空', trigger: 'blur' }
  ],
  component: [
    { required: true, message: '组件路径不能为空', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '访问路径不能为空', trigger: 'blur' }
  ]
}

const iconList = [
  'el-icon-myshouye',
  'el-icon-myfabiaowenzhang',
  'el-icon-myyonghuliebiao',
  'el-icon-myxiaoxi',
  'el-icon-myliuyan',
  'el-icon-setting',
  'el-icon-user',
  'el-icon-document',
  'el-icon-folder',
  'el-icon-star-off'
]

// 查询菜单列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listMenus({ keywords: keywords.value })
    if (res.flag) {
      menuList.value = res.data
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增菜单'
  showType.value = true
  isCatalog.value = true
  resetForm()
  dialogVisible.value = true
}

// 新增子菜单
const handleAddChild = (row) => {
  dialogTitle.value = '新增菜单'
  showType.value = false
  isCatalog.value = false
  resetForm()
  form.parentId = row.id
  form.component = ''
  dialogVisible.value = true
}

// 编辑
const handleUpdate = (row) => {
  dialogTitle.value = '修改菜单'
  showType.value = false
  isCatalog.value = !row.component || row.component === 'Layout'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 隐藏状态改变
const handleHiddenChange = async (row) => {
  try {
    const res = await saveOrUpdateMenu(row)
    if (res.flag) {
      ElMessage.success('修改成功')
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    row.isHidden = row.isHidden === 1 ? 0 : 1
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`是否确认删除菜单"${row.name}"?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMenu(row.id)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除菜单失败:', error)
    }
  }).catch(() => {})
}

// 选择图标
const selectIcon = (icon) => {
  form.icon = icon
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isCatalog.value && !form.parentId) {
          form.component = 'Layout'
        }
        const res = await saveOrUpdateMenu(form)
        if (res.flag) {
          ElMessage.success(form.id ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存菜单失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 关闭对话框
const handleClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.icon = ''
  form.component = 'Layout'
  form.path = ''
  form.orderNum = 1
  form.parentId = null
  form.isHidden = 0
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.menu-management {
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

.icon-item {
  cursor: pointer;
  padding: 8px;
  text-align: center;
  border-radius: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;

  &:hover {
    background-color: #f5f7fa;
  }
}
</style>
