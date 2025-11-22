package com.cheng.common.enums;

/**
 * 編碼列舉基礎介面
 * <p>
 * 所有使用 code + description 模式的列舉都應該實作此介面
 * 以便統一處理和轉換
 * </p>
 *
 * @param <T> 編碼類型（如 Integer, String 等）
 * @author cheng
 */
public interface CodedEnum<T> {

    /**
     * 取得列舉代碼
     *
     * @return 列舉代碼
     */
    T getCode();

    /**
     * 取得列舉描述
     *
     * @return 列舉描述
     */
    String getDescription();

    /*
     * 根據代碼查找對應的列舉值（需在實作類別中提供靜態方法）
     * <p>
     * 範例實作：
     * <pre>
     * public static Status fromCode(Integer code) {
     *     return EnumUtils.fromCode(Status.class, code);
     * }
     * </pre>
     * </p>
     */
}
