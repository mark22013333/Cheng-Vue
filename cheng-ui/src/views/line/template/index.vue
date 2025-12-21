<template>
  <div class="template-container">
    <!-- 左側範本列表 -->
    <div class="left-panel">
      <TemplateList
        :list="templateList"
        :loading="loading"
        :selected-id="selectedTemplateId"
        @select="handleSelectTemplate"
        @add="handleAdd"
      />
    </div>

    <!-- 右側編輯區域 -->
    <div class="right-panel">
      <template v-if="showEditor">
        <TemplateEditor
          :template="currentTemplate"
          :flex-templates="flexTemplates"
          @save="handleSave"
          @cancel="handleCancel"
        />
      </template>
      <template v-else-if="selectedTemplate">
        <!-- 預覽模式 -->
        <div class="preview-panel">
          <div class="preview-header">
            <div class="preview-title">{{ selectedTemplate.templateName }}</div>
            <div class="preview-actions">
              <el-button type="primary" :icon="Edit" @click="handleEdit">編輯</el-button>
              <el-button :icon="View" @click="handlePreview">預覽</el-button>
              <el-dropdown trigger="click">
                <el-button :icon="More">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleUseTemplate">使用此範本推播</el-dropdown-item>
                    <el-dropdown-item @click="handleDuplicate">複製範本</el-dropdown-item>
                    <el-dropdown-item divided @click="handleDelete" style="color: #f56c6c">刪除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div class="preview-body">
            <div class="preview-info">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="範本代碼">{{ selectedTemplate.templateCode || '-' }}</el-descriptions-item>
                <el-descriptions-item label="訊息類型">
                  <el-tag :type="getMsgTypeTag(selectedTemplate.msgType)" size="small">
                    {{ getMsgTypeLabel(selectedTemplate.msgType) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="狀態">
                  <el-switch
                    v-model="selectedTemplate.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleStatusChange(selectedTemplate)"
                  />
                </el-descriptions-item>
                <el-descriptions-item label="使用次數">{{ selectedTemplate.useCount || 0 }} 次</el-descriptions-item>
                <el-descriptions-item label="建立時間" :span="2">{{ selectedTemplate.createTime }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <div class="preview-content">
              <div class="preview-subtitle">訊息預覽</div>
              <div class="preview-message">
                <MessagePreview
                  :msg-type="selectedTemplate.msgType"
                  :content="selectedTemplate.content"
                  :preview-img="selectedTemplate.previewImg"
                  :full-size="true"
                  :bot-name="botName"
                  :bot-avatar="botAvatar"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <!-- 空狀態 -->
        <div class="empty-panel">
          <el-empty description="選擇左側範本查看詳情，或點擊「新增範本」開始建立">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增範本</el-button>
          </el-empty>
        </div>
      </template>
    </div>

    <!-- 預覽對話框 -->
    <el-dialog v-model="previewDialogVisible" title="訊息預覽" width="500px" destroy-on-close>
      <div class="preview-dialog-content">
        <MessagePreview
          :msg-type="selectedTemplate?.msgType"
          :content="selectedTemplate?.content"
          :preview-img="selectedTemplate?.previewImg"
          :full-size="true"
          :bot-name="botName"
          :bot-avatar="botAvatar"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="LineTemplate">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, View, More, ChatLineSquare, Picture, VideoCamera, Headset, Location, PriceTag, Grid, Document } from '@element-plus/icons-vue'
import TemplateList from './components/TemplateList.vue'
import TemplateEditor from './components/TemplateEditor.vue'
import MessagePreview from './components/MessagePreview.vue'
import { listTemplate, getTemplate, addTemplate, updateTemplate, delTemplate, changeTemplateStatus } from '@/api/line/template'
import { getDefaultChannel } from '@/api/line/config'

const router = useRouter()

// LINE Bot 資訊
const lineConfig = ref(null)
const botName = computed(() => lineConfig.value?.botDisplayName || lineConfig.value?.channelName || '')
const botAvatar = computed(() => lineConfig.value?.botPictureUrl || '')

const msgTypeOptions = [
  { value: 'TEXT', label: '文字', tag: 'primary' },
  { value: 'IMAGE', label: '圖片', tag: 'success' },
  { value: 'VIDEO', label: '影片', tag: 'success' },
  { value: 'AUDIO', label: '音訊', tag: 'success' },
  { value: 'LOCATION', label: '位置', tag: 'info' },
  { value: 'STICKER', label: '貼圖', tag: 'info' },
  { value: 'IMAGEMAP', label: '圖片地圖', tag: 'warning' },
  { value: 'FLEX', label: 'Flex', tag: 'danger' }
]

// Flex 預設範本（內嵌基礎結構，完整範本可從 docs/Line/FlexTemplate 載入）
const flexTemplates = {
  restaurant: {
    type: 'bubble',
    hero: { type: 'image', url: 'https://developers-resource.landpress.line.me/fx/img/01_1_cafe.png', size: 'full', aspectRatio: '20:13', aspectMode: 'cover' },
    body: {
      type: 'box', layout: 'vertical',
      contents: [
        { type: 'text', text: '餐廳名稱', weight: 'bold', size: 'xl' },
        { type: 'box', layout: 'baseline', margin: 'md', contents: [
          { type: 'icon', size: 'sm', url: 'https://developers-resource.landpress.line.me/fx/img/review_gold_star_28.png' },
          { type: 'text', text: '4.0', size: 'sm', color: '#999999', margin: 'md' }
        ]},
        { type: 'box', layout: 'vertical', margin: 'lg', spacing: 'sm', contents: [
          { type: 'box', layout: 'baseline', spacing: 'sm', contents: [
            { type: 'text', text: '地址', color: '#aaaaaa', size: 'sm', flex: 1 },
            { type: 'text', text: '請輸入地址', wrap: true, color: '#666666', size: 'sm', flex: 5 }
          ]}
        ]}
      ]
    },
    footer: { type: 'box', layout: 'vertical', spacing: 'sm', contents: [
      { type: 'button', style: 'link', height: 'sm', action: { type: 'uri', label: '撥打電話', uri: 'tel:0900000000' }}
    ]}
  },
  hotel: {
    type: 'bubble',
    body: {
      type: 'box', layout: 'vertical', paddingAll: '0px',
      contents: [
        { type: 'image', url: 'https://developers-resource.landpress.line.me/fx/clip/clip3.jpg', size: 'full', aspectMode: 'cover', aspectRatio: '1:1', gravity: 'center' },
        { type: 'box', layout: 'horizontal', position: 'absolute', offsetBottom: '0px', offsetStart: '0px', offsetEnd: '0px', paddingAll: '20px', contents: [
          { type: 'box', layout: 'vertical', spacing: 'xs', contents: [
            { type: 'text', text: '飯店名稱', size: 'xl', color: '#ffffff' },
            { type: 'text', text: '¥62,000', color: '#ffffff', size: 'md' }
          ]}
        ]}
      ]
    }
  },
  realestate: {
    type: 'bubble',
    body: {
      type: 'box', layout: 'vertical', paddingAll: '20px', backgroundColor: '#464F69',
      contents: [
        { type: 'text', text: '物件名稱', size: 'xl', color: '#ffffff', weight: 'bold' },
        { type: 'text', text: '3 房, ¥35,000/月', color: '#ffffffcc', size: 'sm' },
        { type: 'box', layout: 'vertical', margin: 'xl', paddingAll: '13px', backgroundColor: '#ffffff1A', cornerRadius: '2px', contents: [
          { type: 'text', text: '特色：私人泳池、地暖系統', size: 'sm', color: '#ffffffde', wrap: true }
        ]}
      ]
    }
  },
  apparel: {
    type: 'bubble',
    body: {
      type: 'box', layout: 'vertical', paddingAll: '0px',
      contents: [
        { type: 'image', url: 'https://developers-resource.landpress.line.me/fx/clip/clip1.jpg', size: 'full', aspectMode: 'cover', aspectRatio: '2:3', gravity: 'top' },
        { type: 'box', layout: 'vertical', position: 'absolute', offsetBottom: '0px', offsetStart: '0px', offsetEnd: '0px', backgroundColor: '#03303Acc', paddingAll: '20px', contents: [
          { type: 'text', text: '商品名稱', size: 'xl', color: '#ffffff', weight: 'bold' },
          { type: 'box', layout: 'baseline', spacing: 'lg', contents: [
            { type: 'text', text: '¥35,800', color: '#ebebeb', size: 'sm' },
            { type: 'text', text: '¥75,000', color: '#ffffffcc', decoration: 'line-through', size: 'sm' }
          ]}
        ]}
      ]
    }
  },
  localsearch: {
    type: 'carousel',
    contents: [
      { type: 'bubble', size: 'micro',
        hero: { type: 'image', url: 'https://developers-resource.landpress.line.me/fx/clip/clip10.jpg', size: 'full', aspectMode: 'cover', aspectRatio: '320:213' },
        body: { type: 'box', layout: 'vertical', spacing: 'sm', paddingAll: '13px', contents: [
          { type: 'text', text: '地點名稱', weight: 'bold', size: 'sm', wrap: true },
          { type: 'text', text: '描述文字', color: '#8c8c8c', size: 'xs' }
        ]}
      }
    ]
  }
}

const loading = ref(false)
const templateList = ref([])
const selectedTemplateId = ref(null)
const selectedTemplate = ref(null)
const currentTemplate = ref(null)
const showEditor = ref(false)
const previewDialogVisible = ref(false)

const getMsgTypeLabel = (type) => msgTypeOptions.find(o => o.value === type)?.label || type
const getMsgTypeTag = (type) => msgTypeOptions.find(o => o.value === type)?.tag || 'info'

const getList = async () => {
  loading.value = true
  try {
    const res = await listTemplate({ pageNum: 1, pageSize: 100 })
    templateList.value = res.rows || []
  } finally {
    loading.value = false
  }
}

const handleSelectTemplate = async (template) => {
  if (showEditor.value) {
    await ElMessageBox.confirm('目前有未儲存的編輯內容，確定要離開嗎？', '提示', { type: 'warning' })
      .catch(() => { throw new Error('cancel') })
    showEditor.value = false
  }
  
  selectedTemplateId.value = template.templateId
  const res = await getTemplate(template.templateId)
  selectedTemplate.value = res.data
}

const handleAdd = () => {
  currentTemplate.value = null
  showEditor.value = true
}

const handleEdit = async () => {
  if (!selectedTemplate.value) return
  currentTemplate.value = { ...selectedTemplate.value }
  showEditor.value = true
}

const handleCancel = () => {
  showEditor.value = false
  currentTemplate.value = null
}

const handleSave = async (data) => {
  try {
    // 構建 content
    const msgType = data.messages.length === 1 ? data.messages[0].type : 'FLEX'
    let content, altText = ''

    if (data.messages.length === 1) {
      const msg = data.messages[0]
      content = buildMessageContent(msg)
      altText = msg.altText || ''
    } else {
      // 多訊息組合
      content = JSON.stringify({ messages: data.messages.map(buildMessageObject) })
      altText = data.messages.find(m => m.altText)?.altText || '多訊息範本'
    }

    const submitData = {
      templateId: data.templateId,
      templateName: data.templateName,
      templateCode: data.templateCode,
      msgType,
      content,
      altText,
      status: data.status,
      sortOrder: data.sortOrder,
      remark: data.remark
    }

    if (data.templateId) {
      await updateTemplate(submitData)
      ElMessage.success('修改成功')
    } else {
      const res = await addTemplate(submitData)
      if (res.data) {
        submitData.templateId = res.data
      }
      ElMessage.success('新增成功')
    }

    await getList()
    
    // 選中剛編輯的範本並保持編輯狀態
    const targetId = submitData.templateId
    if (targetId) {
      const template = templateList.value.find(t => t.templateId === targetId)
      if (template) {
        selectedTemplateId.value = targetId
        selectedTemplate.value = template
        currentTemplate.value = { ...template }
        showEditor.value = true
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '儲存失敗')
  }
}

const buildMessageContent = (msg) => {
  switch (msg.type) {
    case 'TEXT':
      return msg.text
    case 'IMAGE':
      return JSON.stringify({
        type: 'image',
        originalContentUrl: msg.originalContentUrl,
        previewImageUrl: msg.previewImageUrl || msg.originalContentUrl
      })
    case 'VIDEO':
      return JSON.stringify({
        type: 'video',
        originalContentUrl: msg.originalContentUrl,
        previewImageUrl: msg.previewImageUrl
      })
    case 'AUDIO':
      return JSON.stringify({
        type: 'audio',
        originalContentUrl: msg.originalContentUrl,
        duration: msg.duration
      })
    case 'LOCATION':
      return JSON.stringify({
        type: 'location',
        title: msg.title,
        address: msg.address,
        latitude: msg.latitude,
        longitude: msg.longitude
      })
    case 'STICKER':
      return JSON.stringify({
        type: 'sticker',
        packageId: msg.packageId,
        stickerId: msg.stickerId
      })
    case 'FLEX':
    case 'IMAGEMAP':
      return msg.contents
    default:
      return msg.text || ''
  }
}

const buildMessageObject = (msg) => {
  const obj = { type: msg.type.toLowerCase() }
  switch (msg.type) {
    case 'TEXT':
      obj.text = msg.text
      break
    case 'IMAGE':
      obj.originalContentUrl = msg.originalContentUrl
      obj.previewImageUrl = msg.previewImageUrl || msg.originalContentUrl
      break
    case 'VIDEO':
      obj.originalContentUrl = msg.originalContentUrl
      obj.previewImageUrl = msg.previewImageUrl
      break
    case 'AUDIO':
      obj.originalContentUrl = msg.originalContentUrl
      obj.duration = msg.duration
      break
    case 'LOCATION':
      obj.title = msg.title
      obj.address = msg.address
      obj.latitude = msg.latitude
      obj.longitude = msg.longitude
      break
    case 'STICKER':
      obj.packageId = msg.packageId
      obj.stickerId = msg.stickerId
      break
    case 'FLEX':
      obj.altText = msg.altText
      obj.contents = JSON.parse(msg.contents)
      break
    case 'IMAGEMAP':
      obj.altText = msg.altText
      Object.assign(obj, JSON.parse(msg.contents))
      break
  }
  return obj
}

const handleDelete = async () => {
  if (!selectedTemplate.value) return
  await ElMessageBox.confirm('確定要刪除此範本嗎？', '提示', { type: 'warning' })
  await delTemplate(selectedTemplate.value.templateId)
  ElMessage.success('刪除成功')
  selectedTemplate.value = null
  selectedTemplateId.value = null
  await getList()
}

const handleStatusChange = async (template) => {
  try {
    await changeTemplateStatus(template.templateId, template.status)
    ElMessage.success(template.status === 1 ? '已啟用' : '已停用')
  } catch (e) {
    template.status = template.status === 1 ? 0 : 1
  }
}

const handlePreview = () => {
  previewDialogVisible.value = true
}

const handleUseTemplate = () => {
  if (!selectedTemplate.value) return
  router.push({ path: '/line/message', query: { templateId: selectedTemplate.value.templateId } })
}

const handleDuplicate = async () => {
  if (!selectedTemplate.value) return
  currentTemplate.value = {
    ...selectedTemplate.value,
    templateId: null,
    templateName: selectedTemplate.value.templateName + ' (副本)',
    templateCode: ''
  }
  showEditor.value = true
}

// 取得 LINE Bot 設定
const getLineConfig = async () => {
  try {
    const res = await getDefaultChannel()
    if (res.data) {
      lineConfig.value = res.data
    }
  } catch (e) {
    console.warn('無法取得 LINE 設定:', e)
  }
}

onMounted(() => {
  getList()
  getLineConfig()
})
</script>

<style scoped lang="scss">
.template-container {
  display: flex;
  height: calc(100vh - 84px);
  background: #f0f2f5;
}

.left-panel {
  width: 320px;
  flex-shrink: 0;
}

.right-panel {
  flex: 1;
  min-width: 0;
  margin: 12px;
  margin-left: 0;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.preview-panel {
  display: flex;
  flex-direction: column;
  height: 100%;

  .preview-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;
    background: #fafafa;

    .preview-title {
      font-size: 16px;
      font-weight: 500;
    }

    .preview-actions {
      display: flex;
      gap: 8px;
    }
  }

  .preview-body {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
  }

  .preview-info {
    margin-bottom: 24px;
  }

  .preview-content {
    .preview-subtitle {
      font-weight: 500;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;
    }

    .preview-message {
      display: flex;
      justify-content: center;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 8px;
    }
  }
}

.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.preview-dialog-content {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
</style>
