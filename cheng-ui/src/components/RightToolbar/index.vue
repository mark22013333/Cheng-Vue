<template>
  <div class="top-right-btn" :style="style">
    <el-row>
      <el-tooltip class="item" effect="dark" :content="showSearch ? '隱藏搜尋' : '顯示搜尋'" placement="top" v-if="search">
        <el-button circle icon="Search" @click="toggleSearch()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="重新整理" placement="top">
        <el-button circle icon="Refresh" @click="refresh()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="顯示/隱藏(欄位)" placement="top" v-if="Object.keys(columns).length > 0">
        <el-button circle icon="Menu" @click="showColumn()" v-if="showColumnsType == 'transfer'"/>
        <el-dropdown trigger="click" :hide-on-click="false" style="padding-left: 12px" v-if="showColumnsType == 'checkbox'">
          <el-button circle icon="Menu" />
          <template #dropdown>
            <el-dropdown-menu class="column-dropdown-menu">
              <!-- 全選/反選 按鈕 -->
              <el-dropdown-item>
                <el-checkbox :indeterminate="isIndeterminate" v-model="isChecked" @change="toggleCheckAll"> 列展示 </el-checkbox>
              </el-dropdown-item>
              <div class="check-line"></div>
              <div class="drag-tip">
                <el-icon><Rank /></el-icon>
                <span>拖曳調整欄位順序</span>
              </div>
              <div ref="sortableContainer" class="sortable-container">
                <div v-for="item in sortedColumns" :key="item.key" :data-key="item.key" class="sortable-item">
                  <el-icon class="drag-handle"><Rank /></el-icon>
                  <el-checkbox 
                    :model-value="columns[item.key]?.visible" 
                    @change="checkboxChange($event, item.key)" 
                    :label="item.label" 
                  />
                </div>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-tooltip>
    </el-row>
    <el-dialog :title="title" v-model="open" append-to-body>
      <el-transfer
        :titles="['顯示', '隱藏']"
        v-model="value"
        :data="transferData"
        @change="dataChange"
      ></el-transfer>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useTableConfig } from '@/composables/useTableConfig'
import { ElMessage } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'

const props = defineProps({
  /* 是否顯示檢索条件 */
  showSearch: {
    type: Boolean,
    default: true
  },
  /* 顯示/隱藏(欄位)訊息（陣列格式、對象格式） */
  columns: {
    type: [Array, Object],
    default: () => ({})
  },
  /* 是否顯示檢索圖標 */
  search: {
    type: Boolean,
    default: true
  },
  /* 顯示/隱藏(欄位)類型（transfer穿梭框、checkbox複選框） */
  showColumnsType: {
    type: String,
    default: "checkbox"
  },
  /* 右外邊距 */
  gutter: {
    type: Number,
    default: 10
  },
  /* 頁面唯一標識（用於儲存配置） */
  pageKey: {
    type: String,
    default: ''
  },
  /* 是否自動儲存欄位配置 */
  autoSave: {
    type: Boolean,
    default: true
  }
})

const emits = defineEmits(['update:showSearch', 'queryTable', 'columnsOrderChange'])

// 顯隱資料
const value = ref([])
// 彈出層標題
const title = ref("顯示/隱藏")
// 是否顯示彈出層
const open = ref(false)

const style = computed(() => {
  const ret = {}
  if (props.gutter) {
    ret.marginRight = `${props.gutter / 2}px`
  }
  return ret
})

// 是否全選/半選 狀態
const isChecked = computed({
  get: () => Array.isArray(props.columns) ? props.columns.every(col => col.visible) : Object.values(props.columns).every((col) => col.visible),
  set: () => {}
})
const isIndeterminate = computed(() => Array.isArray(props.columns) ? props.columns.some((col) => col.visible) && !isChecked.value : Object.values(props.columns).some((col) => col.visible) && !isChecked.value)
const transferData = computed(() => Array.isArray(props.columns) ? props.columns.map((item, index) => ({ key: index, label: item.label })) : Object.keys(props.columns).map((key, index) => ({ key: index, label: props.columns[key].label })))

// 排序後的欄位列表
const sortedColumns = computed(() => {
  if (Array.isArray(props.columns)) {
    return props.columns.map((col, index) => ({ ...col, key: index }))
  }
  return Object.entries(props.columns)
    .map(([key, col]) => ({ ...col, key, order: col.order ?? 999 }))
    .sort((a, b) => a.order - b.order)
})

// Sortable 實例
const sortableContainer = ref(null)
let sortableInstance = null

// 搜尋
function toggleSearch() {
  emits("update:showSearch", !props.showSearch)
}

// 重新整理
function refresh() {
  console.log('🔄 RightToolbar: 點擊重新整理按鈕，發送 queryTable 事件')
  emits("queryTable")
}

// 右側列表元素變化
function dataChange(data) {
  if (Array.isArray(props.columns)) {
    for (let item in props.columns) {
      const key = props.columns[item].key
      props.columns[item].visible = !data.includes(key)
    }
  } else {
    Object.keys(props.columns).forEach((key, index) => {
      props.columns[key].visible = !data.includes(index)
    })
  }
  // 觸發自動儲存
  triggerAutoSave()
}

