<template>
  <div class="app-container product-edit-page">
    <!-- 頁面頂部 Hero -->
    <div class="page-hero">
      <div class="page-hero__text">
        <div class="page-hero__crumbs">商城管理 / 商品 / 編輯商品</div>
        <h1 class="page-hero__title">編輯商品</h1>
        <p class="page-hero__subtitle">設定商品基本資訊、圖片、規格與詳情，完成後點擊右下方「儲存商品」。</p>
      </div>
      <div class="page-hero__actions">
        <el-button size="small" @click="expandAll">全部展開</el-button>
        <el-button size="small" @click="collapseAll">全部收合</el-button>
        <el-button plain @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">儲存商品</el-button>
      </div>
    </div>

    <!-- 摘要列 -->
    <ProductSummaryBar :summary="summary" :has-unsaved-changes="isDirty" @scroll-to="scrollToSection" />

    <el-form ref="productFormRef" :model="form" :rules="formRules" label-width="100px" label-position="top">
      <!-- ❶ 基本資訊 -->
      <AccordionSection
        v-model:collapsed="sections.basic"
        title="❶ 基本資訊"
        description="商品在前台顯示的核心欄位"
        section-key="basic"
        :has-error="firstErrorSection === 'basic'"
        :is-dirty="sectionDirty.basic"
      >
        <template #summary>{{ summary.basic }}</template>
        <ProductBasicSection :form="form" :category-options="categoryOptions" />
      </AccordionSection>

      <!-- ❷ 規格與庫存 -->
      <AccordionSection
        v-model:collapsed="sections.sku"
        title="❷ 規格與庫存"
        description="SKU 規格與庫存設定"
        section-key="sku"
        :has-error="firstErrorSection === 'sku'"
        :is-dirty="sectionDirty.sku"
      >
        <template #summary>{{ summary.sku }}</template>
        <ProductSkuSection
          :sku-list="skuList"
          :inv-item-options="invItemOptions"
          :inv-item-loading="invItemLoading"
          :sku-errors="skuValidationErrors"
          :show-price="true"
          @add-sku="addSku"
          @remove-sku="removeSku"
          @stock-mode-change="handleStockModeChange"
          @inv-item-change="handleInvItemChange"
          @unlink-inv-item="handleUnlinkInvItem"
          @search-inv-items="searchInvItems"
          @open-import-dialog="importDialogVisible = true"
        />
      </AccordionSection>

      <!-- ❸ 定價與促銷 -->
      <AccordionSection
        v-model:collapsed="sections.pricing"
        title="❸ 定價與促銷"
        description="商品主價格、SKU 個別售價、促銷設定"
        section-key="pricing"
        :has-error="firstErrorSection === 'pricing'"
        :is-dirty="sectionDirty.pricing"
      >
        <template #summary>{{ summary.pricing }}</template>
        <ProductPricingSection
          :form="form"
          :sku-list="skuList"
          :computed-min-price="computedMinPrice"
          :is-auto-price="isAutoPrice"
          :sku-errors="skuValidationErrors"
          :sale-price-summary="salePriceSummary"
          @toggle-auto-price="toggleAutoPrice"
        />
      </AccordionSection>

      <!-- ❹ 詳情與展示 -->
      <AccordionSection
        v-model:collapsed="sections.detail"
        title="❹ 詳情與展示"
        description="富文字內容、展示旗標"
        section-key="detail"
        :has-error="firstErrorSection === 'detail'"
        :is-dirty="sectionDirty.detail"
      >
        <template #summary>{{ summary.detail }}</template>
        <ProductDetailSection :form="form" />
      </AccordionSection>
    </el-form>

    <!-- 庫存匯入 Dialog -->
    <InventoryImportDialog
      v-model:visible="importDialogVisible"
      :existing-inv-item-ids="existingInvItemIds"
      @confirm="handleImportConfirm"
    />

    <!-- 底部操作列 -->
    <div class="action-bar">
      <div class="action-bar__inner">
        <span class="action-bar__hint">
          <template v-if="isDirty">● 有未儲存的變更</template>
          <template v-else>所有變更需點擊「儲存商品」後才會生效</template>
        </span>
        <div class="action-bar__buttons">
          <el-button plain @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleSave">儲存商品</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="ShopProductEdit">
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useProductForm } from './composables/useProductForm'
import AccordionSection from './components/AccordionSection.vue'
import ProductBasicSection from './components/ProductBasicSection.vue'
import ProductDetailSection from './components/ProductDetailSection.vue'
import ProductSkuSection from './components/ProductSkuSection.vue'
import ProductPricingSection from './components/ProductPricingSection.vue'
import ProductSummaryBar from './components/ProductSummaryBar.vue'
import InventoryImportDialog from './components/InventoryImportDialog.vue'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.productId)

