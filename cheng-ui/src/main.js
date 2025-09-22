import Vue from 'vue'

import Cookies from 'js-cookie'

import Element from 'element-ui'
import locale from 'element-ui/lib/locale/lang/zh-TW'
import './assets/styles/element-variables.scss'

import '@/assets/styles/index.scss' // global css
import '@/assets/styles/cheng.scss' // cheng css
import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import {download} from '@/utils/request'

import './assets/icons' // icon
import './permission' // permission control
import {getDicts} from "@/api/system/dict/data"
import {getConfigKey} from "@/api/system/config"
import {addDateRange, handleTree, parseTime, resetForm, selectDictLabel, selectDictLabels} from "@/utils/cheng"
// 分頁元件
import Pagination from "@/components/Pagination"
// 自定義表格工具元件
import RightToolbar from "@/components/RightToolbar"
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
// 字典數據元件
import DictData from '@/components/DictData'

// 全域方法掛載
Vue.prototype.getDicts = getDicts
Vue.prototype.getConfigKey = getConfigKey
Vue.prototype.parseTime = parseTime
Vue.prototype.resetForm = resetForm
Vue.prototype.addDateRange = addDateRange
Vue.prototype.selectDictLabel = selectDictLabel
Vue.prototype.selectDictLabels = selectDictLabels
Vue.prototype.download = download
Vue.prototype.handleTree = handleTree

// 全域元件掛載
Vue.component('DictTag', DictTag)
Vue.component('Pagination', Pagination)
Vue.component('RightToolbar', RightToolbar)
Vue.component('Editor', Editor)
Vue.component('FileUpload', FileUpload)
Vue.component('ImageUpload', ImageUpload)
Vue.component('ImagePreview', ImagePreview)

Vue.use(directive)
Vue.use(plugins)
DictData.install()

/**
 * If you don't want to use mock-server,
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently, MockJs will be used in the production environment,
 * please remove it before going online! ! !
 */

Vue.use(Element, {
  size: Cookies.get('size') || 'medium', // set element-ui default size
  locale // 設定繁體中文語言包
})

Vue.config.productionTip = false

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
