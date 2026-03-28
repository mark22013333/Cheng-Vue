<template>
  <div class="app-container line-message-page">
    <el-row :gutter="16">
      <!-- 左側：範本選擇 -->
      <el-col :xs="24" :sm="24" :md="15" :lg="15">
        <el-card shadow="never" class="template-card" role="region" aria-label="訊息範本選擇區">
          <template #header>
            <div class="card-header">
              <div class="card-title-group">
                <span class="card-title">選擇訊息範本</span>
                <el-tag effect="plain" size="small" round v-if="total > 0">{{ total }} 個範本</el-tag>
              </div>
              <el-button type="primary" link @click="goToTemplateManagement">
                <el-icon><Setting /></el-icon> 管理範本
              </el-button>
            </div>
          </template>

          <!-- 篩選列 -->
          <div class="filter-bar">
            <el-select v-model="filterType" placeholder="全部類型" clearable @change="loadTemplates" class="filter-type">
              <el-option v-for="item in msgTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-input
              v-model="filterKeyword"
              placeholder="搜尋範本名稱..."
              clearable
              :prefix-icon="Search"
              @input="handleSearch"
              class="filter-search"
            />
          </div>

          <!-- 範本列表 -->
          <div class="template-list" v-loading="templateLoading" role="listbox" aria-label="訊息範本列表">
            <!-- 載入範本失敗提示 -->
            <div v-if="templateLoadError" class="template-empty">
              <el-icon :size="48" color="#F53F3F"><WarningFilled /></el-icon>
              <span class="error-hint-text">載入範本失敗</span>
              <el-button type="primary" @click="loadTemplates">重新載入</el-button>
            </div>
            <div
              v-for="item in templateList"
              :key="item.templateId"
              class="template-item"
              :class="{ selected: selectedTemplate?.templateId === item.templateId }"
              role="option"
              :aria-selected="selectedTemplate?.templateId === item.templateId"
              tabindex="0"
              @click="selectTemplate(item)"
              @keydown.enter="selectTemplate(item)"
            >
              <div class="template-check" v-if="selectedTemplate?.templateId === item.templateId">
                <el-icon><Check /></el-icon>
              </div>
              <div class="template-preview">
                <MessagePreview :msg-type="item.msgType" :content="item.content" :preview-img="item.previewImg" />
              </div>
              <div class="template-info">
                <div class="template-name" :title="item.templateName">{{ item.templateName }}</div>
                <el-tag :type="getMsgTypeTag(item.msgType)" size="small" effect="light" round>{{ getMsgTypeLabel(item.msgType) }}</el-tag>
              </div>
            </div>
            <div v-if="templateList.length === 0 && !templateLoading && !templateLoadError" class="template-empty">
              <el-empty description="沒有可用範本" :image-size="80">
                <el-button type="primary" @click="goToTemplateManagement">前往建立範本</el-button>
              </el-empty>
            </div>
          </div>

          <!-- 分頁 -->
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              small
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="loadTemplates"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 右側：發送設定 -->
      <el-col :xs="24" :sm="24" :md="9" :lg="9">
        <el-card shadow="never" class="send-card" role="region" aria-label="發送設定區">
          <template #header>
            <div class="card-header">
              <span class="card-title">發送設定</span>
              <el-tag :type="sendStepTag" size="small" effect="dark" round>{{ sendStepText }}</el-tag>
            </div>
          </template>

          <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-position="top">
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
              <el-radio-group v-model="sendForm.sendType" @change="handleSendTypeChange" class="send-type-group">
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
                placeholder="輸入名稱搜尋使用者..."
                style="width: 100%"
              >
                <template #empty>
                  <div class="user-search-empty">
                    <span>找不到符合的使用者</span>
                  </div>
                </template>
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
                  <div class="template-detail-actions">
                    <el-tag :type="getMsgTypeTag(selectedTemplate.msgType)" size="small" effect="light" round>
                      {{ getMsgTypeLabel(selectedTemplate.msgType) }}
                    </el-tag>
                    <el-button type="primary" link @click="previewDialogVisible = true" class="zoom-btn" aria-label="放大預覽範本">
                      <el-icon :size="18"><ZoomIn /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
              <div v-else class="empty-template-hint">
                <el-icon :size="32" color="#86909C"><Document /></el-icon>
                <span>請從左側選擇一個範本</span>
              </div>
            </el-form-item>

            <!-- 發送按鈕 -->
            <el-form-item class="send-btn-wrapper">
              <el-button
                v-hasPermi="[LINE_MESSAGE_SEND]"
                type="primary"
                :icon="Promotion"
                :loading="sending"
                :disabled="!selectedTemplate || !canSend"
                @click="handleSend"
                size="large"
                class="send-btn"
              >
                {{ sending ? '發送中...' : '發送訊息' }}
              </el-button>
              <div v-if="!hasPermission(LINE_MESSAGE_SEND)" class="no-permission-tip">
                您沒有發送訊息的權限，請聯繫管理員
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 發送記錄 -->
        <el-card shadow="never" class="history-card" role="region" aria-label="發送記錄區">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近發送記錄</span>
              <div class="card-header-right">
                <el-tag v-if="sendHistory.length > 0" effect="plain" size="small" round>{{ sendHistory.length }} 筆</el-tag>
                <el-button
                  v-if="sendHistory.length > 0"
                  type="primary"
                  link
                  class="history-toggle-btn"
                  @click="historyCollapsed = !historyCollapsed"
                  :aria-expanded="!historyCollapsed"
                  aria-controls="history-content"
                >
                  <el-icon><ArrowUp v-if="!historyCollapsed" /><ArrowDown v-else /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <div id="history-content" v-show="!historyCollapsed">
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
                  <el-tag :type="item.success ? 'success' : 'danger'" size="small" round>
                    {{ item.success ? '成功' : '失敗' }}
                  </el-tag>
                </div>
                <div class="history-detail">
                  {{ item.sendType === 'BROADCAST' ? '廣播' : `發送給 ${item.targetCount} 人` }}
                </div>
              </el-timeline-item>
            </el-timeline>
            <div v-else class="history-empty">
              <el-icon :size="28" color="#86909C"><Clock /></el-icon>
              <span>暫無發送記錄</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 範本放大預覽 Dialog -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="selectedTemplate?.templateName || '範本預覽'"
      width="600px"
      class="template-preview-dialog"
      destroy-on-close
      append-to-body
    >
      <div v-if="selectedTemplate" class="preview-dialog-content">
        <MessagePreview
          :msg-type="selectedTemplate.msgType"
          :content="selectedTemplate.content"
          :preview-img="selectedTemplate.previewImg"
          :full-size="true"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="LineMessage">
