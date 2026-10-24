<template>
  <div class="about-page">
    <!-- 头部:可配置封面横带 + 头像 + 环绕标签 -->
    <section class="about-hero" :style="heroStyle">
      <div class="hero-inner">
        <span class="hero-blob hero-blob--l" aria-hidden="true" />
        <span class="hero-blob hero-blob--r" aria-hidden="true" />

        <div class="hero-stage">
          <span
            v-for="(chip, i) in heroChips"
            :key="chip.text"
            class="hero-chip"
            :class="`hero-chip--${i % 2 === 0 ? 'l' : 'r'} hero-chip--row-${Math.floor(i / 2) + 1}`"
          >
            <i>{{ chip.emoji }}</i>{{ chip.text }}
          </span>

          <div class="hero-avatar">
            <span class="hero-avatar__ring" aria-hidden="true" />
            <span class="hero-avatar__dots" aria-hidden="true" />
            <v-avatar size="132">
              <v-img class="hero-avatar__img" :src="avatar" alt="博主头像" cover />
            </v-avatar>
            <span class="hero-avatar__badge" aria-hidden="true">
              <span class="hero-avatar__badge-dot" />
              在线
            </span>

            <!-- 封面透明度调节:收起为头像左下角小圆钮,点击展开 -->
            <div v-if="hasCover" class="cover-tuner" :class="{ 'cover-tuner--open': tunerOpen }">
              <button
                class="cover-tuner__toggle"
                type="button"
                :aria-expanded="tunerOpen"
                aria-label="调整封面透明度"
                title="封面透明度"
                @click.stop="tunerOpen = !tunerOpen"
              >
                <v-icon size="16">mdi-contrast</v-icon>
              </button>
              <div class="cover-tuner__panel" v-show="tunerOpen" @click.stop>
                <span class="cover-tuner__label">封面</span>
                <input
                  v-model.number="coverVisibility"
                  class="cover-tuner__range"
                  type="range"
                  min="0"
                  max="100"
                  step="1"
                  aria-label="封面透明度"
                />
                <span class="cover-tuner__value">{{ coverVisibility }}%</span>
              </div>
            </div>
          </div>
        </div>

        <p class="hero-kicker">ABOUT ME</p>
        <h1 class="hero-title">关于我</h1>
        <p class="hero-tagline">
          <transition name="tagline-flip" mode="out-in">
            <span :key="taglineIndex">{{ taglines[taglineIndex] }}</span>
          </transition>
        </p>
      </div>
    </section>

    <!-- 便当格卡片 -->
    <main class="about-grid">
      <!-- 自我介绍 -->
      <section class="bento bento--intro">
        <span class="bento--intro__circle bento--intro__circle--a" aria-hidden="true" />
        <span class="bento--intro__circle bento--intro__circle--b" aria-hidden="true" />
        <p class="intro-hi">Hi，我是 <strong>{{ author }}</strong> 👋</p>
        <p class="intro-text">{{ intro }}</p>
        <div class="intro-social" v-if="socialLinks.length">
          <a
            v-for="link in socialLinks"
            :key="link.url"
            :href="link.url"
            target="_blank"
            rel="noopener noreferrer"
            class="intro-social__btn"
            :aria-label="link.label"
          >
            <v-icon size="18">{{ link.icon }}</v-icon>
          </a>
        </div>
      </section>

      <!-- 站点数据 -->
      <section class="bento bento--stats">
        <p class="stats-caption">站点已运行 <strong>{{ runDays }}</strong> 天</p>
        <div class="stats-list">
          <div v-for="stat in siteStats" :key="stat.label" class="stats-item">
            <strong>{{ stat.value }}</strong>
            <span>{{ stat.label }}</span>
          </div>
        </div>
      </section>

      <!-- 更多关于我 -->
      <div class="grid-divider">
        <span class="grid-divider__line" aria-hidden="true" />
        <span class="grid-divider__text">更多关于我 · MORE</span>
        <span class="grid-divider__line" aria-hidden="true" />
      </div>

      <!-- 游戏卡:大图 + 底部少量文字 -->
      <section
        v-for="game in gameCards"
        :key="game.name"
        class="bento bento--game"
        :class="[`bento--game--${game.theme}`, { 'bento--game--has-img': game.image }]"
      >
        <div
          v-if="game.image"
          class="game-bg"
          :style="{ backgroundImage: `url(${game.image})` }"
          aria-hidden="true"
        />
        <div class="game-meta">
          <h3 class="game-name">{{ game.name }}</h3>
          <p class="game-note">{{ game.note }}</p>
        </div>
      </section>

      <!-- 追番:左大图 + 右侧缩略图,点击切换/收起 -->
      <section class="bento bento--anime">
        <h2 class="bento__title"><span class="bento__icon">📺</span>追番中</h2>
        <div class="anime-stage">
          <div
            class="anime-main"
            :class="[
              `anime-main--${currentAnime.theme}`,
              { 'anime-main--fold': animeFold, 'anime-main--img': currentAnime.image }
            ]"
          >
            <div
              v-if="currentAnime.image"
              class="anime-main__backdrop"
              :style="{ backgroundImage: `url(${currentAnime.image})` }"
              aria-hidden="true"
            />
            <div
              v-if="currentAnime.image"
              class="anime-main__frame"
              :style="{ backgroundImage: `url(${currentAnime.image})` }"
              aria-hidden="true"
            />
            <div class="anime-main__info">
              <p class="anime-main__name">{{ currentAnime.name }}</p>
              <p class="anime-main__quote">{{ currentAnime.quote }}</p>
            </div>
          </div>
          <div class="anime-thumbs">
            <button
              v-for="anime in animeList"
              :key="anime.name"
              type="button"
              class="anime-thumb"
              :class="[
                `anime-thumb--${anime.theme}`,
                { 'anime-thumb--active': anime.name === currentAnime.name }
              ]"
              :style="anime.image ? { backgroundImage: `url(${anime.image})` } : {}"
              :title="anime.name"
              @click="toggleAnime(anime.name)"
            >
              <span class="anime-thumb__name">{{ anime.name }}</span>
            </button>
          </div>
        </div>
      </section>

      <!-- 坐标:中国地图 + 高亮定位 -->
      <section class="bento bento--geo">
        <div ref="geoChartEl" class="geo-map"></div>
        <p class="geo-city">📍 中国 · 贵州</p>
        <p class="geo-sub">我现在住在这里,写代码,过日子</p>
      </section>

      <!-- 个人档案 -->
      <section class="bento bento--profile">
        <h2 class="bento__title"><span class="bento__icon">📇</span>个人档案</h2>
        <ul class="profile-list">
          <li v-for="row in profileRows" :key="row.label">
            <v-icon size="17">{{ row.icon }}</v-icon>
            <span class="profile-list__label">{{ row.label }}</span>
            <span class="profile-list__value">{{ row.value }}</span>
          </li>
        </ul>
      </section>

      <!-- 喜好 -->
      <section class="bento bento--fav">
        <h2 class="bento__title"><span class="bento__icon">💡</span>我的喜好</h2>
        <ul class="fav-list">
          <li v-for="row in favoriteRows" :key="row.label">
            <span class="fav-list__emoji">{{ row.emoji }}</span>
            <span class="fav-list__label">{{ row.label }}</span>
            <span class="fav-list__value">{{ row.value }}</span>
          </li>
        </ul>
      </section>

      <!-- 技术栈 -->
      <section class="bento bento--stack">
        <h2 class="bento__title"><span class="bento__icon">🧰</span>技术装备</h2>
        <div class="stack-groups">
          <div v-for="group in stackGroups" :key="group.name" class="stack-group">
            <p class="stack-group__name">{{ group.name }}</p>
            <div class="stack-group__chips">
              <span v-for="item in group.items" :key="item" class="stack-chip">{{ item }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 关于正文(Markdown) -->
      <section class="bento bento--story" :class="{ 'bento--story--empty': !aboutContent }">
        <h2 class="bento__title"><span class="bento__icon">📖</span>关于正文</h2>
        <v-progress-circular v-if="contentLoading" indeterminate size="28" class="story-loading" />
        <article
          v-else-if="aboutContent"
          ref="aboutRef"
          class="about-content markdown-body"
          v-html="aboutContent"
        />
        <p v-else class="story-empty">博主还没有写下自我介绍,敬请期待~</p>
      </section>
    </main>

    <p class="about-foot">
      <span class="about-foot__line" aria-hidden="true" />
      感谢你的到访,愿我们都能保持热爱 ✦
      <span class="about-foot__line" aria-hidden="true" />
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useTheme } from 'vuetify'
import * as echarts from 'echarts'
// 注册中国地图（数据提取自 UMD 格式 china.js,独立成 ESM 模块后 import 才不会触发 require 报错）
import '@/assets/js/chinaMap'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getAbout } from '@/api/site'
import markdownToHtml from '@/utils/markdown'

