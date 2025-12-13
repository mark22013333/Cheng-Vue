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
  <div class="editor" ref="editorContainerRef">
    <quill-editor
      ref="quillEditorRef"
      v-model:content="content"
      contentType="html"
      @update:content="handleContentUpdate"
      :options="options"
      :style="styles"
    />

    <div
      v-show="videoOverlayVisible"
      class="video-resize-overlay"
      :style="videoOverlayStyle"
      @mousedown.stop
      @click.stop
    >
      <el-tooltip content="切換影片尺寸（360/480/640）" placement="top">
        <button class="video-resize-btn" type="button" @click="handleVideoResizeClick">尺寸</button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import {ElMessageBox} from 'element-plus'
import {QuillEditor, Quill} from "@vueup/vue-quill"
import "@vueup/vue-quill/dist/vue-quill.snow.css"
import {getToken} from "@/utils/auth"

const {proxy} = getCurrentInstance()
const emits = defineEmits(['update:modelValue'])

const quillEditorRef = ref()
const editorContainerRef = ref(null)
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/common/upload") // 上傳的圖片伺服器位置
const headers = ref({
  Authorization: "Bearer " + getToken()
})

const DEFAULT_VIDEO_WIDTH = '100%'
const VIDEO_HEIGHT_PRESETS = [360, 480, 640]

function normalizeVideoUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const raw = url.trim()

  // YouTube: https://www.youtube.com/watch?v=xxxx
  // YouTube short: https://youtu.be/xxxx
  // YouTube shorts: https://www.youtube.com/shorts/xxxx
  try {
    const u = new URL(raw)
    const host = u.hostname.replace(/^www\./, '')
    const pathname = u.pathname || ''

    if (host === 'youtu.be') {
      const id = pathname.replace('/', '')
      return id ? `https://www.youtube.com/embed/${id}` : raw
    }

    if (host === 'youtube.com' || host === 'm.youtube.com') {
      if (pathname.startsWith('/embed/')) return raw
      if (pathname === '/watch') {
        const id = u.searchParams.get('v')
        return id ? `https://www.youtube.com/embed/${id}` : raw
      }
      if (pathname.startsWith('/shorts/')) {
        const id = pathname.replace('/shorts/', '').split('/')[0]
        return id ? `https://www.youtube.com/embed/${id}` : raw
      }
    }
  } catch (e) {
    // ignore
  }

  return raw
}

function isSingleUrlText(text) {
  if (!text || typeof text !== 'string') return false
  const t = text.trim()
  if (!t) return false
  if (/\s/.test(t)) return false
  try {
    // eslint-disable-next-line no-new
    new URL(t)
    return true
  } catch (e) {
    return false
  }
}

function isYoutubeUrl(url) {
  try {
    const u = new URL(url)
    const host = u.hostname.replace(/^www\./, '')
    return host === 'youtube.com' || host === 'm.youtube.com' || host === 'youtu.be'
  } catch (e) {
    return false
  }
}

function normalizeWidth(value) {
  if (value == null) return DEFAULT_VIDEO_WIDTH
  if (typeof value === 'number') return `${value}px`
  const s = String(value).trim()
  if (!s) return DEFAULT_VIDEO_WIDTH
  if (/^\d+$/.test(s)) return `${s}px`
  return s
}

function normalizeHeight(value) {
  if (value == null) return `${VIDEO_HEIGHT_PRESETS[1]}px`
  if (typeof value === 'number') return `${value}px`
  const s = String(value).trim()
  if (!s) return `${VIDEO_HEIGHT_PRESETS[1]}px`
  if (s.endsWith('px')) return s
  if (/^\d+$/.test(s)) return `${s}px`
  return s
}

const BlockEmbed = Quill.import('blots/block/embed')

class ResizableVideo extends BlockEmbed {
  static blotName = 'video'

  static tagName = 'iframe'

  static className = 'ql-video'

  static create(value) {
    const node = super.create()
    const videoValue = typeof value === 'string' ? {url: value} : (value || {})
    const url = normalizeVideoUrl(videoValue.url || '')
    const width = videoValue.width || DEFAULT_VIDEO_WIDTH
    const height = videoValue.height || VIDEO_HEIGHT_PRESETS[1]
    node.setAttribute('src', url)
    node.setAttribute('frameborder', '0')
    node.setAttribute('allow', 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share')
    node.setAttribute('allowfullscreen', 'true')
    node.setAttribute('width', String(width))
    node.setAttribute('height', String(height))

    node.style.width = normalizeWidth(width)
    node.style.height = normalizeHeight(height)

    return node
  }

  static value(domNode) {
    const styleWidth = domNode.style?.width
    const styleHeight = domNode.style?.height
    return {
      url: domNode.getAttribute('src') || '',
      width: styleWidth || domNode.getAttribute('width') || DEFAULT_VIDEO_WIDTH,
      height: (styleHeight ? styleHeight.replace('px', '') : '') || domNode.getAttribute('height') || String(VIDEO_HEIGHT_PRESETS[1])
    }
  }
}

Quill.register(ResizableVideo, true)

const videoOverlayVisible = ref(false)
const videoOverlayStyle = ref({ top: '0px', left: '0px' })
let activeVideoElement = null
let editorMouseMoveHandler = null
let editorMouseLeaveHandler = null
let editorScrollHandler = null

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
    default: 10,
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

function handleContentUpdate(v) {
  emits('update:modelValue', v)
}

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

    toolbar.addHandler('video', async (value) => {
      if (!value) {
        quill.format('video', false)
        return
      }

      const urlResult = await ElMessageBox.prompt('請輸入影片連結', '嵌入影片', {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        inputPlaceholder: 'https://...'
      }).catch(() => null)
      if (!urlResult?.value) return

      const range = quill.getSelection(true)
      const index = range ? range.index : quill.getLength()
      quill.insertEmbed(index, 'video', {
        url: urlResult.value,
        width: DEFAULT_VIDEO_WIDTH,
        height: VIDEO_HEIGHT_PRESETS[1]
      })
      quill.setSelection(index + 1)
    })

