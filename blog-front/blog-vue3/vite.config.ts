import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VuetifyResolver } from 'unplugin-vue-components/resolvers'
import vuetify from 'vite-plugin-vuetify'

export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vuetify({ autoImport: true }),
    AutoImport({
      imports: [
        'vue',
        'vue-router',
        'pinia',
        {
          '@/stores/user': ['useUserStore'],
          '@/stores/ui': ['useUIStore'],
          '@/stores/blogInfo': ['useBlogInfoStore'],
          '@/composables/useToast': ['useToast'],
          '@/utils/filters': ['formatDate', 'formatTime', 'formatNum']
        }
      ],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    host: true,
    open: true,
    proxy: {
      '/api': {
       target: 'http://8.137.86.224:8088',
       //  target: 'http://localhost:8088',

        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/websocket': {
        target: 'ws://8.137.86.224:8088',
        // target: 'ws://localhost:8088',
        ws: true,
        changeOrigin: true
      },
      '/notice-websocket': {
        target: 'ws://8.137.86.224:8088',
        // target: 'ws://localhost:8088',
        ws: true,
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler'
      },
      sass: {
        api: 'modern-compiler'
      }
    }
  },
  build: {
    target: 'esnext',
    minify: 'esbuild',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return

          if (id.includes('node_modules/vuetify/')) {
            return 'vuetify'
          }

          if (id.includes('node_modules/markdown-it/')) {
            return 'markdown-core'
          }

          if (id.includes('node_modules/highlight.js/')) {
            return 'markdown-highlight'
          }

          if (id.includes('node_modules/katex/')) {
            return 'markdown-katex'
          }

          if (id.includes('node_modules/tocbot/')) {
            return 'tocbot'
          }

          if (
            id.includes('node_modules/markdown-it-') ||
            id.includes('node_modules/linkify-it/') ||
            id.includes('node_modules/mdurl/') ||
            id.includes('node_modules/punycode.js/') ||
            id.includes('node_modules/uc.micro/')
          ) {
            return 'markdown-plugins'
          }

          if (id.includes('node_modules/swiper/')) {
            return 'swiper'
          }
        }
      }
    }
  }
})
