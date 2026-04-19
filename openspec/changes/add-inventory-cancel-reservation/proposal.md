## Why

目前庫存預約流程中，使用者提交預約後無法自行取消，只能被動等待管理者審核拒絕。這導致使用者不再需要預約時，管理者仍須手動處理拒絕操作，浪費雙方時間且庫存無法即時釋放。新增取消預約功能可讓使用者自主管理預約，同時通知審核人員預約已取消。

## What Changes

- 後端新增「取消預約」API 端點（`POST /inventory/management/cancel-reserve`），將待審核的預約狀態更新為已取消（CANCELLED=4），並恢復庫存（`reserved_qty` → `available_qty`）
- 取消預約後，自動建立系統通知（`sys_notice`），發送給具有 `inventory:borrow:approve` 審核權限的角色所屬使用者
- 前端庫存管理頁面（`/cadm/inventory/management`）在預約記錄中新增「取消預約」按鈕，僅對自己的待審核預約顯示
- 發布 `ReservationEvent`（eventType="cancelled"），維持 SSE 即時更新一致性

## Capabilities

### New Capabilities
- `cancel-reservation`: 使用者取消待審核預約的完整流程，包含庫存恢復、通知審核人員、SSE 事件廣播

### Modified Capabilities

（無既有 spec 需修改）

## Impact

- **後端**：`InvManagementController` 新增端點、`InvItemServiceImpl` 新增取消預約邏輯、新增查詢審核權限使用者的 SQL
- **前端**：`inventory/management/index.vue` 新增取消按鈕與 API 呼叫
- **資料庫**：不需要 schema 變更，`reserve_status=4`（CANCELLED）已存在
- **通知系統**：程式化建立 `SysNotice` 記錄，需新增依權限查找使用者 ID 的 Mapper 方法
- **權限**：複用現有 `inventory:management:reserve` 權限（預約者才能取消自己的預約）
