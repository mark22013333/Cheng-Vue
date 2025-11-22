import { createApp } from 'vue'

import Cookies from 'js-cookie'
import useClipboard from 'vue-clipboard3'

import ElementPlus from 'element-plus'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import 'element-plus/dist/index.css'

import '@/assets/styles/index.scss' // global css
import '@/assets/styles/cheng.scss' // cheng css
import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

import 'virtual:svg-icons-register' // svg icon
import './permission' // permission control
import { getDicts } from "@/api/system/dict/data"
import { getConfigKey } from "@/api/system/config"
import { addDateRange, handleTree, parseTime, resetForm, selectDictLabel, selectDictLabels } from "@/utils/cheng"
// 分頁元件
import Pagination from "@/components/Pagination"
// 自定義表格工具元件
import RightToolbar from "@/components/RightToolbar"
// 富文字元件
import Editor from "@/components/Editor"
// 檔案上傳元件
import FileUpload from "@/components/FileUpload"
// 圖片上傳元件
import ImageUpload from "@/components/ImageUpload"
// 圖片預覽元件
import ImagePreview from "@/components/ImagePreview"
// 字典標籤元件
import DictTag from '@/components/DictTag'
// 字典數據元件
import DictData from '@/components/DictData'

const app = createApp(App)

// 全域方法掛載
app.config.globalProperties.getDicts = getDicts
app.config.globalProperties.getConfigKey = getConfigKey
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.selectDictLabels = selectDictLabels
app.config.globalProperties.download = download
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.useClipboard = useClipboard

// 全域元件掛載
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('RightToolbar', RightToolbar)
app.component('Editor', Editor)
app.component('FileUpload', FileUpload)
app.component('ImageUpload', ImageUpload)
app.component('ImagePreview', ImagePreview)

app.use(directive)
app.use(plugins)
app.use(DictData)

// Element Plus 設定
app.use(ElementPlus, {
  size: Cookies.get('size') || 'default',
  locale: zhTw
})

app.use(store)
app.use(router)

// 隱藏 loading
router.isReady().then(() => {
  app.mount('#app')
  // 隱藏載入動畫
  document.body.classList.add('loaded')
})
