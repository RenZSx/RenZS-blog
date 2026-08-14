# 后端接口集成说明

## 后端配置信息

### 服务地址
- **开发环境**: `http://localhost:8088`
- **API 前缀**: `/api`
- **完整示例**: `http://localhost:8088/api/login`

### 登录接口

#### 请求
```
POST /api/login
Content-Type: application/json

{
  "username": "用户名",
  "password": "密码"
}
```

#### 响应
```json
{
  "code": 200,
  "flag": true,
  "message": "登录成功",
  "data": {
    "tokenName": "Authorization",
    "tokenValue": "uuid-token-string",
    "tokenTimeout": 7200,
    "userInfo": {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "avatar": "头像URL",
      "intro": "简介",
      "webSite": "网站",
      "articleCount": 10,
      "categoryCount": 5,
      "tagCount": 8
    }
  }
}
```

## 前端配置

### 1. Vite 配置 (`vite.config.js`)
```javascript
const baseUrl = 'http://localhost:8088' // 博客后端接口端口

server: {
  port: 80,
  proxy: {
    '/api': {
      target: baseUrl,
      changeOrigin: true,
      rewrite: (p) => p.replace(/^\/api/, '')
    }
  }
}
```

### 2. 环境变量 (`.env.development`)
```
VITE_APP_TITLE = 博客后台管理系统
VITE_APP_BASE_API = '/api'
```

### 3. API 调用 (`src/api/login.js`)
```javascript
export function login(username, password) {
  return request({
    url: '/login',  // 实际请求: /api/login
    method: 'post',
    data: { username, password }
  })
}
```

### 4. Store 处理 (`src/store/modules/user.js`)
```javascript
login(userInfo) {
  return new Promise((resolve, reject) => {
    login(username, password).then(res => {
      // res = { tokenName, tokenValue, userInfo }
      const token = res.tokenValue
      setToken(token)
      this.token = token
      resolve()
    })
  })
}
```

## 响应拦截器处理

### request.js 配置
```javascript
// 响应拦截
service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = res.data.message || res.data.msg || '操作失败'
  
  if (code === 200) {
    // 返回 data 字段内容
    return Promise.resolve(res.data.data || res.data)
  } else if (code === 500) {
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
  // ... 其他状态码处理
})
```

## 鉴权机制

### Sa-Token 配置
后端使用 Sa-Token 进行鉴权:

1. **Cookie 模式** (Web 端推荐)
   - 后端自动设置 Cookie
   - 前端无需手动处理
   - 浏览器自动携带

2. **Header 模式** (App 端)
   - 前端存储 tokenValue
   - 请求头携带: `Authorization: Bearer {tokenValue}`

### 前端 Token 处理
```javascript
// 请求拦截器自动添加 Token
service.interceptors.request.use(config => {
  if (getToken() && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken()
  }
  return config
})
```

## 常见问题

### 1. 登录提示 500 错误
**原因**: 后端服务未启动或端口错误
**解决**:
- 确认后端运行在 8088 端口
- 检查控制台网络请求是否正确
- 查看后端日志

### 2. 跨域问题
**原因**: 开发环境未配置代理
**解决**:
- 已配置 Vite 代理,无需后端 CORS
- 生产环境需要后端配置 CORS

### 3. Token 无效
**原因**: Token 过期或未正确存储
**解决**:
- 检查 localStorage 中的 token
- 确认 Authorization Header 格式正确
- 后端检查 Sa-Token 配置

### 4. 获取用户信息失败
**原因**: 接口路径或返回格式不匹配
**解决**:
- 检查 `/getInfo` 接口是否存在
- 确认返回的用户信息结构

## 接口测试

### 使用 curl 测试登录
```bash
curl -X POST http://localhost:8088/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### 预期响应
```json
{
  "code": 200,
  "flag": true,
  "data": {
    "tokenValue": "...",
    "userInfo": { ... }
  }
}
```

## 其他博客接口

### 文章相关
- `GET /admin/articles` - 文章列表
- `GET /admin/articles/{id}` - 文章详情
- `POST /admin/articles` - 新增文章
- `PUT /admin/articles` - 更新文章
- `DELETE /admin/articles` - 删除文章

### 分类标签
- `GET /admin/categories` - 分类列表
- `GET /admin/tags` - 标签列表

### 评论留言
- `GET /admin/comments` - 评论列表
- `GET /admin/messages` - 留言列表

### 用户权限
- `GET /admin/users` - 用户列表
- `GET /admin/roles` - 角色列表
- `GET /admin/menus` - 菜单列表

**注意**: 所有管理接口都需要在请求头携带 Token

## 启动顺序

1. **启动后端服务**
```bash
cd D:\桌面\blog-master\blog-satoken\renzs-blog-satoken
# 运行 Spring Boot 项目
# 确认启动在 8088 端口
```

2. **启动前端服务**
```bash
cd D:\桌面\blog-master\blog-satoken\blog-front\blog-admin-Vue3
npm run dev
```

3. **访问登录页面**
```
http://localhost:80
```

4. **登录测试**
- 输入用户名和密码
- 点击登录按钮
- 查看控制台网络请求
- 确认跳转到首页

---

**配置完成日期**: 2026-08-14  
**配置状态**: ✅ 完成  
**测试状态**: ⏳ 待测试
