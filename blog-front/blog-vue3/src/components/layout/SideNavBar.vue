<template>
  <v-navigation-drawer
    v-model="drawer"
    temporary
    width="250"
    location="right"
  >
    <!-- 博主介绍 -->
    <div class="blogger-info">
      <v-avatar size="80" style="margin-bottom: 0.5rem">
        <v-img :src="websiteConfig.websiteAvatar" />
      </v-avatar>
    </div>
    <!-- 博客信息 -->
    <div class="blog-info-wrapper">
      <div class="blog-info-data">
        <router-link to="/archives">
          <div style="font-size: 0.875rem">文章</div>
          <div style="font-size: 1.125rem">
            {{ blogInfo.articleCount }}
          </div>
        </router-link>
      </div>
      <div class="blog-info-data">
        <router-link to="/categories">
          <div style="font-size: 0.875rem">分类</div>
          <div style="font-size: 1.125rem">
            {{ blogInfo.categoryCount }}
          </div>
        </router-link>
      </div>
      <div class="blog-info-data">
        <router-link to="/tags">
          <div style="font-size: 0.875rem">标签</div>
          <div style="font-size: 1.125rem">
            {{ blogInfo.tagCount }}
          </div>
        </router-link>
      </div>
    </div>
    <hr />
    <!-- 页面导航 -->
    <div class="menu-container">
      <div class="menu-group-title">首页</div>
      <div class="menus-item">
        <router-link to="/">
          <v-icon size="18">mdi-home-city</v-icon> 博客
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/home/start">
          <v-icon size="18">mdi-magnify</v-icon> 起始页
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/home/content">
          <v-icon size="18">mdi-folder-open</v-icon> 内容
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/home/columns">
          <v-icon size="18">mdi-tag-multiple</v-icon> 专栏
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/archives">
          <v-icon size="18">mdi-archive</v-icon> 归档
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/albums">
          <v-icon size="18">mdi-image-multiple</v-icon> 相册
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/talks">
          <v-icon size="18">mdi-comment-text</v-icon> 说说
        </router-link>
      </div>
      <div v-if="isChatRoomEnabled" class="menus-item">
        <router-link to="/chat">
          <v-icon size="18">mdi-chat</v-icon> 聊天室
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/categories">
          <v-icon size="18">mdi-folder</v-icon> 分类
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/tags">
          <v-icon size="18">mdi-tag</v-icon> 标签
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/love">
          <v-icon size="18">mdi-heart-pulse</v-icon> 家
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/links">
          <v-icon size="18">mdi-link</v-icon> 友链
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/about">
          <v-icon size="18">mdi-airplane</v-icon> 关于
        </router-link>
      </div>
      <div class="menus-item">
        <router-link to="/message">
          <v-icon size="18">mdi-comment-multiple</v-icon> 留言
        </router-link>
      </div>
      <div class="menus-item">
        <a :href="websiteConfig.websiteBgAddress" target="_blank">
          <v-icon size="18">mdi-view-dashboard</v-icon> 后台
        </a>
      </div>
      <div class="menus-item" v-if="!userStore.avatar">
        <a @click="openLogin">
          <v-icon size="18">mdi-login</v-icon> 登录
        </a>
      </div>
      <template v-else>
        <div class="menus-item">
          <router-link to="/user">
            <v-icon size="18">mdi-account</v-icon> 个人中心
          </router-link>
        </div>
        <div class="menus-item">
          <a @click="handleLogout">
            <v-icon size="18">mdi-logout</v-icon> 退出
          </a>
        </div>
      </template>
    </div>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { logout } from '@/api/user'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const uiStore = useUIStore()
const userStore = useUserStore()
const blogInfoStore = useBlogInfoStore()

const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig || {})
const isChatRoomEnabled = computed(() => Number(websiteConfig.value.isChatRoom) === 1)

const drawer = computed({
  get: () => uiStore.drawer,
  set: (value) => uiStore.setDrawer(value)
})

function openLogin() {
  uiStore.saveLoginUrl(router.currentRoute.value.fullPath)
  router.push({
    path: '/login',
    query: { redirect: router.currentRoute.value.fullPath }
  })
}

async function handleLogout() {
  if (router.currentRoute.value.path === '/user') {
    router.go(-1)
  }

  try {
    const { data } = await logout()
    if (data.flag) {
      userStore.logout()
      useToast({ type: 'success', message: '注销成功' })
    } else {
      useToast({ type: 'error', message: data.message })
    }
  } catch (error) {
    useToast({ type: 'error', message: '注销失败' })
  }
}
</script>

<style scoped>
.blogger-info {
  padding: 26px 30px 0;
  text-align: center;
}

.blog-info-wrapper {
  display: flex;
  align-items: center;
  padding: 12px 10px 0;
}

.blog-info-data {
  flex: 1;
  line-height: 2;
  text-align: center;
}

.blog-info-data a {
  text-decoration: none;
  color: inherit;
}

hr {
  border: 2px dashed #d2ebfd;
  margin: 20px 0;
}

.menu-container {
  padding: 0 10px 40px;
  animation: 0.8s ease 0s 1 normal none running sidebarItem;
}

.menu-group-title {
  padding: 4px 30px 2px;
  color: #7b8794;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.menus-item a {
  padding: 6px 30px;
  display: block;
  line-height: 2;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.menus-item i {
  margin-right: 1rem;
}

@keyframes sidebarItem {
  0% {
    transform: translateX(200px);
  }
  100% {
    transform: translateX(0);
  }
}
</style>
