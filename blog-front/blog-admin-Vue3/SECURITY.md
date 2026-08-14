# 安全性检查清单

## 项目安全概览
本文档列出了博客后台管理系统的安全措施、安全隐患和改进建议。

## 已实现的安全措施

### 1. 身份认证机制
✅ **Token 认证**
- 使用 JWT (JSON Web Token) 进行用户认证
- Token 存储在客户端（通过 js-cookie）
- 请求头携带 Authorization: Bearer {token}

```javascript
// utils/auth.js
import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}
```

### 2. 请求拦截器
✅ **统一的请求拦截**
- 自动在请求头添加 Token
- 统一处理 401 未授权响应
- 统一错误处理

```javascript
// utils/request.js 示例
import axios from 'axios'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      // 未授权，跳转登录
      router.push('/login')
    }
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)
```

### 3. 权限控制
✅ **路由权限控制**
- 路由守卫验证用户登录状态
- 基于角色的访问控制（RBAC）

```javascript
// router/index.js 示例
router.beforeEach((to, from, next) => {
  const token = getToken()
  
  if (to.meta.requireAuth && !token) {
    next('/login')
  } else {
    next()
  }
})
```

### 4. 密码加密
✅ **使用 JSEncrypt 进行 RSA 加密**
- 登录密码在前端加密后传输
- 使用公钥加密，后端私钥解密

```javascript
import JSEncrypt from 'jsencrypt'

export function encryptPassword(password, publicKey) {
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(publicKey)
  return encrypt.encrypt(password)
}
```

---

## 需要注意的安全点

### 1. XSS（跨站脚本攻击）防护

#### 风险等级：🔴 高

#### 当前状况
- Vue 3 默认对模板插值进行 HTML 转义
- 使用 `v-html` 指令存在 XSS 风险

#### 潜在风险点
```vue
<!-- ⚠️ 危险：BlogEditor 组件使用 v-html -->
<div v-html="innerText" />

<!-- ⚠️ 危险：文章内容渲染 -->
<div v-html="article.content" />
```

#### 防护措施

**方案 1：内容过滤（推荐）**
```bash
npm install dompurify
```

```javascript
import DOMPurify from 'dompurify'

// 在渲染前清理 HTML
const sanitizeHtml = (html) => {
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'h1', 'h2', 'h3', 'img', 'a'],
    ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class']
  })
}

const safeContent = computed(() => {
  return sanitizeHtml(article.value.content)
})
```

**方案 2：CSP（内容安全策略）**
```html
<!-- index.html -->
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; 
               script-src 'self' 'unsafe-inline' 'unsafe-eval'; 
               style-src 'self' 'unsafe-inline';">
```

**方案 3：禁用危险标签**
```javascript
// 评论、留言等用户输入内容，禁止使用 v-html
// 使用纯文本显示
<div>{{ comment.content }}</div>
```

**行动建议：**
- [ ] 安装 DOMPurify 并在所有使用 v-html 的地方应用
- [ ] 审查所有用户可输入内容的渲染方式
- [ ] 配置 CSP 策略
- [ ] 对富文本编辑器输出进行白名单过滤

---

### 2. CSRF（跨站请求伪造）防护

#### 风险等级：🟡 中

#### 当前状况
- 使用 Token 认证机制提供了一定防护
- 未实现专门的 CSRF Token

#### 防护措施

**方案 1：双重 Cookie 验证**
```javascript
// request.js
service.interceptors.request.use(config => {
  const token = getToken()
  const csrfToken = Cookies.get('XSRF-TOKEN')
  
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }
  
  return config
})
```

**方案 2：验证 Referer**
```javascript
// 后端验证（示例）
if (request.getHeader('Referer').startsWith('https://yourdomain.com')) {
  // 允许请求
}
```

**方案 3：SameSite Cookie 属性**
```javascript
// 设置 Cookie 时
Cookies.set(TokenKey, token, { 
  sameSite: 'strict',
  secure: true // HTTPS only
})
```

**行动建议：**
- [ ] 后端实现 CSRF Token 机制
- [ ] 前端配合传递 CSRF Token
- [ ] 重要操作添加二次确认

---

### 3. SQL 注入防护（后端责任）

#### 风险等级：🔴 高

#### 前端职责
虽然 SQL 注入主要由后端防护，但前端也应承担基本责任：

