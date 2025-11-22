package com.cheng.common.utils;

import com.cheng.common.enums.CodedEnum;

import java.util.EnumSet;
import java.util.Objects;

/**
 * 列舉工具類
 * <p>
 * 提供統一的列舉轉換和查詢方法
 * </p>
 *
 * @author cheng
 */
public class EnumUtils {

    /**
     * 根據 code 查找對應的列舉值
     *
     * @param enumClass 列舉類別
     * @param code      代碼
     * @param <E>       列舉類型
     * @param <T>       代碼類型
     * @return 對應的列舉值，找不到則返回 null
     */
    public static <E extends Enum<E> & CodedEnum<T>, T> E fromCode(Class<E> enumClass, T code) {
        if (code == null) {
            return null;
        }
        return EnumSet.allOf(enumClass).stream()
                .filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根據 code 查找對應的列舉值（找不到則拋出異常）
     *
     * @param enumClass 列舉類別
     * @param code      代碼
     * @param <E>       列舉類型
     * @param <T>       代碼類型
     * @return 對應的列舉值
     * @throws IllegalArgumentException 找不到對應的列舉值時拋出
     */
    public static <E extends Enum<E> & CodedEnum<T>, T> E fromCodeOrThrow(Class<E> enumClass, T code) {
        E result = fromCode(enumClass, code);
        if (result == null) {
            throw new IllegalArgumentException(
                    String.format("未知的%s代碼: %s", enumClass.getSimpleName(), code)
            );
        }
        return result;
    }

    /**
     * 根據 code 查找對應的描述
     *
     * @param enumClass 列舉類別
     * @param code      代碼
     * @param <E>       列舉類型
     * @param <T>       代碼類型
     * @return 對應的描述，找不到則返回空字串
     */
    public static <E extends Enum<E> & CodedEnum<T>, T> String getDescription(Class<E> enumClass, T code) {
        E enumValue = fromCode(enumClass, code);
        return enumValue != null ? enumValue.getDescription() : "";
    }

    /**
     * 根據 code 查找對應的描述（找不到則返回預設值）
     *
     * @param enumClass    列舉類別
     * @param code         代碼
     * @param defaultValue 預設值
     * @param <E>          列舉類型
     * @param <T>          代碼類型
     * @return 對應的描述，找不到則返回預設值
     */
    public static <E extends Enum<E> & CodedEnum<T>, T> String getDescription(
            Class<E> enumClass, T code, String defaultValue) {
        E enumValue = fromCode(enumClass, code);
        return enumValue != null ? enumValue.getDescription() : defaultValue;
    }

    /**
     * 檢查代碼是否有效
     *
     * @param enumClass 列舉類別
     * @param code      代碼
     * @param <E>       列舉類型
     * @param <T>       代碼類型
     * @return true=有效, false=無效
     */
    public static <E extends Enum<E> & CodedEnum<T>, T> boolean isValidCode(Class<E> enumClass, T code) {
        return fromCode(enumClass, code) != null;
    }
}
