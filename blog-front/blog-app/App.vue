<script>
import { useUserStore } from '@/store/user'
import { useNoticeStore } from '@/store/notice'
import { useThemeStore } from '@/store/theme'
import { useNoticeSocket } from '@/composables/useNoticeSocket'
import { watch } from 'vue'

export default {
  async onLaunch() {
    const userStore = useUserStore()
    const noticeStore = useNoticeStore()
    const { start: startWs, isConnected } = useNoticeSocket()

    // 1. 校验 token,有效则同步用户信息
    const ok = await userStore.syncSession()

    // 2. 已登录:拉未读数 + 启动 WS
    if (ok && userStore.isLoggedIn) {
      noticeStore.fetchUnreadCount()
      startWs()
    }

    // 3. 主题同步到 DOM (H5 端有效)
    const themeStore = useThemeStore()
    const applyTheme = () => {
      if (typeof document !== 'undefined' && document.body) {
        document.body.classList.toggle('theme-dark', themeStore.isDark)
      }
    }
    applyTheme()
    watch(() => themeStore.isDark, applyTheme)

    // 4. 网络从无到有时,自动重连 WS
    try {
      uni.onNetworkStatusChange((res) => {
        if (res.isConnected && userStore.isLoggedIn && !isConnected()) {
          startWs()
        }
      })
    } catch (e) { /* 平台不支持时静默 */ }

  },
  onShow() {
    // App 端从后台回到前台:检测 WS 是否仍连接,断了则重连
    try {
      const userStore = useUserStore()
      const { start: startWs, isConnected } = useNoticeSocket()
      if (userStore.isLoggedIn && !isConnected()) {
        startWs()
      }
    } catch (e) { /* noop */ }
  },
  onHide() {
    // 不主动断开 WS,保留连接;系统休眠会自动处理
  }
}
</script>

<style lang="scss">
/* ============================================================
 * H5 端隐藏浏览器原生滚动条
 * - WebKit (Chrome / Safari / Edge): ::-webkit-scrollbar width:0
 * - Firefox: scrollbar-width:none
 * - IE: -ms-overflow-style:none
 * 注意:只隐藏样式,不影响滚动功能,touch / wheel 仍然能滚
 * ============================================================ */
::-webkit-scrollbar {
  width: 0 !important;
  height: 0 !important;
  display: none;
  -webkit-appearance: none;
  background: transparent;
}
::-webkit-scrollbar-thumb,
::-webkit-scrollbar-track {
  background: transparent;
}

/* uniapp 自带的 scroll-view 也走原生滚动条样式 */
scroll-view ::-webkit-scrollbar,
uni-scroll-view ::-webkit-scrollbar {
  width: 0 !important;
  height: 0 !important;
  display: none;
}

/* Firefox / IE 兼容 */
html,
uni-app,
uni-page-wrapper,
uni-page-body,
uni-scroll-view,
scroll-view {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* ========= 主题变量(浅色默认) ========= */
page {
  --bg-page: #f3f4f6;
  --bg-card: #ffffff;
  --bg-soft: #f9fafb;
  --bg-overlay: rgba(17, 24, 39, 0.42);

  --text-primary: #1f2937;
  --text-regular: #4b5563;
  --text-secondary: #6b7280;
  --text-placeholder: #9ca3af;

  --border-color: #e5e7eb;
  --border-color-light: #f3f4f6;

  --color-primary: #4F46E5;
  --color-primary-soft: #EEF2FF;
  --color-primary-muted: #818CF8;

  --shadow-sm: 0 2rpx 8rpx rgba(24, 24, 24, 0.04), 0 1rpx 2rpx rgba(24, 24, 24, 0.05);
  --shadow-md: 0 12rpx 32rpx rgba(15, 23, 42, 0.04), 0 2rpx 4rpx rgba(15, 23, 42, 0.02);
  --shadow-lg: 0 14rpx 34rpx rgba(24, 24, 24, 0.08), 0 5rpx 10rpx rgba(24, 24, 24, 0.04);

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
  --bg-page: #0f172a;
  --bg-card: #1e293b;
  --bg-soft: #334155;
  --bg-overlay: rgba(0, 0, 0, 0.65);

  --text-primary: #f1f5f9;
  --text-regular: #cbd5e1;
  --text-secondary: #94a3b8;
  --text-placeholder: #64748b;

  --border-color: #334155;
  --border-color-light: #1e293b;

  --color-primary: #818CF8;
  --color-primary-soft: #334155;
  --color-primary-muted: #818CF8;

  --shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.4);
  --shadow-md: 0 12rpx 32rpx rgba(15, 23, 42, 0.04), 0 2rpx 4rpx rgba(15, 23, 42, 0.02);
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
