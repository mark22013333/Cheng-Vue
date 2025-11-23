<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="參數名稱" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="請輸入參數名稱"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="參數鍵名" prop="configKey">
        <el-input
          v-model="queryParams.configKey"
          placeholder="請輸入參數鍵名"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="系統內建" prop="configType">
        <el-select v-model="queryParams.configType" clearable placeholder="系統內建">
          <el-option
            v-for="dict in dict.type.sys_yes_no"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
        <el-button icon="el-icon-search" type="primary" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
          v-hasPermi="['system:config:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:config:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:config:remove']"
        >刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['system:config:export']"
        >匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-refresh"
          @click="handleRefreshCache"
          v-hasPermi="['system:config:remove']"
        >重新整理暫存
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column align="center" label="參數主鍵" prop="configId"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="參數名稱" prop="configName"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="參數鍵名" prop="configKey"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="參數鍵值" prop="configValue"/>
      <el-table-column align="center" label="系統內建" prop="configType">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_yes_no" :value="scope.row.configType"/>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="備註" prop="remark"/>
      <el-table-column align="center" label="建立時間" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            type="primary"
            link
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:config:edit']"
          >修改</el-button>
          <el-button
            type="primary"
            link
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:config:remove']"
          >刪除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增或修改參數設定對話框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="參數名稱" prop="configName">
          <el-input v-model="form.configName" placeholder="請輸入參數名稱"/>
        </el-form-item>
        <el-form-item label="參數鍵名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="請輸入參數鍵名"/>
        </el-form-item>
        <el-form-item label="參數鍵值" prop="configValue">
          <el-input v-model="form.configValue" placeholder="請輸入參數鍵值" type="textarea"/>
        </el-form-item>
        <el-form-item label="系統內建" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio
              v-for="dict in dict.type.sys_yes_no"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" placeholder="請輸入内容" type="textarea"/>
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

<script>
import {addConfig, delConfig, getConfig, listConfig, refreshCache, updateConfig} from "@/api/system/config"

export default {
  name: "Config",
  dicts: ['sys_yes_no'],
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
      // 總則數
      total: 0,
      // 參數表格數據
      configList: [],
      // 彈出層標題
      title: "",
      // 是否顯示彈出層
      open: false,
      // 日期範圍
      dateRange: [],
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configName: undefined,
        configKey: undefined,
        configType: undefined
      },
      // 表單參數
      form: {},
      // 表單校驗
      rules: {
        configName: [
          {required: true, message: "參數名稱不能為空", trigger: "blur"}
        ],
        configKey: [
          {required: true, message: "參數鍵名不能為空", trigger: "blur"}
        ],
        configValue: [
          {required: true, message: "參數鍵值不能為空", trigger: "blur"}
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查詢參數列表 */
    getList() {
      this.loading = true
      listConfig(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.configList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    // 取消按鈕
    cancel() {
      this.open = false
      this.reset()
    },
    // 表單重置
    reset() {
      this.form = {
        configId: undefined,
        configName: undefined,
        configKey: undefined,
        configValue: undefined,
        configType: "Y",
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增參數"
    },
    // 多選框選中數據
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset()
      const configId = row.configId || this.ids
      getConfig(configId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改參數"
      })
    },
    /** 提交按鈕 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            updateConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addConfig(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const configIds = row.configId || this.ids
      this.$modal.confirm('是否確認刪除參數編號為"' + configIds + '"的數據項？').then(function () {
        return delConfig(configIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {})
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('system/config/export', {
        ...this.queryParams
      }, `config_${new Date().getTime()}.xlsx`)
    },
    /** 重新整理暫存按鈕操作 */
    handleRefreshCache() {
      refreshCache().then(() => {
        this.$modal.msgSuccess("重新整理成功")
      })
    }
  }
}
</script>
