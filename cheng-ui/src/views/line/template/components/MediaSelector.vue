<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="900px" destroy-on-close>
    <div class="media-selector">
      <!-- 搜尋與篩選 -->
      <div class="selector-toolbar">
        <el-input v-model="searchText" placeholder="搜尋檔案名稱..." clearable style="width: 240px" :prefix-icon="Search" />
        <div class="toolbar-right">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="table"><el-icon><List /></el-icon></el-radio-button>
            <el-radio-button value="grid"><el-icon><Grid /></el-icon></el-radio-button>
          </el-radio-group>
          <el-button type="primary" :icon="Upload" @click="handleUpload">上傳新檔案</el-button>
        </div>
      </div>

      <!-- 表格視圖 -->
      <el-table
        v-if="viewMode === 'table'"
        v-loading="loading"
        :data="filteredList"
        highlight-current-row
        @current-change="handleSelect"
        style="width: 100%"
        max-height="400px"
      >
        <el-table-column width="80" label="預覽">
          <template #default="{ row }">
            <div class="table-preview">
              <el-image v-if="mediaType === 'image'" :src="getImageUrl(row.relativePath)" fit="cover" :preview-src-list="[getImageUrl(row.relativePath)]" />
              <video v-else-if="mediaType === 'video'" :src="getImageUrl(row.relativePath)" @loadeddata="handleVideoLoaded" />
              <el-icon v-else :size="24"><Headset /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="檔案名稱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column v-if="mediaType === 'audio' || mediaType === 'video'" prop="durationMs" label="時長" width="100">
          <template #default="{ row }">{{ formatDuration(row.durationMs) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="上傳時間" width="160" />
      </el-table>

      <!-- 網格視圖 -->
      <div v-else class="media-grid" v-loading="loading">
        <div
          v-for="item in filteredList"
          :key="item.assetId"
          :class="['media-item', { selected: selectedId === item.assetId }]"
          @click="handleSelect(item)"
        >
          <div class="media-preview">
            <el-image v-if="mediaType === 'image'" :src="getImageUrl(item.relativePath)" fit="cover" />
            <video v-else-if="mediaType === 'video'" :src="getImageUrl(item.relativePath)" @loadeddata="handleVideoLoaded" />
            <el-icon v-else-if="mediaType === 'audio'" :size="40"><Headset /></el-icon>
          </div>
          <div class="media-info">
            <div class="media-name">{{ item.originalName }}</div>
            <div class="media-meta">{{ formatSize(item.fileSize) }}</div>
          </div>
          <el-icon v-if="selectedId === item.assetId" class="check-icon"><Check /></el-icon>
        </div>

        <el-empty v-if="filteredList.length === 0 && !loading" description="暫無素材" :image-size="60" />
      </div>

      <!-- 已選擇的檔案資訊 -->
      <div v-if="selectedItem" class="selected-info">
        <el-tag type="success">已選擇：{{ selectedItem.originalName }}</el-tag>
        <span class="selected-url">{{ getProfileStaticUrl(selectedItem.relativePath) }}</span>
      </div>

      <!-- 分頁 -->
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadList"
        style="margin-top: 16px"
      />
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedItem" @click="handleConfirm">確定選擇</el-button>
    </template>

    <!-- 上傳對話框 -->
    <el-dialog v-model="uploadVisible" title="上傳檔案" width="500px" append-to-body>
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        :accept="acceptTypes"
        :on-change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖曳檔案到此處，或<em>點擊上傳</em></div>
        <template #tip>
          <div class="el-upload__tip">{{ uploadTip }}</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="doUpload">上傳</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Upload, UploadFilled, Headset, Check, List, Grid } from '@element-plus/icons-vue'
import { listMaterialImage, listMaterialVideo, listMaterialAudio, uploadMaterialImage, uploadMaterialVideo, uploadMaterialAudio } from '@/api/system/material'
import { getImageUrl, getProfileStaticUrl } from '@/utils/image'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mediaType: { type: String, default: 'image' } // image, video, audio
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const viewMode = ref('table') // table or grid

