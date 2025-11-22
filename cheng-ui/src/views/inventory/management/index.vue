<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <!-- 物品管理頁籤 -->
      <el-tab-pane label="物品管理" name="items">
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
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['inventory:management:remove']"
        >刪除
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
      <el-table-column label="圖片" align="center" width="80">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="getImageUrl(scope.row.imageUrl)"
            :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
            fit="cover"
            style="width: 50px; height: 50px; border-radius: 4px; cursor: pointer;"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline" style="font-size: 30px; color: #ccc;"></i>
            </div>
          </el-image>
          <span v-else style="color: #ccc;">無圖</span>
        </template>
      </el-table-column>
      <el-table-column label="物品名稱" align="center" prop="itemName" min-width="150" sortable="custom"
                       :show-overflow-tooltip="true"/>
      <el-table-column label="作者" align="center" prop="author" width="120" :show-overflow-tooltip="true" v-if="hasAuthorColumn"/>
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

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width operation-column" min-width="120" fixed="right">
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
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['inventory:management:remove']"
            style="color: #F56C6C;"
          >刪除
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
        <el-descriptions-item label="作者" v-if="detailData.author">{{ detailData.author }}</el-descriptions-item>
        <el-descriptions-item label="分類">{{ detailData.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="規格">{{ detailData.specification }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detailData.brand }}</el-descriptions-item>
        <el-descriptions-item label="型號">{{ detailData.model }}</el-descriptions-item>
        <el-descriptions-item label="單位">{{ detailData.unit }}</el-descriptions-item>
        <el-descriptions-item label="供應商">{{ detailData.supplier }}</el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ detailData.location }}</el-descriptions-item>
        <el-descriptions-item label="條碼">
          <span>{{ detailData.barcode }}</span>
          <el-button
            v-if="detailData.barcode && isValidIsbn(detailData.barcode)"
            type="primary"
            size="mini"
            icon="el-icon-refresh"
            @click="handleRefreshIsbn"
            style="margin-left: 10px;"
          >重新抓取
          </el-button>
        </el-descriptions-item>
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
        <el-descriptions-item label="圖片" :span="2">
          <el-image
            v-if="detailData.imageUrl"
            :src="getImageUrl(detailData.imageUrl)"
            :preview-src-list="[getImageUrl(detailData.imageUrl)]"
            fit="contain"
            style="max-width: 200px; max-height: 200px; border-radius: 4px; cursor: pointer;"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline" style="font-size: 50px; color: #ccc;"></i>
            </div>
          </el-image>
          <span v-else style="color: #999;">無圖片</span>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="備註" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 進度對話框 -->
    <ProgressDialog ref="progressDialog" />

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
        <el-form-item label="圖片" prop="imageUrl">
          <image-upload v-model="editForm.imageUrl" :limit="1"/>
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
      </el-tab-pane>

      <!-- 分類管理頁籤 -->
      <el-tab-pane label="分類管理" name="categories">
        <CategoryManagement />
      </el-tab-pane>
    </el-tabs>
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
import { createRefreshTask } from "@/api/inventory/scan"
import ImageUpload from '@/components/ImageUpload'
import ProgressDialog from '@/components/ProgressDialog'
import { getImageUrl } from '@/utils/image'
import CategoryManagement from './components/CategoryManagement'

