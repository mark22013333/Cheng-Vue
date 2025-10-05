<template>
  <div class="app-container">
    <!-- 搜尋表單 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="物品編碼" prop="itemCode">
        <el-input
          v-model="queryParams.itemCode"
          placeholder="請輸入物品編碼"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物品名稱" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="請輸入物品名稱"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="庫存狀態" prop="stockStatus">
        <el-select v-model="queryParams.stockStatus" placeholder="請選擇" clearable style="width: 150px"
                   @change="handleStockStatusChange">
          <el-option label="全部" value=""/>
          <el-option label="正常" value="0"/>
          <el-option label="低庫存" value="1"/>
          <el-option label="無庫存" value="2"/>
        </el-select>
      </el-form-item>
      <el-form-item label="低庫存閾值" prop="lowStockThreshold" v-if="queryParams.stockStatus === '1'">
        <el-input-number v-model="queryParams.lowStockThreshold" :min="0" :max="1000" placeholder="預設為物品最低庫存"
                         style="width: 150px"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜尋</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按鈕 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['inventory:management:add']"
        >新增物品
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['inventory:management:export']"
        >匯出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-warning"
          size="mini"
          @click="showLowStockOnly"
        >低庫存提醒
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-tooltip content="點擊可設定全域低庫存閾值" placement="top">
          <el-input-number
            v-model="globalLowStockThreshold"
            :min="0"
            :max="1000"
            size="mini"
            placeholder="低庫存閾值"
            style="width: 130px"
            @change="handleThresholdChange"
          />
        </el-tooltip>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 資料表格 -->
    <el-table v-loading="loading" :data="managementList" @selection-change="handleSelectionChange"
              @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="物品編碼" align="center" prop="itemCode" width="140" sortable="custom"
                       :show-overflow-tooltip="true"/>
      <el-table-column label="物品名稱" align="center" prop="itemName" min-width="150" sortable="custom"
                       :show-overflow-tooltip="true"/>
      <el-table-column label="規格" align="center" prop="specification" width="120"/>
      <el-table-column label="品牌/型號" align="center" width="150">
        <template slot-scope="scope">
          {{ scope.row.brand }} {{ scope.row.model }}
        </template>
      </el-table-column>
      <el-table-column label="單位" align="center" prop="unit" width="60"/>

      <!-- 庫存資訊 -->
      <el-table-column label="總數量" align="center" prop="totalQuantity" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.totalQuantity > 0" type="success">{{ scope.row.totalQuantity }}</el-tag>
          <el-tag v-else type="danger">0</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可用" align="center" prop="availableQty" width="70"/>
      <el-table-column label="借出" align="center" prop="borrowedQty" width="70"/>
      <el-table-column label="庫存狀態" align="center" prop="stockStatusText" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.stockStatus === '0'" type="success">{{ scope.row.stockStatusText }}</el-tag>
          <el-tag v-else-if="scope.row.stockStatus === '1'" type="warning">{{ scope.row.stockStatusText }}</el-tag>
          <el-tag v-else type="danger">{{ scope.row.stockStatusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="存放位置" align="center" prop="location" width="140" sortable="custom"
                       :show-overflow-tooltip="true"/>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['inventory:management:query']"
          >詳情
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-top"
            @click="handleStockIn(scope.row)"
            v-hasPermi="['inventory:management:stockIn']"
          >入庫
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-bottom"
            @click="handleStockOut(scope.row)"
            v-hasPermi="['inventory:management:stockOut']"
          >出庫
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['inventory:management:edit']"
          >修改
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 入庫對話框 -->
    <el-dialog :title="'入庫 - ' + currentItem.itemName" :visible.sync="stockInDialogVisible" width="500px"
               append-to-body>
      <el-form ref="stockInForm" :model="stockInForm" :rules="stockInRules" label-width="100px">
        <el-form-item label="入庫數量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" :max="10000" controls-position="right"
                           style="width: 100%"/>
        </el-form-item>
        <el-form-item label="入庫原因" prop="reason">
          <el-input v-model="stockInForm.reason" type="textarea" :rows="3" placeholder="請輸入入庫原因"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="stockInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockIn">確定</el-button>
      </div>
    </el-dialog>

    <!-- 出庫對話框 -->
    <el-dialog :title="'出庫 - ' + currentItem.itemName" :visible.sync="stockOutDialogVisible" width="500px"
               append-to-body>
      <el-form ref="stockOutForm" :model="stockOutForm" :rules="stockOutRules" label-width="100px">
        <el-form-item label="可用數量">
          <span style="color: #409EFF; font-weight: bold;">{{ currentItem.availableQty }}</span>
        </el-form-item>
        <el-form-item label="出庫數量" prop="quantity">
          <el-input-number v-model="stockOutForm.quantity" :min="1" :max="currentItem.availableQty"
                           controls-position="right" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="出庫原因" prop="reason">
          <el-input v-model="stockOutForm.reason" type="textarea" :rows="3" placeholder="請輸入出庫原因"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="stockOutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockOut">確定</el-button>
      </div>
    </el-dialog>

    <!-- 詳情對話框 -->
    <el-dialog title="物品與庫存詳情" :visible.sync="detailDialogVisible" width="900px" append-to-body>
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="物品編碼">{{ detailData.itemCode }}</el-descriptions-item>
        <el-descriptions-item label="物品名稱">{{ detailData.itemName }}</el-descriptions-item>
        <el-descriptions-item label="分類">{{ detailData.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="規格">{{ detailData.specification }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detailData.brand }}</el-descriptions-item>
        <el-descriptions-item label="型號">{{ detailData.model }}</el-descriptions-item>
        <el-descriptions-item label="單位">{{ detailData.unit }}</el-descriptions-item>
        <el-descriptions-item label="供應商">{{ detailData.supplier }}</el-descriptions-item>
        <el-descriptions-item label="採購價格">{{ detailData.purchasePrice }}</el-descriptions-item>
        <el-descriptions-item label="現價">{{ detailData.currentPrice }}</el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ detailData.location }}</el-descriptions-item>
        <el-descriptions-item label="條碼">{{ detailData.barcode }}</el-descriptions-item>

        <el-descriptions-item label="庫存狀態" :span="2">
          <el-tag v-if="detailData.stockStatus === '0'" type="success">{{ detailData.stockStatusText }}</el-tag>
          <el-tag v-else-if="detailData.stockStatus === '1'" type="warning">{{ detailData.stockStatusText }}</el-tag>
          <el-tag v-else type="danger">{{ detailData.stockStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="總數量">{{ detailData.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="可用數量">{{ detailData.availableQty }}</el-descriptions-item>
        <el-descriptions-item label="借出數量">{{ detailData.borrowedQty }}</el-descriptions-item>
        <el-descriptions-item label="預留數量">{{ detailData.reservedQty }}</el-descriptions-item>
        <el-descriptions-item label="損壞數量">{{ detailData.damagedQty }}</el-descriptions-item>
        <el-descriptions-item label="庫存總價值">{{
            detailData.stockValue ? '¥' + detailData.stockValue : '-'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="最低庫存">{{ detailData.minStock }}</el-descriptions-item>
        <el-descriptions-item label="最高庫存">{{ detailData.maxStock }}</el-descriptions-item>
        <el-descriptions-item label="最後入庫時間" :span="2">{{
            parseTime(detailData.lastInTime)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="最後出庫時間" :span="2">{{
            parseTime(detailData.lastOutTime)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="備註" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import {
  listManagement,
  getManagement,
  delManagement,
  exportManagement,
  stockIn,
  stockOut
} from "@/api/inventory/management"

export default {
  name: "InvManagement",
  data() {
    return {
      // 遮罩層
      loading: true,
      // 選中陣列
      ids: [],
      // 非單個停用
      single: true,
      // 非多個停用
      multiple: true,
      // 顯示搜尋條件
      showSearch: true,
      // 總條數
      total: 0,
      // 物品與庫存整合表格資料
      managementList: [],
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        itemCode: null,
        itemName: null,
        categoryId: null,
        brand: null,
        status: null,
        stockStatus: null,
        lowStockThreshold: null,
        orderByColumn: null,
        isAsc: null
      },
      // 全域低庫存閾值
      globalLowStockThreshold: null,
      // 入庫表單
      stockInForm: {
        itemId: null,
        quantity: 1,
        reason: ''
      },
      stockInRules: {
        quantity: [
          {required: true, message: "入庫數量不能為空", trigger: "blur"}
        ]
      },
      // 出庫表單
      stockOutForm: {
        itemId: null,
        quantity: 1,
        reason: ''
      },
      stockOutRules: {
        quantity: [
          {required: true, message: "出庫數量不能為空", trigger: "blur"}
        ]
      },
      // 當前操作的物品
      currentItem: {},
      // 對話框顯示
      stockInDialogVisible: false,
      stockOutDialogVisible: false,
      detailDialogVisible: false,
      // 詳情資料
      detailData: null
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查詢物品與庫存整合列表 */
    getList() {
      this.loading = true;
      listManagement(this.queryParams).then(response => {
        this.managementList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
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
    /** 多選框選中資料 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.itemId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.$router.push("/inventory/item/add");
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      const itemId = row.itemId || this.ids
      this.$router.push("/inventory/item/edit/" + itemId);
    },
    /** 查看詳情 */
    handleView(row) {
      this.detailDialogVisible = true;
      getManagement(row.itemId).then(response => {
        this.detailData = response.data;
      });
    },
    /** 入庫操作 */
    handleStockIn(row) {
      this.currentItem = row;
      this.stockInForm = {
        itemId: row.itemId,
        quantity: 1,
        reason: ''
      };
      this.stockInDialogVisible = true;
    },
    /** 提交入庫 */
    submitStockIn() {
      this.$refs["stockInForm"].validate(valid => {
        if (valid) {
          stockIn(this.stockInForm).then(response => {
            this.$modal.msgSuccess("入庫成功");
            this.stockInDialogVisible = false;
            this.getList();
          });
        }
      });
    },
    /** 出庫操作 */
    handleStockOut(row) {
      if (!row.availableQty || row.availableQty <= 0) {
        this.$modal.msgWarning("可用數量不足，無法出庫");
        return;
      }
      this.currentItem = row;
      this.stockOutForm = {
        itemId: row.itemId,
        quantity: 1,
        reason: ''
      };
      this.stockOutDialogVisible = true;
    },
    /** 提交出庫 */
    submitStockOut() {
      this.$refs["stockOutForm"].validate(valid => {
        if (valid) {
          stockOut(this.stockOutForm).then(response => {
            this.$modal.msgSuccess("出庫成功");
            this.stockOutDialogVisible = false;
            this.getList();
          });
        }
      });
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('inventory/management/export', {
        ...this.queryParams
      }, `物品庫存_${new Date().getTime()}.xlsx`)
    },
    /** 顯示低庫存提醒 */
    showLowStockOnly() {
      this.queryParams.stockStatus = '1';
      this.handleQuery();
    },
    /** 排序變化處理 */
    handleSortChange({column, prop, order}) {
      if (order === 'ascending') {
        this.queryParams.orderByColumn = prop;
        this.queryParams.isAsc = 'asc';
      } else if (order === 'descending') {
        this.queryParams.orderByColumn = prop;
        this.queryParams.isAsc = 'desc';
      } else {
        this.queryParams.orderByColumn = null;
        this.queryParams.isAsc = null;
      }
      this.handleQuery();
    },
    /** 全域低庫存閾值變化 */
    handleThresholdChange(value) {
      if (this.queryParams.stockStatus === '1') {
        this.queryParams.lowStockThreshold = value;
        this.handleQuery();
      }
    },
    /** 庫存狀態變化處理 */
    handleStockStatusChange() {
      // 當切換到非低庫存狀態時，清除低庫存閾值參數
      if (this.queryParams.stockStatus !== '1') {
        this.queryParams.lowStockThreshold = null;
      }
      this.handleQuery();
    }
  }
};
</script>
