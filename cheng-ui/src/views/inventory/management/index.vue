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
      <el-table-column label="物品編碼" align="center" prop="itemCode" min-width="180" sortable="custom"
                       :show-overflow-tooltip="true"/>
      <el-table-column label="物品名稱" align="center" prop="itemName" min-width="150" sortable="custom"
                       :show-overflow-tooltip="true"/>
      <el-table-column label="規格" align="center" prop="specification" width="120"/>
      <el-table-column label="品牌/型號" align="center" width="150">
        <template slot-scope="scope">
          {{ scope.row.brand }} {{ scope.row.model }}
        </template>
      </el-table-column>

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
        <el-descriptions-item label="存放位置">{{ detailData.location }}</el-descriptions-item>
        <el-descriptions-item label="條碼">{{ detailData.barcode }}</el-descriptions-item>
        <el-descriptions-item label="最低庫存">{{ detailData.minStock }}</el-descriptions-item>
        <el-descriptions-item label="最高庫存">{{ detailData.maxStock }}</el-descriptions-item>

        <el-descriptions-item label="庫存狀態" :span="2">
          <el-tag v-if="detailData.stockStatus === '0'" type="success">{{ detailData.stockStatusText }}</el-tag>
          <el-tag v-else-if="detailData.stockStatus === '1'" type="warning">{{ detailData.stockStatusText }}</el-tag>
          <el-tag v-else type="danger">{{ detailData.stockStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="採購價格">{{ formatMoney(detailData.purchasePrice) }}</el-descriptions-item>
        <el-descriptions-item label="現價">{{ formatMoney(detailData.currentPrice) }}</el-descriptions-item>
        <el-descriptions-item label="總數量">{{ detailData.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="可用數量">{{ detailData.availableQty }}</el-descriptions-item>
        <el-descriptions-item label="借出數量">{{ detailData.borrowedQty }}</el-descriptions-item>
        <el-descriptions-item label="損壞數量">{{ detailData.damagedQty }}</el-descriptions-item>
        <el-descriptions-item label="遺失數量">{{ detailData.lostQty || 0 }}</el-descriptions-item>

        <!-- ========== 財務分析 ========== -->
        <!-- 歷史成本 -->
        <el-descriptions-item label="歷史採購成本" :span="2">
          <span style="color: #909399; font-weight: bold;">{{ formatMoney(detailData.historicalCost) }}</span>
          <el-tag type="info" size="mini" style="margin-left: 8px;">已支付總成本</el-tag>
        </el-descriptions-item>

        <!-- 當前庫存資產 -->
        <el-descriptions-item label="當前庫存成本" :span="2">
          <span style="color: #E6A23C; font-weight: bold;">{{ formatMoney(detailData.costValue) }}</span>
          <span style="color: #909399; font-size: 12px; margin-left: 8px;">（現存 {{ detailData.totalQuantity }} 件）</span>
        </el-descriptions-item>
        <el-descriptions-item label="當前庫存市值" :span="2">
          <span style="color: #409EFF; font-weight: bold;">{{ formatMoney(detailData.stockValue) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="預期利潤" :span="2">
          <span :style="{color: detailData.expectedProfit >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold'}">
            {{ formatMoney(detailData.expectedProfit) }}
          </span>
        </el-descriptions-item>

        <!-- 可用庫存資產 -->
        <el-descriptions-item label="可用庫存成本" :span="2">
          <span style="color: #E6A23C; font-weight: bold;">{{ formatMoney(detailData.availableCost) }}</span>
          <span style="color: #909399; font-size: 12px; margin-left: 8px;">（可售 {{ detailData.availableQty }} 件）</span>
        </el-descriptions-item>
        <el-descriptions-item label="可用庫存市值" :span="2">
          <span style="color: #67C23A; font-weight: bold;">{{ formatMoney(detailData.availableValue) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="可實現利潤" :span="2">
          <span :style="{color: detailData.realizableProfit >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold'}">
            {{ formatMoney(detailData.realizableProfit) }}
          </span>
          <span style="color: #909399; font-size: 12px; margin-left: 8px;">（利潤率: {{ formatPercent(detailData.profitRate) }}）</span>
        </el-descriptions-item>

        <!-- 損失明細 -->
        <el-descriptions-item label="損壞損失" :span="2" v-if="detailData.damagedQty > 0">
          <span style="color: #E6A23C; font-weight: bold;">-{{ formatMoney(detailData.damagedValue) }}</span>
          <el-tag type="warning" size="mini" style="margin-left: 8px;">{{ detailData.damagedQty }} 件損壞</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="遺失損失" :span="2" v-if="detailData.lostQty > 0">
          <span style="color: #F56C6C; font-weight: bold;">-{{ formatMoney(detailData.lostValue) }}</span>
          <el-tag type="danger" size="mini" style="margin-left: 8px;">{{ detailData.lostQty }} 件遺失</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累計損失" :span="2" v-if="detailData.totalLoss > 0">
          <span style="color: #F56C6C; font-weight: bold; font-size: 16px;">-{{ formatMoney(detailData.totalLoss) }}</span>
          <el-tag type="danger" size="mini" style="margin-left: 8px;">⚠️ 總損失</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="最後入庫時間" :span="2">{{
            parseTime(detailData.lastInTime)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="最後出庫時間" :span="2">{{
            parseTime(detailData.lastOutTime)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="備註" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 編輯對話框 -->
    <el-dialog :title="editDialogTitle" :visible.sync="editDialogVisible" width="800px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="物品編碼" prop="itemCode">
              <el-input v-model="editForm.itemCode" :disabled="isEdit" placeholder="請輸入物品編碼"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物品名稱" prop="itemName">
              <el-input v-model="editForm.itemName" placeholder="請輸入物品名稱"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="物品分類" prop="categoryId">
              <el-select v-model="editForm.categoryId" placeholder="請選擇分類" style="width: 100%">
                <el-option
                  v-for="category in categoryList"
                  :key="category.categoryId"
                  :label="category.categoryName"
                  :value="category.categoryId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="條碼" prop="barcode">
              <el-input v-model="editForm.barcode" :disabled="isEdit" :placeholder="isEdit ? '條碼不可修改' : '請輸入條碼'"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="規格" prop="specification">
              <el-input v-model="editForm.specification" placeholder="請輸入規格"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="單位" prop="unit">
              <el-input v-model="editForm.unit" placeholder="請輸入單位"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="editForm.brand" placeholder="請輸入品牌"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="型號" prop="model">
              <el-input v-model="editForm.model" placeholder="請輸入型號"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供應商" prop="supplier">
              <el-input v-model="editForm.supplier" placeholder="請輸入供應商"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="採購價格" prop="purchasePrice">
              <el-input-number v-model="editForm.purchasePrice" :precision="2" :min="0" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="現價" prop="currentPrice">
              <el-input-number v-model="editForm.currentPrice" :precision="2" :min="0" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="最低庫存" prop="minStock">
              <el-input-number v-model="editForm.minStock" :min="0" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高庫存" prop="maxStock">
              <el-input-number v-model="editForm.maxStock" :min="0" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="存放位置" prop="location">
          <el-input v-model="editForm.location" placeholder="請輸入存放位置"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="請輸入描述"/>
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" placeholder="請輸入備註"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">確定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listManagement,
  getManagement,
  delManagement,
  addManagement,
  updateManagement,
  exportManagement,
  stockIn,
  stockOut
} from "@/api/inventory/management"
import { listCategory } from "@/api/inventory/category"

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
      detailData: null,
      // 編輯對話框
      editDialogVisible: false,
      editDialogTitle: "修改物品資訊",
      isEdit: true,
      editForm: {},
      editRules: {
        itemCode: [
          {required: true, message: "物品編碼不能為空", trigger: "blur"}
        ],
        itemName: [
          {required: true, message: "物品名稱不能為空", trigger: "blur"}
        ],
        categoryId: [
          {required: true, message: "分類不能為空", trigger: "change"}
        ]
      },
      // 分類列表
      categoryList: []
    };
  },
  created() {
    this.getList();
    this.getCategoryList();
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
      this.resetEditForm();
      this.editDialogTitle = "新增物品";
      this.isEdit = false;
      this.editDialogVisible = true;
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      const itemId = row.itemId || this.ids;
      this.editDialogTitle = "修改物品資訊";
      this.isEdit = true;
      getManagement(itemId).then(response => {
        this.editForm = response.data;
        this.editDialogVisible = true;
      });
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
    },
    /** 格式化金錢顯示 */
    formatMoney(value) {
      if (value == null || value === '') {
        return '-';
      }
      const num = parseFloat(value);
      if (isNaN(num)) {
        return '-';
      }
      return '$' + num.toLocaleString('zh-TW', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    },
    /** 格式化百分比顯示 */
    formatPercent(value) {
      if (value == null || value === '') {
        return '-';
      }
      const num = parseFloat(value);
      if (isNaN(num)) {
        return '-';
      }
      return num.toFixed(2) + '%';
    },
    /** 重置編輯表單 */
    resetEditForm() {
      this.editForm = {
        itemId: null,
        itemCode: null,
        itemName: null,
        categoryId: null,
        barcode: null,
        specification: null,
        unit: "個",
        brand: null,
        model: null,
        purchasePrice: 0,
        currentPrice: 0,
        supplier: null,
        minStock: 0,
        maxStock: 0,
        location: null,
        description: null,
        status: "0",
        remark: null
      };
    },
    /** 取得分類列表 */
    getCategoryList() {
      listCategory({ status: '0' }).then(response => {
        this.categoryList = response.rows || [];
      });
    },
    /** 提交編輯 */
    submitEdit() {
      this.$refs["editForm"].validate(valid => {
        if (valid) {
          if (this.isEdit) {
            // 修改物品
            updateManagement(this.editForm).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.editDialogVisible = false;
              this.getList();
            });
          } else {
            // 新增物品
            addManagement(this.editForm).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.editDialogVisible = false;
              this.getList();
            });
          }
        }
      });
    }
  }
};
</script>
