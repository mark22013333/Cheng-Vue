import defaultSettings from '@/settings'
import useSettingsStore from '@/store/modules/settings'

/**
 * 動態修改標題
 */
export function useDynamicTitle() {
  const settingsStore = useSettingsStore()
  if (settingsStore.dynamicTitle) {
    document.title = settingsStore.title + ' - ' + defaultSettings.title
  } else {
    document.title = defaultSettings.title
  }
}
