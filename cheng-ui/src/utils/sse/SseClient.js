/**
 * SSE (Server-Sent Events) 客戶端工具
 * 提供統一的 SSE 連線、重連、事件處理功能
 * 適配 Vue 3 + Vite 環境
 *
 * @author cheng
 */

import {getToken} from '@/utils/auth'

export default class SseClient {
  /**
   * 建立 SSE 客戶端
   *
   * @param {Object} options 配置選項
   * @param {string} options.channel 頻道名稱
   * @param {string} options.taskId 任務 ID
   * @param {number} options.timeout 超時時間（毫秒），預設 30 分鐘
   * @param {boolean} options.autoReconnect 是否自動重連，預設 true
   * @param {number} options.reconnectInterval 重連間隔（毫秒），預設 3000
   * @param {number} options.maxReconnectAttempts 最大重連次數，預設 5
   */
  constructor(options = {}) {
    this.channel = options.channel
    this.taskId = options.taskId
    this.timeout = options.timeout || 1800000 // 預設 30 分鐘
    this.autoReconnect = options.autoReconnect !== false
    this.reconnectInterval = options.reconnectInterval || 3000
    this.maxReconnectAttempts = options.maxReconnectAttempts || 5

    this.eventSource = null
    this.reconnectAttempts = 0
    this.isManualClose = false
    this.listeners = new Map() // 事件監聽器

    // 預設處理器
    this.onConnected = null
    this.onError = null
    this.onClosed = null
  }

  /**
   * 建立 SSE 連線
   */
  connect() {
    if (this.eventSource) {
      console.warn('[SSE] 已存在連線，先關閉舊連線')
      this.close()
    }

    try {
      // 建構 SSE URL
      // 注意：Vite 環境通常使用 import.meta.env.VITE_APP_BASE_API
      // 如果您的專案環境變數名稱不同，請在此修改
      const baseUrl = import.meta.env.VITE_APP_BASE_API || ''

      // SSE 標準不支援 Header 傳遞 Token，通常透過 Query String 傳遞
      const token = getToken()
      // 這裡假設後端接收 Authorization 參數，如果後端不需要 token 可移除
      const url = `${baseUrl}/sse/subscribe/${this.channel}/${this.taskId}?timeout=${this.timeout}&Authorization=${token}`

      console.log(`[SSE] 建立連線: ${url}`)

      // 建立 EventSource
      this.eventSource = new EventSource(url)

      // 連線成功
      this.eventSource.onopen = (event) => {
        console.log('[SSE] 連線成功', event)
        this.reconnectAttempts = 0

        if (this.onConnected) {
          this.onConnected(event)
        }

        this.emit('connected', event)
      }

      // 接收訊息
      this.eventSource.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('[SSE] 收到訊息:', data)

          // 根據 eventName 分發事件
          if (data.eventName) {
            this.emit(data.eventName, data)
          }

          // 統一的 message 事件
          this.emit('message', data)

        } catch (e) {
          // SSE 有時候會收到純文字或是非 JSON 格式的 ping，視情況忽略錯誤
          // console.error('[SSE] 解析訊息失敗:', e, event.data)
        }
      }

      // 監聽命名事件（不包含 connected，因為 onopen 已經處理）
      const eventTypes = ['progress', 'success', 'error', 'heartbeat']
      eventTypes.forEach(eventType => {
        this.eventSource.addEventListener(eventType, (event) => {
          try {
            const data = JSON.parse(event.data)
            console.log(`[SSE] 收到 ${eventType} 事件:`, data)

            // 分發事件
            this.emit(eventType, data)
            this.emit('message', data)

          } catch (e) {
            console.error(`[SSE] 解析 ${eventType} 事件失敗:`, e, event.data)
          }
        })
      })

      // 連線錯誤
      this.eventSource.onerror = (event) => {
        // EventSource 錯誤時通常無法取得詳細狀態碼，只會觸發錯誤
        console.error('[SSE] 連線錯誤 (readyState):', this.eventSource.readyState)

        if (this.onError) {
          this.onError(event)
        }

        this.emit('error', event)

        // 自動重連
        if (!this.isManualClose && this.autoReconnect) {
          this.eventSource.close() // 確保舊的關閉
          this.reconnect()
        }
      }

    } catch (error) {
      console.error('[SSE] 建立連線失敗:', error)
      if (this.onError) {
        this.onError(error)
      }
    }
  }

  /**
   * 重新連線
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error(`[SSE] 達到最大重連次數 (${this.maxReconnectAttempts})，停止重連`)
      this.emit('maxReconnectReached')
      return
    }

    this.reconnectAttempts++
    console.log(`[SSE] 嘗試重連 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)

    setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  /**
   * 關閉連線
   */
  close(eventName, data) {
    this.isManualClose = true

    if (this.eventSource) {
      console.log('[SSE] 關閉連線')
      this.eventSource.close()
      this.eventSource = null

      if (this.onClosed) {
        this.onClosed()
      }

      if (eventName) {
        this.emit(eventName, data)
      } else {
        this.emit('closed', data)
      }
    }
  }

  /**
   * 監聽事件
   *
   * @param {string} eventName 事件名稱 (connected, message, error, closed, progress, success, etc.)
   * @param {Function} callback 回調函數
   */
  on(eventName, callback) {
    if (!this.listeners.has(eventName)) {
      this.listeners.set(eventName, [])
    }
    this.listeners.get(eventName).push(callback)
    return this
  }

  /**
   * 移除事件監聽
   *
   * @param {string} eventName 事件名稱
   * @param {Function} callback 回調函數（可選，不傳則移除所有）
   */
  off(eventName, callback) {
    if (!this.listeners.has(eventName)) {
      return this
    }

    if (callback) {
      const callbacks = this.listeners.get(eventName)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    } else {
      this.listeners.delete(eventName)
    }

    return this
  }

  /**
   * 觸發事件
   *
   * @param {string} eventName 事件名稱
   * @param {any} data 事件資料
   */
  emit(eventName, data) {
    if (this.listeners.has(eventName)) {
      const callbacks = this.listeners.get(eventName)
      callbacks.forEach(callback => {
        try {
          callback(data)
        } catch (e) {
          console.error(`[SSE] 事件處理器執行錯誤 (${eventName}):`, e)
        }
      })
    }
  }

  /**
   * 取得連線狀態
   *
   * @returns {number} 0: CONNECTING, 1: OPEN, 2: CLOSED
   */
  getReadyState() {
    return this.eventSource ? this.eventSource.readyState : 2
  }

  /**
   * 是否已連線
   *
   * @returns {boolean}
   */
  isConnected() {
    return this.eventSource && this.eventSource.readyState === 1
  }
}

/**
 * SSE 事件類型常數
 */
export const SSE_EVENTS = {
  CONNECTED: 'connected',
  MESSAGE: 'message',
  ERROR: 'error',
  CLOSED: 'closed',
  PROGRESS: 'progress',
  SUCCESS: 'success',
  HEARTBEAT: 'heartbeat'
}

/**
 * SSE 頻道常數
 */
export const SSE_CHANNELS = {
  RICHMENU_PUBLISH: 'richmenu-publish',
  QRCODE_SCAN: 'qrcode-scan',
  CRAWL_TASK: 'crawl-task'
}
