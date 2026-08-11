# RenZS Blog - 基于 Sa-Token 的前后端分离博客系统

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.4.1-brightgreen.svg)
![Sa-Token](https://img.shields.io/badge/Sa--Token-1.39.0-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.4.21-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

一个功能完善的个人博客系统，支持文章发布、评论互动、说说动态、相册展示、在线聊天等功能。

[在线演示](https://www.renzs.top/) · [问题反馈](https://gitee.com/chen_fuyun/blog-satoken/issues) · [贡献指南](#贡献指南)

</div>

---

## ✨ 特性

- 🔐 **轻量级鉴权** - 使用 Sa-Token 替代 Spring Security，配置简单、性能优越
- 🎨 **三端适配** - 后台管理（Vue 2）、博客前台（Vue 3）、移动端 App
- 📝 **富文本编辑** - Markdown 编辑器，支持代码高亮、LaTeX 公式、任务列表
- 💬 **实时通讯** - WebSocket 聊天室、消息通知
- 🔍 **全文搜索** - Elasticsearch 支持文章快速检索
- 🎯 **消息队列** - RocketMQ + RabbitMQ 异步处理
- 🌓 **主题切换** - 支持明暗主题无缝切换
- 📱 **响应式设计** - 完美适配桌面、平板、移动端
- 🚀 **高性能** - Redis 缓存、接口限流、SQL 优化

---

## 📦 项目结构

```
blog-satoken/
├── renzs-blog-satoken/          # 后端 Spring Boot 项目
│   ├── src/main/java/com/chen/blog/
│   │   ├── common/              # 公共模块
│   │   │   ├── satoken/         # Sa-Token 鉴权配置
│   │   │   ├── config/          # 全局配置
│   │   │   ├── handler/         # 异常处理器
│   │   │   └── util/            # 工具类
│   │   ├── module/              # 业务模块
│   │   │   ├── user/            # 用户模块（登录、注册、OAuth）
│   │   │   ├── article/         # 文章模块
│   │   │   ├── comment/         # 评论模块
│   │   │   ├── talk/            # 说说模块
│   │   │   ├── album/           # 相册模块
│   │   │   ├── notice/          # 通知模块（WebSocket）
│   │   │   └── ...
│   │   └── consumer/            # 消息队列消费者
│   └── src/main/resources/
│       ├── application.yml      # 主配置文件
│       ├── application-dev.yml  # 开发环境配置
│       └── application-pro.yml  # 生产环境配置
├── blog-front/
│   ├── admin/                   # 后台管理系统（Vue 2 + Element UI）
│   ├── blog-vue3/               # 博客前台（Vue 3 + Vuetify + Element Plus）
│   └── blog-app/                # 移动端 App
└── README.md
```

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 2.4.1 | 核心框架 |
| Sa-Token | 1.39.0 | 鉴权框架（Cookie 模式） |
| MyBatis-Plus | 3.4.0 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存 + 会话存储 |
| Elasticsearch | 7.x | 全文搜索引擎 |
| RocketMQ | 2.1.1 | 分布式消息队列 |
| RabbitMQ | - | 消息队列 |
| Swagger | 2.0.7 (Knife4j) | API 文档 |
| WebSocket | - | 实时通讯 |
| Hutool | 5.7.5 | Java 工具库 |
| Lombok | - | 简化代码 |
| XXL-Job | 2.3.1 | 分布式任务调度 |
| Aliyun OSS / 腾讯云 COS | - | 对象存储 |

### 前端技术

#### 博客前台（Vue 3）

| 技术 | 版本 | 说明 |
|---|---|---|
| Vue | 3.4.21 | 渐进式框架 |
| TypeScript | 5.4.0 | 类型安全 |
| Vite | 5.1.6 | 构建工具 |
| Vue Router | 4.3.0 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Vuetify | 3.5.8 | Material Design UI |
| Element Plus | 2.6.1 | UI 组件库 |
| Axios | 1.6.7 | HTTP 客户端 |
| Markdown-it | 14.0.0 | Markdown 渲染 |
| Highlight.js | 11.9.0 | 代码高亮 |

#### 后台管理（Vue 2）

| 技术 | 版本 | 说明 |
|---|---|---|
| Vue | 2.6.11 | 渐进式框架 |
| Element UI | 2.15.5 | UI 组件库 |
| Vuex | 3.4.0 | 状态管理 |
| Mavon-Editor | 2.9.0 | Markdown 编辑器 |
| ECharts | 4.8.0 | 数据可视化 |

---

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 14+
- MySQL 8.0+
- Redis 6.0+

### 1. 克隆项目

```bash
git clone https://gitee.com/chen_fuyun/blog-satoken.git
cd blog-satoken
```

### 2. 数据库初始化

1. 创建数据库 `chen-blog`
2. 导入 SQL 脚本（联系作者获取或查看项目文档）
3. 修改后端配置文件 `renzs-blog-satoken/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chen-blog?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 3. 启动后端

```bash
cd renzs-blog-satoken
mvn clean install
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8088`

### 4. 启动前端

#### 博客前台（Vue 3）

```bash
cd blog-front/blog-vue3
npm install
npm run dev
```

前台服务运行在 `http://localhost:3000`

#### 后台管理（Vue 2）

```bash
cd blog-front/admin
npm install
npm run serve
```

后台服务运行在 `http://localhost:8080`

### 5. 访问系统

- 博客前台：http://localhost:3000
- 后台管理：http://localhost:8080
- API 文档：http://localhost:8088/doc.html

---

## 📖 核心功能

### 用户端功能

- ✅ 用户注册、登录（账号密码 / QQ / 微博 / Gitee）
- ✅ 文章浏览、搜索、分类、标签
- ✅ 文章点赞、收藏、评论、回复
- ✅ 说说动态发布与互动
- ✅ 相册展示
- ✅ 在线聊天室
- ✅ 留言板
- ✅ 友情链接
- ✅ 关于页面
- ✅ 归档时间轴
- ✅ 主题切换（明暗模式）

### 管理端功能

- ✅ 文章管理（发布、编辑、删除、置顶）
- ✅ 分类 / 标签管理
- ✅ 评论管理（审核、删除）
- ✅ 用户管理（禁用、角色分配）
- ✅ 角色权限管理（RBAC）
- ✅ 菜单资源管理
- ✅ 友链管理
- ✅ 相册管理
- ✅ 说说管理
- ✅ 在线用户监控
- ✅ 操作日志
- ✅ 定时任务管理
- ✅ 数据统计（访问量、文章数、用户数）

---

## 🔐 鉴权机制

本项目使用 [Sa-Token](https://sa-token.cc) 轻量级鉴权框架，相比 Spring Security：

- **更简洁的 API**：`StpUtil.login()` / `StpUtil.checkPermission()`
- **更灵活的配置**：支持多账号体系、单点登录、OAuth2
- **更好的性能**：无复杂过滤器链，性能损耗小

### 登录流程

1. 前端提交账号密码到 `POST /login`
2. 后端验证密码（BCrypt）并调用 `StpUtil.login(userInfoId)`
3. Sa-Token 生成 token 并写入 Cookie（名称：`JSESSIONID`）
4. 后续请求自动携带 Cookie，Sa-Token 拦截器验证 token

### 权限控制

- **动态 URL 权限**：启动时从 `tb_resource` 表加载到 `ResourceRoleCache`
- **拦截器**：`SaInterceptor` 全局路由拦截
- **角色查询**：`StpInterfaceImpl.getRoleList()` 从数据库读取用户角色
- **业务侧获取登录用户**：`UserUtils.getLoginUser()`

### API 说明

| 方法 | URL | 说明 |
|---|---|---|
| POST | `/login` | 账号密码登录 |
| POST | `/logout` | 注销登录 |
| GET | `/users/current` | 获取当前登录用户信息 |
| POST | `/users/oauth/qq` | QQ 第三方登录 |
| POST | `/users/oauth/weibo` | 微博第三方登录 |
| POST | `/users/oauth/gitee` | Gitee 第三方登录 |

---

## 🎯 配置说明

### 后端配置

主要配置文件：`application-dev.yml`

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chen-blog
    username: root
    password: password

# Redis 配置
  redis:
    host: localhost
    port: 6379
    password: 

# Sa-Token 配置
sa-token:
  token-name: JSESSIONID
  timeout: 2592000          # token 有效期（秒），默认 30 天
  active-timeout: -1        # token 临时有效期（秒），-1 表示禁用
  is-concurrent: true       # 是否允许同一账号并发登录
  is-share: false           # 多端登录是否共享 token
  token-style: uuid         # token 风格
  is-log: false             # 是否输出操作日志

# Elasticsearch 配置（可选）
  elasticsearch:
    rest:
      uris: http://localhost:9200

# 文件上传配置
upload:
  mode: oss                 # 上传模式：local / oss / cos
  oss:
    url: https://your-bucket.oss-cn-hangzhou.aliyuncs.com
    endpoint: oss-cn-hangzhou.aliyuncs.com
    accessKeyId: your-access-key-id
    accessKeySecret: your-access-key-secret
    bucketName: your-bucket
```

### 前端配置

博客前台 `.env` 文件：

```env
VITE_APP_TITLE=Renzs Blog
VITE_API_BASE_URL=    # 留空，通过 Vite 代理转发
```

后台管理无需额外配置，API 地址硬编码在 `axios` 实例中。

---

## 🧪 测试

```bash
cd renzs-blog-satoken
mvn test
```

测试覆盖：
- 登录 / 注销接口
- 鉴权拦截器
- 角色权限查询
- WebSocket 握手鉴权
- 第三方登录

---

## 📦 部署

### 后端部署

1. 修改 `application-pro.yml` 生产环境配置
2. 打包项目：

```bash
cd renzs-blog-satoken
mvn clean package -DskipTests
```

3. 运行 JAR 包：

```bash
java -jar target/renzs-blog-satoken-0.0.1.jar --spring.profiles.active=pro
```

4. 使用 Nginx 反向代理（可选）

### 前端部署

#### 博客前台

```bash
cd blog-front/blog-vue3
npm run build
```

构建产物在 `dist/` 目录，部署到 Nginx 静态服务器。

#### 后台管理

```bash
cd blog-front/admin
npm run build
```

构建产物在 `dist/` 目录，部署到 Nginx 静态服务器。

### Nginx 配置示例

```nginx
# 后端 API
upstream blog-api {
    server 127.0.0.1:8088;
}

# 博客前台
server {
    listen 80;
    server_name yourdomain.com;
    
    root /var/www/blog-vue3/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://blog-api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /ws {
        proxy_pass http://blog-api;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}

# 后台管理
server {
    listen 80;
    server_name admin.yourdomain.com;
    
    root /var/www/admin/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://blog-api;
    }
}
```

---

## 🤝 贡献指南

欢迎贡献代码、提交 Issue 或 Pull Request！

### 贡献流程

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

### 代码规范

- 后端：遵循阿里巴巴 Java 开发手册
- 前端：使用 ESLint + Prettier 格式化代码
- 提交信息：遵循 Conventional Commits 规范

---

## 📝 更新日志

详见 [CHANGELOG.md](renzs-blog-satoken/CHANGELOG.md)

---

## 📄 开源协议

本项目基于 [MIT](LICENSE) 协议开源，可自由使用、修改、商用。

---

## 💬 联系方式

- Gitee：https://gitee.com/chen_fuyun/blog-satoken
- GitHub：https://github.com/RenZSx/RenZS-blog
- Issue：https://gitee.com/chen_fuyun/blog-satoken/issues

---

## 🌟 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Sa-Token](https://sa-token.cc)
- [Vue.js](https://vuejs.org)
- [Element UI](https://element.eleme.io) / [Element Plus](https://element-plus.org)
- [Vuetify](https://vuetifyjs.com)
- [MyBatis-Plus](https://baomidou.com)

---

<div align="center">

如果这个项目对你有帮助，请点个 ⭐️ Star 支持一下！

</div>
