package com.cheng.common.enums;

/**
 * 業務操作類型
 *
 * @author cheng
 */
public enum BusinessType {
    /**
     * 其它
     */
    OTHER,

    /**
     * 新增
     */
    INSERT,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 刪除
     */
    DELETE,

    /**
     * 授權
     */
    GRANT,

    /**
     * 匯出
     */
    EXPORT,

    /**
     * 匯入
     */
    IMPORT,

    /**
     * 強制登出
     */
    FORCE,

    /**
     * 產生程式碼
     */
    GENCODE,

    /**
     * 清除數據
     */
    CLEAN,
}
