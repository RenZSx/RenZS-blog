import { defineStore } from 'pinia'
import { ref } from 'vue'

// 网站配置接口
export interface WebsiteConfig {
  websiteName: string
  websiteAuthor: string
  websiteAvatar: string
  websiteIntro: string
  websiteNotice: string
  websiteCreateTime: string
  websiteRecordNo: string
  qq: string
  github: string
  gitee: string
  socialUrlList: string[]
  websocketUrl: string
  isReward: number
  weiXinQRCode: string
  alipayQRCode: string
  socialLoginList: string[]
  touristAvatar: string
  isMusicPlayer: number
  isChatRoom: number
  isCommentReview: number
  isEmailNotice: number
  isEmailRegister: number
  [key: string]: any
}

// 博客信息接口
export interface BlogInfo {
  websiteConfig: WebsiteConfig
  pageList: PageItem[]
  articleCount: number
  categoryCount: number
  tagCount: number
  viewsCount: number
  [key: string]: any
}

export interface PageItem {
  id: number
  pageLabel: string
  pageCover: string
  pageName: string
}

// 创建默认博客信息
function createDefaultBlogInfo(): BlogInfo {
  return {
    websiteConfig: {
      websiteName: 'Renzs Blog',
      websiteAuthor: 'Renzs',
      websiteAvatar: '',
      websiteIntro: '',
      websiteNotice: '欢迎来到我的博客',
      websiteCreateTime: new Date().toISOString(),
      websiteRecordNo: '',
      qq: '',
      github: '',
      gitee: '',
      socialUrlList: [],
      websocketUrl: '',
      isReward: 0,
      weiXinQRCode: '',
      alipayQRCode: '',
      socialLoginList: [],
      touristAvatar: '',
      isMusicPlayer: 0,
      isChatRoom: 0,
      isCommentReview: 0,
      isEmailNotice: 0,
      isEmailRegister: 1
    },
    pageList: [],
    articleCount: 0,
    categoryCount: 0,
    tagCount: 0,
    viewsCount: 0
  }
}

// 规范化博客信息
function normalizeBlogInfo(info: Partial<BlogInfo>): BlogInfo {
  const defaultInfo = createDefaultBlogInfo()
  return {
    websiteConfig: {
      ...defaultInfo.websiteConfig,
      ...(info.websiteConfig || {})
    },
    pageList: info.pageList || defaultInfo.pageList,
    articleCount: info.articleCount ?? defaultInfo.articleCount,
    categoryCount: info.categoryCount ?? defaultInfo.categoryCount,
    tagCount: info.tagCount ?? defaultInfo.tagCount,
    viewsCount: info.viewsCount ?? defaultInfo.viewsCount
  }
}

export const useBlogInfoStore = defineStore('blogInfo', () => {
  // State
  const blogInfo = ref<BlogInfo>(createDefaultBlogInfo())

  // Actions
  function setBlogInfo(info: Partial<BlogInfo>) {
    blogInfo.value = normalizeBlogInfo(info)
  }

  function updateWebsiteConfig(config: Partial<WebsiteConfig>) {
    blogInfo.value.websiteConfig = {
      ...blogInfo.value.websiteConfig,
      ...config
    }
  }

  function getPageCover(pageLabel: string): string {
    const page = blogInfo.value.pageList.find(p => p.pageLabel === pageLabel)
    return page?.pageCover || ''
  }

  return {
    // State
    blogInfo,
    // Actions
    setBlogInfo,
    updateWebsiteConfig,
    getPageCover
  }
}, {
  persist: {
    key: 'blogInfo-store',
    storage: localStorage,
    paths: ['blogInfo']
  }
})
