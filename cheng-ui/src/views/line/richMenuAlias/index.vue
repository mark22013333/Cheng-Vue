<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="Alias ID" prop="aliasId">
        <el-input
          v-model="queryParams.aliasId"
          placeholder="請輸入 Alias ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="queryParams.description"
          placeholder="請輸入描述"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          :icon="Plus"
          @click="handleAdd"
          v-hasPermi="['line:richMenuAlias:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['line:richMenuAlias:remove']"
        >刪除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          :icon="Refresh"
          @click="handleSync"
          v-hasPermi="['line:richMenuAlias:sync']"
        >從 LINE 同步</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="aliasList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="Alias ID" align="center" prop="aliasId" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-tag>{{ scope.row.aliasId }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Rich Menu" align="center" prop="richMenuName" :show-overflow-tooltip="true" />
      <el-table-column label="LINE Rich Menu ID" align="center" prop="lineRichMenuId" width="220" :show-overflow-tooltip="true">
        <template #default="scope">
          <div v-if="scope.row.lineRichMenuId" style="display: flex; align-items: center; justify-content: center; gap: 5px;">
            <span style="font-size: 12px; color: #606266;">
              {{ scope.row.lineRichMenuId.substring(0, 18) }}...
            </span>
            <el-button
              type="text"
              :icon="DocumentCopy"
              @click="handleCopyRichMenuId(scope.row.lineRichMenuId)"
              title="複製完整 ID"
            ></el-button>
          </div>
          <span v-else style="color: #999;">未發布</span>
        </template>
      </el-table-column>
      <el-table-column label="頻道" align="center" prop="channelName" width="120" />
      <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="建立時間" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width operation-column" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['line:richMenuAlias:remove']">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增或修改對話框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="Rich Menu" prop="richMenuId">
          <el-select v-model="formData.richMenuId" placeholder="請選擇 Rich Menu" style="width: 100%;">
            <el-option
              v-for="menu in richMenuOptions"
              :key="menu.id"
              :label="`${menu.name} (${menu.channelName})`"
              :value="menu.id"
              :disabled="!menu.richMenuId"
            >
              <span style="float: left">{{ menu.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ menu.channelName }}</span>
            </el-option>
          </el-select>
          <span style="color: #999; font-size: 12px">
            ⚠️ 只能選擇已發布到 LINE 的 Rich Menu
          </span>
        </el-form-item>
        <el-form-item label="Alias ID" prop="aliasId">
          <el-input
            v-model="formData.aliasId"
            placeholder="richmenu-alias-home"
            maxlength="32"
            show-word-limit
            :disabled="formData.id != null"
          />
          <span style="color: #999; font-size: 12px">
            ⚠️ 建立後不可修改，最多 32 字元。<strong>只能包含小寫字母、數字和連字號</strong>，例如：richmenu-alias-home
          </span>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            placeholder="請輸入描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            placeholder="請輸入備註"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">確 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="LineRichMenuAlias">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { Search, Refresh, Plus, Delete, DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRichMenuAlias, getRichMenuAlias, addRichMenuAlias, updateRichMenuAlias, delRichMenuAlias, checkAliasUsage, syncAliasFromLine } from '@/api/line/richMenuAlias'
import { listRichMenu } from '@/api/line/richMenu'

const { proxy } = getCurrentInstance()

const queryFormRef = ref()
const formRef = ref()

const loading = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const showSearch = ref(true)
const total = ref(0)
const aliasList = ref([])
const richMenuOptions = ref([])
const title = ref('')
const open = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  aliasId: null,
  description: null,
  richMenuId: null
})

const formData = reactive({
  id: null,
  richMenuId: null,
  aliasId: null,
  description: null,
  remark: null
})

