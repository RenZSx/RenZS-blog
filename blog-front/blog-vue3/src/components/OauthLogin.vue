<template>
  <div class="oauth-login">
    <v-container>
      <v-row justify="center">
        <v-col cols="12" class="text-center">
          <v-progress-circular indeterminate color="primary" />
          <p class="mt-4">正在登录中...</p>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useUIStore } from '@/stores/ui'
import { bindQq, getCurrentUser, giteeLogin, qqLogin, weiboLogin } from '@/api/user'
import { useToast } from '@/composables/useToast'
import { clearOauthMode, getOauthMode } from '@/utils/oauthMode'
import { extractQqOAuthPayload, hasQqAccessToken } from '@/utils/qqOAuth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const uiStore = useUIStore()

onMounted(async () => {
  const { code } = route.query
  const path = route.path
  const qqPayload = extractQqOAuthPayload(route.query, route.hash || window.location.hash || window.location.href)

  try {
    let data: any
    const oauthMode = getOauthMode()

    if (path.includes('qq') && !hasQqAccessToken(qqPayload)) {
      throw new Error('QQ授权信息缺失，请重新授权')
    }

    if (path.includes('qq') && oauthMode?.provider === 'qq' && oauthMode.mode === 'bind') {
      data = await bindQq(qqPayload)
      clearOauthMode()
      if (data?.data?.flag) {
        const currentUser = await getCurrentUser()
        if (currentUser.data?.flag && currentUser.data.data) {
          userStore.login({
            userInfo: currentUser.data.data,
            tokenName: '',
            tokenValue: '',
            tokenTimeout: 0
          })
        } else {
          userStore.qqBound = true
        }
        useToast({ type: 'success', message: '绑定成功' })
      } else {
        useToast({ type: 'error', message: data?.data?.message || '绑定失败' })
      }
    } else if (path.includes('qq')) {
      data = await qqLogin(qqPayload)
    } else if (path.includes('weibo')) {
      data = await weiboLogin({ code: code as string })
    } else if (path.includes('gitee')) {
      data = await giteeLogin({ code: code as string })
    }

    if (path.includes('qq') && oauthMode?.provider === 'qq' && oauthMode.mode === 'bind') {
      // 绑定模式已经在上方完成提示和用户状态刷新。
    } else if (data?.data?.flag) {
      if (path.includes('qq')) {
        clearOauthMode()
      }
      userStore.login(data.data.data)
      useToast({ type: 'success', message: '登录成功' })
    } else {
      useToast({ type: 'error', message: data?.data?.message || '登录失败' })
    }
  } catch (error) {
    useToast({ type: 'error', message: error instanceof Error ? error.message : '登录失败' })
  }

  // 跳转到之前的页面或首页
  const redirectUrl = uiStore.loginUrl || '/'
  uiStore.saveLoginUrl('')
  router.replace(redirectUrl)
})
</script>
