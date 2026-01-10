<template>
  <div class="confirm-template-editor">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
      確認模板包含一段文字和兩個動作按鈕（例如：是/否、確認/取消）
    </el-alert>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-form-item label="確認文字" required>
          <el-input 
            v-model="data.text" 
            type="textarea"
            :rows="4"
            placeholder="請輸入要確認的內容，最多 240 字"
            :maxlength="240"
            show-word-limit
            @input="emitChange"
          />
        </el-form-item>

        <div class="section-title">動作按鈕（固定 2 個）</div>

        <el-row :gutter="16">
          <el-col :span="12">
            <div class="action-card positive">
              <div class="action-card-title">
                <el-icon><CircleCheck /></el-icon>
                確認按鈕
              </div>
              <el-form-item label="類型" size="small">
                <el-select v-model="data.actions[0].type" @change="handleActionTypeChange(0)">
                  <el-option label="發送訊息" value="message" />
                  <el-option label="開啟連結" value="uri" />
                  <el-option label="Postback" value="postback" />
                </el-select>
              </el-form-item>
              <el-form-item label="標籤" size="small" required>
                <el-input 
                  v-model="data.actions[0].label" 
                  placeholder="按鈕文字"
                  :maxlength="20"
                  @input="emitChange"
                />
              </el-form-item>
              <ActionFields :action="data.actions[0]" @change="emitChange" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="action-card negative">
              <div class="action-card-title">
                <el-icon><CircleClose /></el-icon>
                取消按鈕
              </div>
              <el-form-item label="類型" size="small">
                <el-select v-model="data.actions[1].type" @change="handleActionTypeChange(1)">
                  <el-option label="發送訊息" value="message" />
                  <el-option label="開啟連結" value="uri" />
                  <el-option label="Postback" value="postback" />
                </el-select>
              </el-form-item>
              <el-form-item label="標籤" size="small" required>
                <el-input 
                  v-model="data.actions[1].label" 
                  placeholder="按鈕文字"
                  :maxlength="20"
                  @input="emitChange"
                />
              </el-form-item>
              <ActionFields :action="data.actions[1]" @change="emitChange" />
            </div>
          </el-col>
        </el-row>
      </el-col>

      <el-col :span="10">
        <div class="preview-section">
          <div class="preview-title">即時預覽</div>
          <div class="preview-content">
            <div class="confirm-preview">
              <div class="preview-text">{{ data.text || '請輸入確認文字' }}</div>
              <div class="preview-actions">
                <div class="preview-action-btn positive">
                  {{ data.actions[0]?.label || '是' }}
                </div>
                <div class="preview-action-btn negative">
                  {{ data.actions[1]?.label || '否' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import ActionFields from './ActionFields.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const data = ref({
  text: '',
  actions: [
    { type: 'message', label: '是', text: '是' },
    { type: 'message', label: '否', text: '否' }
  ]
})

const handleActionTypeChange = (index) => {
  const action = data.value.actions[index]
  // 清除舊類型的屬性
  delete action.text
  delete action.uri
  delete action.data
  delete action.displayText

  // 設定新類型的預設值
  switch (action.type) {
    case 'message':
      action.text = action.label || ''
      break
    case 'uri':
      action.uri = ''
      break
    case 'postback':
      action.data = ''
      break
  }
  emitChange()
}

// 標記是否正在內部更新，避免 watch 循環
let isInternalUpdate = false

const emitChange = () => {
  isInternalUpdate = true
  emit('update:modelValue', { ...data.value })
  emit('change', { ...data.value })
  setTimeout(() => { isInternalUpdate = false }, 0)
}

watch(() => props.modelValue, (newVal) => {
  if (isInternalUpdate) return
  
  if (newVal) {
    data.value = {
      text: newVal.text || '',
      actions: newVal.actions || [
        { type: 'message', label: '是', text: '是' },
        { type: 'message', label: '否', text: '否' }
      ]
    }
    // 確保有兩個 actions
    while (data.value.actions.length < 2) {
      data.value.actions.push({ type: 'message', label: '', text: '' })
    }
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.confirm-template-editor {
  .section-title {
    font-weight: 500;
    font-size: 14px;
    color: #303133;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }

  .action-card {
    background: #fafafa;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 12px;

    &.positive {
      border-color: #67c23a;
      .action-card-title {
        color: #67c23a;
      }
    }

    &.negative {
      border-color: #909399;
      .action-card-title {
        color: #909399;
      }
    }

    .action-card-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 500;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px dashed currentColor;
    }

    :deep(.el-form-item) {
      margin-bottom: 8px;
    }
  }

  .preview-section {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
    position: sticky;
    top: 16px;

    .preview-title {
      font-weight: 500;
      margin-bottom: 12px;
      text-align: center;
      color: #606266;
    }

    .preview-content {
      display: flex;
      justify-content: center;
    }
  }

  .confirm-preview {
    width: 240px;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .preview-text {
      padding: 16px;
      font-size: 14px;
      color: #303133;
      line-height: 1.6;
      word-break: break-word;
    }

    .preview-actions {
      display: flex;
      border-top: 1px solid #ebeef5;

      .preview-action-btn {
        flex: 1;
        padding: 12px;
        text-align: center;
        font-size: 14px;
        cursor: pointer;

        &.positive {
          color: #67c23a;
          border-right: 1px solid #ebeef5;
        }

        &.negative {
          color: #909399;
        }

        &:hover {
          background: #f5f7fa;
        }
      }
    }
  }
}
</style>
