# Vue 2.6 → Vue 3 + Vite 升级指南

## 目录

1. [概述与前置条件](#1-概述与前置条件)
2. [模块0：项目脚手架](#2-模块0项目脚手架)
3. [模块1：核心基础设施](#3-模块1核心基础设施)
4. [模块2：UI框架迁移](#4-模块2ui框架迁移)
5. [模块3：全局插件迁移](#5-模块3全局插件迁移)
6. [模块4：布局组件](#6-模块4布局组件)
7. [模块5：模态框系统](#7-模块5模态框系统)
8. [模块6：首页模块](#8-模块6首页模块)
9. [模块7：文章页面模块](#9-模块7文章页面模块)
10. [模块8：评论系统](#10-模块8评论系统)
11. [模块9：其他页面](#11-模块9其他页面)
12. [模块10：第三方插件替换](#12-模块10第三方插件替换)
13. [模块11：Admin项目](#13-模块11admin项目)
14. [验证清单](#14-验证清单)

---

## 1. 概述与前置条件

### 1.1 项目基线

| 项目 | 当前版本 | 目标版本 |
|------|---------|---------|
| Vue | 2.6.11 | 3.4.x |
| Vue Router | 3.1.6 | 4.2.x |
| Vuex | 3.1.3 | → Pinia 2.x |
| Vuetify | 2.6.4 | 3.4.x |
| 构建工具 | Vue CLI 4 | Vite 5.x |
| Node.js | 14.x (legacy) | 18.x+ |

### 1.2 文件统计

- **Blog前端**: 52个 `.vue` 文件
  - 40个使用 Vuetify 组件 (77%)
  - 2个使用 ElementUI 组件
- **Admin后台**: 30个 `.vue` 文件
  - 主要使用 ElementUI

### 1.3 关键阻碍点

1. **Vuetify 2→3**: 大量破坏性变更，组件API重写
2. **无Vue 3版本的插件** (6个):
   - `vue-baberrage` (弹幕)
   - `vue-cute-timeline` (时间线)
   - `vue-live2d` (看板娘)
   - `vue-seamless-scroll` (无缝滚动)
   - `vue-social-share` (社交分享)
   - `vue-image-swipe` (图片轮播)
3. **Vue Filters**: 已移除，需转为工具函数
4. **Vue.extend**: 已移除，影响Toast插件

### 1.4 技术选型

| 功能 | Vue 2 方案 | Vue 3 方案 |
|------|-----------|-----------|
| 状态管理 | Vuex 3 | Pinia 2 |
| 路由 | Vue Router 3 | Vue Router 4 |
| UI框架 | Vuetify 2 + ElementUI 2 | Vuetify 3 + Element Plus |
| 构建工具 | Vue CLI 4 | Vite 5 |
| CSS预处理 | Sass/Stylus | Sass (推荐移除Stylus) |

---

## 2. 模块0：项目脚手架

### 2.1 创建新项目

```bash
# 使用 Vite 创建 Vue 3 项目
npm create vite@latest blog-vue3 -- --template vue

cd blog-vue3
npm install
```

### 2.2 安装核心依赖

```bash
# Vue 生态
npm install vue@latest vue-router@4 pinia

# Vuetify 3
npm install vuetify@3 @mdi/font

# Element Plus (替代 ElementUI)
npm install element-plus

# 工具库
npm install axios dayjs nprogress

# Markdown 相关
npm install markdown-it highlight.js
npm install markdown-it-emoji markdown-it-container markdown-it-footnote markdown-it-task-lists markdown-it-katex-external

# 其他
npm install animate.css clipboard tocbot

# 开发依赖
npm install -D sass @vitejs/plugin-vue vite
```

### 2.3 目录结构

```
blog-vue3/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/                 # API 请求层
│   ├── assets/              # 静态资源
│   │   ├── css/
│   │   └── js/
│   ├── components/          # 公共组件
│   │   ├── comment/
│   │   ├── layout/
│   │   ├── model/
│   │   ├── state/
│   │   └── toast/
│   ├── composables/         # 组合式函数 (新增)
│   ├── plugins/             # 插件配置
│   ├── router/              # 路由配置
│   ├── stores/              # Pinia stores (替代 Vuex)
│   ├── utils/               # 工具函数
│   ├── views/               # 页面组件
│   │   ├── about/
│   │   ├── album/
│   │   ├── archive/
│   │   ├── article/
│   │   ├── category/
│   │   ├── chat/
│   │   ├── home/
│   │   ├── link/
│   │   ├── message/
│   │   ├── tag/
│   │   ├── talk/
│   │   └── user/
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json            # 如使用 TypeScript
└── vite.config.ts
```

### 2.4 Vite 配置

**文件**: `vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 3. 模块1：核心基础设施

### 3.1 main.ts 入口文件

**风险等级**: 🔴 高

**Vue 2 代码** (`src/main.js`):
```javascript
import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import vuetify from "./plugins/vuetify";

Vue.use(ElementUI);
Vue.prototype.config = config;
Vue.filter("date", function(value) { ... });
Vue.filter("time", function(value) { ... });

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount("#app");
```

**Vue 3 代码** (`src/main.ts`):
```typescript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import vuetify from './plugins/vuetify'

// 样式
import '@mdi/font/css/materialdesignicons.css'
import 'animate.css'
import 'nprogress/nprogress.css'
import 'highlight.js/styles/atom-one-dark.css'

// 全局样式
import './assets/css/tokens.css'
import './assets/css/index.css'
import './assets/css/iconfont.css'
import './assets/css/markdown.css'

// Element Plus (按需引入)
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// Toast 插件 (Vue 3 版本)
import { useToast } from './composables/useToast'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vuetify)
app.use(ElementPlus)

// 全局属性
app.config.globalProperties.$config = config

// 挂载
app.mount('#app')
```

**迁移步骤**:

- [ ] 将 `main.js` 重命名为 `main.ts`
- [ ] 移除所有 `Vue.filter()` 调用，转为工具函数
- [ ] 移除所有 `Vue.use()` 调用，改为 `app.use()`
- [ ] 将 `Vue.prototype.xxx` 改为 `app.config.globalProperties.xxx`
- [ ] 将 Vuex Store 替换为 Pinia
- [ ] 更新路由和 Vuetify 初始化方式

### 3.2 路由配置

**风险等级**: 🟡 中

**Vue 2 代码** (`src/router/index.js`):
```javascript
import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    component: resolve => require(["../views/home/Home.vue"], resolve)
  },
  // ...
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;
```

**Vue 3 代码** (`src/router/index.ts`):
```typescript
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Home.vue')
  },
  {
    path: '/articles/:articleId',
    name: 'Article',
    component: () => import('@/views/article/Article.vue')
  },
  {
    path: '/archives',
    name: 'Archive',
    component: () => import('@/views/archive/Archive.vue'),
    meta: { title: '归档' }
  },
  {
    path: '/albums',
    name: 'Album',
    component: () => import('@/views/album/Album.vue'),
    meta: { title: '相册' }
  },
  {
    path: '/chat',
    name: 'ChatRoom',
    component: () => import('@/views/chat/ChatRoom.vue'),
    meta: { title: '聊天室' }
  },
  {
    path: '/talks',
    name: 'Talk',
    component: () => import('@/views/talk/Talk.vue'),
    meta: { title: '说说' }
  },
  {
    path: '/talks/:talkId',
    name: 'TalkInfo',
    component: () => import('@/views/talk/TalkInfo.vue'),
    meta: { title: '说说' }
  },
  {
    path: '/albums/:albumId',
    name: 'Photo',
    component: () => import('@/views/album/Photo.vue')
  },
  {
    path: '/tags',
    name: 'Tag',
    component: () => import('@/views/tag/Tag.vue'),
    meta: { title: '标签' }
  },
  {
    path: '/categories',
    name: 'Category',
    component: () => import('@/views/category/Category.vue'),
    meta: { title: '分类' }
  },
  {
    path: '/categories/:categoryId',
    name: 'CategoryArticles',
    component: () => import('@/views/article/ArticleList.vue')
  },
  {
    path: '/tags/:tagId',
    name: 'TagArticles',
    component: () => import('@/views/article/ArticleList.vue')
  },
  {
    path: '/links',
    name: 'Link',
    component: () => import('@/views/link/Link.vue'),
    meta: { title: '友链列表' }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/about/About.vue'),
    meta: { title: '关于我' }
  },
  {
    path: '/message',
    name: 'Message',
    component: () => import('@/views/message/Message.vue'),
    meta: { title: '留言板' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/User.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/oauth/login/qq',
    name: 'OAuthQQ',
    component: () => import('@/components/OauthLogin.vue')
  },
  {
    path: '/oauth/login/weibo',
    name: 'OAuthWeibo',
    component: () => import('@/components/OauthLogin.vue')
  },
  {
    path: '/oauth/login/gitee',
    name: 'OAuthGitee',
    component: () => import('@/components/OauthLogin.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  if (to.meta.title) {
    document.title = to.meta.title as string
  }
  next()
})

router.afterEach(() => {
  window.scrollTo({ top: 0, behavior: 'instant' })
  NProgress.done()
})

export default router
```

**关键变更**:

| Vue 2 | Vue 3 |
|-------|-------|
| `new VueRouter()` | `createRouter()` |
| `mode: 'history'` | `createWebHistory()` |
| `base: process.env.BASE_URL` | `import.meta.env.BASE_URL` |
| `require(['...'], resolve)` | `() => import('...')` |

### 3.3 状态管理 (Vuex → Pinia)

**风险等级**: 🔴 高

**Vue 2 代码** (`src/store/index.js`):
```javascript
import Vue from "vue";
import Vuex from "vuex";
import createPersistedState from "vuex-persistedstate";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    searchFlag: false,
    loginFlag: false,
    userId: null,
    avatar: null,
    nickname: null,
    blogInfo: {}
  },
  mutations: {
    login(state, user) { ... },
    logout(state) { ... }
  }
});
```

**Vue 3 代码** (`src/stores/user.ts`):
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const userId = ref<string | null>(null)
  const avatar = ref<string | null>(null)
  const nickname = ref<string | null>(null)
  const intro = ref<string | null>(null)
  const webSite = ref<string | null>(null)
  const email = ref<string | null>(null)
  const loginType = ref<string | null>(null)
  const articleLikeSet = ref<number[]>([])
  const commentLikeSet = ref<number[]>([])
  const talkLikeSet = ref<number[]>([])

  // Actions (替代 Mutations)
  function login(user: UserInfo) {
    userId.value = user.userInfoId
    avatar.value = user.avatar
    nickname.value = user.nickname
    intro.value = user.intro
    webSite.value = user.webSite
    email.value = user.email
    loginType.value = user.loginType
    articleLikeSet.value = user.articleLikeSet || []
    commentLikeSet.value = user.commentLikeSet || []
    talkLikeSet.value = user.talkLikeSet || []
  }

  function logout() {
    userId.value = null
    avatar.value = null
    nickname.value = null
    intro.value = null
    webSite.value = null
    email.value = null
    loginType.value = null
    articleLikeSet.value = []
    commentLikeSet.value = []
    talkLikeSet.value = []
  }

  function toggleArticleLike(articleId: number) {
    const index = articleLikeSet.value.indexOf(articleId)
    if (index > -1) {
      articleLikeSet.value.splice(index, 1)
    } else {
      articleLikeSet.value.push(articleId)
    }
  }

  return {
    // State
    userId, avatar, nickname, intro, webSite, email, loginType,
    articleLikeSet, commentLikeSet, talkLikeSet,
    // Actions
    login, logout, toggleArticleLike
  }
}, {
  persist: {
    key: 'user-store',
    storage: localStorage,
    paths: ['userId', 'avatar', 'nickname', 'intro', 'webSite', 'email', 'loginType',
            'articleLikeSet', 'commentLikeSet', 'talkLikeSet']
  }
})
```

**Vue 3 代码** (`src/stores/ui.ts`):
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const searchFlag = ref(false)
  const loginFlag = ref(false)
  const registerFlag = ref(false)
  const forgetFlag = ref(false)
  const emailFlag = ref(false)
  const drawer = ref(false)
  const loginUrl = ref('')

  function setSearchFlag(value: boolean) {
    searchFlag.value = value
  }

  function setLoginFlag(value: boolean) {
    loginFlag.value = value
  }

  function setRegisterFlag(value: boolean) {
    registerFlag.value = value
  }

  function setForgetFlag(value: boolean) {
    forgetFlag.value = value
  }

  function setEmailFlag(value: boolean) {
    emailFlag.value = value
  }

  function setDrawer(value: boolean) {
    drawer.value = value
  }

  function closeAllModals() {
    searchFlag.value = false
    loginFlag.value = false
    registerFlag.value = false
    forgetFlag.value = false
    emailFlag.value = false
  }

  return {
    searchFlag, loginFlag, registerFlag, forgetFlag, emailFlag, drawer, loginUrl,
    setSearchFlag, setLoginFlag, setRegisterFlag, setForgetFlag, setEmailFlag,
    setDrawer, closeAllModals
  }
})
```

**Vue 3 代码** (`src/stores/blogInfo.ts`):
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { BlogInfo } from '@/types/blog'

export const useBlogInfoStore = defineStore('blogInfo', () => {
  const blogInfo = ref<BlogInfo>(createDefaultBlogInfo())

  function setBlogInfo(info: BlogInfo) {
    blogInfo.value = normalizeBlogInfo(info)
  }

  return {
    blogInfo,
    setBlogInfo
  }
}, {
  persist: {
    key: 'blogInfo-store',
    storage: localStorage
  }
})
```

**安装 Pinia 持久化插件**:
```bash
npm install pinia-plugin-persistedstate
```

**迁移步骤**:

- [ ] 安装 Pinia 和持久化插件
- [ ] 创建 `stores/` 目录
- [ ] 将 Vuex modules 拆分为独立的 Pinia stores
- [ ] 将 `mutations` 转为 `actions`
- [ ] 更新组件中的 `this.$store` 调用为 `useXxxStore()`

### 3.4 API 层迁移

**风险等级**: 🟢 低

**Vue 2 代码** (`src/api/request.js`):
```javascript
import axios from "axios";
export default axios;
```

**Vue 3 代码** (`src/api/request.ts`):
```typescript
import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useToast } from '@/composables/useToast'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 可添加 token 等
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    if (data.code === 50000) {
      useToast({ type: 'error', message: '系统异常' })
    }
    return response
  },
  (error) => {
    useToast({ type: 'error', message: error.message || '网络错误' })
    return Promise.reject(error)
  }
)

export default request
```

**迁移步骤**:

- [ ] 添加 TypeScript 类型
- [ ] 更新环境变量 (`VITE_API_BASE_URL`)
- [ ] 统一错误处理

---

## 4. 模块2：UI框架迁移

### 4.1 Vuetify 2 → 3 迁移

**风险等级**: 🔴 高

**涉及文件**:
- `src/plugins/vuetify.js`
- 所有使用 Vuetify 组件的 `.vue` 文件 (40个)

**Vuetify 2 配置** (`src/plugins/vuetify.js`):
```javascript
import Vue from "vue";
import Vuetify from "vuetify/lib";

Vue.use(Vuetify);

export default new Vuetify({
  icons: { iconfont: "mdi" }
});
```

**Vuetify 3 配置** (`src/plugins/vuetify.ts`):
```typescript
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: { mdi }
  },
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1976D2',
          secondary: '#424242',
          accent: '#82B1FF',
          error: '#FF5252',
          info: '#2196F3',
          success: '#4CAF50',
          warning: '#FFC107'
        }
      },
      dark: {
        colors: {
          primary: '#2196F3',
          secondary: '#424242',
          accent: '#FF4081',
          error: '#FF5252',
          info: '#2196F3',
          success: '#4CAF50',
          warning: '#FFC107'
        }
      }
    }
  }
})

export default vuetify
```

### 4.2 Vuetify 组件变更对照表

| Vue 2 组件 | Vue 3 组件 | 变更说明 |
|-----------|-----------|---------|
| `<v-app>` | `<v-app>` | 保持不变 |
| `<v-main>` | `<v-main>` | 保持不变 |
| `<v-dialog v-model="flag">` | `<v-dialog v-model="flag">` | 保持不变 |
| `<v-btn @click>` | `<v-btn @click>` | 保持不变 |
| `<v-text-field>` | `<v-text-field>` | 保持不变 |
| `<v-card>` | `<v-card>` | 保持不变 |
| `<v-icon>mdi-close</v-icon>` | `<v-icon icon="mdi-close" />` | 推荐使用 `icon` prop |
| `<v-row>` / `<v-col>` | `<v-row>` / `<v-col>` | 保持不变 |

### 4.3 ElementUI → Element Plus

**风险等级**: 🟡 中

**涉及文件**:
- `src/main.js` (ElementUI 引入)
- 使用 ElementUI 的组件 (2个)

**安装**:
```bash
npm install element-plus
```

**配置** (`src/main.ts`):
```typescript
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

app.use(ElementPlus, { locale: zhCn })
```

**组件变更对照表**:

| ElementUI 2 | Element Plus | 变更说明 |
|-------------|--------------|---------|
| `this.$notify()` | `ElNotification()` | 需单独导入 |
| `this.$message()` | `ElMessage()` | 需单独导入 |
| `el-dialog` | `el-dialog` | API 基本兼容 |

**迁移步骤**:

- [ ] 安装 Element Plus
- [ ] 更新导入语句
- [ ] 替换 `this.$notify` 为 `ElNotification`
- [ ] 替换 `this.$message` 为 `ElMessage`

---

## 5. 模块3：全局插件迁移

### 5.1 Filters → 工具函数

**风险等级**: 🟡 中

**Vue 2 Filters** (`src/main.js`):
```javascript
Vue.filter("date", function(value) {
  return dayjs(value).format("YYYY-MM-DD");
});

Vue.filter("year", function(value) {
  return dayjs(value).format("YYYY");
});

Vue.filter("hour", function(value) {
  return dayjs(value).format("HH:mm:ss");
});

Vue.filter("time", function(value) {
  return dayjs(value).format("YYYY-MM-DD HH:mm:ss");
});

Vue.filter("num", function(value) {
  if (value >= 1000) {
    return (value / 1000).toFixed(1) + "k";
  }
  return value;
});
```

**Vue 3 工具函数** (`src/utils/filters.ts`):
```typescript
import dayjs from 'dayjs'

export function formatDate(value: string | Date): string {
  return dayjs(value).format('YYYY-MM-DD')
}

export function formatYear(value: string | Date): string {
  return dayjs(value).format('YYYY')
}

export function formatHour(value: string | Date): string {
  return dayjs(value).format('HH:mm:ss')
}

export function formatTime(value: string | Date): string {
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

export function formatNum(value: number): string {
  if (value >= 1000) {
    return (value / 1000).toFixed(1) + 'k'
  }
  return String(value)
}
```

**在组件中使用**:
```vue
<script setup lang="ts">
import { formatDate, formatTime, formatNum } from '@/utils/filters'
</script>

<template>
  <span>{{ formatDate(article.createTime) }}</span>
  <span>{{ formatNum(article.viewCount) }}</span>
</template>
```

### 5.2 Toast 插件重写

**风险等级**: 🔴 高

**Vue 2 Toast** (`src/components/toast/index.js`):
```javascript
import ToastComponent from "./Toast.vue";

const Toast = {};

Toast.install = function(Vue) {
  const ToastConstructor = Vue.extend(ToastComponent);
  const instance = new ToastConstructor();
  instance.$mount(document.createElement("div"));
  document.body.appendChild(instance.$el);

  Vue.prototype.$toast = (options, duration = 2000) => {
    instance.message = options.message;
    instance.type = options.type;
    instance.show = true;
    setTimeout(() => {
      instance.show = false;
    }, duration);
  };
};

export default Toast;
```

**Vue 3 Composable** (`src/composables/useToast.ts`):
```typescript
import { createApp, h } from 'vue'
import ToastComponent from '@/components/toast/Toast.vue'

interface ToastOptions {
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  duration?: number
}

let toastInstance: ReturnType<typeof createApp> | null = null
let toastContainer: HTMLDivElement | null = null

export function useToast(options: ToastOptions) {
  const { type, message, duration = 2000 } = options

  // 创建容器
  if (!toastContainer) {
    toastContainer = document.createElement('div')
    document.body.appendChild(toastContainer)
  }

  // 创建应用实例
  if (toastInstance) {
    toastInstance.unmount()
  }

  toastInstance = createApp({
    render() {
      return h(ToastComponent, {
        type,
        message,
        show: true
      })
    }
  })

  const instance = toastInstance.mount(toastContainer)

  // 自动关闭
  setTimeout(() => {
    if (toastInstance) {
      toastInstance.unmount()
      toastInstance = null
    }
  }, duration)
}
```

**Toast 组件** (`src/components/toast/Toast.vue`):
```vue
<template>
  <Transition name="toast-fade">
    <div v-if="show" class="toast-container" :class="`toast-${type}`">
      <span class="toast-message">{{ message }}</span>
    </div>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{
  show: boolean
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
}>()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.toast-success { background: #f0f9eb; color: #67c23a; }
.toast-error { background: #fef0f0; color: #f56c6c; }
.toast-warning { background: #fdf6ec; color: #e6a23c; }
.toast-info { background: #f4f4f5; color: #909399; }

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.3s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
}
</style>
```

### 5.3 NProgress 配置

**风险等级**: 🟢 低

NProgress 在 Vue 3 中使用方式不变，只需在路由守卫中正确引入：

```typescript
// src/router/index.ts
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

router.beforeEach((to, from, next) => {
  NProgress.start()
  next()
})

router.afterEach(() => {
  NProgress.done()
})
```

### 5.4 dayjs 配置

**风险等级**: 🟢 低

```typescript
// src/utils/dayjs.ts
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export default dayjs
```

---

## 6. 模块4：布局组件

### 6.1 App.vue

**风险等级**: 🟡 中

**Vue 2 代码**:
```vue
<template>
  <v-app id="app">
    <TopNavBar></TopNavBar>
    <SideNavBar></SideNavBar>
    <v-main>
      <router-view :key="$route.fullPath" />
    </v-main>
    <InitialPopup></InitialPopup>
    <Footer></Footer>
    <BackTop></BackTop>
    <searchModel></searchModel>
    <LoginModel></LoginModel>
    <RegisterModel></RegisterModel>
    <ForgetModel></ForgetModel>
    <EmailModel></EmailModel>
    <ChatRoom v-if="blogInfo.websiteConfig.isChatRoom"></ChatRoom>
  </v-app>
</template>

<script>
import TopNavBar from "./components/layout/TopNavBar";
// ...
export default {
  components: { TopNavBar, /* ... */ },
  computed: {
    blogInfo() {
      return this.$store.state.blogInfo;
    }
  }
}
</script>
```

**Vue 3 代码**:
```vue
<template>
  <v-app id="app">
    <TopNavBar />
    <SideNavBar />
    <v-main>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </v-main>
    <InitialPopup />
    <Footer />
    <BackTop />
    <SearchModel />
    <LoginModel />
    <RegisterModel />
    <ForgetModel />
    <EmailModel />
    <ChatRoom v-if="blogInfoStore.blogInfo.websiteConfig?.isChatRoom" />
  </v-app>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useTheme } from 'vuetify'
import TopNavBar from '@/components/layout/TopNavBar.vue'
import SideNavBar from '@/components/layout/SideNavBar.vue'
import Footer from '@/components/layout/Footer.vue'
import BackTop from '@/components/BackTop.vue'
import SearchModel from '@/components/model/SearchModel.vue'
import LoginModel from '@/components/model/LoginModel.vue'
import RegisterModel from '@/components/model/RegisterModel.vue'
import ForgetModel from '@/components/model/ForgetModel.vue'
import EmailModel from '@/components/model/EmailModel.vue'
import ChatRoom from '@/components/ChatRoom.vue'
import InitialPopup from '@/components/InitialPopup.vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getBlogInfo, reportVisitor } from '@/api/site'

const theme = useTheme()
const blogInfoStore = useBlogInfoStore()

// 初始化
onMounted(async () => {
  await getBlogInfo()
  reportVisitor()
  syncPageThemeBackground(theme.global.current.value.dark)
})

// 监听主题变化
watch(
  () => theme.global.current.value.dark,
  (isDark) => {
    syncPageThemeBackground(isDark)
  }
)

function syncPageThemeBackground(isDark: boolean) {
  const background = isDark ? '#121212' : '#ffffff'
  document.documentElement.style.backgroundColor = background
  document.body.style.backgroundColor = background
}
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
```

### 6.2 TopNavBar.vue

**风险等级**: 🟡 中

**关键变更**:
- `this.$store` → `useUIStore()`
- `this.$vuetify.theme.dark` → `useTheme().global.current.value.dark`
- `this.$router` → `useRouter()`

**Vue 3 代码示例**:
```vue
<script setup lang="ts">
import { computed } from 'vue'
import { useTheme } from 'vuetify'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'

const theme = useTheme()
const router = useRouter()
const uiStore = useUIStore()
const userStore = useUserStore()

const isDark = computed(() => theme.global.current.value.dark)

function toggleTheme() {
  theme.global.name.value = isDark.value ? 'light' : 'dark'
}

function openLogin() {
  uiStore.setLoginFlag(true)
}

function logout() {
  userStore.logout()
  router.push('/')
}
</script>
```

### 6.3 SideNavBar.vue

**风险等级**: 🟡 中

**关键变更**:
- `v-model="drawer"` → 使用 `useUIStore().drawer`
- 抽屉状态管理

### 6.4 Footer.vue

**风险等级**: 🟢 低

基本保持不变，更新样式引用即可。

---

## 7. 模块5：模态框系统

### 5.1 LoginModel.vue

**风险等级**: 🟡 中

**Vue 2 关键代码**:
```vue
<script>
export default {
  computed: {
    loginFlag: {
      set(value) {
        this.$store.commit("setLoginFlag", value);
      },
      get() {
        return this.$store.state.loginFlag;
      }
    }
  },
  methods: {
    login() {
      // ...
      this.$toast({ type: "success", message: "登录成功" });
    }
  }
}
</script>
```

**Vue 3 代码**:
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDisplay } from 'vuetify'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { login } from '@/api/user'

const { mobile } = useDisplay()
const uiStore = useUIStore()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)

const loginFlag = computed({
  get: () => uiStore.loginFlag,
  set: (value) => uiStore.setLoginFlag(value)
})

const isMobile = computed(() => mobile.value)

async function handleLogin() {
  // 验证逻辑...
  try {
    const { data } = await login({ username: username.value, password: password.value })
    if (data.flag) {
      userStore.login(data.data)
      uiStore.closeAllModals()
      useToast({ type: 'success', message: '登录成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '登录失败' })
  }
}
</script>
```

### 5.2 RegisterModel.vue

**风险等级**: 🟡 中

与 LoginModel 类似的迁移模式。

### 5.3 SearchModel.vue

**风险等级**: 🟢 低

主要更新 store 调用方式。

### 5.4 ForgetModel.vue / EmailModel.vue

**风险等级**: 🟢 低

主要更新 store 调用方式。

---

## 8. 模块6：首页模块

### 8.1 Home.vue

**风险等级**: 🔴 高

**涉及文件**:
- `src/views/home/Home.vue`
- `src/views/home/components/HomeHero.vue`
- `src/views/home/components/HomeArticleList.vue`
- `src/views/home/components/HomeSidebar.vue`
- `src/views/home/components/HomeTalkCard.vue`
- `src/views/home/components/ProfileCard.vue`
- `src/views/home/components/QuickCategoryCards.vue`

**Vue 2 关键模式**:
```vue
<script>
export default {
  data() {
    return {
      articleList: [],
      current: 1
    }
  },
  computed: {
    blogInfo() {
      return this.$store.state.blogInfo;
    }
  },
  methods: {
    infiniteHandler($state) {
      this.axios.get("/api/articles", { params: { current: this.current } })
        .then(({ data }) => {
          // ...
        });
    }
  }
}
</script>
```

**Vue 3 代码**:
```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useUserStore } from '@/stores/user'
import HomeHero from './components/HomeHero.vue'
import HomeArticleList from './components/HomeArticleList.vue'
import HomeSidebar from './components/HomeSidebar.vue'
import HomeTalkCard from './components/HomeTalkCard.vue'
import ProfileCard from './components/ProfileCard.vue'
import QuickCategoryCards from './components/QuickCategoryCards.vue'
import request from '@/api/request'
import type { Article } from '@/types/article'

const blogInfoStore = useBlogInfoStore()
const userStore = useUserStore()

const articleList = ref<Article[]>([])
const talkList = ref([])
const newestArticleList = ref([])
const newCommentsList = ref([])
const categoryList = ref([])
const current = ref(1)
const isCardLayout = ref(false)

const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig || {})

onMounted(async () => {
  await Promise.all([
    fetchArticles(),
    fetchTalks(),
    fetchNewestArticles(),
    fetchComments(),
    fetchCategories()
  ])
})

async function fetchArticles() {
  const { data } = await request.get('/api/articles', { params: { current: current.value } })
  articleList.value.push(...data.data)
  current.value++
}

// ... 其他方法
</script>
```

### 8.2 HomeArticleList.vue

**风险等级**: 🟡 中

**关键变更**:
- `vue-infinite-loading` 需替换为 Vue 3 兼容版本或自定义实现
- `.sync` 修饰符 → `v-model:propName`

**Vue 2**:
```vue
<HomeArticleList :isCardLayout.sync="isCardLayout" />
```

**Vue 3**:
```vue
<HomeArticleList v-model:isCardLayout="isCardLayout" />
```

**子组件**:
```vue
<script setup lang="ts">
const props = defineProps<{ isCardLayout: boolean }>()
const emit = defineEmits<{ 'update:isCardLayout': [value: boolean] }>()

function toggleLayout() {
  emit('update:isCardLayout', !props.isCardLayout)
}
</script>
```

---

## 9. 模块7：文章页面模块

### 9.1 Article.vue

**风险等级**: 🔴 高

**涉及文件**:
- `src/views/article/Article.vue`
- `src/views/article/ArticleList.vue`
- `src/views/article/components/ArticleContent.vue`
- `src/views/article/components/ArticleHeader.vue`
- `src/views/article/components/ArticleSidebar.vue`
- `src/views/article/components/ArticleRecommend.vue`
- `src/views/article/components/ArticleActions.vue`

**关键变更**:
- Markdown 渲染器配置
- 目录生成 (tocbot)
- 代码高亮 (highlight.js)

### 9.2 ArticleContent.vue

**风险等级**: 🟡 中

**Vue 3 代码示例**:
```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import MarkdownIt from 'markdown-it'
import highlight from 'highlight.js'
import tocbot from 'tocbot'

const route = useRoute()
const content = ref('')
const md = new MarkdownIt({
  html: true,
  highlight: (str, lang) => {
    if (lang && highlight.getLanguage(lang)) {
      return highlight.highlight(str, { language: lang }).value
    }
    return ''
  }
})

onMounted(() => {
  tocbot.init({
    tocSelector: '.toc',
    contentSelector: '.article-content',
    headingSelector: 'h1, h2, h3, h4'
  })
})

onUnmounted(() => {
  tocbot.destroy()
})
</script>
```

---

## 10. 模块8：评论系统

### 8.1 Comment.vue

**风险等级**: 🔴 高

**涉及文件**:
- `src/components/Comment.vue`
- `src/components/comment/CommentEditor.vue`
- `src/components/comment/CommentList.vue`
- `src/components/comment/CommentItem.vue`
- `src/components/comment/ReplyList.vue`
- `src/components/Reply.vue`
- `src/components/TalkComment.vue`

**Vue 2 关键模式** (`this.$set`):
```vue
<script>
export default {
  methods: {
    like(comment) {
      // Vue 2 响应式数组更新
      this.$set(comment, "likeCount", comment.likeCount + 1);
    }
  }
}
</script>
```

**Vue 3 代码**:
```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { sendCommentLike } from '@/api/comment'

const userStore = useUserStore()

async function handleLike(comment: Comment) {
  if (!userStore.userId) {
    // 打开登录框
    return
  }

  const { data } = await sendCommentLike(comment.id)
  if (data.flag) {
    // Vue 3 直接修改即可，无需 $set
    comment.likeCount += userStore.commentLikeSet.includes(comment.id) ? -1 : 1
    userStore.toggleCommentLike(comment.id)
  }
}
</script>
```

### 8.2 CommentEditor.vue

**风险等级**: 🟡 中

**关键变更**:
- `v-model` 在自定义组件上的使用方式
- Emoji 选择器

---

## 11. 模块9：其他页面

### 9.1 页面清单

| 页面 | 文件路径 | 风险等级 |
|------|---------|---------|
| 归档 | `views/archive/Archive.vue` | 🟡 中 |
| 相册 | `views/album/Album.vue` | 🟡 中 |
| 相册详情 | `views/album/Photo.vue` | 🟡 中 |
| 分类 | `views/category/Category.vue` | 🟢 低 |
| 标签 | `views/tag/Tag.vue` | 🟢 低 |
| 友链 | `views/link/Link.vue` | 🟢 低 |
| 关于 | `views/about/About.vue` | 🟢 低 |
| 留言 | `views/message/Message.vue` | 🟡 中 |
| 说说 | `views/talk/Talk.vue` | 🟡 中 |
| 说说详情 | `views/talk/TalkInfo.vue` | 🟡 中 |
| 用户中心 | `views/user/User.vue` | 🔴 高 |
| 聊天室 | `views/chat/ChatRoom.vue` | 🔴 高 |

### 9.2 User.vue

**风险等级**: 🔴 高

**关键变更**:
- `vue-avatar-cropper` 需替换为 Vue 3 兼容版本
- 用户信息表单处理

### 9.3 ChatRoom.vue

**风险等级**: 🔴 高

**关键变更**:
- WebSocket 连接管理
- `recorderx` 音频录制

---

## 12. 模块10：第三方插件替换

### 10.1 需替换插件列表

| 插件 | Vue 2 版本 | Vue 3 替代方案 | 风险 |
|------|-----------|---------------|------|
| `vue-baberrage` | 3.2.4 | 自定义实现 / `vue3-baberrage` | 🟡 |
| `vue-cute-timeline` | 1.2.8 | 自定义组件 | 🟡 |
| `vue-live2d` | 1.3.3 | `pixi-live2d-display` | 🔴 |
| `vue-seamless-scroll` | 1.1.23 | `vue3-seamless-scroll` | 🟢 |
| `vue-social-share` | 0.0.3 | 自定义分享组件 | 🟡 |
| `vue-image-swipe` | 1.0.5 | `swiper` / `vue-awesome-swiper` | 🟢 |
| `vue-infinite-loading` | 2.4.5 | `vue-infinite-loading@3` | 🟢 |
| `vue-avatar-cropper` | 1.0.9 | `vue-advanced-cropper` | 🟡 |

### 10.2 vue-baberrage 替代方案

**选项 1**: 使用 `vue3-baberrage` (如果有社区维护版本)

**选项 2**: 自定义弹幕组件

```vue
<template>
  <div class="barrage-container">
    <div
      v-for="item in barrageList"
      :key="item.id"
      class="barrage-item"
      :style="{ top: item.top, animationDuration: item.duration }"
    >
      {{ item.content }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface BarrageItem {
  id: number
  content: string
  top: string
  duration: string
}

const barrageList = ref<BarrageItem[]>([])
let barrageId = 0

function addBarrage(content: string) {
  const item: BarrageItem = {
    id: barrageId++,
    content,
    top: Math.random() * 80 + '%',
    duration: (Math.random() * 5 + 5) + 's'
  }
  barrageList.value.push(item)

  // 动画结束后移除
  setTimeout(() => {
    const index = barrageList.value.findIndex(b => b.id === item.id)
    if (index > -1) {
      barrageList.value.splice(index, 1)
    }
  }, parseFloat(item.duration) * 1000)
}

defineExpose({ addBarrage })
</script>

<style scoped>
.barrage-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.barrage-item {
  position: absolute;
  right: -100%;
  white-space: nowrap;
  animation: barrage-scroll linear forwards;
}

@keyframes barrage-scroll {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(calc(-100vw - 100%));
  }
}
</style>
```

### 10.3 vue-social-share 替代方案

```vue
<template>
  <div class="social-share">
    <button @click="shareToQQ">QQ</button>
    <button @click="shareToWeibo">微博</button>
    <button @click="shareToWechat">微信</button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  url: string
  title: string
  description: string
  image?: string
}>()

function shareToQQ() {
  const url = `https://connect.qq.com/widget/shareqq/index.html?url=${encodeURIComponent(props.url)}&title=${encodeURIComponent(props.title)}`
  window.open(url, '_blank')
}

function shareToWeibo() {
  const url = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(props.url)}&title=${encodeURIComponent(props.title)}`
  window.open(url, '_blank')
}

function shareToWechat() {
  // 显示二维码
}
</script>
```

### 10.4 vue-live2d 替代方案

使用 `pixi-live2d-display`:

```bash
npm install pixi.js pixi-live2d-display
```

```vue
<template>
  <canvas ref="canvasRef" class="live2d-canvas"></canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as PIXI from 'pixi.js'
import { Live2DModel } from 'pixi-live2d-display'

const canvasRef = ref<HTMLCanvasElement>()
let app: PIXI.Application | null = null

onMounted(async () => {
  if (!canvasRef.value) return

  app = new PIXI.Application({
    view: canvasRef.value,
    width: 300,
    height: 400,
    transparent: true
  })

  const model = await Live2DModel.from('/path/to/model.json')
  app.stage.addChild(model)
})

onUnmounted(() => {
  app?.destroy(true)
})
</script>
```

---

## 13. 模块11：Admin项目

### 11.1 Admin 项目概述

Admin 后台使用 ElementUI 作为主要 UI 框架，迁移策略与 Blog 前端类似。

**涉及文件**: 30个 `.vue` 文件

### 11.2 迁移步骤

1. **创建新项目**
   ```bash
   npm create vite@latest admin-vue3 -- --template vue-ts
   ```

2. **安装依赖**
   ```bash
   npm install element-plus vue-router@4 pinia
   ```

3. **迁移组件**
   - 按模块逐步迁移
   - 优先迁移核心功能页面

### 11.3 Admin 特有组件

| 组件 | 迁移说明 |
|------|---------|
| 富文本编辑器 | 替换为 `@wangeditor/editor-for-vue` |
| 图表 | 替换为 `echarts` + `vue-echarts` |
| 表格 | Element Plus 内置 |

---

## 14. 验证清单

### 14.1 功能验证

#### 核心功能
- [ ] 首页加载正常
- [ ] 文章列表分页正常
- [ ] 文章详情页渲染正常
- [ ] 评论功能正常
- [ ] 登录/注册流程正常
- [ ] 搜索功能正常
- [ ] 主题切换正常

#### 页面验证
- [ ] 首页 (`/`)
- [ ] 文章详情 (`/articles/:id`)
- [ ] 归档 (`/archives`)
- [ ] 相册 (`/albums`)
- [ ] 相册详情 (`/albums/:id`)
- [ ] 分类 (`/categories`)
- [ ] 分类文章 (`/categories/:id`)
- [ ] 标签 (`/tags`)
- [ ] 标签文章 (`/tags/:id`)
- [ ] 友链 (`/links`)
- [ ] 关于 (`/about`)
- [ ] 留言 (`/message`)
- [ ] 说说 (`/talks`)
- [ ] 说说详情 (`/talks/:id`)
- [ ] 用户中心 (`/user`)
- [ ] 聊天室 (`/chat`)
- [ ] OAuth 登录 (`/oauth/login/*`)

### 14.2 组件验证

#### 布局组件
- [ ] TopNavBar 显示正常
- [ ] SideNavBar 显示正常
- [ ] Footer 显示正常
- [ ] BackTop 功能正常

#### 模态框
- [ ] LoginModel 打开/关闭正常
- [ ] RegisterModel 打开/关闭正常
- [ ] SearchModel 打开/关闭正常
- [ ] ForgetModel 打开/关闭正常
- [ ] EmailModel 打开/关闭正常

#### 评论组件
- [ ] CommentEditor 输入正常
- [ ] CommentList 渲染正常
- [ ] CommentItem 显示正常
- [ ] ReplyList 显示正常

### 14.3 API 验证

- [ ] 文章 API
- [ ] 用户 API
- [ ] 评论 API
- [ ] 说说 API
- [ ] 相册 API
- [ ] 其他 API

### 14.4 性能验证

- [ ] 首屏加载时间 < 3s
- [ ] 路由切换流畅
- [ ] 大列表滚动流畅
- [ ] 内存占用正常

### 14.5 兼容性验证

- [ ] Chrome 最新版
- [ ] Firefox 最新版
- [ ] Safari 最新版
- [ ] Edge 最新版
- [ ] 移动端 Chrome
- [ ] 移动端 Safari

---

## 附录

### A. Vue 2 → Vue 3 破坏性变更速查

| 变更项 | Vue 2 | Vue 3 |
|--------|-------|-------|
| 全局 API | `new Vue()` | `createApp()` |
| 插件注册 | `Vue.use()` | `app.use()` |
| 全局属性 | `Vue.prototype.xxx` | `app.config.globalProperties.xxx` |
| 过滤器 | `Vue.filter()` | 已移除，使用方法调用 |
| 事件 API | `$on`, `$off`, `$once` | 已移除，使用外部库 |
| 响应式 | `Vue.set` / `this.$set` | 直接赋值即可 |
| v-model | 单个 v-model | 支持多个 v-model |
| .sync | `:prop.sync` | `v-model:prop` |
| 生命周期 | `beforeDestroy` | `beforeUnmount` |
| 生命周期 | `destroyed` | `unmounted` |
| 函数式组件 | `functional: true` | 移除，使用普通函数 |
| 异步组件 | `() => import()` | `defineAsyncComponent()` |

### B. 常见问题解决

#### Q1: Vuetify 组件样式不生效
确保在 `main.ts` 中正确导入样式：
```typescript
import 'vuetify/styles'
```

#### Q2: Pinia 持久化不工作
检查 `pinia-plugin-persistedstate` 配置是否正确。

#### Q3: 路由守卫中无法访问 Pinia Store
在路由文件中，确保在 `router.afterEach` 等回调中使用 Store。

### C. 参考资源

- [Vue 3 官方迁移指南](https://v3-migration.vuejs.org/)
- [Vuetify 3 迁移指南](https://vuetifyjs.com/en/getting-started/upgrade-guide/)
- [Pinia 官方文档](https://pinia.vuejs.org/)
- [Vue Router 4 迁移指南](https://router.vuejs.org/guide/migration/)
- [Element Plus 文档](https://element-plus.org/)
- [Vite 官方文档](https://vitejs.dev/)
