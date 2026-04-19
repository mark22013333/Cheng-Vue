# Spec — 驗證規則

## 驗證架構

```
useProductForm
  ├── formRules             ← el-form 原生 rules（ProductForm 欄位）
  ├── validateSkuList()     ← 程式化驗證（SkuRow[] 陣列）
  ├── validate()            ← 統一入口（兩者合併），Accordion 儲存時呼叫
  └── validateStep(step)    ← 逐步入口，Steps 每步「下一步」時呼叫
```

---

## formRules（el-form 原生 rules）

```js
const formRules = {
  // ❶ 基本資訊
  title: [
    { required: true, message: '商品名稱不能為空', trigger: 'blur' },
    { min: 2, max: 100, message: '名稱長度須為 2-100 字', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '請選擇商品分類', trigger: 'change' }
  ],
  subTitle: [
    { max: 200, message: '簡介不可超過 200 字', trigger: 'blur' }
  ],

  // ❸ 定價與促銷
  price: [
    { required: true, message: '商品價格不能為空', trigger: 'blur' },
    { validator: nonNegativeValidator('價格'), trigger: 'blur' }
  ],
  originalPrice: [
    { validator: nonNegativeValidator('原價'), trigger: 'blur' }
  ],
  salePrice: [
    { validator: salePriceValidator, trigger: 'blur' }
  ]
}
```

### 自訂 validator 實作

```js
function nonNegativeValidator(fieldLabel) {
  return (rule, value, callback) => {
    if (value !== undefined && value !== null && value < 0) {
      callback(new Error(`${fieldLabel}不可為負數`))
    } else {
      callback()
    }
  }
}

function salePriceValidator(rule, value, callback) {
  if (value === undefined || value === null || value === '') {
    callback() // 選填
    return
  }
  if (value < 0) {
    callback(new Error('特惠價不可為負數'))
  } else if (form.value.price && value >= form.value.price) {
    callback(new Error('特惠價須低於商品售價'))
  } else {
    callback()
  }
}
```

---

## validateSkuList()（程式化驗證）

### 回傳型別

```js
/**
 * @returns {{ valid: boolean, errors: SkuError[] }}
 *
 * @typedef {Object} SkuError
 * @property {number} index       - SKU 在 skuList 中的 index（-1 表示全域錯誤）
 * @property {string} field       - 錯誤欄位名
 * @property {string} message     - 錯誤訊息
 */
```

### 規則清單

```js
function validateSkuList() {
  const errors = []

  // S0: 至少一個 SKU
  if (skuList.value.length === 0) {
    errors.push({ index: -1, field: 'skuList', message: '請至少建立一個商品規格' })
    return { valid: false, errors }
  }

  skuList.value.forEach((sku, index) => {
    // S1: 規格名稱必填
    if (!sku.skuName || sku.skuName.trim() === '') {
      errors.push({ index, field: 'skuName', message: '規格名稱不能為空' })
    }

    // S2: 規格名稱長度 ≤ 100
    if (sku.skuName && sku.skuName.length > 100) {
      errors.push({ index, field: 'skuName', message: '規格名稱不可超過 100 字' })
    }

    // S3: SKU 售價必填且 ≥ 0
    if (sku.price === undefined || sku.price === null || sku.price === '') {
      errors.push({ index, field: 'price', message: '售價不能為空' })
    } else if (sku.price < 0) {
      errors.push({ index, field: 'price', message: '售價不可為負數' })
    }

    // S4: 成本價 ≥ 0（選填）
    if (sku.costPrice !== undefined && sku.costPrice !== null && sku.costPrice < 0) {
      errors.push({ index, field: 'costPrice', message: '成本價不可為負數' })
    }

    // S5: 庫存 ≥ 0
    if (sku.stockQuantity !== undefined && sku.stockQuantity < 0) {
      errors.push({ index, field: 'stockQuantity', message: '庫存不可為負數' })
    }

    // S6: LINKED 模式必須有 invItemId
    if (sku.stockMode === 'LINKED' && !sku.invItemId) {
      errors.push({ index, field: 'invItemId', message: '關聯庫存模式須選擇庫存物品' })
    }

    // S7: SKU 編碼長度 ≤ 50
    if (sku.skuCode && sku.skuCode.length > 50) {
      errors.push({ index, field: 'skuCode', message: 'SKU 編碼不可超過 50 字' })
    }

    // S8: 原價 ≥ 售價（如有填寫）
    if (sku.originalPrice != null && sku.price != null && sku.originalPrice < sku.price) {
      errors.push({ index, field: 'originalPrice', message: '原價不可低於售價' })
    }
  })

  // S9: SKU 編碼不可重複（排除空值）
  const codes = skuList.value
    .map((s, i) => ({ code: s.skuCode?.trim(), index: i }))
    .filter(c => c.code)
  const seenCodes = new Set()
  for (const { code, index } of codes) {
    if (seenCodes.has(code)) {
      errors.push({ index, field: 'skuCode', message: `SKU 編碼「${code}」重複` })
    }
    seenCodes.add(code)
  }

  // S10: 同一 invItemId 不可關聯多個 SKU
  const invIds = skuList.value
    .map((s, i) => ({ id: s.invItemId, index: i }))
    .filter(c => c.id)
  const seenInvIds = new Set()
  for (const { id, index } of invIds) {
    if (seenInvIds.has(id)) {
      errors.push({ index, field: 'invItemId', message: '同一庫存物品不可關聯多個 SKU' })
    }
    seenInvIds.add(id)
  }

  return { valid: errors.length === 0, errors }
}
```

