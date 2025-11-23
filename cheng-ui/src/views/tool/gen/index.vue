<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="表名稱" prop="tableName">
        <el-input
          v-model="queryParams.tableName"
          placeholder="請輸入表名稱"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表描述" prop="tableComment">
        <el-input
          v-model="queryParams.tableComment"
          placeholder="請輸入表描述"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="建立時間">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          end-placeholder="結束日期"
          start-placeholder="開始日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="small" type="primary" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-download"
          size="small"
          :disabled="multiple"
          @click="handleGenTable"
          v-hasPermi="['tool:gen:code']"
        >產生
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="openCreateTable"
          v-hasRole="['admin']"
        >建立
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload"
          size="small"
          @click="openImportTable"
          v-hasPermi="['tool:gen:import']"
        >匯入
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleEditTable"
          v-hasPermi="['tool:gen:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['tool:gen:remove']"
        >刪除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tables" v-loading="loading" :data="tableList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" align="center" width="55"></el-table-column>
      <el-table-column align="center" label="序號" type="index" width="50">
        <template #default="scope">
          <span>{{(queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="表名稱" prop="tableName" width="120"/>
      <el-table-column label="表描述" align="center" prop="tableComment" :show-overflow-tooltip="true" width="120" />
      <el-table-column :show-overflow-tooltip="true" align="center" label="實體" prop="className" width="120"/>
      <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="建立時間" prop="createTime"
                       sortable="custom" width="160"/>
      <el-table-column :sort-orders="['descending', 'ascending']" align="center" label="更新時間" prop="updateTime"
                       sortable="custom" width="160"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            type="text"
            size="small"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
            v-hasPermi="['tool:gen:preview']"
          >預覽
          </el-button>
          <el-button
            type="text"
            size="small"
            icon="el-icon-edit"
            @click="handleEditTable(scope.row)"
            v-hasPermi="['tool:gen:edit']"
          >編輯
          </el-button>
          <el-button
            type="text"
            size="small"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tool:gen:remove']"
          >刪除
          </el-button>
          <el-button
            type="text"
            size="small"
            icon="el-icon-refresh"
            @click="handleSynchDb(scope.row)"
            v-hasPermi="['tool:gen:edit']"
          >同步</el-button>
          <el-button
            type="text"
            size="small"
            icon="el-icon-download"
            @click="handleGenTable(scope.row)"
            v-hasPermi="['tool:gen:code']"
          >產生程式碼
          </el-button>
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
    <!-- 預覽界面 -->
    <el-dialog :title="preview.title" :visible.sync="preview.open" width="80%" top="5vh" append-to-body class="scrollbar">
      <el-tabs v-model="preview.activeName">
        <el-tab-pane
          v-for="(value, key) in preview.data"
          :label="key.substring(key.lastIndexOf('/')+1,key.indexOf('.vm'))"
          :name="key.substring(key.lastIndexOf('/')+1,key.indexOf('.vm'))"
          :key="key"
        >
          <el-link v-clipboard:copy="value" v-clipboard:success="clipboardSuccess" :underline="false"
                   icon="el-icon-document-copy" style="float:right">複製
          </el-link>
          <pre><code class="hljs" v-html="highlightedCode(value, key)"></code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    <import-table ref="import" @ok="handleQuery" />
    <create-table ref="create" @ok="handleQuery" />
  </div>
</template>

<script>
import {delTable, genCode, listTable, previewTable, synchDb} from "@/api/tool/gen"
import importTable from "./importTable"
import createTable from "./createTable"
import hljs from "highlight.js/lib/highlight"
import "highlight.js/styles/github-gist.css"

hljs.registerLanguage("java", require("highlight.js/lib/languages/java"))
hljs.registerLanguage("xml", require("highlight.js/lib/languages/xml"))
hljs.registerLanguage("html", require("highlight.js/lib/languages/xml"))
hljs.registerLanguage("vue", require("highlight.js/lib/languages/xml"))
hljs.registerLanguage("javascript", require("highlight.js/lib/languages/javascript"))
hljs.registerLanguage("sql", require("highlight.js/lib/languages/sql"))

export default {
  name: "Gen",
  components: { importTable, createTable },
  data() {
    return {
      // 遮罩層
      loading: true,
      // 唯一標識符
      uniqueId: "",
      // 選中陣列
      ids: [],
      // 選中表陣列
      tableNames: [],
      // 非單個禁用
      single: true,
      // 非多個禁用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總則數
      total: 0,
      // 表數據
      tableList: [],
      // 日期範圍
      dateRange: "",
      // 預設排序
      defaultSort: { prop: "createTime", order: "descending" },
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        tableName: undefined,
        tableComment: undefined
      },
      // 預覽參數
      preview: {
        open: false,
        title: "程式碼預覽",
        data: {},
        activeName: "domain.java"
      }
    }
  },
  created() {
    this.queryParams.orderByColumn = this.defaultSort.prop
    this.queryParams.isAsc = this.defaultSort.order
    this.getList()
  },
  activated() {
    const time = this.$route.query.t
    if (time != null && time != this.uniqueId) {
      this.uniqueId = time
      this.queryParams.pageNum = Number(this.$route.query.pageNum)
      this.getList()
    }
  },
  methods: {
    /** 查詢表集合 */
    getList() {
      this.loading = true
      listTable(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.tableList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 產生程式碼操作 */
    handleGenTable(row) {
      const tableNames = row.tableName || this.tableNames
      if (tableNames == "") {
        this.$modal.msgError("請選擇要產生的數據")
        return
      }
      if(row.genType === "1") {
        genCode(row.tableName).then(response => {
          this.$modal.msgSuccess("成功產生到自定義路徑：" + row.genPath)
        })
      } else {
        this.$download.zip("/tool/gen/batchGenCode?tables=" + tableNames, "cheng.zip")
      }
    },
    /** 同步資料庫操作 */
    handleSynchDb(row) {
      const tableName = row.tableName
      this.$modal.confirm('確認要強制同步"' + tableName + '"表結構嗎？').then(function () {
        return synchDb(tableName)
      }).then(() => {
        this.$modal.msgSuccess("同步成功")
      }).catch(() => {})
    },
    /** 打開匯入表彈窗 */
    openImportTable() {
      this.$refs.import.show()
    },
    /** 打開建立表彈窗 */
    openCreateTable() {
      this.$refs.create.show()
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.queryParams.pageNum = 1
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order)
    },
    /** 預覽按鈕 */
    handlePreview(row) {
      previewTable(row.tableId).then(response => {
        this.preview.data = response.data
        this.preview.open = true
        this.preview.activeName = "domain.java"
      })
    },
    /** 高亮顯示 */
    highlightedCode(code, key) {
      const vmName = key.substring(key.lastIndexOf("/") + 1, key.indexOf(".vm"))
      var language = vmName.substring(vmName.indexOf(".") + 1, vmName.length)
      const result = hljs.highlight(language, code || "", true)
      return result.value || '&nbsp;'
    },
    /** 複製程式碼成功 */
    clipboardSuccess() {
      this.$modal.msgSuccess("複製成功")
    },
    // 多選框選中數據
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.tableId)
      this.tableNames = selection.map(item => item.tableName)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 排序觸發事件 */
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop
      this.queryParams.isAsc = column.order
      this.getList()
    },
    /** 修改按鈕操作 */
    handleEditTable(row) {
      const tableId = row.tableId || this.ids[0]
      const tableName = row.tableName || this.tableNames[0]
      const params = { pageNum: this.queryParams.pageNum }
      this.$tab.openPage("修改[" + tableName + "]產生設定", '/tool/gen-edit/index/' + tableId, params)
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const tableIds = row.tableId || this.ids
      this.$modal.confirm('是否確認刪除表編號為"' + tableIds + '"的數據項？').then(function () {
        return delTable(tableIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {})
    }
  }
}
</script>
