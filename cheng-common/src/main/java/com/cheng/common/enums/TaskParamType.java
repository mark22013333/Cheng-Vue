package com.cheng.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任務參數類型列舉
 * 定義任務參數在前端的輸入方式
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Getter
@AllArgsConstructor
public enum TaskParamType {
    
    /**
     * 字串輸入（單行文字框）
     */
    STRING("字串", "string"),
    
    /**
     * 數字輸入（數字輸入框）
     */
    NUMBER("數字", "number"),
    
    /**
     * 布林值（開關/勾選框）
     */
    BOOLEAN("布林值", "boolean"),
    
    /**
     * 下拉選單（單選）
     */
    SELECT("下拉選單", "select"),
    
    /**
     * 多選下拉
     */
    MULTI_SELECT("多選下拉", "multiSelect"),
    
    /**
     * 日期選擇器（單一日期）
     */
    DATE("日期", "date"),
    
    /**
     * 日期範圍選擇器
     */
    DATE_RANGE("日期範圍", "dateRange"),
    
    /**
     * 時間選擇器
     */
    TIME("時間", "time"),
    
    /**
     * 文字區域（多行文字框）
     */
    TEXTAREA("文字區域", "textarea");
    
    /**
     * 顯示名稱
     */
    private final String label;
    
    /**
     * 程式碼（用於前端識別）
     */
    private final String code;
}
