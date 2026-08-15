# blog-app (uniapp + Vue 3)

基于 Sa-Token 纯 Header 鉴权的博客 App,使用 uniapp + HBuilderX 打包成安卓 APK。

## 技术栈

- **uniapp** + Vue 3 (`<script setup>`)
- **Pinia** 状态管理
- **uni.request** 封装(Authorization Bearer Header 鉴权)
- **HBuilderX** 打包工具(云打包)

## 后端对接

| 项 | 值 |
|---|---|
| 基地址 | `http://localhost:8088` |
| 鉴权方式 | `Authorization: Bearer {token}` Header |
| token 存储 | `uni.storage` key=`token` |
| 业务码 40001 | 未登录,自动清 token 并跳登录页 |

配置位于 `utils/config.js`,后续要切换服务器只改这一行。

## 项目结构

```
blog-app/
├── pages.json              # 路由 + TabBar
├── manifest.json           # App 打包配置
├── App.vue                 # 全局入口(启动时校验 token)
├── main.js                 # Vue 实例化(pinia 安装)
├── uni.scss                # 全局 SCSS 变量
├── package.json
├── pages/                  # 9 个页面
│   ├── index/index.vue           # 首页(文章列表)
│   ├── category/category.vue     # 分类
│   ├── notice/notice.vue         # 通知(占位)
│   ├── profile/profile.vue       # 个人中心
│   ├── login/login.vue           # 登录
│   ├── register/register.vue     # 注册
│   ├── article/article.vue       # 文章详情
│   ├── article/comments.vue      # 评论
│   └── settings/settings.vue     # 设置(退出登录)
├── api/
│   ├── request.js          # 核心:uni.request + Header 注入 + 40001 处理
│   ├── user.js             # 登录/注册/退出/当前用户
│   ├── article.js          # 文章列表/详情/点赞/分类
│   └── comment.js          # 评论列表/发表/点赞
├── store/
│   └── user.js             # Pinia store(token + userInfo)
├── utils/
│   ├── config.js           # BASE_URL / 常量
│   ├── auth.js             # token 存取
│   └── markdown.js         # 简易 markdown 渲染
└── static/                 # 静态资源(图标/启动图)
```

## 使用方法

### 1. 用 HBuilderX 打开

1. 下载并安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)(选 Standard 版即可)
2. 文件 → 打开目录 → 选择 `D:\桌面\blog-master\blog-satoken\blog-front\blog-app`
3. HBuilderX 会提示绑定 appid → 跟随向导生成(免费)

### 2. 本地调试

#### H5 调试(最快)
- 菜单: 运行 → 运行到浏览器 → Chrome
- 浏览器打开,可在 Console 看到日志

#### 安卓真机调试
- 安卓手机开启 USB 调试,USB 连电脑
- 菜单: 运行 → 运行到手机或模拟器 → 运行到 Android App 基座
- HBuilderX 会安装"HBuilder 基座 App"到手机,实时同步代码

### 3. 打包 APK

- 菜单: 发行 → 原生 App-云打包
- 勾选 Android, 选择"使用 DCloud 公用证书"
- 提交打包(免费,等待 3-10 分钟)
- 下载 APK,安装到手机

## 测试账号

使用 blog-vue3 / admin 已有的账号即可登录,token 在三端互不影响(各自存储)。

## 已知限制 (TODO)

- ❌ 文章详情 markdown 渲染较简陋(建议接入 mp-html 组件)
- ❌ 通知页未接入 WebSocket(占位)
- ❌ 分类点击未跳转文章列表(占位)
- ❌ 不支持第三方 OAuth 登录(QQ/微博/Gitee)
- ❌ 不支持文件上传(头像修改)
- ❌ 不支持主题切换/暗色模式

这些可按需在 MVP 跑通后增量补充。

## 常见问题

### Q: HBuilderX 打开提示"无 appid"?
A: 顶部菜单 → 工具 → 真机运行 → 重新获取 manifest.json appid

### Q: 安卓 9+ 网络请求失败?
A: 已在 manifest.json 配置 `usesCleartextTraffic: true` 允许 HTTP 明文,不需要额外处理。

### Q: H5 调试时跨域?
A: H5 调试时浏览器有跨域限制,后端 CORS 已配置 `allowedOriginPatterns: *`,正常工作。如有问题,可在 manifest.json 的 h5 配置加 devServer.proxy。
