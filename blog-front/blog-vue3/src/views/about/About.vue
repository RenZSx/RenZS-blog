<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">关于我</h1>
    </div>
    <!-- 关于我内容 -->
    <v-card class="blog-container about-container">
      <!-- 博主头像 -->
      <div class="my-wrapper">
        <v-avatar size="110">
          <v-img class="author-avatar" :src="avatar" />
        </v-avatar>
      </div>
      <!-- 介绍 -->
      <div
        ref="aboutRef"
        class="about-content markdown-body"
        v-html="aboutContent"
      />
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getAbout } from '@/api/site'
import markdownToHtml from '@/utils/markdown'

const blogInfoStore = useBlogInfoStore()

const aboutContent = ref('')
const aboutRef = ref<HTMLElement | null>(null)
const imgList = ref<string[]>([])

const avatar = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig?.websiteAvatar || ''
})

const cover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const aboutPage = pageList.find(item => item.pageLabel === 'about')
  const coverUrl = aboutPage?.pageCover || ''
  if (coverUrl) {
    return `background: url(${coverUrl}) center center / cover no-repeat`
  }
  // 默认背景渐变
  return 'background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
})

async function getAboutContent() {
  try {
    const { data } = await getAbout()
    aboutContent.value = markdownToHtml(data.data || '')

    await nextTick()

    // 添加图片预览功能
    if (aboutRef.value) {
      const imgs = aboutRef.value.getElementsByTagName('img')
      for (let i = 0; i < imgs.length; i++) {
        imgList.value.push(imgs[i].src)
        imgs[i].style.cursor = 'pointer'
        imgs[i].addEventListener('click', (e: Event) => {
          const target = e.target as HTMLImageElement
          previewImg(target.src)
        })
      }
    }
  } catch (error) {
    console.error('获取关于内容失败:', error)
  }
}

function previewImg(src: string) {
  const index = imgList.value.indexOf(src)
  if (index !== -1) {
    window.open(src, '_blank')
  }
}

onMounted(() => {
  getAboutContent()
})

onUnmounted(() => {
  // 清理事件监听
  if (aboutRef.value) {
    const imgs = aboutRef.value.getElementsByTagName('img')
    for (let i = 0; i < imgs.length; i++) {
      imgs[i].removeEventListener('click', () => {})
    }
  }
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 380px;
  color: #eee;
}

.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-title {
  position: absolute;
  bottom: 50px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.about-container {
  padding: 30px 40px;
  margin: 20px auto 40px !important;
  max-width: 860px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.about-content {
  word-break: break-word;
  line-height: 1.8;
  color: var(--text-primary);
}

.my-wrapper {
  text-align: center;
  margin-bottom: 20px;
}

.my-wrapper :deep(.v-avatar) {
  border: 4px solid rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.16);
}

.author-avatar {
  transition: all 0.5s;
}

.author-avatar:hover {
  transform: rotate(360deg);
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .about-container {
    margin: 20px 10px 20px;
    padding: 20px;
  }
}
</style>
