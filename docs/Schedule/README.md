# 排程系統文件

本目錄包含 Quartz 排程系統的開發、測試和配置相關文件。

## 📚 文件列表

### 開發指南
- **排程開發與測試指南.md** - 完整的排程功能開發與測試流程（必讀）
- **QUARTZ_QUICK_START.md** - Quartz 快速入門指南

### 功能設計
- **任務類型動態配置整合說明.md** - 動態任務類型配置機制
- **QUARTZ_JOB_TYPE_ENUM.md** - 任務類型列舉設計與實作
- **QUARTZ_TEMPLATE_FEATURE.md** - 任務範本功能說明

### 前端整合
- **QUARTZ_FRONTEND_INTEGRATION.md** - Quartz 與前端的完整整合方案

### 架構與重構
- **架構重構說明.md** - 排程系統架構重構記錄
- **修正完成總結.md** - 排程功能修正總結

### 問題排除
- **DEBUG_QUARTZ_PARAMS.md** - Quartz 參數除錯指南
- **FIXES_QUARTZ_TEMPLATE.md** - 任務範本問題修正

## 🎯 核心功能

### 支援的任務類型
- **爬蟲任務** - 網站資料爬取
- **資料清理** - 定期資料清理
- **報表產生** - 定期報表產生
- **系統維護** - 自動化維護任務

### 動態配置
透過 `@CrawlerType` 註解自動註冊任務類型，支援：
- 動態參數配置
- 參數範本管理
- 前端動態表單產生

## 🚀 快速開始

1. 閱讀 [排程開發與測試指南](./排程開發與測試指南.md)
2. 參考 [任務類型動態配置](./任務類型動態配置整合說明.md)
3. 查看 [前端整合方案](./QUARTZ_FRONTEND_INTEGRATION.md)

## 🔗 相關連結
- [爬蟲系統](../Crawler/)
- [部署檢查清單](../Deployment/DEPLOYMENT_CHECKLIST_QUARTZ.md)
