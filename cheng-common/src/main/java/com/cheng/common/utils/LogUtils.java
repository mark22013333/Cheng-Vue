package com.cheng.common.utils;

/**
 * 處理並記錄日誌文件
 *
 * @author cheng
 */
public class LogUtils
{
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
