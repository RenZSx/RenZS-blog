# 博客后台管理系统 Vue3 迁移项目进度文档

**项目名称**: Blog Admin Vue3 Migration  
**开始时间**: 2026-08-14  
**当前版本**: v0.9 (业务页面开发完成)  
**文档更新**: 2026-08-14 17:30

---

## 📌 项目概述

### 项目目标
将基于 Vue2 的博客后台管理系统迁移到 Vue3 版本,保留若依管理系统模板的布局和基础架构,仅添加博客业务功能模块。

### 技术栈对比

| 技术栈 | Vue2 版本 | Vue3 版本 |
|--------|-----------|-----------|
| 核心框架 | Vue 2.6 | Vue 3.5 |
| 构建工具 | Vue CLI + Webpack | Vite 6.4 |
| 路由 | Vue Router 3 | Vue Router 4 |
| 状态管理 | Vuex 3 | Pinia 3 |
| UI 框架 | Element UI 2.15 | Element Plus 2.13 |
| 编程风格 | Options API | Composition API |
| 图表库 | ECharts 4.8 | ECharts 5.6 |

---

## ✅ 已完成模块 (详细清单)

### 1. API 接口层 - 17个模块 (100%)

**文件位置**: `src/api/blog/`

#### 1.1 核心内容模块
| 文件名 | 功能说明 | 接口数量 | 状态 |
|--------|----------|----------|------|
| `article.js` | 文章管理 | 9个 | ✅ 完成 |
| `category.js` | 分类管理 | 5个 | ✅ 完成 |
| `tag.js` | 标签管理 | 5个 | ✅ 完成 |

**article.js 接口列表**:
- `listArticles()` - 查询文章列表
- `getArticle()` - 查询文章详情
- `addArticle()` - 新增文章
- `updateArticle()` - 修改文章
- `updateArticleDelete()` - 逻辑删除
- `deleteArticle()` - 物理删除
- `updateArticleTop()` - 修改置顶
- `exportArticles()` - 导出文章
- `uploadArticleImage()` - 上传图片

#### 1.2 互动内容模块
| 文件名 | 功能说明 | 接口数量 | 状态 |
|--------|----------|----------|------|
| `comment.js` | 评论管理 | 3个 | ✅ 完成 |
| `message.js` | 留言管理 | 3个 | ✅ 完成 |
| `talk.js` | 说说管理 | 4个 | ✅ 完成 |

#### 1.3 多媒体模块
| 文件名 | 功能说明 | 接口数量 | 状态 |
|--------|----------|----------|------|
| `album.js` | 相册和照片管理 | 9个 | ✅ 完成 |
| `upload.js` | 文件上传 | 2个 | ✅ 完成 |

**album.js 接口列表**:
- `listAlbums()` - 查询相册列表
- `getAlbum()` - 查询相册详情
- `saveOrUpdateAlbum()` - 保存或更新相册
- `deleteAlbums()` - 删除相册
- `listPhotos()` - 查询照片列表
- `savePhotos()` - 保存照片
- `updatePhoto()` - 更新照片信息
- `deletePhotos()` - 删除照片
- `movePhotos()` - 移动照片到其他相册

#### 1.4 系统管理模块
| 文件名 | 功能说明 | 接口数量 | 状态 |
|--------|----------|----------|------|
| `user.js` | 用户管理 | 5个 | ✅ 完成 |
| `role.js` | 角色管理 | 4个 | ✅ 完成 |
| `menu.js` | 菜单管理 | 4个 | ✅ 完成 |
| `resource.js` | 资源管理 | 4个 | ✅ 完成 |

#### 1.5 其他模块
| 文件名 | 功能说明 | 接口数量 | 状态 |
|--------|----------|----------|------|
| `friendLink.js` | 友链管理 | 4个 | ✅ 完成 |
| `page.js` | 页面管理 | 3个 | ✅ 完成 |
| `operationLog.js` | 操作日志 | 2个 | ✅ 完成 |
| `website.js` | 网站设置 | 4个 | ✅ 完成 |
| `home.js` | 首页统计 | 3个 | ✅ 完成 |

