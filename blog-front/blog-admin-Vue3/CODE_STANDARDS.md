# 代码规范文档

## 项目概述
本项目是基于 Vue 3 + Element Plus + Vite 的博客后台管理系统，采用 Composition API 和最新的 Vue 3 特性。

## Vue3 组件规范

### 1. 组件结构
- **统一使用 `<script setup>` 语法**（当前项目使用率：100%）
- 使用 Composition API 而非 Options API
- 组件结构顺序：template → script → style

```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 1. 导入依赖
import { ref, reactive, onMounted } from 'vue'

// 2. Props 和 Emits 定义
const props = defineProps({
  modelValue: String
})
const emit = defineEmits(['update:modelValue'])

// 3. 响应式数据
const loading = ref(false)
const queryParams = reactive({
  current: 1,
  size: 10
})

// 4. 计算属性
const computedValue = computed(() => {})

// 5. 方法定义
const handleQuery = () => {}

// 6. 生命周期钩子
onMounted(() => {})

// 7. 暴露方法（如需要）
defineExpose({
  handleQuery
})
</script>

<style scoped lang="scss">
/* 样式内容 */
</style>
```

### 2. 响应式数据
- 基础类型使用 `ref()`
- 对象类型使用 `reactive()`
- 访问 ref 值时使用 `.value`（仅在 script 中，template 自动解包）

```javascript
const count = ref(0)
const user = reactive({
  name: '',
  age: 0
})
```

### 3. 生命周期钩子
使用组合式 API 的生命周期钩子：
- `onMounted()` - 组件挂载后
- `onUpdated()` - 组件更新后
- `onUnmounted()` - 组件卸载前
- `watch()` - 监听响应式数据变化
- `watchEffect()` - 自动追踪响应式依赖

## 命名规范

### 1. 组件命名
- **组件名**：PascalCase（大驼峰）
  - 单文件组件：`BlogEditor.vue`、`TagCloud.vue`
  - 使用时：`<BlogEditor />` 或 `<blog-editor />`

### 2. 文件命名
- **页面文件**：kebab-case（短横线）或 index.vue
  - `src/views/blog/article/list.vue`
  - `src/views/blog/category/index.vue`
- **组件文件**：PascalCase（项目中使用）或 kebab-case
  - `src/components/BlogEditor/index.vue`
  - `src/components/TagCloud/index.vue`

### 3. 变量和函数命名
- **变量名**：camelCase（小驼峰）
  ```javascript
  const articleList = ref([])
  const categoryList = ref([])
  const queryParams = reactive({})
  ```

- **函数名**：camelCase，使用动词开头
  ```javascript
  const getList = async () => {}
  const handleQuery = () => {}
  const handleAdd = () => {}
  const handleDelete = () => {}
  ```

- **布尔值变量**：使用 is/has 前缀
  ```javascript
  const isLoading = ref(false)
  const hasPermission = ref(true)
  ```

### 4. 常量命名
- **常量名**：UPPER_CASE（全大写，下划线分隔）
  ```javascript
  const MAX_SIZE = 100
  const API_BASE_URL = '/api'
  ```

## 代码组织

### 1. 导入顺序
```javascript
// 1. Vue 核心
import { ref, reactive, computed, watch, onMounted } from 'vue'

// 2. Vue Router / Pinia
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

// 3. 第三方库
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, Edit } from '@element-plus/icons-vue'

// 4. API 接口
import { listArticles, deleteArticle } from '@/api/blog/article'

// 5. 工具函数
import { formatDate } from '@/utils/blog'

// 6. 类型定义（如使用 TypeScript）
import type { Article } from '@/types/blog'
```

### 2. 代码块顺序
在 `<script setup>` 中按以下顺序组织代码：
1. 导入语句
2. Props 和 Emits 定义
3. 响应式状态
4. 计算属性
5. Watch 监听
6. 方法定义
7. 生命周期钩子
8. defineExpose（如需要）

### 3. 注释规范
- 复杂逻辑必须添加注释
- 函数注释说明功能、参数、返回值
- 重要业务逻辑添加行内注释

```javascript
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
```

## API 调用规范

