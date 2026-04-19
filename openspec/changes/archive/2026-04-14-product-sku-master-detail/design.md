# Design — Product SKU Master-Detail 重構

## 決策 1：為何採 Master-Detail，不用 Tabs 或摺疊卡片

### 選項比較

| 方案 | 優點 | 缺點 |
|------|------|------|
| **A. Master-Detail**（選定） | 一眼看全部 SKU；切換編輯焦點快；左側可快速排序/刪除；右側聚焦單一規格，錯誤訊息不互相干擾 | 需新增元件；需管理選中狀態 |
| B. Tabs（頂部一排 `#1 #2 #3`） | 元件現成；切換直觀 | SKU 多時 tab bar 爆掉；看不到所有 SKU 的概覽；啟用/售價必須點進去才看到 |
| C. 每張 SkuCard 可折疊 | 改動最小 | 折疊後資訊密度太低；展開幾張就回到原本的問題；使用者會忘記摺回去 |

### 選 A 的決定性理由

使用者的原話是「**我要左邊列表，當點一個物品後右邊才顯示可編輯的畫面**」— 直接指定了 master-detail。此外：

1. **概覽與編輯分離** — 左側清單扮演「目錄」，右側是「工作檯」，符合電商後台管理多個規格時的心智模型（類 Shopify / Woo 的 variant list）。
2. **錯誤定位明確** — 驗證失敗時左側清單列可用紅點/紅邊框標示，使用者點擊即可跳到右側修正，不需滾動找到出錯的卡片。
3. **垂直空間無壓力** — 無論 SKU 有 2 個還是 20 個，整個區塊的視覺高度恆定（約 480px 左側清單 + 右側表單），不會撐開整個 Accordion。

## 決策 2：選中狀態存在哪

### 候選

- (a) `ProductSkuSection` 本地 `ref`
- (b) `useProductForm` composable 統一管理
- (c) URL query string

### 選 (a) — 本地 `ref`

- 選中只是 UI 視覺焦點，和業務資料無關（`skuList` 本身仍是 single source of truth）
- 不需要跨 section 同步，沒有必要污染 composable
- 不需要 deep link（商品編輯頁沒有外部連結需要跳到特定 SKU）

選 (a) 也有明確的邊界：當 `skuList` 從外部（例如匯入）新增時，`ProductSkuSection` 透過 `watch` 把 `selectedIndex` 設為新增項的 index；當外部刪除時，shift 到相鄰項。這個同步邏輯寫在 `ProductSkuSection` 內就夠了。

## 決策 3：SkuCard 的 `hideActions` vs 拆出新元件

### 為何選 `hideActions` prop 而非新增 `SkuCardCompact`

SkuCard 的 **body 部分**（價格、庫存、規格欄位）在 master-detail 中完全保留，只有 **header 的 switch + delete** 要隱藏（因為搬到左側清單）。拆新元件會導致兩份幾乎相同的 template 難以同步維護，而 `v-if` 隱藏兩個按鈕的成本極低。

### 隱藏的是「視覺」還是「功能」

視覺隱藏，功能保留。SkuCard 內部的 `sku.status` v-model 在左側 SkuListItem 也會綁到同一個物件，狀態仍在 `skuList[index].status` 這個單一來源上同步。`emit('remove')` 在 master-detail 模式下不從 SkuCard 觸發，改從 SkuListItem 觸發。

## 決策 4：已關聯庫存的下拉 — 唯讀 vs 解除關聯按鈕

使用者的抱怨：「既然我已經從庫存匯入了，那應該不用再有關聯庫存物品的下拉選單」。

### 方案比較

| 方案 | 說明 | 判斷 |
|------|------|------|
| (X) 完全隱藏關聯區塊 | `v-if="!(stockMode === 'LINKED' && invItemId)"` 把整塊拿掉 | 使用者會失去「這個 SKU 關聯到哪個庫存物品」的可視資訊 |
| (Y) 顯示唯讀文字 + 解除關聯按鈕 | 顯示 `已關聯：Fisher-Price（FP-001）` + `[解除關聯]` 小按鈕 | ✅ 資訊保留、操作明確、不誤導 |
| (Z) 顯示禁用下拉 | `el-select :disabled="true"` | 仍像下拉，使用者會以為 disabled 是 bug |

選 (Y)。使用者能看到關聯關係（對帳、排查問題時很重要），但不會被引導去「換一個」。真的需要換時按「解除關聯」，意圖從「改選」降級為「清空 → 重選」兩步操作，這是刻意的 friction，防止誤換。

### 「解除關聯」清空哪些欄位

```
sku.invItemId = undefined
sku.invItemName = undefined
sku.invItemCode = undefined
sku.refPrice = undefined
sku.refCost = undefined
```

