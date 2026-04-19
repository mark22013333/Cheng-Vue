# Spec — Accordion 動畫與 CSS

## 動畫方案

**不使用** Element Plus 的 `<el-collapse>` 元件（互斥手風琴，一次只能開一個）。
**使用** Element Plus 的 `<el-collapse-transition>` 元件 + 自訂 CSS。

`<el-collapse-transition>` 是純動畫元件，內部用 JS 計算 `scrollHeight` 做高度動畫，不綁定手風琴邏輯。

### el-collapse-transition 工作原理

```
v-show="true"（展開）：
  1. beforeEnter: height=0, overflow=hidden
  2. enter:       height=scrollHeight（JS 計算實際內容高度）
  3. afterEnter:  height=auto, overflow=visible

v-show="false"（收合）：
  1. beforeLeave: height=scrollHeight, overflow=hidden
  2. leave:       height=0
  3. afterLeave:  display=none
```

不需要手寫 JS 計算高度。

---

## AccordionSection.vue 元件

### Template

```vue
<template>
  <div
    class="accordion-section"
    :class="{
      'is-collapsed': collapsed,
      'has-error': hasError,
      'has-changes': isDirty
    }"
  >
    <!-- 標題列（永遠可見）-->
    <div
      class="accordion-section__header"
      role="button"
      :aria-expanded="!collapsed"
      :aria-controls="'section-' + sectionKey"
      tabindex="0"
      @click="toggle"
      @keydown.enter.space.prevent="toggle"
    >
      <span class="accordion-section__bar" />

      <div class="accordion-section__heading">
        <h3 class="accordion-section__title">
          <span
            v-if="hasError"
            class="accordion-section__indicator accordion-section__indicator--error"
          />
          <span
            v-else-if="isDirty"
            class="accordion-section__indicator accordion-section__indicator--dirty"
          />
          {{ title }}
        </h3>

        <span v-if="collapsed" class="accordion-section__summary">
          <slot name="summary" />
        </span>
        <span v-else class="accordion-section__desc">
          {{ description }}
        </span>
      </div>

      <el-icon class="accordion-section__chevron">
        <ArrowRight />
      </el-icon>
    </div>

    <!-- 內容區（可展開/收合）-->
    <el-collapse-transition>
      <div
        v-show="!collapsed"
        :id="'section-' + sectionKey"
        class="accordion-section__body"
      >
        <slot />
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  collapsed: { type: Boolean, default: false },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  sectionKey: { type: String, required: true },
  hasError: { type: Boolean, default: false },
  isDirty: { type: Boolean, default: false }
})

const emit = defineEmits(['update:collapsed'])

function toggle() {
  emit('update:collapsed', !props.collapsed)
}
</script>
```

---

## 完整 CSS

所有數值嚴格遵循 `DESIGN.md`。

