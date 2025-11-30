<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>庫存報表</span>
      </div>

      <el-row :gutter="10" class="mb8">
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'stock'}]"
                   @click.native="handleStockReport">
            <div class="report-icon">
              <i class="el-icon-goods"></i>
            </div>
            <div class="report-title">庫存狀況報表</div>
            <div class="report-desc">查看當前庫存狀況、低庫存預警</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'borrow'}]"
                   @click.native="handleBorrowReport">
            <div class="report-icon">
              <i class="el-icon-edit"></i>
            </div>
            <div class="report-title">借出歸還報表</div>
            <div class="report-desc">查看借出歸還記錄、逾期統計</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'movement'}]"
                   @click.native="handleMovementReport">
            <div class="report-icon">
              <i class="el-icon-refresh"></i>
            </div>
            <div class="report-title">庫存異動報表</div>
            <div class="report-desc">查看庫存異動記錄、進出庫統計</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :class="['report-card', {'report-card-active': currentReport === 'scan'}]"
                   @click.native="handleScanReport">
            <div class="report-icon">
              <i class="el-icon-search"></i>
            </div>
            <div class="report-title">掃描記錄報表</div>
            <div class="report-desc">查看掃描記錄、成功率統計</div>
          </el-card>
        </el-col>
      </el-row>

      <div class="report-content" v-if="currentReport">
        <el-divider content-position="left">{{ reportTitle }}</el-divider>

        <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="100px">
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
              <el-button type="primary" icon="Search" @click="handleQuery">查詢</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button icon="Download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>

          <template v-if="currentReport === 'borrow'">
            <el-form-item label="時間類型">
              <el-select v-model="queryParams.dateType" placeholder="選擇時間類型" style="width: 120px">
                <el-option label="借出時間" value="borrow"/>
                <el-option label="歸還時間" value="return"/>
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
              <el-button type="primary" icon="Search" @click="handleQuery">查詢</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button icon="Download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>

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
              <el-button type="primary" icon="Search" @click="handleQuery">查詢</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button icon="Download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>

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
              <el-button type="primary" icon="Search" @click="handleQuery">查詢</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button icon="Download" @click="exportReport">匯出Excel</el-button>
            </el-form-item>
          </template>
        </el-form>

        <el-row :gutter="20" class="mb8 stat-row" type="flex" v-if="reportStats">
          <el-col v-for="(stat, index) in reportStats" :key="index">
            <el-card class="stat-card" shadow="never">
              <div class="stat-content">
                <div class="stat-number">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

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

        <el-table v-loading="loading" :data="reportData" :key="currentReport" class="report-table">
          <template v-if="currentReport === 'stock'">
            <el-table-column prop="itemCode" label="物品編碼" min-width="120"/>
            <el-table-column prop="itemName" label="物品名稱" min-width="150"/>
            <el-table-column prop="categoryName" label="分類" min-width="100"/>
            <el-table-column prop="specification" label="規格" min-width="120"/>
            <el-table-column prop="totalQuantity" label="總數量" min-width="70" align="center"/>
            <el-table-column prop="availableQty" label="可用" min-width="70" align="center"/>
            <el-table-column prop="borrowedQty" label="借出" min-width="70" align="center"/>
            <el-table-column prop="minStock" label="最小" min-width="70" align="center"/>
            <el-table-column prop="lastInTime" label="最後進貨時間" min-width="160"/>
            <el-table-column prop="location" label="存放位置" min-width="120"/>
            <el-table-column label="庫存狀態" min-width="100">
              <template #default="scope">
                <el-tag :type="getStockStatusType(scope.row)">
                  {{ getStockStatusText(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
          </template>

          <template v-if="currentReport === 'borrow'">
            <el-table-column prop="borrowNo" label="借出編號" min-width="140"/>
            <el-table-column prop="itemName" label="物品名稱" min-width="150"/>
            <el-table-column prop="borrowerName" label="借用人" min-width="100"/>
            <el-table-column prop="quantity" label="數量" min-width="70" align="center"/>
            <el-table-column prop="borrowTime" label="借出時間" min-width="160"/>
            <el-table-column prop="expectedReturn" label="預計歸還" min-width="160"/>
            <el-table-column prop="actualReturn" label="實際歸還" min-width="160"/>
            <el-table-column label="狀態" min-width="100">
              <template #default="scope">
                <el-tag :type="getBorrowStatusType(scope.row.status)">
                  {{ getBorrowStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </template>

          <template v-if="currentReport === 'movement'">
            <el-table-column prop="recordTime" label="操作時間" min-width="160"/>
            <el-table-column prop="itemName" label="物品名稱" min-width="150"/>
            <el-table-column prop="recordType" label="操作類型" min-width="100">
              <template #default="scope">
                <el-tag :type="getRecordTypeTag(scope.row.recordType)">
                  {{ getRecordTypeText(scope.row.recordType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" min-width="100"/>
            <el-table-column prop="beforeQty" label="變動前" min-width="70" align="center"/>
            <el-table-column prop="quantity" label="變動數" min-width="70" align="center"/>
            <el-table-column prop="afterQty" label="變動後" min-width="70" align="center"/>
            <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip/>
          </template>

          <template v-if="currentReport === 'scan'">
            <el-table-column prop="scanTime" label="掃描時間" min-width="160"/>
            <el-table-column prop="operatorName" label="掃描人" min-width="100"/>
            <el-table-column prop="scanType" label="掃描類型" min-width="100">
              <template #default="scope">
                <el-tag :type="scope.row.scanType === '1' ? 'primary' : 'success'">
                  {{ scope.row.scanType === '1' ? '條碼' : 'QR碼' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scanCode" label="掃描內容" min-width="150"/>
            <el-table-column label="物品名稱" min-width="150">
              <template #default="scope">
                <span>{{ scope.row.itemName || '（未識別）' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="scanResult" label="掃描結果" min-width="100">
              <template #default="scope">
                <el-tag :type="scope.row.scanResult === '0' ? 'success' : 'danger'">
                  {{ scope.row.scanResult === '0' ? '成功' : '失敗' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="錯誤訊息" min-width="150" show-overflow-tooltip>
              <template #default="scope">
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
// Script 部分完全保持不變
import {
  getStockReport,
  getBorrowReport,
  getMovementReport,
  getScanReport
} from "@/api/inventory/report";
import {listUsedCategory} from "@/api/inventory/category";
import {download} from '@/utils/request';
import * as echarts from 'echarts';

export default {
  name: "Report",
  data() {
    return {
      currentReport: null,
      reportTitle: '',
      showChart: false,
      loading: false,
      total: 0,
      reportData: [],
      reportStats: null,
      categoryOptions: [],
      dateRange: [],
      queryParams: {
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
      },
      download: download
    };
  },
  created() {
    this.getCategoryList();
  },
  methods: {
    getCategoryList() {
      listUsedCategory().then(response => {
        this.categoryOptions = response.data;
      });
    },
    resetQueryParams() {
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
    handleStockReport() {
      this.currentReport = 'stock';
      this.reportTitle = '庫存狀況報表';
      this.showChart = true;
      this.resetQueryParams();
      this.loadReportData();
    },
    handleBorrowReport() {
      this.currentReport = 'borrow';
      this.reportTitle = '借出歸還報表';
      this.showChart = true;
      this.resetQueryParams();
      this.loadReportData();
    },
    handleMovementReport() {
      this.currentReport = 'movement';
      this.reportTitle = '庫存異動報表';
      this.showChart = false;
      this.resetQueryParams();
      this.loadReportData();
    },
    handleScanReport() {
      this.currentReport = 'scan';
      this.reportTitle = '掃描記錄報表';
      this.showChart = false;
      this.resetQueryParams();
      this.loadReportData();
    },
    handleDateChange(dates) {
      if (dates && dates.length === 2) {
        const begin = dates[0];
        const end = dates[1];
        switch (this.currentReport) {
          case 'stock':
            if (!this.queryParams.params) this.queryParams.params = {};
            this.queryParams.params.beginTime = begin;
            this.queryParams.params.endTime = end;
            break;
          case 'borrow':
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
            if (!this.queryParams.params) this.queryParams.params = {};
            this.queryParams.params.beginRecordTime = begin;
            this.queryParams.params.endRecordTime = end;
            break;
          case 'scan':
            if (!this.queryParams.params) this.queryParams.params = {};
            this.queryParams.params.beginTime = begin;
            this.queryParams.params.endTime = end;
            break;
        }
      } else {
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
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadReportData();
    },
    resetQuery() {
      this.resetQueryParams();
      this.loadReportData();
    },
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
    generateStockReport() {
      getStockReport(this.queryParams).then(response => {
        this.reportData = response.rows;
        this.total = response.total;
        const stats = response.extra.statistics;
        this.reportStats = [
          {label: '總物品數', value: stats.totalItems},
          {label: '總庫存量', value: stats.totalQuantity},
          {label: '低庫存物品', value: stats.lowStockItems},
          {label: '缺貨物品', value: stats.outOfStockItems}
        ];
        this.loading = false;
        this.$nextTick(() => {
          this.initStockCharts(response.rows);
        });
      }).catch(() => {
        this.loading = false;
      });
    },
    generateBorrowReport() {
      getBorrowReport(this.queryParams).then(response => {
        this.reportData = response.rows;
        this.total = response.total;
        const stats = response.extra.statistics;
        this.reportStats = [
          {label: '總借出數', value: stats.totalBorrows},
          {label: '待審核', value: stats.pendingBorrows},
          {label: '已批准', value: stats.approvedBorrows},
          {label: '已歸還', value: stats.returnedBorrows},
          {label: '逾期未還', value: stats.overdueBorrows}
        ];
        this.loading = false;
        this.$nextTick(() => {
          this.initBorrowCharts(stats);
        });
      }).catch(() => {
        this.loading = false;
      });
    },
    generateMovementReport() {
      getMovementReport(this.queryParams).then(response => {
        this.reportData = response.rows;
        this.total = response.total;
        const stats = response.extra.statistics;
        this.reportStats = [
          {label: '總記錄數', value: stats.totalRecords},
          {label: '入庫記錄', value: stats.inRecords},
          {label: '出庫記錄', value: stats.outRecords},
          {label: '盤點記錄', value: stats.checkRecords}
        ];
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    generateScanReport() {
      getScanReport(this.queryParams).then(response => {
        this.reportData = response.rows;
        this.total = response.total;
        const stats = response.extra.statistics;
        const successRate = stats.totalScans > 0 ?
          ((stats.successScans / stats.totalScans) * 100).toFixed(2) + '%' : '0%';
        this.reportStats = [
          {label: '總掃描次數', value: stats.totalScans},
          {label: '成功次數', value: stats.successScans},
          {label: '失敗次數', value: stats.failedScans},
          {label: '條碼掃描', value: stats.barcodeScans},
          {label: 'QR碼掃描', value: stats.qrcodeScans},
          {label: '成功率', value: successRate}
        ];
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
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
    getStockStatusType(row) {
      if (row.totalQuantity <= 0) return 'danger';
      else if (row.totalQuantity <= row.minStock) return 'warning';
      return 'success';
    },
    getStockStatusText(row) {
      if (row.totalQuantity <= 0) return '缺貨';
      else if (row.totalQuantity <= row.minStock) return '低庫存';
      return '正常';
    },
    getBorrowStatusType(status) {
      const statusMap = {'0': 'info', '1': 'primary', '2': 'success', '3': 'danger', '4': 'warning'};
      return statusMap[status] || 'info';
    },
    getBorrowStatusText(status) {
      const statusMap = {'0': '待審核', '1': '已借出', '2': '審核拒絕', '3': '已歸還', '4': '部分歸還', '5': '逾期'};
      return statusMap[status] || '未知';
    },
    getRecordTypeText(type) {
      const typeMap = {'1': '入庫', '2': '出庫', '3': '借出', '4': '歸還', '5': '盤點', '6': '損耗', '7': '遺失'};
      return typeMap[type] || '未知';
    },
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
    initStockCharts(data) {
      if (!data || data.length === 0) return;
      const trendChart = echarts.init(document.getElementById('trendChart'));
      const top10 = data.slice(0, 10);
      trendChart.setOption({
        tooltip: {trigger: 'axis'},
        xAxis: {type: 'category', data: top10.map(item => item.itemName), axisLabel: {rotate: 45, interval: 0}},
        yAxis: {type: 'value'},
        series: [{
          name: '庫存數量',
          type: 'bar',
          data: top10.map(item => item.totalQuantity),
          itemStyle: {color: '#409EFF'}
        }]
      });
      const pieChart = echarts.init(document.getElementById('pieChart'));
      const normal = data.filter(item => item.totalQuantity > item.minStock).length;
      const low = data.filter(item => item.totalQuantity > 0 && item.totalQuantity <= item.minStock).length;
      const out = data.filter(item => item.totalQuantity <= 0).length;
      pieChart.setOption({
        tooltip: {trigger: 'item'},
        legend: {orient: 'vertical', left: 'left'},
        series: [{
          name: '庫存狀態', type: 'pie', radius: '50%',
          data: [{value: normal, name: '正常'}, {value: low, name: '低庫存'}, {value: out, name: '缺貨'}],
          emphasis: {itemStyle: {shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)'}}
        }]
      });
    },
    initBorrowCharts(stats) {
      const trendChart = echarts.init(document.getElementById('trendChart'));
      trendChart.setOption({
        tooltip: {trigger: 'axis'},
        xAxis: {type: 'category', data: ['待審核', '已批准', '已歸還', '逾期']},
        yAxis: {type: 'value'},
        series: [{
          name: '數量',
          type: 'bar',
          data: [stats.pendingBorrows, stats.approvedBorrows, stats.returnedBorrows, stats.overdueBorrows],
          itemStyle: {color: '#67C23A'}
        }]
      });
      const pieChart = echarts.init(document.getElementById('pieChart'));
      pieChart.setOption({
        tooltip: {trigger: 'item'},
        legend: {orient: 'vertical', left: 'left'},
        series: [{
          name: '借出狀態', type: 'pie', radius: '50%',
          data: [{value: stats.pendingBorrows, name: '待審核'}, {
            value: stats.approvedBorrows,
            name: '已批准'
          }, {value: stats.returnedBorrows, name: '已歸還'}, {value: stats.overdueBorrows, name: '逾期'}],
          emphasis: {itemStyle: {shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)'}}
        }]
      });
    }
  }
};
</script>

<style scoped>
/* 報表卡片 (Buttons) 調整 */
.report-card {
  cursor: pointer;
  text-align: center;
  padding: 6px 4px; /* 稍微增加內邊距，讓大字體不那麼擁擠 */
  transition: all 0.3s;
  border: 2px solid transparent;
  height: auto;
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

/* 圖示大小 */
.report-icon {
  font-size: 24px; /* 配合標題大小微調 */
  color: #909399;
  margin-bottom: 4px;
  transition: color 0.3s;
}

/* 標題字體 (大) */
.report-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 2px;
  transition: color 0.3s;
  line-height: 1.4;
}

/* 描述文字字體 (放大) */
.report-desc {
  font-size: 14px; /* 修改處：從 10px 放大至 14px */
  color: #606266; /* 顏色稍微加深一點點以利閱讀 */
  line-height: 1.3;
  /* 移除了 transform: scale(...) 以保持原字體大小 */
}

.report-content {
  margin-top: 20px;
}

/* 修改重點：
   增加 .stat-row 樣式，用於控制統計區域不換行
*/
.stat-row {
  flex-wrap: nowrap !important; /* 強制不換行 */
}

/* 覆蓋 el-col 的寬度限制，讓 flexbox 自動均分 */
.stat-row :deep(.el-col) {
  flex: 1; /* 自動填滿空間 */
  max-width: none; /* 移除最大寬度限制 (原本是 25%) */
}

/* 統計數字卡片 */
.stat-card {
  text-align: center;
  margin-bottom: 15px;
  transition: all 0.3s;
}

:deep(.stat-card .el-card__body) {
  padding: 10px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.report-table {
  width: 100% !important;
}

:deep(.el-table) {
  width: 100%;
}

:deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

/* 表格字體調整 */
:deep(.el-table td), :deep(.el-table th) {
  white-space: nowrap;
  padding: 8px 0; /* 稍微放寬行高 */
  font-size: 14px; /* 強制設定標準字體大小，防止被縮小 */
}

.stat-content {
  padding: 0;
}

/* 統計數字 (維持稍小於最初的樣子) */
.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #606266;
}

@media print {
  .el-button, .el-form, .el-pagination {
    display: none !important;
  }
}
</style>
