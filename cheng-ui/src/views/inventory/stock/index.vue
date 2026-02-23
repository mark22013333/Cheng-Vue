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
      <el-form-item label="物品編碼" prop="itemCode">
        <el-input
          v-model="queryParams.itemCode"
          placeholder="請輸入物品編碼"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分類" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="請選擇分類" clearable>
          <el-option
            v-for="category in categoryOptions"
            :key="category.categoryId"
            :label="category.categoryName"
            :value="category.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="庫存狀態" prop="stockStatus">
        <el-select v-model="queryParams.stockStatus" placeholder="請選擇庫存狀態" clearable>
          <el-option label="正常" value="normal" />
          <el-option label="低庫存" value="low" />
          <el-option label="缺貨" value="out" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleStockIn"
          v-hasPermi="[INVENTORY_STOCK_ADD]"
        >入庫</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleStockOut"
          v-hasPermi="[INVENTORY_STOCK_EDIT]"
        >出庫</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="[INVENTORY_STOCK_EXPORT]"
        >匯出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-view"
          size="small"
          @click="handleStockCheck"
          v-hasPermi="[INVENTORY_STOCK_CHECK]"
        >盤點</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 庫存統計卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>總物品數</span>
            <i class="el-icon-goods el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number">{{ stockStats.totalItems }}</div>
            <div class="desc">種類</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>總庫存量</span>
            <i class="el-icon-box el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number">{{ stockStats.totalQuantity }}</div>
            <div class="desc">件</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>低庫存物品</span>
            <i class="el-icon-warning el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number text-warning">{{ stockStats.lowStockItems }}</div>
            <div class="desc">種類</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="card-header">
            <span>缺貨物品</span>
            <i class="el-icon-error el-icon--right"></i>
          </div>
          <div class="card-content">
            <div class="number text-danger">{{ stockStats.outOfStockItems }}</div>
            <div class="desc">種類</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="stockList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="物品編碼" align="center" prop="itemCode" />
      <el-table-column label="物品名稱" align="center" prop="itemName" />
      <el-table-column label="分類" align="center" prop="categoryName" />
      <el-table-column label="規格" align="center" prop="specification" />
      <el-table-column label="單位" align="center" prop="unit" />
      <el-table-column label="庫存數量" align="center" prop="stockQuantity">
        <template #default="scope">
          <span :class="getStockClass(scope.row)">{{ scope.row.stockQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="可用數量" align="center" prop="availableQuantity" />
      <el-table-column label="借出數量" align="center" prop="borrowedQuantity" />
      <el-table-column label="最小庫存" align="center" prop="minStock" />
      <el-table-column label="最大庫存" align="center" prop="maxStock" />
      <el-table-column label="存放位置" align="center" prop="location" />
      <el-table-column label="庫存狀態" align="center" prop="stockStatus">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row)">
            {{ getStatusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-plus"
            @click="handleStockInItem(scope.row)"
            v-hasPermi="[INVENTORY_STOCK_ADD]"
          >入庫</el-button>
          <el-button
            size="small"
            type="text"
            icon="el-icon-minus"
            @click="handleStockOutItem(scope.row)"
            v-hasPermi="[INVENTORY_STOCK_EDIT]"
          >出庫</el-button>
          <el-button
            size="small"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
          >詳情</el-button>
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

    <!-- 入庫對話框 -->
    <el-dialog title="入庫操作" :model-value="stockInOpen" @update:model-value="val => stockInOpen = val" width="500px" append-to-body>
      <el-form ref="stockInForm" :model="stockInForm" :rules="stockInRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="stockInForm.itemName" disabled />
        </el-form-item>
        <el-form-item label="入庫數量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入庫原因" prop="reason">
          <el-input v-model="stockInForm.reason" type="textarea" placeholder="請輸入入庫原因" />
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="stockInForm.remark" type="textarea" placeholder="請輸入備註" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStockIn">確 定</el-button>
        <el-button @click="cancelStockIn">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 出庫對話框 -->
    <el-dialog title="出庫操作" :model-value="stockOutOpen" @update:model-value="val => stockOutOpen = val" width="500px" append-to-body>
      <el-form ref="stockOutForm" :model="stockOutForm" :rules="stockOutRules" label-width="80px">
        <el-form-item label="物品名稱">
          <el-input v-model="stockOutForm.itemName" disabled />
        </el-form-item>
        <el-form-item label="可用數量">
          <el-input v-model="stockOutForm.availableQuantity" disabled />
        </el-form-item>
        <el-form-item label="出庫數量" prop="quantity">
          <el-input-number v-model="stockOutForm.quantity" :min="1" :max="stockOutForm.availableQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="出庫原因" prop="reason">
          <el-input v-model="stockOutForm.reason" type="textarea" placeholder="請輸入出庫原因" />
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="stockOutForm.remark" type="textarea" placeholder="請輸入備註" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStockOut">確 定</el-button>
        <el-button @click="cancelStockOut">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 盤點對話框 -->
    <el-dialog title="庫存盤點" :model-value="stockCheckOpen" @update:model-value="val => stockCheckOpen = val" width="800px" append-to-body>
      <el-form ref="stockCheckForm" :model="stockCheckForm" label-width="100px">
        <el-form-item label="盤點範圍">
          <el-radio-group v-model="stockCheckForm.checkType">
            <el-radio label="all">全部物品</el-radio>
            <el-radio label="category">按分類</el-radio>
            <el-radio label="location">按位置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="選擇分類" v-if="stockCheckForm.checkType === 'category'">
          <el-select v-model="stockCheckForm.categoryId" placeholder="請選擇分類">
            <el-option
              v-for="category in categoryOptions"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="存放位置" v-if="stockCheckForm.checkType === 'location'">
          <el-input v-model="stockCheckForm.location" placeholder="請輸入存放位置" />
        </el-form-item>
        <el-form-item label="盤點說明">
          <el-input v-model="stockCheckForm.remark" type="textarea" placeholder="請輸入盤點說明" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStockCheck">開始盤點</el-button>
        <el-button @click="cancelStockCheck">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  INVENTORY_STOCK_ADD,
  INVENTORY_STOCK_CHECK,
  INVENTORY_STOCK_EDIT,
  INVENTORY_STOCK_EXPORT
} from '@/constants/permissions'
import { listStock, getStock, stockIn, stockOut, stockCheck, exportStock, getStockStats } from "@/api/inventory/stock";
import { listCategory } from "@/api/inventory/category";

export default {
  setup() {
    return { INVENTORY_STOCK_ADD, INVENTORY_STOCK_CHECK, INVENTORY_STOCK_EDIT, INVENTORY_STOCK_EXPORT }
  },
  name: "Stock",
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
      // 庫存表格資料
      stockList: [],
      // 分類選項
      categoryOptions: [],
      // 庫存統計
      stockStats: {
        totalItems: 0,
        totalQuantity: 0,
        lowStockItems: 0,
        outOfStockItems: 0
      },
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        itemName: null,
        itemCode: null,
        categoryId: null,
        stockStatus: null
      },
      // 入庫對話框
      stockInOpen: false,
      stockInForm: {},
      stockInRules: {
        quantity: [
          { required: true, message: "入庫數量不能為空", trigger: "blur" }
        ],
        reason: [
          { required: true, message: "入庫原因不能為空", trigger: "blur" }
        ]
      },
      // 出庫對話框
      stockOutOpen: false,
      stockOutForm: {},
      stockOutRules: {
        quantity: [
          { required: true, message: "出庫數量不能為空", trigger: "blur" }
        ],
        reason: [
          { required: true, message: "出庫原因不能為空", trigger: "blur" }
        ]
      },
      // 盤點對話框
      stockCheckOpen: false,
      stockCheckForm: {
        checkType: 'all'
      }
    };
  },
  created() {
    this.getList();
    this.getCategoryList();
    this.getStockStatistics();
  },
  methods: {
    /** 查詢庫存列表 */
    getList() {
      this.loading = true;
      listStock(this.queryParams).then(response => {
        this.stockList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.stockList = [];
        this.total = 0;
        this.loading = false;
      });
    },
    /** 查詢分類列表 */
    getCategoryList() {
      listCategory().then(response => {
        this.categoryOptions = response.data;
      }).catch(() => {
        this.categoryOptions = [];
      });
    },
    /** 取得庫存統計 */
    getStockStatistics() {
      getStockStats().then(response => {
        this.stockStats = response.data;
      }).catch(() => {
        this.stockStats = {};
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
        stockId: null,
        itemId: null,
        stockQuantity: null,
        availableQuantity: null,
        borrowedQuantity: null,
        minStock: null,
        maxStock: null,
        location: null,
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多選框選中資料
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.stockId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 入庫按鈕操作 */
    handleStockIn() {
      this.$router.push('/cadm/inventory/stock/in');
    },
    /** 出庫按鈕操作 */
    handleStockOut() {
      if (this.ids.length === 0) {
        this.$message.warning('請選擇要出庫的物品');
        return;
      }
      this.$router.push('/cadm/inventory/stock/out?ids=' + this.ids.join(','));
    },
    /** 單個物品入庫 */
    handleStockInItem(row) {
      this.stockInForm = {
        itemId: row.itemId,
        itemName: row.itemName,
        quantity: 1,
        reason: '',
        remark: ''
      };
      this.stockInOpen = true;
    },
    /** 單個物品出庫 */
    handleStockOutItem(row) {
      this.stockOutForm = {
        itemId: row.itemId,
        itemName: row.itemName,
        availableQuantity: row.availableQuantity,
        quantity: 1,
        reason: '',
        remark: ''
      };
      this.stockOutOpen = true;
    },
    /** 提交入庫 */
    submitStockIn() {
      this.$refs["stockInForm"].validate(valid => {
        if (valid) {
          stockIn(this.stockInForm).then(response => {
            this.$modal.msgSuccess("入庫成功");
            this.stockInOpen = false;
            this.getList();
            this.getStockStatistics();
          });
        }
      });
    },
    /** 取消入庫 */
    cancelStockIn() {
      this.stockInOpen = false;
      this.stockInForm = {};
    },
    /** 提交出庫 */
    submitStockOut() {
      this.$refs["stockOutForm"].validate(valid => {
        if (valid) {
          stockOut(this.stockOutForm).then(response => {
            this.$modal.msgSuccess("出庫成功");
            this.stockOutOpen = false;
            this.getList();
            this.getStockStatistics();
          });
        }
      });
    },
    /** 取消出庫 */
    cancelStockOut() {
      this.stockOutOpen = false;
      this.stockOutForm = {};
    },
    /** 盤點按鈕操作 */
    handleStockCheck() {
      this.stockCheckForm = {
        checkType: 'all',
        categoryId: null,
        location: null,
        remark: ''
      };
      this.stockCheckOpen = true;
    },
    /** 提交盤點 */
    submitStockCheck() {
      stockCheck(this.stockCheckForm).then(response => {
        this.$modal.msgSuccess("盤點任務已建立");
        this.stockCheckOpen = false;
        this.getList();
      });
    },
    /** 取消盤點 */
    cancelStockCheck() {
      this.stockCheckOpen = false;
      this.stockCheckForm = {};
    },
    /** 查看詳情 */
    handleDetail(row) {
      this.$router.push('/cadm/inventory/stock/detail/' + row.stockId);
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('inventory/stock/export', {
        ...this.queryParams
      }, `stock_${new Date().getTime()}.xlsx`)
    },
    /** 取得庫存狀態樣式 */
    getStockClass(row) {
      if (row.stockQuantity <= 0) {
        return 'text-danger';
      } else if (row.stockQuantity <= row.minStock) {
        return 'text-warning';
      }
      return '';
    },
    /** 取得狀態類型 */
    getStatusType(row) {
      if (row.stockQuantity <= 0) {
        return 'danger';
      } else if (row.stockQuantity <= row.minStock) {
        return 'warning';
      }
      return 'success';
    },
    /** 取得狀態文字 */
    getStatusText(row) {
      if (row.stockQuantity <= 0) {
        return '缺貨';
      } else if (row.stockQuantity <= row.minStock) {
        return '低庫存';
      }
      return '正常';
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

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}
</style>
