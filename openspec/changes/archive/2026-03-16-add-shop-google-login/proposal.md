## Why

專案目前僅支援 LINE 第三方登入，但 Google 在台灣的使用率極高，新增 Google OAuth 登入可以降低註冊門檻、提升轉換率。現有架構已採用策略模式（`OAuthProviderStrategy`），`SocialProvider` 列舉和 `ShopConfigKey` 也已預留 Google 相關定義，實作成本低。

## What Changes

- 新增 `GoogleOAuthProvider`，實作 `OAuthProviderStrategy` 介面，完成 Google OAuth 2.0 授權碼流程（authorize → token exchange → userinfo）
- 在系統參數或資料庫中新增 Google OAuth Client ID / Secret 配置
- 前端登入頁新增 Google 登入按鈕，複用現有 OAuth 回調頁面與流程
- 新增 Flyway 遷移腳本，插入 Google OAuth 預設啟用配置

## Capabilities

### New Capabilities
- `google-oauth-provider`: Google OAuth 2.0 登入策略實作，包含授權 URL 產生、token 交換、用戶資料取得

### Modified Capabilities

（無既有 spec 需修改，現有 OAuth 框架、回調頁面、綁定/解綁功能皆可直接複用）

## Impact

- **後端**：`cheng-shop` 模組新增 `GoogleOAuthProvider` 類別，無需修改現有控制器或服務層
- **前端**：`cheng-ui` 登入頁新增 Google 按鈕，複用 `oauth-callback.vue`
- **資料庫**：新增 Flyway 遷移插入 Google 配置參數
- **外部依賴**：使用 Google OAuth 2.0 API（accounts.google.com），透過現有 OkHttp 客戶端呼叫，無需額外 SDK
- **安全性**：Google Client Secret 需透過系統參數管理，生產環境應加密存儲
