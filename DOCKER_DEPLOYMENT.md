# Docker 部署教程

本文档提供基于 Docker 和 Docker Compose 的一键部署方案，包含 MySQL、Redis、Elasticsearch、后端服务和前端服务。

---

## 📋 目录

- [前置准备](#前置准备)
- [快速部署（推荐）](#快速部署推荐)
- [分步部署](#分步部署)
- [配置说明](#配置说明)
- [常见问题](#常见问题)
- [生产环境优化](#生产环境优化)

---

## 前置准备

### 1. 安装 Docker 和 Docker Compose

#### Linux (Ubuntu/Debian)

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | bash -s docker

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### Windows / macOS

下载并安装 [Docker Desktop](https://www.docker.com/products/docker-desktop)，已内置 Docker Compose。

### 2. 克隆项目

```bash
git clone https://gitee.com/chen_fuyun/blog-satoken.git
cd blog-satoken
```

### 3. 准备数据库初始化脚本

将数据库 SQL 初始化脚本放置在 `docker/mysql/init/` 目录：

```bash
mkdir -p docker/mysql/init
# 将 chen-blog.sql 文件复制到 docker/mysql/init/ 目录
```

---

## 快速部署（推荐）

### 一键启动所有服务

```bash
# 构建并启动所有容器
docker-compose up -d

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 访问服务

- **博客前台**：http://localhost:3000
- **后台管理**：http://localhost:8080
- **后端 API**：http://localhost:8088
- **API 文档**：http://localhost:8088/doc.html
- **MySQL**：localhost:3306（用户名：root，密码：blog_password_123）
- **Redis**：localhost:6379（密码：redis_password_123）
- **Elasticsearch**：http://localhost:9200

### 停止服务

```bash
# 停止所有容器
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除容器和数据卷（⚠️ 会删除数据库数据）
docker-compose down -v
```

---

## 分步部署

### 步骤 1：创建 Docker 网络

```bash
docker network create blog-network
```

### 步骤 2：启动 MySQL

```bash
docker run -d \
  --name blog-mysql \
  --network blog-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=blog_password_123 \
  -e MYSQL_DATABASE=chen-blog \
  -e TZ=Asia/Shanghai \
  -v blog-mysql-data:/var/lib/mysql \
  -v $(pwd)/docker/mysql/init:/docker-entrypoint-initdb.d \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --default-authentication-plugin=mysql_native_password
```

### 步骤 3：启动 Redis

```bash
docker run -d \
  --name blog-redis \
  --network blog-network \
  -p 6379:6379 \
  -e TZ=Asia/Shanghai \
  -v blog-redis-data:/data \
  redis:6.2-alpine \
  redis-server --requirepass redis_password_123 --appendonly yes
```

### 步骤 4：启动 Elasticsearch（可选）

```bash
docker run -d \
  --name blog-elasticsearch \
  --network blog-network \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e TZ=Asia/Shanghai \
  -v blog-es-data:/usr/share/elasticsearch/data \
  elasticsearch:7.17.9
```

### 步骤 5：构建并启动后端服务

```bash
cd renzs-blog-satoken
docker build -t blog-backend:latest .
docker run -d \
  --name blog-backend \
  --network blog-network \
  -p 8088:8088 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e TZ=Asia/Shanghai \
  blog-backend:latest
```

### 步骤 6：构建并启动前端服务

#### 博客前台

```bash
cd blog-front/blog-vue3
docker build -t blog-frontend:latest .
docker run -d \
  --name blog-frontend \
  --network blog-network \
  -p 3000:80 \
  blog-frontend:latest
```

#### 后台管理

```bash
cd blog-front/admin
docker build -t blog-admin:latest .
docker run -d \
  --name blog-admin \
  --network blog-network \
  -p 8080:80 \
  blog-admin:latest
```

---

## 配置说明

### Docker Compose 配置文件

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: blog-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: blog_password_123
      MYSQL_DATABASE: chen-blog
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
      - ./docker/mysql/conf:/etc/mysql/conf.d
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
      - --max_connections=1000
    networks:
      - blog-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-pblog_password_123"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis 缓存
  redis:
    image: redis:6.2-alpine
    container_name: blog-redis
    restart: unless-stopped
    environment:
      TZ: Asia/Shanghai
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --requirepass redis_password_123 --appendonly yes
    networks:
      - blog-network
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 3

  # Elasticsearch 搜索引擎（可选）
  elasticsearch:
    image: elasticsearch:7.17.9
    container_name: blog-elasticsearch
    restart: unless-stopped
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - TZ=Asia/Shanghai
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    networks:
      - blog-network
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5

  # 后端服务
  backend:
    build:
      context: ./renzs-blog-satoken
      dockerfile: Dockerfile
    container_name: blog-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: docker
      TZ: Asia/Shanghai
      JAVA_OPTS: "-Xms512m -Xmx1024m"
    ports:
      - "8088:8088"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - blog-network
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8088/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  # 博客前台
  frontend:
    build:
      context: ./blog-front/blog-vue3
      dockerfile: Dockerfile
    container_name: blog-frontend
    restart: unless-stopped
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - blog-network

  # 后台管理
  admin:
    build:
      context: ./blog-front/admin
      dockerfile: Dockerfile
    container_name: blog-admin
    restart: unless-stopped
    ports:
      - "8080:80"
    depends_on:
      - backend
    networks:
      - blog-network

networks:
  blog-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
  es-data:
```

### 后端 Dockerfile

创建 `renzs-blog-satoken/Dockerfile`：

```dockerfile
# 第一阶段：构建
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# 第二阶段：运行
FROM openjdk:8-jre-slim

WORKDIR /app

# 安装必要工具
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 从构建阶段复制 JAR 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8088

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD curl -f http://localhost:8088/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

### 后端 Docker 配置文件

创建 `renzs-blog-satoken/src/main/resources/application-docker.yml`：

```yaml
server:
  port: 8088

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://mysql:3306/chen-blog?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true
    username: root
    password: blog_password_123
    
  redis:
    host: redis
    port: 6379
    password: redis_password_123
    database: 0
    lettuce:
      pool:
        max-active: 200
        max-wait: -1ms
        max-idle: 10
        min-idle: 0
        
  elasticsearch:
    rest:
      uris: http://elasticsearch:9200

# Sa-Token 配置
sa-token:
  token-name: JSESSIONID
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: false
  token-style: uuid
  is-log: false

# 文件上传（使用本地存储）
upload:
  mode: local
  local:
    path: /app/upload

# 日志配置
logging:
  level:
    com.chen.blog: INFO
    org.springframework: WARN
  file:
    name: /app/logs/blog.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 前端 Dockerfile（Vue 3）

创建 `blog-front/blog-vue3/Dockerfile`：

```dockerfile
# 第一阶段：构建
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build

# 第二阶段：运行
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 前端 Nginx 配置

创建 `blog-front/blog-vue3/nginx.conf`：

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1000;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api {
        proxy_pass http://blog-backend:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 代理
    location /ws {
        proxy_pass http://blog-backend:8088;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 86400;
    }

    # 缓存静态资源
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 后台管理 Dockerfile（Vue 2）

创建 `blog-front/admin/Dockerfile`：

```dockerfile
# 第一阶段：构建
FROM node:14-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build

# 第二阶段：运行
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 后台管理 Nginx 配置

创建 `blog-front/admin/nginx.conf`：

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api {
        proxy_pass http://blog-backend:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 常见问题

### 1. 容器无法启动

**检查日志**：
```bash
docker-compose logs -f [service_name]
```

**常见原因**：
- 端口被占用：修改 `docker-compose.yml` 中的端口映射
- 内存不足：调整 Docker Desktop 的内存限制（推荐 4GB+）
- 数据库初始化失败：检查 `docker/mysql/init/` 目录中的 SQL 文件

### 2. 后端连接数据库失败

**解决方案**：
- 等待 MySQL 完全启动（约 30-60 秒）
- 检查数据库密码是否与配置文件一致
- 使用 `docker-compose restart backend` 重启后端

### 3. 前端无法访问后端 API

**检查后端状态**：
```bash
curl http://localhost:8088/actuator/health
```

**解决方案**：
- 确认后端容器已启动：`docker ps | grep blog-backend`
- 检查 Nginx 配置中的 `proxy_pass` 地址
- 查看前端容器日志：`docker logs blog-frontend`

### 4. Elasticsearch 启动失败（内存不足）

**临时方案**：
禁用 Elasticsearch（搜索功能不可用）

```yaml
# 在 docker-compose.yml 中注释掉 elasticsearch 服务
# elasticsearch:
#   ...
```

**长期方案**：
增加系统 `vm.max_map_count`

```bash
# Linux
sudo sysctl -w vm.max_map_count=262144
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf

# macOS / Windows (Docker Desktop)
# Settings -> Resources -> Advanced -> Memory: 增加到 4GB
```

### 5. 数据持久化

数据存储在 Docker 卷中，使用以下命令管理：

```bash
# 查看所有卷
docker volume ls

# 备份数据库
docker exec blog-mysql mysqldump -uroot -pblog_password_123 chen-blog > backup.sql

# 恢复数据库
docker exec -i blog-mysql mysql -uroot -pblog_password_123 chen-blog < backup.sql
```

---

## 生产环境优化

### 1. 使用环境变量管理敏感信息

创建 `.env` 文件：

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_strong_password
MYSQL_DATABASE=chen-blog

# Redis 配置
REDIS_PASSWORD=your_redis_password

# 后端配置
SPRING_PROFILES_ACTIVE=docker
JAVA_OPTS=-Xms1g -Xmx2g

# 文件上传（使用 OSS）
UPLOAD_MODE=oss
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret
OSS_BUCKET_NAME=your_bucket
```

在 `docker-compose.yml` 中引用：

```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
  
  redis:
    command: redis-server --requirepass ${REDIS_PASSWORD}
  
  backend:
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      JAVA_OPTS: ${JAVA_OPTS}
```

### 2. 配置 HTTPS（使用 Nginx + Let's Encrypt）

```yaml
# 添加 Nginx 反向代理服务
nginx-proxy:
  image: nginx:alpine
  container_name: nginx-proxy
  restart: unless-stopped
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./nginx/conf.d:/etc/nginx/conf.d
    - ./nginx/ssl:/etc/nginx/ssl
    - ./nginx/logs:/var/log/nginx
  networks:
    - blog-network
```

### 3. 资源限制

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 512M
```

### 4. 日志管理

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 5. 自动重启策略

```yaml
services:
  backend:
    restart: unless-stopped  # 或 always
```

### 6. 健康检查和依赖管理

已在上面的 `docker-compose.yml` 中配置，确保服务按正确顺序启动。

---

## 监控与维护

### 查看容器状态

```bash
# 查看所有容器
docker-compose ps

# 查看资源占用
docker stats

# 查看实时日志
docker-compose logs -f --tail=100
```

### 更新服务

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build

# 仅重启特定服务
docker-compose restart backend
```

### 清理资源

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的卷
docker volume prune

# 清理所有未使用的资源
docker system prune -a --volumes
```

---

## 总结

使用 Docker Compose 部署的优势：

✅ **一键启动** - 所有服务自动编排，无需手动配置  
✅ **环境隔离** - 容器之间相互独立，不影响宿主机  
✅ **快速回滚** - 出现问题可立即回退到上一版本  
✅ **易于扩展** - 需要时可轻松添加新服务（如 RabbitMQ、Nginx）  
✅ **跨平台** - Linux、Windows、macOS 统一部署方式

如有问题，请提交 [Issue](https://gitee.com/chen_fuyun/blog-satoken/issues)。
