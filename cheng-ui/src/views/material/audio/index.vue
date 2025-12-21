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
          v-hasPermi="['system:material:audio:upload']"
        >
          <el-button type="primary" plain :icon="Upload">上傳</el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          :icon="Microphone"
          @click="openRecordDialog"
          v-hasPermi="['system:material:audio:upload']"
        >
          錄音
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:material:audio:remove']"
        >
          刪除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-progress v-if="uploading" :percentage="uploadPercent" :stroke-width="10" style="margin: 10px 0" />

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
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
            v-hasPermi="['system:material:audio:remove']"
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

    <el-dialog v-model="previewOpen" title="音檔預覽" width="720px" append-to-body>
      <audio v-if="previewUrl" :src="previewUrl" controls style="width: 100%" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewOpen = false">關閉</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="recordOpen" title="錄音" width="520px" append-to-body>
      <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap">
        <el-button :disabled="recording || converting || uploading" type="primary" @click="startRecording">開始</el-button>
        <el-button :disabled="!recording" type="warning" @click="stopRecording">停止</el-button>
        <el-button :disabled="!recordBlob || converting || uploading" type="success" :loading="converting || uploading" @click="uploadRecordedAudio">
          {{ converting ? '轉換中...' : uploading ? '上傳中...' : '上傳' }}
        </el-button>
        <span v-if="recordDurationMs" style="color: #909399">長度：{{ Math.floor(recordDurationMs / 1000) }} 秒</span>
      </div>
      <div v-if="converting" style="margin-top: 10px">
        <el-progress :percentage="convertProgress" :stroke-width="8" />
        <div style="color: #909399; font-size: 12px; margin-top: 4px">{{ convertMessage }}</div>
      </div>
      <div v-if="uploading && !converting" style="margin-top: 10px">
        <el-progress :percentage="uploadPercent" :stroke-width="8" />
        <div style="color: #909399; font-size: 12px; margin-top: 4px">上傳中...</div>
      </div>
      <div style="margin-top: 10px">
        <audio v-if="recordUrl" :src="recordUrl" controls style="width: 100%" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeRecordDialog">關閉</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MaterialAudio">
import { listMaterialAudio, delMaterialAudio, existsMaterialAudio, uploadMaterialAudio } from '@/api/system/material'
import { getProfileApiUrl, getProfileStaticUrl } from '@/utils/image'
import { convertToMp3, needsConversion } from '@/utils/audioConverter'
import { Search, Refresh, Upload, Delete, DocumentCopy, VideoPlay, Microphone } from '@element-plus/icons-vue'

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

const recordOpen = ref(false)
const recording = ref(false)
const recordBlob = ref(null)
const recordUrl = ref('')
const recordDurationMs = ref(0)
const converting = ref(false)
const convertProgress = ref(0)
const convertMessage = ref('')
let mediaRecorder = null
let recordChunks = []
let recordStartedAt = 0

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  originalName: undefined,
  status: undefined
})

function getPublicUrl(row) {
  return row?.relativePath ? getProfileApiUrl(row.relativePath) : ''
}

function openRecordDialog() {
  recordOpen.value = true
}

function closeRecordDialog() {
  recordOpen.value = false
  if (recordUrl.value) {
    URL.revokeObjectURL(recordUrl.value)
  }
  recording.value = false
  recordBlob.value = null
  recordUrl.value = ''
  recordDurationMs.value = 0
  mediaRecorder = null
  recordChunks = []
  recordStartedAt = 0
}

async function startRecording() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    proxy.$modal.msgError('瀏覽器不支援錄音')
    return
  }

  const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
  recordChunks = []
  recordStartedAt = Date.now()

  mediaRecorder = new MediaRecorder(stream)
  mediaRecorder.ondataavailable = (e) => {
    if (e.data && e.data.size > 0) {
      recordChunks.push(e.data)
    }
  }
  mediaRecorder.onstop = () => {
    stream.getTracks().forEach(t => t.stop())
    const blob = new Blob(recordChunks, { type: mediaRecorder.mimeType || 'audio/webm' })
    recordBlob.value = blob
    recordDurationMs.value = Math.max(0, Date.now() - recordStartedAt)
    if (recordUrl.value) {
      URL.revokeObjectURL(recordUrl.value)
    }
    recordUrl.value = URL.createObjectURL(blob)
  }

  recording.value = true
  mediaRecorder.start()
}

