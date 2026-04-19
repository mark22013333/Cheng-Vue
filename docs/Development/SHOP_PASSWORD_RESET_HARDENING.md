# 商城忘記密碼流程強化（2026-03）

## 變更摘要

- 新增 `POST /shop/auth/validate-reset-token`（安全版，使用 request body）。
- 保留 `GET /shop/auth/validate-reset-token` 作為相容入口，已標記 Deprecated。
- 新增 `GET /shop/auth/password-reset-policy`，回傳：
  - `expireMinutes`
  - `minPasswordLength`
  - `resendCooldownSeconds`
- 重設連結改為 hash fragment：`/reset-password#selector=...&token=...`。
- 重設密碼改為原子消耗 token，避免競態重放。

## API 相容策略

### 現行建議（前端使用）

- `POST /shop/auth/validate-reset-token`
  - Request body:
    ```json
    {
      "selector": "string",
      "token": "string"
    }
    ```

### 相容保留（即將移除）

- `GET /shop/auth/validate-reset-token?selector=...&token=...`
  - 僅供舊版連結過渡使用。
  - 計畫於下一主要版本移除。

## 安全注意事項

- 不得在 log 輸出完整 reset URL 或 token。
- 只允許記錄脫敏資訊（email mask、selector prefix）。
- 前端載入 token 後需立即清除網址中的敏感參數。

## 排程維護

- 新增 Quartz 任務：`shopSecurityTask.cleanupExpiredTokens`
- 預設排程：每日 `03:30`（`0 30 3 * * ?`）
- 任務內容：清理已過期的：
  - `shop_password_reset`
  - `shop_email_verification`
