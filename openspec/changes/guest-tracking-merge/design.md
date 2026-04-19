## Context

CoolApps 商城已有 `shop_browsing_logs` + `shop_search_logs` 表，但 `member_id` 為 NOT NULL，僅追蹤已登入會員。前端 `useMarketingTracker` 在有 `Member-Token` 時才呼叫 API。需擴展支援訪客追蹤，並在登入後合併。

現有技術棧：Spring Boot 3.5 + MyBatis + MySQL 8 + Vue 3 + Pinia。
Cookie 同意架構已有三層（essential/functional/marketing）。

## Goals / Non-Goals

**Goals:**
- 訪客（未登入）瀏覽和搜尋行為也能持久化至 DB
- 訪客登入後，自動將 `guest_id` 的歷史記錄合併至 `member_id`
- `guest_tracking_id` Cookie 受行銷同意層級管控
- 熱門商品/搜尋統計包含訪客資料

**Non-Goals:**
- 不做跨裝置訪客合併（同一訪客換裝置視為不同人）
- 不建立訪客畫像分析系統
- 不做 guest_id 的後端 session 管理（純前端 Cookie）
- 不處理 Cookie 過期後的 guest_id 重置（自然過期即可）

## Decisions

### 1. Guest ID 策略：UUID v4 存 Cookie

**選擇**：前端用 `crypto.randomUUID()` 產生 UUID，存入 `guest_tracking_id` Cookie（90 天有效期）。此 Cookie 歸類為行銷層級，需使用者同意。

**理由**：UUID 無碰撞風險、不含個資、前端自主生成不需後端參與。90 天效期足夠覆蓋大多數購物決策週期。

**替代方案**：
- 後端生成 session-based ID：增加 API 呼叫，首次造訪延遲
- localStorage 存 ID：跨子域名無法共享，且清快取即消失；Cookie 可跨 path 且更穩定

### 2. DB 結構調整：ALTER TABLE + 新索引

**選擇**：
```sql
ALTER TABLE shop_browsing_logs
  ADD COLUMN guest_id VARCHAR(36) NULL AFTER member_id,
  MODIFY COLUMN member_id BIGINT NULL;

ALTER TABLE shop_search_logs
  ADD COLUMN guest_id VARCHAR(36) NULL AFTER member_id,
  MODIFY COLUMN member_id BIGINT NULL;
```
新增 CHECK 約束確保 `member_id` 和 `guest_id` 至少有一個非 NULL。

**理由**：最小化結構變更，不影響現有已登入會員的記錄（member_id 仍有值）。

### 3. API 改為公開 + 智能識別

**選擇**：追蹤上報端點改為 `@Anonymous`（公開），後端邏輯：
1. 檢查 request header 是否有有效的 Member-Token → 有則取 `member_id`
2. 否則從 request body 取 `guestId` → 寫入 `guest_id` 欄位
3. 兩者皆無 → 拒絕（400）

**理由**：單一端點同時服務訪客和會員，前端邏輯最簡單。已登入時仍優先用 member_id。

**替代方案**：
- 分開兩套端點（`/guest/tracking` + `/member/tracking`）：維護成本高
- 全部走 guestId，登入後才合併：即時性差

### 4. 合併時機：登入 API 回應後觸發

**選擇**：前端在收到登入成功回應後，呼叫 `POST /shop/member/tracking/merge` 帶上 `guestId`。後端將該 `guest_id` 的所有記錄的 `member_id` UPDATE 為登入會員的 ID，並清空 `guest_id`。前端同時清除 `guest_tracking_id` Cookie。

**理由**：合併在登入瞬間完成，使用者登入後立即可在「最近瀏覽」看到訪客時期的記錄。

**替代方案**：
- 後端 Login API 內自動合併：需在 auth 層拿到 guestId，跨模組耦合
- 非同步排程合併：延遲過大，使用者體驗差

## Risks / Trade-offs

- **[UUID 碰撞]** → UUID v4 碰撞機率極低（2^122），可忽略。
- **[Cookie 被清除]** → 訪客清除 Cookie 後會產生新 guest_id，舊記錄成為孤立資料。可接受，定期清理即可。
- **[合併 API 被濫用]** → 任何人可呼叫合併 API 將 guest_id 歸屬到自己。實際影響低（只是瀏覽/搜尋記錄），且合併 API 需登入。
- **[大量 UPDATE]** → 合併時可能 UPDATE 大量行。實際上訪客在登入前不太可能有超過幾百條記錄，效能可接受。
