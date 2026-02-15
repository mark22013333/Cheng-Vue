import request from '@/utils/requestShop'

/**
 * 取得可用物流方式
 * @param {number} productAmount - 商品金額（用於計算運費）
 */
export function getShippingMethods(productAmount = 0) {
  return request({
    url: '/shop/logistics/methods',
    method: 'get',
    params: { productAmount }
  })
}

/**
 * 計算運費
 * @param {string} shippingMethod - 物流方式代碼
 * @param {number} productAmount - 商品金額
 */
export function calculateShippingFee(shippingMethod, productAmount = 0) {
  return request({
    url: '/shop/logistics/shipping-fee',
    method: 'get',
    params: { shippingMethod, productAmount }
  })
}

/**
 * 取得電子地圖表單 HTML
 * @param {string} shippingMethod - 物流方式代碼（CVS_711, CVS_FAMILY, CVS_HILIFE）
 * @param {string} storeKey - 門市暫存 key（用於關聯回調）
 */
export function getMapFormHtml(shippingMethod, storeKey) {
  return request({
    url: '/shop/logistics/map/url',
    method: 'post',
    data: { shippingMethod, storeKey }
  })
}

/**
 * 查詢已選取的超商門市資訊
 * @param {string} storeKey - 門市暫存 key
 */
export function getCvsStore(storeKey) {
  return request({
    url: `/shop/logistics/cvs/store/${storeKey}`,
    method: 'get'
  })
}
