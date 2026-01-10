/**
 * 系統更新日誌資料模組
 */

/**
 * 設定檔：定義標籤類型與顏色的對應關係 (Style Mapping)
 */
const LOG_STYLES = {
  LATEST: {tag: "最新版本", tagType: "success", color: "#67C23A", icon: "el-icon-s-flag"},
  INITIAL: {tag: "首發版本", tagType: "danger", color: "#F56C6C", icon: "el-icon-trophy"},
  MAJOR: {tag: "重大更新", tagType: "warning", color: "#E6A23C", icon: "el-icon-star-on"},
  SECURE: {tag: "安全更新", tagType: "danger", color: "#F56C6C", icon: "el-icon-lock"},
  STABLE: {tag: "穩定版", tagType: "success", color: "#409EFF", icon: "el-icon-circle-check"},
  FEATURE: {tag: "功能更新", tagType: "primary", color: "#409EFF", icon: "el-icon-circle-plus"},
  FIX: {tag: "問題修復", tagType: "info", color: "#909399", icon: "el-icon-s-tools"},
  HISTORY: {tag: "歷史版本", tagType: "info", color: "#C0C4CC", icon: "el-icon-time"}
};

/**
 * 原始資料 (Raw Data)
 * 僅需維護 version, date, items，其餘樣式由演算法自動產生
 *
 * ⚠️ 重要：第一個 version 欄位會被 build-backend.sh 自動解析作為建置版本號
 *    格式必須是：version: "vX.Y.Z"（使用雙引號）
 */