```css
/* ================================================================
   Accordion Section — 通用折疊區塊
   
   DESIGN.md 對照：
   - radius: md(10px) → var(--radius-md)
   - spacing: comfortable density, md(16px) → var(--sp-md)
   - motion: medium(250ms) → var(--transition-normal)
   - color: warm gray neutrals → var(--color-*)
   ================================================================ */

/* ----- 容器 ----- */
.accordion-section {
  margin-bottom: var(--sp-md);               /* 16px */
  border: 1px solid var(--color-border);     /* #E5E6EB */
  border-radius: var(--radius-md);           /* 10px */
  background: var(--color-bg-1);             /* #FFFFFF */
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: box-shadow 220ms ease, border-color var(--transition-fast);
  overflow: hidden;
}

.accordion-section:hover {
  box-shadow: 0 4px 12px -4px rgba(15, 23, 42, 0.1);
}

/* 錯誤狀態 */
.accordion-section.has-error {
  border-color: var(--color-error);          /* #F53F3F */
}

.accordion-section.has-error .accordion-section__bar {
  background: linear-gradient(180deg, var(--color-error) 0%, #FF7875 100%);
}

/* ----- 標題列 ----- */
.accordion-section__header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 24px;
  cursor: pointer;
  user-select: none;
  background: var(--color-bg-2);             /* #F7F8FA */
  transition: background var(--transition-fast); /* 150ms ease-out */
}

.accordion-section__header:hover {
  background: var(--color-bg-3);             /* #F2F3F5 */
}

.accordion-section__header:focus-visible {
  outline: 2px solid var(--color-primary);   /* #409EFF */
  outline-offset: -2px;
  border-radius: var(--radius-md);
}

/* 展開時底部分隔線 */
.accordion-section:not(.is-collapsed) .accordion-section__header {
  border-bottom: 1px solid var(--color-border);
}

/* ----- 左側色條 ----- */
.accordion-section__bar {
  width: 4px;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  flex-shrink: 0;
  transition: background var(--transition-fast);
}

/* ----- 標題文字區 ----- */
.accordion-section__heading {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;                              /* 允許文字截斷 */
}

.accordion-section__title {
  margin: 0;
  font-size: 16px;                           /* md */
  font-weight: 600;
  color: var(--color-text-1);                /* #1D2129 */
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
  gap: var(--sp-sm);                         /* 8px */
}

.accordion-section__summary {
  font-size: 13px;                           /* sm */
  color: var(--color-text-3);                /* #86909C */
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.accordion-section__desc {
  font-size: 12px;                           /* xs */
  color: var(--color-text-3);                /* #86909C */
}

/* ----- 狀態指示器（圓點）----- */
.accordion-section__indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 紅點 = 驗證錯誤 */
.accordion-section__indicator--error {
  background: var(--color-error);            /* #F53F3F */
  box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.2);
  animation: pulse-error 1.5s ease-in-out infinite;
}

/* 藍點 = 未儲存變更 */
.accordion-section__indicator--dirty {
  background: var(--color-primary);          /* #409EFF */
}

@keyframes pulse-error {
  0%, 100% { box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.2); }
  50%      { box-shadow: 0 0 0 6px rgba(245, 63, 63, 0.1); }
}

/* ----- 箭頭 ----- */
.accordion-section__chevron {
  font-size: 16px;
  color: var(--color-text-4);                /* #C9CDD4 */
  flex-shrink: 0;
  transition: transform var(--transition-normal); /* 250ms ease-out */
}

/* 展開時旋轉 90° */
.accordion-section:not(.is-collapsed) .accordion-section__chevron {
  transform: rotate(90deg);
}

/* ----- 內容區 ----- */
.accordion-section__body {
  padding: 24px 28px 8px;
}

/* ----- el-collapse-transition duration 覆寫 -----
   Element Plus 源碼硬編碼 0.3s，覆寫為 DESIGN.md 的 250ms。
*/
.accordion-section .el-collapse-transition {
  transition-duration: 250ms !important;
  transition-timing-function: ease-out !important;
}

/* ================================================================
   Reduced Motion
   ================================================================ */
@media (prefers-reduced-motion: reduce) {
  .accordion-section,
  .accordion-section__header,
  .accordion-section__chevron,
  .accordion-section .el-collapse-transition {
    transition: none !important;
  }
  .accordion-section__indicator--error {
    animation: none;
    box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.2);
  }
}

/* ================================================================
   響應式
   ================================================================ */
@media (max-width: 768px) {
  .accordion-section__header {
    padding: 14px var(--sp-md);              /* 14px 16px */
    gap: 10px;
  }
  .accordion-section__body {
    padding: var(--sp-md) var(--sp-md) var(--sp-sm); /* 16px 16px 8px */
  }
  .accordion-section__bar {
    height: 22px;
  }
  .accordion-section__title {
    font-size: 14px;                         /* base */
  }
}
```

---

## 動畫時序總表

