import axios from 'axios'
import { ElNotification, ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { getMemberToken, removeMemberToken } from '@/utils/memberAuth'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '~/utils/cheng'
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'

let downloadLoadingInstance
export let shopRelogin = { show: false }

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

function normalizeApiBase(baseApi) {
  if (!baseApi) return '/prod-api'
  const value = String(baseApi).trim()
  if (!value) return '/prod-api'
  if (/^https?:\/\//i.test(value)) {
    return value.replace(/\/+$/, '')
  }
  const withLeadingSlash = value.startsWith('/') ? value : `/${value}`
  return withLeadingSlash.replace(/\/+$/, '')
}

function getShopBaseURL() {
  // 商城 API 統一使用 /dev-api 或 /prod-api，不需要 /cadm 前綴
  return normalizeApiBase(import.meta.env.VITE_APP_BASE_API)
}

// 建立axios實例（baseURL 會在請求攔截器中動態設定）
const service = axios.create({
  timeout: 10000
})

service.interceptors.request.use(config => {
  // 動態設定 baseURL
  config.baseURL = getShopBaseURL()

  const isToken = (config.headers || {}).isToken === false
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  if (getMemberToken() && !isToken) {
    config.headers['Member-Token'] = 'Bearer ' + getMemberToken()
  }
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
    const requestSize = Object.keys(JSON.stringify(requestObj)).length
    const limitSize = 5 * 1024 * 1024
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: ` + '請求資料大小超出允許的5M限制，無法進行防重複提交驗證。')
      return config
    }
    const sessionObj = cache.session.getJSON('shopSessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('shopSessionObj', requestObj)
    } else {
      const s_url = sessionObj.url
      const s_data = sessionObj.data
      const s_time = sessionObj.time
      const interval = 1000
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = '資料正在處理，請勿重複提交'
        console.warn(`[${s_url}]: ` + message)
        return Promise.reject(new Error(message))
      } else {
        cache.session.setJSON('shopSessionObj', requestObj)
      }
    }
  }
  return config
}, error => {
  console.log(error)
  return Promise.reject(error)
})

service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = errorCode[code] || res.data.msg || errorCode['default']
  if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
    return res.data
  }
  if (code === 401) {
    if (!shopRelogin.show) {
      shopRelogin.show = true
      ElMessageBox.confirm('登入狀態已過期，請重新登入', '系統提示', {
        confirmButtonText: '重新登入',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        shopRelogin.show = false
        removeMemberToken()
        location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
      }).catch(() => {
        shopRelogin.show = false
      })
    }
    return Promise.reject('無效的Session，或者Session已過期，請重新登入。')
  } else if (code === 500) {
    if (typeof msg === 'string' && msg.includes('<div')) {
      ElMessageBox.alert(msg, '系統提示', {
        dangerouslyUseHTMLString: true,
        type: 'error',
        confirmButtonText: '確定'
      })
    } else {
      ElMessage({ message: msg, type: 'error' })
    }
    return Promise.reject(new Error(msg))
  } else if (code === 601) {
    ElMessage({ message: msg, type: 'warning' })
    return Promise.reject(new Error(msg))
  } else if (code !== 200) {
    ElNotification.error({ title: msg })
    return Promise.reject('error')
  } else {
    return Promise.resolve(res.data)
  }
},
error => {
  console.log('err' + error)
  let { message } = error
  if (message == 'Network Error') {
    message = '後端API連接異常'
  } else if (message.includes('timeout')) {
    message = '系統API請求逾時'
  } else if (message.includes('Request failed with status code')) {
    message = '系統API' + message.substr(message.length - 3) + '異常'
  }
  ElMessage({ message: message, type: 'error', duration: 5 * 1000 })
  return Promise.reject(error)
})

export function download(url, params, filename, config) {
  downloadLoadingInstance = ElLoading.service({ text: '正在下載資料，請稍候', background: 'rgba(0, 0, 0, 0.7)' })
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
      ElMessage.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((r) => {
    console.error(r)
    ElMessage.error('下載文件出現錯誤，請聯絡管理員！')
    downloadLoadingInstance.close()
  })
}

export default service
