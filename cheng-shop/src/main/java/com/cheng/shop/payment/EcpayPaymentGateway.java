package com.cheng.shop.payment;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopOrderItem;
import com.cheng.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cheng.common.utils.uuid.IdUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * ECPay 綠界金流閘道實作
 * <p>
 * 重點：
 * <ul>
 *   <li>TradeDesc 不做預先 URL 編碼，避免 CheckMacValue 雙重編碼導致驗簽失敗</li>
 *   <li>HTML form value 使用 HTML entity 跳脫，防止 XSS</li>
 * </ul>
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcpayPaymentGateway implements PaymentGateway {

    private final ISysConfigService configService;

    private static final String TEST_SERVICE_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";
    private static final String PROD_SERVICE_URL = "https://payment.ecpay.com.tw/Cashier/AioCheckOut/V5";

    /**
     * Implements ECPay payment creation by building an auto submit form
     * <a href="https://developers.ecpay.com.tw/2862/">ECPay API Docs</a>
     */
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        ShopOrder order = request.getOrder();
        if (order == null || order.getOrderNo() == null) {
            throw new ServiceException("訂單資訊無效");
        }

        String merchantId = getConfig("shop.ecpay.merchant_id");
        String hashKey = getConfig("shop.ecpay.hash_key");
        String hashIv = getConfig("shop.ecpay.hash_iv");
        String mode = getConfig("shop.ecpay.mode");
        String serviceUrl = "prod".equals(mode) ? PROD_SERVICE_URL : TEST_SERVICE_URL;

        log.info("ECPay 建立付款：orderNo={}, mode={}, merchantId={}, serviceUrl={}",
                order.getOrderNo(), mode, merchantId, serviceUrl);

        // 組裝 ECPay 參數（TreeMap 自動按 key 排序）
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MerchantID", merchantId);
        // 每次付款產生唯一 MerchantTradeNo（ECPay 不允許重複），用 CustomField2 回傳 orderNo
        String merchantTradeNo = generateMerchantTradeNo();
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(order.getCreateTime()));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(order.getTotalAmount().intValue()));
        params.put("TradeDesc", "CoolApps Order");
        params.put("ItemName", buildItemName(order));
        params.put("ReturnURL", request.getNotifyUrl());
        params.put("OrderResultURL", request.getReturnUrl());
        params.put("ChoosePayment", "ALL");
        params.put("IgnorePayment", "WeiXin");
        params.put("EncryptType", "1");
        params.put("CustomField1", String.valueOf(order.getMemberId()));
        params.put("CustomField2", order.getOrderNo());

        // 計算 CheckMacValue
        String checkMacValue = generateCheckMacValue(params, hashKey, hashIv, params.get("EncryptType"));
        params.put("CheckMacValue", checkMacValue);

        log.info("ECPay 參數：MerchantTradeNo={}, TotalAmount={}, ReturnURL={}, CheckMacValue={}",
                params.get("MerchantTradeNo"), params.get("TotalAmount"),
                params.get("ReturnURL"), checkMacValue);

        // 產生自動提交 HTML form
        String formHtml = buildAutoSubmitForm(serviceUrl, params);

        return PaymentResponse.builder()
                .formHtml(formHtml)
                .build();
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String hashKey = getConfig("shop.ecpay.hash_key");
        String hashIv = getConfig("shop.ecpay.hash_iv");

        String receivedCheckMac = params.get("CheckMacValue");
        if (receivedCheckMac == null) {
            return false;
        }

        // 移除 CheckMacValue 與 EncryptType 後重新計算（依 ECPay SDK 規則）
        TreeMap<String, String> paramsWithoutCheck = new TreeMap<>(params);
        paramsWithoutCheck.remove("CheckMacValue");
        String encryptType = paramsWithoutCheck.remove("EncryptType");
        if (encryptType == null || encryptType.isBlank()) {
            encryptType = "1";
        }

        String calculated = generateCheckMacValue(paramsWithoutCheck, hashKey, hashIv, encryptType);
        return calculated.equalsIgnoreCase(receivedCheckMac);
    }

    @Override
    public CallbackResult parseCallback(Map<String, String> params) {
        String rtnCode = params.get("RtnCode");
        String tradeNo = params.get("TradeNo");

        // orderNo 存在 CustomField2（MerchantTradeNo 為每次付款的獨立編號）
        String orderNo = params.get("CustomField2");
        if (orderNo == null || orderNo.isBlank()) {
            // 相容舊訂單：CustomField2 為空時 fallback 到 MerchantTradeNo
            orderNo = params.get("MerchantTradeNo");
        }

        boolean success = "1".equals(rtnCode);

        return CallbackResult.builder()
                .paymentSuccess(success)
                .orderNo(orderNo)
                .tradeNo(tradeNo)
                .responseBody("1|OK")
                .rawInfo(mapToJson(params))
                .build();
    }

    @Override
    public boolean supports(String paymentMethod) {
        return "ECPAY".equals(paymentMethod);
    }

    // ==================== 內部方法 ====================

    /**
     * 產生每次付款的唯一 MerchantTradeNo（ECPay 上限 20 碼，不允許重複）
     * <p>
     * 格式：P + yyMMddHHmmss(12) + 7碼隨機英數字 = 20碼
     */
    private String generateMerchantTradeNo() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = IdUtils.fastSimpleUUID().substring(0, 7).toUpperCase();
        return "P" + dateTime + random;
    }

    /**
     * 產生 CheckMacValue（SHA256）
     * <p>
     * 演算法：
     * <ol>
     *   <li>按 key 字母排序（不分大小寫）</li>
     *   <li>組合: "HashKey={key}&amp;k1=v1&amp;k2=v2&amp;...&amp;HashIV={iv}"</li>
     *   <li>URL encode → 轉小寫</li>
     *   <li>特殊字元替換（ECPay 規範）</li>
     *   <li>SHA256 雜湊 → 轉大寫</li>
     * </ol>
     */
    String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv, String encryptType) {
        // 1. 按 key 排序（不分大小寫）
        TreeMap<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(params);

        // 2. 組合字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey).append("&");
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("HashIV=").append(hashIv);

        // 3. URL encode → 轉小寫
        String encoded = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8).toLowerCase();

        // 4. 特殊字元替換（ECPay .NET URLEncode 規範）
        encoded = encoded.replace("%2d", "-")
                .replace("%5f", "_")
                .replace("%2e", ".")
                .replace("%21", "!")
                .replace("%2a", "*")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%20", "+");

        // 5. 依 EncryptType 進行雜湊 → 轉大寫
        if ("0".equals(encryptType)) {
            return md5(encoded).toUpperCase();
        }
        return sha256(encoded).toUpperCase();
    }

    /**
     * 組裝商品名稱（ECPay 限制 200 字元，多商品用 # 分隔）
     */
    private String buildItemName(ShopOrder order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return "CoolApps商品";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.getOrderItems().size(); i++) {
            if (i > 0) {
                sb.append("#");
            }
            ShopOrderItem item = order.getOrderItems().get(i);
            sb.append(item.getProductName());
            if (item.getQuantity() > 1) {
                sb.append(" x").append(item.getQuantity());
            }
        }

        String result = sb.toString();
        if (result.length() > 200) {
            result = result.substring(0, 197) + "...";
        }
        return result;
    }

    /**
     * 產生自動提交的 HTML form（值經 HTML entity 跳脫）
     */
    private String buildAutoSubmitForm(String actionUrl, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head>");
        sb.append("<body onload=\"document.forms[0].submit()\">");
        sb.append("<form method=\"post\" action=\"").append(htmlEscape(actionUrl))
                .append("\" accept-charset=\"UTF-8\">");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("<input type=\"hidden\" name=\"").append(htmlEscape(entry.getKey()))
                    .append("\" value=\"").append(htmlEscape(entry.getValue())).append("\">");
        }
        sb.append("</form></body></html>");
        return sb.toString();
    }

    /**
     * HTML entity 跳脫
     */
    private String htmlEscape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * SHA256 雜湊
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new ServiceException("SHA256 計算失敗");
        }
    }

    /**
     * MD5 雜湊
     */
    private String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new ServiceException("MD5 計算失敗");
        }
    }

    /**
     * Map 轉 JSON 字串
     */
    private String mapToJson(Map<String, String> params) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue().replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 讀取系統設定
     */
    private String getConfig(String key) {
        String value = configService.selectConfigByKey(key);
        if (value == null || value.isBlank()) {
            throw new ServiceException("缺少系統設定：" + key);
        }
        return value;
    }
}
