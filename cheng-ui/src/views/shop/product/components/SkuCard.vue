<template>
  <div class="sku-card" :class="{ 'sku-card--disabled': sku.status === 'DISABLED' }">
    <!-- 卡片標題列 -->
    <div class="sku-card__header">
      <span class="sku-card__index">#{{ index + 1 }}</span>
      <span class="sku-card__name">{{ sku.skuName || '未命名規格' }}</span>
      <div v-if="!hideActions" class="sku-card__actions">
        <el-switch v-model="sku.status" active-value="ENABLED" inactive-value="DISABLED" size="small" />
        <el-button link type="danger" icon="Delete" size="small" @click="emit('remove')" />
      </div>
    </div>

    <!-- 卡片內容 -->
    <div class="sku-card__body">
      <!-- 庫存模式切換 -->
      <el-form-item label="庫存模式">
        <el-radio-group
          :model-value="sku.stockMode"
          size="small"
          @change="(val) => emit('stock-mode-change', val)"
        >
          <el-radio-button value="LINKED">關聯庫存</el-radio-button>
          <el-radio-button value="MANUAL">手動輸入</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- LINKED 模式：已關聯 → 唯讀資訊 + 解除關聯；未關聯 → 可搜尋下拉 -->
      <el-form-item v-if="sku.stockMode === 'LINKED'" label="關聯庫存物品" :error="fieldError('invItemId')">
        <div v-if="sku.invItemId" class="sku-card__linked-info">
          <div class="sku-card__linked-text">
            <el-icon class="sku-card__linked-icon"><Link /></el-icon>
            <span class="sku-card__linked-name">{{ sku.invItemName || '（未知名稱）' }}</span>
            <span v-if="sku.invItemCode" class="sku-card__linked-code">{{ sku.invItemCode }}</span>
          </div>
          <el-button
            link
            type="warning"
            size="small"
            @click="emit('unlink-inv-item')"
          >
            解除關聯
          </el-button>
        </div>
        <el-select
          v-else
          v-model="sku.invItemId"
          placeholder="輸入名稱、編碼或 #標籤搜尋"
          clearable
          filterable
          remote
          reserve-keyword
          :remote-method="(query) => emit('search-inv-items', query)"
          :loading="invItemLoading"
          style="width: 100%"
          popper-class="inv-item-select-popper"
          @change="(val) => emit('inv-item-change', val)"
        >
          <el-option
            v-for="item in invItemOptions"
            :key="item.itemId"
            :label="item.itemName"
            :value="item.itemId"
          >
            <div class="inv-item-option">
              <div class="inv-item-main">
                <span class="inv-item-name">{{ item.itemName }}</span>
                <span class="inv-item-meta">
                  <span v-if="item.itemCode" class="inv-item-code">{{ item.itemCode }}</span>
                  <span class="inv-item-stock">庫存: {{ item.currentStock ?? item.stockQuantity ?? 0 }}</span>
                </span>
              </div>
              <div v-if="item.tags && item.tags.length" class="inv-item-tags">
                <el-tag v-for="tag in item.tags.slice(0, 3)" :key="tag.tagId" size="small" type="info">
                  {{ tag.tagName }}
                </el-tag>
              </div>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- MANUAL 模式提示 -->
      <el-form-item v-else label="庫存來源">
        <span class="manual-mode-hint">
          <el-icon><InfoFilled /></el-icon>
          預購/叫貨模式，無需關聯庫存
        </span>
      </el-form-item>

      <!-- 規格名稱 + SKU 編碼 -->
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="規格名稱" :error="fieldError('skuName')">
            <el-input v-model="sku.skuName" placeholder="如：紅色/L" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="SKU 編碼" :error="fieldError('skuCode')">
            <el-input v-model="sku.skuCode" placeholder="選填" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 庫存 -->
      <el-form-item label="庫存數量" :error="fieldError('stockQuantity')">
        <el-input-number
          v-model="sku.stockQuantity"
          :min="0"
          controls-position="right"
          style="width: 160px"
          :disabled="sku.stockMode === 'LINKED' && sku.invItemId != null"
        />
        <span v-if="sku.stockMode === 'LINKED' && sku.invItemId" class="sku-card__hint">
          庫存由關聯的物品庫存同步
        </span>
      </el-form-item>

      <!-- 價格區塊（showPrice 時顯示）-->
      <template v-if="showPrice">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="售價" :error="fieldError('price')">
              <el-input-number
                v-model="sku.price"
                :min="0"
                :precision="0"
                controls-position="right"
                style="width: 100%"
                :class="{ 'sku-input--warn': isZeroPrice }"
              />
              <div v-if="suggestedPrice != null" class="sku-card__suggest">
                <el-button link type="primary" size="small" @click="applySuggestedPrice">
                  建議售價 ${{ suggestedPrice }}
                </el-button>
                <span class="sku-card__suggest-hint">（成本 × 1.2）</span>
              </div>
              <div v-else-if="sku.refPrice != null" class="sku-card__ref">參考: ${{ sku.refPrice }}</div>
              <div v-if="isZeroPrice" class="sku-card__warn">
                <el-icon><WarningFilled /></el-icon>
                0 元商品需於儲存時確認
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原價" :error="fieldError('originalPrice')">
              <el-input-number v-model="sku.originalPrice" :min="0" :precision="0" controls-position="right" style="width: 100%" placeholder="選填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="成本價" :error="fieldError('costPrice')">
              <el-input-number
                v-model="sku.costPrice"
                :min="0"
                :precision="0"
                controls-position="right"
                style="width: 100%"
                placeholder="選填"
                :class="{ 'sku-input--error': costExceedsPrice }"
              />
              <div v-if="sku.refCost != null" class="sku-card__ref">參考: ${{ sku.refCost }}</div>
              <div v-if="costExceedsPrice" class="sku-card__error">
                <el-icon><WarningFilled /></el-icon>
                成本價不可高於售價
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="特惠價" :error="fieldError('salePrice')">
              <el-input-number v-model="sku.salePrice" :min="0" :precision="0" controls-position="right" style="width: 100%" placeholder="選填，留空表示無特惠" />
              <div v-if="sku.salePrice > 0 && sku.price > 0 && sku.price > sku.salePrice" class="sku-card__ref sku-card__ref--sale">
                省 ${{ sku.price - sku.salePrice }}
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="特價結束時間" :error="fieldError('saleEndDate')">
              <el-date-picker
                v-model="sku.saleEndDate"
                type="datetime"
                placeholder="留空表示長期"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { InfoFilled, Link, WarningFilled } from '@element-plus/icons-vue'
