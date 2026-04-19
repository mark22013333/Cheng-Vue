## ADDED Requirements

### Requirement: 三層同意狀態管理
系統 SHALL 管理三個同意層級：`essential`（必要）、`functional`（功能性）、`marketing`（行銷）。`essential` 層級永遠為 `true`，不可被使用者關閉。`functional` 與 `marketing` 層級預設為 `false`，需使用者明確同意後才啟用。

#### Scenario: 初次訪問未做選擇
- **WHEN** 使用者首次訪問網站，尚未做出任何同意選擇
- **THEN** `essential` 為 `true`，`functional` 為 `false`，`marketing` 為 `false`

#### Scenario: 使用者點選全部接受
- **WHEN** 使用者點選「全部接受」
- **THEN** 三個層級皆設為 `true`，同意狀態持久化至 localStorage

#### Scenario: 使用者點選僅必要
- **WHEN** 使用者點選「僅必要」
- **THEN** `essential` 為 `true`，`functional` 為 `false`，`marketing` 為 `false`，同意狀態持久化至 localStorage

### Requirement: 同意狀態持久化
系統 SHALL 將同意狀態以 JSON 格式存入 localStorage key `cookie_consent`，包含各層級的布林值與同意時間戳。

#### Scenario: 持久化格式
- **WHEN** 使用者做出同意選擇
- **THEN** localStorage 寫入 `{ essential: true, functional: boolean, marketing: boolean, timestamp: number }`

#### Scenario: 頁面重載後恢復狀態
- **WHEN** 使用者已做過同意選擇後重新載入頁面
- **THEN** 系統從 localStorage 讀取並恢復先前的同意狀態，不再顯示提示

### Requirement: 舊版同意遷移
系統 SHALL 偵測舊版 `cookie_consent_accepted` key，若存在則自動遷移為新格式（視為全部接受），並移除舊 key。

#### Scenario: 偵測到舊版同意
- **WHEN** localStorage 存在 `cookie_consent_accepted` key
- **THEN** 系統建立新格式 `cookie_consent`（三層皆 `true`），移除 `cookie_consent_accepted`，不顯示提示

### Requirement: 受保護的儲存 API
系統 SHALL 提供 `guardedStorage(level)` 方法，回傳一個包含 `get`/`set`/`remove` 的物件。當對應層級未被同意時，`set` 操作 SHALL 靜默失敗（不寫入），`get` SHALL 回傳 `null`，`remove` 正常執行。

#### Scenario: 功能性層級未同意時寫入偏好
- **WHEN** `functional` 為 `false` 且程式嘗試透過 `guardedStorage('functional').set('mall_theme', value)` 寫入
- **THEN** 寫入操作靜默失敗，localStorage 中不產生 `mall_theme` key

#### Scenario: 功能性層級已同意時寫入偏好
- **WHEN** `functional` 為 `true` 且程式透過 `guardedStorage('functional').set('mall_theme', value)` 寫入
- **THEN** 正常寫入 localStorage

### Requirement: 拒絕後清除已存在的非必要資料
當使用者選擇「僅必要」時，系統 SHALL 清除所有功能性與行銷層級的 Cookie 與 localStorage 資料。

#### Scenario: 拒絕後清除功能性 Cookie
- **WHEN** 使用者點選「僅必要」且 localStorage 中已有 `mall_theme`、Cookie 中已有 `username`
- **THEN** 系統移除 `mall_theme`（localStorage）、移除 `username`/`password`/`rememberMe`（Cookie），不影響 `Member-Token`
