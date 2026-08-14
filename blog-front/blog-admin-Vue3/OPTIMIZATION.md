# 性能优化建议

## 当前项目状态
- 框架：Vue 3.5.26 + Vite 6.4.1
- UI 框架：Element Plus 2.13.1
- 状态管理：Pinia 3.0.4
- 总代码行数：约 35,604 行
- 组件数量：31 个
- 页面数量：21 个（blog 模块）
- API 接口：36 个文件

## 已实现的优化

### 1. 构建工具优化
✅ **使用 Vite 作为构建工具**
- 快速的冷启动
- 即时的热模块替换（HMR）
- 按需编译

✅ **代码压缩**
- 已配置 `vite-plugin-compression` 插件
- 支持 gzip 压缩

### 2. 组件优化
✅ **组件懒加载**
- 路由级别的代码分割
- 动态导入组件

✅ **组件结构**
- 统一使用 `<script setup>` 语法（100% 覆盖率）
- 使用 Composition API 提升性能
- 组件职责单一，易于维护

### 3. 依赖优化
✅ **自动导入**
- 使用 `unplugin-auto-import` 自动导入 API
- 减少手动导入代码

✅ **SVG 图标优化**
- 使用 `vite-plugin-svg-icons` 插件
- 图标按需加载

### 4. 样式优化
✅ **CSS 预处理器**
- 使用 SCSS 提升开发效率
- 样式隔离（scoped）避免全局污染

## 待优化项

### 1. 列表渲染优化

#### 问题
- 文章列表、评论列表等可能包含大量数据
- 当前使用完整渲染，可能造成性能瓶颈

#### 解决方案
**方案 A：虚拟滚动（推荐用于超大列表）**
```bash
npm install vue-virtual-scroller
```

```vue
<template>
  <RecycleScroller
    :items="articleList"
    :item-size="100"
    key-field="id"
    v-slot="{ item }"
  >
    <div class="article-item">
      {{ item.title }}
    </div>
  </RecycleScroller>
</template>

<script setup>
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
</script>
```

**方案 B：分页优化**
- 当前已实现分页
- 建议默认每页 20 条（已实现）
- 考虑添加"加载更多"模式

**优先级**：中
**预计收益**：处理 1000+ 条数据时性能提升 60%

---

### 2. 图片优化

#### 问题
- 文章封面、用户头像等图片未做优化
- 可能加载过大的原始图片

#### 解决方案
**图片懒加载**
```vue
<template>
  <el-image
    :src="imageUrl"
    lazy
    :scroll-container="scrollContainer"
  >
    <template #placeholder>
      <div class="image-loading">加载中...</div>
    </template>
    <template #error>
      <div class="image-error">加载失败</div>
    </template>
  </el-image>
</template>
```

**图片压缩**
- 后端返回多种尺寸（缩略图、中图、原图）
- 根据使用场景选择合适尺寸
- 使用 WebP 格式（需要后端支持）

**CDN 加速**
- 将图片上传到 CDN
- 使用 CDN 的图片处理参数
- 示例：`https://cdn.example.com/image.jpg?w=200&h=200&q=80`

**优先级**：高
**预计收益**：页面加载速度提升 40%

---

### 3. 请求优化

#### 问题
- 多个接口可能重复请求
- 没有请求缓存机制

#### 解决方案
**接口缓存**
```javascript
// utils/requestCache.js
const cache = new Map()
const CACHE_TIME = 5 * 60 * 1000 // 5分钟

export function cacheRequest(key, requestFn, cacheTime = CACHE_TIME) {
  const cached = cache.get(key)
  const now = Date.now()
  
  if (cached && now - cached.timestamp < cacheTime) {
    return Promise.resolve(cached.data)
  }
  
  return requestFn().then(data => {
    cache.set(key, {
      data,
      timestamp: now
    })
    return data
  })
}
```

