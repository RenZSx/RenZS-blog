# 开发者指南

> 面向新开发者的详细开发指南，帮助你快速上手博客后台管理系统的开发工作。

## 目录

- [快速上手](#快速上手)
- [项目结构详解](#项目结构详解)
- [如何新增功能模块](#如何新增功能模块)
- [如何新增页面](#如何新增页面)
- [API 调用规范](#api-调用规范)
- [状态管理使用](#状态管理使用)
- [组件开发规范](#组件开发规范)
- [样式开发规范](#样式开发规范)
- [调试技巧](#调试技巧)
- [常见问题解决](#常见问题解决)

---

## 快速上手

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/blog-satoken.git
cd blog-satoken/blog-front/blog-admin-Vue3
```

### 2. 安装依赖

推荐使用 pnpm 或 npm：

```bash
# 使用 npm
npm install

# 使用 pnpm（推荐）
pnpm install

# 使用 yarn
yarn install
```

### 3. 配置环境

项目提供了三个环境配置文件：

#### 开发环境 (`.env.development`)
```env
# 页面标题
VITE_APP_TITLE = 若依管理系统

# 开发环境配置
VITE_APP_ENV = 'development'

# API 基础路径
VITE_APP_BASE_API = '/dev-api'
```

#### 生产环境 (`.env.production`)
```env
VITE_APP_TITLE = 若依管理系统
VITE_APP_ENV = 'production'
VITE_APP_BASE_API = '/prod-api'
```

**重要配置：后端接口地址**

修改 `vite.config.js` 中的后端接口地址：

```javascript
const baseUrl = 'http://localhost:8080' // 修改为你的后端接口地址
```

### 4. 启动开发服务器

```bash
npm run dev
```

服务启动后，浏览器会自动打开 `http://localhost:80`

默认账号：`admin` / `admin123`

### 5. 构建生产环境

```bash
# 生产环境构建
npm run build:prod

# 测试环境构建
npm run build:stage
```

---

## 项目结构详解

```
blog-admin-Vue3/
├── public/                    # 静态资源（不经过webpack处理）
├── src/                       # 源代码目录
│   ├── api/                   # API 接口定义
│   │   ├── blog/              # 博客相关 API
│   │   │   ├── article.js     # 文章接口
│   │   │   ├── category.js    # 分类接口
│   │   │   ├── tag.js         # 标签接口
│   │   │   ├── comment.js     # 评论接口
│   │   │   └── ...
│   │   ├── monitor/           # 监控相关 API
│   │   ├── system/            # 系统管理 API
│   │   └── tool/              # 工具相关 API
│   │
│   ├── assets/                # 静态资源
│   │   ├── icons/             # 图标文件
│   │   │   └── svg/           # SVG 图标
│   │   ├── images/            # 图片资源
│   │   ├── styles/            # 全局样式
│   │   │   ├── index.scss     # 样式入口文件
│   │   │   ├── variables.module.scss  # 样式变量
│   │   │   ├── blog.scss      # 博客相关样式
│   │   │   ├── element-ui.scss # Element Plus 样式覆盖
│   │   │   └── ...
│   │   └── js/                # JS 工具文件
│   │
│   ├── components/            # 全局组件
│   │   ├── BlogEditor/        # 博客编辑器（Markdown）
│   │   ├── TagCloud/          # 标签云组件
│   │   ├── ImageUpload/       # 图片上传组件
│   │   ├── FileUpload/        # 文件上传组件
│   │   ├── Pagination/        # 分页组件
│   │   ├── RightToolbar/      # 右侧工具栏
│   │   ├── DictTag/           # 字典标签
│   │   └── ...
│   │
│   ├── config/                # 配置文件
│   │
│   ├── directive/             # 自定义指令
│   │   ├── permission/        # 权限指令 v-hasPermi
│   │   └── common/            # 通用指令
│   │
│   ├── layout/                # 布局组件
│   │   ├── components/        # 布局子组件
│   │   │   ├── Sidebar/       # 侧边栏
│   │   │   ├── Navbar/        # 顶部导航
│   │   │   ├── TagsView/      # 标签页
│   │   │   └── AppMain.vue    # 主内容区
│   │   └── index.vue          # 布局入口
│   │
│   ├── plugins/               # 插件
│   │   ├── cache.js           # 缓存插件
│   │   ├── download.js        # 下载插件
│   │   └── modal.js           # 弹窗插件
│   │
│   ├── router/                # 路由配置
│   │   ├── index.js           # 路由入口
│   │   └── blog.js            # 博客模块路由
│   │
│   ├── store/                 # Pinia 状态管理
│   │   ├── modules/           # 状态模块
│   │   │   ├── user.js        # 用户状态
│   │   │   ├── app.js         # 应用状态
│   │   │   ├── permission.js  # 权限状态
│   │   │   ├── settings.js    # 设置状态
│   │   │   └── tagsView.js    # 标签页状态
│   │   └── index.js           # store 入口
│   │
│   ├── utils/                 # 工具函数
│   │   ├── request.js         # axios 封装（重要）
│   │   ├── auth.js            # 认证相关
│   │   ├── ruoyi.js           # 通用工具函数
│   │   ├── validate.js        # 表单验证
│   │   ├── dict.js            # 字典工具
│   │   └── errorCode.js       # 错误码定义
│   │
│   ├── views/                 # 页面组件
│   │   ├── blog/              # 博客管理页面
│   │   │   ├── article/       # 文章管理
│   │   │   │   ├── list.vue   # 文章列表
│   │   │   │   └── edit.vue   # 文章编辑
│   │   │   ├── category/      # 分类管理
│   │   │   ├── tag/           # 标签管理
│   │   │   ├── comment/       # 评论管理
│   │   │   ├── talk/          # 说说管理
│   │   │   ├── album/         # 相册管理
│   │   │   └── ...
│   │   ├── system/            # 系统管理页面
│   │   ├── monitor/           # 系统监控页面
│   │   ├── index.vue          # 首页
│   │   ├── login.vue          # 登录页
│   │   └── error/             # 错误页面
│   │
│   ├── App.vue                # 根组件
│   └── main.js                # 入口文件
│
├── .env.development           # 开发环境变量
├── .env.production            # 生产环境变量
├── .env.staging               # 测试环境变量
├── index.html                 # HTML 模板
├── package.json               # 项目依赖
├── vite.config.js             # Vite 配置文件
└── README.md                  # 项目说明
```

### 关键目录说明

- **`src/api/`**: 所有 API 接口的定义，按模块划分，每个文件导出多个接口函数
- **`src/components/`**: 全局共享组件，可在任何页面中使用
- **`src/layout/`**: 应用的主布局结构（侧边栏、顶栏、内容区）
- **`src/router/`**: 路由配置，包括公共路由和动态路由
- **`src/store/`**: Pinia 状态管理，用于全局状态共享
- **`src/utils/`**: 通用工具函数，如 API 请求封装、权限验证等
- **`src/views/`**: 所有页面组件，按功能模块划分

---

## 如何新增功能模块

假设我们要新增一个**"图书管理"**模块，包含图书的增删改查功能。

### 第 1 步：创建 API 接口文件

在 `src/api/blog/` 目录下创建 `book.js`：

```javascript
import request from '@/utils/request'

// 查询图书列表
export function listBooks(query) {
  return request({
    url: '/admin/books',
    method: 'get',
    params: query
  })
}

// 查询图书详情
export function getBook(bookId) {
  return request({
    url: '/admin/books/' + bookId,
    method: 'get'
  })
}

// 新增图书
export function addBook(data) {
  return request({
    url: '/admin/books',
    method: 'post',
    data: data
  })
}

// 修改图书
export function updateBook(data) {
  return request({
    url: '/admin/books',
    method: 'put',
    data: data
  })
}

// 删除图书
export function deleteBook(bookIds) {
  return request({
    url: '/admin/books',
    method: 'delete',
    data: bookIds
  })
}
```

### 第 2 步：创建页面组件

在 `src/views/blog/` 下创建 `book` 目录，并创建 `index.vue`：

```vue
<template>
  <div class="app-container">
    <el-card shadow="hover">
      <!-- 搜索栏 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="图书名称" prop="bookName">
          <el-input
            v-model="queryParams.bookName"
            placeholder="请输入图书名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['blog:book:add']"
          >新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['blog:book:remove']"
          >删除</el-button>
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="bookList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="图书ID" align="center" prop="bookId" />
        <el-table-column label="图书名称" align="center" prop="bookName" />
        <el-table-column label="作者" align="center" prop="author" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['blog:book:edit']"
            >修改</el-button>
            <el-button
              type="primary"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['blog:book:remove']"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="bookRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="图书名称" prop="bookName">
          <el-input v-model="form.bookName" placeholder="请输入图书名称" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Book">
import { listBooks, getBook, addBook, updateBook, deleteBook } from '@/api/blog/book'

const { proxy } = getCurrentInstance()

// 响应式数据
const bookList = ref([])
const loading = ref(true)
const open = ref(false)
const title = ref('')
const total = ref(0)
const ids = ref([])
const multiple = ref(true)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  bookName: undefined
})

const form = ref({})

const rules = ref({
  bookName: [
    { required: true, message: '图书名称不能为空', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '作者不能为空', trigger: 'blur' }
  ]
})

// 获取列表
function getList() {
  loading.value = true
  listBooks(queryParams.value).then(response => {
    bookList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 搜索按钮操作
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.bookId)
  multiple.value = !selection.length
}

// 新增按钮操作
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加图书'
}

// 修改按钮操作
function handleUpdate(row) {
  reset()
  const bookId = row.bookId || ids.value[0]
  getBook(bookId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改图书'
  })
}

// 提交按钮
function submitForm() {
  proxy.$refs['bookRef'].validate(valid => {
    if (valid) {
      if (form.value.bookId != null) {
        updateBook(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addBook(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

// 删除按钮操作
function handleDelete(row) {
  const bookIds = row.bookId || ids.value
  proxy.$modal.confirm('是否确认删除图书编号为"' + bookIds + '"的数据项？').then(function() {
    return deleteBook(bookIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    bookId: null,
    bookName: null,
    author: null,
    description: null
  }
  proxy.resetForm('bookRef')
}

// 初始化
onMounted(() => {
  getList()
})
</script>
```

### 第 3 步：配置路由

在 `src/router/blog.js` 或 `src/router/index.js` 中添加路由：

```javascript
{
  path: '/blog/book',
  component: Layout,
  hidden: false,
  children: [
    {
      path: '',
      component: () => import('@/views/blog/book/index'),
      name: 'Book',
      meta: { 
        title: '图书管理', 
        icon: 'book',
        noCache: true 
      }
    }
  ]
}
```

### 第 4 步：添加菜单（可选）

如果需要在侧边栏显示菜单，需要在后端数据库中添加菜单记录，或在系统管理 -> 菜单管理中手动添加：

- 菜单名称：图书管理
- 路由地址：`/blog/book`
- 组件路径：`blog/book/index`
- 权限标识：`blog:book:list`

### 第 5 步：测试功能

1. 启动开发服务器：`npm run dev`
2. 登录系统
3. 访问图书管理页面
4. 测试增删改查功能

---

## 如何新增页面

### 基础页面模板

创建一个标准的列表页面模板：

```vue
<template>
  <div class="app-container">
    <el-card shadow="hover">
      <!-- 标题区域 -->
      <template #header>
        <div class="card-header">
          <span>页面标题</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入关键字"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮区域 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            icon="Plus"
            @click="handleAdd"
          >新增</el-button>
        </el-col>
      </el-row>

      <!-- 表格数据区域 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="名称" align="center" prop="name" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button
              type="primary"
              link
              icon="Edit"
              @click="handleUpdate(scope.row)"
            >修改</el-button>
            <el-button
              type="primary"
              link
              icon="Delete"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PageName">
import { listXXX, getXXX, addXXX, updateXXX, deleteXXX } from '@/api/blog/xxx'

const { proxy } = getCurrentInstance()

// ========== 响应式数据 ==========
const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const ids = ref([])
const multiple = ref(true)

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined
})

// 表单数据
const formData = ref({
  id: null,
  name: null
})

// 表单验证规则
const formRules = ref({
  name: [
    { required: true, message: '名称不能为空', trigger: 'blur' }
  ]
})

// ========== 方法定义 ==========

/** 加载数据列表 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await listXXX(queryParams.value)
    dataList.value = res.rows
    total.value = res.total
  } catch (error) {
    console.error('加载数据失败:', error)
    proxy.$modal.msgError('加载数据失败')
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadData()
}

/** 重置按钮操作 */
const resetQuery = () => {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '添加数据'
  dialogVisible.value = true
}

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  resetForm()
  const id = row.id || ids.value[0]
  try {
    const res = await getXXX(id)
    formData.value = res.data
    dialogTitle.value = '修改数据'
    dialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    proxy.$modal.msgError('获取详情失败')
  }
}

/** 提交表单 */
const submitForm = () => {
  proxy.$refs['formRef'].validate(async (valid) => {
    if (valid) {
      try {
        if (formData.value.id) {
          await updateXXX(formData.value)
          proxy.$modal.msgSuccess('修改成功')
        } else {
          await addXXX(formData.value)
          proxy.$modal.msgSuccess('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败:', error)
        proxy.$modal.msgError('操作失败')
      }
    }
  })
}

/** 删除按钮操作 */
const handleDelete = (row) => {
  const deleteIds = row.id ? [row.id] : ids.value
  proxy.$modal.confirm('是否确认删除选中的数据？').then(async () => {
    try {
      await deleteXXX(deleteIds)
      proxy.$modal.msgSuccess('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      proxy.$modal.msgError('删除失败')
    }
  }).catch(() => {})
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: null,
    name: null
  }
  proxy.resetForm('formRef')
}

// ========== 生命周期 ==========
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

### 页面开发步骤

1. **确定页面功能需求**：明确页面要实现什么功能
2. **创建 Vue 文件**：在 `src/views/` 对应目录下创建组件
3. **编写页面结构**：使用 Element Plus 组件构建 UI
4. **引入 API**：从 `src/api/` 引入所需接口
5. **实现业务逻辑**：编写数据加载、表单提交等逻辑
6. **添加样式**：编写组件样式
7. **配置路由**：在路由文件中注册页面
8. **测试功能**：全面测试页面各项功能

---

## API 调用规范

### 1. 请求封装

所有 API 请求都通过 `src/utils/request.js` 中封装的 axios 实例发送：

```javascript
import request from '@/utils/request'

// GET 请求
export function getData(params) {
  return request({
    url: '/api/data',
    method: 'get',
    params: params  // 查询参数
  })
}

// POST 请求
export function createData(data) {
  return request({
    url: '/api/data',
    method: 'post',
    data: data  // 请求体
  })
}

// PUT 请求
export function updateData(data) {
  return request({
    url: '/api/data',
    method: 'put',
    data: data
  })
}

// DELETE 请求
export function deleteData(ids) {
  return request({
    url: '/api/data',
    method: 'delete',
    data: ids
  })
}
```

### 2. 错误处理

请求已经在 `request.js` 中统一处理了错误：

```javascript
// 在组件中使用 try-catch 捕获错误
const loadData = async () => {
  loading.value = true
  try {
    const res = await listData(queryParams.value)
    dataList.value = res.rows
    total.value = res.total
  } catch (error) {
    console.error('加载失败:', error)
    // 错误提示已由拦截器处理，这里只需记录日志
  } finally {
    loading.value = false
  }
}
```

### 3. Loading 管理

```javascript
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getData()
    // 处理数据
  } finally {
    loading.value = false  // 确保 loading 状态被重置
  }
}
```

### 4. 数据转换

响应拦截器会自动处理响应数据，返回格式：

```javascript
// 成功响应（code=200）
{
  code: 200,
  msg: "操作成功",
  data: { ... },
  rows: [ ... ],  // 列表数据
  total: 100      // 总记录数
}

// 在组件中使用
const res = await listData()
dataList.value = res.rows    // 列表数据
total.value = res.total      // 总数
```

### 5. 请求头配置

```javascript
// 自动添加 Token（已在 request.js 中配置）
config.headers['Authorization'] = 'Bearer ' + getToken()

// 特殊请求头
export function uploadFile(data) {
  return request({
    url: '/api/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: data
  })
}

// 不需要 Token 的请求
export function publicApi() {
  return request({
    url: '/api/public',
    method: 'get',
    headers: {
      isToken: false  // 不添加 Token
    }
  })
}
```

### 6. 防重复提交

系统已内置防重复提交机制，可配置：

```javascript
export function submitForm(data) {
  return request({
    url: '/api/submit',
    method: 'post',
    data: data,
    headers: {
      repeatSubmit: false,  // 启用防重复提交
      interval: 2000        // 间隔时间（毫秒）
    }
  })
}
```

---

## 状态管理使用

### 1. Pinia Store 创建

在 `src/store/modules/` 下创建新的 store：

```javascript
// src/store/modules/blog.js
import { defineStore } from 'pinia'
import { listArticles } from '@/api/blog/article'

const useBlogStore = defineStore('blog', {
  // State 定义
  state: () => ({
    articles: [],
    categories: [],
    tags: [],
    currentArticle: null
  }),

  // Getters 定义
  getters: {
    // 获取文章总数
    articleCount: (state) => state.articles.length,
    
    // 获取已发布文章
    publishedArticles: (state) => {
      return state.articles.filter(item => item.status === 1)
    },
    
    // 根据 ID 获取文章
    getArticleById: (state) => {
      return (id) => state.articles.find(item => item.id === id)
    }
  },

  // Actions 定义
  actions: {
    // 加载文章列表
    async fetchArticles(params) {
      try {
        const res = await listArticles(params)
        this.articles = res.rows
        return res
      } catch (error) {
        console.error('加载文章失败:', error)
        throw error
      }
    },

    // 设置当前文章
    setCurrentArticle(article) {
      this.currentArticle = article
    },

    // 清空数据
    clearData() {
      this.articles = []
      this.currentArticle = null
    }
  }
})

export default useBlogStore
```

### 2. 在组件中使用 Store

```vue
<script setup>
import useBlogStore from '@/store/modules/blog'

const blogStore = useBlogStore()

// 访问 State
console.log(blogStore.articles)

// 访问 Getters
console.log(blogStore.articleCount)
console.log(blogStore.publishedArticles)

// 调用 Actions
const loadArticles = async () => {
  await blogStore.fetchArticles({ pageNum: 1, pageSize: 10 })
}

// 直接修改 State
blogStore.currentArticle = { id: 1, title: '文章标题' }

// 使用 $patch 批量修改
blogStore.$patch({
  articles: [],
  currentArticle: null
})

// 使用 $patch 函数式修改
blogStore.$patch((state) => {
  state.articles.push(newArticle)
  state.currentArticle = newArticle
})

// 重置 Store
blogStore.$reset()
</script>
```

### 3. Store 持久化

如果需要持久化存储，可以使用 `pinia-plugin-persistedstate`：

```javascript
import { defineStore } from 'pinia'

const useBlogStore = defineStore('blog', {
  state: () => ({
    articles: []
  }),
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'blog-store',
        storage: localStorage,  // 或 sessionStorage
        paths: ['articles']     // 只持久化指定字段
      }
    ]
  }
})
```

### 4. 现有 Store 说明

- **useUserStore**: 用户信息、登录状态、权限
- **useAppStore**: 应用配置、侧边栏状态、设备类型
- **usePermissionStore**: 路由权限、菜单权限
- **useSettingsStore**: 主题设置、布局配置
- **useTagsViewStore**: 标签页管理

---

## 组件开发规范

### 1. 组件命名

- **文件名**：使用 PascalCase（大驼峰）命名
  ```
  BlogEditor.vue
  TagCloud.vue
  ImageUpload.vue
  ```

- **组件 name**：与文件名保持一致
  ```vue
  <script setup name="BlogEditor">
  </script>
  ```

### 2. Props 定义

使用 TypeScript 风格定义 Props：

```vue
<script setup>
// 方式 1：简单定义
const props = defineProps({
  title: String,
  count: Number,
  disabled: Boolean
})

// 方式 2：详细定义（推荐）
const props = defineProps({
  title: {
    type: String,
    default: '',
    required: true
  },
  count: {
    type: Number,
    default: 0
  },
  options: {
    type: Array,
    default: () => []
  },
  config: {
    type: Object,
    default: () => ({})
  },
  callback: {
    type: Function,
    default: null
  }
})

// 方式 3：使用解构
const { title, count = 0 } = defineProps({
  title: String,
  count: Number
})
</script>
```

### 3. Emits 定义

```vue
<script setup>
// 定义事件
const emit = defineEmits(['update', 'delete', 'change'])

// 触发事件
const handleClick = () => {
  emit('update', { id: 1, name: '测试' })
}

// 带验证的 emits
const emit = defineEmits({
  update: (payload) => {
    // 验证 payload
    return payload.id != null
  },
  delete: null  // 不验证
})
</script>
```

### 4. 插槽使用

```vue
<template>
  <div class="card">
    <!-- 默认插槽 -->
    <div class="card-body">
      <slot>默认内容</slot>
    </div>

    <!-- 具名插槽 -->
    <div class="card-header">
      <slot name="header"></slot>
    </div>

    <!-- 作用域插槽 -->
    <div class="card-footer">
      <slot name="footer" :data="footerData"></slot>
    </div>
  </div>
</template>

<script setup>
const footerData = ref({ count: 10 })
</script>
```

使用组件：

```vue
<template>
  <Card>
    <!-- 默认插槽 -->
    <p>这是内容</p>

    <!-- 具名插槽 -->
    <template #header>
      <h3>标题</h3>
    </template>

    <!-- 作用域插槽 -->
    <template #footer="{ data }">
      <span>总数：{{ data.count }}</span>
    </template>
  </Card>
</template>
```

### 5. 组件通信

**父子组件通信：**

```vue
<!-- 父组件 -->
<template>
  <ChildComponent
    :title="title"
    @update="handleUpdate"
  />
</template>

<script setup>
const title = ref('标题')
const handleUpdate = (data) => {
  console.log('子组件传递的数据:', data)
}
</script>

<!-- 子组件 -->
<script setup>
const props = defineProps({
  title: String
})

const emit = defineEmits(['update'])

const handleClick = () => {
  emit('update', { message: '更新数据' })
}
</script>
```

**跨层级通信（provide/inject）：**

```vue
<!-- 祖先组件 -->
<script setup>
import { provide } from 'vue'

const theme = ref('dark')
provide('theme', theme)
</script>

<!-- 后代组件 -->
<script setup>
import { inject } from 'vue'

const theme = inject('theme', 'light')  // 第二个参数是默认值
</script>
```

### 6. 组件引用

```vue
<template>
  <el-form ref="formRef">
    <!-- 表单内容 -->
  </el-form>
</template>

<script setup>
const formRef = ref(null)

// 调用子组件方法
const validate = () => {
  formRef.value.validate((valid) => {
    console.log('验证结果:', valid)
  })
}

// 访问子组件属性
const getFormData = () => {
  console.log(formRef.value.model)
}
</script>
```

---

## 样式开发规范

### 1. SCSS 变量使用

项目中的样式变量定义在 `src/assets/styles/variables.module.scss`：

```vue
<script setup>
import variables from '@/assets/styles/variables.module.scss'

// 使用变量
console.log(variables.colorPrimary)  // #409EFF
console.log(variables.menuBg)        // #1a1f2e
</script>

<style scoped lang="scss">
// 在样式中导入变量
@import '@/assets/styles/variables.module.scss';

.my-component {
  background-color: $menuBg;
  color: $colorPrimary;
}
</style>
```

### 2. 公共样式类

项目提供了丰富的公共样式类（`src/assets/styles/ruoyi.scss`）：

```html
<!-- 间距 -->
<div class="mb8">下边距 8px</div>
<div class="mt10">上边距 10px</div>
<div class="ml20">左边距 20px</div>
<div class="mr15">右边距 15px</div>

<!-- 布局 -->
<div class="clearfix">清除浮动</div>
<div class="pull-left">左浮动</div>
<div class="pull-right">右浮动</div>

<!-- 文本 -->
<div class="text-center">居中对齐</div>
<div class="text-left">左对齐</div>
<div class="text-right">右对齐</div>

<!-- 容器 -->
<div class="app-container">应用容器</div>
<div class="dialog-footer">对话框底部</div>
```

### 3. 响应式设计

使用 SCSS mixin 实现响应式：

```scss
<style scoped lang="scss">
.my-component {
  // 桌面端
  width: 1200px;

  // 平板
  @media (max-width: 992px) {
    width: 100%;
  }

  // 手机
  @media (max-width: 768px) {
    width: 100%;
    padding: 10px;
  }
}
</style>
```

### 4. 主题定制

使用 CSS 变量定制主题：

```scss
<style scoped lang="scss">
.my-component {
  // 使用 CSS 变量
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
  border: 1px solid var(--el-border-color);
}

// 暗黑模式适配
html.dark {
  .my-component {
    background-color: var(--el-bg-color-overlay);
  }
}
</style>
```

### 5. Scoped 样式

```vue
<style scoped lang="scss">
/* 只作用于当前组件 */
.button {
  color: red;
}

/* 深度选择器：修改子组件样式 */
:deep(.el-input__inner) {
  background-color: #f5f5f5;
}

/* 插槽选择器 */
:slotted(.slot-content) {
  color: blue;
}

/* 全局选择器 */
:global(.global-class) {
  font-size: 14px;
}
</style>
```

### 6. 样式组织建议

```vue
<style scoped lang="scss">
// 1. 导入变量和 mixin
@import '@/assets/styles/variables.module.scss';

// 2. 组件根元素
.my-component {
  // 布局相关
  display: flex;
  flex-direction: column;
  
  // 尺寸相关
  width: 100%;
  height: 100%;
  
  // 间距相关
  padding: 20px;
  margin: 10px 0;
  
  // 颜色相关
  background-color: $menuBg;
  color: $menuText;
  
  // 字体相关
  font-size: 14px;
  font-weight: bold;
  
  // 其他
  border-radius: 4px;
  transition: all 0.3s;
}

// 3. 子元素
.my-component__header {
  // ...
}

.my-component__body {
  // ...
}

// 4. 状态修饰
.my-component.is-active {
  // ...
}

// 5. 响应式
@media (max-width: 768px) {
  .my-component {
    padding: 10px;
  }
}
</style>
```

---

## 调试技巧

### 1. Vue DevTools 使用

安装 Vue DevTools 浏览器扩展，可以：

- 查看组件树结构
- 检查组件的 props、data、computed
- 查看 Pinia store 状态
- 时间旅行调试
- 性能分析

### 2. 网络请求调试

打开浏览器开发者工具 -> Network 面板：

- 查看请求 URL、方法、状态码
- 检查请求头和响应头
- 查看请求参数和响应数据
- 分析请求耗时

**使用 console.log 调试请求：**

```javascript
const loadData = async () => {
  console.log('请求参数:', queryParams.value)
  
  const res = await listData(queryParams.value)
  
  console.log('响应数据:', res)
  console.log('列表数据:', res.rows)
  console.log('总记录数:', res.total)
}
```

### 3. 性能分析

**使用 Performance 面板：**

1. 打开 Chrome DevTools -> Performance
2. 点击录制按钮
3. 操作页面
4. 停止录制，分析结果

**Vue 性能提示：**

```javascript
// 避免不必要的响应式
const data = shallowRef(largeObject)  // 浅层响应式

// 使用 computed 缓存计算结果
const filtered = computed(() => {
  return list.value.filter(item => item.status === 1)
})

// 使用 v-once 渲染静态内容
<div v-once>{{ staticContent }}</div>

// 使用 v-memo 缓存子树
<div v-memo="[item.id]">{{ item.name }}</div>
```

### 4. 错误追踪

**全局错误处理：**

```javascript
// main.js
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err)
  console.error('组件实例:', instance)
  console.error('错误信息:', info)
}
```

**组件内错误处理：**

```vue
<script setup>
import { onErrorCaptured } from 'vue'

onErrorCaptured((err, instance, info) => {
  console.error('捕获错误:', err)
  return false  // 阻止错误继续传播
})
</script>
```

### 5. 断点调试

在代码中添加 `debugger` 语句：

```javascript
const handleSubmit = () => {
  debugger  // 程序会在这里暂停
  
  const data = {
    name: formData.value.name,
    age: formData.value.age
  }
  
  submitData(data)
}
```

也可以在浏览器 DevTools -> Sources 面板中设置断点。

### 6. 常用调试命令

```javascript
// 打印对象
console.log('数据:', data)

// 打印表格
console.table(list)

// 打印警告
console.warn('警告信息')

// 打印错误
console.error('错误信息')

// 分组打印
console.group('分组标题')
console.log('项目 1')
console.log('项目 2')
console.groupEnd()

// 计时
console.time('操作')
// ... 执行操作
console.timeEnd('操作')  // 输出耗时

// 追踪调用栈
console.trace('调用栈')
```

---

## 常见问题解决

### 1. 路由跳转问题

**问题：路由跳转不生效**

```javascript
// 错误写法
router.push('/blog/article')  // router 未定义

// 正确写法
import { useRouter } from 'vue-router'
const router = useRouter()
router.push('/blog/article')

// 或使用命名路由
router.push({ name: 'Article' })

// 带参数跳转
router.push({
  path: '/blog/article/edit',
  query: { id: 123 }
})
```

**问题：页面刷新后 404**

确保路由模式配置正确，并在服务器配置 URL 重写规则。

```javascript
// router/index.js
const router = createRouter({
  history: createWebHistory(),  // 使用 history 模式
  routes: constantRoutes
})
```

### 2. 权限控制问题

**问题：按钮权限不生效**

```vue
<!-- 使用 v-hasPermi 指令 -->
<el-button
  v-hasPermi="['blog:article:add']"
  @click="handleAdd"
>新增</el-button>

<!-- 多个权限（满足一个即可） -->
<el-button
  v-hasPermi="['blog:article:add', 'blog:article:edit']"
  @click="handleAdd"
>操作</el-button>
```

**问题：页面权限不生效**

在路由配置中添加权限标识：

```javascript
{
  path: '/blog/article',
  component: () => import('@/views/blog/article/list'),
  meta: {
    title: '文章管理',
    permissions: ['blog:article:list']  // 权限标识
  }
}
```

### 3. 样式冲突问题

**问题：样式被覆盖**

```vue
<!-- 使用 scoped 限制样式作用域 -->
<style scoped lang="scss">
.my-class {
  color: red;
}
</style>

<!-- 需要修改子组件样式时使用深度选择器 -->
<style scoped lang="scss">
:deep(.el-input__inner) {
  background-color: #f5f5f5;
}
</style>
```

**问题：Element Plus 样式不生效**

确保已正确导入 Element Plus 样式：

```javascript
// main.js
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
```

### 4. 性能优化问题

**问题：列表数据量大导致页面卡顿**

```vue
<template>
  <!-- 使用虚拟滚动 -->
  <el-table-v2
    :columns="columns"
    :data="largeList"
    :width="700"
    :height="400"
  />

  <!-- 或使用分页 -->
  <pagination
    :total="total"
    v-model:page="queryParams.pageNum"
    v-model:limit="queryParams.pageSize"
    @pagination="loadData"
  />
</template>
```

**问题：图片加载慢**

```vue
<template>
  <!-- 使用懒加载 -->
  <el-image
    :src="imageUrl"
    lazy
    :preview-src-list="[imageUrl]"
  />
</template>
```

**问题：组件渲染慢**

```vue
<script setup>
// 使用 shallowRef 减少响应式开销
const largeData = shallowRef({})

// 使用 computed 缓存计算结果
const filteredList = computed(() => {
  return list.value.filter(item => item.status === 1)
})

// 使用 watchEffect 替代 watch
watchEffect(() => {
  // 自动追踪依赖
  console.log(count.value)
})
</script>
```

### 5. 接口调用问题

**问题：接口请求失败（404）**

检查以下配置：

1. 后端服务是否启动
2. `vite.config.js` 中的 `baseUrl` 是否正确
3. API 路径是否正确

```javascript
// vite.config.js
const baseUrl = 'http://localhost:8080'  // 确保端口号正确

// .env.development
VITE_APP_BASE_API = '/dev-api'  // 确保路径正确
```

**问题：跨域问题**

在 `vite.config.js` 中配置代理：

```javascript
server: {
  proxy: {
    '/dev-api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (p) => p.replace(/^\/dev-api/, '')
    }
  }
}
```

**问题：Token 过期**

系统会自动检测 Token 过期（401），并提示重新登录。手动退出登录：

```javascript
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
await userStore.logOut()
router.push('/login')
```

### 6. 表单验证问题

**问题：表单验证不生效**

```vue
<template>
  <el-form ref="formRef" :model="formData" :rules="formRules">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="formData.username" />
    </el-form-item>
  </el-form>
</template>

<script setup>
const formRef = ref(null)
const formData = ref({
  username: ''
})

const formRules = ref({
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ]
})

// 提交时验证
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 验证通过，提交表单
      console.log('提交数据:', formData.value)
    }
  })
}
</script>
```

**问题：自定义验证规则**

```javascript
const validateAge = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入年龄'))
  } else if (!Number.isInteger(value)) {
    callback(new Error('请输入数字'))
  } else if (value < 18 || value > 100) {
    callback(new Error('年龄必须在 18 到 100 之间'))
  } else {
    callback()
  }
}

