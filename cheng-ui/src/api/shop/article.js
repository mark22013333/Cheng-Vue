import request from '@/utils/request'

// 查詢文章列表
export function listArticle(query) {
  return request({
    url: '/shop/article/list',
    method: 'get',
    params: query
  })
}

// 查詢文章詳細
export function getArticle(articleId) {
  return request({
    url: '/shop/article/' + articleId,
    method: 'get'
  })
}

// 新增文章
export function addArticle(data) {
  return request({
    url: '/shop/article',
    method: 'post',
    data: data
  })
}

// 修改文章
export function updateArticle(data) {
  return request({
    url: '/shop/article',
    method: 'put',
    data: data
  })
}

// 刪除文章
export function delArticle(articleId) {
  return request({
    url: '/shop/article/' + articleId,
    method: 'delete'
  })
}
