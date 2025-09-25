import tab from './tab'
import auth from './auth'
import cache from './cache'
import modal from './modal'
import download from './download'

export default {
  install(Vue) {
    // 頁籤操作
    Vue.prototype.$tab = tab
    // 認證物件
    Vue.prototype.$auth = auth
    // 暫存物件
    Vue.prototype.$cache = cache
    // 模態框物件
    Vue.prototype.$modal = modal
    // 下載檔案
    Vue.prototype.$download = download
  }
}
