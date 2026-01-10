<template>
  <div class="template-message-preview" :class="{ compact: compact }">
    <!-- Buttons 預覽 -->
    <div v-if="templateType === 'buttons'" class="buttons-preview">
      <div v-if="templateData.thumbnailImageUrl" class="preview-image">
        <el-image :src="templateData.thumbnailImageUrl" fit="cover" />
      </div>
      <div class="preview-body">
        <div v-if="templateData.title" class="preview-title">{{ templateData.title }}</div>
        <div class="preview-text">{{ templateData.text || '請輸入內文' }}</div>
      </div>
      <div class="preview-actions">
        <div 
          v-for="(action, idx) in (templateData.actions || [])" 
          :key="idx"
          class="preview-action-btn"
        >
          {{ action.label || '按鈕 ' + (idx + 1) }}
        </div>
        <div v-if="!templateData.actions?.length" class="preview-action-btn empty">
          請新增按鈕
        </div>
      </div>
    </div>

    <!-- Confirm 預覽 -->
    <div v-else-if="templateType === 'confirm'" class="confirm-preview">
      <div class="preview-text">{{ templateData.text || '請輸入確認文字' }}</div>
      <div class="preview-actions">
        <div class="preview-action-btn positive">
          {{ templateData.actions?.[0]?.label || '是' }}
        </div>
        <div class="preview-action-btn negative">
          {{ templateData.actions?.[1]?.label || '否' }}
        </div>
      </div>
    </div>

    <!-- Carousel 預覽 -->
    <div v-else-if="templateType === 'carousel'" class="carousel-preview">
      <div class="carousel-track">
        <div 
          v-for="(column, index) in (templateData.columns || [])" 
          :key="index" 
          class="carousel-card"
        >
          <div v-if="column.thumbnailImageUrl" class="card-image">
            <el-image :src="column.thumbnailImageUrl" fit="cover" />
          </div>
          <div v-else class="card-image placeholder">
            <el-icon :size="24"><Picture /></el-icon>
          </div>
          <div class="card-body">
            <div v-if="column.title" class="card-title">{{ column.title }}</div>
            <div class="card-text">{{ column.text || '內文' }}</div>
          </div>
          <div class="card-actions">
            <div 
              v-for="(action, idx) in (column.actions || [])" 
              :key="idx"
              class="card-action-btn"
            >
              {{ action.label || '按鈕' }}
            </div>
          </div>
        </div>
        <div v-if="!templateData.columns?.length" class="carousel-empty">
          <el-empty description="請新增卡片" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- Image Carousel 預覽 -->
    <div v-else-if="templateType === 'image_carousel'" class="image-carousel-preview">
      <div class="carousel-track">
        <div 
          v-for="(column, index) in (templateData.columns || [])" 
          :key="index" 
          class="carousel-image"
        >
          <el-image 
            v-if="column.imageUrl" 
            :src="column.imageUrl" 
            fit="cover"
          />
          <div v-else class="image-placeholder">
            <el-icon :size="24"><Picture /></el-icon>
          </div>
        </div>
        <div v-if="!templateData.columns?.length" class="carousel-empty">
          <el-empty description="請新增圖片" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 替代文字提示 -->
    <div class="alt-text-hint" v-if="altText">
      <el-icon><InfoFilled /></el-icon>
      替代文字：{{ altText }}
    </div>
  </div>
</template>

<script setup>
import { Picture, InfoFilled } from '@element-plus/icons-vue'

defineProps({
  templateType: {
    type: String,
    default: 'buttons'
  },
  templateData: {
    type: Object,
    default: () => ({})
  },
  altText: {
    type: String,
    default: ''
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped lang="scss">
.template-message-preview {
  width: 100%;
  max-width: 300px;

  // Compact mode for list preview
  &.compact {
    max-width: 200px;
    
    .alt-text-hint {
      display: none;
    }
    
    .buttons-preview, .confirm-preview {
      .preview-image {
        height: 80px;
      }
      .preview-body {
        padding: 8px;
        .preview-title {
          font-size: 12px;
        }
        .preview-text {
          font-size: 11px;
        }
      }
      .preview-actions .preview-action-btn {
        padding: 8px;
        font-size: 12px;
      }
    }
    
    .carousel-preview, .image-carousel-preview {
      .carousel-track {
        gap: 8px;
      }
      .carousel-card {
        width: 120px;
        .card-image {
          height: 60px;
        }
        .card-body {
          padding: 6px;
          .card-title, .card-text {
            font-size: 10px;
          }
        }
        .card-actions .card-action-btn {
          padding: 6px;
          font-size: 10px;
        }
      }
      .carousel-image {
        width: 80px;
        height: 80px;
      }
    }
  }

  .alt-text-hint {
    margin-top: 12px;
    padding: 8px 12px;
    background: #f4f4f5;
    border-radius: 4px;
    font-size: 12px;
    color: #909399;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  // Buttons 預覽
  .buttons-preview {
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

      .preview-title {
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

  // Confirm 預覽
  .confirm-preview {
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

        &.positive {
          color: #67c23a;
          border-right: 1px solid #ebeef5;
        }

        &.negative {
          color: #909399;
        }
      }
    }
  }

  // Carousel 預覽
  .carousel-preview {
    overflow-x: auto;

    .carousel-track {
      display: flex;
      gap: 8px;
      padding: 4px;

      .carousel-card {
        width: 180px;
        flex-shrink: 0;
        background: #fff;
        border-radius: 10px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .card-image {
          height: 90px;
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
          padding: 10px;

          .card-title {
            font-weight: bold;
            font-size: 13px;
            margin-bottom: 4px;
            color: #303133;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .card-text {
            font-size: 12px;
            color: #606266;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
        }

        .card-actions {
          border-top: 1px solid #ebeef5;

          .card-action-btn {
            padding: 8px;
            text-align: center;
            color: #409eff;
            font-size: 12px;
            border-bottom: 1px solid #ebeef5;

            &:last-child {
              border-bottom: none;
            }
          }
        }
      }

      .carousel-empty {
        width: 100%;
        min-width: 200px;
      }
    }
  }

  // Image Carousel 預覽
  .image-carousel-preview {
    overflow-x: auto;

    .carousel-track {
      display: flex;
      gap: 8px;
      padding: 4px;

      .carousel-image {
        width: 100px;
        height: 100px;
        flex-shrink: 0;
        border-radius: 8px;
        overflow: hidden;
        background: #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        :deep(.el-image) {
          width: 100%;
          height: 100%;
        }

        .image-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f0f0f0;
          color: #c0c4cc;
        }
      }

      .carousel-empty {
        width: 100%;
        min-width: 200px;
      }
    }
  }
}
</style>
