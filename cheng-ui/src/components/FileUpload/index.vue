<template>
  <div class="upload-file">
    <el-upload
      multiple
      :action="uploadFileUrl"
      :before-upload="handleBeforeUpload"
      :file-list="fileList"
      :data="data"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      class="upload-file-uploader"
      ref="fileUpload"
      v-if="!disabled"
    >
      <!-- 上傳按鈕 -->
      <el-button size="small" type="primary">選取檔案</el-button>
      <!-- 上傳提示 -->
      <div class="el-upload__tip" slot="tip" v-if="showTip">
        請上傳
        <template v-if="fileSize"> 大小不超過 <b style="color: #f56c6c">{{ fileSize }}MB</b></template>
        <template v-if="fileType"> 格式為 <b style="color: #f56c6c">{{ fileType.join("/") }}</b></template>
        的檔案
      </div>
    </el-upload>

    <!-- 檔案列表 -->
    <transition-group ref="uploadFileList" class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li :key="file.url" class="el-upload-list__item ele-upload-list__item-content" v-for="(file, index) in fileList">
        <el-link :href="`${baseUrl}${file.url}`" :underline="false" target="_blank">
          <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        </el-link>
        <div class="ele-upload-list__item-content-action">
          <el-link v-if="!disabled" :underline="false" type="danger" @click="handleDelete(index)">刪除</el-link>
        </div>
      </li>
    </transition-group>
  </div>
</template>

<script>
import {getToken} from "@/utils/auth"
import Sortable from 'sortablejs'

export default {
  name: "FileUpload",
  props: {
    // 值
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
    // 數量限制
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
      default: () => ["doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "pdf"]
    },
    // 是否顯示提示
    isShowTip: {
      type: Boolean,
      default: true
    },
    // 禁用元件（僅查看檔案）
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
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadFileUrl: process.env.VUE_APP_BASE_API + this.action, // 上傳檔案伺服器地址
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      fileList: []
    }
  },
  mounted() {
    if (this.drag && !this.disabled) {
      this.$nextTick(() => {
        const element = this.$refs.uploadFileList?.$el || this.$refs.uploadFileList
        Sortable.create(element, {
          ghostClass: 'file-upload-darg',
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
          let temp = 1
          // 首先將值轉為陣列
          const list = Array.isArray(val) ? val : this.value.split(',')
          // 然後將陣列轉為物件陣列
          this.fileList = list.map(item => {
            if (typeof item === "string") {
              item = { name: item, url: item }
            }
            item.uid = item.uid || new Date().getTime() + temp++
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
    // 上傳前校驗格式和大小
    handleBeforeUpload(file) {
      // 校驗檔案類型
      if (this.fileType) {
        const fileName = file.name.split('.')
        const fileExt = fileName[fileName.length - 1]
        const isTypeOk = this.fileType.indexOf(fileExt) >= 0
        if (!isTypeOk) {
          this.$modal.msgError(`檔案格式不正確，請上傳${this.fileType.join("/")}格式檔案!`)
          return false
        }
      }
      // 校驗檔案名是否包含特殊字串
      if (file.name.includes(',')) {
        this.$modal.msgError('檔案名不正確，不能包含英文逗號!')
        return false
      }
      // 校驗檔案大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$modal.msgError(`上傳檔案大小不能超過 ${this.fileSize} MB!`)
          return false
        }
      }
      this.$modal.loading("正在上傳檔案，請稍候...")
      this.number++
      return true
    },
    // 檔案個數超出
    handleExceed() {
      this.$modal.msgError(`上傳檔案數量不能超過 ${this.limit} 個!`)
    },
    // 上傳失敗
    handleUploadError(err) {
      this.$modal.msgError("上傳檔案失敗，請重試")
      this.$modal.closeLoading()
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
        this.$refs.fileUpload.handleRemove(file)
        this.uploadedSuccessfully()
      }
    },
    // 刪除檔案
    handleDelete(index) {
      this.fileList.splice(index, 1)
      this.$emit("input", this.listToString(this.fileList))
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
    // 取得檔案名稱
    getFileName(name) {
      // 如果是url那么取最後的名字 如果不是直接返回
      if (name.lastIndexOf("/") > -1) {
        return name.slice(name.lastIndexOf("/") + 1)
      } else {
        return name
      }
    },
    // 物件轉成指定字串分隔
    listToString(list, separator) {
      let strs = ""
      separator = separator || ","
      for (let i in list) {
        strs += list[i].url + separator
      }
      return strs != '' ? strs.substr(0, strs.length - 1) : ''
    }
  }
}
</script>

<style scoped lang="scss">
.file-upload-darg {
  opacity: 0.5;
  background: #c8ebfb;
}
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