---

## validate()（統一入口）

```js
async function validate() {
  let formValid = true
  let skuValid = true

  // 1. el-form 驗證
  try {
    await productFormRef.value?.validate()
  } catch {
    formValid = false
  }

  // 2. SKU 程式化驗證
  const skuResult = validateSkuList()
  skuValid = skuResult.valid
  skuValidationErrors.value = skuResult.errors

  // 3. 判斷第一個錯誤所在 section
  if (!formValid) {
    const errorFields = productFormRef.value?.fields
      ?.filter(f => f.validateState === 'error')
      ?.map(f => f.prop) || []
    firstErrorSection.value = mapFieldToSection(errorFields[0])
  } else if (!skuValid) {
    const first = skuResult.errors[0]
    firstErrorSection.value = mapFieldToSection(first.field)
  } else {
    firstErrorSection.value = null
  }

  return formValid && skuValid
}
```

### 欄位 → Section 映射表

```js
function mapFieldToSection(fieldName) {
  const map = {
    // basic
    title: 'basic',
    categoryId: 'basic',
    subTitle: 'basic',
    mainImage: 'basic',
    // sku
    skuList: 'sku',
    skuName: 'sku',
    skuCode: 'sku',
    invItemId: 'sku',
    stockQuantity: 'sku',
    // pricing
    price: 'pricing',
    originalPrice: 'pricing',
    salePrice: 'pricing',
    saleEndDate: 'pricing',
    costPrice: 'pricing',
    // detail
    description: 'detail',
    sortOrder: 'detail'
  }
  return map[fieldName] || 'basic'
}
```

---

## validateStep(step)（Steps 模式逐步驗證）

```js
async function validateStep(step) {
  switch (step) {
    case 0: // ❶ 基本資訊 — title + categoryId
      try {
        await productFormRef.value?.validateField(['title', 'categoryId'])
        return true
      } catch {
        return false
      }

    case 1: { // ❷ 規格與庫存 — skuList + skuName + invItemId + skuCode + stockQuantity
      if (skuList.value.length === 0) {
        proxy.$modal.msgWarning('請至少建立一個商品規格')
        return false
      }
      const skuFieldsForStep2 = ['skuList', 'skuName', 'invItemId', 'skuCode', 'stockQuantity']
      const errors = validateSkuList().errors.filter(e => skuFieldsForStep2.includes(e.field))
      skuValidationErrors.value = errors
      if (errors.length > 0) {
        proxy.$modal.msgWarning(errors[0].message)
        return false
      }
      return true
    }

    case 2: { // ❸ 定價與促銷 — form.price/salePrice + sku.price/costPrice/originalPrice
      let formValid = true
      try {
        await productFormRef.value?.validateField(['price', 'originalPrice', 'salePrice'])
      } catch {
        formValid = false
      }
      const priceFields = ['price', 'costPrice', 'originalPrice']
      const priceErrors = validateSkuList().errors.filter(e => priceFields.includes(e.field))
      skuValidationErrors.value = priceErrors
      if (priceErrors.length > 0) {
        proxy.$modal.msgWarning(priceErrors[0].message)
        return false
      }
      return formValid
    }

    case 3: // ❹ 詳情與發布 — 無必填欄位
      return true
  }
}
```

