import request from '@/utils/request'

/**
 * 取得所有任務類型
 */
export function listJobTypes() {
  return request({
    url: '/monitor/job/types',
    method: 'get'
  })
}

/**
 * 根據分類取得任務類型
 * @param {string} category - 分類名稱
 */
export function listJobTypesByCategory(category) {
  return request({
    url: `/monitor/job/types/category/${category}`,
    method: 'get'
  })
}

/**
 * 取得所有分類
 */
export function listJobCategories() {
  return request({
    url: '/monitor/job/types/categories',
    method: 'get'
  })
}

/**
 * 根據 code 取得任務類型詳情
 * @param {string} code - 任務代碼
 */
export function getJobTypeByCode(code) {
  return request({
    url: `/monitor/job/types/${code}`,
    method: 'get'
  })
}
