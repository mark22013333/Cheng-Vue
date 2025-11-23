<template>
  <div class="scan-result-button-container">
    <!-- 浮動按鈕（只在手機裝置顯示）-->
    <div
      v-if="showButton"
      class="scan-result-button"
      :style="buttonPosition"
      @touchstart="handleDragStart"
      @touchmove="handleDragMove"
      @touchend="handleDragEnd"
    >
      <el-button
        type="success"
        icon="el-icon-document"
        circle
        class="result-btn"
      ></el-button>
      <!-- 自訂徽章 -->
      <span v-if="scanResults.length > 0" class="result-badge">
        {{ scanResults.length > 99 ? '99+' : scanResults.length }}
      </span>
    </div>

    <!-- 掃描結果清單對話框 -->
    <el-dialog
      title="掃描結果"
      :visible.sync="dialogVisible"
      width="90%"
      :close-on-click-modal="false"
    >
      <div class="result-list">
        <el-card
          v-for="(result, index) in scanResults"
          :key="result.isbn"
          class="result-card"
          shadow="hover"
        >
          <div class="card-header">
            <div class="book-info">
              <img
                v-if="result.coverImagePath"
                :src="result.coverImagePath"
                class="book-cover"
                @error="handleImageError"
              />
              <div class="book-details">
                <div class="book-title">{{ result.title }}</div>
                <div class="book-meta">
                  <el-tag size="small">ISBN: {{ result.isbn }}</el-tag>
                  <el-tag size="small" type="info">{{ result.author || '未知作者' }}</el-tag>
                </div>
                <div class="book-publisher">{{ result.publisher }}</div>
              </div>
            </div>
            <div class="card-actions">
              <el-button
                type="primary"
                icon="el-icon-plus"
                size="small"
                @click="addToInventory(result, index)"
                :loading="result.adding"
              >
                +1 入庫
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                class="delete-btn"
                @click="removeResult(index)"
              ></el-button>
            </div>
          </div>
          
          <div class="card-content" v-if="result.introduction">
            <el-collapse>
              <el-collapse-item title="簡介" name="1">
                <div class="book-introduction">{{ result.introduction }}</div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="clearAll">清空全部</el-button>
        <el-button type="primary" @click="dialogVisible = false">關閉</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import DraggableMixin from '@/mixins/draggable'
import { quickStockIn } from '@/api/inventory/management'
import eventBus from '@/utils/eventBus'

export default {
  name: 'ScanResultButton',
  mixins: [DraggableMixin],
  
  data() {
    return {
      showButton: false,
      scanResults: [],
      dialogVisible: false
    }
  },
  
  mounted() {
    this.checkMobileDevice()
    this.loadButtonPosition()
    this.loadScanResults()
    
    // 監聽掃描成功事件（Vue 3 使用 eventBus）
    eventBus.on('scan-success', this.handleScanSuccess)
  },
  
  beforeUnmount() {
    eventBus.off('scan-success', this.handleScanSuccess)
  },
  
  methods: {
    /** 檢查是否為手機裝置 */
    checkMobileDevice() {
      const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
      this.showButton = isMobile
    },
    
    /** 處理點擊 */
    handleClick() {
      this.dialogVisible = true
    },
    
    /** 處理掃描成功事件 */
    handleScanSuccess(bookInfo) {
      console.log('ScanResultButton 收到 scan-success 事件:', bookInfo);
      
      // 確認必要欄位
      if (!bookInfo || !bookInfo.isbn || !bookInfo.itemId) {
        console.warn('掃描成功但資料不完整', bookInfo);
        this.$message.warning('掃描成功但資料不完整');
        return;
      }
      
      // 檢查是否已存在
      const exists = this.scanResults.some(r => r.isbn === bookInfo.isbn)
      if (exists) {
        this.$message.warning(`ISBN ${bookInfo.isbn} 已在列表中`)
        return
      }
      
      // 加入列表
      this.scanResults.unshift({
        ...bookInfo,
        adding: false
      })
      
      // 儲存到 localStorage
      this.saveScanResults()
      
      this.$notify({
        title: '掃描成功',
        message: `《${bookInfo.title}》已加入列表`,
        type: 'success',
        duration: 2000
      })
    },
    
    /** +1 入庫 */
    addToInventory(result, index) {
      result.adding = true
      
      // 呼叫入庫 API
      quickStockIn({
        itemId: result.itemId,
        quantity: 1,
        reason: 'ISBN 掃描入庫'
      }).then(response => {
        this.$message.success(`《${result.title}》已入庫 +1`)
        
        // 從列表移除
        setTimeout(() => {
          this.removeResult(index)
        }, 500)
      }).catch(error => {
        result.adding = false
        this.$message.error('入庫失敗：' + (error.msg || '未知錯誤'))
      })
    },
    
    /** 移除結果 */
    removeResult(index) {
      this.scanResults.splice(index, 1)
      this.saveScanResults()
      
      if (this.scanResults.length === 0) {
        this.dialogVisible = false
      }
    },
    
    /** 清空全部 */
    clearAll() {
      this.$confirm('確定要清空所有掃描結果嗎？', '提示', {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.scanResults = []
        this.saveScanResults()
        this.dialogVisible = false
        this.$message.success('已清空')
      }).catch(() => {})
    },
    
    /** 圖片載入失敗 */
    handleImageError(e) {
      e.target.style.display = 'none'
    },
    
    /** 儲存按鈕位置 */
    saveButtonPosition() {
      localStorage.setItem('scan-result-button-position', JSON.stringify({
        x: this.buttonX,
        y: this.buttonY
      }))
    },
    
    /** 載入按鈕位置 */
    loadButtonPosition() {
      const saved = localStorage.getItem('scan-result-button-position')
      if (saved) {
        const { x, y } = JSON.parse(saved)
        this.buttonX = x
        this.buttonY = y
      } else {
        // 預設位置：左下角（距離右側 20px，底部 100px）
        this.buttonX = 20
        this.buttonY = 100
      }
    },
    
    /** 儲存掃描結果 */
    saveScanResults() {
      localStorage.setItem('scan-results', JSON.stringify(this.scanResults))
    },
    
    /** 載入掃描結果 */
    loadScanResults() {
      const saved = localStorage.getItem('scan-results')
      if (saved) {
        this.scanResults = JSON.parse(saved)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.scan-result-button-container {
  position: relative;
  z-index: 9999;
}

.scan-result-button {
  position: fixed;
  z-index: 9999;
  cursor: move;
  user-select: none;
  -webkit-user-select: none;
  touch-action: none;
}

.result-btn {
  width: 60px !important;
  height: 60px !important;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  transition: all 0.3s ease;
}

.result-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.6);
}

/* 自訂徽章樣式（跟掃碼圓球一致）*/
.result-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #f56c6c;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.result-list {
  max-height: 60vh;
  overflow-y: auto;
}

.result-card {
  margin-bottom: 15px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  :deep(.el-card__body) {
    padding: 15px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.book-info {
  display: flex;
  flex: 1;
  gap: 12px;
}

.book-cover {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.book-details {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 6px;
  
  .el-tag {
    font-size: 12px;
  }
}

.book-publisher {
  font-size: 13px;
  color: #909399;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 10px;
}

/* 修正刪除按鈕為正圓形 */
.delete-btn {
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  min-width: 32px !important;
}

.card-content {
  margin-top: 10px;
  
  :deep(.el-collapse-item__header) {
    font-size: 13px;
    font-weight: bold;
  }
}

.book-introduction {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
  max-height: 200px;
  overflow-y: auto;
}

.dialog-footer {
  text-align: right;
}
</style>
