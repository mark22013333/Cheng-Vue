<template>
  <div class="template-list">
    <!-- 搜尋區 -->
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜尋範本..."
        :prefix-icon="Search"
        clearable
        @input="handleSearch"
      />
      <el-select v-model="filterType" placeholder="類型" clearable style="width: 100px; margin-left: 8px" @change="handleSearch">
        <el-option v-for="item in msgTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <!-- 範本列表 -->
    <div class="list-container" v-loading="loading">
      <div
        v-for="item in filteredList"
        :key="item.templateId"
        :class="['list-item', { active: selectedId === item.templateId }]"
        @click="handleSelect(item)"
      >
        <div class="item-icon">
          <el-icon :size="20"><component :is="getMsgTypeIcon(item.msgType)" /></el-icon>
        </div>
        <div class="item-content">
          <div class="item-title">{{ item.templateName }}</div>
          <div class="item-meta">
            <el-tag :type="getMsgTypeTag(item.msgType)" size="small">{{ getMsgTypeLabel(item.msgType) }}</el-tag>
            <span v-if="item.templateCode" class="code">{{ item.templateCode }}</span>
          </div>
        </div>
        <div class="item-status">
          <el-tag v-if="item.status === 1" type="success" size="small" effect="plain">啟用</el-tag>
          <el-tag v-else type="info" size="small" effect="plain">停用</el-tag>
        </div>
      </div>

      <el-empty v-if="filteredList.length === 0 && !loading" description="無符合條件的範本" :image-size="60" />
    </div>

    <!-- 新增按鈕 -->
    <div class="add-button">
      <el-button type="primary" :icon="Plus" @click="$emit('add')">新增範本</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, Plus, ChatLineSquare, Picture, VideoCamera, Headset, Location, PriceTag, Grid, Document } from '@element-plus/icons-vue'

const props = defineProps({
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  selectedId: { type: [Number, String], default: null }
})

const emit = defineEmits(['select', 'add'])

const msgTypeOptions = [
  { value: 'TEXT', label: '文字', icon: ChatLineSquare, tag: 'primary' },
  { value: 'IMAGE', label: '圖片', icon: Picture, tag: 'success' },
  { value: 'VIDEO', label: '影片', icon: VideoCamera, tag: 'success' },
  { value: 'AUDIO', label: '音訊', icon: Headset, tag: 'success' },
  { value: 'LOCATION', label: '位置', icon: Location, tag: 'info' },
  { value: 'STICKER', label: '貼圖', icon: PriceTag, tag: 'info' },
  { value: 'IMAGEMAP', label: '圖片地圖', icon: Grid, tag: 'warning' },
  { value: 'FLEX', label: 'Flex', icon: Document, tag: 'danger' }
]

const searchText = ref('')
const filterType = ref('')

const filteredList = computed(() => {
  let result = props.list
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(item =>
      item.templateName?.toLowerCase().includes(keyword) ||
      item.templateCode?.toLowerCase().includes(keyword)
    )
  }
  if (filterType.value) {
    result = result.filter(item => item.msgType === filterType.value)
  }
  return result
})

const getMsgTypeLabel = (type) => msgTypeOptions.find(o => o.value === type)?.label || type
const getMsgTypeTag = (type) => msgTypeOptions.find(o => o.value === type)?.tag || 'info'
const getMsgTypeIcon = (type) => msgTypeOptions.find(o => o.value === type)?.icon || Document

const handleSearch = () => {
  // 搜尋時觸發
}

const handleSelect = (item) => {
  emit('select', item)
}
</script>

<style scoped lang="scss">
.template-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-right: 1px solid #ebeef5;
}

.search-bar {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.list-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #ecf5ff;
    border: 1px solid #409eff;
  }

  .item-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #606266;
  }

  .item-content {
    flex: 1;
    min-width: 0;

    .item-title {
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;

      .code {
        color: #909399;
        background: #f0f2f5;
        padding: 1px 4px;
        border-radius: 2px;
        font-family: monospace;
        font-size: 11px;
      }
    }
  }

  .item-status {
    flex-shrink: 0;
  }
}

.add-button {
  padding: 12px;
  border-top: 1px solid #ebeef5;

  .el-button {
    width: 100%;
  }
}
</style>