| 動作 | 時長 | Easing | CSS 變數 | DESIGN.md 分類 |
|------|------|--------|----------|---------------|
| 標題列 hover 背景色 | 150ms | ease-out | `--transition-fast` | short |
| 標題列 focus-visible outline | 即時 | — | — | — |
| 箭頭旋轉 | 250ms | ease-out | `--transition-normal` | medium |
| 內容展開/收合（高度） | 250ms | ease-out | el-collapse-transition override | medium |
| 卡片 hover 陰影 | 220ms | ease | 延續現有 edit.vue 慣例 | medium |
| 錯誤紅點脈動 | 1.5s | ease-in-out | `@keyframes pulse-error` | decorative |
| 邊框色變化（has-error） | 150ms | ease-out | `--transition-fast` | short |
| `scrollIntoView` | 瀏覽器原生 | smooth | `behavior: 'smooth'` | — |

---

## Accordion 狀態管理

### edit.vue 內部

```js
// 預設全部收合
const sections = reactive({
  basic: true,       // true = collapsed
  sku: true,
  pricing: true,
  detail: true
})

// 全部展開
function expandAll() {
  Object.keys(sections).forEach(k => sections[k] = false)
}

// 全部收合
function collapseAll() {
  Object.keys(sections).forEach(k => sections[k] = true)
}

// 滾動到指定 section 並展開
function scrollToSection(sectionKey) {
  sections[sectionKey] = false  // 展開

  nextTick(() => {
    const el = document.getElementById('section-' + sectionKey)
    if (el) {
      el.closest('.accordion-section')?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      })
    }
  })
}

// 儲存失敗 → 自動展開有錯誤的 section
async function handleSave() {
  const valid = await validate()
  if (!valid) {
    const errorSection = firstErrorSection.value
    if (errorSection) {
      scrollToSection(errorSection)
    }
    return
  }
  await saveProduct()
}
```

### 互動行為規則

| 行為 | 規則 |
|------|------|
| 預設狀態（新增） | 不使用 Accordion，用 Steps |
| 預設狀態（編輯） | 全部收合 |
| 展開/收合觸發 | 點擊標題列、Enter/Space 鍵 |
| 同時展開多個 | 允許（非互斥手風琴） |
| 收合已修改區塊 | 不需確認，資料保留在記憶體 |
| 離開頁面 | 若 isDirty=true，彈出「未儲存變更」確認 |
| 驗證錯誤 | 有錯誤的區塊自動展開 + 紅點 + 紅邊框 |
| 第一個錯誤 | 自動 scrollIntoView + focus 第一個錯誤欄位 |

---

## 未儲存變更追蹤

### 實作方式

```js
// useProductForm 內

// 初始快照（載入完成後拍照）
const initialSnapshot = ref(null)

function takeSnapshot() {
  initialSnapshot.value = JSON.stringify({
    form: toRaw(form.value),
    skuList: toRaw(skuList.value)
  })
}

// 載入完成後拍照
async function loadProduct() {
  // ... fetch ...
  await nextTick()
  takeSnapshot()
}

// 新增模式也要在 onMounted 後拍照
onMounted(() => {
  if (!productId?.value) {
    nextTick(() => takeSnapshot())
  }
})

// 全域 dirty
const isDirty = computed(() => {
  if (!initialSnapshot.value) return false
  const current = JSON.stringify({
    form: toRaw(form.value),
    skuList: toRaw(skuList.value)
  })
  return current !== initialSnapshot.value
})

// 各 section dirty
const sectionDirty = computed(() => {
  if (!initialSnapshot.value) return {}
  const initial = JSON.parse(initialSnapshot.value)

  const basicFields = ['title', 'categoryId', 'subTitle', 'mainImage', 'sliderImages']
  const pricingFields = ['price', 'originalPrice', 'salePrice', 'saleEndDate', 'priceAutoSync']
  const detailFields = ['description', 'sortOrder', 'isRecommend', 'isNew', 'isHot']

  const fieldChanged = (fields) =>
    fields.some(f => JSON.stringify(form.value[f]) !== JSON.stringify(initial.form[f]))

  const skuChanged =
    JSON.stringify(toRaw(skuList.value)) !== JSON.stringify(initial.skuList)

  // pricing dirty = form pricing fields OR any sku price/cost changed
  const skuPriceChanged = skuList.value.some((s, i) => {
    const orig = initial.skuList[i]
    if (!orig) return true
    return s.price !== orig.price
      || s.costPrice !== orig.costPrice
      || s.originalPrice !== orig.originalPrice
  })

  return {
    basic: fieldChanged(basicFields),
    sku: skuChanged,
    pricing: fieldChanged(pricingFields) || skuPriceChanged,
    detail: fieldChanged(detailFields)
  }
})
```

