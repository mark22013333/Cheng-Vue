<template>
  <div class="top-right-btn" :style="style">
    <el-row>
      <el-tooltip v-if="search" :content="showSearch ? '隱藏搜尋' : '顯示搜尋'" class="item" effect="dark"
                  placement="top">
        <el-button size="small" circle @click="toggleSearch()">
          <el-icon><Search /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip class="item" content="重新整理" effect="dark" placement="top">
        <el-button size="small" circle @click="refresh()">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip v-if="Object.keys(columns).length > 0" class="item" content="顯隱列" effect="dark" placement="top">
        <el-button size="small" circle @click="showColumn()" v-if="showColumnsType == 'transfer'">
          <el-icon><Menu /></el-icon>
        </el-button>
        <el-dropdown trigger="click" :hide-on-click="false" style="padding-left: 12px" v-if="showColumnsType == 'checkbox'">
          <el-button size="small" circle>
            <el-icon><Menu /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
            <!-- 全選/反選 按鈕 -->
            <el-dropdown-item>
              <el-checkbox :indeterminate="isIndeterminate" v-model="isChecked" @change="toggleCheckAll"> 列展示 </el-checkbox>
            </el-dropdown-item>
            <div class="check-line"></div>
            <template v-for="(item, key) in columns" :key="key">
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

<script>
import { Search, Refresh, Menu } from '@element-plus/icons-vue'

export default {
  name: "RightToolbar",
  components: {
    Search,
    Refresh,
    Menu
  },
  data() {
    return {
      // 顯隱數據
      value: [],
      // 彈出層標題
      title: "顯示/隱藏",
      // 是否顯示彈出層
      open: false
    }
  },
  props: {
    /* 是否顯示檢索條件 */
    showSearch: {
      type: Boolean,
      default: true
    },
    /* 顯隱列訊息（陣列格式、物件格式） */
    columns: {
      type: [Array, Object],
      default: () => ({})
    },
    /* 是否顯示檢索圖標 */
    search: {
      type: Boolean,
      default: true
    },
    /* 顯隱列類型（transfer穿梭框、checkbox複選框） */
    showColumnsType: {
      type: String,
      default: "checkbox"
    },
    /* 右外邊距 */
    gutter: {
      type: Number,
      default: 10
    },
  },
  computed: {
    style() {
      const ret = {}
      if (this.gutter) {
        ret.marginRight = `${this.gutter / 2}px`
      }
      return ret
    },
    isChecked: {
      get() {
        return Array.isArray(this.columns) ? this.columns.every((col) => col.visible) : Object.values(this.columns).every((col) => col.visible)
      },
      set() {}
    },
    isIndeterminate() {
      return Array.isArray(this.columns) ? this.columns.some((col) => col.visible) && !this.isChecked : Object.values(this.columns).some((col) => col.visible) && !this.isChecked
    },
    transferData() {
      if (Array.isArray(this.columns)) {
        return this.columns.map((item, index) => ({ key: index, label: item.label }))
      } else {
        return Object.keys(this.columns).map((key, index) => ({ key: index, label: this.columns[key].label }))
      }
    }
  },
  created() {
    if (this.showColumnsType == 'transfer') {
      // transfer穿梭顯隱列初始預設隱藏列
      if (Array.isArray(this.columns)) {
        for (let item in this.columns) {
          if (this.columns[item].visible === false) {
            this.value.push(parseInt(item))
          }
        }
      } else {
        Object.keys(this.columns).forEach((key, index) => {
          if (this.columns[key].visible === false) {
            this.value.push(index)
          }
        })
      }
    }
  },
  methods: {
    // 搜尋
    toggleSearch() {
      this.$emit("update:showSearch", !this.showSearch)
    },
    // 重新整理
    refresh() {
      this.$emit("queryTable")
    },
    // 右側列表元素變化
    dataChange(data) {
      if (Array.isArray(this.columns)) {
        for (let item in this.columns) {
          const key = this.columns[item].key
          this.columns[item].visible = !data.includes(key)
        }
      } else {
        Object.keys(this.columns).forEach((key, index) => {
          this.columns[key].visible = !data.includes(index)
        })
      }
    },
    // 打開顯隱列dialog
    showColumn() {
      this.open = true
    },
    // 單勾選
    checkboxChange(event, key) {
      if (Array.isArray(this.columns)) {
        this.columns.filter(item => item.key == key)[0].visible = event
      } else {
        this.columns[key].visible = event
      }
    },
    // 切換全選/反選
    toggleCheckAll() {
      const newValue = !this.isChecked
      if (Array.isArray(this.columns)) {
        this.columns.forEach((col) => (col.visible = newValue))
      } else {
        Object.values(this.columns).forEach((col) => (col.visible = newValue))
      }
    }
  },
}
</script>

<style lang="scss" scoped>
:deep(.el-transfer__button) {
  border-radius: 50%;
  padding: 12px;
  display: block;
  margin-left: 0px;
}
:deep(.el-transfer__button:first-child) {
  margin-bottom: 10px;
}
.check-line {
  width: 90%;
  height: 1px;
  background-color: #ccc;
  margin: 3px auto;
}
</style>
