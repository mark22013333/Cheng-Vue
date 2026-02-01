package com.cheng.shop.payment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * ECPay CheckMacValue 驗證測試
 * <p>
 * 使用官方範例數據驗證計算結果：
 * https://github.com/andy6804tw/ecpay-payment-demo/blob/master/Decode.md
 */
public class EcpayCheckMacValueTest {

    public static void main(String[] args) {
        // === 官方範例測試 ===
        // HashKey=5294y06JbISpM5x9, HashIV=v77hoKGq4kWxNNIS
        String hashKey = "5294y06JbISpM5x9";
        String hashIv = "v77hoKGq4kWxNNIS";

        TreeMap<String, String> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");
        params.put("ItemName", "Apple iphone 7 手機殼");
        params.put("MerchantID", "2000132");
        params.put("MerchantTradeDate", "2018/05/04 11:07:23");
        params.put("MerchantTradeNo", "ecpay20180504110723");
        params.put("PaymentType", "aio");
        params.put("ReturnURL", "https://www.ecpay.com.tw/receive.php");
        params.put("TotalAmount", "1000");
        params.put("TradeDesc", "促銷方案");

        String expected = "B4A5010C622CC8710182465D1A8CFFF29B9212264E679C8468893C4A6EBB716B";

        // 用我們的計算方法
        String result = generateCheckMacValue(params, hashKey, hashIv);

        System.out.println("=== ECPay CheckMacValue 驗證 ===");
        System.out.println("期望值: " + expected);
        System.out.println("計算值: " + result);
        System.out.println("結果:   " + (expected.equals(result) ? "✅ 通過" : "❌ 失敗"));

        // 印出中間步驟以便除錯
        System.out.println("\n=== 中間步驟 ===");
        debugCheckMacValue(params, hashKey, hashIv);

        // === 查詢訂單範例 ===
        System.out.println("\n=== 查詢訂單範例 ===");
        TreeMap<String, String> queryParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        queryParams.put("MerchantID", "2000132");
        queryParams.put("MerchantTradeNo", "ecpay20180504112423");
        queryParams.put("TimeStamp", "1525410372");

        String expectedQuery = "48ABB4DBF2365897B2A17C48EBCBA82274E7D7DE79F1F955115C4258482A045B";
        String resultQuery = generateCheckMacValue(queryParams, hashKey, hashIv);

        System.out.println("期望值: " + expectedQuery);
        System.out.println("計算值: " + resultQuery);
        System.out.println("結果:   " + (expectedQuery.equals(resultQuery) ? "✅ 通過" : "❌ 失敗"));
    }

    static String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
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

        // 5. SHA256 雜湊 → 轉大寫
        return sha256(encoded).toUpperCase();
    }

    static void debugCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        TreeMap<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(params);

        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey).append("&");
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("HashIV=").append(hashIv);
        String raw = sb.toString();
        System.out.println("Step 2 (raw): " + raw);

        String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8).toLowerCase();
        System.out.println("Step 3 (encoded+lower): " + encoded.substring(0, Math.min(200, encoded.length())) + "...");

        encoded = encoded.replace("%2d", "-")
                .replace("%5f", "_")
                .replace("%2e", ".")
                .replace("%21", "!")
                .replace("%2a", "*")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%20", "+");
        System.out.println("Step 4 (dotnet): " + encoded.substring(0, Math.min(200, encoded.length())) + "...");

        String hash = sha256(encoded).toUpperCase();
        System.out.println("Step 5 (sha256): " + hash);
    }

    static String sha256(String input) {
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
            throw new RuntimeException("SHA256 計算失敗", e);
        }
    }
}
