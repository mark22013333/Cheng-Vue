<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="字典名稱" prop="dictType">
        <el-select v-model="queryParams.dictType">
          <el-option
            v-for="item in typeOptions"
            :key="item.dictId"
            :label="item.dictName"
            :value="item.dictType"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="字典標籤" prop="dictLabel">
        <el-input
          v-model="queryParams.dictLabel"
          placeholder="請輸入字典標籤"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="數據狀態">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['system:dict:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:dict:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:dict:remove']"
        >刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['system:dict:export']"
        >匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          @click="handleClose"
        >關閉
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column align="center" label="字典編碼" prop="dictCode"/>
      <el-table-column align="center" label="字典標籤" prop="dictLabel">
        <template #default="scope">
          <span v-if="(scope.row.listClass == '' || scope.row.listClass == 'default') && (scope.row.cssClass == '' || scope.row.cssClass == null)">{{ scope.row.dictLabel }}</span>
          <el-tag v-else :type="scope.row.listClass == 'primary' ? '' : scope.row.listClass" :class="scope.row.cssClass">{{ scope.row.dictLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="字典鍵值" prop="dictValue"/>
      <el-table-column label="字典排序" align="center" prop="dictSort" />
      <el-table-column align="center" label="狀態" prop="status">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
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
            v-hasPermi="['system:dict:edit']"
          >修改</el-button>
          <el-button
            type="primary"
            link
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:dict:remove']"
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
        <el-form-item label="字典類型">
          <el-input v-model="form.dictType" :disabled="true" />
        </el-form-item>
        <el-form-item label="數據標籤" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="請輸入數據標籤"/>
        </el-form-item>
        <el-form-item label="數據鍵值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="請輸入數據鍵值"/>
        </el-form-item>
        <el-form-item label="樣式屬性" prop="cssClass">
          <el-input v-model="form.cssClass" placeholder="請輸入樣式屬性"/>
        </el-form-item>
        <el-form-item label="顯示排序" prop="dictSort">
          <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="顯示樣式" prop="listClass">
          <el-select v-model="form.listClass">
            <el-option
              v-for="item in listClassOptions"
              :key="item.value"
              :label="item.label + '(' + item.value + ')'"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" placeholder="請輸入内容" type="textarea"></el-input>
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
import {addData, delData, getData, listData, updateData} from "@/api/system/dict/data"
import {getType, optionselect as getDictOptionselect} from "@/api/system/dict/type"

export default {
  name: "Data",
  dicts: ['sys_normal_disable'],
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
      // 字典表格數據
      dataList: [],
      // 預設字典類型
      defaultDictType: "",
      // 彈出層標題
      title: "",
      // 是否顯示彈出層
      open: false,
      // 數據標籤顯示樣式
      listClassOptions: [
        {
          value: "default",
          label: "預設"
        },
        {
          value: "primary",
          label: "主要"
        },
        {
          value: "success",
          label: "成功"
        },
        {
          value: "info",
          label: "訊息"
        },
        {
          value: "warning",
          label: "警告"
        },
        {
          value: "danger",
          label: "危險"
        }
      ],
      // 類型數據字典
      typeOptions: [],
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dictType: undefined,
        dictLabel: undefined,
        status: undefined
      },
      // 表單參數
      form: {},
      // 表單校驗
      rules: {
        dictLabel: [
          {required: true, message: "數據標籤不能為空", trigger: "blur"}
        ],
        dictValue: [
          {required: true, message: "數據鍵值不能為空", trigger: "blur"}
        ],
        dictSort: [
          {required: true, message: "數據順序不能為空", trigger: "blur"}
        ]
      }
    }
  },
  created() {
    const dictId = this.$route.params && this.$route.params.dictId
    this.getType(dictId)
    this.getTypeList()
  },
  methods: {
    /** 查詢字典類型詳細 */
    getType(dictId) {
      getType(dictId).then(response => {
        this.queryParams.dictType = response.data.dictType
        this.defaultDictType = response.data.dictType
        this.getList()
      })
    },
    /** 查詢字典類型列表 */
    getTypeList() {
      getDictOptionselect().then(response => {
        this.typeOptions = response.data
      })
    },
    /** 查詢字典數據列表 */
    getList() {
      this.loading = true
      listData(this.queryParams).then(response => {
        this.dataList = response.rows
        this.total = response.total
        this.loading = false
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
        dictCode: undefined,
        dictLabel: undefined,
        dictValue: undefined,
        cssClass: undefined,
        listClass: 'default',
        dictSort: 0,
        status: "0",
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 返回按鈕操作 */
    handleClose() {
      const obj = { path: "/system/dict" }
      this.$tab.closeOpenPage(obj)
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.queryParams.dictType = this.defaultDictType
      this.handleQuery()
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增字典數據"
      this.form.dictType = this.queryParams.dictType
    },
    // 多選框選中數據
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.dictCode)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset()
      const dictCode = row.dictCode || this.ids
      getData(dictCode).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改字典數據"
      })
    },
    /** 提交按鈕 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.dictCode != undefined) {
            updateData(this.form).then(response => {
              this.$store.dispatch('dict/removeDict', this.queryParams.dictType)
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addData(this.form).then(response => {
              this.$store.dispatch('dict/removeDict', this.queryParams.dictType)
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
      const dictCodes = row.dictCode || this.ids
      this.$modal.confirm('是否確認刪除字典編碼為"' + dictCodes + '"的數據項？').then(function () {
        return delData(dictCodes)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
        this.$store.dispatch('dict/removeDict', this.queryParams.dictType)
      }).catch(() => {})
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('system/dict/data/export', {
        ...this.queryParams
      }, `data_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
