import axios from 'axios'
import {Loading, Message, MessageBox, Notification} from 'element-ui'
import store from '@/store'
import {getToken} from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import {blobValidate, tansParams} from "@/utils/cheng"
import cache from '@/plugins/cache'
import {saveAs} from 'file-saver'

let downloadLoadingInstance
// 是否顯示重新登入
export let isRelogin = { show: false }

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 建立axios實體
const service = axios.create({
  // axios中請求設定有baseURL選項，表示請求URL公開部分
  baseURL: process.env.VUE_APP_BASE_API,
  // 逾時
  timeout: 10000
})

// request攔截器
service.interceptors.request.use(config => {
  // 是否需要設定 token
  const isToken = (config.headers || {}).isToken === false
  // 是否需要防止數據重複提交
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  if (getToken() && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken() // 讓每個請求攜帶自定義token 請根據實際情況自行修改
  }
  // get請求映射params參數
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }
  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length // 請求數據大小
    const limitSize = 5 * 1024 * 1024 // 限制存放數據5M
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: ` + '請求數據大小超出允許的5M限制，無法進行防重複提交驗證。')
      return config
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url                  // 請求地址
      const s_data = sessionObj.data                // 請求數據
      const s_time = sessionObj.time                // 請求時間
      const interval = 1000                         // 間隔時間(ms)，小於此時間視為重複提交
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = '數據正在處理，請勿重複提交'
        console.warn(`[${s_url}]: ` + message)
        return Promise.reject(new Error(message))
      } else {
        cache.session.setJSON('sessionObj', requestObj)
      }
    }
  }
  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 響應攔截器
service.interceptors.response.use(res => {
    // 未設定狀態碼則預設成功狀態
    const code = res.data.code || 200
    // 取得錯誤訊息
    const msg = errorCode[code] || res.data.msg || errorCode['default']
    // 二進制數據則直接返回
    if (res.request.responseType ===  'blob' || res.request.responseType ===  'arraybuffer') {
      return res.data
    }
    if (code === 401) {
      if (!isRelogin.show) {
        isRelogin.show = true
        MessageBox.confirm('登入狀態已過期，您可以繼續留在該頁面，或者重新登入', '系統提示', {
          confirmButtonText: '重新登入',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          isRelogin.show = false
          store.dispatch('LogOut').then(() => {
            location.href = '/index'
          })
      }).catch(() => {
        isRelogin.show = false
      })
    }
      return Promise.reject('無效的會話，或者會話已過期，請重新登入。')
    } else if (code === 500) {
      // 檢查是否包含 HTML 標籤
      const isHtml = /<[^>]+>/.test(msg)
      if (isHtml) {
        Notification.error({
          title: '操作失敗',
          dangerouslyUseHTMLString: true,
          message: `<div style="max-height: 400px; overflow-y: auto;">${msg}</div>`,
          duration: 8000
        })
      } else {
        Message({ message: msg, type: 'error' })
      }
      return Promise.reject(new Error(msg))
    } else if (code === 601) {
      Message({ message: msg, type: 'warning' })
      return Promise.reject('error')
    } else if (code !== 200) {
      // 檢查是否包含 HTML 標籤
      const isHtml = /<[^>]+>/.test(msg)
      if (isHtml) {
        Notification.error({
          title: '錯誤',
          dangerouslyUseHTMLString: true,
          message: `<div style="max-height: 400px; overflow-y: auto;">${msg}</div>`,
          duration: 8000
        })
      } else {
        Notification.error({ title: msg })
      }
      return Promise.reject('error')
    } else {
      return res.data
    }
  },
  error => {
    console.log('err' + error)
    let { message } = error
    if (message == "Network Error") {
      message = "後端介面連線異常"
    } else if (message.includes("timeout")) {
      message = "系統介面請求逾時"
    } else if (message.includes("Request failed with status code")) {
      message = "系統介面" + message.substr(message.length - 3) + "異常"
    }
    Message({ message: message, type: 'error', duration: 5 * 1000 })
    return Promise.reject(error)
  }
)

// 共用下載方法
export function download(url, params, filename, config) {
  downloadLoadingInstance = Loading.service({
    text: "正在下載數據，請稍候",
    spinner: "el-icon-loading",
    background: "rgba(0, 0, 0, 0.7)",
  })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      Message.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((r) => {
    console.error(r)
    Message.error('下載檔案出現錯誤，請聯絡管理員！')
    downloadLoadingInstance.close()
  })
}

export default service
