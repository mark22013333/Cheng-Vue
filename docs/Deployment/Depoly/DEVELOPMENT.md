# 🛠️ 開發指南

## 📋 目錄

- [環境準備](#環境準備)
- [快速開始](#快速開始)
- [開發規範](#開發規範)
- [核心開發規範](#核心開發規範)
- [專案結構](#專案結構)
- [新增功能模組](#新增功能模組)
- [開發工具](#開發工具)

## 🔧 環境準備

### 必要軟體

確保你的開發環境已安裝以下軟體：

| 工具 | 版本要求 | 檢查指令 | 說明 |
|------|----------|----------|------|
| JDK | 17+ | `java -version` | Java 開發環境 |
| Node.js | 18+ | `node -v` | 前端開發環境 |
| Maven | 3.9+ | `mvn -v` | 專案建置工具 |
| MySQL | 8.0+ | `mysql --version` | 資料庫 |
| Redis | 6.0+ | `redis-cli --version` | 快取資料庫 |
| Git | 2.x+ | `git --version` | 版本控制 |

### IDE 建議

- **後端**：IntelliJ IDEA 2023+
- **前端**：Visual Studio Code / WebStorm

## 🚀 快速開始

### 1. 資料庫初始化

```sql
-- 建立資料庫
CREATE DATABASE `cool-apps` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> **注意**：資料庫結構由 Flyway Migration 自動建立，無需手動匯入 SQL 檔案。

### 2. 環境變數設定

本專案使用 **Jasypt 加密**敏感配置，需要設定解密密碼。

#### 方式一：IDE 啟動（推薦）

在 Run Configuration 的 **VM Options** 中設定：

```
-Djasypt.encryptor.password=${JASYPT_PASSWORD}
-Dspring.profiles.active=local
```

#### 方式二：命令列啟動

```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=${JASYPT_PASSWORD}
```

#### 取得 JASYPT_PASSWORD

⚠️ **重要**：`JASYPT_PASSWORD` 不存放在 Git 版控中。

**取得方式**：
- 聯繫專案負責人取得
- 或查看團隊內部文檔（如 Notion、Confluence）

### 3. 配置本地環境

編輯 `cheng-admin/src/main/resources/application-local.yml`：

```yaml
# 資料庫連線（已加密，無需修改）
spring:
  datasource:
    druid:
      master:
        url: ENC(...)        # 已加密
        username: ENC(...)   # 已加密
        password: ENC(...)   # 已加密

# Redis 連線（根據實際環境修改）
  redis:
    host: localhost
    port: 6379
    database: 0
    password: ''

# 檔案上傳路徑（根據需要修改）
cheng:
  profile: /Users/cheng/uploadPath
```

### 4. 啟動後端服務

#### 使用 IDE（推薦）

1. 開啟 `CoolAppsApplication.java`
2. 右鍵選擇 **Run 'CoolAppsApplication'**
3. 確認 VM Options 已設定 `-Djasypt.encryptor.password=${JASYPT_PASSWORD}`

#### 使用 Maven

```bash
cd /path/to/Cheng-Vue
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=${JASYPT_PASSWORD}
```

#### 啟動成功標誌

看到以下彩色日誌輸出即表示啟動成功：

```
════════════════════════════════════════════════════
  Git Hooks 自動安裝
════════════════════════════════════════════════════
✅ 成功安裝 2 個 Git Hook(s)

════════════════════════════════════════════════════
  LOG 系統配置資訊
════════════════════════════════════════════════════
應用程式名稱: CoolApps
啟用環境: local
日誌根目錄: /Users/cheng/cool-logs
```

驗證後端：
```bash
# 訪問首頁
curl http://localhost:8080

# 訪問 API 文檔
open http://localhost:8080/swagger-ui/index.html
```

### 5. 啟動前端服務

```bash
cd cheng-ui

# 安裝依賴（首次執行）
npm install

# 啟動開發伺服器
npm run dev
```

訪問系統：
```
前端地址：http://localhost:1024
後端地址：http://localhost:8080
API 文檔：http://localhost:8080/swagger-ui/index.html

預設帳號：admin
預設密碼：admin123
```

## 📝 開發規範

### 1. 程式碼風格

- **縮排**：4 個空格（Java）、2 個空格（Vue/JS）
- **命名規範**：
  - 類名：`PascalCase`
  - 方法名：`camelCase`
  - 常數：`UPPER_SNAKE_CASE`
  - 變數名：`camelCase`

### 2. 註解使用

#### `@PreAuthorize` - 權限控制
```java
@PreAuthorize("@ss.hasPermi('system:user:list')")
@GetMapping("/list")
public TableDataInfo list(SysUser user) { }
```

#### `@Log` - 操作日誌
```java
@Log(title = "使用者管理", businessType = BusinessType.INSERT)
@PostMapping
public AjaxResult add(@RequestBody SysUser user) { }
```

#### `@RepeatSubmit` - 防重複提交
```java
@RepeatSubmit(interval = 3000, message = "請勿重複提交")
@PostMapping("/save")
public AjaxResult save(@RequestBody Data data) { }
```

#### `@Anonymous` - 匿名訪問
```java
@Anonymous
@GetMapping("/public/data")
public AjaxResult publicData() { }
```

### 3. 異常處理

- 使用統一的異常處理機制
- 自定義業務異常繼承 `ServiceException`
- 返回統一的響應格式 `AjaxResult`

### 4. Git 提交規範

專案已配置 **Git Hooks 自動驗證**，提交時會自動檢查：

#### Commit Message 格式

```bash
# 正確格式
git commit -m "feat: 新增使用者管理功能"
git commit -m "fix: 修正登入驗證邏輯"
git commit -m "docs: 更新 README 說明"

# 帶 scope（推薦）
git commit -m "feat(auth): 新增 JWT Token 驗證"
git commit -m "fix(system): 修正選單權限檢查問題"

# 跳過驗證（不建議）
git commit --no-verify -m "message"
```

**允許的 prefix**：
- `feat`: 新增功能
- `fix`: 修正錯誤
- `docs`: 文檔更新
- `style`: 程式碼格式調整
- `refactor`: 重構
- `perf`: 效能優化
- `test`: 測試相關
- `chore`: 建置/工具變更

詳細說明：[Git Hooks 自動安裝機制](./docs/Line/GIT_HOOKS_AUTO_INSTALL.md)

## 🎯 核心開發規範

> **重要**：這些是專案的核心規範，優先級最高，必須嚴格遵守！

### 1. ⭐ 強制使用 Enum（最重要！）

**絕對禁止使用魔術數字或字串**：

```java
// ❌ 錯誤：使用魔術數字
if (status.equals("0")) { ... }
if (type == 1) { ... }

// ✅ 正確：使用 Enum
if (status == Status.NORMAL) { ... }
if (channelType == ChannelType.MAIN) { ... }
```

**Enum 標準範本**：
```java
public enum Status implements CodedEnum<String> {
    NORMAL("0", "正常"),
    DISABLE("1", "停用");

    private final String code;
    private final String description;

    Status(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static Status getByCode(String code) {
        return Arrays.stream(values())
            .filter(e -> e.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
}
```

### 2. ⭐ 前端狀態參數處理

#### 後端返回 → 前端顯示
```javascript
let statusValue = '1'
if (response.data.status) {
  const status = response.data.status
  if (typeof status === 'object' && status.code !== undefined) {
    statusValue = String(status.code)  // 枚舉物件
  } else if (typeof status === 'number') {
    statusValue = String(status)  // 數字
  } else if (typeof status === 'string') {
    statusValue = status === 'ENABLE' ? '1' : '0'  // 字串
  }
}
this.form.status = statusValue
```

#### 前端提交 → 後端接收
```javascript
const submitData = {
  ...this.form,
  statusCode: parseInt(this.form.status),
  status: undefined  // 移除避免衝突
}
```

### 3. ⭐ MyBatis 動態 SQL 字串比較

```xml
<!-- ❌ 錯誤：數字比較 -->
<when test="stockStatus == 0">

<!-- ✅ 正確：使用 .equals() 方法 -->
<when test='stockStatus.equals("0")'>
```

### 4. ⭐ 必須使用工具類

- **HTTP 請求**：必須使用 `OkHttpUtils`
- **JSON 處理**：必須使用 `JacksonUtil`
- **LINE Bot SDK**：使用建構者模式

### 5. ⭐ 開發前檢查清單

- [ ] 是否使用 Enum 替代魔術數字？
- [ ] 前端狀態參數是否正確轉換？
- [ ] 所有列表查詢都使用分頁？
- [ ] 異常訊息是否清晰明確？
- [ ] 多表操作是否加上 `@Transactional`？

## 📁 專案結構

```
src/main/java/com/cheng/
├── common/              # 通用模組
│   ├── annotation/      # 自定義註解
│   ├── config/          # 設定類
│   ├── constant/        # 常數定義
│   ├── enums/          # 列舉定義（強制使用）
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

## 🆕 新增功能模組

### 1. 建立實體類

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

### 2. 建立 Mapper 介面

```java
@Mapper
public interface SysExampleMapper extends BaseMapper<SysExample> {
    List<SysExample> selectExampleList(SysExample example);
}
```

### 3. 建立 Service 層

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

### 4. 建立 Controller

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

## 🔧 開發工具

### LOG 系統

專案整合了智慧日誌系統：

**核心功能**：
- 🔍 **TraceId 追蹤**：自動產生唯一 ID，追蹤完整請求鏈路
- 📦 **自動壓縮**：歷史日誌自動壓縮為 .gz
- 📁 **目錄分離**：current（當日）/ archive（歷史）
- 🎨 **彩色輸出**：開發環境彩色日誌
- ⚡ **非同步支援**：非同步任務自動繼承 TraceId

**日誌目錄**：
```
本地：/Users/cheng/cool-logs/
├── current/              # 當日日誌
│   ├── sys-info.log
│   ├── sys-error.log
│   └── sys-user.log
└── archive/              # 歷史日誌（保留 60 天）
    └── sys-info.2025-11-01.log.gz
```

**查詢日誌**：
```bash
# 根據 TraceId 查詢
grep "a1B2c3D4" /Users/cheng/cool-logs/current/*.log

# 即時查看
tail -f /Users/cheng/cool-logs/current/sys-info.log
```

詳細說明：[LOG 系統配置說明](./docs/Architecture/LOG系統配置說明.md)

### Git Hooks

專案已配置自動化 Git Hooks：

- ✅ **pre-commit**：提交前自動驗證所有 Mapper 的 XML 實作
- ✅ **commit-msg**：驗證 commit message 格式

啟動後端服務時會**自動安裝**，無需手動配置。

### API 文檔

系統整合 Swagger 3.0（Springdoc）：

- **開發環境**：http://localhost:8080/swagger-ui/index.html
- **正式環境**：https://cool-apps.zeabur.app/swagger-ui/index.html

## 🔗 相關文檔

- [部署指南](./DEPLOYMENT.md)
- [專案架構文檔](./docs/Architecture/)
- [LINE Bot 開發文檔](./docs/Line/)
- [開發規範文檔](./docs/Development/)
- [Git Hooks 說明](./docs/Line/GIT_HOOKS_AUTO_INSTALL.md)
- [LOG 系統說明](./docs/Architecture/LOG系統配置說明.md)

## ❓ 常見問題

### Q: 如何取得 Jasypt 密碼？

A: 聯繫專案負責人或查看團隊內部文檔，**不要**將密碼提交到 Git。

### Q: 啟動時提示「Could not resolve placeholder 'jasypt.encryptor.password'」

A: 需要設定 `-Djasypt.encryptor.password=${JASYPT_PASSWORD}` 環境變數。

### Q: 前端無法連接後端

A: 檢查：
1. 後端是否正常啟動（http://localhost:8080）
2. 前端 `.env.development` 中的 `VUE_APP_BASE_API` 是否正確

### Q: Git 提交被拒絕

A: 檢查 commit message 格式是否符合規範（需要 prefix：feat/fix/docs 等）

## 📞 技術支援

遇到問題？

1. 查看 [常見問題](#常見問題)
2. 檢查專案 `/docs` 目錄下的詳細文檔
3. 聯繫專案負責人
