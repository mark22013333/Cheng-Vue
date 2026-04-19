## Context

CoolApps 商城後端為 Spring Boot 3.5 + MyBatis + MySQL 8，前端為 Vue 3 + Pinia。現有行銷追蹤僅在前端 localStorage，已登入會員的瀏覽行為無法跨裝置、無法用於後端推薦。

後端慣例：
- Controller 使用 `@Anonymous` + `@PublicApi` 標記公開 API，路由前綴 `/shop/front` 或 `/shop/member`
- 回傳格式：`AjaxResult`（單筆）、`TableDataInfo`（分頁）
- Flyway 最新版本 V46，新遷移為 V47
- 表名前綴 `shop_`，主鍵 `BIGINT AUTO_INCREMENT`

## Goals / Non-Goals

**Goals:**
- 將已登入會員的瀏覽足跡與搜尋記錄持久化至 MySQL
- 提供查詢 API：最近瀏覽、個人熱門分類、熱門搜尋關鍵字
- 前端 `useMarketingTracker` 在已登入狀態下非同步雙寫（localStorage + API）
- 高頻寫入場景下確保不影響使用者體驗（非同步、fire-and-forget）

**Non-Goals:**
- 不建立推薦演算法（本階段只收集資料）
- 不做即時分析 dashboard（未來可接 BI 工具）
- 不處理訪客行為持久化（訪客仍用 localStorage，登入後不回溯合併）
- 不做資料老化自動清理（初期資料量不大，後續可加排程）

## Decisions

### 1. 表結構設計：寬表 vs 事件表

**選擇**：兩張獨立寬表（`shop_browsing_logs` + `shop_search_logs`），非通用事件表

```sql
shop_browsing_logs
├── log_id (PK)
├── member_id (FK → shop_members)
├── product_id (FK → shop_products)
├── product_name (冗餘，避免 JOIN)
├── category_id
├── source (來源：DIRECT/SEARCH/RECOMMEND/CATEGORY)
├── create_time

shop_search_logs
├── log_id (PK)
├── member_id (FK → shop_members)
├── keyword
├── result_count (搜尋結果數量)
├── create_time
```

**理由**：寬表查詢效能好，不需要 JSON 解析；兩種行為結構差異大，拆分更清晰。通用事件表（`event_type` + `event_data` JSON）雖靈活，但查詢複雜度高、索引效率差。

**替代方案**：
- 通用事件表：靈活但查詢效能差，MySQL JSON 索引不如原生欄位
- MongoDB：更適合事件流，但專案已用 MySQL，不引入新依賴

### 2. 寫入策略：同步 vs 非同步

**選擇**：前端 fire-and-forget（不等待回應），後端 `@Async` 非同步寫入

```
前端 trackProductView()
  → localStorage 寫入（即時，確保本地快取可用）
  → fetch POST /shop/member/tracking/browse（fire-and-forget，不 await）
      → 後端 Controller 接收
        → @Async Service 寫入 DB
```

**理由**：瀏覽記錄是高頻低價值操作（丟幾條不影響業務），不值得阻塞使用者操作或增加請求延遲。

**替代方案**：
- 前端批次上報（累積 N 條或 T 秒後批次發送）：更省請求數，但增加前端複雜度，頁面關閉時可能遺失。初期單量不大，不需優化
- 後端 MQ（Kafka/RabbitMQ）：過度工程，日活千級不需要

### 3. API 路由設計

**選擇**：寫入走 `/shop/member/tracking/*`（需登入），查詢走 `/shop/front/tracking/*`（部分公開）

| 端點 | 方法 | 認證 | 說明 |
|------|------|------|------|
| `/shop/member/tracking/browse` | POST | 需登入 | 上報瀏覽事件 |
| `/shop/member/tracking/search` | POST | 需登入 | 上報搜尋事件 |
| `/shop/member/tracking/recent-views` | GET | 需登入 | 查詢個人最近瀏覽 |
| `/shop/front/tracking/popular-searches` | GET | 公開 | 熱門搜尋關鍵字 |
| `/shop/front/tracking/hot-products` | GET | 公開 | 熱門瀏覽商品 |

**理由**：寫入必須關聯 member_id 所以需登入；熱門統計是公開資訊可匿名存取。

### 4. 前端同步時機

**選擇**：修改 `useMarketingTracker`，增加 `syncToBackend` 邏輯，判斷已登入（有 Member-Token）時呼叫 API

**理由**：最小修改，不改變現有 localStorage 邏輯，只在上層加一個 API 呼叫。

## Risks / Trade-offs

- **[重複記錄]** → 同一使用者短時間內重複瀏覽同一商品會產生多條記錄。初期可接受（反映真實瀏覽頻率），後續可在查詢層去重或加 5 分鐘防抖
- **[資料量增長]** → 瀏覽記錄長期累積會佔用儲存空間。後續可加排程任務清理 90 天以上的舊記錄，或做聚合統計後刪除明細
- **[API 失敗]** → 後端不可用時前端上報會失敗，但 localStorage 仍有資料，使用者體驗不受影響。前端使用 fire-and-forget 不重試
- **[冗餘欄位]** → `product_name` 冗餘存儲，商品改名後舊記錄不更新。可接受，瀏覽記錄是歷史快照
