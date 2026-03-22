import { useConsentStore } from '@/store/modules/consent'
import { getMemberToken } from '@/utils/memberAuth'
import { reportBrowse, reportSearch } from '@/api/shop/marketing'
import Cookies from 'js-cookie'

const BROWSING_KEY = 'browsing_history'
const SEARCH_KEY = 'search_history'
const MAX_BROWSING = 50
const MAX_SEARCH = 20
const GUEST_COOKIE_KEY = 'guest_tracking_id'
const GUEST_COOKIE_EXPIRES = 90

/** 模組級：已註冊的第三方追蹤器 */
const registeredTrackers = []

/** 模組級：已載入的腳本 URL（避免重複載入） */
const loadedScripts = new Set()

/**
 * 安全讀取 localStorage（隱私模式或容量滿時不拋錯）
 * @param {string} key
 * @returns {any|null}
 */
function safeGetStorage(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

/**
 * 安全寫入 localStorage
 * @param {string} key
 * @param {any} value
 */
function safeSetStorage(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    // 隱私模式或容量已滿，靜默忽略
  }
}

/**
 * 動態載入外部腳本（僅載入一次）
 * @param {string} url
 * @returns {Promise<void>}
 */
function loadScript(url) {
  if (loadedScripts.has(url)) return Promise.resolve()

  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = url
    script.async = true
    script.onload = () => {
      loadedScripts.add(url)
      resolve()
    }
    script.onerror = () => reject(new Error(`[MarketingTracker] 腳本載入失敗: ${url}`))
    document.head.appendChild(script)
  })
}

/**
 * 行銷追蹤 composable
 *
 * 提供統一的事件追蹤介面、瀏覽足跡記錄、搜尋關鍵字記錄、
 * 最近瀏覽查詢，以及第三方追蹤碼注入點。
 * 所有功能在行銷未同意時靜默不執行。
 */
/**
 * 取得或建立訪客追蹤 ID（Cookie，90 天有效期）
 * 已登入時不產生也不使用；未同意行銷時不產生也不讀取
 */
function getOrCreateGuestId(consentStore) {
  if (!consentStore.isAllowed('marketing')) return null
  if (getMemberToken()) return null

  let guestId = Cookies.get(GUEST_COOKIE_KEY)
  if (!guestId) {
    guestId = crypto.randomUUID()
    Cookies.set(GUEST_COOKIE_KEY, guestId, { expires: GUEST_COOKIE_EXPIRES, path: '/' })
  }
  return guestId
}

export function useMarketingTracker() {
  const consentStore = useConsentStore()

  /**
   * 4.1 統一事件追蹤介面
   * 轉發事件至所有已註冊的第三方追蹤器
   * @param {string} name - 事件名稱
   * @param {object} [payload={}] - 事件資料
   */
  function trackEvent(name, payload = {}) {
    if (!consentStore.isAllowed('marketing')) return

    registeredTrackers.forEach(tracker => {
      try {
        tracker.onEvent?.(name, payload)
      } catch (e) {
        console.warn(`[MarketingTracker] 追蹤器 "${tracker.name}" 事件處理失敗`, e)
      }
    })
  }

  /**
   * 4.2 瀏覽足跡記錄
   * 記錄商品瀏覽，上限 50 筆，超過移除最舊
   * @param {string|number} productId - 商品 ID
   * @param {string} productName - 商品名稱
   */
  function trackProductView(productId, productName) {
    if (!consentStore.isAllowed('marketing')) return

    const history = safeGetStorage(BROWSING_KEY) || []
    const now = Date.now()

    // 移除同一商品的舊記錄（避免重複）
    const filtered = history.filter(item => item.productId !== productId)

    // 在最前面插入新記錄
    filtered.unshift({
      productId,
      name: productName,
      timestamp: now
    })

    // 超過上限時截斷
    if (filtered.length > MAX_BROWSING) {
      filtered.length = MAX_BROWSING
    }

    safeSetStorage(BROWSING_KEY, filtered)

    // 非同步上報後端（fire-and-forget）：已登入用 memberId，未登入用 guestId
    const guestId = getOrCreateGuestId(consentStore)
    if (getMemberToken() || guestId) {
      reportBrowse({ productId, productName, categoryId: null, source: 'DIRECT', guestId }).catch(() => {})
    }

    // 同時觸發統一事件
    trackEvent('product_view', { productId, productName })
  }

  /**
   * 4.3 搜尋關鍵字記錄
   * 重複關鍵字更新時間戳而非新增，上限 20 筆
   * @param {string} keyword - 搜尋關鍵字
   */
  function trackSearch(keyword) {
    if (!consentStore.isAllowed('marketing')) return
    if (!keyword || !keyword.trim()) return

    const trimmed = keyword.trim()
    const history = safeGetStorage(SEARCH_KEY) || []
    const now = Date.now()

    // 找到相同關鍵字的索引
    const existingIndex = history.findIndex(item => item.keyword === trimmed)

    if (existingIndex !== -1) {
      // 重複關鍵字：移除舊的，放到最前面並更新時間戳
      history.splice(existingIndex, 1)
    }

    history.unshift({
      keyword: trimmed,
      timestamp: now
    })

    // 超過上限時截斷
    if (history.length > MAX_SEARCH) {
      history.length = MAX_SEARCH
    }

    safeSetStorage(SEARCH_KEY, history)

    // 非同步上報後端（fire-and-forget）：已登入用 memberId，未登入用 guestId
    const guestId = getOrCreateGuestId(consentStore)
    if (getMemberToken() || guestId) {
      reportSearch({ keyword: trimmed, resultCount: null, guestId }).catch(() => {})
    }

    // 同時觸發統一事件
    trackEvent('search', { keyword: trimmed })
  }

  /**
   * 4.4 最近瀏覽查詢
   * 回傳最近瀏覽商品列表（依時間倒序）
   * @param {number} [limit=10] - 回傳筆數上限
   * @returns {Array<{productId: string|number, name: string, timestamp: number}>}
   */
  function getRecentlyViewed(limit = 10) {
    if (!consentStore.isAllowed('marketing')) return []

    const history = safeGetStorage(BROWSING_KEY) || []

    // 已依時間倒序儲存，直接截取即可
    return history.slice(0, limit)
  }

  /**
   * 4.5 第三方追蹤碼注入點
   * @param {object} config - 追蹤器設定
   * @param {string} config.name - 追蹤器名稱
   * @param {string} [config.scriptUrl] - 外部腳本 URL
   * @param {function} config.onEvent - 事件處理函式 (name, payload) => void
   */
  function registerTracker(config) {
    if (!config || !config.name || !config.onEvent) {
      console.warn('[MarketingTracker] registerTracker: 缺少必要參數 name 或 onEvent')
      return
    }

    // 避免重複註冊同名追蹤器
    const exists = registeredTrackers.some(t => t.name === config.name)
    if (exists) return

    // 行銷未同意時，僅登記但不載入腳本
    registeredTrackers.push({
      name: config.name,
      scriptUrl: config.scriptUrl || null,
      onEvent: config.onEvent
    })

    // 行銷已同意且有腳本 URL 時，動態載入
    if (consentStore.isAllowed('marketing') && config.scriptUrl) {
      loadScript(config.scriptUrl).catch(err => {
        console.warn(err.message)
      })
    }
  }

  return {
    trackEvent,
    trackProductView,
    trackSearch,
    getRecentlyViewed,
    registerTracker
  }
}
