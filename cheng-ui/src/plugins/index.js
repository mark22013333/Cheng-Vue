import tab from './tab'
import auth from './auth'
import cache from './cache'
import modal from './modal'
import download from './download'

export default function installPlugins(app){
  // 頁籤操作
  app.config.globalProperties.$tab = tab
  // 認證對象
  app.config.globalProperties.$auth = auth
  // 暫存對象
  app.config.globalProperties.$cache = cache
  // 模態框對象
  app.config.globalProperties.$modal = modal
  // 下載文件
  app.config.globalProperties.$download = download
}
