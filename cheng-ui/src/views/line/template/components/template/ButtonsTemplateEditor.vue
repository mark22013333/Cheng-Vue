<template>
  <div class="buttons-template-editor">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
      按鈕模板可包含圖片、標題、文字和最多 4 個動作按鈕
    </el-alert>

    <el-row :gutter="20">
      <el-col :span="14">
        <!-- 圖片設定 -->
        <div class="section-title">圖片設定（選填）</div>
        <el-form-item label="縮圖網址">
          <div class="input-with-picker">
            <el-input 
              v-model="data.thumbnailImageUrl" 
              placeholder="https://... (HTTPS 圖片，最大寬度 1024px)"
              @input="emitChange"
            />
            <el-button type="primary" plain @click="openImagePicker">
              <el-icon><FolderOpened /></el-icon>
              選擇素材
            </el-button>
          </div>
        </el-form-item>
        
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="長寬比">
              <el-select v-model="data.imageAspectRatio" @change="emitChange">
                <el-option label="橫向 (1.51:1)" value="rectangle" />
                <el-option label="正方形 (1:1)" value="square" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="填滿模式">
              <el-select v-model="data.imageSize" @change="emitChange">
                <el-option label="填滿 (Cover)" value="cover" />
                <el-option label="包含 (Contain)" value="contain" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="背景色">
              <el-color-picker v-model="data.imageBackgroundColor" @change="emitChange" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 文字內容 -->
        <div class="section-title">文字內容</div>
        <el-form-item label="標題">
          <el-input 
            v-model="data.title" 
            placeholder="選填，最多 40 字"
            :maxlength="40"
            show-word-limit
            @input="emitChange"
          />
        </el-form-item>
        
        <el-form-item label="內文" required>
          <el-input 
            v-model="data.text" 
            type="textarea"
            :rows="3"
            :placeholder="textPlaceholder"
            :maxlength="textMaxLength"
            show-word-limit
            @input="emitChange"
          />
          <div class="form-tip">{{ textLimitTip }}</div>
        </el-form-item>

        <!-- 動作按鈕 -->
        <div class="section-title">
          動作按鈕
          <el-tag type="info" size="small" style="margin-left: 8px;">
            {{ data.actions.length }} / 4 個
          </el-tag>
        </div>

        <ActionEditor 
          v-model="data.actions"
          :max-actions="4"
          @change="emitChange"
        />
      </el-col>

      <el-col :span="10">
        <!-- 預覽區 -->
        <div class="preview-section">
          <div class="preview-title">即時預覽</div>
          <div class="preview-content">
            <div class="buttons-preview">
              <div v-if="data.thumbnailImageUrl" class="preview-image">
                <el-image :src="data.thumbnailImageUrl" fit="cover" />
              </div>
              <div class="preview-body">
                <div v-if="data.title" class="preview-title-text">{{ data.title }}</div>
                <div class="preview-text">{{ data.text || '請輸入內文' }}</div>
              </div>
              <div class="preview-actions">
                <div 
                  v-for="(action, idx) in data.actions" 
                  :key="idx"
                  class="preview-action-btn"
                >
                  {{ action.label || '按鈕 ' + (idx + 1) }}
                </div>
                <div v-if="data.actions.length === 0" class="preview-action-btn empty">
                  請新增按鈕
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

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
import { FolderOpened } from '@element-plus/icons-vue'
import ActionEditor from './ActionEditor.vue'
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

const handleImageSelect = (selected) => {
  if (selected?.url) {
    data.value.thumbnailImageUrl = selected.url
    emitChange()
  }
  imagePickerVisible.value = false
}

const data = ref({
  thumbnailImageUrl: '',
  imageAspectRatio: 'rectangle',
  imageSize: 'cover',
  imageBackgroundColor: '#FFFFFF',
  title: '',
  text: '',
  defaultAction: null,
  actions: []
})

// 文字限制依據是否有圖片/標題而不同
const hasImageOrTitle = computed(() => {
  return !!data.value.thumbnailImageUrl || !!data.value.title
})

const textMaxLength = computed(() => hasImageOrTitle.value ? 60 : 160)
const textPlaceholder = computed(() => 
  hasImageOrTitle.value 
    ? '有圖片或標題時最多 60 字' 
    : '無圖片和標題時最多 160 字'
)
const textLimitTip = computed(() => 
  hasImageOrTitle.value 
    ? '※ 有圖片或標題時，內文最多 60 字' 
    : '※ 無圖片和標題時，內文最多 160 字'
)

// 標記是否正在內部更新，避免 watch 循環
let isInternalUpdate = false

const emitChange = () => {
  isInternalUpdate = true
  emit('update:modelValue', { ...data.value })
  emit('change', { ...data.value })
  setTimeout(() => { isInternalUpdate = false }, 0)
}

watch(() => props.modelValue, (newVal) => {
  // 如果是內部觸發的更新，跳過
  if (isInternalUpdate) return
  
  if (newVal) {
    data.value = {
      thumbnailImageUrl: newVal.thumbnailImageUrl || '',
      imageAspectRatio: newVal.imageAspectRatio || 'rectangle',
      imageSize: newVal.imageSize || 'cover',
      imageBackgroundColor: newVal.imageBackgroundColor || '#FFFFFF',
      title: newVal.title || '',
      text: newVal.text || '',
      defaultAction: newVal.defaultAction || null,
      actions: newVal.actions || []
    }
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.buttons-template-editor {
  .input-with-picker {
    display: flex;
    gap: 8px;

    :deep(.el-input) {
      flex: 1;
    }
  }

  .section-title {
    font-weight: 500;
    font-size: 14px;
    color: #303133;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
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

  .buttons-preview {
    width: 240px;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .preview-image {
      height: 120px;
      background: #f0f0f0;

      :deep(.el-image) {
        width: 100%;
        height: 100%;
      }
    }

    .preview-body {
      padding: 12px;

      .preview-title-text {
        font-weight: bold;
        font-size: 14px;
        margin-bottom: 6px;
        color: #303133;
      }

      .preview-text {
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        word-break: break-word;
      }
    }

    .preview-actions {
      border-top: 1px solid #ebeef5;

      .preview-action-btn {
        padding: 12px;
        text-align: center;
        color: #409eff;
        font-size: 14px;
        cursor: pointer;
        border-bottom: 1px solid #ebeef5;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: #f5f7fa;
        }

        &.empty {
          color: #c0c4cc;
        }
      }
    }
  }
}
</style>
