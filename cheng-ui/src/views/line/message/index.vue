<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左側：範本選擇 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>選擇訊息範本</span>
              <el-button type="primary" link @click="goToTemplateManagement">
                <el-icon><Setting /></el-icon> 管理範本
              </el-button>
            </div>
          </template>

          <!-- 篩選 -->
          <el-form :inline="true" class="filter-form">
            <el-form-item label="類型">
              <el-select v-model="filterType" placeholder="全部" clearable style="width: 120px" @change="loadTemplates">
                <el-option v-for="item in msgTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="搜尋">
              <el-input v-model="filterKeyword" placeholder="範本名稱" clearable style="width: 160px" @input="handleSearch" />
            </el-form-item>
          </el-form>

          <!-- 範本列表 -->
          <div class="template-list" v-loading="templateLoading">
            <div
              v-for="item in templateList"
              :key="item.templateId"
              class="template-item"
              :class="{ selected: selectedTemplate?.templateId === item.templateId }"
              @click="selectTemplate(item)"
            >
              <div class="template-preview">
                <MessagePreview :msg-type="item.msgType" :content="item.content" :preview-img="item.previewImg" />
              </div>
              <div class="template-info">
                <div class="template-name">{{ item.templateName }}</div>
                <el-tag :type="getMsgTypeTag(item.msgType)" size="small">{{ getMsgTypeLabel(item.msgType) }}</el-tag>
              </div>
            </div>
            <el-empty v-if="templateList.length === 0 && !templateLoading" description="沒有可用範本" />
          </div>

          <!-- 分頁 -->
          <el-pagination
            v-if="total > 0"
            small
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="loadTemplates"
            class="pagination"
          />
        </el-card>
      </el-col>

      <!-- 右側：發送設定 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span>發送設定</span>
          </template>

          <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-width="90px">
            <!-- 頻道選擇 -->
            <el-form-item label="LINE 頻道" prop="configId">
              <el-select v-model="sendForm.configId" placeholder="選擇頻道" style="width: 100%">
                <el-option
                  v-for="config in channelList"
                  :key="config.configId"
                  :label="config.channelName"
                  :value="config.configId"
                />
              </el-select>
            </el-form-item>

            <!-- 發送方式 -->
            <el-form-item label="發送方式" prop="sendType">
              <el-radio-group v-model="sendForm.sendType" @change="handleSendTypeChange">
                <el-radio-button value="PUSH">單人推播</el-radio-button>
                <el-radio-button value="MULTICAST">多人推播</el-radio-button>
                <el-radio-button value="BROADCAST">廣播</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <!-- 發送對象 -->
            <el-form-item v-if="sendForm.sendType !== 'BROADCAST'" label="發送對象" prop="targets">
              <el-select
                v-model="sendForm.targets"
                :multiple="sendForm.sendType === 'MULTICAST'"
                filterable
                remote
                reserve-keyword
                :remote-method="searchUsers"
                :loading="userLoading"
                placeholder="搜尋使用者"
                style="width: 100%"
              >
                <el-option
                  v-for="user in userOptions"
                  :key="user.lineUserId"
                  :label="user.lineDisplayName || user.lineUserId"
                  :value="user.lineUserId"
                >
                  <div class="user-option">
                    <el-avatar :src="user.linePictureUrl" :size="24" />
                    <span>{{ user.lineDisplayName || user.lineUserId }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item v-if="sendForm.sendType === 'BROADCAST'">
              <el-alert type="warning" :closable="false" show-icon>
                廣播將發送給所有好友，請謹慎使用
              </el-alert>
            </el-form-item>

            <!-- 已選範本預覽 -->
            <el-form-item label="已選範本">
              <div v-if="selectedTemplate" class="selected-template-preview">
                <MessagePreview
                  :msg-type="selectedTemplate.msgType"
                  :content="selectedTemplate.content"
                  :preview-img="selectedTemplate.previewImg"
                  :full-size="true"
                />
                <div class="template-detail">
                  <strong>{{ selectedTemplate.templateName }}</strong>
                  <el-tag :type="getMsgTypeTag(selectedTemplate.msgType)" size="small">
                    {{ getMsgTypeLabel(selectedTemplate.msgType) }}
                  </el-tag>
                </div>
              </div>
              <el-empty v-else description="請從左側選擇範本" :image-size="60" />
            </el-form-item>

            <!-- 發送按鈕 -->
            <el-form-item>
              <el-button
                v-hasPermi="['line:message:send']"
                type="primary"
                :icon="Promotion"
                :loading="sending"
                :disabled="!selectedTemplate || !canSend"
                @click="handleSend"
                size="large"
                style="width: 100%"
              >
                {{ sending ? '發送中...' : '發送訊息' }}
              </el-button>
              <div v-if="!hasPermission('line:message:send')" class="no-permission-tip">
                您沒有發送訊息的權限，請聯繫管理員
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 發送記錄 -->
        <el-card shadow="hover" class="mt16">
          <template #header>
            <span>最近發送記錄</span>
          </template>
          <el-timeline v-if="sendHistory.length > 0">
            <el-timeline-item
              v-for="(item, index) in sendHistory"
              :key="index"
              :timestamp="item.time"
              :type="item.success ? 'success' : 'danger'"
              placement="top"
            >
              <div class="history-item">
                <span>{{ item.templateName }}</span>
                <el-tag :type="item.success ? 'success' : 'danger'" size="small">
                  {{ item.success ? '成功' : '失敗' }}
                </el-tag>
              </div>
              <div class="history-detail">
                {{ item.sendType === 'BROADCAST' ? '廣播' : `發送給 ${item.targetCount} 人` }}
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暫無發送記錄" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="LineMessage">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Promotion } from '@element-plus/icons-vue'
import MessagePreview from '../template/components/MessagePreview.vue'
import { listTemplate } from '@/api/line/template'
import { listConfig } from '@/api/line/config'
import { listUser } from '@/api/line/user'
import { sendMessage } from '@/api/line/message'
import { checkPermi } from '@/utils/permission'

// 檢查權限
const hasPermission = (permission) => {
  return checkPermi([permission])
}

const route = useRoute()
const router = useRouter()

const msgTypeOptions = [
  { value: 'TEXT', label: '文字', tag: 'primary' },
  { value: 'IMAGE', label: '圖片', tag: 'success' },
  { value: 'VIDEO', label: '影片', tag: 'success' },
  { value: 'AUDIO', label: '音訊', tag: 'success' },
  { value: 'LOCATION', label: '位置', tag: 'info' },
  { value: 'STICKER', label: '貼圖', tag: 'info' },
  { value: 'IMAGEMAP', label: '圖片地圖', tag: 'warning' },
  { value: 'FLEX', label: 'Flex 訊息', tag: 'danger' }
]

const getMsgTypeLabel = (type) => msgTypeOptions.find(o => o.value === type)?.label || type
const getMsgTypeTag = (type) => msgTypeOptions.find(o => o.value === type)?.tag || 'info'

const templateLoading = ref(false)
const templateList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)
const filterType = ref('')
const filterKeyword = ref('')
const selectedTemplate = ref(null)

