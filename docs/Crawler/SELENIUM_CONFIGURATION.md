# Selenium 配置說明文件

## 概述

系統支援兩種 Selenium WebDriver 模式：
- **Remote 模式**：連接到獨立的 Selenium 容器
- **Local 模式**：使用容器內或本機的 ChromeDriver

**本專案的環境配置**（與一般配置相反）：
- 🖥️ **本機開發**：使用 `remote` 模式（連接 Docker Selenium 容器 `localhost:9515`）
- ☁️ **Zeabur 部署**：使用 `local` 模式（Chrome 安裝在後端容器內）

可透過配置檔案輕鬆切換，無需修改程式碼。

---

## 配置方式

### 1. 主配置檔案 (`application.yml`)

```yaml
# 爬蟲相關設定
crawler:
  selenium:
    # 模式：remote (使用 Docker Selenium) 或 local (使用本地 ChromeDriver)
    mode: local
    # Docker Selenium 遠端連線 URL
    remote-url: http://localhost:9515
    # 本地 ChromeDriver 路徑
    chrome-driver-path: /usr/bin/chromedriver
```

### 2. 本地環境配置 (`application-local.yml`)

```yaml
# 爬蟲設定 - 本地環境（使用 Docker Selenium）
crawler:
  selenium:
    mode: remote
    remote-url: http://localhost:9515
```

**注意**：本機開發使用 Docker Selenium 容器，所以是 `remote` 模式。

### 3. 正式環境配置 (`application-prod.yml`)

```yaml
# 爬蟲設定 - 正式環境（Chrome 安裝在容器內）
crawler:
  selenium:
    mode: local
    chrome-driver-path: /usr/bin/chromedriver
```

**注意**：Zeabur 部署時 Chrome 安裝在後端容器內，所以是 `local` 模式。

---

## Docker Selenium 設定

### Docker Compose 配置範例

```yaml
services:
  chrome:
    build:
      context: .
      dockerfile: Dockerfile
      platforms:
        - linux/amd64
        - linux/arm64
    container_name: chrome-driver
    restart: unless-stopped
    ports:
      - "9515:9515"
    shm_size: 2gb
    labels:
      maintainer: "Cheng® <mark22013333@gmail.com>"
      purpose: "Selenium ChromeDriver Container for Crawler"
    environment:
      - CHROME_ARGS=--headless --no-sandbox --disable-dev-shm-usage
```

### 啟動 Docker Selenium

```bash
# 啟動容器
docker-compose up -d chrome

# 檢查容器狀態
docker ps | grep chrome-driver

# 測試連接
curl http://localhost:9515/status
```

---

## 程式碼使用範例

### 1. 自動使用配置（推薦）

爬蟲 Handler 會自動根據配置檔建立 WebDriver，無需額外設定：

```java
@Override
protected List<RawData> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) throws Exception {
    // driver 已經根據配置自動建立（remote 或 local）
    driver.get("https://example.com");
    // ... 爬取邏輯
}
```

### 2. 手動指定模式

如果需要在程式中手動控制：

```java
// 使用配置建立（推薦）
WebDriver driver = SeleniumUtil.createWebDriver(crawlerProperties);

// 或手動指定 Remote 模式
WebDriver remoteDriver = SeleniumUtil.createRemoteWebDriver(
    "http://localhost:9515", 
    true,  // headless
    true,  // randomUserAgent
    true   // antiDetection
);

// 或手動指定 Local 模式
WebDriver localDriver = SeleniumUtil.createLocalWebDriver(
    "/usr/local/bin/chromedriver",
    true,  // headless
    true,  // randomUserAgent
    true   // antiDetection
);
```

---

## 環境切換

### 本機開發環境（使用 Docker Selenium）

**步驟**：

1. 啟動 Docker Selenium 容器：
```bash
docker-compose up -d chrome
# 或使用你的 Selenium image
docker run -d -p 9515:9515 --name chrome-driver your-selenium-image
```

