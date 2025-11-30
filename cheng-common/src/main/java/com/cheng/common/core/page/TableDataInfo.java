package com.cheng.common.core.page;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表格分頁數據物件
 *
 * @author cheng
 */
@Setter
@Getter
public class TableDataInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 總記錄數
     */
    private long total;

    /**
     * 列表數據
     */
    private List<?> rows;

    /**
     * 訊息狀態碼
     */
    private int code;

    /**
     * 訊息内容
     */
    private String msg;

    /**
     * 額外資料（用於存放統計資料等）
     */
    private Map<String, Object> extra;

    /**
     * 表格數據物件
     */
    public TableDataInfo() {
        this.extra = new HashMap<>();
    }

    /**
     * 分頁
     *
     * @param list  列表數據
     * @param total 總記錄數
     */
    public TableDataInfo(List<?> list, long total) {
        this.rows = list;
        this.total = total;
        this.extra = new HashMap<>();
    }

    /**
     * 新增額外資料
     *
     * @param key   鍵
     * @param value 值
     * @return TableDataInfo
     */
    public TableDataInfo put(String key, Object value) {
        this.extra.put(key, value);
        return this;
    }

}
