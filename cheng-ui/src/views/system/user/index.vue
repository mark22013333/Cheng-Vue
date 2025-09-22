<template>
  <div class="app-container">
    <el-row :gutter="20">
      <splitpanes :horizontal="this.$store.getters.device === 'mobile'" class="default-theme">
        <!--部門數據-->
        <pane size="16">
          <el-col>
            <div class="head-container">
              <el-input v-model="deptName" clearable placeholder="請輸入部門名稱" prefix-icon="el-icon-search"
                        size="small" style="margin-bottom: 20px"/>
            </div>
            <div class="head-container">
              <el-tree :data="deptOptions" :props="defaultProps" :expand-on-click-node="false" :filter-node-method="filterNode" ref="tree" node-key="id" default-expand-all highlight-current @node-click="handleNodeClick" />
            </div>
          </el-col>
        </pane>
        <!--使用者數據-->
        <pane size="84">
          <el-col>
            <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="85px">
              <el-form-item label="使用者名稱" prop="userName">
                <el-input v-model="queryParams.userName" clearable placeholder="請輸入使用者名稱" style="width: 240px"
                          @keyup.enter.native="handleQuery"/>
              </el-form-item>
              <el-form-item label="手機號碼" prop="phonenumber">
                <el-input v-model="queryParams.phonenumber" clearable placeholder="請輸入手機號碼" style="width: 240px"
                          @keyup.enter.native="handleQuery"/>
              </el-form-item>
              <el-form-item label="狀態" prop="status">
                <el-select v-model="queryParams.status" clearable placeholder="使用者狀態" style="width: 140px">
                  <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="建立時間">
                <el-date-picker v-model="dateRange" end-placeholder="結束日期" range-separator="-" start-placeholder="開始日期"
                                style="width: 240px" type="daterange"
                                value-format="yyyy-MM-dd"></el-date-picker>
              </el-form-item>
              <el-form-item>
                <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜尋</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-hasPermi="['system:user:remove']" :disabled="multiple" icon="el-icon-delete" plain size="mini"
                           type="danger" @click="handleDelete">刪除
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-hasPermi="['system:user:import']" icon="el-icon-upload2" plain size="mini" type="info"
                           @click="handleImport">匯入
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-hasPermi="['system:user:export']" icon="el-icon-download" plain size="mini" type="warning"
                           @click="handleExport">匯出
                </el-button>
              </el-col>
              <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
            </el-row>

            <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column v-if="columns.userId.visible" key="userId" align="center" label="使用者編號"
                               prop="userId"/>
              <el-table-column v-if="columns.userName.visible" key="userName" :show-overflow-tooltip="true" align="center"
                               label="使用者名稱" prop="userName"/>
              <el-table-column v-if="columns.nickName.visible" key="nickName" :show-overflow-tooltip="true" align="center"
                               label="使用者暱稱" prop="nickName"/>
              <el-table-column v-if="columns.deptName.visible" key="deptName" :show-overflow-tooltip="true" align="center"
                               label="部門" prop="dept.deptName"/>
              <el-table-column v-if="columns.phonenumber.visible" key="phonenumber" align="center" label="手機號碼"
                               prop="phonenumber" width="120"/>
              <el-table-column v-if="columns.status.visible" key="status" align="center" label="狀態">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)"></el-switch>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.createTime.visible" align="center" label="建立時間" prop="createTime"
                               width="160">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
                <template slot-scope="scope" v-if="scope.row.userId !== 1">
                  <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']">修改</el-button>
                  <el-button v-hasPermi="['system:user:remove']" icon="el-icon-delete" size="mini" type="text"
                             @click="handleDelete(scope.row)">刪除
                  </el-button>
                  <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:user:resetPwd', 'system:user:edit']">
                    <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item v-hasPermi="['system:user:resetPwd']" command="handleResetPwd"
                                        icon="el-icon-key">重置密碼
                      </el-dropdown-item>
                      <el-dropdown-item command="handleAuthRole" icon="el-icon-circle-check" v-hasPermi="['system:user:edit']">分配角色</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>

            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
          </el-col>
        </pane>
      </splitpanes>
    </el-row>

    <!-- 新增或修改使用者配置對話框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="使用者暱稱" prop="nickName">
              <el-input v-model="form.nickName" maxlength="30" placeholder="請輸入使用者暱稱"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="歸屬部門" prop="deptId">
              <treeselect v-model="form.deptId" :options="enabledDeptOptions" :show-count="true"
                          placeholder="請選擇歸屬部門"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手機號碼" prop="phonenumber">
              <el-input v-model="form.phonenumber" maxlength="11" placeholder="請輸入手機號碼"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="信箱" prop="email">
              <el-input v-model="form.email" maxlength="50" placeholder="請輸入信箱"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="使用者名稱" prop="userName">
              <el-input v-model="form.userName" maxlength="30" placeholder="請輸入使用者名稱"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="使用者密碼" prop="password">
              <el-input v-model="form.password" maxlength="20" placeholder="請輸入使用者密碼" show-password
                        type="password"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="使用者性别">
              <el-select v-model="form.sex" placeholder="請選擇性别">
                <el-option v-for="dict in dict.type.sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="狀態">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="職位">
              <el-select v-model="form.postIds" multiple placeholder="請選擇職位">
                <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" :disabled="item.status == 1" ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple placeholder="請選擇角色">
                <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="備註">
              <el-input v-model="form.remark" placeholder="請輸入内容" type="textarea"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 使用者匯入對話框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload ref="upload" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">將文件拖到此處，或<em>點擊上傳</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport"/>
            是否更新已經存在的使用者數據
          </div>
          <span>僅允許匯入xls、xlsx格式文件。</span>
          <el-link :underline="false" style="font-size: 12px; vertical-align: baseline" type="primary"
                   @click="importTemplate">下載模板
          </el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">確 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addUser,
  changeUserStatus,
  delUser,
  deptTreeSelect,
  getUser,
  listUser,
  resetUserPwd,
  updateUser
} from "@/api/system/user"
import {getToken} from "@/utils/auth"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"
import {Pane, Splitpanes} from "splitpanes"
import "splitpanes/dist/splitpanes.css"

