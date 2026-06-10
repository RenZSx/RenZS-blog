<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">{{ photoAlbumName }}</h1>
    </div>
    <!-- 相册列表 -->
    <v-card class="blog-container photo-container">
      <div class="photo-wrap">
        <img
          v-for="(item, index) of visiblePhotoList"
          class="photo"
          :key="index"
          :src="getPhotoSrc(item)"
          :alt="getPhotoTitle(item, index)"
          loading="lazy"
          decoding="async"
          @click="openPreview(index)"
        />
      </div>
      <div
        v-if="hasRenderableMore"
        ref="loadMoreTriggerRef"
        class="photo-load-trigger"
        aria-hidden="true"
      />
      <div v-if="!loading && photoList.length === 0" class="empty-tip">
        暂无照片
      </div>
    </v-card>

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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()

interface PhotoItem {
  id?: number
  photoName?: string
  photoDesc?: string
  photoSrc: string
}

type PhotoResponseItem = string | PhotoItem

const photoAlbumName = ref('')
const photoAlbumCover = ref('')
const photoList = ref<PhotoResponseItem[]>([])
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const hasMore = ref(true)
const renderBatchSize = 24
const visiblePhotoCount = ref(renderBatchSize)
const loadMoreTriggerRef = ref<HTMLElement | null>(null)
let loadMoreObserver: IntersectionObserver | null = null
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewScale = ref(1)
const previewRotate = ref(0)

const cover = computed(() => {
  return `background: url(${photoAlbumCover.value}) center center / cover no-repeat`
})

const currentPreviewImage = computed(() => getPhotoSrc(photoList.value[previewIndex.value]))
const visiblePhotoList = computed(() => photoList.value.slice(0, visiblePhotoCount.value))
const hasRenderableMore = computed(() => visiblePhotoCount.value < photoList.value.length || hasMore.value)
const previewImageStyle = computed(() => ({
  transform: `scale(${previewScale.value}) rotate(${previewRotate.value}deg)`
}))

function resetPreview() {
  previewScale.value = 1
  previewRotate.value = 0
}

function getPhotoSrc(photo?: PhotoResponseItem) {
  return typeof photo === 'string' ? photo : photo?.photoSrc || ''
}

function getPhotoTitle(photo: PhotoResponseItem, index: number) {
  if (typeof photo !== 'string' && photo.photoName) {
    return photo.photoName
  }

  const fileName = decodeURIComponent(getPhotoSrc(photo).split('/').pop()?.split('?')[0] || '')
  const title = fileName.replace(/\.[^.]+$/, '').replace(/[-_]+/g, ' ').trim()
  return title || `${photoAlbumName.value || '照片'} ${index + 1}`
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
  const photoUrl = currentPreviewImage.value
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

function revealLoadedPhotos() {
  visiblePhotoCount.value = Math.min(
    visiblePhotoCount.value + renderBatchSize,
    photoList.value.length
  )
}

async function loadPhotos() {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const { data } = await request.get(`/api/albums/${route.params.albumId}/photos`, {
      params: {
        current: current.value,
        size: size.value
      }
    })

    photoAlbumCover.value = data.data?.photoAlbumCover || ''
    photoAlbumName.value = data.data?.photoAlbumName || ''

    const photos = data.data?.photoList || []
    if (photos.length) {
      current.value++
      photoList.value.push(...photos)
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取照片失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadMoreForScroll() {
  if (visiblePhotoCount.value < photoList.value.length) {
    revealLoadedPhotos()
    return
  }

  await loadPhotos()
}

watch(loadMoreTriggerRef, (element) => {
  if (element) {
    loadMoreObserver?.observe(element)
  }
})

onMounted(() => {
  loadPhotos()
  loadMoreObserver = new IntersectionObserver(
    (entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        void loadMoreForScroll()
      }
    },
    {
      rootMargin: '360px 0px'
    }
  )
  if (loadMoreTriggerRef.value) {
    loadMoreObserver.observe(loadMoreTriggerRef.value)
  }
  window.addEventListener('keydown', handlePreviewKeydown)
})

onUnmounted(() => {
  loadMoreObserver?.disconnect()
  loadMoreObserver = null
  window.removeEventListener('keydown', handlePreviewKeydown)
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 280px;
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
  bottom: 40px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.photo-container {
  max-width: 1100px;
  padding: 30px;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.photo-wrap {
  display: flex;
  flex-wrap: wrap;
}

.photo {
  margin: 3px;
  cursor: pointer;
  flex-grow: 1;
  object-fit: cover;
  height: 200px;
  content-visibility: auto;
  contain-intrinsic-size: 200px 300px;
}

.photo-wrap::after {
  content: "";
  display: block;
  flex-grow: 9999;
}

.no-more,
.empty-tip {
  color: #999;
  font-size: 14px;
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

@media (max-width: 759px) {
  .banner {
    height: 220px;
  }

  .banner-title {
    font-size: 1.5rem;
  }

  .photo-container {
    margin: -40px 10px 20px;
    padding: 15px;
  }

  .photo {
    width: calc(50% - 6px);
    height: 150px;
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