    const qlEditor = quill.root
    const container = editorContainerRef.value

    const isVideoIframe = (el) => {
      return !!(el && el.tagName === 'IFRAME' && el.classList && el.classList.contains('ql-video'))
    }

    const updateOverlayPosition = () => {
      if (!activeVideoElement || !container) return
      const iframeRect = activeVideoElement.getBoundingClientRect()
      const containerRect = container.getBoundingClientRect()
      const top = Math.max(0, iframeRect.top - containerRect.top + 8)
      const left = Math.max(0, iframeRect.right - containerRect.left - 44)
      videoOverlayStyle.value = { top: `${top}px`, left: `${left}px` }
    }

    // 使用 mouseover/mouseout 來捕捉進出 iframe 邊界（滑鼠進入 iframe 內容時，父層不會收到 mousemove）
    editorMouseMoveHandler = (e) => {
      const target = e?.target
      const iframe = isVideoIframe(target) ? target : target?.closest?.('iframe.ql-video')
      if (!iframe || !container?.contains(iframe)) return

      activeVideoElement = iframe
      videoOverlayVisible.value = true
      updateOverlayPosition()
    }

    editorMouseLeaveHandler = (e) => {
      const related = e?.relatedTarget
      if (related && related.closest && related.closest('.video-resize-overlay')) return
      if (activeVideoElement && (related === activeVideoElement || related?.closest?.('iframe.ql-video') === activeVideoElement)) return

      videoOverlayVisible.value = false
      activeVideoElement = null
    }

    editorScrollHandler = () => {
      if (!videoOverlayVisible.value) return
      updateOverlayPosition()
    }

    qlEditor.addEventListener('mouseover', editorMouseMoveHandler)
    qlEditor.addEventListener('mouseout', editorMouseLeaveHandler)
    qlEditor.addEventListener('scroll', editorScrollHandler, { capture: true, passive: true })

    // 修正舊資料：把 watch/shorts/短連結轉成 embed，避免 iframe 被封鎖
    setTimeout(() => {
      const root = quill?.root
      if (!root) return
      const iframes = root.querySelectorAll('iframe.ql-video')
      iframes.forEach((iframe) => {
        const src = iframe.getAttribute('src') || ''
        const normalized = normalizeVideoUrl(src)
        if (normalized && normalized !== src) {
          iframe.setAttribute('src', normalized)
        }
        if (!iframe.getAttribute('allow')) {
          iframe.setAttribute('allow', 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share')
        }
      })
    }, 0)

    quill.root.addEventListener('paste', handlePasteCapture, true)
  }
})

onUnmounted(() => {
  const quill = quillEditorRef.value?.getQuill?.()
  const root = quill?.root
  if (root && editorMouseMoveHandler) root.removeEventListener('mouseover', editorMouseMoveHandler)
  if (root && editorMouseLeaveHandler) root.removeEventListener('mouseout', editorMouseLeaveHandler)
  if (root && editorScrollHandler) root.removeEventListener('scroll', editorScrollHandler, { capture: true })
})

function handleVideoResizeClick() {
  const quill = quillEditorRef.value?.getQuill?.()
  if (!quill || !activeVideoElement) return

  const current = ResizableVideo.value(activeVideoElement)
  const currentHeight = parseInt(String(current.height || '').replace('px', ''), 10)
  const currentIdx = VIDEO_HEIGHT_PRESETS.indexOf(Number.isFinite(currentHeight) ? currentHeight : VIDEO_HEIGHT_PRESETS[1])
  const nextHeight = VIDEO_HEIGHT_PRESETS[(currentIdx + 1) % VIDEO_HEIGHT_PRESETS.length]

  activeVideoElement.setAttribute('height', String(nextHeight))
  activeVideoElement.style.height = `${nextHeight}px`

  quill.update('user')

  const container = editorContainerRef.value
  if (container) {
    const iframeRect = activeVideoElement.getBoundingClientRect()
    const containerRect = container.getBoundingClientRect()
    const top = Math.max(0, iframeRect.top - containerRect.top + 8)
    const left = Math.max(0, iframeRect.right - containerRect.left - 44)
    videoOverlayStyle.value = { top: `${top}px`, left: `${left}px` }
  }

  content.value = quill.root.innerHTML
  emits('update:modelValue', content.value)
}

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
    const text = clipboard.getData?.('text/plain')
    if (text && isSingleUrlText(text) && isYoutubeUrl(text)) {
      e.preventDefault()
      const quill = toRaw(quillEditorRef.value).getQuill()
      const range = quill.getSelection(true)
      const index = range ? range.index : quill.getLength()
      quill.insertEmbed(index, 'video', {
        url: text,
        width: DEFAULT_VIDEO_WIDTH,
        height: VIDEO_HEIGHT_PRESETS[1]
      })
      quill.setSelection(index + 1)
      return
    }

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

/* 限制編輯器中的圖片最大寬度 */
.ql-editor img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px 0;
}

.editor {
  position: relative;
}

.video-resize-overlay {
  position: absolute;
  z-index: 10;
}

.video-resize-btn {
  height: 28px;
  min-width: 40px;
  padding: 0 10px;
  border-radius: 14px;
  border: 1px solid rgba(64, 158, 255, 0.7);
  background: rgba(255, 255, 255, 0.9);
  color: #409EFF;
  cursor: pointer;
  font-size: 12px;
  line-height: 28px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.video-resize-btn:hover {
  background: #409EFF;
  color: #fff;
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