### 離開頁面確認

```js
// edit.vue / create.vue 內
import { onBeforeRouteLeave } from 'vue-router'

onBeforeRouteLeave((to, from, next) => {
  if (isDirty.value) {
    ElMessageBox.confirm('有未儲存的變更，確定要離開嗎？', '提示', {
      confirmButtonText: '離開',
      cancelButtonText: '留下',
      type: 'warning'
    }).then(() => next())
      .catch(() => next(false))
  } else {
    next()
  }
})
```

### 儲存成功後重置快照

```js
async function saveProduct() {
  // ... API call ...
  // 成功後更新快照
  await nextTick()
  takeSnapshot()
}
```

---

## 各 Section 在容器中的使用範例

### edit.vue（Accordion 模式）

```vue
<template>
  <div class="product-edit-page">
    <ProductSummaryBar ... />

    <AccordionSection
      v-model:collapsed="sections.basic"
      title="❶ 基本資訊"
      description="商品在前台顯示的核心欄位"
      section-key="basic"
      :has-error="firstErrorSection === 'basic'"
      :is-dirty="sectionDirty.basic"
    >
      <template #summary>{{ summary.basic }}</template>
      <ProductBasicSection v-model:form="form" :category-options="categoryOptions" />
    </AccordionSection>

    <AccordionSection
      v-model:collapsed="sections.sku"
      title="❷ 規格與庫存"
      description="SKU 規格與庫存設定"
      section-key="sku"
      :has-error="firstErrorSection === 'sku'"
      :is-dirty="sectionDirty.sku"
    >
      <template #summary>{{ summary.sku }}</template>
      <ProductSkuSection v-model:sku-list="skuList" :show-price="true" ... />
    </AccordionSection>

    <AccordionSection
      v-model:collapsed="sections.pricing"
      title="❸ 定價與促銷"
      description="商品主價格、SKU 個別售價、促銷設定"
      section-key="pricing"
      :has-error="firstErrorSection === 'pricing'"
      :is-dirty="sectionDirty.pricing"
    >
      <template #summary>{{ summary.pricing }}</template>
      <ProductPricingSection v-model:form="form" v-model:sku-list="skuList" ... />
    </AccordionSection>

    <AccordionSection
      v-model:collapsed="sections.detail"
      title="❹ 詳情與展示"
      description="富文字內容、展示旗標"
      section-key="detail"
      :has-error="firstErrorSection === 'detail'"
      :is-dirty="sectionDirty.detail"
    >
      <template #summary>{{ summary.detail }}</template>
      <ProductDetailSection v-model:form="form" />
    </AccordionSection>

    <!-- 底部操作列 -->
    <div class="action-bar">
      <div class="action-bar__inner">
        <span class="action-bar__hint">
          <template v-if="isDirty">● 有未儲存的變更</template>
          <template v-else>所有變更需點擊「儲存商品」後才會生效</template>
        </span>
        <div class="action-bar__buttons">
          <el-button plain @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave">儲存商品</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
```