import {
  LINE_MESSAGE_SEND
} from '@/constants/permissions'
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Promotion, Search, Check, Document, Clock, ZoomIn, WarningFilled, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import MessagePreview from '../template/components/MessagePreview.vue'
import { listTemplate } from '@/api/line/template'
import { listConfig } from '@/api/line/config'
import { listUser } from '@/api/line/user'
import { sendMessage, listMessageLog, addMessageLog } from '@/api/line/message'
import { checkPermi } from '@/utils/permission'

/** 廣播二次確認關鍵字 */
const BROADCAST_CONFIRM_KEYWORD = '確認廣播'

/** 前端 sendType 對應後端 targetType 的映射 */
const SEND_TYPE_TO_TARGET_TYPE = {
  'PUSH': 'SINGLE',
  'MULTICAST': 'MULTIPLE',
  'BROADCAST': 'ALL'
}

/** 後端 targetType 對應前端顯示的映射 */
const TARGET_TYPE_TO_SEND_TYPE = {
  'SINGLE': 'PUSH',
  'MULTIPLE': 'MULTICAST',
  'ALL': 'BROADCAST'
}

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
const templateLoadError = ref(false)
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
const previewDialogVisible = ref(false)
const historyCollapsed = ref(false)

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

const sendStepText = computed(() => {
  if (!sendForm.configId) return '請選擇頻道'
  if (!selectedTemplate.value) return '請選擇範本'
  if (sendForm.sendType !== 'BROADCAST' && !sendForm.targets) return '請選擇對象'
  return '準備就緒'
})