const rules = {
  richMenuId: [
    { required: true, message: 'Rich Menu 不能為空', trigger: 'change' }
  ],
  aliasId: [
    { required: true, message: 'Alias ID 不能為空', trigger: 'blur' },
    { min: 1, max: 32, message: 'Alias ID 長度必須在 1 到 32 個字元之間', trigger: 'blur' },
    { pattern: /^[a-z0-9-]+$/, message: 'Alias ID 只能包含小寫字母、數字和連字號', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述長度不能超過 200 個字元', trigger: 'blur' }
  ]
}

/** 查詢 Rich Menu Alias 列表 */
function getList() {
  loading.value = true
  listRichMenuAlias(queryParams).then(response => {
    aliasList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取得 Rich Menu 選項 */
function getRichMenuOptions() {
  listRichMenu({ pageNum: 1, pageSize: 9999 }).then(response => {
    richMenuOptions.value = response.rows
  })
}

/** 取消按鈕 */
function cancel() {
  open.value = false
  reset()
}

/** 表單重置 */
function reset() {
  Object.assign(formData, {
    id: null,
    richMenuId: null,
    aliasId: null,
    description: null,
    remark: null
  })
  formRef.value?.resetFields()
}

/** 搜尋按鈕操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按鈕操作 */
function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 多選框選中資料 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按鈕操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '新增 Rich Menu Alias'
}

/** 修改按鈕操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getRichMenuAlias(id).then(response => {
    Object.assign(formData, response.data)
    open.value = true
    title.value = '修改 Rich Menu Alias'
  })
}

/** 提交按鈕 */
function submitForm() {
  formRef.value?.validate(valid => {
    if (valid) {
      if (formData.id != null) {
        updateRichMenuAlias(formData).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        addRichMenuAlias(formData).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 刪除按鈕操作 */
async function handleDelete(row) {
  const delIds = row.id || ids.value
  const aliasId = row.aliasId
  
  if (row.id && aliasId) {
    try {
      const response = await checkAliasUsage(aliasId)
      const { usedByMenus, usageCount } = response.data
      
      let message = '是否確認刪除此 Rich Menu Alias？此操作會同步刪除 LINE 平台上的 Alias。'
      
      if (usageCount > 0) {
        const menuNames = usedByMenus.map(m => m.name).join('、')
        message = `<div style="text-align: left;">
          <p><strong>警告：</strong>以下 ${usageCount} 個選單正在使用此別名：</p>
          <p style="color: #E6A23C; padding: 10px; background: #FDF6EC; border-radius: 4px; margin: 10px 0;">
            ${menuNames}
          </p>
          <p>刪除後，這些選單中的別名引用將被<strong>自動清空</strong>（保留 action 結構）</p>
          <p style="color: #F56C6C;">確定要繼續刪除嗎？</p>
        </div>`
      }
      
      ElMessageBox.confirm(message, '刪除確認', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '確定刪除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delRichMenuAlias(delIds)
      }).then(() => {
        getList()
        ElMessage.success('刪除成功')
      }).catch(() => {})
      
    } catch (error) {
      console.error('檢查別名使用情況失敗', error)
      ElMessageBox.confirm('無法檢查別名使用情況，是否仍要刪除？', '提示', {
        type: 'warning'
      }).then(() => {
        return delRichMenuAlias(delIds)
      }).then(() => {
        getList()
        ElMessage.success('刪除成功')
      }).catch(() => {})
    }
  } else {
    ElMessageBox.confirm('是否確認刪除選中的 Rich Menu Alias？此操作會同步刪除 LINE 平台上的 Alias。', '提示', {
      type: 'warning'
    }).then(() => {
      return delRichMenuAlias(delIds)
    }).then(() => {
      getList()
      ElMessage.success('刪除成功')
    }).catch(() => {})
  }
}

/** 複製 Rich Menu ID */
function handleCopyRichMenuId(richMenuId) {
  const textarea = document.createElement('textarea')
  textarea.value = richMenuId
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  
  try {
    document.execCommand('copy')
    ElMessage.success('已複製到剪貼簿')
  } catch (err) {
    ElMessage.error('複製失敗')
  } finally {
    document.body.removeChild(textarea)
  }
}

/** 從 LINE 同步 */
function handleSync() {
  ElMessageBox.confirm('是否從 LINE 平台同步 Alias 列表？', '提示', {
    type: 'info'
  }).then(() => {
    loading.value = true
    return syncAliasFromLine()
  }).then(response => {
    ElMessage.success(response.msg || '同步成功')
    getList()
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  getList()
  getRichMenuOptions()
})
</script>

<style scoped lang="scss">
</style>
