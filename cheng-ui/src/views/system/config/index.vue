<template>
   <div class="app-container config-category-page">
      <!-- 頂部工具列 -->
      <div class="config-toolbar">
         <div class="toolbar-left">
            <el-input
               v-model="searchKeyword"
               placeholder="搜尋參數名稱或鍵名..."
               clearable
               class="search-input"
               @clear="clearSearch"
            >
               <template #prefix>
                  <el-icon><Search /></el-icon>
               </template>
            </el-input>
         </div>
         <div class="toolbar-right">
            <el-button type="primary" plain @click="handleAdd" v-hasPermi="[SYSTEM_CONFIG_ADD]">
               <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button type="warning" plain @click="handleExport" v-hasPermi="[SYSTEM_CONFIG_EXPORT]">
               <el-icon><Download /></el-icon>匯出
            </el-button>
            <el-button type="danger" plain @click="handleRefreshCache" v-hasPermi="[SYSTEM_CONFIG_REMOVE]">
               <el-icon><Refresh /></el-icon>重新整理暫存
            </el-button>
         </div>
      </div>

      <!-- 主內容區 -->
      <div class="config-main" v-loading="loading">
         <!-- 搜尋模式 -->
         <template v-if="isSearchMode">
            <div class="search-results">
               <div class="search-header">
                  <div class="search-header-left">
                     <el-icon :size="16" class="search-result-icon"><Search /></el-icon>
                     <span>搜尋「<strong>{{ searchKeyword }}</strong>」找到 <strong>{{ filteredConfigs.length }}</strong> 筆參數</span>
                  </div>
                  <el-button link type="primary" @click="clearSearch">
                     <el-icon><Close /></el-icon>清除搜尋
                  </el-button>
               </div>
               <el-table :data="filteredConfigs" stripe class="config-table">
                  <el-table-column label="參數名稱" prop="configName" min-width="160" show-overflow-tooltip />
                  <el-table-column label="參數鍵名" prop="configKey" min-width="240" show-overflow-tooltip>
                     <template #default="scope">
                        <el-tag size="small" type="info" effect="plain" class="key-tag">{{ scope.row.configKey }}</el-tag>
                     </template>
                  </el-table-column>
                  <el-table-column label="參數鍵值" prop="configValue" min-width="200" show-overflow-tooltip />
                  <el-table-column label="系統內建" prop="configType" width="90" align="center">
                     <template #default="scope">
                        <dict-tag :options="sys_yes_no" :value="scope.row.configType" />
                     </template>
                  </el-table-column>
                  <el-table-column label="所屬類別" width="120" align="center">
                     <template #default="scope">
                        <el-tag size="small" :type="getCategoryTag(scope.row.configKey)">
                           {{ getCategoryLabel(scope.row.configKey) }}
                        </el-tag>
                     </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150" align="center" fixed="right">
                     <template #default="scope">
                        <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="[SYSTEM_CONFIG_EDIT]">
                           <el-icon><Edit /></el-icon>修改
                        </el-button>
                        <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="[SYSTEM_CONFIG_REMOVE]">
                           <el-icon><Delete /></el-icon>刪除
                        </el-button>
                     </template>
                  </el-table-column>
               </el-table>
            </div>
         </template>

         <!-- 類別模式 -->
         <template v-else>
            <aside class="category-sidebar" :style="{ width: sidebarWidth + 'px' }">
               <div class="sidebar-title">參數類別</div>
               <div
                  ref="categoryListRef"
                  class="category-list"
                  tabindex="0"
                  @keydown="handleCategoryKeydown"
                  @focus="categoryListFocused = true"
                  @blur="categoryListFocused = false"
               >
                  <button
                     v-for="cat in categoriesWithCount"
                     :key="cat.key"
                     class="category-item"
                     :class="{ active: activeCategory === cat.key }"
                     @click="selectCategory(cat.key)"
                     tabindex="-1"
                  >
                     <span class="category-icon" :class="cat.key">
                        <el-icon :size="16"><component :is="cat.icon" /></el-icon>
                     </span>
                     <span class="category-label">{{ cat.label }}</span>
                     <span class="category-count" v-if="cat.count > 0">{{ cat.count }}</span>
                  </button>
               </div>
               <div class="sidebar-hint" :class="{ visible: categoryListFocused }">
                  <el-icon :size="12"><Top /></el-icon>
                  <el-icon :size="12"><Bottom /></el-icon>
                  <span>鍵盤切換類別</span>
               </div>
               <div class="sidebar-footer">
                  合計 <strong>{{ allConfigs.length }}</strong> 筆參數
               </div>
            </aside>

            <!-- 可拖曳分隔線 -->
            <div
               class="resize-handle"
               @mousedown="startResize"
               :title="'拖曳調整寬度'"
            >
               <div class="resize-handle-dots">
                  <span></span><span></span><span></span>
               </div>
            </div>

            <main class="category-content">
               <div class="content-header">
                  <div class="header-info">
                     <div class="header-title">
                        <span class="header-icon" :class="activeCategoryObj?.key">
                           <el-icon :size="20"><component :is="activeCategoryObj?.icon" /></el-icon>
                        </span>
                        <h3>{{ activeCategoryObj?.label }}</h3>
                     </div>
                     <p class="header-desc">{{ activeCategoryObj?.desc }}</p>
                  </div>
                  <div class="header-badge">{{ activeCategoryConfigs.length }} 項參數</div>
               </div>

               <!-- 第三方登入提示區 -->
               <div v-if="activeCategory === 'shop-oauth'" class="oauth-hint">
                  <el-alert type="info" :closable="false" show-icon>
                     <template #title>
                        <span>LINE 登入的 Channel ID / Channel Secret 需於 LINE 頻道管理頁面設定，此處僅控制啟用狀態與基本參數。</span>
                     </template>
                     <template #default>
                        <div class="oauth-hint-actions">
                           <el-button type="primary" size="small" @click="$router.push('/cadm/marketing/line/config')">
                              <el-icon><Setting /></el-icon>前往 LINE 頻道設定
                           </el-button>
                        </div>
                     </template>
                  </el-alert>
               </div>

               <el-table :data="activeCategoryConfigs" stripe class="config-table">
                  <el-table-column label="參數名稱" prop="configName" min-width="160" show-overflow-tooltip />
                  <el-table-column label="參數鍵名" prop="configKey" min-width="240" show-overflow-tooltip>
                     <template #default="scope">
                        <el-tag size="small" type="info" effect="plain" class="key-tag">{{ scope.row.configKey }}</el-tag>
                     </template>
                  </el-table-column>
                  <el-table-column label="參數鍵值" prop="configValue" min-width="200" show-overflow-tooltip>
                     <template #default="scope">
                        <span class="config-value" :class="{ 'is-long': scope.row.configValue && scope.row.configValue.length > 40 }">
                           {{ scope.row.configValue }}
                        </span>
                     </template>
                  </el-table-column>
                  <el-table-column label="系統內建" prop="configType" width="90" align="center">
                     <template #default="scope">
                        <dict-tag :options="sys_yes_no" :value="scope.row.configType" />
                     </template>
                  </el-table-column>
                  <el-table-column label="備註" prop="remark" min-width="120" show-overflow-tooltip />
                  <el-table-column label="操作" width="150" align="center" fixed="right">
                     <template #default="scope">
                        <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="[SYSTEM_CONFIG_EDIT]">
                           <el-icon><Edit /></el-icon>修改
                        </el-button>
                        <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="[SYSTEM_CONFIG_REMOVE]">
                           <el-icon><Delete /></el-icon>刪除
                        </el-button>
                     </template>
                  </el-table-column>
               </el-table>

               <div v-if="activeCategoryConfigs.length === 0" class="empty-state">
                  <el-empty description="此類別尚無參數" :image-size="80" />
               </div>
            </main>
         </template>
      </div>

      <!-- 新增或修改參數配置對話框 -->
      <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
         <el-form ref="configRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="參數名稱" prop="configName">
               <el-input v-model="form.configName" placeholder="請輸入參數名稱" />
            </el-form-item>
            <el-form-item label="參數鍵名" prop="configKey">
               <el-input v-model="form.configKey" placeholder="請輸入參數鍵名" />
            </el-form-item>
            <el-form-item label="參數鍵值" prop="configValue">
               <el-input v-model="form.configValue" type="textarea" placeholder="請輸入參數鍵值" :rows="3" />
            </el-form-item>
            <el-form-item label="系統內建" prop="configType">
               <el-radio-group v-model="form.configType">
                  <el-radio
                     v-for="dict in sys_yes_no"
                     :key="dict.value"
                     :value="dict.value"
                  >{{ dict.label }}</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item label="備註" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="請輸入備註" :rows="2" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">確 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Config">