const sendStepTag = computed(() => {
  return sendStepText.value === '準備就緒' ? 'success' : 'info'
})

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
  templateLoadError.value = false
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
  } catch (e) {
    console.error('載入範本失敗:', e)
    templateLoadError.value = true
    templateList.value = []
    total.value = 0
    ElMessage.error('載入範本失敗，請重新整理頁面')
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
    ElMessage.error('載入頻道失敗，請重新整理頁面')
  }
}

const loadHistory = async () => {
  try {
    const res = await listMessageLog({ pageNum: 1, pageSize: 5 })
    sendHistory.value = (res.rows || []).map(log => ({
      templateName: log.messageContent || '訊息',
      sendType: TARGET_TYPE_TO_SEND_TYPE[log.targetType] || log.targetType || '',
      targetCount: log.targetCount,
      success: log.sendStatus === 'SUCCESS',
      time: log.createTime
    }))
  } catch (e) {
    console.error('載入發送記錄失敗:', e)
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
    ElMessage.error('搜尋使用者失敗，請重試')
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

  // 廣播需要二次確認：輸入「確認廣播」才能發送
  if (sendForm.sendType === 'BROADCAST') {
    const { value: inputValue } = await ElMessageBox.prompt(
      `廣播將發送給所有好友且不可撤回，請輸入「${BROADCAST_CONFIRM_KEYWORD}」以確認`,
      '廣播確認',
      {
        type: 'warning',
        inputPattern: new RegExp(`^${BROADCAST_CONFIRM_KEYWORD}$`),
        inputErrorMessage: `請輸入「${BROADCAST_CONFIRM_KEYWORD}」`,
        inputPlaceholder: BROADCAST_CONFIRM_KEYWORD,
        confirmButtonText: '發送廣播',
        cancelButtonText: '取消'
      }
    )
    // 額外防禦：即使通過 pattern，仍核對一次
    if (inputValue !== BROADCAST_CONFIRM_KEYWORD) return
  } else {
    await ElMessageBox.confirm('確定要發送此訊息嗎？', '確認發送', { type: 'warning' })
  }

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

    // 記錄發送成功
    try {
      await addMessageLog({
        configId: sendForm.configId,
        targetType: SEND_TYPE_TO_TARGET_TYPE[sendForm.sendType],
        targetCount: sendForm.sendType === 'BROADCAST' ? 0 : (Array.isArray(sendForm.targets) ? sendForm.targets.length : 1),
        sendStatus: 'SUCCESS',
        messageContent: selectedTemplate.value.templateName
      })
    } catch (logErr) {
      console.error('記錄發送日誌失敗:', logErr)
    }
    await loadHistory()
  } catch (e) {
    ElMessage.error('發送失敗：' + (e.response?.data?.msg || '請稍後重試'))

    // 記錄發送失敗
    try {
      await addMessageLog({
        configId: sendForm.configId,
        targetType: SEND_TYPE_TO_TARGET_TYPE[sendForm.sendType],
        targetCount: sendForm.sendType === 'BROADCAST' ? 0 : (Array.isArray(sendForm.targets) ? sendForm.targets.length : 1),
        sendStatus: 'FAILED',
        messageContent: selectedTemplate.value.templateName,
        errorMessage: e.response?.data?.msg || e.message || '未知錯誤'
      })
    } catch (logErr) {
      console.error('記錄發送日誌失敗:', logErr)
    }
    await loadHistory()
  } finally {
    sending.value = false
  }
}

const goToTemplateManagement = () => {
  router.push('/cadm/line/template')
}

onMounted(async () => {
  // 手機版預設收合發送記錄
  const MOBILE_BREAKPOINT = 767
  if (window.innerWidth <= MOBILE_BREAKPOINT) {
    historyCollapsed.value = true
  }

  await Promise.all([loadTemplates(), loadChannels(), loadHistory()])

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
.line-message-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 130px);

  > .el-row {
    flex: 1;
    align-items: stretch;

    > .el-col {
      display: flex;
      flex-direction: column;
    }
  }

  :deep(.el-card) {
    border-radius: 10px;
    border: 1px solid #E5E6EB;
  }

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #F2F3F5;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
}

/* ===== 篩選列 ===== */
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;

  .filter-type {
    width: 130px;
    flex-shrink: 0;
  }

  .filter-search {
    flex: 1;
  }
}

