## 1. 後端 - 查詢審核權限使用者

- [x] 1.1 在 `SysUserMapper` 新增 `selectUserIdsByPermission(String perms)` 方法，透過 `sys_menu` + `sys_role_menu` + `sys_user_role` 關聯查詢具有指定權限的使用者 ID 列表
- [x] 1.2 在 `SysUserMapper.xml` 實作對應的 SQL 查詢（排除停用帳號）
- [x] 1.3 在 `ISysUserService` / `SysUserServiceImpl` 暴露此方法

## 2. 後端 - 取消預約核心邏輯

- [x] 2.1 在 `InvItemServiceImpl` 新增 `cancelReservation(Long borrowId)` 方法：驗證預約存在、狀態為待審核、為本人建立；更新狀態為 CANCELLED；呼叫 `restoreReservedQuantity()` 恢復庫存
- [x] 2.2 取消成功後發布 `ReservationEvent`（eventType="cancelled"）
- [x] 2.3 取消成功後建立 `SysNotice`（noticeType=1），標題含物品名與取消者，發送給審核權限使用者

## 3. 後端 - API 端點

- [x] 3.1 在 `InvManagementController` 新增 `POST /inventory/management/cancel-reserve` 端點，接收 `itemId` 參數，權限為 `inventory:management:reserve`
- [x] 3.2 回傳取消結果（包含 availableQty、reservedQty 供前端更新）

## 4. 前端 - 取消按鈕

- [x] 4.1 在 `management.js` API 層新增 `cancelReserveItem(itemId)` 方法
- [x] 4.2 在庫存管理頁面的操作欄新增「取消預約」按鈕，顯示條件：`reservedQty > 0`，權限 `inventory:management:reserve`
- [x] 4.3 點擊按鈕後顯示確認對話框，確認後呼叫 API，成功後重新載入列表

## 5. 驗證

- [x] 5.1 編譯後端，確認無錯誤
- [ ] 5.2 測試取消預約流程：建立預約 → 取消 → 確認庫存恢復 + 通知產生
- [ ] 5.3 測試權限控制：嘗試取消他人預約，確認被拒絕
