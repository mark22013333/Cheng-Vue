# CLAUDE.md - CoolApps 專案規範

> 通用規範請參考 `~/.claude/CLAUDE.md`

## 快速參考

| 項目 | 指令/位置 |
|------|----------|
| 啟動後端 | `mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password="$JASYPT_ENCRYPTOR_PASSWORD"` |
| 啟動前端 | `cd cheng-ui && pnpm run dev` |
| 執行測試 | `mvn -T 1C -DskipITs=false test` |
| 快速建置 | `mvn -q -DskipTests -pl cheng-admin -am package` |
| 前端 | http://localhost:1024 |
| 後端 API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

---

## 技術棧

**後端**：Spring Boot 3.5.4 · JDK 25 · MyBatis 3.0.4 · MySQL 8.2.0 · Flyway 11.14.1
**前端**：Vue 3.5.24 · Vite 5.4.21 · Pinia 2.3.1 · Element Plus 2.11.8
**安全**：Spring Security + JWT · Jasypt 加密
**外部**：LINE Bot SDK 9.12.0 · OkHttp 4.12.0

---

## 模組結構

```
cheng-admin/      → WAR 進入點，REST 控制器
cheng-framework/  → 核心框架（安全、AOP、配置）
cheng-common/     → 共用工具、枚舉、註解
cheng-system/     → 系統核心（使用者、角色、MyBatis Mapper）
cheng-crawler/    → ISBN 爬蟲（Selenium + ChromeDriver）
cheng-quartz/     → 排程任務
cheng-line/       → LINE Messaging API
cheng-generator/  → 程式碼產生器
cheng-shop/       → 電商模組
cheng-ui/         → Vue 3 前端
```

**依賴關係**：
```
cheng-admin ← cheng-framework ← cheng-common + cheng-system
           ← cheng-quartz/generator/crawler/line/shop
```

---

## 分層架構

```
Controller → Service → Mapper → MySQL
   ↓           ↓         ↓
web/controller  service/  mapper/ + resources/mapper/*.xml
               impl/
```

**DTO/VO 位置**：`{module}/src/main/java/com/cheng/{module}/domain/dto|vo`

---

## 專案特定規範

### 前端套件管理

**強制使用 pnpm**，禁用 npm/yarn：
```bash
cd cheng-ui
pnpm install
pnpm run dev      # port 1024
pnpm run build
```

### Jasypt 加密

所有環境必須提供密碼。實際密碼請從 1Password / 團隊共享位置取得，切勿硬編在程式或文件中：

```bash
# 方式 1：匯入到環境變數（推薦）
export JASYPT_ENCRYPTOR_PASSWORD="<your-jasypt-password>"

# 方式 2：以 JVM 參數傳入（從環境變數引用，避免落在歷史紀錄）
-Djasypt.encryptor.password="$JASYPT_ENCRYPTOR_PASSWORD"
```

### Spring Profiles

- `local` - 本地開發（Tomcat10 + Ngins + Ngrok）
- `prod` - 生產環境 (Docker_Tomcat10 + Docker_Nginx + Zeabur)
- `vm` - VM 環境 (Tomcat10 + Ngins + CentOS-8)

### 資料庫遷移

Flyway 檔案：`cheng-admin/src/main/resources/db/migration/`
- 命名：`V{版本號}__{描述}.sql`
- 啟動時自動執行

---

## 檔案結構慣例

### 後端模組

```
{module}/src/main/java/com/cheng/{module}/
├── controller/     # REST API
├── service/        # 服務介面
│   └── impl/       # 服務實作
├── mapper/         # MyBatis Mapper 介面
├── domain/         # 實體
│   ├── dto/        # 資料傳輸物件
│   └── vo/         # 視圖物件
├── config/         # Spring 配置
└── util/           # 工具類

resources/mapper/   # MyBatis XML
```

### 前端

```
cheng-ui/src/
├── api/            # API 服務
├── views/          # 頁面元件
├── components/     # 可重用元件
├── layout/         # 版面
├── store/          # Pinia 狀態
├── router/         # Vue Router
├── utils/          # 工具函式
└── composables/    # Vue 3 Composables
```

---

## 建置與部署

```bash
./build-all.sh        # 完整建置
./build-frontend.sh   # 前端
./build-backend.sh    # 後端
```

**CI/CD**：`.github/workflows/ci-cd.yml`
1. 驗證 MyBatis Mapper
2. 建置前端（Node 18, pnpm）
3. 建置後端（Java 25, Maven）
4. 部署

---

## 例外處理

```
Controller：轉換錯誤格式（不含業務判斷）
Service：拋出 BusinessException
全域處理：framework 模組 @ControllerAdvice
```

---

## Lombok

允許：`@Getter` `@Setter` `@Builder` `@ToString` `@EqualsAndHashCode` `@Slf4j`
- 實體類別謹慎使用 `@Data`
- 禁止暴露可變集合的 setter

---

## 業務規則交叉引用

### SKU 售價 / 成本合理性防呆

規格文件：`openspec/changes/archive/2026-04-14-sku-price-sanity-guard/specs/sku-price-sanity-guard/spec.md`

| 規則 | 前端 | 後端 |
|------|------|------|
| cost > price 硬阻擋 | `useProductForm.js` validateSkuList() | `ShopProductServiceImpl.validateAndEnrichSkuPricing()` |
| price=0 軟確認（dirty tracking） | `useProductForm.js` saveProduct() + ElMessageBox | 不阻擋，由前端把關 |
| price=null 自動推算 | `priceSafeguard.js` recommendedPriceFromCost() | `ShopProductServiceImpl` 以 cost × MARKUP_RATIO 填入 |
| 共用常數 MARKUP_RATIO=1.2 | `priceSafeguard.js` | `ShopPriceConstants.java` |
| 匯入 Dialog 防呆 | `InventoryImportDialog.vue` costBlockerCount | — |

---

## Design System

Always read `DESIGN.md` before making any visual or UI decisions.
All font choices, colors, spacing, and aesthetic direction are defined there.
Do not deviate without explicit user approval.
In QA mode, flag any code that doesn't match DESIGN.md.