const channelList = ref([])
const userOptions = ref([])
const userLoading = ref(false)
const sending = ref(false)
const sendHistory = ref([])

const sendFormRef = ref(null)
const sendForm = reactive({
  configId: null,
  sendType: 'PUSH',
  targets: null
})

const sendRules = {
  configId: [{ required: true, message: '請選擇頻道', trigger: 'change' }],
  targets: [{ required: true, message: '請選擇發送對象', trigger: 'change' }]
}

const canSend = computed(() => {
  if (!sendForm.configId) return false
  if (sendForm.sendType === 'BROADCAST') return true
  if (sendForm.sendType === 'PUSH') return !!sendForm.targets
  if (sendForm.sendType === 'MULTICAST') return Array.isArray(sendForm.targets) && sendForm.targets.length > 0
  return false
})

let searchTimer = null
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadTemplates()
  }, 300)
}

const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await listTemplate({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      msgType: filterType.value || undefined,
      templateName: filterKeyword.value || undefined,
      status: 1
    })
    templateList.value = res.rows || []
    total.value = res.total || 0
  } finally {
    templateLoading.value = false
  }
}

const loadChannels = async () => {
  try {
    const res = await listConfig({ status: 1 })
    channelList.value = res.rows || []
    if (channelList.value.length > 0) {
      const defaultChannel = channelList.value.find(c => c.isDefault === 1)
      sendForm.configId = defaultChannel ? defaultChannel.configId : channelList.value[0].configId
    }
  } catch (e) {
    console.error('載入頻道失敗:', e)
  }
}