**输入验证**
```javascript
// 验证搜索关键词
const validateKeywords = (keywords) => {
  // 限制长度
  if (keywords.length > 100) {
    return false
  }
  
  // 过滤危险字符
  const dangerousChars = /[';--\/\*]/
  if (dangerousChars.test(keywords)) {
    ElMessage.warning('搜索关键词包含非法字符')
    return false
  }
  
  return true
}

const handleQuery = () => {
  if (!validateKeywords(queryParams.keywords)) {
    return
  }
  getList()
}
```

**行动建议：**
- [ ] 所有用户输入进行长度限制
- [ ] 过滤特殊字符
- [ ] 使用白名单验证
- [ ] 确保后端使用参数化查询

---

### 4. 敏感信息处理

#### 风险等级：🔴 高

#### 潜在风险

**❌ 不要在前端代码中硬编码敏感信息**
```javascript
// ❌ 错误示例
const API_KEY = 'sk-1234567890abcdef'
const SECRET_KEY = 'my-secret-key'
const ADMIN_PASSWORD = 'admin123'
```

**❌ 不要在控制台输出敏感信息**
```javascript
// ❌ 错误示例
console.log('用户 Token:', token)
console.log('用户信息:', userInfo)
```

**❌ 不要在 localStorage 存储敏感信息**
```javascript
// ❌ 错误示例
localStorage.setItem('password', password)
localStorage.setItem('creditCard', cardNumber)
```

#### 安全实践

**✅ 使用环境变量**
```javascript
// .env.production
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_APP_KEY=public-key-only

// 代码中使用
const apiUrl = import.meta.env.VITE_API_BASE_URL
```

**✅ 清理控制台日志**
```javascript
// vite.config.js
export default defineConfig({
  build: {
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,  // 生产环境移除 console
        drop_debugger: true
      }
    }
  }
})
```

**✅ 敏感数据脱敏显示**
```javascript
// 手机号脱敏
const maskPhone = (phone) => {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 邮箱脱敏
const maskEmail = (email) => {
  return email.replace(/(.{2}).*@/, '$1***@')
}
```

**行动建议：**
- [ ] 审查代码中的硬编码敏感信息
- [ ] 配置生产环境移除 console
- [ ] 敏感数据脱敏显示
- [ ] 定期轮换 Token

---

### 5. 文件上传安全

#### 风险等级：🟡 中

#### 安全措施

**文件类型验证**
```javascript
const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const maxSize = 5 * 1024 * 1024 // 5MB

const beforeUpload = (file) => {
  // 验证文件类型
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只能上传图片文件（JPG/PNG/GIF/WebP）')
    return false
  }
  
  // 验证文件大小
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  
  return true
}
```

**文件扩展名验证**
```javascript
const validateFileExtension = (filename) => {
  const allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp']
  const ext = filename.substring(filename.lastIndexOf('.')).toLowerCase()
  return allowedExtensions.includes(ext)
}
```

**行动建议：**
- [ ] 实现前端文件类型验证
- [ ] 限制文件大小
- [ ] 后端验证文件真实类型（MIME type）
- [ ] 文件重命名，避免路径遍历

---

### 6. 点击劫持（Clickjacking）防护

#### 风险等级：🟡 中

#### 防护措施

**方案 1：X-Frame-Options（后端设置）**
```
X-Frame-Options: DENY
X-Frame-Options: SAMEORIGIN
```

**方案 2：CSP frame-ancestors**
```html
<meta http-equiv="Content-Security-Policy" 
      content="frame-ancestors 'none';">
```

**方案 3：JavaScript 防护**
```javascript
// 防止被嵌入 iframe
if (window.top !== window.self) {
  window.top.location = window.self.location
}
```

**行动建议：**
- [ ] 后端设置 X-Frame-Options 响应头
- [ ] 配置 CSP frame-ancestors
- [ ] 敏感页面添加 JavaScript 防护

---

## 依赖安全

### 1. 依赖漏洞扫描

**定期扫描依赖漏洞**
```bash
# 使用 npm audit
npm audit

# 查看详细信息
npm audit --json

# 自动修复
npm audit fix

# 强制修复（可能破坏兼容性）
npm audit fix --force
```

**使用 Snyk 扫描**
```bash
# 安装 Snyk
npm install -g snyk

# 登录
snyk auth

# 扫描项目
snyk test

# 监控项目
snyk monitor
```

### 2. 依赖版本管理

