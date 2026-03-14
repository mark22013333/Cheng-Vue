<template>
  <div class="app-container">
    <!-- CPU & 記憶體 概覽卡片 -->
    <el-row :gutter="16" class="mb16">
      <el-col :span="6">
        <div class="stat-card stat-card--blue">
          <div class="stat-card__icon">
            <Cpu style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ server.cpu ? server.cpu.used + '%' : '--' }}</div>
            <div class="stat-card__label">CPU 使用率</div>
          </div>
          <div class="stat-card__ring">
            <svg viewBox="0 0 36 36">
              <path class="ring-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="ring-fill" :stroke-dasharray="cpuPercent + ', 100'" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--green">
          <div class="stat-card__icon">
            <Tickets style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ server.mem ? server.mem.usage + '%' : '--' }}</div>
            <div class="stat-card__label">記憶體使用率</div>
          </div>
          <div class="stat-card__ring">
            <svg viewBox="0 0 36 36">
              <path class="ring-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="ring-fill" :stroke-dasharray="memPercent + ', 100'" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--violet">
          <div class="stat-card__icon">
            <CoffeeCup style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ server.jvm ? server.jvm.usage + '%' : '--' }}</div>
            <div class="stat-card__label">JVM 使用率</div>
          </div>
          <div class="stat-card__ring">
            <svg viewBox="0 0 36 36">
              <path class="ring-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="ring-fill" :stroke-dasharray="jvmPercent + ', 100'" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--amber">
          <div class="stat-card__icon">
            <Cpu style="width: 22px; height: 22px;" />
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ server.cpu ? server.cpu.cpuNum : '--' }}</div>
            <div class="stat-card__label">CPU 核心數</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- CPU & 記憶體 詳細 -->
    <el-row :gutter="16" class="mb16">
      <el-col :span="12">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <Cpu style="width: 16px; height: 16px;" />
              <span>CPU</span>
            </div>
          </div>
          <div class="monitor-panel__body">
            <dl class="desc-list">
              <div class="desc-list__item">
                <dt>核心數</dt>
                <dd>{{ server.cpu ? server.cpu.cpuNum : '--' }}</dd>
              </div>
              <div class="desc-list__item">
                <dt>使用者使用率</dt>
                <dd>{{ server.cpu ? server.cpu.used + '%' : '--' }}</dd>
              </div>
              <div class="desc-list__item">
                <dt>系統使用率</dt>
                <dd>{{ server.cpu ? server.cpu.sys + '%' : '--' }}</dd>
              </div>
              <div class="desc-list__item">
                <dt>目前閒置率</dt>
                <dd>{{ server.cpu ? server.cpu.free + '%' : '--' }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <Tickets style="width: 16px; height: 16px;" />
              <span>記憶體</span>
            </div>
          </div>
          <div class="monitor-panel__body">
            <div class="mem-grid">
              <div class="mem-grid__header">
                <span></span>
                <span>記憶體</span>
                <span>JVM</span>
              </div>
              <div class="mem-grid__row">
                <span class="mem-grid__label">總量</span>
                <span>{{ server.mem ? server.mem.total + 'G' : '--' }}</span>
                <span>{{ server.jvm ? server.jvm.total + 'M' : '--' }}</span>
              </div>
              <div class="mem-grid__row">
                <span class="mem-grid__label">已用</span>
                <span>{{ server.mem ? server.mem.used + 'G' : '--' }}</span>
                <span>{{ server.jvm ? server.jvm.used + 'M' : '--' }}</span>
              </div>
              <div class="mem-grid__row">
                <span class="mem-grid__label">剩餘</span>
                <span>{{ server.mem ? server.mem.free + 'G' : '--' }}</span>
                <span>{{ server.jvm ? server.jvm.free + 'M' : '--' }}</span>
              </div>
              <div class="mem-grid__row">
                <span class="mem-grid__label">使用率</span>
                <span :class="{'text-danger': server.mem && server.mem.usage > 80}">{{ server.mem ? server.mem.usage + '%' : '--' }}</span>
                <span :class="{'text-danger': server.jvm && server.jvm.usage > 80}">{{ server.jvm ? server.jvm.usage + '%' : '--' }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 伺服器訊息 -->
    <div class="monitor-panel mb16">
      <div class="monitor-panel__header">
        <div class="monitor-panel__title">
          <Monitor style="width: 16px; height: 16px;" />
          <span>伺服器訊息</span>
        </div>
      </div>
      <div class="monitor-panel__body">
        <dl class="desc-list desc-list--2col">
          <div class="desc-list__item">
            <dt>伺服器名稱</dt>
            <dd>{{ server.sys ? server.sys.computerName : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>作業系統</dt>
            <dd>{{ server.sys ? server.sys.osName : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>伺服器IP</dt>
            <dd>{{ server.sys ? server.sys.computerIp : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>系統架構</dt>
            <dd>{{ server.sys ? server.sys.osArch : '--' }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- Java 虛擬機訊息 -->
    <div class="monitor-panel mb16">
      <div class="monitor-panel__header">
        <div class="monitor-panel__title">
          <CoffeeCup style="width: 16px; height: 16px;" />
          <span>Java 虛擬機訊息</span>
        </div>
      </div>
      <div class="monitor-panel__body">
        <dl class="desc-list desc-list--2col">
          <div class="desc-list__item">
            <dt>Java 名稱</dt>
            <dd>{{ server.jvm ? server.jvm.name : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>Java 版本</dt>
            <dd>{{ server.jvm ? server.jvm.version : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>啟動時間</dt>
            <dd>{{ server.jvm ? server.jvm.startTime : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>執行時間</dt>
            <dd>{{ server.jvm ? server.jvm.runTime : '--' }}</dd>
          </div>
        </dl>
        <dl class="desc-list desc-list--full">
          <div class="desc-list__item">
            <dt>安裝路徑</dt>
            <dd class="desc-list__mono">{{ server.jvm ? server.jvm.home : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>專案路徑</dt>
            <dd class="desc-list__mono">{{ server.sys ? server.sys.userDir : '--' }}</dd>
          </div>
          <div class="desc-list__item">
            <dt>執行參數</dt>
            <dd class="desc-list__mono">{{ server.jvm ? server.jvm.inputArgs : '--' }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- 磁碟狀態 -->
    <div class="monitor-panel">
      <div class="monitor-panel__header">
        <div class="monitor-panel__title">
          <MessageBox style="width: 16px; height: 16px;" />
          <span>磁碟狀態</span>
        </div>
      </div>
      <div class="monitor-panel__body" style="padding: 0;">
        <el-table :data="server.sysFiles" class="custom-table panel-table">
          <el-table-column label="磁碟路徑" align="center" prop="dirName" />
          <el-table-column label="文件系統" align="center" prop="sysTypeName" />
          <el-table-column label="類型" align="center" prop="typeName" />
          <el-table-column label="總大小" align="center" prop="total" />
          <el-table-column label="可用" align="center" prop="free" />
          <el-table-column label="已用" align="center" prop="used" />
          <el-table-column label="使用率" align="center">
            <template #default="scope">
              <span :class="{'text-danger': scope.row.usage > 80}">{{ scope.row.usage }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getServer } from '@/api/monitor/server'

const server = ref([])
const { proxy } = getCurrentInstance()

const cpuPercent = computed(() => server.value.cpu ? server.value.cpu.used : 0)
const memPercent = computed(() => server.value.mem ? server.value.mem.usage : 0)
const jvmPercent = computed(() => server.value.jvm ? server.value.jvm.usage : 0)

function getList() {
  proxy.$modal.loading("正在載入服務監控資料，請稍候！")
  getServer().then(response => {
    server.value = response.data
    proxy.$modal.closeLoading()
  })
}

getList()
</script>

<style lang="scss" scoped>
.mb16 { margin-bottom: 16px; }

/* ====== 概覽統計卡 ====== */
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

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 24px;
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

  &__ring {
    width: 44px;
    height: 44px;
    flex-shrink: 0;

    svg {
      width: 100%;
      height: 100%;
      transform: rotate(-90deg);
    }

    .ring-bg {
      fill: none;
      stroke: rgba(0, 0, 0, 0.06);
      stroke-width: 3;
    }

    .ring-fill {
      fill: none;
      stroke-width: 3;
      stroke-linecap: round;
      transition: stroke-dasharray 0.6s ease;
    }
  }

  /* 主題色 */
  &--blue {
    .stat-card__icon { background: #EFF6FF; color: #3B82F6; }
    .stat-card__value { color: #3B82F6; }
    .ring-fill { stroke: #3B82F6; }
  }
  &--green {
    .stat-card__icon { background: #ECFDF5; color: #10B981; }
    .stat-card__value { color: #10B981; }
    .ring-fill { stroke: #10B981; }
  }
  &--violet {
    .stat-card__icon { background: #F5F3FF; color: #8B5CF6; }
    .stat-card__value { color: #8B5CF6; }
    .ring-fill { stroke: #8B5CF6; }
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
      width: 120px;
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

  &--2col {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0 32px;
  }

  &--full {
    margin-top: 4px;
  }

  &__mono {
    font-family: 'SF Mono', 'Fira Code', monospace;
    font-size: 13px !important;
    word-break: break-all;
    color: var(--el-text-color-regular) !important;
  }
}

/* ====== 記憶體網格 ====== */
.mem-grid {
  &__header {
    display: grid;
    grid-template-columns: 100px 1fr 1fr;
    gap: 12px;
    padding: 8px 0 12px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);

    span {
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-secondary, #64748b);
      letter-spacing: 0.01em;
    }
  }

  &__row {
    display: grid;
    grid-template-columns: 100px 1fr 1fr;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    font-size: 14px;

    &:last-child { border-bottom: none; }
  }

  &__label {
    font-size: 13px;
    font-weight: 500;
    color: var(--el-text-color-secondary, #6b7280);
  }
}

/* 面板內嵌表格 */
:deep(.panel-table) {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

/* ====== 暗色模式 ====== */
html.dark {
  .stat-card {
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: none;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
    }

    &--blue {
      .stat-card__icon { background: rgba(59, 130, 246, 0.1); }
    }
    &--green {
      .stat-card__icon { background: rgba(16, 185, 129, 0.1); }
    }
    &--violet {
      .stat-card__icon { background: rgba(139, 92, 246, 0.1); }
    }
    &--amber {
      .stat-card__icon { background: rgba(245, 158, 11, 0.1); }
    }

    .ring-bg { stroke: rgba(255, 255, 255, 0.08); }
  }

  .monitor-panel {
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: none;

    &__header {
      border-bottom-color: rgba(255, 255, 255, 0.06);
    }
  }

  .desc-list__item {
    border-bottom-color: rgba(255, 255, 255, 0.04);
  }

  .mem-grid {
    &__header { border-bottom-color: rgba(255, 255, 255, 0.06); }
    &__row { border-bottom-color: rgba(255, 255, 255, 0.04); }
  }
}

/* ====== RWD ====== */
@media (max-width: 768px) {
  .stat-card {
    padding: 14px;

    &__value { font-size: 20px; }
    &__ring { width: 36px; height: 36px; }
    &__icon { width: 36px; height: 36px; }
  }

  .desc-list--2col {
    grid-template-columns: 1fr;
  }
}
</style>
