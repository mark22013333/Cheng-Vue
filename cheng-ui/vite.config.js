import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import path from 'path'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import viteCompression from 'vite-plugin-compression'
import autoImport from 'unplugin-auto-import/vite'

const pathSrc = path.resolve(__dirname, 'src')

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const baseUrl = 'http://localhost:8080'

  // 後端 context path（如果有的話）
  const backendContext = env.VITE_BACKEND_CONTEXT || ''

  return {
    // 單一應用程式，base 永遠是 /
    base: '/',

    plugins: [
      vue(),
      // Vue 3 JSX 支援（排除 tool/build 下的 Vue 2 JSX 文件）
      vueJsx({
        exclude: [/\/tool\/build\//]
      }),
      // 自動匯入 Vue 3 API、Vue Router 和 Pinia API
      autoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia'
        ],
        dts: false
      }),
      // SVG Icon 支援
      createSvgIconsPlugin({
        iconDirs: [path.resolve(process.cwd(), 'src/assets/icons/svg')],
        symbolId: 'icon-[name]',
      }),
      // Gzip 壓縮
      viteCompression({
        verbose: true,
        disable: false,
        threshold: 10240,
        algorithm: 'gzip',
        ext: '.gz',
      }),
    ],

    resolve: {
      alias: {
        '~': pathSrc,
        '@': pathSrc,
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },

    // CSS 設定
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler'
        }
      }
    },

    // 開發伺服器設定
    server: {
      host: '0.0.0.0',
      port: 1024,
      open: true,
      // 允許所有 hosts 連線 (解決 Ngrok 等外部通道被擋的問題)
      allowedHosts: true,
      proxy: {
        // 統一 API 代理：/dev-api/** → 後端
        [env.VITE_APP_BASE_API]: {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp('^' + env.VITE_APP_BASE_API), backendContext || '')
        },
        // 靜態檔案代理（頭像、上傳檔案）
        '/profile': {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (path) => backendContext ? `${backendContext}${path}` : path
        },
        // Springdoc API 文件代理
        '^/v3/api-docs': {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (path) => backendContext ? `${backendContext}${path}` : path
        }
      }
    },

    // 建構設定
    build: {
      outDir: 'dist',
      assetsDir: 'static',
      sourcemap: false,
      // 分包策略
      rollupOptions: {
        output: {
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            'element-plus': ['element-plus', '@element-plus/icons-vue'],
            'chart': ['echarts'],
          }
        }
      },
      // 調整 chunk 大小警告限制
      chunkSizeWarningLimit: 2000,
    },

    // 優化設定
    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'element-plus',
        '@element-plus/icons-vue',
        'axios',
        'echarts',
      ]
    }
  }
})
