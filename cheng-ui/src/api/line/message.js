import request from '@/utils/request'

// 查詢推播訊息記錄列表
export function listMessage(query) {
  return request({
    url: '/line/message/list',
    method: 'get',
    params: query
  })
}

// 查詢推播訊息記錄列表（別名，供訊息編輯器頁面使用）
export function listMessageLog(query) {
  return request({
    url: '/line/message/list',
    method: 'get',
    params: query
  })
}

// 查詢推播訊息記錄詳細
export function getMessage(messageId) {
  return request({
    url: '/line/message/' + messageId,
    method: 'get'
  })
}

// 發送推播訊息（單人）
export function sendPushMessage(data) {
  return request({
    url: '/line/message/push',
    method: 'post',
    data: data
  })
}

// 發送推播訊息（多人）
export function sendMulticastMessage(data) {
  return request({
    url: '/line/message/multicast',
    method: 'post',
    data: data
  })
}

// 發送廣播訊息
export function sendBroadcastMessage(data) {
  return request({
    url: '/line/message/broadcast',
    method: 'post',
    data: data
  })
}

// 發送 Flex Message（彈性訊息）- 舊 API
export function sendFlexMessage(data) {
  return request({
    url: '/line/message/flex',
    method: 'post',
    data: data
  })
}

// 發送訊息（通用介面）
export function sendMessage(data) {
  return request({
    url: '/line/message/send',
    method: 'post',
    data: data
  })
}

// 發送 Flex 訊息（新 API）
export function sendFlexMessageV2(data) {
  return request({
    url: '/line/message/send/flex',
    method: 'post',
    data: data
  })
}

// 發送文字訊息
export function sendTextMessage(data) {
  return request({
    url: '/line/message/send/text',
    method: 'post',
    data: data
  })
}

// 發送圖片訊息
export function sendImageMessage(data) {
  return request({
    url: '/line/message/send/image',
    method: 'post',
    data: data
  })
}

// 使用範本發送訊息
export function sendTemplateMessage(data) {
  return request({
    url: '/line/message/send/template',
    method: 'post',
    data: data
  })
}

// 刪除推播訊息記錄
export function delMessage(messageId) {
  return request({
    url: '/line/message/' + messageId,
    method: 'delete'
  })
}

// 匯出推播訊息記錄
export function exportMessage(query) {
  return request({
    url: '/line/message/export',
    method: 'post',
    params: query
  })
}

// 取得訊息統計資料
export function getMessageStats(query) {
  return request({
    url: '/line/message/stats',
    method: 'get',
    params: query
  })
}

// 驗證 Flex Message JSON 格式
export function validateFlexMessage(data) {
  return request({
    url: '/line/message/validateFlex',
    method: 'post',
    data: data
  })
}

// 預覽 Flex Message
export function previewFlexMessage(data) {
  return request({
    url: '/line/message/previewFlex',
    method: 'post',
    data: data
  })
}
