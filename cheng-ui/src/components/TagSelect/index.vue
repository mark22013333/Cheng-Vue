<template>
  <div class="custom-tag-select" :style="{ width: width }">
    <!-- 已選標籤顯示區 -->
    <div 
      class="tag-select-input"
      :class="{ 'is-focus': isFocused, 'is-disabled': disabled }"
      @click="toggleDropdown"
    >
      <div class="selected-tags">
        <template v-if="multiple && Array.isArray(selectedValue) && selectedValue.length > 0">
          <span
            v-for="tagId in selectedValue"
            :key="tagId"
            class="custom-tag"
            :style="{ backgroundColor: getTagColor(tagId), borderColor: getTagColor(tagId) }"
          >
            <span class="tag-text">{{ getTagName(tagId) }}</span>
            <span class="tag-close" @click.stop="handleRemoveTag(tagId)">&times;</span>
          </span>
        </template>
        <template v-else-if="!multiple && selectedValue">
          <span
            class="custom-tag"
            :style="{ backgroundColor: getTagColor(selectedValue), borderColor: getTagColor(selectedValue) }"
          >
            <span class="tag-text">{{ getTagName(selectedValue) }}</span>
          </span>
        </template>
        <span v-else class="placeholder-text">{{ placeholder }}</span>
      </div>
      <span class="select-arrow" :class="{ 'is-reverse': showDropdown }">
        <i class="el-icon"><svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M831.872 340.864 512 652.672 192.128 340.864a30.592 30.592 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.592 30.592 0 0 0-42.752 0z"></path></svg></i>
      </span>
      <span v-if="clearable && hasValue" class="clear-btn" @click.stop="handleClear">&times;</span>
    </div>
    
    <!-- 下拉選單 -->
    <transition name="el-zoom-in-top">
      <div v-show="showDropdown" class="tag-dropdown">
        <!-- 搜尋框 -->
        <div v-if="filterable" class="search-box">
          <input
            ref="searchInput"
            v-model="searchText"
            type="text"
            class="search-input"
            placeholder="搜尋標籤..."
            @click.stop
          />
        </div>
        <!-- 選項列表 -->
        <div class="options-list">
          <div
            v-for="tag in filteredOptions"
            :key="tag.tagId"
            class="option-item"
            :class="{ 'is-selected': isSelected(tag.tagId) }"
            @click.stop="handleSelect(tag.tagId)"
          >
            <span
              class="option-tag"
              :style="{ backgroundColor: tag.tagColor || '#909399' }"
            >
              {{ tag.tagName }}
            </span>
            <span v-if="showTagCode && tag.tagCode" class="tag-code">{{ tag.tagCode }}</span>
            <span v-if="isSelected(tag.tagId)" class="check-icon">✓</span>
          </div>
          <div v-if="filteredOptions.length === 0" class="no-data">
            無符合的標籤
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'TagSelect',
  props: {
    modelValue: {
      type: [Number, Array, String],
      default: null
    },
    options: {
      type: Array,
      default: () => []
    },
    placeholder: {
      type: String,
      default: '選擇標籤'
    },
    clearable: {
      type: Boolean,
      default: true
    },
    multiple: {
      type: Boolean,
      default: false
    },
    filterable: {
      type: Boolean,
      default: false
    },
    width: {
      type: String,
      default: '200px'
    },
    disabled: {
      type: Boolean,
      default: false
    },
    showTagCode: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'change'],
  data() {
    return {
      showDropdown: false,
      isFocused: false,
      searchText: ''
    }
  },
  computed: {
    selectedValue: {
      get() {
        return this.modelValue
      },
      set(val) {
        this.$emit('update:modelValue', val)
      }
    },
    colorMap() {
      const map = {}
      this.options.forEach(opt => {
        map[opt.tagId] = opt.tagColor || '#909399'
      })
      return map
    },
    hasValue() {
      if (this.multiple) {
        return Array.isArray(this.selectedValue) && this.selectedValue.length > 0
      }
      return this.selectedValue != null
    },
    filteredOptions() {
      if (!this.searchText) return this.options
      const search = this.searchText.toLowerCase()
      return this.options.filter(tag => 
        tag.tagName.toLowerCase().includes(search) ||
        (tag.tagCode && tag.tagCode.toLowerCase().includes(search))
      )
    }
  },
  mounted() {
    document.addEventListener('click', this.handleClickOutside)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside)
  },
  methods: {
    getTagColor(tagId) {
      return this.colorMap[tagId] || '#909399'
    },
    getTagName(tagId) {
      const tag = this.options.find(t => t.tagId === tagId)
      return tag?.tagName || tagId
    },
    toggleDropdown() {
      if (this.disabled) return
      this.showDropdown = !this.showDropdown
      this.isFocused = this.showDropdown
      if (this.showDropdown && this.filterable) {
        this.$nextTick(() => {
          this.$refs.searchInput?.focus()
        })
      }
    },
    handleClickOutside(e) {
      if (!this.$el.contains(e.target)) {
        this.showDropdown = false
        this.isFocused = false
        this.searchText = ''
      }
    },
    handleSelect(tagId) {
      if (this.multiple) {
        const current = Array.isArray(this.selectedValue) ? [...this.selectedValue] : []
        const index = current.indexOf(tagId)
        if (index > -1) {
          current.splice(index, 1)
        } else {
          current.push(tagId)
        }
        this.selectedValue = current
      } else {
        this.selectedValue = tagId
        this.showDropdown = false
        this.isFocused = false
      }
      this.$emit('change', this.selectedValue)
    },
    handleRemoveTag(tagId) {
      if (this.multiple) {
        const newValue = this.selectedValue.filter(id => id !== tagId)
        this.selectedValue = newValue
        this.$emit('change', newValue)
      } else {
        this.selectedValue = null
        this.$emit('change', null)
      }
    },
    handleClear() {
      this.selectedValue = this.multiple ? [] : null
      this.$emit('change', this.selectedValue)
    },
    isSelected(tagId) {
      if (this.multiple) {
        return Array.isArray(this.selectedValue) && this.selectedValue.includes(tagId)
      }
      return this.selectedValue === tagId
    }
  }
}
</script>

