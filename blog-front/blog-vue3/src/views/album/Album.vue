<template>
  <div class="album-page">
    <section class="album-hero" :style="cover">
      <div class="album-hero__content">
        <span class="album-hero__label">时光相册</span>
        <h1>每一张照片都是一次美好的记忆</h1>
      </div>
    </section>

    <main class="album-main">
      <div v-if="photoAlbumList.length > 0" class="album-tabs" aria-label="相册分类">
        <button
          v-for="item of photoAlbumList"
          :key="item.id"
          type="button"
          class="album-tab"
          :class="{ 'album-tab--active': item.id === activeAlbumId }"
          :style="getAlbumTagStyle(item.id, item.id === activeAlbumId)"
          @click="changeAlbum(item)"
        >
          <span class="album-tab__label">{{ item.albumName }}</span>
          <strong>{{ item.photoCount ?? 0 }}</strong>
        </button>
      </div>

      <h2 v-if="activeAlbum" class="album-title">{{ activeAlbum.albumName }}</h2>

      <section class="photo-grid" aria-label="照片列表">
        <article
          v-for="(item, index) of visiblePhotoList"
          :key="`${item}-${index}`"
          class="photo-card"
          @click="openPreview(index)"
        >
          <div class="photo-card__image-wrap">
            <img
              class="photo-card__image"
              :src="getOssThumbUrl(getPhotoSrc(item), 600)"
              :srcset="`${getOssThumbUrl(getPhotoSrc(item), 600)} 1x, ${getOssThumbUrl(getPhotoSrc(item), 1200)} 2x`"
              :alt="getPhotoTitle(item, index)"
              loading="lazy"
              decoding="async"
              :fetchpriority="index < 4 ? 'high' : 'low'"
            />
          </div>
        </article>
      </section>

      <nav v-if="showPagination" class="photo-pagination" aria-label="照片分页">
        <button
          type="button"
          class="photo-pagination__button"
          :disabled="loading || current <= 1"
          @click="changePhotoPage(current - 1)"
        >
          上一页
        </button>
        <span class="photo-pagination__info">
          {{ current }} / {{ totalPages }}
        </span>
        <button
          type="button"
          class="photo-pagination__button"
          :disabled="loading || current >= totalPages"
          @click="changePhotoPage(current + 1)"
        >
          下一页
        </button>
      </nav>

      <div v-if="!loading && photoAlbumList.length === 0" class="empty-tip">
        暂无相册
      </div>
      <div v-else-if="!loading && photoList.length === 0" class="empty-tip">
        暂无照片
      </div>
    </main>

    <div v-if="previewVisible" class="photo-preview" @click="closePreview">
      <div class="photo-preview__toolbar" @click.stop>
        <button
          type="button"
          class="photo-preview__btn"
          title="缩小"
          aria-label="缩小"
          @click="zoomOut"
        >
          <v-icon size="22">mdi-magnify-minus-outline</v-icon>
        </button>
        <button
          type="button"
          class="photo-preview__btn"
          title="放大"
          aria-label="放大"
          @click="zoomIn"
        >
          <v-icon size="22">mdi-magnify-plus-outline</v-icon>
        </button>
        <button
          type="button"
          class="photo-preview__btn"
          title="旋转"
          aria-label="旋转"
          @click="rotateRight"
        >
          <v-icon size="22">mdi-rotate-right</v-icon>
        </button>
        <button
          type="button"
          class="photo-preview__btn"
          title="重置"
          aria-label="重置"
          @click="resetPreview"
        >
          <v-icon size="22">mdi-restore</v-icon>
        </button>
        <button
          type="button"
          class="photo-preview__btn"
          title="下载"
          aria-label="下载"
          @click="downloadPhoto"
        >
          <v-icon size="22">mdi-download</v-icon>
        </button>
        <button
          type="button"
          class="photo-preview__btn"
          title="关闭"
          aria-label="关闭"
          @click="closePreview"
        >
          <v-icon size="22">mdi-close</v-icon>
        </button>
      </div>

      <button
        v-if="photoList.length > 1"
        type="button"
        class="photo-preview__nav photo-preview__nav--prev"
        title="上一张"
        aria-label="上一张"
        @click.stop="showPrev"
      >
        <v-icon size="34">mdi-chevron-left</v-icon>
      </button>

      <div class="photo-preview__stage" @click.stop>
        <img
          class="photo-preview__image"
          :src="currentPreviewImage"
          :style="previewImageStyle"
          alt=""
        />
      </div>

      <button
        v-if="photoList.length > 1"
        type="button"
        class="photo-preview__nav photo-preview__nav--next"
        title="下一张"
        aria-label="下一张"
        @click.stop="showNext"
      >
        <v-icon size="34">mdi-chevron-right</v-icon>
      </button>

      <div class="photo-preview__counter" @click.stop>
        {{ previewIndex + 1 }} / {{ photoList.length }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onDeactivated, onMounted, onUnmounted, ref } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import request from '@/api/request'

