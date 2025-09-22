package com.cheng.common.constant;

/**
 * 返回狀態碼
 *
 * @author cheng
 */
public class HttpStatus
{
    /**
     * 操作成功
     */
    public static final int SUCCESS = 200;

    /**
     * 物件建立成功
     */
    public static final int CREATED = 201;

    /**
     * 請求已經被接受
     */
    public static final int ACCEPTED = 202;

    /**
     * 操作已經執行成功，但是沒有返回數據
     */
    public static final int NO_CONTENT = 204;

    /**
     * 資源已被移除
     */
    public static final int MOVED_PERM = 301;

    /**
     * 重定向
     */
    public static final int SEE_OTHER = 303;

    /**
     * 資源沒有被修改
     */
    public static final int NOT_MODIFIED = 304;

    /**
     * 參數列表錯誤（缺少，格式不匹配）
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 未授權
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 訪問受限，授權過期
     */
    public static final int FORBIDDEN = 403;

    /**
     * 資源，服務未找到
     */
    public static final int NOT_FOUND = 404;

    /**
     * 不允許的http方法
     */
    public static final int BAD_METHOD = 405;

    /**
     * 資源衝突，或者資源被鎖
     */
    public static final int CONFLICT = 409;

    /**
     * 不支援的數據，媒體類型
     */
    public static final int UNSUPPORTED_TYPE = 415;

    /**
     * 系統内部錯誤
     */
    public static final int ERROR = 500;

    /**
     * 介面未實現
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * 系統警告訊息
     */
    public static final int WARN = 601;
}
