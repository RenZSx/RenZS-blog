<template>
  <main class="music-page">
    <div class="music-container">
      <section class="listening-room" aria-labelledby="music-title">
        <div class="room-heading">
          <span class="room-brand">
            <v-icon size="18">mdi-music-note-eighth</v-icon>
            MY LITTLE MUSIC SPACE
          </span>
          <span class="room-edition">VOL. 01 <i></i> EVERYDAY SOUNDS</span>
        </div>

        <div class="room-main">
          <div class="room-copy">
            <span class="room-eyebrow"><i></i> 给生活一点背景音乐</span>
            <h1 id="music-title">把日常，<br /><em>调频成喜欢的声音。</em></h1>
            <p>写字、散步，或者只是发一会儿呆。<br />挑一首歌，让这一刻慢下来。</p>
            <button class="listen-cta" type="button" :disabled="tracks.length === 0" @click="startListening">
              <span><v-icon size="18">{{ isPlaying ? 'mdi-pause' : 'mdi-play' }}</v-icon></span>
              {{ isPlaying ? '暂停，歇一会儿' : currentTrack ? '继续聆听' : '开始今天的播放' }}
            </button>
          </div>

          <div class="album-scene" :class="{ 'album-scene--playing': isPlaying }">
            <div class="album-orbit" aria-hidden="true"></div>
            <div class="album-art">
              <img v-if="displayTrack" :src="displayTrack.cover" :alt="`${displayTrack.title} 封面`" />
              <div v-else class="album-art__placeholder">
                <v-icon size="58">mdi-music-note-outline</v-icon>
              </div>
              <div class="album-art__top">
                <span>{{ isPlaying ? 'NOW PLAYING' : 'A MOMENT FOR YOU' }}</span>
                <v-icon size="17">mdi-music-note</v-icon>
              </div>
              <div class="album-art__caption">
                <span>{{ displayTrack ? displayTrack.artist : 'MY DAILY PLAYLIST' }}</span>
                <strong>{{ displayTrack ? displayTrack.title : '等一首喜欢的歌' }}</strong>
              </div>
            </div>
            <span class="album-sticker"><v-icon size="15">mdi-headphones</v-icon> JUST LISTEN</span>
          </div>
        </div>

        <div class="player-deck">
          <div class="player-track">
            <span class="player-status" :class="{ 'player-status--active': isPlaying }">
              <i></i><i></i><i></i><i></i>
            </span>
            <div>
              <span class="player-label">{{ isPlaying ? '正在播放' : currentTrack ? '继续这份好心情' : '你的私人歌单' }}</span>
              <p><strong>{{ currentTrack?.title || '选一首喜欢的音乐' }}</strong><span>{{ currentTrack?.artist || `${tracks.length} 首声音，等你发现` }}</span></p>
            </div>
          </div>

          <div class="player-controls">
            <button type="button" aria-label="上一首" title="上一首" :disabled="tracks.length === 0" @click="playPrevious">
              <v-icon size="20">mdi-skip-previous</v-icon>
            </button>
            <button
              class="player-controls__main"
              type="button"
              :aria-label="isPlaying ? '暂停' : '播放'"
              :title="isPlaying ? '暂停' : '播放'"
              :disabled="tracks.length === 0"
              @click="startListening"
            >
              <v-icon size="23">{{ isPlaying ? 'mdi-pause' : 'mdi-play' }}</v-icon>
            </button>
            <button type="button" aria-label="下一首" title="下一首" :disabled="tracks.length === 0" @click="playNext">
              <v-icon size="20">mdi-skip-next</v-icon>
            </button>
          </div>

          <div class="player-timeline">
            <span>{{ formatTime(currentTime) }}</span>
            <input
              class="player-range"
              type="range"
              min="0"
              :max="Math.max(currentTrack?.durationSeconds || 0, 1)"
              step="0.1"
              :value="currentTime"
              :disabled="!currentTrack?.durationSeconds"
              :style="{ '--range-progress': `${progressPercent}%` }"
              aria-label="播放进度"
              @input="seekTrack"
            />
            <span>{{ currentTrack?.duration || '00:00' }}</span>
          </div>

          <div class="player-tools">
            <span class="playback-notice" aria-live="polite">{{ playbackNotice || '好听的歌，值得慢慢听。' }}</span>
            <div class="volume-control">
              <button type="button" :aria-label="volume === 0 ? '取消静音' : '静音'" @click="toggleMute">
                <v-icon size="18">{{ volume === 0 ? 'mdi-volume-off' : volume < 0.5 ? 'mdi-volume-low' : 'mdi-volume-high' }}</v-icon>
              </button>
              <input
                class="player-range"
                type="range"
                min="0"
                max="1"
                step="0.01"
                :value="volume"
                :style="{ '--range-progress': `${volume * 100}%` }"
                aria-label="音量"
                @input="changeVolume"
              />
            </div>
          </div>
        </div>
      </section>

      <section class="music-library" aria-labelledby="library-title">
        <div class="library-heading">
          <div>
            <span class="library-kicker">A SONG FOR EVERY MOOD</span>
            <h2 id="library-title">下一首，也不错。</h2>
          </div>
          <button class="upload-button" type="button" @click="openUpload">
            <v-icon size="18">mdi-plus</v-icon>
            添加音乐
          </button>
          <input
            ref="fileInput"
            class="sr-only"
            type="file"
            accept=".mp3,.wav,.m4a,audio/mpeg,audio/wav,audio/x-wav,audio/mp4"
            multiple
            @change="handleUpload"
          />
        </div>

        <div class="music-toolbar" aria-label="音乐筛选">
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
          <div class="toolbar-right">
            <span class="track-count">{{ filteredTracks.length }} 首音乐</span>
            <label class="search-field">
              <v-icon size="18">mdi-magnify</v-icon>
              <input v-model="searchQuery" type="search" placeholder="搜索歌名或艺术家" aria-label="搜索音乐" />
            </label>
          </div>
        </div>

        <div v-if="filteredTracks.length" class="track-list" aria-label="音乐收藏">
          <button
            v-for="(track, index) in filteredTracks"
            :key="track.id"
            class="track-item"
            :class="{ 'track-item--active': currentTrackId === track.id }"
            type="button"
            :aria-label="currentTrackId === track.id && isPlaying ? `暂停 ${track.title}` : `播放 ${track.title}`"
            @click="playTrack(track)"
          >
            <span class="track-item__number">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="track-item__cover">
              <img :src="track.cover" :alt="''" />
              <v-icon size="20">{{ currentTrackId === track.id && isPlaying ? 'mdi-pause' : 'mdi-play' }}</v-icon>
            </span>
            <span class="track-item__info">
              <strong>{{ track.title }}</strong>
              <small>{{ track.artist }}</small>
            </span>
            <span class="track-item__tag">{{ track.category }}</span>
            <span class="track-item__duration">{{ track.duration }}</span>
            <span class="track-item__action">
              <v-icon size="17">{{ currentTrackId === track.id && isPlaying ? 'mdi-pause' : 'mdi-play' }}</v-icon>
            </span>
          </button>
        </div>

        <div v-else class="empty-state">
          <span><v-icon size="30">mdi-music-note-outline</v-icon></span>
          <strong>{{ tracks.length ? '没有找到这段旋律' : '歌单正在等第一首歌' }}</strong>
          <p>{{ tracks.length ? '换个关键词试试，也许会遇见新的惊喜。' : '管理员上传音乐后，会在这里与你见面。' }}</p>
          <button v-if="tracks.length" type="button" @click="resetFilters">查看全部音乐</button>
        </div>
      </section>

      <footer class="music-footnote">
        <span>不必刻意寻找意义，这一刻好听就够了。</span>
        <span>MADE FOR SLOW MOMENTS <v-icon size="14">mdi-music-note</v-icon></span>
      </footer>
    </div>

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
const volume = ref(0.72)
const previousVolume = ref(0.72)
const canManageMusic = computed(() => userStore.isLoggedIn && userStore.isAdmin)
const currentTrack = computed(() => tracks.value.find(track => track.id === currentTrackId.value) || null)
const displayTrack = computed(() => currentTrack.value || tracks.value[0] || null)
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
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1000&q=85',
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
    openLoginRequiredPrompt({ message: '登录后才能添加音乐', redirect: route.fullPath })
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

