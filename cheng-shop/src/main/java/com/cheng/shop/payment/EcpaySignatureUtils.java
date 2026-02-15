package com.cheng.shop.payment;

import com.cheng.common.exception.ServiceException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 綠界簽章工具類（金流與物流共用）
 * <p>
 * CheckMacValue 演算法：
 * <ol>
 *   <li>按 key 字母排序（不分大小寫）</li>
 *   <li>組合: "HashKey={key}&amp;k1=v1&amp;k2=v2&amp;...&amp;HashIV={iv}"</li>
 *   <li>URL encode → 轉小寫</li>
 *   <li>特殊字元替換（ECPay .NET URLEncode 規範）</li>
 *   <li>SHA256 或 MD5 雜湊 → 轉大寫</li>
 * </ol>
 *
 * @author cheng
 */
public final class EcpaySignatureUtils {

    private EcpaySignatureUtils() {
    }

    /**
     * 產生 CheckMacValue（SHA256）
     *
     * @param params  參數 Map（不含 CheckMacValue）
     * @param hashKey 綠界 HashKey
     * @param hashIv  綠界 HashIV
     * @return CheckMacValue（大寫）
     */
    public static String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        return generateCheckMacValue(params, hashKey, hashIv, "1");
    }

    /**
     * 產生 CheckMacValue
     *
     * @param params      參數 Map（不含 CheckMacValue）
     * @param hashKey     綠界 HashKey
     * @param hashIv      綠界 HashIV
     * @param encryptType 加密類型：0=MD5, 1=SHA256
     * @return CheckMacValue（大寫）
     */
    public static String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv, String encryptType) {
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
     * 驗證 CheckMacValue
     *
     * @param params  包含 CheckMacValue 的完整參數
     * @param hashKey 綠界 HashKey
     * @param hashIv  綠界 HashIV
     * @return 是否驗證通過
     */
    public static boolean verifyCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        String receivedCheckMac = params.get("CheckMacValue");
        if (receivedCheckMac == null || receivedCheckMac.isBlank()) {
            return false;
        }

        // 移除 CheckMacValue 與 EncryptType 後重新計算
        TreeMap<String, String> paramsWithoutCheck = new TreeMap<>(params);
        paramsWithoutCheck.remove("CheckMacValue");
        String encryptType = paramsWithoutCheck.remove("EncryptType");
        if (encryptType == null || encryptType.isBlank()) {
            encryptType = "1";
        }

        String calculated = generateCheckMacValue(paramsWithoutCheck, hashKey, hashIv, encryptType);
        return calculated.equalsIgnoreCase(receivedCheckMac);
    }

    /**
     * SHA256 雜湊
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new ServiceException("SHA256 計算失敗");
        }
    }

    /**
     * MD5 雜湊
     */
    public static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new ServiceException("MD5 計算失敗");
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 產生自動提交的 HTML form（值經 HTML entity 跳脫）
     *
     * @param actionUrl 提交目標 URL
     * @param params    表單參數
     * @return HTML 字串
     */
    public static String buildAutoSubmitForm(String actionUrl, Map<String, String> params) {
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
    public static String htmlEscape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
