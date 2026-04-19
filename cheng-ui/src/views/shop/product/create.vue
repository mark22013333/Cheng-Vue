<template>
  <div class="app-container product-create-page">
    <!-- 頁面 Hero -->
    <div class="page-hero">
      <div class="page-hero__text">
        <div class="page-hero__crumbs">商城管理 / 商品 / 新增商品</div>
        <h1 class="page-hero__title">新增商品</h1>
        <p class="page-hero__subtitle">按步驟填寫商品資訊，最後一步儲存。</p>
      </div>
    </div>

    <!-- Steps 導航 -->
    <el-steps :active="currentStep" finish-status="success" class="create-steps">
      <el-step title="基本資訊" />
      <el-step title="規格與庫存" />
      <el-step title="定價與促銷" />
      <el-step title="詳情與發布" />
    </el-steps>

    <!-- 表單內容 -->
    <el-form ref="productFormRef" :model="form" :rules="formRules" label-width="100px" label-position="top">
      <el-card class="step-card" shadow="never">
        <!-- Step 0: 基本資訊 -->
        <div v-show="currentStep === 0">
          <ProductBasicSection :form="form" :category-options="categoryOptions" />
        </div>

        <!-- Step 1: 規格與庫存（不顯示價格） -->
        <div v-show="currentStep === 1">
          <ProductSkuSection
            :sku-list="skuList"
            :inv-item-options="invItemOptions"
            :inv-item-loading="invItemLoading"
            :sku-errors="skuValidationErrors"
            :show-price="false"
            @add-sku="addSku"
            @remove-sku="removeSku"
            @stock-mode-change="handleStockModeChange"
            @inv-item-change="handleInvItemChange"
            @unlink-inv-item="handleUnlinkInvItem"
            @search-inv-items="searchInvItems"
            @open-import-dialog="importDialogVisible = true"
          />
        </div>

        <!-- Step 2: 定價與促銷 -->
        <div v-show="currentStep === 2">
          <ProductPricingSection
            :form="form"
            :sku-list="skuList"
            :computed-min-price="computedMinPrice"
            :is-auto-price="isAutoPrice"
            :sku-errors="skuValidationErrors"
            :sale-price-summary="salePriceSummary"
            @toggle-auto-price="toggleAutoPrice"
          />
        </div>

        <!-- Step 3: 詳情與發布 -->
        <div v-show="currentStep === 3">
          <ProductDetailSection :form="form" />
        </div>
      </el-card>
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
        <span class="action-bar__hint">步驟 {{ currentStep + 1 }} / 4</span>
        <div class="action-bar__buttons">
          <el-button v-if="currentStep > 0" plain @click="prevStep">上一步</el-button>
          <el-button v-if="currentStep < 3" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-if="currentStep === 3" type="primary" :loading="loading" @click="handleSave">儲存商品</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="ShopProductCreate">
import { ref, onMounted, getCurrentInstance } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useProductForm } from './composables/useProductForm'
import ProductBasicSection from './components/ProductBasicSection.vue'
import ProductSkuSection from './components/ProductSkuSection.vue'
import ProductPricingSection from './components/ProductPricingSection.vue'
import ProductDetailSection from './components/ProductDetailSection.vue'
import InventoryImportDialog from './components/InventoryImportDialog.vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

const {
  form, skuList, categoryOptions, invItemOptions, invItemLoading,
  loading, skuValidationErrors, productFormRef, formRules,
  computedMinPrice, isAutoPrice, salePriceSummary, existingInvItemIds, isDirty,
  init, addSku, removeSku, handleStockModeChange, handleInvItemChange, handleUnlinkInvItem,
  searchInvItems, importFromInventory, toggleAutoPrice, saveProduct, validateStep
} = useProductForm(undefined, { proxy })  // 不傳 productId = 新增模式

const currentStep = ref(0)
const importDialogVisible = ref(false)

/** 下一步（先驗證當前步驟） */
async function nextStep() {
  const valid = await validateStep(currentStep.value)
  if (valid) currentStep.value++
}

/** 上一步 */
function prevStep() {
  if (currentStep.value > 0) currentStep.value--
}

/** 儲存商品 */
async function handleSave() {
  const savedId = await saveProduct()
  if (savedId) router.push('/cadm/shop/product/list')
}

/** 庫存匯入確認 */
function handleImportConfirm(items, options) {
  importFromInventory(items, options)
}

/** 離開頁面前確認未儲存變更 */
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
.product-create-page {
  padding-bottom: 96px;
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

/* ===== Steps 導航 ===== */
.create-steps {
  margin-bottom: 24px;
  padding: 0 40px;
}

/* ===== 步驟卡片 ===== */
.step-card {
  margin-bottom: 18px;
  border: 1px solid #e6ebf5;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: box-shadow 220ms ease, transform 220ms ease;
}
.step-card:hover {
  box-shadow: 0 8px 24px -16px rgba(15, 23, 42, 0.18);
}
.step-card :deep(.el-card__body) {
  padding: 24px 28px 8px;
}

/* ===== 表單微調 ===== */
.step-card :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
  padding-bottom: 6px;
}
.step-card :deep(.el-form--label-top .el-form-item__label) {
  line-height: 1.4;
}
.step-card :deep(.el-input__wrapper),
.step-card :deep(.el-input-number),
.step-card :deep(.el-textarea__inner) {
  border-radius: 8px;
}
.step-card :deep(.el-input__wrapper):hover {
  box-shadow: 0 0 0 1px #c0d4ef inset;
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
  .create-steps {
    padding: 0 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .step-card,
  .action-bar {
    transition: none;
  }
}
</style>
