package com.cheng.common.core.text;

import com.cheng.common.utils.StringUtils;

/**
 * 字串格式化
 *
 * @author cheng
 */
public class StrFormatter {
    public static final String EMPTY_JSON = "{}";
    public static final char C_BACKSLASH = '\\';
    public static final char C_DELIM_START = '{';
    public static final char C_DELIM_END = '}';

    /**
     * 格式化字串<br>
     * 此方法只是簡單將佔位符號 {} 按照順序替換為參數<br>
     * 如果想輸出 {} 使用 \\轉譯 { 即可，如果想輸出 {} 之前的 \ 使用雙轉譯符號 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 轉譯{}： format ("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 轉譯\： format ("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param strPattern 字串模板
     * @param argArray   參數列表
     * @return 結果
     */
    public static String format(final String strPattern, final Object... argArray) {
        if (StringUtils.isEmpty(strPattern) || StringUtils.isEmpty(argArray)) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();

        // 初始化定義好的長度以獲得更好的性能
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;
        int delimIndex;// 佔位符號所在位置
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    return strPattern;
                } else { // 字串模板剩於部分不再包含佔位符號，加入剩於部分後返回結果
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {
                        // 轉譯符號之前還有一個轉譯符號，佔位符號依舊有效
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(Convert.utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        // 佔位符號被轉譯
                        argIndex--;
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(C_DELIM_START);
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    // 正常佔位符號
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(Convert.utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // 加入最後一個佔位符號後所有的字串
        sbuf.append(strPattern, handledPosition, strPattern.length());

        return sbuf.toString();
    }
}