import { ref, reactive, computed, toRefs, markRaw, getCurrentInstance, onBeforeUnmount, nextTick } from 'vue'
import {
  SYSTEM_CONFIG_ADD,
  SYSTEM_CONFIG_EDIT,
  SYSTEM_CONFIG_EXPORT,
  SYSTEM_CONFIG_REMOVE
} from '@/constants/permissions'
import { listConfig, getConfig, delConfig, addConfig, updateConfig, refreshCache } from '@/api/system/config'
import {
  Search, Plus, Download, Refresh, Edit, Delete, Close,
  Monitor, Lock, Discount, Van, CreditCard, Box,
  Wallet, Location, Key, Message, Connection, More, Top, Bottom, Setting
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { sys_yes_no } = proxy.useDict('sys_yes_no')

// ====== 類別定義 ======
const CATEGORIES = [
  {
    key: 'sys-ui', label: '系統介面', icon: markRaw(Monitor),
    desc: '主題外觀、側邊欄風格等介面顯示設定',
    match: (k) => k.startsWith('sys.index.')
  },
  {
    key: 'sys-security', label: '帳號安全', icon: markRaw(Lock),
    desc: '驗證碼、註冊開關、初始密碼、登入限制等安全策略',
    match: (k) => k.startsWith('sys.account.') || k.startsWith('sys.user.') || k.startsWith('sys.login.') || k.startsWith('sys.captcha.')
  },
  {
    key: 'shop-promo', label: '商城促銷', icon: markRaw(Discount),
    desc: '折扣、新會員優惠、滿額贈品等促銷設定',
    match: (k) => k.startsWith('shop.discount.') || k.startsWith('shop.gift.')
  },
  {
    key: 'shop-shipping', label: '運費設定', icon: markRaw(Van),
    desc: '免運門檻、預設運費、運費開關等設定',
    match: (k) => k.startsWith('shop.shipping.')
  },
  {
    key: 'shop-ecpay', label: '綠界金流', icon: markRaw(CreditCard),
    desc: '綠界支付 API 的 MerchantID、HashKey、HashIV 等串接設定',
    match: (k) => k.startsWith('shop.ecpay.') && !k.startsWith('shop.ecpay.logistics.')
  },
  {
    key: 'shop-ecpay-logistics', label: '綠界物流', icon: markRaw(Box),
    desc: '綠界物流 API 串接設定（超商取貨地圖、建立物流訂單等）',
    match: (k) => k.startsWith('shop.ecpay.logistics.')
  },
  {
    key: 'shop-payment', label: '付款設定', icon: markRaw(Wallet),
    desc: '支援的付款方式、貨到付款、轉帳匯款等設定',
    match: (k) => k.startsWith('shop.payment.')
  },
  {
    key: 'shop-logistics', label: '物流設定', icon: markRaw(Location),
    desc: '物流供應商、宅配/超商取貨啟用狀態、溫層等設定',
    match: (k) => k.startsWith('shop.logistics.')
  },
  {
    key: 'shop-password', label: '商城密碼政策', icon: markRaw(Lock),
    desc: '商城會員密碼長度與複雜度要求（適用於註冊與密碼重設）',
    match: (k) => k.startsWith('shop.password.')
  },
  {
    key: 'shop-pwd-reset', label: '商城密碼重設', icon: markRaw(Key),
    desc: '商城密碼重設功能的啟用開關、Token 有效期、信件主旨等設定',
    match: (k) => k.startsWith('shop.pwd_reset.')
  },
  {
    key: 'shop-mail', label: '郵件服務', icon: markRaw(Message),
    desc: 'SMTP 郵件伺服器與 Email 驗證相關設定',
    match: (k) => k.startsWith('shop.mail.') || k.startsWith('shop.email_verify.')
  },
  {
    key: 'shop-oauth', label: '第三方登入', icon: markRaw(Connection),
    desc: '第三方登入 OAuth 啟用開關與基本參數，LINE 頻道詳細設定請至「LINE 頻道管理」頁面',
    match: (k) => k.startsWith('shop.oauth.')
  },
  {
    key: 'other', label: '其他', icon: markRaw(More),
    desc: '未分類的系統參數',
    match: () => true
  }
]

// ====== Sidebar 寬度 & 拖曳 ======
const SIDEBAR_STORAGE_KEY = 'config-sidebar-width'
const SIDEBAR_MIN = 180
const SIDEBAR_MAX = 400
const SIDEBAR_DEFAULT = 240

const sidebarWidth = ref(
  parseInt(localStorage.getItem(SIDEBAR_STORAGE_KEY)) || SIDEBAR_DEFAULT
)

let isResizing = false

function startResize(e) {
  isResizing = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  const startX = e.clientX
  const startWidth = sidebarWidth.value

  function onMouseMove(ev) {
    if (!isResizing) return
    const delta = ev.clientX - startX
    const newWidth = Math.min(SIDEBAR_MAX, Math.max(SIDEBAR_MIN, startWidth + delta))
    sidebarWidth.value = newWidth
  }

  function onMouseUp() {
    isResizing = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(sidebarWidth.value))
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

onBeforeUnmount(() => {
  isResizing = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
})

// ====== 鍵盤導航 ======
const categoryListRef = ref(null)
const categoryListFocused = ref(false)

function selectCategory(key) {
  activeCategory.value = key
  // 點擊後讓 list 取得焦點，這樣可以繼續用鍵盤操作
  categoryListRef.value?.focus()
}

function handleCategoryKeydown(e) {
  const cats = categoriesWithCount.value
  if (!cats.length) return
  const currentIdx = cats.findIndex(c => c.key === activeCategory.value)

  if (e.key === 'ArrowDown' || e.key === 'j') {
    e.preventDefault()
    const nextIdx = currentIdx < cats.length - 1 ? currentIdx + 1 : 0
    activeCategory.value = cats[nextIdx].key
    scrollActiveIntoView()
  } else if (e.key === 'ArrowUp' || e.key === 'k') {
    e.preventDefault()
    const prevIdx = currentIdx > 0 ? currentIdx - 1 : cats.length - 1
    activeCategory.value = cats[prevIdx].key
    scrollActiveIntoView()
  }
}

function scrollActiveIntoView() {
  nextTick(() => {
    const active = categoryListRef.value?.querySelector('.category-item.active')
    active?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  })
}

// ====== 資料狀態 ======
const allConfigs = ref([])
const loading = ref(true)
const open = ref(false)
const dialogTitle = ref('')
const searchKeyword = ref('')
const activeCategory = ref(CATEGORIES[0].key)

const data = reactive({
  form: {},
  rules: {
    configName: [{ required: true, message: '參數名稱不能為空', trigger: 'blur' }],
    configKey: [{ required: true, message: '參數鍵名不能為空', trigger: 'blur' }],
    configValue: [{ required: true, message: '參數鍵值不能為空', trigger: 'blur' }]
  }
})
const { form, rules } = toRefs(data)

// ====== 計算屬性 ======

/** 是否處於搜尋模式 */
const isSearchMode = computed(() => searchKeyword.value && searchKeyword.value.trim().length > 0)

/** 將所有參數分配到類別中 */
const groupedConfigs = computed(() => {
  const groups = {}
  CATEGORIES.forEach(cat => { groups[cat.key] = [] })

  allConfigs.value.forEach(config => {
    const key = config.configKey || ''
    for (const cat of CATEGORIES) {
      if (cat.match(key)) {
        groups[cat.key].push(config)
        break
      }
    }
  })
  return groups
})

/** 各類別含數量資訊（排除空的「其他」類別） */
const categoriesWithCount = computed(() => {
  return CATEGORIES
    .map(cat => ({
      ...cat,
      count: groupedConfigs.value[cat.key]?.length || 0
    }))
    .filter(cat => cat.key !== 'other' || cat.count > 0)
})

/** 當前選中的類別物件 */
const activeCategoryObj = computed(() => {
  return categoriesWithCount.value.find(c => c.key === activeCategory.value)
})

/** 當前類別的參數列表 */
const activeCategoryConfigs = computed(() => {
  return groupedConfigs.value[activeCategory.value] || []
})

/** 搜尋篩選結果 */
const filteredConfigs = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return []
  return allConfigs.value.filter(c =>
    (c.configName && c.configName.toLowerCase().includes(kw)) ||
    (c.configKey && c.configKey.toLowerCase().includes(kw)) ||
    (c.configValue && c.configValue.toLowerCase().includes(kw)) ||
    (c.remark && c.remark.toLowerCase().includes(kw))
  )
})

