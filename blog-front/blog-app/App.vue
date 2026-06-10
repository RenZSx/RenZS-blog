<script>
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'
import { watch } from 'vue'

export default {
  onLaunch() {
    const userStore = useUserStore()
    userStore.syncSession()

    const themeStore = useThemeStore()
    // 把主题状态同步到 DOM(H5 端有效;App 端无 document 但页面层级会 fallback 到 page 默认色)
    const applyTheme = () => {
      if (typeof document !== 'undefined' && document.body) {
        document.body.classList.toggle('theme-dark', themeStore.isDark)
      }
    }
    applyTheme()
    watch(() => themeStore.isDark, applyTheme)
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
/* ========= 主题变量(浅色默认) ========= */
page {
  --bg-page: #f7f8fa;
  --bg-card: #ffffff;
  --bg-soft: #f5f7fa;
  --bg-overlay: rgba(0, 0, 0, 0.45);

  --text-primary: #1d1f23;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  --border-color: #f0f2f5;
  --border-color-light: #f5f7fa;

  --shadow-sm: 0 2rpx 8rpx rgba(15, 23, 42, 0.04);
  --shadow-md: 0 6rpx 16rpx rgba(15, 23, 42, 0.06);
  --shadow-lg: 0 12rpx 32rpx rgba(15, 23, 42, 0.08);

  background-color: var(--bg-page);
  font-size: 28rpx;
  color: var(--text-primary);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -webkit-tap-highlight-color: transparent;
}

/* 暗色主题:H5 端给 body 加 .theme-dark,所有 page 继承 */
body.theme-dark page,
page.theme-dark {
  --bg-page: #0f1011;
  --bg-card: #1d1f23;
  --bg-soft: #2a2c30;
  --bg-overlay: rgba(0, 0, 0, 0.65);

  --text-primary: #f5f7fa;
  --text-regular: #c0c4cc;
  --text-secondary: #909399;
  --text-placeholder: #606266;

  --border-color: #2a2c30;
  --border-color-light: #1d1f23;

  --shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.4);
  --shadow-md: 0 6rpx 16rpx rgba(0, 0, 0, 0.5);
  --shadow-lg: 0 12rpx 32rpx rgba(0, 0, 0, 0.6);
}

view, text, input, textarea, button {
  box-sizing: border-box;
}

button { outline: none; border: none; }
button::after { border: none; }

/* ========= 工具类 ========= */
.flex { display: flex; }
.flex-center { display: flex; justify-content: center; align-items: center; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-start { display: flex; align-items: center; }
.flex-col { display: flex; flex-direction: column; }

.text-ellipsis {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.text-ellipsis-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ========= 动效 ========= */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8rpx); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24rpx); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes pop {
  0% { transform: scale(1); }
  40% { transform: scale(1.18); }
  100% { transform: scale(1); }
}

.skeleton {
  background: linear-gradient(90deg, #f0f2f5 25%, #fafafa 50%, #f0f2f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
  border-radius: 12rpx;
}

body.theme-dark .skeleton,
page.theme-dark .skeleton {
  background: linear-gradient(90deg, #1d1f23 25%, #2a2c30 50%, #1d1f23 75%);
  background-size: 200% 100%;
}

.fade-in { animation: fadeIn 280ms ease; }
.fade-in-up { animation: fadeInUp 360ms ease; }
.pop { animation: pop 320ms cubic-bezier(0.34, 1.56, 0.64, 1); }
</style>