function stopRecording() {
  if (!mediaRecorder) return
  if (mediaRecorder.state === 'recording') {
    mediaRecorder.stop()
  }
  recording.value = false
}

async function uploadRecordedAudio() {
  if (!recordBlob.value) return

  let fileToUpload
  let fileName = 'record.mp3'

  // 檢查是否需要轉換格式（webm -> mp3）
  const mimeType = recordBlob.value.type || 'audio/webm'
  if (needsConversion(mimeType)) {
    converting.value = true
    convertProgress.value = 0
    convertMessage.value = '準備轉換...'

    try {
      const mp3Blob = await convertToMp3(recordBlob.value, (progress, message) => {
        convertProgress.value = progress
        convertMessage.value = message
      })
      fileToUpload = new File([mp3Blob], fileName, { type: 'audio/mp3' })
      converting.value = false
    } catch (e) {
      converting.value = false
      console.error('音檔轉換失敗:', e)
      proxy.$modal.msgError('音檔轉換失敗: ' + (e.message || '未知錯誤'))
      return
    }
  } else {
    // 已經是支援的格式，直接使用
    const ext = mimeType.includes('mp3') || mimeType.includes('mpeg') ? 'mp3' : 'm4a'
    fileName = 'record.' + ext
    fileToUpload = new File([recordBlob.value], fileName, { type: mimeType })
  }

  uploading.value = true
  uploadPercent.value = 0
  try {
    const existsRes = await existsMaterialAudio(fileName)
    const exists = !!(existsRes && existsRes.code === 200 && existsRes.data === true)
    let overwrite = false
    if (exists) {
      await proxy.$modal.confirm('檔名已存在，是否覆蓋？')
      overwrite = true
    }

    const res = await uploadMaterialAudio(fileToUpload, recordDurationMs.value, overwrite, (evt) => {
      handleUploadProgress(evt)
    })
    uploading.value = false
    uploadPercent.value = 100
    if (res && res.code === 200) {
      proxy.$modal.msgSuccess('上傳成功')
      getList()
      closeRecordDialog()
      return
    }
    proxy.$modal.msgError(res?.msg || '上傳失敗')
  } catch (e) {
    uploading.value = false
    if (e && e !== 'cancel') {
      console.error('錄音上傳失敗:', e)
      proxy.$modal.msgError('上傳失敗')
    }
  }
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
  listMaterialAudio(queryParams).then((res) => {
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
  // 關鍵流程：前端先做基本格式/大小檢查，避免大檔案上傳後才失敗
  const ext = (file.name || '').split('.').pop().toLowerCase()
  const allowed = ['mp3', 'wav', 'm4a', 'aac', 'ogg']
  if (!allowed.includes(ext)) {
    proxy.$modal.msgError('檔案格式不正確，請上傳 ' + allowed.join('/') + ' 格式')
    return false
  }
  const maxMb = 200
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
    const existsRes = await existsMaterialAudio(file.name)
    const exists = !!(existsRes && existsRes.code === 200 && existsRes.data === true)
    let overwrite = false
    if (exists) {
      await proxy.$modal.confirm('檔名已存在，是否覆蓋？')
      overwrite = true
    }

    const res = await uploadMaterialAudio(file, undefined, overwrite, (evt) => {
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
    return delMaterialAudio(assetIds)
  }).then(() => {
    proxy.$modal.msgSuccess('刪除成功')
    getList()
  })
}

onMounted(() => {
  getList()
})

onBeforeUnmount(() => {
  if (recordUrl.value) {
    URL.revokeObjectURL(recordUrl.value)
  }
})
</script>
