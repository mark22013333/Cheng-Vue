# LINE Controller 標準化說明

## 修改目的

統一所有 LINE 相關 Controller 的實作方式，讓它們都繼承 `BaseController`，以達到：
1. **統一日誌處理**：使用 `BaseController` 提供的 `logger`
2. **統一工具方法**：可使用 `success()`、`error()`、`toAjax()` 等方法
3. **程式碼一致性**：保持與其他系統 Controller 相同的實作風格

## 修改內容

### ✅ 已完成的修改

所有 LINE 相關的 Controller 已統一繼承 `BaseController`：

| Controller | 路徑 | 狀態 | 說明 |
|-----------|------|------|------|
| **LineConfigController** | `/line/config` | ✅ 已完成 | LINE 頻道設定管理 |
| **LineUserController** | `/line/user` | ✅ 已完成 | LINE 使用者管理 |
| **LineMessageController** | `/line/message` | ✅ 已完成 | LINE 訊息管理 |
| **LineWebhookController** | `/webhook/line/{botBasicId}` | ✅ 已完成 | LINE Webhook 接收處理器 |

### 📝 LineWebhookController 特別修改

**修改前**：
```java
@Slf4j
@LineMessageHandler
@RestController
@RequestMapping("/webhook/line")
public class LineWebhookController {
    
    // 使用 Lombok 的 @Slf4j 提供的 log
    log.info("處理事件...");
}
```

**修改後**：
```java
@LineMessageHandler
@RestController
@RequestMapping("/webhook/line")
public class LineWebhookController extends BaseController {
    
    // 使用 BaseController 提供的 logger
    logger.info("處理事件...");
}
```

**修改項目**：
1. ✅ 移除 `@Slf4j` 註解
2. ✅ 移除 `import lombok.extern.slf4j.Slf4j`
3. ✅ 新增 `extends BaseController`
4. ✅ 將所有 `log.` 替換為 `logger.`

## 返回格式說明

### 一般 Controller（返回 AjaxResult）

`LineConfigController`、`LineUserController`、`LineMessageController` 這些 Controller 是給**前端呼叫**的，使用統一的 `AjaxResult` 返回格式：

```java
// 成功返回
return success();
return success("操作成功");
return success(data);

// 失敗返回
return error();
return error("操作失敗");

// 根據結果返回
return toAjax(rows);
return toAjax(result);
```

**返回格式範例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {...}
}
```

### Webhook Controller（返回 ResponseEntity）

`LineWebhookController` 是給 **LINE Platform 呼叫**的，必須返回 LINE Platform 期望的格式，因此保持使用 `ResponseEntity`：

```java
// 成功返回
return ResponseEntity.ok("{\"success\": true}");

// 錯誤返回
return ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body("{\"error\": \"頻道設定不存在\"}");
```

**為什麼不統一為 AjaxResult？**
- LINE Platform 期望的返回格式與系統前端不同
- Webhook 需要快速返回 200 OK 給 LINE Platform
- 錯誤處理需要符合 LINE Platform 的規範

## BaseController 提供的功能

### 1. 日誌記錄
```java
logger.info("資訊日誌");
logger.error("錯誤日誌", exception);
logger.debug("除錯日誌");
logger.warn("警告日誌");
```

### 2. 統一返回方法
```java
success()              // 返回成功
success("訊息")        // 返回成功訊息
success(data)          // 返回成功資料
error()                // 返回失敗
error("訊息")          // 返回失敗訊息
warn("訊息")           // 返回警告訊息
toAjax(rows)          // 根據影響行數返回
toAjax(result)        // 根據布林值返回
```

### 3. 分頁支援
```java
startPage()           // 開始分頁
getDataTable(list)    // 返回分頁資料
clearPage()           // 清理分頁
```

### 4. 使用者資訊
```java
getLoginUser()        // 取得登入使用者
getUserId()           // 取得使用者 ID
getDeptId()           // 取得部門 ID
getUsername()         // 取得使用者名
```

## 優勢總結

### ✨ 統一化優勢

1. **日誌管理統一**：所有 Controller 使用相同的 `logger`，便於日誌追蹤和管理
2. **程式碼風格一致**：與系統其他 Controller（System、Inventory、Monitor 等）保持一致
3. **減少依賴**：移除 Lombok `@Slf4j`，減少註解依賴
4. **工具方法共用**：可直接使用 `BaseController` 提供的工具方法
5. **維護性提升**：未來若需修改基礎功能，只需調整 `BaseController`

### 🎯 保持彈性

- Webhook Controller 保持 `ResponseEntity` 返回格式，符合 LINE Platform 規範
- 其他 Controller 使用 `AjaxResult` 返回格式，符合系統前端規範
- 兩種返回格式並存，各司其職

## 測試建議

修改完成後，建議測試以下功能：

### LINE 管理功能（前端呼叫）
- ✅ LINE 頻道設定的增刪改查
- ✅ LINE 使用者管理
- ✅ LINE 訊息發送

### Webhook 功能（LINE Platform 呼叫）
- ✅ 關注/取消關注事件
- ✅ 訊息接收事件
- ✅ Postback 事件
- ✅ 簽名驗證

## 修改日期

2025-10-29

## 相關檔案

- `/cheng-common/src/main/java/com/cheng/common/core/controller/BaseController.java`
- `/cheng-admin/src/main/java/com/cheng/web/controller/line/LineWebhookController.java`
- `/cheng-admin/src/main/java/com/cheng/web/controller/line/LineConfigController.java`
- `/cheng-admin/src/main/java/com/cheng/web/controller/line/LineUserController.java`
- `/cheng-admin/src/main/java/com/cheng/web/controller/line/LineMessageController.java`
