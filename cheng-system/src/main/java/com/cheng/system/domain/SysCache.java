package com.cheng.system.domain;

import com.cheng.common.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 暫存訊息
 *
 * @author cheng
 */
@Setter
@Getter
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

}
