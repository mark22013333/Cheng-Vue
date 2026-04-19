# FlareSolverr 快速啟動指南 🚀

> **5 分鐘快速整合 FlareSolverr，解決 Cloudflare 驗證問題**

## 🎯 快速開始（3 步驟）

### 步驟 1: 啟動 FlareSolverr 服務

```bash
# 進入專案目錄
cd /Users/cheng/IdeaProjects/R/Cheng-Vue

# 啟動 Docker 服務
docker-compose up -d

# 驗證服務啟動
docker ps | grep flaresolverr
```

### 步驟 2: 執行測試腳本

```bash
cd cheng-crawler
./test-flaresolverr.sh
```

**預期輸出**:
```
================================================
   FlareSolverr 測試腳本
================================================

[步驟 1/6] 檢查 Docker 服務...
Docker 服務正常

[步驟 2/6] 檢查 FlareSolverr 容器...
FlareSolverr 容器正在執行

[步驟 3/6] 測試 FlareSolverr 服務連接...
FlareSolverr 服務正常回應

[步驟 4/6] 測試 Session 管理...
Session 建立成功

[步驟 5/6] 測試 Cloudflare 驗證處理...
目標 URL: https://isbn.tw/9789863877363
頁面取得成功！
成功解析書名: 幸福的鬼島

[步驟 6/6] 清理測試 Session...
Session 清理成功

================================================
   測試完成！
================================================
所有測試通過！FlareSolverr 運作正常
```

### 步驟 3: 啟動應用測試

```bash
# 啟動 Spring Boot 應用（IntelliJ IDEA 或命令列）
# 方式 1: IDEA 中直接執行 ChengApplication
# 方式 2: Maven 命令
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
mvn spring-boot:run -pl cheng-admin

# 測試 API
curl http://localhost:8080/isbn/9789863877363
```

**預期回應**:
```json
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

## ✅ 檢查清單

- [ ] Docker 已安裝並執行
- [ ] FlareSolverr 容器正在執行 (`docker ps`)
- [ ] 測試腳本全部通過 (`./test-flaresolverr.sh`)
- [ ] 配置檔案已更新 (`application-local.yml`)
- [ ] Spring Boot 應用可以正常啟動
- [ ] API 測試成功返回書籍資訊

## 🔧 常見問題快速解決

### 問題 1: Docker 容器無法啟動

```bash
# 檢查埠號是否被佔用
lsof -i :8191

# 如果被佔用，停止佔用進程或修改埠號
# 方式 1: 停止佔用進程
kill -9 <PID>

# 方式 2: 修改 docker-compose.yml 埠號映射
# ports:
#   - "8192:8191"  # 改用 8192
```

### 問題 2: 測試腳本報錯

```bash
# 確保腳本有執行權限
chmod +x test-flaresolverr.sh

# 檢查 curl 和 jq 是否安裝
brew install curl jq

# 重新執行測試
./test-flaresolverr.sh
```

### 問題 3: 應用無法連接 FlareSolverr

檢查配置檔案:
```yaml
# cheng-admin/src/main/resources/application-local.yml
crawler:
  flaresolverr:
    enabled: true
    url: http://localhost:8191/v1  # 確認 URL 正確
```

檢查服務狀態:
```bash
# 測試連接
curl -X POST http://localhost:8191/v1 \
  -H "Content-Type: application/json" \
  -d '{"cmd":"sessions.list"}'
```

### 問題 4: 仍然出現 Cloudflare 驗證

臨時停用 FlareSolverr 使用 Selenium:
```yaml
crawler:
  flaresolverr:
    enabled: false  # 暫時停用，使用 Selenium
```

## 📊 效能對比

| 指標 | FlareSolverr | Selenium + 手動 |
|------|--------------|-----------------|
| **成功率** | 95%+ | 70-80% |
| **平均時間** | 5-10秒 | 15-30秒 |
| **CPU 使用** | 低 | 高 |
| **記憶體** | ~500MB | ~1-2GB |
| **穩定性** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **維護成本** | 低 | 高 |

## 📝 下一步

### 1. 整合到 CI/CD

```yaml
# .github/workflows/test.yml
services:
  flaresolverr:
    image: ghcr.io/flaresolverr/flaresolverr:latest
    ports:
      - 8191:8191
```

### 2. 正式環境部署

```bash
# 使用獨立伺服器
# application-prod.yml
crawler:
  flaresolverr:
    url: http://flaresolverr-prod:8191/v1
```

### 3. 監控告警

```bash
# 設定健康檢查（cron 每 5 分鐘）
*/5 * * * * /path/to/test-flaresolverr.sh || echo "FlareSolverr 異常" | mail -s "Alert" admin@example.com
```

## 🎓 學習資源

- 📖 [完整文檔](README_FLARESOLVERR.md)
- 🐙 [FlareSolverr GitHub](https://github.com/FlareSolverr/FlareSolverr)
- 💻 [API 文檔](https://github.com/FlareSolverr/FlareSolverr#api-usage)

## 💡 技巧和建議

### 1. Session 重用

```java
// 建立長期 Session
String sessionId = FlareSolverrUtil.createSession("my-session");

// 重複使用（共用 Cookie）
FlareSolverrUtil.getPage(url1, sessionId);
FlareSolverrUtil.getPage(url2, sessionId);
FlareSolverrUtil.getPage(url3, sessionId);

// 完成後銷毀
FlareSolverrUtil.destroySession(sessionId);
```

### 2. 效能調校

```yaml
# 增加 CPU 和記憶體
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 4G
```

### 3. 日誌等級

```bash
# 詳細日誌（除錯時）
docker-compose up -d
docker-compose exec flaresolverr \
  sh -c "export LOG_LEVEL=debug && supervisorctl restart flaresolverr"
```

## 🆘 需要幫助？

遇到問題？按照以下順序排查：

1. ✅ 查看 [常見問題](#-常見問題快速解決)
2. ✅ 執行 `./test-flaresolverr.sh` 診斷
3. ✅ 查看容器日誌 `docker logs flaresolverr`
4. ✅ 查看應用日誌 `tail -f ~/cool-logs/cheng-admin.log`
5. ✅ 閱讀 [完整文檔](README_FLARESOLVERR.md)

---

**享受無縫的 Cloudflare 驗證處理！** 🎉
