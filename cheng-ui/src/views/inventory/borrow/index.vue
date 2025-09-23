<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="物品名稱" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="請輸入物品名稱"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="借用人" prop="borrowerName">
        <el-input
          v-model="queryParams.borrowerName"
          placeholder="請輸入借用人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="借出狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇借出狀態" clearable>
          <el-option label="待審核" value="0" />
          <el-option label="已借出" value="1" />
          <el-option label="已歸還" value="2" />
          <el-option label="已拒絕" value="3" />
          <el-option label="逾期" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="借出時間">
        <el-date-picker
          v-model="daterangeBorrow"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="開始日期"
          end-placeholder="結束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['inventory:borrow:add']"
        >新增借出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['inventory:borrow:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['inventory:borrow:remove']"
        >刪除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['inventory:borrow:export']"
        >匯出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-warning"
          size="mini"
          @click="handleOverdue"
        >逾期提醒</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 借出統計卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>待審核</span>
            <i class="el-icon-time el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number text-warning">{{ borrowStats.pending }}</div>
            <div class="desc">筆</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>已借出</span>
            <i class="el-icon-check el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number text-primary">{{ borrowStats.borrowed }}</div>
            <div class="desc">筆</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>已歸還</span>
            <i class="el-icon-success el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number text-success">{{ borrowStats.returned }}</div>
            <div class="desc">筆</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>逾期未還</span>
            <i class="el-icon-error el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number text-danger">{{ borrowStats.overdue }}</div>
            <div class="desc">筆</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="borrowList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="借出編號" align="center" prop="borrowCode" />
      <el-table-column label="物品名稱" align="center" prop="itemName" />
      <el-table-column label="物品編碼" align="center" prop="itemCode" />
      <el-table-column label="借出數量" align="center" prop="quantity" />
      <el-table-column label="借用人" align="center" prop="borrowerName" />
      <el-table-column label="借用目的" align="center" prop="purpose" show-overflow-tooltip />
      <el-table-column label="借出時間" align="center" prop="borrowTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.borrowTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="預計歸還" align="center" prop="expectedReturn" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.expectedReturn, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="實際歸還" align="center" prop="actualReturn" width="180">
        <template slot-scope="scope">
          <span v-if="scope.row.actualReturn">{{ parseTime(scope.row.actualReturn, '{y}-{m}-{d} {h}:{i}') }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="狀態" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['inventory:borrow:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-if="scope.row.status === '0'"
            v-hasPermi="['inventory:borrow:approve']"
          >審核</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            @click="handleReturn(scope.row)"
            v-if="scope.row.status === '1'"
            v-hasPermi="['inventory:borrow:return']"
          >歸還</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['inventory:borrow:remove']"
          >刪除</el-button>
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

    <!-- 添加或修改借出記錄對話框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="物品" prop="itemId">
          <el-select v-model="form.itemId" placeholder="請選擇物品" @change="handleItemChange">
            <el-option
              v-for="item in itemOptions"
              :key="item.itemId"
              :label="item.itemName + ' (' + item.itemCode + ')'"
              :value="item.itemId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="可用數量" v-if="form.itemId">
          <el-input v-model="selectedItem.availableQuantity" disabled />
        </el-form-item>
        <el-form-item label="借出數量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="selectedItem.availableQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="借用人" prop="borrowerName">
          <el-input v-model="form.borrowerName" placeholder="請輸入借用人" />
        </el-form-item>
        <el-form-item label="借用目的" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" placeholder="請輸入借用目的" />
        </el-form-item>
        <el-form-item label="預計歸還" prop="expectedReturn">
          <el-date-picker
            v-model="form.expectedReturn"
            type="datetime"
            placeholder="選擇預計歸還時間"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="form.remark" type="textarea" placeholder="請輸入備註" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 審核對話框 -->
    <el-dialog title="審核借出申請" :visible.sync="approveOpen" width="400px" append-to-body>
      <el-form ref="approveForm" :model="approveForm" label-width="80px">
        <el-form-item label="審核結果">
          <el-radio-group v-model="approveForm.result">
            <el-radio label="1">通過</el-radio>
            <el-radio label="3">拒絕</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="審核意見">
          <el-input v-model="approveForm.remark" type="textarea" placeholder="請輸入審核意見" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitApprove">確 定</el-button>
        <el-button @click="cancelApprove">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 歸還對話框 -->
    <el-dialog title="物品歸還" :visible.sync="returnOpen" width="400px" append-to-body>
      <el-form ref="returnForm" :model="returnForm" label-width="80px">
        <el-form-item label="歸還數量">
          <el-input-number v-model="returnForm.quantity" :min="1" :max="returnForm.maxQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="物品狀態">
          <el-radio-group v-model="returnForm.condition">
            <el-radio label="good">完好</el-radio>
            <el-radio label="damaged">損壞</el-radio>
            <el-radio label="lost">遺失</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="歸還說明">
          <el-input v-model="returnForm.remark" type="textarea" placeholder="請輸入歸還說明" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReturn">確 定</el-button>
        <el-button @click="cancelReturn">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listBorrow, getBorrow, delBorrow, addBorrow, updateBorrow, approveBorrow, returnBorrow, getBorrowStats } from "@/api/inventory/borrow";
import { listItem } from "@/api/inventory/item";

export default {
  name: "Borrow",
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
      // 借出記錄表格資料
      borrowList: [],
      // 物品選項
      itemOptions: [],
      // 選中的物品
      selectedItem: {},
      // 借出統計
      borrowStats: {
        pending: 0,
        borrowed: 0,
        returned: 0,
        overdue: 0
      },
      // 彈出層標題
      title: "",
      // 是否顯示彈出層
      open: false,
      // 審核對話框
      approveOpen: false,
      // 歸還對話框
      returnOpen: false,
      // 日期範圍
      daterangeBorrow: [],
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        itemName: null,
        borrowerName: null,
        status: null,
        beginBorrowTime: null,
        endBorrowTime: null
      },
      // 表單參數
      form: {},
      // 審核表單
      approveForm: {},
      // 歸還表單
      returnForm: {},
      // 表單校驗
      rules: {
        itemId: [
          { required: true, message: "物品不能為空", trigger: "change" }
        ],
        quantity: [
          { required: true, message: "借出數量不能為空", trigger: "blur" }
        ],
        borrowerName: [
          { required: true, message: "借用人不能為空", trigger: "blur" }
        ],
        purpose: [
          { required: true, message: "借用目的不能為空", trigger: "blur" }
        ],
        expectedReturn: [
          { required: true, message: "預計歸還時間不能為空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getItemList();
    this.getBorrowStatistics();
  },
  methods: {
    /** 查詢借出記錄列表 */
    getList() {
      this.loading = true;
      if (null != this.daterangeBorrow && '' != this.daterangeBorrow) {
        this.queryParams.beginBorrowTime = this.daterangeBorrow[0];
        this.queryParams.endBorrowTime = this.daterangeBorrow[1];
      }
      listBorrow(this.queryParams).then(response => {
        this.borrowList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 查詢物品列表 */
    getItemList() {
      listItem().then(response => {
        this.itemOptions = response.data;
      });
    },
    /** 獲取借出統計 */
    getBorrowStatistics() {
      getBorrowStats().then(response => {
        this.borrowStats = response.data;
      });
    },
    /** 取消按鈕 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表單重置 */
    reset() {
      this.form = {
        borrowId: null,
        itemId: null,
        quantity: null,
        borrowerName: null,
        purpose: null,
        expectedReturn: null,
        remark: null
      };
      this.resetForm("form");
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按鈕操作 */
    resetQuery() {
      this.daterangeBorrow = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多選框選中資料
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.borrowId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加借出記錄";
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset();
      const borrowId = row.borrowId || this.ids
      getBorrow(borrowId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改借出記錄";
      });
    },
    /** 物品選擇變化 */
    handleItemChange(itemId) {
      this.selectedItem = this.itemOptions.find(item => item.itemId === itemId) || {};
    },
    /** 提交按鈕 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.borrowId != null) {
            updateBorrow(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addBorrow(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
              this.getBorrowStatistics();
            });
          }
        }
      });
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const borrowIds = row.borrowId || this.ids;
      this.$modal.confirm('是否確認刪除借出記錄編號為"' + borrowIds + '"的資料項？').then(function() {
        return delBorrow(borrowIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("刪除成功");
      }).catch(() => {});
    },
    /** 審核按鈕操作 */
    handleApprove(row) {
      this.approveForm = {
        borrowId: row.borrowId,
        result: '1',
        remark: ''
      };
      this.approveOpen = true;
    },
    /** 提交審核 */
    submitApprove() {
      approveBorrow(this.approveForm).then(response => {
        this.$modal.msgSuccess("審核成功");
        this.approveOpen = false;
        this.getList();
        this.getBorrowStatistics();
      });
    },
    /** 取消審核 */
    cancelApprove() {
      this.approveOpen = false;
      this.approveForm = {};
    },
    /** 歸還按鈕操作 */
    handleReturn(row) {
      this.returnForm = {
        borrowId: row.borrowId,
        quantity: row.quantity,
        maxQuantity: row.quantity,
        condition: 'good',
        remark: ''
      };
      this.returnOpen = true;
    },
    /** 提交歸還 */
    submitReturn() {
      returnBorrow(this.returnForm).then(response => {
        this.$modal.msgSuccess("歸還成功");
        this.returnOpen = false;
        this.getList();
        this.getBorrowStatistics();
      });
    },
    /** 取消歸還 */
    cancelReturn() {
      this.returnOpen = false;
      this.returnForm = {};
    },
    /** 逾期提醒 */
    handleOverdue() {
      this.$router.push('/inventory/borrow/overdue');
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('inventory/borrow/export', {
        ...this.queryParams
      }, `borrow_${new Date().getTime()}.xlsx`)
    },
    /** 獲取狀態類型 */
    getStatusType(status) {
      const statusMap = {
        '0': 'info',     // 待審核
        '1': 'primary',  // 已借出
        '2': 'success',  // 已歸還
        '3': 'danger',   // 已拒絕
        '4': 'warning'   // 逾期
      };
      return statusMap[status] || 'info';
    },
    /** 獲取狀態文字 */
    getStatusText(status) {
      const statusMap = {
        '0': '待審核',
        '1': '已借出',
        '2': '已歸還',
        '3': '已拒絕',
        '4': '逾期'
      };
      return statusMap[status] || '未知';
    }
  }
};
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.card-content {
  text-align: center;
  padding: 10px 0;
}

.card-content .number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-content .desc {
  font-size: 12px;
  color: #909399;
}

.text-primary {
  color: #409eff;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}
</style>