const blogInfoStore = useBlogInfoStore()
const theme = useTheme()

const aboutContent = ref('')
const contentLoading = ref(true)
const aboutRef = ref<HTMLElement | null>(null)
const imgList = ref<string[]>([])
const imageHandlers = new Map<HTMLImageElement, EventListener>()

const author = computed(() => {
  return blogInfoStore.blogInfo.websiteConfig.websiteAuthor || 'renzs'
})

const avatar = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig?.websiteAvatar || ''
})

// 关于页封面:后台可配置;有图时叠加深色遮罩保证导航与文字可读,无图回退纯渐变
const COVER_TUNER_KEY = 'about-cover-visibility'

const aboutCover = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  return pageList.find(item => item.pageLabel === 'about')?.pageCover || ''
})

const hasCover = computed(() => Boolean(aboutCover.value))

// 封面可见度 0-100:0 = 遮罩最深(封面几乎不可见),100 = 封面完全清晰;选择记在本地
const coverVisibility = ref(Math.min(100, Math.max(0, Number(localStorage.getItem(COVER_TUNER_KEY)) || 20)))

watch(coverVisibility, value => {
  localStorage.setItem(COVER_TUNER_KEY, String(Math.round(value)))
})

// 调节器展开状态;点击页面其他位置自动收起
const tunerOpen = ref(false)
const closeTuner = () => {
  tunerOpen.value = false
}

