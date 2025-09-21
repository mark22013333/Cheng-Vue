<template>
  <div>
    <el-upload
      :action="uploadUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      name="file"
      :show-file-list="false"
      :headers="headers"
      style="display: none"
      ref="upload"
      v-if="this.type == 'url'"
    >
    </el-upload>
    <div class="editor" ref="editor" :style="styles"></div>
  </div>
</template>

<script>
import axios from "axios"
import Quill from "quill"
import "quill/dist/quill.core.css"
import "quill/dist/quill.snow.css"
import "quill/dist/quill.bubble.css"
import {getToken} from "@/utils/auth"

export default {
  name: "Editor",
  props: {
    /* 編輯器的内容 */
    value: {
      type: String,
      default: "",
    },
    /* 高度 */
    height: {
      type: Number,
      default: null,
    },
    /* 最小高度 */
    minHeight: {
      type: Number,
      default: null,
    },
    /* 唯讀 */
    readOnly: {
      type: Boolean,
      default: false,
    },
    /* 上傳文件大小限制(MB) */
    fileSize: {
      type: Number,
      default: 5,
    },
    /* 類型（base64格式、url格式） */
    type: {
      type: String,
      default: "url",
    }
  },
  data() {
    return {
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload", // 上傳的圖片伺服器地址
      headers: {
        Authorization: "Bearer " + getToken()
      },
      Quill: null,
      currentValue: "",
      options: {
        theme: "snow",
        bounds: document.body,
        debug: "warn",
        modules: {
          // 工具欄配置
          toolbar: [
            ["bold", "italic", "underline", "strike"],       // 加粗 斜體 下畫線 刪除線
            ["blockquote", "code-block"],                    // 引用  程式碼塊
            [{list: "ordered"}, {list: "bullet"}],       // 有序、無序列表
            [{indent: "-1"}, {indent: "+1"}],            // 縮進
            [{size: ["small", false, "large", "huge"]}],   // 字體大小
            [{header: [1, 2, 3, 4, 5, 6, false]}],         // 標題
            [{color: []}, {background: []}],             // 字體顏色、字體背景顏色
            [{align: []}],                                 // 對齐方式
            ["clean"],                                       // 清除文字格式
            ["link", "image", "video"]                       // 鏈接、圖片、影片
          ],
        },
        placeholder: "請輸入内容",
        readOnly: this.readOnly,
      },
    }
  },
  computed: {
    styles() {
      let style = {}
      if (this.minHeight) {
        style.minHeight = `${this.minHeight}px`
      }
      if (this.height) {
        style.height = `${this.height}px`
      }
      return style
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val !== this.currentValue) {
          this.currentValue = val === null ? "" : val
          if (this.Quill) {
            this.Quill.clipboard.dangerouslyPasteHTML(this.currentValue)
          }
        }
      },
      immediate: true,
    },
  },
  mounted() {
    this.init()
  },
  beforeDestroy() {
    this.Quill = null
  },
  methods: {
    init() {
      const editor = this.$refs.editor
      this.Quill = new Quill(editor, this.options)
      // 如果設定了上傳地址則自定義圖片上傳事件
      if (this.type == 'url') {
        let toolbar = this.Quill.getModule("toolbar")
        toolbar.addHandler("image", (value) => {
          if (value) {
            this.$refs.upload.$children[0].$refs.input.click()
          } else {
            this.quill.format("image", false)
          }
        })
        this.Quill.root.addEventListener('paste', this.handlePasteCapture, true)
      }
      this.Quill.clipboard.dangerouslyPasteHTML(this.currentValue)
      this.Quill.on("text-change", (delta, oldDelta, source) => {
        const html = this.$refs.editor.children[0].innerHTML
        const text = this.Quill.getText()
        const quill = this.Quill
        this.currentValue = html
        this.$emit("input", html)
        this.$emit("on-change", { html, text, quill })
      })
      this.Quill.on("text-change", (delta, oldDelta, source) => {
        this.$emit("on-text-change", delta, oldDelta, source)
      })
      this.Quill.on("selection-change", (range, oldRange, source) => {
        this.$emit("on-selection-change", range, oldRange, source)
      })
      this.Quill.on("editor-change", (eventName, ...args) => {
        this.$emit("on-editor-change", eventName, ...args)
      })
    },
    // 上傳前校檢格式和大小
    handleBeforeUpload(file) {
      const type = ["image/jpeg", "image/jpg", "image/png", "image/svg"]
      const isJPG = type.includes(file.type)
      // 檢驗文件格式
      if (!isJPG) {
        this.$message.error(`圖片格式錯誤!`)
        return false
      }
      // 校檢文件大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$message.error(`上傳文件大小不能超過 ${this.fileSize} MB!`)
          return false
        }
      }
      return true
    },
    handleUploadSuccess(res, file) {
      // 如果上傳成功
      if (res.code == 200) {
        // 取得富文字元件實體
        let quill = this.Quill
        // 取得光標所在位置
        let length = quill.getSelection().index
        // 新增圖片  res.url為伺服器返回的圖片地址
        quill.insertEmbed(length, "image", process.env.VUE_APP_BASE_API + res.fileName)
        // 調整光標到最後
        quill.setSelection(length + 1)
      } else {
        this.$message.error("圖片新增失敗")
      }
    },
    handleUploadError() {
      this.$message.error("圖片新增失敗")
    },
    // 複製粘贴圖片處理
    handlePasteCapture(e) {
      const clipboard = e.clipboardData || window.clipboardData
      if (clipboard && clipboard.items) {
        for (let i = 0; i < clipboard.items.length; i++) {
          const item = clipboard.items[i]
          if (item.type.indexOf('image') !== -1) {
            e.preventDefault()
            const file = item.getAsFile()
            this.insertImage(file)
          }
        }
      }
    },
    insertImage(file) {
      const formData = new FormData()
      formData.append("file", file)
      axios.post(this.uploadUrl, formData, { headers: { "Content-Type": "multipart/form-data", Authorization: this.headers.Authorization } }).then(res => {
        this.handleUploadSuccess(res.data)
      })
    }
  }
}
</script>

<style>
.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "請輸入鏈接地址:";
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "儲存";
  padding-right: 0px;
}
.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "請輸入影片地址:";
}
.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}
.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: "文字";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: "標題1";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: "標題2";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: "標題3";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: "標題4";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: "標題5";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: "標題6";
}
.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: "標準字體";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: "衬線字體";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: "等寬字體";
}
</style>
