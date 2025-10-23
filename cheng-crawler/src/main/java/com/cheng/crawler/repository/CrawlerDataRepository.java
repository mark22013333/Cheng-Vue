package com.cheng.crawler.repository;

import com.cheng.crawler.enums.CrawlerType;

import java.util.List;

/**
 * 爬蟲資料儲存介面（策略模式 + 泛型）
 * 不同類型的爬蟲可以實作此介面來定義自己的資料儲存邏輯
 *
 * @param <T> 資料類型（提供類型安全）
 * @author Cheng
 * @since 2025-10-19
 */
public interface CrawlerDataRepository<T> {
    
    /**
     * 批次儲存爬蟲資料
     *
     * @param dataList    處理後的業務物件列表（類型安全）
     * @param crawlerType 爬蟲類型
     * @return 實際儲存的筆數
     * @throws Exception 儲存失敗時拋出例外
     */
    int batchSave(List<T> dataList, CrawlerType crawlerType) throws Exception;
    
    /**
     * 儲存單筆爬蟲資料
     *
     * @param data        業務物件（類型安全）
     * @param crawlerType 爬蟲類型
     * @return 是否儲存成功
     * @throws Exception 儲存失敗時拋出例外
     */
    boolean save(T data, CrawlerType crawlerType) throws Exception;
}
