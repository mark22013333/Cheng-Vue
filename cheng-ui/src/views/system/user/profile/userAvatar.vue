<template>
  <div>
    <!-- 頭像顯示區 -->
    <div class="avatar-container" @click="editCropper()">
      <div class="avatar-wrapper">
        <img class="avatar-image" :src="options.img" alt="使用者頭像"/>
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
      v-model="open"
      width="900px"
      append-to-body
      destroy-on-close
      @opened="modalOpened"
      @close="closeDialog"
      custom-class="avatar-dialog"
      :close-on-click-modal="false">
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
              :canScale="options.canScale"
              :centerBox="options.centerBox"
              :high="options.high"
              :full="options.full"
              :mode="options.mode"
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
                <el-button type="primary" size="default">
                  <el-icon style="margin-right: 6px;"><FolderOpened /></el-icon>
                  選擇圖片
                </el-button>
              </el-upload>
              <span class="control-tip">💡 拖曳方框四角調整大小，拖曳圖片調整位置</span>
            </div>
            <div class="control-group">
              <el-button-group>
                <el-button size="default" @click="changeScale(1)" title="放大圖片">
                  <el-icon :size="18" style="margin-right: 4px;"><ZoomIn /></el-icon>
                  放大
                </el-button>
                <el-button size="default" @click="changeScale(-1)" title="縮小圖片">
                  <el-icon :size="18" style="margin-right: 4px;"><ZoomOut /></el-icon>
                  縮小
                </el-button>
                <el-button size="default" @click="rotateLeft()" title="逆時針旋轉">
                  <el-icon :size="18" style="margin-right: 4px;"><RefreshLeft /></el-icon>
                  ↶
                </el-button>
                <el-button size="default" @click="rotateRight()" title="順時針旋轉">
                  <el-icon :size="18" style="margin-right: 4px;"><RefreshRight /></el-icon>
                  ↷
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
                <li><strong>調整大小：</strong>拖曳方框四角小點即可調整裁剪區域</li>
                <li><strong>移動裁剪框：</strong>點擊方框邊線並拖曳可移動位置</li>
                <li><strong>移動圖片：</strong>直接拖曳圖片可調整顯示位置</li>
                <li><strong>縮放圖片：</strong>按「放大」「縮小」按鈕或滑鼠滾輪</li>
                <li><strong>建議尺寸：</strong>200x200 像素以上</li>
                <li><strong>檔案限制：</strong>JPG、PNG、GIF，≤2MB</li>
              </ul>
            </el-alert>
          </div>
        </div>
      </div>

      <!-- 底部按鈕 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false" size="default">取消</el-button>
          <el-button type="primary" @click="uploadImg()" size="default" :loading="uploading">
            <el-icon v-if="!uploading" style="margin-right: 6px;"><Upload /></el-icon>
            {{ uploading ? '上傳中...' : '確定上傳' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {VueCropper} from "vue-cropper"
import {uploadAvatar} from "@/api/system/user"
import {debounce} from '@/utils'
import useUserStore from '@/store/modules/user'
import {FolderOpened, ZoomIn, ZoomOut, RefreshLeft, RefreshRight, Upload} from '@element-plus/icons-vue'

export default {
  components: { 
    VueCropper,
    FolderOpened,
    ZoomIn,
    ZoomOut,
    RefreshLeft,
    RefreshRight,
    Upload
  },
  data() {
    const userStore = useUserStore()
    return {
      userStore,  // 儲存 store 參考以便後續使用
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
        img: userStore.avatar,  // 裁剪圖片的位置
        autoCrop: true,             // 是否預設產生截圖框
        autoCropWidth: 200,         // 預設產生截圖框寬度
        autoCropHeight: 200,        // 預設產生截圖框高度
        fixedBox: false,            // 允許調整截圖框大小（拖曳四角可調整）
        fixed: true,                // 固定比例
        fixedNumber: [1, 1],        // 固定比例 1:1（方形）
        canMove: true,              // 可以移動圖片（拖曳圖片本身）
        canMoveBox: true,           // 可以移動截圖框（拖曳裁剪框中心）
        canScale: true,             // 可以縮放圖片（滑鼠滾輪）
        centerBox: true,            // 截圖框居中顯示
        high: true,                 // 高畫質
        full: false,                // 按照裁剪框尺寸輸出
        mode: 'contain',            // 圖片自動縮放以完整顯示
        limitMinSize: [50, 50],     // 最小裁剪尺寸
        outputType: "png",          // 預設產生截圖為PNG格式
        filename: 'avatar'          // 檔案名稱
      },
      previews: {
        url: '',
        img: ''
      },  // 初始化預覽物件
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
      // 空方法，防止預設上傳
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
      if (!this.$refs.cropper) return
      // 直接使用 changeScale 進行增量縮放，而非絕對縮放
      // num 為正數放大，負數縮小
      this.$refs.cropper.changeScale(num || 1)
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
          // 處理頭像 URL：無論開發或正式環境，都需要加上 API 前綴
          let avatarUrl = response.imgUrl
          if (avatarUrl && avatarUrl.startsWith('/profile')) {
            // /profile 開頭的路徑需要加上 API 前綴
            // 開發環境：/dev-api/profile/xxx -> proxy 轉發
            // 正式環境：/prod-api/profile/xxx -> Nginx 代理
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
          this.userStore.avatar = avatarUrl  // 使用 Pinia 更新頭像
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
      this.options.img = this.userStore.avatar
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
:deep(.avatar-dialog) {
  border-radius: 12px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;

  .el-dialog__header {
    border-bottom: 2px solid #f0f2f5;
    padding: 20px 24px;
    flex-shrink: 0;

    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .el-dialog__body {
    padding: 20px 24px;
    overflow-y: auto;
    flex: 1;
    min-height: 0;
  }

  .el-dialog__footer {
    border-top: 1px solid #f0f2f5;
    padding: 16px 24px;
    flex-shrink: 0;
    background: #fff;
    position: sticky;
    bottom: 0;
    z-index: 10;
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
    height: 420px;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    transition: all 0.3s;
    background: #f5f7fa;

    // 強制顯示 vue-cropper 的裁剪框和控制點
    :deep(.cropper-crop-box) {
      border: 3px solid #409eff !important;
      box-shadow: 0 0 0 2px #fff, 0 0 15px rgba(0, 0, 0, 0.4), inset 0 0 0 1px rgba(255, 255, 255, 0.8) !important;
    }

    :deep(.cropper-view-box) {
      outline: 3px solid rgba(64, 158, 255, 0.9) !important;
      outline-offset: -3px;
      box-shadow: inset 0 0 20px rgba(64, 158, 255, 0.1) !important;
    }

    // 讓裁剪框的八個控制點更明顯
    :deep(.cropper-point) {
      width: 14px !important;
      height: 14px !important;
      background-color: #409eff !important;
      border: 3px solid #fff !important;
      border-radius: 50% !important;
      box-shadow: 0 3px 6px rgba(0, 0, 0, 0.4) !important;
      opacity: 1 !important;
      
      &:hover {
        transform: scale(1.3) !important;
        background-color: #66b1ff !important;
      }
    }

    // 裁剪框的線條
    :deep(.cropper-line) {
      background-color: #409eff !important;
      opacity: 1 !important;
      
      &.line-e, &.line-w {
        width: 3px !important;
      }
      
      &.line-n, &.line-s {
        height: 3px !important;
      }
    }

    // 裁剪框可拖曳區域
    :deep(.cropper-face) {
      background-color: transparent !important;
      cursor: move !important;
    }

    // 裁剪框尺寸資訊顯示（左上角的 200 x 200）
    :deep(.cropper-info) {
      position: absolute;
      left: 0;
      top: 0;
      background: rgba(0, 0, 0, 0.75) !important;
      color: #fff !important;
      padding: 6px 12px !important;
      font-size: 13px !important;
      font-weight: 500 !important;
      border-radius: 0 0 8px 0 !important;
      z-index: 100 !important;
      pointer-events: none !important;
    }

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
    margin-top: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;

    .control-group {
      display: flex;
      align-items: center;
      gap: 10px;

      .control-tip {
        color: #909399;
        font-size: 12px;
      }

      // 選擇圖片按鈕特殊樣式
      :deep(.el-upload) {
        .el-button--primary {
          background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
          border: none;
          padding: 10px 20px;
          font-weight: 600;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
          display: inline-flex;
          align-items: center;

          &:hover {
            background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.5);
          }

          .el-icon {
            font-size: 17px;
          }
        }
      }
    }

    :deep(.el-button) {
      padding: 10px 18px;
      font-weight: 500;
      border-radius: 6px;
      transition: all 0.3s;
      display: inline-flex;
      align-items: center;
      font-size: 14px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &.el-button--primary {
        background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
        border: none;
      }

      &.is-plain {
        background: #fff;
        
        &:hover {
          background: #ecf5ff;
          color: #409eff;
          border-color: #c6e2ff;
        }
      }

      // Icon 樣式
      .el-icon {
        font-size: 16px;
      }
    }
  }
}

// 預覽區
.preview-section {
  width: 260px;
  flex-shrink: 0;

  @media (max-width: 768px) {
    width: 100%;
  }

  h4 {
    margin: 0 0 12px 0;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .preview-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;

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
    :deep(.el-alert) {
      padding: 10px 12px;

      .el-alert__title {
        font-size: 13px;
        margin-bottom: 6px;
      }

      ul {
        margin: 0;
        padding-left: 18px;

        li {
          font-size: 12px;
          line-height: 1.6;
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
  margin: 0;

  :deep(.el-button) {
    min-width: 110px;
    height: 40px;
    font-size: 14px;
    font-weight: 500;
    border-radius: 6px;
    transition: all 0.3s;
    display: inline-flex;
    align-items: center;
    justify-content: center;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.el-button--primary {
      background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
      border: none;
      padding: 0 24px;

      &:hover {
        background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
      }
    }

    &.el-button--default {
      &:hover {
        color: #409eff;
        border-color: #c6e2ff;
        background-color: #ecf5ff;
      }
    }

    .el-icon {
      font-size: 16px;
    }
  }
}
</style>
