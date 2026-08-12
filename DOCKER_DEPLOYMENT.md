# Docker 一键部署

仓库已包含完整的 Docker Compose 配置，可一键启动以下服务：

- MySQL 8.0
- Redis 6.2
- Spring Boot 后端
- Vue 3 博客前台
- Vue 2 管理后台

默认使用 MySQL 搜索、直接邮件模式和本地文件上传。Elasticsearch 与 RocketMQ 不属于默认栈，不会影响首次启动。

## 环境要求

- Docker Engine 20.10+，或 Docker Desktop
- Docker Compose v2（使用 `docker compose` 命令）
- 建议至少为 Docker 分配 2 GB 可用内存

在项目根目录执行：

```bash
docker --version
docker compose version
```

## 一键启动

无需手工复制 SQL 或创建数据库，直接运行：

```bash
docker compose up -d --build
```

首次构建需要下载 Maven、Node.js、MySQL、Redis 和 Nginx 依赖，耗时取决于网络。Compose 会等待 MySQL、Redis 和后端健康后再启动前端服务。

查看状态：

```bash
docker compose ps
```

全部服务显示为 `running` 或 `healthy` 后即可访问：

| 服务 | 默认地址 |
| --- | --- |
| 博客前台 | <http://localhost:3000> |
| 管理后台 | <http://localhost:8080> |
| 后端 API | <http://localhost:8088> |
| API 文档 | <http://localhost:8088/doc.html> |
| 健康检查 | <http://localhost:8088/actuator/health> |

MySQL 和 Redis 默认只在 Compose 内部网络中使用，不映射到宿主机端口。

## 自定义配置

本地体验可以直接使用内置默认值。正式部署前应复制环境变量模板并修改密码、域名与端口：

```bash
cp .env.example .env
```

Windows PowerShell：

```powershell
Copy-Item .env.example .env
```

常用变量如下：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | `blog_password_123` | MySQL root 密码，生产环境必须修改 |
| `REDIS_PASSWORD` | `redis_password_123` | Redis 密码，生产环境必须修改 |
| `FRONTEND_PORT` | `3000` | 博客前台宿主机端口 |
| `ADMIN_PORT` | `8080` | 管理后台宿主机端口 |
| `BACKEND_PORT` | `8088` | 后端宿主机端口 |
| `SITE_URL` | `http://localhost:3000` | 邮件通知中的站点地址 |
| `UPLOAD_BASE_URL` | `http://localhost:3000/upload/` | 本地上传文件的公开地址，必须以 `/` 结尾 |
| `SEARCH_MODE` | `mysql` | 默认一键栈只支持 `mysql` |
| `EMAIL_MODE` | `direct` | `direct` 或 `mq` |
| `UPLOAD_MODE` | `local` | `local`、`oss` 或 `cos` |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | 后端 JVM 参数 |

修改 `.env` 后重新创建容器：

```bash
docker compose up -d --build --force-recreate
```

### 邮件与 RocketMQ

默认配置关闭邮箱注册和邮件通知，也不需要 RocketMQ，避免空白凭据导致验证码请求失败。若要启用邮箱注册或邮件通知，需要在 `.env` 中设置 `ALIYUNMAIL_ACCESS_KEY`、`ALIYUNMAIL_SECRET_KEY`、`ALIYUNMAIL_REPLY_ADDRESS` 和 `ALIYUNMAIL_ACCOUNT_NAME`，再在后台网站配置中开启对应开关。

若使用外部 RocketMQ，将 `EMAIL_MODE` 改为 `mq`，并设置：

```dotenv
ROCKETMQ_NAME_SERVER=rocketmq.example.com:9876
ROCKETMQ_PRODUCER_GROUP=email-group
```

然后使用 MQ 覆盖文件启动：

```bash
docker compose -f docker-compose.yml -f docker-compose.mq.yml up -d --build
```

Compose 不会自动部署 RocketMQ；该地址必须可由后端容器访问。默认命令不会创建 RocketMQ producer。

### OSS 或 COS 上传

默认上传文件保存在 Docker 命名卷中，并由前台和后台的 Nginx 通过 `/upload/` 提供访问。若改用 OSS 或 COS，还需按 `application-docker.yml` 中的变量名补齐对应凭据，例如 `OSS_ENDPOINT`、`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET` 和 `OSS_BUCKET_NAME`。

## 日常运维

查看所有日志：

```bash
docker compose logs -f
```

只查看后端日志：

```bash
docker compose logs -f backend
```

重启服务：

```bash
docker compose restart
```

拉取代码后更新：

```bash
git pull
docker compose up -d --build
```

停止并删除容器，但保留数据：

```bash
docker compose down
```

## 数据持久化与初始化

以下命名卷不会因 `docker compose down` 被删除：

| 卷 | 数据 |
| --- | --- |
| `mysql-data` | 数据库 |
| `redis-data` | Redis 持久化数据 |
| `upload-data` | 本地上传文件 |
| `logs-data` | 后端文件日志 |

数据库首次创建时会依次执行：

1. `renzs-blog-satoken/chen_blog.sql`：创建 `chen_blog` 数据库及表结构。
2. `docker/mysql/init/02-default-config.sql`：写入首页必需的默认站点配置。

MySQL 初始化脚本只会在数据卷为空时执行。修改 SQL 后不会自动覆盖已有数据库。

下面的命令会永久删除数据库、Redis、上传文件和日志，仅在明确需要全量重置时使用：

```bash
docker compose down -v
docker compose up -d --build
```

项目 SQL 当前只有表结构和站点基础配置，不包含管理员、角色、菜单或历史业务数据。部署已有站点时，请将原数据库备份导入 `chen_blog`，不要依赖弱口令默认管理员。

## 排查启动问题

先查看状态和后端日志：

```bash
docker compose ps
docker compose logs --tail=200 mysql redis backend
```

常见情况：

- 端口被占用：在 `.env` 中修改 `FRONTEND_PORT`、`ADMIN_PORT` 或 `BACKEND_PORT`。
- 后端连接数据库失败：确认 `.env` 中的密码没有在 MySQL 卷初始化后被单独修改；已有卷仍保留旧密码。
- 上传文件返回旧域名：修改 `UPLOAD_BASE_URL` 后重新创建后端容器。
- 前端返回 `502`：等待 `backend` 变为 `healthy`，再检查 `docker compose logs backend`。
- 邮件发送失败：默认空白凭据不会发送邮件，需要配置阿里云邮件服务后再启用相关功能。

## 生产部署建议

- 必须修改 MySQL 和 Redis 默认密码。
- 将 `SITE_URL` 与 `UPLOAD_BASE_URL` 改为实际 HTTPS 域名。
- 在 Compose 前增加具备 TLS 证书的反向代理，只对公网开放 80/443。
- 不要把 MySQL、Redis 或 Actuator 详细信息直接暴露到公网。
- 定期备份 `mysql-data` 和 `upload-data`，并实际验证恢复流程。
- `.env` 包含凭据且已被仓库 `.gitignore` 忽略，不要提交到版本库。
