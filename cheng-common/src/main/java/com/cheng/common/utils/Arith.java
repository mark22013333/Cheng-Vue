package com.cheng.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 精確的浮點數運算
 *
 * @author cheng
 */
public class Arith
{

    /**
     * 預設除法運算精度
     */
    private static final int DEF_DIV_SCALE = 10;

    /** 這個類不能實體化 */
    private Arith()
    {
    }

    /**
     * 提供精確的加法運算。
     * @param v1 被加數
     * @param v2 加數
     * @return 兩個參數的和
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精確的减法運算。
     * @param v1 被减數
     * @param v2 减數
     * @return 兩個參數的差
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精確的乘法運算。
     * @param v1 被乘數
     * @param v2 乘數
     * @return 兩個參數的积
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相對）精確的除法運算，當發生除不尽的情況時，精確到
     * 小數點以後10位，以後的數字四舍五入。
     * @param v1 被除數
     * @param v2 除數
     * @return 兩個參數的商
     */
    public static double div(double v1, double v2)
    {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相對）精確的除法運算。當發生除不尽的情況時，由scale參數指
     * 定精度，以後的數字四舍五入。
     * @param v1 被除數
     * @param v2 除數
     * @param scale 表示表示需要精確到小數點以後幾位。
     * @return 兩個參數的商
     */
    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精確的小數位四舍五入處理。
     * @param v 需要四舍五入的數字
     * @param scale 小數點後保留幾位
     * @return 四舍五入後的結果
     */
    public static double round(double v, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        return b.divide(BigDecimal.ONE, scale, RoundingMode.HALF_UP).doubleValue();
    }
}
