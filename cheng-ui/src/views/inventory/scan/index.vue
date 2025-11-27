<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>行動掃碼</span>
      </div>
      
      <div class="scan-container">
        <el-row :gutter="20">
          <!-- 左側：掃描區 -->
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <div class="camera-scan">
              <div id="qr-reader" style="width: 100%; height: 300px;"></div>
              <div class="scan-controls">
                <el-button type="primary" icon="Camera" @click="startScan" :disabled="isScanning">開始掃描</el-button>
                <el-button type="danger" icon="SwitchButton" @click="stopScan" :disabled="!isScanning">停止</el-button>
              </div>
            </div>
            
            <div class="manual-input">
              <el-divider content-position="center">或手動輸入</el-divider>
              <el-form :model="scanForm" :inline="true" @submit.native.prevent>
                <el-form-item>
                  <el-input v-model="scanForm.scanCode" placeholder="請輸入條碼/ISBN" clearable @keyup.enter.native="handleManualScan">
                    <template #append>
                      <el-button icon="Search" @click="handleManualScan"></el-button>
                    </template>
                  </el-input>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
          
          <!-- 右側：結果區 -->
          <el-col :xs="24" :sm="12" :md="16" :lg="18">
            <div v-if="scanResult" class="result-card">
              <el-descriptions title="掃描結果" :column="1" border>
                <template #extra>
                  <el-tag :type="scanResult.stockStatus === '0' ? 'success' : 'danger'">
                    {{ scanResult.stockStatusText || (scanResult.stockStatus === '0' ? '正常' : '缺貨') }}
                  </el-tag>
                </template>
                
                <el-descriptions-item label="圖片">
                  <div class="item-image">
                    <el-image 
                      style="width: 150px; height: 150px"
                      :src="getImageUrl(scanResult.imageUrl)" 
                      :preview-src-list="[getImageUrl(scanResult.imageUrl)]"
                      fit="contain">
                      <template #error>
                        <div class="image-slot">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                  </div>
                </el-descriptions-item>
                
                <el-descriptions-item label="物品名稱">
                  <span style="font-size: 18px; font-weight: bold;">{{ scanResult.itemName }}</span>
                </el-descriptions-item>
                
                <el-descriptions-item label="物品編碼">{{ scanResult.itemCode }}</el-descriptions-item>
                <el-descriptions-item label="條碼/ISBN">{{ scanResult.barcode }}</el-descriptions-item>
                <el-descriptions-item label="作者" v-if="scanResult.author">{{ scanResult.author }}</el-descriptions-item>
                <el-descriptions-item label="分類">{{ scanResult.categoryName }}</el-descriptions-item>
                <el-descriptions-item label="存放位置">{{ scanResult.location }}</el-descriptions-item>
                
                <el-descriptions-item label="庫存數量">
                  <span class="text-success" style="font-size: 16px;">{{ scanResult.totalQuantity }}</span>
                  <span style="margin-left: 10px; color: #909399;">(可用: {{ scanResult.availableQty }})</span>
                </el-descriptions-item>
              </el-descriptions>
              
              <div class="action-buttons" style="margin-top: 20px; text-align: center;">
                <el-button type="primary" size="large" icon="Top" @click="handleStockIn">入庫</el-button>
                <el-button type="warning" size="large" icon="Bottom" @click="handleStockOut">出庫</el-button>
                <el-button type="info" size="large" icon="View" @click="handleViewDetail">詳情</el-button>
              </div>
            </div>
            
            <el-empty v-else description="請掃描或輸入條碼查詢物品" :image-size="200"></el-empty>
            
            <!-- 爬蟲任務列表 -->
            <div v-if="activeTasks.length > 0" style="margin-top: 20px;">
              <el-divider content-position="left">正在抓取書籍資訊</el-divider>
              <el-card v-for="task in activeTasks" :key="task.taskId" style="margin-bottom: 10px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <div>
                    <span style="font-weight: bold;">ISBN: {{ task.isbn }}</span>
                    <el-tag size="small" style="margin-left: 10px;">{{ getStatusText(task.status) }}</el-tag>
                  </div>
                  <el-progress type="circle" :percentage="task.progress" :width="40"></el-progress>
                </div>
                <div style="margin-top: 5px; font-size: 12px; color: #666;">
                  {{ task.message }}
                </div>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 入庫對話框 -->
    <el-dialog title="快速入庫" v-model="stockInDialogVisible" width="90%" append-to-body>
      <el-form ref="stockInForm" :model="stockForm" :rules="stockRules" label-width="80px">
        <el-form-item label="物品">
          <span>{{ stockForm.itemName }}</span>
        </el-form-item>
        <el-form-item label="數量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="10000" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="備註" prop="reason">
          <el-input v-model="stockForm.reason" placeholder="入庫原因(選填)"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockInDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStockIn">確定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 出庫對話框 -->
    <el-dialog title="快速出庫" v-model="stockOutDialogVisible" width="90%" append-to-body>
      <el-form ref="stockOutForm" :model="stockForm" :rules="stockRules" label-width="80px">
        <el-form-item label="物品">
          <span>{{ stockForm.itemName }}</span>
        </el-form-item>
        <el-form-item label="數量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="scanResult ? scanResult.availableQty : 10000" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="備註" prop="reason">
          <el-input v-model="stockForm.reason" placeholder="出庫原因(選填)"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockOutDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStockOut">確定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Html5Qrcode } from "html5-qrcode";