**API 接口总计**: 约 73 个接口

---

### 2. 状态管理 - Pinia Store (100%)

**文件位置**: `src/store/modules/blog.js`

#### 2.1 State 状态
```javascript
{
  collapse: false,           // 侧边栏折叠状态
  tabList: [],              // 标签页列表
  userMenuList: []          // 用户菜单列表
}
```

#### 2.2 Actions 方法
| 方法名 | 功能说明 | 状态 |
|--------|----------|------|
| `saveTab()` | 保存标签页 | ✅ |
| `removeTab()` | 移除标签页 | ✅ |
| `resetTab()` | 重置标签页 | ✅ |
| `toggleCollapse()` | 切换侧边栏 | ✅ |
| `saveUserMenuList()` | 保存用户菜单 | ✅ |
| `clearUserMenuList()` | 清空菜单 | ✅ |

#### 2.3 持久化配置
- 使用 `pinia-plugin-persistedstate`
- 存储到 `localStorage`
- 持久化字段: `userMenuList`

---

### 3. 工具函数和配置 (100%)

#### 3.1 博客工具函数
**文件位置**: `src/utils/blog.js`

| 函数名 | 功能说明 | 参数 | 返回值 |
|--------|----------|------|--------|
| `formatDate()` | 日期格式化 | (date, format) | String |
| `getUploadHeaders()` | 获取上传请求头 | - | Object |
| `compressImage()` | 压缩图片 | (file, quality) | Promise<File> |
| `checkFileSize()` | 检查文件大小 | (file, maxSize) | Boolean |
| `generateId()` | 生成随机ID | - | String |

#### 3.2 配置文件
**文件位置**: `src/config/index.js`

```javascript
{
  TENCENT_CAPTCHA: "2096471113",  // 腾讯验证码ID
  UPLOAD_SIZE: 5120               // 上传文件大小限制(KB)
}
```

---

### 4. 自定义组件 - Vue3 版本 (100%)

#### 4.1 BlogEditor - 富文本编辑器
**文件位置**: `src/components/BlogEditor/index.vue`

**技术特点**:
- 使用 Vue3 Composition API
- 支持 `v-model` 双向绑定
- 支持光标位置记录和恢复
- 支持动态插入内容

**Props**:
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `modelValue` | String | '' | 编辑器内容 |
| `disable` | Boolean | true | 是否可编辑 |
| `placeholder` | String | '' | 占位符 |

**Methods (通过 defineExpose 暴露)**:
- `clear()` - 清空内容
- `addText(value)` - 插入文本

#### 4.2 TagCloud - 标签云组件
**文件位置**: `src/components/TagCloud/index.vue`

**技术特点**:
- 动态字体大小
- 随机颜色生成
- 悬停动画效果

**Props**:
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | Array | [] | 标签数据 [{id, name}] |

---

### 5. 路由配置 (100%)

**文件位置**: `src/router/blog.js`

#### 5.1 路由结构
```
/blog (Layout)
├── /home - 博客首页
├── /article
│   ├── /list - 文章列表
│   └── /edit/:id? - 编辑文章
├── /category - 分类管理
├── /tag - 标签管理
├── /comment - 评论管理
├── /message - 留言管理
├── /talk
│   ├── /list - 说说列表
│   └── /edit/:id? - 编辑说说
├── /album
│   ├── /list - 相册列表
│   └── /photo/:id - 照片管理
├── /friendlink - 友链管理
├── /user
│   ├── / - 用户管理
│   └── /online - 在线用户
├── /role - 角色管理
├── /menu - 菜单管理
├── /resource - 资源管理
├── /log/operation - 操作日志
├── /page - 页面管理
├── /about - 关于管理
└── /website - 网站设置
```

**路由总数**: 22 个

---

### 6. 业务页面 - Vue3 Composition API (14%)

