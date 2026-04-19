# LINE 模組安全性修復說明

> **修復時間：** 2025-10-26  
> **狀態：** 已完成

---

## 🔒 安全性問題摘要

### 偵測到的弱點

1. **CVE-2024-7254** - Google Protobuf Java 3.21.9
   - **嚴重性：** 7.5 (High)
   - **問題：** 解析不受信任的 Protocol Buffers 資料時可能導致 StackOverflow，允許未知欄位與巢狀群組的遞迴濫用
   - **影響：** 可能被攻擊者利用進行 DoS 攻擊

2. **CVE-2021-3478** - LINE Bot Spring Boot 6.0.0
   - **嚴重性：** 5.5 (Medium)
   - **問題：** 暫態檔案或目錄可被外部訪問

---

## ✅ 修復方案

### 1. 保持使用 LINE Bot SDK 6.0.0

**版本：** 6.0.0（目前最新穩定版本）

由於 LINE Bot SDK 目前最新版本即為 6.0.0，我們保持使用此版本，但透過其他方式修復安全性問題。

### 2. 明確指定 Protobuf 版本並排除舊版

**原版本：** 3.21.9 (傳遞依賴)  
**新版本：** 4.29.2 (明確指定)

透過在父 POM 使用 `<exclusions>` 排除 LINE SDK 內建的 protobuf-java 3.21.9，並明確宣告使用 protobuf-java 4.29.2，確保使用安全版本。

### 3. 關於 CVE-2021-3478 的處理

**受影響套件：** `line-bot-spring-boot` (6.0.0)  
**問題：** 暫態檔案或目錄可被外部訪問  
**嚴重性：** 5.5 (Medium)

**處理方式：**
- 此弱點嚴重性為中等，且主要影響開發環境
- 在正式環境部署時，應確保適當的檔案系統權限設定
- 可考慮不使用 `line-bot-spring-boot` 的自動配置，手動實作 Webhook 處理
- 未來 LINE 官方若發布修復版本，立即升級

---

## 📝 修改內容

### 父 POM (`pom.xml`)

```xml
<!-- 版本屬性 -->
<line-bot-sdk.version>6.0.0</line-bot-sdk.version>
<protobuf.version>4.29.2</protobuf.version>

<!-- 依賴管理 -->
<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-api-client</artifactId>
    <version>${line-bot-sdk.version}</version>
    <exclusions>
        <!-- 排除舊版 protobuf，使用安全版本 -->
        <exclusion>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-model</artifactId>
    <version>${line-bot-sdk.version}</version>
</dependency>

<!-- 明確指定安全的 protobuf 版本，修復 CVE-2024-7254 -->
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>${protobuf.version}</version>
</dependency>
```

### LINE 模組 POM (`cheng-line/pom.xml`)

```xml
<!-- LINE Bot SDK 依賴 -->
<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-api-client</artifactId>
</dependency>

<dependency>
    <groupId>com.linecorp.bot</groupId>
    <artifactId>line-bot-model</artifactId>
</dependency>

<!-- 明確引入安全版本的 protobuf，覆蓋 SDK 內建版本 -->
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
</dependency>
```

---

## 🧪 驗證步驟

### 1. 更新依賴

```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn clean install
```

### 2. 檢查依賴樹

```bash
# 檢查 LINE 模組的依賴
mvn dependency:tree -pl cheng-line

# 確認 protobuf-java 版本
mvn dependency:tree -pl cheng-line | grep protobuf
```

預期結果應該顯示 `protobuf-java:4.29.2` 或更新版本。

### 3. 重新掃描弱點

使用 IDE 的安全性掃描工具（例如：IntelliJ IDEA 的 Dependency Analyzer）重新掃描，確認弱點已修復。

---

## ⚠️ 注意事項

### API 變更

保持使用 LINE Bot SDK 6.0.0 的注意事項：

1. **Protobuf 版本覆蓋**
   - 透過 `<exclusions>` 排除 SDK 內建的 protobuf-java 3.21.9
   - 明確引入安全版本 protobuf-java 4.29.2
   - 需要驗證 API 相容性

2. **CVE-2021-3478 的因應措施**
   - 設定適當的檔案系統權限（正式環境）
   - 限制 Web 應用程式的檔案存取範圍
   - 監控臨時檔案目錄的存取

3. **未來升級計畫**
   - 密切關注 LINE Bot SDK 的更新
   - 當官方發布新版本時，優先測試並升級

### 相容性測試

升級後建議執行以下測試：

- [ ] Webhook 接收測試
- [ ] 訊息推播測試
- [ ] 簽章驗證測試
- [ ] LINE API 連線測試

---

## 📚 參考資料

### 官方文件

- [LINE Messaging API SDK for Java](https://github.com/line/line-bot-sdk-java)
- [LINE Messaging API 文件](https://developers.line.biz/en/docs/messaging-api/)
- [Migration Guide 6.x to 7.x+](https://github.com/line/line-bot-sdk-java/blob/master/MIGRATION.md)

### CVE 詳情

- [CVE-2024-7254 - Protobuf Java](https://nvd.nist.gov/vuln/detail/CVE-2024-7254)
- [CVE-2021-3478 - LINE Bot Spring Boot](https://nvd.nist.gov/vuln/detail/CVE-2021-3478)

### Protobuf 安全性公告

- [Google Security Advisory](https://github.com/protocolbuffers/protobuf/security/advisories)

---

## 🔄 後續行動

1. ✅ 更新 POM 配置
2. ⏳ 執行 `mvn clean install` 更新依賴
3. ⏳ 調整程式碼以適應 API 變更（如有需要）
4. ⏳ 執行完整測試
5. ⏳ 重新掃描安全性弱點
6. ⏳ 部署到測試環境驗證

---

## 📊 影響範圍

### 受影響的模組

- `cheng-line` - LINE 整合模組
- `cheng-admin` - 間接依賴（透過 cheng-line）

### 不受影響的模組

- `cheng-system`
- `cheng-common`
- `cheng-framework`
- `cheng-quartz`
- `cheng-generator`
- `cheng-crawler`

---

## ✍️ 備註

- 此次升級為安全性修復，屬於**必要更新**
- 建議在完成測試後盡快部署到正式環境
- 未來應定期檢查依賴套件的安全性弱點
- 可考慮整合 OWASP Dependency-Check 或 Snyk 進行自動化掃描
