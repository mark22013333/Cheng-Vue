<template>
  <div class="category-management">
    <!-- 操作按鈕 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['inventory:category:add']"
        >新增分類
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['inventory:category:remove']"
        >刪除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['inventory:category:export']"
        >匯出Excel
        </el-button>
      </el-col>
    </el-row>

    <!-- 分類列表 -->
    <el-table v-loading="loading" :data="categoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="分類編號" align="center" prop="categoryId" width="100"/>
      <el-table-column label="分類名稱" align="center" prop="categoryName" :show-overflow-tooltip="true"
                       min-width="120"/>
      <el-table-column label="分類編碼" align="center" prop="categoryCode" :show-overflow-tooltip="true" width="120"/>
      <el-table-column label="排序" align="center" prop="sortOrder" width="80"/>
      <el-table-column label="預設分類" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.remark && scope.row.remark.includes('預設分類')" type="success" size="small">
            預設
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="狀態" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="備註" align="center" prop="remark" :show-overflow-tooltip="true" min-width="150"/>
      <el-table-column label="建立者" align="center" prop="createBy" width="100"/>
      <el-table-column label="建立時間" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新時間" align="center" prop="updateTime" width="160">
        <template #default="scope">
          <span v-if="scope.row.updateTime">{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width operation-column" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['inventory:category:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['inventory:category:remove']">刪除</el-button>
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

    <!-- 新增或修改分類對話框 -->
    <el-dialog :title="title" :model-value="open" @update:model-value="val => open = val" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分類名稱" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="請輸入分類名稱" maxlength="50"/>
        </el-form-item>
        <el-form-item label="分類編碼" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="請輸入分類編碼（英文大寫，如：BOOK）" maxlength="30"/>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right"
                           style="width: 150px"/>
        </el-form-item>
        <el-form-item label="設為預設分類" prop="isDefault">
          <el-switch
            v-model="form.isDefault"
            active-text="是"
            inactive-text="否"
            @change="handleDefaultChange"
          ></el-switch>
          <span class="text-muted ml-2"
                style="font-size: 12px;">（預設分類會在新增物品時自動選擇，只能有一個預設分類）</span>
        </el-form-item>
        <el-form-item label="狀態">
          <el-radio-group v-model="form.status">
            <el-radio label="0">啟用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="請輸入備註"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm">確定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listCategory, getCategory, delCategory, addCategory, updateCategory} from "@/api/inventory/category"
import {download} from '@/utils/request'

export default {
  name: "CategoryManagement",
  dicts: ['sys_normal_disable'],
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
      // 總條數
      total: 0,
      // 分類列表
      categoryList: [],
      // 當前預設分類
      currentDefaultCategory: null,
      // 彈出層標題
      title: "",
      // 是否顯示彈出層
      open: false,
      // 下載方法
      download: download,
      // 查詢參數
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        categoryName: null,
        categoryCode: null,
        status: null
      },
      // 表單參數
      form: {},
      // 表單校驗
      rules: {
        categoryName: [
          {required: true, message: "分類名稱不能為空", trigger: "blur"}
        ],
        categoryCode: [
          {required: true, message: "分類編碼不能為空", trigger: "blur"},
          {pattern: /^[A-Z_]+$/, message: "分類編碼只能包含大寫字母和底線", trigger: "blur"}
        ],
        sortOrder: [
          {required: true, message: "排序不能為空", trigger: "blur"}
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查詢分類列表 */
    getList() {
      this.loading = true;
      listCategory(this.queryParams).then(response => {
        this.categoryList = response.rows;
        this.total = response.total;
        // 找出當前預設分類
        this.currentDefaultCategory = this.categoryList.find(cat =>
          cat.remark && cat.remark.includes('預設分類')
        );
        this.loading = false;
      });
    },
    // 取消按鈕
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表單重置
    reset() {
      this.form = {
        categoryId: null,
        categoryName: null,
        categoryCode: null,
        sortOrder: 0,
        status: "0",
        isDefault: false,
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
      this.ids = selection.map(item => item.categoryId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按鈕操作 */
    handleAdd() {
      console.log('[分類管理調試] 點擊新增分類按鈕');
      this.reset();
      console.log('[分類管理調試] 重置表單完成');
      this.open = true;
      console.log('[分類管理調試] 設定 open = true，當前值:', this.open);
      this.title = "新增分類";
      console.log('[分類管理調試] 設定標題:', this.title);
      this.$nextTick(() => {
        console.log('[分類管理調試] DOM 更新後，dialog 是否可見:', this.open);
      });
    },
    /** 修改按鈕操作 */
    handleUpdate(row) {
      this.reset();
      const categoryId = row.categoryId || this.ids[0];
      getCategory(categoryId).then(response => {
        this.form = response.data;
        // 檢查是否為預設分類
        this.form.isDefault = this.form.remark && this.form.remark.includes('預設分類');
        this.open = true;
        this.title = "修改分類";
      });
    },
    /** 處理預設分類切換 */
    handleDefaultChange(value) {
      if (value && this.currentDefaultCategory) {
        // 如果當前已有預設分類，且不是正在編輯的這個分類
        if (!this.form.categoryId || this.form.categoryId !== this.currentDefaultCategory.categoryId) {
          this.$modal.confirm(
            `目前預設分類為「${this.currentDefaultCategory.categoryName}」，確定要切換到「${this.form.categoryName || '此分類'}」嗎？切換後原預設分類將被取消。`
          ).then(() => {
            // 使用者確認，保持開關狀態
            this.form.isDefault = true;
          }).catch(() => {
            // 使用者取消，恢復開關狀態
            this.form.isDefault = false;
          });
        }
      }
    },
    /** 狀態修改 */
    handleStatusChange(row) {
      let text = row.status === "0" ? "啟用" : "停用";
      this.$modal.confirm('確認要"' + text + '""' + row.categoryName + '"分類嗎？').then(function () {
        return updateCategory(row);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function () {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    /** 提交按鈕 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 處理預設分類標記
          if (this.form.isDefault) {
            // 如果設為預設，將「預設分類」加入備註
            const remarkBase = this.form.remark ? this.form.remark.replace(/；?預設分類/g, '').trim() : '';
            this.form.remark = remarkBase ? remarkBase + '；預設分類' : '預設分類';
          } else {
            // 如果取消預設，從備註中移除「預設分類」
            if (this.form.remark) {
              this.form.remark = this.form.remark.replace(/；?預設分類/g, '').trim();
              if (!this.form.remark) {
                this.form.remark = null;
              }
            }
          }

          if (this.form.categoryId != null) {
            updateCategory(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCategory(this.form).then(response => {
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
      const categoryIds = row.categoryId ? [row.categoryId] : this.ids;
      const categoryNames = row.categoryId ? row.categoryName : this.categoryList.filter(c => this.ids.includes(c.categoryId)).map(c => c.categoryName).join('、');
      
      // 先檢查分類是否被物品使用
      this.$modal.confirm(`是否確認刪除分類「${categoryNames}」？刪除前會檢查是否有物品使用此分類。`).then(() => {
        return delCategory(categoryIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("刪除成功");
      }).catch((error) => {
        // 如果是後端返回的錯誤訊息，會自動顯示
      });
    },
    /** 匯出按鈕操作 */
    handleExport() {
      this.download('/inventory/category/export', {
        ...this.queryParams
      }, `分類資料_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped lang="scss">
.category-management {
  padding: 20px;
}

.text-muted {
  color: #909399;
}

.ml-2 {
  margin-left: 8px;
}

</style>
