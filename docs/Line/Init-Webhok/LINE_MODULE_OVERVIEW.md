# LINE 模組整合 - 概述文件

> **建立時間：** 2025-10-26  
> **版本：** 1.0  
> **狀態：** 設計階段

---

## 需求概述

### 功能目標
整合 LINE Messaging API，讓系統能夠：
1. 在後台設定 LINE 頻道參數（支援主頻道、副頻道、測試頻道）
2. 提供連線測試與 Webhook 驗證功能
3. 執行訊息推播（單人、群組、標籤、廣播）
4. 管理 LINE 使用者與系統使用者的綁定關係
5. 記錄完整的推播歷史

### 使用者故事
- 作為系統管理員，我希望能設定多個 LINE 頻道（主、副、測試），以便在不同情境使用
- 作為行銷人員，我希望能依標籤推播訊息給特定群組的 LINE 使用者
- 作為測試人員，我希望能快速驗證 LINE 連線與 Webhook 是否正常運作

### 成功條件
- 後台可設定 3 種頻道類型（主、副、測試），並獨立測試連線
- 可發送文字、圖片、模板訊息至指定對象
- 完整記錄推播歷史（包含目標 UID、標籤）
- 只有具備「行銷」或「系統管理」權限的使用者可進入模組

---

## 技術棧

### 後端
- **框架**: Spring Boot 3.5.4
- **LINE SDK**: line-bot-sdk-java 6.0.0
- **資料庫**: MySQL 8.0
- **ORM**: MyBatis 3.0.4
- **加密**: Jasypt (敏感資料加密)

### 前端
- **框架**: Vue.js 2.x
- **UI 套件**: Element UI
- **HTTP 客戶端**: Axios

---

## 模組結構

```
cheng-line/                           # 新增模組
├── pom.xml
└── src/main/
    ├── java/com/cheng/line/
    │   ├── domain/                   # 實體類別
    │   ├── dto/                      # 資料傳輸物件
    │   ├── vo/                       # 視圖物件
    │   ├── enums/                    # 列舉類別
    │   ├── mapper/                   # MyBatis Mapper
    │   ├── service/                  # Service 層
    │   └── util/                     # 工具類別
    └── resources/mapper/line/        # MyBatis XML

cheng-admin/                          # 控制器層
└── src/main/java/com/cheng/web/controller/line/
    ├── LineConfigController.java
    ├── LinePushController.java
    ├── LineUserController.java
    └── LineWebhookController.java

cheng-ui/                             # 前端頁面
└── src/
    ├── api/line/                     # API 定義
    ├── views/line/                   # 頁面元件
    └── router/index.js               # 路由配置
```

---

## Maven 依賴配置

```xml
<!-- LINE Bot SDK (版本 6.0.0) -->
<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-api-client</artifactId>
    <version>6.0.0</version>
</dependency>

<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-model</artifactId>
    <version>6.0.0</version>
</dependency>

<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-spring-boot</artifactId>
    <version>6.0.0</version>
</dependency>
```

---

## 開發計畫

### Phase 1: 基礎建設（第 1-2 天）
- [x] 建立設計文件
- [ ] 建立 `cheng-line` 模組結構
- [ ] 建立資料庫表格（Flyway Migration）
- [ ] 建立列舉類別（Enum）
- [ ] 建立 Domain 實體類別
- [ ] 建立 Mapper 介面和 XML

### Phase 2: 後端 API - 設定管理（第 3-4 天）
- [ ] 實作 LineConfigService（設定管理）
- [ ] 實作 LineConfigController（API 端點）
- [ ] 實作連線測試功能
- [ ] 實作 Webhook 驗證功能
- [ ] 實作 Webhook 接收處理

### Phase 3: 後端 API - 推播功能（第 5-6 天）
- [ ] 實作 LinePushService（推播邏輯）
- [ ] 實作 LinePushController（推播 API）
- [ ] 實作訊息記錄功能
- [ ] 實作 LINE 使用者管理

### Phase 4: 前端介面（第 7-8 天）
- [ ] 建立 LINE 設定頁面
- [ ] 建立推播訊息管理頁面
- [ ] 建立 LINE 使用者管理頁面
- [ ] 整合 API 並測試

### Phase 5: 測試與優化（第 9-10 天）
- [ ] 整合測試
- [ ] 權限測試
- [ ] 安全性測試
- [ ] 撰寫使用手冊

---

## 相關文件

- [資料庫設計](./LINE_MODULE_DATABASE.md) - 詳細的資料表設計
- [API 設計](./LINE_MODULE_API.md) - 完整的 API 規格
- [前端設計](./LINE_MODULE_FRONTEND.md) - UI/UX 設計與元件
- [安全性設計](./LINE_MODULE_SECURITY.md) - 安全性與權限控制

---

## 重要提醒

### 設計原則
1. **使用 Enum 而非字串/數字**：所有狀態、類型都使用列舉類別，提升可讀性
2. **敏感資料加密**：Channel Secret、Access Token 使用 Jasypt 加密
3. **完整的時間記錄**：記錄所有關鍵操作的時間點
4. **權限控制嚴格**：只有授權使用者可進入模組

### LINE API 限制
- **Push Message**: 每月有免費額度限制
- **Broadcast**: 需要升級為付費方案
- **Rate Limit**: API 呼叫有速率限制

### 參考資源
- [LINE Messaging API 官方文件](https://developers.line.biz/en/docs/messaging-api/)
- [LINE Bot SDK Java GitHub](https://github.com/line/line-bot-sdk-java)
- [LINE Developers Console](https://developers.line.biz/console/)
