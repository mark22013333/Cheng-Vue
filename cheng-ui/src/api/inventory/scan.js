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
