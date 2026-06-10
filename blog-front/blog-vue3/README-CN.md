# Renzs Blog Vue3 项目文档

## 项目简介

Renzs Blog 前端，基于 Vue 3（Composition API + `<script setup>`）、TypeScript 构建，UI 层使用 Vuetify + Element Plus 混用方案。前端通过 Axios 与后端 Spring Boot 通信（代理到 `127.0.0.1:8088`），聊天室功能通过 WebSocket 实现。

---

## 常用命令

```bash
npm run dev      # 启动 Vite 开发服务器，端口 3000
npm run build    # TypeScript 类型检查 + 生产构建
npm run preview  # 预览生产构建产物
npm run lint     # ESLint 检查并自动修复
npm run test     # Vitest 单元测试
```

---

## 项目结构

```
src/
├── api/             # Axios 请求封装，每个业务域一个文件
├── assets/css/      # 全局样式
│   ├── tokens.css   # CSS 变量（主题色、间距、字体等）
│   ├── index.css    # 重置样式、工具类、动画
│   └── markdown.css # 文章内容 Markdown 渲染样式
├── components/      # 公共组件
│   ├── layout/      # TopNavBar（顶部导航）、SideNavBar（侧边栏）、Footer
│   ├── model/       # 弹窗组件：SearchModel、LoginModel、RegisterModel 等
│   └── comment/     # 评论系统：Editor、Item、List、Reply
├── composables/     # 组合式函数，useToast.ts 为独立 Vue 应用的 Toast 通知
├── plugins/         # Vuetify 插件配置，定义主题和 MDI 图标
├── router/          # Vue Router 配置，20 个路由，全部懒加载
├── stores/          # Pinia 状态管理
├── types/           # TypeScript 类型声明
├── utils/           # 工具函数
│   ├── filters.ts   # dayjs 日期时间格式化
│   └── markdown.ts  # markdown-it 配置，文章内容渲染引擎
└── views/           # 页面组件（Home、Article、Talk、Album、Chat 等）
```

---

## API 层

`src/api/request.ts` 是 Axios 实例，所有业务 API 文件都基于它封装。主要功能：
- 自动附加 Bearer Token
- 统一错误处理
- 通过 Vite 代理将 `/api` 前缀转发到后端

业务 API 模块：
| 文件 | 说明 |
|---|---|
| `site.ts` | 站点信息、访客上报、关于页 |
| `user.ts` | 登录/注册/OAuth/用户信息 |
| `article.ts` | 文章列表、详情、点赞、搜索、归档 |
| `comment.ts` | 评论列表、提交、回复、点赞 |
| `talk.ts` | 说说列表、详情、点赞 |
| `misc.ts` | 分类、标签、友链、留言、首页说说 |

---

## 状态管理

三个 Pinia Store：

| Store | 文件 | 说明 | 持久化 |
|---|---|---|---|
| useUserStore | `stores/user.ts` | 用户信息、认证状态、点赞记录 | ✅ localStorage |
| useUIStore | `stores/ui.ts` | 弹窗开关（搜索、登录、注册等） | ❌ |
| useBlogInfoStore | `stores/blogInfo.ts` | 站点配置、页面列表、计数 | ✅ localStorage |

---

## 样式系统

- **CSS 变量**：`assets/css/tokens.css` 定义了 `--primary-color`、`--text-primary`、`--bg-primary`、`--spacing-*` 等变量，切换主题色时只需修改变量值
- **暗色模式**：`.dark` 选择器覆盖变量实现暗色主题，配合 Vuetify 主题系统
- **无 Tailwind**：纯 CSS + CSS 变量 + 少量工具类（`.text-ellipsis`、`.flex`、`.mt-*` 等）

---

## Markdown 渲染

`utils/markdown.ts` 导出一个配置好的 `markdown-it` 实例，支持：
- 语法高亮（highlight.js）
- 代码行号 + 复制按钮
- 扩展语法：上标/下标/高亮/删除线/缩写/emoji/脚注/KaTeX/任务列表

用于文章详情页渲染文章内容，同时配合 `tocbot` 生成文章目录。

---

## 特殊机制

### Toast 通知
`composables/useToast.ts` 创建了一个挂载在 `document.body` 上的独立 Vue 应用，而不是渲染在主应用树中。这样 Toast 可以在任何组件中被调用，不受组件层级限制。

### Auth 路由布局
带有 `meta.layout: 'auth'` 的路由（如 `/login`、`/register`）会在 `App.vue` 中隐藏顶部导航、侧边栏和底部组件，呈现独立的认证页面。

### 自动导入
通过 `unplugin-auto-import` 和 `unplugin-vue-components` 实现：
- Vue API（ref、computed 等）、Pinia、Router API 无需手动 import
- `.vue` 组件无需手动 import
- 如需排除，可在 `src/auto-imports.d.ts` 或 `src/components.d.ts` 中配置

### 构建代码分割
Vite 配置了手动 chunk 分割，将以下库单独打包：vuetify、markdown-it 核心及插件、highlight.js、KaTeX、tocbot、swiper。

---

## 环境变量

`.env` 文件：

```
VITE_APP_TITLE=Renzs Blog
VITE_API_BASE_URL=   # 空字符串，通过 Vite 代理转发 /api 请求
```

---

## 路由配置

`src/router/index.ts` 使用 `createWebHistory`，特点：
- 页面标题自动拼接 `{meta.title} - Renzs Blog`
- 切换路由时显示 NProgress 进度条
- 滚动行为：跳转到新页面时滚动到顶部，但保持页面位置记忆（前进/后退时恢复）

---

## Vite 配置

`vite.config.ts` 关键配置：
- 开发服务器端口 3000
- `/api` 请求代理到 `http://127.0.0.1:8088`，并去除 `/api` 前缀
- SCSS 使用现代编译器 API
- 开启 Vuetify 自动导入插件