const formRules = ref({
  age: [
    { validator: validateAge, trigger: 'blur' }
  ]
})
```

### 7. 开发环境问题

**问题：npm install 失败**

```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install

# 或使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

**问题：端口被占用**

修改 `vite.config.js`：

```javascript
server: {
  port: 8081,  // 修改为其他端口
  host: true,
  open: true
}
```

**问题：热更新不生效**

```javascript
// vite.config.js
server: {
  watch: {
    usePolling: true  // 使用轮询模式
  }
}
```

---

## 总结

本指南涵盖了博客后台管理系统开发的核心内容：

1. ✅ **快速上手**：从零开始启动项目
2. ✅ **项目结构**：理解项目各目录的作用
3. ✅ **功能开发**：掌握新增模块和页面的完整流程
4. ✅ **API 规范**：学会正确调用后端接口
5. ✅ **状态管理**：使用 Pinia 管理全局状态
6. ✅ **组件开发**：遵循最佳实践开发组件
7. ✅ **样式规范**：编写可维护的样式代码
8. ✅ **调试技巧**：高效定位和解决问题
9. ✅ **常见问题**：快速解决开发中的常见错误

## 进一步学习

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Element Plus 官方文档](https://element-plus.org/zh-CN/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Pinia 官方文档](https://pinia.vuejs.org/zh/)
- [若依官方文档](http://doc.ruoyi.vip/)

## 获取帮助

如果在开发过程中遇到问题：

1. 查阅本指南的"常见问题解决"章节
2. 查看项目中的其他文档（README.md、QUICKSTART.md 等）
3. 搜索相关技术栈的官方文档
4. 向团队成员寻求帮助

祝你开发愉快！🎉
