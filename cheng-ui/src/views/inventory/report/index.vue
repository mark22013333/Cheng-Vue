<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>庫存報表</span>
      </div>

      <!-- 報表類型選擇 -->
      <el-row :gutter="20" class="mb8">
        <el-col :span="6">
          <el-card shadow="hover" class="report-card" @click.native="handleStockReport">
            <div class="report-icon">
              <i class="el-icon-goods"></i>
            </div>
            <div class="report-title">庫存狀況報表</div>
            <div class="report-desc">查看當前庫存狀況、低庫存預警</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="report-card" @click.native="handleBorrowReport">
            <div class="report-icon">
              <i class="el-icon-edit"></i>
            </div>
            <div class="report-title">借出歸還報表</div>
            <div class="report-desc">查看借出歸還記錄、逾期統計</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="report-card" @click.native="handleMovementReport">
            <div class="report-icon">
              <i class="el-icon-refresh"></i>
            </div>
            <div class="report-title">庫存異動報表</div>
            <div class="report-desc">查看庫存異動記錄、進出庫統計</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="report-card" @click.native="handleScanReport">
            <div class="report-icon">
              <i class="el-icon-search"></i>
            </div>
            <div class="report-title">掃描記錄報表</div>
            <div class="report-desc">查看掃描記錄、成功率統計</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 報表內容區域 -->
      <div class="report-content" v-if="currentReport">
        <el-divider content-position="left">{{ reportTitle }}</el-divider>
        
        <!-- 篩選條件 -->
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
          <el-form-item label="時間範圍">
            <el-date-picker
              v-model="dateRange"
              style="width: 240px"
              value-format="yyyy-MM-dd"
              type="daterange"
              range-separator="-"
              start-placeholder="開始日期"
              end-placeholder="結束日期"
              @change="handleDateChange"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="分類" v-if="currentReport === 'stock'">
            <el-select v-model="queryParams.categoryId" placeholder="請選擇分類" clearable>
              <el-option
                v-for="category in categoryOptions"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="generateReport">產生報表</el-button>
            <el-button icon="el-icon-download" @click="exportReport">匯出Excel</el-button>
            <el-button icon="el-icon-printer" @click="printReport">列印報表</el-button>
          </el-form-item>
        </el-form>

        <!-- 報表統計卡片 -->
        <el-row :gutter="20" class="mb8" v-if="reportStats">
          <el-col :span="6" v-for="(stat, index) in reportStats" :key="index">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 報表圖表 -->
        <el-row :gutter="20" class="mb8" v-if="showChart">
          <el-col :span="12">
            <el-card>
              <div slot="header">
                <span>趨勢圖表</span>
              </div>
              <div id="trendChart" style="width: 100%; height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <div slot="header">
                <span>分布圖表</span>
              </div>
              <div id="pieChart" style="width: 100%; height: 300px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 報表表格 -->
        <el-table v-loading="loading" :data="reportData" style="width: 100%">
          <!-- 庫存報表欄位 -->
          <template v-if="currentReport === 'stock'">
            <el-table-column prop="itemCode" label="物品編碼" />
            <el-table-column prop="itemName" label="物品名稱" />
            <el-table-column prop="categoryName" label="分類" />
            <el-table-column prop="stockQuantity" label="庫存數量" />
            <el-table-column prop="availableQuantity" label="可用數量" />
            <el-table-column prop="borrowedQuantity" label="借出數量" />
            <el-table-column prop="minStock" label="最小庫存" />
            <el-table-column label="庫存狀態">
              <template slot-scope="scope">
                <el-tag :type="getStockStatusType(scope.row)">
                  {{ getStockStatusText(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
          </template>

          <!-- 借出報表欄位 -->
          <template v-if="currentReport === 'borrow'">
            <el-table-column prop="borrowCode" label="借出編號" />
            <el-table-column prop="itemName" label="物品名稱" />
            <el-table-column prop="borrowerName" label="借用人" />
            <el-table-column prop="quantity" label="借出數量" />
            <el-table-column prop="borrowTime" label="借出時間" />
            <el-table-column prop="expectedReturn" label="預計歸還" />
            <el-table-column prop="actualReturn" label="實際歸還" />
            <el-table-column label="狀態">
              <template slot-scope="scope">
                <el-tag :type="getBorrowStatusType(scope.row.status)">
                  {{ getBorrowStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </template>

          <!-- 異動報表欄位 -->
          <template v-if="currentReport === 'movement'">
            <el-table-column prop="itemName" label="物品名稱" />
            <el-table-column prop="operationType" label="操作類型" />
            <el-table-column prop="quantity" label="數量" />
            <el-table-column prop="beforeQuantity" label="操作前數量" />
            <el-table-column prop="afterQuantity" label="操作後數量" />
            <el-table-column prop="operatorName" label="操作人" />
            <el-table-column prop="operationTime" label="操作時間" />
            <el-table-column prop="reason" label="原因" />
          </template>

          <!-- 掃描報表欄位 -->
          <template v-if="currentReport === 'scan'">
            <el-table-column prop="scanTime" label="掃描時間" />
            <el-table-column prop="scanType" label="掃描類型">
              <template slot-scope="scope">
                <el-tag :type="scope.row.scanType === '1' ? 'primary' : 'success'">
                  {{ scope.row.scanType === '1' ? '條碼' : 'QR碼' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scanCode" label="掃描內容" />
            <el-table-column prop="itemName" label="物品名稱" />
            <el-table-column prop="scannerName" label="掃描人" />
            <el-table-column prop="scanResult" label="掃描結果">
              <template slot-scope="scope">
                <el-tag :type="scope.row.scanResult === '0' ? 'success' : 'danger'">
                  {{ scope.row.scanResult === '0' ? '成功' : '失敗' }}
                </el-tag>
              </template>
            </el-table-column>
          </template>
        </el-table>

        <pagination
          v-show="total > 0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="generateReport"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { 
  getStockReport, 
  getBorrowReport, 
  getMovementReport, 
  getScanReport,
  exportStockReport,
  exportBorrowReport,
  exportMovementReport,
  exportScanReport
} from "@/api/inventory/report";
import { listCategory } from "@/api/inventory/item";

export default {
  name: "Report",
  data() {
    return {
      // 當前報表類型
      currentReport: null,
      // 報表標題
      reportTitle: '',
      // 顯示圖表
      showChart: false,
      // 載入狀態
      loading: false,
      // 總條數
      total: 0,
      // 報表資料
      reportData: [],
      // 報表統計
      reportStats: null,
      // 分類選項
      categoryOptions: [],
      // 日期範圍
      dateRange: [],
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        categoryId: null,
        beginTime: null,
        endTime: null
      }
    };
  },
  created() {
    this.getCategoryList();
  },
  methods: {
    /** 查詢分類列表 */
    getCategoryList() {
      listCategory().then(response => {
        this.categoryOptions = response.data;
      });
    },

    /** 庫存報表 */
    handleStockReport() {
      this.currentReport = 'stock';
      this.reportTitle = '庫存狀況報表';
      this.showChart = true;
      this.generateReport();
    },

    /** 借出報表 */
    handleBorrowReport() {
      this.currentReport = 'borrow';
      this.reportTitle = '借出歸還報表';
      this.showChart = true;
      this.generateReport();
    },

    /** 異動報表 */
    handleMovementReport() {
      this.currentReport = 'movement';
      this.reportTitle = '庫存異動報表';
      this.showChart = false;
      this.generateReport();
    },

    /** 掃描報表 */
    handleScanReport() {
      this.currentReport = 'scan';
      this.reportTitle = '掃描記錄報表';
      this.showChart = false;
      this.generateReport();
    },

    /** 日期變更 */
    handleDateChange(dates) {
      if (dates && dates.length === 2) {
        this.queryParams.beginTime = dates[0];
        this.queryParams.endTime = dates[1];
      } else {
        this.queryParams.beginTime = null;
        this.queryParams.endTime = null;
      }
    },

    /** 產生報表 */
    generateReport() {
      if (!this.currentReport) return;

      this.loading = true;
      
      switch (this.currentReport) {
        case 'stock':
          this.generateStockReport();
          break;
        case 'borrow':
          this.generateBorrowReport();
          break;
        case 'movement':
          this.generateMovementReport();
          break;
        case 'scan':
          this.generateScanReport();
          break;
      }
    },

    /** 產生庫存報表 */
    generateStockReport() {
      getStockReport(this.queryParams).then(response => {
        const result = response.data;
        this.reportData = result.data;
        this.total = result.data.length;
        
        const stats = result.statistics;
        this.reportStats = [
          { label: '總物品數', value: stats.totalItems },
          { label: '總庫存量', value: stats.totalQuantity },
          { label: '低庫存物品', value: stats.lowStockItems },
          { label: '缺貨物品', value: stats.outOfStockItems }
        ];
        
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },

    /** 產生借出報表 */
    generateBorrowReport() {
      getBorrowReport(this.queryParams).then(response => {
        const result = response.data;
        this.reportData = result.data;
        this.total = result.data.length;
        
        const stats = result.statistics;
        this.reportStats = [
          { label: '總借出數', value: stats.totalBorrows },
          { label: '待審核', value: stats.pendingBorrows },
          { label: '已批准', value: stats.approvedBorrows },
          { label: '已歸還', value: stats.returnedBorrows },
          { label: '逾期未還', value: stats.overdueBorrows }
        ];
        
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },

    /** 產生異動報表 */
    generateMovementReport() {
      getMovementReport(this.queryParams).then(response => {
        const result = response.data;
        this.reportData = result.data;
        this.total = result.data.length;
        
        const stats = result.statistics;
        this.reportStats = [
          { label: '總記錄數', value: stats.totalRecords },
          { label: '入庫記錄', value: stats.inRecords },
          { label: '出庫記錄', value: stats.outRecords },
          { label: '盤點記錄', value: stats.checkRecords }
        ];
        
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },

    /** 產生掃描報表 */
    generateScanReport() {
      getScanReport(this.queryParams).then(response => {
        const result = response.data;
        this.reportData = result.data;
        this.total = result.data.length;
        
        const stats = result.statistics;
        const successRate = stats.totalScans > 0 ? 
          ((stats.successScans / stats.totalScans) * 100).toFixed(2) + '%' : '0%';
        
        this.reportStats = [
          { label: '總掃描次數', value: stats.totalScans },
          { label: '成功次數', value: stats.successScans },
          { label: '失敗次數', value: stats.failedScans },
          { label: '條碼掃描', value: stats.barcodeScans },
          { label: 'QR碼掃描', value: stats.qrcodeScans },
          { label: '成功率', value: successRate }
        ];
        
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
      this.loading = false;
    },

    /** 匯出報表 */
    exportReport() {
      if (!this.currentReport) {
        this.$message.warning('請先選擇報表類型');
        return;
      }
      
      let exportFunction;
      switch (this.currentReport) {
        case 'stock':
          exportFunction = exportStockReport;
          break;
        case 'borrow':
          exportFunction = exportBorrowReport;
          break;
        case 'movement':
          exportFunction = exportMovementReport;
          break;
        case 'scan':
          exportFunction = exportScanReport;
          break;
        default:
          this.$message.error('不支援的報表類型');
          return;
      }
      
      this.$message.loading('正在匯出報表...');
      exportFunction(this.queryParams).then(() => {
        this.$message.success('報表匯出成功');
      }).catch(() => {
        this.$message.error('報表匯出失敗');
      });
    },

    /** 列印報表 */
    printReport() {
      if (!this.currentReport) {
        this.$message.warning('請先選擇報表類型');
        return;
      }
      
      window.print();
    },

    /** 獲取庫存狀態類型 */
    getStockStatusType(row) {
      if (row.stockQuantity <= 0) {
        return 'danger';
      } else if (row.stockQuantity <= row.minStock) {
        return 'warning';
      }
      return 'success';
    },

    /** 獲取庫存狀態文字 */
    getStockStatusText(row) {
      if (row.stockQuantity <= 0) {
        return '缺貨';
      } else if (row.stockQuantity <= row.minStock) {
        return '低庫存';
      }
      return '正常';
    },

    /** 獲取借出狀態類型 */
    getBorrowStatusType(status) {
      const statusMap = {
        '0': 'info',     // 待審核
        '1': 'primary',  // 已借出
        '2': 'success',  // 已歸還
        '3': 'danger',   // 已拒絕
        '4': 'warning'   // 逾期
      };
      return statusMap[status] || 'info';
    },

    /** 獲取借出狀態文字 */
    getBorrowStatusText(status) {
      const statusMap = {
        '0': '待審核',
        '1': '已借出',
        '2': '已歸還',
        '3': '已拒絕',
        '4': '逾期'
      };
      return statusMap[status] || '未知';
    }
  }
};
</script>

<style scoped>
.report-card {
  cursor: pointer;
  text-align: center;
  padding: 20px;
  transition: all 0.3s;
}

.report-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.report-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 15px;
}

.report-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.report-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.report-content {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 20px;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

@media print {
  .el-button, .el-form, .el-pagination {
    display: none !important;
  }
}
</style>
