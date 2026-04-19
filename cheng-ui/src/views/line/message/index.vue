<template>
  <div class="app-container line-message-page">
    <el-tabs v-model="activeTab" class="message-tabs" @tab-change="handleTabChange">
      <!-- ==================== Tab 1: 發送訊息 ==================== -->
      <el-tab-pane label="發送訊息" name="send">
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
                    <el-radio-button value="TAG">標籤推播</el-radio-button>
                    <el-radio-button value="BROADCAST">廣播</el-radio-button>
                  </el-radio-group>
                </el-form-item>

                <!-- 發送對象：單人/多人 -->
                <el-form-item v-if="sendForm.sendType === 'PUSH' || sendForm.sendType === 'MULTICAST'" label="發送對象" prop="targets">
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

                <!-- 標籤推播設定 -->
                <template v-if="sendForm.sendType === 'TAG'">
                  <!-- 標籤多選器 -->
                  <el-form-item label="選擇標籤" prop="targetTagIds">
                    <el-select
                      v-model="sendForm.targetTagIds"
                      multiple
                      filterable
                      collapse-tags
                      collapse-tags-tooltip
                      placeholder="選擇推播標籤..."
                      style="width: 100%"
                      @change="handleTagChange"
                    >
                      <el-option
                        v-for="tag in tagOptions"
                        :key="tag.tagId"
                        :label="tag.tagName"
                        :value="tag.tagId"
                      >
                        <div class="tag-option">
                          <span>{{ tag.tagName }}</span>
                          <el-tag size="small" type="info" round>{{ tag.userCount ?? 0 }} 人</el-tag>
                        </div>
                      </el-option>
                    </el-select>
                  </el-form-item>

                  <!-- 標籤群組選擇器（可選） -->
                  <el-form-item label="標籤群組（可選）">
                    <el-select
                      v-model="sendForm.targetTagGroupIds"
                      multiple
                      filterable
                      collapse-tags
                      collapse-tags-tooltip
                      placeholder="選擇標籤群組..."
                      style="width: 100%"
                      @change="handleTagChange"
                    >
                      <el-option
                        v-for="group in tagGroupOptions"
                        :key="group.groupId"
                        :label="group.groupName"
                        :value="group.groupId"
                      >
                        <div class="tag-option">
                          <span>{{ group.groupName }}</span>
                          <el-tag size="small" :type="group.logicType === 'AND' ? 'warning' : 'success'" round>
                            {{ group.logicType }}
                          </el-tag>
                        </div>
                      </el-option>
                    </el-select>
                  </el-form-item>

                  <!-- 預計推播人數 -->
                  <div class="tag-preview-count">
                    <el-icon v-if="tagPreviewLoading" class="is-loading"><Loading /></el-icon>
                    <template v-else-if="tagPreviewCount !== null">
                      <el-icon color="#409EFF"><User /></el-icon>
                      <span>預計推播 <strong>{{ tagPreviewCount }}</strong> 人（去重後）</span>
                    </template>
                    <template v-else>
                      <el-icon color="#86909C"><User /></el-icon>
                      <span class="hint-text">請選擇標籤以預覽推播人數</span>
                    </template>
                  </div>
                </template>

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
                  <div class="send-btn-group">
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
                      {{ sending ? '發送中...' : '立即發送' }}
                    </el-button>
                    <el-button
                      v-hasPermi="[LINE_MESSAGE_SEND]"
                      type="info"
                      :icon="Timer"
                      :disabled="!selectedTemplate || !canSend"
                      @click="openScheduleDialog"
                      size="large"
                      class="schedule-btn"
                    >
                      排程發送
                    </el-button>
                  </div>
                  <div v-if="!hasPermission(LINE_MESSAGE_SEND)" class="no-permission-tip">
                    您沒有發送訊息的權限，請聯繫管理員
                  </div>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ==================== Tab 2: 排程列表 ==================== -->
      <el-tab-pane label="排程列表" name="schedule" lazy>
        <el-card shadow="never" class="schedule-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">LINE 推播排程</span>
              <el-button type="primary" :icon="Refresh" link @click="loadScheduleList">重新載入</el-button>
            </div>
          </template>

          <el-table v-loading="scheduleLoading" :data="scheduleList" stripe class="schedule-table">
            <el-table-column label="排程名稱" prop="jobName" min-width="160" show-overflow-tooltip />
            <el-table-column label="Cron 表達式" prop="cronExpression" min-width="160" show-overflow-tooltip />
            <el-table-column label="狀態" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small" round>
                  {{ row.status === '0' ? '正常' : '暫停' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下次執行" prop="nextValidTime" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.nextValidTime || '--' }}
              </template>
            </el-table-column>
            <el-table-column label="建立時間" prop="createTime" min-width="170" show-overflow-tooltip />
            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === '0'"
                  type="warning"
                  link
                  @click="handleScheduleStatusChange(row, '1')"
                >
                  暫停
                </el-button>
                <el-button
                  v-else
                  type="success"
                  link
                  @click="handleScheduleStatusChange(row, '0')"
                >
                  恢復
                </el-button>
                <el-button type="primary" link @click="handleRunSchedule(row)">執行一次</el-button>
                <el-button type="danger" link @click="handleDeleteSchedule(row)">刪除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="scheduleList.length === 0 && !scheduleLoading" class="schedule-empty">
            <el-empty description="尚無排程推播">
              <el-button type="primary" @click="activeTab = 'send'">前往建立</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ==================== Tab 3: 發送記錄 ==================== -->
      <el-tab-pane label="發送記錄" name="history" lazy>
        <el-card shadow="never" class="history-table-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">發送記錄</span>
              <el-button type="primary" :icon="Refresh" link @click="loadHistoryTable">重新載入</el-button>
            </div>
          </template>

          <!-- 篩選列 -->
          <div class="history-filter-bar">
            <el-date-picker
              v-model="historyFilter.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="開始日期"
              end-placeholder="結束日期"
              value-format="YYYY-MM-DD"
              class="history-date-picker"
            />
            <el-select v-model="historyFilter.targetType" placeholder="發送方式" clearable class="history-type-filter">
              <el-option label="單人推播" value="SINGLE" />
              <el-option label="多人推播" value="MULTIPLE" />
              <el-option label="標籤推播" value="TAG" />
              <el-option label="廣播" value="ALL" />
            </el-select>
            <el-select v-model="historyFilter.sendStatus" placeholder="狀態" clearable class="history-status-filter">
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失敗" value="FAILED" />
              <el-option label="發送中" value="SENDING" />
            </el-select>
            <el-button type="primary" @click="loadHistoryTable">查詢</el-button>
          </div>

          <el-table v-loading="historyTableLoading" :data="historyTableList" stripe>
            <el-table-column label="時間" prop="createTime" min-width="170" show-overflow-tooltip />
            <el-table-column label="範本名稱" prop="messageContent" min-width="140" show-overflow-tooltip />
            <el-table-column label="發送方式" width="110" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getTargetTypeTag(row.targetType)" round>
                  {{ getTargetTypeLabel(row.targetType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="對象人數" prop="targetCount" width="100" align="center">
              <template #default="{ row }">
                {{ row.targetCount || '--' }}
              </template>
            </el-table-column>
            <el-table-column label="成功/失敗" width="110" align="center">
              <template #default="{ row }">
                <span class="success-count">{{ row.successCount ?? '--' }}</span>
                /
                <span class="fail-count">{{ row.failCount ?? '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="狀態" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.sendStatus === 'SUCCESS' ? 'success' : row.sendStatus === 'FAILED' ? 'danger' : 'warning'"
                  size="small"
                  round
                >
                  {{ row.sendStatus === 'SUCCESS' ? '成功' : row.sendStatus === 'FAILED' ? '失敗' : '發送中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.targetType === 'TAG'"
                  type="primary"
                  link
                  @click="openDetailDialog(row)"
                >
                  查看明細
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分頁 -->
          <div class="history-pagination" v-if="historyTotal > 0">
            <el-pagination
              layout="total, prev, pager, next"
              :total="historyTotal"
              :page-size="historyPageSize"
              v-model:current-page="historyCurrentPage"
              @current-change="loadHistoryTable"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- ==================== 範本放大預覽 Dialog ==================== -->
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

    <!-- ==================== 排程發送 Dialog ==================== -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="排程發送"
      width="520px"
      append-to-body
      destroy-on-close
      class="schedule-dialog"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleRules"
        label-position="top"
      >
        <el-form-item label="排程名稱" prop="jobName">
          <el-input v-model="scheduleForm.jobName" placeholder="輸入排程名稱" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="排程模式" prop="scheduleMode">
          <el-radio-group v-model="scheduleForm.scheduleMode">
            <el-radio-button value="once">一次性</el-radio-button>
            <el-radio-button value="recurring">重複</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 一次性：日期時間選擇器 -->
        <el-form-item v-if="scheduleForm.scheduleMode === 'once'" label="排程時間" prop="scheduleDatetime">
          <el-date-picker
            v-model="scheduleForm.scheduleDatetime"
            type="datetime"
            placeholder="選擇排程時間"
            :disabled-date="disablePastDate"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <!-- 重複：Cron 輸入框 -->
        <el-form-item v-if="scheduleForm.scheduleMode === 'recurring'" label="Cron 表達式" prop="cronExpression">
          <el-input v-model="scheduleForm.cronExpression" placeholder="例如：0 0 9 ? * MON（每週一 9:00）">
            <template #append>
              <el-tooltip content="Cron 表達式格式：秒 分 時 日 月 週 [年]">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <!-- 排程摘要 -->
        <div class="schedule-summary">
          <el-icon color="#409EFF"><InfoFilled /></el-icon>
          <span v-if="selectedTemplate">範本：{{ selectedTemplate.templateName }}</span>
          <span v-if="sendForm.sendType === 'TAG' && tagPreviewCount"> | 預計 {{ tagPreviewCount }} 人</span>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="scheduleSubmitting" @click="handleScheduleSubmit">建立排程</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 推播進度 Dialog ==================== -->
    <el-dialog
      v-model="progressDialogVisible"
      title="推播進度"
      width="480px"
      :close-on-click-modal="false"
      append-to-body
      class="progress-dialog"
      @close="handleProgressDialogClose"
    >
      <div v-if="progressData" class="progress-content">
        <el-progress
          :percentage="progressPercentage"
          :status="progressData.status === 'DONE' ? 'success' : undefined"
          :stroke-width="20"
          text-inside
        />

        <div class="progress-stats">
          <div class="stat-item">
            <span class="stat-label">已發送</span>
            <span class="stat-value">{{ progressData.sent }} / {{ progressData.total }}</span>
          </div>
          <div class="stat-item success">
            <span class="stat-label">成功</span>
            <span class="stat-value">{{ progressData.success }}</span>
          </div>
          <div class="stat-item fail">
            <span class="stat-label">失敗</span>
            <span class="stat-value">{{ progressData.failed }}</span>
          </div>
          <div class="stat-item" v-if="progressData.estimatedRemainingSeconds">
            <span class="stat-label">預估剩餘</span>
            <span class="stat-value">{{ formatRemainingTime(progressData.estimatedRemainingSeconds) }}</span>
          </div>
        </div>

        <div v-if="progressData.status === 'DONE'" class="progress-done">
          <el-result icon="success" title="推播完成">
            <template #extra>
              <el-button type="primary" @click="goToHistoryTab">查看發送記錄</el-button>
            </template>
          </el-result>
        </div>

        <div v-else class="progress-hint">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>推播進行中，關閉此視窗不會中斷發送</span>
        </div>
      </div>
    </el-dialog>

    <!-- ==================== 推播明細 Dialog ==================== -->
    <el-dialog
      v-model="detailDialogVisible"
      title="推播明細"
      width="640px"
      append-to-body
      destroy-on-close
      class="detail-dialog"
    >
      <el-table v-loading="detailLoading" :data="detailList" stripe max-height="400px">
        <el-table-column label="使用者" prop="lineDisplayName" min-width="140" show-overflow-tooltip />
        <el-table-column label="狀態" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'SUCCESS' ? 'success' : row.status === 'BLOCKED' ? 'warning' : 'danger'"
              size="small"
              round
            >
              {{ row.status === 'SUCCESS' ? '成功' : row.status === 'BLOCKED' ? '已封鎖' : '失敗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="重試次數" prop="retryCount" width="90" align="center" />
        <el-table-column label="錯誤訊息" prop="errorMessage" min-width="180" show-overflow-tooltip />
        <el-table-column label="發送時間" prop="sendTime" min-width="170" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="LineMessage">
import {
  LINE_MESSAGE_SEND
} from '@/constants/permissions'
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Promotion, Search, Check, Document, Clock, ZoomIn,
  WarningFilled, ArrowUp, ArrowDown, Timer, Refresh,
  Loading, User, QuestionFilled, InfoFilled
} from '@element-plus/icons-vue'
import MessagePreview from '../template/components/MessagePreview.vue'
import { listTemplate } from '@/api/line/template'
import { listConfig } from '@/api/line/config'
import { listUser } from '@/api/line/user'
import { sendMessage, listMessageLog, addMessageLog, previewTagTargets, getSendProgress, listPushDetail } from '@/api/line/message'
import { listLineTags } from '@/api/tag/line'
import { listLineTagGroups } from '@/api/tag/group'
import { listJob, addJob, changeJobStatus, delJob, runJob } from '@/api/monitor/job'
import { checkPermi } from '@/utils/permission'

/** 廣播二次確認關鍵字 */
const BROADCAST_CONFIRM_KEYWORD = '確認廣播'

/** LINE 推播排程的 jobGroup */
const LINE_PUSH_JOB_GROUP = 'LINE_PUSH'

/** 進度 polling 間隔 (毫秒) */
const PROGRESS_POLL_INTERVAL = 3000

/** 前端 sendType 對應後端 targetType 的映射 */
const SEND_TYPE_TO_TARGET_TYPE = {
  'PUSH': 'SINGLE',
  'MULTICAST': 'MULTIPLE',
  'TAG': 'TAG',
  'BROADCAST': 'ALL'
}

/** 後端 targetType 對應前端顯示的映射 */
const TARGET_TYPE_LABELS = {
  'SINGLE': '單人推播',
  'MULTIPLE': '多人推播',
  'TAG': '標籤推播',
  'ALL': '廣播'
}

const TARGET_TYPE_TAGS = {
  'SINGLE': 'primary',
  'MULTIPLE': 'success',
  'TAG': 'warning',
  'ALL': 'danger'
}

const getTargetTypeLabel = (type) => TARGET_TYPE_LABELS[type] || type
const getTargetTypeTag = (type) => TARGET_TYPE_TAGS[type] || 'info'

// 檢查權限
const hasPermission = (permission) => {
  return checkPermi([permission])
}

const route = useRoute()
const router = useRouter()

// ==================== Tab 控制 ====================
const activeTab = ref('send')

const handleTabChange = (tab) => {
  if (tab === 'schedule') {
    loadScheduleList()
  } else if (tab === 'history') {
    loadHistoryTable()
  }
}

// ==================== 範本相關 ====================
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
const previewDialogVisible = ref(false)

// ==================== 標籤相關 ====================
const tagOptions = ref([])
const tagGroupOptions = ref([])
const tagPreviewCount = ref(null)
const tagPreviewLoading = ref(false)

// ==================== 發送表單 ====================
const sendFormRef = ref(null)
const sendForm = reactive({
  configId: null,
  sendType: 'PUSH',
  targets: null,
  targetTagIds: [],
  targetTagGroupIds: []
})

const sendRules = {
  configId: [{ required: true, message: '請選擇頻道', trigger: 'change' }],
  targets: [{ required: true, message: '請選擇發送對象', trigger: 'change' }],
  targetTagIds: [{ required: true, message: '請至少選擇一個標籤', trigger: 'change', type: 'array', min: 1 }]
}

const sendStepText = computed(() => {
  if (!sendForm.configId) return '請選擇頻道'
  if (!selectedTemplate.value) return '請選擇範本'
  if (sendForm.sendType === 'PUSH' && !sendForm.targets) return '請選擇對象'
  if (sendForm.sendType === 'MULTICAST' && (!Array.isArray(sendForm.targets) || sendForm.targets.length === 0)) return '請選擇對象'
  if (sendForm.sendType === 'TAG' && sendForm.targetTagIds.length === 0 && sendForm.targetTagGroupIds.length === 0) return '請選擇標籤'
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
  if (sendForm.sendType === 'TAG') return sendForm.targetTagIds.length > 0 || sendForm.targetTagGroupIds.length > 0
  return false
})

// ==================== 排程相關 ====================
const scheduleDialogVisible = ref(false)
const scheduleFormRef = ref(null)
const scheduleSubmitting = ref(false)
const scheduleForm = reactive({
  jobName: '',
  scheduleMode: 'once',
  scheduleDatetime: null,
  cronExpression: ''
})

const scheduleRules = {
  jobName: [{ required: true, message: '請輸入排程名稱', trigger: 'blur' }],
  scheduleDatetime: [{ required: true, message: '請選擇排程時間', trigger: 'change' }],
  cronExpression: [{ required: true, message: '請輸入 Cron 表達式', trigger: 'blur' }]
}

const scheduleLoading = ref(false)
const scheduleList = ref([])

// ==================== 進度追蹤 ====================
const progressDialogVisible = ref(false)
const progressData = ref(null)
let progressTimer = null
let currentTaskId = null

const progressPercentage = computed(() => {
  if (!progressData.value || !progressData.value.total) return 0
  return Math.round((progressData.value.sent / progressData.value.total) * 100)
})

// ==================== 發送記錄（Tab 3） ====================
const historyTableLoading = ref(false)
const historyTableList = ref([])
const historyTotal = ref(0)
const historyCurrentPage = ref(1)
const historyPageSize = ref(10)
const historyFilter = reactive({
  dateRange: null,
  targetType: '',
  sendStatus: ''
})

// ==================== 推播明細 Dialog ====================
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailList = ref([])

// ==================== 載入方法 ====================
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

const loadTagOptions = async () => {
  try {
    const [tagRes, groupRes] = await Promise.all([
      listLineTags({ status: 1, pageNum: 1, pageSize: 200 }),
      listLineTagGroups({ pageNum: 1, pageSize: 100 })
    ])
    tagOptions.value = tagRes.rows || []
    tagGroupOptions.value = groupRes.rows || []
  } catch (e) {
    console.error('載入標籤失敗:', e)
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
  sendForm.targetTagIds = []
  sendForm.targetTagGroupIds = []
  tagPreviewCount.value = null
}

// ==================== 標籤預覽人數 ====================
let tagPreviewTimer = null
const handleTagChange = () => {
  clearTimeout(tagPreviewTimer)
  tagPreviewTimer = setTimeout(() => {
    loadTagPreview()
  }, 500)
}

const loadTagPreview = async () => {
  const hasTagIds = sendForm.targetTagIds.length > 0
  const hasGroupIds = sendForm.targetTagGroupIds.length > 0
  if (!hasTagIds && !hasGroupIds) {
    tagPreviewCount.value = null
    return
  }
  tagPreviewLoading.value = true
  try {
    const params = {}
    if (hasTagIds) params.tagIds = sendForm.targetTagIds.join(',')
    if (hasGroupIds) params.tagGroupIds = sendForm.targetTagGroupIds.join(',')
    const res = await previewTagTargets(params)
    tagPreviewCount.value = res.data ?? res.count ?? 0
  } catch (e) {
    console.error('預覽標籤人數失敗:', e)
    tagPreviewCount.value = null
  } finally {
    tagPreviewLoading.value = false
  }
}

// ==================== 發送處理 ====================
const handleSend = async () => {
  if (!selectedTemplate.value) {
    ElMessage.warning('請選擇訊息範本')
    return
  }

  await sendFormRef.value?.validate()

  // 廣播需要二次確認
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
    } else if (sendForm.sendType === 'TAG') {
      data.targetTagIds = sendForm.targetTagIds
      data.targetTagGroupIds = sendForm.targetTagGroupIds
    }

    const res = await sendMessage(data)

    // 標籤推播回傳 taskId，打開進度 Dialog
    if (sendForm.sendType === 'TAG' && res.taskId) {
      ElMessage.success('推播任務已建立，背景發送中')
      startProgressPolling(res.taskId)
    } else {
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
    }
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
  } finally {
    sending.value = false
  }
}

// ==================== 進度 Polling ====================
const startProgressPolling = (taskId) => {
  currentTaskId = taskId
  progressData.value = { taskId, status: 'SENDING', total: 0, sent: 0, success: 0, failed: 0 }
  progressDialogVisible.value = true
  pollProgress()
}

const pollProgress = async () => {
  if (!currentTaskId) return
  try {
    const res = await getSendProgress(currentTaskId)
    progressData.value = res.data ?? res
    if (progressData.value.status === 'DONE') {
      stopProgressPolling()
      return
    }
  } catch (e) {
    console.error('查詢進度失敗:', e)
  }
  progressTimer = setTimeout(pollProgress, PROGRESS_POLL_INTERVAL)
}

const stopProgressPolling = () => {
  if (progressTimer) {
    clearTimeout(progressTimer)
    progressTimer = null
  }
}

const handleProgressDialogClose = () => {
  // 關閉 Dialog 不中斷發送，只停止 polling
  stopProgressPolling()
  currentTaskId = null
}

const formatRemainingTime = (seconds) => {
  if (seconds < 60) return `${seconds} 秒`
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins} 分 ${secs} 秒`
}

const goToHistoryTab = () => {
  progressDialogVisible.value = false
  activeTab.value = 'history'
}

// ==================== 排程處理 ====================
const openScheduleDialog = () => {
  scheduleForm.jobName = ''
  scheduleForm.scheduleMode = 'once'
  scheduleForm.scheduleDatetime = null
  scheduleForm.cronExpression = ''
  scheduleDialogVisible.value = true
}

const disablePastDate = (date) => {
  return date.getTime() < Date.now() - 86400000
}

/** 將日期時間轉為一次性 Cron 表達式 */
const datetimeToCron = (datetime) => {
  const d = new Date(datetime)
  const sec = d.getSeconds()
  const min = d.getMinutes()
  const hour = d.getHours()
  const day = d.getDate()
  const month = d.getMonth() + 1
  const year = d.getFullYear()
  return `${sec} ${min} ${hour} ${day} ${month} ? ${year}`
}

const handleScheduleSubmit = async () => {
  await scheduleFormRef.value?.validate()

  const cronExpression = scheduleForm.scheduleMode === 'once'
    ? datetimeToCron(scheduleForm.scheduleDatetime)
    : scheduleForm.cronExpression

  // 組合 invokeTarget 參數
  const taskParams = {
    configId: sendForm.configId,
    templateId: selectedTemplate.value.templateId,
    sendType: sendForm.sendType
  }

  if (sendForm.sendType === 'TAG') {
    taskParams.tagIds = sendForm.targetTagIds.join(',')
    if (sendForm.targetTagGroupIds.length > 0) {
      taskParams.tagGroupIds = sendForm.targetTagGroupIds.join(',')
    }
  } else if (sendForm.sendType === 'PUSH') {
    taskParams.targetUserId = sendForm.targets
  } else if (sendForm.sendType === 'MULTICAST') {
    taskParams.targetUserIds = sendForm.targets?.join(',')
  }

  const invokeTarget = `lineScheduledPushTask.execute('${JSON.stringify(taskParams)}')`

  scheduleSubmitting.value = true
  try {
    await addJob({
      jobName: scheduleForm.jobName,
      jobGroup: LINE_PUSH_JOB_GROUP,
      invokeTarget,
      cronExpression,
      misfirePolicy: '2', // 忽略過期
      concurrent: '1',    // 禁止併發
      status: '0'         // 正常
    })
    ElMessage.success('排程建立成功')
    scheduleDialogVisible.value = false
    // 切換到排程列表
    activeTab.value = 'schedule'
    await loadScheduleList()
  } catch (e) {
    ElMessage.error('建立排程失敗：' + (e.response?.data?.msg || '請稍後重試'))
  } finally {
    scheduleSubmitting.value = false
  }
}

// ==================== 排程列表 ====================
const loadScheduleList = async () => {
  scheduleLoading.value = true
  try {
    const res = await listJob({ jobGroup: LINE_PUSH_JOB_GROUP, pageNum: 1, pageSize: 100 })
    scheduleList.value = res.rows || []
  } catch (e) {
    console.error('載入排程列表失敗:', e)
    ElMessage.error('載入排程列表失敗')
  } finally {
    scheduleLoading.value = false
  }
}

const handleScheduleStatusChange = async (row, newStatus) => {
  const actionText = newStatus === '0' ? '恢復' : '暫停'
  try {
    await ElMessageBox.confirm(`確定要${actionText}排程「${row.jobName}」嗎？`, '確認', { type: 'warning' })
    await changeJobStatus(row.jobId, newStatus)
    ElMessage.success(`${actionText}成功`)
    await loadScheduleList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${actionText}失敗`)
    }
  }
}

const handleRunSchedule = async (row) => {
  try {
    await ElMessageBox.confirm(`確定要立即執行排程「${row.jobName}」嗎？`, '確認', { type: 'warning' })
    await runJob(row.jobId, row.jobGroup)
    ElMessage.success('已觸發執行')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('執行失敗')
    }
  }
}

const handleDeleteSchedule = async (row) => {
  try {
    await ElMessageBox.confirm(`確定要刪除排程「${row.jobName}」嗎？此操作不可恢復。`, '確認刪除', { type: 'warning' })
    await delJob(row.jobId)
    ElMessage.success('刪除成功')
    await loadScheduleList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('刪除失敗')
    }
  }
}

// ==================== 發送記錄（Tab 3） ====================
const loadHistoryTable = async () => {
  historyTableLoading.value = true
  try {
    const params = {
      pageNum: historyCurrentPage.value,
      pageSize: historyPageSize.value
    }
    if (historyFilter.targetType) params.targetType = historyFilter.targetType
    if (historyFilter.sendStatus) params.sendStatus = historyFilter.sendStatus
    if (historyFilter.dateRange && historyFilter.dateRange.length === 2) {
      params.beginTime = historyFilter.dateRange[0]
      params.endTime = historyFilter.dateRange[1]
    }
    const res = await listMessageLog(params)
    historyTableList.value = res.rows || []
    historyTotal.value = res.total || 0
  } catch (e) {
    console.error('載入發送記錄失敗:', e)
    ElMessage.error('載入發送記錄失敗')
  } finally {
    historyTableLoading.value = false
  }
}

// ==================== 推播明細 ====================
const openDetailDialog = async (row) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await listPushDetail({ messageId: row.messageId || row.logId, pageNum: 1, pageSize: 200 })
    detailList.value = res.rows || []
  } catch (e) {
    console.error('載入推播明細失敗:', e)
    ElMessage.error('載入推播明細失敗')
    detailList.value = []
  } finally {
    detailLoading.value = false
  }
}

// ==================== 導航 ====================
const goToTemplateManagement = () => {
  router.push('/cadm/line/template')
}

// ==================== 生命週期 ====================
onMounted(async () => {
  await Promise.all([loadTemplates(), loadChannels(), loadTagOptions()])

  if (route.query.templateId) {
    const targetId = parseInt(route.query.templateId)
    const found = templateList.value.find(t => t.templateId === targetId)
    if (found) {
      selectTemplate(found)
    }
  }
})

onUnmounted(() => {
  stopProgressPolling()
  clearTimeout(searchTimer)
  clearTimeout(tagPreviewTimer)
})
</script>

<style scoped lang="scss">
.line-message-page {
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

/* ===== Tabs ===== */
.message-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

/* ===== 範本卡片 ===== */
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
        font-size: 13px;
        padding: 8px 4px;
      }
    }
  }
}

