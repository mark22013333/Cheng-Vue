package com.cheng.common.utils.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Excel數據格式處理適應器
 *
 * @author cheng
 */
public interface ExcelHandlerAdapter {
    /**
     * 格式化
     *
     * @param value 單元格數據值
     * @param args  excel註解args參數陣列
     * @param cell  單元格物件
     * @param wb    工作簿物件
     * @return 處理後的值
     */
    Object format(Object value, String[] args, Cell cell, Workbook wb);
}