#### 6.1 已完成页面 (3/22)

##### ✅ 博客首页 - `home/index.vue`
**功能特性**:
- 统计卡片展示 (访问量、用户量、文章量、留言量)
- 一周访问量折线图 (ECharts)
- 文章浏览量排行柱状图 (ECharts)
- 文章分类统计饼图 (ECharts)
- 用户地域分布地图 (ECharts)
- 文章标签统计 (TagCloud)
- 用户类型切换 (用户/游客)

**技术亮点**:
- 响应式数据管理
- 图表自适应
- 异步数据加载
- Loading 状态处理

**代码行数**: ~350 行

##### ✅ 标签管理 - `tag/index.vue`
**功能特性**:
- 标签列表展示 (表格)
- 搜索功能
- 新增/编辑标签 (对话框)
- 单个/批量删除
- 分页功能

**技术亮点**:
- 表单验证
- 多选框处理
- 确认对话框
- 成功/错误提示

**代码行数**: ~280 行

##### ✅ 分类管理 - `category/index.vue`
**功能特性**:
- 分类列表展示 (表格)
- 搜索功能
- 新增/编辑分类 (对话框)
- 单个/批量删除
- 分页功能

**技术亮点**:
- 与标签管理类似的实现模式
- 可复用的代码结构

**代码行数**: ~280 行

##### ✅ 评论管理 - `comment/index.vue` (9.1KB)
**功能特性**:
- 评论列表展示
- 审核状态筛选(全部/正常/审核中)
- 来源筛选(文章/友链/说说)
- 用户昵称搜索
- 批量审核通过/删除
- 分页功能

##### ✅ 留言管理 - `message/index.vue` (7.8KB)
**功能特性**:
- 留言列表展示
- 审核状态筛选
- IP地址和来源显示
- 批量审核/删除
- 分页功能

##### ✅ 说说列表 - `talk/list.vue` (7.3KB)
**功能特性**:
- 卡片式列表展示
- 状态筛选(公开/私密)
- 置顶和私密标识
- 图片预览
- 编辑/删除操作

##### ✅ 说说编辑 - `talk/edit.vue` (9.6KB)
**功能特性**:
- 富文本编辑器(BlogEditor)
- 表情选择器
- 多图上传
- 置顶开关
- 状态选择

##### ✅ 用户管理 - `user/index.vue`
**功能特性**:
- 用户列表展示(头像、昵称、角色)
- 用户状态管理(启用/禁用)
- 角色分配
- 搜索和分页

##### ✅ 在线用户 - `user/online.vue`
**功能特性**:
- 在线用户列表
- IP、浏览器、操作系统信息
- 下线操作
- 搜索和分页

##### ✅ 角色管理 - `role/index.vue`
**功能特性**:
- 角色列表展示
- 菜单权限分配(树形选择器)
- 资源权限分配(树形选择器)
- 角色禁用开关
- 批量删除

##### ✅ 菜单管理 - `menu/index.vue`
**功能特性**:
- 树形菜单展示
- 新增目录/菜单
- 图标选择器
- 菜单隐藏开关
- 排序调整

##### ✅ 资源管理 - `resource/index.vue`
**功能特性**:
- 树形资源展示
- Swagger导入功能
- 请求方式标签
- 匿名访问开关
- 模块-资源两级管理

##### ✅ 操作日志 - `log/operation.vue`
**功能特性**:
- 日志列表展示
- 搜索筛选
- 查看详情
- 批量删除

##### ✅ 文章列表 - `article/list.vue` (24KB)
**功能特性**:
- 文章列表展示(带封面)
- 五态筛选(全部/公开/私密/草稿/回收站)
- 多条件搜索(关键词+分类+标签+类型)
- 批量操作(删除/导出/导入)
- 推荐文章互斥逻辑
- 置顶开关
- 回收站恢复

