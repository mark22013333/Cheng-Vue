package com.cheng.common.exception.base;

import com.cheng.common.utils.MessageUtils;
import com.cheng.common.utils.StringUtils;
import lombok.Getter;

import java.io.Serial;

/**
 * 基礎異常
 *
 * @author cheng
 */
@Getter
public class BaseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所屬模組
     */
    private final String module;

    /**
     * 錯誤碼
     */
    private final String code;

    /**
     * 錯誤碼對應的參數
     */
    private final Object[] args;

    /**
     * 錯誤訊息
     */
    private final String defaultMessage;

    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    @Override
    public String getMessage() {
        String message = null;
        if (!StringUtils.isEmpty(code)) {
            message = MessageUtils.message(code, args);
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }

}
