package com.cheng.shop.logistics;

import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.enums.ShippingMethod;
import com.cheng.shop.payment.EcpaySignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 綠界物流閘道
 * <p>
 * 負責：
 * <ul>
 *   <li>產生電子地圖 URL（含 CheckMacValue）</li>
 *   <li>驗證電子地圖回調</li>
 *   <li>建立物流訂單（Express/Create）</li>
 * </ul>
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcpayLogisticsGateway {

    private final ShopConfigService shopConfig;

    // --- 測試環境 ---
    private static final String TEST_MAP_URL = "https://logistics-stage.ecpay.com.tw/Express/map";
    private static final String TEST_CREATE_URL = "https://logistics-stage.ecpay.com.tw/Express/Create";

    // --- 正式環境 ---
    private static final String PROD_MAP_URL = "https://logistics.ecpay.com.tw/Express/map";
    private static final String PROD_CREATE_URL = "https://logistics.ecpay.com.tw/Express/Create";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 產生電子地圖自動提交表單
     *
     * @param storeKey         前端識別 key
     * @param logisticsSubType FAMI / UNIMART / HILIFE
     * @param serverReplyUrl   回調 URL（公開可存取）
     * @return 自動提交 HTML form
     */
    public String generateMapFormHtml(String storeKey, String logisticsSubType, String serverReplyUrl) {
        String merchantId = shopConfig.getEcpayLogisticsMerchantId();
        String hashKey = shopConfig.getEcpayLogisticsHashKey();
        String hashIv = shopConfig.getEcpayLogisticsHashIv();
        String mapUrl = shopConfig.isEcpayLogisticsProdMode() ? PROD_MAP_URL : TEST_MAP_URL;

        // MerchantTradeNo 限制 20 碼，生成格式：L + yyMMddHHmmss(12) + 6碼隨機 = 19碼
        String merchantTradeNo = generateLogisticsTradeNo();

        TreeMap<String, String> params = new TreeMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("LogisticsType", "CVS");
        params.put("LogisticsSubType", logisticsSubType);
        params.put("IsCollection", "N");  // N=不代收貨款
        params.put("ServerReplyURL", serverReplyUrl);
        params.put("ExtraData", storeKey);  // 用 ExtraData 傳遞 storeKey
        params.put("Device", "0");  // 0=PC, 1=Mobile

        // 物流 API 使用 MD5（encryptType = "0"）
        String checkMac = EcpaySignatureUtils.generateCheckMacValue(params, hashKey, hashIv, "0");
        params.put("CheckMacValue", checkMac);

        log.info("產生電子地圖表單：logisticsSubType={}, merchantTradeNo={}, serverReplyUrl={}",
                logisticsSubType, merchantTradeNo, serverReplyUrl);

        return EcpaySignatureUtils.buildAutoSubmitForm(mapUrl, params);
    }

    /**
     * 建立物流訂單
     *
     * @param order 訂單（需包含超商門市資訊或宅配地址）
     * @return 物流結果
     */
    public LogisticsResult createShipment(ShopOrder order) {
        String merchantId = shopConfig.getEcpayLogisticsMerchantId();
        String hashKey = shopConfig.getEcpayLogisticsHashKey();
        String hashIv = shopConfig.getEcpayLogisticsHashIv();
        String createUrl = shopConfig.isEcpayLogisticsProdMode() ? PROD_CREATE_URL : TEST_CREATE_URL;

        ShippingMethod shippingMethod = order.getShippingMethodEnum();
        if (shippingMethod == null) {
            return LogisticsResult.fail("物流方式未設定");
        }

        boolean isCvs = shippingMethod.isCvs();

        // 物流狀態回調 URL（ECPay API 必填）
        String serverReplyUrl = shopConfig.getEcpayLogisticsServerReplyUrl();

        TreeMap<String, String> params = new TreeMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", generateLogisticsTradeNo());
        params.put("MerchantTradeDate", formatTradeDate());
        params.put("LogisticsType", shippingMethod.getEcpayLogisticsType());
        params.put("LogisticsSubType", shippingMethod.getEcpayLogisticsSubType());
        params.put("GoodsAmount", String.valueOf(order.getTotalAmount().intValue()));
        params.put("GoodsName", truncate(buildGoodsName(order), 50));
        params.put("SenderName", shopConfig.getSenderName());
        params.put("SenderPhone", shopConfig.getSenderPhone());
        params.put("SenderCellPhone", shopConfig.getSenderPhone());
        params.put("ReceiverName", truncate(order.getReceiverName(), 10));
        params.put("ReceiverCellPhone", order.getReceiverMobile());
        // 物流狀態回調 URL（ECPay API 必填）
        params.put("ServerReplyURL", serverReplyUrl);

        if (isCvs) {
            // 超商取貨
            if (order.getCvsStoreId() == null || order.getCvsStoreId().isBlank()) {
                return LogisticsResult.fail("超商門市代號未設定");
            }
            params.put("ReceiverStoreID", order.getCvsStoreId());
            params.put("IsCollection", "N");
        } else {
            // 宅配
            params.put("SenderZipCode", shopConfig.getSenderZip());
            params.put("SenderAddress", shopConfig.getSenderAddress());
            params.put("ReceiverZipCode", order.getReceiverZip() != null ? order.getReceiverZip() : "");
            params.put("ReceiverAddress", order.getReceiverAddress());
            params.put("Temperature", "0001");  // 常溫
            params.put("Distance", "00");       // 同縣市
            params.put("Specification", "0001"); // 60cm
        }

        // 物流 API 使用 MD5（encryptType = "0"）
        String checkMac = EcpaySignatureUtils.generateCheckMacValue(params, hashKey, hashIv, "0");
        params.put("CheckMacValue", checkMac);

        log.info("建立物流訂單：orderNo={}, shippingMethod={}, params={}",
                order.getOrderNo(), shippingMethod, params);

        // 呼叫綠界 API
        try {
            String response = httpPost(createUrl, params);
            return parseLogisticsResponse(response);
        } catch (Exception e) {
            log.error("建立物流訂單失敗：orderNo={}", order.getOrderNo(), e);
            return LogisticsResult.fail("物流 API 呼叫失敗：" + e.getMessage());
        }
    }

    /**
     * 驗證電子地圖回調參數
     */
    public boolean verifyMapCallback(Map<String, String> params) {
        String merchantId = shopConfig.getEcpayLogisticsMerchantId();
        String receivedMerchantId = params.get("MerchantID");

        if (!merchantId.equals(receivedMerchantId)) {
            log.warn("電子地圖回調 MerchantID 不匹配：expected={}, received={}",
                    merchantId, receivedMerchantId);
            return false;
        }

        return true;
    }

    // ==================== 內部方法 ====================

    /**
     * 產生物流交易編號（限制 20 碼）
     * 格式：L + yyMMddHHmmss(12) + 6碼隨機 = 19碼
     */
    private String generateLogisticsTradeNo() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = IdUtils.fastSimpleUUID().substring(0, 6).toUpperCase();
        return "L" + dateTime + random;
    }

    /**
     * 格式化交易日期
     */
    private String formatTradeDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    /**
     * 組裝商品名稱
     * ECPay 限制：最多 50 位元組（中文 25 字、英文 50 字）
     * 使用「訂單編號 + 商品數量」格式，簡潔且有辨識度
     */
    private String buildGoodsName(ShopOrder order) {
        int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        if (itemCount == 0) {
            return "訂單" + order.getOrderNo();
        }
        // 格式：訂單O2602151525 共2件
        return "訂單" + order.getOrderNo() + " 共" + itemCount + "件";
    }

    /**
     * 截斷字串（按位元組數，中文算2位元組）
     */
    private String truncate(String str, int maxBytes) {
        if (str == null) {
            return "";
        }
        int byteCount = 0;
        StringBuilder result = new StringBuilder();
        for (char c : str.toCharArray()) {
            int charBytes = (c > 127) ? 2 : 1;
            if (byteCount + charBytes > maxBytes) {
                break;
            }
            result.append(c);
            byteCount += charBytes;
        }
        return result.toString();
    }

    /**
     * 解析建立物流回應
     * 成功回應格式：1|AllPayLogisticsID=xxx&CVSPaymentNo=xxx&...
     * 失敗回應格式：0|錯誤訊息 或 ErrorCode|ErrorMessage
     */
    private LogisticsResult parseLogisticsResponse(String response) {
        log.info("綠界物流回應：{}", response);

        if (response == null || response.isBlank()) {
            return LogisticsResult.fail("物流 API 回應為空");
        }

        if (!response.startsWith("1|")) {
            // 解析錯誤回應
            String errorMsg = response;
            if (response.contains("|")) {
                errorMsg = response.substring(response.indexOf("|") + 1);
            }
            return LogisticsResult.fail(errorMsg, response);
        }

        // 成功回應：解析 URL 參數格式
        // 格式：1|AllPayLogisticsID=xxx&BookingNote=&CVSPaymentNo=xxx&...
        String paramString = response.substring(2);  // 移除 "1|"
        Map<String, String> params = parseUrlParams(paramString);

        String logisticsId = params.get("AllPayLogisticsID");
        String cvsPaymentNo = params.get("CVSPaymentNo");
        String cvsValidationNo = params.get("CVSValidationNo");
        String rtnCode = params.get("RtnCode");
        String rtnMsg = params.get("RtnMsg");

        log.info("解析物流回應：AllPayLogisticsID={}, CVSPaymentNo={}, RtnCode={}, RtnMsg={}",
                logisticsId, cvsPaymentNo, rtnCode, rtnMsg);

        return LogisticsResult.builder()
                .success(true)
                .logisticsId(logisticsId)
                .cvsPaymentNo(cvsPaymentNo)
                .cvsValidationNo(cvsValidationNo)
                .rawResponse(response)
                .build();
    }

    /**
     * 解析 URL 參數字串
     */
    private Map<String, String> parseUrlParams(String paramString) {
        Map<String, String> params = new TreeMap<>();
        if (paramString == null || paramString.isBlank()) {
            return params;
        }
        String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * HTTP POST 請求
     */
    private String httpPost(String url, Map<String, String> params) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();

        int maxRetry = 3;
        int retryDelay = 1000;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.body() != null) {
                    return response.body().string();
                }
                return null;
            } catch (IOException e) {
                log.warn("呼叫綠界物流 API 失敗，第 {} 次，URL={}", attempt, url, e);
                if (attempt < maxRetry) {
                    try {
                        Thread.sleep((long) retryDelay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重試被中斷", ie);
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("物流 API 呼叫失敗，已重試 " + maxRetry + " 次");
    }

}
