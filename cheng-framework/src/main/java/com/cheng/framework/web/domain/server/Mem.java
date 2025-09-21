package com.cheng.framework.web.domain.server;

import com.cheng.common.utils.Arith;

/**
 * 內存相關訊息
 *
 * @author cheng
 */
public class Mem
{
    /**
     * 記憶體總量
     */
    private double total;

    /**
     * 已用記憶體
     */
    private double used;

    /**
     * 剩於記憶體
     */
    private double free;

    public double getTotal()
    {
        return Arith.div(total, (1024 * 1024 * 1024), 2);
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public double getUsed()
    {
        return Arith.div(used, (1024 * 1024 * 1024), 2);
    }

    public void setUsed(long used)
    {
        this.used = used;
    }

    public double getFree()
    {
        return Arith.div(free, (1024 * 1024 * 1024), 2);
    }

    public void setFree(long free)
    {
        this.free = free;
    }

    public double getUsage()
    {
        return Arith.mul(Arith.div(used, total, 4), 100);
    }
}
