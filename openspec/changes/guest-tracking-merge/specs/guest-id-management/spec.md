## ADDED Requirements

### Requirement: 訪客 ID 產生
系統 SHALL 在使用者首次觸發行銷追蹤（瀏覽商品或搜尋）時，檢查是否已有 `guest_tracking_id` Cookie。若無且使用者未登入且已同意行銷 Cookie，則產生 UUID v4 並寫入 `guest_tracking_id` Cookie（有效期 90 天，path=/）。

#### Scenario: 首次訪客瀏覽商品
- **WHEN** 未登入使用者已同意行銷 Cookie，首次瀏覽商品詳情頁，且無 `guest_tracking_id` Cookie
- **THEN** 系統產生 UUID v4 存入 `guest_tracking_id` Cookie（90 天有效期），並使用此 ID 上報瀏覽事件

#### Scenario: 已有 guest_tracking_id
- **WHEN** 未登入使用者已有 `guest_tracking_id` Cookie 且瀏覽商品
- **THEN** 系統使用既有 Cookie 值作為 guestId 上報，不重新產生

#### Scenario: 已登入使用者
- **WHEN** 已登入使用者瀏覽商品
- **THEN** 系統使用 member_id 上報，不產生也不使用 guest_tracking_id

### Requirement: 訪客 ID 歸類為行銷層級
`guest_tracking_id` Cookie SHALL 受行銷同意層級管控。未同意行銷 Cookie 時，不產生也不讀取此 Cookie。

#### Scenario: 未同意行銷
- **WHEN** 使用者未同意行銷 Cookie 且瀏覽商品
- **THEN** 不產生 `guest_tracking_id` Cookie，不上報任何追蹤事件

### Requirement: 登入後清除訪客 Cookie
使用者成功登入後，系統 SHALL 清除 `guest_tracking_id` Cookie（因為後續追蹤改用 member_id）。

#### Scenario: 登入後清除
- **WHEN** 使用者登入成功且有 `guest_tracking_id` Cookie
- **THEN** 系統在觸發合併 API 後清除 `guest_tracking_id` Cookie

### Requirement: 同意撤銷時清除
當使用者將行銷同意從 true 改為 false 時，系統 SHALL 清除 `guest_tracking_id` Cookie。

#### Scenario: 撤銷行銷同意
- **WHEN** 使用者點選「僅必要」且先前有 `guest_tracking_id` Cookie
- **THEN** 系統清除 `guest_tracking_id` Cookie
