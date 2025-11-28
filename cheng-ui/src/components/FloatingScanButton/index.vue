<template>
  <div>
    <!-- 浮動掃描按鈕 -->
    <div
      class="floating-scan-button"
      v-if="showButton"
      :style="buttonPosition"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
      @click="handleScanClick"
    >
      <el-tooltip content="掃描功能" placement="left">
        <el-button
          type="primary"
          circle
          size="large"
          :loading="scanning"
          class="scan-btn"
          style="border-radius: 50% !important; width: 60px; height: 60px;"
        >
          <el-icon><Camera /></el-icon>
        </el-button>
      </el-tooltip>

      <!-- 任務數量標記 -->
      <span v-if="activeTasks.length > 0" class="task-badge">{{ activeTasks.length }}</span>
    </div>


    <!-- 快速掃描對話框 -->
    <el-dialog
      title="快速掃描"
      v-model="quickScanVisible"
      width="90%"
      :close-on-click-modal="false"
      :modal="false"
      :lock-scroll="false"
      custom-class="mobile-scan-dialog"
      :z-index="3000"
      append-to-body
      top="5vh"
    >
      <div class="quick-scan-container">
        <!-- 攝影機掃描區域 -->
        <div class="camera-area">
          <div id="floating-qr-reader" style="width: 100%; min-height: 300px;"></div>
          <div class="scan-tips">
            <p><el-icon><InfoFilled /></el-icon> 自動使用後置鏡頭掃描</p>
            <p><el-icon><SuccessFilled /></el-icon> 將條碼或QR碼放在掃描框內</p>
          </div>
        </div>

        <!-- 掃描控制 -->
        <div class="scan-controls">
          <el-button
            type="primary"
            @click="startQuickScan"
            :disabled="isScanning"
          >
            <el-icon><VideoCamera /></el-icon> {{ isScanning ? '掃描中...' : '開始掃描' }}
          </el-button>
          <el-button
            @click="stopQuickScan"
            :disabled="!isScanning"
          >
            <el-icon><VideoPause /></el-icon> 停止掃描
          </el-button>
          <el-button @click="switchCamera">
            <el-icon><Refresh /></el-icon> 切換攝影機
          </el-button>
        </div>

        <!-- 手動輸入 - 移到掃描結果後面 -->

        <!-- 掃描結果 -->
        <div class="scan-result" v-if="scanResult">
          <el-divider>掃描結果</el-divider>
          <el-card shadow="hover" class="result-card">
            <div class="result-info">
              <h4>
                {{ scanResult.itemName }}
                <el-tag v-if="scanResult.barcode && isValidIsbn(scanResult.barcode)"
                        type="warning" size="small" style="margin-left: 8px;">
                  <el-icon><Reading /></el-icon> 書籍
                </el-tag>
              </h4>
              <p><strong>編碼：</strong>{{ scanResult.itemCode }}</p>
              <p v-if="scanResult.barcode"><strong>條碼/ISBN：</strong>{{ scanResult.barcode }}</p>
              <p><strong>庫存：</strong><span class="stock-num">{{ scanResult.totalQuantity || 0 }}</span></p>
              <p><strong>可用：</strong><span class="available-num">{{ scanResult.availableQty || 0 }}</span></p>
            </div>
            <div class="result-actions">
              <el-button size="small" type="success" @click="handleQuickStockIn"
                         :loading="stockInLoading">
                <el-icon><Download /></el-icon> 快速入庫 +1
              </el-button>
              <el-button size="small" type="info" @click="showDetailDialog">
                <el-icon><View /></el-icon> 查看詳情
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 手動輸入 -->
        <div class="manual-input">
          <el-divider>或手動輸入</el-divider>
          <el-input
            v-model="manualCode"
            placeholder="請輸入條碼或QR碼"
            @keyup.enter.native="handleManualInput"
          >
            <template #append>
              <el-button @click="handleManualInput">
                <el-icon><Search /></el-icon> 查詢
              </el-button>
            </template>
          </el-input>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="closeQuickScan">關閉</el-button>
        <el-button type="primary" @click="goToFullScan">完整掃描頁面</el-button>
      </div>
    </el-dialog>

    <!-- 物品詳情對話框 -->
    <el-dialog
      title="物品詳情"
      v-model="detailDialogVisible"
      width="90%"
      :modal="false"
      custom-class="mobile-detail-dialog"
      append-to-body
    >
      <div v-if="itemDetail" class="detail-content">
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="物品名稱">
            <strong>{{ itemDetail.itemName }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="物品編碼">
            {{ itemDetail.itemCode }}
          </el-descriptions-item>
          <el-descriptions-item label="條碼/ISBN" v-if="itemDetail.barcode">
            {{ itemDetail.barcode }}
          </el-descriptions-item>
          <el-descriptions-item label="分類">
            {{ itemDetail.categoryName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="庫存狀態">
            <el-tag
              :type="itemDetail.stockStatus === '0' ? 'success' : itemDetail.stockStatus === '1' ? 'warning' : 'danger'">
              {{ itemDetail.stockStatusText }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="總數量">
            <span style="font-size: 18px; font-weight: bold; color: #409eff;">
              {{ itemDetail.totalQuantity || 0 }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="可用數量">
            <span style="font-size: 18px; font-weight: bold; color: #67c23a;">
              {{ itemDetail.availableQty || 0 }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="借出數量" v-if="itemDetail.borrowedQty">
            <span style="color: #e6a23c;">{{ itemDetail.borrowedQty }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="規格" v-if="itemDetail.specification">
            {{ itemDetail.specification }}
          </el-descriptions-item>
          <el-descriptions-item label="單位" v-if="itemDetail.unit">
            {{ itemDetail.unit }}
          </el-descriptions-item>
          <el-descriptions-item label="存放位置" v-if="itemDetail.location">
            {{ itemDetail.location }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" v-if="itemDetail.description">
            {{ itemDetail.description }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">關閉</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {Html5QrcodeScanner, Html5QrcodeScanType} from "html5-qrcode";
import {scanIsbn, scanCode} from "@/api/inventory/scan";
import {quickStockIn, getManagement} from "@/api/inventory/management";
import eventBus from '@/utils/eventBus';
import {createCrawlTask, getTaskStatus} from "@/api/inventory/crawlTask";
import { Camera, VideoCamera, VideoPause, Refresh, Search, InfoFilled, SuccessFilled, Reading, Download, View } from '@element-plus/icons-vue';

export default {
  name: "FloatingScanButton",
  components: {
    Camera, VideoCamera, VideoPause, Refresh, Search, InfoFilled, SuccessFilled, Reading, Download, View
  },
  data() {
    return {
      showButton: false,
      scanning: false,
      quickScanVisible: false,
      isScanning: false,
      manualCode: '',
      scanResult: null,
      html5QrCode: null,
      currentCamera: 'environment', // 'environment' 或 'user'
      stockInLoading: false, // 入庫載入狀態
      detailDialogVisible: false, // 詳情對話框顯示
      itemDetail: null, // 物品詳細資訊
      // 拖動相關
      isDragging: false,
      dragStartX: 0,
      dragStartY: 0,
      buttonX: 0,
      buttonY: 0,
      hasMoved: false, // 是否已經移動
      // 任務管理
      activeTasks: [], // 進行中的任務列表
      pollingTimer: null, // 輪詢計時器
      // 掃描防重複
      lastScannedCode: null, // 最後掃描的條碼
      lastScanTime: 0, // 最後掃描時間
      scanCooldown: 3000, // 冷卻時間 3 秒
      recentScannedCodes: new Set(), // 最近掃描過的條碼
      // 變焦控制
      hasZoom: false,
      zoomValue: 1,
      zoomMin: 1,
      zoomMax: 5,
      videoTrack: null
    };
  },
  mounted() {
    this.checkMobileDevice();
    this.checkCurrentRoute();
    this.loadButtonPosition();
    this.loadActiveTasks(); // 載入持久化的任務列表
  },
  watch: {
    '$route'() {
      this.checkCurrentRoute();
    }
  },
  methods: {
    /** 檢查是否為手機設備 */
    checkMobileDevice() {
      const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
      this.showButton = isMobile;
    },

    /** 檢查當前路由是否需要顯示按鈕 */
    checkCurrentRoute() {
      // 在掃描頁面本身不顯示浮動按鈕
      const currentPath = this.$route.path;
      const hiddenPaths = ['/inventory/scan', '/login'];
      this.showButton = this.showButton && !hiddenPaths.some(path => currentPath.includes(path));
    },

    /** 處理掃描按鈕點擊 */
    handleScanClick() {
      // 如果正在拖動或已經移動，不觸發點擊
      if (this.isDragging || this.hasMoved) {
        return;
      }
      
      console.log('FloatingScanButton: handleScanClick triggered');

      this.quickScanVisible = true;
      this.$nextTick(() => {
        this.initQuickScanner();
        // 延遲自動啟動掃描（給予初始化時間）
        setTimeout(() => {
          this.startQuickScan();
        }, 800);
      });
    },

    /** 初始化快速掃描器 */
    initQuickScanner() {
      try {
        // 確保對話框完全載入後再初始化掃描器
        setTimeout(() => {
          const config = {
            fps: 10,
            qrbox: {width: 250, height: 250},
            aspectRatio: 1.0,
            // 手機端優化設定：預設使用後置鏡頭
            videoConstraints: {
              facingMode: this.currentCamera,
              focusMode: "continuous",
              // 手機端zoom設定（提高掃描距離和清晰度）
              zoom: 1.5
            },
            // 確保攝影機權限請求不被阻擋
            rememberLastUsedCamera: true,
            showTorchButtonIfSupported: true,
            // 優化掃描設定
            experimentalFeatures: {
              useBarCodeDetectorIfSupported: true
            }
          };

          // 檢查元素是否存在
          const readerElement = document.getElementById("floating-qr-reader");
          if (!readerElement) {
            console.error('掃描器容器元素未找到');
            this.$message.error('掃描器容器未找到，請重試');
            return;
          }

          this.html5QrCode = new Html5QrcodeScanner("floating-qr-reader", config, false);
          console.log('FloatingScanButton: 快速掃描器初始化成功');
          this.$message.success('掃描器初始化成功');
        }, 300); // 延遲300ms確保DOM完全載入
      } catch (error) {
        console.error('快速掃描器初始化失敗:', error);
        this.$message.error('掃描器初始化失敗: ' + error.message);
      }
    },

    /** 開始快速掃描 */
    startQuickScan() {
      if (!this.html5QrCode) {
        this.$message.info('正在初始化掃描器...');
        this.initQuickScanner();
        // 等待初始化完成後再啟動掃描
        setTimeout(() => {
          this.startActualScan();
        }, 500);
        return;
      }

      this.startActualScan();
    },

    /** 實際啟動掃描 */
    startActualScan() {
      this.isScanning = true;

      try {
        // 請求攝影機權限（優先使用後置鏡頭）
        navigator.mediaDevices.getUserMedia({
          video: {
            facingMode: {ideal: this.currentCamera},
            focusMode: "continuous",
            zoom: 1.5,
            width: {ideal: 1920},
            height: {ideal: 1080}
          }
        }).then((stream) => {
          console.log('FloatingScanButton: Camera permission granted', stream);
          
          // 初始化變焦
          this.initZoom(stream);

          // 權限取得成功後啟動掃描
          if (this.html5QrCode) {
            this.html5QrCode.render(
              (decodedText) => {
                this.handleQuickScanSuccess(decodedText);
              },
              (error) => {
                // 只在非預期錯誤時顯示警告
                if (!error.includes('NotFoundException')) {
                  console.warn('掃描中...', error);
                }
              }
            );
            this.$message.success('掃描器已啟動，請將條碼或QR碼對準攝影機');
          }
        }).catch((error) => {
          console.error('攝影機權限被拒絕:', error);
          this.$message.error('無法存取攝影機，請檢查瀏覽器權限設定');
          this.isScanning = false;
        });
      } catch (error) {
        console.error('啟動掃描失敗:', error);
        this.$message.error('啟動掃描失敗: ' + error.message);
        this.isScanning = false;
      }
    },

    /** 停止快速掃描 */
    stopQuickScan() {
      this.isScanning = false;
      if (this.html5QrCode) {
        try {
          this.html5QrCode.clear();
        } catch (error) {
          console.error('停止掃描失敗:', error);
        }
      }
    },

    /** 切換攝影機 */
    switchCamera() {
      this.stopQuickScan();
      this.currentCamera = this.currentCamera === 'environment' ? 'user' : 'environment';
      this.$message.info(`已切換到${this.currentCamera === 'environment' ? '後置' : '前置'}攝影機`);

      // 清除舊的掃描器實例
      this.html5QrCode = null;

      setTimeout(() => {
        this.initQuickScanner();
      }, 800);
    },

    /** 快速掃描成功處理 */
    handleQuickScanSuccess(decodedText) {
      // 停止掃描器
      this.stopQuickScan();

      // 執行掃描
      this.performQuickScan(decodedText);

      // 簡短提示
      this.$message({
        message: `掃描成功: ${decodedText}`,
        type: 'success',
        duration: 1500
      });
    },

    /** 手動輸入處理 */
    handleManualInput() {
      if (!this.manualCode.trim()) {
        this.$message.warning('請輸入掃描內容');
        return;
      }
      this.performQuickScan(this.manualCode.trim());
    },

    /** 執行快速掃描 */
    performQuickScan(code) {
      // 檢查是否為 ISBN 格式
      const isIsbn = this.isValidIsbn(code);

      if (isIsbn) {
        // 使用 ISBN 掃描 API（會自動爬取書籍資訊）
        this.performIsbnQuickScan(code);
      } else {
        // 使用一般掃描 API
        this.performNormalQuickScan(code);
      }
    },

    /** 執行 ISBN 快速掃描（非同步版本） */
    performIsbnQuickScan(code) {
      console.log('=== performIsbnQuickScan 開始 ===');
      console.log('ISBN code:', code);
      
      // 建立爬取任務
      createCrawlTask(code).then(response => {
        console.log('=== createCrawlTask API 回應 ===');
        console.log('response:', JSON.stringify(response, null, 2));
        console.log('response.code:', response.code);
        console.log('response.data:', response.data);
        
        if (response.code === 200) {
          const taskId = response.data;
          console.log('任務建立成功，taskId:', taskId);

          // 立即提示加入佇列
          this.$notify({
            title: '已加入佇列',
            message: `ISBN ${code} 正在爬取中...`,
            type: 'info',
            duration: 2000
          });

          // 加入任務列表
          this.activeTasks.push({
            taskId: taskId,
            isbn: code,
            status: 'PENDING'
          });
          console.log('已加入 activeTasks，當前任務數:', this.activeTasks.length);

          // 使用 SSE 訂閱任務狀態
          this.subscribeTaskStatus(taskId);

        } else {
          console.error('建立任務失敗，response.code 不是 200:', response.code);
          this.$message.error('建立任務失敗');
        }
      }).catch(error => {
        console.error('=== createCrawlTask API 錯誤 ===');
        console.error('error:', error);
        console.error('error.msg:', error.msg);
        console.error('error.message:', error.message);
        console.error('error.code:', error.code);
        this.$message.error('建立任務失敗：' + (error.msg || error.message || '未知錯誤'));
      });
    },

    /** 初始化變焦功能 */
    initZoom(stream) {
      // 等待一點時間確保 video 元素已準備好
      setTimeout(() => {
        const videoTrack = stream.getVideoTracks()[0];
        if (!videoTrack) return;

        const capabilities = videoTrack.getCapabilities();
        console.log('Camera capabilities:', capabilities);

        if (capabilities.zoom) {
          this.hasZoom = true;
          this.zoomMin = capabilities.zoom.min;
          this.zoomMax = capabilities.zoom.max;
          this.videoTrack = videoTrack;
          
          // 設定預設縮放為 3.5 (如果支援)
          let targetZoom = 3.5;
          if (targetZoom > this.zoomMax) targetZoom = this.zoomMax;
          if (targetZoom < this.zoomMin) targetZoom = this.zoomMin;
          
          this.zoomValue = targetZoom;
          this.setZoom(targetZoom);
          console.log(`Zoom initialized to ${targetZoom}`);
        } else {
          this.hasZoom = false;
          console.log('Zoom not supported');
        }
      }, 500);
    },

    /** 設定變焦 */
    setZoom(val) {
      if (this.videoTrack) {
        this.videoTrack.applyConstraints({
          advanced: [{ zoom: val }]
        }).catch(err => console.error("變焦失敗", err));
      }
    },

    /** 執行一般快速掃描 */
    performNormalQuickScan(code) {
      scanCode({
        scanCode: code,
        scanType: '2' // 預設為QR碼
      }).then(response => {
        if (response.code === 200 && response.data) {
          this.scanResult = response.data;
          this.$message.success('掃描成功！');
        } else {
          this.$message.error('掃描失敗：未找到對應物品');
        }
      }).catch(error => {
        this.$message.error('掃描失敗：' + (error.msg || '未找到對應物品'));
      });
    },

    /** 驗證 ISBN 格式 */
    isValidIsbn(code) {
      if (!code) return false;
      // 移除可能的連字號或空格
      const cleanCode = code.replace(/[-\s]/g, '');
      // 檢查是否為10位或13位數字
      return /^\d{10}$/.test(cleanCode) || /^\d{13}$/.test(cleanCode);
    },

    /** 快速入庫 +1 */
    handleQuickStockIn() {
      if (!this.scanResult || !this.scanResult.itemId) {
        this.$message.error('物品ID不存在，無法入庫');
        return;
      }

      this.$confirm(`確定要為「${this.scanResult.itemName}」入庫 1 個嗎？`, '確認入庫', {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.stockInLoading = true;

        const stockInData = {
          itemId: this.scanResult.itemId,
          quantity: 1,
          reason: `手機端掃碼_ ${this.$store.getters.name || '登入者'}`
        };

        quickStockIn(stockInData).then(response => {
          this.stockInLoading = false;
          this.$notify({
            title: '入庫成功',
            message: `「${this.scanResult.itemName}」已入庫 1 個`,
            type: 'success',
            duration: 2000
          });

          // 更新庫存顯示（使用 $set 確保響應式更新）
          if (this.scanResult.totalQuantity !== undefined && this.scanResult.totalQuantity !== null) {
            this.$set(this.scanResult, 'totalQuantity', this.scanResult.totalQuantity + 1);
          }
          if (this.scanResult.availableQty !== undefined && this.scanResult.availableQty !== null) {
            this.$set(this.scanResult, 'availableQty', this.scanResult.availableQty + 1);
          }

          console.log('入庫後更新庫存：', {
            totalQuantity: this.scanResult.totalQuantity,
            availableQty: this.scanResult.availableQty
          });
        }).catch(error => {
          this.stockInLoading = false;

          // 檢查是否為權限錯誤
          if (error.code === 403 || error.code === 401 ||
            (error.msg && (error.msg.includes('權限') || error.msg.includes('Access Denied')))) {
            this.$alert(
              '您沒有掃碼入庫的權限，請聯絡系統管理員開通「掃描功能」權限（inventory:scan:use）。',
              '權限不足',
              {
                confirmButtonText: '確定',
                type: 'warning',
                dangerouslyUseHTMLString: false
              }
            );
          } else {
            // 其他錯誤
            const errorMsg = error.msg || error.message || '請稍後再試';
            this.$message.error('入庫失敗：' + errorMsg);
          }
        });
      }).catch(() => {
        // 取消入庫
      });
    },

    /** 顯示詳情對話框 */
    showDetailDialog() {
      if (!this.scanResult || !this.scanResult.itemId) {
        this.$message.error('物品ID不存在，無法查看詳情');
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: '載入中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      getManagement(this.scanResult.itemId).then(response => {
        loading.close();
        this.itemDetail = response.data;
        this.detailDialogVisible = true;
      }).catch(error => {
        loading.close();
        this.$message.error('取得詳情失敗：' + (error.msg || '請稍後再試'));
      });
    },

    /** 前往完整掃描頁面 */
    goToFullScan() {
      this.$router.push('/inventory/scan');
      this.closeQuickScan();
    },

    /** 關閉快速掃描 */
    closeQuickScan() {
      this.stopQuickScan();
      this.quickScanVisible = false;
      this.scanResult = null;
      this.manualCode = '';
    },

    // ==================== 拖動功能 ====================

    /** 觸控開始 */
    handleTouchStart(e) {
      this.hasMoved = false;
      this.dragStartX = e.touches[0].clientX;
      this.dragStartY = e.touches[0].clientY;

      // 顯示長按進度指示器
      // 直接進入拖動模式（移除長按等待）
      this.isDragging = true;
      this.hasMoved = false;

      // 阻止事件冒泡
      e.stopPropagation();
    },

    /** 觸控移動 */
    handleTouchMove(e) {
      if (!this.isDragging) {
        return;
      }

      this.hasMoved = true;
      e.preventDefault();
      e.stopPropagation();

      // 計算移動距離（修正 Y 軸反向問題）
      const deltaX = this.dragStartX - e.touches[0].clientX;
      const deltaY = this.dragStartY - e.touches[0].clientY;  // 修正！

      // 更新按鈕位置
      let newX = this.buttonX + deltaX;
      let newY = this.buttonY + deltaY;

      // 邊界限制（不超出螢幕）
      const screenWidth = window.innerWidth;
      const screenHeight = window.innerHeight;
      const buttonSize = 60;

      newX = Math.max(10, Math.min(newX, screenWidth - buttonSize - 10));
      newY = Math.max(10, Math.min(newY, screenHeight - buttonSize - 10));

      this.buttonX = newX;
      this.buttonY = newY;

      // 更新拖動起始點
      this.dragStartX = e.touches[0].clientX;
      this.dragStartY = e.touches[0].clientY;
    },

    /** 觸控結束 */
    handleTouchEnd(e) {
      if (this.isDragging && this.hasMoved) {
        // 儲存位置到 localStorage
        this.saveButtonPosition();

        e.stopPropagation();
        e.preventDefault();
      } else if (this.isDragging && !this.hasMoved) {
        // 沒有移動，視為點擊事件
        // 觸發掃描功能
        this.quickScanVisible = true;
      }

      // 重置拖動狀態
      this.isDragging = false;
      this.hasMoved = false;
    },

    /** 載入按鈕位置 */
    loadButtonPosition() {
      const savedPosition = localStorage.getItem('scan-button-position');
      if (savedPosition) {
        const position = JSON.parse(savedPosition);
        this.buttonX = position.x || 20;
        this.buttonY = position.y || 80;
      } else {
        // 預設位置（右下角）
        this.buttonX = 20;
        this.buttonY = 80;
      }
    },

    /** 儲存按鈕位置 */
    saveButtonPosition() {
      localStorage.setItem('scan-button-position', JSON.stringify({
        x: this.buttonX,
        y: this.buttonY
      }));
    },

    /** 儲存進行中的任務列表 */
    saveActiveTasks() {
      try {
        // 只儲存基本資訊，不儲存整個 bookInfo 物件
        const tasksToSave = this.activeTasks.map(task => ({
          taskId: task.taskId,
          isbn: task.isbn,
          status: task.status,
          createTime: task.createTime
        }));
        localStorage.setItem('active-crawl-tasks', JSON.stringify(tasksToSave));
      } catch (error) {
        console.error('儲存任務列表失敗', error);
      }
    },

    /** 載入進行中的任務列表 */
    loadActiveTasks() {
      try {
        const saved = localStorage.getItem('active-crawl-tasks');
        if (saved) {
          const tasks = JSON.parse(saved);
          // 過濾掉超過 10 分鐘的舊任務
          const tenMinutesAgo = new Date(Date.now() - 10 * 60 * 1000);
          const validTasks = tasks.filter(task => {
            const createTime = new Date(task.createTime);
            return createTime > tenMinutesAgo;
          });

          if (validTasks.length > 0) {
            this.activeTasks = validTasks;
            // 重新訂閱這些任務
            validTasks.forEach(task => {
              if (task.status === 'PENDING' || task.status === 'PROCESSING') {
                this.subscribeTaskStatus(task.taskId);
              }
            });
          } else {
            // 清除過期的資料
            localStorage.removeItem('active-crawl-tasks');
          }
        }
      } catch (error) {
        console.error('載入任務列表失敗', error);
        localStorage.removeItem('active-crawl-tasks');
      }
    },

    // ==================== SSE 訂閱 ====================

    /** 訂閱任務狀態（SSE） */
    subscribeTaskStatus(taskId) {
      const baseURL = import.meta.env.VITE_APP_BASE_API || '';
      const eventSource = new EventSource(`${baseURL}/inventory/crawlTask/subscribe/${taskId}`);

      // 監聽任務更新事件
      eventSource.addEventListener('task-update', (event) => {
        try {
          const task = JSON.parse(event.data);
          this.handleTaskUpdate(task);
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
      if (!this.sseConnections) {
        this.sseConnections = new Map();
      }
      this.sseConnections.set(taskId, eventSource);
    },

    /** 取消訂閱 */
    unsubscribeTaskStatus(taskId) {
      if (this.sseConnections && this.sseConnections.has(taskId)) {
        const eventSource = this.sseConnections.get(taskId);
        eventSource.close();
        this.sseConnections.delete(taskId);
      }
    },

    /** 處理任務更新 */
    handleTaskUpdate(task) {
      const index = this.activeTasks.findIndex(t => t.taskId === task.taskId);
      if (index === -1) return;

      // 更新任務狀態
      this.activeTasks[index] = task;
      this.saveActiveTasks(); // 儲存任務列表

      // 檢查是否完成
      if (task.status === 'COMPLETED') {
        console.log('=== 任務完成，準備觸發 scan-success 事件 ===');
        console.log('bookInfo:', JSON.stringify(task.bookInfo, null, 2));

        // 觸發全域事件，讓掃描結果按鈕接收（Vue 3 使用 eventBus）
        eventBus.emit('scan-success', task.bookInfo);
        console.log('scan-success 事件已發送');

        // 任務完成通知
        this.$notify({
          title: '書籍爬取完成',
          message: `《${task.bookInfo?.title || task.isbn}》已加入掃描結果`,
          type: 'success',
          duration: 3000
        });

        // 取消訂閱
        this.unsubscribeTaskStatus(task.taskId);

        // 從列表移除（紅色徽章數量會減少）
        console.log(`任務完成，從 activeTasks 移除，剩餘任務數: ${this.activeTasks.length - 1}`);
        this.activeTasks.splice(index, 1);
        this.saveActiveTasks(); // 儲存任務列表
      } else if (task.status === 'FAILED') {
        // 任務失敗通知
        this.$notify({
          title: '爬取失敗',
          message: `ISBN ${task.isbn} 爬取失敗：${task.errorMessage || '未知錯誤'}`,
          type: 'error',
          duration: 5000
        });

        // 取消訂閱
        this.unsubscribeTaskStatus(task.taskId);

        // 從列表移除（紅色徽章數量會減少）
        console.log(`任務失敗，從 activeTasks 移除，剩餘任務數: ${this.activeTasks.length - 1}`);
        this.activeTasks.splice(index, 1);
        this.saveActiveTasks(); // 儲存任務列表
      }
    },

    /** 顯示書籍資訊 */
    showBookInfo(bookInfo) {
      if (!bookInfo) return;

      this.$alert(`
        <div style="text-align: left;">
          <h3 style="margin-top: 0;">${bookInfo.title}</h3>
          <p><strong>作者：</strong>${bookInfo.author || '-'}</p>
          <p><strong>出版社：</strong>${bookInfo.publisher || '-'}</p>
          <p><strong>ISBN：</strong>${bookInfo.isbn || '-'}</p>
        </div>
      `, '書籍資訊', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '確定'
      });
    },
  },

  beforeDestroy() {
    this.stopQuickScan();

    // 關閉所有 SSE 連線
    if (this.sseConnections) {
      this.sseConnections.forEach((eventSource, taskId) => {
        eventSource.close();
      });
      this.sseConnections.clear();
    }
  },

  computed: {
    /** 按鈕位置樣式 */
    buttonPosition() {
      return {
        right: `${this.buttonX}px`,
        bottom: `${this.buttonY}px`,
        transition: this.isDragging ? 'none' : 'all 0.3s ease'
      };
    }
  }
};
</script>

<style scoped>
.floating-scan-button {
  position: fixed;
  z-index: 1000;
  cursor: move;
  user-select: none;
  -webkit-user-select: none;
  touch-action: none;
}

.scan-btn {
  width: 60px !important;
  height: 60px !important;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
}

.scan-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.6);
}

/* 任務數量標記 */
.task-badge {
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

/* 手機端對話框樣式 */
.mobile-scan-dialog {
  margin: 5vh auto !important;
  z-index: 3000 !important;
}

.mobile-scan-dialog .el-dialog {
  z-index: 3000 !important;
}

.mobile-scan-dialog .el-dialog__wrapper {
  z-index: 3000 !important;
}

.quick-scan-container {
  padding: 10px 0;
}

.camera-area {
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.scan-tips {
  margin-top: 10px;
  text-align: center;
  color: #666;
  font-size: 12px;
}

.scan-tips p {
  margin: 5px 0;
}

.scan-controls {
  text-align: center;
  margin-bottom: 30px;
  padding: 10px 0;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
}

.scan-controls .el-button {
  margin: 5px;
}

.manual-input {
  margin: 20px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.scan-result {
  margin-top: 20px;
}

.result-card {
  border: 2px solid #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
}

.result-info {
  margin-bottom: 15px;
}

.result-info h4 {
  margin: 0 0 10px 0;
  color: #303133;
  display: flex;
  align-items: center;
}

.result-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.stock-num {
  color: #409eff;
  font-weight: bold;
}

.available-num {
  color: #67c23a;
  font-weight: bold;
}

.result-actions {
  text-align: center;
}

.result-actions .el-button {
  margin: 0 5px;
}

#floating-qr-reader {
  border: 2px dashed #409eff;
  border-radius: 8px;
  background-color: #000;
  overflow: hidden;
}

#floating-qr-reader video {
  border-radius: 8px;
}

/* 物品詳情對話框 */
.mobile-detail-dialog {
  margin: 5vh auto !important;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-content .el-descriptions {
  margin-top: 10px;
}

.detail-content .el-descriptions-item__label {
  width: 100px;
  font-weight: bold;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .floating-scan-button {
    bottom: 70px;
    right: 15px;
  }

  .scan-btn {
    width: 50px !important;
    height: 50px !important;
    font-size: 20px;
  }

  .mobile-scan-dialog {
    margin: 2vh auto !important;
    width: 95% !important;
  }

  .mobile-detail-dialog {
    margin: 2vh auto !important;
    width: 95% !important;
  }

  #floating-qr-reader {
    min-height: 350px !important;
  }

  .camera-area {
    margin-bottom: 20px;
  }

  .result-actions .el-button {
    margin: 5px 3px !important;
    font-size: 13px;
  }
}
</style>
