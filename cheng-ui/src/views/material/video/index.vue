<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="檔名" prop="originalName">
        <el-input
          v-model="queryParams.originalName"
          placeholder="請輸入檔名"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="狀態" clearable style="width: 140px">
          <el-option label="啟用" value="ACTIVE" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-upload
          :show-file-list="false"
          :before-upload="handleBeforeUpload"
          :http-request="handleHttpUpload"
          v-hasPermi="[SYSTEM_MATERIAL_VIDEO_UPLOAD]"
        >
          <el-button type="primary" plain :icon="Upload">上傳</el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          :icon="Camera"
          @click="openCamera"
          v-hasPermi="[SYSTEM_MATERIAL_VIDEO_UPLOAD]"
        >
          拍攝
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="[SYSTEM_MATERIAL_VIDEO_REMOVE]"
        >
          刪除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-progress v-if="uploading" :percentage="uploadPercent" :stroke-width="10" style="margin: 10px 0" />

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="縮圖" width="120" align="center">
        <template #default="scope">
          <video
            v-if="scope.row.relativePath"
            :src="getThumbUrl(scope.row)"
            muted
            playsinline
            preload="metadata"
            style="width: 96px; height: 54px; border-radius: 6px; object-fit: cover; background: #000"
          />
          <span v-else style="color: #ccc; font-size: 12px">無影片</span>
        </template>
      </el-table-column>
      <el-table-column label="ID" prop="assetId" width="90" />
      <el-table-column label="檔名" prop="originalName" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="大小" prop="fileSize" width="120">
        <template #default="scope">
          <span>{{ formatFileSize(scope.row.fileSize) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="建立時間" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="VideoPlay" @click="handlePreview(scope.row)">預覽</el-button>
          <el-button link type="primary" :icon="DocumentCopy" @click="handleCopy(scope.row)">複製連結</el-button>
          <el-button
            link
            type="danger"
            :icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="[SYSTEM_MATERIAL_VIDEO_REMOVE]"
          >
            刪除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog v-model="previewOpen" title="影片預覽" width="900px" append-to-body>
      <video v-if="previewUrl" :src="previewUrl" controls style="width: 100%; max-height: 65vh" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewOpen = false">關閉</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="cameraOpen" title="拍攝" width="900px" append-to-body :close-on-click-modal="false">
      <div style="display: flex; flex-direction: column; gap: 10px">
        <video ref="cameraVideoRef" autoplay playsinline muted style="width: 100%; max-height: 60vh; background: #000" />
        <video v-if="recordUrl" :src="recordUrl" controls style="width: 100%; max-height: 35vh" />
        <div style="display: flex; gap: 10px; flex-wrap: wrap">
          <el-button type="primary" @click="startCamera" :disabled="cameraStarting">連接相機</el-button>
          <el-button @click="switchCamera" :disabled="cameraStarting">切換鏡頭</el-button>
          <el-button type="warning" @click="startRecording" :disabled="!cameraStream || recording">開始錄影</el-button>
          <el-button type="warning" @click="stopRecording" :disabled="!recording">停止錄影</el-button>
          <el-button type="success" @click="uploadRecordedVideo" :disabled="!recordBlob">上傳</el-button>
          <el-button type="danger" @click="stopCamera" :disabled="!cameraStream">停止</el-button>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeCamera">關閉</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MaterialVideo">
import {
  SYSTEM_MATERIAL_VIDEO_REMOVE,
  SYSTEM_MATERIAL_VIDEO_UPLOAD
} from '@/constants/permissions'
import { listMaterialVideo, delMaterialVideo, existsMaterialVideo, uploadMaterialVideo } from '@/api/system/material'
import { getProfileApiUrl, getProfileStaticUrl } from '@/utils/image'
import { Search, Refresh, Upload, Delete, DocumentCopy, VideoPlay, Camera } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const showSearch = ref(true)

const ids = ref([])
const multiple = ref(true)

const uploading = ref(false)
const uploadPercent = ref(0)

const previewOpen = ref(false)
const previewUrl = ref('')

const cameraOpen = ref(false)
const cameraStarting = ref(false)
const cameraFacing = ref('environment')
const cameraVideoRef = ref(null)
const cameraStream = ref(null)

const recording = ref(false)
const recordBlob = ref(null)
const recordUrl = ref('')
let mediaRecorder = null
let recordChunks = []

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  originalName: undefined,
  status: undefined
})

