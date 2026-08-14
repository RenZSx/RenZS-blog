import { defineStore } from 'pinia'

const useBlogStore = defineStore('blog', {
  state: () => ({
    collapse: false,
    tabList: [{ name: '首页', path: '/index' }],
    userMenuList: []
  }),
  actions: {
    // 保存标签页
    saveTab(tab) {
      if (this.tabList.findIndex(item => item.path === tab.path) === -1) {
        this.tabList.push({ name: tab.name, path: tab.path })
      }
    },
    // 移除标签页
    removeTab(tab) {
      const index = this.tabList.findIndex(item => item.name === tab.name)
      if (index !== -1) {
        this.tabList.splice(index, 1)
      }
    },
    // 重置标签页
    resetTab() {
      this.tabList = [{ name: '首页', path: '/index' }]
    },
    // 切换侧边栏
    toggleCollapse() {
      this.collapse = !this.collapse
    },
    // 保存用户菜单列表
    saveUserMenuList(menuList) {
      this.userMenuList = menuList
    },
    // 清空菜单
    clearUserMenuList() {
      this.userMenuList = []
    }
  },
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'blog-store',
        storage: localStorage,
        paths: ['userMenuList']
      }
    ]
  }
})

export default useBlogStore