export default {
  name: "InvManagement",
  components: {
    ImageUpload,
    ProgressDialog,
    CategoryManagement
  },
  data() {
    return {
      // 當前頁籤
      activeTab: 'items',
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
      // SSE 連線管理（用於並行抓取）
      sseConnections: new Map(),
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
  computed: {
    // 判斷是否有任何物品包含作者資訊（用於顯示作者欄位）
    hasAuthorColumn() {
      return this.managementList.some(item => item.author && item.author.trim() !== '');
    }
  },
  created() {
    // 檢查路由，如果是從分類管理選單進來，自動切換到分類管理頁籤
    if (this.$route.path === '/inventory/category') {
      this.activeTab = 'categories';
    }
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
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const itemIds = row.itemId ? [row.itemId] : this.ids;
      const itemNames = row.itemName ? [row.itemName] : this.managementList
        .filter(item => itemIds.includes(item.itemId))
        .map(item => item.itemName);
      
      const confirmMessage = `
        <div style="text-align: left;">
          <p style="color: #E6A23C; font-weight: bold; margin-bottom: 10px;">
            <i class="el-icon-warning"></i> 警告：此操作將會同時刪除以下相關資料
          </p>
          <ul style="margin: 10px 0; padding-left: 20px; color: #909399; font-size: 13px;">
            <li>物品基本資訊</li>
            <li>書籍詳細資訊（如有）</li>
            <li>庫存記錄</li>
            <li>所有歷史異動記錄</li>
          </ul>
          <p style="font-weight: bold; margin-top: 15px; margin-bottom: 10px;">確定要刪除以下物品嗎？</p>
          <ul style="padding-left: 20px;">
            ${itemNames.map(name => '<li>' + name + '</li>').join('')}
          </ul>
        </div>
      `;
      
      this.$confirm(confirmMessage, '刪除確認', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '確定刪除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delManagement(itemIds);
      }).then((response) => {
        this.getList();
        if (response.code === 200) {
          // 成功或部分成功的情況
          const message = response.msg || "刪除成功";
          if (message.includes('失敗')) {
            // 部分失敗，使用通知框顯示詳細訊息
            this.$notify({
              title: '⚠️ 刪除結果',
              dangerouslyUseHTMLString: true,
              message: `<div style="max-height: 400px; overflow-y: auto;">${message}</div>`,
              type: 'warning',
              duration: 8000,
              customClass: 'delete-result-notification'
            });
          } else {
            // 完全成功
            this.$message.success(message);
          }
        } else {
          // 完全失敗
          this.$notify({
            title: '❌ 刪除失敗',
            dangerouslyUseHTMLString: true,
            message: `<div style="max-height: 400px; overflow-y: auto;">${response.msg || "刪除失敗"}</div>`,
            type: 'error',
            duration: 10000,
            customClass: 'delete-result-notification'
          });
        }
      }).catch((error) => {
        // 使用者取消或其他錯誤
        if (error && error !== 'cancel') {
          console.error('刪除操作錯誤:', error);
        }
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
        imageUrl: null,
        status: "0",
        remark: null
      };
    },
    /** 取得圖片 URL（使用統一工具函數） */
    getImageUrl,
    /** 取得分類列表 */
    getCategoryList() {
      listCategory({ status: '0' }).then(response => {
        this.categoryList = response.rows || [];
      });
    },
    /** Tab 切換事件（重新整理資料） */
    handleTabChange(tab) {
      // 切換頁籤時重新整理對應的資料
      if (tab.name === 'items') {
        // 物品管理頁籤 - 重新整理物品列表
        this.getList();
      } else if (tab.name === 'categories') {
        // 分類管理頁籤 - CategoryManagement 組件會自己載入資料
        // 這裡不需要特別處理，因為子組件有自己的生命週期
      }
    },
    /** 驗證是否為有效的 ISBN */
    isValidIsbn(barcode) {
      if (!barcode) return false;
      // ISBN-10 或 ISBN-13 格式驗證
      const isbn = barcode.replace(/[-\s]/g, '');
      return /^(97[89])?\d{9}[\dXx]$/.test(isbn);
    },
    /** 重新抓取 ISBN 資料（使用 SSE + ProgressDialog） */
    handleRefreshIsbn() {
      if (!this.detailData || !this.detailData.barcode) {
        this.$modal.msgWarning("條碼為空，無法抓取");
        return;
      }
      
      const itemId = this.detailData.itemId;
      const isbn = this.detailData.barcode;
      const itemName = this.detailData.itemName;
      
      this.$confirm(
        `<div style="margin-bottom: 10px;">確定要重新抓取 ISBN <strong>${isbn}</strong> 的書籍資料嗎？</div>` +
        `<div style="color: #909399; font-size: 12px;">
          <p style="margin: 5px 0;"><strong>更新範圍：</strong></p>
          <ul style="margin: 5px 0; padding-left: 20px; text-align: left;">
            <li>書名、作者、出版社</li>
            <li>封面圖片、簡介</li>
            <li>規格、版本資訊</li>
          </ul>
          <p style="margin: 5px 0; color: #67C23A;"><strong>✅ 不影響：</strong>庫存數量、借出狀態</p>
          <p style="margin: 5px 0; color: #E6A23C;"><strong>⚠️ 注意：</strong>如果新資料不完整，則不會更新</p>
          <p style="margin: 10px 0; color: #409EFF;"><strong>💡 提示：</strong>可同時抓取多本書籍</p>
        </div>`,
        "重新抓取確認",
        {
          confirmButtonText: "確定抓取",
          cancelButtonText: "取消",
          type: "warning",
          dangerouslyUseHTMLString: true,
          center: false
        }
      ).then(() => {
        // 1. 建立任務並取得 taskId
        createRefreshTask(itemId).then(response => {
          const taskId = response.data;
          let dialogMinimized = false; // 標記對話框是否被最小化
          
          // 2. 開啟進度對話框
          this.$refs.progressDialog.show({
            title: `重新抓取書籍資料 - ${itemName}`,
            message: '準備中...',
            showLogs: true
          });
          
          // 監聽對話框最小化事件
          const handleMinimize = () => {
            dialogMinimized = true;
            this.$notify.info({
              title: '背景執行中',
              message: `《${itemName}》仍在背景抓取資料...`,
              duration: 3000
            });
          };
          this.$refs.progressDialog.$once('minimize', handleMinimize);
          
          // 3. 建立 SSE 連線
          const baseURL = process.env.VUE_APP_BASE_API || '';
          const eventSource = new EventSource(
            `${baseURL}/inventory/scan/refreshIsbn/subscribe/${taskId}?itemId=${itemId}`
          );
          
          // 儲存連線（用於並行抓取）
          this.sseConnections.set(taskId, eventSource);
          
          // 監聽進度事件
          eventSource.addEventListener('progress', (event) => {
            try {
              const data = JSON.parse(event.data);
              // 只有對話框未最小化時才更新進度
              if (!dialogMinimized) {
                this.$refs.progressDialog.updateProgress(data.progress, data.message);
              }
            } catch (error) {
              console.error('解析進度事件失敗', error);
            }
          });
          
          // 監聽成功事件
          eventSource.addEventListener('success', (event) => {
            try {
              const result = JSON.parse(event.data);
              
              // 如果對話框已最小化，使用通知提示
              if (dialogMinimized) {
                this.$notify.success({
                  title: '✅ 書籍資訊更新成功',
                  message: `《${itemName}》資料已更新完成`,
                  duration: 5000
                });
              } else {
                // 設定進度對話框為成功狀態
                this.$refs.progressDialog.setSuccess(result.message || '書籍資訊更新成功');
              }
              
              // 關閉 SSE 連線
              eventSource.close();
              this.sseConnections.delete(taskId);
              
              // 顯示變更詳情
              if (result.updatedFields && result.updatedFields.length > 0) {
                setTimeout(() => {
                  const changeDetails = Object.entries(result.changes)
                    .map(([key, value]) => `<li><strong>${key}</strong>: ${value}</li>`)
                    .join('');
                  
                  this.$alert(
                    `<div style="text-align: left;">
                      <p style="margin-bottom: 10px; color: #67C23A; font-weight: bold;">${result.message}</p>
                      <p style="margin: 10px 0; color: #606266; font-size: 13px;">
                        資料完整性：舊資料 <strong>${result.existingScore}</strong> 分 → 新資料 <strong>${result.newScore}</strong> 分
                      </p>
                      <p style="margin-bottom: 5px; font-weight: bold;">變更詳情：</p>
                      <ul style="padding-left: 20px;">${changeDetails}</ul>
                    </div>`,
                    "更新成功",
                    {
                      dangerouslyUseHTMLString: true,
                      confirmButtonText: "知道了"
                    }
                  );
                }, 500);
              }
              
              // 重新載入詳情資料
              getManagement(itemId).then(response => {
                this.detailData = response.data;
                // 重新整理列表
                this.getList();
              });
              
            } catch (error) {
              console.error('解析成功事件失敗', error);
            }
          });
          
          // 監聽警告事件（例如：資料相同無需更新）
          eventSource.addEventListener('warning', (event) => {
            try {
              const data = JSON.parse(event.data);
              const warningMsg = data.message || '無需更新';
              
              if (dialogMinimized) {
                this.$notify.warning({
                  title: '⚠️ 提示',
                  message: `《${itemName}》${warningMsg}`,
                  duration: 5000
                });
              } else {
                this.$refs.progressDialog.setWarning(warningMsg);
              }
              
              // 重新載入詳情資料
              getManagement(itemId).then(response => {
                this.detailData = response.data;
              });
            } catch (error) {
              console.error('解析警告事件失敗', error);
            } finally {
              eventSource.close();
              this.sseConnections.delete(taskId);
            }
          });
          
          // 監聽錯誤事件
          eventSource.addEventListener('error', (event) => {
            try {
              const data = JSON.parse(event.data);
              const errorMsg = data.message || '處理失敗';
              
              if (dialogMinimized) {
                this.$notify.error({
                  title: '❌ 抓取失敗',
                  message: `《${itemName}》${errorMsg}`,
                  duration: 5000
                });
              } else {
                this.$refs.progressDialog.setError(errorMsg);
              }
            } catch (error) {
              console.error('解析錯誤事件失敗', error);
            } finally {
              eventSource.close();
              this.sseConnections.delete(taskId);
            }
          });
          
          // 監聽連線錯誤（僅處理真正的網路錯誤）
          eventSource.onerror = (event) => {
            console.error('SSE 連線錯誤', event);
            
            // 如果連線已經正常關閉（任務完成），不做任何處理
            if (eventSource.readyState === EventSource.CLOSED) {
              return;
            }
            
            // 只有在連線異常中斷時才顯示錯誤
            if (eventSource.readyState === EventSource.CONNECTING) {
              // 正在重連，暫時不顯示錯誤
              return;
            }
            
            const errorMsg = '連線中斷，請重試';
            if (dialogMinimized) {
              this.$notify.error({
                title: '❌ 連線中斷',
                message: `《${itemName}》${errorMsg}`,
                duration: 5000
              });
            } else {
              this.$refs.progressDialog.setError(errorMsg);
            }
            
            eventSource.close();
            this.sseConnections.delete(taskId);
          };
          
        }).catch(error => {
          const errorMsg = error.msg || error.message || "建立任務失敗";
          this.$modal.msgError(errorMsg);
        });
        
      }).catch(() => {
        // 使用者取消
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
  },
  beforeDestroy() {
    // 關閉所有 SSE 連線
    if (this.sseConnections) {
      this.sseConnections.forEach((eventSource, taskId) => {
        eventSource.close();
        console.log('關閉 SSE 連線:', taskId);
      });
      this.sseConnections.clear();
    }
  }
};
</script>

<style scoped>
/* 操作欄位自適應 */
.operation-column {
  /* 讓操作欄根據內容自動調整寬度 */
  width: auto !important;
}

.operation-column .cell {
  white-space: nowrap;
  overflow: visible;
}

/* 手機端響應式優化 */
@media (max-width: 768px) {
  /* 縮小表單整體間距 */
  .app-container {
    padding: 10px !important;
  }

  /* 操作欄按鈕優化 */
  .operation-column .el-button--mini {
    padding: 3px 5px !important;
    margin: 2px !important;
  }

  .operation-column .el-button--mini + .el-button--mini {
    margin-left: 3px !important;
  }

  /* 查詢表單優化 */
  .el-form--inline .el-form-item {
    margin-right: 8px !important;
    margin-bottom: 8px !important;
  }

  /* 表單項標籤縮小 */
  .el-form-item__label {
    padding: 0 8px 0 0 !important;
    font-size: 13px !important;
    width: 70px !important;
  }

  /* 輸入框縮小 */
  .el-form-item__content .el-input,
  .el-form-item__content .el-select {
    width: 140px !important;
  }

  /* 輸入數字框縮小 */
  .el-input-number {
    width: 120px !important;
  }

  /* 按鈕組間距縮小 */
  .mb8 {
    margin-bottom: 8px !important;
  }

  .el-row {
    margin-left: -5px !important;
    margin-right: -5px !important;
  }

  .el-col {
    padding-left: 5px !important;
    padding-right: 5px !important;
  }

  /* 按鈕文字和圖標縮小 */
  .el-button--mini {
    padding: 5px 8px !important;
    font-size: 12px !important;
  }

  /* 表格行高縮小 */
  .el-table td,
  .el-table th {
    padding: 6px 0 !important;
  }

  /* 表格字體縮小 */
  .el-table {
    font-size: 12px !important;
  }

  /* 對話框在手機端全屏 */
  .el-dialog {
    width: 95% !important;
    margin-top: 5vh !important;
  }

  .el-dialog__body {
    padding: 15px !important;
  }

  /* 標籤縮小 */
  .el-tag {
    padding: 0 5px !important;
    height: 22px !important;
    line-height: 22px !important;
    font-size: 11px !important;
  }

  /* 圖片縮小 */
  .el-image {
    width: 40px !important;
    height: 40px !important;
  }

  /* 工具提示框優化 */
  .el-tooltip {
    width: 100%;
  }
}

/* 小屏幕進一步優化 */
@media (max-width: 480px) {
  .app-container {
    padding: 5px !important;
  }

  .el-form-item__label {
    width: 60px !important;
    font-size: 12px !important;
  }

  .el-form-item__content .el-input,
  .el-form-item__content .el-select {
    width: 120px !important;
  }

  .el-button--mini {
    padding: 4px 6px !important;
    font-size: 11px !important;
  }

  /* 隱藏部分非必要按鈕文字，只顯示圖標 */
  .el-button--mini span:not(.el-icon) {
    display: none;
  }

  .el-button--mini i {
    margin: 0 !important;
  }

  /* 操作欄在小螢幕上進一步縮小 */
  .operation-column {
    min-width: 80px !important;
  }

  .operation-column .el-button--mini {
    padding: 2px 4px !important;
    margin: 1px !important;
    min-width: 28px;
  }
}

/* 刪除結果通知樣式優化 */
.delete-result-notification {
  width: 500px;
  max-width: 90vw;
}

.delete-result-notification .el-notification__content {
  text-align: left;
  line-height: 1.6;
  font-size: 14px;
}

/* 通知內容中的 code 標籤樣式 */
.delete-result-notification code {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
}

/* 通知內容滾動條美化 */
.delete-result-notification ::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.delete-result-notification ::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.delete-result-notification ::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.3);
}

.delete-result-notification ::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}
</style>