defineOptions({
  name: 'Album'
})

interface PhotoAlbum {
  id: number
  albumName: string
  albumDesc: string
  albumCover: string
  photoCount?: number
}

interface PhotoItem {
  id?: number
  photoName?: string
  photoDesc?: string
  photoSrc: string
}

type PhotoResponseItem = string | PhotoItem

interface AlbumCacheSnapshot {
  albums: PhotoAlbum[]
  activeAlbumId: number | null
  savedAt: number
}

interface PhotoPageCacheSnapshot {
  albumId: number
  current: number
  size: number
  photos: PhotoResponseItem[]
  savedAt: number
}

interface LoadPhotoOptions {
  preferCache?: boolean
  silent?: boolean
  force?: boolean
}

const ALBUM_CACHE_KEY = 'blog:album:list:v1'
const PHOTO_CACHE_KEY_PREFIX = 'blog:album:photos:v1'
const ALBUM_CACHE_TTL = 10 * 60 * 1000
const PHOTO_CACHE_TTL = 10 * 60 * 1000
const MAX_MEMORY_PHOTO_CACHE = 18
const albumMemoryCache = ref<AlbumCacheSnapshot | null>(null)
const photoMemoryCache = new Map<string, PhotoPageCacheSnapshot>()

const blogInfoStore = useBlogInfoStore()

const photoAlbumList = ref<PhotoAlbum[]>([])
const activeAlbumId = ref<number | null>(null)
const photoList = ref<PhotoResponseItem[]>([])
const current = ref(1)
const size = ref(18)
const loading = ref(false)
const hasMore = ref(true)
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewScale = ref(1)
const previewRotate = ref(0)
const activePhotoRequestKey = ref('')
let previewKeydownBound = false

const activeAlbum = computed(() => {
  return photoAlbumList.value.find((item) => item.id === activeAlbumId.value)
})

const cover = computed(() => {
  const coverUrl = activeAlbum.value?.albumCover || getPageCover()
  return `background: linear-gradient(90deg, rgba(8, 13, 18, 0.82), rgba(8, 13, 18, 0.16)), url(${coverUrl}) center center / cover no-repeat`
})

const visiblePhotoList = computed(() => photoList.value)
// 预览大图：用 1600px 的 OSS 缩略图，而非原图
// 大多数显示器宽度不会超过 1600px，1600px 已足够清晰，但比原图小很多，
// 既能加快预览展开速度，也避免“点开预览要等好几秒”的体验问题。
const currentPreviewImage = computed(() =>
  getOssThumbUrl(getPhotoSrc(photoList.value[previewIndex.value]), 1600)
)
const previewImageStyle = computed(() => ({
  transform: `scale(${previewScale.value}) rotate(${previewRotate.value}deg)`
}))
const totalPhotoCount = computed(() => activeAlbum.value?.photoCount || photoList.value.length)
const totalPages = computed(() => Math.max(Math.ceil(totalPhotoCount.value / size.value), 1))
const showPagination = computed(() => totalPhotoCount.value > size.value)
const albumTagStyleMap = ref<Record<number, Record<string, string>>>({})

