import {getTableConfig, saveTableConfig, getTemplateConfig as getTemplateConfigApi, saveTemplateConfig as saveTemplateConfigApi} from '@/api/system/tableConfig'

/**
 * 表格欄位配置 Composable
 * 提供載入、儲存、合併配置的功能
 */
export function useTableConfig() {
  /**
   * 載入使用者儲存的欄位配置
   * @param {string} pageKey 頁面標識
   * @param {object} defaultColumns 預設欄位配置
   * @returns {Promise<object>} 合併後的欄位配置
   */
  async function loadConfig(pageKey, defaultColumns) {
    try {
      const response = await getTableConfig(pageKey)

      // 如果沒有儲存的配置，使用預設配置
      if (!response.data) {
        return {...defaultColumns}
      }

      // 解析儲存的配置
      const savedConfig = JSON.parse(response.data)

      // 合併配置：優先使用儲存的配置，但包含新增的欄位
      return mergeConfig(defaultColumns, savedConfig)
    } catch (error) {
      console.error('載入表格欄位配置失敗：', error)
      // 發生錯誤時使用預設配置
      return {...defaultColumns}
    }
  }

  /**
   * 儲存欄位配置
   * @param {string} pageKey 頁面標識
   * @param {object} columns 欄位配置
   * @returns {Promise}
   */
  async function saveConfig(pageKey, columns) {
    try {
      await saveTableConfig(pageKey, columns)
    } catch (error) {
      console.error('儲存表格欄位配置失敗：', error)
      throw error
    }
  }

  /**
   * 合併配置
   * 規則：
   * 1. 儲存的配置中的欄位，使用儲存的 visible 狀態
   * 2. 新增的欄位（不在儲存配置中），預設顯示（visible: true）
   * 3. 刪除的欄位（在儲存配置但不在預設配置中），忽略
   *
   * @param {object} defaultColumns 預設欄位配置
   * @param {object} savedConfig 儲存的欄位配置
   * @returns {object} 合併後的欄位配置
   */
  function mergeConfig(defaultColumns, savedConfig) {
    const merged = {}

    // 遍歷預設配置
    for (const key in defaultColumns) {
      if (savedConfig.hasOwnProperty(key)) {
        // 如果儲存的配置中有此欄位，使用儲存的 visible 狀態
        merged[key] = {
          ...defaultColumns[key],
          visible: savedConfig[key].visible
        }
      } else {
        // 新增的欄位，預設顯示
        merged[key] = {
          ...defaultColumns[key],
          visible: true
        }
      }
    }

    return merged
  }

  /**
   * 載入全域模版配置
   * @param {string} pageKey 頁面標識
   * @param {object} defaultColumns 預設欄位配置
   * @returns {Promise<object>} 合併後的模版配置
   */
  async function loadTemplateConfig(pageKey, defaultColumns) {
    try {
      const response = await getTemplateConfigApi(pageKey)
      if (!response.data) {
        return { ...defaultColumns }
      }
      const savedConfig = JSON.parse(response.data)
      return mergeConfig(defaultColumns, savedConfig)
    } catch (error) {
      console.error('載入模版配置失敗：', error)
      return { ...defaultColumns }
    }
  }

  /**
   * 儲存全域模版配置
   * @param {string} pageKey 頁面標識
   * @param {object} columns 欄位配置
   * @returns {Promise}
   */
  async function saveTemplate(pageKey, columns) {
    try {
      await saveTemplateConfigApi(pageKey, columns)
    } catch (error) {
      console.error('儲存模版配置失敗：', error)
      throw error
    }
  }

  return {
    loadConfig,
    saveConfig,
    mergeConfig,
    loadTemplateConfig,
    saveTemplate
  }
}