function getPublicUrl(row) {
  return row?.relativePath ? getProfileApiUrl(row.relativePath) : ''
}

function getThumbUrl(row) {
  const url = getPublicUrl(row)
  // 透過時間片段讓瀏覽器更容易抓到第一幀作為縮圖
  return url ? url + '#t=0.1' : ''
}

async function openCamera() {
  cameraOpen.value = true
  await nextTick()
  if (!cameraStream.value) {
    await startCamera()
  } else if (cameraVideoRef.value && cameraVideoRef.value.srcObject !== cameraStream.value) {
    cameraVideoRef.value.srcObject = cameraStream.value
  }
}

async function startCamera() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    proxy.$modal.msgError('瀏覽器不支援相機')
    return
  }
  if (!window.isSecureContext) {
    proxy.$modal.msgError('相機功能僅能在 HTTPS 或 localhost 環境下使用')
    return
  }

  cameraStarting.value = true
  try {
    await stopCamera()
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: { ideal: cameraFacing.value } },
      audio: true
    })
    cameraStream.value = stream
    await nextTick()
    if (cameraVideoRef.value) {
      cameraVideoRef.value.srcObject = stream
    }
  } catch (e) {
    console.error('相機連接失敗:', e)
    proxy.$modal.msgError('無法連接相機，請檢查權限或是否被其他程式佔用')
  } finally {
    cameraStarting.value = false
  }
}

async function stopCamera() {
  stopRecording()
  const stream = cameraStream.value
  if (stream && stream.getTracks) {
    stream.getTracks().forEach(t => t.stop())
  }
  cameraStream.value = null
  if (cameraVideoRef.value) {
    cameraVideoRef.value.srcObject = null
  }
}

function switchCamera() {
  cameraFacing.value = cameraFacing.value === 'environment' ? 'user' : 'environment'
  startCamera()
}

function startRecording() {
  if (!cameraStream.value) return
  recordChunks = []
  recordBlob.value = null
  if (recordUrl.value) {
    URL.revokeObjectURL(recordUrl.value)
    recordUrl.value = ''
  }

  try {
    mediaRecorder = new MediaRecorder(cameraStream.value)
  } catch (e) {
    console.error('MediaRecorder 初始化失敗:', e)
    proxy.$modal.msgError('瀏覽器不支援錄影')
    return
  }

  mediaRecorder.ondataavailable = (e) => {
    if (e.data && e.data.size > 0) {
      recordChunks.push(e.data)
    }
  }
  mediaRecorder.onstop = () => {
    const blob = new Blob(recordChunks, { type: mediaRecorder.mimeType || 'video/webm' })
    recordBlob.value = blob
    recordUrl.value = URL.createObjectURL(blob)
  }

  recording.value = true
  mediaRecorder.start()
}

function stopRecording() {
  if (!mediaRecorder) {
    recording.value = false
    return
  }
  if (mediaRecorder.state === 'recording') {
    mediaRecorder.stop()
  }
  recording.value = false
}

async function uploadRecordedVideo() {
  if (!recordBlob.value) return
  const file = new File([recordBlob.value], `video_${Date.now()}.webm`, { type: recordBlob.value.type || 'video/webm' })
  await handleHttpUpload({
    file,
    onSuccess: () => {},
    onError: () => {}
  })
  closeCamera()
}