<style scoped>
.custom-tag-select {
  position: relative;
  display: inline-block;
}

.tag-select-input {
  min-height: 32px;
  padding: 4px 30px 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  transition: border-color 0.2s;
}

.tag-select-input:hover {
  border-color: #c0c4cc;
}

.tag-select-input.is-focus {
  border-color: #409eff;
}

.tag-select-input.is-disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  flex: 1;
  min-height: 24px;
  align-items: center;
}

.placeholder-text {
  color: #a8abb2;
  font-size: 14px;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 6px 0 8px;
  height: 20px;
  line-height: 20px;
  border-radius: 2px 10px 10px 2px;
  font-size: 12px;
  color: #fff;
  white-space: nowrap;
  position: relative;
}

.custom-tag::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}

.tag-text {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: 2px;
}

.tag-close {
  margin-left: 3px;
  font-size: 12px;
  cursor: pointer;
  opacity: 0.7;
  line-height: 1;
}

.tag-close:hover {
  opacity: 1;
}

.select-arrow {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  transition: transform 0.3s;
}

.select-arrow.is-reverse {
  transform: translateY(-50%) rotate(180deg);
}

.select-arrow .el-icon {
  width: 14px;
  height: 14px;
}

.select-arrow svg {
  width: 100%;
  height: 100%;
}

.clear-btn {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  font-size: 14px;
  cursor: pointer;
}

.clear-btn:hover {
  color: #909399;
}

.tag-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 2000;
  max-height: 274px;
  overflow: hidden;
}

.search-box {
  padding: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.search-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
}

.search-input:focus {
  border-color: #409eff;
}

.options-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 6px 0;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.option-item:hover {
  background-color: #f5f7fa;
}

.option-item.is-selected {
  background-color: #ecf5ff;
}

.option-tag {
  display: inline-block;
  padding: 0 8px 0 10px;
  height: 20px;
  line-height: 20px;
  border-radius: 2px 10px 10px 2px;
  font-size: 12px;
  color: #fff;
  position: relative;
}

.option-tag::before {
  content: '';
  position: absolute;
  left: 2px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}

.tag-code {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.check-icon {
  margin-left: 8px;
  color: #409eff;
  font-weight: bold;
}

.no-data {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