import { getManagement, stockIn, stockOut } from "@/api/inventory/management";
import { getItem } from "@/api/inventory/item";
import { crawlBookByIsbn } from "@/api/inventory/scan";
import { getImageUrl } from "@/utils/image";
import { mapState } from 'pinia';
import useUserStore from '@/store/modules/user';
import { Camera, SwitchButton, Search, Picture, Top, Bottom, View } from '@element-plus/icons-vue';

export default {
  name: "MobileScan",
  components: {
    Camera, SwitchButton, Search, Picture, Top, Bottom, View
  },
  data() {
    return {
      html5QrCode: null,
      isScanning: false,
      scanForm: {
        scanCode: '',
        scanType: 'BARCODE' // BARCODE, QRCODE
      },
      scanResult: null,
      
      // 入庫/出庫表單
      stockInDialogVisible: false,
      stockOutDialogVisible: false,
      stockForm: {
        itemId: null,
        itemName: '',
        quantity: 1,
        reason: ''
      },
      stockRules: {
        quantity: [
          { required: true, message: '請輸入數量', trigger: 'blur' }
        ]
      },
      
      // 爬蟲任務
      activeTasks: [],
      sseConnections: new Map()
    };
  },
  computed: {
    ...mapState(useUserStore, ['id', 'name', 'nickName']),
  },
  mounted() {
    // 檢查相機權限
    Html5Qrcode.getCameras().then(devices => {
      if (devices && devices.length) {
        console.log("Found cameras", devices);
      }
    }).catch(err => {
      console.error("Error getting cameras", err);
    });
  },
  beforeUnmount() {
    this.stopScan();
    // 關閉所有 SSE 連線
    this.sseConnections.forEach((source) => {
      source.close();
    });
    this.sseConnections.clear();
  },
  methods: {
    getImageUrl,
    
    /** 開始掃描 */
    startScan() {
      if (this.isScanning) return;
      
      const config = { fps: 10, qrbox: { width: 250, height: 250 } };
      
      this.html5QrCode = new Html5Qrcode("qr-reader");
      
      this.html5QrCode.start(
        { facingMode: "environment" }, // 使用後置鏡頭
        config,
        this.onScanSuccess,
        this.onScanFailure
      ).then(() => {
        this.isScanning = true;
      }).catch(err => {
        console.error("Error starting scanner", err);
        this.$message.error("無法啟動相機，請檢查權限設定");
      });
    },
    
    /** 停止掃描 */
    stopScan() {
      if (this.html5QrCode && this.isScanning) {
        this.html5QrCode.stop().then(() => {
          this.html5QrCode.clear();
          this.isScanning = false;
        }).catch(err => {
          console.error("Failed to stop scanner", err);
        });
      }
    },
    
    /** 掃描成功回調 */
    onScanSuccess(decodedText, decodedResult) {
      console.log(`Scan result: ${decodedText}`, decodedResult);
      // 暫停掃描，避免重複觸發
      this.stopScan();
      
      this.scanForm.scanCode = decodedText;
      this.handleManualScan();
    },
    
    /** 掃描失敗回調 */
    onScanFailure(error) {
      // console.warn(`Code scan error = ${error}`);
    },
    
    /** 執行查詢 */
    handleManualScan() {
      const code = this.scanForm.scanCode.trim();
      if (!code) {
        this.$message.warning("請輸入條碼或ISBN");
        return;
      }
      
      this.performScan(code);
    },
    
    /** 執行掃描邏輯 */
    performScan(code) {
      const loading = this.$loading({
        lock: true,
        text: '查詢中...',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      // 1. 先嘗試在系統中查詢
      getManagement({ itemCode: code }).then(response => {
        if (response.rows && response.rows.length > 0) {
          // 找到了
          this.scanResult = response.rows[0];
          this.$message.success("查詢成功");
          loading.close();
        } else {
          // 2. 系統中沒有，嘗試用 ISBN 查詢 (如果是 ISBN 格式)
          // 簡單判斷是否為 ISBN (10或13位數字)
          const isIsbn = /^(97(8|9))?\d{9}(\d|X)$/.test(code);
          
          if (isIsbn) {
            this.$confirm(`系統中找不到此物品，是否嘗試從網路抓取書籍資訊 (ISBN: ${code})?`, '提示', {
              confirmButtonText: '抓取',
              cancelButtonText: '取消',
              type: 'info'
            }).then(() => {
              this.startCrawlTask(code);
              loading.close();
            }).catch(() => {
              loading.close();
            });
          } else {
            this.$message.warning("系統中找不到此物品");
            this.scanResult = null;
            loading.close();
          }
        }
      }).catch(err => {
        console.error(err);
        this.$message.error("查詢失敗");
        loading.close();
      });
    },
    
    /** 啟動爬蟲任務 */
    startCrawlTask(isbn) {
      crawlBookByIsbn(isbn).then(response => {
        const taskId = response.data; // 假設返回 taskId
        this.$message.success("已啟動書籍抓取任務");
        
        // 加入任務列表
        this.activeTasks.push({
          taskId: taskId,
          isbn: isbn,
          status: 'RUNNING',
          progress: 0,
          message: '正在啟動...'
        });
        
        // 訂閱 SSE
        this.subscribeTaskStatus(taskId, isbn, 'ISBN');
      }).catch(err => {
        this.$message.error("啟動抓取任務失敗: " + err.msg);
      });
    },
    
    /** 入庫操作 */
    handleStockIn() {
      if (!this.scanResult) return;

      this.stockForm.itemId = this.scanResult.itemId;
      this.stockForm.itemName = this.scanResult.itemName;
      this.stockForm.quantity = 1;
      this.stockForm.reason = '';
      this.stockInDialogVisible = true;
    },

    /** 提交入庫 */
    submitStockIn() {
      this.$refs.stockInForm.validate(valid => {
        if (valid) {
          const stockData = {
            itemId: this.stockForm.itemId,
            quantity: this.stockForm.quantity,
            reason: this.stockForm.reason
          };

          stockIn(stockData).then(response => {
            this.$message.success('入庫成功！');
            this.stockInDialogVisible = false;
            // 重新查詢物品資訊以更新庫存
            this.performScan(this.scanForm.scanCode);
          });
        }
      });
    },

    /** 出庫操作 */
    handleStockOut() {
      if (!this.scanResult) return;

      this.stockForm.itemId = this.scanResult.itemId;
      this.stockForm.itemName = this.scanResult.itemName;
      this.stockForm.quantity = 1;
      this.stockForm.reason = '';
      this.stockOutDialogVisible = true;
    },

    /** 提交出庫 */
    submitStockOut() {
      this.$refs.stockOutForm.validate(valid => {
        if (valid) {
          const stockData = {
            itemId: this.stockForm.itemId,
            quantity: this.stockForm.quantity,
            reason: this.stockForm.reason
          };

          stockOut(stockData).then(response => {
            this.$message.success('出庫成功！');
            this.stockOutDialogVisible = false;
            // 重新查詢物品資訊以更新庫存
            this.performScan(this.scanForm.scanCode);
          });
        }
      });
    },

    /** 查看詳情 */
    handleViewDetail() {
      if (this.scanResult) {
        // 導航到詳情頁，這裡假設有一個詳情路由，或者彈出詳情對話框
        // 由於是行動端，彈出對話框可能更好，或者跳轉
        // 這裡暫時跳轉到管理頁面並帶上查詢參數
        this.$router.push({
          path: '/inventory/management',
          query: { itemCode: this.scanResult.itemCode }
        });
      }
    },
    
    // ==================== SSE 訂閱 ====================
    
    /** 訂閱任務狀態（SSE） */
    subscribeTaskStatus(taskId, isbn, scanType) {
      const baseURL = import.meta.env.VITE_APP_BASE_API || '';
      const eventSource = new EventSource(`${baseURL}/inventory/crawlTask/subscribe/${taskId}`);
      
      console.log('開始訂閱任務狀態, taskId:', taskId);
      
      // 監聽任務更新事件
      eventSource.addEventListener('task-update', (event) => {
        try {
          const task = JSON.parse(event.data);
          console.log('收到任務更新:', task);
          this.handleTaskUpdate(task, isbn, scanType);
        } catch (error) {
          console.error('解析 SSE 資料失敗', error);
        }
      });
      
      // 監聽錯誤事件
      eventSource.addEventListener('error', (event) => {
        console.error('SSE 連線錯誤', event);
        eventSource.close();
      });
      
      // 儲存連線，用於後續關閉
      this.sseConnections.set(taskId, eventSource);
    },
    
    /** 取消訂閱 */
    unsubscribeTaskStatus(taskId) {
      if (this.sseConnections && this.sseConnections.has(taskId)) {
        const eventSource = this.sseConnections.get(taskId);
        eventSource.close();
        this.sseConnections.delete(taskId);
        console.log('取消訂閱任務:', taskId);
      }
    },
    
    /** 處理任務更新 */
    handleTaskUpdate(task, isbn, scanType) {
      const index = this.activeTasks.findIndex(t => t.taskId === task.taskId);
      if (index === -1) return;
      
      // 更新任務狀態
      // Vue 3 不需要 $set
      this.activeTasks[index] = task;
      
      // 檢查是否完成
      if (task.status === 'COMPLETED') {
        console.log('任務完成:', task.bookInfo);
        
        // 顯示掃描結果
        if (task.bookInfo && task.bookInfo.itemId) {
          // 使用 itemId 取得完整物品資訊
          getItem(task.bookInfo.itemId).then(response => {
            if (response.code === 200 && response.data) {
              this.scanResult = response.data;
              
              this.$notify({
                title: '書籍爬取完成',
                message: `《${task.bookInfo.title}》已成功加入系統`,
                type: 'success',
                duration: 3000
              });
            }
          }).catch(error => {
            console.error('取得物品資訊失敗', error);
          });
        }
        
        // 取消訂閱
        this.unsubscribeTaskStatus(task.taskId);
        
        // 從列表移除
        this.activeTasks.splice(index, 1);
        
      } else if (task.status === 'FAILED') {
        // 任務失敗通知
        this.$notify({
          title: '爬取失敗',
          message: `ISBN ${isbn} 爬取失敗：${task.errorMessage || '未知錯誤'}`,
          type: 'error',
          duration: 5000
        });
        
        // 取消訂閱
        this.unsubscribeTaskStatus(task.taskId);
        
        // 從列表移除
        this.activeTasks.splice(index, 1);
      }
    },
    
    getStatusText(status) {
      const map = {
        'PENDING': '等待中',
        'RUNNING': '進行中',
        'COMPLETED': '完成',
        'FAILED': '失敗'
      };
      return map[status] || status;
    }
  }
};
</script>

<style scoped>
.scan-container {
  margin-bottom: 20px;
}

.camera-scan {
  text-align: center;
}

.scan-controls {
  margin-top: 15px;
}

.scan-controls .el-button {
  margin: 0 5px;
}

.manual-input {
  padding: 20px 0;
}

.result-card {
  margin-top: 20px;
  border: 2px solid #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
  padding: 15px;
  border-radius: 8px;
}

.item-image {
  text-align: center;
}

.action-buttons .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

.text-success {
  color: #67c23a;
  font-weight: bold;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

#qr-reader {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

/* 手機端響應式設計 */
@media (max-width: 768px) {
  .scan-container .el-col {
    margin-bottom: 20px;
  }

  .scan-container .el-row {
    flex-direction: column;
  }

  #qr-reader {
    height: 250px !important;
  }

  .camera-scan {
    padding: 10px;
  }

  .scan-controls .el-button {
    margin: 5px 2px;
    font-size: 12px;
  }

  .manual-input .el-form-item {
    margin-bottom: 15px;
  }

  .action-buttons .el-button {
    margin: 5px 2px;
    font-size: 12px;
  }
}
</style>
