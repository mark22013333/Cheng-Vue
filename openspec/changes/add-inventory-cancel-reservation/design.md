## Context

現有預約系統已完整支援：預約（PENDING=1）→ 審核通過（APPROVED=2）/ 拒絕（REJECTED=3），以及定時任務過期取消（CANCELLED=4）。取消預約的庫存恢復邏輯（`reserved_qty` → `available_qty`）已存在於 `InvItemServiceImpl.restoreReservedQuantity()`，可直接複用。

通知系統（`SysNotice`）支援程式化建立通知，配合 `SysNoticeRead` 追蹤已讀狀態。但目前沒有「依權限字串查找使用者」的方法，需新增。

## Goals / Non-Goals

**Goals:**
- 使用者可取消自己的待審核預約
- 取消後庫存自動恢復
- 取消後通知具有審核權限的使用者
- 發布 SSE 事件保持前端即時同步

**Non-Goals:**
- 不支援取消已通過（APPROVED）或已拒絕（REJECTED）的預約
- 不修改現有審核流程
- 不新增獨立權限字串（複用 `inventory:management:reserve`）

## Decisions

### 1. API 端點放在 InvManagementController

**選擇**：`POST /inventory/management/cancel-reserve`

**理由**：預約操作（reserve）已在 `InvManagementController`，取消預約是其逆操作，放在同一控制器保持一致。

### 2. 複用現有 restoreReservedQuantity() 方法

**選擇**：呼叫 `InvItemServiceImpl.restoreReservedQuantity()` 恢復庫存

**理由**：此方法已被定時任務（過期取消）使用，邏輯完全一致：減 `reserved_qty`，加 `available_qty`。

### 3. 通知方式：建立 SysNotice 記錄

**選擇**：程式化建立 `SysNotice`（noticeType=1 通知），搭配查詢具有 `inventory:borrow:approve` 權限的使用者

**理由**：複用現有通知系統，不引入新的通知機制。前台已有未讀通知顯示。

**替代方案**：
- WebSocket 推送 — 過度設計，現有系統沒有 WebSocket
- Email 通知 — 不適合內部系統

### 4. 查詢審核權限使用者的方式

**選擇**：新增 `SysUserMapper.selectUserIdsByPermission(String perms)` 方法，透過 `sys_role_menu` + `sys_menu` + `sys_user_role` 三表關聯查找

**理由**：直接查詢資料庫比遍歷所有使用者檢查權限更高效。同時需排除管理員（admin 通常已知悉）和停用帳號。

### 5. 前端按鈕顯示邏輯

**選擇**：在庫存管理頁面的預約記錄中，對自己的待審核預約（`reserve_status=1` 且 `create_by=當前用戶`）顯示取消按鈕

**理由**：只有預約者本人可以取消自己的預約，管理者透過審核拒絕來處理。

## Risks / Trade-offs

- **[風險] 並發問題** → 取消時使用樂觀鎖，確保庫存一致性
- **[權衡] 不新增獨立權限** → 簡化實作，但表示有預約權限的人就能取消預約。這是合理的，因為取消的是自己的預約
- **[權衡] 通知不精確到個人** → 通知所有審核權限使用者而非特定審核人，因為預約尚未分配審核人
