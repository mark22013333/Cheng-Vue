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
                <el-button type="info" @click="showDebugInfo" :disabled="false" class="debug-btn">
                  <el-icon><Setting /></el-icon>
                </el-button>
              </div>

              <!-- 變焦滑桿 -->
              <div v-if="isScanning && hasZoom" class="zoom-control">
                <span class="zoom-label"><el-icon><ZoomIn /></el-icon></span>
                <el-slider
                  v-model="zoomValue"
                  :min="zoomMin"
                  :max="zoomMax"
                  :step="zoomStep"
                  @input="handleZoomChange"
                  style="width: 200px; display: inline-block; margin: 0 10px;"
                ></el-slider>
              </div>
            </div>

            <div class="manual-input">
              <el-divider content-position="center">或手動輸入</el-divider>
              <el-form :model="scanForm" :inline="true" @submit.native.prevent>
                <el-form-item>
                  <el-input
                    v-model="scanForm.scanCode"
                    placeholder="請輸入條碼/ISBN"
                    prefix-icon="Search"
                    clearable
                    @keyup.enter.native="handleManualScan">
                  </el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleManualScan">查詢</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-col>

          <!-- 右側：結果區 -->
          <el-col :xs="24" :sm="12" :md="16" :lg="18">
            <div v-if="scanResult" class="result-card">
              <el-descriptions title="物品資訊" :column="1" border>
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
                <el-descriptions-item label="分類" v-if="scanResult.categoryName">{{ scanResult.categoryName }}</el-descriptions-item>
                <el-descriptions-item label="存放位置">{{ scanResult.location }}</el-descriptions-item>

                <el-descriptions-item label="庫存數量">
                  <span class="text-success" style="font-size: 16px;">{{ scanResult.totalQuantity }}</span>
                  <span style="margin-left: 10px; color: #909399;">(可用: {{ scanResult.availableQty }})</span>
                </el-descriptions-item>
              </el-descriptions>

              <div class="action-buttons" style="margin-top: 20px; text-align: center;">
                <el-button type="primary" size="large" icon="Top" @click="handleStockIn">入庫</el-button>
                <el-button type="warning" size="large" icon="Bottom" @click="handleStockOut">出庫</el-button>
              </div>
            </div>

            <el-empty v-else description="請掃描或輸入條碼查詢物品" :image-size="200"></el-empty>

            <!-- 爬蟲任務列表 -->
            <div v-if="activeTasks.length > 0" style="margin-top: 20px;">
              <el-divider content-position="left">正在抓取書籍資訊</el-divider>
              <div v-for="task in activeTasks" :key="task.taskId" class="task-item">
                <div class="task-info">
                  <span class="task-isbn">ISBN: {{ task.isbn }}</span>
                  <el-tag size="small" :type="task.status === 'FAILED' ? 'danger' : 'primary'">{{ getStatusText(task.status) }}</el-tag>
                </div>
                <el-progress :percentage="task.progress" :status="task.status === 'FAILED' ? 'exception' : (task.status === 'COMPLETED' ? 'success' : '')"></el-progress>
                <div class="task-msg">{{ task.message }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 入庫對話框 -->
    <el-dialog title="快速入庫" v-model="stockInDialogVisible" width="90%" append-to-body>
      <el-form ref="stockInForm" :model="stockForm" :rules="stockRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="stockForm.itemName" disabled></el-input>
        </el-form-item>
        <el-form-item label="入庫數量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="1000" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="stockForm.reason" placeholder="選填"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockInDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStockIn">確認入庫</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 出庫對話框 -->
    <el-dialog title="快速出庫" v-model="stockOutDialogVisible" width="90%" append-to-body>
      <el-form ref="stockOutForm" :model="stockForm" :rules="stockRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="stockForm.itemName" disabled></el-input>
        </el-form-item>
        <el-form-item label="出庫數量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="stockForm.maxQuantity" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="stockForm.reason" placeholder="選填"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockOutDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="submitStockOut">確認出庫</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 手機端懸浮按鈕 -->
    <div 
      class="floating-action-buttons"
      :style="floatingPosition"
      @touchstart="handleDragStart"
      @touchmove.prevent="handleDragMove"
      @touchend="handleDragEnd"
    >
      <div class="fab-btn green-btn" @click.stop="openCrawledItems">
        <el-badge :value="crawledItems.length" :hidden="crawledItems.length === 0" class="fab-badge">
          <el-icon><List /></el-icon>
        </el-badge>
      </div>
      <div class="fab-btn blue-btn" @click.stop="handleScanClick">
        <el-icon><Camera /></el-icon>
      </div>
    </div>

    <!-- 爬蟲結果列表對話框 -->
    <el-dialog title="已抓取書籍" v-model="showCrawledItemsDialog" width="90%" append-to-body>
      <div v-if="crawledItems.length === 0" style="text-align: center; padding: 20px; color: #909399;">
        尚無抓取記錄
      </div>
      <div v-else class="crawled-list">
        <div v-for="item in crawledItems" :key="item.itemId" class="crawled-item">
          <div class="item-info">
            <div class="item-title">{{ item.itemName }}</div>
            <div class="item-code">{{ item.itemCode }}</div>
          </div>
          <el-button type="primary" size="small" @click="handleQuickStockIn(item)">入庫</el-button>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCrawledItemsDialog = false">關閉</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Html5Qrcode } from "html5-qrcode";
import { listManagement, stockIn, stockOut } from "@/api/inventory/management";
import { getItem } from "@/api/inventory/item";
import { crawlBookByIsbn } from "@/api/inventory/scan";
import { getImageUrl } from "@/utils/image";
import { mapState } from 'pinia';
import useUserStore from '@/store/modules/user';
import { Camera, SwitchButton, Search, Picture, Top, Bottom, View, ZoomIn, Setting, List } from '@element-plus/icons-vue';

export default {
  name: "MobileScan",
  components: {
    Camera, SwitchButton, Search, Picture, Top, Bottom, View, ZoomIn, Setting, List
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
        reason: '',
        maxQuantity: 10000 // 新增 maxQuantity 屬性
      },
      stockRules: {
        quantity: [
          { required: true, message: '請輸入數量', trigger: 'blur' }
        ]
      },

      // 爬蟲任務
      activeTasks: [],
      sseConnections: new Map(),

      // 變焦控制
      hasZoom: false,
      zoomValue: 1,
      zoomMin: 1,
      zoomMax: 5,
      zoomStep: 0.1,
      videoTrack: null,
      
      // 懸浮按鈕拖曳相關
      floatingPosition: { right: '20px', bottom: '20px' },
      isDragging: false,
      dragStartTime: 0,
      dragOffset: { x: 0, y: 0 },
      windowWidth: window.innerWidth,
      windowHeight: window.innerHeight,
      
      // 爬蟲結果列表
      crawledItems: [],
      showCrawledItemsDialog: false,
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

      // 檢查是否為安全上下文 (HTTPS 或 localhost)
      const isSecureContext = window.isSecureContext;
      if (!isSecureContext) {
        this.$alert(
          '瀏覽器限制：相機功能僅能在 HTTPS 安全連線或 localhost 環境下使用。<br>請確認您的網址是否以 <strong>https://</strong> 開頭。',
          '無法啟動相機',
          { dangerouslyUseHTMLString: true, type: 'error' }
        );
        return;
      }

      // 檢查瀏覽器是否支援 mediaDevices
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        this.$alert('您的瀏覽器不支援相機功能，請嘗試使用 Chrome、Safari 或 Firefox 最新版本。', '不支援的瀏覽器', { type: 'error' });
        return;
      }

      const config = {
        fps: 10,
        qrbox: (viewfinderWidth, viewfinderHeight) => {
          // 使用固定尺寸 300x300，適合條碼和 QR code
          let size = Math.min(300, Math.min(viewfinderWidth, viewfinderHeight) * 0.9);
          return {
            width: size,
            height: size
          };
        },
        aspectRatio: 1.0
      };

      this.html5QrCode = new Html5Qrcode("qr-reader");

      // 優先使用後置鏡頭，如果失敗則嘗試使用任何可用鏡頭
      const startCamera = (cameraIdOrConfig) => {
        return this.html5QrCode.start(
          cameraIdOrConfig,
          config,
          this.onScanSuccess,
          this.onScanFailure
        );
      };

      startCamera({ facingMode: "environment" })
        .then(() => {
          this.isScanning = true;
          this.initZoom();
        })
        .catch(err => {
          console.warn("後置鏡頭啟動失敗，嘗試使用預設鏡頭", err);
          // 嘗試不指定 facingMode (自動選擇)
          startCamera({ facingMode: "user" })
            .then(() => {
              this.isScanning = true;
              this.initZoom();
            })
            .catch(finalErr => {
              console.error("無法啟動相機", finalErr);
              let errorTitle = "無法啟動相機";
              let errorMsg = "請檢查相機權限設定或檢查是否啟用安全連線檢查 (HTTPS)。";

              if (finalErr.name === 'NotFoundError') {
                errorTitle = "找不到相機裝置";
                errorMsg = "請確認您的裝置是否有安裝相機，或相機是否被其他應用程式佔用。";
              } else if (finalErr.name === 'NotAllowedError' || finalErr.name === 'PermissionDeniedError') {
                errorTitle = "權限被拒絕";
                errorMsg = "請在瀏覽器設定中允許本網站存取相機。<br>通常在網址列左側的鎖頭圖示中可以設定。";
              } else if (finalErr.name === 'NotReadableError' || finalErr.name === 'TrackStartError') {
                errorTitle = "相機無法讀取";
                errorMsg = "相機可能正在被其他應用程式使用，請關閉其他使用相機的程式後重試。";
              }

              this.$alert(errorMsg, errorTitle, {
                dangerouslyUseHTMLString: true,
                type: 'error',
                confirmButtonText: '我知道了'
              });
            });
        });
    },

    /** 初始化變焦功能 */
    initZoom() {
      // 等待一點時間確保 video 元素已準備好
      setTimeout(() => {
        const videoElement = document.querySelector("#qr-reader video");
        if (videoElement) {
          // 檢查是否支援變焦
          const track = videoElement.srcObject.getVideoTracks()[0];
          const capabilities = track.getCapabilities();

          if (capabilities.zoom) {
            this.hasZoom = true;
            this.zoomMin = capabilities.zoom.min;
            this.zoomMax = capabilities.zoom.max;
            this.zoomStep = capabilities.zoom.step;
            this.zoomValue = capabilities.zoom.min;
            this.videoTrack = track;
          } else {
            this.hasZoom = false;
          }
        }
      }, 500);
    },

    /** 處理變焦變更 */
    handleZoomChange(val) {
      if (this.videoTrack) {
        this.videoTrack.applyConstraints({
          advanced: [{ zoom: val }]
        }).catch(err => console.error("變焦失敗", err));
      }
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
    performScan(code, silent = false) {
      const loading = this.$loading({
        lock: true,
        text: '查詢中...',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      // 1. 先嘗試在系統中查詢
      // 使用 listManagement 透過 itemCode 查詢，因為 getManagement 是透過 ID 查詢
      listManagement({ itemCode: code }).then(response => {
        if (response.rows && response.rows.length > 0) {
          // 找到了
          this.scanResult = response.rows[0];
          if (!silent) {
            this.$message.success("查詢成功");
          }
          loading.close();
        } else {
          // 2. 系統中沒有，嘗試用 ISBN 查詢 (如果是 ISBN 格式)
          // 簡單判斷是否為 ISBN (10或13位數字)
          const isIsbn = /^(97(8|9))?\d{9}(\d|X)$/.test(code);

          if (isIsbn && !silent) {
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
            if (!silent) {
              this.$message.warning("系統中找不到此物品");
            }
            // 如果是靜默模式且找不到，不清除 scanResult，以免畫面閃爍或消失
            if (!silent) {
              this.scanResult = null;
            }
            loading.close();
          }
        }
      }).catch(err => {
        console.error("查詢失敗", err);
        if (!silent) {
          this.$message.error("查詢失敗");
        }
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
            // 重新查詢物品資訊以更新庫存 (靜默模式)
            this.performScan(this.scanForm.scanCode, true);
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
            // 重新查詢物品資訊以更新庫存 (靜默模式)
            this.performScan(this.scanForm.scanCode, true);
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
              
              // 加入已爬取列表 (避免重複)
              const exists = this.crawledItems.some(item => item.itemId === response.data.itemId);
              if (!exists) {
                this.crawledItems.unshift(response.data);
              }

              this.$notify({
                title: '書籍爬取完成',
                message: `《${task.bookInfo.title}》已成功加入系統`,
                type: 'success',
                duration: 3000
              });
            }
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
        'PROCESSING': '處理中',
        'COMPLETED': '已完成',
        'FAILED': '失敗'
      };
      return map[status] || status;
    },

    /** 顯示除錯資訊 */
    showDebugInfo() {
      if (!navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) {
        this.$alert('瀏覽器不支援 enumerateDevices API', '除錯資訊');
        return;
      }

      navigator.mediaDevices.enumerateDevices()
        .then(devices => {
          const videoDevices = devices.filter(device => device.kind === 'videoinput');
          const info = videoDevices.map((d, i) => 
            `${i + 1}. Label: ${d.label || 'Unknown'}<br>ID: ${d.deviceId.substring(0, 10)}...<br>Group: ${d.groupId.substring(0, 10)}...`
          ).join('<hr>');
          
          const secureContext = window.isSecureContext ? 'Yes (HTTPS/Localhost)' : 'No (HTTP)';
          
          this.$alert(
            `<strong>Secure Context:</strong> ${secureContext}<br><br><strong>Video Devices (${videoDevices.length}):</strong><br>${info || 'No video devices found'}`, 
            '相機除錯資訊', 
            { dangerouslyUseHTMLString: true }
          );
        })
        .catch(err => {
          this.$alert(`無法取得裝置列表: ${err.name} - ${err.message}`, '除錯錯誤');
        });
    },

    // ==================== 懸浮按鈕拖曳邏輯 ====================
    
    handleDragStart(e) {
      this.dragStartTime = Date.now();
      // 不要在這裡重置 isDragging，因為 click 事件需要知道是否發生過拖曳
      const touch = e.touches[0];
      const rect = e.currentTarget.getBoundingClientRect();
      
      this.dragOffset = {
        x: touch.clientX - rect.left,
        y: touch.clientY - rect.top
      };
    },
    
    handleDragMove(e) {
      // 標記正在拖曳
      this.isDragging = true;
      
      const touch = e.touches[0];
      const maxX = window.innerWidth - 160; // 預留寬度
      const maxY = window.innerHeight - 80; // 預留高度
      
      let newLeft = touch.clientX - this.dragOffset.x;
      let newTop = touch.clientY - this.dragOffset.y;
      
      // 邊界檢查
      if (newLeft < 10) newLeft = 10;
      if (newLeft > maxX) newLeft = maxX;
      if (newTop < 10) newTop = 10;
      if (newTop > maxY) newTop = maxY;
      
      // 更新位置 (使用 fixed positioning，這裡轉換為 right/bottom 以保持一致性，或者直接改用 left/top)
      // 為了簡單，我們改用 left/top 控制
      this.floatingPosition = {
        left: newLeft + 'px',
        top: newTop + 'px',
        right: 'auto',
        bottom: 'auto'
      };
    },
    
    handleDragEnd(e) {
      // 拖曳結束，不立即重置 isDragging，讓 click handler 判斷
      // 使用 setTimeout 在 click 事件觸發後重置
      setTimeout(() => {
        this.isDragging = false;
      }, 100);
    },
    
    /** 開啟已爬取列表 */
    openCrawledItems() {
      if (this.isDragging) return; // 如果是拖曳結束，不觸發點擊
      this.showCrawledItemsDialog = true;
    },

    /** 處理藍色按鈕點擊 (開始掃描) */
    handleScanClick() {
      if (this.isDragging) return;
      
      // 確保 DOM 更新後再啟動掃描
      this.$nextTick(() => {
        // 檢查元素是否存在
        const qrElement = document.getElementById("qr-reader");
        if (!qrElement) {
          this.$message.error("掃描器容器未找到，請重新整理頁面");
          return;
        }
        
        // 滾動到掃描區
        qrElement.scrollIntoView({ behavior: "smooth", block: "center" });
        
        this.startScan();
      });
    },
    
    /** 從列表快速入庫 */
    handleQuickStockIn(item) {
      this.scanResult = item;
      this.handleStockIn();
      this.showCrawledItemsDialog = false;
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

/* 懸浮按鈕樣式 */
.floating-action-buttons {
  position: fixed;
  z-index: 2000;
  display: flex;
  gap: 15px;
  touch-action: none; /* 防止拖曳時滾動頁面 */
  padding: 10px;
  /* 預設位置，會被 inline style 覆蓋 */
  right: 20px;
  bottom: 20px;
}

.fab-btn {
  width: 60px;
  height: 60px;
  border-radius: 50% !important; /* 強制圓形 */
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  color: white;
  font-size: 24px;
  cursor: pointer;
  transition: transform 0.1s;
  overflow: hidden; /* 確保內容不溢出 */
}

.fab-btn:active {
  transform: scale(0.95);
}

.green-btn {
  background-color: #67c23a;
}

.blue-btn {
  background-color: #409eff;
}

/* 確保 Badge 在按鈕上方 */
.fab-badge :deep(.el-badge__content) {
  top: 5px;
  right: 5px;
}

/* 桌面端隱藏懸浮按鈕 */
@media (min-width: 769px) {
  .floating-action-buttons {
    display: none;
  }
}

.crawled-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.crawled-item:last-child {
  border-bottom: none;
}

.item-title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
}

.item-code {
  font-size: 12px;
  color: #909399;
}
</style>
