# 庫存管理系統文件

本目錄包含庫存管理系統的功能說明、整合指南和問題修復記錄。

## 📚 文件列表

- **INVENTORY_SYSTEM_README.md** - 庫存管理系統完整說明
- **INVENTORY_OPTIMIZATION.md** - 庫存系統效能最佳化方案
- **inventory_integration_guide.md** - 庫存系統整合指南
- **STOCK_STATUS_FIX.md** - 庫存狀態顯示問題修復

## 🎯 核心功能

### 庫存管理
- **入庫管理** - 新品入庫、批次入庫
- **出庫管理** - 出庫登記、庫存扣減
- **盤點功能** - 庫存盤點、差異調整
- **異動記錄** - 完整的庫存異動追蹤

### QR Code 掃描
- **攝影機掃描** - 即時 QR Code/條碼掃描
- **手動輸入** - 支援手動輸入條碼
- **ISBN 查詢** - 整合 ISBN 圖書資料查詢

### 借出歸還
- **借出申請** - 物品借出登記
- **審核流程** - 借出申請審核機制
- **歸還管理** - 物品歸還處理
- **逾期提醒** - 自動逾期提醒功能

### 報表統計
- **庫存報表** - 即時庫存統計
- **借出報表** - 借出歸還統計
- **異動報表** - 庫存異動分析
- **Excel 匯出** - 支援報表匯出

## 🗄️ 資料表結構

```
inventory_category      - 物品分類
inventory_item          - 物品資訊
inventory_stock         - 庫存資料
inventory_borrow_record - 借出記錄
inventory_return_record - 歸還記錄
inventory_scan_record   - 掃描記錄
inventory_transaction   - 異動記錄
```

## 🔗 相關連結
- [圖書管理](../Book/)
- [系統架構](../Architecture/)
- [部署指南](../Deployment/)
