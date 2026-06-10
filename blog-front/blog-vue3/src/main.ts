import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import vuetify from './plugins/vuetify'

// 样式导入
import '@mdi/font/css/materialdesignicons.css'
import 'animate.css'
import 'nprogress/nprogress.css'
import 'highlight.js/styles/atom-one-dark.css'

// 全局样式
import './assets/css/tokens.css'
import './assets/css/iconfont.css'
import './assets/css/index.css'
import './assets/css/markdown.css'
import './assets/css/dark-theme.css'

// 创建应用实例
const app = createApp(App)

// Pinia 状态管理
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 注册插件
app.use(pinia)
app.use(router)
app.use(vuetify)

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('Global error:', err)
  console.error('Component:', instance)
  console.error('Info:', info)
}

// 挂载应用
app.mount('#app')
