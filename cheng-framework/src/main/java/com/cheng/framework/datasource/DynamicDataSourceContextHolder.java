package com.cheng.framework.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 資料來源切換處理
 *
 * @author cheng
 */
public class DynamicDataSourceContextHolder {
    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    /**
     * 使用ThreadLocal维护變數，ThreadLocal為每個使用該變數的執行緒提供独立的變數副本，
     * 所以每一個執行緒都可以独立地改變自己的副本，而不會影響其它執行緒所對應的副本。
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 獲得資料來源的變數
     */
    public static String getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 設定資料來源的變數
     */
    public static void setDataSourceType(String dsType) {
        log.info("切換到{}資料來源", dsType);
        CONTEXT_HOLDER.set(dsType);
    }

    /**
     * 清除資料來源變數
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}
