import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import path from 'path'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import viteCompression from 'vite-plugin-compression'

const pathSrc = path.resolve(__dirname, 'src')

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const baseUrl = 'http://localhost:8080'

  return {
    plugins: [
      vue(),
      // 暫時禁用 vueJsx 插件，因為專案中使用 Vue 2 JSX 語法
      // vueJsx({
      //   include: [/\.vue$/]
      // }),
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
      port: 80,
      open: true,
      proxy: {
        // API 代理
        [env.VITE_APP_BASE_API]: {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp('^' + env.VITE_APP_BASE_API), '')
        },
        // Springdoc API 文件代理
        '^/v3/api-docs': {
          target: baseUrl,
          changeOrigin: true
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
