package com.cheng.quartz.task;

import com.cheng.common.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 定時任務呼叫測試
 *
 * @author cheng
 */
@Component("ryTask")
public class RyTask
{
    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("執行多參方法： 字串類型{}，布林類型{}，長整型{}，浮點型{}，整數類型{}", s, b, l, d, i));
    }

    public void ryParams(String params)
    {
        System.out.println("執行有參數方法：" + params);
    }

    public void ryNoParams()
    {
        System.out.println("執行無參數方法");
    }
}
