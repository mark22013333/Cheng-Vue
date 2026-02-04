package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopPaymentCallbackLog;
import com.cheng.shop.enums.PaymentMethod;
import com.cheng.shop.payment.CallbackResult;
import com.cheng.shop.payment.PaymentGateway;
import com.cheng.shop.payment.PaymentGatewayRouter;
import com.cheng.shop.payment.PaymentRequest;
import com.cheng.shop.payment.PaymentResponse;
import com.cheng.shop.service.IShopOrderService;
import com.cheng.shop.service.IShopPaymentCallbackLogService;
import com.cheng.system.service.ISysConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 金流 API 控制器
 * <p>
 * 使用 {@link PaymentGatewayRouter} 路由到正確的金流閘道。
 * Controller 只負責參數提取、驗簽、調用 Service，業務邏輯在 Service 層處理。
 *
 * @author cheng
 */
@Slf4j
@PublicApi
@RestController
@RequestMapping("/shop/payment")
@RequiredArgsConstructor
public class ShopPaymentController extends BaseController {

    private final PaymentGatewayRouter gatewayRouter;
    private final IShopOrderService orderService;
    private final ISysConfigService configService;
    private final IShopPaymentCallbackLogService callbackLogService;

    private static final String CALLBACK_TYPE_SERVER = "SERVER";
    private static final String CALLBACK_TYPE_BROWSER = "BROWSER";

    /**
     * 建立 ECPay 付款
     *
     * @param orderNo 訂單編號
     * @return 包含 formHtml 的結果（前端直接提交 form 到 ECPay）
     */
    @PostMapping("/ecpay/create")
    public AjaxResult createEcpayPayment(@RequestParam String orderNo, HttpServletRequest request) {
        Long memberId = SecurityUtils.getLoginUser().getUser().getUserId();

        // 查詢訂單
        ShopOrder order = orderService.selectOrderByOrderNo(orderNo);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceException("無權操作此訂單");
        }

        // 載入完整訂單（含 orderItems，ECPay 需要商品名稱）
        ShopOrder fullOrder = orderService.selectOrderById(order.getOrderId());

        // 組裝回調 URL
        // notifyUrl（ReturnURL）：ECPay 伺服器對伺服器回調，必須用公開 URL（ngrok）
        String baseUrl = getBaseUrl(request);
        String notifyUrl = baseUrl + "/shop/payment/ecpay/callback";

        log.info("ECPay 建立付款 URL 配置：baseUrl=[{}], notifyUrl=[{}]", baseUrl, notifyUrl);

        // returnUrl（OrderResultURL）：付款完成後從用戶瀏覽器 form POST 跳轉
        // 用 shop.payment.browser_base_url 設定，本地開發時指向前端 proxy
        // 例如 http://localhost:1024/dev-api，經 Vite proxy 轉到後端
        String browserBase = getBrowserBaseUrl(baseUrl);
        String returnUrl = browserBase + "/shop/payment/ecpay/return?orderNo=" + orderNo;

        // 透過 Router 取得 ECPay 閘道
        PaymentGateway gateway = gatewayRouter.getGateway("ECPAY");
        PaymentResponse response = gateway.createPayment(PaymentRequest.builder()
                .order(fullOrder)
                .returnUrl(returnUrl)
                .notifyUrl(notifyUrl)
                .build());