function getPageCover() {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const albumPage = pageList.find((item) => item.pageLabel === 'album')
  return albumPage?.pageCover || ''
}

function isCacheFresh(savedAt: number, ttl: number) {
  return Date.now() - savedAt < ttl
}

function cloneAlbums(albums: PhotoAlbum[]) {
  return albums.map((item) => ({ ...item }))
}

function clonePhotos(photos: PhotoResponseItem[]) {
  return photos.map((item) => (typeof item === 'string' ? item : { ...item }))
}

function readSessionCache<T>(key: string) {
  if (typeof window === 'undefined') return null

  try {
    const rawCache = window.sessionStorage.getItem(key)
    return rawCache ? (JSON.parse(rawCache) as T) : null
  } catch (error) {
    console.warn('读取相册缓存失败:', error)
    window.sessionStorage.removeItem(key)
    return null
  }
}

function writeSessionCache(key: string, value: unknown) {
  if (typeof window === 'undefined') return

  try {
    window.sessionStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.warn('写入相册缓存失败:', error)
  }
}

function getPhotoPageCacheKey(albumId: number, page: number, pageSize: number) {
  return `${PHOTO_CACHE_KEY_PREFIX}:${albumId}:${page}:${pageSize}`
}

function rememberPhotoPageCache(cacheKey: string, snapshot: PhotoPageCacheSnapshot) {
  photoMemoryCache.set(cacheKey, snapshot)

  if (photoMemoryCache.size > MAX_MEMORY_PHOTO_CACHE) {
    const firstCacheKey = photoMemoryCache.keys().next().value
    if (firstCacheKey) {
      photoMemoryCache.delete(firstCacheKey)
    }
  }
}

function saveAlbumCache(activeId = activeAlbumId.value) {
  const snapshot: AlbumCacheSnapshot = {
    albums: cloneAlbums(photoAlbumList.value),
    activeAlbumId: activeId,
    savedAt: Date.now()
  }

  albumMemoryCache.value = snapshot
  writeSessionCache(ALBUM_CACHE_KEY, snapshot)
}

function savePhotoPageCache(albumId: number, page: number, pageSize: number, photos: PhotoResponseItem[]) {
  const cacheKey = getPhotoPageCacheKey(albumId, page, pageSize)
  const snapshot: PhotoPageCacheSnapshot = {
    albumId,
    current: page,
    size: pageSize,
    photos: clonePhotos(photos),
    savedAt: Date.now()
  }

  rememberPhotoPageCache(cacheKey, snapshot)
  writeSessionCache(cacheKey, snapshot)
}

function readAlbumCache() {
  return albumMemoryCache.value || readSessionCache<AlbumCacheSnapshot>(ALBUM_CACHE_KEY)
}

function readPhotoPageCache(albumId: number, page: number, pageSize: number) {
  const cacheKey = getPhotoPageCacheKey(albumId, page, pageSize)
  const memoryCache = photoMemoryCache.get(cacheKey)

  if (memoryCache) {
    return memoryCache
  }

  const sessionCache = readSessionCache<PhotoPageCacheSnapshot>(cacheKey)
  if (sessionCache) {
    rememberPhotoPageCache(cacheKey, sessionCache)
  }
  return sessionCache
}

function applyAlbumList(albums: PhotoAlbum[], preferredAlbumId?: number | null) {
  photoAlbumList.value = cloneAlbums(albums)
  assignAlbumTagStyles(photoAlbumList.value)

  if (!photoAlbumList.value.length) {
    activeAlbumId.value = null
    photoList.value = []
    return
  }

  const nextActiveAlbumId = preferredAlbumId ?? activeAlbumId.value
  const hasActiveAlbum = photoAlbumList.value.some((item) => item.id === nextActiveAlbumId)
  activeAlbumId.value = hasActiveAlbum ? nextActiveAlbumId : photoAlbumList.value[0].id
}

