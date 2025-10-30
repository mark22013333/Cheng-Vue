package com.cheng.line.config.converter;

import com.cheng.line.enums.FollowStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * String 轉 FollowStatus Enum 轉換器
 *
 * @author cheng
 */
@Component
public class StringToFollowStatusConverter implements Converter<String, FollowStatus> {

    @Override
    public FollowStatus convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }

        try {
            return FollowStatus.fromCode(source);
        } catch (IllegalArgumentException e) {
            try {
                return FollowStatus.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("無效的 FollowStatus 值: " + source);
            }
        }
    }
}
