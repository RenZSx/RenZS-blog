import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach(async (to, from) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    const isLock = useLockStore().isLock
    if (to.path === '/login') {
      NProgress.done()
      return { path: '/' }
    }
    if (isWhiteList(to.path)) {
      return true
    }
    if (isLock && to.path !== '/lock') {
      NProgress.done()
      return { path: '/lock' }
    }
    if (!isLock && to.path === '/lock') {
      NProgress.done()
      return { path: '/' }
    }
    // 检查路由是否已经加载（通过检查 addRoutes 是否为空）
    // 博客后端在登录时已经返回用户信息并设置了 roles，所以不能用 roles.length === 0 来判断
    const permissionStore = usePermissionStore()
    if (permissionStore.addRoutes.length === 0) {
      isRelogin.show = true
      try {
        // 拉取user_info信息（博客后端登录时已返回，这里会直接从 store 返回）
        await useUserStore().getInfo()
        isRelogin.show = false
        // 根据roles权限生成可访问的路由
        console.log('开始生成动态路由...')
        const accessRoutes = await permissionStore.generateRoutes()
        console.log('动态路由生成完成，开始添加到路由器')
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(route)
            console.log('已添加路由:', route.path)
          }
        })
        console.log('路由添加完成，重新导航')
        // 重新导航到目标路由，确保动态路由已注册
        return { ...to, replace: true }
      } catch (err) {
        console.error('路由生成失败:', err)
        await useUserStore().logOut()
        ElMessage.error(err)
        return { path: '/' }
      }
    }
    return true
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      return true
    }
    NProgress.done()
    return `/login?redirect=${to.fullPath}` // 否则全部重定向到登录页
  }
})

router.afterEach(() => {
  NProgress.done()
})
