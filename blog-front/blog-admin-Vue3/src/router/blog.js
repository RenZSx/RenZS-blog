// 博客相关路由配置
export const blogRoutes = [
  {
    path: '/blog',
    component: () => import('@/layout/index.vue'),
    redirect: '/blog/home',
    meta: { title: '博客管理', icon: 'blog' },
    children: [
      {
        path: 'home',
        name: 'BlogHome',
        component: () => import('@/views/blog/home/index.vue'),
        meta: { title: '博客首页', icon: 'dashboard' }
      },
      {
        path: 'article/list',
        name: 'ArticleList',
        component: () => import('@/views/blog/article/list.vue'),
        meta: { title: '文章列表', icon: 'article' }
      },
      {
        path: 'article/edit/:id?',
        name: 'ArticleEdit',
        component: () => import('@/views/blog/article/edit.vue'),
        meta: { title: '编辑文章', icon: 'edit', hidden: true }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/blog/category/index.vue'),
        meta: { title: '分类管理', icon: 'category' }
      },
      {
        path: 'tag',
        name: 'Tag',
        component: () => import('@/views/blog/tag/index.vue'),
        meta: { title: '标签管理', icon: 'tag' }
      },
      {
        path: 'comment',
        name: 'Comment',
        component: () => import('@/views/blog/comment/index.vue'),
        meta: { title: '评论管理', icon: 'comment' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/blog/message/index.vue'),
        meta: { title: '留言管理', icon: 'message' }
      },
      {
        path: 'talk/list',
        name: 'TalkList',
        component: () => import('@/views/blog/talk/list.vue'),
        meta: { title: '说说列表', icon: 'talk' }
      },
      {
        path: 'talk/edit/:id?',
        name: 'TalkEdit',
        component: () => import('@/views/blog/talk/edit.vue'),
        meta: { title: '编辑说说', icon: 'edit', hidden: true }
      },
      {
        path: 'album/list',
        name: 'AlbumList',
        component: () => import('@/views/blog/album/list.vue'),
        meta: { title: '相册列表', icon: 'album' }
      },
      {
        path: 'album/photo/:id',
        name: 'PhotoList',
        component: () => import('@/views/blog/album/photo.vue'),
        meta: { title: '照片管理', icon: 'photo', hidden: true }
      },
      {
        path: 'friendlink',
        name: 'FriendLink',
        component: () => import('@/views/blog/friendlink/index.vue'),
        meta: { title: '友链管理', icon: 'link' }
      },
      {
        path: 'user',
        name: 'BlogUser',
        component: () => import('@/views/blog/user/index.vue'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'user/online',
        name: 'OnlineUser',
        component: () => import('@/views/blog/user/online.vue'),
        meta: { title: '在线用户', icon: 'online' }
      },
      {
        path: 'role',
        name: 'BlogRole',
        component: () => import('@/views/blog/role/index.vue'),
        meta: { title: '角色管理', icon: 'role' }
      },
      {
        path: 'menu',
        name: 'BlogMenu',
        component: () => import('@/views/blog/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'menu' }
      },
      {
        path: 'resource',
        name: 'Resource',
        component: () => import('@/views/blog/resource/index.vue'),
        meta: { title: '资源管理', icon: 'resource' }
      },
      {
        path: 'log/operation',
        name: 'OperationLog',
        component: () => import('@/views/blog/log/operation.vue'),
        meta: { title: '操作日志', icon: 'log' }
      },
      {
        path: 'page',
        name: 'BlogPage',
        component: () => import('@/views/blog/page/index.vue'),
        meta: { title: '页面管理', icon: 'page' }
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/blog/about/index.vue'),
        meta: { title: '关于管理', icon: 'about' }
      },
      {
        path: 'website',
        name: 'Website',
        component: () => import('@/views/blog/website/index.vue'),
        meta: { title: '网站设置', icon: 'setting' }
      }
    ]
  }
]
