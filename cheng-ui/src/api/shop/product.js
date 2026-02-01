import request from '@/utils/request'

// 查詢商品列表
export function listProduct(query) {
  return request({
    url: '/shop/product/list',
    method: 'get',
    params: query
  })
}

// 查詢商品詳細
export function getProduct(productId) {
  return request({
    url: '/shop/product/' + productId,
    method: 'get'
  })
}

// 新增商品
export function addProduct(data) {
  return request({
    url: '/shop/product',
    method: 'post',
    data: data
  })
}

// 修改商品
export function updateProduct(data) {
  return request({
    url: '/shop/product',
    method: 'put',
    data: data
  })
}

// 刪除商品
export function delProduct(productId) {
  return request({
    url: '/shop/product/' + productId,
    method: 'delete'
  })
}

// 上架商品
export function onSaleProduct(productId) {
  return request({
    url: '/shop/product/onSale/' + productId,
    method: 'put'
  })
}

// 下架商品
export function offSaleProduct(productId) {
  return request({
    url: '/shop/product/offSale/' + productId,
    method: 'put'
  })
}

// 批量更新商品標記（熱門/新品/推薦）
export function updateProductFlag(data) {
  return request({
    url: '/shop/product/flag',
    method: 'put',
    data: data
  })
}