function applyPhotoPageCache(snapshot: PhotoPageCacheSnapshot | null, allowStale = false) {
  if (!snapshot) return false
  if (!allowStale && !isCacheFresh(snapshot.savedAt, PHOTO_CACHE_TTL)) return false

  photoList.value = clonePhotos(snapshot.photos)
  hasMore.value = snapshot.photos.length > 0
  syncActiveAlbumPhotoCount()
  return true
}

function restoreAlbumCache() {
  const snapshot = readAlbumCache()
  if (!snapshot) return false

  applyAlbumList(snapshot.albums, snapshot.activeAlbumId)
  return isCacheFresh(snapshot.savedAt, ALBUM_CACHE_TTL)
}

function restoreCurrentPhotoCache(allowStale = true) {
  if (!activeAlbumId.value) return false

  return applyPhotoPageCache(
    readPhotoPageCache(activeAlbumId.value, current.value, size.value),
    allowStale
  )
}

/**
 * 为当前页面的相册标签生成一组随机但观感稳定的高饱和颜色。
 *
 * @param albums 相册列表
 */
function assignAlbumTagStyles(albums: PhotoAlbum[]) {
  const styleMap: Record<number, Record<string, string>> = {}
  const hues = createRandomHueSequence(albums.length)

  albums.forEach((album, index) => {
    const hue = hues[index]
    const saturation = 66 + (index % 3) * 4
    const lightStart = 58 - (index % 2) * 4
    const lightEnd = 46 - (index % 3) * 3

    styleMap[album.id] = {
      '--album-tab-bg': `linear-gradient(135deg, hsl(${hue} 72% ${lightStart}%), hsl(${(hue + 18) % 360} 78% ${lightEnd}%))`,
      '--album-tab-border': `hsla(${hue} 78% 44% / 0.92)`,
      '--album-tab-color': '#ffffff',
      '--album-tab-shadow': `hsla(${hue} ${saturation}% 42% / 0.3)`
    }
  })

  albumTagStyleMap.value = styleMap
}

/**
 * 生成一组不重复的随机色相，保证同一页内标签颜色分散。
 *
 * @param count 标签数量
 * @returns 色相数组
 */
function createRandomHueSequence(count: number) {
  const hues: number[] = []
  const usedHues: number[] = []

  while (hues.length < count) {
    const hue = Math.floor(Math.random() * 360)
    if (usedHues.every((value) => Math.abs(value - hue) >= 28 && Math.abs(value - hue) <= 332)) {
      hues.push(hue)
      usedHues.push(hue)
      continue
    }

    if (usedHues.length >= 8) {
      hues.push(hue)
      usedHues.push(hue)
    }
  }
  return hues
}

/**
 * 获取相册标签样式，当前页面停留期间保持固定。
 *
 * @param albumId 相册 id
 * @param isActive 是否选中
 * @returns 标签样式
 */
function getAlbumTagStyle(albumId: number, isActive: boolean) {
  const baseStyle = albumTagStyleMap.value[albumId] || {
    '--album-tab-bg': 'linear-gradient(135deg, #36cfc9, #1677ff)',
    '--album-tab-border': 'rgba(22, 119, 255, 0.92)',
    '--album-tab-color': '#ffffff',
    '--album-tab-shadow': 'rgba(22, 119, 255, 0.3)'
  }
  return {
    ...baseStyle,
    '--album-tab-shadow': isActive
      ? String(baseStyle['--album-tab-shadow']).replace('/ 0.3)', '/ 0.42)')
      : baseStyle['--album-tab-shadow'],
    '--album-tab-opacity': isActive ? '1' : '0.92'
  }
}

function resetPhotoState() {
  current.value = 1
  photoList.value = []
  hasMore.value = true
}

function syncActiveAlbumPhotoCount() {
  const album = activeAlbum.value
  if (!album) return

  album.photoCount = Math.max(album.photoCount || 0, photoList.value.length)
}

