<template>
  <div class="carousel-template-editor">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
      輪播模板可包含 1~10 個卡片，使用者可左右滑動瀏覽。每個卡片最多 3 個按鈕。
    </el-alert>

    <!-- 全域設定 -->
    <el-row :gutter="12" style="margin-bottom: 16px;">
      <el-col :span="8">
        <el-form-item label="圖片長寬比">
          <el-select v-model="data.imageAspectRatio" @change="emitChange">
            <el-option label="橫向 (1.51:1)" value="rectangle" />
            <el-option label="正方形 (1:1)" value="square" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="圖片填滿模式">
          <el-select v-model="data.imageSize" @change="emitChange">
            <el-option label="填滿 (Cover)" value="cover" />
            <el-option label="包含 (Contain)" value="contain" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <div style="display: flex; justify-content: flex-end; padding-top: 32px;">
          <el-button type="primary" :icon="Plus" @click="addColumn" :disabled="data.columns.length >= 10">
            新增卡片
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 卡片列表（Tab 切換） -->
    <el-tabs v-model="activeColumnIndex" type="card" editable @tab-remove="removeColumn" @tab-add="addColumn">
      <el-tab-pane 
        v-for="(column, index) in data.columns" 
        :key="index" 
        :label="'卡片 ' + (index + 1)"
        :name="String(index)"
        :closable="data.columns.length > 1"
      >
        <el-row :gutter="20">
          <el-col :span="14">
            <!-- 圖片 -->
            <el-form-item label="縮圖網址">
              <div class="input-with-picker">
                <el-input 
                  v-model="column.thumbnailImageUrl" 
                  placeholder="https://... (HTTPS)"
                  @input="emitChange"
                />
                <el-button type="primary" plain @click="openImagePicker(index)">
                  <el-icon><FolderOpened /></el-icon>
                  選擇素材
                </el-button>
              </div>
            </el-form-item>

            <!-- 背景色（當使用 contain 模式時顯示） -->
            <el-form-item v-if="data.imageSize === 'contain'" label="圖片背景色">
              <el-color-picker v-model="column.imageBackgroundColor" @change="emitChange" />
            </el-form-item>

            <!-- 標題 -->
            <el-form-item label="標題">
              <el-input 
                v-model="column.title" 
                placeholder="選填，最多 40 字"
                :maxlength="40"
                show-word-limit
                @input="emitChange"
              />
            </el-form-item>

            <!-- 內文 -->
            <el-form-item label="內文" required>
              <el-input 
                v-model="column.text" 
                type="textarea"
                :rows="2"
                placeholder="最多 120 字（有圖片或標題時 60 字）"
                :maxlength="hasImageOrTitle(column) ? 60 : 120"
                show-word-limit
                @input="emitChange"
              />
            </el-form-item>

            <!-- 動作按鈕 -->
            <div class="section-title">
              動作按鈕
              <el-tag type="info" size="small" style="margin-left: 8px;">
                {{ column.actions?.length || 0 }} / 3 個
              </el-tag>
            </div>
            <ActionEditor 
              v-model="column.actions"
              :max-actions="3"
              @change="emitChange"
            />
          </el-col>

          <el-col :span="10">
            <div class="column-preview">
              <div class="preview-title">卡片預覽</div>
              <div class="carousel-card">
                <div v-if="column.thumbnailImageUrl" class="card-image">
                  <el-image :src="column.thumbnailImageUrl" fit="cover" />
                </div>
                <div v-else class="card-image placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
                <div class="card-body">
                  <div v-if="column.title" class="card-title">{{ column.title }}</div>
                  <div class="card-text">{{ column.text || '請輸入內文' }}</div>
                </div>
                <div class="card-actions">
                  <div 
                    v-for="(action, idx) in (column.actions || [])" 
                    :key="idx"
                    class="card-action-btn"
                  >
                    {{ action.label || '按鈕 ' + (idx + 1) }}
                  </div>
                  <div v-if="!column.actions?.length" class="card-action-btn empty">
                    請新增按鈕
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <div v-if="data.columns.length === 0" class="empty-columns">
      <el-empty description="請新增至少一個卡片">
        <el-button type="primary" @click="addColumn">新增卡片</el-button>
      </el-empty>
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
import { ref, watch } from 'vue'
import { Plus, Picture, FolderOpened } from '@element-plus/icons-vue'
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
const imagePickerTargetIndex = ref(null)

const openImagePicker = (index) => {
  imagePickerTargetIndex.value = index
  imagePickerVisible.value = true
}

const handleImageSelect = (selected) => {
  if (imagePickerTargetIndex.value !== null && selected?.url) {
    const column = data.value.columns[imagePickerTargetIndex.value]
    if (column) {
      column.thumbnailImageUrl = selected.url
      emitChange()
    }
  }
  imagePickerVisible.value = false
}

const data = ref({
  imageAspectRatio: 'rectangle',
  imageSize: 'cover',
  columns: []
})

const activeColumnIndex = ref('0')

const hasImageOrTitle = (column) => {
  return !!column.thumbnailImageUrl || !!column.title
}

const addColumn = () => {
  if (data.value.columns.length >= 10) return
  data.value.columns.push({
    thumbnailImageUrl: '',
    imageBackgroundColor: '#FFFFFF',
    title: '',
    text: '',
    defaultAction: null,
    actions: []
  })
  activeColumnIndex.value = String(data.value.columns.length - 1)
  emitChange()
}

const removeColumn = (index) => {
  const idx = parseInt(index)
  if (data.value.columns.length <= 1) return
  data.value.columns.splice(idx, 1)
  if (parseInt(activeColumnIndex.value) >= data.value.columns.length) {
    activeColumnIndex.value = String(data.value.columns.length - 1)
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
      imageAspectRatio: newVal.imageAspectRatio || 'rectangle',
      imageSize: newVal.imageSize || 'cover',
      columns: newVal.columns || []
    }
    // 確保至少有一個 column
    if (data.value.columns.length === 0) {
      addColumn()
    }
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.carousel-template-editor {
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

  .column-preview {
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
  }

  .carousel-card {
    width: 220px;
    margin: 0 auto;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .card-image {
      height: 110px;
      background: #f0f0f0;

      &.placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;
      }

      :deep(.el-image) {
        width: 100%;
        height: 100%;
      }
    }

    .card-body {
      padding: 12px;

      .card-title {
        font-weight: bold;
        font-size: 14px;
        margin-bottom: 6px;
        color: #303133;
      }

      .card-text {
        font-size: 12px;
        color: #606266;
        line-height: 1.5;
        word-break: break-word;
      }
    }

    .card-actions {
      border-top: 1px solid #ebeef5;

      .card-action-btn {
        padding: 10px;
        text-align: center;
        color: #409eff;
        font-size: 13px;
        border-bottom: 1px solid #ebeef5;

        &:last-child {
          border-bottom: none;
        }

        &.empty {
          color: #c0c4cc;
        }
      }
    }
  }

  .empty-columns {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 40px;
  }
}
</style>
