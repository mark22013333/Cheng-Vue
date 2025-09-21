package com.cheng.common.core.domain;

import com.cheng.common.constant.HttpStatus;
import com.cheng.common.utils.StringUtils;

import java.util.HashMap;
import java.util.Objects;

/**
 * 操作訊息提醒
 *
 * @author cheng
 */
public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 狀態碼
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回内容
     */
    public static final String MSG_TAG = "msg";

    /**
     * 數據物件
     */
    public static final String DATA_TAG = "data";

    /**
     * 初始化一個新建立的 AjaxResult 物件，使其表示一個空訊息。
     */
    public AjaxResult() {
    }

    /**
     * 初始化一個新建立的 AjaxResult 物件
     *
     * @param code 狀態碼
     * @param msg  返回内容
     */
    public AjaxResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一個新建立的 AjaxResult 物件
     *
     * @param code 狀態碼
     * @param msg  返回内容
     * @param data 數據物件
     */
    public AjaxResult(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功訊息
     *
     * @return 成功訊息
     */
    public static AjaxResult success() {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功數據
     *
     * @return 成功訊息
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功訊息
     *
     * @param msg 返回内容
     * @return 成功訊息
     */
    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功訊息
     *
     * @param msg  返回内容
     * @param data 數據物件
     * @return 成功訊息
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回警告訊息
     *
     * @param msg 返回内容
     * @return 警告訊息
     */
    public static AjaxResult warn(String msg) {
        return AjaxResult.warn(msg, null);
    }

    /**
     * 返回警告訊息
     *
     * @param msg  返回内容
     * @param data 數據物件
     * @return 警告訊息
     */
    public static AjaxResult warn(String msg, Object data) {
        return new AjaxResult(HttpStatus.WARN, msg, data);
    }

    /**
     * 返回錯誤訊息
     *
     * @return 錯誤訊息
     */
    public static AjaxResult error() {
        return AjaxResult.error("操作失敗");
    }

    /**
     * 返回錯誤訊息
     *
     * @param msg 返回内容
     * @return 錯誤訊息
     */
    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回錯誤訊息
     *
     * @param msg  返回内容
     * @param data 數據物件
     * @return 錯誤訊息
     */
    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(HttpStatus.ERROR, msg, data);
    }

    /**
     * 返回錯誤訊息
     *
     * @param code 狀態碼
     * @param msg  返回内容
     * @return 錯誤訊息
     */
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    /**
     * 是否為成功訊息
     *
     * @return 結果
     */
    public boolean isSuccess() {
        return Objects.equals(HttpStatus.SUCCESS, this.get(CODE_TAG));
    }

    /**
     * 是否為警告訊息
     *
     * @return 結果
     */
    public boolean isWarn() {
        return Objects.equals(HttpStatus.WARN, this.get(CODE_TAG));
    }

    /**
     * 是否為錯誤訊息
     *
     * @return 結果
     */
    public boolean isError() {
        return Objects.equals(HttpStatus.ERROR, this.get(CODE_TAG));
    }

    /**
     * 方便鏈式呼叫
     *
     * @param key   鍵
     * @param value 值
     * @return 數據物件
     */
    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
