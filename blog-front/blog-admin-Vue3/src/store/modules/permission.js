import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

console.log('Available view modules:', Object.keys(modules))

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      routes: [],
      addRoutes: [],
      defaultRoutes: [],
      topbarRouters: [],
      sidebarRouters: []
    }),
    actions: {
      setRoutes(routes) {
        this.addRoutes = routes
        this.routes = constantRoutes.concat(routes)
      },
      setDefaultRoutes(routes) {
        this.defaultRoutes = constantRoutes.concat(routes)
      },
      setTopbarRoutes(routes) {
        this.topbarRouters = routes
      },
      setSidebarRouters(routes) {
        this.sidebarRouters = routes
      },
      generateRoutes(roles) {
        return new Promise((resolve, reject) => {
          // 向后端请求路由数据
          // 博客后端接口: GET /admin/user/menus
          // 响应拦截器已经返回了 res.data.data，所以这里直接使用 res
          getRouters().then(res => {
            console.log('原始菜单数据:', res)

            // res 已经是菜单数组了，不需要再取 res.data
            const menuData = Array.isArray(res) ? res : []

            if (menuData.length === 0) {
              console.warn('菜单数据为空')
              reject('菜单数据为空')
              return
            }

            console.log('处理后的菜单数据:', menuData)

            // 处理菜单数据：添加 iconfont 前缀、转换组件路径
            const processedData = JSON.parse(JSON.stringify(menuData))

            const sidebarRoutes = filterAsyncRouter(processedData)
            console.log('侧边栏路由:', sidebarRoutes)

            // 动态路由权限过滤
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)

            // 注意: 路由添加由 permission.js 的路由守卫统一处理
            // 这里只返回处理好的路由，不要在这里 addRoute
            this.setRoutes(sidebarRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(sidebarRoutes)

            // 返回所有需要添加的路由（动态路由 + 菜单路由）
            const allRoutes = asyncRoutes.concat(sidebarRoutes)
            console.log('路由生成完成，待添加路由数:', allRoutes.length)
            resolve(allRoutes)
          }).catch(error => {
            console.error('获取菜单失败:', error)
            reject(error)
          })
        })
      }
    }
  })

// 遍历后台传来的路由字符串，转换为组件对象
// 博客后端返回格式: { name, path, component, icon, hidden, children }
// component 格式: "Layout" 或 "/article/ArticleList"
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }

    // 处理图标: 添加 iconfont 前缀 (与 Vue2 menu.js 对齐)
    if (route.icon && !route.icon.includes('iconfont')) {
      route.icon = 'iconfont ' + route.icon
    }

    // 处理 meta 信息 (博客后端没有 meta 字段,需要构造)
    if (!route.meta) {
      route.meta = {
        title: route.name,
        icon: route.icon || '',
        noCache: false
      }
    }

    // 处理 hidden 字段
    if (route.hidden === undefined) {
      route.hidden = false
    }

    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        // 博客后端组件路径格式: /article/ArticleList
        // 需要转换为: article/ArticleList (去掉开头的斜杠)
        const componentPath = route.component.startsWith('/')
          ? route.component.substring(1)
          : route.component
        route.component = loadView(componentPath)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  let res
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]
    if (dir === view) {
      res = () => modules[path]()
    }
  }
  return res
}

export default usePermissionStore