---

## 驗證規則總表

| ID | Section | 欄位 | 規則 | 時機 | 錯誤訊息 |
|----|---------|------|------|------|----------|
| F1 | basic | `title` | 必填 | blur | 商品名稱不能為空 |
| F2 | basic | `title` | 2-100 字 | blur | 名稱長度須為 2-100 字 |
| F3 | basic | `categoryId` | 必填 | change | 請選擇商品分類 |
| F4 | basic | `subTitle` | ≤200 字 | blur | 簡介不可超過 200 字 |
| F5 | pricing | `price` | 必填 | blur | 商品價格不能為空 |
| F6 | pricing | `price` | ≥0 | blur | 價格不可為負數 |
| F7 | pricing | `originalPrice` | ≥0 | blur | 原價不可為負數 |
| F8 | pricing | `salePrice` | ≥0 且 < price | blur | 特惠價須低於商品售價 |
| S0 | sku | `skuList` | length ≥ 1 | submit | 請至少建立一個商品規格 |
| S1 | sku | `sku.skuName` | 必填 | submit | 規格名稱不能為空 |
| S2 | sku | `sku.skuName` | ≤100 字 | submit | 規格名稱不可超過 100 字 |
| S3 | pricing | `sku.price` | 必填且 ≥0 | submit | 售價不能為空 / 不可為負數 |
| S4 | pricing | `sku.costPrice` | ≥0（選填） | submit | 成本價不可為負數 |
| S5 | sku | `sku.stockQuantity` | ≥0 | submit | 庫存不可為負數 |
| S6 | sku | `sku.invItemId` | LINKED 模式必填 | submit | 關聯庫存模式須選擇庫存物品 |
| S7 | sku | `sku.skuCode` | ≤50 字 | submit | SKU 編碼不可超過 50 字 |
| S8 | pricing | `sku.originalPrice` | ≥ sku.price | submit | 原價不可低於售價 |
| S9 | sku | `sku.skuCode` | 唯一（排除空） | submit | SKU 編碼「{code}」重複 |
| S10 | sku | `sku.invItemId` | 唯一（排除空） | submit | 同一庫存物品不可關聯多個 SKU |

---

## SKU 錯誤的 UI 呈現

### 錯誤傳遞

```
useProductForm.skuValidationErrors: SkuError[]
    │
    ▼
ProductSkuSection  /  ProductPricingSection
    │ 過濾 errors.filter(e => e.index === index)
    ▼
SkuCard  /  定價表格的 el-table-column
    │ fieldError(fieldName) → message || undefined
    ▼
el-form-item :error="fieldError('skuName')"
```

### SkuCard 內部

```vue
<script setup>
const props = defineProps({
  errors: { type: Array, default: () => [] }
})
const fieldError = (fieldName) =>
  props.errors.find(e => e.field === fieldName)?.message
</script>

<template>
  <el-form-item :error="fieldError('skuName')">
    <el-input v-model="sku.skuName" />
  </el-form-item>
</template>
```

### 錯誤清除

```js
// useProductForm 內
watch(skuList, () => {
  if (skuValidationErrors.value.length > 0) {
    skuValidationErrors.value = []
  }
}, { deep: true })
```

使用者修改 skuList 任何欄位即清除所有 SKU 錯誤，下次 submit 再重新驗證。
