<template>
  <div class="app-container line-config-container">
    <!-- 頁面標題與操作按鈕 -->
    <el-row :gutter="20" class="header-section">
      <el-col :span="12">
        <div class="page-title">
          <el-icon class="mr-2"><Message /></el-icon>
          <span>LINE 頻道設定</span>
        </div>
      </el-col>
      <el-col :span="12" class="text-right">
        <el-button
          :icon="Refresh"
          @click="getList"
        >
          重新整理
        </el-button>
      </el-col>
    </el-row>

    <!-- 頻道卡片區域 -->
    <el-row :gutter="20" v-loading="loading" class="channel-cards-container">
      <!-- 遍歷三個固定的頻道類型 -->
      <el-col :xs="24" :sm="12" :lg="8" v-for="channel in channels" :key="channel.type">
        <!-- 已設定的頻道卡片 -->
        <el-card
          v-if="channel.config"
          shadow="hover"
          class="channel-card"
          :class="getCardClass(channel)"
        >
          <!-- 卡片標題 -->
          <div slot="header" class="card-header">
            <div class="card-title">
              <el-icon :style="{ color: channel.color, fontSize: '20px' }">
                <component :is="channel.icon" />
              </el-icon>
              <span class="type-label">{{ channel.label }}</span>
              <el-tag v-if="isEnabled(channel.config.status)" type="success" size="small" effect="plain">
                執行中
              </el-tag>
              <el-tag v-else type="info" size="small" effect="plain">
                已停用
              </el-tag>
            </div>
            <div class="card-actions">
              <el-dropdown @command="(command) => handleCommand(command, channel.config)">
                <span class="more-btn">
                  <el-icon><More /></el-icon>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="edit" :icon="Edit">
                    編輯
                  </el-dropdown-item>
                  <el-dropdown-item command="test" :icon="Connection">
                    測試連線
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="delete"
                    :icon="Delete"
                    divided
                  >
                    刪除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>

          <!-- 卡片內容 -->
          <div class="card-content">
            <!-- 頻道名稱 -->
            <div class="channel-name-section">
              <h3>{{ channel.config.channelName }}</h3>
            </div>

            <!-- Bot 資訊 -->
            <div class="info-section">
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span class="label">Bot ID:</span>
                <span class="value">{{ channel.config.botBasicId || '-' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Key /></el-icon>
                <span class="label">Channel ID:</span>
                <span class="value">{{ maskSecret(channel.config.channelId, 4) }}</span>
              </div>
            </div>

            <!-- Webhook 狀態 -->
            <div class="webhook-status">
              <el-icon><Link /></el-icon>
              <span>Webhook:</span>
              <el-tag
                v-if="isEnabled(channel.config.webhookStatus)"
                type="success"
                size="small"
              >
                已驗證
              </el-tag>
              <el-tag v-else type="info" size="small">未驗證</el-tag>
            </div>

            <!-- 快速操作 -->
            <div class="quick-actions">
              <el-button
                type="info"
                :icon="Connection"
                plain
                @click="handleTestConnection(channel.config)"
              >
                測試連線
              </el-button>
              <el-button
                type="primary"
                :icon="Setting"
                @click="handleEdit(channel.config)"
              >
                設定
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 未設定的頻道卡片（空狀態） -->
        <el-card
          v-else
          shadow="hover"
          class="channel-card empty-card"
          :class="'channel-' + channel.type.toLowerCase()"
        >
          <div slot="header" class="card-header">
            <div class="card-title">
              <el-icon :style="{ color: channel.color, fontSize: '20px' }">
                <component :is="channel.icon" />
              </el-icon>
              <span class="type-label">{{ channel.label }}</span>
              <el-tag type="info" size="small" effect="plain">
                未設定
              </el-tag>
            </div>
          </div>

          <div class="card-content empty-content">
            <div class="empty-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <p class="empty-text">尚未設定此頻道</p>
            <p class="empty-desc">{{ channel.description }}</p>
            <el-button
              type="primary"
              size="default"
              :icon="Plus"
              @click="handleAdd(channel.type)"
              v-hasPermi="[LINE_CONFIG_ADD]"
            >
              立即設定
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/編輯對話框 -->
    <config-form
      ref="configForm"
      @success="getList"
    />

    <!-- 連線測試對話框 -->
    <connection-test
      ref="connectionTest"
      @test-complete="getList"
    />
  </div>
</template>

<script>
import {
  LINE_CONFIG_ADD
} from '@/constants/permissions'
import { listConfig, delConfig, setDefaultChannel, testConnection } from '@/api/line/config'
import ConfigForm from './components/ConfigForm'
import ConnectionTest from './components/ConnectionTest'
import { 
  Message, Refresh, StarFilled, Comment, DataAnalysis, 
  More, Edit, Connection, Delete, User, Key, Link, 
  Setting, Warning, Plus 
} from '@element-plus/icons-vue'

export default {
  name: 'LineConfig',
  components: {
    ConfigForm,
    ConnectionTest,
    Message, Refresh, StarFilled, Comment, DataAnalysis,
    More, Edit, Connection, Delete, User, Key, Link,
    Setting, Warning, Plus
  },
  data() {
    return {
      // 載入狀態
      loading: false,
      // 固定的三個頻道類型配置
      channels: [
        {
          type: 'MAIN',
          label: '主頻道',
          icon: 'StarFilled',
          color: '#E6A23C',
          description: '系統預設使用的主要 LINE 頻道',
          config: null
        },
        {
          type: 'SUB',
          label: '副頻道',
          icon: 'Comment',
          color: '#409EFF',
          description: '備用頻道，可用於分流或測試',
          config: null
        },
        {
          type: 'TEST',
          label: '測試頻道',
          icon: 'DataAnalysis',
          color: '#909399',
          description: '開發測試專用頻道，不影響正式環境',
          config: null
        }
      ]
    }
  },
  setup() {
    return {
      LINE_CONFIG_ADD,
      Refresh, Edit, Connection, Delete, Setting, Plus
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查詢頻道列表並填充到對應的頻道卡片 */
    getList() {
      console.log('=== 開始重新載入頻道列表 ===')
      this.loading = true
      listConfig({ pageNum: 1, pageSize: 100 }).then(response => {
        console.log('=== 列表查詢回應 ===')
        console.log('查詢結果:', response.rows)
        const configs = response.rows || []

        // 重置所有頻道的 config
        this.channels.forEach(channel => {
          channel.config = null
        })

        // 將查詢到的配置填充到對應的頻道
        configs.forEach(config => {
          console.log('頻道設定:', config)
          console.log('status 欄位:', config.status)
          console.log('webhookStatus 欄位:', config.webhookStatus)
          console.log('status 類型:', typeof config.status)

          const channel = this.channels.find(ch => ch.type === config.channelType)
          if (channel) {
            channel.config = config
            console.log(`已填充 ${config.channelType} 頻道，webhook狀態：`, config.webhookStatus)
          }
        })
        console.log('最終 channels:', this.channels)
        console.log('=== 列表查詢完成 ===')

        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 新增按鈕操作 */
    handleAdd(channelType) {
      // 傳入頻道類型，讓表單預設該類型
      this.$refs.configForm.open(null, channelType)
    },
    /** 編輯按鈕操作 */
    handleEdit(row) {
      this.$refs.configForm.open(row.configId, row.channelType)
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      this.$modal.confirm('是否確認刪除頻道名稱為"' + row.channelName + '"的資料選項？').then(() => {
        return delConfig(row.configId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('刪除成功')
      }).catch(() => {})
    },
    /** 測試連線 */
    handleTestConnection(row) {
      this.$refs.connectionTest.open(row)
    },
    /** 下拉選單指令處理 */
    handleCommand(command, row) {
      switch (command) {
        case 'edit':
          this.handleEdit(row)
          break
        case 'test':
          this.handleTestConnection(row)
          break
        case 'delete':
          this.handleDelete(row)
          break
      }
    },
    /** 取得卡片樣式類別 */
    getCardClass(channel) {
      const classMap = {
        'MAIN': 'channel-main',
        'SUB': 'channel-sub',
        'TEST': 'channel-test'
      }
      return classMap[channel.type] || ''
    },
    /** 遮罩敏感資訊 */
    maskSecret(secret, showLength = 4) {
      if (!secret) return '-'
      const len = secret.length
      if (len <= showLength * 2) {
        return '*'.repeat(len)
      }
      return secret.substring(0, showLength) + '****' + secret.substring(len - showLength)
    },
    /** 判斷狀態是否啟用 */
    isEnabled(status) {
      console.log('=== isEnabled 檢查 ===')
      console.log('輸入 status:', status)
      console.log('status 類型:', typeof status)

      if (!status) {
        console.log('status 為空，返回 false')
        return false
      }

      // 處理多種格式：
      // 1. 枚舉物件：{ code: 1, description: "啟用" }
      // 2. 數字：1 或 0
      // 3. 字串：'ENABLE' 或 'DISABLE'
      let result = false

      if (typeof status === 'object' && status.code !== undefined) {
        // 枚舉物件
        result = status.code === 1
        console.log('解析為枚舉物件，code:', status.code)
      } else if (typeof status === 'number') {
        // 數字
        result = status === 1
        console.log('解析為數字:', status)
      } else if (typeof status === 'string') {
        // 字串
        result = status === 'ENABLE' || status === '1' || status === 1
        console.log('解析為字串:', status)
      }

      console.log('isEnabled 結果:', result)
      console.log('=== isEnabled 完成 ===')
      return result
    }
  }
}
</script>

<style lang="scss" scoped>
.line-config-container {
  .header-section {
    margin-bottom: 24px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      display: flex;
      align-items: center;

      i {
        font-size: 28px;
        margin-right: 12px;
        color: #06C755;
      }
    }

    .text-right {
      text-align: right;
    }
  }

  .channel-cards-container {
    .channel-card {
      margin-bottom: 20px;
      transition: all 0.3s ease;
      border-radius: 12px;
      overflow: hidden;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      // 主頻道樣式 - 金黃色
      &.channel-main {
        border-left: 4px solid #E6A23C;

        :deep(.el-card__header) {
          background: linear-gradient(135deg, #FDF6EC 0%, #FFF 100%);
          border-bottom: 1px solid #F5DAB1;
        }
      }

      // 副頻道樣式 - 藍色
      &.channel-sub {
        border-left: 4px solid #409EFF;

        :deep(.el-card__header) {
          background: linear-gradient(135deg, #ECF5FF 0%, #FFF 100%);
          border-bottom: 1px solid #B3D8FF;
        }
      }

      // 測試頻道樣式 - 灰色
      &.channel-test {
        border-left: 4px solid #909399;

        :deep(.el-card__header) {
          background: linear-gradient(135deg, #F4F4F5 0%, #FFF 100%);
          border-bottom: 1px solid #DCDFE6;
        }
      }

      // 空狀態卡片
      &.empty-card {
        :deep(.el-card__header) {
          background: #FAFAFA;
        }

        .empty-content {
          text-align: center;
          padding: 30px 20px;

          .empty-icon {
            font-size: 64px;
            color: #C0C4CC;
            margin-bottom: 16px;

            i {
              display: inline-block;
              animation: pulse 2s ease-in-out infinite;
            }
          }

          .empty-text {
            font-size: 16px;
            font-weight: 500;
            color: #606266;
            margin-bottom: 8px;
          }

          .empty-desc {
            font-size: 14px;
            color: #909399;
            margin-bottom: 24px;
            line-height: 1.6;
          }
        }
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .card-title {
          display: flex;
          align-items: center;
          gap: 10px;
          flex: 1;

          i {
            font-size: 20px;
          }

          .type-label {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
          }
        }

        .card-actions {
          .more-btn {
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 4px;
            transition: all 0.2s;

            i {
              font-size: 18px;
              color: #606266;
            }

            &:hover {
              background: rgba(0, 0, 0, 0.05);
            }
          }
        }
      }

      .card-content {
        padding: 4px 0;

        .channel-name-section {
          margin-bottom: 16px;
          padding-bottom: 12px;
          border-bottom: 1px solid #EBEEF5;

          h3 {
            margin: 0;
            font-size: 18px;
            font-weight: 600;
            color: #303133;
          }
        }

        .info-section {
          background: #F5F7FA;
          border-radius: 8px;
          padding: 12px;
          margin-bottom: 16px;

          .info-item {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 10px;

            &:last-child {
              margin-bottom: 0;
            }

            i {
              font-size: 14px;
              color: #909399;
            }

            .label {
              font-size: 12px;
              color: #909399;
              min-width: 80px;
            }

            .value {
              font-size: 13px;
              font-weight: 500;
              color: #303133;
              font-family: 'Monaco', 'Courier New', monospace;
            }
          }
        }

        .webhook-status {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px 12px;
          background: #F5F7FA;
          border-radius: 8px;
          margin-bottom: 16px;

          i {
            font-size: 14px;
            color: #909399;
          }

          span {
            font-size: 13px;
            color: #606266;
          }
        }

        .quick-actions {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;

          .el-button {
            flex: 1;
            min-width: 100px;
            margin-left: 0 !important;
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}
</style>