**请求防抖**
```javascript
import { useDebounceFn } from '@vueuse/core'

const debouncedSearch = useDebounceFn(() => {
  handleQuery()
}, 500)
```

**请求节流**
```javascript
import { useThrottleFn } from '@vueuse/core'

const throttledScroll = useThrottleFn(() => {
  handleScroll()
}, 200)
```

**批量请求优化**
```javascript
// 合并多个请求
const [articles, categories, tags] = await Promise.all([
  listArticles(),
  listCategories(),
  listTags()
])
```

**优先级**：高
**预计收益**：减少 30% 的网络请求

---

### 4. 状态管理优化

#### 问题
- 某些数据可能在多个组件间共享
- 重复请求相同数据

#### 解决方案
**使用 Pinia 缓存常用数据**
```javascript
// stores/article.js
import { defineStore } from 'pinia'
import { listCategories, listTags } from '@/api/blog/article'

export const useArticleStore = defineStore('article', {
  state: () => ({
    categories: [],
    tags: [],
    categoriesLoaded: false,
    tagsLoaded: false
  }),
  
  actions: {
    async loadCategories() {
      if (this.categoriesLoaded) return
      const res = await listCategories()
      this.categories = res.data
      this.categoriesLoaded = true
    },
    
    async loadTags() {
      if (this.tagsLoaded) return
      const res = await listTags()
      this.tags = res.data
      this.tagsLoaded = true
    }
  }
})
```

**优先级**：中
**预计收益**：减少重复请求，提升用户体验

---

### 5. 代码分割优化

#### 当前状态
- 已使用路由懒加载
- 可以进一步细化分割策略

#### 优化方案
**组件懒加载**
```vue
<script setup>
import { defineAsyncComponent } from 'vue'

const BlogEditor = defineAsyncComponent(() =>
  import('@/components/BlogEditor/index.vue')
)
</script>
```

**预加载关键资源**
```javascript
// router/index.js
{
  path: '/article/edit',
  component: () => import('@/views/blog/article/edit.vue'),
  meta: {
    preload: true
  }
}
```

**优先级**：低
**预计收益**：首屏加载时间减少 10-15%

---

## 构建优化

### 1. Vite 配置优化

```javascript
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  plugins: [
    vue(),
    // 打包分析
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true
    })
  ],
  
  build: {
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          // 将 Element Plus 单独打包
          'element-plus': ['element-plus'],
          // 将 echarts 单独打包
          'echarts': ['echarts'],
          // 将第三方库单独打包
          'vendor': ['vue', 'vue-router', 'pinia', 'axios']
        }
      }
    },
    
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除 console
        drop_debugger: true
      }
    },
    
    // chunk 大小警告阈值
    chunkSizeWarningLimit: 1000
  },
  
  // 依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus'
    ]
  }
})
```

### 2. 依赖分析

**当前主要依赖：**
- Vue 3.5.26
- Element Plus 2.13.1（UI 组件库，较大）
- Echarts 5.6.0（图表库，较大）
- Axios 1.13.2
- Pinia 3.0.4

**优化建议：**
1. Element Plus 按需导入（如未实现）
2. Echarts 按需引入图表类型
3. 移除未使用的依赖

### 3. Tree Shaking 优化

**确保以下配置：**
```javascript
// package.json
{
  "sideEffects": false // 启用 tree shaking
}
```

**检查未使用的导出：**
```bash
npx vite-bundle-visualizer
```

---

## 运行时优化

### 1. 计算属性缓存

```javascript
// ✅ 推荐：使用 computed 自动缓存
const filteredList = computed(() => {
  return articleList.value.filter(item => item.status === 1)
})

// ❌ 避免：在模板中直接计算
<div v-for="item in articleList.filter(item => item.status === 1)">
```

### 2. 防抖和节流

**搜索防抖**
```javascript
import { useDebounceFn } from '@vueuse/core'

const handleSearch = useDebounceFn((keywords) => {
  queryParams.keywords = keywords
  getList()
}, 500)
```

