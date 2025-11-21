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
 */
const rawLogs = [
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
    items: ["導入全系統日誌追蹤功能 (TraceId)。",
      "導入開發工具 (Git Hooks, Mapper 驗證) 並擴充 LINE 模組功能。"]
  },
  {
    version: "v1.5.3",
    date: "2025-10-26",
    items: ["導入排程任務範本 (TaskTypeProvider) 動態註冊機制。"]
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
    items: ["導入 Flyway 實作資料庫版本控制與自動化遷移。",
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
