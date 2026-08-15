<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">博客后台管理系统 Vue3 版本</h1>
<h4 align="center">RenZS Blog 博客后台管理系统 (Vue3)</h4>
<p align="center">
	<a href="https://gitee.com/chen_fuyun/blog-satoken/stargazers"><img src="https://gitee.com/chen_fuyun/blog-satoken/badge/star.svg?theme=dark"></a>
	<a href="https://gitee.com/chen_fuyun/blog-satoken"><img src="https://img.shields.io/badge/blog--admin--vue3-v3.9.2-brightgreen.svg"></a>
	<a href="https://gitee.com/chen_fuyun/blog-satoken/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 项目简介

本项目是一个功能完善的博客后台管理系统前端，基于 Vue 3 + Vite + Element Plus 开发，采用现代化的前端技术栈。系统提供了博客内容管理、用户权限管理、系统监控等完整功能。

### 核心特性

- 📝 **文章管理**：支持 Markdown 编辑、文章分类、标签、置顶、推荐等功能
- 💬 **评论管理**：评论审核、回复、批量操作
- 📷 **相册管理**：相册创建、照片上传、批量管理
- 💭 **说说管理**：类似微博的动态发布功能
- 👥 **用户管理**：用户信息、角色分配、权限控制
- 🔐 **权限管理**：基于 RBAC 的细粒度权限控制
- 📊 **数据统计**：访问统计、数据可视化
- 🎨 **个性化配置**：网站信息、社交配置、主题设置
- 📱 **响应式设计**：支持多种设备访问

### 技术栈

