<template>
  <div class="summary-bar">
    <!-- 標題列 -->
    <div class="summary-bar__header">
      <span class="summary-bar__title">商品摘要</span>
      <span v-if="hasUnsavedChanges" class="summary-bar__unsaved">
        <span class="summary-bar__dot" />
        有未儲存的變更
      </span>
    </div>

    <!-- 摘要內容 -->
    <div class="summary-bar__rows">
      <!-- 價格行 -->
      <div class="summary-bar__row" @click="$emit('scroll-to', 'pricing')">
        <span class="summary-bar__icon">💰</span>
        <span class="summary-bar__text">{{ summary.pricing || '尚未設定價格' }}</span>
      </div>
      <!-- 規格庫存行 -->
      <div class="summary-bar__row" @click="$emit('scroll-to', 'sku')">
        <span class="summary-bar__icon">📦</span>
        <span class="summary-bar__text">{{ summary.sku || '尚未建立規格' }}</span>
      </div>
      <!-- 分類旗標行 -->
      <div class="summary-bar__row" @click="$emit('scroll-to', 'detail')">
        <span class="summary-bar__icon">🏷️</span>
        <span class="summary-bar__text">{{ summary.detail || '尚未設定旗標' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  /** @type {import('../composables/useProductForm').SummaryData} */
  summary: { type: Object, default: () => ({ basic: '', sku: '', pricing: '', detail: '' }) },
  hasUnsavedChanges: { type: Boolean, default: false }
})

defineEmits(['scroll-to'])
</script>

<style scoped>
.summary-bar {
  background: linear-gradient(135deg, var(--color-bg-2, #F7F8FA) 0%, #EDF2FC 100%);
  border: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px);
  padding: var(--sp-md, 16px);
  margin-bottom: var(--sp-md, 16px);
}

/* === 標題列 === */
.summary-bar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.summary-bar__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-2, #4E5969);
}
.summary-bar__unsaved {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-primary, #409EFF);
  font-weight: 500;
}
.summary-bar__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary, #409EFF);
  animation: pulse 2s ease-in-out infinite;
}

/* === 摘要行 === */
.summary-bar__rows {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.summary-bar__row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: var(--radius-sm, 6px);
  cursor: pointer;
  transition: background var(--transition-fast, 150ms ease-out);
}
.summary-bar__row:hover {
  background: rgba(64, 158, 255, 0.08);
}
.summary-bar__icon {
  flex-shrink: 0;
  font-size: 14px;
  line-height: 1;
}
.summary-bar__text {
  font-size: 13px;
  color: var(--color-text-2, #4E5969);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* === 動畫 === */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

@media (prefers-reduced-motion: reduce) {
  .summary-bar__dot {
    animation: none;
  }
  .summary-bar__row {
    transition: none;
  }
}
</style>
