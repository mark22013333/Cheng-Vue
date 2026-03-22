import { computed } from 'vue'
import { useConsentStore } from '@/store/modules/consent'

/**
 * Cookie 同意管理 Composable
 *
 * 提供便捷 API 供各模組查詢同意層級、使用受保護的儲存
 */
export function useConsent() {
  const consentStore = useConsentStore()

  /**
   * 檢查指定層級是否已同意
   * @param {'essential' | 'functional' | 'marketing'} level
   * @returns {ComputedRef<boolean>}
   */
  function canUseFeature(level) {
    return computed(() => consentStore.isAllowed(level))
  }

  /**
   * 受保護的 localStorage 操作
   * 當對應層級未同意時，set 靜默失敗，get 回傳 null
   * @param {'essential' | 'functional' | 'marketing'} level
   */
  function guardedStorage(level) {
    return {
      get(key) {
        if (!consentStore.isAllowed(level)) return null
        return localStorage.getItem(key)
      },
      getJSON(key) {
        if (!consentStore.isAllowed(level)) return null
        try {
          const val = localStorage.getItem(key)
          return val ? JSON.parse(val) : null
        } catch {
          return null
        }
      },
      set(key, value) {
        if (!consentStore.isAllowed(level)) return
        localStorage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value))
      },
      remove(key) {
        localStorage.removeItem(key)
      }
    }
  }

  return {
    canUseFeature,
    guardedStorage,
    /** 直接存取 store（用於監聽變化） */
    consentStore
  }
}
