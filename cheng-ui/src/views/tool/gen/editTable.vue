<template>
  <el-card>
    <el-tabs v-model="activeName">
      <el-tab-pane label="基本訊息" name="basic">
        <basic-info-form ref="basicInfo" :info="info" />
      </el-tab-pane>
      <el-tab-pane label="欄位訊息" name="columnInfo">
        <el-table ref="dragTable" :data="columns" row-key="columnId" :max-height="tableHeight">
          <el-table-column class-name="allowDrag" label="序號" min-width="5%" type="index"/>
          <el-table-column :show-overflow-tooltip="true" class-name="allowDrag" label="欄位列名" min-width="10%"
                           prop="columnName"/>
          <el-table-column label="欄位描述" min-width="10%">
            <template slot-scope="scope">
              <el-input v-model="scope.row.columnComment"></el-input>
            </template>
          </el-table-column>
          <el-table-column
            label="物理類型"
            prop="columnType"
            min-width="10%"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="Java類型" min-width="11%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.javaType">
                <el-option label="Long" value="Long" />
                <el-option label="String" value="String" />
                <el-option label="Integer" value="Integer" />
                <el-option label="Double" value="Double" />
                <el-option label="BigDecimal" value="BigDecimal" />
                <el-option label="Date" value="Date" />
                <el-option label="Boolean" value="Boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="java屬性" min-width="10%">
            <template slot-scope="scope">
              <el-input v-model="scope.row.javaField"></el-input>
            </template>
          </el-table-column>

          <el-table-column label="新增" min-width="5%">
            <template slot-scope="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isInsert"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="編輯" min-width="5%">
            <template slot-scope="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isEdit"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="列表" min-width="5%">
            <template slot-scope="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isList"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="查詢" min-width="5%">
            <template slot-scope="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isQuery"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="查詢方式" min-width="10%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.queryType">
                <el-option label="=" value="EQ" />
                <el-option label="!=" value="NE" />
                <el-option label=">" value="GT" />
                <el-option label=">=" value="GTE" />
                <el-option label="<" value="LT" />
                <el-option label="<=" value="LTE" />
                <el-option label="LIKE" value="LIKE" />
                <el-option label="BETWEEN" value="BETWEEN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="必填" min-width="5%">
            <template slot-scope="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isRequired"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="顯示類型" min-width="12%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.htmlType">
                <el-option label="文字框" value="input"/>
                <el-option label="文字域" value="textarea"/>
                <el-option label="下拉框" value="select" />
                <el-option label="單選框" value="radio"/>
                <el-option label="複選框" value="checkbox"/>
                <el-option label="日期元件" value="datetime" />
                <el-option label="圖片上傳" value="imageUpload"/>
                <el-option label="檔案上傳" value="fileUpload"/>
                <el-option label="富文字元件" value="editor"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="字典類型" min-width="12%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.dictType" clearable filterable placeholder="請選擇">
                <el-option
                  v-for="dict in dictOptions"
                  :key="dict.dictType"
                  :label="dict.dictName"
                  :value="dict.dictType">
                  <span style="float: left">{{ dict.dictName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
              </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="產生訊息" name="genInfo">
        <gen-info-form ref="genInfo" :info="info" :tables="tables" :menus="menus"/>
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="100px">
      <el-form-item style="text-align: center;margin-left:-100px;margin-top:10px;">
        <el-button type="primary" @click="submitForm()">提交</el-button>
        <el-button @click="close()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import {getGenTable, updateGenTable} from "@/api/tool/gen"
import {optionselect as getDictOptionselect} from "@/api/system/dict/type"
import {listMenu as getMenuTreeselect} from "@/api/system/menu"
import basicInfoForm from "./basicInfoForm"
import genInfoForm from "./genInfoForm"
import Sortable from 'sortablejs'

export default {
  name: "GenEdit",
  components: {
    basicInfoForm,
    genInfoForm
  },
  data() {
    return {
      // 選中選項卡的 name
      activeName: "columnInfo",
      // 表格的高度
      tableHeight: document.documentElement.scrollHeight - 245 + "px",
      // 表訊息
      tables: [],
      // 表列訊息
      columns: [],
      // 字典訊息
      dictOptions: [],
      // 選單訊息
      menus: [],
      // 表詳細訊息
      info: {}
    }
  },
  created() {
    const tableId = this.$route.params && this.$route.params.tableId
    if (tableId) {
      // 取得表詳細訊息
      getGenTable(tableId).then(res => {
        this.columns = res.data.rows
        this.info = res.data.info
        this.tables = res.data.tables
      })
      /** 查詢字典下拉列表 */
      getDictOptionselect().then(response => {
        this.dictOptions = response.data
      })
      /** 查詢選單下拉列表 */
      getMenuTreeselect().then(response => {
        this.menus = this.handleTree(response.data, "menuId")
      })
    }
  },
  methods: {
    /** 提交按鈕 */
    submitForm() {
      const basicForm = this.$refs.basicInfo.$refs.basicInfoForm
      const genForm = this.$refs.genInfo.$refs.genInfoForm
      Promise.all([basicForm, genForm].map(this.getFormPromise)).then(res => {
        const validateResult = res.every(item => !!item)
        if (validateResult) {
          const genTable = Object.assign({}, basicForm.model, genForm.model)
          genTable.columns = this.columns
          genTable.params = {
            treeCode: genTable.treeCode,
            treeName: genTable.treeName,
            treeParentCode: genTable.treeParentCode,
            parentMenuId: genTable.parentMenuId
          }
          updateGenTable(genTable).then(res => {
            this.$modal.msgSuccess(res.msg)
            if (res.code === 200) {
              this.close()
            }
          })
        } else {
          this.$modal.msgError("表單校驗未通過，請重新檢查提交内容")
        }
      })
    },
    getFormPromise(form) {
      return new Promise(resolve => {
        form.validate(res => {
          resolve(res)
        })
      })
    },
    /** 關閉按鈕 */
    close() {
      const obj = { path: "/tool/gen", query: { t: Date.now(), pageNum: this.$route.query.pageNum } }
      this.$tab.closeOpenPage(obj)
    }
  },
  mounted() {
    const el = this.$refs.dragTable.$el.querySelectorAll(".el-table__body-wrapper > table > tbody")[0]
    const sortable = Sortable.create(el, {
      handle: ".allowDrag",
      onEnd: evt => {
        const targetRow = this.columns.splice(evt.oldIndex, 1)[0]
        this.columns.splice(evt.newIndex, 0, targetRow)
        for (let index in this.columns) {
          this.columns[index].sort = parseInt(index) + 1
        }
      }
    })
  }
}
</script>
