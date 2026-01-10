<template>
  <div class="image-carousel-template-editor">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
      圖片輪播模板可包含 1~10 張圖片，使用者點擊圖片可觸發連結動作。每張圖片僅能設定一個 URI 動作。
    </el-alert>

    <div class="split-panel">
      <!-- 左側：圖片列表 -->
      <div class="image-list-panel">
        <div class="panel-header">
          <span class="panel-title">圖片列表</span>
          <span class="columns-count">{{ data.columns.length }} / 10</span>
        </div>
        
        <draggable
          v-model="data.columns"
          item-key="id"
          handle=".drag-handle"
          animation="200"
          class="image-list"
          @end="emitChange"
        >
          <template #item="{ element: column, index }">
            <div 
              class="image-list-item" 
              :class="{ 
                active: selectedIndex === index,
                'has-error': hasValidationError(column)
              }"
              @click="selectImage(index)"
            >
              <el-icon class="drag-handle"><Rank /></el-icon>
              <div class="item-thumbnail">
                <el-image 
                  v-if="column.imageUrl" 
                  :src="column.imageUrl" 
                  fit="cover"
                />
                <el-icon v-else :size="20"><Picture /></el-icon>
              </div>
              <div class="item-info">
                <span class="item-title">圖片 {{ index + 1 }}</span>
                <span v-if="hasValidationError(column)" class="item-error">
                  <el-icon><WarningFilled /></el-icon>
                  必填欄位未填寫
                </span>
                <span v-else-if="column.imageUrl" class="item-url">{{ truncateUrl(column.imageUrl) }}</span>
                <span v-else class="item-empty">尚未設定</span>
              </div>
              <el-button 
                class="item-delete"
                type="danger" 
                size="small" 
                circle 
                :icon="Delete" 
                @click.stop="removeColumn(index)"
                :disabled="data.columns.length <= 1"
              />
            </div>
          </template>
        </draggable>

        <el-button 
          class="add-image-btn"
          type="primary" 
          :icon="Plus" 
          @click="addColumn" 
          :disabled="data.columns.length >= 10"
        >
          新增圖片
        </el-button>
      </div>

      <!-- 右側：設定面板 -->
      <div class="settings-panel">
        <template v-if="selectedColumn">
          <div class="panel-header">
            <span class="panel-title">圖片 {{ selectedIndex + 1 }} 設定</span>
          </div>

          <div class="settings-content">
            <!-- 圖片預覽 -->
            <div class="large-preview">
              <el-image 
                v-if="selectedColumn.imageUrl" 
                :src="selectedColumn.imageUrl" 
                fit="contain"
                :preview-src-list="[selectedColumn.imageUrl]"
              />
              <div v-else class="preview-placeholder">
                <el-icon :size="48"><Picture /></el-icon>
                <span>請輸入圖片網址</span>
              </div>
            </div>

            <!-- 表單欄位 -->
            <el-form label-position="top" class="settings-form">
              <el-form-item>
                <template #label>
                  <span class="form-label">
                    圖片網址 <span class="required-mark">*</span>
                  </span>
                </template>
                <div class="input-with-picker">
                  <el-input 
                    v-model="selectedColumn.imageUrl" 
                    placeholder="https://... (HTTPS, JPEG/PNG, 最大寬度 1024px)"
                    :class="{ 'is-error': !selectedColumn.imageUrl }"
                    @input="emitChange"
                  />
                  <el-button type="primary" plain @click="openImagePicker">
                    <el-icon><FolderOpened /></el-icon>
                    選擇素材
                  </el-button>
                </div>
                <div v-if="!selectedColumn.imageUrl" class="field-error">
                  <el-icon><WarningFilled /></el-icon>
                  此欄位為必填
                </div>
              </el-form-item>

              <el-form-item>
                <template #label>
                  <span class="form-label">
                    點擊連結 <span class="required-mark">*</span>
                  </span>
                </template>
                <el-input 
                  v-model="selectedColumn.action.uri" 
                  placeholder="https://... (點擊圖片後開啟的連結)"
                  :class="{ 'is-error': !selectedColumn.action.uri }"
                  @input="emitChange"
                >
                  <template #prepend>URI</template>
                </el-input>
                <div v-if="!selectedColumn.action.uri" class="field-error">
                  <el-icon><WarningFilled /></el-icon>
                  此欄位為必填
                </div>
              </el-form-item>

              <el-form-item>
                <template #label>
                  <span class="form-label">
                    動作標籤 <span class="required-mark">*</span>
                  </span>
                </template>
                <el-input 
                  v-model="selectedColumn.action.label" 
                  placeholder="按鈕顯示文字"
                  :maxlength="50"
                  :class="{ 'is-error': !selectedColumn.action.label }"
                  show-word-limit
                  @input="emitChange"
                />
                <div v-if="!selectedColumn.action.label" class="field-error">
                  <el-icon><WarningFilled /></el-icon>
                  此欄位為必填
                </div>
              </el-form-item>
            </el-form>
          </div>
        </template>

        <div v-else class="empty-settings">
          <el-empty description="請從左側選擇圖片進行編輯" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 圖片選擇器對話框 -->
    <MediaSelector
      v-model="imagePickerVisible"
      media-type="image"
      @select="handleImageSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import draggable from 'vuedraggable'
import { Plus, Delete, Rank, Picture, WarningFilled, FolderOpened } from '@element-plus/icons-vue'
import MediaSelector from '@/views/line/template/components/MediaSelector.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 圖片選擇器
const imagePickerVisible = ref(false)

const openImagePicker = () => {
  imagePickerVisible.value = true
}

const handleImageSelect = (data) => {
  if (selectedColumn.value && data?.url) {
    selectedColumn.value.imageUrl = data.url
    emitChange()
  }
  imagePickerVisible.value = false
}

