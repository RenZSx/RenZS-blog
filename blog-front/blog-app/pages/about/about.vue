<template>
  <view class="page" :class="{ 'theme-dark': isDark }">
    <view class="hero">
      <view class="hero-blob blob-1" />
      <image class="hero-avatar" :src="websiteAvatar" mode="aspectFill" />
      <text class="hero-name">{{ websiteName }}</text>
      <text class="hero-author">by {{ websiteAuthor }}</text>
    </view>

    <view class="content-card">
      <view v-if="loading" class="loading-tip">加载中...</view>
      <view v-else-if="!aboutContent" class="empty-state">
        <bx-icon name="bookOpen" :size="120" color="#c0c4cc" />
        <text class="empty-title">关于页内容尚未配置</text>
        <text class="empty-sub">博主可在管理后台 → 关于我 填写</text>
      </view>
      <mp-html
        v-else
        class="markdown-body"
        :content="renderedHtml"
        :tag-style="mdTagStyle"
        selectable
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAbout, getBlogInfo } from '@/api/site'
import { markdownToHtml } from '@/utils/markdown'
import { useThemeClass } from '@/composables/useThemeClass'

const { isDark } = useThemeClass()

const aboutContent = ref('')
const loading = ref(true)
const websiteName = ref('博客')
const websiteAvatar = ref('https://www.gravatar.com/avatar/?d=mp')
const websiteAuthor = ref('博主')

const renderedHtml = computed(() => markdownToHtml(aboutContent.value || ''))

const mdTagStyle = {
  h1: 'font-size:36rpx;font-weight:700;line-height:1.4;margin:32rpx 0 16rpx;color:var(--text-primary);letter-spacing:-0.5rpx;',
  h2: 'font-size:34rpx;font-weight:700;line-height:1.4;margin:28rpx 0 14rpx;color:var(--text-primary);border-bottom:2rpx solid var(--border-color);padding-bottom:10rpx;',
  h3: 'font-size:32rpx;font-weight:700;line-height:1.4;margin:24rpx 0 12rpx;color:var(--text-primary);',
  h4: 'font-size:30rpx;font-weight:700;line-height:1.4;margin:20rpx 0 10rpx;color:var(--text-primary);',
  p: 'font-size:30rpx;line-height:1.85;color:var(--text-regular);margin:16rpx 0;',
  div: 'font-size:30rpx;line-height:1.85;color:var(--text-regular);',
  span: 'font-size:30rpx;',
  a: 'color:#42b983;',
  // 行内代码:与代码块统一黑底白字 + 字号对齐正文 30rpx
  code: 'background:#000000;color:#e8e8e8;padding:2rpx 10rpx;border-radius:6rpx;font-size:30rpx;font-family:Consolas,Menlo,monospace;',
  // 代码块:纯黑底 + 近白字 + 字号对齐正文 30rpx
  pre: 'background:#000000;color:#e8e8e8;padding:24rpx;border-radius:12rpx;overflow-x:auto;font-size:30rpx;line-height:1.85;font-family:Consolas,Menlo,monospace;margin:24rpx 0;white-space:pre;box-shadow:0 4rpx 12rpx rgba(0,0,0,0.12);',
  blockquote: 'border-left:6rpx solid #42b983;padding:16rpx 20rpx;background:rgba(66,185,131,0.06);color:var(--text-regular);margin:24rpx 0;border-radius:0 12rpx 12rpx 0;',
  img: 'max-width:100%;display:block;margin:24rpx 0;border-radius:16rpx;',
  table: 'border-collapse:collapse;width:100%;margin:24rpx 0;font-size:24rpx;display:table;',
  thead: 'background:var(--bg-soft);',
  th: 'padding:12rpx 16rpx;border:2rpx solid var(--border-color);font-weight:600;color:var(--text-primary);text-align:left;',
  td: 'padding:12rpx 16rpx;border:2rpx solid var(--border-color);color:var(--text-regular);'
}

async function load() {
  loading.value = true
  try {
    // 关于页 markdown
    const aboutRes = await getAbout()
    if (aboutRes.flag) {
      aboutContent.value = typeof aboutRes.data === 'string' ? aboutRes.data : (aboutRes.data?.aboutContent || '')
    }
    // 博客基础信息(可选)
    const infoRes = await getBlogInfo()
    if (infoRes.flag && infoRes.data?.websiteConfig) {
      const c = infoRes.data.websiteConfig
      if (c.websiteName) websiteName.value = c.websiteName
      if (c.websiteAvatar) websiteAvatar.value = c.websiteAvatar
      if (c.websiteAuthor) websiteAuthor.value = c.websiteAuthor
    }
  } finally {
    loading.value = false
  }
}

onShow(() => {
  if (!aboutContent.value) load()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 48rpx;
}

.hero {
  position: relative;
  background: linear-gradient(135deg, #42b983 0%, #2d8362 100%);
  padding: 80rpx 32rpx 56rpx;
  text-align: center;
  overflow: hidden;
}

.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(40rpx);
  opacity: 0.4;
}

.blob-1 {
  top: -100rpx;
  right: -100rpx;
  width: 320rpx;
  height: 320rpx;
  background: rgba(255, 255, 255, 0.4);
}

.hero-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #ffffff;
  border: 6rpx solid rgba(255, 255, 255, 0.4);
  margin-bottom: 24rpx;
  position: relative;
  z-index: 1;
}

.hero-name {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 6rpx;
  letter-spacing: -0.5rpx;
  position: relative;
  z-index: 1;
}

.hero-author {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  position: relative;
  z-index: 1;
}

.content-card {
  margin: -32rpx 24rpx 0;
  background: var(--bg-card);
  border-radius: 24rpx;
  padding: 32rpx 28rpx;
  box-shadow: var(--shadow-md);
  position: relative;
  z-index: 2;
}

.loading-tip {
  text-align: center;
  padding: 80rpx 0;
  color: var(--text-secondary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  color: var(--text-secondary);
}

.empty-title {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: var(--text-regular);
}

.empty-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.7;
}

.markdown-body {
  font-size: 30rpx;
  line-height: 1.85;
  color: var(--text-regular);
}

/* mp-html 内置 ._h1 ~ ._h6 用 em 写死,覆盖回 rpx 阅读体系(同 article.vue) */
.markdown-body :deep(._h1) {
  font-size: 36rpx !important;
  line-height: 1.4 !important;
  margin: 32rpx 0 16rpx !important;
}
.markdown-body :deep(._h2) {
  font-size: 34rpx !important;
  line-height: 1.4 !important;
  margin: 28rpx 0 14rpx !important;
}
.markdown-body :deep(._h3) {
  font-size: 32rpx !important;
  line-height: 1.4 !important;
  margin: 24rpx 0 12rpx !important;
}
.markdown-body :deep(._h4),
.markdown-body :deep(._h5),
.markdown-body :deep(._h6) {
  font-size: 30rpx !important;
  line-height: 1.4 !important;
  margin: 20rpx 0 10rpx !important;
}
.markdown-body :deep(._big) { font-size: 32rpx !important; }
.markdown-body :deep(._small) { font-size: 26rpx !important; }
</style>
