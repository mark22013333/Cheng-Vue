import request from '@/utils/requestShop'

/**
 * 結帳預覽
 * @param {number} addressId - 收貨地址 ID（可選）
 */
export function getCheckoutPreview(addressId) {
  return request({
    url: '/shop/checkout/preview',
    method: 'get',
    params: addressId ? { addressId } : {}
  })
}

/**
 * 提交訂單
 * @param {Object} data - { addressId, remark, paymentMethod }
 */
export function submitOrder(data) {
  return request({
    url: '/shop/checkout/submit',
    method: 'post',
    data: data
  })
}
