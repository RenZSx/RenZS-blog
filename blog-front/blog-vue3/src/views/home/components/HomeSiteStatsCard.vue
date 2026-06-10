<template>
  <v-card class="animated zoomIn home-side-card home-side-card-stats">
    <div class="home-side-title">
      <span class="home-side-title-badge home-side-title-badge-emoji">
        🧡
      </span>
      <span class="home-side-title-text">网站资讯</span>
    </div>
    <div class="home-side-body home-side-text home-side-info">
      <div class="home-side-info-row">
        运行时间:<span class="float-right">{{ time }}</span>
      </div>
      <div class="home-side-info-row">
        总访问量🎈:<span class="float-right">
          {{ blogInfo.viewsCount }}
        </span>
      </div>
    </div>
  </v-card>
</template>

<script setup lang="ts">
interface BlogInfo {
  viewsCount: number
  [key: string]: any
}

interface Props {
  blogInfo: BlogInfo
  time: string
}

defineProps<Props>()
</script>

<style scoped>
.home-side-card {
  position: relative;
  margin-top: 0 !important;
  border: 1px solid var(--glass-border);
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--card-radius-md) !important;
  overflow: hidden;
  transition: transform 0.32s ease, border-color 0.32s ease, box-shadow 0.32s ease;
}

.home-side-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background:
    radial-gradient(circle at 14% 0%, rgba(255, 255, 255, 0.74), transparent 34%),
    radial-gradient(circle at 90% 12%, rgba(73, 177, 245, 0.16), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.28), transparent 44%);
  pointer-events: none;
}

.home-side-card:hover {
  transform: translateY(-3px);
  border-color: var(--glass-border-hover);
  box-shadow: var(--glass-shadow-hover);
}

.home-side-title {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 13px 14px 10px;
  margin-bottom: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.38);
  font-size: 14px;
  font-weight: 700;
  line-height: 2;
  background:
    linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.28),
      rgba(255, 255, 255, 0.08)
    );
}

.home-side-title-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 10px;
  background:
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.78),
      rgba(244, 248, 255, 0.34)
    );
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88),
    0 10px 20px rgba(94, 166, 229, 0.12);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  flex-shrink: 0;
}

.home-side-title-badge-emoji {
  font-size: 1rem;
}

.home-side-title-text {
  margin-left: 0;
  letter-spacing: 0.01em;
}

.home-side-text {
  font-size: 0.78rem;
  font-weight: 600;
}

.home-side-body {
  position: relative;
  z-index: 1;
  padding: 12px 14px 14px;
}

.home-side-info {
  color: #5f6472;
}

.home-side-info-row {
  padding: 5px 0 0;
}
</style>