async function listPhotoAlbums() {
  const hasFreshAlbumCache = restoreAlbumCache()
  const currentPhotoCache = activeAlbumId.value
    ? readPhotoPageCache(activeAlbumId.value, current.value, size.value)
    : null
  const hasPhotoCache = applyPhotoPageCache(currentPhotoCache, true)
  const hasFreshPhotoCache = currentPhotoCache
    ? isCacheFresh(currentPhotoCache.savedAt, PHOTO_CACHE_TTL)
    : false

  if (hasFreshAlbumCache && hasFreshPhotoCache) {
    return
  }

  if (hasFreshAlbumCache) {
    await loadPhotos({
      preferCache: true,
      silent: hasPhotoCache
    })
    return
  }

  try {
    const { data } = await request.get('/api/photos/albums')
    const preferredAlbumId = activeAlbumId.value
    applyAlbumList(data.data || [], preferredAlbumId)
    saveAlbumCache(activeAlbumId.value)

    if (activeAlbumId.value) {
      await loadPhotos({
        preferCache: true,
        silent: photoList.value.length > 0
      })
    }
  } catch (error) {
    console.error('获取相册列表失败:', error)
  }
}

async function loadPhotos(options: LoadPhotoOptions = {}) {
  if (!activeAlbumId.value || loading.value) return

  const albumId = activeAlbumId.value
  const page = current.value
  const pageSize = size.value
  const cacheKey = getPhotoPageCacheKey(albumId, page, pageSize)
  let restoredFromCache = false

  if (options.preferCache) {
    const cachedPage = readPhotoPageCache(albumId, page, pageSize)
    const hasFreshCache = applyPhotoPageCache(cachedPage)

    if (hasFreshCache && !options.force) {
      return
    }

    restoredFromCache = applyPhotoPageCache(cachedPage, true)
  }

  activePhotoRequestKey.value = cacheKey
  loading.value = !options.silent && !restoredFromCache
  try {
    const { data } = await request.get(`/api/albums/${albumId}/photos`, {
      params: {
        current: page,
        size: pageSize
      }
    })

    if (activePhotoRequestKey.value !== cacheKey) {
      return
    }

    const photos = data.data?.photoList || []
    photoList.value = photos
    hasMore.value = photos.length > 0
    syncActiveAlbumPhotoCount()
    saveAlbumCache(activeAlbumId.value)
    savePhotoPageCache(albumId, page, pageSize, photos)
  } catch (error) {
    console.error('获取照片失败:', error)
  } finally {
    if (activePhotoRequestKey.value === cacheKey) {
      loading.value = false
    }
  }
}

async function changeAlbum(album: PhotoAlbum) {
  if (album.id === activeAlbumId.value || loading.value) return

  activeAlbumId.value = album.id
  resetPhotoState()
  restoreCurrentPhotoCache(true)
  await nextTick()
  await loadPhotos({
    preferCache: true,
    silent: photoList.value.length > 0
  })
}

async function changePhotoPage(page: number) {
  if (loading.value || page < 1 || page > totalPages.value || page === current.value) return

  current.value = page
  const hasCachedPage = restoreCurrentPhotoCache(true)
  if (!hasCachedPage) {
    photoList.value = []
  }
  await nextTick()
  await loadPhotos({
    preferCache: true,
    silent: hasCachedPage
  })
  window.scrollTo({
    behavior: 'smooth',
    top: 140
  })
}

function getPhotoSrc(photo?: PhotoResponseItem) {
  return typeof photo === 'string' ? photo : photo?.photoSrc || ''
}

/**
 * 把阿里云 OSS 原图链接转换成「服务端缩放 + WebP 压缩」后的缩略图链接
 *
 * 背景：相册主页一次会渲染 18 张图片，如果直接用原图（手机/相机原图常常 1MB~5MB），
 *      浏览器需要并行 decode 大量大图，主线程会被 decode 阻塞导致卡顿。
 *      OSS 自带 IMG 服务，通过 URL 参数即可让 OSS 返回缩略图，无需改后端。
 *
 * @param url   原始图片 URL（任意 URL 都允许传入，非 OSS 会原样返回）
 * @param width 期望的宽度（像素），高度按原图比例自动计算
 * @returns 处理后的 URL；非阿里云 OSS 链接 / 空值会原样返回
 */