const heroStyle = computed(() => {
  if (!hasCover.value) return {}
  // 遮罩浓度随可见度反向变化:约 0.95(全遮) → 0.15(全透)
  const alpha = 0.95 - 0.8 * (coverVisibility.value / 100)
  return {
    backgroundImage: `linear-gradient(165deg, rgba(13, 23, 45, ${alpha}), rgba(19, 31, 63, ${alpha + 0.03}) 55%, rgba(24, 37, 78, ${alpha + 0.05})), url(${aboutCover.value})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center'
  }
})

const intro = computed(() => {
  return (
    blogInfoStore.blogInfo?.websiteConfig?.websiteIntro ||
    '一名热爱技术与生活的博主,在这里记录学习笔记、踩坑经验与日常思考。'
  )
})

// 社交链接:有配置才展示
const socialLinks = computed(() => {
  const config = blogInfoStore.blogInfo?.websiteConfig || {}
  const links: Array<{ label: string; url: string; icon: string }> = []
  if (config.github) links.push({ label: 'GitHub', url: config.github, icon: 'mdi-github' })
  if (config.gitee) links.push({ label: 'Gitee', url: config.gitee, icon: 'mdi-source-branch' })
  if (config.qq) links.push({ label: 'QQ', url: `tencent://message/?uin=${config.qq}`, icon: 'mdi-qqchat' })
  return links
})

// 站点数据卡片
const siteStats = computed(() => {
  const info = blogInfoStore.blogInfo || {}
  return [
    { label: '文章', value: info.articleCount ?? 0 },
    { label: '分类', value: info.categoryCount ?? 0 },
    { label: '标签', value: info.tagCount ?? 0 },
    { label: '访问', value: formatCount(info.viewsCount ?? 0) }
  ]
})

const formatCount = (n: number) => (n >= 10000 ? (n / 10000).toFixed(1) + 'w' : String(n))

// 运行天数:按建站时间计算,异常时兜底为 0
const runDays = computed(() => {
  const created = blogInfoStore.blogInfo?.websiteConfig?.websiteCreateTime
  if (!created) return 0
  const start = new Date(created).getTime()
  if (Number.isNaN(start)) return 0
  return Math.max(0, Math.floor((Date.now() - start) / 86400000))
})

// 头像两侧漂浮标签
const heroChips = [
  { emoji: '💻', text: '数码爱好者' },
  { emoji: '🧱', text: 'CV工程师' },
  { emoji: '🥣', text: '干饭魂' },
  { emoji: '✏️', text: '爱写笔记' },
  { emoji: '🎵', text: '耳机不离身' },
  { emoji: '🌙', text: '夜猫子' }
]

// 标语轮播
const taglines = ['保持热爱,奔赴山海', '代码之外,还有生活', '写点文字,记点瞬间', '慢慢来,比较快']
const taglineIndex = ref(0)
let taglineTimer: ReturnType<typeof setInterval> | null = null

// 技术装备(按本站真实技术栈展示)
const stackGroups = [
  { name: '前端', items: ['Vue 3', 'TypeScript', 'Vuetify', 'Vite'] },
  { name: '后端', items: ['Spring Boot', 'Sa-Token', 'MyBatis-Plus', 'MySQL', 'Redis'] },
  { name: '运维', items: ['Docker', 'Nginx', 'RocketMQ', 'Elasticsearch'] }
]

// ===== 更多关于我:以下数据按需修改 =====

// 游戏卡:大图背景 + 底部一行文字(image 留空或加载失败时回退主题渐变)
const gameCards = [
  {
    name: '三角洲行动',
    theme: 'df',
    image: 'https://cdn.cloudflare.steamstatic.com/steam/apps/2507950/library_hero.jpg',
    note: '烽火地带 · 搜打撤,背包不满不撤'
  },
  {
    name: '英雄联盟',
    theme: 'lol',
    image: 'https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Ahri_0.jpg',
    note: '中野双排 · 峡谷养老大乱斗'
  }
]

// 个人档案(网名取自站点配置)
const profileRows = computed(() => [
  { icon: 'mdi-account-outline', label: '网名', value: author.value },
  { icon: 'mdi-cake-variant-outline', label: '生于', value: '2003' },
  { icon: 'mdi-code-brackets', label: '专业', value: '软件工程' },
  { icon: 'mdi-map-marker-outline', label: '坐标', value: '中国 · 贵州' },
  { icon: 'mdi-quote-open-outline', label: '签名', value: '枯木会逢春,人无再少年' }
])

// 坐标卡:ECharts 中国地图,高亮贵州并打点
const geoChartEl = ref<HTMLElement | null>(null)
let geoChart: echarts.ECharts | null = null
let geoResizeObserver: ResizeObserver | null = null
const HOME_COORD: [number, number] = [106.63, 26.65]

const renderGeoMap = () => {
  if (!geoChartEl.value) return
  if (!geoChart) geoChart = echarts.init(geoChartEl.value)
  geoChart.setOption(buildGeoOption(), true)
}

const buildGeoOption = (): echarts.EChartsCoreOption => {
  const isDark = theme.global.current.value.dark
  return {
    backgroundColor: 'transparent',
    geo: {
      map: 'china',
      zoom: 1.12,
      aspectScale: 0.75,
      roam: false,
      itemStyle: {
        areaColor: isDark ? '#2b3540' : '#e8eef5',
        borderColor: isDark ? '#3b4754' : '#ffffff',
        borderWidth: 1
      },
      emphasis: { disabled: true },
      select: { disabled: true },
      label: { show: false },
      regions: [
        {
          name: '贵州',
          itemStyle: { areaColor: '#4f7cff' },
          label: { show: true, color: '#ffffff', fontSize: 10 }
        }
      ]
    },
    series: [
      {
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: [{ name: 'home', value: [HOME_COORD[0], HOME_COORD[1], 10] }],
        symbolSize: 8,
        rippleEffect: { scale: 3.4, brushType: 'stroke' },
        itemStyle: { color: '#4f7cff', shadowBlur: 6, shadowColor: '#4f7cff' },
        zlevel: 1
      }
    ]
  }
}

// 喜好
const favoriteRows = [
  { emoji: '🎬', label: '影视', value: '动漫 / 悬疑 / 科幻' },
  { emoji: '🎵', label: '音乐', value: '华语流行 / 电子 / Rap' },
  { emoji: '🎮', label: '游戏', value: '三角洲行动 / 英雄联盟' },
  { emoji: '🍜', label: '生活', value: '干饭 / 摸鱼 / 撸码' }
]

// 追番卡:image 为各番海报图;theme 仅作加载前的占位配色
const animeList = [
  { name: '不良人', theme: 'blues', image: 'https://static.renzs.top/dm/buliangren.jpg', quote: '天罡赋,古今多少事,都付笑谈中' },
  { name: '斗破苍穹', theme: 'fire', image: 'https://static.renzs.top/dm/dpcq.jpg', quote: '三十年河东,三十年河西,莫欺少年穷' },
  { name: '完美世界', theme: 'wild', image: 'https://static.renzs.top/dm/wmsj.jpg', quote: '少年荒,当镇世间一切敌' },
  { name: '仙逆', theme: 'frost', image: 'https://static.renzs.top/dm/xianni.jpg', quote: '顺为凡,逆则仙,只在心中一念间' },
  { name: '遮天', theme: 'dark', image: 'https://static.renzs.top/dm/zhetian.jpg', quote: '登天路,踏歌行,弹指遮天' },
  { name: '剑来', theme: 'sword', image: 'https://static.renzs.top/dm/jianlai.jpg', quote: '遇事不决,可问春风' }
]

// 当前展示的番剧与折叠状态:点缩略图切换大图,再点当前项收起
const currentAnimeName = ref(animeList[0].name)
const animeFold = ref(false)

const currentAnime = computed(
  () => animeList.find(a => a.name === currentAnimeName.value) || animeList[0]
)

const toggleAnime = (name: string) => {
  if (currentAnimeName.value === name) {
    animeFold.value = !animeFold.value
  } else {
    currentAnimeName.value = name
    animeFold.value = false
  }
}

async function getAboutContent() {
  try {
    const { data } = await getAbout()
    aboutContent.value = markdownToHtml(data.data || '')

    await nextTick()

    if (aboutRef.value) {
      const imgs = aboutRef.value.getElementsByTagName('img')
      for (let i = 0; i < imgs.length; i++) {
        const image = imgs[i]
        imgList.value.push(image.src)
        image.style.cursor = 'pointer'
        const handler: EventListener = () => previewImg(image.src)
        imageHandlers.set(image, handler)
        image.addEventListener('click', handler)
      }
    }
  } catch (error) {
    console.error('获取关于内容失败:', error)
  } finally {
    contentLoading.value = false
  }
}

function previewImg(src: string) {
  if (imgList.value.includes(src)) {
    window.open(src, '_blank')
  }
}

onMounted(() => {
  getAboutContent()
  document.addEventListener('click', closeTuner)
  taglineTimer = setInterval(() => {
    taglineIndex.value = (taglineIndex.value + 1) % taglines.length
  }, 3200)
  renderGeoMap()
  geoResizeObserver = new ResizeObserver(() => geoChart?.resize())
  if (geoChartEl.value) geoResizeObserver.observe(geoChartEl.value)
})

// 明暗主题切换时重绘地图配色
watch(
  () => theme.global.current.value.dark,
  () => renderGeoMap()
)

onUnmounted(() => {
  if (taglineTimer) clearInterval(taglineTimer)
  document.removeEventListener('click', closeTuner)
  geoResizeObserver?.disconnect()
  geoResizeObserver = null
  geoChart?.dispose()
  geoChart = null
  imageHandlers.forEach((handler, image) => image.removeEventListener('click', handler))
  imageHandlers.clear()
})
</script>

<style scoped lang="scss">
.about-page {
  position: relative;
  width: min(1080px, calc(100% - 32px));
  margin: 0 auto;
  padding: 0 0 72px;
}

:global(.about-overlay-main) {
  padding-top: 0 !important;
}

/* ========== 头部:深色渐变横带,承载透明导航 ========== */
.about-hero {
  /* 全宽出血:容器限宽,用负 margin 拉满视口宽 */
  width: 100vw;
  margin-left: calc(50% - 50vw);
  padding: 88px 0 96px;
  background: linear-gradient(165deg, #0e1a33 0%, #16244a 55%, #1d2c5e 100%);
}

.hero-inner {
  position: relative;
  width: min(1080px, calc(100% - 32px));
  margin: 0 auto;
  text-align: center;
}

.hero-blob {
  position: absolute;
  z-index: 0;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.4;
  pointer-events: none;

  &--l {
    top: 10px;
    left: -60px;
    width: 240px;
    height: 240px;
    background: #4e6ff0;
  }

  &--r {
    top: 60px;
    right: -50px;
    width: 220px;
    height: 220px;
    background: #7c4dff;
  }
}

.hero-stage {
  position: relative;
  z-index: 1;
  display: inline-block;
  padding: 12px 0;
}

.hero-avatar {
  position: relative;
  width: 156px;
  height: 156px;

  &__ring {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: conic-gradient(
      from 0deg,
      #8bc5ff,
      #c0a8ff,
      #7de3f0,
      #ffd76a,
      #8bc5ff
    );
    animation: ring-spin 14s linear infinite;
    /* 只保留 6px 宽的圆环 */
    -webkit-mask: radial-gradient(
      circle,
      transparent calc(50% - 6px),
      #000 calc(50% - 5px)
    );
    mask: radial-gradient(circle, transparent calc(50% - 6px), #000 calc(50% - 5px));
  }

  &__dots {
    position: absolute;
    inset: 9px;
    border-radius: 50%;
    border: 1.5px dashed rgba(255, 255, 255, 0.28);
  }

  :deep(.v-avatar) {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  &__img {
    transition: transform 0.35s ease;
  }

  &:hover &__img {
    transform: scale(1.06);
  }

  &__badge {
    position: absolute;
    right: -4px;
    bottom: 8px;
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 4px 10px;
    border-radius: 999px;
    border: 1px solid rgba(255, 255, 255, 0.16);
    background: #0f1a36;
    box-shadow: 0 6px 16px rgba(5, 10, 26, 0.5);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    line-height: 1;
  }

  &__badge-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #4caf50;
    box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.25);
  }
}

.hero-chip {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  backdrop-filter: blur(6px);
  animation: chip-float 5s ease-in-out infinite;

  i {
    font-style: normal;
    font-size: 15px;
  }

  &--l {
    right: calc(100% + 34px);
    text-align: right;
  }

  &--r {
    left: calc(100% + 34px);
  }

  /* 三行均匀分布,浮动动画交错 */
  &--row-1 { top: 0; }
  &--row-2 { top: 50%; animation-delay: 0.6s; }
  &--row-3 { top: auto; bottom: 0; animation-delay: 1.2s; }
}

.hero-kicker {
  margin: 26px 0 4px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 0.42em;
  font-size: 12px;
  font-weight: 600;
}

.hero-title {
  margin: 0;
  background: linear-gradient(120deg, #9cc3ff 10%, #c9adff 55%, #7de3f0 90%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  font-size: clamp(34px, 6vw, 46px);
  font-weight: 800;
  letter-spacing: 0.06em;
}

.hero-tagline {
  margin: 14px 0 0;
  min-height: 22px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 15px;
}

/* --- 封面透明度调节器:头像左下角小圆钮,点击向左展开面板 --- */
.cover-tuner {
  position: absolute;
  left: -12px;
  bottom: 6px;
  z-index: 3;

  &__toggle {
    display: grid;
    place-items: center;
    width: 34px;
    height: 34px;
    padding: 0;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.16);
    background: #0f1a36;
    box-shadow: 0 6px 16px rgba(5, 10, 26, 0.5);
    color: rgba(255, 255, 255, 0.85);
    cursor: pointer;
    transition: transform 0.2s ease, color 0.2s ease;

    &:hover {
      transform: scale(1.08);
      color: #8bc5ff;
    }
  }

  &__panel {
    position: absolute;
    right: calc(100% + 10px);
    bottom: 4px;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 14px;
    border-radius: 999px;
    border: 1px solid rgba(255, 255, 255, 0.18);
    background: rgba(10, 18, 38, 0.6);
    color: rgba(255, 255, 255, 0.85);
    font-size: 12px;
    white-space: nowrap;
    backdrop-filter: blur(8px);
    user-select: none;
    animation: tuner-pop 0.22s ease;
  }

  &__label {
    letter-spacing: 0.08em;
  }

  &__range {
    width: 118px;
    height: 4px;
    appearance: none;
    -webkit-appearance: none;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.25);
    background-image: linear-gradient(#8bc5ff, #8bc5ff);
    background-repeat: no-repeat;
    outline: none;
    cursor: pointer;

    &::-webkit-slider-thumb {
      -webkit-appearance: none;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      border: 2px solid #8bc5ff;
      background: #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
      transition: transform 0.15s ease;
    }

    &::-webkit-slider-thumb:hover {
      transform: scale(1.15);
    }

    &::-moz-range-thumb {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      border: 2px solid #8bc5ff;
      background: #fff;
    }
  }

  &__value {
    min-width: 34px;
    text-align: right;
    font-variant-numeric: tabular-nums;
    color: #8bc5ff;
    font-weight: 600;
  }
}

@keyframes tuner-pop {
  from {
    opacity: 0;
    transform: translateX(6px);
  }
}

@keyframes tuner-pop-down {
  from {
    opacity: 0;
    transform: translate(-50%, -6px);
  }

  to {
    transform: translate(-50%, 0);
  }
}

.tagline-flip-enter-active,
.tagline-flip-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.tagline-flip-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.tagline-flip-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ========== 便当格 ========== */
.about-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 18px;
  /* 上移叠压在深色横带上,形成层次 */
  margin-top: -52px;
}

.bento {
  position: relative;
  overflow: hidden;
  padding: 24px 26px;
  border-radius: 20px;
  border: 1px solid var(--surface-border);
  background: var(--surface-base, #fff);
  box-shadow: var(--surface-shadow-sm);
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--surface-shadow-md);
  }
}