import { recommendedPriceFromCost } from '../composables/priceSafeguard'

const props = defineProps({
  sku: { type: Object, required: true },
  index: { type: Number, required: true },
  invItemOptions: { type: Array, default: () => [] },
  invItemLoading: { type: Boolean, default: false },
  showPrice: { type: Boolean, default: false },
  errors: { type: Array, default: () => [] },
  hideActions: { type: Boolean, default: false }
})

const emit = defineEmits([
  'update:sku', 'remove', 'stock-mode-change', 'inv-item-change', 'search-inv-items', 'unlink-inv-item'
])

/** 取得特定欄位的錯誤訊息 */
function fieldError(fieldName) {
  return props.errors.find(e => e.field === fieldName)?.message
}

/** 售價為 0 或未填（顯示警告提示） */
const isZeroPrice = computed(() => {
  const p = props.sku.price
  return p !== undefined && p !== null && p !== '' && Number(p) <= 0
})

/** 成本價高於售價（顯示錯誤） */
const costExceedsPrice = computed(() => {
  const price = props.sku.price
  const cost = props.sku.costPrice
  if (cost == null || cost === '' || price == null || price === '') return false
  return Number(cost) > Number(price)
})

/** 系統推薦售價（售價為空或 0 且成本 > 0 時顯示） */
const suggestedPrice = computed(() => {
  const p = Number(props.sku.price) || 0
  const c = Number(props.sku.costPrice) || 0
  if (p > 0 || c <= 0) return null
  return recommendedPriceFromCost(props.sku.costPrice)
})

/** 套用推薦售價 */
function applySuggestedPrice() {
  const suggested = recommendedPriceFromCost(props.sku.costPrice)
  if (suggested != null) {
    props.sku.price = suggested
  }
}
</script>

<style scoped>
.sku-card {
  border: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px);
  background: var(--color-bg-1, #FFFFFF);
  margin-bottom: 12px;
  transition: box-shadow var(--transition-fast, 150ms ease-out);
}
.sku-card:hover {
  box-shadow: 0 2px 8px -2px rgba(15, 23, 42, 0.08);
}
.sku-card--disabled {
  opacity: 0.6;
}

/* === 標題列 === */
.sku-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--color-bg-2, #F7F8FA);
  border-bottom: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px) var(--radius-md, 10px) 0 0;
}
.sku-card__index {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary, #409EFF);
  background: var(--color-primary-lighter, #D9ECFF);
  padding: 2px 8px;
  border-radius: var(--radius-sm, 6px);
}
.sku-card__name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sku-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* === 內容 === */
.sku-card__body {
  padding: 16px;
}
.sku-card__body :deep(.el-form-item) {
  margin-bottom: 14px;
}
.sku-card__body :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
  font-size: 13px;
}
.sku-card__hint {
  font-size: 12px;
  color: var(--color-text-3, #86909C);
  margin-left: 8px;
}
.sku-card__ref {
  font-size: 11px;
  color: var(--color-text-4, #C9CDD4);
  margin-top: 2px;
}
.sku-card__ref--sale {
  color: var(--color-error, #F53F3F);
  font-weight: 500;
}
.sku-card__suggest {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}
.sku-card__suggest-hint {
  font-size: 11px;
  color: var(--color-text-4, #C9CDD4);
}
.sku-card__warn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  font-size: 11px;
  color: var(--color-warning, #FF7D00);
  font-weight: 500;
}
.sku-card__error {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  font-size: 11px;
  color: var(--color-error, #F53F3F);
  font-weight: 500;
}
.sku-input--warn :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--color-warning, #FF7D00) !important;
}
.sku-input--error :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--color-error, #F53F3F) !important;
}

/* === 庫存物品下拉選項 === */
.inv-item-option {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
  line-height: 1.5;
}
.inv-item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.inv-item-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.inv-item-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  flex-shrink: 0;
}
.inv-item-code {
  color: #606266;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
}
.inv-item-stock {
  color: #67c23a;
  font-weight: 500;
}
.inv-item-tags {
  display: flex;
  gap: 4px;
  margin-top: 4px;
  flex-wrap: wrap;
}
.manual-mode-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
}
.manual-mode-hint .el-icon {
  color: #e6a23c;
}

/* === 已關聯庫存唯讀區塊 === */
.sku-card__linked-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 8px 12px;
  background: #F0F9EB;
  border: 1px solid #D0E9C6;
  border-radius: var(--radius-sm, 6px);
}
.sku-card__linked-text {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}
.sku-card__linked-icon {
  color: var(--color-success, #67C23A);
  flex-shrink: 0;
}
.sku-card__linked-name {
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}
.sku-card__linked-code {
  font-size: 11px;
  color: var(--color-text-3, #86909C);
  background: #FFFFFF;
  padding: 1px 6px;
  border-radius: var(--radius-sm, 6px);
  flex-shrink: 0;
}
</style>
