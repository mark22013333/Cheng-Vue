import request from '@/utils/request'

/**
 * 建立 ECPay 付款
 * @param {string} orderNo - 訂單編號
 */
export function createEcpayPayment(orderNo) {
  return request({
    url: '/shop/payment/ecpay/create',
    method: 'post',
    params: { orderNo }
  })
}
