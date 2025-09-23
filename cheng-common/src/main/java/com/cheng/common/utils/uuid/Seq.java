package com.cheng.common.utils.uuid;

import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cheng 序列產生類
 */
public class Seq
{
    // 共用序列類型
    public static final String commSeqType = "COMMON";

    // 上傳序列類型
    public static final String uploadSeqType = "UPLOAD";
    // 機器標識
    private static final String machineCode = "A";
    // 共用介面序列數
    private static AtomicInteger commSeq = new AtomicInteger(1);
    // 上傳介面序列數
    private static AtomicInteger uploadSeq = new AtomicInteger(1);

    /**
     * 取得共用序列號
     * 
     * @return 序列值
     */
    public static String getId()
    {
        return getId(commSeqType);
    }
    
    /**
     * 預設16位序列號 yyMMddHHmmss + 一位機器標識 + 3長度循環遞增字串
     * 
     * @return 序列值
     */
    public static String getId(String type)
    {
        AtomicInteger atomicInt = commSeq;
        if (uploadSeqType.equals(type))
        {
            atomicInt = uploadSeq;
        }
        return getId(atomicInt, 3);
    }

    /**
     * 共用介面序列號 yyMMddHHmmss + 一位機器標識 + length長度循環遞增字串
     *
     * @param atomicInt 序列數
     * @param length 數值長度
     * @return 序列值
     */
    public static String getId(AtomicInteger atomicInt, int length)
    {
        String result = DateUtils.dateTimeNow();
        result += machineCode;
        result += getSeq(atomicInt, length);
        return result;
    }

    /**
     * 序列循環遞增字串[1, 10 的 (length)幂次方), 用0左補齊length位數
     * 
     * @return 序列值
     */
    private synchronized static String getSeq(AtomicInteger atomicInt, int length)
    {
        // 先取值再+1
        int value = atomicInt.getAndIncrement();

        // 如果更新後值>=10 的 (length)幂次方則重置為1
        int maxSeq = (int) Math.pow(10, length);
        if (atomicInt.get() >= maxSeq)
        {
            atomicInt.set(1);
        }
        // 轉字串，用0左補齊
        return StringUtils.padl(value, length);
    }
}
