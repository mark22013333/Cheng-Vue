import { createApp } from 'vue'

import Cookies from 'js-cookie'
import useClipboard from 'vue-clipboard3'

import ElementPlus from 'element-plus'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import 'element-plus/dist/index.css'
import '@/assets/styles/element-variables.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import '@/assets/styles/index.scss'
import '@/assets/styles/cheng.scss'
import App from './App'
import store from './store'
import router from './router'
import directive from './directive'
import plugins from './plugins'
import { download } from '@/utils/request'

import 'virtual:svg-icons-register'
import './permission'

import { getDicts } from "@/api/system/dict/data"
import { getConfigKey } from "@/api/system/config"
import { addDateRange, handleTree, parseTime, resetForm, selectDictLabel, selectDictLabels } from "@/utils/cheng"
import Pagination from "@/components/Pagination"
import RightToolbar from "@/components/RightToolbar"
import Editor from "@/components/Editor"
import FileUpload from "@/components/FileUpload"
import ImageUpload from "@/components/ImageUpload"
import ImagePreview from "@/components/ImagePreview"
import DictTag from '@/components/DictTag'
import DictData from '@/components/DictData'
import SvgIcon from '@/components/SvgIcon'

const app = createApp(App)

// 註冊所有 Element Plus Icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

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
app.component('SvgIcon', SvgIcon)
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('RightToolbar', RightToolbar)
app.component('Editor', Editor)
app.component('FileUpload', FileUpload)
app.component('ImageUpload', ImageUpload)
app.component('ImagePreview', ImagePreview)

app.use(store)
app.use(router)
app.config.globalProperties.$store = store

app.use(directive)
app.use(plugins)

app.use(ElementPlus, {
  size: Cookies.get('size') || 'medium',  // 使用 medium 大小（與 store 預設值一致）
  locale: zhTw
})

app.use(DictData)

app.mount('#app')

// 移除載入動畫遮罩
setTimeout(() => {
  const body = document.body
  const loaderWrapper = document.getElementById('loader-wrapper')
  
  // 添加 loaded class 觸發淡出動畫
  body.classList.add('loaded')
  
  // 等待動畫完成後完全移除元素
  setTimeout(() => {
    if (loaderWrapper) {
      loaderWrapper.remove()
    }
  }, 600) // 配合 CSS transition 時間
}, 500)
