<template>
  <main class="music-page">
    <section class="music-intro" aria-labelledby="music-title">
      <div class="music-intro__copy">
        <span class="section-kicker">
          <v-icon size="16">mdi-music-note-eighth</v-icon>
          SOUND NOTES
        </span>
        <h1 id="music-title">把喜欢的声音，留在这里</h1>
        <p>一些适合写字、散步和发呆时播放的歌。页面保持轻量，点开一张卡片，就能听见它。</p>
        <div class="music-intro__meta">
          <span><strong>{{ tracks.length }}</strong> 首音乐</span>
          <span class="meta-divider"></span>
          <span>点击卡片即可试听</span>
        </div>
      </div>

      <div class="music-intro__action">
        <button class="upload-button" type="button" @click="openUpload">
          <v-icon size="19">mdi-plus</v-icon>
          添加音乐
        </button>
        <span>管理员可添加 · MP3、WAV、M4A</span>
        <input
          ref="fileInput"
          class="sr-only"
          type="file"
          accept=".mp3,.wav,.m4a,audio/mpeg,audio/wav,audio/x-wav,audio/mp4"
          multiple
          @change="handleUpload"
        />
      </div>
    </section>

    <section class="music-toolbar" aria-label="音乐筛选">
      <div class="filter-list" role="tablist" aria-label="音乐分类">
        <button
          v-for="category in categories"
          :key="category"
          class="filter-button"
          :class="{ 'filter-button--active': selectedCategory === category }"
          type="button"
          role="tab"
          :aria-selected="selectedCategory === category"
          @click="selectedCategory = category"
        >
          {{ category }}
        </button>
      </div>

      <label class="search-field">
        <v-icon size="18">mdi-magnify</v-icon>
        <input v-model="searchQuery" type="search" placeholder="搜索歌名或艺术家" />
      </label>
    </section>

    <div v-if="currentTrack" class="listen-status" aria-live="polite">
      <span class="listen-status__dot" :class="{ 'listen-status__dot--playing': isPlaying }"></span>
      <span class="listen-status__label">{{ isPlaying ? '正在试听' : '最近播放' }}</span>
      <strong>{{ currentTrack.title }}</strong>
      <span class="listen-status__artist">{{ currentTrack.artist }}</span>
      <button
        class="status-control"
        type="button"
        :aria-label="isPlaying ? '暂停播放' : '继续播放'"
        :title="isPlaying ? '暂停播放' : '继续播放'"
        @click="togglePlayback"
      >
        <v-icon size="18">{{ isPlaying ? 'mdi-pause' : 'mdi-play' }}</v-icon>
      </button>
      <span class="listen-status__time">{{ formatTime(currentTime) }}</span>
      <div class="listen-status__progress" aria-hidden="true">
        <span :style="{ width: `${progressPercent}%` }"></span>
      </div>
      <span v-if="playbackNotice" class="listen-status__notice">{{ playbackNotice }}</span>
    </div>

    <section class="music-grid" aria-label="音乐收藏">
      <button class="upload-card" type="button" @click="openUpload">
        <span class="upload-card__icon"><v-icon size="27">mdi-upload-outline</v-icon></span>
        <span>
          <strong>放一首新歌</strong>
          <small>登录后由管理员添加</small>
        </span>
        <v-icon class="upload-card__arrow" size="18">mdi-arrow-up-right</v-icon>
      </button>

      <article
        v-for="(track, index) in filteredTracks"
        :key="track.id"
        class="track-card"
        :class="{ 'track-card--active': currentTrackId === track.id }"
        @click="playTrack(track)"
      >
        <div class="track-card__cover-wrap">
          <img class="track-card__cover" :src="track.cover" :alt="`${track.title} 封面`" />
          <span class="track-card__index">{{ String(index + 1).padStart(2, '0') }}</span>
          <button
            class="track-card__play"
            type="button"
            :aria-label="currentTrackId === track.id && isPlaying ? `暂停 ${track.title}` : `播放 ${track.title}`"
            :title="currentTrackId === track.id && isPlaying ? '暂停' : '播放'"
            @click.stop="playTrack(track)"
          >
            <v-icon size="22">
              {{ currentTrackId === track.id && isPlaying ? 'mdi-pause' : 'mdi-play' }}
            </v-icon>
          </button>
        </div>
        <div class="track-card__body">
          <div class="track-card__heading">
            <h2>{{ track.title }}</h2>
            <span>{{ track.duration }}</span>
          </div>
          <p>{{ track.artist }}</p>
          <div class="track-card__tags">
            <span>{{ track.category }}</span>
            <span v-for="tag in track.tags" :key="tag">{{ tag }}</span>
          </div>
        </div>
      </article>
    </section>

    <div v-if="filteredTracks.length === 0" class="empty-state">
      <v-icon size="30">mdi-music-note-off-outline</v-icon>
      <strong>还没有音乐</strong>
      <span>管理员上传音乐后，会显示在这里。</span>
    </div>

