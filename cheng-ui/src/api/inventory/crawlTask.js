import request from '@/utils/request'

/**
 * 建立爬取任務
 */
export function createCrawlTask(isbn) {
  return request({
    url: '/inventory/crawlTask/create',
    method: 'post',
    params: { isbn }
  })
}

/**
 * 查詢任務狀態
 */
export function getTaskStatus(taskId) {
  return request({
    url: `/inventory/crawlTask/status/${taskId}`,
    method: 'get'
  })
}

/**
 * 查詢所有進行中的任務
 */
export function getActiveTasks() {
  return request({
    url: '/inventory/crawlTask/active',
    method: 'get'
  })
}

/**
 * 批次查詢任務狀態
 */
export function getBatchTaskStatus(taskIds) {
  return request({
    url: '/inventory/crawlTask/batchStatus',
    method: 'get',
    params: { taskIds }
  })
}
