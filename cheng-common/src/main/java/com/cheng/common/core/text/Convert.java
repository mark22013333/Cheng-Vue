package com.cheng.common.core.text;

import com.cheng.common.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Set;

/**
 * 類型轉換器
 *
 * @author cheng
 */
public class Convert
{
    /**
     * 轉換為字串<br>
     * 如果給定的值為null，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static String toStr(Object value, String defaultValue)
    {
        if (null == value)
        {
            return defaultValue;
        }
        if (value instanceof String)
        {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * 轉換為字串<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static String toStr(Object value)
    {
        return toStr(value, null);
    }

    /**
     * 轉換為字串<br>
     * 如果給定的值為null，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Character toChar(Object value, Character defaultValue)
    {
        if (null == value)
        {
            return defaultValue;
        }
        if (value instanceof Character)
        {
            return (Character) value;
        }

        final String valueStr = toStr(value, null);
        return StringUtils.isEmpty(valueStr) ? defaultValue : valueStr.charAt(0);
    }

    /**
     * 轉換為字串<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Character toChar(Object value)
    {
        return toChar(value, null);
    }

    /**
     * 轉換為byte<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Byte toByte(Object value, Byte defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Byte)
        {
            return (Byte) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).byteValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Byte.parseByte(valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為byte<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Byte toByte(Object value)
    {
        return toByte(value, null);
    }

    /**
     * 轉換為Short<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Short toShort(Object value, Short defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Short)
        {
            return (Short) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).shortValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Short.parseShort(valueStr.trim());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為Short<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Short toShort(Object value)
    {
        return toShort(value, null);
    }

    /**
     * 轉換為Number<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Number toNumber(Object value, Number defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return (Number) value;
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return NumberFormat.getInstance().parse(valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為Number<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Number toNumber(Object value)
    {
        return toNumber(value, null);
    }

    /**
     * 轉換為int<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Integer toInt(Object value, Integer defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(valueStr.trim());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為int<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Integer toInt(Object value)
    {
        return toInt(value, null);
    }

    /**
     * 轉換為Integer陣列<br>
     *
     * @param str 被轉換的值
     * @return 結果
     */
    public static Integer[] toIntArray(String str)
    {
        return toIntArray(",", str);
    }

    /**
     * 轉換為Long陣列<br>
     *
     * @param str 被轉換的值
     * @return 結果
     */
    public static Long[] toLongArray(String str)
    {
        return toLongArray(",", str);
    }