<!--    <p class="music-footnote">-->
<!--      <v-icon size="16">mdi-information-outline</v-icon>-->
<!--      音乐文件保存到文件服务器的 music 文件夹，名称和链接会记录在数据库中。-->
<!--    </p>-->

    <audio
      ref="audioRef"
      :src="currentTrack?.audio"
      preload="metadata"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleMetadata"
      @play="handlePlay"
      @pause="handlePause"
      @ended="handleEnded"
      @error="handleAudioError"
    ></audio>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getMusicList, uploadMusic, type MusicRecord } from '@/api/music'
import { useUserStore } from '@/stores/user'
import { openLoginRequiredPrompt } from '@/utils/authPrompt'
import { useToast } from '@/composables/useToast'

type Track = {
  id: number
  title: string
  artist: string
  duration: string
  durationSeconds: number
  category: string
  tags: string[]
  cover: string
  audio: string
  featured?: boolean
  uploaded?: boolean
}

const categories = ['全部', '精选']
const route = useRoute()
const userStore = useUserStore()
const selectedCategory = ref('全部')
const searchQuery = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const audioRef = ref<HTMLAudioElement | null>(null)
const tracks = ref<Track[]>([])

const currentTrackId = ref<number | null>(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const playbackNotice = ref('')
const canManageMusic = computed(() => userStore.isLoggedIn && userStore.isAdmin)

const currentTrack = computed(() => tracks.value.find(track => track.id === currentTrackId.value) || null)
const filteredTracks = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  return tracks.value.filter(track => {
    const categoryMatches =
      selectedCategory.value === '全部' ||
      (selectedCategory.value === '精选' ? track.featured : track.category === selectedCategory.value)
    const keywordMatches = !keyword || `${track.title} ${track.artist} ${track.tags.join(' ')}`.toLowerCase().includes(keyword)
    return categoryMatches && keywordMatches
  })
})

const progressPercent = computed(() => {
  if (!currentTrack.value?.durationSeconds) return 0
  return Math.min((currentTime.value / currentTrack.value.durationSeconds) * 100, 100)
})

function toUploadedTrack(record: MusicRecord): Track {
  const extension = record.musicUrl.split('?')[0].split('.').pop()?.toUpperCase() || 'AUDIO'
  return {
    id: -record.id,
    title: record.musicName,
    artist: '管理员上传',
    duration: '读取中',
    durationSeconds: 0,
    category: '精选',
    tags: ['上传', extension],
    cover: 'https://images.unsplash.com/photo-1524230572899-a752b3835840?auto=format&fit=crop&w=720&q=85',
    audio: record.musicUrl,
    featured: true,
    uploaded: true
  }
}

async function loadMusicList() {
  try {
    const response = await getMusicList()
    const musicList = (response.data?.data || []) as MusicRecord[]
    tracks.value = [
      ...musicList.map(toUploadedTrack),
      ...tracks.value.filter(track => !track.uploaded)
    ]
  } catch {
    // 请求拦截器会显示服务端返回的错误信息。
  }
}

function openUpload() {
  if (!userStore.isLoggedIn) {
    openLoginRequiredPrompt({
      message: '登录后才能添加音乐',
      redirect: route.fullPath
    })
    return
  }
  if (!userStore.isAdmin) {
    useToast({ type: 'warning', message: '只有管理员可以添加音乐' })
    return
  }
  fileInput.value?.click()
}

async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (!canManageMusic.value) {
    input.value = ''
    return
  }
  const files = Array.from(input.files || [])
  if (files.length === 0) return

  for (const file of files) {
    try {
      const response = await uploadMusic(file)
      const musicRecord = response.data?.data as MusicRecord | undefined
      if (!musicRecord?.id || !musicRecord.musicUrl) {
        useToast({ type: 'error', message: response.data?.message || '音乐上传失败' })
        continue
      }
      tracks.value.unshift(toUploadedTrack(musicRecord))
      useToast({ type: 'success', message: `${file.name} 上传成功` })
    } catch {
      // 请求拦截器会显示服务端返回的错误信息。
    }
  }
  if (files.length > 0) {
    selectedCategory.value = '全部'
    searchQuery.value = ''
  }
  input.value = ''
}

onMounted(loadMusicList)

async function playTrack(track: Track) {
  playbackNotice.value = ''
  if (currentTrackId.value !== track.id) {
    currentTrackId.value = track.id
    currentTime.value = 0
    isPlaying.value = false
    await nextTick()
    audioRef.value?.load()
  }

  const audio = audioRef.value
  if (!audio) return
  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
    return
  }

  try {
    await audio.play()
    isPlaying.value = true
  } catch {
    isPlaying.value = false
    playbackNotice.value = '音频暂时无法加载，请检查文件或网络'
  }
}

