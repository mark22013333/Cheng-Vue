package com.cheng.common.utils.sql;

import com.cheng.common.exception.UtilException;
import com.cheng.common.utils.StringUtils;

/**
 * sql操作工具類
 *
 * @author cheng
 */
public class SqlUtil {
    /**
     * 限制orderBy最大長度
     */
    private static final int ORDER_BY_MAX_LENGTH = 500;
    /**
     * 定義常用的 sql關鍵字
     */
    public static String SQL_REGEX = "\u000B|and |extractvalue|updatexml|sleep|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";
    /**
     * 僅支援字母、數字、底線、空格、逗號、小數點（支援多個欄位排序）
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_ ,.]+";

    /**
     * 檢查字串，防止注入绕過
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new UtilException("參數不符合規範，不能進行查詢");
        }
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH) {
            throw new UtilException("參數已超過最大限制，不能進行查詢");
        }
        return value;
    }

    /**
     * 驗證 order by 語法是否符合規範
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * SQL關鍵字檢查
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new UtilException("參數存在SQL注入風險");
            }
        }
    }
}
