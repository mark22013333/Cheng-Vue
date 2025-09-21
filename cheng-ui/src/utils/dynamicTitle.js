import store from '@/store'
import defaultSettings from '@/settings'

/**
 * 動態修改標題
 */
export function useDynamicTitle() {
  if (store.state.settings.dynamicTitle) {
    document.title = store.state.settings.title + ' - ' + defaultSettings.title
  } else {
    document.title = defaultSettings.title
  }
}
