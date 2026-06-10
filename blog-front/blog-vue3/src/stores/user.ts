import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  userInfoId: number
  avatar: string
  nickname: string
  intro: string
  webSite: string
  email: string
  loginType: string
  articleLikeSet: number[]
  commentLikeSet: number[]
  talkLikeSet: number[]
}

/**
 * 登录响应数据结构,对齐后端 LoginUserDTO:
 *   - userInfo: 既有 UserInfo 字段
 *   - tokenName / tokenValue / tokenTimeout: sa-token 令牌信息
 *     全端纯 Header 鉴权,tokenValue 持久化到 localStorage('token'),
 *     供 src/api/request.ts 自动注入 Authorization Header,
 *     以及 src/utils/websocket.ts 拼接 WebSocket URL 的 ?token=xxx 参数。
 */
export interface LoginPayload {
  userInfo: UserInfo
  tokenName: string
  tokenValue: string
  tokenTimeout: number
}

// 与 src/api/request.ts 中读取 token 时使用的 key 保持一致
const TOKEN_STORAGE_KEY = 'token'

export const useUserStore = defineStore('user', () => {
  // State
  const userId = ref<number | null>(null)
  const authSessionVersion = ref(0)
  const avatar = ref<string | null>(null)
  const nickname = ref<string | null>(null)
  const intro = ref<string | null>(null)
  const webSite = ref<string | null>(null)
  const email = ref<string | null>(null)
  const loginType = ref<string | null>(null)
  const articleLikeSet = ref<number[]>([])
  const commentLikeSet = ref<number[]>([])
  const talkLikeSet = ref<number[]>([])

  // Getters
  const isLoggedIn = computed(() => userId.value !== null)

  // Actions
  function login(payload: LoginPayload) {
    // 真实登录成功后递增会话版本，驱动依赖登录会话的能力（如通知 websocket）主动重连。
    authSessionVersion.value += 1
    const user = payload.userInfo
    userId.value = user.userInfoId
    avatar.value = user.avatar
    nickname.value = user.nickname
    intro.value = user.intro
    webSite.value = user.webSite
    email.value = user.email
    loginType.value = user.loginType
    articleLikeSet.value = user.articleLikeSet || []
    commentLikeSet.value = user.commentLikeSet || []
    talkLikeSet.value = user.talkLikeSet || []
    // 持久化 token 到 localStorage,与 request.ts/utils/websocket.ts 读取约定一致
    if (payload.tokenValue) {
      localStorage.setItem(TOKEN_STORAGE_KEY, payload.tokenValue)
    }
  }

  function logout() {
    // 退出登录时同样递增版本，保证相关实时连接能感知到会话已经切换。
    authSessionVersion.value += 1
    userId.value = null
    avatar.value = null
    nickname.value = null
    intro.value = null
    webSite.value = null
    email.value = null
    loginType.value = null
    articleLikeSet.value = []
    commentLikeSet.value = []
    talkLikeSet.value = []
    // 清理本地 token,防止 axios 拦截器继续注入失效 token 触发 40001
    localStorage.removeItem(TOKEN_STORAGE_KEY)
  }

  function updateUserInfo(user: Partial<UserInfo>) {
    if (user.nickname !== undefined) nickname.value = user.nickname
    if (user.intro !== undefined) intro.value = user.intro
    if (user.webSite !== undefined) webSite.value = user.webSite
  }

  function updateAvatar(newAvatar: string) {
    avatar.value = newAvatar
  }

  function toggleArticleLike(articleId: number) {
    const index = articleLikeSet.value.indexOf(articleId)
    if (index > -1) {
      articleLikeSet.value.splice(index, 1)
    } else {
      articleLikeSet.value.push(articleId)
    }
  }

  function toggleCommentLike(commentId: number) {
    const index = commentLikeSet.value.indexOf(commentId)
    if (index > -1) {
      commentLikeSet.value.splice(index, 1)
    } else {
      commentLikeSet.value.push(commentId)
    }
  }

  function toggleTalkLike(talkId: number) {
    const index = talkLikeSet.value.indexOf(talkId)
    if (index > -1) {
      talkLikeSet.value.splice(index, 1)
    } else {
      talkLikeSet.value.push(talkId)
    }
  }

  function isArticleLiked(articleId: number) {
    return articleLikeSet.value.includes(articleId)
  }

  function isCommentLiked(commentId: number) {
    return commentLikeSet.value.includes(commentId)
  }

  function isTalkLiked(talkId: number) {
    return talkLikeSet.value.includes(talkId)
  }

  return {
    // State
    userId,
    authSessionVersion,
    avatar,
    nickname,
    intro,
    webSite,
    email,
    loginType,
    articleLikeSet,
    commentLikeSet,
    talkLikeSet,
    // Getters
    isLoggedIn,
    // Actions
    login,
    logout,
    updateUserInfo,
    updateAvatar,
    toggleArticleLike,
    toggleCommentLike,
    toggleTalkLike,
    isArticleLiked,
    isCommentLiked,
    isTalkLiked
  }
}, {
  persist: {
    key: 'user-store',
    storage: localStorage,
    paths: [
      'userId',
      'avatar',
      'nickname',
      'intro',
      'webSite',
      'email',
      'loginType',
      'articleLikeSet',
      'commentLikeSet',
      'talkLikeSet'
    ]
  }
})