function resetFilters() {
  selectedCategory.value = '全部'
  searchQuery.value = ''
}

function startListening() {
  if (currentTrack.value) {
    playTrack(currentTrack.value)
  } else if (tracks.value.length) {
    playTrack(tracks.value[0])
  }
}

function playAdjacentTrack(offset: number) {
  if (!tracks.value.length) return
  const currentIndex = tracks.value.findIndex(track => track.id === currentTrackId.value)
  const nextIndex = (currentIndex + offset + tracks.value.length) % tracks.value.length
  playTrack(tracks.value[nextIndex])
}

function playPrevious() {
  playAdjacentTrack(-1)
}

function playNext() {
  playAdjacentTrack(1)
}

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
  audio.volume = volume.value
  if (isPlaying.value) {
    audio.pause()
    return
  }
  try {
    await audio.play()
  } catch {
    isPlaying.value = false
    playbackNotice.value = '音频暂时无法加载，请检查文件或网络'
  }
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

function seekTrack(event: Event) {
  const audio = audioRef.value
  const time = Number((event.target as HTMLInputElement).value)
  if (!audio || !Number.isFinite(time)) return
  audio.currentTime = time
  currentTime.value = time
}

function changeVolume(event: Event) {
  const nextVolume = Number((event.target as HTMLInputElement).value)
  if (!Number.isFinite(nextVolume)) return
  volume.value = nextVolume
  if (nextVolume > 0) previousVolume.value = nextVolume
  if (audioRef.value) audioRef.value.volume = nextVolume
}

