# 部署指南

本文档介绍博客后台管理系统 Vue3 版本的部署流程和配置方法。

---

## 生产环境构建

### 环境准备

确保已安装以下软件：

- Node.js >= 16.0.0
- npm >= 8.0.0 或 pnpm >= 7.0.0

### 环境配置

#### 1. 修改生产环境配置

编辑 `.env.production` 文件：

```env
# 页面标题
VITE_APP_TITLE = 博客后台管理系统

# 生产环境配置
VITE_APP_ENV = 'production'

# 生产环境 API 基础路径
# 如果前后端同域名部署，使用相对路径
VITE_APP_BASE_API = '/prod-api'

# 如果前后端分离部署，使用完整 URL
# VITE_APP_BASE_API = 'https://api.yourdomain.com'
```

#### 2. 修改 Vite 配置

编辑 `vite.config.js`：

```javascript
export default defineConfig(({ mode, command }) => {
  return {
    // 如果部署在子路径，修改 base
    // 例如：https://yourdomain.com/admin/
    base: '/', // 或 '/admin/'
    
    // 其他配置...
  }
})
```

### 构建命令

```bash
# 安装依赖
npm install

# 构建生产环境
npm run build:prod

# 构建预发布环境
npm run build:stage
```

构建完成后，产物将输出到 `dist` 目录。

### 构建优化配置

项目已内置以下优化配置：

1. **代码分割**：自动按路由分割代码块
2. **资源压缩**：使用 gzip 压缩（需启用 vite-plugin-compression）
3. **Tree Shaking**：自动移除未使用的代码
4. **图片优化**：建议使用 CDN 托管大图片

---

## 服务器部署

### 方案一：Nginx 静态部署

#### 1. 上传构建产物

将 `dist` 目录上传到服务器，例如：

```bash
# 使用 scp
scp -r dist/* user@server:/var/www/blog-admin/

# 或使用 rsync
rsync -avz dist/ user@server:/var/www/blog-admin/
```

#### 2. Nginx 配置

创建或编辑 Nginx 配置文件 `/etc/nginx/sites-available/blog-admin`：

```nginx
server {
    listen 80;
    server_name admin.yourdomain.com;
    
    # 网站根目录
    root /var/www/blog-admin;
    index index.html;
    
    # 开启 gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/javascript application/json;
    
    # 静态资源缓存配置
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # SPA 路由配置（重要）
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理配置（如果前后端同服务器部署）
    location /prod-api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    # 安全配置
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
```

#### 3. 启用配置并重启 Nginx

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/blog-admin /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

#### 4. HTTPS 配置（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
sudo apt-get install certbot python3-certbot-nginx

# 获取证书并自动配置
sudo certbot --nginx -d admin.yourdomain.com

# 证书自动续期
sudo certbot renew --dry-run
```

完整的 HTTPS 配置：

```nginx
server {
    listen 80;
    server_name admin.yourdomain.com;
    
    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name admin.yourdomain.com;
    
    # SSL 证书配置
    ssl_certificate /etc/letsencrypt/live/admin.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/admin.yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # 网站根目录
    root /var/www/blog-admin;
    index index.html;
    
    # 其他配置同上...
}
```

### 方案二：CDN + 对象存储部署

#### 1. 上传到对象存储

以阿里云 OSS 为例：

```bash
# 安装阿里云 OSS 工具
npm install -g ossutil

# 配置 OSS
ossutil config

# 上传文件
ossutil cp -r dist/ oss://your-bucket/blog-admin/ --update
```

#### 2. 配置 CDN

1. 在对象存储中开启静态网站托管
2. 设置默认首页为 `index.html`
3. 设置错误页面为 `index.html`（用于 SPA 路由）
4. 配置 CDN 加速域名
5. 配置 CDN 缓存规则

#### 3. 配置 CDN 缓存规则

| 文件类型 | 缓存时间 |
|---------|---------|
| index.html | 不缓存或 5 分钟 |
| *.js, *.css | 30 天 |
| *.jpg, *.png, *.gif | 30 天 |
| *.woff, *.ttf | 30 天 |

---

## Docker 部署

### Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源码
COPY . .

# 构建项目
RUN npm run build:prod

# 生产阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf

创建 `nginx.conf` 文件：

```nginx
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    index index.html;
    
    # 开启 gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    
    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 构建和运行 Docker 镜像

```bash
# 构建镜像
docker build -t blog-admin:latest .

# 运行容器
docker run -d -p 80:80 --name blog-admin blog-admin:latest

# 查看日志
docker logs -f blog-admin

# 停止容器
docker stop blog-admin

# 删除容器
docker rm blog-admin
```

### docker-compose.yml

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  blog-admin:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: blog-admin
    ports:
      - "80:80"
    restart: always
    networks:
      - blog-network
    environment:
      - TZ=Asia/Shanghai
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  blog-network:
    driver: bridge
```

运行 Docker Compose：

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启服务
docker-compose restart
```

---

## CI/CD 配置

### GitHub Actions

创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy Blog Admin

