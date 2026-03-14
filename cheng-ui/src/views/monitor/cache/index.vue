<template>
  <div class="app-container">
    <!-- Redis 概覽統計卡 -->
    <el-row :gutter="16" class="mb16" v-if="cache.info">
      <el-col :span="6">
        <div class="stat-card stat-card--blue">
          <div class="stat-card__icon">
            <Monitor style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ cache.info.redis_version }}</div>
            <div class="stat-card__label">Redis 版本</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--green">
          <div class="stat-card__icon">
            <Odometer style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ cache.info.used_memory_human }}</div>
            <div class="stat-card__label">使用記憶體</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--violet">
          <div class="stat-card__icon">
            <Key style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ cache.dbSize }}</div>
            <div class="stat-card__label">Key 數量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--amber">
          <div class="stat-card__icon">
            <Connection style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ cache.info.connected_clients }}</div>
            <div class="stat-card__label">客戶端連線數</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 基本訊息 -->
    <div class="monitor-panel mb16">
      <div class="monitor-panel__header">
        <div class="monitor-panel__title">
          <Monitor style="width: 16px; height: 16px;" />
          <span>基本訊息</span>
        </div>
      </div>
      <div class="monitor-panel__body" v-if="cache.info">
        <dl class="desc-list desc-list--3col">
          <div class="desc-list__item">
            <dt>執行模式</dt>
            <dd>{{ cache.info.redis_mode == "standalone" ? "單機" : "集群" }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>埠號</dt>
            <dd>{{ cache.info.tcp_port }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>執行時間(天)</dt>
            <dd>{{ cache.info.uptime_in_days }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>使用 CPU</dt>
            <dd>{{ parseFloat(cache.info.used_cpu_user_children).toFixed(2) }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>記憶體配置</dt>
            <dd>{{ cache.info.maxmemory_human }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>網路 I/O</dt>
            <dd>{{ cache.info.instantaneous_input_kbps }}kps / {{ cache.info.instantaneous_output_kbps }}kps</dd>
          </div>
          <div class="desc-list__item">
            <dt>AOF 開啟</dt>
            <dd>{{ cache.info.aof_enabled == "0" ? "否" : "是" }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>RDB 狀態</dt>
            <dd>{{ cache.info.rdb_last_bgsave_status }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- 圖表 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <PieChart style="width: 16px; height: 16px;" />
              <span>命令統計</span>
            </div>
          </div>
          <div class="monitor-panel__body">
            <div ref="commandstats" style="height: 420px" />
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <Odometer style="width: 16px; height: 16px;" />
              <span>記憶體訊息</span>
            </div>
          </div>
          <div class="monitor-panel__body">
            <div ref="usedmemory" style="height: 420px" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Cache">
import { getCache } from '@/api/monitor/cache'
import * as echarts from 'echarts'

const cache = ref([])
const commandstats = ref(null)
const usedmemory = ref(null)
const { proxy } = getCurrentInstance()

function getList() {
  proxy.$modal.loading("正在載入暫存監控資料，請稍候！")
  getCache().then(response => {
    proxy.$modal.closeLoading()
    cache.value = response.data

    const commandstatsIntance = echarts.init(commandstats.value, "macarons")
    commandstatsIntance.setOption({
      tooltip: {
        trigger: "item",
        formatter: "{a} <br/>{b} : {c} ({d}%)"
      },
      series: [
        {
          name: "命令",
          type: "pie",
          roseType: "radius",
          radius: [15, 95],
          center: ["50%", "38%"],
          data: response.data.commandStats,
          animationEasing: "cubicInOut",
          animationDuration: 1000
        }
      ]
    })
    const usedmemoryInstance = echarts.init(usedmemory.value, "macarons")
    usedmemoryInstance.setOption({
      tooltip: {
        formatter: "{b} <br/>{a} : " + cache.value.info.used_memory_human
      },
      series: [
        {
          name: "峰值",
          type: "gauge",
          min: 0,
          max: 1000,
          detail: {
            formatter: cache.value.info.used_memory_human
          },
          data: [
            {
              value: parseFloat(cache.value.info.used_memory_human),
              name: "記憶體消耗"
            }
          ]
        }
      ]
    })
    window.addEventListener("resize", () => {
      commandstatsIntance.resize()
      usedmemoryInstance.resize()
    })
  })
}

getList()
</script>

<style lang="scss" scoped>
.mb16 { margin-bottom: 16px; }

/* ====== 統計卡 ====== */
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  background: var(--el-bg-color, #ffffff);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__info { flex: 1; min-width: 0; }

  &__value {
    font-size: 22px;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.02em;
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    font-weight: 500;
    color: var(--el-text-color-secondary, #6b7280);
    margin-top: 2px;
  }

  &--blue {
    .stat-card__icon { background: #EFF6FF; color: #3B82F6; }
    .stat-card__value { color: #3B82F6; }
  }
  &--green {
    .stat-card__icon { background: #ECFDF5; color: #10B981; }
    .stat-card__value { color: #10B981; }
  }
  &--violet {
    .stat-card__icon { background: #F5F3FF; color: #8B5CF6; }
    .stat-card__value { color: #8B5CF6; }
  }
  &--amber {
    .stat-card__icon { background: #FFFBEB; color: #F59E0B; }
    .stat-card__value { color: #F59E0B; }
  }
}

/* ====== 監控面板 ====== */
.monitor-panel {
  background: var(--el-bg-color, #ffffff);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 24px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__body {
    padding: 20px 24px;
  }
}

/* ====== 描述列表 ====== */
.desc-list {
  margin: 0;
  padding: 0;

  &__item {
    display: flex;
    align-items: baseline;
    padding: 10px 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);

    &:last-child { border-bottom: none; }

    dt {
      width: 110px;
      flex-shrink: 0;
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-secondary, #6b7280);
    }

    dd {
      flex: 1;
      margin: 0;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  &--3col {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 0 32px;
  }
}

/* ====== 暗色模式 ====== */
html.dark {
  .stat-card {
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: none;

    &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4); }

    &--blue .stat-card__icon { background: rgba(59, 130, 246, 0.1); }
    &--green .stat-card__icon { background: rgba(16, 185, 129, 0.1); }
    &--violet .stat-card__icon { background: rgba(139, 92, 246, 0.1); }
    &--amber .stat-card__icon { background: rgba(245, 158, 11, 0.1); }
  }

  .monitor-panel {
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: none;

    &__header { border-bottom-color: rgba(255, 255, 255, 0.06); }
  }

  .desc-list__item {
    border-bottom-color: rgba(255, 255, 255, 0.04);
  }
}

/* ====== RWD ====== */
@media (max-width: 768px) {
  .stat-card {
    padding: 14px;
    &__value { font-size: 18px; }
    &__icon { width: 36px; height: 36px; }
  }

  .desc-list--3col {
    grid-template-columns: 1fr;
  }
}
</style>
