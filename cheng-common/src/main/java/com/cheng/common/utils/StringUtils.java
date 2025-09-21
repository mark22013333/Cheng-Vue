package com.cheng.common.utils;

import com.cheng.common.constant.Constants;
import com.cheng.common.core.text.StrFormatter;
import org.springframework.util.AntPathMatcher;

import java.util.*;

/**
 * 字串工具類
 *
 * @author cheng
 */
@SuppressWarnings("deprecation")
public class StringUtils extends org.apache.commons.lang3.StringUtils
{
    /**
     * 空字串
     */
    private static final String NULLSTR = "";

    /** 下畫線 */
    private static final char SEPARATOR = '_';

    /** 星號 */
    private static final char ASTERISK = '*';

    /**
     * 取得參數不為空值
     *
     * @param value defaultValue 要判斷的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * * 判斷一個Collection是否為空， 包含List，Set，Queue
     *
     * @param coll 要判斷的Collection
     * @return true：為空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll)
    {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * 判斷一個Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判斷的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll)
    {
        return !isEmpty(coll);
    }

    /**
     * * 判斷一個物件陣列是否為空
     *
     * @param objects 要判斷的物件陣列
     ** @return true：為空 false：非空
     */
    public static boolean isEmpty(Object[] objects)
    {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判斷一個物件陣列是否非空
     *
     * @param objects 要判斷的物件陣列
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects)
    {
        return !isEmpty(objects);
    }

