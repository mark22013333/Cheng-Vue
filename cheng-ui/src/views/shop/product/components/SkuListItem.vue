<template>
  <div
    class="sku-list-item"
    :class="{
      'sku-list-item--active': active,
      'sku-list-item--error': hasError,
      'sku-list-item--disabled': sku.status === 'DISABLED'
    }"
    @click="$emit('click')"
  >
    <div class="sku-list-item__main">
      <div class="sku-list-item__row1">
        <span class="sku-list-item__index">#{{ index + 1 }}</span>
        <span class="sku-list-item__name" :title="sku.skuName || '未命名規格'">
          {{ sku.skuName || '未命名規格' }}
        </span>
        <el-icon v-if="hasError" class="sku-list-item__error-icon">
          <WarningFilled />
        </el-icon>
      </div>
      <div class="sku-list-item__row2">
        <span class="sku-list-item__price" :class="{ 'sku-list-item__price--zero': isZeroPrice }">
          ${{ sku.price ?? 0 }}
        </span>
        <el-tooltip v-if="isZeroPrice" content="此規格為 0 元商品" placement="top">
          <el-icon class="sku-list-item__zero-icon"><WarningFilled /></el-icon>
        </el-tooltip>
        <el-tooltip v-if="costExceedsPrice" content="成本價高於售價" placement="top">
          <el-icon class="sku-list-item__error-icon"><WarningFilled /></el-icon>
        </el-tooltip>
        <span v-if="sku.salePrice > 0" class="sku-list-item__sale-tag">特 ${{ sku.salePrice }}</span>
        <span v-if="sku.invItemId" class="sku-list-item__linked-tag">
          <el-icon><Link /></el-icon>
          庫存
        </span>
        <span class="sku-list-item__stock">庫存 {{ sku.stockQuantity ?? 0 }}</span>
      </div>
    </div>
    <div class="sku-list-item__actions" @click.stop>
      <el-switch
        v-model="sku.status"
        active-value="ENABLED"
        inactive-value="DISABLED"
        size="small"
      />
      <el-button
        link
        type="danger"
        size="small"
        @click="$emit('remove')"
      >
        <el-icon><Delete /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { WarningFilled, Link, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  sku: { type: Object, required: true },
  index: { type: Number, required: true },
  active: { type: Boolean, default: false },
  hasError: { type: Boolean, default: false }
})

defineEmits(['click', 'remove'])

const isZeroPrice = computed(() => {
  const p = props.sku.price
  return p !== undefined && p !== null && p !== '' && Number(p) <= 0
})

const costExceedsPrice = computed(() => {
  const price = props.sku.price
  const cost = props.sku.costPrice
  if (cost == null || cost === '' || price == null || price === '') return false
  return Number(cost) > Number(price)
})
</script>

<style scoped>
.sku-list-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border, #E5E6EB);
  cursor: pointer;
  background: var(--color-bg-2, #F7F8FA);
  transition: background var(--transition-fast, 150ms ease-out);
}
.sku-list-item:hover {
  background: var(--color-bg-3, #F2F3F5);
}
.sku-list-item--active {
  background: var(--color-primary-lighter, #D9ECFF);
  border-left: 3px solid var(--color-primary, #409EFF);
  padding-left: 9px;
}
.sku-list-item--active:hover {
  background: var(--color-primary-lighter, #D9ECFF);
}
.sku-list-item--error {
  border-left: 3px solid var(--color-error, #F53F3F);
  padding-left: 9px;
}
.sku-list-item--error.sku-list-item--active {
  background: #FFF0F0;
}
.sku-list-item--disabled {
  opacity: 0.6;
}

.sku-list-item__main {
  flex: 1;
  min-width: 0;
}
.sku-list-item__row1 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
}
.sku-list-item__index {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary, #409EFF);
  background: var(--color-bg-1, #FFFFFF);
  padding: 1px 6px;
  border-radius: var(--radius-sm, 6px);
  flex-shrink: 0;
}
.sku-list-item__name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}
.sku-list-item__error-icon {
  color: var(--color-error, #F53F3F);
  flex-shrink: 0;
}
.sku-list-item__row2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: var(--color-text-3, #86909C);
}
.sku-list-item__price {
  font-weight: 600;
  color: var(--color-primary, #409EFF);
}
.sku-list-item__price--zero {
  color: var(--color-warning, #FF7D00);
}
.sku-list-item__zero-icon {
  color: var(--color-warning, #FF7D00);
  flex-shrink: 0;
}
.sku-list-item__sale-tag {
  color: var(--color-error, #F53F3F);
  font-weight: 600;
}
.sku-list-item__linked-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--color-success, #67C23A);
}
.sku-list-item__stock {
  margin-left: auto;
}
.sku-list-item__actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
</style>
