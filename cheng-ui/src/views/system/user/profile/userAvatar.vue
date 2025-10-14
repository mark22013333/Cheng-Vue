<template>
  <div>
    <!-- 頭像顯示區 -->
    <div class="avatar-container" @click="editCropper()">
      <div class="avatar-wrapper">
        <img class="avatar-image" :src="options.img" alt="用戶頭像"/>
        <div class="avatar-overlay">
          <i class="el-icon-camera"></i>
          <span>更換頭像</span>
        </div>
      </div>
      <div class="avatar-badge">
        <i class="el-icon-check"></i>
      </div>
    </div>

    <!-- 頭像編輯對話框 -->
    <el-dialog 
      :title="title" 
      :visible.sync="open" 
      width="900px" 
      append-to-body 
      @opened="modalOpened" 
      @close="closeDialog"
      custom-class="avatar-dialog">
      <div class="dialog-content">
        <!-- 左側裁剪區 -->
        <div class="cropper-section">
          <div class="cropper-wrapper" 
               @drop.prevent="handleDrop" 
               @dragover.prevent="isDragging = true"
               @dragleave="isDragging = false"
               :class="{ 'is-dragging': isDragging }">
            <vue-cropper
              ref="cropper"
              :img="options.img"
              :info="true"
              :autoCrop="options.autoCrop"
              :autoCropWidth="options.autoCropWidth"
              :autoCropHeight="options.autoCropHeight"
              :fixedBox="options.fixedBox"
              :fixed="options.fixed"
              :fixedNumber="options.fixedNumber"
              :canMove="options.canMove"
              :canMoveBox="options.canMoveBox"
              :centerBox="options.centerBox"
              :outputType="options.outputType"
              @realTime="realTime"
              v-if="visible"
            />
            <div class="drag-tip" v-if="isDragging">
              <i class="el-icon-upload"></i>
              <p>放開以上傳圖片</p>
            </div>
          </div>
          
          <!-- 操作按鈕 -->
          <div class="cropper-controls">
            <div class="control-group">
              <el-upload 
                action="#" 
                :http-request="requestUpload" 
                :show-file-list="false" 
                :before-upload="beforeUpload">
                <el-button type="primary" icon="el-icon-folder-opened" size="medium">
                  選擇圖片
                </el-button>
              </el-upload>
              <span class="control-tip">拖曳裁剪框四角可調整大小</span>
            </div>
            <div class="control-group">
              <el-button-group>
                <el-button icon="el-icon-zoom-in" size="medium" @click="changeScale(1)" title="放大圖片">
                  <span style="font-size: 12px; margin-left: 4px;">放大</span>
                </el-button>
                <el-button icon="el-icon-zoom-out" size="medium" @click="changeScale(-1)" title="縮小圖片">
                  <span style="font-size: 12px; margin-left: 4px;">縮小</span>
                </el-button>
                <el-button icon="el-icon-refresh-left" size="medium" @click="rotateLeft()" title="逆時針旋轉">
                  <span style="font-size: 12px; margin-left: 4px;">↶</span>
                </el-button>
                <el-button icon="el-icon-refresh-right" size="medium" @click="rotateRight()" title="順時針旋轉">
                  <span style="font-size: 12px; margin-left: 4px;">↷</span>
                </el-button>
              </el-button-group>
            </div>
          </div>
        </div>

        <!-- 右側預覽區 -->
        <div class="preview-section">
          <h4>預覽效果</h4>
          <div class="preview-container">
            <div class="preview-item">
              <div class="preview-box large">
                <img :src="previews.url" :style="previews.img" />
              </div>
              <span>大頭像 (200x200)</span>
            </div>
            <div class="preview-item">
              <div class="preview-box medium">
                <img :src="previews.url" :style="previews.img" />
              </div>
              <span>中頭像 (100x100)</span>
            </div>
            <div class="preview-item">
              <div class="preview-box small">
                <img :src="previews.url" :style="previews.img" />
              </div>
              <span>小頭像 (50x50)</span>
            </div>
          </div>
          
          <div class="upload-tips">
            <el-alert
              title="操作說明"
              type="info"
              :closable="false">
              <ul>
                <li><strong>調整大小：</strong>拖曳裁剪框四角</li>
                <li><strong>移動位置：</strong>拖曳裁剪框內部</li>
                <li><strong>縮放圖片：</strong>使用放大/縮小按鈕</li>
                <li><strong>建議尺寸：</strong>200x200 像素以上</li>
                <li><strong>檔案限制：</strong>JPG、PNG、GIF，≤2MB</li>
              </ul>
            </el-alert>
          </div>
        </div>
      </div>
      
      <!-- 底部按鈕 -->
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false" size="medium">取消</el-button>
        <el-button type="primary" @click="uploadImg()" size="medium" :loading="uploading">
          <i class="el-icon-upload2" v-if="!uploading"></i>
          {{ uploading ? '上傳中...' : '確定上傳' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import store from "@/store"
import {VueCropper} from "vue-cropper"
import {uploadAvatar} from "@/api/system/user"
import {debounce} from '@/utils'

export default {
  components: { VueCropper },
  data() {
    return {
      // 是否顯示彈出層
      open: false,
      // 是否顯示cropper
      visible: false,
      // 是否正在上傳
      uploading: false,
      // 是否正在拖曳
      isDragging: false,
      // 彈出層標題
      title: "編輯頭像",
      options: {
        img: store.getters.avatar,  // 裁剪圖片的地址
        autoCrop: true,             // 是否預設產生截圖框
        autoCropWidth: 200,         // 預設產生截圖框寬度
        autoCropHeight: 200,        // 預設產生截圖框高度
        fixedBox: false,            // 允許調整截圖框大小
        fixed: true,                // 固定比例
        fixedNumber: [1, 1],        // 固定比例 1:1
        canMove: true,              // 可以移動圖片
        canMoveBox: true,           // 可以移動截圖框
        centerBox: true,            // 截圖框居中
        outputType: "png",          // 預設產生截圖為PNG格式
        filename: 'avatar'          // 檔案名稱
      },
      previews: {},
      resizeHandler: null
    }
  },
  methods: {
    // 編輯頭像
    editCropper() {
      this.open = true
    },
    // 打開彈出層結束時的呼叫
    modalOpened() {
      this.visible = true
      if (!this.resizeHandler) {
        this.resizeHandler = debounce(() => {
          this.refresh()
        }, 100)
      }
      window.addEventListener("resize", this.resizeHandler)
    },
    // 重新整理元件
    refresh() {
      this.$refs.cropper.refresh()
    },
    // 覆蓋預設的上傳行為
    requestUpload() {
    },
    // 向左旋轉
    rotateLeft() {
      this.$refs.cropper.rotateLeft()
    },
    // 向右旋轉
    rotateRight() {
      this.$refs.cropper.rotateRight()
    },
    // 圖片縮放
    changeScale(num) {
      num = num || 1
      this.$refs.cropper.changeScale(num)
    },
    // 上傳預處理
    beforeUpload(file) {
      if (file.type.indexOf("image/") == -1) {
        this.$modal.msgError("檔案格式錯誤，請上傳圖片類型,如：JPG，PNG後綴的檔案。")
      } else {
        const reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = () => {
          this.options.img = reader.result
          this.options.filename = file.name
        }
      }
    },
    // 拖曳上傳
    handleDrop(e) {
      this.isDragging = false
      const files = e.dataTransfer.files
      if (files.length > 0) {
        this.beforeUpload(files[0])
      }
    },
    // 上傳圖片
    uploadImg() {
      this.uploading = true
      this.$refs.cropper.getCropBlob(data => {
        let formData = new FormData()
        formData.append("avatarfile", data, this.options.filename)
        uploadAvatar(formData).then(response => {
          this.open = false
          // 處理頭像 URL：無論開發或生產環境，都需要加上 API 前綴
          let avatarUrl = response.imgUrl
          if (avatarUrl && avatarUrl.startsWith('/profile')) {
            // /profile 開頭的路徑需要加上 API 前綴
            // 開發環境：/dev-api/profile/xxx -> proxy 轉發
            // 生產環境：/prod-api/profile/xxx -> Nginx 代理
            const baseApi = process.env.VUE_APP_BASE_API || ''
            if (baseApi) {
              avatarUrl = baseApi + avatarUrl
            }
          } else if (!avatarUrl.startsWith('http')) {
            // 其他相對路徑也加上 API 前綴
            const baseApi = process.env.VUE_APP_BASE_API || ''
            if (baseApi) {
              avatarUrl = baseApi + avatarUrl
            }
          }
          this.options.img = avatarUrl
          store.commit('SET_AVATAR', avatarUrl)
          this.$modal.msgSuccess("頭像更新成功")
          this.visible = false
          this.uploading = false
        }).catch(() => {
          this.uploading = false
        })
      })
    },
    // 即時預覽
    realTime(data) {
      this.previews = data
    },
    // 關閉視窗
    closeDialog() {
      this.options.img = store.getters.avatar
      this.visible = false
      window.removeEventListener("resize", this.resizeHandler)
    }
  }
}
</script>

<style scoped lang="scss">
// 頭像容器
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

      i {
        font-size: 32px;
        margin-bottom: 8px;
      }

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

// 對話框樣式
::v-deep .avatar-dialog {
  border-radius: 12px;

  .el-dialog__header {
    border-bottom: 2px solid #f0f2f5;
    padding: 24px;

    .el-dialog__title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    border-top: 1px solid #f0f2f5;
    padding: 20px 24px;
  }
}

.dialog-content {
  display: flex;
  gap: 24px;

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

// 裁剪區
.cropper-section {
  flex: 1;
  min-width: 0;

  .cropper-wrapper {
    height: 400px;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    transition: all 0.3s;

    &.is-dragging {
      border-color: #409eff;
      background: rgba(64, 158, 255, 0.05);

      .drag-tip {
        display: flex;
      }
    }

    .drag-tip {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(64, 158, 255, 0.9);
      color: white;
      display: none;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      z-index: 10;

      i {
        font-size: 64px;
        margin-bottom: 16px;
      }

      p {
        font-size: 18px;
        margin: 0;
      }
    }
  }

  .cropper-controls {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;

    .control-group {
      display: flex;
      align-items: center;
      gap: 12px;

      .control-tip {
        color: #909399;
        font-size: 13px;
      }
    }
  }
}

// 預覽區
.preview-section {
  width: 280px;
  flex-shrink: 0;

  @media (max-width: 768px) {
    width: 100%;
  }

  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .preview-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    margin-bottom: 20px;

    .preview-item {
      display: flex;
      align-items: center;
      gap: 16px;

      span {
        font-size: 13px;
        color: #606266;
      }

      .preview-box {
        border-radius: 50%;
        overflow: hidden;
        border: 2px solid #e4e7ed;
        background: #f5f7fa;
        flex-shrink: 0;

        &.large {
          width: 80px;
          height: 80px;
        }

        &.medium {
          width: 60px;
          height: 60px;
        }

        &.small {
          width: 40px;
          height: 40px;
        }

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }
  }

  .upload-tips {
    ::v-deep .el-alert {
      padding: 12px;

      .el-alert__title {
        font-size: 14px;
        margin-bottom: 8px;
      }

      ul {
        margin: 0;
        padding-left: 20px;

        li {
          font-size: 12px;
          line-height: 1.8;
          color: #909399;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