* **前端框架**：[Vue 3.5.26](https://cn.vuejs.org/)
* **构建工具**：[Vite 6.4.1](https://cn.vitejs.dev/)
* **UI 组件库**：[Element Plus 2.13.1](https://element-plus.org/zh-CN/)
* **状态管理**：[Pinia 3.0.4](https://pinia.vuejs.org/zh/)
* **路由管理**：[Vue Router 4.6.4](https://router.vuejs.org/zh/)
* **HTTP 客户端**：[Axios 1.13.2](https://axios-http.com/)
* **富文本编辑器**：[Vue Quill 1.2.0](https://github.com/vueup/vue-quill)
* **图表库**：[ECharts 5.6.0](https://echarts.apache.org/zh/index.html)
* **拖拽组件**：[Vue Draggable 4.1.0](https://github.com/SortableJS/vue.draggable.next)

### 配套后端

* 本项目使用 SaToken 作为权限认证框架
* 配套后端代码：[renzs-blog-satoken](https://gitee.com/chen_fuyun/blog-satoken)（项目根目录下的 Spring Boot 后端项目）

## 快速开始

详细的安装和开发指南请查看 [快速开始文档](./QUICKSTART.md)

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产环境
npm run build:prod
```

访问地址：http://localhost:80

默认账号：admin / admin123

## 功能模块

### 博客管理模块

1. **文章管理**
   - 文章列表：分页展示、搜索、筛选（分类、标签、状态）
   - 文章编辑：Markdown 编辑器、封面上传、分类标签选择
   - 文章操作：置顶、推荐、删除、恢复、导入导出
   - 状态管理：公开、私密、草稿

2. **分类管理**
   - 分类 CRUD 操作
   - 文章数量统计
   - 批量操作

3. **标签管理**
   - 标签 CRUD 操作
   - 标签使用频率统计
   - 批量管理

4. **评论管理**
   - 评论审核
   - 评论回复
   - 批量操作（通过、删除）
   - 垃圾评论过滤

5. **留言管理**
   - 留言审核
   - 留言回复
   - 批量处理

6. **说说管理**
   - 说说发布（支持多图）
   - 说说编辑、删除
   - 说说置顶
   - 公开/私密设置

### 内容展示模块

7. **相册管理**
   - 相册创建、编辑、删除
   - 照片上传（单张/批量）
   - 照片移动、删除
   - 相册权限设置（公开/私密）

8. **友链管理**
   - 友链 CRUD 操作
   - 友链审核
   - 友链置顶

9. **页面管理**
   - 页面封面配置
   - 页面内容编辑

10. **关于页面**
    - Markdown 编辑
    - 实时预览

### 系统管理模块

11. **用户管理**
    - 用户列表、搜索、筛选
    - 用户信息编辑
    - 角色分配
    - 禁用/启用用户
    - 密码重置

12. **角色管理**
    - 角色 CRUD 操作
    - 菜单权限分配
    - 资源权限分配
    - 数据权限控制

13. **菜单管理**
    - 菜单树形结构
    - 菜单 CRUD 操作
    - 菜单图标配置
    - 显示/隐藏控制
    - 权限标识

14. **资源管理**
    - API 资源配置
    - 请求方式管理
    - 匿名访问控制

15. **网站配置**
    - 网站基本信息
    - 社交账号配置
    - 用户配置（登录、评论、留言）
    - 打赏配置
    - 默认图片设置

16. **操作日志**
    - 日志查询
    - 日志导出
    - 日志清理

### 监控模块

17. **在线用户**
    - 在线用户列表
    - 强制下线

18. **定时任务**
    - 任务调度管理
    - 任务执行日志
    - 任务启动/停止

19. **服务监控**
    - CPU、内存监控
    - JVM 信息
    - 磁盘状态

20. **缓存监控**
    - 缓存统计
    - 命令统计
    - 缓存清理

## 前端运行

```bash
# 克隆项目
git clone https://github.com/your-repo/blog-satoken.git

# 进入项目目录
cd blog-satoken/blog-front/blog-admin-Vue3

# 安装依赖
npm install
# 或使用 yarn
yarn install
# 或使用 pnpm（推荐）
pnpm install

# 启动服务
npm run dev

# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod

# 前端访问地址
http://localhost:80
```

## 项目文档

- 📖 [快速开始指南](./QUICKSTART.md) - 详细的安装和开发指南
- 🔌 [后端接口集成](./BACKEND_INTEGRATION.md) - 后端接口对接说明 ⭐
- ✅ [功能测试清单](./TEST_CHECKLIST.md) - 完整的功能测试项
- 🚀 [部署指南](./DEPLOYMENT.md) - 生产环境部署流程

## 项目截图

<table>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/cd1f90be5f2684f4560c9519c0f2a232ee8.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/1cbcf0e6f257c7d3a063c0e3f2ff989e4b3.jpg"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8074972883b5ba0622e13246738ebba237a.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-9f88719cdfca9af2e58b352a20e23d43b12.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-39bf2584ec3a529b0d5a3b70d15c9b37646.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-936ec82d1f4872e1bc980927654b6007307.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-b2d62ceb95d2dd9b3fbe157bb70d26001e9.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d67451d308b7a79ad6819723396f7c3d77a.png"/></td>
    </tr>	 
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/5e8c387724954459291aafd5eb52b456f53.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/644e78da53c2e92a95dfda4f76e6d117c4b.jpg"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8370a0d02977eebf6dbf854c8450293c937.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-49003ed83f60f633e7153609a53a2b644f7.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d4fe726319ece268d4746602c39cffc0621.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-c195234bbcd30be6927f037a6755e6ab69c.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/b6115bc8c31de52951982e509930b20684a.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-5e4daac0bb59612c5038448acbcef235e3a.png"/></td>
    </tr>
</table>


## 开发指南

### 代码规范

- 使用 ES6+ 语法
- 组件命名采用 PascalCase
- 使用 Composition API（`<script setup>`）
- 遵循 Vue 3 最佳实践

### 目录结构

```
src/
├── api/              # API 接口定义
│   ├── blog/        # 博客相关接口
│   ├── monitor/     # 监控相关接口
│   ├── system/      # 系统相关接口
│   └── tool/        # 工具相关接口
├── assets/          # 静态资源
├── components/      # 全局组件
├── layout/          # 布局组件
├── router/          # 路由配置
├── store/           # Pinia 状态管理
├── utils/           # 工具函数
└── views/           # 页面组件
```

### 新增功能

在 `src/api/` 对应模块下创建接口文件，在 `src/views/` 下创建页面，并配置路由。

### 常见问题

**Q: 端口被占用**

修改 `vite.config.js` 中的 `server.port` 配置。

**Q: 接口请求失败**

检查 `vite.config.js` 中的 `baseUrl` 是否正确，确保后端服务已启动。

更多问题请查看 [快速开始文档](./QUICKSTART.md)。

## 贡献指南

欢迎提交 Issue 和 Pull Request。

### 提交规范

- feat: 新功能
- fix: 修复 Bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构

### 开发流程

1. Fork 本项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 提交 Pull Request

## 技术支持

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Element Plus 官方文档](https://element-plus.org/zh-CN/)

## 许可证

[MIT License](https://opensource.org/licenses/MIT)

## 致谢

感谢以下开源项目：

- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) - 若依管理系统
- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Element Plus](https://element-plus.org/) - Vue 3 组件库
- [Vite](https://vitejs.dev/) - 下一代前端构建工具

---

⭐ 如果这个项目对你有帮助，请给一个 Star 支持一下！