**保留**：`sku.price`, `sku.costPrice`, `sku.stockQuantity`, `sku.skuName`, `sku.skuCode`。這些是使用者已經在右側編輯過的業務資料，清掉等於報復性的資料遺失。解除關聯不等於重置 SKU，只是斷開與庫存物品的綁定。

## 決策 5：價格防呆規則 — 閾值與加成比例

### 為何閾值是 100

來自使用者的明確指示：「**當匯入庫存的售價小於 100 的時候，一律都先抓原價**」。100 這個門檻的業務含義：

- 小於 100 的品項在本系統的銷售定位中幾乎不存在（最低消費單位是零食包裝，也是 $99 起跳）
- 設為 $1、$30 的幾乎都是：未定價、測試資料、批發內購價
- 大於 100 的就算偏低也有事務意圖（特價、清倉），不該自動覆蓋

### 為何是 `purchasePrice × 1.2` 而不是其他倍率

系統沒有 `originalPrice` 欄位在庫存層，只有 `purchasePrice`（採購成本）。使用者確認「B 選項」= 用採購成本推算原價。20% 加成是零售業界常見的保守毛利率起點，確保上架後至少不虧本。

這個倍率**定義在 `priceSafeguard.js` 的常數**，未來若需要調整（例如改 25%）只需改一個地方。

### 邊界處理（使用者明確要求）

> 確認點2:B選項，但要注意如果為0以下的情況

```
if (current >= 100) return current             // 信任
if (purchase > 0)   return round(purchase*1.2) // 成本加成
return 0                                       // 強制使用者手動處理
```

`purchase <= 0` 時**不嘗試猜測**，而是回傳 0 並讓既有的 SKU 驗證（`validateSkuList()` 的 S3 規則會要求 price 非空非負）在使用者試圖儲存時擋下。這是「不猜測即安全」的原則，系統寧願讓使用者被打斷也不要替他送出一個虧本的定價。

### 為何不在後端做

- 匯入動作發生在前端多步互動（選擇 → 預覽 → 微調），使用者需要在預覽時就看到調整後的數字並能覆寫
- 後端 `POST /shop/product` 只看到最終的 `skuList`，無法分辨「這個 price 是從庫存匯入的」還是「使用者親手輸入的」
- 使用者覆寫後若還是低價（例如真的要賣 $50），應該信任使用者意圖
- 故防呆規則屬於「表現層決策」，落在前端合理

## 決策 6：防呆提示 UI 的顯示時機

### 規則

在 Preview 卡片的售價欄位旁邊，當且僅當下列條件成立時顯示 `⚠ 已套用防呆（原庫存現價 $X 過低）`：

```
importOptions.syncPrice === true
&& item.currentPrice < 100
&& item.editPrice !== item.currentPrice
```

第三個條件是為了讓使用者手動把 `editPrice` 改回 $50 時（明確覆寫防呆），提示消失，不再干擾。

### 為何不用 el-alert 放在 Dialog 頂部

- 單一品項層級的提示應該貼在該品項旁邊，否則使用者不知道是哪一個出問題
- 多個品項都觸發防呆時，頂部 alert 會模糊焦點，反而不如行級 icon + tooltip

## 風險與回退

### 風險 1：左側清單太窄，規格名稱被截斷

規格名稱最多 100 字，280px 顯然不夠顯示全名。處理：
- 左側只顯示前 20-25 字 + `...`
- 整行 `title` 屬性顯示完整名稱，hover 時瀏覽器原生 tooltip
- 若使用者需要看全名可點擊切換到右側編輯區

### 風險 2：Accordion 收合時左側的卷軸狀態

`ProductSkuSection` 在收合時不渲染 body（由 `AccordionSection` 控制），展開時重新渲染。`selectedIndex` 使用 `ref` 存在元件實例上，Accordion 不 unmount 的話狀態會保留；若 unmount 則回到預設 `0`。這是可接受的行為。

### 風險 3：防呆誤傷正常低價商品

例如促銷品真的賣 $88。處理路徑：使用者在 Preview 看到 `editPrice` 被改成 `Math.round(purchasePrice * 1.2)`，可手動改回 $88 → 提示消失 → 確認匯入。**防呆不鎖 UI**，只改預設值並提示。

### 回退策略

若 master-detail 佈局在實際使用後發現問題，可透過在 `ProductSkuSection` 內新增 `layoutMode` prop（`'master-detail' | 'stack'`）並預設 `'master-detail'`，讓切回舊版只需改一個 prop。此回退路徑不影響資料結構，零風險。

防呆規則若要關閉，將 `safeSkuPriceFromInventory` 的實作改為 `return current` 即可完全禁用，不需改呼叫端。
