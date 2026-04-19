# LINE 模組權限設計說明

> **文件版本：** 1.0  
> **更新時間：** 2025-10-28  
> **狀態：** 已實作

---

## 權限設計原則

基於「**最小權限原則**」和「**職責分離**」，將 LINE 模組權限分為三個層級：

1. **查看層級** - 可以查看資料，但不能修改
2. **操作層級** - 可以執行關鍵操作（如發送訊息）
3. **管理層級** - 可以修改設定和刪除資料

---

## 權限清單

### 1. LINE 頻道設定權限

| 權限代碼 | 權限名稱 | 等級 | 說明 |
|---------|---------|------|------|
| `line:config:list` | 頻道列表查看 | 查看 | 查看 LINE 頻道設定列表 |
| `line:config:query` | 頻道詳情查看 | 查看 | 查看頻道詳細資訊（敏感資料會遮罩）|
| `line:config:add` | 新增頻道 | 管理 | 新增副頻道或測試頻道 |
| `line:config:edit` | 編輯頻道 | 管理 | 修改頻道設定 |
| `line:config:remove` | 刪除頻道 | 管理 | 刪除頻道設定 |
| `line:config:test` | 連線測試 | 操作 | 測試頻道連線和 Webhook |

### 2. 推播訊息權限（重點）

| 權限代碼 | 權限名稱 | 等級 | 說明 |
|---------|---------|------|------|
| `line:message:list` | 訊息列表查看 | ✅ 查看 | 查看推播訊息記錄列表 |
| `line:message:query` | 訊息詳情查看 | ✅ 查看 | 查看訊息詳細內容 |
| `line:message:export` | 匯出訊息記錄 | ✅ 查看 | 匯出訊息記錄為 Excel |
| `line:message:send` | ⚠️ **發送訊息** | 🔒 操作 | **發送推播訊息（重要權限）** |
| `line:message:remove` | 刪除訊息記錄 | 管理 | 刪除訊息記錄 |

### 3. LINE 使用者管理權限

| 權限代碼 | 權限名稱 | 等級 | 說明 |
|---------|---------|------|------|
| `line:user:list` | 使用者列表查看 | 查看 | 查看 LINE 使用者列表 |
| `line:user:query` | 使用者詳情查看 | 查看 | 查看使用者詳細資訊 |
| `line:user:bind` | 綁定系統使用者 | 操作 | 綁定/解綁 LINE 使用者與系統帳號 |
| `line:user:export` | 匯出使用者資料 | 查看 | 匯出使用者資料 |

---

## 角色配置範例

### 1. 管理員（admin）
**擁有所有權限**

```yaml
權限:
  - 所有 line:config:* 權限
  - 所有 line:message:* 權限
  - 所有 line:user:* 權限
  
適用人員: 系統管理員、IT 負責人
```

### 2. 行銷主管（marketing_manager）
**擁有發送權限**

```yaml
權限:
  - line:config:query （查看頻道設定）
  - line:message:* （所有推播訊息權限）
  - line:user:list, query, export （使用者查看權限）
  
適用人員: 行銷主管、有權發送訊息的人員
風險等級: ⚠️ 高（可發送訊息給所有使用者）
```

### 3. 行銷人員（marketing）
**只有查看權限，無發送權限**

```yaml
權限:
  - line:message:list （查看訊息列表）
  - line:message:query （查看訊息詳情）
  - line:message:export （匯出訊息記錄）
  - line:user:list （查看使用者列表）
  - line:user:query （查看使用者詳情）
  
適用人員: 行銷專員、資料分析人員
風險等級: ✅ 低（只能查看，不能操作）
```

### 4. 客服人員（customer_service）
**只有使用者查看和綁定權限**

```yaml
權限:
  - line:user:list （查看使用者列表）
  - line:user:query （查看使用者詳情）
  - line:user:bind （綁定系統帳號）
  
適用人員: 客服人員
風險等級: ✅ 低
```

---

## 權限控制實作

### 後端 Controller 層

```java
// 查看權限 - 一般使用者可擁有
@PreAuthorize("@ss.hasPermi('line:message:list')")
@GetMapping("/list")
public TableDataInfo list(LineMessageLog lineMessageLog) {
    // ...
}

// 發送權限 - 需謹慎授予
@PreAuthorize("@ss.hasPermi('line:message:send')")
@PostMapping("/push")
public AjaxResult push(@RequestBody PushMessageDTO dto) {
    // ...
}
```