let columnIdCounter = 0
const generateId = () => `col_${++columnIdCounter}_${Date.now()}`

const data = ref({
  columns: []
})

// 選中的圖片索引
const selectedIndex = ref(0)

// 選中的圖片資料
const selectedColumn = computed(() => {
  return data.value.columns[selectedIndex.value] || null
})

// 選擇圖片
const selectImage = (index) => {
  selectedIndex.value = index
}

// 檢查必填欄位驗證
const hasValidationError = (column) => {
  return !column.imageUrl || !column.action?.uri || !column.action?.label
}

// 截斷 URL 顯示
const truncateUrl = (url) => {
  if (!url) return ''
  if (url.length <= 30) return url
  return url.substring(0, 30) + '...'
}

const addColumn = () => {
  if (data.value.columns.length >= 10) return
  const newIndex = data.value.columns.length
  data.value.columns.push({
    id: generateId(),
    imageUrl: '',
    action: {
      type: 'uri',
      label: '',
      uri: ''
    }
  })
  selectedIndex.value = newIndex
  emitChange()
}

const removeColumn = (index) => {
  if (data.value.columns.length <= 1) return
  data.value.columns.splice(index, 1)
  // 調整選中索引
  if (selectedIndex.value >= data.value.columns.length) {
    selectedIndex.value = Math.max(0, data.value.columns.length - 1)
  }
  emitChange()
}

// 標記是否正在內部更新，避免 watch 循環
let isInternalUpdate = false

const emitChange = () => {
  isInternalUpdate = true
  // 輸出時移除內部 id
  const output = {
    columns: data.value.columns.map(({ id, ...rest }) => rest)
  }
  emit('update:modelValue', output)
  emit('change', output)
  setTimeout(() => { isInternalUpdate = false }, 0)
}

watch(() => props.modelValue, (newVal) => {
  if (isInternalUpdate) return
  
  if (newVal && newVal.columns) {
    data.value.columns = newVal.columns.map(col => ({
      id: generateId(),
      imageUrl: col.imageUrl || '',
      action: {
        type: 'uri',
        label: col.action?.label || '',
        uri: col.action?.uri || ''
      }
    }))
  }
  // 確保至少有一個 column
  if (data.value.columns.length === 0) {
    addColumn()
  }
  // 確保選中索引有效
  if (selectedIndex.value >= data.value.columns.length) {
    selectedIndex.value = 0
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.image-carousel-template-editor {
  .split-panel {
    display: flex;
    gap: 16px;
    min-height: 400px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    overflow: hidden;
  }

  .image-list-panel {
    width: 280px;
    flex-shrink: 0;
    background: #fafafa;
    border-right: 1px solid #ebeef5;
    display: flex;
    flex-direction: column;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid #ebeef5;
      background: #f5f7fa;

      .panel-title {
        font-weight: 600;
        font-size: 14px;
        color: #303133;
      }

      .columns-count {
        font-size: 12px;
        color: #909399;
      }
    }

    .image-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
    }

    .image-list-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px;
      margin-bottom: 6px;
      background: #fff;
      border: 2px solid transparent;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #f0f7ff;
      }

      &.active {
        border-color: #409eff;
        background: #ecf5ff;
      }

      &.has-error {
        border-color: #f56c6c;
        background: #fef0f0;
      }

      .drag-handle {
        cursor: move;
        color: #c0c4cc;
        flex-shrink: 0;

        &:hover {
          color: #909399;
        }
      }

      .item-thumbnail {
        width: 48px;
        height: 48px;
        flex-shrink: 0;
        border-radius: 6px;
        overflow: hidden;
        background: #f0f0f0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;

        :deep(.el-image) {
          width: 100%;
          height: 100%;
        }
      }

      .item-info {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 2px;

        .item-title {
          font-size: 13px;
          font-weight: 500;
          color: #303133;
        }

        .item-url {
          font-size: 11px;
          color: #909399;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .item-empty {
          font-size: 11px;
          color: #c0c4cc;
        }

        .item-error {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 11px;
          color: #f56c6c;
        }
      }

      .item-delete {
        flex-shrink: 0;
        opacity: 0;
        transition: opacity 0.2s;
      }

      &:hover .item-delete {
        opacity: 1;
      }
    }

    .add-image-btn {
      margin: 8px;
    }
  }

  .settings-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #fff;

    .panel-header {
      padding: 12px 16px;
      border-bottom: 1px solid #ebeef5;
      background: #f5f7fa;

      .panel-title {
        font-weight: 600;
        font-size: 14px;
        color: #303133;
      }
    }

    .settings-content {
      flex: 1;
      padding: 16px;
      overflow-y: auto;
    }

    .large-preview {
      height: 180px;
      background: #f5f7fa;
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      justify-content: center;

      :deep(.el-image) {
        max-width: 100%;
        max-height: 100%;
      }

      .preview-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        color: #c0c4cc;
        font-size: 13px;
      }
    }

    .settings-form {
      .form-label {
        font-size: 13px;
        color: #606266;
      }

      .required-mark {
        color: #f56c6c;
        margin-left: 2px;
      }

      .optional-mark {
        color: #909399;
        font-size: 12px;
        margin-left: 4px;
      }

      .field-error {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-top: 4px;
        font-size: 12px;
        color: #f56c6c;
      }

      :deep(.el-input.is-error .el-input__wrapper) {
        box-shadow: 0 0 0 1px #f56c6c inset;
      }

      .input-with-picker {
        display: flex;
        gap: 8px;

        :deep(.el-input) {
          flex: 1;
        }
      }
    }

    .empty-settings {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}
</style>
