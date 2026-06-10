# renzs-blog-satoken

基于 Spring Boot 2.4.1 + Vue 的前后端分离博客系统(后端 sa-token 鉴权版)。

本仓库由 `renzs-blog`(原 Spring Security 版本)迁移而来,鉴权框架替换为 [sa-token](https://sa-token.cc) 1.39.0,前端零改动、数据库零改动。

## 技术栈

| 类别 | 选型 |
|---|---|
| 框架 | Spring Boot 2.4.1 |
| JDK | Java 8 |
| 数据库 | MySQL 8 |
| ORM | MyBatis-Plus |
| 缓存 / 会话 | Redis(sa-token 持久化) |
| 鉴权 | sa-token 1.39.0(Cookie 模式) |
| 密码加密 | Hutool BCrypt |
| 搜索 | Elasticsearch |
| 消息 | RocketMQ + RabbitMQ |
| API 文档 | Swagger |

## 鉴权机制

- **Token 模式**: Cookie 模式(沿用 `JSESSIONID` 作为 token 名,前端零改动)
- **会话存储**: Redis(Key 前缀 `satoken:*`)
- **权限模型**: 动态 URL 权限,启动时从 `tb_resource` 表加载到 `ResourceRoleCache`
- **拦截机制**: `SaInterceptor` 全局路由拦截,排除登录/注册/三方登录/Swagger 等公开路径
- **登录入口**: `LoginController.login()` + `LoginServiceImpl`(自实现,替代原 Spring Security 表单过滤器)
- **业务侧获取登录用户**: `UserUtils.getLoginUser()`(签名不变,内部改为读 SaSession)

### 登录接口

| 方法 | URL | 说明 |
|---|---|---|
| `POST` | `/login` | 账号密码登录(form-urlencoded) |
| `POST` | `/logout` | 注销 |
| `GET` | `/users/current` | 获取当前登录用户 |
| `POST` | `/users/oauth/qq` | QQ 第三方登录 |
| `POST` | `/users/oauth/weibo` | 微博第三方登录 |
| `POST` | `/users/oauth/gitee` | Gitee 第三方登录 |

### 业务码

| 业务码 | 含义 |
|---|---|
| `40001` | 未登录(`NO_LOGIN`) |
| `40300` | 权限不足(`AUTHORIZED`) |
| `52002` | 用户名不存在(`USERNAME_NOT_EXIST`) |
| `20000` | 操作成功(`SUCCESS`) |

## 启动方式

### 1. 准备外部服务

- MySQL 8 (端口 3306,库 `chen-blog`)
- Redis 6+ (端口 6379)
- Elasticsearch 7+(可选,搜索功能用)
- RocketMQ / RabbitMQ(可选)

### 2. 配置数据库

修改 `src/main/resources/application-dev.yml` 中的数据库地址、账号、密码。

### 3. 启动

```bash
mvn spring-boot:run
```

默认端口 `8088`。

## 测试

```bash
mvn test
```

## 项目结构

```
src/main/java/com/chen/blog/
├── common/
│   ├── satoken/                  # ★ sa-token 集成
│   │   ├── SaTokenConfig.java       # SaInterceptor 注册
│   │   ├── StpInterfaceImpl.java    # 角色查询
│   │   ├── ResourceRoleCache.java   # URL-角色映射缓存
│   │   └── SaTokenExceptionHandler.java  # 异常映射
│   ├── util/UserUtils.java       # 获取当前登录用户(读 SaSession)
│   ├── handler/                  # 全局异常处理器
│   ├── config/                   # 其他配置(WebMvcConfig 等)
│   └── ...
└── module/
    ├── user/                     # 用户模块
    │   ├── controller/LoginController.java   # ★ 自实现登录接口
    │   ├── service/LoginService.java         # ★ 登录业务接口
    │   └── service/impl/LoginServiceImpl.java
    ├── article/                  # 文章模块
    ├── comment/                  # 评论模块
    ├── notice/                   # 通知模块(含 WebSocket)
    └── ...
```

## 迁移文档

- 设计文档: `docs/superpowers/specs/2026-06-09-sa-token-migration-design.md`
- 实施计划: `docs/superpowers/plans/2026-06-09-sa-token-migration-plan.md`
- 上线手册: `docs/sa-token-migration.md`
- 变更日志: `CHANGELOG.md`

## License

MIT