// 打開顯示/隱藏(欄位)dialog
function showColumn() {
  open.value = true
}

if (props.showColumnsType == "transfer") {
  // transfer穿梭顯示/隱藏(欄位)初始預設隱藏列
  if (Array.isArray(props.columns)) {
    for (let item in props.columns) {
      if (props.columns[item].visible === false) {
        value.value.push(parseInt(item))
      }
    }
  } else {
    Object.keys(props.columns).forEach((key, index) => {
      if (props.columns[key].visible === false) {
        value.value.push(index)
      }
    })
  }
}

// 單勾選
function checkboxChange(event, key) {
  if (Array.isArray(props.columns)) {
    props.columns.filter(item => item.key == key)[0].visible = event
  } else {
    props.columns[key].visible = event
  }
  // 觸發自動儲存
  triggerAutoSave()
}

// 切換全選/反選
function toggleCheckAll() {
  const newValue = !isChecked.value
  if (Array.isArray(props.columns)) {
    props.columns.forEach((col) => (col.visible = newValue))
  } else {
    Object.values(props.columns).forEach((col) => (col.visible = newValue))
  }
  // 觸發自動儲存
  triggerAutoSave()
}

// ============================================================
// 表格欄位配置自動儲存功能
// ============================================================

const { saveConfig } = useTableConfig()

// 防抖計時器
let saveTimer = null

// 觸發自動儲存（防抖 2 秒）
function triggerAutoSave() {
  if (!props.autoSave || !props.pageKey) {
    return
  }
  
  // 清除之前的計時器
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  
  // 設定新的計時器
  saveTimer = setTimeout(() => {
    handleSaveConfig()
  }, 2000)
}

// 執行儲存配置
async function handleSaveConfig() {
  if (!props.pageKey || Object.keys(props.columns).length === 0) {
    return
  }
  
  try {
    await saveConfig(props.pageKey, props.columns)
    console.log(`✅ 表格欄位配置已自動儲存：${props.pageKey}`)
  } catch (error) {
    console.error('❌ 儲存表格欄位配置失敗：', error)
  }
}

// 監聽 columns 變化，觸發自動儲存
watch(
  () => props.columns,
  () => {
    triggerAutoSave()
  },
  { deep: true }
)

// 組件掛載時，如果有 pageKey，載入配置
onMounted(() => {
  if (props.pageKey) {
    console.log(`📋 表格欄位配置功能已啟用：${props.pageKey}`)
    console.log('💡 提示：修改欄位顯示/隱藏後，會在 2 秒後自動儲存')
  }
  // 初始化拖曳排序
  nextTick(() => {
    initSortable()
  })
})

// 初始化 Sortable
function initSortable() {
  if (sortableContainer.value && !sortableInstance) {
    sortableInstance = Sortable.create(sortableContainer.value, {
      animation: 150,
      handle: '.drag-handle',
      ghostClass: 'sortable-ghost',
      onEnd: handleSortEnd
    })
  }
}

// 拖曳結束處理
function handleSortEnd(evt) {
  const { oldIndex, newIndex } = evt
  if (oldIndex === newIndex) return
  
  // 取得新的順序
  const items = sortableContainer.value.querySelectorAll('.sortable-item')
  const newOrder = Array.from(items).map((item, index) => ({
    key: item.dataset.key,
    order: index
  }))
  
  // 更新 columns 的 order 屬性
  if (!Array.isArray(props.columns)) {
    newOrder.forEach(({ key, order }) => {
      if (props.columns[key]) {
        props.columns[key].order = order
      }
    })
  }
  
  // 發送事件通知父組件
  emits('columnsOrderChange', newOrder)
  
  // 觸發自動儲存
  triggerAutoSave()
  
  console.log('🔄 欄位順序已更新:', newOrder)
}
</script>

<style lang='scss' scoped>
:deep(.el-transfer__button) {
  border-radius: 50%;
  display: block;
  margin-left: 0px;
}
:deep(.el-transfer__button:first-child) {
  margin-bottom: 10px;
}
:deep(.el-dropdown-menu__item) {
  line-height: 30px;
  padding: 0 17px;
}
.check-line {
  width: 90%;
  height: 1px;
  background-color: #ccc;
  margin: 3px auto;
}
.drag-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 17px;
  font-size: 12px;
  color: #909399;
  border-bottom: 1px dashed #e4e7ed;
  margin-bottom: 4px;
}
.sortable-container {
  max-height: 400px;
  overflow-y: auto;
}
.sortable-item {
  display: flex;
  align-items: center;
  padding: 0 17px;
  line-height: 30px;
  cursor: default;
  transition: background-color 0.2s;
}
.sortable-item:hover {
  background-color: #f5f7fa;
}
.sortable-item .drag-handle {
  color: #c0c4cc;
  margin-right: 8px;
  cursor: grab;
}
.sortable-item .drag-handle:active {
  cursor: grabbing;
}
.sortable-ghost {
  background-color: #ecf5ff;
  border: 1px dashed #409eff;
}
:deep(.column-dropdown-menu) {
  min-width: 200px;
}
</style>
