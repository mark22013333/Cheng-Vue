import request from '@/utils/request'

// 根據商品ID查詢SKU列表
export function listSku(productId) {
  return request({
    url: '/shop/sku/list/' + productId,
    method: 'get'
  })
}

// 查詢SKU詳細
export function getSku(skuId) {
  return request({
    url: '/shop/sku/' + skuId,
    method: 'get'
  })
}

// 新增SKU
export function addSku(data) {
  return request({
    url: '/shop/sku',
    method: 'post',
    data: data
  })
}

// 修改SKU
export function updateSku(data) {
  return request({
    url: '/shop/sku',
    method: 'put',
    data: data
  })
}

// 刪除SKU
export function delSku(skuId) {
  return request({
    url: '/shop/sku/' + skuId,
    method: 'delete'
  })
}

// 批量儲存SKU
export function batchSaveSku(productId, skuList) {
  return request({
    url: '/shop/sku/batch/' + productId,
    method: 'post',
    data: skuList
  })
}
