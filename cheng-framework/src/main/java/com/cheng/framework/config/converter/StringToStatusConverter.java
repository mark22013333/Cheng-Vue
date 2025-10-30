package com.cheng.framework.config.converter;

import com.cheng.common.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * String 轉 Status Enum 轉換器
 *
 * @author cheng
 */
@Component
public class StringToStatusConverter implements Converter<String, Status> {

    @Override
    public Status convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 嘗試以代碼轉換
            Integer code = Integer.parseInt(source);
            return Status.fromCode(code);
        } catch (NumberFormatException e) {
            // 如果不是數字，嘗試以名稱轉換
            try {
                return Status.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("無效的 Status 值: " + source);
            }
        }
    }
}