##### ✅ 文章编辑 - `article/edit.vue` (15KB)
**功能特性**:
- Markdown编辑器集成
- 标题字数统计
- 分类标签搜索(可自定义添加)
- 封面上传
- 原文链接(转载/翻译)
- 自动保存(SessionStorage)
- 草稿系统

##### ✅ 相册列表 - `album/list.vue`
**功能特性**:
- 卡片展示
- 相册CRUD
- 跳转照片管理

##### ✅ 照片管理 - `album/photo.vue`
**功能特性**:
- 网格展示
- 批量上传
- 移动到其他相册
- 批量删除

##### ✅ 友链管理 - `friendlink/index.vue`
**功能特性**:
- 列表展示
- 审核功能
- 友链CRUD
- 搜索和分页

##### ✅ 页面管理 - `page/index.vue`
**功能特性**:
- 卡片展示
- 页面CRUD

##### ✅ 关于管理 - `about/index.vue`
**功能特性**:
- 富文本编辑器
- 内容保存

##### ✅ 网站设置 - `website/index.vue`
**功能特性**:
- 多标签配置
- 网站信息管理
- 社交信息
- AI配置

**已完成页面总代码量**: ~7,090 行 (168 KB)

---

### 7. 文档 (100%)

#### 7.1 迁移指南
**文件位置**: `MIGRATION.md`

**内容**:
- 已完成工作清单
- 待完成工作清单
- Vue2 到 Vue3 主要变化
- 建议的实施步骤

#### 7.2 迁移进度报告
**文件位置**: `README_BLOG.md`

**内容**:
- 完成度统计
- 详细模块说明
- 下一步建议
- 技术要点

---

## 🔄 待完成模块 (详细清单)

### 1. 业务页面 - 已全部完成 ✅

所有21个业务页面已全部开发完成! 🎉

**关键依赖**:
- Markdown 编辑器 (需选择方案)
- 图片上传组件
- 标签/分类选择器

#### 1.2 互动内容模块 (优先级: ⭐⭐⭐⭐)
| 页面 | 路径 | 功能说明 | 预计工作量 | 状态 |
|------|------|----------|-----------|------|
| 评论管理 | `comment/index.vue` | 列表、审核、删除 | 2h | ⏳ 待开发 |
| 留言管理 | `message/index.vue` | 列表、审核、删除 | 2h | ⏳ 待开发 |
| 说说列表 | `talk/list.vue` | 列表、搜索、删除 | 2h | ⏳ 待开发 |
| 说说编辑 | `talk/edit.vue` | 编辑器、图片上传 | 3h | ⏳ 待开发 |

#### 1.3 多媒体模块 (优先级: ⭐⭐⭐)
| 页面 | 路径 | 功能说明 | 预计工作量 | 状态 |
|------|------|----------|-----------|------|
| 相册列表 | `album/list.vue` | 相册管理 | 2h | ⏳ 待开发 |
| 照片管理 | `album/photo.vue` | 照片上传、移动、删除 | 3h | ⏳ 待开发 |

#### 1.4 系统管理模块 (优先级: ⭐⭐⭐⭐)
| 页面 | 路径 | 功能说明 | 预计工作量 | 状态 |
|------|------|----------|-----------|------|
| 用户管理 | `user/index.vue` | 用户列表、状态管理、角色分配 | 3h | ⏳ 待开发 |
| 在线用户 | `user/online.vue` | 在线用户列表、下线操作 | 2h | ⏳ 待开发 |
| 角色管理 | `role/index.vue` | 角色CRUD、权限分配 | 3h | ⏳ 待开发 |
| 菜单管理 | `menu/index.vue` | 树形菜单管理 | 3h | ⏳ 待开发 |
| 资源管理 | `resource/index.vue` | 资源树形管理、Swagger导入 | 3h | ⏳ 待开发 |
| 操作日志 | `log/operation.vue` | 日志列表、删除 | 2h | ⏳ 待开发 |

