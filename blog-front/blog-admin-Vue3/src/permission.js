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

const whiteList = ['/login']

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
    // 以"动态路由是否已注册"作为菜单加载标志(等价于 Vue2 的 isMenuRouteLoaded)。
    // 不能用 userStore.roles.length === 0 判断: 博客后端登录接口就返回了用户
    // 信息,roles 在登录那一刻已非空,该分支会被永久跳过导致菜单请求发不出去。
    const permissionStore = usePermissionStore()
    const userStore = useUserStore()
    if (permissionStore.addRoutes.length === 0) {
      isRelogin.show = true
      try {
        // 刷新页面后 Pinia state 被重置(仅 token 存于 Cookie),
        // 需凭 token 重新拉取用户信息;登录跳转时 store 已有数据则跳过,
        // 避免紧随登录再多发一次 /users/current
        if (!userStore.name) {
          await userStore.getInfo()
        }
        isRelogin.show = false
        // 拉取后端菜单并生成可访问路由
        const accessRoutes = await permissionStore.generateRoutes()
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(route)
          }
        })
        // 重新导航到目标路由，确保动态路由已注册
        return { ...to, replace: true }
      } catch (err) {
        console.error('登录态重建失败:', err)
        // token 失效或菜单加载失败,清理本地状态回到登录页
        await userStore.logOut().catch(() => {})
        ElMessage.error(err?.message || '登录已失效，请重新登录')
        return { path: '/login' }
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