function toggleMute() {
  volume.value = volume.value > 0 ? 0 : previousVolume.value
  if (audioRef.value) audioRef.value.volume = volume.value
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
  if (tracks.value.length > 1) playNext()
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

onMounted(() => {
  loadMusicList()
  if (audioRef.value) audioRef.value.volume = volume.value
})

onBeforeUnmount(() => {
  audioRef.value?.pause()
})
</script>

<style scoped>
.music-page {
  --music-canvas: #f6f6f3;
  --music-surface: #fdfdfb;
  --music-ink: #292930;
  --music-muted: #8c8a93;
  --music-line: #e8e6e4;
  --music-accent: #89729e;
  --music-accent-soft: #efebf3;
  min-height: 100vh;
  background: var(--music-canvas);
  color: var(--music-ink);
}

.music-container {
  width: min(1160px, calc(100% - 64px));
  margin: 0 auto;
  padding: 94px 0 48px;
}

.listening-room {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  padding: 29px 42px 0;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 26px;
  background:
    radial-gradient(circle at 78% 36%, rgba(155, 121, 165, 0.28), transparent 37%),
    linear-gradient(115deg, #2e2b3b 0%, #363043 52%, #413447 100%);
  box-shadow: 0 24px 65px rgba(52, 40, 65, 0.15);
  color: #f8f3f5;
}

.listening-room::before {
  position: absolute;
  z-index: -1;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.13) 0.6px, transparent 0.6px);
  background-size: 6px 6px;
  content: '';
  opacity: 0.2;
  pointer-events: none;
}

.room-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #c5bccd;
  font-family: Arial, sans-serif;
  font-size: 9px;
  letter-spacing: 0.16em;
}

.room-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}

.room-brand .v-icon {
  color: #e8bda8;
}

.room-edition {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 8px;
  opacity: 0.75;
}

.room-edition i {
  width: 18px;
  height: 1px;
  background: currentColor;
}

.room-main {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  align-items: center;
  gap: 32px;
  min-height: 325px;
  padding: 24px 14px 27px;
}