function getOssThumbUrl(url: string, width: number): string {
  // 防御性判断：空字符串 / undefined 直接返回，避免拼出畸形 URL
  if (!url) return url
  // 仅对阿里云 OSS 域名生效，外链 / 本地图等保持原样
  if (!url.includes('aliyuncs.com')) return url
  // 如果原 URL 已带查询串（如签名参数），改用 & 连接，避免覆盖原参数
  const sep = url.includes('?') ? '&' : '?'
  // x-oss-process 参数说明：
  //   resize,w_xxx     按宽度等比缩放
  //   quality,q_80     有损压缩到 80%（视觉几乎无损，体积大幅下降）
  //   format,webp      转 WebP 格式（同质量下体积约为 JPEG 的 1/3）
  return `${url}${sep}x-oss-process=image/resize,w_${width}/quality,q_80/format,webp`
}

function getPhotoTitle(photo: PhotoResponseItem, index: number) {
  const fallbackTitle = `${activeAlbum.value?.albumName || '照片'} ${index + 1}`
  if (typeof photo !== 'string' && photo.photoName) {
    return photo.photoName
  }

  const fileName = decodeURIComponent(getPhotoSrc(photo).split('/').pop()?.split('?')[0] || '')
  const title = fileName.replace(/\.[^.]+$/, '').replace(/[-_]+/g, ' ').trim()
  return title || fallbackTitle
}

function resetPreview() {
  previewScale.value = 1
  previewRotate.value = 0
}

function openPreview(index: number) {
  previewIndex.value = index
  previewVisible.value = true
  resetPreview()
}

function closePreview() {
  previewVisible.value = false
}

function showPrev() {
  if (!photoList.value.length) return
  previewIndex.value = (previewIndex.value - 1 + photoList.value.length) % photoList.value.length
  resetPreview()
}

function showNext() {
  if (!photoList.value.length) return
  previewIndex.value = (previewIndex.value + 1) % photoList.value.length
  resetPreview()
}

function zoomIn() {
  previewScale.value = Math.min(3, Number((previewScale.value + 0.2).toFixed(1)))
}

function zoomOut() {
  previewScale.value = Math.max(0.4, Number((previewScale.value - 0.2).toFixed(1)))
}

function rotateRight() {
  previewRotate.value = (previewRotate.value + 90) % 360
}

function downloadPhoto() {
  // 下载始终走原图，不能用 currentPreviewImage（那是 OSS 处理后的 WebP 缩略图）
  const photoUrl = getPhotoSrc(photoList.value[previewIndex.value])
  if (!photoUrl) return

  const link = document.createElement('a')
  link.href = photoUrl
  link.download = decodeURIComponent(photoUrl.split('/').pop()?.split('?')[0] || 'photo')
  link.target = '_blank'
  link.rel = 'noopener'
  link.click()
}

function handlePreviewKeydown(event: KeyboardEvent) {
  if (!previewVisible.value) return

  if (event.key === 'Escape') {
    closePreview()
  } else if (event.key === 'ArrowLeft') {
    showPrev()
  } else if (event.key === 'ArrowRight') {
    showNext()
  }
}

function bindPreviewKeydown() {
  if (previewKeydownBound) return

  window.addEventListener('keydown', handlePreviewKeydown)
  previewKeydownBound = true
}

function unbindPreviewKeydown() {
  if (!previewKeydownBound) return

  window.removeEventListener('keydown', handlePreviewKeydown)
  previewKeydownBound = false
}

onMounted(() => {
  listPhotoAlbums()
  bindPreviewKeydown()
})

onActivated(() => {
  bindPreviewKeydown()
})

onDeactivated(() => {
  closePreview()
  unbindPreviewKeydown()
})

onUnmounted(() => {
  unbindPreviewKeydown()
})
</script>