function togglePlayback() {
  if (currentTrack.value) playTrack(currentTrack.value)
}

function handleTimeUpdate() {
  currentTime.value = audioRef.value?.currentTime || 0
}

function handleMetadata() {
  const audio = audioRef.value
  if (!audio || !currentTrack.value || !Number.isFinite(audio.duration)) return
  currentTrack.value.durationSeconds = audio.duration
  currentTrack.value.duration = formatTime(audio.duration)
}

function handlePlay() {
  isPlaying.value = true
}

function handlePause() {
  isPlaying.value = false
}

function handleEnded() {
  isPlaying.value = false
  currentTime.value = 0
}

function handleAudioError() {
  isPlaying.value = false
  playbackNotice.value = '音频暂时无法加载，请检查文件或网络'
}

function formatTime(seconds: number) {
  if (!Number.isFinite(seconds) || seconds < 0) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainder = Math.floor(seconds % 60)
  return `${String(minutes).padStart(2, '0')}:${String(remainder).padStart(2, '0')}`
}

onBeforeUnmount(() => {
  audioRef.value?.pause()
})
</script>

<style scoped>
.music-page {
  --music-ink: #17212b;
  --music-muted: #6c7986;
  --music-line: rgba(33, 49, 63, 0.12);
  --music-accent: #dc694b;
  --music-accent-soft: #fff0e9;
  max-width: 1180px;
  min-height: calc(100vh - 60px);
  padding: 92px 32px 72px;
  margin: 0 auto;
  color: var(--music-ink);
}

.music-intro {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  padding-bottom: 42px;
  border-bottom: 1px solid var(--music-line);
}

.music-intro__copy {
  max-width: 660px;
}

.section-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--music-accent);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.music-intro h1 {
  margin: 13px 0 12px;
  color: var(--music-ink);
  font-size: clamp(32px, 5vw, 58px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.08;
}

.music-intro p {
  max-width: 520px;
  margin: 0;
  color: var(--music-muted);
  font-size: 15px;
  line-height: 1.9;
}

.music-intro__meta {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 23px;
  color: var(--music-muted);
  font-size: 12px;
}

.music-intro__meta strong {
  color: var(--music-ink);
  font-size: 15px;
}

.meta-divider {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--music-accent);
}

.music-intro__action {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  padding-bottom: 3px;
  color: var(--music-muted);
  font-size: 11px;
}

.upload-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 17px;
  border: 1px solid var(--music-accent);
  border-radius: 8px;
  background: var(--music-accent);
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 800;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.upload-button:hover {
  background: #c95639;
  box-shadow: 0 9px 20px rgba(220, 105, 75, 0.2);
  transform: translateY(-2px);
}

.music-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 25px 0 20px;
}

.filter-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-button {
  min-height: 31px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: var(--music-muted);
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.filter-button:hover {
  border-color: var(--music-line);
  color: var(--music-ink);
}

.filter-button--active {
  border-color: rgba(220, 105, 75, 0.2);
  background: var(--music-accent-soft);
  color: var(--music-accent);
  font-weight: 800;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(100%, 235px);
  min-height: 36px;
  padding: 0 12px;
  border-bottom: 1px solid var(--music-line);
  color: var(--music-muted);
}

.search-field input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--music-ink);
  font: inherit;
  font-size: 12px;
}

.search-field input::placeholder {
  color: #9aa4ad;
}

.listen-status {
  display: flex;
  align-items: center;
  gap: 9px;
  min-height: 37px;
  margin-bottom: 18px;
  padding: 8px 12px;
  border-left: 2px solid var(--music-accent);
  background: color-mix(in srgb, var(--music-accent-soft) 72%, transparent);
  color: var(--music-muted);
  font-size: 12px;
}

.listen-status__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #b7c0c7;
}

.listen-status__dot--playing {
  background: var(--music-accent);
  box-shadow: 0 0 0 4px rgba(220, 105, 75, 0.14);
}

.listen-status__label {
  color: var(--music-accent);
  font-weight: 800;
}

