import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import {
  login as apiLogin,
  logout as apiLogout,
  getCurrentUser,
  updateUserInfo as apiUpdateUserInfo
} from '@/api/user'

/**
 * 用户 store
 *
 * 与 blog-vue3 的 stores/user.ts 概念一致:
 *   - token 走 storage (uni.storage),不进 pinia state 持久化(避免双源)
 *   - userInfo 只在内存中,App 启动时从 syncSession 拉回
 *
 * 不用 pinia-persist 的原因:uniapp 上 pinia-persist 适配差,
 * 直接用 uni.storage 既够用又跨端兼容。
 */
export const useUserStore = defineStore('user', () => {
  // ============ State ============
  const userInfo = ref(null)        // 后端 UserInfo (扁平结构)
  const token = ref(getToken())     // 从 storage 初始化

  // ============ Getters ============
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const userId = computed(() => userInfo.value?.userInfoId || null)

  // ============ Actions ============

  /**
   * 账密登录
   * @param {{username:string, password:string}} payload
   * @returns {Promise<boolean>} 成功返回 true
   */
  async function login(payload) {
    const res = await apiLogin(payload)
    if (!res.flag) {
      throw new Error(res.message || '登录失败')
    }
    // res.data 即 LoginUserDTO: { userInfo, tokenName, tokenValue, tokenTimeout }
    const result = res.data
    setToken(result.tokenValue)
    token.value = result.tokenValue
    userInfo.value = result.userInfo

    // 登录成功后立即启动通知 WS + 拉一次未读数(动态 import 避免循环依赖)
    try {
      const [{ useNoticeSocket }, { useNoticeStore }] = await Promise.all([
        import('@/composables/useNoticeSocket'),
        import('@/store/notice')
      ])
      useNoticeStore().fetchUnreadCount()
      useNoticeSocket().start()
    } catch (e) {
      // 启动失败不阻塞登录流程
      console.warn('启动通知 WS 失败:', e)
    }

    // 登录成功后拉一次收藏 ID 列表(失败不阻塞)
    fetchCollectIds()

    return true
  }

  /**
   * 退出登录
   * 调后端 /logout 让 sa-token 清掉 Redis 中的 token;
   * 无论后端是否成功,本地都必须清干净(避免卡死)
   */
  async function logout() {
    // 优先断开 WS(再调后端登出)
    try {
      const { useNoticeSocket } = await import('@/composables/useNoticeSocket')
      useNoticeSocket().disconnect()
    } catch (e) { /* noop */ }

    try {
      await apiLogout()
    } catch (e) {
      // 后端失败不阻塞本地清理
    }
    clearLocal()
  }

  /**
   * 仅清本地状态(不调后端)
   * 用于 40001 等场景:token 已失效,无需再请求后端
   */
  function clearLocal() {
    removeToken()
    token.value = ''
    userInfo.value = null

    // 同步断 WS + 清通知 store
    try {
      // 用 import().then 不阻塞同步流程
      import('@/composables/useNoticeSocket').then(({ useNoticeSocket }) => {
        useNoticeSocket().disconnect()
      })
      import('@/store/notice').then(({ useNoticeStore }) => {
        useNoticeStore().resetState()
      })
    } catch (e) { /* noop */ }
  }

  /**
   * 启动时校验登录态
   * 本地有 token → 静默请求 /users/current,失败则清理
   */
  async function syncSession() {
    if (!token.value) return false
    try {
      const res = await getCurrentUser()
      if (res.flag && res.data) {
        userInfo.value = res.data
        // session 仍然有效 → 拉一次收藏 ID 列表
        fetchCollectIds()
        return true
      }
      clearLocal()
      return false
    } catch (e) {
      clearLocal()
      return false
    }
  }

  /**
   * 更新本地点赞集合(乐观更新)
   * @param {number} articleId
   */
  function toggleArticleLike(articleId) {
    if (!userInfo.value) return
    if (!Array.isArray(userInfo.value.articleLikeSet)) {
      userInfo.value.articleLikeSet = []
    }
    const set = userInfo.value.articleLikeSet
    const idx = set.indexOf(articleId)
    if (idx > -1) {
      set.splice(idx, 1)
    } else {
      set.push(articleId)
    }
  }

  function isArticleLiked(articleId) {
    if (!userInfo.value || !Array.isArray(userInfo.value.articleLikeSet)) return false
    return userInfo.value.articleLikeSet.includes(articleId)
  }

  function isCommentLiked(commentId) {
    if (!userInfo.value || !Array.isArray(userInfo.value.commentLikeSet)) return false
    return userInfo.value.commentLikeSet.includes(commentId)
  }

  function toggleCommentLike(commentId) {
    if (!userInfo.value) return
    if (!Array.isArray(userInfo.value.commentLikeSet)) {
      userInfo.value.commentLikeSet = []
    }
    const set = userInfo.value.commentLikeSet
    const idx = set.indexOf(commentId)
    if (idx > -1) set.splice(idx, 1)
    else set.push(commentId)
  }

  // ========= 收藏(后端 UserInfoDTO 不返回 collectSet,需单独拉) =========

  /**
   * 启动/登录后调用一次,拉取当前用户的收藏文章 ID 列表
   *
   * 实现:复用已有 /user/collects 分页接口,传大 size 一次拉全,
   * 然后 map 出 articleId 数组写入 userInfo.collectSet。
   * 博客类应用收藏几百条是常态上限,size=999 足够覆盖。
   */
  async function fetchCollectIds() {
    if (!userInfo.value) return
    try {
      const { getMyCollects } = await import('@/api/article')
      const res = await getMyCollects(1, 999)
      if (res.flag && res.data) {
        const list = res.data.recordList || res.data.records || []
        // 后端 ArticleCollectDTO 字段:id / articleId / articleTitle / articleCover / createTime
        userInfo.value.collectSet = list
          .map((item) => item.articleId)
          .filter((id) => id != null)
      } else if (!userInfo.value.collectSet) {
        userInfo.value.collectSet = []
      }
    } catch (e) {
      if (!userInfo.value.collectSet) userInfo.value.collectSet = []
    }
  }

  function isArticleCollected(articleId) {
    if (!userInfo.value || !Array.isArray(userInfo.value.collectSet)) return false
    return userInfo.value.collectSet.includes(articleId)
  }

  function toggleArticleCollect(articleId) {
    if (!userInfo.value) return
    if (!Array.isArray(userInfo.value.collectSet)) {
      userInfo.value.collectSet = []
    }
    const set = userInfo.value.collectSet
    const idx = set.indexOf(articleId)
    if (idx > -1) set.splice(idx, 1)
    else set.push(articleId)
  }

  /**
   * 更新个人资料(昵称/简介/网站等)
   * @param {Object} patch 部分字段对象,直接传给后端 PUT /users/info
   */
  async function updateUserInfo(patch) {
    const res = await apiUpdateUserInfo(patch)
    if (!res.flag) {
      throw new Error(res.message || '更新失败')
    }
    // 同步本地 userInfo,避免回到上一页还看到旧数据
    if (userInfo.value) {
      Object.assign(userInfo.value, patch)
    }
    return true
  }

  /**
   * 仅本地更新头像 URL(头像上传成功后调用,无需再请求 /users/info)
   * @param {string} url 后端返回的头像地址
   */
  function setAvatar(url) {
    if (!userInfo.value || !url) return
    userInfo.value.avatar = url
  }

  return {
    // state
    userInfo,
    token,
    // getters
    isLoggedIn,
    userId,
    // actions
    login,
    logout,
    clearLocal,
    syncSession,
    toggleArticleLike,
    isArticleLiked,
    toggleCommentLike,
    isCommentLiked,
    updateUserInfo,
    setAvatar,
    fetchCollectIds,
    isArticleCollected,
    toggleArticleCollect
  }
})
