# 專案架構概述

## 技術架構

### 技術棧
- **前端**: Vue.js 3 + Element UI 3+ (繁體中文)
- **後端**: Spring Boot 3.5.4 + MyBatis 3.0.4 + Spring Security 6.x
- **資料庫**: MySQL 8.0+ (使用 Flyway 管理遷移)
- **快取**: Redis 6.0+ (Lettuce Client)
- **伺服器**: Tomcat 10.1 / Docker + Nginx 1.27
- **其他**: JWT Token、Druid 連線池、Jasypt 加密、LINE Bot SDK 9.12.0

### 模組結構
```
Cheng-Vue/
├── cheng-admin/              # 主應用模組 (產出 apps.war)
├── cheng-common/             # 通用工具、Enum、常數、異常處理
├── cheng-framework/          # Spring Security、Redis、AOP、攔截器
├── cheng-system/             # 系統管理、用戶角色權限、庫存管理
├── cheng-line/               # LINE Bot 多頻道管理
├── cheng-quartz/             # 定時任務管理
├── cheng-generator/          # 程式碼產生器
├── cheng-crawler/            # 爬蟲模組 (預留)
├── cheng-ui/                 # Vue.js 前端專案
├── cheng.deploy/             # 傳統部署腳本和 Nginx 配置
└── docs/                     # 專案文件
    ├── Architecture/         # 架構設計文件
    ├── BugFix/              # Bug 修復記錄
    ├── Deployment/          # 部署相關文件
    ├── Development/         # 開發規範（本目錄）
    └── Line/                # LINE Bot 專用文件
```

### 各模組職責

#### cheng-admin (主模組)
- 整合所有功能模組
- 產出 `apps.war` 部署檔案
- 包含 `CoolAppsApplication` 主程式

#### cheng-common (通用模組)
- **annotation/**: 自定義註解 (`@Log`, `@RepeatSubmit`, `@RateLimiter`)
- **config/**: 共用設定類
- **constant/**: 常數定義 (`UserConstants`, `HttpStatus`)
- **core/**: 核心工具 (分頁、文字處理、Redis)
- **enums/**: 列舉定義 ⭐ (Status, YesNo, BusinessType 等)
- **exception/**: 異常處理 (`ServiceException`, `GlobalExceptionHandler`)
- **utils/**: 工具類 (StringUtils, DateUtils, SecurityUtils)

#### cheng-framework (框架模組)
- **config/**: Spring 框架設定
- **security/**: Spring Security 認證授權
- **web/**: Web 層共用元件 (攔截器、過濾器)

#### cheng-system (系統模組)
- **domain/**: 實體類 (SysUser, SysRole, InvItem 等)
- **mapper/**: MyBatis Mapper 介面
- **service/**: 業務邏輯層
- **controller**: REST API 控制器

#### cheng-line (LINE Bot 模組)
- **config/**: LINE Bot 設定
- **domain/**: LINE 實體類
- **enums/**: LINE 專用 Enum (ChannelType, MessageType 等)
- **service/**: LINE 訊息處理
- **webhook/**: Webhook 處理器

#### cheng-ui (前端模組)
- **src/api/**: API 請求封裝
- **src/components/**: 共用元件
- **src/views/**: 頁面元件
- **src/router/**: 路由設定
- **src/store/**: Vuex 狀態管理

---

## 效能預估目標

### 使用者規模
- **同時線上人數**: 500 人
- **並發操作人數**: 100 人

### 效能指標

#### API 回應時間
- **平均回應時間**: < 200ms
- **P95 回應時間**: < 500ms
- **P99 回應時間**: < 1000ms

#### 資料庫
- **慢查詢數**: 0 (閾值 1000ms)
- **連線池使用率**: < 70%
- **QPS**: < 500 (單機)
- **連線池設定**:
  - 最小連線: 10
  - 最大連線: 20

#### 快取
- **Redis 命中率**: > 80%
- **權限快取命中率**: > 90%
- **快取過期時間**:
  - 字典資料: 30 分鐘
  - 用戶權限: 30 分鐘
  - 系統配置: 30 分鐘
  - 驗證碼: 5 分鐘
  - JWT Token: 30 分鐘

#### JVM
- **堆記憶體使用率**: < 70%
- **Full GC 次數**: < 5 次/小時
- **GC 暫停時間**: < 100ms

---

## 部署架構

### Local (本地開發)
```
瀏覽器 → localhost:1024 (Vue Dev Server)
      ↓
      → localhost:8080 (Spring Boot)
      ↓
      → MySQL (本地或遠端)
      → Redis (本地或遠端)
```

### VM (虛擬機 Tomcat)
```
瀏覽器 → https://cheng.tplinkdns.com (Nginx)
      ↓
      → /prod-api/ → Tomcat:8080 (Spring Boot WAR)
      → /webhook/line/ → Tomcat:8080
      → / → 靜態檔案 (Vue Build)
      ↓
      → MySQL (雲端)
      → Redis (雲端)
```

### PROD (Docker/Zeabur)
```
瀏覽器 → https://cool-apps.zeabur.app (Nginx Container)
      ↓
      → /prod-api/ → Backend Container:8080
      → /webhook/line/ → Backend Container:8080
      → / → Frontend Container (Nginx)
      ↓
      → MySQL (Zeabur 託管)
      → Redis (Zeabur 託管)
```

---

## 關鍵技術決策

### 為什麼選擇 MyBatis 而非 JPA？
- ✅ 更靈活的 SQL 控制
- ✅ 適合複雜查詢和報表
- ✅ 效能優化空間大
- ✅ 團隊熟悉度高

### 為什麼使用 Enum 而非資料字典表？
- ✅ 類型安全、編譯期檢查
- ✅ IDE 自動完成支援
- ✅ 減少資料庫查詢
- ✅ 程式碼可讀性高
- ⚠️ 缺點：變更需重新部署（但狀態類型很少變動）

### 為什麼前後端分離？
- ✅ 開發效率高（前後端並行開發）
- ✅ 職責清晰
- ✅ 易於擴展（可支援多端：Web、App、小程式）
- ✅ 前端可獨立部署和快取

### 為什麼使用 JWT 而非 Session？
- ✅ 無狀態、易於水平擴展
- ✅ 跨域友善
- ✅ 減少伺服器記憶體壓力
- ⚠️ 注意：Token 過期需配合 Redis 實作黑名單

---

## 開發工具推薦

### 後端開發
- **IDE**: IntelliJ IDEA Ultimate
- **Java**: JDK 25+
- **Maven**: 3.9+
- **資料庫工具**: DBeaver / DataGrip
- **API 測試**: Postman / Swagger UI

### 前端開發
- **IDE**: VS Code / WebStorm
- **Node.js**: 18+
- **包管理器**: npm / yarn
- **瀏覽器**: Chrome (Vue DevTools)

### 運維工具
- **容器**: Docker Desktop
- **版本控制**: Git
- **SSH**: iTerm2 (Mac) / MobaXterm (Windows)
- **日誌查看**: lnav / tail

---

## 重要連結

- **專案 GitHub**: https://github.com/mark22013333/Cheng-Vue
- **線上預覽**: https://cool-apps.zeabur.app
- **API 文檔**: https://cool-apps.zeabur.app/swagger-ui/index.html
- **作者部落格**: https://mark22013333.github.io/

---

**下一步**: 閱讀 [RULE_02_ENUM_STANDARDS.md](./RULE_02_ENUM_STANDARDS.md) 學習 Enum 使用規範（最重要！）
