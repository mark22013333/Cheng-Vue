package com.cheng.common.utils.uuid;

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
     * 取得隨機UUID，使用性能更好的ThreadLocalRandom產生UUID
     *
     * @return 隨機UUID
     */
    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }

    /**
     * 簡化的UUID，去掉了横線，使用性能更好的ThreadLocalRandom產生UUID
     *
     * @return 簡化的UUID，去掉了横線
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }
}
