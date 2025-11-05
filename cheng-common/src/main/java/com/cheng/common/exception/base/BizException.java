package com.cheng.common.exception.base;


import com.cheng.common.enums.ApiResult;
import lombok.Getter;

/**
 * 業務邏輯處理異常
 *
 * @author cheng
 * @since 2022/7/6 16:26
 **/
@Getter
public class BizException extends RuntimeException {
    private final int code;

    private BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    private BizException(Throwable throwable, int code, String msg) {
        super(msg, throwable);
        this.code = code;
    }

    public static BizException error(int code, String msg) {
        return new BizException(code, msg);
    }

    public static BizException error(String msg) {
        return new BizException(ApiResult.ERROR.getCode(), String.format(ApiResult.ERROR.getMsg(), msg));
    }

    public static BizException error(Throwable throwable, String msg) {
        return new BizException(throwable, ApiResult.ERROR.getCode(), String.format(ApiResult.ERROR.getMsg(), msg));
    }

    public static BizException error(Throwable throwable, ApiResult status) {
        return new BizException(throwable, status.getCode(), status.getMsg());
    }

    public static BizException create(ApiResult status, Object... args) {
        String msg;
        if (args.length == 0) {
            msg = status.getMsg();
        } else {
            msg = String.format(status.getMsg(), args);
        }

        return new BizException(status.getCode(), msg);
    }

}

