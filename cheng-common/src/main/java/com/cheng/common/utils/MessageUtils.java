package com.cheng.common.utils;

import com.cheng.common.utils.spring.SpringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 取得i18n資源文件
 *
 * @author cheng
 */
public class MessageUtils
{
    /**
     * 根據訊息鍵和參數 取得訊息 委托給spring messageSource
     *
     * @param code 訊息鍵
     * @param args 參數
     * @return 取得国際化翻译值
     */
    public static String message(String code, Object... args)
    {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