function closeCamera() {
  cameraOpen.value = false
  stopCamera()
  if (recordUrl.value) {
    URL.revokeObjectURL(recordUrl.value)
    recordUrl.value = ''
  }
  recordBlob.value = null
}

function formatFileSize(size) {
  if (!size && size !== 0) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(1) + ' MB'
  return (size / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}

function getList() {
  loading.value = true
  listMaterialVideo(queryParams).then((res) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.pageNum = 1
  queryParams.originalName = undefined
  queryParams.status = undefined
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.assetId)
  multiple.value = !selection.length
}

function handleBeforeUpload(file) {
  // 關鍵流程：影片通常較大，先做基本大小/格式檢查
  const ext = (file.name || '').split('.').pop().toLowerCase()
  const allowed = ['mp4', 'mov', 'webm']
  if (!allowed.includes(ext)) {
    proxy.$modal.msgError('檔案格式不正確，請上傳 ' + allowed.join('/') + ' 格式')
    return false
  }
  const maxMb = 500
  if (file.size / 1024 / 1024 > maxMb) {
    proxy.$modal.msgError('上傳檔案大小不能超過 ' + maxMb + 'MB')
    return false
  }
  uploading.value = true
  uploadPercent.value = 0
  return true
}

function handleUploadProgress(event, file) {
  // Element Plus 會在不同實作下傳入不同格式的進度資料，這裡做防禦性處理
  const percent = event?.percent
  if (typeof percent === 'number' && !Number.isNaN(percent)) {
    uploadPercent.value = Math.min(99, Math.floor(percent))
    return
  }

  const filePercent = file?.percentage
  if (typeof filePercent === 'number' && !Number.isNaN(filePercent)) {
    uploadPercent.value = Math.min(99, Math.floor(filePercent))
  }
}

async function handleHttpUpload(option) {
  const file = option?.file
  if (!file) return

  uploading.value = true
  uploadPercent.value = 0

  try {
    const existsRes = await existsMaterialVideo(file.name)
    const exists = !!(existsRes && existsRes.code === 200 && existsRes.data === true)
    let overwrite = false
    if (exists) {
      await proxy.$modal.confirm('檔名已存在，是否覆蓋？')
      overwrite = true
    }

    const res = await uploadMaterialVideo(file, undefined, overwrite, (evt) => {
      handleUploadProgress(evt)
    })

    uploading.value = false
    uploadPercent.value = 100
    if (res && res.code === 200) {
      proxy.$modal.msgSuccess('上傳成功')
      getList()
      option.onSuccess && option.onSuccess(res)
      return
    }
    proxy.$modal.msgError(res?.msg || '上傳失敗')
    option.onError && option.onError(res)
  } catch (e) {
    uploading.value = false
    if (e && e !== 'cancel') {
      console.error('上傳失敗:', e)
      proxy.$modal.msgError('上傳失敗')
    }
    option.onError && option.onError(e)
  }
}

function handlePreview(row) {
  previewUrl.value = getPublicUrl(row)
  previewOpen.value = true
}

async function handleCopy(row) {
  const text = row?.relativePath ? getProfileStaticUrl(row.relativePath) : ''
  try {
    await navigator.clipboard.writeText(text)
    proxy.$message.success('已複製到剪貼簿')
  } catch (e) {
    console.error('複製失敗:', e)
    proxy.$message.error('複製失敗')
  }
}

function handleDelete(row) {
  const assetIds = row?.assetId || ids.value
  proxy.$modal.confirm('是否確認刪除素材 ID 為 "' + assetIds + '" 的資料？').then(() => {
    return delMaterialVideo(assetIds)
  }).then(() => {
    proxy.$modal.msgSuccess('刪除成功')
    getList()
  })
}

onMounted(() => {
  getList()
})

onBeforeUnmount(() => {
  stopCamera()
  if (recordUrl.value) {
    URL.revokeObjectURL(recordUrl.value)
  }
})
</script>
