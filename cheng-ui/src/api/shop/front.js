import request from '@/utils/request'

/**
 * 查詢前台輪播圖（公開 API）
 */
export function listFrontBanners(position = 'HOME_TOP') {
  return request({
    url: '/shop/front/banners',
    method: 'get',
    params: { position }
  })
}

/**
 * 查詢熱門商品
 */
export function listHotProducts(limit = 8) {
  return request({
    url: '/shop/front/products/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 查詢新品商品
 */
export function listNewProducts(limit = 8) {
  return request({
    url: '/shop/front/products/new',
    method: 'get',
    params: { limit }
  })
}

/**
 * 查詢推薦商品
 */
export function listRecommendProducts(limit = 8) {
  return request({
    url: '/shop/front/products/recommend',
    method: 'get',
    params: { limit }
  })
}

/**
 * 查詢商品列表
 */
export function listProducts(query) {
  return request({
    url: '/shop/front/products',
    method: 'get',
    params: query
  })
}

/**
 * 查詢商品詳情
 */
export function getProduct(productId) {
  return request({
    url: `/shop/front/product/${productId}`,
    method: 'get'
  })
}

/**
 * 查詢商品 SKU 列表
 */
export function getProductSkus(productId) {
  return request({
    url: `/shop/front/product/${productId}/skus`,
    method: 'get'
  })
}

/**
 * 查詢分類列表
 */
export function listCategories() {
  return request({
    url: '/shop/front/categories',
    method: 'get'
  })
}

/**
 * 查詢已發布文章列表（前台，分頁）
 */
export function listFrontArticles(query) {
  return request({
    url: '/shop/front/articles',
    method: 'get',
    params: query
  })
}

/**
 * 查詢文章詳情（前台）
 */
export function getFrontArticle(articleId) {
  return request({
    url: `/shop/front/article/${articleId}`,
    method: 'get'
  })
}

/**
 * 查詢最新文章（首頁用）
 */
export function listLatestArticles(limit = 6) {
  return request({
    url: '/shop/front/articles/latest',
    method: 'get',
    params: { limit }
  })
}
