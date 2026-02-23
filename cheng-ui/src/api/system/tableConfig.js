import request from '@/utils/request'

/**
 * 取得指定頁面的表格欄位配置
 * @param {string} pageKey 頁面標識（如 system_user, inventory_borrow）
 * @returns {Promise} 返回欄位配置（JSON字串）或null
 */
export function getTableConfig(pageKey) {
  return request({
    url: `/system/tableConfig/${pageKey}`,
    method: 'get'
  })
}

/**
 * 儲存表格欄位配置
 * @param {string} pageKey 頁面標識
 * @param {object} columnConfig 欄位配置物件
 * @returns {Promise}
 */
export function saveTableConfig(pageKey, columnConfig) {
  return request({
    url: '/system/tableConfig',
    method: 'post',
    data: {
      pageKey: pageKey,
      columnConfig: JSON.stringify(columnConfig)
    }
  })
}

/**
 * 取得全域模版配置
 * @param {string} pageKey 頁面標識
 * @returns {Promise}
 */
export function getTemplateConfig(pageKey) {
  return request({
    url: `/system/tableConfig/template/${pageKey}`,
    method: 'get'
  })
}

/**
 * 儲存全域模版配置
 * @param {string} pageKey 頁面標識
 * @param {object} columnConfig 欄位配置物件
 * @returns {Promise}
 */
export function saveTemplateConfig(pageKey, columnConfig) {
  return request({
    url: '/system/tableConfig/template',
    method: 'post',
    data: {
      pageKey: pageKey,
      columnConfig: JSON.stringify(columnConfig)
    }
  })
}
