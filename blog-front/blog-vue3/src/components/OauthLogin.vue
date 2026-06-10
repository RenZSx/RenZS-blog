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
import { qqLogin, weiboLogin, giteeLogin } from '@/api/user'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const uiStore = useUIStore()

onMounted(async () => {
  const { code, state } = route.query
  const path = route.path

  try {
    let data: any

    if (path.includes('qq')) {
      data = await qqLogin({ code: code as string, state: state as string })
    } else if (path.includes('weibo')) {
      data = await weiboLogin({ code: code as string })
    } else if (path.includes('gitee')) {
      data = await giteeLogin({ code: code as string })
    }

    if (data?.data?.flag) {
      userStore.login(data.data.data)
      useToast({ type: 'success', message: '登录成功' })
    } else {
      useToast({ type: 'error', message: data?.data?.message || '登录失败' })
    }
  } catch (error) {
    useToast({ type: 'error', message: '登录失败' })
  }

  // 跳转到之前的页面或首页
  const redirectUrl = uiStore.loginUrl || '/'
  uiStore.saveLoginUrl('')
  router.replace(redirectUrl)
})
</script>