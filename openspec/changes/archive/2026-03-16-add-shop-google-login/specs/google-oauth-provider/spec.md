## ADDED Requirements

### Requirement: Google OAuth 授權 URL 產生
系統 SHALL 透過 `GoogleOAuthProvider.buildAuthorizeUrl()` 產生有效的 Google OAuth 2.0 授權 URL，包含 `client_id`、`redirect_uri`、`state`、`scope=openid profile email`、`response_type=code` 參數。

#### Scenario: 成功產生授權 URL
- **WHEN** 前端呼叫 `GET /shop/auth/oauth/authorize-url?provider=GOOGLE` 並提供 redirectUri
- **THEN** 系統回傳以 `https://accounts.google.com/o/oauth2/v2/auth` 開頭的授權 URL，包含所有必要參數

#### Scenario: Google 未啟用
- **WHEN** 系統參數 `shop.oauth.google.enabled` 為 `false` 或 Client ID 未配置
- **THEN** 系統回傳錯誤訊息，告知 Google 登入未啟用

### Requirement: Google Token 交換
系統 SHALL 使用授權碼向 `https://oauth2.googleapis.com/token` 交換 access token，傳送 `grant_type=authorization_code`、`code`、`redirect_uri`、`client_id`、`client_secret`。

#### Scenario: 成功交換 Token
- **WHEN** 使用者授權後，系統以有效的 authorization code 呼叫 Google token 端點
- **THEN** 系統取得 `access_token`、`refresh_token`（如有）、`expires_in`，並封裝為 `OAuthTokenResponse`

#### Scenario: 授權碼無效或過期
- **WHEN** 系統以無效或過期的 authorization code 呼叫 Google token 端點
- **THEN** 系統拋出例外，包含 Google 回傳的錯誤訊息

### Requirement: Google 用戶資料取得
系統 SHALL 使用 access token 向 `https://www.googleapis.com/oauth2/v3/userinfo` 取得用戶資料，並映射為 `OAuthUserProfile`。

#### Scenario: 成功取得用戶資料
- **WHEN** 系統以有效的 access token 呼叫 Google userinfo 端點
- **THEN** 系統取得 `sub`（作為 providerId）、`name`（作為 nickname）、`picture`（作為 avatarUrl）、`email`，封裝為 `OAuthUserProfile`

#### Scenario: Access Token 無效
- **WHEN** 系統以無效的 access token 呼叫 Google userinfo 端點
- **THEN** 系統拋出例外，包含適當錯誤訊息

### Requirement: Google 配置管理
系統 SHALL 從系統參數表讀取 Google OAuth 配置（`shop.oauth.google.enabled`、`shop.oauth.google.client_id`、`shop.oauth.google.client_secret`），透過 `ShopConfigKey` 存取。

#### Scenario: 配置完整且已啟用
- **WHEN** 系統參數中 Google OAuth 已啟用且 Client ID/Secret 已配置
- **THEN** `GoogleOAuthProvider` 能正常產生授權 URL 並完成登入流程

#### Scenario: 配置缺失
- **WHEN** 系統參數中 Google Client ID 或 Secret 未配置
- **THEN** 呼叫 `buildAuthorizeUrl()` 時拋出配置錯誤

### Requirement: 前端 Google 登入按鈕
前端登入頁 SHALL 顯示 Google 登入按鈕，點擊後觸發 OAuth 授權流程，複用現有 `oauth-callback.vue` 處理回調。

#### Scenario: 使用者點擊 Google 登入
- **WHEN** 使用者在商城登入頁點擊 Google 登入按鈕
- **THEN** 前端暫存 `oauth_provider: 'GOOGLE'` 到 sessionStorage，呼叫後端取得授權 URL，跳轉至 Google 授權頁面

#### Scenario: Google 授權回調
- **WHEN** 使用者在 Google 授權頁面同意授權，瀏覽器重導向至 `/oauth/callback`
- **THEN** 回調頁從 sessionStorage 讀取 provider，呼叫後端交換 JWT，登入成功後跳轉回原始頁面

### Requirement: Flyway 遷移腳本
系統 SHALL 提供 Flyway 遷移腳本，在系統參數表中插入 Google OAuth 預設配置。

#### Scenario: 遷移執行
- **WHEN** 應用程式啟動時執行 Flyway 遷移
- **THEN** `sys_config` 表中新增 `shop.oauth.google.enabled`（預設 `false`）、`shop.oauth.google.client_id`、`shop.oauth.google.client_secret` 三筆配置
