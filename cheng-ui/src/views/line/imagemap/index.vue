<template>
  <div class="imagemap-container">
    <!-- 左側範本列表 -->
    <div class="left-panel" :style="{ width: leftPanelWidth + 'px' }">
      <div class="panel-header">
        <span class="panel-title">圖文訊息</span>
        <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
      </div>
      <div class="panel-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜尋範本名稱"
          clearable
          :prefix-icon="Search"
          @input="handleSearch"
        />
      </div>
      <div class="panel-list" v-loading="loading">
        <div
          v-for="item in filteredList"
          :key="item.templateId"
          :class="['list-item', { active: selectedId === item.templateId }]"
          @click="handleSelect(item)"
        >
          <div class="item-preview">
            <el-image
              v-if="item.previewImg"
              :src="getImageUrl(item.previewImg)"
              fit="cover"
            />
            <el-icon v-else :size="32" color="#dcdfe6"><Grid /></el-icon>
          </div>
          <div class="item-info">
            <div class="item-name">{{ item.templateName }}</div>
            <div class="item-meta">
              <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
                {{ item.status === 1 ? '啟用' : '停用' }}
              </el-tag>
              <span class="item-time">{{ formatDate(item.createTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && filteredList.length === 0" description="暫無圖文訊息" />
      </div>
    </div>

    <!-- 可拖曳分隔線 -->
    <div 
      class="resize-handle"
      :class="{ 'is-resizing': isResizing }"
      @mousedown="startResize"
    ></div>

    <!-- 右側編輯/預覽區域 -->
    <div class="right-panel">
      <template v-if="showEditor">
        <!-- 編輯模式 -->
        <div class="editor-header">
          <span class="editor-title">{{ isNew ? '新增圖文訊息' : '編輯圖文訊息' }}</span>
          <div class="editor-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleSave" :loading="saving">儲存</el-button>
          </div>
        </div>
        <div class="editor-body">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
            <!-- 基本資訊 -->
            <div class="form-section">
              <div class="section-title">基本資訊</div>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="範本名稱" prop="templateName">
                    <el-input v-model="form.templateName" placeholder="請輸入範本名稱" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="範本代碼" prop="templateCode">
                    <el-input v-model="form.templateCode" placeholder="選填，用於程式引用" maxlength="50" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="狀態">
                    <el-radio-group v-model="form.status">
                      <el-radio :value="1">啟用</el-radio>
                      <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="排序">
                    <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 圖文訊息編輯器 -->
            <div class="form-section">
              <div class="section-title">圖文訊息設定</div>
              <ImagemapEditor
                ref="imagemapEditorRef"
                v-model="imagemapData"
                @change="onImagemapChange"
              />
            </div>

            <!-- 備註 -->
            <div class="form-section">
              <el-form-item label="備註">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="選填" maxlength="500" />
              </el-form-item>
            </div>
          </el-form>
        </div>
      </template>
      <template v-else-if="selectedTemplate">
        <!-- 預覽模式 -->
        <div class="preview-header">
          <span class="preview-title">{{ selectedTemplate.templateName }}</span>
          <div class="preview-actions">
            <el-button type="primary" :icon="Edit" @click="handleEdit">編輯</el-button>
            <el-dropdown trigger="click">
              <el-button :icon="More">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleDuplicate">複製範本</el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete" style="color: #f56c6c">刪除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div class="preview-body">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="範本代碼">{{ selectedTemplate.templateCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="狀態">
              <el-switch
                v-model="selectedTemplate.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(selectedTemplate)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="替代文字">{{ selectedAltText || '-' }}</el-descriptions-item>
            <el-descriptions-item label="熱區數量">{{ selectedActionsCount }} 個</el-descriptions-item>
            <el-descriptions-item label="建立時間" :span="2">{{ selectedTemplate.createTime }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="preview-section">
            <div class="section-subtitle">訊息預覽</div>
            <div class="imagemap-preview">
              <ImagemapPreview :data="selectedImagemapData" />
            </div>
          </div>

          <!-- 引用此圖文範本的訊息範本列表 -->
          <div class="preview-section">
            <div class="section-subtitle">
              <span>引用此範本的訊息</span>
              <el-button 
                v-if="referenceList.length > 0" 
                type="primary" 
                size="small" 
                plain 
                :loading="syncing"
                @click="handleSyncReferences"
              >
                同步更新引用
              </el-button>
            </div>
            <div v-if="loadingRefs" class="refs-loading">
              <el-icon class="is-loading"><Loading /></el-icon> 載入中...
            </div>
            <div v-else-if="referenceList.length === 0" class="refs-empty">
              目前沒有訊息範本引用此圖文訊息
            </div>
            <div v-else class="refs-list">
              <div v-for="ref in referenceList" :key="ref.refId" class="ref-item">
                <el-icon><Document /></el-icon>
                <span class="ref-name">{{ ref.templateName }}</span>
                <el-tag size="small" type="info">訊息 {{ ref.messageIndex + 1 }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <!-- 空狀態 -->
        <div class="empty-panel">
          <el-empty description="選擇左側範本查看詳情，或點擊「新增」開始建立">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增圖文訊息</el-button>
          </el-empty>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup name="LineImagemap">
import { ref, reactive, computed, onMounted, onUnmounted, onActivated, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, More, Search, Grid, Loading, Document } from '@element-plus/icons-vue'
import ImagemapEditor from '../template/components/ImagemapEditor.vue'
import ImagemapPreview from './components/ImagemapPreview.vue'
import { listTemplate, getTemplate, addTemplate, updateTemplate, delTemplate, changeTemplateStatus, getImagemapReferences, syncImagemapReferences } from '@/api/line/template'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const templateList = ref([])
const selectedId = ref(null)

// 引用列表相關
const referenceList = ref([])
const loadingRefs = ref(false)
const syncing = ref(false)

// 可拖曳分隔線
const leftPanelWidth = ref(320)
const isResizing = ref(false)
const MIN_WIDTH = 200
const MAX_WIDTH = 500

const startResize = (e) => {
  isResizing.value = true
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

const handleResize = (e) => {
  if (!isResizing.value) return
  const containerLeft = document.querySelector('.imagemap-container')?.getBoundingClientRect().left || 0
  let newWidth = e.clientX - containerLeft - 12 // 12 is the margin
  newWidth = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, newWidth))
  leftPanelWidth.value = newWidth
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
})
const selectedTemplate = ref(null)
const showEditor = ref(false)
const isNew = ref(false)
const formRef = ref(null)
const imagemapEditorRef = ref(null)

const form = reactive({
  templateId: null,
  templateName: '',
  templateCode: '',
  status: 1,
  sortOrder: 0,
  remark: ''
})

const imagemapData = ref({
  baseUrl: '',
  altText: '圖片訊息',
  baseSize: { width: 1040, height: 700 },
  actions: []
})

const rules = {
  templateName: [{ required: true, message: '請輸入範本名稱', trigger: 'blur' }]
}

const filteredList = computed(() => {
  if (!searchKeyword.value) return templateList.value
  const keyword = searchKeyword.value.toLowerCase()
  return templateList.value.filter(item =>
    item.templateName?.toLowerCase().includes(keyword) ||
    item.templateCode?.toLowerCase().includes(keyword)
  )
})

const selectedImagemapData = computed(() => {
  if (!selectedTemplate.value?.content) return null
  try {
    return JSON.parse(selectedTemplate.value.content)
  } catch {
    return null
  }
})

const selectedAltText = computed(() => {
  return selectedImagemapData.value?.altText || selectedTemplate.value?.altText || ''
})

const selectedActionsCount = computed(() => {
  return selectedImagemapData.value?.actions?.length || 0
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listTemplate({ msgType: 'IMAGEMAP', pageNum: 1, pageSize: 100 })
    templateList.value = res.rows || []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 使用 computed 過濾，無需額外處理
}

const handleSelect = async (item) => {
  if (showEditor.value) {
    try {
      await ElMessageBox.confirm('目前有未儲存的編輯內容，確定要離開嗎？', '提示', { type: 'warning' })
      showEditor.value = false
    } catch {
      return
    }
  }
  
  selectedId.value = item.templateId
  const res = await getTemplate(item.templateId)
  selectedTemplate.value = res.data
  
  // 載入引用列表
  loadReferences(item.templateId)
}

// 載入引用此圖文範本的訊息範本列表
const loadReferences = async (imagemapId) => {
  loadingRefs.value = true
  referenceList.value = []
  try {
    const res = await getImagemapReferences(imagemapId)
    referenceList.value = res.data || []
  } catch (e) {
    console.error('載入引用列表失敗', e)
  } finally {
    loadingRefs.value = false
  }
}

// 同步更新引用的訊息範本
const handleSyncReferences = async () => {
  if (!selectedTemplate.value) return
  syncing.value = true
  try {
    const res = await syncImagemapReferences(selectedTemplate.value.templateId)
    ElMessage.success(res.msg || '同步完成')
  } catch (e) {
    ElMessage.error('同步失敗：' + (e.message || '未知錯誤'))
  } finally {
    syncing.value = false
  }
}

const handleAdd = () => {
  isNew.value = true
  showEditor.value = true
  resetForm()
}

const handleEdit = () => {
  if (!selectedTemplate.value) return
  isNew.value = false
  showEditor.value = true
  
  Object.assign(form, {
    templateId: selectedTemplate.value.templateId,
    templateName: selectedTemplate.value.templateName,
    templateCode: selectedTemplate.value.templateCode,
    status: selectedTemplate.value.status,
    sortOrder: selectedTemplate.value.sortOrder || 0,
    remark: selectedTemplate.value.remark
  })
  
  try {
    imagemapData.value = JSON.parse(selectedTemplate.value.content)
  } catch {
    imagemapData.value = {
      baseUrl: '',
      altText: '圖片訊息',
      baseSize: { width: 1040, height: 700 },
      actions: []
    }
  }
}

const handleCancel = () => {
  showEditor.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(form, {
    templateId: null,
    templateName: '',
    templateCode: '',
    status: 1,
    sortOrder: 0,
    remark: ''
  })
  imagemapData.value = {
    baseUrl: '',
    altText: '圖片訊息',
    baseSize: { width: 1040, height: 700 },
    actions: []
  }
}

const onImagemapChange = (data) => {
  imagemapData.value = data
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    // 滾動到第一個錯誤欄位
    nextTick(() => {
      const errorEl = document.querySelector('.el-form-item.is-error')
      if (errorEl) {
        errorEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    })
    return
  }

  // 使用 ImagemapEditor 的驗證方法
  const imagemapValidation = imagemapEditorRef.value?.validate()
  if (imagemapValidation && !imagemapValidation.valid) {
    ElMessage.warning(imagemapValidation.message)
    // 滾動到圖文訊息編輯器
    nextTick(() => {
      const editorSection = document.querySelector('.imagemap-editor')
      if (editorSection) {
        editorSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    })
    return
  }

  saving.value = true
  try {
    const content = JSON.stringify(imagemapData.value)
    // previewImg 需要加上尺寸後綴才能訪問實際圖片
    let previewImg = imagemapData.value.baseUrl || ''
    if (previewImg && !previewImg.match(/\/\d+$/)) {
      previewImg = previewImg + '/700'
    }
    const submitData = {
      ...form,
      msgType: 'IMAGEMAP',
      content,
      altText: imagemapData.value.altText,
      previewImg
    }

    if (form.templateId) {
      await updateTemplate(submitData)
      // 自動同步更新引用此圖文的所有範本
      try {
        const syncRes = await syncImagemapReferences(form.templateId)
        const syncCount = syncRes.data || 0
        if (syncCount > 0) {
          ElMessage.success(`修改成功，已同步更新 ${syncCount} 個引用範本`)
        } else {
          ElMessage.success('修改成功')
        }
      } catch {
        ElMessage.success('修改成功')
      }
    } else {
      const res = await addTemplate(submitData)
      if (res.data) {
        form.templateId = res.data
      }
      ElMessage.success('新增成功')
    }

    showEditor.value = false
    await getList()
    
    // 選中剛編輯的範本
    if (form.templateId) {
      const template = templateList.value.find(t => t.templateId === form.templateId)
      if (template) {
        await handleSelect(template)
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '儲存失敗')
  } finally {
    saving.value = false
  }
}

const handleDelete = async () => {
  if (!selectedTemplate.value) return
  await ElMessageBox.confirm('確定要刪除此圖文訊息嗎？', '提示', { type: 'warning' })
  await delTemplate(selectedTemplate.value.templateId)
  ElMessage.success('刪除成功')
  selectedTemplate.value = null
  selectedId.value = null
  await getList()
}

const handleStatusChange = async (template) => {
  try {
    await changeTemplateStatus(template.templateId, template.status)
    ElMessage.success(template.status === 1 ? '已啟用' : '已停用')
  } catch {
    template.status = template.status === 1 ? 0 : 1
  }
}

const handleDuplicate = () => {
  if (!selectedTemplate.value) return
  isNew.value = true
  showEditor.value = true
  
  Object.assign(form, {
    templateId: null,
    templateName: selectedTemplate.value.templateName + ' (副本)',
    templateCode: '',
    status: 1,
    sortOrder: 0,
    remark: selectedTemplate.value.remark
  })
  
  try {
    imagemapData.value = JSON.parse(selectedTemplate.value.content)
  } catch {
    imagemapData.value = {
      baseUrl: '',
      altText: '圖片訊息',
      baseSize: { width: 1040, height: 700 },
      actions: []
    }
  }
}

onMounted(() => {
  getList()
})

// 頁面重新激活時（從 keep-alive 緩存恢復），重新載入引用列表
onActivated(() => {
  if (selectedTemplate.value?.templateId) {
    loadReferences(selectedTemplate.value.templateId)
  }
})
</script>

<style scoped lang="scss">
.imagemap-container {
  display: flex;
  height: calc(100vh - 84px);
  background: #f0f2f5;
}

.resize-handle {
  width: 6px;
  cursor: col-resize;
  background: transparent;
  transition: background 0.2s;
  margin: 12px 0;
  border-radius: 3px;
  
  &:hover,
  &.is-resizing {
    background: #409EFF;
  }
}

.left-panel {
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  margin: 12px;
  margin-right: 0;
  border-radius: 8px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  
  .panel-title {
    font-size: 16px;
    font-weight: 500;
  }
}

.panel-search {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.panel-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.list-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
  
  &.active {
    background: #ecf5ff;
    border: 1px solid #409EFF;
  }
  
  .item-preview {
    width: 60px;
    height: 60px;
    background: #f5f7fa;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    flex-shrink: 0;
    
    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }
  }
  
  .item-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  
  .item-name {
    font-weight: 500;
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .item-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .item-time {
      font-size: 12px;
      color: #909399;
    }
  }
}

.right-panel {
  flex: 1;
  min-width: 0;
  margin: 12px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.editor-header,
.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
  
  .editor-title,
  .preview-title {
    font-size: 16px;
    font-weight: 500;
  }
  
  .editor-actions,
  .preview-actions {
    display: flex;
    gap: 8px;
  }
}

.editor-body,
.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.form-section {
  margin-bottom: 24px;
  
  .section-title {
    font-weight: 500;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }
}

.preview-section {
  margin-top: 24px;
  
  .section-subtitle {
    font-weight: 500;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.refs-loading {
  color: #909399;
  font-size: 13px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refs-empty {
  color: #909399;
  font-size: 13px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.refs-list {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .ref-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 4px;

    .el-icon {
      color: #409eff;
    }

    .ref-name {
      flex: 1;
      font-size: 13px;
    }
  }
}

.imagemap-preview {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