.listen-status strong {
  overflow: hidden;
  color: var(--music-ink);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.listen-status__artist {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 27px;
  height: 27px;
  margin-left: auto;
  border: 1px solid var(--music-line);
  border-radius: 50%;
  background: var(--bg-primary);
  color: var(--music-ink);
  cursor: pointer;
}

.listen-status__time {
  flex-shrink: 0;
  min-width: 37px;
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.listen-status__progress {
  width: 80px;
  height: 2px;
  overflow: hidden;
  background: rgba(33, 49, 63, 0.12);
}

.listen-status__progress span {
  display: block;
  height: 100%;
  background: var(--music-accent);
  transition: width 0.2s linear;
}

.listen-status__notice {
  color: var(--music-accent);
  font-size: 11px;
}

.music-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 15px;
}

.upload-card,
.track-card {
  min-width: 0;
  border: 1px solid var(--music-line);
  border-radius: 8px;
  background: var(--surface-base);
}

.upload-card {
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: 102px;
  padding: 15px;
  color: var(--music-ink);
  cursor: pointer;
  font: inherit;
  text-align: left;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.upload-card:hover {
  border-color: rgba(220, 105, 75, 0.42);
  background: var(--music-accent-soft);
  transform: translateY(-2px);
}

.upload-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  border: 1px dashed rgba(220, 105, 75, 0.5);
  border-radius: 7px;
  color: var(--music-accent);
}

.upload-card strong,
.upload-card small {
  display: block;
}

.upload-card strong {
  font-size: 13px;
}

.upload-card small {
  margin-top: 3px;
  color: var(--music-muted);
  font-size: 11px;
}

.upload-card__arrow {
  margin-left: auto;
  color: var(--music-muted);
}

.track-card {
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: 102px;
  padding: 11px;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.track-card:hover,
.track-card--active {
  border-color: rgba(220, 105, 75, 0.4);
  box-shadow: 0 9px 22px rgba(33, 49, 63, 0.09);
  transform: translateY(-2px);
}

.track-card--active {
  background: linear-gradient(120deg, var(--surface-base), var(--music-accent-soft));
}

.track-card__cover-wrap {
  position: relative;
  width: 79px;
  height: 79px;
  flex: 0 0 79px;
  overflow: hidden;
  border-radius: 6px;
  background: #d9e0e2;
}

.track-card__cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease, filter 0.35s ease;
}

.track-card:hover .track-card__cover,
.track-card--active .track-card__cover {
  filter: saturate(0.8) brightness(0.75);
  transform: scale(1.06);
}

.track-card__index {
  position: absolute;
  top: 6px;
  left: 7px;
  color: rgba(255, 255, 255, 0.9);
  font-family: Consolas, monospace;
  font-size: 10px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
}

.track-card__play {
  position: absolute;
  inset: 50% auto auto 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  background: rgba(22, 29, 33, 0.48);
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.8);
  transition: opacity 0.2s ease, transform 0.2s ease, background 0.2s ease;
}

.track-card:hover .track-card__play,
.track-card--active .track-card__play {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}

.track-card__play:hover {
  background: var(--music-accent);
}

.track-card__body {
  min-width: 0;
  flex: 1;
}

.track-card__heading {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.track-card h2 {
  overflow: hidden;
  margin: 0;
  color: var(--music-ink);
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-card__heading span {
  flex-shrink: 0;
  color: var(--music-muted);
  font-family: Consolas, monospace;
  font-size: 10px;
}

.track-card p {
  overflow: hidden;
  margin: 4px 0 10px;
  color: var(--music-muted);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.track-card__tags span {
  padding: 2px 6px;
  border-radius: 3px;
  background: var(--bg-secondary);
  color: var(--music-muted);
  font-size: 9px;
}

.track-card__tags span:first-child {
  background: var(--music-accent-soft);
  color: var(--music-accent);
}

.empty-state {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 7px;
  padding: 70px 20px;
  color: var(--music-muted);
  text-align: center;
}

.empty-state strong {
  color: var(--music-ink);
  font-size: 14px;
}

.empty-state span {
  font-size: 12px;
}

.music-footnote {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 32px 0 0;
  color: var(--music-muted);
  font-size: 11px;
  text-align: center;
}

audio {
  display: none;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.dark .music-page {
  --music-ink: #f0f4f6;
  --music-muted: #a6b0b8;
  --music-line: rgba(194, 208, 220, 0.16);
  --music-accent-soft: rgba(220, 105, 75, 0.14);
}

@media (max-width: 900px) {
  .music-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .music-page {
    padding: 78px 17px 50px;
  }

  .music-intro {
    align-items: flex-start;
    flex-direction: column;
    gap: 24px;
    padding-bottom: 30px;
  }

  .music-intro__action {
    align-items: flex-start;
  }

  .music-intro h1 {
    font-size: 37px;
  }

  .music-toolbar {
    align-items: stretch;
    flex-direction: column;
    gap: 17px;
  }

  .search-field {
    width: 100%;
  }

  .listen-status {
    flex-wrap: wrap;
    padding: 9px 10px;
  }

  .listen-status__artist {
    max-width: 100px;
  }

  .listen-status__progress {
    order: 5;
    width: 100%;
    flex-basis: 100%;
  }

  .listen-status__notice {
    order: 6;
    flex-basis: 100%;
  }

  .music-grid {
    grid-template-columns: 1fr;
  }
}
</style>
