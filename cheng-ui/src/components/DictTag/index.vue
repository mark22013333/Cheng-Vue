<template>
  <div>
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span
          v-if="(item.raw.listClass == 'default' || item.raw.listClass == '') && (item.raw.cssClass == '' || item.raw.cssClass == null)"
          :key="item.value"
          :index="index"
          :class="item.raw.cssClass"
          >{{ item.label + ' ' }}</span
        >
        <el-tag
          v-else
          :disable-transitions="true"
          :key="item.value"
          :index="index"
          :type="getTagType(item.raw.listClass)"
          :class="item.raw.cssClass"
        >
          {{ item.label + ' ' }}
        </el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ unmatchArray | handleArray }}
    </template>
  </div>
</template>

<script>
export default {
  name: "DictTag",
  props: {
    options: {
      type: Array,
      default: null,
    },
    value: [Number, String, Array],
    // 當未找到匹配的數據時，顯示value
    showValue: {
      type: Boolean,
      default: true,
    },
    separator: {
      type: String,
      default: ","
    }
  },
  data() {
    return {
      unmatchArray: [], // 記錄未匹配的項
    }
  },
  computed: {
    values() {
      if (this.value === null || typeof this.value === 'undefined' || this.value === '') return []
      return Array.isArray(this.value) ? this.value.map(item => '' + item) : String(this.value).split(this.separator)
    },
    unmatch() {
      this.unmatchArray = []
      // 沒有value不顯示
      if (this.value === null || typeof this.value === 'undefined' || this.value === '' || this.options.length === 0) return false
      // 傳入值為陣列
      let unmatch = false // 新增一個標誌來判斷是否有未匹配項
      this.values.forEach(item => {
        if (!this.options.some(v => v.value === item)) {
          this.unmatchArray.push(item)
          unmatch = true // 如果有未匹配項，將標誌設定為true
        }
      })
      return unmatch // 返回標誌的值
    },

  },
  methods: {
    /**
     * 取得 el-tag 的 type 屬性值
     * Element Plus 只接受 ['primary', 'success', 'info', 'warning', 'danger'] 或空字串
     * 將空字串轉換為 undefined，避免 prop 驗證警告
     */
    getTagType(listClass) {
      if (!listClass || listClass === '' || listClass === 'default' || listClass === 'primary') {
        return undefined  // 使用預設類型（Element Plus 會顯示為 primary 樣式）
      }
      // 確保值在允許的範圍內
      const validTypes = ['success', 'info', 'warning', 'danger']
      return validTypes.includes(listClass) ? listClass : undefined
    }
  },
  filters: {
    handleArray(array) {
      if (array.length === 0) return ''
      return array.reduce((pre, cur) => {
        return pre + ' ' + cur
      })
    },
  }
}
</script>
<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