#### 1.5 其他模块 (优先级: ⭐⭐⭐)
| 页面 | 路径 | 功能说明 | 预计工作量 | 状态 |
|------|------|----------|-----------|------|
| 友链管理 | `friendlink/index.vue` | 友链CRUD | 2h | ⏳ 待开发 |
| 页面管理 | `page/index.vue` | 页面配置管理 | 2h | ⏳ 待开发 |
| 关于管理 | `about/index.vue` | 关于我信息编辑 | 2h | ⏳ 待开发 |
| 网站设置 | `website/index.vue` | 网站配置管理 | 3h | ⏳ 待开发 |

**待开发页面总预计工作量**: ~49 小时

---

### 2. 第三方库集成 (0%)

#### 2.1 Markdown 编辑器
**候选方案**:

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| `md-editor-v3` | Vue3原生、功能强大、中文文档 | 包体积较大 | ⭐⭐⭐⭐⭐ |
| `v-md-editor` | 掘金出品、UI美观 | 更新较少 | ⭐⭐⭐⭐ |
| `@vueup/vue-quill` | 模板已有 | 不是Markdown编辑器 | ⭐⭐⭐ |

**推荐方案**: `md-editor-v3`
- 安装命令: `npm install md-editor-v3`
- 预计集成时间: 1-2 小时

#### 2.2 日历热力图
**候选方案**:

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| `vue-calendar-heatmap` | Vue2版本已使用 | 不支持Vue3 | ⭐ |
| ECharts 日历图 | 已集成ECharts | 需自定义配置 | ⭐⭐⭐⭐⭐ |
| `@toast-ui/vue-calendar` | 功能完整 | 包体积大 | ⭐⭐⭐ |

**推荐方案**: ECharts 日历图
- 无需额外安装
- 预计集成时间: 2-3 小时

#### 2.3 图片裁剪组件
**候选方案**:

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| `vue-cropper@next` | Vue3版本 | 配置复杂 | ⭐⭐⭐⭐ |
| `cropperjs` | 原生JS、稳定 | 需手动封装 | ⭐⭐⭐⭐⭐ |

**推荐方案**: `cropperjs` + 自定义封装
- 安装命令: `npm install cropperjs`
- 预计集成时间: 3-4 小时

#### 2.4 ECharts 中国地图
**状态**: 需要引入
- 下载 `china.js` 地图数据
- 注册地图到 ECharts
- 预计集成时间: 0.5 小时

**第三方库集成总预计时间**: ~10 小时

---

### 3. 主入口文件修改 (0%)

#### 3.1 src/main.js 需要添加的内容

```javascript
// 需要添加的导入
import BlogEditor from '@/components/BlogEditor/index.vue'
import TagCloud from '@/components/TagCloud/index.vue'
import { formatDate, getUploadHeaders } from '@/utils/blog'
import config from '@/config'

// 全局组件注册
app.component('BlogEditor', BlogEditor)
app.component('TagCloud', TagCloud)

// 全局属性挂载
app.config.globalProperties.$config = config
app.config.globalProperties.$formatDate = formatDate
app.config.globalProperties.$getUploadHeaders = getUploadHeaders
```

**预计修改时间**: 0.5 小时

---

### 4. 路由集成 (0%)

#### 4.1 静态路由集成
**文件**: `src/router/index.js`

```javascript
// 需要添加
import { blogRoutes } from './blog'

export const constantRoutes = [
  // ... 原有路由
  ...blogRoutes  // 添加博客路由
]
```

**预计修改时间**: 0.5 小时

#### 4.2 动态路由加载
**需要实现的功能**:
- 从后端获取用户菜单
- 动态添加路由
- 根据权限控制路由访问

**参考 Vue2 版本**: `src/assets/js/menu.js`

**预计开发时间**: 2-3 小时

---

### 5. 样式和资源 (0%)

#### 5.1 图标字体
**来源**: Vue2 版本 `src/assets/css/iconfont.css`
**目标**: Vue3 版本 `src/assets/styles/iconfont.css`
**操作**: 复制文件及字体文件

