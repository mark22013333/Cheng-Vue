import request from '@/utils/request'

// 查詢訊息範本列表
export function listTemplate(query) {
  return request({
    url: '/line/template/list',
    method: 'get',
    params: query
  })
}

// 查詢訊息範本詳細
export function getTemplate(templateId) {
  return request({
    url: '/line/template/' + templateId,
    method: 'get'
  })
}

// 根據範本代碼查詢
export function getTemplateByCode(templateCode) {
  return request({
    url: '/line/template/code/' + templateCode,
    method: 'get'
  })
}

// 根據訊息類型查詢範本列表
export function listTemplateByType(msgType) {
  return request({
    url: '/line/template/type/' + msgType,
    method: 'get'
  })
}

// 新增訊息範本
export function addTemplate(data) {
  return request({
    url: '/line/template',
    method: 'post',
    data: data
  })
}

// 修改訊息範本
export function updateTemplate(data) {
  return request({
    url: '/line/template',
    method: 'put',
    data: data
  })
}

// 刪除訊息範本
export function delTemplate(templateId) {
  return request({
    url: '/line/template/' + templateId,
    method: 'delete'
  })
}

// 驗證 Flex Message JSON
export function validateFlexJson(content) {
  return request({
    url: '/line/template/validate/flex',
    method: 'post',
    data: { content }
  })
}

// 格式化 JSON
export function formatJson(content) {
  return request({
    url: '/line/template/format/json',
    method: 'post',
    data: { content }
  })
}

// 預覽範本
export function previewTemplate(templateId, lineUserId) {
  return request({
    url: '/line/template/preview',
    method: 'post',
    data: { templateId, lineUserId }
  })
}

// 發送測試推播
export function sendTestMessage(templateId, lineUserId) {
  return request({
    url: '/line/template/sendTest',
    method: 'post',
    data: { templateId, lineUserId }
  })
}

// 匯出訊息範本
export function exportTemplate(query) {
  return request({
    url: '/line/template/export',
    method: 'post',
    params: query
  })
}

// 變更範本狀態
export function changeTemplateStatus(templateId, status) {
  return request({
    url: '/line/template/changeStatus',
    method: 'put',
    data: { templateId, status }
  })
}

// 取得 Flex 預設範本列表
export function getFlexPresets() {
  return request({
    url: '/line/template/flex/presets',
    method: 'get'
  })
}

// 取得指定 Flex 預設範本內容
export function getFlexPresetContent(templateName) {
  return request({
    url: '/line/template/flex/presets/' + templateName,
    method: 'get'
  })
}

// 上傳 Imagemap 圖片
export function uploadImagemapImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/line/template/upload/imagemap',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 刪除 Imagemap 圖片
export function deleteImagemapImage(uuid) {
  return request({
    url: '/line/template/imagemap/' + uuid,
    method: 'delete'
  })
}

// 查詢圖文範本被哪些訊息範本引用
export function getImagemapReferences(imagemapId) {
  return request({
    url: '/line/template/imagemap/' + imagemapId + '/references',
    method: 'get'
  })
}

// 同步更新引用的訊息範本
export function syncImagemapReferences(imagemapId, templateIds = null) {
  return request({
    url: '/line/template/imagemap/' + imagemapId + '/sync-references',
    method: 'post',
    data: templateIds ? { templateIds } : {}
  })
}