**锁定依赖版本**
- 使用 `package-lock.json` 锁定确切版本
- 谨慎使用 `^` 和 `~` 版本范围

**定期更新依赖**
```bash
# 检查过时的包
npm outdated

# 更新到安全版本
npm update

# 更新到最新版本（谨慎）
npm install package@latest
```

### 3. 当前依赖安全状态

**建议更新的依赖：**
- 定期检查 Element Plus、Vue、Axios 等核心依赖的安全更新
- 关注 CVE（通用漏洞披露）公告

**行动建议：**
- [ ] 每月运行 `npm audit` 检查漏洞
- [ ] 订阅依赖库的安全公告
- [ ] 建立依赖更新流程
- [ ] 配置 CI/CD 自动安全扫描

---

## 网络安全

### 1. HTTPS

#### 必须项
- 生产环境必须使用 HTTPS
- 强制 HTTP 重定向到 HTTPS
- 使用有效的 SSL 证书

```javascript
// 检测是否使用 HTTPS
if (location.protocol !== 'https:' && process.env.NODE_ENV === 'production') {
  location.href = 'https:' + location.href.substring(location.protocol.length)
}
```

### 2. API 安全

**请求频率限制（后端实现）**
- 限制单个 IP 的请求频率
- 防止暴力破解和 DDoS 攻击

**前端配合：**
```javascript
// 防止重复提交
const submitting = ref(false)

const handleSubmit = async () => {
  if (submitting.value) {
    ElMessage.warning('请勿重复提交')
    return
  }
  
  submitting.value = true
  try {
    await saveData()
  } finally {
    submitting.value = false
  }
}
```

**行动建议：**
- [ ] 生产环境配置 HTTPS
- [ ] 实现请求防重复提交
- [ ] 敏感操作添加验证码
- [ ] 配置 API 速率限制

---

## 安全开发实践

### 1. 代码审查清单

**每次提交前检查：**
- [ ] 没有硬编码的密码、密钥、Token
- [ ] 没有泄露的敏感信息
- [ ] 用户输入已验证
- [ ] 使用 v-html 的地方已过滤
- [ ] 文件上传已验证类型和大小
- [ ] 敏感操作有二次确认

### 2. 安全测试

**手动测试：**
- XSS 测试：在输入框输入 `<script>alert('XSS')</script>`
- SQL 注入测试：输入 `' OR '1'='1`
- 路径遍历测试：输入 `../../etc/passwd`

**自动化测试：**
```bash
# 使用 OWASP ZAP 进行安全扫描
docker run -t owasp/zap2docker-stable zap-baseline.py -t http://localhost:3000
```

### 3. 日志和监控

**前端错误监控**
```javascript
// 全局错误处理
window.addEventListener('error', (event) => {
  // 上报错误到监控系统
  reportError({
    message: event.message,
    source: event.filename,
    lineno: event.lineno,
    colno: event.colno,
    stack: event.error?.stack
  })
})

// Vue 错误处理
app.config.errorHandler = (err, instance, info) => {
  reportError({
    message: err.message,
    stack: err.stack,
    info: info
  })
}
```

**行动建议：**
- [ ] 配置前端错误监控（如 Sentry）
- [ ] 记录关键操作日志
- [ ] 配置安全告警

---

## 安全检查时间表

### 日常检查（开发中）
- 代码提交前安全审查
- 避免硬编码敏感信息

### 每周检查
- 审查新增的用户输入处理
- 检查依赖更新

### 每月检查
- 运行 `npm audit`
- 检查安全漏洞公告
- 更新依赖到安全版本

### 每季度检查
- 完整的安全审计
- 渗透测试
- 更新安全策略

---

## 应急响应

### 发现安全漏洞时的处理流程

1. **评估影响范围**
   - 确定漏洞类型和严重程度
   - 评估受影响的用户和数据

2. **临时缓解措施**
   - 关闭受影响的功能
   - 增强监控和日志

3. **修复漏洞**
   - 开发补丁
   - 进行安全测试
   - 部署修复版本

4. **事后总结**
   - 分析漏洞产生原因
   - 改进开发流程
   - 更新安全培训

---

## 参考资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Vue Security Best Practices](https://vuejs.org/guide/best-practices/security.html)
- [MDN Web Security](https://developer.mozilla.org/en-US/docs/Web/Security)
- [npm Security Best Practices](https://docs.npmjs.com/packages-and-modules/securing-your-code)
