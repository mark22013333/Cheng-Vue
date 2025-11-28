<template>
  <el-dialog
    title="測試 LINE 連線"
    v-model="dialogVisible"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
  >
    <div class="test-container">
      <!-- 頻道資訊 -->
      <el-alert
        title="測試頻道資訊"
        type="info"
        :closable="false"
        class="mb-3"
      >
        <div class="channel-info">
          <div><strong>頻道名稱：</strong>{{ config.channelName }}</div>
          <div><strong>Bot Basic ID：</strong>{{ config.botBasicId || '-' }}</div>
          <div><strong>Channel ID：</strong>{{ config.channelId }}</div>
        </div>
      </el-alert>

      <!-- 測試項目 -->
      <el-card shadow="never" class="test-card">
        <div slot="header">
          <span>測試項目</span>
        </div>

        <el-timeline>
          <!-- API 連線測試 -->
          <el-timeline-item
            :type="getStepType(testResults.api)"
            :icon="getStepIcon(testResults.api)"
            :class="{'breathing-success': testResults.api && testResults.api.success}"
          >
            <div class="test-item">
              <div class="test-title">
                <span>API 連線測試</span>
                <el-tag
                  v-if="testResults.api"
                  :type="getResultType(testResults.api.success)"
                  size="small"
                >
                  {{ testResults.api.success ? '通過' : '失敗' }}
                </el-tag>
              </div>
              <div class="test-desc">測試 Channel Access Token 是否有效</div>
              <div v-if="testResults.api && testResults.api.message" class="test-message">
                <el-alert
                  :type="testResults.api.success ? 'success' : 'error'"
                  :closable="false"
                  show-icon
                >
                  {{ testResults.api.message }}
                </el-alert>
              </div>
            </div>
          </el-timeline-item>

          <!-- Webhook 設定檢查 -->
          <el-timeline-item
            :type="getStepType(testResults.webhook)"
            :icon="getStepIcon(testResults.webhook)"
            :class="{'breathing-success': testResults.webhook && testResults.webhook.success}"
          >
            <div class="test-item">
              <div class="test-title">
                <span>Webhook 設定檢查</span>
                <el-tag
                  v-if="testResults.webhook"
                  :type="getResultType(testResults.webhook.success)"
                  size="small"
                >
                  {{ testResults.webhook.success ? '通過' : '失敗' }}
                </el-tag>
              </div>
              <div class="test-desc">檢查 LINE Developers 的 Webhook 設定</div>
              <div v-if="testResults.webhook && testResults.webhook.message" class="test-message">
                <el-alert
                  :type="testResults.webhook.success ? 'success' : 'warning'"
                  :closable="false"
                  show-icon
                >
                  {{ testResults.webhook.message }}
                </el-alert>
              </div>
            </div>
          </el-timeline-item>

          <!-- Bot 資訊取得 -->
          <el-timeline-item
            :type="getStepType(testResults.bot)"
            :icon="getStepIcon(testResults.bot)"
            :class="{'breathing-success': testResults.bot && testResults.bot.success}"
          >
            <div class="test-item">
              <div class="test-title">
                <span>Bot 資訊取得</span>
                <el-tag
                  v-if="testResults.bot"
                  :type="getResultType(testResults.bot.success)"
                  size="small"
                >
                  {{ testResults.bot.success ? '通過' : '失敗' }}
                </el-tag>
              </div>
              <div class="test-desc">取得 Bot 的基本資訊</div>
              <div v-if="testResults.bot && testResults.bot.data" class="test-data">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="顯示名稱">
                    {{ testResults.bot.data.displayName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="狀態訊息">
                    {{ testResults.bot.data.statusMessage || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="圖片 URL">
                    <el-link :href="testResults.bot.data.pictureUrl" target="_blank" type="primary">
                      查看圖片
                    </el-link>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 測試結果摘要 -->
      <el-alert
        v-if="testComplete"
        :type="allTestsPassed ? 'success' : 'error'"
        :title="allTestsPassed ? '所有測試通過' : '✗ 部分測試失敗'"
        :closable="false"
        show-icon
        class="mt-3"
      />
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">關 閉</el-button>
      <el-button
        type="primary"
        @click="runTest"
        :loading="testing"
        icon="el-icon-refresh-right"
      >
        {{ testing ? '測試中...' : '重新測試' }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { testConnection } from '@/api/line/config'

export default {
  name: 'ConnectionTest',
  data() {
    return {
      // 是否顯示對話框
      dialogVisible: false,
      // 是否測試中
      testing: false,
      // 測試完成
      testComplete: false,
      // 頻道設定
      config: {},
      // 測試結果
      testResults: {
        api: null,
        webhook: null,
        bot: null
      }
    }
  },
  computed: {
    /** 是否所有測試通過 */
    allTestsPassed() {
      return this.testResults.api?.success &&
             this.testResults.webhook?.success &&
             this.testResults.bot?.success
    }
  },
  methods: {
    /** 開啟對話框 */
    open(config) {
      this.config = config
      this.testResults = {
        api: null,
        webhook: null,
        bot: null
      }
      this.testComplete = false
      this.dialogVisible = true
      // 自動執行測試
      this.$nextTick(() => {
        this.runTest()
      })
    },
    /** 執行測試 */
    runTest() {
      this.testing = true
      this.testComplete = false
      this.testResults = {
        api: null,
        webhook: null,
        bot: null
      }

      testConnection(this.config.configId)
        .then(response => {
          console.log('=== 測試連線完成 ===')
          console.log('測試結果:', response.data)
          this.testResults = response.data

          // 測試完成後，通知父元件重新整理列表（更新 webhook 狀態）
          console.log('觸發 test-complete 事件，通知父元件重新載入列表')
          this.$emit('test-complete')
        })
        .catch(error => {
          console.error('測試連線失敗:', error)
          this.$modal.msgError('測試連線失敗：' + error.message)
        })
        .finally(() => {
          this.testing = false
          this.testComplete = true
        })
    },
    /** 取得步驟類型 */
    getStepType(result) {
      if (!result) return 'info'
      return result.success ? 'success' : 'danger'
    },
    /** 取得步驟圖示 */
    getStepIcon(result) {
      if (!result) return 'el-icon-loading'
      return result.success ? 'el-icon-circle-check' : 'el-icon-circle-close'
    },
    /** 取得結果類型 */
    getResultType(success) {
      return success ? 'success' : 'danger'
    }
  }
}
</script>

<style lang="scss" scoped>
.test-container {
  .channel-info {
    font-size: 14px;
    line-height: 1.8;
  }

  .test-card {
    margin-bottom: 16px;

    .test-item {
      .test-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        font-size: 15px;
        font-weight: 600;
      }

      .test-desc {
        font-size: 13px;
        color: #909399;
        margin-bottom: 10px;
      }

      .test-message,
      .test-data {
        margin-top: 10px;
      }
    }
  }
}

.mb-3 {
  margin-bottom: 16px;
}

.mt-3 {
  margin-top: 16px;
}

// 綠光呼吸燈效果
.breathing-success {
  :deep(.el-timeline-item__node) {
    animation: breathing 2s ease-in-out infinite;
  }
}

@keyframes breathing {
  0%, 100% {
    box-shadow: 0 0 5px rgba(103, 194, 58, 0.5),
                0 0 10px rgba(103, 194, 58, 0.3),
                0 0 15px rgba(103, 194, 58, 0.2);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 0 10px rgba(103, 194, 58, 0.8),
                0 0 20px rgba(103, 194, 58, 0.6),
                0 0 30px rgba(103, 194, 58, 0.4);
    transform: scale(1.05);
  }
}
</style>
