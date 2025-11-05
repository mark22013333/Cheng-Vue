package com.cheng.common.enums;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author cheng
 * @since 2024/3/7 22:59
 **/
public interface Base<T> {

    T getValue();

    String getLabel();

    /**
     * 使用指定的映射函數在枚舉類中查找匹配的枚舉實例
     *
     * @param <E>         枚舉類型，必須實現{@link Base}介面
     * @param value       要匹配的值
     * @param valueMapper 將枚舉實例映射到比較值的函數
     * @param clazz       枚舉的Class物件
     * @return 匹配的枚舉實例的Optional封裝，如果沒有找到匹配則為Optional.empty()
     */
    private static <E extends Enum<E> & Base<?>> Optional<E> findMatchingEnum(Object value, Function<E, ?> valueMapper, Class<E> clazz) {
        return EnumSet.allOf(clazz).stream()
                .filter(e -> Objects.equals(valueMapper.apply(e), value))
                .findFirst();
    }

    /**
     * 根據值返回相對應的枚舉物件
     *
     * @param value 要匹配的值，不能為null
     * @param clazz 枚舉的Class物件
     * @return 匹配值的枚舉實例的Optional封裝，如果沒有找到匹配則為Optional.empty()
     * @throws NullPointerException 如果value為null
     */
    static <E extends Enum<E> & Base<?>> Optional<E> getEnumByValue(Object value, Class<E> clazz) {
        Objects.requireNonNull(value, "value cannot be null");
        return findMatchingEnum(value, Base::getValue, clazz);
    }

    /**
     * 根據值返回相對應的枚舉的標籤
     *
     * @param value 要匹配的值，不能為null。
     * @param clazz 枚舉的Class物件。
     * @return 匹配提供的值的枚舉標籤的Optional封裝，如果沒有找到匹配則為Optional.empty()
     * @throws NullPointerException 如果value為null
     */
    static <E extends Enum<E> & Base<?>> Optional<String> getLabelByValue(Object value, Class<E> clazz) {
        Objects.requireNonNull(value, "value cannot be null");
        return getEnumByValue(value, clazz).map(Base::getLabel);
    }

    /**
     * 根據標籤返回相對應的枚舉的值
     *
     * @param label 要匹配的標籤，不能為null
     * @param clazz 枚舉的Class物件
     * @return 匹配標籤的枚舉值的Optional封裝，如果沒有找到匹配則為Optional.empty()
     * @throws NullPointerException 如果label為null
     */
    static <E extends Enum<E> & Base<?>> Optional<Object> getValueByLabel(String label, Class<E> clazz) {
        Objects.requireNonNull(label, "label cannot be null");
        return findMatchingEnum(label, Base::getLabel, clazz)
                .map(Base::getValue);
    }
}
