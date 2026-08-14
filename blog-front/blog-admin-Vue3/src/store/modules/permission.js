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
        // 博客后端组件路径格式: /article/ArticleList.vue 或 /home/Home.vue
        // 需要转换为: article/ArticleList 或 home/Home (去掉开头的斜杠和 .vue 后缀)
        let componentPath = route.component
        // 去掉开头的斜杠
        if (componentPath.startsWith('/')) {
          componentPath = componentPath.substring(1)
        }
        // 去掉 .vue 后缀
        if (componentPath.endsWith('.vue')) {
          componentPath = componentPath.replace(/\.vue$/, '')
        }
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
  // Vue2 到 Vue3 的路径映射
  // Vue2: home/Home → Vue3: blog/home/index
  // Vue2: article/ArticleList → Vue3: blog/article/list
  const pathMap = {
    'home/Home': 'blog/home/index',
    'article/Article': 'blog/article/edit',
    'article/ArticleList': 'blog/article/list',
    'category/Category': 'blog/category/index',
    'tag/Tag': 'blog/tag/index',
    'comment/Comment': 'blog/comment/index',
    'message/Message': 'blog/message/index',
    'user/User': 'blog/user/index',
    'user/Online': 'blog/user/online',
    'role/Role': 'blog/role/index',
    'resource/Resource': 'blog/resource/index',
    'menu/Menu': 'blog/menu/index',
    'friendLink/FriendLink': 'blog/friendlink/index',
    'about/About': 'blog/about/index',
    'log/Operation': 'blog/log/operation',
    'album/Album': 'blog/album/list',
    'album/Photo': 'blog/album/photo',
    'album/Delete': 'blog/album/photo', // 暂时映射到 photo，后续需要创建 delete 组件
    'page/Page': 'blog/page/index',
    'website/Website': 'blog/website/index',
    'talk/Talk': 'blog/talk/edit',
    'talk/TalkList': 'blog/talk/list',
    'notice/SystemNotice': 'system/notice/index', // 若依自带
    'setting/Setting': 'system/user/profile/index', // 映射到个人中心
    'love/Love': 'blog/home/index', // 暂时映射到首页，后续需要创建
  }

  // 如果有映射，使用映射后的路径
  const mappedView = pathMap[view] || view

  let res
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]
    if (dir === mappedView) {
      res = () => modules[path]()
    }
  }
  return res
}

export default usePermissionStore