const rawLogs = [
  {
    "version": "v2.3.6",
    "date": "2026-01-10",
    "items": [
      "LINE 訊息功能大升級：正式支援「模板訊息 (Template Message)」，包含按鈕 (Buttons)、確認 (Confirm)、輪播 (Carousel) 與圖片輪播 (Image Carousel) 等互動樣式。",
      "Flex 範本管理優化：新增「我的範本」與「共用範本」分類，您可以將設計好的 Flex 訊息直接儲存為個人範本，或設為公開供團隊使用。",
      "權限控管強化：發送訊息與測試發送功能加入權限驗證，確保僅有授權人員可執行推播操作。",
      "範本資訊更透明：範本列表現在會顯示建立者名稱，並支援透過建立者進行搜尋篩選。",
      "編輯器體驗改善：優化訊息預覽功能，並修正部分欄位驗證邏輯，提升編輯流暢度。"
    ]
  },
  {
    "version": "v2.3.3",
    "date": "2025-12-29",
    "items": [
      "LINE 訊息範本編輯器重大更新：新增「快速回覆 (Quick Reply)」功能，現在可以為文字訊息附加最多 13 個快速操作按鈕。",
      "快速回覆支援多種動作類型，包含：發送文字、開啟連結、回傳資料 (Postback)、選擇日期時間、開啟相機/相簿、傳送位置與複製文字。",
      "訊息預覽功能升級，編輯時可即時查看快速回覆按鈕在手機上的呈現樣式。",
      "優化 Flex Message 編輯體驗，修復了切換或儲存後範本下拉選單會被重置的問題。",
      "圖文選單 (Imagemap) 編輯器改進，增強了連結網址的格式檢查，防止設定錯誤的連結協議。"
    ]
  },
  {
    "version": "v2.3.2",
    "date": "2025-12-27",
    "items": [
      "Flex 訊息預覽大幅升級：現在支援 Carousel 輪播格式預覽，可透過介面左右按鈕或滑動查看多張卡片細節。",
      "編輯功能優化：新增「訊息拖曳排序」功能，長按拖曳圖示即可輕鬆調整訊息發送順序，操作更直覺。",
      "新增貼圖預覽：在編輯 LINE 訊息範本時，現在可以直接預覽貼圖 (Sticker) 的顯示效果。",
      "介面體驗改善：建立新範本時將以乾淨的空白列表開始，不再預設帶有空白文字訊息，讓編輯流程更順暢。"
    ]
  },
  {
    "version": "v2.3.0",
    "date": "2025-12-27",
    "items": [
      "新增「圖文訊息 (Imagemap)」管理功能：支援後台直接上傳背景圖片，系統自動裁切為 LINE 所需的 5 種尺寸。",
      "強大的圖文編輯器：提供視覺化操作介面，可直接在圖片上拖曳設定點擊熱區，並具備區域重疊防呆檢測。",
      "範本連動更新機制：當圖文範本修改時，可一鍵同步更新所有引用此圖文的推播訊息，無需逐一修改。",
      "Rich Menu 功能擴充：新增支援「開啟相機」、「開啟相簿」、「傳送位置」及「複製文字」等點擊動作。",
      "文字訊息升級：新增 Emoji 表情符號選擇器，讓推播訊息更加生動活潑。",
      "系統架構優化：重構 LINE 模組相關資料表名稱，提升系統規範性。"
    ]
  },
  {
    "version": "v2.2.0",
    "date": "2025-12-21",
    "items": [
      "新增「素材管理庫」功能，可集中上傳與管理行銷用的圖片、影片與音效檔案，並支援自動轉檔。",
      "推出「LINE 訊息範本」編輯器，提供視覺化 Flex 訊息預覽，支援匯入 JSON 與多款預設範本快速套用。",
      "強化訊息發送能力，現在系統支援直接傳送影片、錄音檔、位置資訊以及 LINE 貼圖。",
      "新增訊息範本測試功能，編輯時可即時推播至指定測試帳號，確保顯示效果無誤。",
      "優化前端操作體驗，新增即時錄音上傳與素材選擇器，提升行銷內容製作效率。"
    ]
  },
  {
    "version": "v2.1.7",
    "date": "2025-12-13",
    "items": [
      "全新通知中心上線：導覽列新增通知鈴鐺，可即時查看未讀通知數量與內容，並支援一鍵標記已讀。",
      "首頁儀表板升級：新增「系統公告」區塊，自動標記最新（3天內）與近期（7天內）公告，重要訊息不漏接。",
      "行動裝置體驗優化：全面改善手機版介面的通知與公告閱讀體驗，文字排版更易讀，並支援圖片點擊預覽。",
      "編輯器新增影片尺寸調整功能，支援透過懸浮按鈕切換 360/480/640px 高度" +
      "貼上 YouTube 連結自動偵測並轉為嵌入式影片 (支援 Shorts 與標準連結)" +
      "系統效能與穩定性提升：修復了部分檔案操作可能導致的資源佔用問題，並解決下載檔案時中文檔名顯示異常的錯誤。",
      "編輯器顯示優化：修正富文本編輯器中圖片過大導致版面跑版的問題。"
    ]
  },
  {
    "version": "v2.1.6",
    "date": "2025-12-11",
    "items": [
      "更新「頁面不存在」及「權限不足」的圖檔和畫面。",
      "核心架構升級：系統底層已全面升級至 Java 25，大幅提升應用程式的穩定性與執行效率。",
      "效能優化：啟用新一代 ZGC (Z Garbage Collector) 記憶體管理技術，有效降低系統延遲，提供更順暢的運作體驗。",
      "部署安全強化：新增伺服器環境檢核機制，部署時自動偵測遠端 JDK 版本是否符合需求，防止環境不一致導致的錯誤。",
    ]
  },
  {
    version: "v2.0.5",
    date: "2025-12-08",
    items: [
      "匯入功能大幅升級：支援上傳包含圖片的 ZIP 壓縮檔，可一次性批量建立物品資料與圖片，無需逐筆上傳。",
      "新增「匯入範本懶人包」下載，內含 Excel 範本、範例圖片與詳細操作說明文件，資料整理更上手。",
      "優化匯出功能，現在可匯出包含物品圖片的完整資料備份，並提供即時的進度顯示。",
      "匯入結果報告更清晰，系統將詳細列出成功、失敗筆數以及圖片處理狀況（如圖片遺失或覆蓋提示）。",
      "新增系統自動維護機制，定期清理過期的備份檔案，確保系統運作效能。"
    ]
  },
  {
    version: "v2.0.2",
    date: "2025-12-07",
    items: [
      "新增物品預約功能與借用權限控管，提升借用流程彈性。",
      "新增物品「遺失」狀態處理流程與審核備註功能。",
      "優化搜尋功能與操作歷程記錄，查詢更便利。",
      "新增表格欄位個人化配置，系統將自動記憶欄位設定。"
    ]
  },
  {
    version: "v2.0.1",
    date: "2025-12-03",
    items: [
      "新增物品資料 Excel 批次匯入功能與進度即時顯示。"
    ]
  },
  {
    version: "v2.0.0",
    date: "2025-11-30",
    items: [
      "核心架構全面升級：前端遷移至 Vue 3.5 + Vite 5 + Pinia，採用 Composition API 重構，大幅提升建置速度與執行效能。",
      "UI 介面現代化：導入 Element Plus 框架與全 SVG Icon 組件，優化登入頁面視覺、新增側邊欄拖曳調整寬度功能及個人中心多色主題切換。",
      "行動端掃描重構：庫存掃描功能升級，新增相機變焦 (Zoom) 控制、懸浮操作按鈕，並整合 ISBN 爬蟲與 SSE 即時進度顯示。",
      "Line 模組優化：重構 Rich Menu 圖文選單編輯器，解決版面配置問題並支援即時發布狀態監控；優化使用者頭像裁切工具。",
      "系統標準化與修復：修復動態路由命名衝突，並增強開發環境的錯誤日誌排查機制。"
    ]
  },
  {
    version: "v1.6.5",
    date: "2025-11-22",
    items: [
      "重構系統配置鍵管理並優化庫存報表介面顯示。",
      "調整庫存報表的頂部卡片樣式，放大標題與說明文字的字體大小，並微調內距與圖示大小以維持視覺緊湊感。",
      "調整報表統計區塊的佈局（stat-row），強制設定為不換行（nowrap），解決當統計項目較多（如掃描報表）時會自動換行導致排版錯亂的問題。",
    ]
  },
  {
    version: "v1.6.4",
    date: "2025-11-22",
    items: [
      "優化庫存掃描記錄與報表顯示邏輯。",
      "更新報表頁面 (report/index.vue)，調整表格欄位順序與寬度，新增顯示「錯誤訊息」欄位，並優化掃描類型與結果的標籤顯示。",
      "修正報表切換時的資料重置邏輯 (resetQueryParams)，避免切換不同報表時殘留舊的查詢參數或資料。",
      "新增共用的 Tab 與 Card 切換自動更新機制，並應用於庫存管理頁面"
    ]
  },
  {
    version: "v1.6.3",
    date: "2025-11-22",
    items: [
      "優化物品重新抓取、刪除安全檢查與操作日誌記錄。",
      "更新庫存管理頁面 (InvManagement)，整合新的 SSE 重新抓取 API，並提供詳細的更新前後差異比對與進度提示。"
    ]
  },
  {
    version: "v1.6.2",
    date: "2025-11-21",
    items: ["針對庫存管理模組進行安全性與功能性的增強，同時大幅度優化首頁儀表板和登入及跳轉畫面。"]
  },
  {
    version: "v1.6.1",
    date: "2025-11-15",
    items: ["新增 LINE Rich Menu 的發布功能，同時也新增了完整的圖片上傳、自動裁切/壓縮機制...等，並優化了前後端交互體驗。"]
  },
  {
    version: "v1.5.4",
    date: "2025-11-02",
    items: ["匯入全系統日誌追蹤功能 (TraceId)。",
      "匯入開發工具 (Git Hooks, Mapper 驗證) 並擴充 LINE 模組功能。"]
  },
  {
    version: "v1.5.3",
    date: "2025-10-26",
    items: ["匯入排程任務範本 (TaskTypeProvider) 動態註冊機制。"]
  },
  {
    version: "v1.5.2",
    date: "2025-10-25",
    items: ["整合 Selenium 爬蟲框架。",
      "新增 CA102 爬蟲 (臺灣證券交易所-即時重大訊息)。",
      "新增 CA103 爬蟲 (臺灣證券交易所-上市公司每日收盤價)。"]
  },
  {
    version: "v1.5.1",
    date: "2025-10-22",
    items: ["建立爬蟲模組並整合 Quartz 定時任務排程。",
      "更新「定時任務」模組的新增/修改彈窗。",
      "新增「設定方式」切換功能：「從範本選擇」或「手動輸入」。"]
  },
  {
    version: "v1.4.0",
    date: "2025-10-22",
    items: ["匯入 Flyway 實作資料庫版本控制與自動化遷移。",
      "優化報表篩選，僅顯示已使用的物品分類。"]
  },
  {
    version: "v1.3.4",
    date: "2025-10-14",
    items: ["全面更新登入頁面視覺設計，提升使用者體驗。",
      "增強手機端掃描功能，支援快速入庫與詳情檢視。",
      "對庫存管理主頁面進行響應式設計（RWD）優化，改善在手機等小螢幕裝置上的表格與操作按鈕佈局，提升行動裝置操作體驗。"]
  },
  {
    version: "v1.2.3",
    date: "2025-10-13",
    items: ["Docker 容器化部署流程，進行了全面的架構調整與功能增強。"]
  },
  {
    version: "v1.2.2",
    date: "2025-10-10",
    items: ["個人中心 (User Profile) UI/UX 全面升級。",
      "庫存管理 (Inventory) 新增圖片功能。"]
  },
  {
    version: "v1.2.1",
    date: "2025-10-05",
    items: ["整合物品與庫存管理，並新增可拖曳調整寬度的側邊選單。"]
  },
  {
    version: "v1.2.0",
    date: "2025-10-03",
    items: ["整合 ISBN 掃描與書籍資訊爬蟲功能。"]
  },
  {
    version: "v1.1.1",
    date: "2025-09-23",
    items: ["新增「庫存管理」模組，包含物品管理、庫存查詢、借出管理、掃描功能與庫存報表等頁面。",
      "新增一個全域浮動的掃描按鈕元件，提升操作便利性。",
      "為掃描功能整合了 html5-qrcode 套件。"]
  },
  {
    version: "v1.0.1",
    date: "2025-09-23",
    items: ["整合了 Jasypt，對設定檔 (application.yml) 中的敏感資訊（如資料庫密碼、Token Secret）進行加密處理。",
      "將加密演算法升級為更安全的 PBEWITHHMACSHA512ANDAES_256。"]
  },
  {
    version: "v1.0.0",
    date: "2025-09-22",
    items: ["CoolApps 前後端分離系統正式發布"]
  }
];

