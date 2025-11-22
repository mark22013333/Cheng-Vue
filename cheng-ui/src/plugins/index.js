import tab from './tab'
import auth from './auth'
import cache from './cache'
import modal from './modal'
import download from './download'

export default {
  install(app) {
    // 頁籤操作
    app.config.globalProperties.$tab = tab
    // 認證物件
    app.config.globalProperties.$auth = auth
    // 暫存物件
    app.config.globalProperties.$cache = cache
    // 模態框物件
    app.config.globalProperties.$modal = modal
    // 下載檔案
    app.config.globalProperties.$download = download
  }
}
