import request from '@/utils/request'

// 提交掃描結果
export function submitScan(data) {
  return request({
    url: '/inventory/scan/submit',
    method: 'post',
    data: data
  })
}

// ISBN 掃描（書籍專用）
export function scanIsbn(data) {
  return request({
    url: '/inventory/scan/isbn',
    method: 'post',
    data: data
  })
}

// 掃描條碼或QR碼（共用）
export function scanCode(data) {
  return request({
    url: '/inventory/item/scan',
    method: 'post',
    data: data
  })
}

// 建立重新抓取 ISBN 任務
export function createRefreshTask(itemId) {
  return request({
    url: '/inventory/scan/refreshIsbn/create',
    method: 'post',
    data: { itemId }
  })
}

// 重新抓取 ISBN 資料並更新（同步版本，已棄用，建議使用 SSE）
// @deprecated
export function refreshIsbn(itemId) {
  return request({
    url: '/inventory/scan/refreshIsbn',
    method: 'post',
    data: { itemId }
  })
}
