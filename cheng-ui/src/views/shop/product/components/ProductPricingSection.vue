<template>
  <div class="pricing-section">
    <!-- 商品主價格 -->
    <div class="pricing-section__main">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="商品價格" prop="price">
            <div class="pricing-price-wrapper">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="0"
                :disabled="isAutoPrice"
                style="width: 100%"
              />
              <el-tag v-if="isAutoPrice" type="info" size="small" class="pricing-price-tag">
                自動 = min(SKU 售價)
              </el-tag>
              <el-tag v-else type="warning" size="small" class="pricing-price-tag">
                手動覆寫
              </el-tag>
            </div>
            <div class="pricing-price-action">
              <el-button
                v-if="isAutoPrice"
                link
                type="primary"
                size="small"
                @click="emit('toggle-auto-price')"
              >
                切換為手動
              </el-button>
              <el-button
                v-else
                link
                type="primary"
                size="small"
                @click="emit('toggle-auto-price')"
              >
                恢復自動
              </el-button>
              <span v-if="computedMinPrice != null" class="pricing-min-hint">
                SKU 最低價: ${{ computedMinPrice }}
              </span>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="原價" prop="originalPrice">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <!-- 促銷設定（SKU 級） -->
    <div class="pricing-section__promo">
      <h4 class="pricing-section__subtitle">促銷設定</h4>
      <div v-if="salePriceSummary" class="promo-summary">
        <el-tag type="danger" size="small">SKU 特惠啟用中</el-tag>
        <span class="promo-summary__text">{{ salePriceSummary }}</span>
      </div>
      <div v-else class="promo-summary promo-summary--empty">
        尚未設定 SKU 特惠價，請在「規格與庫存」中為各規格設定特惠價
      </div>
    </div>

    <!-- SKU 個別定價表格 -->
    <div v-if="skuList.length > 0" class="pricing-section__sku-prices">
      <h4 class="pricing-section__subtitle">SKU 個別售價</h4>
      <el-table :data="skuList" border size="small" max-height="300">
        <el-table-column prop="skuName" label="規格名稱" min-width="140">
          <template #default="{ row }">
            {{ row.skuName || '未命名' }}
          </template>
        </el-table-column>
        <el-table-column label="參考價" width="90" align="right">
          <template #default="{ row }">
            <span class="ref-price">{{ row.refPrice != null ? '$' + row.refPrice : '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="售價" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.price"
              :min="0"
              :precision="0"
              size="small"
              controls-position="right"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column prop="salePrice" label="特惠價" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.salePrice > 0" class="price-sale">${{ row.salePrice }}</span>
            <span v-else class="price-empty">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原價" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.originalPrice"
              :min="0"
              :precision="0"
              size="small"
              controls-position="right"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column prop="costPrice" label="成本價" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.costPrice"
              :min="0"
              :precision="0"
              size="small"
              controls-position="right"
              style="width: 100%"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true },
  skuList: { type: Array, required: true },
  computedMinPrice: { type: Number, default: null },
  isAutoPrice: { type: Boolean, default: true },
  skuErrors: { type: Array, default: () => [] },
  salePriceSummary: { type: String, default: '' }
})

const emit = defineEmits(['update:form', 'update:skuList', 'toggle-auto-price'])
</script>

<style scoped>
.pricing-section__subtitle {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
}
.pricing-section__promo {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #E5E6EB);
}
.pricing-section__sku-prices {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #E5E6EB);
}

/* === 主價格 === */
.pricing-price-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pricing-price-tag {
  flex-shrink: 0;
}
.pricing-price-action {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}
.pricing-min-hint {
  font-size: 12px;
  color: var(--color-text-3, #86909C);
}

/* === 促銷摘要 === */
.promo-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #FEF0F0;
  border-radius: var(--radius-sm, 6px);
}
.promo-summary--empty {
  background: var(--color-bg-2, #F7F8FA);
  color: var(--color-text-3, #86909C);
  font-size: 13px;
}
.promo-summary__text {
  font-size: 13px;
  color: var(--color-text-2, #4E5969);
}

/* === SKU 表格價格 === */
.price-sale {
  color: var(--color-error, #F53F3F);
  font-weight: 600;
}
.price-empty {
  color: var(--color-text-4, #C9CDD4);
}

/* === 參考價 === */
.ref-price {
  font-size: 12px;
  color: var(--color-text-4, #C9CDD4);
}

/* === 表單微調 === */
.pricing-section :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
  padding-bottom: 6px;
}
</style>
