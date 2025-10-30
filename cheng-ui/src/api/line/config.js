import request from '@/utils/request'

// 查詢 LINE 頻道設定列表
export function listConfig(query) {
  return request({
    url: '/line/config/list',
    method: 'get',
    params: query
  })
}

// 查詢 LINE 頻道設定詳細
export function getConfig(configId) {
  return request({
    url: '/line/config/' + configId,
    method: 'get'
  })
}

// 新增 LINE 頻道設定
export function addConfig(data) {
  return request({
    url: '/line/config',
    method: 'post',
    data: data
  })
}

// 修改 LINE 頻道設定
export function updateConfig(data) {
  return request({
    url: '/line/config',
    method: 'put',
    data: data
  })
}

// 刪除 LINE 頻道設定
export function delConfig(configId) {
  return request({
    url: '/line/config/' + configId,
    method: 'delete'
  })
}

// 測試 LINE 連線
export function testConnection(configId) {
  return request({
    url: '/line/config/testConnection/' + configId,
    method: 'post'
  })
}

// 測試 Webhook
export function testWebhook(data) {
  return request({
    url: '/line/config/testWebhook',
    method: 'post',
    data: data
  })
}

// 設定為預設頻道
export function setDefaultChannel(configId) {
  return request({
    url: '/line/config/setDefault/' + configId,
    method: 'put'
  })
}

// 取得預設頻道
export function getDefaultChannel() {
  return request({
    url: '/line/config/default',
    method: 'get'
  })
}

// 匯出 LINE 頻道設定
export function exportConfig(query) {
  return request({
    url: '/line/config/export',
    method: 'post',
    params: query
  })
}

// 產生 Webhook URL
export function generateWebhookUrl(botBasicId) {
  return request({
    url: '/line/config/generateWebhookUrl',
    method: 'get',
    params: { botBasicId }
  })
}

// 檢查指定類型的頻道是否已存在
export function checkChannelType(channelType) {
  return request({
    url: '/line/config/checkChannelType/' + channelType,
    method: 'get'
  })
}

// 設定 LINE Webhook URL
export function setLineWebhook(configId) {
  return request({
    url: '/line/config/setLineWebhook/' + configId,
    method: 'post'
  })
}

// 設定 LINE Webhook URL（使用表單當前值）
export function setLineWebhookWithParams(data) {
  return request({
    url: '/line/config/setLineWebhookWithParams',
    method: 'post',
    data: data
  })
}

// 取得系統預設的 Webhook 基礎 URL
export function getDefaultWebhookBaseUrl() {
  return request({
    url: '/line/config/defaultWebhookBaseUrl',
    method: 'get'
  })
}
