<template>
  <div class="app-container">
    <!-- 查詢區域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="85px">
      <el-form-item label="選單名稱" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="請輸入選單名稱"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
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
          <el-option label="左右兩格" value="TWO_COLS" />
          <el-option label="左右兩格（高版）" value="TWO_COLS_HIGH" />
          <el-option label="上下三格" value="THREE_ROWS" />
          <el-option label="左右三格" value="THREE_COLS" />
          <el-option label="四格（2x2）" value="FOUR_GRID" />
          <el-option label="六格（2x3）" value="SIX_GRID" />
          <el-option label="六格（3x2）" value="SIX_GRID_ALT" />
          <el-option label="別名切換網格" value="ALIAS_SWITCH_GRID" />
          <el-option label="自訂版型" value="CUSTOM" />
        </el-select>
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="選單狀態" clearable style="width: 120px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="啟用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕區域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['line:richMenu:add']"
        >
          新增選單
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['line:richMenu:edit']"
        >
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['line:richMenu:remove']"
        >
          刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['line:richMenu:export']"
        >
          匯出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="richMenuList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="選單名稱" align="center" prop="name" :show-overflow-tooltip="true" min-width="150">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="success" size="mini" style="margin-right: 5px">
            預設
          </el-tag>
          <el-tag v-if="scope.row.selected === 1" type="primary" size="mini" style="margin-right: 5px">
            使用中
          </el-tag>
          {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column label="頻道名稱" align="center" prop="channelName" width="120" />
      <el-table-column label="版型類型" align="center" width="140">
        <template slot-scope="scope">
          <el-tag size="small" type="info">{{ getTemplateTypeName(scope.row.templateType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="圖片尺寸" align="center" prop="imageSize" width="120" />
      <el-table-column label="狀態" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 'DRAFT'" type="info" size="small">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === 'ACTIVE'" type="success" size="small">啟用</el-tag>
          <el-tag v-else type="warning" size="small">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="LINE Menu ID" align="center" prop="richMenuId" :show-overflow-tooltip="true" width="200">
        <template slot-scope="scope">
          <div v-if="scope.row.richMenuId" style="display: flex; align-items: center; justify-content: center;">
            <span style="margin-right: 5px; font-size: 12px;">{{ scope.row.richMenuId.substring(0, 15) }}...</span>
            <el-button
              type="text"
              icon="el-icon-document-copy"
              size="mini"
              @click="handleCopyRichMenuId(scope.row.richMenuId)"
              style="padding: 0;"
              title="複製 Rich Menu ID"
            ></el-button>
          </div>
          <span v-else style="color: #ccc">未發布</span>
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="70" />
      <el-table-column label="建立時間" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['line:richMenu:edit']"
          >編輯</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-upload2"
            @click="handlePublish(scope.row)"
            v-hasPermi="['line:richMenu:publish']"
          >{{ scope.row.richMenuId ? '重新發布' : '發布' }}</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" style="margin-left: 10px">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-if="scope.row.richMenuId && scope.row.isDefault !== 1"
                command="setDefault"
                icon="el-icon-star-on"
                v-hasPermi="['line:richMenu:setDefault']"
              >
                設為預設
              </el-dropdown-item>
              <el-dropdown-item
                v-if="scope.row.richMenuId"
                command="deleteFromLine"
                icon="el-icon-remove"
                divided
                v-hasPermi="['line:richMenu:publish']"
              >
                從 LINE 刪除
              </el-dropdown-item>
              <el-dropdown-item command="delete" icon="el-icon-delete" divided v-hasPermi="['line:richMenu:remove']">
                刪除
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/修改對話框 -->
    <el-dialog :title="title" :visible.sync="open" width="95%" append-to-body :close-on-click-modal="false" top="3vh" @opened="onDialogOpened">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" :key="dialogKey">
        <el-row>
          <el-col :span="12">
            <el-form-item label="頻道" prop="configId">
              <el-select v-model="form.configId" placeholder="請選擇頻道" style="width: 100%">
                <el-option
                  v-for="config in channelList"
                  :key="config.configId"
                  :label="config.channelName"
                  :value="config.configId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="選單名稱" prop="name">
              <el-input v-model="form.name" placeholder="請輸入選單名稱" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="選單說明" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="請輸入選單說明" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="版型類型" prop="templateType">
              <el-select v-model="form.templateType" placeholder="請選擇版型" @change="handleTemplateChange" style="width: 100%">
                <el-option label="左右兩格" value="TWO_COLS" />
                <el-option label="左右兩格（高版）" value="TWO_COLS_HIGH" />
                <el-option label="上下三格" value="THREE_ROWS" />
                <el-option label="左右三格" value="THREE_COLS" />
                <el-option label="四格（2x2）" value="FOUR_GRID" />
                <el-option label="六格（2x3）" value="SIX_GRID" />
                <el-option label="六格（3x2）" value="SIX_GRID_ALT" />
                <el-option label="別名切換網格" value="ALIAS_SWITCH_GRID" />
                <el-option label="自訂版型" value="CUSTOM" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="圖片尺寸" prop="imageSize">
              <el-select v-model="form.imageSize" placeholder="請選擇圖片尺寸" @change="handleImageSizeChange" style="width: 100%">
                <el-option label="2500x1686（全版高）" value="2500x1686" />
                <el-option label="2500x843（全版矮）" value="2500x843" />
                <el-option label="1200x810（半版高）" value="1200x810" />
                <el-option label="1200x405（半版矮）" value="1200x405" />
                <el-option label="800x540（1/3 版高）" value="800x540" />
                <el-option label="800x270（1/3 版矮）" value="800x270" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="聊天欄位文字" prop="chatBarText">
              <el-input v-model="form.chatBarText" placeholder="請輸入聊天欄位顯示文字（最多14字）" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Alias ID" prop="suggestedAliasId">
              <el-input v-model="form.suggestedAliasId" placeholder="選填，用於選單切換（僅小寫字母、數字、連字號）" maxlength="32">
                <template slot="prepend">richmenu-alias-</template>
              </el-input>
              <span style="color: #999; font-size: 12px">ℹ️ 填寫後發布時會自動建立 Alias</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="選單圖片" prop="imageUrl">
              <el-input v-model="form.imageUrl" placeholder="圖片 URL（暫時使用文字輸入，後續將整合圖片上傳）" />
              <span style="color: #999; font-size: 12px">註：後續版本將提供圖片上傳功能</span>
            </el-form-item>
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
                @change="handleAreasChange"
                @manual-edit="handleManualEdit"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="備註" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="請輸入備註" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRichMenu, getRichMenu, addRichMenu, updateRichMenu, delRichMenu, publishRichMenu, setDefaultRichMenu, deleteRichMenuFromLine } from '@/api/line/richMenu'
import { listConfig } from '@/api/line/config'
import RichMenuEditor from './components/RichMenuEditor'

export default {
  name: 'LineRichMenu',
  components: {
    RichMenuEditor
  },
  data() {
    return {
      // 遮罩層
      loading: true,
      // 選中陣列
      ids: [],
      // 非單個禁用
      single: true,
      // 非多個禁用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總條數
      total: 0,
      // Rich Menu 列表
      richMenuList: [],
      // 頻道列表
      channelList: [],
      // 彈出層標題
      title: '',
      // 是否顯示彈出層
      open: false,
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        configId: null,
        templateType: null,
        status: null
      },
      // 表單參數
      form: {
        areas: []
      },
      // 表單校驗
      rules: {
        configId: [
          { required: true, message: '頻道不能為空', trigger: 'change' }
        ],
        name: [
          { required: true, message: '選單名稱不能為空', trigger: 'blur' }
        ],
        templateType: [
          { required: true, message: '版型類型不能為空', trigger: 'change' }
        ],
        imageSize: [
          { required: true, message: '圖片尺寸不能為空', trigger: 'change' }
        ],
        suggestedAliasId: [
          { pattern: /^[a-z0-9-]*$/, message: 'Alias ID 只能包含小寫字母、數字和連字號', trigger: 'blur' }
        ]
      },
      // 版型類型選項
      templateTypeOptions: [
        { label: '左右兩格', value: 'TWO_COLS' },
        { label: '左右兩格（高版）', value: 'TWO_COLS_HIGH' },
        { label: '上下三格', value: 'THREE_ROWS' },
        { label: '左右三格', value: 'THREE_COLS' },
        { label: '四格（2x2）', value: 'FOUR_GRID' },
        { label: '六格（2x3）', value: 'SIX_GRID' },
        { label: '六格（3x2）', value: 'SIX_GRID_ALT' },
        { label: '別名切換網格', value: 'ALIAS_SWITCH_GRID' },
        { label: '自訂版型', value: 'CUSTOM' }
      ],
      // 對話框 key，用於強制重新渲染
      dialogKey: 0
    }
  },
  created() {
    this.getList()
    this.getChannelList()
  },
  methods: {
    /** 查詢 Rich Menu 列表 */
    getList() {
      this.loading = true
      listRichMenu(this.queryParams).then(response => {
        this.richMenuList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查詢頻道列表 */
    getChannelList() {
      listConfig({ status: 1 }).then(response => {
        this.channelList = response.rows || []
      })
    },
    // 取消按鈕
    cancel() {
      this.open = false
      this.reset()
    },
    // 表單重置
    reset() {
      this.form = {
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
        remark: null
      }
      this.resetForm('form')
    },
    /** 區塊設定變更處理 */
    handleAreasChange(areas) {
      // 將 areas 陣列轉換為 JSON 字串存入 areasJson
      this.form.areasJson = JSON.stringify(areas)
    },
    /** 手動編輯處理（拖動/新增區塊） */
    handleManualEdit() {
      // 自動將版型類型設為自訂版型
      if (this.form.templateType !== 'CUSTOM') {
        this.form.templateType = 'CUSTOM'
        this.$message.info('已自動切換為自訂版型')
      }
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    // 多選框選中資料
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      this.dialogKey++ // 強制重新渲染對話框
      // 為新增設定預設版型
      this.form.templateType = 'TWO_COLS'
      this.form.imageSize = '2500x1686'
      this.open = true
      this.title = '新增 Rich Menu'
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      const id = row.id || this.ids
      getRichMenu(id).then(response => {
        // 遞增 dialogKey 強制重新渲染
        this.dialogKey++
        
        // 直接設定從 API 獲取的資料（不要先 reset，避免清空 templateType）
        this.form = response.data
        
        // 將 areasJson 字串轉換為 areas 陣列供編輯器使用
        if (this.form.areasJson) {
          try {
            this.form.areas = JSON.parse(this.form.areasJson)
          } catch (e) {
            console.error('解析 areasJson 失敗:', e)
            this.form.areas = []
          }
        } else {
          this.form.areas = []
        }
        
        // 確保必要欄位有預設值
        if (!this.form.areas) {
          this.form.areas = []
        }
        
        // 使用 nextTick 確保資料完全設定後再打開對話框
        this.$nextTick(() => {
          this.open = true
          this.title = '修改 Rich Menu'
        })
      })
    },
    /** 提交按鈕 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          // 驗證 Action 欄位完整性
          if (this.form.areas && this.form.areas.length > 0) {
            const invalidAreas = []
            this.form.areas.forEach((area, index) => {
              const action = area.action
              if (!action) {
                invalidAreas.push(`區塊 ${index + 1}`)
                return
              }
              
              // 檢查各類 action 的必填欄位
              if (action.type === 'uri' && !action.uri) {
                invalidAreas.push(`區塊 ${index + 1}（缺少 URI）`)
              } else if (action.type === 'message' && !action.text) {
                invalidAreas.push(`區塊 ${index + 1}（缺少訊息文字）`)
              } else if (action.type === 'postback' && !action.data) {
                invalidAreas.push(`區塊 ${index + 1}（缺少 Postback 資料）`)
              } else if (action.type === 'richmenuswitch') {
                if (!action.richMenuAliasId) {
                  invalidAreas.push(`區塊 ${index + 1}（缺少 Alias ID）`)
                } else if (!action.data) {
                  invalidAreas.push(`區塊 ${index + 1}（缺少 Postback 資料）`)
                }
              }
            })
            
            if (invalidAreas.length > 0) {
              this.$modal.msgWarning('以下區塊的 Action 設定不完整，請補充：<br>' + invalidAreas.join('<br>'))
              return
            }
            
            this.form.areasJson = JSON.stringify(this.form.areas)
          }
          
          if (this.form.id != null) {
            updateRichMenu(this.form).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addRichMenu(this.form).then(response => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否確認刪除所選的 Rich Menu？').then(() => {
        return delRichMenu(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('刪除成功')
      }).catch(() => {})
    },
    /** 發布按鈕操作 */
    handlePublish(row) {
      this.$modal.confirm('確認要發布此 Rich Menu 到 LINE 平台嗎？').then(() => {
        return publishRichMenu(row.id)
      }).then(() => {
        this.$modal.msgSuccess('發布成功')
        this.getList()
      }).catch(error => {
        // 顯示詳細錯誤訊息
        if (error && error.msg) {
          this.$modal.msgError(error.msg)
        }
      })
    },
    /** 更多操作 */
    handleCommand(command, row) {
      switch (command) {
        case 'setDefault':
          this.handleSetDefault(row)
          break
        case 'deleteFromLine':
          this.handleDeleteFromLine(row)
          break
        case 'delete':
          this.handleDelete(row)
          break
      }
    },
    /** 設為預設選單 */
    handleSetDefault(row) {
      this.$modal.confirm('確認要將此 Rich Menu 設為預設選單嗎？').then(() => {
        return setDefaultRichMenu(row.id)
      }).then(() => {
        this.$modal.msgSuccess('設定成功')
        this.getList()
      }).catch(error => {
        if (error && error.msg) {
          this.$modal.msgError(error.msg)
        }
      })
    },
    /** 從 LINE 刪除 */
    handleDeleteFromLine(row) {
      this.$modal.confirm('確認要從 LINE 平台刪除此 Rich Menu 嗎？').then(() => {
        return deleteRichMenuFromLine(row.id)
      }).then(() => {
        this.$modal.msgSuccess('刪除成功')
        this.getList()
      }).catch(error => {
        if (error && error.msg) {
          this.$modal.msgError(error.msg)
        }
      })
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('/line/richMenu/export', {
        ...this.queryParams
      }, `richmenu_${new Date().getTime()}.xlsx`)
    },
    /** 版型變更處理 */
    handleTemplateChange(value) {
      // 根據版型自動設定預設圖片尺寸
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
        this.form.imageSize = templateSizeMap[value]
      }
      
      // 清空 areas，觸發 RichMenuEditor 重新初始化
      this.form.areas = []
      
      // 強制重新渲染對話框，修正圖片尺寸變化時的跑版問題
      this.dialogKey++
    },
    /** 圖片尺寸變更處理 */
    handleImageSizeChange() {
      // 圖片尺寸變化時強制重新渲染，修正跑版問題
      this.dialogKey++
      this.$message.info('圖片尺寸已變更，編輯器已重新整理')
    },
    /** 取得版型類型名稱 */
    getTemplateTypeName(value) {
      const option = this.templateTypeOptions.find(item => item.value === value)
      return option ? option.label : value
    },
    /** 對話框打開事件 */
    onDialogOpened() {
      // 對話框完全打開後，確保 RichMenuEditor 能正確接收資料
      this.$nextTick(() => {
        // 強制更新，確保子組件正確渲染
        this.$forceUpdate()
      })
    },
    /** 複製 Rich Menu ID */
    handleCopyRichMenuId(richMenuId) {
      // 使用現代 Clipboard API
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(richMenuId).then(() => {
          this.$message.success('已複製 Rich Menu ID')
        }).catch(() => {
          this.fallbackCopyToClipboard(richMenuId)
        })
      } else {
        // 降級方案：使用 execCommand
        this.fallbackCopyToClipboard(richMenuId)
      }
    },
    /** 降級方案：使用 execCommand 複製 */
    fallbackCopyToClipboard(text) {
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.left = '-9999px'
      document.body.appendChild(textArea)
      textArea.select()
      try {
        document.execCommand('copy')
        this.$message.success('已複製 Rich Menu ID')
      } catch (err) {
        this.$message.error('複製失敗，請手動複製')
      }
      document.body.removeChild(textArea)
    }
  }
}
</script>

<style scoped>
.el-dropdown {
  vertical-align: top;
}
.el-dropdown + .el-dropdown {
  margin-left: 15px;
}
</style>
