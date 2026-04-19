## 1. 資料庫與配置

- [x] 1.1 建立 Flyway 遷移腳本，在 `sys_config` 表插入 `shop.oauth.google.enabled`（預設 `false`）、`shop.oauth.google.client_id`、`shop.oauth.google.client_secret` 配置
- [x] 1.2 確認 `ShopConfigKey` 列舉已包含 Google OAuth 相關鍵，若缺少則補齊

## 2. 後端核心實作

- [x] 2.1 建立 `GoogleOAuthProvider` 類別，實作 `OAuthProviderStrategy` 介面
- [x] 2.2 實作 `buildAuthorizeUrl()`：組裝 Google 授權 URL（scope: `openid profile email`）
- [x] 2.3 實作 `exchangeToken()`：使用 OkHttp 向 `https://oauth2.googleapis.com/token` 交換 access token
- [x] 2.4 實作 `getUserProfile()`：使用 OkHttp 向 `https://www.googleapis.com/oauth2/v3/userinfo` 取得用戶資料，映射為 `OAuthUserProfile`
- [x] 2.5 加入配置檢查邏輯：Google 未啟用或 Client ID/Secret 未配置時拋出適當例外

## 3. 前端 UI

- [x] 3.1 在商城登入頁（`login.vue`）新增 Google 登入按鈕，樣式與 LINE 按鈕一致
- [x] 3.2 實作 `handleGoogleLogin()` 函數：暫存 `oauth_provider: 'GOOGLE'` 到 sessionStorage，呼叫後端取得授權 URL 並跳轉

## 4. 驗證與測試

- [x] 4.1 啟動後端，確認 Flyway 遷移成功執行
- [x] 4.2 確認 `OAuthProviderFactory` 能正確路由到 `GoogleOAuthProvider`
- [x] 4.3 前端點擊 Google 登入按鈕，確認跳轉至 Google 授權頁面
- [x] 4.4 完成 Google 授權流程，確認 JWT 正確回傳且會員建立/綁定正常
