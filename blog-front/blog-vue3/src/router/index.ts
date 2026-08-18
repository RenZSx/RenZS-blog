import {
  createRouter,
  createWebHistory,
  type RouteLocationGeneric,
  type RouteRecordRaw
} from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'

const LOGIN_REQUIRED_PROMPT_SYMBOL = Symbol('loginRequiredPrompt')

type LoginRequiredPromptPayload = {
  message: string
  redirect: string
}

type RouteWithLoginRequiredPrompt = {
  [LOGIN_REQUIRED_PROMPT_SYMBOL]?: LoginRequiredPromptPayload
}

function buildAuthRedirect(to: RouteLocationGeneric, mode: 'login' | 'register' | 'forgot-password') {
  return {
    path: '/auth',
    query: {
      ...to.query,
      mode
    }
  }
}

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/home/start',
    name: 'HomeStart',
    component: () => import('@/views/home/HomeStart.vue'),
    meta: { title: '起始页' }
  },
  {
    path: '/home/content',
    name: 'HomeContent',
    component: () => import('@/views/home/HomeContent.vue'),
    meta: { title: '内容' }
  },
  {
    path: '/home/columns',
    name: 'HomeColumns',
    component: () => import('@/views/home/HomeColumns.vue'),
    meta: { title: '专栏' }
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
    path: '/albums/:albumId',
    name: 'Photo',
    component: () => import('@/views/album/Photo.vue')
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
    path: '/tags',
    name: 'Tag',
    component: () => import('@/views/tag/Tag.vue'),
    meta: { title: '标签' }
  },
  {
    path: '/tags/:tagId',
    name: 'TagArticles',
    component: () => import('@/views/article/ArticleList.vue')
  },
  {
    path: '/talks',
    name: 'Talk',
    component: () => import('@/views/talk/Talk.vue'),
    meta: { title: '说说' }
  },
  {
    path: '/music',
    name: 'Music',
    component: () => import('@/views/music/Music.vue'),
    meta: { title: '音乐' }
  },
  {
    path: '/talks/:talkId',
    name: 'TalkInfo',
    component: () => import('@/views/talk/TalkInfo.vue'),
    meta: { title: '说说详情' }
  },
  {
    path: '/links',
    name: 'Link',
    component: () => import('@/views/link/Link.vue'),
    meta: { title: '友链' }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/about/About.vue'),
    meta: { title: '关于' }
  },
  {
    path: '/message',
    name: 'Message',
    component: () => import('@/views/message/Message.vue'),
    meta: { title: '留言板' }
  },
  {
    path: '/letter',
    name: 'Letter',
    component: () => import('@/views/letter/Letter.vue'),
    meta: { title: '信件' }
  },
  {
    path: '/love',
    name: 'Love',
    component: () => import('@/views/love/Love.vue'),
    meta: { title: '纪念页' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/User.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'ChatRoom',
    component: () => import('@/views/chat/ChatRoom.vue'),
    meta: { title: '聊天室' }
  },
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('@/views/auth/AuthPage.vue'),
    meta: { title: '登录', layout: 'auth' }
  },
  {
    path: '/login',
    name: 'Login',
    redirect: (to) => buildAuthRedirect(to, 'login')
  },
  {
    path: '/register',
    name: 'Register',
    redirect: (to) => buildAuthRedirect(to, 'register')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    redirect: (to) => buildAuthRedirect(to, 'forgot-password')
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
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return { el: to.hash, top: 88, behavior: 'smooth' }
    }
    return { top: 0, behavior: 'instant' }
  }
})

// 配置 NProgress
NProgress.configure({ showSpinner: false })

// 全局前置守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  NProgress.start()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    ;(to as RouteWithLoginRequiredPrompt)[LOGIN_REQUIRED_PROMPT_SYMBOL] = {
      message: '当前页面需要登录后才能访问',
      redirect: to.fullPath
    }
    next(false)
    return
  }

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Renzs Blog`
  } else {
    document.title = 'Renzs Blog'
  }

  next()
})

// 全局后置守卫
router.afterEach((to) => {
  NProgress.done()

  const routeWithPrompt = to as RouteWithLoginRequiredPrompt
  const loginRequiredPromptPayload = routeWithPrompt[LOGIN_REQUIRED_PROMPT_SYMBOL]

  if (!loginRequiredPromptPayload) {
    return
  }

  delete routeWithPrompt[LOGIN_REQUIRED_PROMPT_SYMBOL]
  openLoginRequiredPrompt(loginRequiredPromptPayload)
})

export default router