// ====== 類別輔助函數 ======
function getCategoryLabel(configKey) {
  for (const cat of CATEGORIES) {
    if (cat.match(configKey)) return cat.label
  }
  return '其他'
}

const TAG_TYPES = {
  'sys-ui': '', 'sys-security': 'warning', 'shop-promo': 'danger',
  'shop-shipping': 'success', 'shop-ecpay': '', 'shop-ecpay-logistics': 'info',
  'shop-payment': 'warning', 'shop-logistics': 'success', 'shop-password': 'warning', 'shop-pwd-reset': 'danger',
  'shop-mail': 'info', 'shop-oauth': '', 'other': 'info'
}

function getCategoryTag(configKey) {
  for (const cat of CATEGORIES) {
    if (cat.match(configKey)) return TAG_TYPES[cat.key] || 'info'
  }
  return 'info'
}

// ====== 資料載入 ======
function getList() {
  loading.value = true
  listConfig({ pageNum: 1, pageSize: 999 }).then(response => {
    allConfigs.value = response.rows || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// ====== 搜尋操作 ======
function clearSearch() {
  searchKeyword.value = ''
}

// ====== CRUD 操作 ======
function reset() {
  form.value = {
    configId: undefined,
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: 'Y',
    remark: undefined
  }
  proxy.resetForm('configRef')
}

function cancel() {
  open.value = false
  reset()
}

function handleAdd() {
  reset()
  open.value = true
  dialogTitle.value = '新增參數'
}

function handleUpdate(row) {
  reset()
  getConfig(row.configId).then(response => {
    form.value = response.data
    open.value = true
    dialogTitle.value = '修改參數'
  })
}

function submitForm() {
  proxy.$refs['configRef'].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateConfig(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addConfig(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否確認刪除參數「' + row.configName + '」？').then(() => {
    return delConfig(row.configId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('system/config/export', {}, `config_${new Date().getTime()}.xlsx`)
}

function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess('重新整理暫存成功')
  })
}

// ====== 初始化 ======
getList()
</script>

<style scoped>
/* ====== 主題變數 ====== */
.config-category-page {
  --cfg-bg: var(--el-bg-color-overlay, #fff);
  --cfg-bg-sidebar: var(--el-fill-color-blank, #fafbfc);
  --cfg-bg-muted: var(--el-fill-color-light, #f5f7fa);
  --cfg-bg-hover: var(--el-fill-color, #f0f2f5);
  --cfg-border: var(--el-border-color-lighter, #f0f2f5);
  --cfg-text-primary: var(--el-text-color-primary, #303133);
  --cfg-text-regular: var(--el-text-color-regular, #606266);
  --cfg-text-secondary: var(--el-text-color-secondary, #909399);
  --cfg-text-placeholder: var(--el-text-color-placeholder, #c0c4cc);
  --cfg-icon-bg: var(--el-fill-color, #e8eaed);
  --cfg-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ====== 頂部工具列 ====== */
.config-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: var(--cfg-bg);
  border-radius: 8px;
  box-shadow: var(--cfg-shadow);
}

.toolbar-left {
  flex: 0 0 320px;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: var(--cfg-bg-muted);
  box-shadow: none;
  transition: all 0.25s;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  background: var(--cfg-bg);
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.toolbar-right {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ====== 主內容區 ====== */
.config-main {
  display: flex;
  gap: 0;
  background: var(--cfg-bg);
  border-radius: 8px;
  box-shadow: var(--cfg-shadow);
  min-height: 560px;
  overflow: hidden;
}

/* ====== 左側類別導航 ====== */
.category-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--cfg-bg-sidebar);
}

/* 可拖曳分隔線 */
.resize-handle {
  width: 8px;
  flex-shrink: 0;
  cursor: col-resize;
  background: var(--cfg-border);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  position: relative;
}

.resize-handle:hover,
.resize-handle:active {
  background: var(--el-border-color, #dcdfe6);
}

.resize-handle-dots {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.resize-handle-dots span {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--cfg-text-placeholder);
  transition: background 0.2s;
}

.resize-handle:hover .resize-handle-dots span,
.resize-handle:active .resize-handle-dots span {
  background: var(--cfg-text-secondary);
}

.sidebar-title {
  padding: 18px 16px 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--cfg-text-secondary);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.category-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--cfg-text-regular);
  text-align: left;
  transition: all 0.2s ease;
  font-family: inherit;
  line-height: 1.4;
}

.category-item:hover {
  background: var(--cfg-bg-hover);
  color: var(--cfg-text-primary);
}

.category-item.active {
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
}

.category-icon {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--cfg-icon-bg);
  color: var(--cfg-text-secondary);
  transition: all 0.2s ease;
}

.category-item.active .category-icon {
  color: #fff;
}

/* 類別圖示顏色 */
.category-item.active .category-icon.sys-ui { background: #409eff; }
.category-item.active .category-icon.sys-security { background: #e6a23c; }
.category-item.active .category-icon.shop-promo { background: #f56c6c; }
.category-item.active .category-icon.shop-shipping { background: #67c23a; }
.category-item.active .category-icon.shop-ecpay { background: #409eff; }
.category-item.active .category-icon.shop-ecpay-logistics { background: #909399; }
.category-item.active .category-icon.shop-payment { background: #e6a23c; }
.category-item.active .category-icon.shop-logistics { background: #67c23a; }
.category-item.active .category-icon.shop-password { background: #e6a23c; }
.category-item.active .category-icon.shop-pwd-reset { background: #f56c6c; }
.category-item.active .category-icon.shop-mail { background: #909399; }
.category-item.active .category-icon.shop-oauth { background: #409eff; }
.category-item.active .category-icon.other { background: #c0c4cc; }

.category-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-count {
  flex-shrink: 0;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  background: var(--cfg-icon-bg);
  color: var(--cfg-text-secondary);
}

.category-item.active .category-count {
  background: var(--el-color-primary-light-8, #d9ecff);
  color: var(--el-color-primary, #409eff);
}

/* 鍵盤操作提示 */
.sidebar-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  margin: 0 8px;
  border-radius: 6px;
  background: var(--cfg-bg-hover);
  font-size: 11px;
  color: var(--cfg-text-placeholder);
  transition: all 0.25s ease;
  opacity: 0.6;
}

.sidebar-hint.visible {
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  opacity: 1;
}

.sidebar-hint .el-icon {
  background: var(--cfg-icon-bg);
  border-radius: 3px;
  padding: 1px;
  color: var(--cfg-text-secondary);
  transition: all 0.25s ease;
}

.sidebar-hint.visible .el-icon {
  background: var(--el-color-primary-light-7, #b3d8ff);
  color: #fff;
}

/* category-list focus 樣式 */
.category-list:focus {
  outline: none;
}

.category-list:focus-visible {
  outline: none;
}

.sidebar-footer {
  padding: 14px 16px;
  font-size: 12px;
  color: var(--cfg-text-secondary);
  border-top: 1px solid var(--cfg-border);
  text-align: center;
}

.sidebar-footer strong {
  color: var(--cfg-text-regular);
}

/* ====== 右側內容區 ====== */
.category-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 24px;
}

.content-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--cfg-border);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

/* 頭部圖示背景 */
.header-icon.sys-ui { background: linear-gradient(135deg, #409eff, #53a8ff); }
.header-icon.sys-security { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.header-icon.shop-promo { background: linear-gradient(135deg, #f56c6c, #f89898); }
.header-icon.shop-shipping { background: linear-gradient(135deg, #67c23a, #85ce61); }
.header-icon.shop-ecpay { background: linear-gradient(135deg, #409eff, #53a8ff); }
.header-icon.shop-ecpay-logistics { background: linear-gradient(135deg, #909399, #a6a9ad); }
.header-icon.shop-payment { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.header-icon.shop-logistics { background: linear-gradient(135deg, #67c23a, #85ce61); }
.header-icon.shop-password { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.header-icon.shop-pwd-reset { background: linear-gradient(135deg, #f56c6c, #f89898); }
.header-icon.shop-mail { background: linear-gradient(135deg, #909399, #a6a9ad); }
.header-icon.shop-oauth { background: linear-gradient(135deg, #409eff, #53a8ff); }
.header-icon.other { background: linear-gradient(135deg, #c0c4cc, #d3d5d9); }

.header-info h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--cfg-text-primary);
  line-height: 36px;
}

.header-desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--cfg-text-secondary);
  line-height: 1.5;
}

.header-badge {
  flex-shrink: 0;
  padding: 4px 14px;
  border-radius: 20px;
  background: var(--cfg-bg-muted);
  font-size: 13px;
  font-weight: 500;
  color: var(--cfg-text-secondary);
  margin-top: 4px;
}

/* ====== 表格共用 ====== */
.config-table {
  width: 100%;
}

.config-table :deep(.el-table__header th) {
  background: var(--cfg-bg-sidebar) !important;
  font-weight: 600;
  color: var(--cfg-text-regular);
  font-size: 13px;
}

.config-table :deep(.el-table__body td) {
  font-size: 13px;
}

.key-tag {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.config-value.is-long {
  font-size: 12px;
  color: var(--cfg-text-secondary);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

/* ====== 第三方登入提示 ====== */
.oauth-hint {
  margin-bottom: 16px;
}

.oauth-hint :deep(.el-alert) {
  border-radius: 8px;
}

.oauth-hint :deep(.el-alert__title) {
  font-size: 13px;
  line-height: 1.6;
}

.oauth-hint-actions {
  margin-top: 8px;
}

/* ====== 搜尋結果 ====== */
.search-results {
  flex: 1;
  padding: 20px 24px;
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--cfg-border);
}

.search-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--cfg-text-regular);
}

/* ====== 搜尋結果圖示 ====== */
.search-result-icon {
  color: var(--cfg-text-secondary);
}

/* ====== RWD ====== */
@media (max-width: 992px) {
  .config-main {
    flex-direction: column;
  }

  .category-sidebar {
    width: 100% !important;
    border-right: none;
    border-bottom: 1px solid var(--cfg-border);
  }

  .resize-handle {
    display: none;
  }

  .sidebar-title {
    display: none;
  }

  .category-list {
    flex-direction: row;
    overflow-x: auto;
    padding: 8px;
    gap: 4px;
  }

  .category-item {
    flex-direction: column;
    gap: 4px;
    min-width: fit-content;
    padding: 8px 14px;
    font-size: 12px;
    text-align: center;
  }

  .category-count {
    display: none;
  }

  .sidebar-footer {
    display: none;
  }

  .toolbar-left {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .config-toolbar {
    flex-direction: column;
    gap: 12px;
  }

  .toolbar-left {
    flex: none;
    width: 100%;
  }

  .toolbar-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
