<template>
  <div>
    <div class="avatar-container" @click="editCropper()">
      <div class="avatar-wrapper">
        <img class="avatar-image" :src="options.img" alt="使用者頭像"/>
        <div class="avatar-overlay">
          <el-icon style="font-size: 32px; margin-bottom: 8px;">
            <Camera/>
          </el-icon>
          <span>更換頭像</span>
        </div>
      </div>
      <div class="avatar-badge">
        <el-icon>
          <Check/>
        </el-icon>
      </div>
    </div>

    <el-dialog
      :title="title"
      v-model="open"
      width="800px"
      append-to-body
      @close="closeDialog"
      class="avatar-dialog"
      :close-on-click-modal="false">

      <div class="dialog-content">
        <div class="left-section">
          <div class="cropper-wrapper">
            <vue-cropper
              v-if="visible"
              :key="cropKey"
              ref="cropper"
              :img="options.img"
              :info="true"
              :autoCrop="true"
              :autoCropWidth="200"
              :autoCropHeight="200"
              :fixedBox="false"
              :fixed="true"
              :fixedNumber="[1, 1]"
              :centerBox="true"
              :outputType="options.outputType"
              @realTime="realTime"
            />

            <div class="drag-tip" v-if="isDragging"
                 @drop.prevent="handleDrop"
                 @dragover.prevent="isDragging = true"
                 @dragleave="isDragging = false">
              <el-icon style="font-size: 64px; margin-bottom: 16px;">
                <Upload/>
              </el-icon>
              <p>放開以上傳圖片</p>
            </div>
          </div>

          <div class="cropper-controls">
            <div class="control-row upload-row">
              <el-upload
                action="#"
                :http-request="requestUpload"
                :show-file-list="false"
                :before-upload="beforeUpload">
                <el-button type="primary" icon="FolderOpened">
                  選擇圖片
                </el-button>
              </el-upload>
              <span class="tip-text">拖曳藍色框調整範圍</span>
            </div>

            <div class="control-row tools-row">
              <el-button-group>
                <el-tooltip content="放大" placement="top">
                  <el-button @click="changeScale(1)" icon="ZoomIn"></el-button>
                </el-tooltip>
                <el-tooltip content="縮小" placement="top">
                  <el-button @click="changeScale(-1)" icon="ZoomOut"></el-button>
                </el-tooltip>
                <el-tooltip content="向左旋轉" placement="top">
                  <el-button @click="rotateLeft()" icon="RefreshLeft"></el-button>
                </el-tooltip>
                <el-tooltip content="向右旋轉" placement="top">
                  <el-button @click="rotateRight()" icon="RefreshRight"></el-button>
                </el-tooltip>
                <el-tooltip content="重置" placement="top">
                  <el-button @click="resetCrop" icon="Refresh"></el-button>
                </el-tooltip>
              </el-button-group>
            </div>
          </div>
        </div>

        <div class="right-section">
          <div class="preview-group">
            <h4>預覽效果</h4>
            <div class="preview-list">
              <div class="preview-item">
                <div class="preview-circle large">
                  <img v-if="previews.url" :src="previews.url" :style="previews.img">
                </div>
                <span>大頭像 (200x200)</span>
              </div>
              <div class="preview-item">
                <div class="preview-circle medium">
                  <img v-if="previews.url" :src="previews.url" :style="previews.img">
                </div>
                <span>中頭像 (100x100)</span>
              </div>
              <div class="preview-item">
                <div class="preview-circle small">
                  <img v-if="previews.url" :src="previews.url" :style="previews.img">
                </div>
                <span>小頭像 (50x50)</span>
              </div>
            </div>
          </div>

          <div class="instruction-box">
            <h4>操作指南</h4>
            <ul>
              <li><strong>縮放/移動：</strong>可滾動滑鼠縮放圖片，或拖曳圖片移動位置</li>
              <li><strong>調整範圍：</strong>拖曳藍色框四角可調整大小</li>
              <li><strong>格式限制：</strong>支援 JPG、PNG、GIF</li>
            </ul>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">取消</el-button>
          <el-button type="primary" @click="uploadImg()" :loading="uploading">
            確定上傳
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {VueCropper} from "vue-cropper"
import {uploadAvatar} from "@/api/system/user"
import useUserStore from '@/store/modules/user'
import {
  Camera, Check, FolderOpened, ZoomIn, ZoomOut,
  RefreshLeft, RefreshRight, Upload, Refresh
} from '@element-plus/icons-vue'

