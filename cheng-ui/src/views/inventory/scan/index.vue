<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>掃描功能</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleBack">返回</el-button>
      </div>

      <!-- 掃描區域 -->
      <div class="scan-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header">
                <i class="el-icon-camera"></i>
                <span>攝影機掃描</span>
              </div>
              <div class="camera-scan">
                <div id="qr-reader" style="width: 100%; height: 300px;"></div>
                <div class="scan-controls">
                  <el-button type="primary" @click="startScan" :disabled="isScanning">
                    <i class="el-icon-video-camera"></i>
                    {{ isScanning ? '掃描中...' : '開始掃描' }}
                  </el-button>
                  <el-button @click="stopScan" :disabled="!isScanning">
                    <i class="el-icon-video-pause"></i>
                    停止掃描
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header">
                <i class="el-icon-edit"></i>
                <span>手動輸入</span>
              </div>
              <div class="manual-input">
                <el-form :model="scanForm" ref="scanForm" label-width="100px">
                  <el-form-item label="掃描類型">
                    <el-radio-group v-model="scanForm.scanType">
                      <el-radio label="1">條碼</el-radio>
                      <el-radio label="2">QR碼</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="掃描內容">
                    <el-input
                      v-model="scanForm.scanCode"
                      placeholder="請輸入條碼或QR碼內容"
                      @keyup.enter.native="handleManualScan"
                    >
                      <el-button slot="append" @click="handleManualScan">
                        <i class="el-icon-search"></i>
                        查詢
                      </el-button>
                    </el-input>
                  </el-form-item>
                </el-form>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 掃描結果 -->
      <div class="scan-result" v-if="scanResult">
        <el-divider content-position="left">
          <i class="el-icon-tickets"></i> 掃描結果
        </el-divider>
        <el-card shadow="hover" class="result-card">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="item-image">
                <el-image
                  :src="getImageUrl(scanResult.imageUrl) || require('@/assets/images/profile.jpg')"
                  fit="cover"
                  style="width: 180px; height: 180px; border-radius: 8px;"
                >
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
              </div>
            </el-col>
            <el-col :span="18">
              <div class="item-info">
                <h3>
                  {{ scanResult.itemName }}
                  <el-tag v-if="scanResult.barcode && isValidIsbn(scanResult.barcode)"
                          type="warning" size="small" style="margin-left: 10px;">
                    <i class="el-icon-reading"></i> 書籍
                  </el-tag>
                </h3>
                <el-descriptions :column="2" border size="medium">
                  <el-descriptions-item label="物品編碼">
                    <el-tag size="small">{{ scanResult.itemCode }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="分類">{{ scanResult.categoryName || '未分類' }}</el-descriptions-item>
                  <el-descriptions-item label="條碼/ISBN">{{ scanResult.barcode || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="規格">{{ scanResult.specification || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="單位">{{ scanResult.unit || '個' }}</el-descriptions-item>
                  <el-descriptions-item label="品牌">{{ scanResult.brand || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="型號">{{ scanResult.model || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="庫存數量">
                    <span :class="{'text-danger': scanResult.stockQuantity <= scanResult.minStock}">
                      <strong>{{ scanResult.stockQuantity || 0 }}</strong>
                    </span>
                  </el-descriptions-item>
                  <el-descriptions-item label="可用數量">
                    <span class="text-success">
                      <strong>{{ scanResult.availableQuantity || 0 }}</strong>
                    </span>
                  </el-descriptions-item>
                  <el-descriptions-item label="存放位置">
                    <el-tag type="info" size="small">{{ scanResult.location || '未設定' }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="供應商">{{ scanResult.supplier || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="說明" :span="2">{{ scanResult.description || scanResult.remark || '無' }}</el-descriptions-item>
                </el-descriptions>

                <div class="action-buttons" style="margin-top: 20px;">
                  <el-button type="primary" icon="el-icon-sold-out" @click="handleBorrow">借出物品</el-button>
                  <el-button type="success" icon="el-icon-download" @click="handleStockIn">入庫</el-button>
                  <el-button type="warning" icon="el-icon-upload2" @click="handleStockOut">出庫</el-button>
                  <el-button type="info" icon="el-icon-view" @click="handleViewDetail">查看詳情</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </div>

      <!-- 掃描歷史 -->
      <div class="scan-history" v-if="scanHistory.length > 0">
        <el-divider content-position="left">掃描歷史</el-divider>
        <el-table :data="scanHistory" style="width: 100%" stripe>
          <el-table-column prop="scanTime" label="掃描時間" width="180">
            <template slot-scope="scope">
              {{ parseTime(scope.row.scanTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
            </template>
          </el-table-column>
          <el-table-column prop="codeType" label="類型" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.codeType === 'ISBN' ? 'warning' : 'info'" size="small">
                {{ scope.row.codeType || '一般' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scanType" label="掃描方式" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.scanType === '1' ? 'primary' : 'success'" size="small">
                {{ scope.row.scanType === '1' ? '條碼' : 'QR碼' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scanCode" label="掃描內容" width="180"/>
          <el-table-column prop="itemName" label="物品名稱" show-overflow-tooltip/>
          <el-table-column prop="scanResult" label="結果" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.scanResult === '0' ? 'success' : 'danger'" size="small">
                {{ scope.row.scanResult === '0' ? '成功' : '失敗' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="handleRescan(scope.row)">
                <i class="el-icon-refresh"></i> 重新掃描
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 借出對話框 -->
    <el-dialog title="借出物品" :visible.sync="borrowDialogVisible" width="500px">
      <el-form :model="borrowForm" ref="borrowForm" :rules="borrowRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="borrowForm.itemName" disabled/>
        </el-form-item>
        <el-form-item label="借出數量" prop="quantity">
          <el-input-number v-model="borrowForm.quantity" :min="1" :max="scanResult && scanResult.availableQuantity"/>
        </el-form-item>
        <el-form-item label="借用目的" prop="purpose">
          <el-input v-model="borrowForm.purpose" type="textarea" placeholder="請輸入借用目的"/>
        </el-form-item>
        <el-form-item label="預計歸還">
          <el-date-picker
            v-model="borrowForm.expectedReturn"
            type="datetime"
            placeholder="選擇預計歸還時間"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="borrowDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitBorrow">確 定</el-button>
      </div>
    </el-dialog>

    <!-- 入庫對話框 -->
    <el-dialog title="入庫操作" :visible.sync="stockInDialogVisible" width="400px">
      <el-form :model="stockForm" ref="stockInForm" :rules="stockRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="stockForm.itemName" disabled/>
        </el-form-item>
        <el-form-item label="入庫數量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1"/>
        </el-form-item>
        <el-form-item label="入庫原因" prop="reason">
          <el-input v-model="stockForm.reason" placeholder="請輸入入庫原因"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="stockInDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitStockIn">確 定</el-button>
      </div>
    </el-dialog>

    <!-- 出庫對話框 -->
    <el-dialog title="出庫操作" :visible.sync="stockOutDialogVisible" width="400px">
      <el-form :model="stockForm" ref="stockOutForm" :rules="stockRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="stockForm.itemName" disabled/>
        </el-form-item>
        <el-form-item label="出庫數量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="scanResult && scanResult.availableQuantity"/>
        </el-form-item>
        <el-form-item label="出庫原因" prop="reason">
          <el-input v-model="stockForm.reason" placeholder="請輸入出庫原因"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="stockOutDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitStockOut">確 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {Html5QrcodeScanner, Html5QrcodeScanType} from "html5-qrcode";
import {scanIsbn, scanCode} from "@/api/inventory/scan";
import {getItem} from "@/api/inventory/item";
import {borrowItem} from "@/api/inventory/borrow";
import {stockIn, stockOut} from "@/api/inventory/stock";
import {parseTime} from "@/utils";
import {getImageUrl} from "@/utils/image";

export default {
  name: "Scan",
  data() {
    return {
      // 掃描狀態
      isScanning: false,
      // 掃描表單
      scanForm: {
        scanType: '1', // 1條碼 2QR碼
        scanCode: ''
      },
      // 掃描結果
      scanResult: null,
      // 掃描歷史
      scanHistory: [],
      // 借出對話框
      borrowDialogVisible: false,
      borrowForm: {
        itemId: null,
        itemName: '',
        quantity: 1,
        purpose: '',
        expectedReturn: null
      },
      borrowRules: {
        quantity: [
          {required: true, message: "借出數量不能為空", trigger: "blur"}
        ],
        purpose: [
          {required: true, message: "借用目的不能為空", trigger: "blur"}
        ]
      },
      // 庫存操作對話框
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
          {required: true, message: "數量不能為空", trigger: "blur"}
        ],
        reason: [
          {required: true, message: "原因不能為空", trigger: "blur"}
        ]
      },
      // QR掃描器
      html5QrCode: null
    };
  },
  mounted() {
    this.initQrScanner();
    this.loadScanHistory();
  },
  beforeDestroy() {
    this.stopScan();
  },
  methods: {
    parseTime,
    getImageUrl,

    /** 初始化QR掃描器 */
    initQrScanner() {
      try {
        // 檢測是否為手機端
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

        const config = {
          fps: 10,
          qrbox: isMobile ? {width: 200, height: 200} : {width: 250, height: 250},
          aspectRatio: 1.0,
          // 手機端優化設定
          ...(isMobile && {
            // 支援近距離掃描
            supportedScanTypes: [Html5QrcodeScanType.SCAN_TYPE_CAMERA],
            // 優化手機端攝影機設定
            videoConstraints: {
              facingMode: "environment", // 使用後置攝影機
              focusMode: "continuous", // 連續對焦
              advanced: [
                {focusMode: "continuous"},
                {focusDistance: {min: 0.1, max: 10}}, // 支援近距離對焦
                {zoom: {min: 1, max: 3}} // 支援縮放
              ]
            }
          })
        };

        this.html5QrCode = new Html5QrcodeScanner("qr-reader", config, false);
        console.log('QR掃描器初始化成功');
      } catch (error) {
        console.error('QR掃描器初始化失敗:', error);
        this.$message.error('QR掃描器初始化失敗，請檢查攝影機權限');
      }
    },

    /** 開始掃描 */
    startScan() {
      if (!this.html5QrCode) {
        this.initQrScanner();
      }

      this.isScanning = true;

      try {
        this.html5QrCode.render(
          (decodedText, decodedResult) => {
            // 掃描成功Callback
            this.handleScanSuccess(decodedText);
          },
          (error) => {
            // 掃描失敗Callback（可以忽略，因為會持續掃描）
            console.warn('掃描中...', error);
          }
        );
      } catch (error) {
        console.error('啟動掃描失敗:', error);
        this.$message.error('啟動掃描失敗，請檢查攝影機權限');
        this.isScanning = false;
      }
      this.$message.success('掃描功能已啟動，請將條碼或QR碼對準攝影機');
    },

    /** 停止掃描 */
    stopScan() {
      this.isScanning = false;
      if (this.html5QrCode) {
        try {
          this.html5QrCode.clear();
          this.$message.info('掃描已停止');
        } catch (error) {
          console.error('停止掃描失敗:', error);
        }
      }
    },

    /** 掃描成功處理 */
    handleScanSuccess(decodedText) {
      this.scanForm.scanCode = decodedText;
      this.stopScan();
      this.performScan(decodedText, this.scanForm.scanType);
      this.$message.success(`掃描成功: ${decodedText}`);
    },

    /** 手動掃描 */
    handleManualScan() {
      if (!this.scanForm.scanCode) {
        this.$message.warning('請輸入掃描內容');
        return;
      }
      this.performScan(this.scanForm.scanCode, this.scanForm.scanType);
    },

    /** 執行掃描 */
    performScan(scanCode, scanType) {
      // 檢查是否為 ISBN 格式（10位或13位數字）
      const isIsbn = this.isValidIsbn(scanCode);

      if (isIsbn) {
        // 使用 ISBN 掃描 API（會自動爬取書籍資訊）
        this.performIsbnScan(scanCode, scanType);
      } else {
        // 使用一般掃描 API
        this.performNormalScan(scanCode, scanType);
      }
    },

    /** 執行 ISBN 掃描 */
    performIsbnScan(code, scanType) {
      const loading = this.$loading({
        lock: true,
        text: '正在爬取書籍資訊...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      scanIsbn({ isbn: code }).then(response => {
        loading.close();

        if (response.code === 200 && response.data && response.data.item) {
          this.scanResult = response.data.item;
          this.addToHistory(code, scanType, '0', response.data.item.itemName, 'ISBN');

          const message = response.data.message || '書籍掃描成功';
          this.$notify({
            title: '成功',
            message: message,
            type: 'success',
            duration: 3000
          });
        } else {
          this.addToHistory(code, scanType, '1', '', 'ISBN');
          this.$message.error('ISBN 掃描失敗');
        }
      }).catch(error => {
        loading.close();
        this.addToHistory(code, scanType, '1', '', 'ISBN');
        this.$message.error('ISBN 掃描失敗：' + (error.msg || '無法取得書籍資訊'));
      });
    },

    /** 執行一般掃描 */
    performNormalScan(code, scanType) {
      scanCode({ scanCode: code, scanType: scanType }).then(response => {
        if (response.code === 200 && response.data) {
          this.scanResult = response.data;
          this.addToHistory(code, scanType, '0', response.data.itemName, '一般');
          this.$message.success('掃描成功！');
        } else {
          this.addToHistory(code, scanType, '1', '', '一般');
          this.$message.error('掃描失敗：未找到對應物品');
        }
      }).catch(error => {
        this.addToHistory(code, scanType, '1', '', '一般');
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

    /** 新增到掃描歷史 */
    addToHistory(scanCode, scanType, result, itemName, codeType = '一般') {
      const historyItem = {
        scanTime: new Date(),
        scanCode: scanCode,
        scanType: scanType,
        scanResult: result,
        itemName: itemName,
        codeType: codeType  // ISBN 或 一般
      };
      this.scanHistory.unshift(historyItem);

      // 保存到本地存儲
      localStorage.setItem('scanHistory', JSON.stringify(this.scanHistory.slice(0, 50))); // 只保留最近50條
    },

    /** 載入掃描歷史 */
    loadScanHistory() {
      const history = localStorage.getItem('scanHistory');
      if (history) {
        this.scanHistory = JSON.parse(history);
      }
    },

    /** 重新掃描 */
    handleRescan(row) {
      this.scanForm.scanCode = row.scanCode;
      this.scanForm.scanType = row.scanType;
      this.performScan(row.scanCode, row.scanType);
    },

    /** 借出物品 */
    handleBorrow() {
      if (!this.scanResult) return;

      this.borrowForm.itemId = this.scanResult.itemId;
      this.borrowForm.itemName = this.scanResult.itemName;
      this.borrowForm.quantity = 1;
      this.borrowDialogVisible = true;
    },

    /** 提交借出 */
    submitBorrow() {
      this.$refs.borrowForm.validate(valid => {
        if (valid) {
          const borrowData = {
            itemId: this.borrowForm.itemId,
            quantity: this.borrowForm.quantity,
            purpose: this.borrowForm.purpose,
            expectedReturn: this.borrowForm.expectedReturn,
            borrowerId: this.$store.state.user.id,
            borrowerName: this.$store.state.user.name
          };

          borrowItem(borrowData).then(response => {
            this.$message.success('借出成功！');
            this.borrowDialogVisible = false;
            // 重新查詢物品資訊以更新庫存
            this.performScan(this.scanForm.scanCode, this.scanForm.scanType);
          });
        }
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
            this.performScan(this.scanForm.scanCode, this.scanForm.scanType);
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
            this.performScan(this.scanForm.scanCode, this.scanForm.scanType);
          });
        }
      });
    },

    /** 查看詳情 */
    handleViewDetail() {
      if (this.scanResult) {
        this.$router.push('/inventory/item/detail/' + this.scanResult.itemId);
      }
    },

    /** 返回 */
    handleBack() {
      this.$router.go(-1);
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
}

.item-image {
  text-align: center;
}

.item-info h3 {
  margin-bottom: 15px;
  color: #303133;
}

.action-buttons .el-button {
  margin-right: 10px;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
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

.result-card {
  border: 2px solid #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
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

  .scan-container .el-col:first-child {
    margin-bottom: 20px;
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

  .item-info .el-descriptions {
    font-size: 12px;
  }

  .action-buttons .el-button {
    margin: 5px 2px;
    font-size: 12px;
  }

  .scan-history .el-table {
    font-size: 12px;
  }

  .result-card {
    margin: 10px 0;
  }

  .item-image .el-image {
    width: 120px !important;
    height: 120px !important;
  }
}

@media (max-width: 480px) {
  .app-container {
    padding: 10px;
  }

  .box-card {
    margin: 0;
  }

  .scan-container .el-col {
    width: 100%;
  }

  #qr-reader {
    height: 200px !important;
  }

  .scan-controls .el-button {
    display: block;
    width: 100%;
    margin: 5px 0;
  }

  .action-buttons .el-button {
    display: block;
    width: 100%;
    margin: 5px 0;
  }

  .item-image .el-image {
    width: 100px !important;
    height: 100px !important;
  }

  .el-descriptions {
    font-size: 11px;
  }
}
</style>
