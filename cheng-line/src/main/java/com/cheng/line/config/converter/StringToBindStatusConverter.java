package com.cheng.line.config.converter;

import com.cheng.line.enums.BindStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * String 轉 BindStatus Enum 轉換器
 *
 * @author cheng
 */
@Component
public class StringToBindStatusConverter implements Converter<String, BindStatus> {

    @Override
    public BindStatus convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }

        try {
            return BindStatus.fromCode(source);
        } catch (IllegalArgumentException e) {
            try {
                return BindStatus.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("無效的 BindStatus 值: " + source);
            }
        }
    }
}
