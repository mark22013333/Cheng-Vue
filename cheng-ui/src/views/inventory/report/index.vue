<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>庫存報表</span>
      </div>

      <!-- 報表類型選擇 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'stock'}]" @click.native="handleStockReport">
            <div class="report-icon">
              <i class="el-icon-goods"></i>
            </div>
            <div class="report-title">庫存狀況報表</div>
            <div class="report-desc">查看當前庫存狀況、低庫存預警</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'borrow'}]" @click.native="handleBorrowReport">
            <div class="report-icon">
              <i class="el-icon-edit"></i>
            </div>
            <div class="report-title">借出歸還報表</div>
            <div class="report-desc">查看借出歸還記錄、逾期統計</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'movement'}]" @click.native="handleMovementReport">
            <div class="report-icon">
              <i class="el-icon-refresh"></i>
            </div>
            <div class="report-title">庫存異動報表</div>
            <div class="report-desc">查看庫存異動記錄、進出庫統計</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'scan'}]" @click.native="handleScanReport">
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
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="100px">
          <!-- 庫存報表的篩選 -->
          <template v-if="currentReport === 'stock'">
            <el-form-item label="最後進貨時間">
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
            <el-form-item label="分類">
              <el-select v-model="queryParams.categoryId" placeholder="請選擇分類" clearable style="width: 150px">
                <el-option
                  v-for="category in categoryOptions"
                  :key="category.categoryId"
                  :label="category.categoryName"
                  :value="category.categoryId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label-width="0px">
              <el-button type="primary" icon="el-icon-search" @click="handleQuery">查詢</el-button>
              <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
              <el-button icon="el-icon-download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>

          <!-- 借出報表的篩選 -->
          <template v-if="currentReport === 'borrow'">
            <el-form-item label="時間類型">
              <el-select v-model="queryParams.dateType" placeholder="選擇時間類型" style="width: 120px">
                <el-option label="借出時間" value="borrow" />
                <el-option label="歸還時間" value="return" />
              </el-select>
            </el-form-item>
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
            <el-form-item label-width="0px">
              <el-button type="primary" icon="el-icon-search" @click="handleQuery">查詢</el-button>
              <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
              <el-button icon="el-icon-download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>

          <!-- 異動報表的篩選 -->
          <template v-if="currentReport === 'movement'">
            <el-form-item label="異動時間">
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
            <el-form-item label-width="0px">
              <el-button type="primary" icon="el-icon-search" @click="handleQuery">查詢</el-button>
              <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
              <el-button icon="el-icon-download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>

          <!-- 掃描報表的篩選 -->
          <template v-if="currentReport === 'scan'">
            <el-form-item label="掃描時間">
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
            <el-form-item label-width="0px">
              <el-button type="primary" icon="el-icon-search" @click="handleQuery">查詢</el-button>
              <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
              <el-button icon="el-icon-download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>
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
        <el-table v-loading="loading" :data="reportData" :key="currentReport" class="report-table">
          <!-- 庫存報表欄位 -->
          <template v-if="currentReport === 'stock'">
            <el-table-column prop="itemCode" label="物品編碼" min-width="120" />
            <el-table-column prop="itemName" label="物品名稱" min-width="150" />
            <el-table-column prop="categoryName" label="分類" min-width="100" />
            <el-table-column prop="specification" label="規格" min-width="120" />
