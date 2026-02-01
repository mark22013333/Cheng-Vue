<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="名稱" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="請輸入禮物名稱"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇" clearable style="width: 120px">
          <el-option label="啟用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['shop:gift:add']">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['shop:gift:remove']">
          刪除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="giftList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="imageUrl" label="圖片" width="120" align="center">
        <template #default="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="getImageUrl(scope.row.imageUrl)"
            style="width: 80px; height: 80px"
            fit="cover"
            :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="禮物名稱" min-width="150" show-overflow-tooltip />
      <el-table-column prop="thresholdAmount" label="消費門檻" width="120" align="center">
        <template #default="scope">
          ${{ scope.row.thresholdAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="stockQuantity" label="庫存" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.stockQuantity > 0 ? 'success' : 'danger'" size="small">
            {{ scope.row.stockQuantity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="狀態" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="ENABLED"
            inactive-value="DISABLED"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['shop:gift:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="建立時間" width="180" align="center" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['shop:gift:edit']">
            編輯
          </el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['shop:gift:remove']">
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
      <el-form ref="giftFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="禮物名稱" prop="name">
          <el-input v-model="form.name" placeholder="請輸入禮物名稱" />
        </el-form-item>
        <el-form-item label="禮物圖片" prop="imageUrl">
          <image-upload v-model="form.imageUrl" :limit="1" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="消費門檻" prop="thresholdAmount">
              <el-input-number v-model="form.thresholdAmount" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="庫存數量" prop="stockQuantity">
              <el-input-number v-model="form.stockQuantity" :min="0" :max="99999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="狀態" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="ENABLED">啟用</el-radio>
                <el-radio label="DISABLED">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="選填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopGift">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { listGift, getGift, addGift, updateGift, delGift } from '@/api/shop/gift'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const giftList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const ids = ref([])
const multiple = ref(true)

const queryFormRef = ref(null)
const giftFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  status: undefined
})

const form = ref({})

const rules = {
  name: [{ required: true, message: '禮物名稱不能為空', trigger: 'blur' }],
  thresholdAmount: [{ required: true, message: '消費門檻不能為空', trigger: 'blur' }],
  stockQuantity: [{ required: true, message: '庫存數量不能為空', trigger: 'blur' }]
}

onMounted(() => {
  getList()
})

function getList() {
  loading.value = true
  listGift(queryParams).then(response => {
    giftList.value = response.rows
    total.value = response.total
    loading.value = false
  })
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
  ids.value = selection.map(item => item.giftId)
  multiple.value = !selection.length
}

function handleStatusChange(row) {
  const text = row.status === 'ENABLED' ? '啟用' : '停用'
  proxy.$modal.confirm('確認要「' + text + '」禮物「' + row.name + '」嗎？').then(() => {
    return updateGift({ giftId: row.giftId, status: row.status })
  }).then(() => {
    proxy.$modal.msgSuccess(text + '成功')
  }).catch(() => {
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增禮物'
}

function handleUpdate(row) {
  reset()
  getGift(row.giftId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改禮物'
  })
}

function handleDelete(row) {
  const giftIds = row.giftId || ids.value
  proxy.$modal.confirm('確認刪除選中的禮物嗎？').then(() => {
    return delGift(giftIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function submitForm() {
  giftFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.giftId) {
        updateGift(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addGift(form.value).then(() => {
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
    giftId: undefined,
    name: undefined,
    imageUrl: undefined,
    thresholdAmount: 0,
    stockQuantity: 0,
    sortOrder: 0,
    status: 'ENABLED',
    remark: undefined
  }
  giftFormRef.value?.resetFields()
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}
</script>