.room-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #c8bfd0;
  font-size: 11px;
  letter-spacing: 0.09em;
}

.room-eyebrow i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #e9bda8;
  box-shadow: 0 0 0 4px rgba(233, 189, 168, 0.08);
}

.room-copy h1 {
  margin: 14px 0 13px;
  color: #f9f5f7;
  font-size: clamp(32px, 3.8vw, 47px);
  font-weight: 650;
  letter-spacing: -0.045em;
  line-height: 1.38;
}

.room-copy h1 em {
  color: #edc3ae;
  font-style: normal;
}

.room-copy p {
  margin: 0;
  color: #b9b1c4;
  font-size: 12px;
  letter-spacing: 0.03em;
  line-height: 1.95;
}

.listen-cta {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  margin-top: 22px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #eee6ed;
  cursor: pointer;
  font: inherit;
  font-size: 11px;
  letter-spacing: 0.04em;
}

.listen-cta > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 31px;
  height: 31px;
  border: 1px solid rgba(237, 195, 174, 0.5);
  border-radius: 50%;
  color: #edc3ae;
  transition: background 0.2s ease, transform 0.2s ease;
}

.listen-cta:hover > span {
  background: rgba(237, 195, 174, 0.12);
  transform: scale(1.08);
}

.listen-cta:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.album-scene {
  position: relative;
  justify-self: center;
  width: 264px;
  height: 264px;
}

.album-orbit {
  position: absolute;
  inset: -25px -38px;
  border: 1px solid rgba(230, 204, 218, 0.13);
  border-radius: 50%;
  transform: rotate(-28deg);
}

.album-orbit::after {
  position: absolute;
  top: 24px;
  right: 36px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #eac1b0;
  box-shadow: 0 0 16px rgba(234, 193, 176, 0.5);
  content: '';
}

.album-art {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border: 7px solid rgba(255, 255, 255, 0.1);
  border-radius: 5px;
  background: linear-gradient(145deg, #88748f, #cfab9f);
  box-shadow: 15px 20px 34px rgba(16, 10, 27, 0.3);
  transform: rotate(4deg);
}

.album-art > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(0.75) sepia(0.12);
}

.album-art::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(29, 22, 34, 0.3), transparent 37%, rgba(29, 22, 34, 0.75));
  content: '';
}

.album-art__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(255, 255, 255, 0.7);
}

.album-art__top,
.album-art__caption {
  position: absolute;
  z-index: 1;
  right: 16px;
  left: 16px;
  display: flex;
}

.album-art__top {
  top: 14px;
  align-items: center;
  justify-content: space-between;
  color: #f5e7e9;
  font-family: Arial, sans-serif;
  font-size: 7px;
  letter-spacing: 0.14em;
}

.album-art__caption {
  bottom: 16px;
  flex-direction: column;
  gap: 3px;
}

