import { ref, reactive, computed, watch, toRaw } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getProduct, addProduct, updateProduct } from '@/api/shop/product'
import { listSku } from '@/api/shop/sku'
import { treeCategory } from '@/api/shop/category'
import { listItemWithStock, getItem } from '@/api/inventory/item'
import { safeSkuPriceFromInventory } from './priceSafeguard'

/**
 * 商品表單核心 composable
 * create.vue 和 edit.vue 共用，管理所有商品相關狀態與方法
 *
 * @param {import('vue').Ref<number>|undefined} productId - 編輯模式傳入
 * @param {object} [options]
 * @param {object} [options.proxy] - getCurrentInstance().proxy，用於 $modal 等全域工具
 */
export function useProductForm(productId, options = {}) {
  const { proxy } = options

  // ===== 表單狀態 =====
  const form = ref(createDefaultForm())
  const skuList = ref([])
  const categoryOptions = ref([])
  const invItemOptions = ref([])
  const invItemAllOptions = ref([])
  const invItemLoading = ref(false)
  const loading = ref(false)
  const skuValidationErrors = ref([])
  const firstErrorSection = ref(null)
  const productFormRef = ref(null)

  // 快照：用於 isDirty 比對
  let formSnapshot = null
  let skuSnapshot = null

  // 搜尋防抖計時器
  let searchTimer = null

  // ===== 計算屬性 =====

  /** SKU 最低售價 */
  const computedMinPrice = computed(() => {
    const prices = skuList.value
      .filter(s => s.status === 'ENABLED' && s.price != null && s.price !== '')
      .map(s => Number(s.price))
    return prices.length > 0 ? Math.min(...prices) : null
  })

  /** SKU 最低原價 */
  const computedMinOriginalPrice = computed(() => {
    const prices = skuList.value
      .filter(s => s.status === 'ENABLED' && s.originalPrice != null)
      .map(s => Number(s.originalPrice))
    return prices.length > 0 ? Math.min(...prices) : null
  })

  /** 是否自動推算價格 */
  const isAutoPrice = computed(() => form.value.priceAutoSync)

  /** 總庫存 */
  const totalStock = computed(() =>
    skuList.value.reduce((sum, s) => sum + (s.stockQuantity || 0), 0)
  )

  /** 關聯庫存的 SKU 數量 */
  const linkedSkuCount = computed(() =>
    skuList.value.filter(s => s.stockMode === 'LINKED' && s.invItemId).length
  )

  /** 已關聯的 invItemId 列表（供 InventoryImportDialog 使用） */
  const existingInvItemIds = computed(() =>
    skuList.value.filter(s => s.invItemId).map(s => s.invItemId)
  )

  /** SKU 特惠價摘要（供 ProductPricingSection 使用） */
  const salePriceSummary = computed(() => {
    const salePrices = skuList.value
      .filter(s => s.salePrice != null && s.salePrice !== '' && Number(s.salePrice) > 0)
      .map(s => Number(s.salePrice))
    if (salePrices.length === 0) return ''
    const min = Math.min(...salePrices)
    const max = Math.max(...salePrices)
    const count = salePrices.length
    const total = skuList.value.length
    const range = min === max ? `$${min}` : `$${min} ~ $${max}`
    return `${count}/${total} 個規格設定特惠價，範圍 ${range}`
  })

  /** 商品摘要資訊 */
  const summary = computed(() => {
    // 價格摘要
    let pricing = ''
    if (computedMinPrice.value != null) {
      pricing = `$${computedMinPrice.value} 起`
      if (isAutoPrice.value) pricing += '（自動）'
      if (form.value.originalPrice) pricing += ` 原價 $${form.value.originalPrice}`
      // SKU 級特惠摘要
      const salePrices = skuList.value
        .filter(s => s.salePrice != null && s.salePrice !== '' && Number(s.salePrice) > 0)
        .map(s => Number(s.salePrice))
      if (salePrices.length > 0) {
        const min = Math.min(...salePrices)
        const max = Math.max(...salePrices)
        pricing += min === max
          ? ` 特惠 $${min}`
          : ` 特惠 $${min}~$${max}`
      }
    } else if (form.value.price) {
      pricing = `$${form.value.price}`
    }

    // SKU 摘要
    let sku = ''
    if (skuList.value.length > 0) {
      const skuNames = skuList.value
        .slice(0, 2)
        .map(s => `${s.skuName || '未命名'}($${s.price || 0})`)
        .join(', ')
      const suffix = skuList.value.length > 2 ? '...' : ''
      sku = `${skuList.value.length} SKU：${skuNames}${suffix} — 庫存 ${totalStock.value}`
    }

    // 旗標摘要
    const detail = [
      `推薦 ${form.value.isRecommend ? '✓' : '✗'}`,
      `新品 ${form.value.isNew ? '✓' : '✗'}`,
      `熱門 ${form.value.isHot ? '✓' : '✗'}`,
      `排序 ${form.value.sortOrder || 0}`
    ].join(' / ')

    // 基本資訊
    const basic = form.value.title || '未命名商品'

    return { basic, sku, pricing, detail }
  })

  /** 是否有未儲存的變更 */
  const isDirty = computed(() => {
    if (!formSnapshot) return false
    return JSON.stringify(toRaw(form.value)) !== formSnapshot ||
           JSON.stringify(toRaw(skuList.value)) !== skuSnapshot
  })

  /** 各區塊是否有未儲存變更 */
  const sectionDirty = computed(() => {
    if (!formSnapshot) return { basic: false, sku: false, pricing: false, detail: false }

    const original = JSON.parse(formSnapshot)
    const originalSkus = skuSnapshot ? JSON.parse(skuSnapshot) : []
    const current = toRaw(form.value)

    const fieldChanged = (fields) =>
      fields.some(k => JSON.stringify(current[k]) !== JSON.stringify(original[k]))

    // pricing dirty = form pricing fields OR any SKU price/cost changed
    const skuPriceChanged = skuList.value.some((s, i) => {
      const orig = originalSkus[i]
      if (!orig) return true
      return s.price !== orig.price
        || s.costPrice !== orig.costPrice
        || s.originalPrice !== orig.originalPrice
        || s.salePrice !== orig.salePrice
        || s.saleEndDate !== orig.saleEndDate
    }) || skuList.value.length !== originalSkus.length

    return {
      basic: fieldChanged(['title', 'categoryId', 'subTitle', 'mainImage', 'sliderImages']),
      sku: JSON.stringify(toRaw(skuList.value)) !== skuSnapshot,
      pricing: fieldChanged(['price', 'originalPrice', 'priceAutoSync']) || skuPriceChanged,
      detail: fieldChanged(['description', 'isRecommend', 'isNew', 'isHot', 'sortOrder'])
    }
  })

  // ===== 驗證規則 =====

  function nonNegativeValidator(fieldLabel) {
    return (rule, value, callback) => {
      if (value !== undefined && value !== null && value < 0) {
        callback(new Error(`${fieldLabel}不可為負數`))
      } else {
        callback()
      }
    }
  }

  const formRules = {
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
    price: [
      { required: true, message: '商品價格不能為空', trigger: 'blur' },
      { validator: nonNegativeValidator('價格'), trigger: 'blur' }
    ],
    originalPrice: [
      { validator: nonNegativeValidator('原價'), trigger: 'blur' }
    ]
  }

  // ===== 資料載入 =====

  /** 載入分類樹 */
  function getCategoryTree() {
    return treeCategory().then(response => {
      categoryOptions.value = response.data
    })
  }

  /** 載入庫存物品列表 */
  function loadInvItems(params = {}) {
    const query = { pageNum: 1, pageSize: 30, ...params }
    return listItemWithStock(query).then(response => {
      const items = (response.rows || []).map(item => ({
        itemId: item.itemId,
        itemCode: item.itemCode,
        itemName: item.itemName,
        barcode: item.barcode,
        currentStock: item.availableQty || 0,
        stockQuantity: item.totalQuantity || 0,
        currentPrice: item.currentPrice,
        purchasePrice: item.purchasePrice,
        tags: item.tags || []
      }))
      // 只有初始載入時才更新 allOptions
      if (!params.itemName && !params.itemCode && !params.keyword && !params.tagId && !params.tagName) {
        invItemAllOptions.value = items
      }
      invItemOptions.value = items
      return items
    })
  }

  /** 搜尋庫存物品（300ms 防抖） */
  function searchInvItems(query) {
    if (searchTimer) clearTimeout(searchTimer)

    if (!query || query.trim() === '') {
      invItemOptions.value = invItemAllOptions.value
      return
    }

    searchTimer = setTimeout(() => {
      invItemLoading.value = true
      const params = {}

      if (query.startsWith('#')) {
        const tagName = query.substring(1).trim()
        if (tagName) params.tagName = tagName
      } else {
        params.keyword = query.trim()
      }

      loadInvItems(params).finally(() => {
        invItemLoading.value = false
      })
    }, 300)
  }

  /** 聚焦下拉時載入初始資料 */
  function handleInvSelectFocus() {
    if (invItemOptions.value.length === 0) {
      loadInvItems()
    }
  }

  /** 載入商品資料（編輯模式） */
  async function loadProduct() {
    if (!productId?.value) return
    loading.value = true
    try {
      const response = await getProduct(productId.value)
      const data = response.data
      // 將 JSON 陣列格式的 sliderImages 轉換為逗號分隔格式
      if (data.sliderImages) {
        try {
          const images = JSON.parse(data.sliderImages)
          if (Array.isArray(images)) {
            data.sliderImages = images.join(',')
          }
        } catch {
          // 已經是逗號分隔格式，不需要轉換
        }
      }
      delete data.skuList
      form.value = { ...createDefaultForm(), ...data }
      await loadSkuList()
      takeSnapshot()
    } finally {
      loading.value = false
    }
  }

  /** 載入 SKU 列表 */
  async function loadSkuList() {
    if (!productId?.value) return
    const response = await listSku(productId.value)
    const skus = response.data || []
    skuList.value = skus.map(sku => ({
      ...sku,
      stockMode: sku.invItemId ? 'LINKED' : 'MANUAL'
    }))
    await ensureLinkedItemsLoaded()
  }

  /** 確保已關聯的庫存物品載入到下拉選項中 */
  async function ensureLinkedItemsLoaded() {
    const linkedItemIds = skuList.value
      .filter(sku => sku.invItemId)
      .map(sku => sku.invItemId)

    if (linkedItemIds.length === 0) return

    for (const itemId of linkedItemIds) {
      const exists = invItemAllOptions.value.some(item => item.itemId === itemId)
      if (!exists) {
        try {
          const response = await getItem(itemId)
          if (response.data) {
            const item = response.data
            invItemAllOptions.value.push({
              itemId: item.itemId,
              itemCode: item.itemCode,
              itemName: item.itemName,
              barcode: item.barcode,
              currentStock: item.stockQuantity || 0,
              stockQuantity: item.stockQuantity || 0,
              currentPrice: item.currentPrice,
              purchasePrice: item.purchasePrice,
              tags: item.tags || []
            })
          }
        } catch {
          console.warn('無法載入庫存物品:', itemId)
        }
      }
    }
    invItemOptions.value = [...invItemAllOptions.value]
  }

  // ===== SKU 操作 =====

  /** 新增 SKU */
  function addSku() {
    skuList.value.push({
      skuId: undefined,
      productId: productId?.value,
      skuCode: '',
      skuName: '',
      price: form.value.price || 0,
      originalPrice: undefined,
      salePrice: undefined,
      saleEndDate: undefined,
      costPrice: undefined,
      stockQuantity: 0,
      invItemId: undefined,
      stockMode: 'LINKED',
      status: 'ENABLED',
      refPrice: undefined,
      refCost: undefined,
      invItemName: undefined,
      invItemCode: undefined
    })
  }

  /** 刪除 SKU */
  function removeSku(index) {
    skuList.value.splice(index, 1)
  }

  /** 切換庫存模式 */
  function handleStockModeChange(index, mode) {
    const sku = skuList.value[index]
    if (!sku) return
    if (mode === 'MANUAL') {
      sku.invItemId = undefined
      sku.invItemName = undefined
      sku.invItemCode = undefined
      sku.refPrice = undefined
      sku.refCost = undefined
    }
    sku.stockMode = mode
  }

  /** 庫存物品變更時帶入資料 */
  function handleInvItemChange(index, invItemId) {
    const sku = skuList.value[index]
    if (!sku) return

    if (invItemId) {
      let invItem = invItemOptions.value.find(item => item.itemId === invItemId)
      if (!invItem) {
        invItem = invItemAllOptions.value.find(item => item.itemId === invItemId)
      }
      if (invItem) {
        // 保留已有值的邏輯
        sku.skuName = sku.skuName || invItem.itemName
        sku.skuCode = sku.skuCode || invItem.itemCode
        sku.stockQuantity = invItem.currentStock ?? invItem.stockQuantity ?? 0

        // 價格帶入（如果 SKU 售價為空或為 0）
        // 套用防呆：當庫存現價 < 100 時改以採購成本加成推算，避免虧損
        if (!sku.price || sku.price === 0) {
          sku.price = safeSkuPriceFromInventory(invItem.currentPrice, invItem.purchasePrice)
        }
        // 成本價帶入（如果 SKU costPrice 為空）
        if (sku.costPrice == null) {
          sku.costPrice = invItem.purchasePrice
        }

        // 參考價（永遠更新，唯讀參考用）
        sku.refPrice = invItem.currentPrice
        sku.refCost = invItem.purchasePrice
        sku.invItemName = invItem.itemName
        sku.invItemCode = invItem.itemCode
      }
    } else {
      sku.stockQuantity = 0
      sku.refPrice = undefined
      sku.refCost = undefined
      sku.invItemName = undefined
      sku.invItemCode = undefined
    }
  }

  /**
   * 解除 SKU 與庫存物品的關聯
   *
   * 清空：invItemId、invItemName、invItemCode、refPrice、refCost
   * 保留：price、costPrice、stockQuantity、skuName、skuCode（使用者已編輯過的業務資料）
   */
  function handleUnlinkInvItem(index) {
    const sku = skuList.value[index]
    if (!sku) return
    sku.invItemId = undefined
    sku.invItemName = undefined
    sku.invItemCode = undefined
    sku.refPrice = undefined
    sku.refCost = undefined
  }

  /** 從庫存匯入（InventoryImportDialog confirm 後呼叫） */
  function importFromInventory(items, importOpts) {
    for (const item of items) {
      // 衝突檢測
      const existingIdx = skuList.value.findIndex(s => s.invItemId === item.itemId)

      if (existingIdx >= 0) {
        // 更新：刷新庫存、參考價，不覆蓋現有售價
        const existing = skuList.value[existingIdx]
        existing.stockQuantity = item.currentStock
        existing.refPrice = item.currentPrice
        existing.refCost = item.purchasePrice
        continue
      }

      // 新增 SKU
      skuList.value.push({
        skuId: undefined,
        productId: form.value.productId,
        skuCode: item.itemCode || '',
        skuName: item.itemName || '',
        price: importOpts.syncPrice ? (item.currentPrice || 0) : 0,
        costPrice: importOpts.syncCost ? (item.purchasePrice || undefined) : undefined,
        originalPrice: undefined,
        stockQuantity: item.currentStock || 0,
        invItemId: item.itemId,
        stockMode: 'LINKED',
        status: 'ENABLED',
        refPrice: item.currentPrice,
        refCost: item.purchasePrice,
        invItemName: item.itemName,
        invItemCode: item.itemCode
      })
    }
  }

  // ===== 價格自動推算 =====

  /** 切換自動/手動價格模式 */
  function toggleAutoPrice() {
    form.value.priceAutoSync = !form.value.priceAutoSync
    if (form.value.priceAutoSync && computedMinPrice.value != null) {
      form.value.price = computedMinPrice.value
    }
  }

  // SKU 價格變動時自動同步商品主價格
  watch(
    () => skuList.value.map(s => s.price),
    () => {
      if (form.value.priceAutoSync && computedMinPrice.value != null) {
        form.value.price = computedMinPrice.value
      }
    },
    { deep: true }
  )

  // SKU 變動時清除驗證錯誤
  watch(skuList, () => {
    if (skuValidationErrors.value.length > 0) {
      skuValidationErrors.value = []
    }
  }, { deep: true })

  // ===== 驗證方法 =====

  /**
   * 判斷 SKU 是否於本次編輯中被修改過（價格相關欄位）。
   * 只比對 price / costPrice，不比對名稱、庫存等無關欄位。
   * 新增的 SKU（originalSkus 中不存在對應 index）必然回傳 true。
   *
   * @param {object} sku 當前 SKU
   * @param {number} index SKU 在 skuList 中的索引
   * @returns {boolean}
   */
  function isSkuDirtyForPriceCheck(sku, index) {
    if (!skuSnapshot) return true
    const originalSkus = JSON.parse(skuSnapshot)
    const orig = originalSkus?.[index]
    if (!orig) return true
    return Number(orig.price) !== Number(sku.price)
      || Number(orig.costPrice) !== Number(sku.costPrice)
  }

  /** SKU 列表程式化驗證 */
  function validateSkuList() {
    const errors = []
    const needsConfirm = []

    // S0: 至少一個 SKU
    if (skuList.value.length === 0) {
      errors.push({ index: -1, field: 'skuList', message: '請至少建立一個商品規格' })
      return { valid: false, errors, needsConfirm }
    }

    skuList.value.forEach((sku, index) => {
      // S1: 規格名稱必填
      if (!sku.skuName || sku.skuName.trim() === '') {
        errors.push({ index, field: 'skuName', message: '規格名稱不能為空' })
      }
      // S2: 規格名稱長度
      if (sku.skuName && sku.skuName.length > 100) {
        errors.push({ index, field: 'skuName', message: '規格名稱不可超過 100 字' })
      }
      // S3: SKU 售價必填且 >= 0
      if (sku.price === undefined || sku.price === null || sku.price === '') {
        errors.push({ index, field: 'price', message: '售價不能為空' })
      } else if (sku.price < 0) {
        errors.push({ index, field: 'price', message: '售價不可為負數' })
      }
      // S3b: 本次編輯過且 price <= 0 → 需使用者於儲存時確認（軟性）
      const priceNum = Number(sku.price)
      if (sku.price !== undefined && sku.price !== null && sku.price !== ''
          && priceNum <= 0
          && isSkuDirtyForPriceCheck(sku, index)) {
        needsConfirm.push({
          index,
          skuName: sku.skuName || `規格 ${index + 1}`,
          reason: 'zero-price'
        })
      }
      // S4: 成本價 >= 0（選填）
      if (sku.costPrice !== undefined && sku.costPrice !== null && sku.costPrice < 0) {
        errors.push({ index, field: 'costPrice', message: '成本價不可為負數' })
      }
      // S4b: 成本價不可高於售價（寬鬆版：允許等值）
      if (sku.costPrice != null && sku.costPrice !== ''
          && sku.price != null && sku.price !== ''
          && Number(sku.costPrice) > Number(sku.price)) {
        errors.push({ index, field: 'costPrice', message: '成本價不可高於售價' })
      }
      // S5: 庫存 >= 0
      if (sku.stockQuantity !== undefined && sku.stockQuantity < 0) {
        errors.push({ index, field: 'stockQuantity', message: '庫存不可為負數' })
      }
      // S6: LINKED 模式必須有 invItemId
      if (sku.stockMode === 'LINKED' && !sku.invItemId) {
        errors.push({ index, field: 'invItemId', message: '關聯庫存模式須選擇庫存物品' })
      }
      // S7: SKU 編碼長度
      if (sku.skuCode && sku.skuCode.length > 50) {
        errors.push({ index, field: 'skuCode', message: 'SKU 編碼不可超過 50 字' })
      }
      // S8: 原價 >= 售價
      if (sku.originalPrice != null && sku.price != null && sku.originalPrice < sku.price) {
        errors.push({ index, field: 'originalPrice', message: '原價不可低於售價' })
      }
      // S-SP1: 特惠價非負
      if (sku.salePrice != null && sku.salePrice !== '' && sku.salePrice < 0) {
        errors.push({ index, field: 'salePrice', message: '特惠價不可為負數' })
      }
      // S-SP2: 特惠價 < 售價
      if (sku.salePrice != null && sku.salePrice !== '' && sku.price != null && sku.salePrice >= sku.price) {
        errors.push({ index, field: 'salePrice', message: '特惠價須低於該規格售價' })
      }
      // S-SP3: 有到期時間但沒特惠價
      if (sku.saleEndDate != null && (sku.salePrice == null || sku.salePrice === '')) {
        errors.push({ index, field: 'saleEndDate', message: '設定特價結束時間前須先填寫特惠價' })
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

    return { valid: errors.length === 0, errors, needsConfirm }
  }

  /** 欄位 → Section 映射 */
  function mapFieldToSection(fieldName) {
    const map = {
      title: 'basic', categoryId: 'basic', subTitle: 'basic', mainImage: 'basic',
      skuList: 'sku', skuName: 'sku', skuCode: 'sku', invItemId: 'sku', stockQuantity: 'sku',
      price: 'pricing', originalPrice: 'pricing', salePrice: 'pricing', saleEndDate: 'pricing', costPrice: 'pricing', skuSalePrice: 'pricing',
      description: 'detail', sortOrder: 'detail'
    }
    return map[fieldName] || 'basic'
  }

  /** 統一驗證入口（Accordion 儲存時呼叫） */
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

  /** 逐步驗證（Steps 模式） */
  async function validateStep(step) {
    switch (step) {
      case 0: // 基本資訊
        try {
          await productFormRef.value?.validateField(['title', 'categoryId'])
          return true
        } catch {
          return false
        }

      case 1: { // 規格與庫存
        if (skuList.value.length === 0) {
          proxy?.$modal?.msgWarning('請至少建立一個商品規格')
          return false
        }
        const skuFieldsForStep2 = ['skuList', 'skuName', 'invItemId', 'skuCode', 'stockQuantity']
        const errors = validateSkuList().errors.filter(e => skuFieldsForStep2.includes(e.field))
        skuValidationErrors.value = errors
        if (errors.length > 0) {
          proxy?.$modal?.msgWarning(errors[0].message)
          return false
        }
        return true
      }

      case 2: { // 定價與促銷
        let formOk = true
        try {
          await productFormRef.value?.validateField(['price', 'originalPrice'])
        } catch {
          formOk = false
        }
        const priceFields = ['price', 'costPrice', 'originalPrice', 'salePrice', 'saleEndDate']
        const priceErrors = validateSkuList().errors.filter(e => priceFields.includes(e.field))
        skuValidationErrors.value = priceErrors
        if (priceErrors.length > 0) {
          proxy?.$modal?.msgWarning(priceErrors[0].message)
          return false
        }
        return formOk
      }

      case 3: // 詳情與發布
        return true

      default:
        return true
    }
  }

  /** 取得第一個錯誤的 section */
  function getFirstErrorSection() {
    return firstErrorSection.value
  }

  // ===== 儲存 =====

  /** 儲存商品（將 skuList 嵌入 payload 一次送出） */
  async function saveProduct() {
    const isValid = await validate()
    if (!isValid) return false

    // 0 元商品確認對話（僅針對本次編輯過的 SKU）
    const { needsConfirm } = validateSkuList()
    if (needsConfirm.length > 0) {
      const zeroList = needsConfirm
        .filter(c => c.reason === 'zero-price')
        .map(c => `  • ${c.skuName}`)
        .join('\n')
      if (zeroList) {
        try {
          await ElMessageBox.confirm(
            `以下規格售價為 $0，屬例外情境（贈品 / 試用品）：\n\n${zeroList}\n\n確定要建立 0 元商品嗎？`,
            '確認 0 元商品',
            {
              type: 'warning',
              confirmButtonText: '確定儲存',
              cancelButtonText: '取消',
              dangerouslyUseHTMLString: false
            }
          )
        } catch {
          // 使用者取消，中止儲存（不清空表單）
          return false
        }
      }
    }

    loading.value = true
    try {
      const isEdit = !!productId?.value
      const payload = { ...toRaw(form.value), skuList: toRaw(skuList.value) }
      const saveApi = isEdit ? updateProduct : addProduct
      const response = await saveApi(payload)
      const savedProductId = response.data?.productId || form.value.productId || productId?.value

      takeSnapshot()
      proxy?.$modal?.msgSuccess(isEdit ? '修改成功' : '新增成功')
      return savedProductId
    } catch (e) {
      const msg = e?.response?.data?.msg || e?.message || '儲存失敗'
      proxy?.$modal?.msgError(msg)
      return false
    } finally {
      loading.value = false
    }
  }

  // ===== 工具方法 =====

  function createDefaultForm() {
    return {
      productId: undefined,
      categoryId: undefined,
      title: undefined,
      subTitle: undefined,
      mainImage: undefined,
      sliderImages: undefined,
      price: 0,
      originalPrice: 0,
      description: undefined,
      sortOrder: 0,
      isRecommend: false,
      isNew: false,
      isHot: false,
      priceAutoSync: true
    }
  }

  /** 拍攝快照用於 isDirty 比對 */
  function takeSnapshot() {
    formSnapshot = JSON.stringify(toRaw(form.value))
    skuSnapshot = JSON.stringify(toRaw(skuList.value))
  }

  /** 初始化（載入分類、庫存物品、商品資料） */
  async function init() {
    await Promise.all([
      getCategoryTree(),
      loadInvItems()
    ])
    if (productId?.value) {
      await loadProduct()
    } else {
      takeSnapshot()
    }
  }

  return {
    // 狀態
    form, skuList, categoryOptions, invItemOptions, invItemAllOptions,
    invItemLoading, loading, skuValidationErrors, firstErrorSection, productFormRef,

    // 計算屬性
    computedMinPrice, computedMinOriginalPrice, isAutoPrice, salePriceSummary,
    totalStock, linkedSkuCount, existingInvItemIds, summary, isDirty, sectionDirty,

    // 資料載入
    init, loadProduct, getCategoryTree, loadInvItems,
    searchInvItems, handleInvSelectFocus,

    // SKU 操作
    addSku, removeSku, handleStockModeChange, handleInvItemChange, handleUnlinkInvItem, importFromInventory,

    // 價格
    toggleAutoPrice,

    // 驗證
    formRules, validate, validateStep, validateSkuList, mapFieldToSection, getFirstErrorSection,

    // 儲存
    saveProduct
  }
}
