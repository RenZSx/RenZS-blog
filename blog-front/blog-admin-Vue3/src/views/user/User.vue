<template>
  <div class="user-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select
          v-model="queryParams.loginType"
          placeholder="请选择登录方式"
          clearable
          style="width: 150px; margin-right: 10px"
        >
          <el-option
            v-for="item in loginTypeList"
            :key="item.type"
            :label="item.desc"
            :value="item.type"
          />
        </el-select>
        <el-input
          v-model="queryParams.keywords"
          placeholder="请输入昵称"
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
        <el-table-column label="昵称" align="center" prop="nickname" width="140" />
        <el-table-column label="登录方式" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.loginType === 1" type="success">邮箱</el-tag>
            <el-tag v-else-if="scope.row.loginType === 2">QQ</el-tag>
            <el-tag v-else-if="scope.row.loginType === 3" type="danger">微博</el-tag>
            <el-tag v-else-if="scope.row.loginType === 4" type="primary">Gitee</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户角色" align="center">
          <template #default="scope">
            <el-tag
              v-for="(item, index) in scope.row.roleList"
              :key="index"
              style="margin: 2px"
            >
              {{ item.roleName }}
            </el-tag>
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
        <el-table-column label="登录IP" align="center" prop="ipAddress" width="140" />
        <el-table-column label="登录地址" align="center" prop="ipSource" width="140" />
        <el-table-column label="创建时间" align="center" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上次登录时间" align="center" width="180">
          <template #default="scope">
            <span>{{ formatDate(scope.row.lastLoginTime, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
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

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="修改用户"
      width="500px"
      @close="handleClose"
    >
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="roleIdList">
            <el-checkbox
              v-for="item in roleList"
              :key="item.id"
              :label="item.id"
            >
              {{ item.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增用户对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增用户"
      width="500px"
      @close="handleAddClose"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="addForm.password"
            type="password"
            show-password
            placeholder="请输入密码(至少6位)"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称(可选)" />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="addRoleIdList">
            <el-checkbox
              v-for="item in roleList"
              :key="item.id"
              :label="item.id"
            >
              {{ item.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleAddSubmit" :loading="addLoading">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Edit, Plus } from '@element-plus/icons-vue'
import { listUsers, addUser, updateUserStatus, updateUserRole } from '@/api/blog/user'
import { listRoles } from '@/api/blog/role'
import { formatDate } from '@/utils/blog'

const loading = ref(false)
const submitLoading = ref(false)
const addLoading = ref(false)
const total = ref(0)
const userList = ref([])
const roleList = ref([])
const dialogVisible = ref(false)
const addDialogVisible = ref(false)
const roleIdList = ref([])
const addRoleIdList = ref([])
const formRef = ref(null)
const addFormRef = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  keywords: '',
  loginType: null
})

const form = reactive({
  userInfoId: null,
  nickname: '',
  roleList: []
})

const addForm = reactive({
  username: '',
  password: '',
  nickname: ''
})

const addRules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ]
}

const loginTypeList = [
  { type: 1, desc: '邮箱' },
  { type: 2, desc: 'QQ' },
  { type: 3, desc: '微博' },
  { type: 4, desc: 'Gitee' }
]

// 查询用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listUsers(queryParams)
    if (res.flag) {
      userList.value = res.data.recordList
      total.value = res.data.count
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const getRoleList = async () => {
  try {
    const res = await listRoles({})
    if (res.flag) {
      roleList.value = res.data.recordList
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 禁用状态改变
const handleDisableChange = async (row) => {
  try {
    const res = await updateUserStatus({
      id: row.userInfoId,
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

// 新增用户
const handleAdd = () => {
  addForm.username = ''
  addForm.password = ''
  addForm.nickname = ''
  addRoleIdList.value = []
  addDialogVisible.value = true
}

// 新增用户提交
const handleAddSubmit = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    addLoading.value = true
    try {
      const res = await addUser({
        username: addForm.username.trim(),
        password: addForm.password,
        nickname: addForm.nickname.trim() || null,
        roleIdList: addRoleIdList.value.length ? addRoleIdList.value : null
      })
      if (res.flag) {
        ElMessage.success('新增成功')
        addDialogVisible.value = false
        getList()
      }
    } catch (error) {
      console.error('新增用户失败:', error)
    } finally {
      addLoading.value = false
    }
  })
}

// 关闭新增对话框
const handleAddClose = () => {
  addRoleIdList.value = []
  addFormRef.value?.resetFields()
}

// 编辑
const handleEdit = (row) => {
  form.userInfoId = row.userInfoId
  form.nickname = row.nickname
  form.roleList = row.roleList
  roleIdList.value = row.roleList.map(item => item.id)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const res = await updateUserRole({
      userInfoId: form.userInfoId,
      nickname: form.nickname,
      roleIdList: roleIdList.value
    })
    if (res.flag) {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error('修改用户角色失败:', error)
  } finally {
    submitLoading.value = false
  }
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
  roleIdList.value = []
}

onMounted(() => {
  getList()
  getRoleList()
})
</script>

<style scoped lang="scss">
.user-management {
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