.album-art__caption span {
  overflow: hidden;
  color: #d7c6d2;
  font-size: 9px;
  letter-spacing: 0.05em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-art__caption strong {
  overflow: hidden;
  color: #fff8f7;
  font-size: 16px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-sticker {
  position: absolute;
  right: -27px;
  bottom: 26px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 10px;
  border-radius: 3px;
  background: #e9c0aa;
  box-shadow: 0 5px 14px rgba(19, 13, 25, 0.16);
  color: #4a3846;
  font-family: Arial, sans-serif;
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.06em;
  transform: rotate(-7deg);
}

.player-deck {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px 24px;
  padding: 22px 0 18px;
  border-top: 1px solid rgba(228, 208, 226, 0.15);
}

.player-track {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.player-label {
  display: block;
  margin-bottom: 3px;
  color: #ada1b9;
  font-size: 9px;
  letter-spacing: 0.06em;
}

.player-track p {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
  margin: 0;
}

.player-track strong {
  overflow: hidden;
  color: #f3eaf2;
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.player-track p > span {
  overflow: hidden;
  flex-shrink: 0;
  max-width: 140px;
  color: #a598b0;
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.player-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  border-radius: 50%;
  background: rgba(226, 196, 216, 0.08);
}

.player-status i {
  width: 2px;
  height: 4px;
  border-radius: 2px;
  background: #c2aacd;
}

.player-status i:nth-child(2) { height: 11px; }
.player-status i:nth-child(3) { height: 7px; }
.player-status--active i { animation: sound-wave 0.9s ease-in-out infinite alternate; }
.player-status--active i:nth-child(2) { animation-delay: -0.5s; }
.player-status--active i:nth-child(3) { animation-delay: -0.25s; }
.player-status--active i:nth-child(4) { animation-delay: -0.7s; }

.player-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.player-controls button,
.volume-control button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #c6b7ce;
  cursor: pointer;
}

.player-controls > button:not(.player-controls__main) {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  transition: color 0.2s ease, background 0.2s ease;
}

.player-controls > button:not(.player-controls__main):hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.player-controls .player-controls__main {
  width: 43px;
  height: 43px;
  border-radius: 50%;
  background: #eac1ae;
  color: #423143;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.player-controls .player-controls__main:hover {
  box-shadow: 0 0 0 6px rgba(234, 193, 174, 0.08);
  transform: scale(1.05);
}

.player-controls button:disabled {
  cursor: not-allowed;
  opacity: 0.35;
}

.player-timeline {
  display: grid;
  grid-column: 1 / -1;
  grid-template-columns: 33px minmax(0, 1fr) 33px;
  align-items: center;
  gap: 11px;
  color: #a99bb4;
  font-family: Arial, sans-serif;
  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

.player-timeline > span:last-child {
  text-align: right;
}

.player-range {
  width: 100%;
  height: 3px;
  margin: 0;
  border-radius: 3px;
  outline: none;
  appearance: none;
  background: linear-gradient(to right, #d3b2c8 0%, #d3b2c8 var(--range-progress), rgba(213, 184, 211, 0.18) var(--range-progress), rgba(213, 184, 211, 0.18) 100%);
  cursor: pointer;
}

.player-range::-webkit-slider-thumb {
  width: 8px;
  height: 8px;
  border: 0;
  border-radius: 50%;
  appearance: none;
  background: #f0d3c4;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.player-range::-moz-range-thumb {
  width: 8px;
  height: 8px;
  border: 0;
  border-radius: 50%;
  background: #f0d3c4;
}

.player-timeline:hover .player-range::-webkit-slider-thumb,
.player-range:focus-visible::-webkit-slider-thumb {
  opacity: 1;
}

.player-range:disabled {
  cursor: default;
  opacity: 0.6;
}

.player-tools {
  display: flex;
  grid-column: 1 / -1;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 18px;
  margin-top: -7px;
}

.playback-notice {
  overflow: hidden;
  color: #92849e;
  font-size: 9px;
  letter-spacing: 0.02em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 90px;
  flex-shrink: 0;
}

.volume-control .player-range {
  height: 2px;
}

.music-library {
  padding-top: 44px;
}

.library-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.library-kicker {
  display: block;
  margin-bottom: 6px;
  color: var(--music-accent);
  font-family: Arial, sans-serif;
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.library-heading h2 {
  margin: 0;
  color: var(--music-ink);
  font-size: 25px;
  font-weight: 650;
  letter-spacing: -0.04em;
}

.upload-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 35px;
  padding: 0 13px;
  border: 1px solid var(--music-line);
  border-radius: 7px;
  background: transparent;
  color: var(--music-muted);
  cursor: pointer;
  font: inherit;
  font-size: 11px;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.upload-button:hover {
  border-color: #c9bbd5;
  background: var(--music-accent-soft);
  color: var(--music-accent);
}

.music-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin: 26px 0 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--music-line);
}

.filter-list {
  display: flex;
  gap: 6px;
}

.filter-button {
  min-width: 48px;
  min-height: 28px;
  padding: 0 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--music-muted);
  cursor: pointer;
  font: inherit;
  font-size: 11px;
  transition: color 0.2s ease, background 0.2s ease;
}

.filter-button:hover {
  color: var(--music-accent);
}

.filter-button--active {
  background: var(--music-accent-soft);
  color: var(--music-accent);
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.track-count {
  color: var(--music-muted);
  font-size: 10px;
  white-space: nowrap;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 190px;
  color: var(--music-muted);
}

.search-field input {
  width: 100%;
  min-width: 0;
  padding: 5px 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--music-ink);
  font: inherit;
  font-size: 11px;
}

.search-field:focus-within {
  color: var(--music-accent);
}

.search-field input::placeholder {
  color: var(--music-muted);
  opacity: 0.8;
}

.track-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 13px 24px;
}

.track-item {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  min-height: 82px;
  padding: 10px 15px;
  border: 1px solid transparent;
  border-radius: 11px;
  background: var(--music-surface);
  box-shadow: 0 3px 12px rgba(54, 42, 61, 0.025);
  color: var(--music-ink);
  cursor: pointer;
  font: inherit;
  text-align: left;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.track-item:hover {
  border-color: #ddd4e4;
  box-shadow: 0 8px 22px rgba(63, 47, 73, 0.06);
  transform: translateY(-2px);
}

.track-item--active {
  border-color: #d8cbe1;
  background: var(--music-accent-soft);
}

.track-item__number {
  flex-shrink: 0;
  color: #aaa3ae;
  font-family: Arial, sans-serif;
  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

.track-item--active .track-item__number {
  color: var(--music-accent);
}

.track-item__cover {
  position: relative;
  width: 58px;
  height: 58px;
  flex: 0 0 58px;
  overflow: hidden;
  border-radius: 7px;
  background: #c7b3c6;
}

.track-item__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(0.75);
  transition: filter 0.2s ease, transform 0.3s ease;
}

.track-item__cover > .v-icon {
  position: absolute;
  inset: 50% auto auto 50%;
  color: white;
  opacity: 0;
  transform: translate(-50%, -50%);
  transition: opacity 0.2s ease;
}

.track-item:hover .track-item__cover img,
.track-item--active .track-item__cover img {
  filter: brightness(0.65) saturate(0.7);
  transform: scale(1.05);
}

.track-item:hover .track-item__cover > .v-icon,
.track-item--active .track-item__cover > .v-icon {
  opacity: 1;
}

.track-item__info {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.track-item__info strong,
.track-item__info small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-item__info strong {
  font-size: 13px;
  font-weight: 600;
}

.track-item__info small {
  color: var(--music-muted);
  font-size: 10px;
}

.track-item__tag {
  flex-shrink: 0;
  padding: 3px 7px;
  border-radius: 4px;
  background: var(--music-accent-soft);
  color: var(--music-accent);
  font-size: 8px;
}

.track-item__duration {
  flex-shrink: 0;
  min-width: 34px;
  color: var(--music-muted);
  font-family: Arial, sans-serif;
  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

.track-item__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border: 1px solid var(--music-line);
  border-radius: 50%;
  color: var(--music-muted);
}

.track-item--active .track-item__action {
  border-color: #c6b3d3;
  background: var(--music-accent);
  color: #fff;
}

.empty-state {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 53px 20px;
  border: 1px dashed var(--music-line);
  border-radius: 13px;
  color: var(--music-muted);
  text-align: center;
}

.empty-state > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 61px;
  height: 61px;
  margin-bottom: 13px;
  border-radius: 50%;
  background: var(--music-accent-soft);
  color: var(--music-accent);
}

.empty-state strong {
  color: var(--music-ink);
  font-size: 14px;
  font-weight: 600;
}

.empty-state p {
  margin: 6px 0 15px;
  font-size: 11px;
}

.empty-state button {
  padding: 6px 12px;
  border: 1px solid #d5c8df;
  border-radius: 5px;
  background: transparent;
  color: var(--music-accent);
  cursor: pointer;
  font: inherit;
  font-size: 10px;
}

.music-footnote {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 37px;
  padding-top: 20px;
  border-top: 1px solid var(--music-line);
  color: var(--music-muted);
  font-size: 9px;
  letter-spacing: 0.04em;
}

.music-footnote > span:last-child {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-family: Arial, sans-serif;
  font-size: 8px;
  letter-spacing: 0.12em;
}

button:focus-visible,
input:focus-visible {
  outline: 2px solid #b99bca;
  outline-offset: 4px;
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
  --music-canvas: #131218;
  --music-surface: #1c1a23;
  --music-ink: #ede7f0;
  --music-muted: #98909f;
  --music-line: #2c2834;
  --music-accent: #c0a5d1;
  --music-accent-soft: #292331;
}

.dark .listening-room {
  box-shadow: 0 24px 65px rgba(0, 0, 0, 0.2);
}

.dark .track-item:hover {
  border-color: #4c3e59;
}

.dark .track-item--active,
.dark .track-item__cover {
  border-color: #574563;
}

@keyframes sound-wave {
  from { height: 3px; }
  to { height: 13px; }
}

@media (max-width: 900px) {
  .music-container {
    width: min(100% - 48px, 1160px);
  }

  .listening-room {
    padding-right: 30px;
    padding-left: 30px;
  }

  .room-main {
    grid-template-columns: 1.1fr 0.9fr;
    gap: 15px;
    padding-right: 0;
    padding-left: 0;
  }

  .album-scene {
    width: 224px;
    height: 224px;
  }

  .album-sticker {
    right: -18px;
  }

  .track-list {
    gap: 12px;
  }

  .track-item {
    gap: 10px;
    padding: 10px;
  }

  .track-item__number,
  .track-item__tag {
    display: none;
  }
}

@media (max-width: 680px) {
  .music-container {
    width: calc(100% - 32px);
    padding: 79px 0 35px;
  }

  .listening-room {
    padding: 22px 22px 0;
    border-radius: 20px;
  }

  .room-edition {
    display: none;
  }

  .room-main {
    grid-template-columns: 1fr;
    gap: 39px;
    padding: 31px 0 40px;
  }

  .room-copy h1 {
    font-size: clamp(29px, 8.4vw, 40px);
  }

  .room-copy p {
    font-size: 11px;
  }

  .album-scene {
    width: min(238px, calc(100vw - 118px));
    height: min(238px, calc(100vw - 118px));
  }

  .player-deck {
    gap: 17px 10px;
    padding-top: 20px;
  }

  .player-track {
    gap: 9px;
  }

  .player-track p {
    display: block;
  }

  .player-track p > span {
    display: block;
    margin-top: 2px;
    font-size: 9px;
  }

  .player-controls {
    gap: 3px;
  }

  .player-controls > button:not(.player-controls__main) {
    width: 26px;
  }

  .player-controls .player-controls__main {
    width: 38px;
    height: 38px;
  }

  .player-tools {
    margin-top: -4px;
  }

  .playback-notice {
    max-width: 58%;
  }

  .music-library {
    padding-top: 35px;
  }

  .library-heading h2 {
    font-size: 23px;
  }

  .music-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 11px;
    margin-top: 21px;
  }

  .toolbar-right {
    justify-content: space-between;
    width: 100%;
    gap: 15px;
  }

  .search-field {
    width: min(220px, 65%);
  }

  .search-field input {
    text-align: right;
  }

  .track-list {
    grid-template-columns: 1fr;
  }

  .track-item__tag {
    display: inline;
  }

  .music-footnote {
    justify-content: center;
    margin-top: 29px;
  }

  .music-footnote > span:last-child {
    display: none;
  }
}

@media (max-width: 380px) {
  .listening-room {
    padding-right: 17px;
    padding-left: 17px;
  }

  .player-status,
  .track-item__tag {
    display: none;
  }

  .track-item {
    gap: 9px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .music-page *,
  .music-page *::before,
  .music-page *::after {
    scroll-behavior: auto !important;
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
