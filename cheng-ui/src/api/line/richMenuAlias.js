import request from '@/utils/request'

// 查詢 Rich Menu Alias 列表
export function listRichMenuAlias(query) {
  return request({
    url: '/line/richMenuAlias/list',
    method: 'get',
    params: query
  })
}

// 根據 Rich Menu ID 查詢所有 Alias
export function listRichMenuAliasByRichMenuId(richMenuId) {
  return request({
    url: `/line/richMenuAlias/listByRichMenuId/${richMenuId}`,
    method: 'get'
  })
}

// 查詢所有可用的 Alias（用於下拉選單）
export function listAllAliases() {
  return request({
    url: '/line/richMenuAlias/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 9999 }
  })
}

// 查詢 Rich Menu Alias 詳細
export function getRichMenuAlias(id) {
  return request({
    url: '/line/richMenuAlias/' + id,
    method: 'get'
  })
}

// 根據 Alias ID 查詢詳細
export function getRichMenuAliasByAliasId(aliasId) {
  return request({
    url: '/line/richMenuAlias/byAliasId/' + aliasId,
    method: 'get'
  })
}

// 新增 Rich Menu Alias
export function addRichMenuAlias(data) {
  return request({
    url: '/line/richMenuAlias',
    method: 'post',
    data: data
  })
}

// 修改 Rich Menu Alias
export function updateRichMenuAlias(data) {
  return request({
    url: '/line/richMenuAlias',
    method: 'put',
    data: data
  })
}

// 刪除 Rich Menu Alias
export function delRichMenuAlias(id) {
  return request({
    url: '/line/richMenuAlias/' + id,
    method: 'delete'
  })
}

// 檢查 Alias ID 是否唯一
export function checkAliasIdUnique(aliasId) {
  return request({
    url: '/line/richMenuAlias/checkAliasIdUnique',
    method: 'get',
    params: { aliasId }
  })
}

// 從 LINE 平台同步 Alias 列表
export function syncAliasFromLine() {
  return request({
    url: '/line/richMenuAlias/sync',
    method: 'post'
  })
}

// 檢查別名使用情況
export function checkAliasUsage(aliasId) {
  return request({
    url: '/line/richMenuAlias/checkUsage/' + aliasId,
    method: 'get'
  })
}
