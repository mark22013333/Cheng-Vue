<template>
  <div class="monthly-join-chart">
    <el-card shadow="hover">
      <template #header>
        <div class="chart-header">
          <span><el-icon><DataLine /></el-icon> 每月加入好友趨勢</span>
          <el-button
            type="text"
            :loading="loading"
            @click="loadData"
          >
            <el-icon><Refresh /></el-icon> 重新整理
          </el-button>
        </div>
      </template>
      <div v-loading="loading" class="chart-container">
        <div v-if="!hasData" class="no-data">
          <el-icon><Warning /></el-icon>
          <p>暫無資料</p>
        </div>
        <div v-else ref="chart" class="chart" style="width: 100%; height: 400px;"></div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getMonthlyStats } from '@/api/line/user'
import { DataLine, Refresh, Warning } from '@element-plus/icons-vue'

export default {
  name: 'MonthlyJoinChart',
  components: {
    DataLine, Refresh, Warning
  },
  props: {
    dateRange: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      loading: false,
      chart: null,
      chartData: []
    }
  },
  computed: {
    hasData() {
      return this.chartData && this.chartData.length > 0
    }
  },
  watch: {
    dateRange: {
      handler(newVal) {
        if (newVal && newVal.length === 2) {
          this.loadData()
        }
      },
      deep: true
    }
  },
  mounted() {
    if (this.dateRange && this.dateRange.length === 2) {
      this.loadData()
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    async loadData() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        return
      }

      this.loading = true
      try {
        const response = await getMonthlyStats(this.dateRange[0], this.dateRange[1])
        this.chartData = response.data || []
        this.$nextTick(() => {
          this.renderChart()
        })
      } catch (error) {
        console.error('載入每月統計資料失敗', error)
        this.$message.error('載入每月統計資料失敗')
      } finally {
        this.loading = false
      }
    },
    renderChart() {
      if (!this.hasData) {
        return
      }

      // 初始化圖表
      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chart)
      }

      // 準備資料
      const months = this.chartData.map(item => item.month)
      const counts = this.chartData.map(item => item.count)

      // 設定選項
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const data = params[0]
            return `${data.name}<br/>加入人數：<strong>${data.value}</strong> 人`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: months,
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '人數',
          minInterval: 1
        },
        series: [
          {
            name: '加入人數',
            type: 'bar',
            data: counts,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#2378f7' },
                  { offset: 0.7, color: '#2378f7' },
                  { offset: 1, color: '#83bff6' }
                ])
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}'
            }
          }
        ]
      }

      this.chart.setOption(option)

      // 監聽視窗大小變化
      window.addEventListener('resize', this.handleResize)
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.monthly-join-chart {
  margin-top: 20px;
  margin-bottom: 20px;

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    span {
      font-size: 16px;
      font-weight: 600;
      color: #303133;

      i {
        margin-right: 8px;
        color: #409EFF;
      }
    }
  }

  .chart-container {
    min-height: 400px;
    position: relative;

    .no-data {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      height: 400px;
      color: #909399;

      i {
        font-size: 64px;
        margin-bottom: 16px;
      }

      p {
        font-size: 14px;
        margin: 0;
      }
    }
  }
}
</style>