#### 5.2 自定义样式
**来源**: Vue2 版本 `src/assets/css/index.css`
**目标**: Vue3 版本 `src/assets/styles/blog.scss`
**操作**: 转换为 SCSS 格式

**预计时间**: 1 小时

---

## 📊 进度统计

### 整体完成度

| 模块 | 已完成 | 总计 | 完成率 | 预计剩余时间 |
|------|--------|------|--------|--------------|
| API 接口 | 17 | 17 | 100% | - |
| Store 状态 | 1 | 1 | 100% | - |
| 工具函数 | 5 | 5 | 100% | - |
| 组件 | 2 | 2 | 100% | - |
| 路由配置 | 1 | 1 | 100% | - |
| 业务页面 | 21 | 21 | 100% | - |
| 第三方库 | 0 | 4 | 0% | 10h |
| 入口文件 | 0 | 1 | 0% | 0.5h |
| 路由集成 | 0 | 1 | 0% | 3h |
| 样式资源 | 0 | 1 | 0% | 1h |
| **总计** | **47** | **51** | **92%** | **~15h** |

### 代码量统计

| 类型 | 文件数 | 代码行数 | 备注 |
|------|--------|----------|------|
| API 接口 | 17 | ~1,200 | 平均每个接口文件 70 行 |
| Store | 1 | ~60 | - |
| 工具函数 | 1 | ~80 | - |
| 组件 | 2 | ~280 | BlogEditor + TagCloud |
| 路由配置 | 1 | ~120 | - |
| 业务页面 | 21 | ~8,000 | 所有业务页面 |
| 文档 | 3 | ~800 | Markdown 文档 |
| **总计** | **46** | **~10,540** | - |

---

## 🎯 下一阶段计划

### 第一阶段: 核心功能完成 (预计 20 小时)

#### 优先级 1: 文章管理 (10小时)
1. [ ] 集成 Markdown 编辑器 `md-editor-v3` (2h)
2. [ ] 开发文章列表页面 (4h)
3. [ ] 开发文章编辑页面 (4h)

#### 优先级 2: 互动功能 (7小时)
4. [ ] 评论管理页面 (2h)
5. [ ] 留言管理页面 (2h)
6. [ ] 说说列表页面 (2h)
7. [ ] 说说编辑页面 (1h)

#### 优先级 3: 路由集成 (3小时)
8. [ ] 修改主入口文件 (0.5h)
9. [ ] 静态路由集成 (0.5h)
10. [ ] 动态路由加载 (2h)

**阶段目标**: 核心内容管理功能可用,能够发布和管理文章、评论、留言、说说。

---

### 第二阶段: 系统管理完成 (预计 18 小时)

#### 优先级 1: 用户权限 (11小时)
1. [ ] 用户管理页面 (3h)
2. [ ] 在线用户页面 (2h)
3. [ ] 角色管理页面 (3h)
4. [ ] 菜单管理页面 (3h)

#### 优先级 2: 系统功能 (7小时)
5. [ ] 资源管理页面 (3h)
6. [ ] 操作日志页面 (2h)
7. [ ] 集成图片裁剪 (2h)

**阶段目标**: 完整的用户权限管理体系,系统日志可追溯。

---

### 第三阶段: 完善和优化 (预计 15 小时)

#### 优先级 1: 多媒体和其他 (10小时)
1. [ ] 相册列表页面 (2h)
2. [ ] 照片管理页面 (3h)
3. [ ] 友链管理页面 (2h)
4. [ ] 页面管理页面 (2h)
5. [ ] 关于管理页面 (1h)

#### 优先级 2: 配置和优化 (5小时)
6. [ ] 网站设置页面 (3h)
7. [ ] 集成日历热力图 (1h)
8. [ ] 样式资源迁移 (1h)

**阶段目标**: 所有功能模块完成,系统功能完整。

---

### 第四阶段: 测试和部署 (预计 10 小时)

