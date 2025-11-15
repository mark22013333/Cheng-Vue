<template>
  <el-dialog
    :title="title"
    :visible.sync="dialogVisible"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="canClose"
    :before-close="handleClose"
  >
    <div class="progress-container">
      <!-- 進度條 -->
      <el-progress
        :percentage="percentage"
        :status="progressStatus"
        :stroke-width="20"
        :show-text="true"
      ></el-progress>
      
      <!-- 狀態訊息 -->
      <div class="status-message" :class="messageClass">
        <i :class="messageIcon"></i>
        <span>{{ currentMessage }}</span>
      </div>
      
      <!-- 詳細日誌（可選） -->
      <div v-if="showLogs && logs.length > 0" class="logs-container">
        <div class="log-item" v-for="(log, index) in logs" :key="index">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
    
    <div slot="footer" class="dialog-footer">
      <el-button v-if="canClose" @click="handleClose">關閉</el-button>
      <el-button v-if="canRetry" type="primary" @click="handleRetry">重試</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ProgressDialog',
  data() {
    return {
      dialogVisible: false,
      title: '處理中',
      percentage: 0,
      currentMessage: '準備中...',
      progressStatus: null, // null, 'success', 'exception', 'warning'
      logs: [],
      showLogs: false,
      canClose: false,
      canRetry: false,
      retryCallback: null
    }
  },
  computed: {
    messageClass() {
      if (this.progressStatus === 'success') return 'success'
      if (this.progressStatus === 'exception') return 'error'
      if (this.progressStatus === 'warning') return 'warning'
      return 'info'
    },
    messageIcon() {
      if (this.progressStatus === 'success') return 'el-icon-success'
      if (this.progressStatus === 'exception') return 'el-icon-error'
      if (this.progressStatus === 'warning') return 'el-icon-warning'
      return 'el-icon-loading'
    }
  },
  methods: {
    /**
     * 顯示對話框
     */
    show(options = {}) {
      this.dialogVisible = true
      this.title = options.title || '處理中'
      this.percentage = options.percentage || 0
      this.currentMessage = options.message || '準備中...'
      this.progressStatus = null
      this.logs = []
      this.showLogs = options.showLogs || false
      this.canClose = false
      this.canRetry = false
      this.retryCallback = null
    },
    
    /**
     * 更新進度
     */
    updateProgress(percentage, message) {
      this.percentage = Math.min(100, Math.max(0, percentage))
      if (message) {
        this.currentMessage = message
        this.addLog(message)
      }
    },
    
    /**
     * 設定成功狀態
     */
    setSuccess(message) {
      this.percentage = 100
      this.progressStatus = 'success'
      this.currentMessage = message || '處理完成！'
      this.canClose = true
      this.addLog(this.currentMessage)
      
      // 3 秒後自動關閉
      setTimeout(() => {
        if (this.dialogVisible) {
          this.close()
        }
      }, 3000)
    },
    
    /**
     * 設定錯誤狀態
     */
    setError(message, allowRetry = false, retryCallback = null) {
      this.progressStatus = 'exception'
      this.currentMessage = message || '處理失敗'
      this.canClose = true
      this.canRetry = allowRetry
      this.retryCallback = retryCallback
      this.addLog(this.currentMessage)
    },
    
    /**
     * 設定警告狀態
     */
    setWarning(message) {
      this.progressStatus = 'warning'
      this.currentMessage = message || '處理完成但有警告'
      this.canClose = true
      this.addLog(this.currentMessage)
    },
    
    /**
     * 添加日誌
     */
    addLog(message) {
      if (this.showLogs) {
        const now = new Date()
        const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
        this.logs.push({ time, message })
        
        // 限制日誌數量
        if (this.logs.length > 50) {
          this.logs.shift()
        }
      }
    },
    
    /**
     * 關閉對話框
     */
    close() {
      this.dialogVisible = false
      this.$emit('closed')
    },
    
    /**
     * 處理關閉
     */
    handleClose() {
      if (this.canClose) {
        this.close()
      }
    },
    
    /**
     * 處理重試
     */
    handleRetry() {
      if (this.retryCallback) {
        this.retryCallback()
        this.close()
      }
    }
  }
}
</script>

<style scoped>
.progress-container {
  padding: 20px;
}

.status-message {
  margin-top: 20px;
  padding: 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.status-message i {
  margin-right: 8px;
  font-size: 18px;
}

.status-message.info {
  background-color: #f4f4f5;
  color: #909399;
}

.status-message.success {
  background-color: #f0f9ff;
  color: #67c23a;
}

.status-message.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.status-message.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.logs-container {
  margin-top: 20px;
  max-height: 200px;
  overflow-y: auto;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
}

.log-item {
  font-size: 12px;
  color: #606266;
  margin-bottom: 5px;
  display: flex;
}

.log-time {
  color: #909399;
  margin-right: 8px;
  min-width: 60px;
}

.log-message {
  flex: 1;
}

.dialog-footer {
  text-align: right;
}
</style>
