<template>
  <div class="app-container">
    <el-alert type="info" :closable="true" show-icon style="margin-bottom: 12px;">
      <template #title>
        物品貼標說明
      </template>
      <div style="line-height: 1.8; font-size: 13px; color: #606266;">
        此列表僅顯示透過「批次貼標」功能上傳的物品 ID 與標籤關聯記錄。
        <ul style="padding-left: 18px; margin: 6px 0 0;">
          <li>點擊「批次貼標」可上傳 Excel 檔案或手動輸入物品 ID，批次為多個物品貼上標籤。</li>
          <li>支援同時選擇多個標籤，所有選擇的標籤將套用至每個物品。</li>
          <li>若需移除標籤，請勾選記錄後點擊「移除標籤」按鈕。</li>
        </ul>
      </div>
    </el-alert>

    <!-- 當次上傳結果提示 -->
    <el-alert 
      v-if="lastUploadIds.length > 0" 
      type="success" 
      :closable="true" 
      show-icon 
      style="margin-bottom: 12px;"
      @close="clearLastUpload"
    >
      <template #title>
        本次上傳結果
      </template>
      <div style="line-height: 1.8; font-size: 13px;">
        成功為 <strong>{{ lastUploadIds.length }}</strong> 個物品貼上標籤。
        <el-button link type="primary" @click="showLastUploadOnly" style="margin-left: 8px;">僅顯示本次結果</el-button>
        <el-button link type="info" @click="showAllRecords" style="margin-left: 8px;">顯示全部紀錄</el-button>
      </div>
    </el-alert>

    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="顯示範圍" prop="viewMode">
        <el-select v-model="queryParams.viewMode" style="width: 160px" @change="handleQuery">
          <el-option label="全部紀錄" value="all" />
          <el-option label="本次上傳" value="current" :disabled="lastUploadIds.length === 0" />
        </el-select>
      </el-form-item>
      <el-form-item label="標籤" prop="tagId">
        <el-select v-model="queryParams.tagId" placeholder="請選擇標籤" clearable style="width: 200px">
          <el-option
            v-for="tag in tagOptions"
            :key="tag.tagId"
            :label="tag.tagName"
            :value="tag.tagId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="物品名稱" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="請輸入物品名稱"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleBatchBind"
          v-hasPermi="['tag:inventory:batchBind']"
        >批次貼標</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['tag:inventory:unbind']"
        >移除標籤</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="relationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="關聯ID" align="center" prop="id" width="80" />
      <el-table-column label="物品編碼" align="center" prop="itemCode" min-width="120" />
      <el-table-column label="物品名稱" align="center" prop="itemName" min-width="180" show-overflow-tooltip />
      <el-table-column label="標籤" align="center" prop="tagName" min-width="120">
        <template #default="scope">
          <bookmark-tag :label="scope.row.tagName" :color="scope.row.tagColor" />
        </template>
      </el-table-column>
      <el-table-column label="建立時間" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template #default="scope">
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['tag:inventory:unbind']">移除</el-button>
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

    <!-- 批次貼標對話框 -->
    <el-dialog title="批次貼標" v-model="batchBindVisible" width="700px" append-to-body :close-on-click-modal="false">
      <el-form :model="batchBindForm" label-width="120px">
        <el-form-item label="預設標籤" required>
          <tag-select
            v-model="batchBindForm.tagIds"
            :options="tagOptions"
            :multiple="true"
            :filterable="true"
            :show-tag-code="true"
            placeholder="選擇標籤（可多選）"
            width="100%"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            如果 Excel 中的「額外標籤」欄位為空，將使用此處選擇的標籤
          </div>
        </el-form-item>
        <el-form-item label="輸入方式" required>
          <el-radio-group v-model="batchBindForm.inputType">
            <el-radio label="excel">Excel 檔案上傳</el-radio>
            <el-radio label="text">手動輸入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="batchBindForm.inputType === 'excel'" label="上傳檔案" required>
          <el-upload
            ref="uploadRef"
            :limit="1"
            accept=".xlsx, .xls"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary" plain icon="Upload">選擇檔案</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支援多個 Sheet，每個 Sheet 可包含大量資料，
                <el-button link type="primary" @click="downloadTemplate">下載範本</el-button>
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="batchBindForm.inputType === 'text'" label="物品編碼" required>
          <el-input
            v-model="batchBindForm.itemCodes"
            type="textarea"
            :rows="8"
            placeholder="請輸入物品編碼，每行一個"
          />
        </el-form-item>
        <el-form-item>
          <el-alert type="info" :closable="false" show-icon>
            <template #title>
              <div v-if="batchBindForm.inputType === 'excel'">
                <p style="margin: 0;"><strong>Excel 範本格式：</strong></p>
                <p style="margin: 4px 0 0 0;">- 第一欄：物品編碼（必填）</p>
                <p style="margin: 2px 0 0 0;">- 第二欄：額外標籤（選填，如有填寫會自動建立標籤）</p>
                <p style="margin: 4px 0 0 0; color: #e6a23c;">- 系統會驗證物品編碼是否存在，不存在則跳過</p>
              </div>
              <div v-else>
                <p style="margin: 0;">每行輸入一個物品編碼，系統將自動去除重複項</p>
                <p style="margin: 4px 0 0 0;">所有選擇的標籤將被貼到每個物品</p>
              </div>
            </template>
          </el-alert>
        </el-form-item>
        <!-- 上傳進度 -->
        <el-form-item v-if="uploadProgress.visible" label="上傳進度">
          <el-progress :percentage="uploadProgress.percent" :status="uploadProgress.status" />
          <div style="font-size: 12px; color: #606266; margin-top: 4px;">
            {{ uploadProgress.message }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchBindVisible = false" :disabled="batchBindLoading">取消</el-button>
        <el-button type="primary" @click="submitBatchBind" :loading="batchBindLoading">開始上傳</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="InventoryBindItem">
import { ref, reactive, toRefs, onMounted, getCurrentInstance } from 'vue'
import { listInvItemTagRelations, delInvItemTagRelations, getInventoryTagOptions, batchBindInvItemTagWithValidation } from '@/api/tag/inventory'

const { proxy } = getCurrentInstance()

const relationList = ref([])
const tagOptions = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

// 當次上傳結果
const lastUploadIds = ref([])
const lastUploadTagCount = ref(0)

const queryFormRef = ref(null)

// 批次貼標相關
const batchBindVisible = ref(false)
const batchBindLoading = ref(false)
const uploadRef = ref(null)
const uploadFile = ref(null)
const batchBindForm = reactive({
  tagIds: [],
  itemCodes: '',
  inputType: 'excel'
})

// 上傳進度
const uploadProgress = reactive({
  visible: false,
  percent: 0,
  status: '',
  message: ''
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tagId: undefined,
    itemName: undefined,
    viewMode: 'current',
    itemIds: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  // 根據顯示範圍設定查詢參數
  const params = { ...queryParams.value }
  if (params.viewMode === 'current' && lastUploadIds.value.length > 0) {
    params.itemIds = lastUploadIds.value.join(',')
  } else {
    params.itemIds = undefined
  }
  delete params.viewMode
  
  listInvItemTagRelations(params).then(response => {
    relationList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getTagOptions() {
  getInventoryTagOptions(1).then(response => {
    tagOptions.value = response.data
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryFormRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

function handleDelete(row) {
  const relationIds = row.id || ids.value
  proxy.$modal.confirm('確認要移除選中的標籤關聯嗎？').then(() => {
    return delInvItemTagRelations(relationIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('移除成功')
  }).catch(() => {})
}

function clearLastUpload() {
  lastUploadIds.value = []
  queryParams.value.viewMode = 'all'
}

function showLastUploadOnly() {
  queryParams.value.viewMode = 'current'
  handleQuery()
}

function showAllRecords() {
  queryParams.value.viewMode = 'all'
  handleQuery()
}

function handleBatchBind() {
  batchBindForm.tagIds = []
  batchBindForm.itemCodes = ''
  batchBindForm.inputType = 'excel'
  uploadFile.value = null
  uploadProgress.visible = false
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  batchBindVisible.value = true
}

function handleFileChange(file) {
  uploadFile.value = file.raw
}

function handleFileRemove() {
  uploadFile.value = null
}

function downloadTemplate() {
  import('@/utils/excel').then(({ exportTemplate }) => {
    exportTemplate(
      ['物品編碼', '額外標籤'],
      [
        ['ITEM001', 'VIP專屬'],
        ['ITEM002', ''],
        ['ITEM003', '限量版']
      ],
      '物品貼標範本'
    )
  })
}

async function submitBatchBind() {
  if (!batchBindForm.tagIds || batchBindForm.tagIds.length === 0) {
    proxy.$modal.msgWarning('請選擇至少一個預設標籤')
    return
  }

  let records = []

  if (batchBindForm.inputType === 'excel') {
    if (!uploadFile.value) {
      proxy.$modal.msgWarning('請上傳 Excel 檔案')
      return
    }
    try {
      uploadProgress.visible = true
      uploadProgress.percent = 0
      uploadProgress.status = ''
      uploadProgress.message = '正在解析 Excel 檔案...'

      const { parseItemTagExcel } = await import('@/utils/excel')
      records = await parseItemTagExcel(uploadFile.value)

      if (!records || records.length === 0) {
        proxy.$modal.msgWarning('Excel 檔案中沒有有效資料')
        uploadProgress.visible = false
        return
      }

      uploadProgress.message = `解析完成，共 ${records.length} 筆資料，準備上傳...`
    } catch (error) {
      proxy.$modal.msgError('解析 Excel 檔案失敗：' + error.message)
      uploadProgress.visible = false
      return
    }
  } else {
    if (!batchBindForm.itemCodes.trim()) {
      proxy.$modal.msgWarning('請輸入物品編碼')
      return
    }
    const itemCodes = [...new Set(
      batchBindForm.itemCodes
        .split('\n')
        .map(code => code.trim())
        .filter(code => code.length > 0)
    )]
    records = itemCodes.map(code => ({ itemCode: code, extraTagName: null }))
  }

  if (records.length === 0) {
    proxy.$modal.msgWarning('請輸入有效的物品編碼')
    return
  }

  // 分批上傳
  batchBindLoading.value = true
  uploadProgress.visible = true

  const BATCH_SIZE = 5000
  const totalBatches = Math.ceil(records.length / BATCH_SIZE)
  let totalSuccess = 0
  let totalFailed = 0
  const failedRecords = []
  const newTagsCreated = []

  try {
    for (let i = 0; i < totalBatches; i++) {
      const batchRecords = records.slice(i * BATCH_SIZE, (i + 1) * BATCH_SIZE)
      const batchNo = i + 1

      uploadProgress.percent = Math.round((i / totalBatches) * 100)
      uploadProgress.message = `正在處理第 ${batchNo}/${totalBatches} 批（共 ${batchRecords.length} 筆）...`

      const response = await batchBindInvItemTagWithValidation({
        records: batchRecords,
        defaultTagIds: batchBindForm.tagIds,
        batchNo: batchNo,
        totalBatches: totalBatches
      })

      if (response) {
        totalSuccess += response.successCount || 0
        totalFailed += response.failedCount || 0
        if (response.failedRecords) {
          failedRecords.push(...response.failedRecords.slice(0, 10))
        }
        if (response.newTagsCreated) {
          newTagsCreated.push(...response.newTagsCreated)
        }
      }
    }

    uploadProgress.percent = 100
    uploadProgress.status = 'success'
    uploadProgress.message = `上傳完成！成功 ${totalSuccess} 筆，失敗 ${totalFailed} 筆`

    let resultMsg = `成功貼標 ${totalSuccess} 筆`
    if (totalFailed > 0) {
      resultMsg += `，失敗 ${totalFailed} 筆`
    }
    if (newTagsCreated.length > 0) {
      resultMsg += `，新建標籤：${newTagsCreated.join('、')}`
    }
    proxy.$modal.msgSuccess(resultMsg)

    lastUploadIds.value = records.map(r => r.itemCode)
    lastUploadTagCount.value = batchBindForm.tagIds.length

    setTimeout(() => {
      batchBindVisible.value = false
      uploadProgress.visible = false
      queryParams.value.viewMode = 'current'
      getList()
    }, 1500)

  } catch (error) {
    uploadProgress.status = 'exception'
    uploadProgress.message = `上傳失敗：${error.message || '未知錯誤'}`
    proxy.$modal.msgError('批次貼標失敗：' + (error.message || '未知錯誤'))
  } finally {
    batchBindLoading.value = false
  }
}

onMounted(() => {
  getList()
  getTagOptions()
})
</script>