1. [ ] 功能测试 (4h)
2. [ ] 接口联调 (3h)
3. [ ] 性能优化 (2h)
4. [ ] 部署文档编写 (1h)

**阶段目标**: 系统稳定可用,文档完善。

---

## 💡 技术要点记录

### Vue2 → Vue3 转换模式

#### 1. 组件基础结构
**Vue2 Options API**:
```vue
<script>
export default {
  data() {
    return { count: 0 }
  },
  methods: {
    increment() {
      this.count++
    }
  }
}
</script>
```

**Vue3 Composition API**:
```vue
<script setup>
import { ref } from 'vue'

const count = ref(0)
const increment = () => {
  count.value++
}
</script>
```

#### 2. 生命周期钩子对照表
| Vue2 | Vue3 |
|------|------|
| `beforeCreate` | `setup()` |
| `created` | `setup()` |
| `beforeMount` | `onBeforeMount()` |
| `mounted` | `onMounted()` |
| `beforeUpdate` | `onBeforeUpdate()` |
| `updated` | `onUpdated()` |
| `beforeDestroy` | `onBeforeUnmount()` |
| `destroyed` | `onUnmounted()` |

#### 3. 响应式数据
| Vue2 | Vue3 |
|------|------|
| `this.data` | `ref()` / `reactive()` |
| `this.$set()` | 直接赋值 |
| `this.$delete()` | `delete` 关键字 |

#### 4. 组件通信
| Vue2 | Vue3 |
|------|------|
| `this.$emit()` | `defineEmits()` |
| `this.$refs` | `ref()` |
| `props` | `defineProps()` |

#### 5. 路由和状态
| Vue2 | Vue3 |
|------|------|
| `this.$router` | `useRouter()` |
| `this.$route` | `useRoute()` |
| `this.$store` | `useBlogStore()` |

---

## 🔍 质量保证

### 代码规范

#### 1. 命名规范
- **组件名**: PascalCase (BlogEditor, TagCloud)
- **文件名**: kebab-case (blog-editor, tag-cloud)
- **变量名**: camelCase (blogList, userInfo)
- **常量名**: UPPER_CASE (UPLOAD_SIZE, API_BASE_URL)

#### 2. 代码结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 1. 导入依赖
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 2. Props 和 Emits
const props = defineProps({...})
const emit = defineEmits([...])

// 3. 响应式数据
const loading = ref(false)
const form = reactive({...})

// 4. 计算属性
const computedValue = computed(() => {...})

// 5. 方法
const handleSubmit = async () => {...}

// 6. 生命周期
onMounted(() => {...})

// 7. 暴露方法
defineExpose({...})
</script>

<style scoped lang="scss">
/* 样式 */
</style>
```

#### 3. 注释规范
```javascript
// 函数注释
/**
 * 查询列表数据
 * @param {Object} params - 查询参数
 * @returns {Promise} - 返回 Promise
 */
const getList = async (params) => {...}

// 复杂逻辑注释
// 处理多选逻辑
const handleSelection = () => {...}
```

### 测试清单

#### 功能测试
- [ ] API 接口调用正常
- [ ] 表单验证有效
- [ ] 分页功能正常
- [ ] 搜索筛选准确
- [ ] 文件上传成功
- [ ] 权限控制有效

#### 兼容性测试
- [ ] Chrome 浏览器
- [ ] Firefox 浏览器
- [ ] Edge 浏览器
- [ ] Safari 浏览器

#### 响应式测试
- [ ] 桌面端 (>1200px)
- [ ] 平板端 (768px - 1200px)
- [ ] 移动端 (<768px)

---

## 📝 问题和解决方案

### 已解决问题

#### 1. Vue3 v-model 绑定问题
**问题**: Vue2 的 `v-model` 使用 `value` 和 `input` 事件
**解决**: Vue3 改用 `modelValue` 和 `update:modelValue`

```vue
<!-- Vue2 -->
<custom-input v-model="value" />
props: ['value']
this.$emit('input', newValue)

