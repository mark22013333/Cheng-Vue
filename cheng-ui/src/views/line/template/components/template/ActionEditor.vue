<template>
  <div class="action-editor">
    <div class="split-panel">
      <!-- 左側：動作列表 -->
      <div class="action-list-panel">
        <div class="panel-header">
          <span class="panel-title">動作按鈕</span>
          <span class="action-count">{{ actions.length }} / {{ maxActions }}</span>
        </div>

        <draggable
          v-model="actions"
          item-key="id"
          handle=".drag-handle"
          animation="200"
          class="action-list"
          @end="emitChange"
        >
          <template #item="{ element: action, index }">
            <div 
              class="action-list-item"
              :class="{ 
                active: selectedIndex === index,
                'has-error': hasValidationError(action)
              }"
              @click="selectAction(index)"
            >
              <el-icon class="drag-handle"><Rank /></el-icon>
              <div class="item-info">
                <span class="item-title">{{ action.label || '動作 ' + (index + 1) }}</span>
                <span class="item-type">{{ getActionTypeLabel(action.type) }}</span>
              </div>
              <el-icon v-if="hasValidationError(action)" class="error-icon"><WarningFilled /></el-icon>
              <el-button 
                class="item-delete"
                type="danger" 
                size="small" 
                circle 
                :icon="Delete" 
                @click.stop="removeAction(index)"
              />
            </div>
          </template>
        </draggable>

        <el-button 
          v-if="actions.length < maxActions"
          class="add-action-btn"
          type="primary"
          :icon="Plus"
          @click="addAction"
        >
          新增動作按鈕
        </el-button>

        <div v-if="actions.length === 0" class="empty-list">
          <el-empty description="請新增動作按鈕" :image-size="60" />
        </div>
      </div>

      <!-- 右側：設定面板 -->
      <div class="settings-panel">
        <template v-if="selectedAction">
          <div class="panel-header">
            <span class="panel-title">動作 {{ selectedIndex + 1 }} 設定</span>
          </div>

          <div class="settings-content">
            <el-form label-position="top" class="settings-form">
              <el-form-item>
                <template #label>
                  <span class="form-label">標籤 <span class="required-mark">*</span></span>
                </template>
                <el-input 
                  v-model="selectedAction.label" 
                  placeholder="按鈕文字"
                  :maxlength="20"
                  :class="{ 'is-error': !selectedAction.label }"
                  show-word-limit
                  @input="emitChange"
                />
                <div v-if="!selectedAction.label" class="field-error">
                  <el-icon><WarningFilled /></el-icon>
                  此欄位為必填
                </div>
              </el-form-item>

              <el-form-item>
                <template #label>
                  <span class="form-label">類型 <span class="required-mark">*</span></span>
                </template>
                <el-select v-model="selectedAction.type" @change="handleActionTypeChange(selectedAction)" style="width: 100%">
                  <el-option label="發送訊息" value="message" />
                  <el-option label="開啟連結" value="uri" />
                  <el-option label="Postback" value="postback" />
                  <el-option label="日期選擇" value="datetimepicker" />
                </el-select>
              </el-form-item>

              <!-- Message 類型 -->
              <template v-if="selectedAction.type === 'message'">
                <el-form-item>
                  <template #label>
                    <span class="form-label">發送文字 <span class="required-mark">*</span></span>
                  </template>
                  <el-input 
                    v-model="selectedAction.text" 
                    placeholder="點擊後發送的訊息"
                    :maxlength="300"
                    :class="{ 'is-error': !selectedAction.text }"
                    show-word-limit
                    @input="emitChange"
                  />
                  <div v-if="!selectedAction.text" class="field-error">
                    <el-icon><WarningFilled /></el-icon>
                    此欄位為必填
                  </div>
                </el-form-item>
              </template>

              <!-- URI 類型 -->
              <template v-else-if="selectedAction.type === 'uri'">
                <el-form-item>
                  <template #label>
                    <span class="form-label">連結網址 <span class="required-mark">*</span></span>
                  </template>
                  <el-input 
                    v-model="selectedAction.uri" 
                    placeholder="https://..."
                    :class="{ 'is-error': !selectedAction.uri }"
                    @input="emitChange"
                  />
                  <div v-if="!selectedAction.uri" class="field-error">
                    <el-icon><WarningFilled /></el-icon>
                    此欄位為必填
                  </div>
                </el-form-item>
              </template>

              <!-- Postback 類型 -->
              <template v-else-if="selectedAction.type === 'postback'">
                <el-form-item>
                  <template #label>
                    <span class="form-label">回傳資料 <span class="required-mark">*</span></span>
                  </template>
                  <el-input 
                    v-model="selectedAction.data" 
                    placeholder="action=xxx"
                    :maxlength="300"
                    :class="{ 'is-error': !selectedAction.data }"
                    show-word-limit
                    @input="emitChange"
                  />
                  <div v-if="!selectedAction.data" class="field-error">
                    <el-icon><WarningFilled /></el-icon>
                    此欄位為必填
                  </div>
                </el-form-item>
                <el-form-item>
                  <template #label>
                    <span class="form-label">顯示文字</span>
                    <span class="optional-mark">（選填）</span>
                  </template>
                  <el-input 
                    v-model="selectedAction.displayText" 
                    placeholder="點擊後在聊天室顯示"
                    :maxlength="300"
                    show-word-limit
                    @input="emitChange"
                  />
                </el-form-item>
              </template>

              <!-- Datetimepicker 類型 -->
              <template v-else-if="selectedAction.type === 'datetimepicker'">
                <el-form-item>
                  <template #label>
                    <span class="form-label">模式</span>
                  </template>
                  <el-select v-model="selectedAction.mode" @change="emitChange" style="width: 100%">
                    <el-option label="日期時間" value="datetime" />
                    <el-option label="僅日期" value="date" />
                    <el-option label="僅時間" value="time" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <template #label>
                    <span class="form-label">回傳資料 <span class="required-mark">*</span></span>
                  </template>
                  <el-input 
                    v-model="selectedAction.data" 
                    placeholder="action=selectDate"
                    :class="{ 'is-error': !selectedAction.data }"
                    @input="emitChange"
                  />
                  <div v-if="!selectedAction.data" class="field-error">
                    <el-icon><WarningFilled /></el-icon>
                    此欄位為必填
                  </div>
                </el-form-item>
              </template>
            </el-form>
          </div>
        </template>

        <div v-else class="empty-settings">
          <el-empty description="請從左側選擇動作進行編輯" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import draggable from 'vuedraggable'
