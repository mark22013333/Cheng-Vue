package com.cheng.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author cheng
 * @since 2022/7/6 16:27
 **/
@Getter
public enum ApiResult {
    SUCCESS(HttpStatus.OK.value(), "Success"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "===> User not found"),
    FLEX_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "===> Can't get flex content from mKey or flexCode"),
    PARAM_ERROR(HttpStatus.BAD_REQUEST.value(), "===> Param error: %s"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Data not found"),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "%s not found"),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST.value(), "===> Validation error"),
    NO_DATA_UPDATE(HttpStatus.NO_CONTENT.value(), "===> There is no data to update"),
    TAG_IS_DISABLED(HttpStatus.FORBIDDEN.value(), "===> The tag does not exist or is disabled"),
    TAG_NOT_EXIST(HttpStatus.FORBIDDEN.value(), "===> Tag does not exist"),
    TAG_IS_ALREADY_IN_STATES(HttpStatus.NO_CONTENT.value(), "===> Already in this state"),

    AUTH_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "===> Auth type Not Found"),
    ACCESS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "===>Access unauthorized"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED.value(), "===>The token has expired or is invalid"),
    TOKEN_BLOCK(HttpStatus.UNAUTHORIZED.value(), "===>The token has been blocked"),
    TOKEN_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "===>The token does not have permission to access this resource"),

    USERNAME_OR_PASSWORD_ERROR(HttpStatus.BAD_REQUEST.value(), "===>The username or password is incorrect"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "The requested resource does not exist"),


    ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "===> ERROR: %s"),
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "===> SYSTEM ERROR: %s"),
    API_PARAM_ERROR(1, "%s is required"),
    API_KEY_ERROR(2, "wrong apikey: %s"),

    ;

    private final int code;
    private final String codeStr;
    private final String msg;

    ApiResult(int code, String msg) {
        this.codeStr = null;
        this.code = code;
        this.msg = msg;
    }
}