// composable — 核心狀態與方法
const {
  form, skuList, categoryOptions, invItemOptions, invItemLoading,
  loading, skuValidationErrors, firstErrorSection, productFormRef,
  computedMinPrice, isAutoPrice, salePriceSummary, existingInvItemIds,
  summary, isDirty, sectionDirty, formRules,
  init, addSku, removeSku, handleStockModeChange, handleInvItemChange, handleUnlinkInvItem,
  searchInvItems, importFromInventory, toggleAutoPrice, saveProduct, validate
} = useProductForm(productId, { proxy })

// Accordion 狀態（全部預設收合）
const sections = reactive({ basic: true, sku: true, pricing: true, detail: true })
const importDialogVisible = ref(false)

/** 全部展開 */
function expandAll() {
  Object.keys(sections).forEach(k => sections[k] = false)
}

/** 全部收合 */
function collapseAll() {
  Object.keys(sections).forEach(k => sections[k] = true)
}

/** 滾動到指定 section 並展開 */
function scrollToSection(sectionKey) {
  sections[sectionKey] = false
  nextTick(() => {
    const el = document.getElementById('section-' + sectionKey)
    if (el) {
      el.closest('.accordion-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  })
}

/** 儲存商品 — 驗證失敗時自動展開錯誤 section */
async function handleSave() {
  const valid = await validate()
  if (!valid) {
    const errorSection = firstErrorSection.value
    if (errorSection) scrollToSection(errorSection)
    return
  }
  const savedId = await saveProduct()
  if (savedId) handleCancel()
}

/** 庫存匯入確認回呼 */
function handleImportConfirm(items, options) {
  importFromInventory(items, options)
}

/** 取消 — 返回商品列表 */
function handleCancel() {
  router.push('/cadm/shop/product/list')
}

// 離開頁面確認（有未儲存變更時）
onBeforeRouteLeave((to, from, next) => {
  if (isDirty.value) {
    ElMessageBox.confirm('有未儲存的變更，確定要離開嗎？', '提示', {
      confirmButtonText: '離開',
      cancelButtonText: '留下',
      type: 'warning'
    }).then(() => next()).catch(() => next(false))
  } else {
    next()
  }
})

onMounted(() => {
  init()
})
</script>

<style scoped>
/* ===== 頁面整體 ===== */
.product-edit-page {
  padding-bottom: 96px; /* 預留底部固定操作列空間 */
  background: #f6f8fb;
  min-height: calc(100vh - 84px);
}

/* ===== 頁面標題 Hero ===== */
.page-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  margin-bottom: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f2f6fc 100%);
  border: 1px solid #e6ebf5;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.page-hero__crumbs {
  font-size: 12px;
  color: #94a3b8;
  letter-spacing: 0.3px;
  margin-bottom: 6px;
}
.page-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.5px;
}
.page-hero__subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}
.page-hero__actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

/* ===== 固定底部操作列 ===== */
.action-bar {
  position: fixed;
  left: var(--app-sidebar-width, 220px);
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(8px);
  border-top: 1px solid #e6ebf5;
  box-shadow: 0 -8px 24px -16px rgba(15, 23, 42, 0.18);
  z-index: 100;
  transition: left 200ms ease;
}
.action-bar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 32px;
  max-width: 1600px;
  margin: 0 auto;
}
.action-bar__hint {
  font-size: 12px;
  color: #94a3b8;
}
.action-bar__buttons {
  display: flex;
  gap: 10px;
}

/* ===== 響應式 ===== */
@media (max-width: 992px) {
  .action-bar {
    left: 0;
  }
  .page-hero {
    flex-direction: column;
  }
  .page-hero__actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (prefers-reduced-motion: reduce) {
  .action-bar {
    transition: none;
  }
}
</style>
