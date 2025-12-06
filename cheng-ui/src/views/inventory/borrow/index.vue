<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="物品名稱" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="請輸入物品名稱"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="借用人" prop="borrowerName" v-hasPermi="['inventory:borrow:all']">
        <el-input
          v-model="queryParams.borrowerName"
          placeholder="請輸入借用人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="借出狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="請選擇借出狀態" clearable style="width: 200px">
          <el-option label="待審核" value="0"/>
          <el-option label="已借出" value="1"/>
          <el-option label="審核拒絕" value="2"/>
          <el-option label="已歸還" value="3"/>
          <el-option label="部分歸還" value="4"/>
          <el-option label="逾期" value="5"/>
        </el-select>
      </el-form-item>
      <el-form-item label="借出時間">
        <el-date-picker
          v-model="daterangeBorrow"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="開始日期"
          end-placeholder="結束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜尋</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['inventory:borrow:add']"
        >新增借出
        </el-button>
      </el-col>
      <!-- 移除頂部修改和刪除按鈕，只保留新增 -->
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['inventory:borrow:export']"
        >匯出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
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
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="借出單號" align="center" prop="borrowNo" width="180" :show-overflow-tooltip="true"/>
      <el-table-column label="物品名稱" align="center" prop="itemName"/>
      <el-table-column label="物品編碼" align="center" prop="itemCode"/>
      <el-table-column label="借出數量" align="center" prop="quantity"/>
      <el-table-column label="借用人" align="center" prop="borrowerName"/>
      <el-table-column label="借用目的" align="center" prop="purpose" show-overflow-tooltip/>
      <el-table-column label="借出時間" align="center" prop="borrowTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.borrowTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="預計歸還" align="center" prop="expectedReturn" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.expectedReturn, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="實際歸還" align="center" prop="actualReturn" width="180">
        <template #default="scope">
          <span v-if="scope.row.actualReturn">{{ parseTime(scope.row.actualReturn, '{y}-{m}-{d} {h}:{i}') }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="狀態" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <!-- 只有待審核狀態可以修改，但預約記錄除外 -->
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-if="scope.row.status === '0' && scope.row.reserveStatus !== 1"
            v-hasPermi="['inventory:borrow:edit']"
          >修改
          </el-button>
          <!-- 只有待審核狀態顯示審核按鈕 -->
          <el-button
            link
            type="primary"
            icon="Check"
            @click="handleApprove(scope.row)"
            v-if="scope.row.status === '0'"
            v-hasPermi="['inventory:borrow:approve']"
          >審核
          </el-button>
          <!-- 已借出、部分歸還、逾期狀態可以歸還（只有借出人本人或管理員）-->
          <el-button
            link
            type="primary"
            icon="RefreshLeft"
            @click="handleReturn(scope.row)"
            v-if="(scope.row.status === '1' || scope.row.status === '4' || scope.row.status === '5') && canReturn(scope.row)"
            v-hasPermi="['inventory:borrow:return']"
          >歸還
          </el-button>
          <!-- 已歸還或部分歸還狀態顯示查看歸還記錄按鈕 -->
          <el-button
            link
            type="primary"
            icon="Document"
            @click="handleViewReturnRecords(scope.row)"
            v-if="scope.row.status === '3' || scope.row.status === '4'"
            v-hasPermi="['inventory:borrow:query']"
          >歸還記錄
          </el-button>
          <!-- 移除刪除按鈕，借出記錄應完整保留 -->
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

    <!-- 新增或修改借出記錄對話框 -->
    <el-dialog :title="title" :model-value="open" @update:model-value="val => open = val" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="物品" prop="itemId">
          <el-select
            v-model="form.itemId"
            placeholder="🔍 可輸入物品名稱或編碼搜尋"
            filterable
            clearable
            style="width: 100%"
            @change="handleItemChange"
            :loading="itemLoading"
            loading-text="載入中..."
            no-data-text="無可用物品"
            no-match-text="找不到符合的物品">
            <el-option
              v-for="item in itemOptions"
              :key="item.itemId"
              :label="item.itemName + ' (' + item.itemCode + ')'"
              :value="item.itemId"
              :disabled="!item.availableQuantity || item.availableQuantity <= 0">
              <span style="float: left">{{ item.itemName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ item.itemCode }} | 庫存: {{ item.availableQuantity || 0 }}
              </span>
            </el-option>
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            💡 提示：點擊後直接輸入文字即可快速搜尋物品
          </div>
        </el-form-item>
        <el-form-item label="可用數量" v-if="form.itemId">
          <el-input v-model="selectedItem.availableQuantity" disabled/>
        </el-form-item>
        <el-form-item label="借出數量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="selectedItem.availableQuantity" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="借用人" prop="borrowerName">
          <el-input v-model="form.borrowerName" placeholder="請輸入借用人"/>
        </el-form-item>
        <el-form-item label="借用目的" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" placeholder="請輸入借用目的"/>
        </el-form-item>
        <el-form-item label="預計歸還" prop="expectedReturn">
          <el-date-picker
            v-model="form.expectedReturn"
            type="datetime"
            placeholder="選擇預計歸還時間"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="form.remark" type="textarea" placeholder="請輸入備註"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 審核對話框 -->
    <el-dialog title="審核借出申請" :model-value="approveOpen" @update:model-value="val => approveOpen = val" width="500px" append-to-body>
      <el-form ref="approveForm" :model="approveForm" label-width="100px">
        <el-form-item label="借用人">
          <el-input v-model="approveForm.borrowerName" disabled/>
        </el-form-item>
        <el-form-item label="借用物品">
          <el-input v-model="approveForm.itemName" disabled/>
        </el-form-item>
        <el-form-item label="借用數量">
          <el-input-number v-model="approveForm.quantity" disabled style="width: 100%"/>
        </el-form-item>
        <el-form-item label="審核結果">
          <el-radio-group v-model="approveForm.approved">
            <el-radio :label="true">通過</el-radio>
            <el-radio :label="false">拒絕</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="審核意見">
          <el-input v-model="approveForm.remark" type="textarea" placeholder="請輸入審核意見"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitApprove">確 定</el-button>
        <el-button @click="cancelApprove">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 歸還對話框 -->
    <el-dialog title="物品歸還" :model-value="returnOpen" @update:model-value="val => returnOpen = val" width="400px" append-to-body>
      <el-form ref="returnForm" :model="returnForm" label-width="80px">
        <el-form-item label="歸還數量">
          <el-input-number v-model="returnForm.quantity" :min="1" :max="returnForm.maxQuantity" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="物品狀態">
          <el-radio-group v-model="returnForm.condition">
            <el-radio label="good">完好</el-radio>
            <el-radio label="damaged">損壞</el-radio>
            <el-radio label="lost">遺失</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="歸還說明">
          <el-input v-model="returnForm.remark" type="textarea" placeholder="請輸入歸還說明"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReturn">確 定</el-button>
        <el-button @click="cancelReturn">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 歸還記錄對話框 -->
    <el-dialog title="歸還記錄" :model-value="returnRecordsOpen" @update:model-value="val => returnRecordsOpen = val" width="800px" append-to-body>
      <el-table :data="returnRecords" style="width: 100%">
        <el-table-column label="歸還時間" align="center" prop="returnTime" width="160"/>
        <el-table-column label="歸還數量" align="center" prop="returnQuantity" width="100"/>
        <el-table-column label="物品狀態" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.itemCondition === 'good'" type="success">完好</el-tag>
            <el-tag v-else-if="scope.row.itemCondition === 'damaged'" type="warning">損壞</el-tag>
            <el-tag v-else-if="scope.row.itemCondition === 'lost'" type="danger">遺失</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否逾期" align="center" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isOverdue === '1'" type="danger">逾期 {{ scope.row.overdueDays }} 天</el-tag>
            <el-tag v-else type="success">準時</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="說明" align="center" min-width="150" :show-overflow-tooltip="true">
          <template #default="scope">
            {{ scope.row.damageDescription || scope.row.remark || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="returnRecordsOpen = false">關 閉</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listBorrow,
  getBorrow,
  delBorrow,
  addBorrow,
  updateBorrow,
  approveBorrow,
  returnBorrow,
  lostItem,
  getBorrowStats,
  getReturnRecords
} from "@/api/inventory/borrow";
import {listManagement} from "@/api/inventory/management";
import { mapState } from 'pinia';
import useUserStore from '@/store/modules/user';

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
      // 物品載入狀態
      itemLoading: false,
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
      // 歸還記錄對話框
      returnRecordsOpen: false,
      // 歸還記錄資料
      returnRecords: [],
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
          {required: true, message: "物品不能為空", trigger: "change"}
        ],
        quantity: [
          {required: true, message: "借出數量不能為空", trigger: "blur"}
        ],
        borrowerName: [
          {required: true, message: "借用人不能為空", trigger: "blur"}
        ],
        purpose: [
          {required: true, message: "借用目的不能為空", trigger: "blur"}
        ],
        expectedReturn: [
          {required: true, message: "預計歸還時間不能為空", trigger: "blur"}
        ]
      },
      // 時間選擇器配置：只能選擇未來時間
      disabledDate(time) {
        return time.getTime() < Date.now() - 8.64e7; // 禁用今天之前的日期
      }
    };
  },
  computed: {
    ...mapState(useUserStore, ['id', 'roles']),
    /** 判斷當前使用者是否為管理員 */
    isAdmin() {
      return this.roles && this.roles.includes('admin');
    }
  },
  created() {
    this.getList();
    this.getItemList();
    this.getBorrowStatistics();
  },
  activated() {
    this.getList();
    this.getItemList();
    this.getBorrowStatistics();
  },
  methods: {
    /** 判斷是否可以歸還（只有借出人本人或管理員可以歸還）*/
    canReturn(row) {
      // 管理員可以歸還任何人的借出
      if (this.isAdmin) {
        return true;
      }
      // 借出人本人可以歸還自己的借出
      return row.borrowerId === this.id;
    },
    /** 查詢借出記錄列表 */
    getList() {
      this.loading = true;
      // 檢查日期範圍是否有效
      if (this.daterangeBorrow && this.daterangeBorrow.length === 2) {
        this.queryParams.beginBorrowTime = this.daterangeBorrow[0];
        this.queryParams.endBorrowTime = this.daterangeBorrow[1];
      } else {
        // 清除日期範圍參數
        this.queryParams.beginBorrowTime = null;
        this.queryParams.endBorrowTime = null;
      }
      listBorrow(this.queryParams).then(response => {
        this.borrowList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 查詢物品列表 */
    getItemList() {
      this.itemLoading = true;
      // 使用 listManagement API 取得物品和庫存資訊
      listManagement({
        status: '0',  // 只查詢正常狀態的物品
        pageNum: 1,
        pageSize: 1000  // 取得所有物品
      }).then(response => {
        const items = response.rows || [];
        // 只顯示有可用庫存的物品，並按物品名稱排序
        this.itemOptions = items
          .filter(item => item.availableQty && item.availableQty > 0)
          .map(item => ({
            itemId: item.itemId,
            itemCode: item.itemCode,
            itemName: item.itemName,
            totalQuantity: item.totalQuantity,
            availableQuantity: item.availableQty,  // 使用 availableQty
            location: item.location
          }))
          .sort((a, b) => {
            const nameA = a.itemName || '';
            const nameB = b.itemName || '';
            return nameA.localeCompare(nameB, 'zh-CN');
          });
        this.itemLoading = false;
      }).catch(error => {
        console.error('載入物品列表失敗:', error);
        this.$modal.msgError('載入物品列表失敗，請稍後再試');
        this.itemLoading = false;
      });
    },
    /** 取得借出統計 */
    getBorrowStatistics() {
      // 使用相同的搜尋條件過濾統計結果
      const statsQuery = {
        itemName: this.queryParams.itemName,
        borrowerName: this.queryParams.borrowerName,
        beginBorrowTime: this.queryParams.beginBorrowTime,
        endBorrowTime: this.queryParams.endBorrowTime
      };
      getBorrowStats(statsQuery).then(response => {
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
        borrowNo: null,  // 清除借出單號
        itemId: null,
        quantity: 1,  // 重置為預設值
        borrowerName: null,
        purpose: null,
        expectedReturn: null,
        remark: null
      };
      this.selectedItem = {};  // 清除選中的物品
      this.resetForm("form");
    },
    /** 搜尋按鈕操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
      // 同時更新統計數字
      this.getBorrowStatistics();
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按鈕操作 */
    handleAdd() {
      console.log('[借出管理調試] 點擊新增按鈕');
      this.reset();
      this.open = true;
      this.title = "新增借出記錄";
      console.log('[借出管理調試] dialog 開啟:', this.open);
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      console.log('[借出管理調試] 點擊修改按鈕', row);
      this.reset();
      const borrowId = row.borrowId || this.ids
      console.log('[借出管理調試] borrowId:', borrowId);
      getBorrow(borrowId).then(response => {
        console.log('[借出管理調試] 取得借出資料:', response.data);
        this.form = response.data;
        this.open = true;
        this.title = "修改借出記錄";
        console.log('[借出管理調試] dialog 開啟:', this.open);
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
      this.$modal.confirm('是否確認刪除借出記錄編號為"' + borrowIds + '"的資料選項？').then(function () {
        return delBorrow(borrowIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("刪除成功");
      }).catch(() => {
      });
    },
    /** 審核按鈕操作 */
    handleApprove(row) {
      this.approveForm = {
        borrowId: row.borrowId,
        borrowerName: row.borrowerName,
        itemName: row.itemName,
        quantity: row.quantity,
        approved: true,
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
      // 計算未歸還數量
      const remainingQty = row.quantity - (row.returnQuantity || 0);
      this.returnForm = {
        borrowId: row.borrowId,
        quantity: remainingQty,
        maxQuantity: remainingQty,
        condition: 'good',
        remark: ''
      };
      this.returnOpen = true;
    },
    /** 提交歸還 */
    submitReturn() {
      // 如果是遺失，跳出二次確認
      if (this.returnForm.condition === 'lost') {
        this.$confirm('確定要將此物品標記為遺失嗎？此操作將記錄在操作日誌中。', '遺失確認', {
          confirmButtonText: '確定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 確認後調用遺失物品 API
          const requestData = {
            borrowId: this.returnForm.borrowId,
            returnQuantity: this.returnForm.quantity,
            remark: this.returnForm.remark
          };

          lostItem(requestData).then(response => {
            this.$modal.msgSuccess("已記錄為遺失");
            this.returnOpen = false;
            this.getList();
            this.getBorrowStatistics();
            this.getItemList();
          }).catch(error => {
            console.error('遺失記錄失敗:', error);
          });
        }).catch(() => {
          // 取消遺失
        });
        return;
      }

      // 一般歸還或損壞歸還
      const requestData = {
        borrowId: this.returnForm.borrowId,
        returnQuantity: this.returnForm.quantity,
        conditionDesc: this.returnForm.condition, // 傳遞物品狀態：good/damaged/lost
        isDamaged: this.returnForm.condition === 'damaged' ? '1' : '0',
        damageDesc: this.returnForm.condition === 'damaged' ? this.returnForm.remark : null,
        remark: this.returnForm.remark // 所有狀態都傳遞說明
      };

      returnBorrow(requestData).then(response => {
        this.$modal.msgSuccess("歸還成功");
        this.returnOpen = false;
        this.getList();
        this.getBorrowStatistics();
        // 重新載入物品列表，更新可用數量（特別是損壞物品的情況）
        this.getItemList();
      }).catch(error => {
        console.error('歸還失敗:', error);
      });
    },
    /** 取消歸還 */
    cancelReturn() {
      this.returnOpen = false;
      this.returnForm = {};
    },
    /** 查看歸還記錄 */
    handleViewReturnRecords(row) {
      getReturnRecords(row.borrowId).then(response => {
        this.returnRecords = response.data || [];
        this.returnRecordsOpen = true;
      }).catch(error => {
        console.error('查詢歸還記錄失敗:', error);
        this.$modal.msgError('查詢歸還記錄失敗');
      });
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
    /** 取得狀態類型 */
    getStatusType(status) {
      const statusMap = {
        '0': 'info',     // 待審核
        '1': 'primary',  // 已借出
        '2': 'danger',   // 審核拒絕
        '3': 'success',  // 已歸還
        '4': 'warning',  // 部分歸還
        '5': 'danger'    // 逾期
      };
      return statusMap[status] || 'info';
    },
    /** 取得狀態文字 */
    getStatusText(status) {
      const statusMap = {
        '0': '待審核',
        '1': '已借出',
        '2': '審核拒絕',
        '3': '已歸還',
        '4': '部分歸還',
        '5': '逾期'
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
