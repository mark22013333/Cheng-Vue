package com.cheng.system.domain;

import com.cheng.common.utils.StringUtils;

/**
 * 暫存訊息
 *
 * @author cheng
 */
public class SysCache {
    /**
     * 暫存名稱
     */
    private String cacheName = "";

    /**
     * 暫存鍵名
     */
    private String cacheKey = "";

    /**
     * 暫存内容
     */
    private String cacheValue = "";

    /**
     * 備註
     */
    private String remark = "";

    public SysCache() {

    }

    public SysCache(String cacheName, String remark) {
        this.cacheName = cacheName;
        this.remark = remark;
    }

    public SysCache(String cacheName, String cacheKey, String cacheValue) {
        this.cacheName = StringUtils.replace(cacheName, ":", "");
        this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
        this.cacheValue = cacheValue;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getCacheValue() {
        return cacheValue;
    }

    public void setCacheValue(String cacheValue) {
        this.cacheValue = cacheValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