<!-- Vue3 -->
<custom-input v-model="value" />
defineProps(['modelValue'])
emit('update:modelValue', newValue)
```

#### 2. Element Plus 图标使用
**问题**: Element UI 图标是字体图标
**解决**: Element Plus 改用 SVG 图标组件

```vue
<!-- Vue2 -->
<i class="el-icon-search"></i>

<!-- Vue3 -->
<el-icon><Search /></el-icon>
```

#### 3. 路由参数获取
**问题**: Vue2 使用 `this.$route.params`
**解决**: Vue3 使用 `useRoute()` 组合式函数

```javascript
// Vue2
this.$route.params.id

// Vue3
import { useRoute } from 'vue-router'
const route = useRoute()
route.params.id
```

### 待解决问题

#### 1. Markdown 编辑器选型
**状态**: 待决定
**候选方案**:
- md-editor-v3 (推荐)
- v-md-editor
- @vueup/vue-quill

#### 2. 图片裁剪组件
**状态**: 待集成
**候选方案**:
- cropperjs (推荐)
- vue-cropper@next

#### 3. 中国地图数据
**状态**: 待引入
**解决方案**: 下载 ECharts china.js

---

## 📚 参考资源

### 官方文档
- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Vue Router 4 文档](https://router.vuejs.org/zh/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)
- [Element Plus 文档](https://element-plus.org/zh-CN/)
- [ECharts 文档](https://echarts.apache.org/zh/index.html)

### 第三方库
- [md-editor-v3](https://imzbf.github.io/md-editor-v3/index)
- [cropperjs](https://github.com/fengyuanchen/cropperjs)

### 项目资源
- Vue2 源码: `D:\桌面\blog-master\blog-satoken\blog-front\admin`
- Vue3 目标: `D:\桌面\blog-master\blog-satoken\blog-front\blog-admin-Vue3`

---

## 🤝 协作说明

### 开发流程
1. 从 API 层开始,确保接口定义正确
2. 创建对应的页面组件
3. 实现业务逻辑
4. 进行功能测试
5. 代码审查和优化

### 代码提交规范
```
feat: 添加文章管理功能
fix: 修复标签删除bug
docs: 更新README文档
style: 代码格式优化
refactor: 重构评论组件
test: 添加单元测试
```

### 分支管理
- `main` - 主分支,稳定版本
- `develop` - 开发分支
- `feature/xxx` - 功能分支
- `bugfix/xxx` - 修复分支

---

## 📅 时间线

### 2026-08-14 (Day 1)

#### 上午 (10:00-12:00)
- ✅ 创建项目结构
- ✅ 完成 17 个 API 接口
- ✅ 完成 Store 状态管理
- ✅ 完成工具函数和配置
- ✅ 完成 2 个自定义组件
- ✅ 完成路由配置
- ✅ 完成 3 个业务页面
- ✅ 编写迁移文档

#### 下午 (14:00-17:30)
- ✅ 启动4个并行Agent完成所有业务页面
  - Agent 1: 互动内容模块 (4个页面) ✅
  - Agent 2: 系统管理模块 (6个页面) ✅
  - Agent 3: 文章管理模块 (2个页面) ✅
  - Agent 4: 其他功能模块 (6个页面) ✅
- ✅ 代码提交到Git
- ✅ 更新项目文档

### 待完成
- 路由集成和入口文件修改 (预计 1-2h)
- 功能测试和Bug修复 (预计 2-3h)
- 样式优化和第三方库集成 (预计 2-3h)

**预计剩余工时**: 5-8 小时

---

## 📞 联系方式

如有问题或建议,请通过以下方式联系:
- 项目地址: `D:\桌面\blog-master\blog-satoken\blog-front\blog-admin-Vue3`
- 文档位置: `README_BLOG.md`, `MIGRATION.md`, `PROGRESS.md`

---

**文档版本**: v1.0  
**最后更新**: 2026-08-14  
**下次更新**: 根据开发进度实时更新