export default {
  name: "User",
  dicts: ['sys_normal_disable', 'sys_user_sex'],
  components: { Treeselect, Splitpanes, Pane },
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
      // 使用者表格數據
      userList: null,
      // 彈出層標題
      title: "",
      // 所有部門樹選項
      deptOptions: undefined,
      // 過濾掉已禁用部門樹選項
      enabledDeptOptions: undefined,
      // 是否顯示彈出層
      open: false,
      // 部門名稱
      deptName: undefined,
      // 預設密碼
      initPassword: undefined,
      // 日期範圍
      dateRange: [],
      // 職位選項
      postOptions: [],
      // 角色選項
      roleOptions: [],
      // 表單參數
      form: {},
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 使用者匯入參數
      upload: {
        // 是否顯示彈出層（使用者匯入）
        open: false,
        // 彈出層標題（使用者匯入）
        title: "",
        // 是否禁用上傳
        isUploading: false,
        // 是否更新已經存在的使用者數據
        updateSupport: 0,
        // 設定上傳的請求頭部
        headers: { Authorization: "Bearer " + getToken() },
        // 上傳的地址
        url: process.env.VUE_APP_BASE_API + "/system/user/importData"
      },
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phonenumber: undefined,
        status: undefined,
        deptId: undefined
      },
      // 列訊息
      columns: {
        userId: {label: '使用者編號', visible: true},
        userName: {label: '使用者名稱', visible: true},
        nickName: {label: '使用者暱稱', visible: true},
        deptName: {label: '部門', visible: true},
        phonenumber: {label: '手機號碼', visible: true},
        status: {label: '狀態', visible: true},
        createTime: {label: '建立時間', visible: true}
      },
      // 表單校驗
      rules: {
        userName: [
          {required: true, message: "使用者名稱不能為空", trigger: "blur"},
          {min: 2, max: 20, message: '使用者名稱長度必須介於 2 和 20 之間', trigger: 'blur'}
        ],
        nickName: [
          {required: true, message: "使用者暱稱不能為空", trigger: "blur"}
        ],
        password: [
          {required: true, message: "使用者密碼不能為空", trigger: "blur"},
          {min: 5, max: 20, message: '使用者密碼長度必須介於 5 和 20 之間', trigger: 'blur'},
          {pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字串：< > \" ' \\\ |", trigger: "blur"}
        ],
        email: [
          {
            type: "email",
            message: "請輸入正確的信箱地址",
            trigger: ["blur", "change"]
          }
        ],
        phonenumber: [
          {
            pattern: /^09\d{8}$/,
            message: "請輸入正確的手機號碼",
            trigger: "blur"
          }
        ]
      }
    }
  },
  watch: {
    // 根據名稱筛選部門樹
    deptName(val) {
      this.$refs.tree.filter(val)
    }
  },
  created() {
    this.getList()
    this.getDeptTree()
    this.getConfigKey("sys.user.initPassword").then(response => {
      this.initPassword = response.msg
    })
  },
  methods: {
    /** 查詢使用者列表 */
    getList() {
      this.loading = true
      listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.userList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    /** 查詢部門下拉樹結構 */
    getDeptTree() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data
        this.enabledDeptOptions = this.filterDisabledDept(JSON.parse(JSON.stringify(response.data)))
      })
    },
    // 過濾禁用的部門
    filterDisabledDept(deptList) {
      return deptList.filter(dept => {
        if (dept.disabled) {
          return false
        }
        if (dept.children && dept.children.length) {
          dept.children = this.filterDisabledDept(dept.children)
        }
        return true
      })
    },
    // 筛選節點
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    // 節點單擊事件
    handleNodeClick(data) {
      this.queryParams.deptId = data.id
      this.handleQuery()
    },
    // 使用者狀態修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "啟用" : "停用"
      this.$modal.confirm('確認要"' + text + '""' + row.userName + '"使用者吗？').then(function () {
        return changeUserStatus(row.userId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
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
        userId: undefined,
        deptId: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        phonenumber: undefined,
        email: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
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
      this.queryParams.deptId = undefined
      this.$refs.tree.setCurrentKey(null)
      this.handleQuery()
    },
    // 多選框選中數據
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    // 更多操作觸發
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row)
          break
        case "handleAuthRole":
          this.handleAuthRole(row)
          break
        default:
          break
      }
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset()
      getUser().then(response => {
        this.postOptions = response.posts
        this.roleOptions = response.roles
        this.open = true
        this.title = "新增使用者"
        this.form.password = this.initPassword
      })
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset()
      const userId = row.userId || this.ids
      getUser(userId).then(response => {
        this.form = response.data
        this.postOptions = response.posts
        this.roleOptions = response.roles
        this.$set(this.form, "postIds", response.postIds)
        this.$set(this.form, "roleIds", response.roleIds)
        this.open = true
        this.title = "修改使用者"
        this.form.password = ""
      })
    },
    /** 重置密碼按鈕操作 */
    handleResetPwd(row) {
      this.$prompt('請輸入"' + row.userName + '"的新密碼', "提示", {
        confirmButtonText: "確定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "使用者密碼長度必須介於 5 和 20 之間",
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "不能包含非法字串：< > \" ' \\\ |"
          }
        },
      }).then(({ value }) => {
          resetUserPwd(row.userId, value).then(response => {
            this.$modal.msgSuccess("修改成功，新密碼是：" + value)
          })
        }).catch(() => {})
    },
    /** 分配角色操作 */
    handleAuthRole: function(row) {
      const userId = row.userId
      this.$router.push("/system/user-auth/role/" + userId)
    },
    /** 提交按鈕 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != undefined) {
            updateUser(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addUser(this.form).then(response => {
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
      const userIds = row.userId || this.ids
      this.$modal.confirm('是否確認刪除使用者編號為"' + userIds + '"的數據項？').then(function () {
        return delUser(userIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("刪除成功")
      }).catch(() => {})
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('system/user/export', {
        ...this.queryParams
      }, `user_${new Date().getTime()}.xlsx`)
    },
    /** 匯入按鈕操作 */
    handleImport() {
      this.upload.title = "使用者匯入"
      this.upload.open = true
    },
    /** 下載模板操作 */
    importTemplate() {
      this.download('system/user/importTemplate', {
      }, `user_template_${new Date().getTime()}.xlsx`)
    },
    // 文件上傳中處理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true
    },
    // 文件上傳成功處理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false
      this.upload.isUploading = false
      this.$refs.upload.clearFiles()
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "匯入結果", {dangerouslyUseHTMLString: true})
      this.getList()
    },
    // 提交上傳文件
    submitFileForm() {
      const file = this.$refs.upload.uploadFiles
      if (!file || file.length === 0 || !file[0].name.toLowerCase().endsWith('.xls') && !file[0].name.toLowerCase().endsWith('.xlsx')) {
        this.$modal.msgError("請選擇後綴為 “xls”或“xlsx”的文件。")
        return
      }
      this.$refs.upload.submit()
    }
  }
}
</script>