### 前端按鈕控制

```vue
<!-- 發送按鈕 - 只有擁有 send 權限的人才能看到 -->
<el-button
  v-hasPermi="['line:message:send']"
  type="primary"
  @click="handleSend">
  發送訊息
</el-button>

<!-- 查看按鈕 - 擁有 query 權限即可看到 -->
<el-button
  v-hasPermi="['line:message:query']"
  type="info"
  @click="handleView">
  查看詳情
</el-button>
```

---

## 安全建議

### 1. 發送權限管理
- ⚠️ **`line:message:send` 權限極為重要**，建議只授予信任的人員
- 定期審查擁有發送權限的使用者清單
- 考慮實作「雙人審核」機制（草稿 → 審核 → 發送）

### 2. 操作日誌
- 所有發送操作都會記錄在 `sys_oper_log` 表
- 記錄發送人、發送時間、目標數量、訊息內容
- 定期檢查操作日誌，發現異常立即處理

### 3. 訊息內容審核
- 考慮實作敏感詞過濾
- 大量推播前建議先測試發送給少數使用者
- 重要訊息建議使用草稿功能，經審核後再發送

### 4. 頻率限制
- 考慮實作發送頻率限制（例如：每小時最多發送 10 次）
- 避免誤操作導致大量重複訊息
- LINE 官方也有發送額度限制，需注意

---

## 權限配置步驟

### 方法一：透過後台介面配置

1. **登入系統** → 系統管理 → 角色管理
2. **編輯角色** → 選擇要配置的角色
3. **展開選單權限** → 勾選 LINE 模組相關權限
4. **儲存** → 權限立即生效

### 方法二：透過 SQL 直接配置

```sql
-- 為指定角色新增發送權限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id  -- 假設 role_id = 2 是行銷主管
FROM sys_menu
WHERE perms = 'line:message:send';

-- 移除發送權限
DELETE FROM sys_role_menu
WHERE role_id = 3  -- 假設 role_id = 3 是行銷人員
  AND menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms = 'line:message:send'
);
```

---

## 測試檢查清單

- [ ] 擁有 `line:message:list` 權限的使用者可以查看訊息列表
- [ ] 沒有 `line:message:send` 權限的使用者看不到「發送訊息」按鈕
- [ ] 嘗試直接呼叫 API 發送訊息，沒有權限會被拒絕（403 Forbidden）
- [ ] 操作日誌正確記錄發送人和操作內容
- [ ] 行銷人員角色只能查看，不能發送
- [ ] 管理員角色擁有所有權限

---

## 常見問題

### Q1: 如何授予某個使用者發送權限？
**A:** 有兩種方式：
1. 將使用者加入擁有發送權限的角色（例如：行銷主管）
2. 為使用者的現有角色新增 `line:message:send` 權限

### Q2: 權限修改後需要重新登入嗎？
**A:** 是的，權限變更後需要使用者重新登入才會生效。或者可以使用「強制登出」功能讓使用者重新登入。

### Q3: 可以設定「只能發送給特定群組」的權限嗎？
**A:** 目前版本尚未實作資料權限控制。如需此功能，可以：
1. 建立不同的頻道（副頻道）給不同的使用者使用
2. 在程式碼層面實作資料權限過濾

### Q4: 如何實作「雙人審核」機制？
**A:** 建議的實作方式：
1. 新增 `line:message:draft` 權限（建立草稿）
2. 新增 `line:message:approve` 權限（審核草稿）
3. 新增 `line:message:send` 權限（發送已審核的訊息）
4. 在資料表中新增 `approve_status` 欄位

---

## 變更記錄

| 版本 | 日期 | 說明 |
|-----|------|------|
| 1.0 | 2025-10-28 | 初版，建立權限設計文件 |

---

## 相關文件

- [LINE 模組概述](./LINE_MODULE_OVERVIEW.md)
- [LINE 模組開發進度](./LINE_MODULE_PROGRESS.md)
- [LINE SDK 升級指南](./LINE_SDK_9_UPGRADE_GUIDE.md)
