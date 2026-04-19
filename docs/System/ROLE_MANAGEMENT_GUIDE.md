# 角色管理完整指南

## 📖 目錄
- [角色權限系統架構](#角色權限系統架構)
- [新增角色流程](#新增角色流程)
- [權限字串規範](#權限字串規範)
- [選單權限配置](#選單權限配置)
- [程式碼中使用權限](#程式碼中使用權限)
- [常見問題](#常見問題)

---

## 角色權限系統架構

### 核心資料表

```sql
-- 角色表
sys_role (role_id, role_name, role_key, role_sort, status)

-- 角色選單權限關聯表
sys_role_menu (role_id, menu_id)

-- 使用者角色關聯表
sys_user_role (user_id, role_id)

-- 選單表
sys_menu (menu_id, menu_name, parent_id, perms, menu_type)
```

### 權限驗證流程

```
使用者登入 
  → 取得使用者的角色列表 (sys_user_role)
  → 取得角色的選單權限 (sys_role_menu)
  → 取得選單的權限字串 (sys_menu.perms)
  → Controller 的 @PreAuthorize 驗證權限
```

---

## 新增角色流程

### 1. 後台新增角色

#### 進入路徑
```
系統管理 → 角色管理 → 新增按鈕
```

#### 必填欄位

| 欄位名稱 | 說明 | 範例 | 驗證規則 |
|---------|------|------|---------|
| **角色名稱** | 顯示名稱 | 庫存管理員 | 最多 30 字元 |
| **權限字串** | 程式碼識別用 | `inventory_admin` | 最多 100 字元，建議用底線分隔 |
| **角色順序** | 排序用 | 3 | 數字 |
| **狀態** | 啟用/停用 | 正常 | - |

#### 權限字串命名規範 ⭐

✅ **正確範例**：
```
admin           - 超級管理員
common          - 普通角色
inventory_admin - 庫存管理員
line_operator   - LINE 管理員
report_viewer   - 報表檢視者
marketing       - 行銷人員
warehouse_staff - 倉庫人員
```

❌ **錯誤範例**：
```
Inventory-Admin  - 不要用大寫和連字號
庫存管理員         - 不要用中文
inventory admin  - 不要有空格
inventory.admin  - 避免用點號
```

### 2. 選擇選單權限

勾選該角色可以存取的選單項目：

#### 選單類型說明

- **M (目錄)**：一級選單（如：庫存管理）
- **C (選單)**：二級選單（如：物品與庫存管理）
- **F (按鈕)**：功能權限（如：物品新增、物品刪除）

#### 權限繼承規則

```
✅ 正確：勾選父選單 + 子權限
庫存管理 (M)
  └─ 物品與庫存管理 (C)
       ├─ 物品庫存查詢 (F) ✓
       ├─ 物品新增 (F) ✓
       └─ 物品修改 (F) ✓

❌ 錯誤：只勾選子權限
庫存管理 (M) ✗
  └─ 物品與庫存管理 (C) ✗
       └─ 物品新增 (F) ✓  ← 無法存取，因為父選單未勾選
```

### 3. 指派角色給使用者

```
系統管理 → 使用者管理 → 修改使用者 → 選擇角色
```

---

## 程式碼中使用權限

### 1. 角色權限檢查（@PreAuthorize）

#### 檢查單一角色

```java
@RestController
@RequestMapping("/inventory/category")
public class InvCategoryController {
    
    /**
     * 刪除分類 - 只有管理員可以執行
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(categoryService.deleteByIds(ids));
    }
}
```

#### 檢查多個角色（任一即可）

```java
/**
 * 查看報表 - 管理員或報表檢視者都可以
 */
@PreAuthorize("@ss.hasAnyRoles('admin,report_viewer')")
@GetMapping("/report")
public AjaxResult viewReport() {
    return success(reportService.generateReport());
}
```

### 2. 選單權限檢查（@PreAuthorize）

```java
/**
 * 新增物品 - 必須有「物品新增」權限
 */
@PreAuthorize("@ss.hasPermi('inventory:management:add')")
@PostMapping
public AjaxResult add(@RequestBody InvItem item) {
    return toAjax(itemService.insert(item));
}

/**
 * 匯出資料 - 必須有「物品庫存匯出」權限
 */
@PreAuthorize("@ss.hasPermi('inventory:management:export')")
@PostMapping("/export")
public void export(HttpServletResponse response) {
    // 匯出邏輯
}
```

### 3. 在程式碼中動態檢查權限

```java
import com.cheng.common.utils.SecurityUtils;

// 檢查當前使用者是否有某角色
if (SecurityUtils.hasRole("admin")) {
    // 執行管理員專屬操作
}

// 取得當前使用者的所有角色
List<SysRole> roles = SecurityUtils.getLoginUser().getUser().getRoles();

// 取得當前使用者的權限字串
Set<String> permissions = SecurityUtils.getLoginUser().getPermissions();
```

---

## 選單權限配置

### 目前庫存管理選單結構

```
庫存管理 (M)
├─ 物品與庫存管理 (C) - inventory:management:list
│   ├─ 物品庫存查詢 (F) - inventory:management:query
│   ├─ 物品新增 (F) - inventory:management:add
│   ├─ 物品修改 (F) - inventory:management:edit
│   ├─ 物品刪除 (F) - inventory:management:remove
│   ├─ 物品庫存匯出 (F) - inventory:management:export
│   ├─ 入庫操作 (F) - inventory:management:stockIn
│   ├─ 出庫操作 (F) - inventory:management:stockOut
│   └─ 盤點操作 (F) - inventory:management:stockCheck
├─ 借出管理 (C) - inventory:borrow:list
│   ├─ 借出查詢 (F) - inventory:borrow:query
│   ├─ 借出新增 (F) - inventory:borrow:add
│   ├─ 借出修改 (F) - inventory:borrow:edit
│   ├─ 借出刪除 (F) - inventory:borrow:remove
│   ├─ 借出匯出 (F) - inventory:borrow:export
│   ├─ 借出物品 (F) - inventory:borrow:borrow
│   ├─ 歸還物品 (F) - inventory:borrow:return
│   └─ 審核借出 (F) - inventory:borrow:approve
├─ 掃描功能 (C) - inventory:scan:use
├─ 庫存報表 (C) - inventory:report:view
└─ 物品分類 (C) - inventory:category:list
```

### 權限字串命名規範

```
格式：模組:功能:操作
範例：inventory:management:add
     │        │          └── 操作（add/edit/remove/query/export）
     │        └── 功能（management/borrow/scan）
     └── 模組（inventory/system/line）
```

---

## 常見問題

### Q1: 新增的角色無法看到選單？

**檢查步驟**：
1. 確認角色狀態為「正常」
2. 確認已勾選對應的選單權限
3. 確認使用者已被指派該角色
4. 重新登入系統

### Q2: 權限字串應該用什麼格式？

**建議**：
- 使用小寫字母和底線：`inventory_admin`
- 簡潔明確：`admin`、`common`、`operator`
- 避免中文、空格、特殊符號

### Q3: 如何建立只能查看的角色？

**範例**：報表檢視者

1. 新增角色：`report_viewer`
2. 只勾選查詢類權限：
   - ✅ 物品庫存查詢
   - ✅ 庫存報表查看
   - ❌ 物品新增（不勾選）
   - ❌ 物品刪除（不勾選）

### Q4: 如何讓某功能只有管理員可用？

**在 Controller 加上註解**：

```java
@PreAuthorize("@ss.hasRole('admin')")
@DeleteMapping("/dangerous-operation")
public AjaxResult dangerousOp() {
    // 只有 admin 角色可以執行
}
```

### Q5: 舊的「物品管理」和「庫存查詢」選單還在嗎？

已在 V6 版本合併為「物品與庫存管理」，執行 V20 Migration 可完全移除舊選單：

```bash
# 執行資料庫遷移
# V21__cleanup_old_inventory_menus.sql 會自動清理舊選單
```

---

## 參考資料

### 相關檔案

- **權限服務**：`cheng-framework/src/main/java/com/cheng/framework/web/service/PermissionService.java`
- **安全工具**：`cheng-common/src/main/java/com/cheng/common/utils/SecurityUtils.java`
- **角色實體**：`cheng-common/src/main/java/com/cheng/common/core/domain/entity/SysRole.java`
- **選單 SQL**：`cheng-admin/src/main/resources/db/migration/V6__integrate_inventory_menu.sql`

### 範例 Controller

```java
@RestController
@RequestMapping("/inventory/management")
public class InventoryManagementController extends BaseController {
    
    @Autowired
    private IInvItemService itemService;
    
    /**
     * 查詢物品列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvItem item) {
        startPage();
        List<InvItem> list = itemService.selectList(item);
        return getDataTable(list);
    }
    
    /**
     * 新增物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:add')")
    @Log(title = "物品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody InvItem item) {
        return toAjax(itemService.insert(item));
    }
    
    /**
     * 修改物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:edit')")
    @Log(title = "物品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody InvItem item) {
        return toAjax(itemService.update(item));
    }
    
    /**
     * 刪除物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:remove')")
    @Log(title = "物品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(itemService.deleteByIds(ids));
    }
}
```

---

## 總結

1. **角色新增**：後台建立 → 填寫權限字串 → 選擇選單權限
2. **權限字串**：使用小寫字母和底線，如 `inventory_admin`
3. **程式碼使用**：`@PreAuthorize("@ss.hasRole('角色key')")` 或 `@ss.hasPermi('權限字串')`
4. **選單清理**：執行 V20 Migration 移除舊的「物品管理」和「庫存查詢」選單
