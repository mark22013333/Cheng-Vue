# 🚀 CoolApps 管理系統

<div align="center">

![CoolApps Logo](https://img.shields.io/badge/CoolApps-v1.2.2-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-2.6.12-4fc08d.svg)
![Element UI](https://img.shields.io/badge/Element%20UI-2.15.14-409EFF.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**一個現代化的企業級後台管理系統**

基於 Spring Boot 3.5.4 + Vue.js 2.6 + Element UI 的前後端分離架構

[線上預覽](https://cool-apps.zeabur.app) | [API 文檔](https://cool-apps.zeabur.app/swagger-ui/index.html)

</div>

## 📋 目錄

- [專案介紹](#專案介紹)
- [技術架構](#技術架構)
- [功能特色](#功能特色)
- [系統模組](#系統模組)
- [快速開始](#快速開始)
- [開發指南](#開發指南)
- [庫存管理系統](#庫存管理系統)
- [API 文檔](#api-文檔)
- [更新日誌](#更新日誌)
- [常見問題](#常見問題)
- [授權協議](#授權協議)

## 🎯 專案介紹

CoolApps 是一個基於若依(RY)專案修改的 Spring Boot 3.5.4 和 Vue.js 2.6 的企業級後台管理系統，採用前後端分離架構設計。系統提供了完整的用戶管理、角色權限、系統監控、程式碼產生等功能，適用於各種企業級應用場景。

### ✨ 專案亮點

- 🏗️ **現代化架構**：採用 Spring Boot 3.5.4 + Vue.js 2.6 前後端分離
- 🐳 **容器化部署**：Docker + Docker Buildx 多平台支援
- 🔐 **安全可靠**：JWT Token 認證 + Spring Security 權限控制 + Jasypt 加密
- 🎨 **優雅介面**：基於 Element UI 的現代化管理介面（繁體中文）
- 📱 **響應式設計**：支援桌面端和行動端訪問
- 🚀 **高效能**：Redis 暫存 + Druid 連線池優化
- 🛠️ **開發友善**：完整的程式碼產生器和自動化建置腳本
- 📦 **庫存管理**：完整的庫存管理系統（包含 QR Code 掃描功能）

## 🏛️ 技術架構

### 後端技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Spring Boot | 3.5.4 | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis | 3.0.4 | ORM 框架 |
| Druid | 1.2.23 | 資料庫連線池 |
| Redis | - | 分散式暫存 |
| JWT | 0.9.1 | Token 認證 |
| Jasypt | 1.9.3 | 配置加密 |
| Swagger | 3.0.0 | API 文檔 |
| Quartz | - | 定時任務 |
| Tomcat | 10.1 | Servlet 容器 |
| Maven | 3.9.6 | 專案管理 |

### 前端技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Vue.js | 2.6.12 | 前端框架 |
| Vue Router | 3.4.9 | 路由管理 |
| Vuex | 3.6.0 | 狀態管理 |
| Element UI | 2.15.14 | UI 元件庫（繁體中文）|
| Axios | 0.28.1 | HTTP 請求 |
| ECharts | 5.4.0 | 圖表庫 |
| Quill | 2.0.2 | 富文字編輯器 |
| html5-qrcode | 2.3.8 | QR Code 掃描 |
| Nginx | 1.27 | Web 伺服器 |

### 部署架構

| 技術 | 版本 | 說明 |
|------|------|------|
| Docker | 20.10+ | 容器化平台 |
| Docker Buildx | - | 多平台建置工具 |
| Nginx | 1.27-alpine | 前端靜態檔案服務 |
| Tomcat | 10.1-jdk17 | 後端應用伺服器 |
| MySQL | 8.0+ | 資料庫（雲端託管）|
| Redis | 6.0+ | 快取資料庫（雲端託管）|

### 開發環境

| 工具 | 版本要求 | 說明 |
|------|----------|------|
| JDK | 17+ | Java 開發環境 |
| Node.js | 18+ | 前端開發環境 |
| Maven | 3.9+ | 專案建置工具 |
| Docker | 20.10+ | 容器化工具 |
| Git | 2.x+ | 版本控制 |

## 🎨 功能特色

### 🔐 權限管理
- **用戶管理**：用戶新增、編輯、刪除、角色分配
- **角色管理**：角色權限設定、數據權限控制
- **選單管理**：動態選單設定、權限控制
- **部門管理**：組織架構管理、數據權限範圍

### 📊 系統監控
- **線上用戶**：即時監控線上用戶狀態
- **系統監控**：伺服器效能監控、JVM 監控
- **操作日誌**：用戶操作記錄追蹤
- **登入日誌**：用戶登入記錄管理
- **暫存監控**：Redis 暫存狀態監控

### 🛠️ 系統工具
- **程式碼產生**：自動產生 CRUD 程式碼
- **系統介面**：動態介面設定
- **定時任務**：Quartz 定時任務管理
- **資料字典**：系統字典管理
- **參數設定**：系統參數設定

### 📝 內容管理
- **通知公告**：系統公告發布管理
- **個人中心**：個人資料、密碼修改、頭像上傳
- **檔案管理**：檔案上傳、下載管理

### 📦 庫存管理系統
- **QR Code 掃描**：支援攝影機即時掃描和手動輸入
- **物品管理**：物品分類、資訊管理、圖片上傳
- **庫存管理**：入庫、出庫、盤點、庫存追蹤
- **借出管理**：借出申請、審核、歸還管理
- **報表統計**：多維度報表分析和 Excel 匯出

## 🏗️ 系統模組

```
Cheng-Vue/
├── cheng-admin/              # 管理後台模組 (產出 apps.war)
├── cheng-common/             # 通用工具模組
├── cheng-framework/          # 核心框架模組
├── cheng-generator/          # 程式碼產生模組
├── cheng-quartz/             # 定時任務模組
├── cheng-system/             # 系統管理模組
├── cheng-crawler/            # 爬蟲模組（預留）
├── cheng-ui/                 # 前端 Vue.js 專案
│   ├── src/                  # 原始碼
│   ├── public/               # 靜態資源
│   ├── Dockerfile            # 前端 Docker 映像
│   └── nginx.conf            # Nginx 配置
├── cheng.deploy/             # 傳統部署腳本
├── sql/                      # 資料庫初始化腳本
├── docs/                     # 專案文檔
├── build-frontend.sh         # 前端 Docker 建置腳本
├── build-backend.sh          # 後端 Docker 建置腳本
├── build-all.sh              # 一鍵建置腳本
├── Dockerfile.backend-tomcat # 後端 Docker 映像
└── pom.xml                   # Maven 主配置
```

### 模組說明

- **cheng-admin**：系統主模組，整合所有功能模組，產出 `apps.war` 部署檔案
- **cheng-common**：通用工具類、常數定義、共用元件、JSON 序列化等
- **cheng-framework**：核心框架設定，包含 Spring Security、Redis、異常處理等
- **cheng-generator**：程式碼產生器，支援自動產生 Controller、Service、Mapper 等
- **cheng-quartz**：定時任務管理，基於 Quartz 實現
- **cheng-system**：系統管理功能，包含用戶、角色、選單、部門、庫存管理等
- **cheng-ui**：前端專案，基於 Vue.js 2.6 + Element UI（繁體中文）

## 🚀 快速開始

### 環境準備

確保你的開發環境已安裝以下軟體：

```bash
# 檢查 Java 版本 (需要 17+)
java -version

# 檢查 Node.js 版本 (需要 16+)
node -v

# 檢查 Maven 版本 (需要 3.6+)
mvn -v
```

### 資料庫初始化

1. 建立資料庫：
```sql
CREATE DATABASE `cool-apps` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 匯入資料庫腳本：
```bash
# 匯入主要資料庫結構和數據
mysql -u root -p cool-apps < sql/cool-apps_20250922-tw.sql

# 匯入定時任務相關表 (可選)
mysql -u root -p cool-apps < sql/quartz-tw.sql
```

### 後端啟動

1. 修改資料庫設定：
```yaml
# cheng-admin/src/main/resources/application-local.yml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/cool-apps?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: your_username
        password: your_password
```

2. 啟動後端服務：
```bash
# 方式一：IDE 中直接執行 CoolAppsApplication.main()

# 方式二：Maven 命令啟動
mvn clean compile
mvn spring-boot:run -pl cheng-admin

# 方式三：打包後啟動
mvn clean package -DskipTests
java -jar cheng-admin/target/apps.war
```

3. 驗證後端啟動：
```bash
# 訪問後端首頁
curl http://localhost:8080

# 訪問 API 文檔
curl http://localhost:8080/swagger-ui/index.html
```

### 前端啟動

1. 安裝依賴：
```bash
cd cheng-ui
npm install
```

2. 啟動開發服務：
```bash
npm run dev
```

3. 訪問系統：
```
前端地址：http://localhost:1024
後端地址：http://localhost:8080
API 文檔：http://localhost:8080/swagger-ui/index.html
```

### 環境變數說明

| 變數名稱 | 說明 | 預設值 | 必填 |
|---------|------|--------|------|
| `JASYPT_ENCRYPTOR_PASSWORD` | Jasypt 解密密碼 | - | ✅ |
| `SPRING_PROFILES_ACTIVE` | 環境設定 | prod | ✅ |
| `PORT` | 後端服務埠號 | 8080 | ❌ |
| `TZ` | 時區設定 | Asia/Taipei | ❌ |

## 👨‍💻 開發指南

### 程式碼結構

```
src/main/java/com/cheng/
├── common/              # 通用模組
│   ├── annotation/      # 自定義註解
│   ├── config/          # 設定類
│   ├── constant/        # 常數定義
│   ├── core/           # 核心工具
│   ├── enums/          # 列舉定義
│   ├── exception/      # 異常處理
│   └── utils/          # 工具類
├── framework/          # 框架模組
│   ├── config/         # 框架設定
│   ├── security/       # 安全設定
│   └── web/           # Web 設定
├── system/            # 系統模組
│   ├── domain/        # 實體類
│   ├── mapper/        # 數據訪問層
│   └── service/       # 業務邏輯層
└── web/               # Web 層
    └── controller/    # 控制器
```

### 開發規範

1. **程式碼風格**：
   - 使用 4 個空格縮進
   - 類名使用 PascalCase
   - 方法名使用 camelCase
   - 常數使用 UPPER_SNAKE_CASE

2. **註解使用**：
   - `@PreAuthorize`：權限控制
   - `@Log`：操作日誌記錄
   - `@RepeatSubmit`：防重複提交
   - `@RateLimiter`：介面限流

3. **異常處理**：
   - 使用統一的異常處理機制
   - 自定義業務異常繼承 `ServiceException`
   - 返回統一的響應格式 `AjaxResult`

### 新增功能模組

1. **建立實體類**：
```java
@Data
@TableName("sys_example")
public class SysExample extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private String status;
}
```

2. **建立 Mapper 介面**：
```java
@Mapper
public interface SysExampleMapper extends BaseMapper<SysExample> {
    List<SysExample> selectExampleList(SysExample example);
}
```

3. **建立 Service 層**：
```java
@Service
public class SysExampleServiceImpl implements ISysExampleService {
    @Autowired
    private SysExampleMapper exampleMapper;
    
    @Override
    public List<SysExample> selectExampleList(SysExample example) {
        return exampleMapper.selectExampleList(example);
    }
}
```

4. **建立 Controller**：
```java
@RestController
@RequestMapping("/system/example")
public class SysExampleController extends BaseController {
    @Autowired
    private ISysExampleService exampleService;
    
    @PreAuthorize("@ss.hasPermi('system:example:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysExample example) {
        startPage();
        List<SysExample> list = exampleService.selectExampleList(example);
        return getDataTable(list);
    }
}
```

## 📦 庫存管理系統

### 系統簡介

CoolApps 內建完整的庫存管理系統，支援 QR Code/條碼掃描、物品管理、庫存追蹤、借出歸還等功能。

### 核心功能

#### 1. QR Code 掃描功能

使用 html5-qrcode 函式庫實現：

- ✅ **攝影機掃描**：支援即時掃描 QR Code 和條碼
- ✅ **手動輸入**：支援手動輸入物品編號
- ✅ **掃描歷史**：記錄所有掃描操作
- ✅ **響應式介面**：自適應手機和桌面端

```javascript
// 前端路由
/inventory/scan           // QR Code 掃描頁面
/inventory/scan/history   // 掃描歷史記錄
```

#### 2. 物品管理

- **物品分類**：支援多層級分類管理
- **物品資訊**：名稱、描述、圖片、條碼等
- **圖片上傳**：支援物品圖片上傳和預覽
- **批次操作**：批次匯入、批次刪除

```javascript
// API 端點
GET  /inventory/item/list      // 查詢物品列表
POST /inventory/item           // 新增物品
PUT  /inventory/item           // 更新物品
DELETE /inventory/item/{ids}   // 刪除物品
```

#### 3. 庫存管理

- **入庫管理**：物品入庫登記、數量記錄
- **出庫管理**：物品出庫申請、審核流程
- **庫存盤點**：定期盤點、庫存調整
- **庫存追蹤**：完整的庫存異動記錄

```javascript
// API 端點
GET  /inventory/stock/list         // 查詢庫存
POST /inventory/stock/in           // 入庫操作
POST /inventory/stock/out          // 出庫操作
GET  /inventory/stockRecord/list   // 異動記錄
```

#### 4. 借出管理

- **借出申請**：用戶提交借出申請
- **審核流程**：管理員審核借出請求
- **歸還管理**：物品歸還登記
- **逾期提醒**：自動計算逾期狀態

```javascript
// API 端點
GET  /inventory/borrow/list     // 借出記錄
POST /inventory/borrow          // 新增借出
PUT  /inventory/borrow/return  // 歸還操作
```

#### 5. 報表統計

- **庫存報表**：當前庫存統計
- **借出報表**：借出歸還統計
- **物品報表**：物品使用頻率分析
- **Excel 匯出**：支援報表匯出

```javascript
// API 端點
GET /inventory/report/stock    // 庫存報表
GET /inventory/report/borrow   // 借出報表
GET /inventory/report/export   // 匯出報表
```

### 資料庫設計

系統包含以下主要資料表：

- `inv_category`：物品分類表
- `inv_item`：物品資訊表
- `inv_stock`：庫存表
- `inv_borrow`：借出記錄表
- `inv_scan_record`：掃描記錄表
- `inv_stock_record`：庫存異動表

### 初始化腳本

```bash
# 匯入庫存管理系統資料庫
mysql -u root -p cool-apps < sql/cool-apps_20250922-tw.sql
```

## 📚 API 文檔

系統整合了 Swagger 3.0（Springdoc），提供完整的 API 文檔：

- **開發環境**：http://localhost:8080/swagger-ui/index.html
- **正式環境**：https://cool-apps.zeabur.app/swagger-ui/index.html

### 主要 API 端點

| 模組 | 端點 | 說明 |
|------|------|------|
| 認證 | `/login` | 用戶登入 |
| 認證 | `/logout` | 用戶登出 |
| 用戶 | `/system/user/*` | 用戶管理 |
| 角色 | `/system/role/*` | 角色管理 |
| 選單 | `/system/menu/*` | 選單管理 |
| 部門 | `/system/dept/*` | 部門管理 |
| 監控 | `/monitor/*` | 系統監控 |

### API 呼叫範例

```javascript
// 登入
POST /login
{
  "username": "admin",
  "password": "admin123",
  "code": "1234",
  "uuid": "uuid-string"
}

// 取得用戶列表
GET /system/user/list?pageNum=1&pageSize=10

// 新增用戶
POST /system/user
{
  "userName": "testuser",
  "nickName": "測試用戶",
  "email": "test@example.com",
  "phonenumber": "13800138000",
  "sex": "1",
  "status": "0"
}
```

## 📈 效能優化

### 後端優化

1. **資料庫優化**：
   - 使用 Druid 連線池
   - 設定合理的連線池參數
   - 開啟 SQL 監控和慢查詢記錄

2. **暫存優化**：
   - Redis 暫存熱點數據
   - 用戶權限資訊暫存
   - 字典數據暫存

3. **查詢優化**：
   - 分頁查詢優化
   - 索引優化
   - 避免 N+1 查詢問題

### 前端優化

1. **建置優化**：
   - Gzip 壓縮
   - 程式碼分割
   - 靜態資源 CDN

2. **載入優化**：
   - 路由懶載入
   - 元件按需載入
   - 圖片懶載入

## 🔒 安全特性

### 認證授權

- **JWT Token**：無狀態認證機制
- **Spring Security**：完整的安全框架
- **權限控制**：細粒度權限控制
- **數據權限**：基於部門的數據權限

### 安全防護

- **XSS 防護**：輸入輸出過濾
- **CSRF 防護**：跨站請求偽造防護
- **SQL 注入防護**：參數化查詢
- **介面限流**：防止惡意請求

### 操作審計

- **操作日誌**：記錄所有用戶操作
- **登入日誌**：記錄用戶登入資訊
- **異常日誌**：記錄系統異常資訊

## 🧪 測試

### 介面測試

使用 Swagger UI 或 Postman 進行 API 測試：

1. 匯入 API 文檔到 Postman
2. 設定環境變數
3. 執行測試集合

## 📊 監控告警

### 系統監控

- **伺服器監控**：CPU、記憶體、磁碟使用率
- **JVM 監控**：堆記憶體、GC 情況
- **資料庫監控**：連線數、慢查詢
- **Redis 監控**：記憶體使用、命中率

### 日誌管理

- **應用日誌**：Logback 設定
- **存取日誌**：Nginx 存取記錄
- **錯誤日誌**：異常堆疊追蹤
- **審計日誌**：用戶操作記錄

## 🔄 更新日誌

### v1.2.2 (2025-10-13)
- 🐳 新增 Docker 容器化部署支援
- 📜 提供自動化建置腳本（build-frontend.sh、build-backend.sh、build-all.sh）
- 🔧 修正 Nginx 反向代理配置，解決 405 錯誤
- ✅ 優化語義化版本號管理
- 📦 完善庫存管理系統功能
- 🌐 全面繁體中文化（Element UI、Druid 監控介面）
- 🔐 Jasypt 配置加密實施
- 📝 更新完整的 README 文檔

### v0.9.0 (2025-09-22)
- ✨ 專案架構從 LineGroup 遷移到 CoolApps
- 🔧 WAR 檔案名稱統一為 `apps.war`
- 🚀 升級 Spring Boot 到 3.5.4
- 🎨 優化前端介面和用戶體驗
- 📦 新增完整的庫存管理系統
- 🔐 增強安全設定和權限控制
- 📝 完善部署文檔和腳本

## ❓ 常見問題

### 安裝與環境

**Q: JDK 17 如何安裝？**  
A: 
- macOS: `brew install openjdk@17`
- Ubuntu: `sudo apt install openjdk-17-jdk`
- Windows: 從 [Oracle 官網](https://www.oracle.com/java/technologies/downloads/) 下載安裝

**Q: Docker Buildx 如何安裝？**  
A: Docker Desktop 已內建 Buildx，或執行：
```bash
docker buildx install
docker buildx create --use
```

**Q: 前端如何設定開發代理？**  
A: 修改 `cheng-ui/vue.config.js` 中的 `devServer.proxy` 配置：
```javascript
proxy: {
  [process.env.VUE_APP_BASE_API]: {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

### 聯絡方式

- **專案主頁**：[GitHub Repository](https://github.com/mark22013333/Cheng-Vue)
- **線上預覽**：[https://cool-apps.zeabur.app](https://cool-apps.zeabur.app)
- **作者部落格**：[https://mark22013333.github.io/](https://mark22013333.github.io/)
- **問題回報**：[GitHub Issues](https://github.com/mark22013333/Cheng-Vue/issues)

### 貢獻方式

歡迎提交 Pull Request 或 Issue！

1. Fork 本專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 📄 授權協議

本專案採用 [MIT License](./LICENSE) 授權協議。

```
MIT License

Copyright (c) 2025 CoolApps

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

**⭐ 如果這個專案對你有幫助，請給我們一個 Star！**

Made with ❤️ by [CoolApps Team](https://mark22013333.github.io/)

</div>
