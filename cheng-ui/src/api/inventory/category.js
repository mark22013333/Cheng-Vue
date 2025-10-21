import request from '@/utils/request'

// 查詢物品分類列表
export function listCategory(query) {
  return request({
    url: '/inventory/category/list',
    method: 'get',
    params: query
  })
}

// 查詢已使用的物品分類列表（用於下拉選單）
export function listUsedCategory() {
  return request({
    url: '/inventory/category/used',
    method: 'get'
  })
}

// 查詢物品分類詳細
export function getCategory(categoryId) {
  return request({
    url: '/inventory/category/' + categoryId,
    method: 'get'
  })
}

// 新增物品分類
export function addCategory(data) {
  return request({
    url: '/inventory/category',
    method: 'post',
    data: data
  })
}

// 修改物品分類
export function updateCategory(data) {
  return request({
    url: '/inventory/category',
    method: 'put',
    data: data
  })
}

// 刪除物品分類
export function delCategory(categoryId) {
  return request({
    url: '/inventory/category/' + categoryId,
    method: 'delete'
  })
}