    /**
     * * 判斷一個Map是否為空
     *
     * @param map 要判斷的Map
     * @return true：為空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * 判斷一個Map是否為空
     *
     * @param map 要判斷的Map
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }

    /**
     * * 判斷一個字串是否為空串
     * 
     * @param str String
     * @return true：為空 false：非空
     */
    public static boolean isEmpty(String str)
    {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * * 判斷一個字串是否為非空串
     * 
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    /**
     * * 判斷一個物件是否為空
     * 
     * @param object Object
     * @return true：為空 false：非空
     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * * 判斷一個物件是否非空
     * 
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    /**
     * * 判斷一個物件是否是陣列類型（Java基本型别的陣列）
     *
     * @param object 物件
     * @return true：是陣列 false：不是陣列
     */
    public static boolean isArray(Object object)
    {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * 去空格
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }

    /**
     * 替換指定字串的指定區間内字串為"*"
     *
     * @param str 字串
     * @param startInclude 開始位置（包含）
     * @param endExclude 結束位置（不包含）
     * @return 替換後的字串
     */
    public static String hide(CharSequence str, int startInclude, int endExclude)
    {
        if (isEmpty(str))
        {
            return NULLSTR;
        }
        final int strLength = str.length();
        if (startInclude > strLength)
        {
            return NULLSTR;
        }
        if (endExclude > strLength)
        {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            // 如果起始位置大於結束位置，不替換
            return NULLSTR;
        }
        final char[] chars = new char[strLength];
        for (int i = 0; i < strLength; i++)
        {
            if (i >= startInclude && i < endExclude)
            {
                chars[i] = ASTERISK;
            }
            else
            {
                chars[i] = str.charAt(i);
            }
        }
        return new String(chars);
    }

    /**
     * 截取字串
     *
     * @param str 字串
     * @param start 開始
     * @return 結果
     */
    public static String substring(final String str, int start)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = str.length() + start;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (start > str.length())
        {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * 截取字串
     *
     * @param str 字串
     * @param start 開始
     * @param end 結束
     * @return 結果
     */
    public static String substring(final String str, int start, int end)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (end < 0)
        {
            end = str.length() + end;
        }
        if (start < 0)
        {
            start = str.length() + start;
        }

        if (end > str.length())
        {
            end = str.length();
        }

        if (start > end)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 在字串中尋找第一個出現的 `open` 和最後一個出現的 `close` 之間的子字串
     *
     * @param str 要截取的字串
     * @param open 起始字串
     * @param close 結束字串
     * @return 截取結果
     */
    public static String substringBetweenLast(final String str, final String open, final String close)
    {
        if (isEmpty(str) || isEmpty(open) || isEmpty(close))
        {
            return NULLSTR;
        }
        final int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND)
        {
            final int end = str.lastIndexOf(close);
            if (end != INDEX_NOT_FOUND)
            {
                return str.substring(start + open.length(), end);
            }
        }
        return NULLSTR;
    }

    /**
     * 判斷是否為空，並且不是空白字串
     *
     * @param str 要判斷的value
     * @return 結果
     */
    public static boolean hasText(String str)
    {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str)
    {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++)
        {
            if (!Character.isWhitespace(str.charAt(i)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 格式化文字, {} 表示佔位符號<br>
     * 此方法只是簡單將佔位符號 {} 按照順序替換為參數<br>
     * 如果想輸出 {} 使用 \\轉譯 { 即可，如果想輸出 {} 之前的 \ 使用雙轉譯符號 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 轉譯{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 轉譯\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template 文字模板，被替換的部分用 {} 表示
     * @param params 參數值
     * @return 格式化後的文字
     */
    public static String format(String template, Object... params)
    {
        if (isEmpty(params) || isEmpty(template))
        {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * 是否為http(s)://開頭
     *
     * @param link 鏈接
     * @return 結果
     */
    public static boolean ishttp(String link)
    {
        return StringUtils.startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 字串轉set
     *
     * @param str 字串
     * @param sep 分隔符號
     * @return set集合
     */
    public static final Set<String> str2Set(String str, String sep)
    {
        return new HashSet<String>(str2List(str, sep, true, false));
    }

    /**
     * 字串轉list
     *
     * @param str 字串
     * @param sep 分隔符號
     * @return list集合
     */
    public static final List<String> str2List(String str, String sep)
    {
        return str2List(str, sep, true, false);
    }

    /**
     * 字串轉list
     *
     * @param str 字串
     * @param sep 分隔符號
     * @param filterBlank 過濾纯空白
     * @param trim 去掉首尾空白
     * @return list集合
     */
    public static final List<String> str2List(String str, String sep, boolean filterBlank, boolean trim)
    {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isEmpty(str))
        {
            return list;
        }

        // 過濾空白字串
        if (filterBlank && StringUtils.isBlank(str))
        {
            return list;
        }
        String[] split = str.split(sep);
        for (String string : split)
        {
            if (filterBlank && StringUtils.isBlank(string))
            {
                continue;
            }
            if (trim)
            {
                string = string.trim();
            }
            list.add(string);
        }

        return list;
    }

    /**
     * 判斷給定的collection列表中是否包含陣列array 判斷給定的陣列array中是否包含給定的元素value
     *
     * @param collection 給定的集合
     * @param array 給定的陣列
     * @return boolean 結果
     */
    public static boolean containsAny(Collection<String> collection, String... array)
    {
        if (isEmpty(collection) || isEmpty(array))
        {
            return false;
        }
        else
        {
            for (String str : array)
            {
                if (collection.contains(str))
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 尋找指定字串是否包含指定字串列表中的任意一個字串同時串忽略大小寫
     *
     * @param cs 指定字串
     * @param searchCharSequences 需要檢查的字串陣列
     * @return 是否包含任意一個字串
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences)
    {
        if (isEmpty(cs) || isEmpty(searchCharSequences))
        {
            return false;
        }
        for (CharSequence testStr : searchCharSequences)
        {
            if (containsIgnoreCase(cs, testStr))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 驼峰轉下畫線命名
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字串是否大寫
        boolean preCharIsUpperCase = true;
        // 目前字串是否大寫
        boolean curreCharIsUpperCase = true;
        // 下一字串是否大寫
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字串
     *
     * @param str 驗證字串
     * @param strs 字串組
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 將下畫線大寫方式命名的字串轉換為驼峰式。如果轉換前的下畫線大寫方式命名的字串為空，則返回空字串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 轉換前的下畫線大寫方式命名的字串
     * @return 轉換後的驼峰式命名的字串
     */
    public static String convertToCamelCase(String name)
    {
        StringBuilder result = new StringBuilder();
        // 快速檢查
        if (name == null || name.isEmpty()) {
            // 沒必要轉換
            return "";
        }
        else if (!name.contains("_")) {
            // 不含下畫線，僅將首字母大寫
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下畫線將原始字串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳過原始字串中開頭、結尾的下換線或雙重下畫線
            if (camel.isEmpty())
            {
                continue;
            }
            // 首字母大寫
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 驼峰式命名法
     * 例如：user_name->userName
     */
    public static String toCamelCase(String s)
    {
        if (s == null)
        {
            return null;
        }
        if (s.indexOf(SEPARATOR) == -1)
        {
            return s;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (c == SEPARATOR)
            {
                upperCase = true;
            }
            else if (upperCase)
            {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 尋找指定字串是否匹配指定字串列表中的任意一個字串
     *
     * @param str 指定字串
     * @param strs 需要檢查的字串陣列
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> strs)
    {
        if (isEmpty(str) || isEmpty(strs))
        {
            return false;
        }
        for (String pattern : strs)
        {
            if (isMatch(pattern, str))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判斷url是否與規則配置: 
     * ? 表示單個字串; 
     * * 表示一層路徑内的任意字串，不可跨層級; 
     * ** 表示任意層路徑;
     *
     * @param pattern 匹配規則
     * @param url 需要匹配的url
     * @return
     */
    public static boolean isMatch(String pattern, String url)
    {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj)
    {
        return (T) obj;
    }

    /**
     * 數字左邊补齐0，使之達到指定長度。註意，如果數字轉換為字串後，長度大於size，則只保留 最後size個字串。
     *
     * @param num 數字物件
     * @param size 字串指定長度
     * @return 返回數字的字串格式，該字串為指定長度。
     */
    public static final String padl(final Number num, final int size)
    {
        return padl(num.toString(), size, '0');
    }

    /**
     * 字串左补齐。如果原始字串s長度大於size，則只保留最後size個字串。
     *
     * @param s 原始字串
     * @param size 字串指定長度
     * @param c 用於补齐的字串
     * @return 返回指定長度的字串，由原字串左补齐或截取得到。
     */
    public static final String padl(final String s, final int size, final char c)
    {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null)
        {
            final int len = s.length();
            if (s.length() <= size)
            {
                for (int i = size - len; i > 0; i--)
                {
                    sb.append(c);
                }
                sb.append(s);
            }
            else
            {
                return s.substring(len - size, len);
            }
        }
        else
        {
            for (int i = size; i > 0; i--)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}