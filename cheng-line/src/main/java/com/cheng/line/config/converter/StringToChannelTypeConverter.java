package com.cheng.line.config.converter;

import com.cheng.line.enums.ChannelType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * String 轉 ChannelType Enum 轉換器
 *
 * @author cheng
 */
@Component
public class StringToChannelTypeConverter implements Converter<String, ChannelType> {

    @Override
    public ChannelType convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }

        try {
            // 嘗試以代碼轉換
            return ChannelType.fromCode(source);
        } catch (IllegalArgumentException e) {
            // 嘗試以名稱轉換
            try {
                return ChannelType.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("無效的 ChannelType 值: " + source);
            }
        }
    }
}
