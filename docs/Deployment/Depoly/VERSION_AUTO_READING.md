# 自動版本讀取功能

## 概述

建置腳本會自動從不同來源讀取版本號，避免手動維護多個地方的版本號：
- **後端** `build-backend.sh`：讀取 `pom.xml` 的 `cheng.version` 屬性
- **前端** `build-frontend.sh`：讀取 `cheng-ui/src/data/changelog.js` 的最新版本

## 後端版本讀取（build-backend.sh）

### 工作原理

1. 腳本執行時會調用 `read_backend_version()` 函數
2. 使用 `grep '<cheng.version>' pom.xml` 提取屬性值
3. 通過正則表達式 `^[0-9]+\.[0-9]+\.[0-9]+$` 驗證格式
4. 如果提取失敗，使用 `BASE_VERSION="3.9.0"` 作為 fallback

### 格式要求

`pom.xml` 中的版本號必須遵循以下格式：

```xml
<properties>
    <cheng.version>3.9.0</cheng.version>
</properties>
```

**重要**：
- 版本號格式：`X.Y.Z`（無 v 前綴）
- 符合 Maven 版本號慣例

## 前端版本讀取（build-frontend.sh）

### 工作原理

1. 腳本執行時會調用 `read_frontend_version()` 函數
2. 使用 `grep -m 1 '^    version: "v'` 提取第一個符合格式的版本號
3. 通過正則表達式 `^v[0-9]+\.[0-9]+\.[0-9]+$` 驗證格式
4. 如果提取失敗，使用 `BASE_VERSION="v1.3.1"` 作為 fallback

### 格式要求

`changelog.js` 中的版本號必須遵循以下格式：

```javascript
const rawLogs = [
  {
    version: "v2.0.0",  // ← 必須是這個格式
    date: "2025-11-30",
    items: [...]
  }
];
```

**重要**：
- 必須使用 4 個空格縮排
- 必須使用雙引號 `"`
- 版本號格式：`vX.Y.Z`

## 版本號格式對比

| 項目 | 讀取來源 | 格式 | 範例 | Docker 標籤 |
|------|----------|------|------|-------------|
| 後端 | pom.xml cheng.version | X.Y.Z | 3.9.0 | 3.9.0-20251201-1516 |
| 前端 | changelog.js | vX.Y.Z | v2.0.0 | v2.0.0-20251201-1517 |

## 錯誤處理

- 如果來源檔案不存在，使用預設版本
- 如果格式不符，顯示警告並使用預設版本
- 所有錯誤都有詳細的日誌輸出

## 使用方式

```bash
# 後端建置（自動讀取 pom.xml）
./build-backend.sh

# 前端建置（自動讀取 changelog.js）
./build-frontend.sh

# 手動指定版本（覆蓋自動讀取）
BASE_VERSION=v2.1.0 ./build-frontend.sh
BASE_VERSION=3.10.0 ./build-backend.sh

# 自動確認（CI/CD 環境）
AUTO_CONFIRM=true ./build-backend.sh
```

## 注意事項

- 不要修改 changelog.js 註解中的 `version: "vX.Y.Z"` 範例，會影響解析
- 如果使用 Prettier 等格式化工具，確保不改變版本號行的格式
- 在 CI/CD 環境中建議設定 `AUTO_CONFIRM=true`
- 後端版本號遵循 Maven 慣例，前端版本號遵循語義化版本慣例
