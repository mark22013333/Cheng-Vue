<template>
  <div class="app-container">
    <el-alert type="info" :closable="true" show-icon style="margin-bottom: 12px;">
      <template #title>
        Rich Menu 設定步驟說明
      </template>
      <div style="line-height: 1.9; font-size: 13px; color: #606266;">
        <ol style="padding-left: 18px; margin: 6px 0 0;">
          <li>先到「LINE 頻道設定」建立頻道，並確認 Access Token、Channel Secret 等資訊可用。</li>
          <li>在本頁點「新增選單」，選擇要套用的「頻道」。</li>
          <li>設定「版型類型」與「圖片尺寸」，再上傳或輸入「選單圖片」。</li>
          <li>在「區塊設定」點選畫布中的區塊，為每個區塊設定動作（開啟網址 / 傳送訊息 / 回傳資料 / 切換選單）。</li>
          <li>若要做「切換選單」：
            <div style="margin-top: 4px;">
              <div>1) 先為「目標選單」填寫 Alias ID（建議用表單中的 Alias ID 欄位）。</div>
              <div>2) 將當前區塊的動作類型選為「切換選單 (Switch)」，再選擇目標 Alias。</div>
              <div>3) 建議回傳資料固定使用可辨識字串，方便後續追蹤（例如 richmenu-changed）。</div>
            </div>
          </li>
          <li>按「確定」儲存為草稿，確認預覽無誤後再按列表中的「發布 / 重新發布」同步到 LINE 平台。</li>
          <li>發布後可用「設為預設」設定此頻道的預設選單；需要時再用「更新預覽圖」重新整理本地預覽。</li>
        </ol>
        <div style="margin-top: 8px; color: #909399;">
          提醒：未發布的選單在 LINE 不會生效；Alias 也必須在發布時建立/同步後，切換功能才會正常運作。
        </div>
      </div>
    </el-alert>

    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="85px">
      <el-form-item label="選單名稱" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="請輸入選單名稱"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="頻道" prop="configId">
        <el-select v-model="queryParams.configId" placeholder="請選擇頻道" clearable style="width: 200px">
          <el-option
            v-for="config in channelList"
            :key="config.configId"
            :label="config.channelName"
            :value="config.configId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="版型類型" prop="templateType">
        <el-select v-model="queryParams.templateType" placeholder="版型類型" clearable style="width: 150px">
          <el-option label="左右兩格" value="TWO_COLS"/>
          <el-option label="左右兩格（高版）" value="TWO_COLS_HIGH"/>
          <el-option label="上下三格" value="THREE_ROWS"/>
          <el-option label="左右三格" value="THREE_COLS"/>
          <el-option label="四格（2x2）" value="FOUR_GRID"/>
          <el-option label="六格（2x3）" value="SIX_GRID"/>
          <el-option label="六格（3x2）" value="SIX_GRID_ALT"/>
          <el-option label="別名切換網格" value="ALIAS_SWITCH_GRID"/>
          <el-option label="自訂版型" value="CUSTOM"/>
        </el-select>
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="選單狀態" clearable style="width: 120px">
          <el-option label="草稿" value="DRAFT"/>
          <el-option label="啟用" value="ACTIVE"/>
          <el-option label="停用" value="INACTIVE"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-if="hasPerm('line:richMenu:add')">新增選單
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain :icon="Edit" :disabled="single" @click="handleUpdate"
                   v-if="hasPerm('line:richMenu:edit')">修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete"
                   v-if="hasPerm('line:richMenu:remove')">刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Download" @click="handleExport" v-if="hasPerm('line:richMenu:export')">
          匯出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="richMenuList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center"/>
      <el-table-column label="預覽圖" align="center" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.localImagePath"
            :src="getImageUrl(scope.row.localImagePath)"
            :preview-src-list="[getImageUrl(scope.row.localImagePath)]"
            :hide-on-click-modal="true"
            :preview-teleported="true"
            fit="cover"
            style="width: 60px; height: 40px; cursor: pointer; border-radius: 4px;"
          >
            <template #error>
              <div class="image-slot"
                   style="background: #f5f7fa; display: flex; justify-content: center; align-items: center; width: 100%; height: 100%;">
                <el-icon>
                  <Picture/>
                </el-icon>
              </div>
            </template>
          </el-image>
          <span v-else style="color: #ccc; font-size: 12px">無圖片</span>
        </template>
      </el-table-column>
      <el-table-column label="選單名稱" align="center" prop="name" :show-overflow-tooltip="true" min-width="150">
        <template #default="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="success" size="small" style="margin-right: 5px">預設</el-tag>
          <el-tag v-if="scope.row.selected === 1" type="primary" size="small" style="margin-right: 5px">使用中</el-tag>
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column label="頻道名稱" align="center" prop="channelName" width="120"/>
      <el-table-column label="版型類型" align="center" width="140">
        <template #default="scope">
          <el-tag size="small" type="info">{{ getTemplateTypeName(scope.row.templateType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="圖片尺寸" align="center" prop="imageSize" width="120"/>
      <el-table-column label="狀態" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'DRAFT'" type="info" size="small">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === 'ACTIVE'" type="success" size="small">啟用</el-tag>
          <el-tag v-else type="warning" size="small">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="LINE Menu ID" align="center" prop="richMenuId" :show-overflow-tooltip="true" width="200">
        <template #default="scope">
          <div v-if="scope.row.richMenuId" style="display: flex; align-items: center; justify-content: center;">
            <span style="margin-right: 5px; font-size: 12px;">{{ scope.row.richMenuId.substring(0, 15) }}...</span>
            <el-button type="primary" link :icon="DocumentCopy" @click="handleCopyRichMenuId(scope.row.richMenuId)"
                       style="padding: 0;" title="複製"></el-button>
          </div>
          <span v-else style="color: #ccc">未發布</span>
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="70"/>
      <el-table-column label="建立時間" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width operation-column"
                       fixed="right">
        <template #default="scope">
          <el-button v-if="hasPerm('line:richMenu:edit')" link type="primary" :icon="Edit"
                     @click="handleUpdate(scope.row)">編輯
          </el-button>
          <el-button v-if="hasPerm('line:richMenu:publish')" link type="primary" :icon="Upload"
                     @click="handlePublish(scope.row)">{{ scope.row.richMenuId ? '重新發布' : '發布' }}
          </el-button>
          <el-dropdown @command="(command) => handleCommand(command, scope.row)" style="margin-left: 10px">
            <el-button link type="primary" :icon="DArrowRight">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="scope.row.richMenuId && scope.row.isDefault !== 1 && hasPerm('line:richMenu:setDefault')"
                  command="setDefault" :icon="Star">設為預設
                </el-dropdown-item>
                <el-dropdown-item v-if="scope.row.richMenuId && hasPerm('line:richMenu:edit')" command="refreshPreview"
                                  :icon="Refresh">更新預覽圖
                </el-dropdown-item>
                <el-dropdown-item v-if="scope.row.richMenuId && hasPerm('line:richMenu:publish')"
                                  command="deleteFromLine" :icon="Remove" divided>從 LINE 刪除
                </el-dropdown-item>
                <el-dropdown-item v-if="hasPerm('line:richMenu:remove')" command="delete" :icon="Delete" divided>刪除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize" @pagination="getList"/>

    <el-dialog :title="title" v-model="open" width="95%" append-to-body :close-on-click-modal="false" top="3vh"
               destroy-on-close @opened="onDialogOpened">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" :key="dialogKey">
        <el-row>
          <el-col :span="12">
            <el-form-item label="頻道" prop="configId">
              <el-select v-model="form.configId" placeholder="請選擇頻道" style="width: 100%">
                <el-option v-for="config in channelList" :key="config.configId" :label="config.channelName"
                           :value="config.configId"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="選單名稱" prop="name">
              <el-input v-model="form.name" placeholder="請輸入選單名稱" maxlength="255"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="選單說明" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="請輸入選單說明" maxlength="500"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="版型類型" prop="templateType">
              <el-select v-model="form.templateType" placeholder="請選擇版型" @change="handleTemplateChange"
                         style="width: 100%">
                <el-option label="左右兩格" value="TWO_COLS"/>
                <el-option label="左右兩格（高版）" value="TWO_COLS_HIGH"/>
                <el-option label="上下三格" value="THREE_ROWS"/>
                <el-option label="左右三格" value="THREE_COLS"/>
                <el-option label="四格（2x2）" value="FOUR_GRID"/>
                <el-option label="六格（2x3）" value="SIX_GRID"/>
                <el-option label="六格（3x2）" value="SIX_GRID_ALT"/>
                <el-option label="別名切換網格" value="ALIAS_SWITCH_GRID"/>
                <el-option label="自訂版型" value="CUSTOM"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="圖片尺寸" prop="imageSize">
              <el-select v-model="form.imageSize" placeholder="請選擇圖片尺寸" @change="handleImageSizeChange"
                         style="width: 100%">
                <el-option label="2500x1686（全版高）" value="2500x1686"/>
                <el-option label="2500x843（全版矮）" value="2500x843"/>
                <el-option label="1200x810（半版高）" value="1200x810"/>
                <el-option label="1200x405（半版矮）" value="1200x405"/>
                <el-option label="800x540（1/3 版高）" value="800x540"/>
                <el-option label="800x270（1/3 版矮）" value="800x270"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="聊天欄位文字" prop="chatBarText">
              <el-input v-model="form.chatBarText" placeholder="請輸入聊天欄位顯示文字（最多14字）" maxlength="50"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Alias ID" prop="suggestedAliasId">
              <el-input v-model="form.suggestedAliasId" placeholder="選填，用於選單切換" maxlength="32">
                <template #prepend>richmenu-alias-</template>
              </el-input>
              <span style="color: #999; font-size: 12px">ℹ️ 填寫後發布時會自動建立 Alias</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="選單圖片" prop="imageUrl">
              <el-tabs v-model="imageInputType" type="card" style="width: 100%;">
                <el-tab-pane label="上傳圖片（推薦）" name="upload">
                  <image-upload v-model="form.imageUrl" :limit="1"/>
                </el-tab-pane>

                <el-tab-pane label="輸入網址" name="url">
                  <el-input v-model="form.imageUrl" placeholder="請輸入圖片網址" clearable/>
                  <div v-if="form.imageUrl && imageInputType === 'url'" style="margin-top: 15px; width: 100%;">
                    <el-image
                      :key="form.imageUrl"
                      :src="form.imageUrl"
                      fit="contain"
                      style="width: 100%; height: 250px; border: 1px solid #dcdfe6; border-radius: 4px; background: #eef0f6;"
                      :preview-src-list="[form.imageUrl]"
                      :preview-teleported="true"
                    >
                      <template #error>
                        <div style="height: 100%; display: flex; align-items: center; justify-content: center; color: #909399; flex-direction: column;">
                          <el-icon size="30"><Picture /></el-icon>
                          <span style="margin-top: 8px;">圖片無法載入</span>
                        </div>
                      </template>
                    </el-image>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <div
              style="padding: 15px; background: #f9f9f9; border-radius: 4px; border: 1px solid #ebeef5; height: 100%;">
              <div
                style="margin-bottom: 10px; font-weight: bold; font-size: 14px; border-bottom: 1px solid #e4e7ed; padding-bottom: 8px;">
                <el-icon style="color: #409EFF; margin-right: 4px; vertical-align: middle;">
                  <InfoFilled/>
                </el-icon>
                圖片規格
              </div>

              <div v-if="imageInfo.originalSize" style="margin-bottom: 15px;">
                <div style="margin-bottom: 5px;">
                  <span style="color: #606266; font-size: 13px;">原始尺寸：</span>
                  <el-tag size="small" type="info">{{ imageInfo.originalSize }}</el-tag>
                </div>
                <div style="margin-bottom: 5px;">
                  <span style="color: #606266; font-size: 13px;">調整結果：</span>
                  <el-tag v-if="imageInfo.resized" size="small" type="warning">{{ imageInfo.finalSize }}</el-tag>
                  <el-tag v-else size="small" type="success">無需調整</el-tag>
                </div>
                <div style="color: #909399; font-size: 12px;">
                  <el-icon style="margin-right: 4px; vertical-align: middle;">
                    <Document/>
                  </el-icon>
                  檔案大小：{{ imageInfo.fileSizeKB }} KB
                </div>
              </div>

              <div style="line-height: 2; color: #606266; font-size: 13px;">
                <div style="margin-bottom: 5px; font-weight: bold;">支援尺寸：</div>
                <div style="display: flex; flex-direction: column;">
                  <span>• 2500x1686（全版高）</span><span>• 2500x843（全版矮）</span>
                  <span>• 1200x810（半版高）</span><span>• 1200x405（半版矮）</span>
                  <span>• 800x540（1/3 版高）</span><span>• 800x270（1/3 版矮）</span>
                </div>
              </div>
              <div
                style="margin-top: 15px; padding: 8px; background: #fef0f0; border-radius: 4px; color: #F56C6C; font-size: 12px; display: flex;">
                <el-icon style="margin-right: 4px; margin-top: 2px;">
                  <Warning/>
                </el-icon>
                <span>上傳任意尺寸圖片，系統會自動調整為所選尺寸</span>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="區塊設定" prop="areas">
              <rich-menu-editor
                :key="'editor-' + dialogKey"
                v-model="form.areas"
                :template-type="form.templateType"
                :image-size="form.imageSize"
                :image-url="form.imageUrl"
                @change="handleAreasChange"
                @manual-edit="handleManualEdit"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="備註" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="請輸入備註" maxlength="500"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">確 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <progress-dialog ref="progressDialogRef"/>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, onBeforeUnmount, nextTick, watch, getCurrentInstance} from 'vue'
import {ElMessage, ElMessageBox, ElLoading} from 'element-plus'
import {
  Search, Refresh, Plus, Edit, Delete, Download,
  DocumentCopy, Upload, DArrowRight, Star, Remove,
  Picture, InfoFilled, Warning, Document
} from '@element-plus/icons-vue'

import {
  listRichMenu,
  getRichMenu,
  addRichMenu,
  updateRichMenu,
  delRichMenu,
  publishRichMenu,
  setDefaultRichMenu,
  deleteRichMenuFromLine,
  refreshPreviewImage
} from '@/api/line/richMenu'
import {listConfig} from '@/api/line/config'
import RichMenuEditor from './components/RichMenuEditor.vue'
import ImageUpload from '@/components/ImageUpload'
import ProgressDialog from '@/components/ProgressDialog'
import {getImageUrl} from '@/utils/image'
import request, { download } from '@/utils/request'
import SseClient, {SSE_CHANNELS, SSE_EVENTS} from '@/utils/sse/SseClient'

const {proxy} = getCurrentInstance()

function hasPerm(permission) {
  if (proxy.$auth && proxy.$auth.hasPermi) {
    return proxy.$auth.hasPermi(permission)
  }
  return true
}

const loading = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const showSearch = ref(true)
const total = ref(0)
const richMenuList = ref([])
const channelList = ref([])
const title = ref('')
const open = ref(false)
const dialogKey = ref(0)
const imageInputType = ref('upload')
const currentUploadedFileName = ref(null)
const formRef = ref(null)
const queryFormRef = ref(null)
const progressDialogRef = ref(null)
const imageUploadRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: null,
  configId: null,
  templateType: null,
  status: null
})

const form = reactive({
  id: null,
  configId: null,
  name: null,
  description: null,
  templateType: null,
  chatBarText: null,
  imageUrl: null,
  imageSize: null,
  areas: [],
  areasJson: null,
  remark: null,
  suggestedAliasId: null
})

const imageInfo = reactive({
  originalSize: '',
  finalSize: '',
  resized: false,
  fileSizeKB: 0
})

const rules = {
  configId: [{required: true, message: '頻道不能為空', trigger: 'change'}],
  name: [{required: true, message: '選單名稱不能為空', trigger: 'blur'}],
  templateType: [{required: true, message: '版型類型不能為空', trigger: 'change'}],
  imageSize: [{required: true, message: '圖片尺寸不能為空', trigger: 'change'}],
  suggestedAliasId: [{pattern: /^[a-z0-9-]*$/, message: 'Alias ID 只能包含小寫字母、數字和連字號', trigger: 'blur'}]
}

const templateTypeOptions = [
  {label: '左右兩格', value: 'TWO_COLS'},
  {label: '左右兩格（高版）', value: 'TWO_COLS_HIGH'},
  {label: '上下三格', value: 'THREE_ROWS'},
  {label: '左右三格', value: 'THREE_COLS'},
  {label: '四格（2x2）', value: 'FOUR_GRID'},
  {label: '六格（2x3）', value: 'SIX_GRID'},
  {label: '六格（3x2）', value: 'SIX_GRID_ALT'},
  {label: '別名切換網格', value: 'ALIAS_SWITCH_GRID'},
  {label: '自訂版型', value: 'CUSTOM'}
]

let sseClient = null
let sseConnectTimer = null

onMounted(() => {
  getList()
  getChannelList()
})

onBeforeUnmount(() => {
  if (sseClient) {
    sseClient.close()
    sseClient = null
  }
  if (sseConnectTimer) {
    clearTimeout(sseConnectTimer)
    sseConnectTimer = null
  }
})

watch(() => form.imageUrl, (newVal) => {
  console.log('🖼️ form.imageUrl 更新:', newVal)
})

const parseTime = (time, pattern) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getUploadUrl = () => '/line/richMenu/uploadImage'

const handleImageUploadResponse = (response) => {
  console.log('📸 圖片上傳響應：', response)
  if (!response) return

  if (response.code === 200) {
    const data = response.data || response

    if (data.fileName) {
      currentUploadedFileName.value = data.fileName
    }

    let finalUrl = ''
    if (data.url) {
      finalUrl = data.url
    } else if (data.fileName) {
      if (data.fileName.startsWith('/')) {
        finalUrl = data.fileName
      } else {
        finalUrl = `/profile/upload/richmenu/${data.fileName}`
      }
    }

    if (finalUrl) {
      form.imageUrl = finalUrl
      ElMessage.success('圖片上傳成功')

      // 關鍵步驟：上傳成功後，清除 ImageUpload 元件內部的檔案列表
      // 這樣上傳按鈕會重新出現，且不會顯示破圖的縮圖
      // 我們依賴下方的 el-image 來顯示預覽
      nextTick(() => {
        if (imageUploadRef.value && imageUploadRef.value.clearFiles) {
          imageUploadRef.value.clearFiles()
        }
      })
    } else {
      console.error('無法解析圖片路徑:', data)
      ElMessage.warning('上傳成功但無法解析圖片路徑')
    }

    if (data.originalSize) {
      imageInfo.originalSize = data.originalSize
      imageInfo.finalSize = data.finalSize || data.originalSize
      imageInfo.resized = data.resized === true
      imageInfo.fileSizeKB = data.fileSizeKB || 0
    }
  } else {
    if (response.msg) ElMessage.error(response.msg)
  }
}

const handleRemoveImage = () => {
  form.imageUrl = ''
  currentUploadedFileName.value = null
  imageInfo.originalSize = ''
}

const handleBeforeUpload = async (file) => {
  if (currentUploadedFileName.value) {
    try {
      await request.delete('/line/richMenu/deleteImage?fileName=' + encodeURIComponent(currentUploadedFileName.value))
    } catch (error) {
    }
  }
  return true
}

const getList = () => {
  loading.value = true
  listRichMenu(queryParams).then(response => {
    richMenuList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

const getChannelList = () => {
  listConfig({status: 1, pageNum: 1, pageSize: 9999}).then(response => {
    if (response && response.rows) {
      channelList.value = response.rows.sort((a, b) => {
        if (a.isPrimary === b.isPrimary) return 0
        return a.isPrimary === 1 ? -1 : 1
      })
    } else {
      channelList.value = []
    }
  }).catch(error => {
    console.error(error)
    channelList.value = []
  })
}

const cancel = () => {
  open.value = false
  reset()
}

const reset = () => {
  Object.assign(form, {
    id: null,
    configId: null,
    name: null,
    description: null,
    templateType: null,
    chatBarText: null,
    imageUrl: null,
    imageSize: null,
    areas: [],
    areasJson: null,
    remark: null,
    suggestedAliasId: null
  })
  Object.assign(imageInfo, {
    originalSize: '',
    finalSize: '',
    resized: false,
    fileSizeKB: 0
  })
  currentUploadedFileName.value = null
  if (formRef.value) formRef.value.resetFields()
  if (imageUploadRef.value && imageUploadRef.value.clearFiles) {
    imageUploadRef.value.clearFiles()
  }
}

const handleAreasChange = (areas) => {
  form.areasJson = JSON.stringify(areas)
}

const handleManualEdit = () => {
  if (form.templateType !== 'CUSTOM') {
    form.templateType = 'CUSTOM'
    ElMessage.info('已自動切換為自訂版型')
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  if (queryFormRef.value) queryFormRef.value.resetFields()
  handleQuery()
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

const handleAdd = () => {
  reset()
  dialogKey.value++
  form.templateType = 'TWO_COLS'
  form.imageSize = '2500x1686'

  if (channelList.value.length > 0) {
    const primaryChannel = channelList.value.find(c => c.isPrimary === 1)
    if (primaryChannel) {
      form.configId = primaryChannel.configId
      ElMessage.success(`已自動選擇主頻道：${primaryChannel.channelName}`)
    } else {
      form.configId = channelList.value[0].configId
      ElMessage.info(`已自動選擇頻道：${channelList.value[0].channelName}`)
    }
  } else {
    ElMessage.warning('目前沒有可用的頻道，請先設定 LINE 頻道')
  }

  open.value = true
  title.value = '新增 Rich Menu'
}

const handleUpdate = (row) => {
  const id = row.id || ids.value[0]
  getRichMenu(id).then(response => {
    dialogKey.value++
    Object.assign(form, response.data)

    if (form.areasJson) {
      try {
        form.areas = JSON.parse(form.areasJson)
      } catch (e) {
        form.areas = []
      }
    } else {
      form.areas = []
    }

    if (!form.areas) form.areas = []

    nextTick(() => {
      open.value = true
      title.value = '修改 Rich Menu'
    })
  })
}

const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.areas && form.areas.length > 0) {
        const invalidAreas = []
        form.areas.forEach((area, index) => {
          const action = area.action
          if (!action) {
            invalidAreas.push(`區塊 ${index + 1}`)
            return
          }
          if (action.type === 'uri' && !action.uri) invalidAreas.push(`區塊 ${index + 1}（缺少 URI）`)
          else if (action.type === 'message' && !action.text) invalidAreas.push(`區塊 ${index + 1}（缺少訊息文字）`)
          else if (action.type === 'postback' && !action.data) invalidAreas.push(`區塊 ${index + 1}（缺少 Postback 資料）`)
          else if (action.type === 'richmenuswitch') {
            if (!action.richMenuAliasId) invalidAreas.push(`區塊 ${index + 1}（缺少 Alias ID）`)
            else if (!action.data) invalidAreas.push(`區塊 ${index + 1}（缺少 Postback 資料）`)
          } else if (action.type === 'datetimepicker' && !action.data) {
            invalidAreas.push(`區塊 ${index + 1}（日期時間選擇器缺少回傳資料）`)
          }
        })

        if (invalidAreas.length > 0) {
          ElMessage.warning({
            dangerouslyUseHTMLString: true,
            message: '以下區塊的 Action 設定不完整，請補充：<br>' + invalidAreas.join('<br>')
          })
          return
        }
        form.areasJson = JSON.stringify(form.areas)
      }

      const req = form.id != null ? updateRichMenu(form) : addRichMenu(form)
      req.then(() => {
        ElMessage.success(form.id != null ? '修改成功' : '新增成功')
        open.value = false
        getList()
      })
    }
  })
}

const handleDelete = (row) => {
  const deleteIds = row.id || ids.value
  ElMessageBox.confirm('是否確認刪除所選的 Rich Menu？').then(() => {
    return delRichMenu(deleteIds)
  }).then(() => {
    getList()
    ElMessage.success('刪除成功')
  }).catch(() => {
  })
}

const handlePublish = (row) => {
  ElMessageBox.confirm('確認要發布此 Rich Menu 到 LINE 平台嗎？').then(() => {
    const taskId = 'richmenu-publish-' + Date.now()
    progressDialogRef.value.show({
      title: '發布 Rich Menu',
      message: '正在準備發布...',
      showLogs: true
    })

    sseClient = new SseClient({
      channel: SSE_CHANNELS.RICHMENU_PUBLISH,
      taskId: taskId,
      timeout: 300000
    })

    sseClient.on(SSE_EVENTS.PROGRESS, (data) => {
      progressDialogRef.value.updateProgress(data.progress || 0, data.message)
    })

    sseClient.on(SSE_EVENTS.SUCCESS, (data) => {
      progressDialogRef.value.setSuccess(data.message || '發布成功！')
      if (sseClient) {
        sseClient.close();
        sseClient = null
      }
      if (sseConnectTimer) {
        clearTimeout(sseConnectTimer);
        sseConnectTimer = null
      }
      setTimeout(() => getList(), 1000)
    })

    sseClient.on(SSE_EVENTS.ERROR, (data) => {
      progressDialogRef.value.setError(data.message || '發布失敗', true, () => handlePublish(row))
      if (sseClient) {
        sseClient.close();
        sseClient = null
      }
      if (sseConnectTimer) {
        clearTimeout(sseConnectTimer);
        sseConnectTimer = null
      }
    })

    sseClient.on(SSE_EVENTS.CONNECTED, () => {
      if (sseConnectTimer) {
        clearTimeout(sseConnectTimer);
        sseConnectTimer = null
      }
      publishRichMenu(row.id, taskId).catch(error => {
        progressDialogRef.value.setError(error.msg || '呼叫發布 API 失敗', true, () => handlePublish(row))
        if (sseClient) {
          sseClient.close();
          sseClient = null
        }
      })
    })

    sseClient.connect()

    sseConnectTimer = setTimeout(() => {
      if (sseClient && !sseClient.isConnected()) {
        progressDialogRef.value.setError('SSE 連線逾時，請檢查網路或重試', true, () => handlePublish(row))
        if (sseClient) {
          sseClient.close();
          sseClient = null
        }
      }
    }, 10000)

  }).catch((error) => {
    if (error === 'cancel') return
    ElMessage.error(error.msg)
    if (sseClient) {
      sseClient.close();
      sseClient = null
    }
    if (sseConnectTimer) {
      clearTimeout(sseConnectTimer);
      sseConnectTimer = null
    }
  })
}

const handleCommand = (command, row) => {
  switch (command) {
    case 'setDefault':
      handleSetDefault(row);
      break
    case 'refreshPreview':
      handleRefreshPreview(row);
      break
    case 'deleteFromLine':
      handleDeleteFromLine(row);
      break
    case 'delete':
      handleDelete(row);
      break
  }
}

const handleSetDefault = (row) => {
  ElMessageBox.confirm('確認要將此 Rich Menu 設為預設選單嗎？').then(() => {
    return setDefaultRichMenu(row.id)
  }).then(() => {
    ElMessage.success('設定成功')
    getList()
  }).catch(error => {
    if (error?.msg) ElMessage.error(error.msg)
  })
}

const handleRefreshPreview = (row) => {
  ElMessageBox.confirm('確認要從 LINE 平台重新下載並更新預覽圖嗎？').then(() => {
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在下載圖片...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    return refreshPreviewImage(row.id).finally(() => loadingInstance.close())
  }).then(() => {
    ElMessage.success('預覽圖更新成功')
    getList()
  }).catch(error => {
    if (error?.msg) ElMessage.error(error.msg)
  })
}

const handleDeleteFromLine = (row) => {
  ElMessageBox.confirm('確認要從 LINE 平台刪除此 Rich Menu 嗎？').then(() => {
    return deleteRichMenuFromLine(row.id)
  }).then(() => {
    ElMessage.success('刪除成功')
    getList()
  }).catch(error => {
    if (error?.msg) ElMessage.error(error.msg)
  })
}

const handleExport = () => {
  download(
    '/line/richMenu/export',
    {
      ...queryParams
    },
    `richmenu_${new Date().getTime()}.xlsx`
  )
}

const handleTemplateChange = (value) => {
  const templateSizeMap = {
    'TWO_COLS': '2500x843',
    'TWO_COLS_HIGH': '2500x1686',
    'THREE_ROWS': '2500x1686',
    'THREE_COLS': '2500x843',
    'FOUR_GRID': '2500x1686',
    'SIX_GRID': '2500x1686',
    'SIX_GRID_ALT': '2500x1686',
    'ALIAS_SWITCH_GRID': '2500x1686',
    'CUSTOM': '2500x1686'
  }
  if (templateSizeMap[value]) {
    form.imageSize = templateSizeMap[value]
  }
  form.areas = []
  dialogKey.value++
}

const handleImageSizeChange = () => {
  dialogKey.value++
  ElMessage.info('圖片尺寸已變更，編輯器已重新整理')
}

const getTemplateTypeName = (value) => {
  const option = templateTypeOptions.find(item => item.value === value)
  return option ? option.label : value
}

const onDialogOpened = () => {
  nextTick(() => {
  })
}

const handleCopyRichMenuId = async (richMenuId) => {
  try {
    await navigator.clipboard.writeText(richMenuId)
    ElMessage.success('已複製 Rich Menu ID')
  } catch (err) {
    console.error('複製失敗:', err)
    ElMessage.error('複製失敗，請手動複製')
  }
}
</script>

<style scoped lang="scss">
.el-dropdown {
  vertical-align: top;
}

.el-dropdown + .el-dropdown {
  margin-left: 15px;
}

/* 使用 /deep/ 或 :deep() 來強制隱藏 ImageUpload 的列表 */
/* 注意：如果您的 style 標籤沒有 scoped，就不要加 scoped */
:deep(.custom-upload-hide .el-upload-list) {
  display: none !important;
}
</style>
