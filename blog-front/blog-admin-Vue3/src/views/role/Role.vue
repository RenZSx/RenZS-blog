<template>
  <div class="role-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入角色名"
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
        :data="roleList"
        @selection-change="handleSelectionChange"
        style="margin-top: 20px"
        border
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="角色名" align="center" prop="roleName" />
        <el-table-column label="权限标签" align="center" prop="roleLabel">
          <template #default="scope">
            <el-tag>{{ scope.row.roleLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="禁用" align="center" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isDisable"
              :active-value="1"
              :inactive-value="0"
              @change="handleDisableChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleMenuPermission(scope.row)">
              <el-icon><Edit /></el-icon> 菜单权限
            </el-button>
            <el-button type="success" size="small" @click="handleResourcePermission(scope.row)">
              <el-icon><FolderChecked /></el-icon> 资源权限
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

    <!-- 菜单权限对话框 -->
    <el-dialog
      v-model="menuDialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleClose"
    >
      <el-form ref="menuFormRef" :model="roleForm" :rules="rules" label-width="80px">
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名" />
        </el-form-item>
        <el-form-item label="权限标签" prop="roleLabel">
          <el-input v-model="roleForm.roleLabel" placeholder="请输入权限标签" />
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuList"
            :props="{ label: 'name', children: 'children' }"
            :default-checked-keys="roleForm.menuIdList"
            show-checkbox
            check-strictly
            node-key="id"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleMenuSubmit" :loading="submitLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>

    <!-- 资源权限对话框 -->
    <el-dialog
      v-model="resourceDialogVisible"
      title="修改资源权限"
      width="500px"
      @close="handleClose"
    >
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色名">
          <el-input v-model="roleForm.roleName" disabled />
        </el-form-item>
        <el-form-item label="权限标签">
          <el-input v-model="roleForm.roleLabel" disabled />
        </el-form-item>
        <el-form-item label="资源权限">
          <el-tree
            ref="resourceTreeRef"
            :data="resourceList"
            :props="{ label: 'resourceName', children: 'children' }"
            :default-checked-keys="roleForm.resourceIdList"
            show-checkbox
            node-key="id"
            style="width: 100%"
          />
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, Edit, FolderChecked } from '@element-plus/icons-vue'
import { listRoles, saveOrUpdateRole, deleteRoles } from '@/api/blog/role'
import { listMenus } from '@/api/blog/menu'
import { listResources } from '@/api/blog/resource'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const roleList = ref([])
const menuList = ref([])
const resourceList = ref([])
const menuDialogVisible = ref(false)
const resourceDialogVisible = ref(false)
const dialogTitle = ref('')
const multiple = ref(true)
const ids = ref([])
const menuFormRef = ref(null)
const menuTreeRef = ref(null)
const resourceTreeRef = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: ''
})

const roleForm = reactive({
  id: null,
  roleName: '',
  roleLabel: '',
  menuIdList: [],
  resourceIdList: []
})

const rules = {
  roleName: [
    { required: true, message: '角色名不能为空', trigger: 'blur' }
  ],
  roleLabel: [
    { required: true, message: '权限标签不能为空', trigger: 'blur' }
  ]
}

// 查询角色列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listRoles(queryParams)
    if (res.flag) {
      roleList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取菜单列表
const getMenuList = async () => {
  try {
    const res = await listMenus({})
    if (res.flag) {
      menuList.value = res.data
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
  }
}

// 获取资源列表
const getResourceList = async () => {
  try {
    const res = await listResources({})
    if (res.flag) {
      resourceList.value = res.data
    }
  } catch (error) {
    console.error('获取资源列表失败:', error)
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  menuDialogVisible.value = true
  resetForm()
}

// 菜单权限
const handleMenuPermission = async (row) => {
  dialogTitle.value = row.id ? '修改角色' : '新增角色'
  if (row.id) {
    Object.assign(roleForm, row)
  }
  await nextTick()
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys(roleForm.menuIdList || [])
  }
  menuDialogVisible.value = true
}

// 资源权限
const handleResourcePermission = async (row) => {
  Object.assign(roleForm, row)
  await nextTick()
  if (resourceTreeRef.value) {
    resourceTreeRef.value.setCheckedKeys(roleForm.resourceIdList || [])
  }
  resourceDialogVisible.value = true
}

// 禁用状态改变
const handleDisableChange = async (row) => {
  try {
    const res = await saveOrUpdateRole({
      id: row.id,
      roleName: row.roleName,
      roleLabel: row.roleLabel,
      isDisable: row.isDisable
    })
    if (res.flag) {
      ElMessage.success('修改成功')
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    row.isDisable = row.isDisable === 1 ? 0 : 1
  }
}

// 删除
const handleDelete = (row) => {
  const roleIds = row?.id ? [row.id] : ids.value
  const message = row?.id ? `是否确认删除角色"${row.roleName}"?` : '是否确认删除选中的角色?'

  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRoles(roleIds)
      if (res.flag) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除角色失败:', error)
    }
  }).catch(() => {})
}

// 提交菜单权限
const handleMenuSubmit = async () => {
  if (!menuFormRef.value) return
  await menuFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const checkedKeys = menuTreeRef.value.getCheckedKeys()
        const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
        const res = await saveOrUpdateRole({
          id: roleForm.id,
          roleName: roleForm.roleName,
          roleLabel: roleForm.roleLabel,
          menuIdList: [...checkedKeys, ...halfCheckedKeys],
          resourceIdList: null
        })
        if (res.flag) {
          ElMessage.success(roleForm.id ? '修改成功' : '新增成功')
          menuDialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存角色失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 提交资源权限
const handleResourceSubmit = async () => {
  submitLoading.value = true
  try {
    const checkedKeys = resourceTreeRef.value.getCheckedKeys()
    const res = await saveOrUpdateRole({
      id: roleForm.id,
      roleName: roleForm.roleName,
      roleLabel: roleForm.roleLabel,
      menuIdList: null,
      resourceIdList: checkedKeys
    })
    if (res.flag) {
      ElMessage.success('修改成功')
      resourceDialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error('保存角色失败:', error)
  } finally {
    submitLoading.value = false
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

// 关闭对话框
const handleClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  roleForm.id = null
  roleForm.roleName = ''
  roleForm.roleLabel = ''
  roleForm.menuIdList = []
  roleForm.resourceIdList = []
  menuFormRef.value?.resetFields()
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys([])
  }
  if (resourceTreeRef.value) {
    resourceTreeRef.value.setCheckedKeys([])
  }
}

onMounted(() => {
  getList()
  getMenuList()
  getResourceList()
})
</script>

<style scoped lang="scss">
.role-management {
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
