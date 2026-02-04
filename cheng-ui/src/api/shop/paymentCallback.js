import request from '@/utils/request'

// 查詢金流回調紀錄列表
export function listPaymentCallbackLog(query) {
  return request({
    url: '/shop/payment/callback/log/list',
    method: 'get',
    params: query
  })
}

// 查詢金流回調紀錄詳情
export function getPaymentCallbackLog(logId) {
  return request({
    url: '/shop/payment/callback/log/' + logId,
    method: 'get'
  })
}
