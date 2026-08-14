import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

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
            // 响应拦截器已剥离外层 Result 包装,res 即菜单数组
            const menuData = Array.isArray(res) ? res : []

            if (menuData.length === 0) {
              reject(new Error('菜单数据为空,请检查当前账号是否已分配菜单权限'))
              return
            }

            // 深拷贝后再转换,避免 filterAsyncRouter 修改原始响应数据
            const processedData = JSON.parse(JSON.stringify(menuData))
            const sidebarRoutes = filterAsyncRouter(processedData)

            // 动态路由权限过滤
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)

            // 注意: 实际的 router.addRoute 由 permission.js 路由守卫统一执行,
            // 这里只负责生成路由对象并写入 store
            this.setRoutes(sidebarRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(sidebarRoutes)

            resolve(asyncRoutes.concat(sidebarRoutes))
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

    // 路径语法转换: 后端菜单沿用 Vue Router 3 的通配符写法 '/articles/*',
    // Vue Router 4 已废弃 '*',必须改写为具名通配参数,否则该路由无法匹配。
    if (route.path && route.path.includes('*')) {
      route.path = route.path.replace(/\/\*$/, '/:pathMatch(.*)*')
    }

    // 处理图标: 添加 iconfont 前缀 (与 Vue2 menu.js 对齐)
    // 后端 icon 可能为 null(如首页/个人中心的父级目录)
    if (route.icon && !route.icon.includes('iconfont')) {
      route.icon = 'iconfont ' + route.icon
    }

    // 处理 meta 信息 (博客后端没有 meta 字段,需要构造)
    // 注意: 后端 name 可能为 null(首页、个人中心的父级),用子菜单名兜底
    if (!route.meta) {
      const fallbackTitle = route.name || route.children?.[0]?.name || ''
      route.meta = {
        title: fallbackTitle,
        icon: route.icon || route.children?.[0]?.icon || '',
        noCache: false
      }
    }

    // 处理 hidden 字段: 后端可能返回 null,Vue Router 侧边栏判断需要布尔值
    route.hidden = route.hidden === true

    // Vue Router 4 要求 name 全局唯一。后端菜单的 name 是中文显示名,
    // 且存在 null(首页父级)与重复(发布文章/修改文章同名场景)的情况,
    // 直接透传会导致 addRoute 相互覆盖。这里统一改为按 path 生成唯一 name。
    if (route.path) {
      route.name = generateRouteName(route.path)
    } else {
      delete route.name
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

/**
 * 由路由 path 生成唯一且合法的路由 name。
 *
 * 后端菜单 name 是中文显示名,可能为 null 或重复(如"发布文章"和"修改文章"
 * 共用 /article/Article.vue),而 Vue Router 4 的 name 必须全局唯一,
 * 重复会导致后注册的路由静默覆盖先注册的。
 *
 * 转换示例:
 *   '/'                → 'Root'
 *   '/article-list'    → 'ArticleList'
 *   '/albums/:albumId' → 'AlbumsAlbumId'
 *   '/articles/*'      → 'ArticlesAll'
 *
 * @param {string} path 路由路径
 * @returns {string} 唯一的路由 name
 */
function generateRouteName(path) {
  if (!path || path === '/') {
    return 'Root'
  }
  const name = path
    .replace(/\*/g, 'All')       // 通配符 * → All
    .split(/[/\-:]/)             // 按 / - : 切分
    .filter(Boolean)
    .map(seg => seg.charAt(0).toUpperCase() + seg.slice(1))
    .join('')
  return name || 'Root'
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
  // 后端返回格式: /home/Home.vue → 处理后: home/Home → 映射到: blog/home/index
  const pathMap = {
    // 首页
    'home/Home': 'blog/home/index',

    // 文章管理
    'article/Article': 'blog/article/edit',
    'article/ArticleList': 'blog/article/list',
    'category/Category': 'blog/category/index',
    'tag/Tag': 'blog/tag/index',

    // 消息管理
    'comment/Comment': 'blog/comment/index',
    'message/Message': 'blog/message/index',
    'notice/SystemNotice': 'blog/notice/index',

    // 用户管理
    'user/User': 'blog/user/index',
    'user/Online': 'blog/user/online',

    // 权限管理
    'role/Role': 'blog/role/index',
    'resource/Resource': 'blog/resource/index',
    'menu/Menu': 'blog/menu/index',

    // 系统管理
    'website/Website': 'blog/website/index',
    'page/Page': 'blog/page/index',
    'friendLink/FriendLink': 'blog/friendlink/index',
    'about/About': 'blog/about/index',
    'love/Love': 'blog/love/index',

    // 相册管理
    'album/Album': 'blog/album/list',
    'album/Photo': 'blog/album/photo',
    'album/Delete': 'blog/album/delete',

    // 说说管理
    'talk/Talk': 'blog/talk/edit',
    'talk/TalkList': 'blog/talk/list',

    // 日志管理
    'log/Operation': 'blog/log/operation',

    // 个人中心
    'setting/Setting': 'blog/setting/index',
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