**滚动节流**
```javascript
import { useThrottleFn } from '@vueuse/core'

const handleScroll = useThrottleFn(() => {
  // 处理滚动逻辑
}, 200)
```

### 3. 使用 shallowRef 优化大对象

```javascript
import { shallowRef } from 'vue'

// 对于大数组或深层嵌套对象，使用 shallowRef
const largeList = shallowRef([])

// 更新时重新赋值
largeList.value = [...newList]
```

### 4. 组件缓存

```vue
<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="['ArticleList', 'CategoryList']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>
```

---

## 网络优化

### 1. HTTP/2 支持
- 确保服务器支持 HTTP/2
- 多路复用减少连接数

### 2. 资源预加载
```html
<link rel="preload" href="/path/to/font.woff2" as="font" type="font/woff2" crossorigin>
<link rel="prefetch" href="/api/articles">
```

### 3. Service Worker 缓存
```javascript
// 缓存静态资源
self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((response) => {
      return response || fetch(event.request)
    })
  )
})
```

---

## 监控和分析

### 1. 性能监控

**使用 Performance API**
```javascript
// 监控页面加载时间
window.addEventListener('load', () => {
  const perfData = window.performance.timing
  const loadTime = perfData.loadEventEnd - perfData.navigationStart
  console.log('页面加载时间:', loadTime, 'ms')
})
```

**使用 Lighthouse**
```bash
# 安装 Lighthouse
npm install -g lighthouse

# 运行分析
lighthouse http://localhost:3000 --view
```

### 2. 打包分析

```bash
# 安装分析工具
npm install --save-dev rollup-plugin-visualizer

# 构建时生成分析报告
npm run build:prod
```

---

## 优化优先级总结

| 优化项 | 优先级 | 难度 | 预计收益 | 工作量 |
|--------|--------|------|----------|--------|
| 图片优化 | 高 | 低 | 40% 加载速度提升 | 1-2 天 |
| 接口缓存 | 高 | 中 | 减少 30% 请求 | 2-3 天 |
| 虚拟滚动 | 中 | 中 | 大列表性能提升 60% | 2-3 天 |
| 状态管理优化 | 中 | 低 | 减少重复请求 | 1-2 天 |
| 代码分割优化 | 低 | 低 | 首屏加载减少 10-15% | 1 天 |
| Service Worker | 低 | 高 | 离线访问 | 3-5 天 |

---

## 性能指标目标

| 指标 | 当前值 | 目标值 | 优化措施 |
|------|--------|--------|----------|
| FCP（首次内容绘制） | 待测 | < 1.5s | 代码分割、预加载 |
| LCP（最大内容绘制） | 待测 | < 2.5s | 图片优化、CDN |
| TTI（可交互时间） | 待测 | < 3.5s | 减少 JS 体积 |
| CLS（累积布局偏移） | 待测 | < 0.1 | 预留空间、骨架屏 |
| FID（首次输入延迟） | 待测 | < 100ms | 减少主线程工作 |

---

## 下一步行动计划

### 第一阶段（1-2 周）
1. 实现图片懒加载和优化
2. 添加接口请求缓存
3. 实现搜索防抖

### 第二阶段（2-3 周）
1. 大列表实现虚拟滚动
2. 优化 Vite 构建配置
3. 添加性能监控

### 第三阶段（3-4 周）
1. 实现状态管理优化
2. 添加打包分析和优化
3. 性能测试和调优

---

## 参考资源

- [Vue 3 性能优化指南](https://vuejs.org/guide/best-practices/performance.html)
- [Vite 性能优化](https://vitejs.dev/guide/performance.html)
- [Element Plus 按需导入](https://element-plus.org/zh-CN/guide/quickstart.html#on-demand-import)
- [Web Vitals](https://web.dev/vitals/)
