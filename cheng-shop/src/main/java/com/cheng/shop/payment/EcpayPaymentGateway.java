package com.cheng.shop.payment;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;
import java.util.Map;

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

    private final ShopConfigService shopConfig;

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

        String merchantId = shopConfig.getEcpayMerchantId();
        String hashKey = shopConfig.getEcpayHashKey();
        String hashIv = shopConfig.getEcpayHashIv();
        String serviceUrl = shopConfig.isEcpayProdMode() ? PROD_SERVICE_URL : TEST_SERVICE_URL;

        log.info("ECPay 建立付款：orderNo={}, mode={}, merchantId={}, serviceUrl={}",
                order.getOrderNo(), shopConfig.getEcpayMode(), merchantId, serviceUrl);

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
        String checkMacValue = EcpaySignatureUtils.generateCheckMacValue(params, hashKey, hashIv, params.get("EncryptType"));
        params.put("CheckMacValue", checkMacValue);

        log.info("ECPay 參數：MerchantTradeNo={}, TotalAmount={}, ReturnURL={}, CheckMacValue={}",
                params.get("MerchantTradeNo"), params.get("TotalAmount"),
                params.get("ReturnURL"), checkMacValue);

        // 產生自動提交 HTML form
        String formHtml = EcpaySignatureUtils.buildAutoSubmitForm(serviceUrl, params);

        return PaymentResponse.builder()
                .formHtml(formHtml)
                .build();
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String hashKey = shopConfig.getEcpayHashKey();
        String hashIv = shopConfig.getEcpayHashIv();
        return EcpaySignatureUtils.verifyCheckMacValue(params, hashKey, hashIv);
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

}