export default {
  components: {
    VueCropper, Camera, Check, FolderOpened, ZoomIn, ZoomOut,
    RefreshLeft, RefreshRight, Upload, Refresh
  },
  data() {
    const userStore = useUserStore()
    return {
      userStore,
      open: false,
      visible: false,
      cropKey: 0,
      uploading: false,
      isDragging: false,
      title: "編輯頭像",
      options: {
        img: userStore.avatar,
        outputType: "png",
        filename: 'avatar'
      },
      previews: {},
    }
  },
  watch: {
    open(val) {
      if (val) {
        this.visible = true;
        this.cropKey++;

        this.$nextTick(() => {
          setTimeout(() => {
            this.resetCrop();
          }, 100);
        });
      } else {
        setTimeout(() => {
          this.visible = false;
        }, 300);
      }
    }
  },
  methods: {
    editCropper() {
      this.open = true
    },
    // [修正] 移除錯誤的屬性修改，只呼叫方法
    resetCrop() {
      if (this.$refs.cropper) {
        this.$refs.cropper.goAutoCrop()
      }
    },
    realTime(data) {
      this.previews = data
    },
    requestUpload() {
    },
    rotateLeft() {
      this.$refs.cropper.rotateLeft()
    },
    rotateRight() {
      this.$refs.cropper.rotateRight()
    },
    changeScale(num) {
      num = num || 1
      this.$refs.cropper.changeScale(num)
    },
    beforeUpload(file) {
      if (file.type.indexOf("image/") == -1) {
        this.$modal.msgError("檔案格式錯誤，請上傳圖片類型。")
      } else {
        const reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = () => {
          this.options.img = reader.result
          this.options.filename = file.name
          this.cropKey++;
        }
      }
    },
    handleDrop(e) {
      this.isDragging = false
      if (e.dataTransfer.files.length > 0) this.beforeUpload(e.dataTransfer.files[0])
    },
    uploadImg() {
      this.uploading = true
      this.$refs.cropper.getCropBlob(data => {
        let formData = new FormData()
        formData.append("avatarfile", data, this.options.filename)
        uploadAvatar(formData).then(response => {
          this.open = false
          let avatarUrl = response.imgUrl
          const baseApi = import.meta.env.VITE_APP_BASE_API || ''
          if (avatarUrl && !avatarUrl.startsWith('http') && !avatarUrl.startsWith(baseApi)) {
            avatarUrl = baseApi + avatarUrl
          }
          this.options.img = avatarUrl
          this.userStore.avatar = avatarUrl
          this.$modal.msgSuccess("頭像更新成功")
          this.visible = false
          this.uploading = false
        }).catch(() => {
          this.uploading = false
        })
      })
    },
    closeDialog() {
      this.open = false
    }
  }
}
</script>

<style scoped lang="scss">
.avatar-container {
  position: relative;
  display: inline-block;
  cursor: pointer;

  .avatar-wrapper {
    position: relative;
    width: 140px;
    height: 140px;
    border-radius: 50%;
    overflow: hidden;
    border: 4px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;

    &:hover {
      border-color: rgba(255, 255, 255, 0.6);
      transform: scale(1.05);

      .avatar-overlay {
        opacity: 1;
      }
    }

    .avatar-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: white;
      opacity: 0;
      transition: opacity 0.3s;

      span {
        font-size: 13px;
      }
    }
  }

  .avatar-badge {
    position: absolute;
    bottom: 8px;
    right: 8px;
    width: 32px;
    height: 32px;
    background: #67c23a;
    border-radius: 50%;
    border: 3px solid white;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
}

:deep(.avatar-dialog) {
  .el-dialog__header {
    margin-right: 0;
    border-bottom: 1px solid #f0f2f5;
  }

  .el-dialog__body {
    padding: 24px !important;
  }
}

.dialog-content {
  display: flex;
  gap: 24px;
  height: 500px;
  overflow: hidden;
}

.left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.cropper-wrapper {
  width: 100%;
  height: 380px;
  position: relative;

  // 方格背景
  background-color: #f8f9fa;
  background-image: linear-gradient(45deg, #e5e5e5 25%, transparent 25%, transparent 75%, #e5e5e5 75%, #e5e5e5),
  linear-gradient(45deg, #e5e5e5 25%, transparent 25%, transparent 75%, #e5e5e5 75%, #e5e5e5);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;

  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;

  // 裁切框樣式
  :deep(.cropper-view-box) {
    outline: 2px solid #fff;
    outline-offset: -1px;
    box-shadow: 0 0 0 1px #409eff;
  }

  :deep(.cropper-point) {
    width: 6px !important;
    height: 6px !important;
    background: #fff !important;
    border: 1px solid #409eff !important;
    opacity: 1 !important;
  }

  // 隱藏中間點
  :deep(.point-n), :deep(.point-s), :deep(.point-e), :deep(.point-w) {
    display: none !important;
  }
}

.drag-tip {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  z-index: 99;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.cropper-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;

  .control-row {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .tip-text {
      color: #909399;
      font-size: 13px;
    }
  }
}

.right-section {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  border-left: 1px solid #f0f2f5;
  padding-left: 24px;

  h4 {
    margin: 0 0 16px 0;
    color: #303133;
    font-size: 15px;
    font-weight: 600;
  }
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .preview-item {
    display: flex;
    align-items: center;
    gap: 16px;

    span {
      font-size: 13px;
      color: #606266;
    }

    .preview-circle {
      border-radius: 50%;
      overflow: hidden;
      background: #f5f7fa;
      border: 1px solid #e4e7ed;
      flex-shrink: 0;
      position: relative;

      // 確保圖片在圓框內
      img {
        position: absolute;
        top: 0;
        left: 0;
        max-width: none !important;
        max-height: none !important;
      }

      &.large {
        width: 90px;
        height: 90px;
      }

      &.medium {
        width: 64px;
        height: 64px;
      }

      &.small {
        width: 40px;
        height: 40px;
      }
    }
  }
}

.instruction-box {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: auto;
  border: 1px solid #ebeef5;

  h4 {
    margin-bottom: 10px;
    font-size: 13px;
    color: #606266;
  }

  ul {
    margin: 0;
    padding-left: 18px;

    li {
      font-size: 12px;
      color: #909399;
      line-height: 1.8;
      margin-bottom: 2px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 10px;
}
</style>
