import { createApp } from 'vue'

import Cookies from 'js-cookie'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import locale from 'element-plus/es/locale/lang/zh-tw'

import '@/assets/styles/index.scss' // global css

import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive

// 註冊指令
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

// svg圖標
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'

import './permission' // permission control

import { useDict } from '@/utils/dict'
import { getConfigKey } from "@/api/system/config"
import { parseTime, resetForm, addDateRange, handleTree, selectDictLabel, selectDictLabels } from '~/utils/cheng'

// 分頁元件
import Pagination from '@/components/Pagination'
// 自定義表格工具元件
import RightToolbar from '@/components/RightToolbar'
// 富文字元件
import Editor from "@/components/Editor"
// 文件上傳元件
import FileUpload from "@/components/FileUpload"
// 圖片上傳元件
import ImageUpload from "@/components/ImageUpload"
// 圖片預覽元件
import ImagePreview from "@/components/ImagePreview"
// 字典標籤元件
import DictTag from '@/components/DictTag'

const app = createApp(App)

// 全域方法掛載
app.config.globalProperties.useDict = useDict
app.config.globalProperties.download = download
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.getConfigKey = getConfigKey
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.selectDictLabels = selectDictLabels

// 全域元件掛載
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('FileUpload', FileUpload)
app.component('ImageUpload', ImageUpload)
app.component('ImagePreview', ImagePreview)
app.component('RightToolbar', RightToolbar)
app.component('Editor', Editor)

app.use(router)
app.use(store)
app.use(plugins)
app.use(elementIcons)
app.component('svg-icon', SvgIcon)

directive(app)

// 使用 element-plus 並且設定全域的大小
app.use(ElementPlus, {
  locale: locale,
  // 可用 large、default、small
  size: Cookies.get('size') || 'default'
})

app.mount('#app')
