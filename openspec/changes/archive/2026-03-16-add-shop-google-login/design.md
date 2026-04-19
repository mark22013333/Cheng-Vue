## Context

專案已有完整的 OAuth 第三方登入框架，採用策略模式（`OAuthProviderStrategy`）+ 工廠模式（`OAuthProviderFactory`）。目前僅實作 LINE 登入（`LineOAuthProvider`），但 `SocialProvider` 列舉已預留 `GOOGLE`，`ShopConfigKey` 已定義 Google 相關配置鍵。

現有架構：
- **控制器**：`ShopOAuthController` — 通用 OAuth 端點，不綁定特定平台
- **編排服務**：`SocialLoginService` — 管理 state 驗證、token 交換、會員建立/綁定
- **策略介面**：`OAuthProviderStrategy` — `buildAuthorizeUrl()`, `exchangeToken()`, `getUserProfile()`
- **前端**：`oauth-callback.vue` — 通用回調頁，透過 `sessionStorage` 辨別平台

新增 Google 登入只需實作一個新的 Strategy Bean，無需修改現有控制器或服務。

## Goals / Non-Goals

**Goals:**
- 實作 `GoogleOAuthProvider`，完成 Google OAuth 2.0 授權碼流程
- 前端登入頁新增 Google 登入按鈕
- 在系統參數中管理 Google Client ID / Secret
- 複用現有 OAuth 回調、綁定/解綁功能

**Non-Goals:**
- 不實作 Google One Tap 登入（僅標準 OAuth 2.0 流程）
- 不實作 Facebook 登入（未來再處理）
- 不修改現有 LINE 登入邏輯
- 不新增後台管理 Google 配置的 UI（使用現有系統參數管理）

## Decisions

### 1. 使用 Google OAuth 2.0 標準授權碼流程

**選擇**：Authorization Code Flow（server-side）

**理由**：與現有 LINE 登入流程一致，token 交換在後端進行，Client Secret 不暴露給前端。

**替代方案**：
- Google Identity Services (GIS) 新版 SDK — 需額外前端 JS SDK，增加複雜度
- Implicit Flow — 不安全，token 暴露在 URL

### 2. 使用 OkHttp 呼叫 Google API（不引入額外 SDK）

**選擇**：直接使用專案已有的 OkHttp 客戶端呼叫 Google OAuth 端點

**理由**：與 `LineOAuthProvider` 保持一致，不增加依賴。Google OAuth API 僅需 3 個 HTTP 呼叫。

**替代方案**：
- `google-api-client` SDK — 引入大量傳遞依賴，過重
- Spring Security OAuth2 Client — 需重構現有架構

### 3. Google 配置存於系統參數表（`sys_config`）

**選擇**：透過 `ShopConfigKey` 讀取 `shop.oauth.google.*` 配置

**理由**：`ShopConfigKey` 已預定義 Google 配置鍵，且有快取機制。LINE 的設定雖移至專屬頻道表，但 Google 無此需求，系統參數即可。

### 4. 前端複用現有 OAuth 回調頁面

**選擇**：登入頁新增 Google 按鈕，`sessionStorage` 存 `oauth_provider: 'GOOGLE'`，複用 `oauth-callback.vue`

**理由**：現有回調頁已是平台無關設計，只需在登入頁新增一個按鈕即可。

### 5. Google API 端點

| 用途 | 端點 |
|------|------|
| 授權 | `https://accounts.google.com/o/oauth2/v2/auth` |
| Token 交換 | `https://oauth2.googleapis.com/token` |
| 用戶資料 | `https://www.googleapis.com/oauth2/v3/userinfo` |

**Scope**：`openid profile email`（比 LINE 多取 email）

## Risks / Trade-offs

- **[風險] Google Cloud Console 配置錯誤** → 需在部署文件中說明：建立 OAuth 2.0 Client ID、設定授權重導向 URI、啟用 Google+ API
- **[風險] 不同環境的 redirect URI 不一致** → 與 LINE 相同做法：前端動態組裝 `${window.location.origin}/oauth/callback`，後端不做 URI 校驗
- **[權衡] 不使用 Google One Tap** → 簡化實作但犧牲部分 UX，未來可作為增強功能
- **[權衡] email 可能為 null** → Google 通常回傳 email，但需處理 null 情況（與現有邏輯一致）