### 1. API 文件组织
- 按模块划分 API 文件：`src/api/blog/article.js`
- 每个文件导出相关的 API 函数
- 使用统一的 request 工具

```javascript
import request from '@/utils/request'

export function listArticles(query) {
  return request({
    url: '/admin/articles',
    method: 'get',
    params: query
  })
}

export function deleteArticle(articleIds) {
  return request({
    url: '/admin/articles',
    method: 'delete',
    data: articleIds
  })
}
```

### 2. 统一错误处理
```javascript
const handleDelete = async (id) => {
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
    ElMessage.error('删除失败')
  }
}
```

### 3. Loading 状态管理
- 使用 `v-loading` 指令显示加载状态
- 在请求开始时设置 loading 为 true
- 使用 finally 确保 loading 重置

```javascript
const loading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const res = await listArticles(queryParams)
    // 处理数据
  } finally {
    loading.value = false
  }
}
```

### 4. 成功/失败提示
- 使用 `ElMessage` 统一提示
- 成功：`ElMessage.success('操作成功')`
- 失败：`ElMessage.error('操作失败')`
- 警告：`ElMessage.warning('警告信息')`

## 样式规范

### 1. 使用 SCSS
- 项目统一使用 SCSS 预处理器
- 使用 `<style scoped lang="scss">` 确保样式隔离

### 2. 样式组织
```scss
<style scoped lang="scss">
// 1. 容器样式
.article-management {
  padding: 20px;
}

// 2. 布局样式
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

// 3. 组件样式
.search-bar {
  display: flex;
  align-items: center;
  
  .el-input {
    width: 200px;
  }
}

// 4. 嵌套样式（避免过深）
.article-cover-wrapper {
  position: relative;
  
  .status-icon {
    position: absolute;
    right: 8px;
    bottom: 8px;
  }
}
</style>
```

### 3. 命名约定
- 使用语义化的类名
- 避免使用过于通用的类名
- 使用 BEM 命名法（可选但推荐）

### 4. 响应式设计
- 使用 Flexbox 和 Grid 布局
- 适配不同屏幕尺寸
- 使用 Element Plus 的响应式工具

## Git 提交规范

### 提交信息格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型
- **feat**: 新功能
- **fix**: 修复 bug
- **docs**: 文档更新
- **style**: 代码格式调整（不影响功能）
- **refactor**: 代码重构
- **perf**: 性能优化
- **test**: 测试相关
- **chore**: 构建/工具变动

### 示例
```
feat(article): 添加文章批量导出功能

- 实现批量选择文章
- 支持导出为 Markdown 格式
- 添加导出进度提示

Closes #123
```

## 最佳实践

### 1. Props 验证
```javascript
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})
```

### 2. 避免直接修改 Props
```javascript
// ❌ 错误
const updateValue = () => {
  props.value = 'new value'
}

// ✅ 正确
const emit = defineEmits(['update:modelValue'])
const updateValue = () => {
  emit('update:modelValue', 'new value')
}
```

### 3. 使用 computed 而非 watch
```javascript
// ✅ 推荐
const fullName = computed(() => {
  return `${user.firstName} ${user.lastName}`
})

// ❌ 不推荐
watch([() => user.firstName, () => user.lastName], () => {
  fullName.value = `${user.firstName} ${user.lastName}`
})
```

### 4. 组件拆分
- 单个组件不超过 500 行
- 提取可复用的逻辑到 composables
- 保持组件职责单一

### 5. 性能优化
- 使用 `v-show` 而非 `v-if` 频繁切换
- 大列表使用 `key` 属性
- 避免在模板中使用复杂表达式
- 使用 `shallowRef` 和 `shallowReactive` 优化大对象

## 代码审查清单

- [ ] 是否使用 `<script setup>` 语法
- [ ] 导入顺序是否正确
- [ ] 命名是否符合规范
- [ ] 是否有适当的错误处理
- [ ] 是否有 loading 状态
- [ ] 样式是否使用 scoped
- [ ] 是否有必要的注释
- [ ] Git 提交信息是否规范
- [ ] 代码是否符合 ESLint 规则