/* ===== 標籤選項 ===== */
.tag-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.tag-preview-count {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #F7F8FA;
  border-radius: 6px;
  font-size: 13px;
  color: #4E5969;
  margin-bottom: 18px;

  strong {
    color: #409EFF;
    font-size: 16px;
  }

  .hint-text {
    color: #86909C;
  }
}

/* ===== 發送按鈕組 ===== */
.send-btn-group {
  display: flex;
  gap: 8px;
  width: 100%;

  .send-btn {
    flex: 1;
    border-radius: 6px;
    font-weight: 600;
  }

  .schedule-btn {
    flex-shrink: 0;
    border-radius: 6px;
    font-weight: 500;
  }
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

/* ===== 排程 Dialog ===== */
.schedule-dialog {
  .schedule-summary {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 14px;
    background: #F7F8FA;
    border-radius: 6px;
    font-size: 13px;
    color: #4E5969;
  }
}

/* ===== 進度 Dialog ===== */
.progress-dialog {
  .progress-content {
    padding: 8px 0;
  }

  .progress-stats {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-top: 20px;

    .stat-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 12px;
      background: #F7F8FA;
      border-radius: 6px;

      .stat-label {
        font-size: 12px;
        color: #86909C;
      }

      .stat-value {
        font-size: 18px;
        font-weight: 600;
        color: #1D2129;
        font-family: 'DM Sans', system-ui, sans-serif;
      }

      &.success .stat-value {
        color: #00B42A;
      }

      &.fail .stat-value {
        color: #F53F3F;
      }
    }
  }

  .progress-done {
    margin-top: 16px;
  }

  .progress-hint {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-top: 16px;
    padding: 12px;
    color: #86909C;
    font-size: 13px;
  }
}

/* ===== 排程列表 ===== */
.schedule-card {
  .schedule-empty {
    padding: 24px 0;
  }
}

/* ===== 發送記錄 Tab ===== */
.history-table-card {
  .history-filter-bar {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    flex-wrap: wrap;

    .history-date-picker {
      width: 260px;
    }

    .history-type-filter,
    .history-status-filter {
      width: 130px;
    }
  }

  .history-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 16px;
  }

  .success-count {
    color: #00B42A;
    font-weight: 600;
  }

  .fail-count {
    color: #F53F3F;
    font-weight: 600;
  }
}

/* ===== 推播明細 Dialog ===== */
.detail-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
  }
}

/* ===== RWD ===== */
@media (max-width: 991px) {
  .send-card {
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

  .send-type-group {
    :deep(.el-radio-button) {
      .el-radio-button__inner {
        font-size: 12px !important;
        padding: 8px 2px !important;
      }
    }
  }

  .send-btn-group {
    flex-direction: column;
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

  .history-filter-bar {
    flex-direction: column;

    .history-date-picker,
    .history-type-filter,
    .history-status-filter {
      width: 100% !important;
    }
  }

  .progress-stats {
    grid-template-columns: 1fr !important;
  }
}
</style>
