import request from '@/utils/request'

// 查詢分類列表
export function listCategory(query) {
  return request({
    url: '/shop/category/list',
    method: 'get',
    params: query
  })
}

// 查詢分類樹狀結構
export function treeCategory() {
  return request({
    url: '/shop/category/tree',
    method: 'get'
  })
}

// 查詢分類詳細
export function getCategory(categoryId) {
  return request({
    url: '/shop/category/' + categoryId,
    method: 'get'
  })
}

// 新增分類
export function addCategory(data) {
  return request({
    url: '/shop/category',
    method: 'post',
    data: data
  })
}

// 修改分類
export function updateCategory(data) {
  return request({
    url: '/shop/category',
    method: 'put',
    data: data
  })
}

// 刪除分類
export function delCategory(categoryId) {
  return request({
    url: '/shop/category/' + categoryId,
    method: 'delete'
  })
}