/* ===== 範本卡片（撐滿剩餘空間） ===== */
.template-card {
  flex: 1;
  display: flex;
  flex-direction: column;

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

.template-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  flex: 1;
  align-content: start;
  min-height: 0;
  overflow-y: auto;
  padding: 2px;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: #C9CDD4;
    border-radius: 3px;
  }
}

.template-item {
  position: relative;
  border: 2px solid #E5E6EB;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 150ms ease-out;
  background: #fff;
  outline: none;

  &:hover,
  &:focus-visible {
    border-color: #79BBFF;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.12);
    transform: translateY(-1px);
  }

  &:focus-visible {
    box-shadow: 0 0 0 2px #409eff, 0 4px 12px rgba(64, 158, 255, 0.12);
  }

  &.selected {
    border-color: #409eff;
    box-shadow: 0 0 0 1px #409eff, 0 4px 12px rgba(64, 158, 255, 0.18);

    .template-info {
      background: #D9ECFF;
    }
  }

  .template-check {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: #409eff;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    z-index: 2;
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.35);
  }

  .template-preview {
    height: 130px;
    background: linear-gradient(135deg, #F7F8FA 0%, #F2F3F5 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    padding: 8px;
  }

  .template-info {
    padding: 8px 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    border-top: 1px solid #F2F3F5;
    transition: background 150ms ease-out;

    .template-name {
      font-size: 13px;
      font-weight: 500;
      color: #1D2129;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      flex: 1;
    }
  }
}

.template-empty {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 240px;
}

.error-hint-text {
  font-size: 14px;
  color: #4E5969;
  font-weight: 500;
}

/* ===== 分頁 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #F2F3F5;
  margin-top: auto;
  flex-shrink: 0;
}

/* ===== 右側面板 ===== */
.send-card {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #4E5969;
  }

  .send-type-group {
    width: 100%;

    :deep(.el-radio-button) {
      flex: 1;

      .el-radio-button__inner {
        width: 100%;
      }
    }
  }
}

.send-btn {
  width: 100%;
  border-radius: 6px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-search-empty {
  padding: 16px;
  text-align: center;
  color: #86909C;
  font-size: 13px;
}

/* ===== 已選範本預覽 ===== */
.selected-template-preview {
  border: 1px solid #E5E6EB;
  border-radius: 10px;
  padding: 16px;
  background: #F7F8FA;
  width: 100%;

  .template-detail {
    margin-top: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 12px;
    border-top: 1px solid #E5E6EB;
  }

  .template-detail-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .zoom-btn {
    padding: 4px;

    &:hover {
      color: #409eff;
    }
  }
}

.empty-template-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 0;
  color: #86909C;
  font-size: 13px;
  width: 100%;
}

/* ===== 發送記錄 ===== */
.history-card {
  margin-top: 16px;
  flex: 1;
}

.history-toggle-btn {
  padding: 4px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-detail {
  font-size: 12px;
  color: #86909C;
  margin-top: 4px;
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 0;
  color: #86909C;
  font-size: 13px;
}

/* ===== 範本放大預覽 Dialog ===== */
.template-preview-dialog {
  :deep(.el-dialog__body) {
    padding: 24px;
  }

  .preview-dialog-content {
    display: flex;
    justify-content: center;
    max-height: 70vh;
    overflow-y: auto;
  }
}

/* ===== 權限提示 ===== */
.no-permission-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  color: #F53F3F;
  font-size: 12px;
  text-align: center;
  width: 100%;
}

/* ===== RWD ===== */
@media (max-width: 991px) {
  .send-card,
  .history-card {
    margin-top: 16px;
  }
}

@media (max-width: 767px) {
  .template-list {
    grid-template-columns: 1fr;
    max-height: 50vh;
    overflow-y: auto;
  }

  .filter-bar {
    flex-direction: column;

    .filter-type {
      width: 100%;
    }
  }

  /* 發送按鈕固定在底部 */
  .send-btn-wrapper {
    position: sticky;
    bottom: 0;
    z-index: 10;
    background: #FFFFFF;
    padding-top: 8px;
    margin-bottom: 0 !important;
  }

  /* 發送記錄預設收合 */
  .history-card {
    #history-content {
      /* 由 v-show 控制，手機版預設收合透過 onMounted 設定 */
    }
  }
}
</style>
