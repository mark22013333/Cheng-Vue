# 本機 ngrok + Docker Nginx 路由紀錄（ECPay）

## 背景
- ngrok 對外端口：`80`
- 使用 Docker Nginx 轉導
- 目標：讓 ECPay 回跳 URL（含 `/prod-api`）能正確進入後端 `8080`

## 目前設定摘要
- `backend_main` → `host.docker.internal:8080`
- `backend_auth` → `host.docker.internal:8060`
- `backend_msg` → `host.docker.internal:8090`
- `backend_legacy` → `host.docker.internal:1024`
- `location /` → `backend_legacy`
- `location ~* ^/(LineBC|webhook/line|wise|SmartBC|Fubon)/` → `backend_main`

## 問題
ECPay 回跳會打到：
```
https://<ngrok-domain>/prod-api/shop/payment/ecpay/return
```
但目前 Nginx 未定義 `/prod-api`，所以會落到 `location /` → `backend_legacy`（1024），導致 404。

## 修正建議（必做）
在 `location /` 之前新增：
```nginx
location /prod-api/ {
    proxy_pass http://backend_main/;
    proxy_set_header X-Forwarded-Proto https;
    proxy_set_header Host $proxy_host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

> 注意：`proxy_pass http://backend_main/;` 末尾的 `/` 會把 `/prod-api` 前綴去掉，
> 使後端收到 `/shop/payment/ecpay/return`（符合 Spring context-path = `/`）。

## sys_config（對應 ngrok）
```
shop.payment.base_url = https://<ngrok-domain>/prod-api
shop.payment.browser_base_url = https://<ngrok-domain>/prod-api
```

## 驗證方式
- 直接測：
  - `https://<ngrok-domain>/prod-api/shop/payment/ecpay/return`
- 或本機測：
  - `http://127.0.0.1:8080/shop/payment/ecpay/return`

若以上可通，ECPay 回跳即不會 404。
