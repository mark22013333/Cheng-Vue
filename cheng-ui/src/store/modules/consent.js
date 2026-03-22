import { defineStore } from 'pinia'
import Cookies from 'js-cookie'

const STORAGE_KEY = 'cookie_consent'
const LEGACY_KEY = 'cookie_consent_accepted'

/** 功能性層級需清除的 Cookie 名稱 */
const FUNCTIONAL_COOKIES = ['username', 'password', 'rememberMe']

/** 功能性層級需清除的 localStorage key */
const FUNCTIONAL_STORAGE_KEYS = ['mall_theme']

/** 行銷層級需清除的 localStorage key */
const MARKETING_STORAGE_KEYS = ['browsing_history', 'search_history']

export const useConsentStore = defineStore('consent', {
  state: () => ({
    essential: true,
    functional: false,
    marketing: false,
    /** 是否已做過任何選擇（用於判斷是否顯示提示） */
    hasDecided: false
  }),

  getters: {
    /** 檢查指定層級是否已同意 */
    isAllowed: (state) => (level) => {
      if (level === 'essential') return true
      return state[level] === true
    }
  },

  actions: {
    /** 全部接受：三層皆開啟 */
    acceptAll() {
      this.essential = true
      this.functional = true
      this.marketing = true
      this.hasDecided = true
      this._persist()
    },

    /** 僅必要：關閉功能性與行銷，並清除已存在的非必要資料 */
    acceptEssentialOnly() {
      this.essential = true
      this.functional = false
      this.marketing = false
      this.hasDecided = true
      this._clearNonEssentialData()
      this._persist()
    },

    /** 持久化同意狀態至 localStorage */
    _persist() {
      const data = {
        essential: this.essential,
        functional: this.functional,
        marketing: this.marketing,
        timestamp: Date.now()
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    },

    /** 清除所有非必要的 Cookie 與 localStorage 資料 */
    _clearNonEssentialData() {
      // 清除功能性 Cookie
      FUNCTIONAL_COOKIES.forEach(name => Cookies.remove(name))

      // 清除功能性 localStorage
      FUNCTIONAL_STORAGE_KEYS.forEach(key => localStorage.removeItem(key))

      // 清除行銷 localStorage
      MARKETING_STORAGE_KEYS.forEach(key => localStorage.removeItem(key))

      // 清除訪客追蹤 Cookie
      Cookies.remove('guest_tracking_id', { path: '/' })
    },

    /** 初始化：讀取持久化狀態或遷移舊版 */
    init() {
      // 優先檢查舊版 key，執行遷移
      const legacyValue = localStorage.getItem(LEGACY_KEY)
      if (legacyValue) {
        this.essential = true
        this.functional = true
        this.marketing = true
        this.hasDecided = true
        localStorage.removeItem(LEGACY_KEY)
        this._persist()
        return
      }

      // 讀取新版同意狀態
      try {
        const saved = localStorage.getItem(STORAGE_KEY)
        if (saved) {
          const data = JSON.parse(saved)
          this.essential = true
          this.functional = data.functional === true
          this.marketing = data.marketing === true
          this.hasDecided = true
        }
      } catch (e) {
        console.error('[ConsentStore] 讀取同意狀態失敗', e)
      }
    }
  }
})
