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
            <el-dropdown-menu>
              <!-- 全選/反選 按鈕 -->
              <el-dropdown-item>
                <el-checkbox :indeterminate="isIndeterminate" v-model="isChecked" @change="toggleCheckAll"> 列展示 </el-checkbox>
              </el-dropdown-item>
              <div class="check-line"></div>
              <template v-for="(item, key) in columns" :key="item.key">
                <el-dropdown-item>
                  <el-checkbox v-model="item.visible" @change="checkboxChange($event, key)" :label="item.label" />
                </el-dropdown-item>
              </template>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useTableConfig } from '@/composables/useTableConfig'
import { ElMessage } from 'element-plus'

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

const emits = defineEmits(['update:showSearch', 'queryTable'])

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
})
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
</style>
