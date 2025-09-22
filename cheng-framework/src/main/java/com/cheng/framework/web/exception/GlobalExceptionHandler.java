package com.cheng.framework.web.exception;

import com.cheng.common.constant.HttpStatus;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.text.Convert;
import com.cheng.common.exception.DemoModeException;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.html.EscapeUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

/**
 * 全域異常處理器
 *
 * @author cheng
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 權限校驗異常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("請求網址'{}',權限校驗失敗'{}'", requestURI, e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "沒有權限，請聯絡管理員授權");
    }

    /**
     * 請求方式不支援
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("請求網址'{}',不支援'{}'請求", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 業務異常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 請求路徑中缺少必需的路徑變數
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("請求路徑中缺少必需的路徑變數'{}',發生系統異常.", requestURI, e);
        return AjaxResult.error(String.format("請求路徑中缺少必需的路徑變數[%s]", e.getVariableName()));
    }

    /**
     * 請求參數類型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value)) {
            value = EscapeUtil.clean(value);
        }
        log.error("請求參數類型不匹配'{}',發生系統異常.", requestURI, e);
        return AjaxResult.error(String.format("請求參數類型不匹配，參數[%s]要求類型為：'%s'，但輸入值為：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 攔截未知的執行時異常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("請求網址'{}',發生未知異常.", requestURI, e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 系統異常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("請求網址'{}',發生系統異常.", requestURI, e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 自定義驗證異常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e) {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定義驗證異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 展示模式異常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e) {
        return AjaxResult.error("展示模式，不允許操作");
    }
}