import { Plus, Delete, Rank, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  maxActions: {
    type: Number,
    default: 4
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

let actionIdCounter = 0
const actions = ref([])
const selectedIndex = ref(0)

const generateId = () => `action_${++actionIdCounter}_${Date.now()}`

// 選中的動作
const selectedAction = computed(() => {
  return actions.value[selectedIndex.value] || null
})

// 選擇動作
const selectAction = (index) => {
  selectedIndex.value = index
}

// 取得動作類型標籤
const getActionTypeLabel = (type) => {
  const labels = {
    message: '發送訊息',
    uri: '開啟連結',
    postback: 'Postback',
    datetimepicker: '日期選擇'
  }
  return labels[type] || type
}

// 檢查必填欄位驗證
const hasValidationError = (action) => {
  if (!action.label) return true
  switch (action.type) {
    case 'message':
      return !action.text
    case 'uri':
      return !action.uri
    case 'postback':
      return !action.data
    case 'datetimepicker':
      return !action.data
    default:
      return false
  }
}

const addAction = () => {
  if (actions.value.length >= props.maxActions) return
  const newIndex = actions.value.length
  actions.value.push({
    id: generateId(),
    type: 'message',
    label: '',
    text: ''
  })
  selectedIndex.value = newIndex
  emitChange()
}

const removeAction = (index) => {
  actions.value.splice(index, 1)
  // 調整選中索引
  if (selectedIndex.value >= actions.value.length) {
    selectedIndex.value = Math.max(0, actions.value.length - 1)
  }
  emitChange()
}

const handleActionTypeChange = (action) => {
  // 清除舊類型的屬性
  delete action.text
  delete action.uri
  delete action.data
  delete action.displayText
  delete action.mode
  delete action.initial
  delete action.max
  delete action.min

  // 設定新類型的預設值
  switch (action.type) {
    case 'message':
      action.text = ''
      break
    case 'uri':
      action.uri = ''
      break
    case 'postback':
      action.data = ''
      action.displayText = ''
      break
    case 'datetimepicker':
      action.data = ''
      action.mode = 'datetime'
      break
  }
  emitChange()
}

// 標記是否正在內部更新，避免 watch 循環
let isInternalUpdate = false

const emitChange = () => {
  // 輸出時移除內部 id
  isInternalUpdate = true
  const output = actions.value.map(({ id, ...rest }) => rest)
  emit('update:modelValue', output)
  emit('change', output)
  // 使用 setTimeout 確保 Vue 完成更新後再重置標記
  setTimeout(() => { isInternalUpdate = false }, 0)
}

watch(() => props.modelValue, (newVal) => {
  // 如果是內部觸發的更新，跳過
  if (isInternalUpdate) return
  
  if (newVal && Array.isArray(newVal)) {
    // 只在初始化或外部更新時才重新生成 actions
    actions.value = newVal.map(a => ({
      id: generateId(),
      ...a
    }))
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.action-editor {
  .split-panel {
    display: flex;
    gap: 16px;
    min-height: 300px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    overflow: hidden;
  }

  .action-list-panel {
    width: 220px;
    flex-shrink: 0;
    background: #fafafa;
    border-right: 1px solid #ebeef5;
    display: flex;
    flex-direction: column;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 12px;
      border-bottom: 1px solid #ebeef5;
      background: #f5f7fa;

      .panel-title {
        font-weight: 600;
        font-size: 13px;
        color: #303133;
      }

      .action-count {
        font-size: 11px;
        color: #909399;
      }
    }

    .action-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
    }

    .action-list-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 10px;
      margin-bottom: 4px;
      background: #fff;
      border: 2px solid transparent;
      border-radius: 6px;
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
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .item-type {
          font-size: 11px;
          color: #909399;
        }
      }

      .error-icon {
        color: #f56c6c;
        flex-shrink: 0;
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

    .add-action-btn {
      margin: 8px;
    }

    .empty-list {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }
  }

  .settings-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #fff;

    .panel-header {
      padding: 10px 12px;
      border-bottom: 1px solid #ebeef5;
      background: #f5f7fa;

      .panel-title {
        font-weight: 600;
        font-size: 13px;
        color: #303133;
      }
    }

    .settings-content {
      flex: 1;
      padding: 12px;
      overflow-y: auto;
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

      :deep(.el-form-item) {
        margin-bottom: 12px;
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