<!--            <el-table-column prop="unit" label="單位" min-width="80" />-->
            <el-table-column prop="totalQuantity" label="總數量" min-width="90" />
            <el-table-column prop="availableQty" label="可用數量" min-width="90" />
            <el-table-column prop="borrowedQty" label="借出數量" min-width="90" />
            <el-table-column prop="minStock" label="最小庫存" min-width="90" />
            <el-table-column prop="lastInTime" label="最後進貨時間" min-width="160" />
            <el-table-column prop="location" label="存放位置" min-width="120" />
            <el-table-column label="庫存狀態" min-width="100">
              <template slot-scope="scope">
                <el-tag :type="getStockStatusType(scope.row)">
                  {{ getStockStatusText(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
          </template>

          <!-- 借出報表欄位 -->
          <template v-if="currentReport === 'borrow'">
            <el-table-column prop="borrowNo" label="借出編號" min-width="140" />
            <el-table-column prop="itemName" label="物品名稱" min-width="150" />
            <el-table-column prop="borrowerName" label="借用人" min-width="100" />
            <el-table-column prop="quantity" label="借出數量" min-width="90" />
            <el-table-column prop="borrowTime" label="借出時間" min-width="160" />
            <el-table-column prop="expectedReturn" label="預計歸還" min-width="160" />
            <el-table-column prop="actualReturn" label="實際歸還" min-width="160" />
            <el-table-column label="狀態" min-width="100">
              <template slot-scope="scope">
                <el-tag :type="getBorrowStatusType(scope.row.status)">
                  {{ getBorrowStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </template>

          <!-- 異動報表欄位 -->
          <template v-if="currentReport === 'movement'">
            <el-table-column prop="recordTime" label="操作時間" min-width="160" />
            <el-table-column prop="itemName" label="物品名稱" min-width="150" />
            <el-table-column prop="recordType" label="操作類型" min-width="100">
              <template slot-scope="scope">
                <el-tag :type="getRecordTypeTag(scope.row.recordType)">
                  {{ getRecordTypeText(scope.row.recordType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" min-width="100" />
            <el-table-column prop="beforeQty" label="操作前數量" min-width="100" />
            <el-table-column prop="quantity" label="異動數量" min-width="90" />
            <el-table-column prop="afterQty" label="操作後數量" min-width="100" />
            <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
          </template>

          <!-- 掃描報表欄位 -->
          <template v-if="currentReport === 'scan'">
            <el-table-column prop="scanTime" label="掃描時間" min-width="160" />
            <el-table-column prop="operatorName" label="掃描人" min-width="100" />
            <el-table-column prop="scanType" label="掃描類型" min-width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.scanType === '1' ? 'primary' : 'success'">
                  {{ scope.row.scanType === '1' ? '條碼' : 'QR碼' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scanCode" label="掃描內容" min-width="150" />
            <el-table-column label="物品名稱" min-width="150">
              <template slot-scope="scope">
                <span>{{ scope.row.itemName || '（未識別）' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="scanResult" label="掃描結果" min-width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.scanResult === '0' ? 'success' : 'danger'">
                  {{ scope.row.scanResult === '0' ? '成功' : '失敗' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="錯誤訊息" min-width="150" show-overflow-tooltip>
              <template slot-scope="scope">
                <span v-if="scope.row.errorMsg" style="color: #F56C6C;">{{ scope.row.errorMsg }}</span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
          </template>
        </el-table>

        <pagination
          v-show="total > 0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="loadReportData"
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
  getScanReport
} from "@/api/inventory/report";
import { listUsedCategory } from "@/api/inventory/category";
import { download } from '@/utils/request';
import * as echarts from 'echarts';

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
        dateType: 'borrow', // 借出報表的時間類型：borrow=借出時間, return=歸還時間
        beginTime: null,
        endTime: null,
        beginBorrowTime: null,
        endBorrowTime: null,
        beginActualReturn: null,
        endActualReturn: null,
        params: {}
      },
      // 下載方法
      download: download
    };
  },
  created() {
    this.getCategoryList();
  },
  methods: {
    /** 查詢分類列表 */
    getCategoryList() {
      listUsedCategory().then(response => {
        this.categoryOptions = response.data;
      });
    },

    /** 重置查詢參數（切換報表時使用） */
    resetQueryParams() {
      // 立即清空舊資料，避免切換時顯示錯亂
      this.reportData = [];
      this.reportStats = [];
      this.total = 0;
      
      this.dateRange = [];
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        categoryId: null,
        dateType: 'borrow',
        beginTime: null,
        endTime: null,
        beginBorrowTime: null,
        endBorrowTime: null,
        beginActualReturn: null,
        endActualReturn: null,
        params: {}
      };
    },

    /** 庫存報表 */
    handleStockReport() {
      this.currentReport = 'stock';
      this.reportTitle = '庫存狀況報表';
      this.showChart = true;
      this.resetQueryParams();
      this.loadReportData();
    },

    /** 借出報表 */
    handleBorrowReport() {
      this.currentReport = 'borrow';
      this.reportTitle = '借出歸還報表';
      this.showChart = true;
      this.resetQueryParams();
      this.loadReportData();
    },

    /** 異動報表 */
    handleMovementReport() {
      this.currentReport = 'movement';
      this.reportTitle = '庫存異動報表';
      this.showChart = false;
      this.resetQueryParams();
      this.loadReportData();
    },

    /** 掃描報表 */
    handleScanReport() {
      this.currentReport = 'scan';
      this.reportTitle = '掃描記錄報表';
      this.showChart = false;
      this.resetQueryParams();
      this.loadReportData();
    },

    /** 日期變更 */
    handleDateChange(dates) {
      if (dates && dates.length === 2) {
        const begin = dates[0];
        const end = dates[1];

        // 根據不同報表類型設置不同的時間參數
        switch (this.currentReport) {
          case 'stock':
            // 庫存報表 - 最後進貨時間
            if (!this.queryParams.params) {
              this.queryParams.params = {};
            }
            this.queryParams.params.beginTime = begin;
            this.queryParams.params.endTime = end;
            break;
          case 'borrow':
            // 借出報表 - 根據 dateType 設置不同欄位
            if (this.queryParams.dateType === 'return') {
              this.queryParams.beginActualReturn = begin;
              this.queryParams.endActualReturn = end;
              this.queryParams.beginBorrowTime = null;
              this.queryParams.endBorrowTime = null;
            } else {
              this.queryParams.beginBorrowTime = begin;
              this.queryParams.endBorrowTime = end;
              this.queryParams.beginActualReturn = null;
              this.queryParams.endActualReturn = null;
            }
            break;
          case 'movement':
            // 異動報表 - 使用 params
            if (!this.queryParams.params) {
              this.queryParams.params = {};
            }
            this.queryParams.params.beginRecordTime = begin;
            this.queryParams.params.endRecordTime = end;
            break;
          case 'scan':
            // 掃描報表 - 使用 params
            if (!this.queryParams.params) {
              this.queryParams.params = {};
            }
            this.queryParams.params.beginTime = begin;
            this.queryParams.params.endTime = end;
            break;
        }
      } else {
        // 清空所有時間參數
        this.queryParams.beginTime = null;
        this.queryParams.endTime = null;
        this.queryParams.beginBorrowTime = null;
        this.queryParams.endBorrowTime = null;
        this.queryParams.beginActualReturn = null;
        this.queryParams.endActualReturn = null;
        if (this.queryParams.params) {
          this.queryParams.params.beginRecordTime = null;
          this.queryParams.params.endRecordTime = null;
          this.queryParams.params.beginTime = null;
          this.queryParams.params.endTime = null;
        }
      }
    },

    /** 查詢 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadReportData();
    },

    /** 重置（同一報表內的搜尋重置） */
    resetQuery() {
      this.resetQueryParams();
      this.loadReportData();
    },

    /** 載入報表資料 */
    loadReportData() {
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
        this.reportData = response.rows;
        this.total = response.total;

        const stats = response.extra.statistics;
        this.reportStats = [
          { label: '總物品數', value: stats.totalItems },
          { label: '總庫存量', value: stats.totalQuantity },
          { label: '低庫存物品', value: stats.lowStockItems },
          { label: '缺貨物品', value: stats.outOfStockItems }
        ];

        this.loading = false;

        // 初始化圖表
        this.$nextTick(() => {
          this.initStockCharts(response.rows);
        });
      }).catch(() => {
        this.loading = false;
      });
    },

    /** 產生借出報表 */
    generateBorrowReport() {
      getBorrowReport(this.queryParams).then(response => {
        this.reportData = response.rows;
        this.total = response.total;

        const stats = response.extra.statistics;
        this.reportStats = [
          { label: '總借出數', value: stats.totalBorrows },
          { label: '待審核', value: stats.pendingBorrows },
          { label: '已批准', value: stats.approvedBorrows },
          { label: '已歸還', value: stats.returnedBorrows },
          { label: '逾期未還', value: stats.overdueBorrows }
        ];

        this.loading = false;

        // 初始化圖表
        this.$nextTick(() => {
          this.initBorrowCharts(stats);
        });
      }).catch(() => {
        this.loading = false;
      });
    },

    /** 產生異動報表 */
    generateMovementReport() {
      getMovementReport(this.queryParams).then(response => {
        this.reportData = response.rows;
        this.total = response.total;

        const stats = response.extra.statistics;
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
        this.reportData = response.rows;
        this.total = response.total;

        const stats = response.extra.statistics;
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
    },

    /** 匯出報表 */
    exportReport() {
      if (!this.currentReport) {
        this.$message.warning('請先選擇報表類型');
        return;
      }

      let fileName = '';
      switch (this.currentReport) {
        case 'stock':
          fileName = '庫存狀況報表';
          break;
        case 'borrow':
          fileName = '借出歸還報表';
          break;
        case 'movement':
          fileName = '庫存異動報表';
          break;
        case 'scan':
          fileName = '掃描記錄報表';
          break;
        default:
          this.$message.error('不支援的報表類型');
          return;
      }

      this.download('/inventory/report/' + this.currentReport + '/export', {
        ...this.queryParams
      }, fileName + '.xlsx');
    },

    /** 取得庫存狀態類型 */
    getStockStatusType(row) {
      if (row.totalQuantity <= 0) {
        return 'danger';
      } else if (row.totalQuantity <= row.minStock) {
        return 'warning';
      }
      return 'success';
    },

    /** 取得庫存狀態文字 */
    getStockStatusText(row) {
      if (row.totalQuantity <= 0) {
        return '缺貨';
      } else if (row.totalQuantity <= row.minStock) {
        return '低庫存';
      }
      return '正常';
    },

    /** 取得借出狀態類型 */
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

    /** 取得借出狀態文字 */
    getBorrowStatusText(status) {
      const statusMap = {
        '0': '待審核',
        '1': '已借出',
        '2': '審核拒絕',
        '3': '已歸還',
        '4': '部分歸還',
        '5': '逾期'
      };
      return statusMap[status] || '未知';
    },

    /** 取得操作類型文字 */
    getRecordTypeText(type) {
      const typeMap = {
        '1': '入庫',
        '2': '出庫',
        '3': '借出',
        '4': '歸還',
        '5': '盤點',
        '6': '損耗',
        '7': '遺失'
      };
      return typeMap[type] || '未知';
    },

    /** 取得操作類型標籤 */
    getRecordTypeTag(type) {
      const tagMap = {
        '1': 'success',
        '2': 'warning',
        '3': 'primary',
        '4': 'success',
        '5': 'info',
        '6': 'danger',
        '7': 'danger'
      };
      return tagMap[type] || 'info';
    },

    /** 初始化庫存圖表 */
    initStockCharts(data) {
      if (!data || data.length === 0) return;

      // 趨勢圖表 - 庫存數量前10項
      const trendChart = echarts.init(document.getElementById('trendChart'));
      const top10 = data.slice(0, 10);
      trendChart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: top10.map(item => item.itemName),
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '庫存數量',
          type: 'bar',
          data: top10.map(item => item.totalQuantity),
          itemStyle: {
            color: '#409EFF'
          }
        }]
      });

      // 分布圖表 - 庫存狀態分布
      const pieChart = echarts.init(document.getElementById('pieChart'));
      const normal = data.filter(item => item.totalQuantity > item.minStock).length;
      const low = data.filter(item => item.totalQuantity > 0 && item.totalQuantity <= item.minStock).length;
      const out = data.filter(item => item.totalQuantity <= 0).length;

      pieChart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '庫存狀態',
          type: 'pie',
          radius: '50%',
          data: [
            { value: normal, name: '正常' },
            { value: low, name: '低庫存' },
            { value: out, name: '缺貨' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      });
    },

    /** 初始化借出圖表 */
    initBorrowCharts(stats) {
      // 趨勢圖表 - 借出狀態統計
      const trendChart = echarts.init(document.getElementById('trendChart'));
      trendChart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['待審核', '已批准', '已歸還', '逾期']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '數量',
          type: 'bar',
          data: [
            stats.pendingBorrows,
            stats.approvedBorrows,
            stats.returnedBorrows,
            stats.overdueBorrows
          ],
          itemStyle: {
            color: '#67C23A'
          }
        }]
      });

      // 分布圖表 - 借出狀態分布
      const pieChart = echarts.init(document.getElementById('pieChart'));
      pieChart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '借出狀態',
          type: 'pie',
          radius: '50%',
          data: [
            { value: stats.pendingBorrows, name: '待審核' },
            { value: stats.approvedBorrows, name: '已批准' },
            { value: stats.returnedBorrows, name: '已歸還' },
            { value: stats.overdueBorrows, name: '逾期' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      });
    }
  }
};
</script>

<style scoped>
.report-card {
  cursor: pointer;
  text-align: center;
  padding: 8px 6px;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.report-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
  border-color: #409eff;
}

.report-card-active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.report-card-active .report-icon {
  color: #409eff;
}

.report-card-active .report-title {
  color: #409eff;
}

.report-icon {
  font-size: 28px;
  color: #909399;
  margin-bottom: 6px;
  transition: color 0.3s;
}

.report-title {
  font-size: 13px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  transition: color 0.3s;
}

.report-desc {
  font-size: 10px;
  color: #909399;
  line-height: 1.3;
}

.report-content {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.report-table {
  width: 100% !important;
}

/* 讓表格欄位自適應 */
::v-deep .el-table {
  width: 100%;
}

::v-deep .el-table__body-wrapper {
  overflow-x: auto;
}

::v-deep .el-table td,
::v-deep .el-table th {
  white-space: nowrap;
}

.stat-content {
  padding: 12px 10px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 12px;
  color: #606266;
}

@media print {
  .el-button, .el-form, .el-pagination {
    display: none !important;
  }
}
</style>
