'use strict'
const path = require('path')

function resolve(dir) {
  return path.join(__dirname, dir)
}

const CompressionPlugin = require('compression-webpack-plugin')

const name = process.env.VUE_APP_TITLE || 'CoolApps管理系統' // 網頁標題

const baseUrl = 'http://localhost:8080' // 後端介面

const port = process.env.port || process.env.npm_config_port || 80 // 埠號

// vue.config.js 配置說明
//官方vue.config.js 參考文件 https://cli.vuejs.org/zh/config/#css-loaderoptions
// 這里只列一部分，具體配置參考文件
module.exports = {
  // 部署正式環境和開發環境下的URL。
  // 預設情況下，Vue CLI 會假設你的應用是被部署在一個域名的根路徑上
  // 例如 https://www.cheng.vip/。如果應用被部署在一個子路徑上，你就需要用這個選項指定這個子路徑。例如，如果你的應用被部署在 https://www.cheng.vip/admin/，則設定 baseUrl 為 /admin/。
  publicPath: process.env.NODE_ENV === "production" ? "/" : "/",
  // 在npm run build 或 yarn build 時 ，產生文件的目錄名稱（要和baseUrl的正式環境路徑一致）（預設dist）
  outputDir: 'dist',
  // 用於放置產生的靜態資源 (js、css、img、fonts) 的；（專案打包之後，靜態資源會放在這個文件夹下）
  assetsDir: 'static',
  // 如果你不需要正式環境的 source map，可以將其設定為 false 以加速正式環境構建。
  productionSourceMap: false,
  transpileDependencies: ['quill'],
  // webpack-dev-server 相關配置
  devServer: {
    host: '0.0.0.0',
    port: port,
    open: true,
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: baseUrl,
        changeOrigin: true,
        pathRewrite: {
          ['^' + process.env.VUE_APP_BASE_API]: ''
        }
      },
      // springdoc proxy
      '^/v3/api-docs/(.*)': {
        target: baseUrl,
        changeOrigin: true
      }
    },
    disableHostCheck: true
  },
  css: {
    loaderOptions: {
      sass: {
        sassOptions: { outputStyle: "expanded" }
      }
    }
  },
  configureWebpack: {
    name: name,
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
    plugins: [
      // http://doc.cheng.vip/cheng-vue/other/faq.html#使用gzip解壓縮靜態文件
      new CompressionPlugin({
        cache: false,                                  // 不啟用文件暫存
        test: /\.(js|css|html|jpe?g|png|gif|svg)?$/i,  // 壓縮文件格式
        filename: '[path][base].gz[query]',            // 壓縮後的文件名
        algorithm: 'gzip',                             // 使用gzip壓縮
        minRatio: 0.8,                                 // 壓縮比例，小於 80% 的文件不會被壓縮
        deleteOriginalAssets: false                    // 壓縮後刪除原文件
      })
    ],
  },
  chainWebpack(config) {
    config.plugins.delete('preload') // TODO: need test
    config.plugins.delete('prefetch') // TODO: need test

    // set svg-sprite-loader
    config.module
      .rule('svg')
      .exclude.add(resolve('src/assets/icons'))
      .end()
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('src/assets/icons'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
      .end()

    config.when(process.env.NODE_ENV !== 'development', config => {
          config
            .plugin('ScriptExtHtmlWebpackPlugin')
            .after('html')
            .use('script-ext-html-webpack-plugin', [{
            // `runtime` must same as runtimeChunk name. default is `runtime`
              inline: /runtime\..*\.js$/
            }])
            .end()

          config.optimization.splitChunks({
            chunks: 'all',
            cacheGroups: {
              libs: {
                name: 'chunk-libs',
                test: /[\\/]node_modules[\\/]/,
                priority: 10,
                chunks: 'initial' // only package third parties that are initially dependent
              },
              elementUI: {
                name: 'chunk-elementUI', // split elementUI into a single package
                test: /[\\/]node_modules[\\/]_?element-ui(.*)/, // in order to adapt to cnpm
                priority: 20 // the weight needs to be larger than libs and app, or it will be packaged into libs or app
              },
              commons: {
                name: 'chunk-commons',
                test: resolve('src/components'), // can customize your rules
                minChunks: 3, //  minimum common number
                priority: 5,
                reuseExistingChunk: true
              }
            }
          })
          config.optimization.runtimeChunk('single')
    })
  }
}
