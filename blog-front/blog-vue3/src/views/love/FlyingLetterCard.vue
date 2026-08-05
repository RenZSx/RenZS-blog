<template>
  <button
    class="flying-letter-card"
    :class="{ 'flying-letter-card--departing': departing }"
    type="button"
    :disabled="departing"
    :aria-label="label"
    @click="$emit('activate')"
  >
    <svg
      class="flying-letter-card__scene"
      viewBox="0 0 800 282"
      preserveAspectRatio="xMidYMid slice"
      aria-hidden="true"
    >
      <rect width="800" height="282" rx="24" fill="#4fa9bf" />
      <circle cx="90" cy="62" r="36" fill="#bfe8ef" opacity="0.42" />
      <circle cx="714" cy="228" r="28" fill="#2c7d98" opacity="0.28" />

      <g class="flying-letter-card__cloud flying-letter-card__cloud--back" fill="#b9e3ec">
        <path d="M410 203c-14 0-25-10-25-23 0-12 9-22 21-23 5-18 20-30 40-30 23 0 41 16 44 37 18 1 32 15 32 32 0 19-16 34-35 34h-77c-13 0-23-10-23-22 0-3 1-5 2-7 6 2 13 2 21 2Z" />
        <path d="M50 222c-12 0-22-9-22-20 0-10 8-19 18-20 4-15 17-26 34-26 20 0 35 14 38 31 15 1 27 13 27 27 0 16-13 29-30 29H50Z" />
      </g>

      <path
        class="flying-letter-card__trail"
        d="M350 150C430 95 503 94 586 126"
        fill="none"
        stroke="#e2f7fa"
        stroke-linecap="round"
        stroke-width="6"
      />

      <g class="flying-letter-card__envelope">
        <path d="M522 141l148-51-39 112-42-46-67-15Z" fill="#fff0c6" />
        <path d="m522 141 67 15 81-66M589 156l42 46" fill="none" stroke="#c99f58" stroke-linecap="round" stroke-linejoin="round" stroke-width="5" />
        <path d="m613 125 31-11-8 26-23-15Z" fill="#4fa9bf" />
      </g>

      <g class="flying-letter-card__vehicle">
        <path d="M574 80h77c14 0 25 11 25 25v20H549v-20c0-14 11-25 25-25Z" fill="#d94442" />
        <path d="M577 60h67c12 0 22 10 22 22h-99c0-12-2-22 10-22Z" fill="#f05a55" />
        <rect x="599" y="69" width="28" height="18" rx="3" fill="#c8edf3" />
        <circle class="flying-letter-card__wheel" cx="581" cy="127" r="13" fill="#153f55" />
        <circle class="flying-letter-card__wheel" cx="649" cy="127" r="13" fill="#153f55" />
        <path d="m573 127 7-8 7 8-7 8-7-8Zm68 0 7-8 7 8-7 8-7-8Z" fill="#bce6ed" />
      </g>
    </svg>

    <span class="flying-letter-card__title">{{ label }}</span>
    <span class="flying-letter-card__arrow" aria-hidden="true">→</span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  departing: boolean
}>()

defineEmits<{
  activate: []
}>()
</script>

<style scoped>
.flying-letter-card {
  position: relative;
  width: min(420px, 88vw);
  aspect-ratio: 800 / 282;
  display: block;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.52);
  border-radius: 8px;
  padding: 0;
  background: #4fa9bf;
  box-shadow: 0 16px 34px rgba(13, 63, 80, 0.22);
  color: #fff;
  cursor: pointer;
  transition: box-shadow 240ms ease, transform 240ms ease;
}

.flying-letter-card:focus-visible {
  outline: 3px solid #f3b74f;
  outline-offset: 4px;
}

.flying-letter-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 22px 42px rgba(13, 63, 80, 0.28);
}

.flying-letter-card__scene {
  width: 100%;
  height: 100%;
  display: block;
}

.flying-letter-card__title {
  position: absolute;
  top: 50%;
  left: 9%;
  max-width: 42%;
  color: #fff;
  font-size: clamp(20px, 5vw, 27px);
  font-weight: 900;
  line-height: 1.2;
  text-align: left;
  text-shadow: 0 3px 12px rgba(7, 47, 64, 0.45);
  transform: translateY(-50%);
  transition: opacity 180ms ease, transform 220ms ease;
}

.flying-letter-card__arrow {
  position: absolute;
  bottom: 15%;
  left: 9%;
  color: #ffe3a4;
  font-size: 24px;
  font-weight: 800;
  transition: transform 220ms ease;
}

.flying-letter-card:hover .flying-letter-card__arrow {
  transform: translateX(8px);
}

.flying-letter-card__cloud--back {
  animation: flying-letter-cloud 9s ease-in-out infinite alternate;
}

.flying-letter-card__trail {
  stroke-dasharray: 12 14;
  animation: flying-letter-trail 1.2s linear infinite;
}

.flying-letter-card__vehicle {
  transform-box: fill-box;
  transform-origin: center;
  animation: flying-letter-vehicle 3.6s ease-in-out infinite;
  transition: transform 240ms ease;
}

.flying-letter-card__envelope {
  transform-box: fill-box;
  transform-origin: center;
  animation: flying-letter-envelope 3s ease-in-out infinite;
}

.flying-letter-card__wheel {
  transform-box: fill-box;
  transform-origin: center;
  animation: flying-letter-wheel 1.4s linear infinite;
}

.flying-letter-card:hover .flying-letter-card__vehicle,
.flying-letter-card:hover .flying-letter-card__envelope {
  animation-play-state: paused;
  transform: translateX(13px) rotate(-1deg);
}

.flying-letter-card--departing {
  pointer-events: none;
  animation: flying-letter-card-depart 620ms cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.flying-letter-card--departing .flying-letter-card__vehicle,
.flying-letter-card--departing .flying-letter-card__envelope {
  animation: flying-letter-depart 620ms cubic-bezier(0.3, 0, 0.2, 1) forwards;
}

.flying-letter-card--departing .flying-letter-card__title,
.flying-letter-card--departing .flying-letter-card__arrow {
  opacity: 0;
  transform: translate(18px, -50%);
}

@keyframes flying-letter-cloud {
  to { transform: translateX(-16px); }
}

@keyframes flying-letter-trail {
  to { stroke-dashoffset: -52; }
}

@keyframes flying-letter-vehicle {
  0%, 100% { transform: translateY(0) rotate(0); }
  50% { transform: translateY(-5px) rotate(-0.8deg); }
}

@keyframes flying-letter-envelope {
  0%, 100% { transform: translateY(0) rotate(0); }
  50% { transform: translateY(-6px) rotate(1.2deg); }
}

@keyframes flying-letter-wheel {
  to { transform: rotate(360deg); }
}

@keyframes flying-letter-card-depart {
  55% { transform: translateY(-4px) scale(1.015); }
  100% { transform: translateX(24px) scale(0.98); opacity: 0; }
}

@keyframes flying-letter-depart {
  to { transform: translateX(250px) translateY(-16px) rotate(-3deg); opacity: 0; }
}

@media (prefers-reduced-motion: reduce) {
  .flying-letter-card,
  .flying-letter-card *,
  .flying-letter-card--departing {
    animation: none !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
