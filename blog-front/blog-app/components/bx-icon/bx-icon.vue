<template>
  <view class="bx-icon" :style="wrapStyle">
    <!-- 用 view + mask-image 渲染 SVG,可统一通过 color 控制颜色 -->
    <view class="bx-icon-svg" :style="iconStyle" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { ICON_PATHS } from './paths'

const props = defineProps({
  name: { type: String, required: true },         // 图标名,见 paths.js
  size: { type: [Number, String], default: 40 },  // rpx
  color: { type: String, default: '#1d1f23' }
})

const sizeRpx = computed(() => typeof props.size === 'number' ? `${props.size}rpx` : props.size)

const wrapStyle = computed(() => ({
  width: sizeRpx.value,
  height: sizeRpx.value
}))

const iconStyle = computed(() => {
  const svg = ICON_PATHS[props.name] || ICON_PATHS.help
  // 用 data URI 喂给 mask-image,实现单色矢量图标(可染色)
  const dataUri = `url("data:image/svg+xml;utf8,${encodeURIComponent(svg)}")`
  return {
    backgroundColor: props.color,
    maskImage: dataUri,
    webkitMaskImage: dataUri,
    maskSize: 'contain',
    webkitMaskSize: 'contain',
    maskRepeat: 'no-repeat',
    webkitMaskRepeat: 'no-repeat',
    maskPosition: 'center',
    webkitMaskPosition: 'center'
  }
})
</script>

<style scoped>
.bx-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

.bx-icon-svg {
  width: 100%;
  height: 100%;
}
</style>