<style scoped>
.album-page {
  min-height: 100vh;
  padding: 18px 0 70px;
  background:
    radial-gradient(circle at 10% 12%, rgba(255, 210, 224, 0.32), transparent 26%),
    linear-gradient(180deg, #f5f7fb 0%, #eef2f7 100%);
}

.album-hero {
  position: relative;
  width: min(1180px, calc(100% - 48px));
  height: 120px;
  margin: 0 auto 34px;
  overflow: hidden;
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.18);
}

.album-hero__content {
  position: relative;
  z-index: 1;
  display: flex;
  height: 100%;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
  padding: 0 30px;
}

.album-hero__label {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.album-hero h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.03em;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.32);
}

.album-main {
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
}

.album-tabs {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 14px;
  margin-bottom: 24px;
}

.album-tab {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid var(--album-tab-border, rgba(229, 231, 235, 0.96));
  border-radius: 10px;
  color: var(--album-tab-color, #ffffff);
  background: var(--album-tab-bg, linear-gradient(135deg, #8b95a7 0%, #667085 100%));
  box-shadow: 0 10px 24px var(--album-tab-shadow, rgba(15, 23, 42, 0.18));
  opacity: var(--album-tab-opacity, 1);
  cursor: pointer;
  font-weight: 700;
  transition: box-shadow 0.2s ease, filter 0.2s ease, opacity 0.2s ease, transform 0.2s ease;
}

.album-tab__label {
  font-size: 15px;
  line-height: 1;
}

.album-tab strong {
  color: inherit;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.album-tab:hover,
.album-tab--active {
  color: var(--album-tab-color, #ffffff);
  box-shadow: 0 12px 28px var(--album-tab-shadow, rgba(15, 23, 42, 0.22));
  opacity: 1;
  filter: saturate(1.05);
}

.album-title {
  margin: 16px 0 26px;
  text-align: center;
  color: #050505;
  font-size: 30px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 28px;
}

.photo-card {
  overflow: hidden;
  padding: 8px;
  border: 1px solid rgba(203, 213, 225, 0.9);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.2);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  /* 让浏览器跳过视口外卡片的渲染/布局/绘制，显著降低长列表的合成成本 */
  content-visibility: auto;
  /* 配合 content-visibility 给浏览器一个尺寸预估，避免滚动条跳动 */
  contain-intrinsic-size: 360px 360px;
}

.photo-card:hover {
  border-color: rgba(148, 163, 184, 0.95);
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.2);
}

.photo-card:hover .photo-card__image {
  transform: scale(1.06);
}

.photo-card__image-wrap {
  overflow: hidden;
  aspect-ratio: 1 / 1.04;
  border-radius: 8px;
  background: #e5e7eb;
}

.photo-card__image {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.35s ease;
  /* 提示浏览器把 transform 提升为合成层，hover 缩放走 GPU，避免主线程重排 */
  will-change: transform;
}

.photo-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin: 34px 0 4px;
}

.photo-pagination__button {
  min-width: 88px;
  height: 38px;
  border: 1px solid rgba(148, 163, 184, 0.58);
  border-radius: 999px;
  color: #263445;
  background: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  font-weight: 700;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, color 0.2s ease;
}

.photo-pagination__button:not(:disabled):hover {
  color: #111827;
  border-color: rgba(73, 177, 245, 0.72);
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.18);
}

.photo-pagination__button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.photo-pagination__info {
  min-width: 72px;
  color: #64748b;
  text-align: center;
  font-size: 14px;
  font-weight: 800;
}

.empty-tip {
  padding: 42px 0;
  color: #8a8f98;
  text-align: center;
}

.photo-load-trigger {
  width: 100%;
  height: 1px;
}

.photo-preview {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: rgba(5, 10, 18, 0.88);
  backdrop-filter: blur(12px);
}

.photo-preview__stage {
  display: flex;
  align-items: center;
  justify-content: center;
  width: min(86vw, 1280px);
  height: min(80vh, 820px);
  overflow: hidden;
}

.photo-preview__image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  transition: transform 0.22s ease;
  transform-origin: center;
}

