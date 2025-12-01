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
      class="editor-img-uploader"
      v-if="type == 'url'"
    >
      <i ref="uploadRef" class="editor-img-uploader"></i>
    </el-upload>
  </div>
  <div class="editor">
    <quill-editor
      ref="quillEditorRef"
      v-model:content="content"
      contentType="html"
      @textChange="(e) => $emit('update:modelValue', content)"
      :options="options"
      :style="styles"
    />
  </div>
</template>

<script setup>
import axios from 'axios'
import {QuillEditor} from "@vueup/vue-quill"
import "@vueup/vue-quill/dist/vue-quill.snow.css"
import {getToken} from "@/utils/auth"

const {proxy} = getCurrentInstance()

const quillEditorRef = ref()
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/common/upload") // 上傳的圖片伺服器位置
const headers = ref({
  Authorization: "Bearer " + getToken()
})

const props = defineProps({
  /* 編輯器的内容 */
  modelValue: {
    type: String,
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
  /* 只讀 */
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
})

const options = ref({
  theme: "snow",
  bounds: document.body,
  debug: "warn",
  modules: {
    // 工具欄配置
    toolbar: [
      ["bold", "italic", "underline", "strike"],      // 加粗 斜體 下底線 刪除線
      ["blockquote", "code-block"],                   // 引用  代碼塊
      [{list: "ordered"}, {list: "bullet"}],      // 有序、無序列表
      [{indent: "-1"}, {indent: "+1"}],           // 縮進
      [{size: ["small", false, "large", "huge"]}],  // 字體大小
      [{header: [1, 2, 3, 4, 5, 6, false]}],        // 標題
      [{color: []}, {background: []}],            // 字體顏色、字體背景顏色
      [{align: []}],                                // 對齊方式
      ["clean"],                                      // 清除文字格式
      ["link", "image", "video"]                      // 連結、圖片、影片
    ],
  },
  placeholder: "請輸入内容",
  readOnly: props.readOnly
})

const styles = computed(() => {
  let style = {}
  if (props.minHeight) {
    style.minHeight = `${props.minHeight}px`
  }
  if (props.height) {
    style.height = `${props.height}px`
  }
  return style
})

const content = ref("")
watch(() => props.modelValue, (v) => {
  if (v !== content.value) {
    content.value = v == undefined ? "<p></p>" : v
  }
}, {immediate: true})

// 如果設定了上傳位置則自定義圖片上傳事件
onMounted(() => {
  if (props.type == 'url') {
    let quill = quillEditorRef.value.getQuill()
    let toolbar = quill.getModule("toolbar")
    toolbar.addHandler("image", (value) => {
      if (value) {
        proxy.$refs.uploadRef.click()
      } else {
        quill.format("image", false)
      }
    })
    quill.root.addEventListener('paste', handlePasteCapture, true)
  }
})

// 上傳前檢驗格式和大小
function handleBeforeUpload(file) {
  const type = ["image/jpeg", "image/jpg", "image/png", "image/svg"]
  const isJPG = type.includes(file.type)
  // 檢驗文件格式
  if (!isJPG) {
    proxy.$modal.msgError(`圖片格式錯誤!`)
    return false
  }
  // 檢驗文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      proxy.$modal.msgError(`上傳文件大小不能超過 ${props.fileSize} MB!`)
      return false
    }
  }
  return true
}

// 上傳成功處理
function handleUploadSuccess(res, file) {
  // 如果上傳成功
  if (res.code == 200) {
    // 取得豐富文字實例
    let quill = toRaw(quillEditorRef.value).getQuill()
    // 取得光標位置
    let length = quill.selection.savedRange.index
    // 插入圖片，res.url為伺服器返回的圖片連結位置
    quill.insertEmbed(length, "image", import.meta.env.VITE_APP_BASE_API + res.fileName)
    // 調整光標到最後
    quill.setSelection(length + 1)
  } else {
    proxy.$modal.msgError("圖片插入失败")
  }
}

// 上傳失败處理
function handleUploadError() {
  proxy.$modal.msgError("圖片插入失败")
}

// 複製圖片處理
function handlePasteCapture(e) {
  const clipboard = e.clipboardData || window.clipboardData
  if (clipboard && clipboard.items) {
    for (let i = 0; i < clipboard.items.length; i++) {
      const item = clipboard.items[i]
      if (item.type.indexOf('image') !== -1) {
        e.preventDefault()
        const file = item.getAsFile()
        insertImage(file)
      }
    }
  }
}

function insertImage(file) {
  const formData = new FormData()
  formData.append("file", file)
  axios.post(uploadUrl.value, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: headers.value.Authorization
    }
  }).then(res => {
    handleUploadSuccess(res.data)
  })
}
</script>

<style>
.editor-img-uploader {
  display: none;
}

.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}

.quill-img {
  display: none;
}

.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "請輸入連結位置:";
}

.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "保存";
  padding-right: 0px;
}

.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "請輸入影片位置:";
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
  content: "標准字體";
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
