<template>
  <div class="floating-scan-button" v-if="showButton" @click="handleScanClick">
    <el-tooltip content="掃描功能" placement="left">
      <el-button
        type="primary"
        icon="el-icon-camera"
        circle
        size="large"
        :loading="scanning"
        class="scan-btn"
      >
      </el-button>
    </el-tooltip>

    <!-- 快速掃描對話框 -->
    <el-dialog
      title="快速掃描"
      :visible.sync="quickScanVisible"
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
          <div id="floating-qr-reader" style="width: 100%; height: 250px;"></div>
          <div class="scan-tips">
            <p><i class="el-icon-info"></i> 將QR碼或條碼對準攝影機</p>
            <p><i class="el-icon-warning"></i> 請保持適當距離以取得最佳掃描效果</p>
          </div>
        </div>

        <!-- 掃描控制 -->
        <div class="scan-controls">
          <el-button
            type="primary"
            @click="startQuickScan"
            :disabled="isScanning"
            icon="el-icon-video-camera"
          >
            {{ isScanning ? '掃描中...' : '開始掃描' }}
          </el-button>
          <el-button
            @click="stopQuickScan"
            :disabled="!isScanning"
            icon="el-icon-video-pause"
          >
            停止掃描
          </el-button>
          <el-button @click="switchCamera" icon="el-icon-refresh">
            切換攝影機
          </el-button>
        </div>

        <!-- 手動輸入 - 移到掃描結果後面 -->

        <!-- 掃描結果 -->
        <div class="scan-result" v-if="scanResult">
          <el-divider>掃描結果</el-divider>
          <el-card shadow="never">
            <div class="result-info">
              <h4>{{ scanResult.itemName }}</h4>
              <p><strong>編碼：</strong>{{ scanResult.itemCode }}</p>
              <p><strong>庫存：</strong>{{ scanResult.stockQuantity }}</p>
              <p><strong>可用：</strong>{{ scanResult.availableQuantity }}</p>
            </div>
            <div class="result-actions">
              <el-button size="small" type="primary" @click="handleQuickBorrow">借出</el-button>
              <el-button size="small" type="success" @click="handleQuickStockIn">入庫</el-button>
              <el-button size="small" type="info" @click="goToDetail">詳情</el-button>
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
            <el-button slot="append" @click="handleManualInput" icon="el-icon-search">
              查詢
            </el-button>
          </el-input>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="closeQuickScan">關閉</el-button>
        <el-button type="primary" @click="goToFullScan">完整掃描頁面</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {Html5QrcodeScanner, Html5QrcodeScanType} from "html5-qrcode";
import {scanItem} from "@/api/inventory/item";

export default {
  name: "FloatingScanButton",
  data() {
    return {
      showButton: false,
      scanning: false,
      quickScanVisible: false,
      isScanning: false,
      manualCode: '',
      scanResult: null,
      html5QrCode: null,
      currentCamera: 'environment' // 'environment' 或 'user'
    };
  },
  mounted() {
    this.checkMobileDevice();
    this.checkCurrentRoute();
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
      this.quickScanVisible = true;
      this.$nextTick(() => {
        this.initQuickScanner();
      });
    },

    /** 初始化快速掃描器 */
    initQuickScanner() {
      try {
        // 確保對話框完全載入後再初始化掃描器
        setTimeout(() => {
          const config = {
            fps: 10,
            qrbox: {width: 380, height: 280},
            aspectRatio: 1.0,
            // 手機端優化設定
            videoConstraints: {
              facingMode: this.currentCamera,
              focusMode: "continuous",
              advanced: [
                {zoom: 3}, // 先嘗試設到最大或中間值，看裝置支援度
                {focusMode: "continuous"},
                {focusDistance: {min: 0.1, max: 10}},
                {zoom: {min: 1, max: 3}}
              ]
            },
            // 確保攝影機權限請求不被阻擋
            rememberLastUsedCamera: true,
            showTorchButtonIfSupported: true
          };

          // 檢查元素是否存在
          const readerElement = document.getElementById("floating-qr-reader");
          if (!readerElement) {
            console.error('掃描器容器元素未找到');
            this.$message.error('掃描器容器未找到，請重試');
            return;
          }

          this.html5QrCode = new Html5QrcodeScanner("floating-qr-reader", config, false);
          console.log('快速掃描器初始化成功');
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
        // 請求攝影機權限
        navigator.mediaDevices.getUserMedia({
          video: {
            facingMode: this.currentCamera,
            focusMode: "continuous"
          }
        }).then(() => {
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
      this.stopQuickScan();
      this.performQuickScan(decodedText);
      this.$message.success(`掃描成功: ${decodedText}`);
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
    performQuickScan(scanCode) {
      const scanData = {
        scanCode: scanCode,
        scanType: '2' // 預設為QR碼
      };

      scanItem(scanData).then(response => {
        this.scanResult = response.data;
        this.$message.success('掃描成功！');
      }).catch(error => {
        this.$message.error('掃描失敗：' + (error.msg || '未找到對應物品'));
      });
    },

    /** 快速借出 */
    handleQuickBorrow() {
      if (this.scanResult) {
        this.$router.push({
          path: '/inventory/borrow/add',
          query: {itemId: this.scanResult.itemId}
        });
        this.closeQuickScan();
      }
    },

    /** 快速入庫 */
    handleQuickStockIn() {
      if (this.scanResult) {
        this.$router.push({
          path: '/inventory/stock/in',
          query: {itemId: this.scanResult.itemId}
        });
        this.closeQuickScan();
      }
    },

    /** 查看詳情 */
    goToDetail() {
      if (this.scanResult) {
        this.$router.push('/inventory/item/detail/' + this.scanResult.itemId);
        this.closeQuickScan();
      }
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
    }
  },

  beforeDestroy() {
    this.stopQuickScan();
  }
};
</script>

<style scoped>
.floating-scan-button {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
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

.result-info {
  margin-bottom: 15px;
}

.result-info h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.result-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.result-actions {
  text-align: center;
}

.result-actions .el-button {
  margin: 0 5px;
}

#floating-qr-reader {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
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
}
</style>
