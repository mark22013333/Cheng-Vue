package com.cheng.common.utils.uuid;

import java.util.concurrent.ThreadLocalRandom;
import com.cheng.common.utils.TraceUtils;

/**
 * ID產生器工具類
 *
 * @author cheng
 */
public class IdUtils {
    /**
     * 取得隨機UUID
     *
     * @return 隨機UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 簡化的UUID，去掉了横線
     *
     * @return 簡化的UUID，去掉了横線
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 取得隨機UUID，使用效能更好的ThreadLocalRandom產生UUID
     *
     * @return 隨機UUID
     */
    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }

    /**
     * 簡化的UUID，去掉了横線，使用效能更好的ThreadLocalRandom產生UUID
     *
     * @return 簡化的UUID，去掉了横線
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * 產生物品編碼
     * 格式：ITEM-{traceId}
     * 使用 TraceUtils 產生唯一識別碼，確保絕對不重複
     *
     * @return 物品編碼
     */
    public static String generateItemCode() {
        return "ITEM-" + TraceUtils.generateTraceId();
    }

    /**
     * 產生指定前綴的編碼
     * 格式：前綴 + 時間戳 + 3位隨機數
     *
     * @param prefix 編碼前綴
     * @return 編碼
     */
    public static String generateCode(String prefix) {
        long timestamp = System.currentTimeMillis();
        int randomNum = ThreadLocalRandom.current().nextInt(1000);
        return prefix + timestamp + String.format("%03d", randomNum);
    }
}