2. 啟動後端應用（使用 local profile）：
```bash
# 設定 profile 為 local
export SPRING_PROFILES_ACTIVE=local

# 啟動應用
mvn spring-boot:run -Dspring-boot.run.profiles=local -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

**要求**：
- Docker Selenium 容器執行在 `localhost:9515`
- 可以透過 `curl http://localhost:9515/status` 測試連接

**日誌輸出**：
```
[CA101] 使用 remote 模式建立 WebDriver
建立遠端 WebDriver，URL: http://localhost:9515
```

---

### Zeabur 部署環境（Chrome 在容器內）

**步驟**：

1. 建置 Docker Image（已包含 Chrome）：
```bash
docker build -f Dockerfile.backend-tomcat -t cheng-backend .
```

2. 部署到 Zeabur：
```bash
# Zeabur 會自動使用 prod profile
# Chrome 和 ChromeDriver 已安裝在容器內
```

**要求**：
- Dockerfile 已安裝 Chrome 和 ChromeDriver
- 容器內有 `/usr/bin/chromedriver`

**日誌輸出**：
```
[CA101] 使用 local 模式建立 WebDriver
建立本地 WebDriver，路徑: /usr/bin/chromedriver
```

---

## 環境對應表

| 環境 | Profile | Selenium 模式 | 連接方式 | Chrome 位置 |
|------|---------|--------------|----------|------------|
| 🖥️ **本機開發** | `local` | `remote` | `http://localhost:9515` | Docker 容器 |
| ☁️ **Zeabur** | `prod` | `local` | `/usr/bin/chromedriver` | 後端容器內 |

**為什麼與一般配置相反？**
- 本機開發時，已使用 Docker Selenium 容器，避免污染本機環境
- Zeabur 部署時，為簡化架構，將 Chrome 包在後端容器內（單容器部署）

---

## 故障排除

### 問題 1：無法連接到遠端 Selenium

**錯誤訊息**：
```
無法連接到遠端 Selenium: http://localhost:9515
```

**解決方案**：
1. 檢查 Docker 容器是否執行：`docker ps | grep chrome`
2. 檢查埠號是否正確：`curl http://localhost:9515/status`
3. 檢查防火牆設定
4. 驗證配置檔中的 `remote-url` 是否正確

### 問題 2：本地 ChromeDriver 找不到

**錯誤訊息**：
```
The path to the driver executable must be set
```

**解決方案**：
1. 確認 ChromeDriver 已安裝：`which chromedriver`
2. 檢查配置檔路徑是否正確
3. 確認檔案有執行權限：`chmod +x /path/to/chromedriver`

### 問題 3：配置未生效

**現象**：
```
CrawlerProperties 未載入，使用預設 ChromeDriver 路徑
```

**解決方案**：
1. 檢查 `application.yml` 配置格式是否正確
2. 確認 profile 已正確設定
3. 重新啟動應用程式

---

## 配置項目說明

| 配置項 | 說明 | 預設值 | 範例 |
|--------|------|--------|------|
| `crawler.selenium.mode` | Selenium 模式 | `local` | `remote` 或 `local` |
| `crawler.selenium.remote-url` | 遠端 Selenium URL | `http://localhost:9515` | `http://selenium:4444` |
| `crawler.selenium.chrome-driver-path` | 本地 ChromeDriver 路徑 | `/usr/bin/chromedriver` | `/usr/local/bin/chromedriver` |

---

## 優勢

✅ **環境隔離**：開發和正式環境使用不同配置  
✅ **彈性切換**：透過配置檔輕鬆切換模式  
✅ **向後相容**：保留原有的無參數方法  
✅ **易於維護**：配置集中管理  
✅ **容器化支援**：完整支援 Docker Selenium  

---

## 相關檔案

- **配置類別**：`com.cheng.crawler.config.CrawlerProperties`
- **工具類別**：`com.cheng.crawler.utils.SeleniumUtil`
- **基礎類別**：`com.cheng.crawler.handler.CrawlerHandler`
- **常數類別**：`com.cheng.crawler.enums.Constant`

---

**最後更新**：2025-10-24  
**作者**：Cheng