        return success(response);
    }

    /**
     * ECPay 伺服器回調（Server to Server）
     * <p>
     * ECPay 付款完成後會 POST 到此端點。
     * Controller 只負責：參數提取 → 驗簽 → 調用 Service → 回傳響應。
     * 業務邏輯（訂單更新、銷量更新）在 Service 層處理。
     */
    @Anonymous
    @PostMapping("/ecpay/callback")
    @ResponseBody
    public String ecpayCallback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        log.info("收到 ECPay 回調：{}", params);

        try {
            PaymentGateway gateway = gatewayRouter.getGateway("ECPAY");

            boolean verifyOk = false;
            String verifyMessage = null;
            if (params.containsKey("CheckMacValue")) {
                verifyOk = gateway.verifyCallback(params);
                if (!verifyOk) {
                    verifyMessage = "CheckMacValue verify fail";
                }
            } else {
                verifyMessage = "Missing CheckMacValue";
            }

            recordCallbackLog(PaymentMethod.ECPAY.getCode(), CALLBACK_TYPE_SERVER, params, verifyOk, verifyMessage, null);

            // 1. 驗簽
            if (!verifyOk) {
                log.error("ECPay 回調驗證失敗，參數：{}", params);
                return "0|ErrorMessage=CheckMacValue Error";
            }

            // 2. 解析回調結果
            CallbackResult result = gateway.parseCallback(params);

            // 3. 委派給 Service 處理（含行鎖、冪等性檢查、訂單更新、事件發布）
            return orderService.handlePaymentCallback(result);

        } catch (Exception e) {
            log.error("ECPay 回調處理異常：{}", e.getMessage(), e);
            return "0|ErrorMessage=" + e.getMessage();
        }
    }

    /**
     * ECPay 用戶返回（Fallback）
     * <p>
     * 正常流程下 OrderResultURL 直接指向前端，不經過此端點。
     * 此端點作為 fallback：當 shop.payment.frontend_url 未設定時，
     * OrderResultURL 會指向此處，再用 JS 跳轉至前端。
     * <p>
     * 支援 GET and POST，因為 ECPay 使用 POST，但瀏覽器直接訪問是 GET。
     */
    @Anonymous
    @RequestMapping(value = "/ecpay/return", method = {RequestMethod.GET, RequestMethod.POST})
    public void ecpayReturn(@RequestParam(required = false) String orderNo,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        // 付款完成後，ECPay 會以瀏覽器 POST 回傳完整參數
        // 若伺服器回調（ReturnURL）失敗，這裡作為補救更新訂單狀態
        Map<String, String> params = extractParams(request);
        if (!params.isEmpty()) {
            log.info("ECPay 前端回傳參數：{}", params);
            try {
                PaymentGateway gateway = gatewayRouter.getGateway("ECPAY");
                boolean verifyOk = false;
                String verifyMessage = null;
                if (params.containsKey("CheckMacValue")) {
                    verifyOk = gateway.verifyCallback(params);
                    if (!verifyOk) {
                        verifyMessage = "CheckMacValue verify fail";
                    }
                } else {
                    verifyMessage = "Missing CheckMacValue";
                }

                recordCallbackLog(PaymentMethod.ECPAY.getCode(), CALLBACK_TYPE_BROWSER, params, verifyOk, verifyMessage, orderNo);

                if (verifyOk) {
                    CallbackResult result = gateway.parseCallback(params);
                    if ((result.getOrderNo() == null || result.getOrderNo().isBlank())
                            && orderNo != null && !orderNo.isBlank()) {
                        result = CallbackResult.builder()
                                .paymentSuccess(result.isPaymentSuccess())
                                .orderNo(orderNo)
                                .tradeNo(result.getTradeNo())
                                .responseBody(result.getResponseBody())
                                .rawInfo(result.getRawInfo())
                                .build();
                    }
                    orderService.handlePaymentCallback(result);
                } else {
                    log.warn("ECPay 前端回傳驗證失敗，orderNo={}", orderNo);
                }
            } catch (Exception e) {
                log.error("ECPay 前端回傳處理異常", e);
            }
        }

        String targetUrl = getFrontendUrl() + "/mall/member/orders";

        log.info("ECPay 用戶返回（fallback），orderNo={}，重導向至：{}", orderNo, targetUrl);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<title>付款完成</title>"
                + "<style>body{display:flex;justify-content:center;align-items:center;min-height:100vh;"
                + "font-family:-apple-system,sans-serif;background:#f5f7fa;margin:0;}"
                + ".card{text-align:center;padding:40px;background:#fff;border-radius:12px;"
                + "box-shadow:0 2px 12px rgba(0,0,0,.1);}"
                + "h2{color:#67c23a;margin:0 0 12px;}p{color:#606266;margin:0 0 20px;}"
                + "a{color:#409eff;text-decoration:none;font-weight:500;}</style>"
                + "</head><body><div class='card'>"
                + "<h2>付款完成</h2>"
                + "<p>正在跳轉至訂單頁面...</p>"
                + "<a href='" + targetUrl + "'>若未自動跳轉，請點此前往</a>"
                + "</div>"
                + "<script>window.location.replace('" + targetUrl + "');</script>"
                + "</body></html>"
        );
    }

    /**
     * 從 HttpServletRequest 提取所有參數
     */
    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    private void recordCallbackLog(String paymentMethod, String callbackType, Map<String, String> params,
                                   boolean verifyOk, String verifyMessage, String orderNoFallback) {
        if (params == null || params.isEmpty()) {
            return;
        }
        try {
            ShopPaymentCallbackLog logEntry = new ShopPaymentCallbackLog();
            logEntry.setPaymentMethod(paymentMethod);
            logEntry.setCallbackType(callbackType);
            logEntry.setOrderNo(firstNonBlank(
                    params.get("CustomField2"),
                    params.get("MerchantTradeNo"),
                    orderNoFallback
            ));
            logEntry.setTradeNo(params.get("TradeNo"));
            logEntry.setRtnCode(params.get("RtnCode"));
            logEntry.setVerifyStatus(verifyOk ? 1 : 0);
            logEntry.setVerifyMessage(verifyMessage);
            logEntry.setRawInfo(JacksonUtil.encodeToJson(params));
            callbackLogService.logCallback(logEntry);
        } catch (Exception e) {
            log.warn("金流回調紀錄失敗", e);
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * 取得前端基礎 URL（用於 JS 跳轉目標）
     */
    private String getFrontendUrl() {
        String url = configService.selectConfigByKey("shop.payment.frontend_url");
        if (url != null && !url.isBlank()) {
            url = url.trim();
            return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        }
        return "";
    }

    /**
     * 取得瀏覽器可存取的後端 Base URL（用於 ECPay OrderResultURL）
     * <p>
     * 讀取 sys_config 中的 shop.payment.browser_base_url。
     * <ul>
     *   <li>本地開發：設為 http://localhost:1024/dev-api（經 Vite proxy 到後端）</li>
     *   <li>正式環境：留空，自動使用 shop.payment.base_url（前後端同域名）</li>
     * </ul>
     */
    private String getBrowserBaseUrl(String fallbackBaseUrl) {
        String url = configService.selectConfigByKey("shop.payment.browser_base_url");
        if (url != null && !url.isBlank()) {
            url = url.trim();
            return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        }
        return fallbackBaseUrl;
    }

    /**
     * 取得伺服器基礎 URL
     * <p>
     * 優先讀取 sys_config 中的 shop.payment.base_url 配置，
     * 若未配置則讀取反向代理標頭，最後才用 request 本身的資訊。
     * 本地開發使用 ngrok 時必須設定此配置，否則 ECPay 無法回調 localhost。
     */
    private String getBaseUrl(HttpServletRequest request) {
        // 1. 優先使用 sys_config 設定的 base URL（本地 ngrok 或正式域名）
        String configuredUrl = configService.selectConfigByKey("shop.payment.base_url");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            configuredUrl = configuredUrl.trim(); // 移除前後空格
            return configuredUrl.endsWith("/")
                    ? configuredUrl.substring(0, configuredUrl.length() - 1)
                    : configuredUrl;
        }

        // 2. 嘗試讀取反向代理標頭（Nginx / ngrok 等）
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            String forwardedProto = request.getHeader("X-Forwarded-Proto");
            String scheme = (forwardedProto != null && !forwardedProto.isBlank()) ? forwardedProto : "https";
            return scheme + "://" + forwardedHost.split(",")[0].trim();
        }

        // 3. 最終 fallback：使用 request 本身的資訊
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        return url.toString();
    }
}