.bento__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 700;
}

.bento__icon {
  font-size: 17px;
}

/* --- 自我介绍:渐变底,两种主题同色 --- */
.bento--intro {
  grid-column: span 8;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 200px;
  border: 0;
  background: linear-gradient(120deg, #1976d2 0%, #4e6ff0 52%, #7c4dff 100%);
  color: #fff;

  &__circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);

    &--a {
      top: -46px;
      right: -30px;
      width: 150px;
      height: 150px;
    }

    &--b {
      bottom: -70px;
      right: 120px;
      width: 110px;
      height: 110px;
      background: rgba(255, 255, 255, 0.07);
    }
  }

  .intro-hi {
    position: relative;
    margin: 0 0 10px;
    font-size: 21px;
    font-weight: 700;

    strong {
      font-size: 24px;
    }
  }

  .intro-text {
    position: relative;
    margin: 0;
    max-width: 92%;
    color: rgba(255, 255, 255, 0.88);
    font-size: 14.5px;
    line-height: 1.9;
  }

  .intro-social {
    position: relative;
    display: flex;
    gap: 10px;
    margin-top: 18px;
  }

  .intro-social__btn {
    display: grid;
    place-items: center;
    width: 36px;
    height: 36px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.16);
    color: #fff;
    transition: background 0.2s ease, transform 0.2s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: translateY(-2px);
    }
  }
}

