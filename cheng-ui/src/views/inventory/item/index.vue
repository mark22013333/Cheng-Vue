<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="物品編碼" prop="itemCode">
        <el-input
          v-model="queryParams.itemCode"
          placeholder="請輸入物品編碼"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物品名稱" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="請輸入物品名稱"
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
      <el-form-item label="狀態" prop="status">
        <el-select v-model="queryParams.status" placeholder="物品狀態" clearable>
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
          @click="handleAdd"
          v-hasPermi="['inventory:item:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['inventory:item:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['inventory:item:remove']"
        >刪除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['inventory:item:export']"
        >匯出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload2"
          size="small"
          @click="handleImport"
          v-hasPermi="['inventory:item:import']"
        >匯入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-camera"
          size="small"
          @click="handleScan"
          v-hasPermi="['inventory:item:scan']"
        >掃描</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="物品編碼" align="center" prop="itemCode" />
      <el-table-column label="物品名稱" align="center" prop="itemName" />
      <el-table-column label="分類" align="center" prop="categoryName" />
      <el-table-column label="規格" align="center" prop="specification" />
      <el-table-column label="單位" align="center" prop="unit" />
      <el-table-column label="品牌" align="center" prop="brand" />
      <el-table-column label="型號" align="center" prop="model" />
      <el-table-column label="庫存數量" align="center" prop="stockQuantity">
        <template #default="scope">
          <span :class="{'text-danger': scope.row.stockQuantity <= scope.row.minStock}">
            {{ scope.row.stockQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="可用數量" align="center" prop="availableQuantity" />
      <el-table-column label="存放位置" align="center" prop="location" />
      <el-table-column label="狀態" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['inventory:item:edit']"
          >修改</el-button>
          <el-button
            size="small"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['inventory:item:remove']"
          >刪除</el-button>
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
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增或修改物品資訊對話框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="物品編碼" prop="itemCode">
              <el-input v-model="form.itemCode" placeholder="請輸入物品編碼" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物品名稱" prop="itemName">
              <el-input v-model="form.itemName" placeholder="請輸入物品名稱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="分類" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="請選擇分類">
                <el-option
                  v-for="category in categoryOptions"
                  :key="category.categoryId"
                  :label="category.categoryName"
                  :value="category.categoryId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="條碼" prop="barcode">
              <el-input v-model="form.barcode" placeholder="請輸入條碼" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="規格" prop="specification">
              <el-input v-model="form.specification" placeholder="請輸入規格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="單位" prop="unit">
              <el-input v-model="form.unit" placeholder="請輸入單位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="請輸入品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型號" prop="model">
              <el-input v-model="form.model" placeholder="請輸入型號" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="採購價格" prop="purchasePrice">
              <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="現價" prop="currentPrice">
              <el-input-number v-model="form.currentPrice" :precision="2" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="最低庫存" prop="minStock">
              <el-input-number v-model="form.minStock" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高庫存" prop="maxStock">
              <el-input-number v-model="form.maxStock" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="供應商" prop="supplier">
              <el-input v-model="form.supplier" placeholder="請輸入供應商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="form.location" placeholder="請輸入存放位置" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="請輸入內容" />
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
          <el-input v-model="form.remark" type="textarea" placeholder="請輸入內容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">確 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 物品匯入對話框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">將檔案拖到此處，或<em>點擊上傳</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" /> 是否更新已經存在的物品資料
          </div>
          <span>僅允許匯入xls、xlsx格式檔案。</span>
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下載範本</el-link>
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
import { listItem, getItem, delItem, addItem, updateItem } from "@/api/inventory/item";
import { getToken } from "@/utils/auth";

export default {
  name: "Item",
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
      // 總條數
      total: 0,
      // 物品資訊表格資料
      itemList: [],
      // 分類選項
      categoryOptions: [],
      // 彈出層標題
      title: "",
      // 是否顯示彈出層
      open: false,
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        itemCode: null,
        itemName: null,
        categoryId: null,
        status: null
      },
      // 表單參數
      form: {},
      // 表單校驗
      rules: {
        itemCode: [
          { required: true, message: "物品編碼不能為空", trigger: "blur" }
        ],
        itemName: [
          { required: true, message: "物品名稱不能為空", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "分類不能為空", trigger: "change" }
        ]
      },
      // 物品匯入參數
      upload: {
        // 是否顯示彈出層（物品匯入）
        open: false,
        // 彈出層標題（物品匯入）
        title: "",
        // 是否禁用上傳
        isUploading: false,
        // 是否更新已經存在的物品資料
        updateSupport: 0,
        // 設定上傳的請求標頭
        headers: { Authorization: "Bearer " + getToken() },
        // 上傳的地址
        url: process.env.VUE_APP_BASE_API + "/inventory/item/importData"
      }
    };
  },
  created() {
    this.getList();
    this.getCategoryList();
  },
  methods: {
    /** 查詢物品資訊列表 */
    getList() {
      this.loading = true;
      listItem(this.queryParams).then(response => {
        this.itemList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 查詢分類列表 */
    getCategoryList() {
      // 這裡需要實現分類查詢API
      // listCategory().then(response => {
      //   this.categoryOptions = response.data;
      // });
    },
    // 取消按鈕
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表單重置
    reset() {
      this.form = {
        itemId: null,
        itemCode: null,
        itemName: null,
        categoryId: null,
        barcode: null,
        qrCode: null,
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
      this.ids = selection.map(item => item.itemId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按鈕操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增物品資訊";
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset();
      const itemId = row.itemId || this.ids
      getItem(itemId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改物品資訊";
      });
    },
    /** 提交按鈕 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.itemId != null) {
            updateItem(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addItem(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 刪除按鈕操作 */
    handleDelete(row) {
      const itemIds = row.itemId || this.ids;
      this.$modal.confirm('是否確認刪除物品資訊編號為"' + itemIds + '"的資料項？').then(function() {
        return delItem(itemIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("刪除成功");
      }).catch(() => {});
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('inventory/item/export', {
        ...this.queryParams
      }, `item_${new Date().getTime()}.xlsx`)
    },
    /** 匯入按鈕操作 */
    handleImport() {
      this.upload.title = "物品匯入";
      this.upload.open = true;
    },
    /** 下載範本操作 */
    importTemplate() {
      this.download('inventory/item/importTemplate', {
      }, `item_template_${new Date().getTime()}.xlsx`)
    },
    // 檔案上傳中處理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 檔案上傳成功處理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "匯入結果", { dangerouslyUseHTMLString: true });
      this.getList();
    },
    // 提交上傳檔案
    submitFileForm() {
      this.$refs.upload.submit();
    },
    /** 掃描按鈕操作 */
    handleScan() {
      this.$router.push('/inventory/scan');
    },
    /** 詳情按鈕操作 */
    handleDetail(row) {
      this.$router.push('/inventory/item/detail/' + row.itemId);
    }
  }
};
</script>

<style scoped>
.text-danger {
  color: #f56c6c;
}
</style>
