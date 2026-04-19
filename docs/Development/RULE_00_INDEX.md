# CoolApps 專案開發規範索引

> **目的**: 本目錄包含 CoolApps 專案的完整開發規範，請將相關內容整合到 `.windsurf/rules` 檔案中。

---

## 📚 規範文件列表

### 核心規範（必讀）
1. **[專案架構概述](./RULE_01_PROJECT_OVERVIEW.md)**
   - 技術架構、模組結構、效能目標
   
2. **[Enum 使用規範](./RULE_02_ENUM_STANDARDS.md)** ⭐ **最重要**
   - 強制使用 Enum 替代魔法數字
   - Enum 設計標準範本
   - 現有 Enum 列表

3. **[設計模式應用](./RULE_03_DESIGN_PATTERNS.md)**
   - 策略模式、模板方法、建造者、工廠、單例

4. **[效能優化規範](./RULE_04_PERFORMANCE.md)**
   - 資料庫優化、快取策略、並發控制

5. **[異常處理規範](./RULE_05_EXCEPTION_HANDLING.md)**
   - ServiceException 使用、異常訊息規範

### 進階規範
6. **[事務管理規範](./RULE_06_TRANSACTION.md)**
   - @Transactional 使用時機、注意事項

7. **[日誌與監控規範](./RULE_07_LOGGING.md)**
   - 日誌級別、敏感資訊脫敏、結構化日誌

8. **[API 設計規範](./RULE_08_API_DESIGN.md)**
   - RESTful 規範、回應格式、分頁參數

9. **[資料驗證規範](./RULE_09_DATA_VALIDATION.md)**
   - Controller 參數校驗、Service 業務校驗

10. **[環境配置規範](./RULE_10_ENVIRONMENT.md)**
    - Local/VM/PROD 環境配置、Jasypt 加密

### 實用工具
11. **[程式碼審查清單](./RULE_11_CODE_REVIEW.md)**
    - 開發完成後必須檢查的項目

12. **[常見陷阱與注意事項](./RULE_12_COMMON_PITFALLS.md)**
    - MyBatis、日期比較、執行緒安全等陷阱

13. **[前端開發規範](./RULE_13_FRONTEND_STANDARDS.md)** ⭐ **重要**
    - 狀態參數處理（0/1/字串）
    - Element UI 元件使用規範
    - 前後端資料格式轉換

---

## 🚀 快速開始

### 對於新加入的開發者
**必讀文件**（優先順序）:
1. ⭐ RULE_02 - Enum 使用規範
2. ⭐ RULE_13 - 前端開發規範（前端開發者必讀）
3. RULE_01 - 專案架構概述
4. RULE_10 - 環境配置規範
5. RULE_11 - 程式碼審查清單

### 對於資深開發者
**重點文件**:
- RULE_04 - 效能優化規範
- RULE_05 - 異常處理規範
- RULE_06 - 事務管理規範
- RULE_12 - 常見陷阱

---

## 📝 如何使用

### 整合到 Windsurf Rules

**請參考**: [WINDSURF_SETUP.md](./WINDSURF_SETUP.md) - Windsurf 設定指南

**快速設定**:
1. 建立本地規則檔案：`.windsurf/rules/cool-apps.md`
2. 複製 [WINDSURF_RULES_TEMPLATE.md](./WINDSURF_RULES_TEMPLATE.md) 的內容
3. 在 Cascade 中使用 `@cool-apps` 引用規則

**注意**: `.windsurf/` 目錄已加入 `.gitignore`，不會進版控

### 查閱規範
- **IDE 內查閱**: 直接在 IDE 中開啟 `docs/Development/` 目錄下的檔案
- **命令列查閱**: 使用 `cat` 或 `less` 命令查看
- **Web 查閱**: 如果專案有文件網站，可瀏覽線上版本

---

## 🔄 更新歷史

- **2025-11-01**: 初版建立，包含 12 個規範文件
- **未來**: 根據專案演進持續更新

---

## 📧 問題回報

如果發現規範有誤或需要補充，請：
1. 提交 GitHub Issue
2. 聯繫專案維護者
3. 直接修改並提交 PR

---

**注意**: 這些規範是根據 CoolApps 專案的實際架構和最佳實踐總結而成，請遵守以確保程式碼品質和一致性。