const dialogTitle = computed(() => {
  const titles = { image: '選擇圖片', video: '選擇影片', audio: '選擇音訊' }
  return titles[props.mediaType] || '選擇素材'
})

const acceptTypes = computed(() => {
  const types = { image: 'image/jpeg,image/png,image/gif', video: 'video/mp4', audio: 'audio/mpeg,audio/m4a' }
  return types[props.mediaType] || '*/*'
})

const uploadTip = computed(() => {
  const tips = {
    image: '支援 JPG、PNG、GIF 格式，最大 10MB',
    video: '支援 MP4 格式，最大 200MB',
    audio: '支援 MP3、M4A 格式，最大 200MB'
  }
  return tips[props.mediaType] || ''
})

const loading = ref(false)
const mediaList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchText = ref('')
const selectedId = ref(null)
const selectedItem = ref(null)

const uploadVisible = ref(false)
const uploading = ref(false)
const uploadRef = ref(null)
const uploadFile = ref(null)

const filteredList = computed(() => {
  if (!searchText.value) return mediaList.value
  const keyword = searchText.value.toLowerCase()
  return mediaList.value.filter(item => item.originalName?.toLowerCase().includes(keyword))
})

const loadList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    let res
    switch (props.mediaType) {
      case 'image':
        res = await listMaterialImage(params)
        break
      case 'video':
        res = await listMaterialVideo(params)
        break
      case 'audio':
        res = await listMaterialAudio(params)
        break
    }
    mediaList.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSelect = (item) => {
  selectedId.value = item.assetId
  selectedItem.value = item
}

const handleConfirm = () => {
  if (!selectedItem.value) return
  emit('select', {
    url: getProfileStaticUrl(selectedItem.value.relativePath),
    relativePath: selectedItem.value.relativePath,
    originalName: selectedItem.value.originalName,
    fileSize: selectedItem.value.fileSize,
    durationMs: selectedItem.value.durationMs
  })
  visible.value = false
}

const handleUpload = () => {
  uploadFile.value = null
  uploadVisible.value = true
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const doUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('請選擇檔案')
    return
  }

  uploading.value = true
  try {
    let res
    switch (props.mediaType) {
      case 'image':
        res = await uploadMaterialImage(uploadFile.value, false)
        break
      case 'video':
        res = await uploadMaterialVideo(uploadFile.value, null, false)
        break
      case 'audio':
        res = await uploadMaterialAudio(uploadFile.value, null, false)
        break
    }
    ElMessage.success('上傳成功')
    uploadVisible.value = false
    await loadList()
    
    // 自動選中剛上傳的檔案
    if (res.data) {
      selectedId.value = res.data.assetId
      selectedItem.value = res.data
    }
  } catch (e) {
    ElMessage.error('上傳失敗：' + (e.message || '未知錯誤'))
  } finally {
    uploading.value = false
  }
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

const formatDuration = (ms) => {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes}:${secs.toString().padStart(2, '0')}`
}

const handleVideoLoaded = (e) => {
  // 影片載入後自動跳到第一幀
  e.target.currentTime = 0.1
}

watch(visible, (val) => {
  if (val) {
    selectedId.value = null
    selectedItem.value = null
    loadList()
  }
})
</script>

<style scoped lang="scss">
.media-selector {
  min-height: 400px;
}

.selector-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.table-preview {
  width: 60px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;

  .el-image, video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.selected-info {
  margin-top: 16px;
  padding: 12px;
  background: #f0f9eb;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;

  .selected-url {
    font-size: 12px;
    color: #606266;
    word-break: break-all;
    flex: 1;
  }
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  min-height: 300px;
}

.media-item {
  position: relative;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
  }

  &.selected {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .media-preview {
    height: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    overflow: hidden;

    .el-image, video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .media-info {
    padding: 8px;

    .media-name {
      font-size: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .media-meta {
      font-size: 11px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .check-icon {
    position: absolute;
    top: 8px;
    right: 8px;
    color: #409eff;
    background: #fff;
    border-radius: 50%;
    padding: 2px;
  }
}
</style>
