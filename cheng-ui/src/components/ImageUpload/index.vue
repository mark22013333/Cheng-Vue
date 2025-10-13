<template>
  <div class="component-upload-image">
    <el-upload
      multiple
      :disabled="disabled"
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :data="data"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      ref="imageUpload"
      :on-remove="handleDelete"
      :show-file-list="true"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{hide: this.fileList.length >= this.limit}"
    >
      <i class="el-icon-plus"></i>
    </el-upload>

    <!-- 上傳提示 -->
    <div class="el-upload__tip" slot="tip" v-if="showTip && !disabled">
      請上傳
      <template v-if="fileSize"> 大小不超過 <b style="color: #f56c6c">{{ fileSize }}MB</b></template>
      <template v-if="fileType"> 格式為 <b style="color: #f56c6c">{{ fileType.join("/") }}</b></template>
      的檔案
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="預覽"
      width="800"
      append-to-body
    >
      <img
        :src="dialogImageUrl"
        style="display: block; max-width: 100%; margin: 0 auto"
      />
    </el-dialog>
  </div>
</template>

<script>
import {getToken} from "@/utils/auth"
import {isExternal} from "@/utils/validate"
import Sortable from 'sortablejs'

export default {
  props: {
    value: [String, Object, Array],
    // 上傳介面地址
    action: {
      type: String,
      default: "/common/upload"
    },
    // 上傳攜帶的參數
    data: {
      type: Object
    },
    // 圖片數量限制
    limit: {
      type: Number,
      default: 5
    },
    // 大小限制(MB)
    fileSize: {
       type: Number,
      default: 5
    },
    // 檔案類型, 例如['png', 'jpg', 'jpeg']
    fileType: {
      type: Array,
      default: () => ["png", "jpg", "jpeg"]
    },
    // 是否顯示提示
    isShowTip: {
      type: Boolean,
      default: true
    },
    // 禁用元件（僅查看圖片）
    disabled: {
      type: Boolean,
      default: false
    },
    // 拖動排序
    drag: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      number: 0,
      uploadList: [],
      dialogImageUrl: "",
      dialogVisible: false,
      hideUpload: false,
      baseUrl: process.env.VUE_APP_BASE_API || '',
      uploadImgUrl: (process.env.VUE_APP_BASE_API || '') + this.action, // 上傳的圖片伺服器地址
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      fileList: []
    }
  },
  mounted() {
    if (this.drag && !this.disabled) {
      this.$nextTick(() => {
        const element = this.$refs.imageUpload?.$el?.querySelector('.el-upload-list')
        Sortable.create(element, {
          onEnd: (evt) => {
            const movedItem = this.fileList.splice(evt.oldIndex, 1)[0]
            this.fileList.splice(evt.newIndex, 0, movedItem)
            this.$emit("input", this.listToString(this.fileList))
          }
        })
      })
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          // 首先將值轉為陣列
          const list = Array.isArray(val) ? val : this.value.split(',')
          // 然後將陣列轉為物件陣列
          this.fileList = list.map(item => {
            if (typeof item === "string") {
              if (item.indexOf(this.baseUrl) === -1 && !isExternal(item)) {
                  item = { name: this.baseUrl + item, url: this.baseUrl + item }
              } else {
                  item = { name: item, url: item }
              }
            }
            return item
          })
        } else {
          this.fileList = []
          return []
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    // 是否顯示提示
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize)
    },
  },
  methods: {
    // 上傳前loading載入
    handleBeforeUpload(file) {
      let isImg = false
      if (this.fileType.length) {
        let fileExtension = ""
        if (file.name.lastIndexOf(".") > -1) {
          fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1)
        }
        isImg = this.fileType.some(type => {
          if (file.type.indexOf(type) > -1) return true
          if (fileExtension && fileExtension.indexOf(type) > -1) return true
          return false
        })
      } else {
        isImg = file.type.indexOf("image") > -1
      }

      if (!isImg) {
        this.$modal.msgError(`檔案格式不正確，請上傳${this.fileType.join("/")}圖片格式檔案!`)
        return false
      }
      if (file.name.includes(',')) {
        this.$modal.msgError('檔案名不正確，不能包含英文逗號!')
        return false
      }
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$modal.msgError(`上傳頭像圖片大小不能超過 ${this.fileSize} MB!`)
          return false
        }
      }
      this.$modal.loading("正在上傳圖片，請稍候...")
      this.number++
    },
    // 檔案個數超出
    handleExceed() {
      this.$modal.msgError(`上傳檔案數量不能超過 ${this.limit} 個!`)
    },
    // 上傳成功呼叫
    handleUploadSuccess(res, file) {
      if (res.code === 200) {
        this.uploadList.push({ name: res.fileName, url: res.fileName })
        this.uploadedSuccessfully()
      } else {
        this.number--
        this.$modal.closeLoading()
        this.$modal.msgError(res.msg)
        this.$refs.imageUpload.handleRemove(file)
        this.uploadedSuccessfully()
      }
    },
    // 刪除圖片
    handleDelete(file) {
      const findex = this.fileList.map(f => f.name).indexOf(file.name)
      if (findex > -1) {
        this.fileList.splice(findex, 1)
        this.$emit("input", this.listToString(this.fileList))
      }
    },
    // 上傳失敗
    handleUploadError() {
      this.$modal.msgError("上傳圖片失敗，請重試")
      this.$modal.closeLoading()
    },
    // 上傳結束處理
    uploadedSuccessfully() {
      if (this.number > 0 && this.uploadList.length === this.number) {
        this.fileList = this.fileList.concat(this.uploadList)
        this.uploadList = []
        this.number = 0
        this.$emit("input", this.listToString(this.fileList))
        this.$modal.closeLoading()
      }
    },
    // 預覽
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    // 物件轉成指定字串分隔
    listToString(list, separator) {
      let strs = ""
      separator = separator || ","
      for (let i in list) {
        if (list[i].url) {
          strs += list[i].url.replace(this.baseUrl, "") + separator
        }
      }
      return strs != '' ? strs.substr(0, strs.length - 1) : ''
    }
  }
}
</script>
<style scoped lang="scss">
// .el-upload--picture-card 控制加號部分
::v-deep.hide .el-upload--picture-card {
  display: none;
}

::v-deep .el-upload-list--picture-card.is-disabled + .el-upload--picture-card {
  display: none !important;
}

// 去掉動畫效果
::v-deep .el-list-enter-active,
::v-deep .el-list-leave-active {
  transition: all 0s;
}

::v-deep .el-list-enter, .el-list-leave-active {
  opacity: 0;
  transform: translateY(0);
}
</style>