.photo-preview__toolbar {
  position: fixed;
  top: 22px;
  right: 22px;
  z-index: 2;
  display: flex;
  gap: 10px;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.58);
  box-shadow: 0 18px 46px rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(16px);
}

.photo-preview__btn,
.photo-preview__nav {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  color: rgba(255, 255, 255, 0.92);
  background: rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.photo-preview__btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
}

.photo-preview__btn:hover,
.photo-preview__nav:hover {
  background: rgba(73, 177, 245, 0.28);
  transform: translateY(-1px);
}

.photo-preview__nav {
  position: fixed;
  top: 50%;
  z-index: 2;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  transform: translateY(-50%);
}

.photo-preview__nav:hover {
  transform: translateY(calc(-50% - 1px));
}

.photo-preview__nav--prev {
  left: 28px;
}

.photo-preview__nav--next {
  right: 28px;
}

.photo-preview__counter {
  position: fixed;
  bottom: 24px;
  left: 50%;
  z-index: 2;
  padding: 8px 14px;
  border-radius: 999px;
  color: rgba(255, 255, 255, 0.88);
  background: rgba(15, 23, 42, 0.62);
  transform: translateX(-50%);
  font-size: 13px;
  backdrop-filter: blur(16px);
}

.dark .album-page {
  background:
    radial-gradient(circle at 10% 12%, rgba(73, 177, 245, 0.12), transparent 26%),
    linear-gradient(180deg, #111827 0%, #0f172a 100%);
}

.dark .album-tab {
  color: var(--album-tab-color, #ffffff);
}

.dark .album-tab strong {
  color: inherit;
}

.dark .album-tab:hover,
.dark .album-tab--active {
  color: var(--album-tab-color, #ffffff);
  box-shadow: 0 14px 30px var(--album-tab-shadow, rgba(0, 0, 0, 0.3));
}

.dark .album-title {
  color: rgba(248, 250, 252, 0.95);
}

.dark .photo-card {
  border-color: rgba(71, 85, 105, 0.78);
  background: rgba(15, 23, 42, 0.86);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.34);
}

.dark .photo-card:hover {
  border-color: rgba(125, 211, 252, 0.45);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.38);
}

.dark .photo-pagination__button {
  color: rgba(226, 232, 240, 0.86);
  border-color: rgba(71, 85, 105, 0.84);
  background: rgba(15, 23, 42, 0.78);
}

.dark .photo-pagination__button:not(:disabled):hover {
  color: #fff;
  border-color: rgba(125, 211, 252, 0.52);
}

.dark .photo-pagination__info {
  color: rgba(203, 213, 225, 0.72);
}

@media (max-width: 960px) {
  .photo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
  }
}

@media (max-width: 759px) {
  .album-page {
    padding-top: 12px;
  }

  .album-hero,
  .album-main {
    width: calc(100% - 24px);
  }

  .album-hero {
    height: 108px;
    margin-bottom: 24px;
    border-radius: 14px;
  }

  .album-hero__content {
    gap: 10px;
    padding: 0 20px;
  }

  .album-hero h1 {
    font-size: 17px;
  }

  .album-tabs {
    justify-content: flex-start;
    gap: 10px;
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 4px;
  }

  .album-tab {
    flex: 0 0 auto;
    min-height: 38px;
    padding: 0 13px;
  }

  .album-title {
    margin: 14px 0 20px;
    font-size: 24px;
  }

  .photo-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .photo-card__image-wrap {
    aspect-ratio: 1 / 0.84;
  }

  .photo-preview__toolbar {
    top: auto;
    right: 12px;
    bottom: 18px;
    left: 12px;
    justify-content: center;
    border-radius: 18px;
  }

  .photo-preview__btn {
    width: 36px;
    height: 36px;
  }

  .photo-preview__nav {
    width: 44px;
    height: 44px;
  }

  .photo-preview__nav--prev {
    left: 10px;
  }

  .photo-preview__nav--next {
    right: 10px;
  }

  .photo-preview__counter {
    bottom: 82px;
  }
}
</style>
