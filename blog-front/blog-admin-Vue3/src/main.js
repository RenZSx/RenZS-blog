import { createApp } from 'vue'

import Cookies from 'js-cookie'

import ElementPlus, { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import locale from 'element-plus/es/locale/lang/zh-cn'

import '@/assets/styles/index.scss' // global css

import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive

// 注册指令
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

// svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'

import './permission' // permission control

import { parseTime, resetForm, addDateRange, handleTree } from '@/utils/blogUtils'

// 分页组件
import Pagination from '@/components/Pagination'
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"

// 导入博客自定义组件
import BlogEditor from '@/components/BlogEditor/index.vue'
import TagCloud from '@/components/TagCloud/index.vue'

// 导入博客工具函数
import { formatDate, getUploadHeaders } from '@/utils/blog'
import config from '@/config'

const app = createApp(App)

// 全局方法挂载
app.config.globalProperties.download = download
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.$config = config
app.config.globalProperties.$formatDate = formatDate
app.config.globalProperties.$getUploadHeaders = getUploadHeaders

// 全局组件挂载
app.component('Pagination', Pagination)
app.component('ImageUpload', ImageUpload)
app.component('BlogEditor', BlogEditor)
app.component('TagCloud', TagCloud)

// 全局异常兜底:组件内未捕获的错误统一提示,避免只有控制台可见
app.config.errorHandler = (err, instance, info) => {
  console.error('全局异常:', err, 'at', info)
  ElMessage.error({ message: '系统出现异常，请稍后重试', grouping: true })
}

app.use(router)
app.use(store)
app.use(plugins)
app.use(elementIcons)
app.component('svg-icon', SvgIcon)

directive(app)

// 使用element-plus 并且设置全局的大小
app.use(ElementPlus, {
  locale: locale,
  // 支持 large、default、small
  size: Cookies.get('size') || 'default'
})

app.mount('#app')
