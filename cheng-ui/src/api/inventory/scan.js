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

// 掃描條碼或QR碼（通用）
export function scanCode(data) {
  return request({
    url: '/inventory/item/scan',
    method: 'post',
    data: data
  })
}

// 重新抓取 ISBN 資料並更新（只更新書籍資訊，不影響庫存）
export function refreshIsbn(itemId) {
  return request({
    url: '/inventory/scan/refreshIsbn',
    method: 'post',
    data: { itemId }
  })
}
