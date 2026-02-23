<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="頁面" prop="pageKey">
        <el-select v-model="queryParams.pageKey" placeholder="請選擇" clearable style="width: 150px">
          <el-option label="首頁" value="HOME" />
          <el-option label="關於我們" value="ABOUT" />
          <el-option label="聯絡資訊" value="CONTACT" />
        </el-select>
      </el-form-item>
      <el-form-item label="區塊識別" prop="blockKey">
        <el-input
          v-model="queryParams.blockKey"
          placeholder="請輸入區塊識別"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="類型" prop="blockType">
        <el-select v-model="queryParams.blockType" placeholder="請選擇" clearable style="width: 150px">
          <el-option label="文字" value="TEXT" />
          <el-option label="圖片" value="IMAGE" />
          <el-option label="HTML" value="HTML" />
          <el-option label="商品列表" value="PRODUCT_LIST" />
        </el-select>
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="[SHOP_BLOCK_ADD]">
          新增
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="blockList">
      <el-table-column prop="pageKey" label="頁面" width="120" align="center">
        <template #default="scope">
          <el-tag>{{ getPageLabel(scope.row.pageKey) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="blockKey" label="區塊識別" width="150" />
      <el-table-column prop="title" label="標題" min-width="150" />
      <el-table-column prop="blockType" label="類型" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getBlockTypeTag(scope.row.blockType)">{{ getBlockTypeLabel(scope.row.blockType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="狀態" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="ENABLED"
            inactive-value="DISABLED"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="[SHOP_BLOCK_EDIT]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新時間" width="180" align="center" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="[SHOP_BLOCK_EDIT]">
            編輯
          </el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="[SHOP_BLOCK_REMOVE]">
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
    <el-dialog :title="title" v-model="open" width="800px" append-to-body destroy-on-close>
      <el-form ref="blockFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="頁面" prop="pageKey">
              <el-select v-model="form.pageKey" placeholder="請選擇" style="width: 100%" :disabled="!!form.blockId">
                <el-option label="首頁" value="HOME" />
                <el-option label="關於我們" value="ABOUT" />
                <el-option label="聯絡資訊" value="CONTACT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="區塊識別" prop="blockKey">
              <el-input v-model="form.blockKey" placeholder="請輸入區塊識別（如 BRAND_INTRO）" :disabled="!!form.blockId" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="標題" prop="title">
              <el-input v-model="form.title" placeholder="請輸入區塊標題" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副標題" prop="subTitle">
              <el-input v-model="form.subTitle" placeholder="請輸入副標題（選填）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="區塊類型" prop="blockType">
              <el-select v-model="form.blockType" placeholder="請選擇" style="width: 100%">
                <el-option label="文字" value="TEXT" />
                <el-option label="圖片" value="IMAGE" />
                <el-option label="HTML" value="HTML" />
                <el-option label="商品列表" value="PRODUCT_LIST" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="狀態" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="ENABLED">啟用</el-radio>
                <el-radio label="DISABLED">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 根據類型顯示不同的內容編輯器 -->
        <el-form-item label="內容" prop="content">
          <!-- 文字類型 -->
          <el-input
            v-if="form.blockType === 'TEXT'"
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="請輸入文字內容"
          />
          <!-- 圖片類型 -->
          <div v-else-if="form.blockType === 'IMAGE'">
            <image-upload v-model="form.imageUrl" :limit="1" />
          </div>
          <!-- HTML 類型 -->
          <editor
            v-else-if="form.blockType === 'HTML'"
            v-model="form.content"
            :min-height="300"
          />
          <!-- 商品列表類型 -->
          <el-input
            v-else-if="form.blockType === 'PRODUCT_LIST'"
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="請輸入商品ID列表，以逗號分隔"
          />
          <!-- 預設 -->
          <el-input
            v-else
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="請先選擇區塊類型"
            disabled
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShopBlock">
import {
  SHOP_BLOCK_ADD,
  SHOP_BLOCK_EDIT,
  SHOP_BLOCK_REMOVE
} from '@/constants/permissions'
import { ref, reactive, onMounted } from 'vue'
import { listBlock, getBlock, addBlock, updateBlock, delBlock } from '@/api/shop/block'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const blockList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')

const queryFormRef = ref(null)
const blockFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  pageKey: undefined,
  blockKey: undefined,
  blockType: undefined,
  status: undefined
})

const form = ref({})

const rules = {
  pageKey: [{ required: true, message: '請選擇頁面', trigger: 'change' }],
  blockKey: [
    { required: true, message: '區塊識別不能為空', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '區塊識別只能包含大寫字母和底線', trigger: 'blur' }
  ],
  title: [{ required: true, message: '標題不能為空', trigger: 'blur' }],
  blockType: [{ required: true, message: '請選擇區塊類型', trigger: 'change' }]
}

const blockTypeMap = {
  TEXT: { label: '文字', type: 'info' },
  IMAGE: { label: '圖片', type: 'success' },
  HTML: { label: 'HTML', type: 'warning' },
  PRODUCT_LIST: { label: '商品列表', type: 'primary' }
}

const pageKeyMap = {
  HOME: '首頁',
  ABOUT: '關於我們',
  CONTACT: '聯絡資訊'
}

onMounted(() => {
  getList()
})

function getPageLabel(key) {
  return pageKeyMap[key] || key
}

function getList() {
  loading.value = true
  listBlock(queryParams).then(response => {
    blockList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getBlockTypeLabel(type) {
  return blockTypeMap[type]?.label || type
}

function getBlockTypeTag(type) {
  return blockTypeMap[type]?.type || 'info'
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleStatusChange(row) {
  const text = row.status === 'ENABLED' ? '啟用' : '停用'
  proxy.$modal.confirm('確認要「' + text + '」區塊「' + row.title + '」嗎？').then(() => {
    return updateBlock({ blockId: row.blockId, status: row.status })
  }).then(() => {
    proxy.$modal.msgSuccess(text + '成功')
  }).catch(() => {
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增區塊'
}

function handleUpdate(row) {
  reset()
  getBlock(row.blockId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改區塊'
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('確認刪除區塊「' + row.title + '」嗎？').then(() => {
    return delBlock(row.blockId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('刪除成功')
  }).catch(() => {})
}

function submitForm() {
  blockFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.blockId) {
        updateBlock(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addBlock(form.value).then(() => {
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
    blockId: undefined,
    pageKey: 'HOME',
    blockKey: undefined,
    title: undefined,
    subTitle: undefined,
    blockType: 'TEXT',
    content: undefined,
    imageUrl: undefined,
    sort: 0,
    status: 'ENABLED'
  }
  blockFormRef.value?.resetFields()
}
</script>
