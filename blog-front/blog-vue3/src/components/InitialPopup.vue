<template>
  <transition name="popup-fade">
    <div
      v-if="visible && article"
      class="popup-mask"
      @click.self="closePopup"
    >
      <transition name="popup-rise">
        <div v-if="visible" class="popup-card">
          <button class="popup-close" type="button" @click="closePopup" aria-label="关闭推荐弹窗">
            <v-icon size="18">mdi-close</v-icon>
          </button>

          <div class="popup-kicker">今日推荐</div>
          <h3 class="popup-title">{{ article.articleTitle }}</h3>

          <div
            v-if="article.articleCover"
            class="popup-cover-wrap"
          >
            <img
              :src="article.articleCover"
              :alt="article.articleTitle"
              class="popup-cover"
            />
          </div>

          <div class="popup-meta">
            <span>{{ article.createTime ? formatDate(article.createTime) : '' }}</span>
            <span v-if="article.categoryName">{{ article.categoryName }}</span>
          </div>

          <p v-if="article.articleDesc" class="popup-desc">
            {{ article.articleDesc }}
          </p>

          <div class="popup-actions">
            <v-btn color="primary" variant="flat" class="popup-action-btn" @click="closeAndSkipToday">
              今日不再提醒
            </v-btn>
            <v-btn variant="tonal" class="popup-action-btn" @click="closePopup">
              关闭
            </v-btn>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInitialArticle } from '@/api/article'
import { formatDate } from '@/utils/filters'

interface InitialArticle {
  id: number
  articleTitle: string
  articleCover?: string
  articleDesc?: string
  categoryName?: string
  createTime?: string
}

const POPUP_RECORD_KEY = 'initial-popup-record'

const visible = ref(false)
const article = ref<InitialArticle | null>(null)

function getTodayDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function shouldSkipToday() {
  const record = localStorage.getItem(POPUP_RECORD_KEY)
  if (!record) return false

  try {
    const parsed = JSON.parse(record) as { date?: string; noRemind?: boolean }
    return parsed.date === getTodayDate() && parsed.noRemind === true
  } catch {
    return false
  }
}

async function loadInitialArticle() {
  if (shouldSkipToday()) return

  try {
    const { data } = await getInitialArticle()
    if (data?.data?.id) {
      article.value = data.data
      visible.value = true
    }
  } catch (error) {
    console.error('获取推荐文章失败:', error)
  }
}

function closePopup() {
  visible.value = false
}

/**
 * 记录当天不再提醒并关闭弹窗。
 */
function closeAndSkipToday() {
  localStorage.setItem(
    POPUP_RECORD_KEY,
    JSON.stringify({
      date: getTodayDate(),
      noRemind: true
    })
  )
  closePopup()
}

onMounted(() => {
  loadInitialArticle()
})
</script>

<style scoped>
.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: opacity 0.24s ease;
}

.popup-fade-enter-from,
.popup-fade-leave-to {
  opacity: 0;
}

.popup-rise-enter-active {
  animation: popup-rise-in 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.popup-rise-leave-active {
  animation: popup-rise-out 0.2s ease-out;
}

@keyframes popup-rise-in {
  from {
    transform: translateY(14px) scale(0.98);
    opacity: 0;
  }

  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

@keyframes popup-rise-out {
  from {
    transform: translateY(0);
    opacity: 1;
  }

  to {
    transform: translateY(-12px);
    opacity: 0;
  }
}

.popup-mask {
  position: fixed;
  inset: 0;
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.52);
  backdrop-filter: blur(10px);
}

.popup-card {
  position: relative;
  width: min(92vw, 540px);
  padding: 22px 22px 20px;
  border: 1px solid rgba(73, 177, 245, 0.16);
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(73, 177, 245, 0.16), transparent 32%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.24);
}

.popup-close {
  position: absolute;
  top: 14px;
  right: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #506173;
  cursor: pointer;
}

.popup-kicker {
  color: #49b1f5;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.popup-title {
  margin: 10px 0 16px;
  padding-right: 40px;
  color: #152033;
  font-size: 26px;
  line-height: 1.3;
}

.popup-cover-wrap {
  overflow: hidden;
  border-radius: 18px;
}

.popup-cover {
  display: block;
  width: 100%;
  max-height: 260px;
  object-fit: cover;
  border-radius: 18px;
  box-shadow: 0 16px 38px rgba(15, 23, 42, 0.16);
}

.popup-meta {
  display: flex;
  gap: 14px;
  margin-top: 14px;
  color: #607286;
  font-size: 13px;
}

.popup-desc {
  margin: 14px 0 0;
  color: #415168;
  font-size: 14px;
  line-height: 1.75;
}

.popup-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.popup-action-btn {
  min-width: 132px;
}

@media (max-width: 759px) {
  .popup-card {
    padding: 18px 18px 18px;
    border-radius: 20px;
  }

  .popup-title {
    font-size: 22px;
  }

  .popup-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .popup-action-btn {
    width: 100%;
  }
}
</style>