/**
 * 核心演算法：決定該版本的樣式
 * @param {Object} log - 當前日誌物件
 * @param {number} index - 陣列索引
 * @param {number} totalLength - 陣列總長度
 */
function determineStyle(log, index, totalLength) {
  // 規則 1: 第一個一定是最新版本
  if (index === 0) {
    return LOG_STYLES.LATEST;
  }

  // 規則 2: 最後一個一定是首發版本
  if (index === totalLength - 1) {
    return LOG_STYLES.INITIAL;
  }

  // 規則 3: 超過前 6 個，視為歷史紀錄 (降低視覺干擾)
  if (index > 5) {
    return LOG_STYLES.HISTORY;
  }

  // 規則 4: Index 1~5 的智能判斷 (依照優先級)
  const contentString = log.items.join(" ");

  // [優先級 A] 重大版本: 結尾是 .0 (如 v1.4.0)
  if (log.version.endsWith(".0")) {
    return LOG_STYLES.MAJOR;
  }

  // [優先級 B] 安全性: 包含 安全, 加密, Token, 憑證
  if (/安全|加密|Token|Auth|Security/.test(contentString)) {
    return LOG_STYLES.SECURE;
  }

  // [優先級 C] 穩定/優化: 包含 優化, 穩定, 效能, 體驗, 升級 (新增此邏輯)
  if (/優化|穩定|效能|體驗|Refactor/.test(contentString)) {
    return LOG_STYLES.STABLE;
  }

  // [優先級 D] 修復: 包含 修復, 修正, Bug, Fix, 調整
  if (/修復|修正|Bug|Fix|調整/.test(contentString)) {
    return LOG_STYLES.FIX;
  }

  // [優先級 E] 預設 -> 功能更新
  return LOG_STYLES.FEATURE;
}

/**
 * 處理後的日誌資料 (匯出給 UI 使用)
 * 會自動合併樣式屬性
 */
export const versionLogs = rawLogs.map((log, index) => {
  const style = determineStyle(log, index, rawLogs.length);
  return {
    ...log,
    ...style
  };
});

// --- Helper Functions ---

/**
 * 取得最新版本號
 * @returns {string} 版本號
 */
export function getLatestVersion() {
  return versionLogs.length > 0 ? versionLogs[0].version : "v1.0.0";
}

/**
 * 取得總版本數
 * @returns {number} 版本總數
 */
export function getTotalVersionCount() {
  return versionLogs.length;
}

/**
 * 根據版本號查詢更新日誌
 * @param {string} version - 版本號
 * @returns {object|null} 版本日誌物件或 null
 */
export function getVersionByNumber(version) {
  return versionLogs.find(log => log.version === version) || null;
}
