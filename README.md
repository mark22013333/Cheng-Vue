# 🚀 CoolApps 管理系統

<div align="center">

![CoolApps Logo](https://img.shields.io/badge/CoolApps-v1.2.2-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-2.6.12-4fc08d.svg)
![Element UI](https://img.shields.io/badge/Element%20UI-2.15.14-409EFF.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**一個現代化的企業級後台管理系統**

基於 Spring Boot 3.5.4 + Vue.js 2.6 + Element UI 的前後端分離架構

[線上預覽](https://cheng.tplinkdns.com) | [部署文檔](./cheng.deploy/README.md) | [API 文檔](https://cheng.tplinkdns.com/prod-api/swagger-ui/index.html)

</div>

## 📋 目錄

- [專案介紹](#專案介紹)
- [技術架構](#技術架構)
- [功能特色](#功能特色)
- [系統模組](#系統模組)
- [快速開始](#快速開始)
- [部署指南](#部署指南)
- [開發指南](#開發指南)
- [API 文檔](#api-文檔)
- [更新日誌](#更新日誌)
- [貢獻指南](#貢獻指南)
- [授權協議](#授權協議)

## 🎯 專案介紹

CoolApps 是一個基於若依(RY)專案修改的 Spring Boot 3.5.4 和 Vue.js 2.6 的企業級後台管理系統，採用前後端分離架構設計。系統提供了完整的用戶管理、角色權限、系統監控、程式碼產生等功能，適用於各種企業級應用場景。

### ✨ 專案亮點

- 🏗️ **現代化架構**：採用 Spring Boot 3.5.4 + Vue.js 2.6 前後端分離
- 🔐 **安全可靠**：JWT Token 認證 + Spring Security 權限控制
- 🎨 **優雅介面**：基於 Element UI 的現代化管理介面
- 📱 **響應式設計**：支援桌面端和行動端訪問
- 🚀 **高效能**：Redis 暫存 + Druid 連線池優化
- 🛠️ **開發友善**：完整的程式碼產生器和開發工具

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
| Swagger | 2.8.9 | API 文檔 |
| Quartz | - | 定時任務 |
| Maven | - | 專案管理 |

### 前端技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Vue.js | 2.6.12 | 前端框架 |
| Vue Router | 3.4.9 | 路由管理 |
| Vuex | 3.6.0 | 狀態管理 |
| Element UI | 2.15.14 | UI 元件庫 |
| Axios | 0.28.1 | HTTP 請求 |
| ECharts | 5.4.0 | 圖表庫 |
| Quill | 2.0.2 | 富文字編輯器 |

### 開發環境

| 工具 | 版本要求 | 說明 |
|------|----------|------|
| JDK | 17+ | Java 開發環境 |
| Node.js | 16+ | 前端開發環境 |
| MySQL | 8.0+ | 資料庫 |
| Redis | 6.0+ | 暫存資料庫 |
| Maven | 3.6+ | 專案建置工具 |

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
- **個人中心**：個人資料、密碼修改
- **檔案管理**：檔案上傳、下載管理

## 🏗️ 系統模組

```
cheng-vue
├── cheng-admin          # 管理後台模組 (產出 apps.war)
├── cheng-common         # 通用工具模組
├── cheng-framework      # 核心框架模組
├── cheng-generator      # 程式碼產生模組
├── cheng-quartz         # 定時任務模組
├── cheng-system         # 系統管理模組
├── cheng-ui             # 前端 Vue.js 專案
├── cheng.deploy         # 部署腳本和設定
├── sql                  # 資料庫腳本
└── doc                  # 專案文檔
```

### 模組說明

- **cheng-admin**：系統主模組，整合所有功能模組，產出 `apps.war` 部署檔案
- **cheng-common**：通用工具類、常數定義、共用元件
- **cheng-framework**：核心框架設定，包含安全、暫存、異常處理等
- **cheng-generator**：程式碼產生器，支援自動產生 Controller、Service、Mapper 等
- **cheng-quartz**：定時任務管理，基於 Quartz 實現
- **cheng-system**：系統管理功能，包含用戶、角色、選單等管理
- **cheng-ui**：前端專案，基於 Vue.js 2.6 + Element UI

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

### 預設帳號

| 角色 | 帳號 | 密碼 | 說明 |
|------|------|------|------|
| 超級管理員 | admin | admin123 | 擁有所有權限 |
| 普通用戶 | cheng | 123456 | 基本權限 |

## 🚢 部署指南

### 自動化部署

系統提供完整的自動化部署腳本：

```bash
# 1. 後端編譯和部署
./cheng.deploy/deploy-backend.sh

# 2. 前端建置和部署
./cheng.deploy/build-frontend.sh

# 3. 完整部署到伺服器
./cheng.deploy/deploy-to-server.sh
```

### 手動部署

詳細的手動部署步驟請參考：[部署指南](./cheng.deploy/manual-deployment-guide.md)

### 正式環境設定

1. **資料庫設定**：
```yaml
# application-prod.yml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/cool-apps?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: ${DB_USERNAME:demouser}
        password: ${DB_PASSWORD:your_secure_password}
```

2. **Redis 設定**：
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:your_redis_password}
      database: 0
```

3. **Nginx 設定**：
```nginx
# 前端靜態資源
location / {
    root /var/www/cheng-vue/frontend;
    try_files $uri $uri/ /index.html;
}

# 後端 API 代理
location /prod-api/ {
    proxy_pass http://127.0.0.1:8080/apps/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

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

## 📚 API 文檔

系統整合了 Swagger 3.0，提供完整的 API 文檔：

- **開發環境**：http://localhost:8080/swagger-ui/index.html
- **正式環境**：https://cheng.tplinkdns.com/prod-api/swagger-ui/index.html

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

### 單元測試

```bash
# 執行所有測試
mvn test

# 執行特定測試類
mvn test -Dtest=SysUserServiceTest

# 產生測試報告
mvn test jacoco:report
```

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

### v3.9.0 (2025-09-22)
- ✨ 專案架構從 LineGroup 遷移到 CoolApps
- 🔧 WAR 檔案名稱統一為 `apps.war`
- 🚀 升級 Spring Boot 到 3.5.4
- 🎨 優化前端介面和用戶體驗
- 📝 完善部署文檔和腳本
- 🔐 增強安全設定和權限控制

## 🤝 貢獻指南

我們歡迎所有形式的貢獻，包括但不限於：

- 🐛 Bug 報告
- 💡 功能建議  
- 📝 文檔改進
- 🔧 程式碼貢獻

### 貢獻流程

1. Fork 本專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

### 開發規範

- 遵循現有的程式碼風格
- 新增適當的測試
- 更新相關文檔
- 確保 CI 檢查通過

## 📞 技術支援

### 聯絡方式

- **專案主頁**：[GitHub Repository](https://github.com/mark22013333/Cheng-Vue)
- **作者部落格**：[https://mark22013333.github.io/](https://mark22013333.github.io/)
- **問題回報**：[GitHub Issues](https://github.com/mark22013333/Cheng-Vue/issues)

### 常見問題

**Q: 如何修改預設密碼？**
A: 在用戶管理中修改 admin 用戶密碼，或在個人中心修改。

**Q: 如何新增自定義選單？**
A: 在系統管理 -> 選單管理中新增選單項目，設定路由和權限。

**Q: 如何部署到正式環境？**
A: 參考 [部署指南](./cheng.deploy/manual-deployment-guide.md) 進行正式環境部署。

**Q: 如何客製化主題樣式？**
A: 在介面設定中選擇主題，或修改 CSS 變數自定義樣式。

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
