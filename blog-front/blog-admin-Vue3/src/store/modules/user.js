import router from '@/router'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import useLockStore from '@/store/modules/lock'
import usePermissionStore from '@/store/modules/permission'
import defAva from '@/assets/images/profile.jpg'


const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      // 博客后端 userInfo 额外字段，个人中心页面需要
      intro: '',
      webSite: '',
      roles: [],
      permissions: []
    }),
    actions: {
      // 登录 - 适配博客后端(不需要验证码)
      login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        return new Promise((resolve, reject) => {
          login(username, password).then(res => {
            if (!res.flag || !res.data) {
              reject(new Error(res.message || '登录失败'))
              return
            }
            // 博客后端返回格式: { flag, code, message, data: { tokenName, tokenValue, userInfo } }
            // 兼容处理: 如果返回的是 token 字段则直接使用,否则使用 tokenValue
            const token = res.data.token || res.data.tokenValue
            setToken(token)
            this.token = token

            // 博客后端登录时一并返回用户信息,直接写入 store,
            // 免去登录后再多发一次 /users/current
            this.setUserInfo(res.data.userInfo)

            useLockStore().unlockScreen()
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      /**
       * 将博客后端的 UserInfoDTO 写入 store。
       *
       * 登录接口与 /users/current 返回的用户结构完全一致(平铺,非模板的
       * { user, roles, permissions } 嵌套结构),故两处共用此方法。
       *
       * 说明: 博客后端的角色信息不随用户信息下发,访问控制完全由后端菜单
       * (/admin/user/menus)决定 —— 能拿到哪些菜单就能访问哪些页面。
       * 这里给定固定角色只为满足模板中 v-hasRole / v-hasPermi 指令的
       * 存在性校验,不作为真正的权限依据。
       *
       * @param {Object} userInfo 后端返回的 UserInfoDTO
       */
      setUserInfo(userInfo) {
        if (!userInfo) return
        this.id = userInfo.userInfoId || userInfo.id
        this.name = userInfo.username
        this.nickName = userInfo.nickname
        this.avatar = userInfo.avatar || defAva
        this.intro = userInfo.intro || ''
        this.webSite = userInfo.webSite || ''
        this.roles = ['admin']
        this.permissions = ['*:*:*']
      },
      /**
       * 获取当前登录用户信息。
       *
       * 刷新页面时 Pinia state 会重置,但 token 仍在 Cookie 中,
       * 此时需要凭 token 向后端重新拉取用户信息以重建登录态。
       */
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then(res => {
            if (!res.flag || !res.data) {
              reject(new Error(res.message || '获取用户信息失败'))
              return
            }
            // 响应格式: { flag, code, message, data: UserInfoDTO }
            this.setUserInfo(res.data)
            resolve(res.data)
          }).catch(error => {
            reject(error)
          })
        })
      },
      /**
       * 清空本地登录态。
       *
       * 必须清空全部用户字段: 路由守卫用 name 是否为空判断要不要重新拉取
       * 用户信息,残留会导致下次登录后跳过拉取。
       */
      resetState() {
        this.token = ''
        this.id = ''
        this.name = ''
        this.nickName = ''
        this.avatar = ''
        this.intro = ''
        this.webSite = ''
        this.roles = []
        this.permissions = []
        removeToken()
        // 清空路由状态,下次登录时重新拉取菜单
        usePermissionStore().resetRoutes()
      },
      // 退出系统
      logOut() {
        return new Promise((resolve, reject) => {
          logout(this.token).then(() => {
            this.resetState()
            resolve()
          }).catch(error => {
            // 后端登出失败(如 token 已失效)也要清理本地状态,
            // 否则会卡在"有 token 但无法使用"的死状态
            this.resetState()
            reject(error)
          })
        })
      }
    }
  })

export default useUserStore