const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }
  userLoading.value = true
  try {
    const res = await listUser({ lineDisplayName: query, pageNum: 1, pageSize: 20 })
    userOptions.value = res.rows || []
  } catch (e) {
    console.error('搜尋使用者失敗:', e)
  } finally {
    userLoading.value = false
  }
}

const selectTemplate = (template) => {
  selectedTemplate.value = template
}

const handleSendTypeChange = () => {
  sendForm.targets = sendForm.sendType === 'MULTICAST' ? [] : null
}

const handleSend = async () => {
  if (!selectedTemplate.value) {
    ElMessage.warning('請選擇訊息範本')
    return
  }

  await sendFormRef.value?.validate()

  const confirmMsg = sendForm.sendType === 'BROADCAST'
    ? '確定要廣播此訊息給所有好友嗎？'
    : `確定要發送此訊息嗎？`

  await ElMessageBox.confirm(confirmMsg, '確認發送', { type: 'warning' })

  sending.value = true
  try {
    const data = {
      configId: sendForm.configId,
      templateId: selectedTemplate.value.templateId,
      sendType: sendForm.sendType
    }

    if (sendForm.sendType === 'PUSH') {
      data.targetUserId = sendForm.targets
    } else if (sendForm.sendType === 'MULTICAST') {
      data.targetUserIds = sendForm.targets
    }

    await sendMessage(data)
    ElMessage.success('發送成功')

    sendHistory.value.unshift({
      templateName: selectedTemplate.value.templateName,
      sendType: sendForm.sendType,
      targetCount: sendForm.sendType === 'BROADCAST' ? '全部' : (Array.isArray(sendForm.targets) ? sendForm.targets.length : 1),
      success: true,
      time: new Date().toLocaleString()
    })

    if (sendHistory.value.length > 5) {
      sendHistory.value.pop()
    }
  } catch (e) {
    sendHistory.value.unshift({
      templateName: selectedTemplate.value.templateName,
      sendType: sendForm.sendType,
      targetCount: sendForm.sendType === 'BROADCAST' ? '全部' : (Array.isArray(sendForm.targets) ? sendForm.targets.length : 1),
      success: false,
      time: new Date().toLocaleString()
    })
  } finally {
    sending.value = false
  }
}

const goToTemplateManagement = () => {
  router.push('/cadm/line/template')
}

onMounted(async () => {
  await Promise.all([loadTemplates(), loadChannels()])

  if (route.query.templateId) {
    const targetId = parseInt(route.query.templateId)
    const found = templateList.value.find(t => t.templateId === targetId)
    if (found) {
      selectTemplate(found)
    }
  }
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 16px;

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 16px;
  }
}

.template-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
}

.template-item {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  }

  &.selected {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .template-preview {
    height: 100px;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }

  .template-info {
    padding: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;

    .template-name {
      font-size: 13px;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      flex: 1;
    }
  }
}

.pagination {
  margin-top: 16px;
  justify-content: center;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-template-preview {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;

  .template-detail {
    margin-top: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
  }
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.mt16 {
  margin-top: 16px;
}

.no-permission-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 12px;
  text-align: center;
}
</style>