on:
  push:
    branches:
      - master
      - main

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Build project
        run: npm run build:prod
      
      - name: Deploy to server
        uses: easingthemes/ssh-deploy@main
        env:
          SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
          REMOTE_HOST: ${{ secrets.REMOTE_HOST }}
          REMOTE_USER: ${{ secrets.REMOTE_USER }}
          SOURCE: "dist/"
          TARGET: "/var/www/blog-admin/"
      
      - name: Restart Nginx
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.REMOTE_HOST }}
          username: ${{ secrets.REMOTE_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: sudo systemctl restart nginx
```

### GitLab CI

创建 `.gitlab-ci.yml`：

```yaml
stages:
  - build
  - deploy

variables:
  NODE_VERSION: "18"

build:
  stage: build
  image: node:${NODE_VERSION}-alpine
  cache:
    key: ${CI_COMMIT_REF_SLUG}
    paths:
      - node_modules/
  script:
    - npm ci
    - npm run build:prod
  artifacts:
    paths:
      - dist/
    expire_in: 1 hour
  only:
    - master
    - main

deploy:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - ssh-keyscan $DEPLOY_HOST >> ~/.ssh/known_hosts
  script:
    - rsync -avz --delete dist/ $DEPLOY_USER@$DEPLOY_HOST:/var/www/blog-admin/
    - ssh $DEPLOY_USER@$DEPLOY_HOST "sudo systemctl restart nginx"
  only:
    - master
    - main
  dependencies:
    - build
```

需要在 GitLab 项目设置中配置以下变量：

- `SSH_PRIVATE_KEY`: SSH 私钥
- `DEPLOY_HOST`: 服务器地址
- `DEPLOY_USER`: 服务器用户名

---

## 部署检查清单

### 构建前检查

- [ ] 更新 `.env.production` 配置
- [ ] 修改 API 地址
- [ ] 检查 `vite.config.js` 的 base 配置
- [ ] 确认所有依赖已安装
- [ ] 运行本地构建测试

### 部署前检查

- [ ] 备份当前生产环境
- [ ] 确认服务器磁盘空间充足
- [ ] 准备回滚方案
- [ ] 通知相关人员

### 部署后检查

- [ ] 检查首页能否正常访问
- [ ] 检查登录功能
- [ ] 检查 API 调用是否正常
- [ ] 检查静态资源加载
- [ ] 检查路由跳转
- [ ] 检查浏览器控制台无报错
- [ ] 测试核心功能
- [ ] 检查性能指标

---

## 常见问题

### Q: 部署后刷新页面出现 404

**A**: Nginx 未正确配置 SPA 路由，确保添加：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### Q: API 请求跨域错误

**A**: 检查以下几点：

1. 后端是否配置了 CORS
2. Nginx 代理配置是否正确
3. `.env.production` 中的 API 地址是否正确

### Q: 静态资源 404

**A**: 检查：

1. `vite.config.js` 中的 `base` 配置
2. 服务器目录权限
3. Nginx root 配置

### Q: 页面样式丢失

**A**: 可能原因：

1. 静态资源路径错误
2. CDN 配置问题
3. 浏览器缓存，尝试强制刷新

### Q: Docker 容器无法访问

**A**: 检查：

1. 端口映射是否正确
2. 防火墙规则
3. 容器是否正常运行：`docker ps`

---

## 性能优化建议

### 1. 启用 HTTP/2

在 Nginx 配置中启用 HTTP/2：

```nginx
listen 443 ssl http2;
```

### 2. 开启 Brotli 压缩

比 gzip 压缩率更高：

```bash
# 安装 Brotli 模块
sudo apt-get install nginx-module-brotli
```

配置：

```nginx
brotli on;
brotli_comp_level 6;
brotli_types text/plain text/css application/json application/javascript text/xml application/xml;
```

### 3. 使用 CDN

将静态资源托管到 CDN，加快全球访问速度。

### 4. 配置浏览器缓存

根据资源类型设置合理的缓存时间。

### 5. 图片优化

- 使用 WebP 格式
- 压缩图片大小
- 使用图片 CDN

---

## 监控与日志

### Nginx 访问日志

```bash
# 查看访问日志
tail -f /var/log/nginx/access.log

# 查看错误日志
tail -f /var/log/nginx/error.log
```

### 性能监控

推荐使用：

- Google Analytics
- 百度统计
- 阿里云 ARMS
- Sentry（错误监控）

---

## 回滚方案

### 方案一：保留历史版本

```bash
# 部署前备份
cp -r /var/www/blog-admin /var/www/blog-admin.backup.$(date +%Y%m%d%H%M%S)

# 回滚
rm -rf /var/www/blog-admin
mv /var/www/blog-admin.backup.YYYYMMDDHHMMSS /var/www/blog-admin
sudo systemctl restart nginx
```

### 方案二：Git 版本管理

在服务器上使用 Git 管理部署：

```bash
cd /var/www/blog-admin
git log --oneline
git reset --hard <commit-hash>
sudo systemctl restart nginx
```

---

## 联系与支持

- 项目文档：[QUICKSTART.md](./QUICKSTART.md)
- 测试清单：[TEST_CHECKLIST.md](./TEST_CHECKLIST.md)
- 问题反馈：提交 Issue

---

**部署日期**: ___________

**部署人员**: ___________

**版本号**: ___________
