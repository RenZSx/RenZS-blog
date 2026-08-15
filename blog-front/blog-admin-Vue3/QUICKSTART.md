# 博客后台管理系统 Vue3 版本 - 快速开始

## 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 pnpm >= 7.0.0
- 现代浏览器（Chrome、Firefox、Edge、Safari 等）

## 安装步骤

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/blog-satoken.git
cd blog-satoken/blog-front/blog-admin-Vue3
```

### 2. 安装依赖

```bash
# 使用 npm
npm install

# 或使用 yarn
yarn install

# 或使用 pnpm（推荐）
pnpm install
```

### 3. 配置环境变量

项目包含三个环境配置文件：

- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置
- `.env.staging` - 预发布环境配置

开发环境默认配置：
```env
# 页面标题
VITE_APP_TITLE = 博客后台管理系统

# 开发环境配置
VITE_APP_ENV = 'development'

# API 基础路径
VITE_APP_BASE_API = '/dev-api'
```

**重要**：修改 `vite.config.js` 中的后端接口地址：

```javascript
const baseUrl = 'http://localhost:8080' // 修改为你的后端接口地址
```

### 4. 启动开发服务器

```bash
npm run dev
```

服务将在 `http://localhost:80` 启动并自动打开浏览器。

### 5. 构建生产版本

```bash
# 生产环境构建
npm run build:prod

# 预发布环境构建
npm run build:stage
```

构建完成后，产物将输出到 `dist` 目录。

### 6. 预览构建结果

```bash
npm run preview
```

## 项目结构说明

```
blog-admin-Vue3/
├── public/              # 静态资源目录
├── src/
│   ├── api/            # API 接口定义
│   │   ├── blog/      # 博客相关接口
│   │   ├── monitor/   # 监控相关接口
│   │   ├── system/    # 系统相关接口
│   │   └── tool/      # 工具相关接口
│   ├── assets/         # 资源文件
│   │   ├── icons/     # 图标
│   │   ├── images/    # 图片
│   │   ├── js/        # JavaScript 工具
│   │   └── styles/    # 样式文件
│   ├── components/     # 全局组件
│   │   ├── BlogEditor/     # 博客编辑器
│   │   ├── Editor/         # 富文本编辑器
│   │   ├── ImageUpload/    # 图片上传
│   │   ├── FileUpload/     # 文件上传
│   │   └── ...
│   ├── layout/         # 布局组件
│   ├── plugins/        # 插件
│   ├── router/         # 路由配置
│   ├── store/          # Pinia 状态管理
│   ├── utils/          # 工具函数
│   ├── views/          # 页面组件
│   │   ├── blog/      # 博客功能页面
│   │   │   ├── about/      # 关于管理
│   │   │   ├── album/      # 相册管理
│   │   │   ├── article/    # 文章管理
│   │   │   ├── category/   # 分类管理
│   │   │   ├── comment/    # 评论管理
│   │   │   ├── friendlink/ # 友链管理
│   │   │   ├── home/       # 首页数据
│   │   │   ├── log/        # 操作日志
│   │   │   ├── menu/       # 菜单管理
│   │   │   ├── message/    # 留言管理
│   │   │   ├── page/       # 页面管理
│   │   │   ├── resource/   # 资源管理
│   │   │   ├── role/       # 角色管理
│   │   │   ├── tag/        # 标签管理
│   │   │   ├── talk/       # 说说管理
│   │   │   ├── user/       # 用户管理
│   │   │   └── website/    # 网站配置
│   │   ├── monitor/   # 监控功能页面
│   │   ├── system/    # 系统功能页面
│   │   └── ...
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── .env.development    # 开发环境变量
├── .env.production     # 生产环境变量
├── .env.staging        # 预发布环境变量
├── vite.config.js      # Vite 配置
└── package.json        # 项目配置
```

## 开发指南

### 代码规范

- 使用 ES6+ 语法
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case
- 常量命名采用 UPPER_CASE
- 变量和函数命名采用 camelCase

### 新增页面

1. 在 `src/views/` 对应模块目录下创建 `.vue` 文件
2. 在 `src/router/` 中配置路由
3. 如需权限控制，在后端配置相应菜单和权限

### API 调用方式

```javascript
// 1. 在 src/api/blog/ 下定义接口
import request from '@/utils/request'

export function getArticleList(query) {
  return request({
    url: '/blog/article/list',
    method: 'get',
    params: query
  })
}

// 2. 在组件中使用
import { getArticleList } from '@/api/blog/article'

const fetchData = async () => {
  const response = await getArticleList({ pageNum: 1, pageSize: 10 })
  console.log(response.data)
}
```

### 状态管理使用

项目使用 Pinia 进行状态管理：

```javascript
// 定义 store
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: { opened: true }
  }),
  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
    }
  }
})

// 在组件中使用
import { useAppStore } from '@/store/modules/app'

const appStore = useAppStore()
appStore.toggleSidebar()
```

### 组件开发

```vue
<template>
  <div class="my-component">
    <el-button @click="handleClick">{{ title }}</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const title = ref('按钮')

const handleClick = () => {
  console.log('clicked')
}
</script>

<style scoped>
.my-component {
  padding: 20px;
}
</style>
```

## 常见问题

### Q: 启动失败，提示端口被占用

**A**: 修改 `vite.config.js` 中的端口配置：

```javascript
server: {
  port: 3000, // 修改为其他端口
  // ...
}
```

### Q: 接口调用失败，出现跨域错误

**A**: 检查以下几点：

1. 确认后端服务已启动
2. 检查 `vite.config.js` 中的 `baseUrl` 是否正确
3. 确认代理配置是否正确

### Q: 路由跳转后页面空白

**A**: 可能的原因：

1. 路由配置错误，检查 `src/router/index.js`
2. 组件路径错误，确认组件是否存在
3. 权限问题，确认用户是否有访问权限

### Q: 组件样式不生效

**A**: 检查：

1. 是否正确导入了样式文件
2. 是否使用了 `scoped` 属性
3. 样式选择器是否正确

### Q: 打包后静态资源路径错误

**A**: 检查 `vite.config.js` 中的 `base` 配置是否与部署路径匹配。

### Q: 开发时修改代码不热更新

**A**: 尝试以下方法：

1. 重启开发服务器
2. 清除浏览器缓存
3. 检查是否有语法错误

## 技术栈

- **前端框架**: Vue 3.5.26
- **构建工具**: Vite 6.4.1
- **UI 组件库**: Element Plus 2.13.1
- **状态管理**: Pinia 3.0.4
- **路由管理**: Vue Router 4.6.4
- **HTTP 客户端**: Axios 1.13.2
- **富文本编辑器**: Vue Quill 1.2.0
- **图表库**: ECharts 5.6.0

## 相关文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Element Plus 官方文档](https://element-plus.org/zh-CN/)
- [Pinia 官方文档](https://pinia.vuejs.org/zh/)
- [测试清单](./TEST_CHECKLIST.md)
- [部署指南](./DEPLOYMENT.md)

## 许可证

MIT License
