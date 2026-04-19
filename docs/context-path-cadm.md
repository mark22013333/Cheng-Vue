# /cadm 後台 + / 商城 路由設計說明

## 目標
- 商城（前台）API 與頁面走根路徑 `/`
- 後台管理介面與 API 統一加前綴 `/cadm`
- 前台可被爬蟲正常收錄，後台與 API 不對爬蟲開放

## 後端（Spring MVC Path Prefix）
- 使用 `AdminPathConfig` 對所有 Controller 加 `/cadm` 前綴
- 例外：標註 `@PublicApi` 的 Controller 不加前綴，維持 `/`

已新增：
- `cheng-framework/src/main/java/com/cheng/framework/config/AdminPathConfig.java`
- `cheng-common/src/main/java/com/cheng/common/annotation/PublicApi.java`

### 已標註 @PublicApi 的 Controller
- `com.cheng.shop.controller.ShopFrontController`
- `com.cheng.shop.controller.ShopMemberCartController`
- `com.cheng.shop.controller.ShopPaymentController`
- `com.cheng.web.controller.shop.ShopCartController`
- `com.cheng.web.controller.shop.ShopCheckoutController`
- `com.cheng.web.controller.shop.ShopMemberAddressController`
- `com.cheng.web.controller.shop.ShopMemberOrderController`
- `com.cheng.web.controller.line.LineWebhookController`

> 若未來新增「前台/對外」API，請加 `@PublicApi` 以維持 `/`。

## 前端（同一個 SPA 支援 / 與 /cadm）
- Router 會依據當前 URL 判斷 base：
  - `/cadm` → base = `/cadm`
  - 其他 → base = `/`
- API Base URL 亦依 URL 判斷：
  - `/cadm` → `/cadm` + `VITE_APP_BASE_API`
  - 其他 → `VITE_APP_BASE_API`

修改檔案：
- `cheng-ui/src/router/index.js`
- `cheng-ui/src/utils/request.js`

## Nginx 設定重點
### 後台 API
- `/cadm/prod-api/` → 轉到後端 `/cadm/`

### 後台頁面
- `/cadm/` → 指向同一份 SPA `index.html`

已更新範本：
- `cheng.deploy/nginx/proxy-ssl.conf`
- `cheng.deploy/nginx/proxy-ssl-corrected.conf`

## 爬蟲與防護
- `cheng-ui/public/robots.txt` 已改為允許 `/mall/`，禁止 `/cadm/` 與 `/prod-api/`
- 若需防 DDoS，建議在 Nginx 針對 `/mall` 或 `/prod-api/shop/front` 加 rate limit

## 驗證建議
- 前台：`/mall` 正常、API `/prod-api/shop/front/**` 正常
- 後台：`/cadm/index` 正常、API `/cadm/prod-api/system/**` 正常
- Line Webhook：`/webhook/line/{botBasicId}` 仍可用
