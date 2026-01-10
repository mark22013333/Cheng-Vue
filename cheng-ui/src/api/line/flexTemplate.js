import request from '@/utils/request'

/**
 * 查詢 Flex 範本列表
 */
export function listFlexTemplate(query) {
  return request({
    url: '/line/flex/template/list',
    method: 'get',
    params: query
  })
}

/**
 * 查詢可用的 Flex 範本（私人 + 公開）
 */
export function getAvailableFlexTemplates() {
  return request({
    url: '/line/flex/template/available',
    method: 'get'
  })
}

/**
 * 查詢 Flex 範本詳情
 */
export function getFlexTemplate(flexTemplateId) {
  return request({
    url: '/line/flex/template/' + flexTemplateId,
    method: 'get'
  })
}

/**
 * 新增 Flex 範本
 */
export function addFlexTemplate(data) {
  return request({
    url: '/line/flex/template',
    method: 'post',
    data: data
  })
}

/**
 * 修改 Flex 範本
 */
export function updateFlexTemplate(data) {
  return request({
    url: '/line/flex/template',
    method: 'put',
    data: data
  })
}

/**
 * 刪除 Flex 範本
 */
export function delFlexTemplate(flexTemplateIds) {
  return request({
    url: '/line/flex/template/' + flexTemplateIds,
    method: 'delete'
  })
}
