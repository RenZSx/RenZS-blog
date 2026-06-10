<template>
  <div class="home-page" :class="{ 'home-theme-dark': isDark }">
    <!-- Hero 区域 -->
    <HomeHero
      :cover="cover"
      :printerInfo="printerInfo"
      :websiteConfig="websiteConfig"
      @refresh-poem="getGuShi"
      @scroll-down="scrollDown"
    />

    <!-- 主页内容 -->
    <v-row
      :class="[
        'home-container',
        'home-layout',
        { 'home-layout-cards': isCardLayout }
      ]"
    >
      <!-- 左侧边栏 - 聚合博主信息、公告和推荐内容 -->
      <v-col
        md="3"
        cols="10"
        class="home-sidebar home-sidebar-left d-md-block d-none"
      >
        <div class="blog-wrapper home-sidebar-sticky">
          <ProfileCard
            :blogInfo="blogInfo"
            :websiteConfig="websiteConfig"
            @bookmark-tip="showBookmarkTip"
          />
          <HomeTalkCard :talkList="talkList" />
          <HomeSidebar
            :blogInfo="blogInfo"
            :newCommentsList="newCommentsList"
          />
          <HomeSiteStatsCard
            :blogInfo="blogInfo"
            :time="runTimeStr"
          />
        </div>
      </v-col>

      <!-- 主体 - 文章列表 -->
      <v-col
        :class="[
          'home-main-column',
          { 'home-main-column-cards': isCardLayout }
        ]"
        md="8"
        cols="12"
      >
        <HomeArticleList
          ref="homeArticleListRef"
          :articleSections="articleSections"
          :activeSectionKey="activeArticleSectionKey"
          :systemNotice="websiteConfig.websiteNotice"
          :isCardLayout="isCardLayout"
          @update:isCardLayout="isCardLayout = $event"
          @show-section-list="showSectionList"
        />
      </v-col>

    </v-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useTheme } from 'vuetify'
import HomeHero from './components/HomeHero.vue'
import HomeArticleList from './components/HomeArticleList.vue'
import HomeSidebar from './components/HomeSidebar.vue'
import HomeSiteStatsCard from './components/HomeSiteStatsCard.vue'
import HomeTalkCard from './components/HomeTalkCard.vue'
import ProfileCard from './components/ProfileCard.vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { useToast } from '@/composables/useToast'
import {
  fetchHomeArticleSections,
  fetchHomeComments,
  fetchHomeTalks
} from './services/homeService'
import MarkdownIt from 'markdown-it'

const theme = useTheme()
const blogInfoStore = useBlogInfoStore()

const md = new MarkdownIt()

// State
const articleSections = ref<any[]>([])
const activeArticleSectionKey = ref('')
const talkList = ref<any[]>([])
const newCommentsList = ref<any[]>([])
const printerInfo = ref('你看对面的青山多漂亮')
const isCardLayout = ref(true)
const runTimeStr = ref('')
const homeArticleListRef = ref<InstanceType<typeof HomeArticleList> | null>(null)
let runTimeInterval: ReturnType<typeof setInterval> | null = null

// Computed
const isDark = computed(() => theme.global.current.value.dark)
const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig)

const cover = computed(() => {
  const pageList = blogInfo.value.pageList || []
  const homePage = pageList.find(item => item.pageLabel === 'home')
  const coverUrl = homePage?.pageCover || ''
  return `background: url(${coverUrl}) center center / cover no-repeat`
})

// Methods
async function fetchHomeSections() {
  try {
    const { data } = await fetchHomeArticleSections()
    const sections = data.data || []
    // 首页卡片不展示正文，但列表模式仍需要纯文本摘要，统一在前端清理 markdown/html。
    sections.forEach((section: any) => {
      section.articleList?.forEach((item: any) => {
        item.articleContent = md
          .render(item.articleContent || '')
          .replace(/<\/?[^>]*>/g, '')
          .replace(/[|]*\n/, '')
          .replace(/&npsp;/gi, '')
      })
    })
    articleSections.value = sections
  } catch (error) {
    useToast({ type: 'error', message: '加载文章失败' })
  }
}

async function fetchInitialData() {
  try {
    // 获取最新评论
    const commentsRes = await fetchHomeComments()
    newCommentsList.value = commentsRes.data.data || []

    // 获取说说
    const talksRes = await fetchHomeTalks()
    talkList.value = talksRes.data.data || []
  } catch (error) {
    console.error('获取初始数据失败:', error)
  }
}

function getGuShi() {
  fetch('https://v1.jinrishici.com/all.json')
    .then(res => res.json())
    .then(data => {
      printerInfo.value = data.content || '你看对面的青山多漂亮'
    })
    .catch(() => {
      printerInfo.value = '你看对面的青山多漂亮'
    })
}

function scrollDown() {
  window.scrollTo({
    behavior: 'smooth',
    top: document.documentElement.clientHeight
  })
}

function showBookmarkTip() {
  useToast({
    type: 'info',
    message: '如果喜欢本网站可以Ctrl+D收藏哦！！'
  })
}

async function showSectionList(sectionKey: string) {
  activeArticleSectionKey.value = sectionKey
  isCardLayout.value = false
  await nextTick()
  homeArticleListRef.value?.focusFirstArticle()
}

function updateRunTime() {
  const createTime = websiteConfig.value.websiteCreateTime || new Date().toISOString()
  const timeDiff = Date.now() - new Date(createTime).getTime()
  const days = Math.floor(timeDiff / (24 * 60 * 60 * 1000))
  const now = new Date()
  runTimeStr.value = `${days}天${now.getHours()}时${now.getMinutes()}分${now.getSeconds()}秒`
}

// Lifecycle
onMounted(async () => {
  // 加载首页文章分组
  await fetchHomeSections()

  // 获取其他数据
  fetchInitialData()

  // 获取诗词
  getGuShi()

  // 运行时间
  updateRunTime()
  runTimeInterval = setInterval(updateRunTime, 1000)
})

onUnmounted(() => {
  if (runTimeInterval) {
    clearInterval(runTimeInterval)
    runTimeInterval = null
  }
})
</script>

<style lang="scss">
.home-page {
  .home-layout {
    width: 100%;
    align-items: flex-start;
    justify-content: center;
    display: flex;
    flex-wrap: nowrap;
    gap: 28px;
  }

  .home-sidebar-sticky {
    position: sticky;
    top: 76px;
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding-right: 2px;
  }

  .home-container {
    margin: calc(100vh - 48px) auto 32px auto;
    padding: 0 24px;
    max-width: 1296px;
  }

  .home-main-column {
    flex: 1 1 928px;
    max-width: 928px;
    min-width: 0;
    padding: 0 !important;
  }

  .home-main-column-cards {
    flex-basis: 928px;
    max-width: 928px;
  }

  .home-sidebar-left {
    flex: 0 0 300px;
    max-width: 300px;
    margin: 0 !important;
    padding: 0 !important;
  }

  @media (max-width: 1499px) and (min-width: 960px) {
    .home-layout {
      gap: 22px;
    }

    .home-container {
      max-width: 1160px;
      padding: 0 20px;
    }

    .home-main-column {
      flex: 1 1 0;
      max-width: none;
      min-width: 0;
    }

    .home-main-column-cards {
      flex-basis: auto;
      max-width: none;
    }

    .home-sidebar-left {
      flex-basis: 270px;
      max-width: 270px;
    }
  }

  @media (max-width: 759px) {
    .home-layout {
      width: auto;
      margin: calc(100vh - 66px) 0 0;
      padding: 0 10px 16px;
    }
  }
}
</style>