/* --- 站点数据:深色底,两种主题同色 --- */
.bento--stats {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
  min-height: 200px;
  border: 0;
  background: linear-gradient(150deg, #1c2a4a 0%, #232f56 60%, #2a3563 100%);
  color: #fff;

  &::before {
    position: absolute;
    top: -34px;
    left: -34px;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(124, 155, 255, 0.25), transparent 70%);
    content: '';
  }

  .stats-caption {
    margin: 0;
    color: rgba(255, 255, 255, 0.82);
    font-size: 13.5px;

    strong {
      color: #ffc107;
      font-size: 18px;
      margin: 0 2px;
    }
  }

  .stats-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .stats-item {
    display: grid;
    gap: 3px;
    text-align: center;

    strong {
      font-size: 20px;
      font-weight: 800;
    }

    span {
      color: rgba(255, 255, 255, 0.62);
      font-size: 12px;
    }
  }
}

/* --- 分隔标题 --- */
.grid-divider {
  grid-column: span 12;
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 6px 0 0;

  &__line {
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--border-color-strong), transparent);
  }

  &__text {
    color: var(--text-secondary);
    font-size: 12.5px;
    font-weight: 600;
    letter-spacing: 0.22em;
  }
}

/* --- 游戏卡:大图主导,文字压在底部渐变上 --- */
.bento--game {
  position: relative;
  grid-column: span 6;
  min-height: 225px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  border: 0;
  padding: 0;
  color: #fff;
  overflow: hidden;

  /* 背景图层,悬停轻微放大 */
  .game-bg {
    position: absolute;
    inset: 0;
    background-size: cover;
    background-position: center;
    transition: transform 0.45s ease;

    /* 仅压暗底部,保住图片观感 */
    &::after {
      position: absolute;
      inset: 0;
      background: linear-gradient(
        180deg,
        rgba(9, 15, 28, 0.08) 0%,
        rgba(9, 15, 28, 0.08) 45%,
        rgba(9, 15, 28, 0.62) 72%,
        rgba(9, 15, 28, 0.92) 100%
      );
      content: '';
    }
  }

  &:hover .game-bg {
    transform: scale(1.05);
  }

  .game-meta {
    position: relative;
    padding: 0 18px 15px;
  }

  .game-name {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    letter-spacing: 0.04em;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.45);
  }

  .game-note {
    margin: 4px 0 0;
    color: rgba(255, 255, 255, 0.78);
    font-size: 12.5px;
  }

  /* 三角洲行动:军绿(无图时的回退渐变) */
  &--df {
    background: linear-gradient(135deg, #2c3b2d 0%, #40573c 55%, #6b6a34 100%);
  }

  /* 英雄联盟:海克斯蓝(无图时的回退渐变) */
  &--lol {
    background: linear-gradient(135deg, #0c1c33 0%, #16325c 55%, #3c3f77 100%);
  }
}

/* --- 追番卡 --- */
.bento--anime {
  grid-column: span 12;
}

.anime-stage {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.anime-main {
  position: relative;
  height: 300px;
  border-radius: 14px;
  overflow: hidden;
  transition: height 0.45s cubic-bezier(0.4, 0, 0.2, 1);

  /* 收起时叠成细条,只露番名 */
  &--fold {
    height: 54px;
  }

  /* 无图时的占位底色 */
  &--blues { background-color: #2d3f6e; }
  &--fire { background-color: #7a2f26; }
  &--wild { background-color: #2f5e46; }
  &--frost { background-color: #2b5876; }
  &--dark { background-color: #3a2f52; }
  &--sword { background-color: #46688a; }

  /* 模糊铺底层:填满窗口,压暗模糊作背景 */
  .anime-main__backdrop {
    position: absolute;
    inset: 0;
    background-size: cover;
    background-position: center;
    filter: blur(14px) brightness(0.45) saturate(1.15);
    transform: scale(1.12);
  }

  /* 前景:完整展示原图,不裁切 */
  .anime-main__frame {
    position: absolute;
    inset: 0;
    background-size: contain;
    background-position: center;
    background-repeat: no-repeat;
    transition: transform 0.4s ease;
  }

  &:hover .anime-main__frame {
    transform: scale(1.02);
  }

  .anime-main__info {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    padding: 34px 18px 14px;
    background: linear-gradient(180deg, transparent, rgba(9, 15, 28, 0.88));
    color: #fff;
  }

  &--fold .anime-main__info {
    padding: 8px 16px;
    background: linear-gradient(180deg, rgba(9, 15, 28, 0.2), rgba(9, 15, 28, 0.55));
  }

  .anime-main__name {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.45);
  }

  .anime-main__quote {
    margin: 5px 0 0;
    color: rgba(255, 255, 255, 0.78);
    font-size: 12.5px;
    transition: opacity 0.25s ease;
  }

  &--fold .anime-main__quote {
    margin: 0;
    opacity: 0;
    height: 0;
    overflow: hidden;
  }
}

.anime-thumbs {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}

.anime-thumb {
  position: relative;
  aspect-ratio: 16 / 9;
  padding: 0;
  border-radius: 10px;
  border: 2px solid transparent;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s ease, border-color 0.2s ease, opacity 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    opacity: 0.92;
  }

  &--active {
    border-color: var(--primary-color);
  }

  /* 未填图时的占位配色 */
  &--blues { background-color: #2d3f6e; }
  &--fire { background-color: #7a2f26; }
  &--wild { background-color: #2f5e46; }
  &--frost { background-color: #2b5876; }
  &--dark { background-color: #3a2f52; }
  &--sword { background-color: #46688a; }

  &__name {
    position: absolute;
    inset: 0;
    display: grid;
    place-items: center;
    padding: 0 6px;
    background: linear-gradient(180deg, rgba(9, 15, 28, 0.12), rgba(9, 15, 28, 0.55));
    color: #fff;
    font-size: 12.5px;
    font-weight: 600;
    letter-spacing: 0.1em;
  }
}

/* --- 坐标卡:中国地图 --- */
.bento--geo {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  text-align: center;

  .geo-map {
    width: 100%;
    height: 172px;
    margin-bottom: 6px;
    pointer-events: none;
  }

  .geo-city {
    margin: 0;
    color: var(--text-primary);
    font-size: 17px;
    font-weight: 700;
  }

  .geo-sub {
    margin: 0;
    color: var(--text-secondary);
    font-size: 13px;
  }
}

/* --- 个人档案 --- */
.bento--profile {
  grid-column: span 4;
}

.profile-list {
  display: grid;
  gap: 13px;
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text-secondary);
    font-size: 14px;
  }

  &__label {
    flex: 0 0 44px;
    color: var(--text-secondary);
  }

  &__value {
    flex: 1;
    overflow: hidden;
    color: var(--text-primary);
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* --- 喜好 --- */
.bento--fav {
  grid-column: span 4;
}

.fav-list {
  display: grid;
  gap: 13px;
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
  }

  &__emoji {
    font-size: 16px;
  }

  &__label {
    flex: 0 0 36px;
    color: var(--text-secondary);
  }

  &__value {
    flex: 1;
    overflow: hidden;
    color: var(--text-primary);
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* --- 技术装备 --- */
.bento--stack {
  grid-column: span 12;
}

.stack-groups {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.stack-group__name {
  margin: 0 0 10px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.stack-group__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stack-chip {
  padding: 6px 13px;
  border-radius: 999px;
  border: 1px solid var(--surface-border);
  background: var(--surface-raised);
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;

  &:hover {
    border-color: #4e6ff0;
    background: rgba(78, 111, 240, 0.08);
    color: #4e6ff0;
  }
}

/* --- 关于正文 --- */
.bento--story {
  grid-column: span 12;

  &--empty {
    text-align: center;
  }
}

.story-loading {
  margin: 40px auto;
  display: block;
  color: var(--primary-color);
}

.story-empty {
  margin: 30px 0;
  color: var(--text-secondary);
}

.about-content {
  position: relative;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.95;

  :deep(*) {
    max-width: 100%;
  }

  :deep(img) {
    border-radius: 12px;
  }

  :deep(blockquote) {
    margin: 14px 0;
    padding: 10px 16px;
    border-left: 4px solid #4e6ff0;
    border-radius: 0 10px 10px 0;
    background: var(--surface-raised);
    color: var(--text-secondary);
  }
}

/* ========== 页脚行 ========== */
.about-foot {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 30px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  letter-spacing: 0.08em;

  &__line {
    flex: 0 0 56px;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--border-color-strong), transparent);
  }
}

/* ========== 动画 ========== */
@keyframes ring-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes chip-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-7px);
  }
}

.bento {
  animation: bento-in 0.5s ease backwards;

  &:nth-child(3) { animation-delay: 0.07s; }
  &:nth-child(5) { animation-delay: 0.14s; }
  &:nth-child(6) { animation-delay: 0.21s; }
  &:nth-child(7) { animation-delay: 0.28s; }
  &:nth-child(8) { animation-delay: 0.35s; }
  &:nth-child(9) { animation-delay: 0.42s; }
  &:nth-child(10) { animation-delay: 0.49s; }
}

@keyframes bento-in {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
}

/* ========== 响应式 ========== */
@media (max-width: 960px) {
  .about-page {
    width: calc(100% - 24px);
  }

  .about-hero {
    padding: 72px 0 84px;
  }

  /* 窄屏面板改为头像正下方弹出,避免向左溢出视口 */
  .cover-tuner__panel {
    top: calc(100% + 10px);
    right: auto;
    bottom: auto;
    left: 50%;
    transform: translateX(-50%);
    animation-name: tuner-pop-down;
  }

  /* 小屏隐藏两侧漂浮标签,避免溢出 */
  .hero-chip {
    display: none;
  }

  .about-grid {
    margin-top: -40px;
  }

  .bento--intro,
  .bento--stats,
  .bento--game,
  .bento--geo,
  .bento--profile,
  .bento--fav {
    grid-column: span 12;
  }

  .bento--game {
    min-height: 195px;
  }

  .anime-main {
    height: 200px;

    &--fold {
      height: 46px;
    }
  }

  .anime-thumbs {
    grid-template-columns: repeat(3, 1fr);
  }

  .anime-thumb__name {
    font-size: 11.5px;
  }

  .stack-groups {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}
</style>
