package com.cheng.line.util;

import com.cheng.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 範本變數替換引擎
 * <p>
 * 支援格式：
 * - {{variable}} - 基本變數替換
 * - {{variable:預設值}} - 帶預設值的變數替換
 *
 * @author cheng
 */
@Slf4j
@Component
public class TemplateVariableEngine {

    /**
     * 變數匹配正則表達式
     * 匹配 {{variableName}} 或 {{variableName:defaultValue}}
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^{}:]+)(?::([^{}]*))?}}");

    /**
     * 解析範本內容，替換變數
     *
     * @param template  範本內容
     * @param variables 變數 Map
     * @return 替換後的內容
     */
    public String parse(String template, Map<String, String> variables) {
        if (StringUtils.isEmpty(template)) {
            return template;
        }

        if (variables == null || variables.isEmpty()) {
            // 如果沒有提供變數，將帶預設值的變數替換為預設值，其餘保留原字串
            return parseWithDefaults(template);
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String defaultValue = matcher.group(2);

            String replacement = getVariableValue(variableName, defaultValue, variables);
            // 避免 $ 和 \ 在 replacement 中被特殊處理
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 僅使用預設值解析範本（當沒有提供變數時）
     *
     * @param template 範本內容
     * @return 替換後的內容
     */
    private String parseWithDefaults(String template) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String defaultValue = matcher.group(2);

            String replacement;
            if (defaultValue != null) {
                // 有預設值，使用預設值
                replacement = defaultValue;
            } else {
                // 無預設值，保留原字串或替換為空字串（根據需求選擇）
                replacement = "";
                log.debug("變數 {{{}}} 無法解析且無預設值，替換為空字串", variableName);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 取得變數值
     *
     * @param variableName 變數名稱
     * @param defaultValue 預設值
     * @param variables    變數 Map
     * @return 變數值
     */
    private String getVariableValue(String variableName, String defaultValue, Map<String, String> variables) {
        String value = variables.get(variableName);

        if (StringUtils.isNotEmpty(value)) {
            return value;
        }

        // 變數不存在或為空，使用預設值
        if (defaultValue != null) {
            return defaultValue;
        }

        // 無預設值，替換為空字串（不拋出異常）
        log.debug("變數 {{{}}} 無法解析且無預設值，替換為空字串", variableName);
        return "";
    }

    /**
     * 檢查範本中是否包含變數
     *
     * @param template 範本內容
     * @return 是否包含變數
     */
    public boolean hasVariables(String template) {
        if (StringUtils.isEmpty(template)) {
            return false;
        }
        return VARIABLE_PATTERN.matcher(template).find();
    }

    /**
     * 提取範本中的所有變數名稱
     *
     * @param template 範本內容
     * @return 變數名稱列表
     */
    public List<String> extractVariableNames(String template) {
        List<String> variableNames = new ArrayList<>();
        if (StringUtils.isEmpty(template)) {
            return variableNames;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            if (!variableNames.contains(variableName)) {
                variableNames.add(variableName);
            }
        }
        return variableNames;
    }

    /**
     * 驗證範本語法是否正確
     *
     * @param template 範本內容
     * @return 驗證結果（null 表示無錯誤）
     */
    public String validateTemplate(String template) {
        if (StringUtils.isEmpty(template)) {
            return null;
        }

        // 檢查是否有未閉合的 {{ 或 }}
        int openCount = countOccurrences(template, "{{");
        int closeCount = countOccurrences(template, "}}");

        if (openCount != closeCount) {
            return "範本語法錯誤：變數標籤 {{ 與 }} 數量不匹配";
        }

        // 檢查是否有巢狀變數（不支援）
        if (template.contains("{{{") || template.contains("}}}")) {
            return "範本語法錯誤：不支援巢狀變數標籤";
        }

        return null;
    }

    /**
     * 計算子字串出現次數
     */
    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