    /**
     * 轉換為Integer陣列<br>
     *
     * @param split 分隔符號
     * @param split 被轉換的值
     * @return 結果
     */
    public static Integer[] toIntArray(String split, String str)
    {
        if (StringUtils.isEmpty(str))
        {
            return new Integer[] {};
        }
        String[] arr = str.split(split);
        final Integer[] ints = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++)
        {
            final Integer v = toInt(arr[i], 0);
            ints[i] = v;
        }
        return ints;
    }

    /**
     * 轉換為Long陣列<br>
     *
     * @param split 分隔符號
     * @param str 被轉換的值
     * @return 結果
     */
    public static Long[] toLongArray(String split, String str)
    {
        if (StringUtils.isEmpty(str))
        {
            return new Long[] {};
        }
        String[] arr = str.split(split);
        final Long[] longs = new Long[arr.length];
        for (int i = 0; i < arr.length; i++)
        {
            final Long v = toLong(arr[i], null);
            longs[i] = v;
        }
        return longs;
    }

    /**
     * 轉換為String陣列<br>
     *
     * @param str 被轉換的值
     * @return 結果
     */
    public static String[] toStrArray(String str)
    {
        if (StringUtils.isEmpty(str))
        {
            return new String[] {};
        }
        return toStrArray(",", str);
    }

    /**
     * 轉換為String陣列<br>
     *
     * @param split 分隔符號
     * @param split 被轉換的值
     * @return 結果
     */
    public static String[] toStrArray(String split, String str)
    {
        return str.split(split);
    }

    /**
     * 轉換為long<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Long toLong(Object value, Long defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Long)
        {
            return (Long) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            // 支援科学計數法
            return new BigDecimal(valueStr.trim()).longValue();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為long<br>
     * 如果給定的值為<code>null</code>，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Long toLong(Object value)
    {
        return toLong(value, null);
    }

    /**
     * 轉換為double<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Double toDouble(Object value, Double defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Double)
        {
            return (Double) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            // 支援科学計數法
            return new BigDecimal(valueStr.trim()).doubleValue();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為double<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Double toDouble(Object value)
    {
        return toDouble(value, null);
    }

    /**
     * 轉換為Float<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Float toFloat(Object value, Float defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Float)
        {
            return (Float) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).floatValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Float.parseFloat(valueStr.trim());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為Float<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Float toFloat(Object value)
    {
        return toFloat(value, null);
    }

    /**
     * 轉換為boolean<br>
     * String支援的值為：true、false、yes、ok、no、1、0、是、否, 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static Boolean toBool(Object value, Boolean defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        valueStr = valueStr.trim().toLowerCase();
        switch (valueStr)
        {
            case "true":
            case "yes":
            case "ok":
            case "1":
            case "是":
                return true;
            case "false":
            case "no":
            case "0":
            case "否":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * 轉換為boolean<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static Boolean toBool(Object value)
    {
        return toBool(value, null);
    }

    /**
     * 轉換為Enum物件<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     *
     * @param clazz Enum的Class
     * @param value 值
     * @param defaultValue 預設值
     * @return Enum
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value, E defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (clazz.isAssignableFrom(value.getClass()))
        {
            @SuppressWarnings("unchecked")
            E myE = (E) value;
            return myE;
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Enum.valueOf(clazz, valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為Enum物件<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<code>null</code><br>
     *
     * @param clazz Enum的Class
     * @param value 值
     * @return Enum
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value)
    {
        return toEnum(clazz, value, null);
    }

    /**
     * 轉換為BigInteger<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static BigInteger toBigInteger(Object value, BigInteger defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof BigInteger)
        {
            return (BigInteger) value;
        }
        if (value instanceof Long)
        {
            return BigInteger.valueOf((Long) value);
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return new BigInteger(valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為BigInteger<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<code>null</code><br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static BigInteger toBigInteger(Object value)
    {
        return toBigInteger(value, null);
    }

    /**
     * 轉換為BigDecimal<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @param defaultValue 轉換錯誤時的預設值
     * @return 結果
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        if (value instanceof Long)
        {
            return new BigDecimal((Long) value);
        }
        if (value instanceof Double)
        {
            return BigDecimal.valueOf((Double) value);
        }
        if (value instanceof Integer)
        {
            return new BigDecimal((Integer) value);
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return new BigDecimal(valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 轉換為BigDecimal<br>
     * 如果給定的值為空，或者轉換失敗，返回預設值<br>
     * 轉換失敗不會報錯
     *
     * @param value 被轉換的值
     * @return 結果
     */
    public static BigDecimal toBigDecimal(Object value)
    {
        return toBigDecimal(value, null);
    }

    /**
     * 將物件轉為字串<br>
     * 1、Byte陣列和ByteBuffer會被轉換為對應字串的陣列 2、物件陣列會呼叫Arrays.toString方法
     *
     * @param obj 物件
     * @return 字串
     */
    public static String utf8Str(Object obj)
    {
        return str(obj, CharsetKit.CHARSET_UTF_8);
    }

    /**
     * 將物件轉為字串<br>
     * 1、Byte陣列和ByteBuffer會被轉換為對應字串的陣列 2、物件陣列會呼叫Arrays.toString方法
     *
     * @param obj 物件
     * @param charsetName 字串集
     * @return 字串
     */
    public static String str(Object obj, String charsetName)
    {
        return str(obj, Charset.forName(charsetName));
    }

    /**
     * 將物件轉為字串<br>
     * 1、Byte陣列和ByteBuffer會被轉換為對應字串的陣列 2、物件陣列會呼叫Arrays.toString方法
     *
     * @param obj 物件
     * @param charset 字串集
     * @return 字串
     */
    public static String str(Object obj, Charset charset)
    {
        if (null == obj)
        {
            return null;
        }

        if (obj instanceof String)
        {
            return (String) obj;
        }
        else if (obj instanceof byte[] || obj instanceof Byte[])
        {
            if (obj instanceof byte[])
            {
                return str((byte[]) obj, charset);
            }
            else
            {
                Byte[] bytes = (Byte[]) obj;
                int length = bytes.length;
                byte[] dest = new byte[length];
                for (int i = 0; i < length; i++)
                {
                    dest[i] = bytes[i];
                }
                return str(dest, charset);
            }
        }
        else if (obj instanceof ByteBuffer)
        {
            return str((ByteBuffer) obj, charset);
        }
        return obj.toString();
    }

    /**
     * 將byte陣列轉為字串
     *
     * @param bytes byte陣列
     * @param charset 字串集
     * @return 字串
     */
    public static String str(byte[] bytes, String charset)
    {
        return str(bytes, StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * 解碼位元組碼
     *
     * @param data 字串
     * @param charset 字串集，如果此欄位為空，則解碼的結果取決於平台
     * @return 解碼後的字串
     */
    public static String str(byte[] data, Charset charset)
    {
        if (data == null)
        {
            return null;
        }

        if (null == charset)
        {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 將編碼的byteBuffer數據轉換為字串
     *
     * @param data 數據
     * @param charset 字串集，如果為空使用目前系統字串集
     * @return 字串
     */
    public static String str(ByteBuffer data, String charset)
    {
        if (data == null)
        {
            return null;
        }

        return str(data, Charset.forName(charset));
    }

    /**
     * 將編碼的byteBuffer數據轉換為字串
     *
     * @param data 數據
     * @param charset 字串集，如果為空使用目前系統字串集
     * @return 字串
     */
    public static String str(ByteBuffer data, Charset charset)
    {
        if (null == charset)
        {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    // ----------------------------------------------------------------------- 全角半角轉換
    /**
     * 半角轉全角
     *
     * @param input String.
     * @return 全角字串.
     */
    public static String toSBC(String input)
    {
        return toSBC(input, null);
    }

    /**
     * 半角轉全角
     *
     * @param input String
     * @param notConvertSet 不替換的字串集合
     * @return 全角字串.
     */
    public static String toSBC(String input, Set<Character> notConvertSet)
    {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++)
        {
            if (null != notConvertSet && notConvertSet.contains(c[i]))
            {
                // 跳過不替換的字串
                continue;
            }

            if (c[i] == ' ')
            {
                c[i] = '\u3000';
            }
            else if (c[i] < '\177')
            {
                c[i] = (char) (c[i] + 65248);

            }
        }
        return new String(c);
    }

    /**
     * 全角轉半角
     *
     * @param input String.
     * @return 半角字串
     */
    public static String toDBC(String input)
    {
        return toDBC(input, null);
    }

    /**
     * 替換全角為半角
     *
     * @param text 文字
     * @param notConvertSet 不替換的字串集合
     * @return 替換後的字串
     */
    public static String toDBC(String text, Set<Character> notConvertSet)
    {
        char[] c = text.toCharArray();
        for (int i = 0; i < c.length; i++)
        {
            if (null != notConvertSet && notConvertSet.contains(c[i]))
            {
                // 跳過不替換的字串
                continue;
            }

            if (c[i] == '\u3000')
            {
                c[i] = ' ';
            }
            else if (c[i] > '\uFF00' && c[i] < '\uFF5F')
            {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 數字金額大寫轉換 先寫個完整的然後將如零拾替換成零
     *
     * @param n 數字
     * @return 中文大寫數字
     */
    public static String digitUppercase(double n)
    {
        String[] fraction = { "角", "分" };
        String[] digit = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
        String[][] unit = { { "元", "万", "亿" }, { "", "拾", "佰", "仟" } };

        String head = n < 0 ? "負" : "";
        n = Math.abs(n);

        String s = "";
        for (int i = 0; i < fraction.length; i++)
        {
            // 優化double計算精度丢失問題
            BigDecimal nNum = new BigDecimal(n);
            BigDecimal decimal = new BigDecimal(10);
            BigDecimal scale = nNum.multiply(decimal).setScale(2, RoundingMode.HALF_EVEN);
            double d = scale.doubleValue();
            s += (digit[(int) (Math.floor(d * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if (s.length() < 1)
        {
            s = "整";
        }
        int integerPart = (int) Math.floor(n);

        for (int i = 0; i < unit[0].length && integerPart > 0; i++)
        {
            String p = "";
            for (int j = 0; j < unit[1].length && n > 0; j++)
            {
                p = digit[integerPart % 10] + unit[1][j] + p;
                integerPart = integerPart / 10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }
}
