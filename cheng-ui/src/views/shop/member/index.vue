<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="暱稱" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="請輸入暱稱"
          clearable
          style="width: 150px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手機號碼" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="請輸入手機號碼"
          clearable
          style="width: 150px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 120px">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="停用" value="DISABLED" />
          <el-option label="凍結" value="FROZEN" />
        </el-select>
      </el-form-item>
      <el-form-item label="會員等級" prop="level">
        <el-select v-model="queryParams.level" placeholder="請選擇" clearable style="width: 120px">
          <el-option label="普通會員" value="NORMAL" />
          <el-option label="VIP" value="VIP" />
          <el-option label="SVIP" value="SVIP" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="[SHOP_MEMBER_ADD]">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="[SHOP_MEMBER_REMOVE]">
          刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="[SHOP_MEMBER_EXPORT]">
          匯出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="memberList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="avatar" label="頭像" width="80" align="center">
        <template #default="scope">
          <el-avatar v-if="scope.row.avatar" :src="scope.row.avatar" :size="40" />
          <el-avatar v-else :size="40" icon="User" />
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="暱稱" min-width="120" />
      <el-table-column prop="memberNo" label="會員編號" width="150" />
      <el-table-column prop="mobile" label="手機號碼" width="130" />
      <el-table-column prop="email" label="Email" width="180" show-overflow-tooltip />
      <el-table-column prop="level" label="會員等級" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getLevelType(scope.row.level)">{{ getLevelLabel(scope.row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="points" label="積分" width="80" align="center" />
      <el-table-column prop="status" label="狀態" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="ACTIVE"
            inactive-value="DISABLED"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="[SHOP_MEMBER_EDIT]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="註冊時間" width="180" align="center" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="[SHOP_MEMBER_EDIT]">
            編輯
          </el-button>
          <el-button link type="warning" icon="Coin" @click="handlePoints(scope.row)" v-hasPermi="[SHOP_MEMBER_EDIT]">
            積分
          </el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="[SHOP_MEMBER_REMOVE]">
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

    <!-- 新增/修改對話框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="memberFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="暱稱" prop="nickname">
              <el-input v-model="form.nickname" placeholder="請輸入暱稱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手機號碼" prop="mobile">
              <el-input v-model="form.mobile" placeholder="請輸入手機號碼" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Email" prop="email">
              <el-input v-model="form.email" placeholder="請輸入Email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密碼" prop="password" v-if="!form.memberId">
              <el-input v-model="form.password" type="password" placeholder="請輸入密碼" show-password />
            </el-form-item>
            <el-form-item label="性別" prop="gender" v-else>
              <el-radio-group v-model="form.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
                <el-radio label="UNKNOWN">保密</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="會員等級" prop="level">
              <el-select v-model="form.level" placeholder="請選擇" style="width: 100%">
                <el-option label="普通會員" value="NORMAL" />
                <el-option label="VIP" value="VIP" />
                <el-option label="SVIP" value="SVIP" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="狀態" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="ACTIVE">正常</el-radio>
                <el-radio label="DISABLED">停用</el-radio>
                <el-radio label="FROZEN">凍結</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="頭像" prop="avatar">
          <image-upload v-model="form.avatar" :limit="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 積分調整對話框 -->
    <el-dialog title="積分調整" v-model="pointsOpen" width="500px" append-to-body>
      <el-form ref="pointsFormRef" :model="pointsForm" :rules="pointsRules" label-width="100px">
        <el-form-item label="會員">
          <span>{{ pointsForm.nickname }} (當前積分: {{ pointsForm.currentPoints }})</span>
        </el-form-item>
        <el-form-item label="調整類型" prop="type">
          <el-radio-group v-model="pointsForm.type">
            <el-radio label="increase">增加</el-radio>
            <el-radio label="decrease">扣減</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="積分數量" prop="points">
          <el-input-number v-model="pointsForm.points" :min="1" :max="999999" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPoints">確 定</el-button>
        <el-button @click="pointsOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopMember">
import {
  SHOP_MEMBER_ADD,
  SHOP_MEMBER_EDIT,
  SHOP_MEMBER_EXPORT,
  SHOP_MEMBER_REMOVE
} from '@/constants/permissions'
import { ref, reactive, onMounted } from 'vue'
import { listMember, getMember, addMember, updateMember, delMember, updateMemberStatus, adjustPoints } from '@/api/shop/member'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const memberList = ref([])
const total = ref(0)
const open = ref(false)
const pointsOpen = ref(false)
const title = ref('')
const ids = ref([])
const multiple = ref(true)

const queryFormRef = ref(null)
const memberFormRef = ref(null)
const pointsFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  nickname: undefined,
  mobile: undefined,
  status: undefined,
  level: undefined
})

const form = ref({})
const pointsForm = ref({})

const rules = {
  nickname: [{ required: true, message: '暱稱不能為空', trigger: 'blur' }],
  mobile: [
    { required: true, message: '手機號碼不能為空', trigger: 'blur' },
    { pattern: /^09\d{8}$/, message: '請輸入正確的手機號碼', trigger: 'blur' }
  ],
  password: [{ required: true, message: '密碼不能為空', trigger: 'blur' }]
}

const pointsRules = {
  type: [{ required: true, message: '請選擇調整類型', trigger: 'change' }],
  points: [{ required: true, message: '請輸入積分數量', trigger: 'blur' }]
}

const levelMap = {
  NORMAL: { label: '普通會員', type: 'info' },
  VIP: { label: 'VIP', type: 'warning' },
  SVIP: { label: 'SVIP', type: 'danger' }
}

onMounted(() => {
  getList()
})

function getList() {
  loading.value = true
  listMember(queryParams).then(response => {
    memberList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getLevelType(level) {
  return levelMap[level]?.type || 'info'
}

function getLevelLabel(level) {
  return levelMap[level]?.label || level
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.memberId)
  multiple.value = !selection.length
}

function handleStatusChange(row) {
  const text = row.status === 'ACTIVE' ? '啟用' : '停用'
  proxy.$modal.confirm('確認要「' + text + '」會員「' + row.nickname + '」嗎？').then(() => {
    return updateMemberStatus(row.memberId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + '成功')
  }).catch(() => {
    row.status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增會員'
}

function handleUpdate(row) {
  reset()
  getMember(row.memberId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改會員'
  })
}

function handlePoints(row) {
  pointsForm.value = {
    memberId: row.memberId,
    nickname: row.nickname,
    currentPoints: row.points,
    type: 'increase',
    points: 0
  }
  pointsOpen.value = true
}

function submitPoints() {
  pointsFormRef.value?.validate(valid => {
    if (valid) {
      adjustPoints(pointsForm.value.memberId, pointsForm.value.points, pointsForm.value.type).then(() => {
        proxy.$modal.msgSuccess('積分調整成功')
        pointsOpen.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const memberIds = row.memberId || ids.value
  proxy.$modal.confirm('確認刪除選中的會員嗎？').then(() => {
    return delMember(memberIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function submitForm() {
  memberFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.memberId) {
        updateMember(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addMember(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    memberId: undefined,
    nickname: undefined,
    mobile: undefined,
    email: undefined,
    password: undefined,
    avatar: undefined,
    gender: 'UNKNOWN',
    level: 'NORMAL',
    status: 'ACTIVE'
  }
  memberFormRef.value?.resetFields()
}

function handleExport() {
  proxy.$modal.msgWarning('匯出功能開發中')
}
</script>
