# FlareSolverrUtil 重構驗證清單

## ✅ 編譯驗證

### 1. 編譯狀態
```bash
mvn clean compile -pl cheng-crawler -am -DskipTests
```

**結果**: ✅ BUILD SUCCESS

```
[INFO] cheng-common ....................................... SUCCESS [  2.638 s]
[INFO] cheng-crawler ...................................... SUCCESS [  0.851 s]
[INFO] BUILD SUCCESS
```

### 2. 依賴檢查

#### cheng-common (已包含)
- ✅ `com.squareup.okhttp3:okhttp`
- ✅ `com.squareup.okhttp3:logging-interceptor`
- ✅ `org.apache.commons:commons-pool2`
- ✅ `org.apache.commons:commons-csv`

#### cheng-crawler (已包含)
- ✅ `com.cheng:cheng-common`
- ✅ Jackson Databind
- ✅ FastJSON2

---

## 🧪 功能驗證

### 測試步驟

#### Step 1: 啟動 FlareSolverr
```bash
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
docker-compose up -d

# 驗證啟動
docker ps | grep flaresolverr
```

**預期結果**: 容器正在執行，埠號 8191 已開放

#### Step 2: 執行單元測試
```bash
cd cheng-crawler
./test-flaresolverr.sh
```

**預期結果**: 6 個測試步驟全部通過
- ✅ Docker 服務檢查
- ✅ FlareSolverr 容器檢查
- ✅ 服務連接測試
- ✅ Session 管理測試
- ✅ Cloudflare 驗證處理測試
- ✅ Session 清理

#### Step 3: 啟動應用測試
```bash
# 方式 1: IDEA 中執行 ChengApplication

# 方式 2: Maven 命令
mvn spring-boot:run -pl cheng-admin
```

#### Step 4: API 測試
```bash
# 測試 ISBN 查詢（會使用 FlareSolverr）
curl http://localhost:8080/isbn/9789863877363

# 預期回應
{
  "code": 200,
  "msg": "查詢成功",
  "data": {
    "isbn": "9789863877363",
    "title": "幸福的鬼島",
    "author": "林宜敬",
    "publisher": "印刻",
    "publishDate": "2024年06月01日",
    "success": true
  }
}
```

---

## 📊 效能驗證

### 連接池監控

檢查 OkHttpUtilsPool 日誌：

```bash
tail -f ~/cool-logs/cheng-admin.log | grep "Pool Status"
```

**預期輸出**:
```
Pool Status: Active=1, Idle=4
Pool Status: Created=5, Destroyed=0
```

### 記憶體監控

```bash
# 查看應用記憶體使用
jps | grep ChengApplication
jstat -gc <PID> 1000 10
```

### 回應時間監控

觀察日誌中的時間記錄：
```
[INFO] 發送 FlareSolverr 請求: https://isbn.tw/9789863877363
[INFO] FlareSolverr 請求成功: Challenge solved!
[INFO] FlareSolverr 成功取得頁面內容（HTML 長度: 12345 字元）
```

**預期時間**: 5-15 秒（首次請求）

---

## 🔍 錯誤檢查

### 常見錯誤排查

#### 錯誤 1: ClassNotFoundException: OkHttpUtils

**檢查**:
```bash
# 確認 cheng-crawler 依賴 cheng-common
grep -A 2 "cheng-common" cheng-crawler/pom.xml
```

**修復**:
```xml
<dependency>
    <groupId>com.cheng</groupId>
    <artifactId>cheng-common</artifactId>
</dependency>
```

#### 錯誤 2: NoSuchMethodError: JacksonUtil.genJsonObject

**檢查**:
```bash
# 確認 JacksonUtil 版本
grep -n "genJsonObject" cheng-common/src/main/java/com/cheng/common/utils/JacksonUtil.java
```

**預期**: 應該找到 `public static ObjectNode genJsonObject()` 方法

#### 錯誤 3: Connection refused (FlareSolverr)

**檢查**:
```bash
# 測試連接
curl -X POST http://localhost:8191/v1 \
  -H "Content-Type: application/json" \
  -d '{"cmd":"sessions.list"}'
```

**修復**:
```bash
# 重啟 FlareSolverr
docker-compose restart flaresolverr
```

---

## 📝 代碼審查檢查清單

### FlareSolverrUtil.java

- [x] 移除所有 Apache HttpClient 匯入
- [x] 匯入 OkHttpUtils 相關類別
- [x] sendRequest 方法使用 OkHttpUtils
- [x] Map → ObjectNode 正確轉換
- [x] HTTP 狀態碼使用 Spring HttpStatus
- [x] 保持重試邏輯
- [x] 保持錯誤處理
- [x] 保持日誌記錄
- [x] 註釋清晰準確

### IsbnCrawlerServiceImpl.java

- [x] crawlWithFlareSolverr 方法正確呼叫 FlareSolverrUtil
- [x] 錯誤處理邏輯完整
- [x] 降級機制正常運作

---

## 🎯 驗證結果

### 編譯驗證
- ✅ cheng-common 編譯成功
- ✅ cheng-crawler 編譯成功
- ✅ 無編譯錯誤
- ✅ 無依賴衝突

### 功能驗證
- ⏳ FlareSolverr 服務測試（待執行）
- ⏳ API 整合測試（待執行）
- ⏳ Session 管理測試（待執行）

### 效能驗證
- ⏳ 連接池運作（待驗證）
- ⏳ 記憶體使用（待監控）
- ⏳ 回應時間（待測量）

---

## 📋 下一步行動

### 立即執行

1. **啟動 FlareSolverr**
   ```bash
   docker-compose up -d
   ```

2. **執行測試腳本**
   ```bash
   cd cheng-crawler
   ./test-flaresolverr.sh
   ```

3. **啟動應用**
   ```bash
   # 在 IDEA 中執行或使用：
   mvn spring-boot:run -pl cheng-admin
   ```

4. **測試 API**
   ```bash
   curl http://localhost:8080/isbn/9789863877363
   ```

### 監控和觀察

1. **查看應用日誌**
   ```bash
   tail -f ~/cool-logs/cheng-admin.log
   ```

2. **監控連接池**
   ```bash
   tail -f ~/cool-logs/cheng-admin.log | grep "Pool Status"
   ```

3. **檢查 FlareSolverr 日誌**
   ```bash
   docker logs -f flaresolverr
   ```

### 問題回報

如果遇到任何問題，請記錄：
1. 錯誤訊息（完整堆疊追蹤）
2. 發生時間和場景
3. 相關日誌片段
4. 系統環境資訊

---

## 🎉 驗證完成標準

當以下所有項目都打勾時，重構驗證完成：

- [x] 編譯成功，無錯誤
- [ ] FlareSolverr 服務正常執行
- [ ] 測試腳本全部通過
- [ ] API 測試返回正確結果
- [ ] 連接池正常運作
- [ ] 無記憶體洩漏
- [ ] 回應時間符合預期
- [ ] 錯誤處理正確
- [ ] 日誌輸出清晰

---

**建立日期**: 2025-01-04  
**最後更新**: 2025-01-04  
**維護人員**: cheng
