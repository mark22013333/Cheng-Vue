<template>
  <div class="app-container">
    <!-- 搜尋區域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="群組名稱" prop="groupName">
        <el-input v-model="queryParams.groupName" placeholder="請輸入群組名稱" clearable style="width: 200px"
                  @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="群組代碼" prop="groupCode">
        <el-input v-model="queryParams.groupCode" placeholder="請輸入群組代碼" clearable style="width: 200px"
                  @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="運算模式" prop="calcMode">
        <el-select v-model="queryParams.calcMode" placeholder="請選擇" clearable style="width: 150px">
          <el-option label="從左到右" value="LEFT_TO_RIGHT" />
          <el-option label="OR 切段" value="OR_OF_AND" />
        </el-select>
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 100px">
          <el-option label="啟用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具列 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
                   v-hasPermi="[TAG_GROUP_INVENTORY_ADD]">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
                   v-hasPermi="[TAG_GROUP_INVENTORY_REMOVE]">刪除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="groupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="群組名稱" prop="groupName" min-width="150" />
      <el-table-column label="群組代碼" prop="groupCode" min-width="150" />
      <el-table-column label="運算模式" prop="calcMode" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.calcMode === 'OR_OF_AND' ? 'warning' : 'info'">
            {{ row.calcMode === 'OR_OF_AND' ? 'OR 切段' : '從左到右' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="結果數量" prop="countResult" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="success">{{ row.countResult || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最後運算" prop="lastCalcTime" width="170" align="center">
        <template #default="{ row }">
          {{ row.lastCalcTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="狀態" prop="status" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '啟用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="建立時間" prop="createTime" width="170" align="center" />
      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handlePreview(row)"
                     v-hasPermi="[TAG_GROUP_INVENTORY_QUERY]">預覽</el-button>
          <el-button link type="success" icon="Refresh" @click="handleCalc(row)"
                     v-hasPermi="[TAG_GROUP_INVENTORY_CALC]">運算</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)"
                     v-hasPermi="[TAG_GROUP_INVENTORY_EDIT]">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)"
                     v-hasPermi="[TAG_GROUP_INVENTORY_REMOVE]">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改對話框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="群組名稱" prop="groupName">
          <el-input v-model="form.groupName" placeholder="請輸入群組名稱" />
        </el-form-item>
        <el-form-item label="群組代碼" prop="groupCode">
          <el-input v-model="form.groupCode" placeholder="請輸入群組代碼（唯一）" />
        </el-form-item>
        <el-form-item label="運算模式" prop="calcMode">
          <el-radio-group v-model="form.calcMode">
            <el-radio label="LEFT_TO_RIGHT">從左到右（預設）</el-radio>
            <el-radio label="OR_OF_AND">OR 切段（DNF）</el-radio>
          </el-radio-group>
          <div class="calc-mode-hint">
            <el-text type="info" size="small">
              從左到右：A AND B OR C → ((A AND B) OR C)<br/>
              OR 切段：A AND B OR C AND D → (A AND B) OR (C AND D)
            </el-text>
          </div>
        </el-form-item>
        <el-form-item label="標籤規則" prop="details" required>
          <div class="tag-rules-container">
            <div v-for="(detail, index) in form.details" :key="index" class="tag-rule-item">
              <el-select v-if="index > 0" v-model="detail.operator" placeholder="運算子" style="width: 80px"
                         class="operator-select">
                <el-option label="AND" value="AND" />
                <el-option label="OR" value="OR" />
              </el-select>
              <span v-else class="operator-placeholder"></span>
              <el-select v-model="detail.tagId" placeholder="請選擇標籤" style="width: 200px" filterable>
                <el-option v-for="tag in tagOptions" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId">
                  <span :style="{ color: tag.tagColor }">● </span>{{ tag.tagName }}
                </el-option>
              </el-select>
              <el-button v-if="form.details.length > 1" type="danger" icon="Delete" circle
                         @click="removeTagRule(index)" />
            </div>
            <el-button v-if="form.details.length < 5" type="primary" plain icon="Plus"
                       @click="addTagRule">新增標籤（最多 5 個）</el-button>
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="請輸入群組描述" />
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">啟用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">確定</el-button>
      </template>
    </el-dialog>

    <!-- 預覽對話框 -->
    <el-dialog title="群組運算預覽" v-model="previewOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="群組名稱">{{ previewData.groupName }}</el-descriptions-item>
        <el-descriptions-item label="符合數量">{{ previewData.matchCount }} 個物品</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">符合的物品 ID（前 10 筆）</el-divider>
      <el-tag v-for="id in previewData.matchedIds" :key="id" class="preview-tag" type="info">{{ id }}</el-tag>
      <el-empty v-if="!previewData.matchedIds?.length" description="沒有符合條件的物品" />
    </el-dialog>

    <!-- 運算中對話框 -->
    <el-dialog title="群組運算" v-model="calcOpen" width="400px" append-to-body :close-on-click-modal="false">
      <div v-if="calcLoading" class="calc-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>正在執行群組運算，請稍候...</p>
        <p class="calc-hint">大量資料運算可能需要幾分鐘</p>
      </div>
      <div v-else class="calc-result">
        <el-result :icon="calcResult.success ? 'success' : 'error'"
                   :title="calcResult.success ? '運算完成' : '運算失敗'">
          <template #sub-title>
            <p v-if="calcResult.success">符合群組的物品數量：<strong>{{ calcResult.matchCount }}</strong></p>
            <p v-if="calcResult.success">執行時間：{{ calcResult.elapsedMs }} ms</p>
            <p v-if="calcResult.error">{{ calcResult.error }}</p>
          </template>
        </el-result>
      </div>
      <template #footer>
        <el-button @click="calcOpen = false" :disabled="calcLoading">關閉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  TAG_GROUP_INVENTORY_ADD,
  TAG_GROUP_INVENTORY_CALC,
  TAG_GROUP_INVENTORY_EDIT,
  TAG_GROUP_INVENTORY_QUERY,
  TAG_GROUP_INVENTORY_REMOVE
} from '@/constants/permissions'
defineOptions({ name: 'InvTagGroup' })

import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import {
  listInvTagGroups,
  getInvTagGroup,
  addInvTagGroup,
  updateInvTagGroup,
  delInvTagGroup,
  calcInvTagGroup,
  previewInvTagGroup
} from '@/api/tag/group'
import { getInvTagOptions } from '@/api/tag/inventory'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const submitLoading = ref(false)
const showSearch = ref(true)
const groupList = ref([])
const total = ref(0)
const title = ref('')
const open = ref(false)
const ids = ref([])
const multiple = ref(true)
const tagOptions = ref([])

const previewOpen = ref(false)
const previewData = ref({})
const calcOpen = ref(false)
const calcLoading = ref(false)
const calcResult = ref({})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  groupName: undefined,
  groupCode: undefined,
  calcMode: undefined,
  status: undefined
})

const form = ref({
  groupId: undefined,
  groupName: '',
  groupCode: '',
  calcMode: 'LEFT_TO_RIGHT',
  description: '',
  status: 1,
  details: [{ groupIndex: 1, tagId: undefined, operator: null }]
})

const rules = {
  groupName: [{ required: true, message: '群組名稱不能為空', trigger: 'blur' }],
  groupCode: [{ required: true, message: '群組代碼不能為空', trigger: 'blur' }],
  calcMode: [{ required: true, message: '請選擇運算模式', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listInvTagGroups(queryParams.value).then(response => {
    groupList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

function getTagOptions() {
  getInvTagOptions(1).then(response => {
    tagOptions.value = response.data || []
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.groupId)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    groupId: undefined,
    groupName: '',
    groupCode: '',
    calcMode: 'LEFT_TO_RIGHT',
    description: '',
    status: 1,
    details: [{ groupIndex: 1, tagId: undefined, operator: null }]
  }
  proxy.resetForm('formRef')
}

function handleAdd() {
  reset()
  title.value = '新增標籤群組'
  open.value = true
}

function handleUpdate(row) {
  reset()
  const groupId = row.groupId || ids.value[0]
  getInvTagGroup(groupId).then(response => {
    const data = response.data
    form.value = {
      groupId: data.groupId,
      groupName: data.groupName,
      groupCode: data.groupCode,
      calcMode: data.calcMode || 'LEFT_TO_RIGHT',
      description: data.description,
      status: data.status,
      details: data.details && data.details.length > 0
        ? data.details.map(d => ({
            groupIndex: d.groupIndex,
            tagId: d.tagId,
            operator: d.operator
          }))
        : [{ groupIndex: 1, tagId: undefined, operator: null }]
    }
    title.value = '修改標籤群組'
    open.value = true
  })
}

function addTagRule() {
  if (form.value.details.length < 5) {
    form.value.details.push({
      groupIndex: form.value.details.length + 1,
      tagId: undefined,
      operator: 'AND'
    })
  }
}

function removeTagRule(index) {
  form.value.details.splice(index, 1)
  form.value.details.forEach((d, i) => {
    d.groupIndex = i + 1
    if (i === 0) d.operator = null
  })
}

function submitForm() {
  proxy.$refs['formRef'].validate(valid => {
    if (!valid) return

    const hasEmptyTag = form.value.details.some(d => !d.tagId)
    if (hasEmptyTag) {
      proxy.$modal.msgError('請選擇所有標籤')
      return
    }

    submitLoading.value = true
    const action = form.value.groupId ? updateInvTagGroup : addInvTagGroup
    action(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.groupId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleDelete(row) {
  const groupIds = row.groupId ? [row.groupId] : ids.value
  proxy.$modal.confirm('確定要刪除選取的群組嗎？').then(() => {
    return delInvTagGroup(groupIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  })
}

function cancel() {
  open.value = false
  reset()
}

function handlePreview(row) {
  previewData.value = { groupName: row.groupName, matchedIds: [], matchCount: 0 }
  previewOpen.value = true
  previewInvTagGroup(row.groupId, 10).then(response => {
    previewData.value = {
      groupName: row.groupName,
      matchedIds: response.data.matchedIds || [],
      matchCount: response.data.matchCount || 0
    }
  })
}

function handleCalc(row) {
  calcResult.value = {}
  calcLoading.value = true
  calcOpen.value = true

  calcInvTagGroup(row.groupId).then(response => {
    calcResult.value = response.data || {}
    calcResult.value.success = true
    getList()
  }).catch(error => {
    calcResult.value = { success: false, error: error.message || '運算失敗' }
  }).finally(() => {
    calcLoading.value = false
  })
}

onMounted(() => {
  getList()
  getTagOptions()
})
</script>

<style scoped>
.tag-rules-container {
  width: 100%;
}
.tag-rule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.operator-select {
  flex-shrink: 0;
}
.operator-placeholder {
  width: 80px;
  flex-shrink: 0;
}
.calc-mode-hint {
  margin-top: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}
.preview-tag {
  margin: 4px;
}
.calc-loading {
  text-align: center;
  padding: 30px;
}
.calc-loading .el-icon {
  font-size: 48px;
  color: var(--el-color-primary);
}
.calc-hint {
  color: #909399;
  font-size: 12px;
}
.calc-result {
  padding: 20px;
}
</style>
